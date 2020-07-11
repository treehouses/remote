/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2007 Kenny Root, Jeffrey Sharkey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.treehouses.remote.SSH.Terminal

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.ClipboardManager
import android.util.Log
import de.mud.terminal.VDUBuffer
import de.mud.terminal.VDUDisplay
import de.mud.terminal.vt320
import io.treehouses.remote.R
import io.treehouses.remote.SSH.Colors
import io.treehouses.remote.SSH.PromptHelper
import io.treehouses.remote.SSH.Relay
import io.treehouses.remote.SSH.SSH
import io.treehouses.remote.SSH.beans.HostBean
import io.treehouses.remote.SSH.beans.SelectionArea
import io.treehouses.remote.SSH.interfaces.BridgeDisconnectedListener
import io.treehouses.remote.SSH.interfaces.FontSizeChangedListener
import java.io.IOException
import java.nio.charset.Charset
import java.util.*
import java.util.regex.Pattern

/**
 * Provides a bridge between a MUD terminal buffer and a possible TerminalView.
 * This separation allows us to keep the TerminalBridge running in a background
 * service. A TerminalView shares down a bitmap that we can use for rendering
 * when available.
 *
 *
 * This class also provides SSH hostkey verification prompting, and password
 * prompting.
 */
// for ClipboardManager
class TerminalBridge : VDUDisplay {
    private var displayDensity = 0f
    private var systemFontScale = 0f
    var color = Colors.defaults
    var defaultFg = 7
    var defaultBg = 0
    protected val manager: TerminalManager?
    var host: HostBean? = null

    /* package */
    var transport: SSH? = null
    val defaultPaint: Paint
    private var relay: Relay? = null
    private val emulation: String?
    private val scrollback: Int
    var bitmap: Bitmap? = null
    override var vDUBuffer: VDUBuffer? = null
    private var parent: TerminalView? = null
    private val canvas = Canvas()

    /**
     * @return whether this connection had started and subsequently disconnected
     */
    var isDisconnected = false
        private set

    /**
     * @return whether the TerminalBridge should close
     */
    var isAwaitingClose = false
        private set
    private var forcedSize = false
    private var columns = 0
    private var rows = 0

    /**
     * @return
     */
    val keyHandler: TerminalKeyListener
    /**
     * Only intended for pre-Honeycomb devices.
     */
    /**
     * Only intended for pre-Honeycomb devices.
     */
    var isSelectingForCopy = false

    /**
     * Only intended for pre-Honeycomb devices.
     */
    val selectionArea: SelectionArea
    var charWidth = -1
    var charHeight = -1
    private var charTop = -1
    private var fontSizeDp = -1f
    private val fontSizeChangedListeners: MutableList<FontSizeChangedListener>
    private val localOutput: MutableList<String>

    /**
     * Flag indicating if we should perform a full-screen redraw during our next
     * rendering pass.
     */
    private var fullRedraw = false
    @JvmField
    var promptHelper: PromptHelper? = null
    private var disconnectListener: BridgeDisconnectedListener? = null

    /**
     * Create a new terminal bridge suitable for unit testing.
     */
    constructor() {
        vDUBuffer = object : vt320() {
            override fun write(b: ByteArray?) {}
            override fun write(b: Int) {}
            public override fun sendTelnetCommand(cmd: Byte) {}
            public override fun setWindowSize(c: Int, r: Int) {}
            override fun debug(s: String?) {}
        }
        emulation = null
        manager = null
        displayDensity = 1f
        defaultPaint = Paint()
        selectionArea = SelectionArea()
        scrollback = 1
        localOutput = ArrayList()
        fontSizeChangedListeners = ArrayList()
        transport = null
        keyHandler = TerminalKeyListener(null, this, vDUBuffer!!, null)
    }

    /**
     * Create new terminal bridge with following parameters. We will immediately
     * launch thread to start SSH connection and handle any hostkey verification
     * and password authentication.
     */
    constructor(manager: TerminalManager, host: HostBean) {
        this.manager = manager
        this.host = host
        emulation = manager.emulation
        scrollback = manager.scrollback

        // create prompt helper to relay password and hostkey requests up to gui
        promptHelper = PromptHelper(this)

        // create our default paint
        defaultPaint = Paint()
        defaultPaint.isAntiAlias = true
        defaultPaint.typeface = Typeface.MONOSPACE
        defaultPaint.isFakeBoldText = true // more readable?
        refreshOverlayFontSize()
        localOutput = ArrayList()
        fontSizeChangedListeners = ArrayList()
        var hostFontSizeDp = host.fontSize
        if (hostFontSizeDp <= 0) {
            hostFontSizeDp = DEFAULT_FONT_SIZE_DP
        }
        fontSize = hostFontSizeDp.toFloat()

        // create terminal buffer and handle outgoing data
        // this is probably status reply information
        vDUBuffer = object : vt320() {
            override fun debug(s: String?) {
                Log.d(TAG, s)
            }

            override fun write(b: ByteArray?) {
                try {
                    if (b != null && transport != null) transport!!.write(b)
                } catch (e: IOException) {
                    Log.e(TAG, "Problem writing outgoing data in vt320() thread", e)
                }
            }

            override fun write(b: Int) {
                try {
                    if (transport != null) transport!!.write(b)
                } catch (e: IOException) {
                    Log.e(TAG, "Problem writing outgoing data in vt320() thread", e)
                }
            }

            // We don't use telnet sequences.
            public override fun sendTelnetCommand(cmd: Byte) {}

            // We don't want remote to resize our window.
            public override fun setWindowSize(c: Int, r: Int) {}
            override fun beep() {
//				if (parent.isShown())
//					manager.playBeep();
//				else
//					manager.sendActivityNotification(host);
            }
        }

        // Don't keep any scrollback if a session is not being opened.
        if (host.wantSession) vDUBuffer?.bufferSize = scrollback else vDUBuffer?.bufferSize = 0
        resetColors()
        vDUBuffer?.display = this
        selectionArea = SelectionArea()
        keyHandler = TerminalKeyListener(manager, this, vDUBuffer!!, host.encoding)
    }

    /**
     * Spawn thread to open connection and start login process.
     */
    fun startConnection() {
        transport = SSH()
        //		if (transport == null) {
//			Log.i(TAG, "No transport found for " + host.getProtocol());
//			return;
//		}
        transport!!.bridge = this
        transport!!.manager = manager
        transport!!.host = host

        // TODO make this more abstract so we don't litter on AbsTransport
        transport!!.setCompression(host!!.compression)
        transport!!.setUseAuthAgent(host!!.useAuthAgent)
        transport!!.emulation = emulation

//		if (transport.canForwardPorts()) {
//			for (PortForwardBean portForward : manager.hostdb.getPortForwardsForHost(host))
//				transport.addPortForward(portForward);
//		}
        outputLine(manager!!.res!!.getString(R.string.terminal_connecting, host!!.hostname, host!!.port, host!!.protocol))
        val connectionThread = Thread(Runnable { transport!!.connect() })
        connectionThread.name = "Connection"
        connectionThread.isDaemon = true
        connectionThread.start()
    }

    /**
     * @return charset in use by bridge
     */
    val charset: Charset?
        get() = relay!!.charset

    /**
     * Sets the encoding used by the terminal. If the connection is live,
     * then the character set is changed for the next read.
     *
     * @param encoding the canonical name of the character encoding
     */
    fun setCharset(encoding: String?) {
        if (relay != null) relay!!.setCharset(encoding!!)
        keyHandler.setCharset(encoding!!)
    }

    /**
     * Convenience method for writing text into the underlying terminal buffer.
     * Should never be called once the session is established.
     */
    fun outputLine(output: String) {
        if (transport != null && transport!!.isSessionOpen) {
            Log.e(TAG, "Session established, cannot use outputLine!",
                    IOException("outputLine call traceback"))
        }
        synchronized(localOutput) {
            for (line in output.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                var line = line
                if (line.length > 0 && line[line.length - 1] == '\r') {
                    line = line.substring(0, line.length - 1)
                }
                val s = "$line\r\n"
                localOutput.add(s)
                (vDUBuffer as vt320?)!!.putString(s)

                // For accessibility
                val charArray = s.toCharArray()
                propagateConsoleText(charArray, charArray.size)
            }
        }
    }

    fun copyCurrentSelection() {
        if (parent != null) {
            parent!!.copyCurrentSelectionToClipboard()
        }
    }

    /**
     * Inject a specific string into this terminal. Used for post-login strings
     * and pasting clipboard.
     */
    fun injectString(string: String?) {
        if (string == null || string.length == 0) return
        val injectStringThread = Thread(Runnable {
            try {
                transport!!.write(string.toByteArray(charset(host!!.encoding)))
            } catch (e: Exception) {
                Log.e(TAG, "Couldn't inject string to remote host: ", e)
            }
        })
        injectStringThread.name = "InjectString"
        injectStringThread.start()
    }

    /**
     * Internal method to request actual PTY terminal once we've finished
     * authentication. If called before authenticated, it will just fail.
     */
    fun onConnected() {
        isDisconnected = false
        (vDUBuffer as vt320?)!!.reset()

        // We no longer need our local output.
        localOutput.clear()

        // previously tried vt100 and xterm for emulation modes
        // "screen" works the best for color and escape codes
        (vDUBuffer as vt320?)!!.setAnswerBack(emulation!!)

//		if (HostDatabase.DELKEY_BACKSPACE.equals(host.getDelKey()))
//			((vt320) buffer).setBackspace(vt320.DELETE_IS_BACKSPACE);
//		else
        (vDUBuffer as vt320?)!!.setBackspace(vt320.DELETE_IS_DEL)
        Log.e("ENTERED", "HERE3")
        if (isSessionOpen) {
            Log.e("ENTERED", "HERE")
            // create thread to relay incoming connection data to buffer
            relay = Relay(this, transport!!, (vDUBuffer as vt320?)!!, host!!.encoding)
            val relayThread = Thread(relay)
            relayThread.isDaemon = true
            relayThread.name = "Relay"
            relayThread.start()
        }

        // force font-size to make sure we resizePTY as needed
        fontSize = fontSizeDp

        // finally send any post-login string, if requested
        injectString(host!!.postLogin)
    }

    /**
     * @return whether a session is open or not
     */
    val isSessionOpen: Boolean
        get() = if (transport != null) transport!!.isSessionOpen else false

    fun setOnDisconnectedListener(disconnectListener: BridgeDisconnectedListener?) {
        this.disconnectListener = disconnectListener
    }

    /**
     * Force disconnection of this terminal bridge.
     */
    fun dispatchDisconnect(immediate: Boolean) {
        // We don't need to do this multiple times.
        synchronized(this) {
            if (isDisconnected && !immediate) return
            isDisconnected = true
        }

        // Cancel any pending prompts.
        promptHelper!!.cancelPrompt()

        // disconnection request hangs if we havent really connected to a host yet
        // temporary fix is to just spawn disconnection into a thread
        val disconnectThread = Thread(Runnable { if (transport != null && transport!!.isConnected) transport!!.close() })
        disconnectThread.name = "Disconnect"
        disconnectThread.start()
        if (immediate || host!!.quickDisconnect && !host!!.stayConnected) {
            isAwaitingClose = true
            triggerDisconnectListener()
        } else {
            run {
                val line = "Connection Lost"
                (this.vDUBuffer as vt320?)!!.putString("""

    $line

    """.trimIndent())
            }
            if (host!!.stayConnected) {
                manager!!.requestReconnect(this)
                return
            }
            val disconnectPromptThread = Thread(Runnable {
                val result = promptHelper!!.requestBooleanPrompt(null, "Host has been Disconnected. Close session?")
                if (result == null || result) {
                    isAwaitingClose = true
                    triggerDisconnectListener()
                }
            })
            disconnectPromptThread.name = "DisconnectPrompt"
            disconnectPromptThread.isDaemon = true
            disconnectPromptThread.start()
        }
    }

    /**
     * Tells the TerminalManager that we can be destroyed now.
     */
    private fun triggerDisconnectListener() {
        if (disconnectListener != null) {
            // The disconnect listener should be run on the main thread if possible.
            Handler(Looper.getMainLooper()).post { disconnectListener!!.onDisconnected(this@TerminalBridge) }
        }
    }

    @Synchronized
    fun tryKeyVibrate() {
        manager!!.tryKeyVibrate()
    }// read new metrics to get exact pixel dimensions

    // refresh any bitmap with new font size
    //		manager.hostdb.saveHost(host);

    /**
     * Request a different font size. Will make call to parentChanged() to make
     * sure we resize PTY if needed.
     *
     * @param sizeDp Size of font in dp
     */
    var fontSize: Float
        get() = fontSizeDp
        private set(sizeDp) {
            if (sizeDp <= 0.0) {
                return
            }
            val fontSizePx = (sizeDp * displayDensity * systemFontScale + 0.5f).toInt()
            defaultPaint.textSize = fontSizePx.toFloat()
            fontSizeDp = sizeDp

            // read new metrics to get exact pixel dimensions
            val fm = defaultPaint.fontMetrics
            charTop = Math.ceil(fm.top.toDouble()).toInt()
            val widths = FloatArray(1)
            defaultPaint.getTextWidths("X", widths)
            charWidth = Math.ceil(widths[0].toDouble()).toInt()
            charHeight = Math.ceil(fm.descent - fm.top.toDouble()).toInt()

            // refresh any bitmap with new font size
            if (parent != null) {
                parentChanged(parent!!)
            }
            for (ofscl in fontSizeChangedListeners) {
                ofscl.onFontSizeChanged(sizeDp)
            }
            host!!.fontSize = sizeDp.toInt()
            //		manager.hostdb.saveHost(host);
            forcedSize = false
        }

    /**
     * Add an [FontSizeChangedListener] to the list of listeners for this
     * bridge.
     *
     * @param listener listener to add
     */
    fun addFontSizeChangedListener(listener: FontSizeChangedListener) {
        fontSizeChangedListeners.add(listener)
    }

    /**
     * Remove an [FontSizeChangedListener] from the list of listeners for
     * this bridge.
     *
     * @param listener
     */
    fun removeFontSizeChangedListener(listener: FontSizeChangedListener?) {
        fontSizeChangedListeners.remove(listener)
    }

    /**
     * Something changed in our parent [TerminalView], maybe it's a new
     * parent, or maybe it's an updated font size. We should recalculate
     * terminal size information and request a PTY resize.
     */
    @Synchronized
    fun parentChanged(parent: TerminalView) {
        if (manager != null && !manager.isResizeAllowed) {
            Log.d(TAG, "Resize is not allowed now")
            return
        }
        this.parent = parent
        val width = parent.width
        val height = parent.height

        // Something has gone wrong with our layout; we're 0 width or height!
        if (width <= 0 || height <= 0) return
        val clipboard = parent.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        keyHandler.setClipboardManager(clipboard)
        if (!forcedSize) {
            // recalculate buffer size
            val newColumns: Int
            val newRows: Int
            newColumns = width / charWidth
            newRows = height / charHeight

            // If nothing has changed in the terminal dimensions and not an intial
            // draw then don't blow away scroll regions and such.
            if (newColumns == columns && newRows == rows) return
            columns = newColumns
            rows = newRows
            refreshOverlayFontSize()
        }

        // reallocate new bitmap if needed
        var newBitmap = bitmap == null
        if (bitmap != null) newBitmap = bitmap!!.width != width || bitmap!!.height != height
        if (newBitmap) {
            discardBitmap()
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            canvas.setBitmap(bitmap)
        }

        // clear out any old buffer information
        defaultPaint.color = Color.BLACK
        canvas.drawPaint(defaultPaint)

        // Stroke the border of the terminal if the size is being forced;
        if (forcedSize) {
            val borderX = columns * charWidth + 1
            val borderY = rows * charHeight + 1
            defaultPaint.color = Color.GRAY
            defaultPaint.strokeWidth = 0.0f
            if (width >= borderX) canvas.drawLine(borderX.toFloat(), 0f, borderX.toFloat(), borderY + 1.toFloat(), defaultPaint)
            if (height >= borderY) canvas.drawLine(0f, borderY.toFloat(), borderX + 1.toFloat(), borderY.toFloat(), defaultPaint)
        }
        try {
            // request a terminal pty resize
            synchronized(vDUBuffer!!) { vDUBuffer!!.setScreenSize(columns, rows, true) }
            if (transport != null) transport!!.setDimensions(columns, rows, width, height)
        } catch (e: Exception) {
            Log.e(TAG, "Problem while trying to resize screen or PTY", e)
        }

        // redraw local output if we don't have a sesson to receive our resize request
        if (transport == null) {
            synchronized(localOutput) {
                (vDUBuffer as vt320?)!!.reset()
                for (line in localOutput) (vDUBuffer as vt320?)!!.putString(line)
            }
        }

        // force full redraw with new buffer size
        fullRedraw = true
        redraw()
        parent.notifyUser(String.format("%d x %d", columns, rows))
        Log.i(TAG, String.format("parentChanged() now width=%d, height=%d", columns, rows))
    }

    /**
     * Somehow our parent [TerminalView] was destroyed. Now we don't need
     * to redraw anywhere, and we can recycle our internal bitmap.
     */
    @Synchronized
    fun parentDestroyed() {
        parent = null
        discardBitmap()
    }

    private fun discardBitmap() {
        if (bitmap != null) bitmap!!.recycle()
        bitmap = null
    }

    fun propagateConsoleText(rawText: CharArray?, length: Int) {
        if (parent != null) {
            parent!!.propagateConsoleText(rawText, length)
        }
    }

    fun onDraw() {
        var fg: Int
        var bg: Int
        synchronized(vDUBuffer!!) {
            val entireDirty = vDUBuffer!!.update[0] || fullRedraw
            var isWideCharacter = false

            // walk through all lines in the buffer
            for (l in 0 until vDUBuffer!!.rows) {

                // check if this line is dirty and needs to be repainted
                // also check for entire-buffer dirty flags
                if (!entireDirty && !vDUBuffer!!.update[l + 1]) continue

                // reset dirty flag for this line
                vDUBuffer!!.update[l + 1] = false

                // walk through all characters in this line
                var c = 0
                while (c < vDUBuffer!!.columns) {
                    var addr = 0
                    val currAttr = vDUBuffer!!.charAttributes!![vDUBuffer!!.windowBase + l][c]
                    run {
                        var fgcolor = defaultFg
                        var bgcolor = defaultBg

                        // check if foreground color attribute is set
                        if (currAttr and VDUBuffer.COLOR_FG != 0L) fgcolor = (currAttr and VDUBuffer.COLOR_FG shr VDUBuffer.COLOR_FG_SHIFT).toInt() - 1
                        fg = if (fgcolor < 8 && currAttr and VDUBuffer.BOLD != 0L) color[fgcolor + 8] else if (fgcolor < 256) color[fgcolor] else -0x1000000 or fgcolor - 256

                        // check if background color attribute is set
                        if (currAttr and VDUBuffer.COLOR_BG != 0L) bgcolor = (currAttr and VDUBuffer.COLOR_BG shr VDUBuffer.COLOR_BG_SHIFT).toInt() - 1
                        bg = if (bgcolor < 256) color[bgcolor] else -0x1000000 or bgcolor - 256
                    }

                    // support character inversion by swapping background and foreground color
                    if (currAttr and VDUBuffer.INVERT != 0L) {
                        val swapc = bg
                        bg = fg
                        fg = swapc
                    }

                    // set underlined attributes if requested
                    defaultPaint.isUnderlineText = currAttr and VDUBuffer.UNDERLINE != 0L
                    isWideCharacter = currAttr and VDUBuffer.FULLWIDTH != 0L
                    if (isWideCharacter) addr++ else {
                        // determine the amount of continuous characters with the same settings and print them all at once
                        while (c + addr < vDUBuffer!!.columns
                                && vDUBuffer!!.charAttributes!![vDUBuffer!!.windowBase + l][c + addr] == currAttr) {
                            addr++
                        }
                    }

                    // Save the current clip region
                    canvas.save()

                    // clear this dirty area with background color
                    defaultPaint.color = bg
                    if (isWideCharacter) {
                        canvas.clipRect(c * charWidth,
                                l * charHeight,
                                (c + 2) * charWidth,
                                (l + 1) * charHeight)
                    } else {
                        canvas.clipRect(c * charWidth,
                                l * charHeight,
                                (c + addr) * charWidth,
                                (l + 1) * charHeight)
                    }
                    canvas.drawPaint(defaultPaint)

                    // write the text string starting at 'c' for 'addr' number of characters
                    defaultPaint.color = fg
                    if (currAttr and VDUBuffer.INVISIBLE == 0L) vDUBuffer!!.charArray!![vDUBuffer!!.windowBase + l]?.let {
                        canvas.drawText(it, c,
                                addr, c * charWidth.toFloat(), l * charHeight - charTop.toFloat(),
                                defaultPaint)
                    }

                    // Restore the previous clip region
                    canvas.restore()

                    // advance to the next text block with different characteristics
                    c += addr - 1
                    if (isWideCharacter) c++
                    c++
                }
            }

            // reset entire-buffer flags
            vDUBuffer!!.update[0] = false
        }
        fullRedraw = false
    }

    override fun redraw() {
        if (parent != null) parent!!.postInvalidate()
    }

    // We don't have a scroll bar.
    override fun updateScrollBar() {}

    /**
     * Resize terminal to fit [rows]x[cols] in screen of size [width]x[height]
     *
     * @param rows   desired number of text rows
     * @param cols   desired numbor of text colums
     * @param width  width of screen in pixels
     * @param height height of screen in pixels
     */
    @Synchronized
    fun resizeComputed(cols: Int, rows: Int, width: Int, height: Int) {
        var sizeDp = 8.0f
        var step = 8.0f
        val limit = 0.125f
        var direction: Int
        while (fontSizeCompare(sizeDp, cols, rows, width, height).also { direction = it } < 0) sizeDp += step
        if (direction == 0) return
        step /= 2.0f
        sizeDp -= step
        while (fontSizeCompare(sizeDp, cols, rows, width, height).also { direction = it } != 0 && step >= limit) {
            step /= 2.0f
            if (direction > 0) sizeDp -= step
            else sizeDp += step
        }
        if (direction > 0) sizeDp -= step
        columns = cols
        this.rows = rows
        fontSize = sizeDp
        forcedSize = true
    }

    private fun fontSizeCompare(sizeDp: Float, cols: Int, rows: Int, width: Int, height: Int): Int {
        // read new metrics to get exact pixel dimensions
        defaultPaint.setTextSize((sizeDp * displayDensity * systemFontScale + 0.5f))
        val fm = defaultPaint.fontMetrics
        val widths = FloatArray(1)
        defaultPaint.getTextWidths("X", widths)
        val termWidth = widths[0].toInt() * cols
        val termHeight = Math.ceil(fm.descent - fm.top.toDouble()).toInt() * rows
        Log.d("fontsize", String.format("font size %fdp resulted in %d x %d", sizeDp, termWidth, termHeight))

        // Check to see if it fits in resolution specified.
        if (termWidth > width || termHeight > height) return 1
        return if (termWidth == width || termHeight == height) 0 else -1
    }

    fun refreshOverlayFontSize() {
        val newDensity = manager!!.resources.displayMetrics.density
        val newFontScale = Settings.System.getFloat(manager.contentResolver,
                Settings.System.FONT_SCALE, 1.0f)
        if (newDensity != displayDensity || newFontScale != systemFontScale) {
            displayDensity = newDensity
            systemFontScale = newFontScale
            defaultPaint.setTextSize((fontSizeDp * displayDensity * systemFontScale + 0.5f))
            fontSize = fontSizeDp
        }
    }
    //	/**
    //	 * @return whether underlying transport can forward ports
    //	 */
    //	public boolean canFowardPorts() {
    //		return transport.canForwardPorts();
    //	}
    //
    //	/**
    //	 * Adds the {@link PortForwardBean} to the list.
    //	 * @param portForward the port forward bean to add
    //	 * @return true on successful addition
    //	 */
    //	public boolean addPortForward(PortForwardBean portForward) {
    //		return transport.addPortForward(portForward);
    //	}
    //
    //	/**
    //	 * Removes the {@link PortForwardBean} from the list.
    //	 * @param portForward the port forward bean to remove
    //	 * @return true on successful removal
    //	 */
    //	public boolean removePortForward(PortForwardBean portForward) {
    //		return transport.removePortForward(portForward);
    //	}
    //
    //	/**
    //	 * @return the list of port forwards
    //	 */
    //	public List<PortForwardBean> getPortForwards() {
    //		return transport.getPortForwards();
    //	}
    //
    //	/**
    //	 * Enables a port forward member. After calling this method, the port forward should
    //	 * be operational.
    //	 * @param portForward member of our current port forwards list to enable
    //	 * @return true on successful port forward setup
    //	 */
    //	public boolean enablePortForward(PortForwardBean portForward) {
    //		if (!transport.isConnected()) {
    //			Log.i(TAG, "Attempt to enable port forward while not connected");
    //			return false;
    //		}
    //
    //		return transport.enablePortForward(portForward);
    //	}
    //
    //	/**
    //	 * Disables a port forward member. After calling this method, the port forward should
    //	 * be non-functioning.
    //	 * @param portForward member of our current port forwards list to enable
    //	 * @return true on successful port forward tear-down
    //	 */
    //	public boolean disablePortForward(PortForwardBean portForward) {
    //		if (!transport.isConnected()) {
    //			Log.i(TAG, "Attempt to disable port forward while not connected");
    //			return false;
    //		}
    //
    //		return transport.disablePortForward(portForward);
    //	}

    /* (non-Javadoc)
     * @see de.mud.terminal.VDUDisplay#setColor(byte, byte, byte, byte)
     */
    override fun setColor(index: Int, red: Int, green: Int, blue: Int) {
        // Don't allow the system colors to be overwritten for now. May violate specs.
        if (index < color.size && index >= 16) color[index] = -0x1000000 or (red shl 16) or (green shl 8) or blue
    }

    override fun resetColors() {
//		int[] defaults = manager.colordb.getDefaultColorsForScheme(HostDatabase.DEFAULT_COLOR_SCHEME);
//		defaultFg = Colors.defaults[0];
//		defaultBg = defaults[1];

//		color = manager.colordb.getColorsForScheme(HostDatabase.DEFAULT_COLOR_SCHEME);
    }

    private object PatternHolder {
        var urlPattern: Pattern? = null

        init {
            // based on http://www.ietf.org/rfc/rfc2396.txt
            val scheme = "[A-Za-z][-+.0-9A-Za-z]*"
            val unreserved = "[-._~0-9A-Za-z]"
            val pctEncoded = "%[0-9A-Fa-f]{2}"
            val subDelims = "[!$&'()*+,;:=]"
            val userinfo = "(?:$unreserved|$pctEncoded|$subDelims|:)*"
            val h16 = "[0-9A-Fa-f]{1,4}"
            val decOctet = "(?:[0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])"
            val ipv4address: String = "$decOctet\\.$decOctet\\.$decOctet\\.$decOctet"
            val ls32 = "(?:$h16:$h16|$ipv4address)"
            val ipv6address = "(?:(?:$h16){6}$ls32)"
            val ipvfuture = "v[0-9A-Fa-f]+.(?:$unreserved|$subDelims|:)+"
            val ipLiteral = "\\[(?:$ipv6address|$ipvfuture)\\]"
            val regName = "(?:$unreserved|$pctEncoded|$subDelims)*"
            val host = "(?:$ipLiteral|$ipv4address|$regName)"
            val port = "[0-9]*"
            val authority = "(?:$userinfo@)?$host(?::$port)?"
            val pchar = "(?:$unreserved|$pctEncoded|$subDelims|@)"
            val segment: String = "$pchar*"
            val pathAbempty = "(?:/$segment)*"
            val segmentNz: String = "$pchar+"
            val pathAbsolute = "/(?:$segmentNz(?:/$segment)*)?"
            val pathRootless: String = "$segmentNz(?:/$segment)*"
            val hierPart = "(?://$authority$pathAbempty|$pathAbsolute|$pathRootless)"
            val query = "(?:$pchar|/|\\?)*"
            val fragment = "(?:$pchar|/|\\?)*"
            val uriRegex: String = "$scheme:$hierPart(?:$query)?(?:#$fragment)?"
            urlPattern = Pattern.compile(uriRegex)
        }
    }

    /**
     * @return
     */
    fun scanForURLs(): List<String> {
        val urls: MutableList<String> = ArrayList()
        val visibleBuffer = CharArray(vDUBuffer!!.rows * vDUBuffer!!.columns)
        for (l in 0 until vDUBuffer!!.rows) System.arraycopy(vDUBuffer!!.charArray!![vDUBuffer!!.windowBase + l]!!, 0,
                visibleBuffer, l * vDUBuffer!!.columns, vDUBuffer!!.columns)
        val urlMatcher = PatternHolder.urlPattern!!.matcher(String(visibleBuffer))
        while (urlMatcher.find()) urls.add(urlMatcher.group())
        return urls
    }

    /**
     * @return
     */
    val isUsingNetwork: Boolean
        get() = transport!!.usesNetwork()

    /**
     *
     */
    fun resetScrollPosition() {
        // if we're in scrollback, scroll to bottom of window on input
        if (vDUBuffer!!.windowBase != vDUBuffer!!.screenBase) vDUBuffer!!.setBaseWindow(vDUBuffer!!.screenBase)
    }

    /**
     * Convenience function to increase the font size by a given step.
     */
    fun increaseFontSize() {
        fontSize = fontSizeDp + FONT_SIZE_STEP
    }

    /**
     * Convenience function to decrease the font size by a given step.
     */
    fun decreaseFontSize() {
        fontSize = fontSizeDp - FONT_SIZE_STEP
    }

    companion object {
        const val TAG = "CB.TerminalBridge"
        private const val DEFAULT_FONT_SIZE_DP = 10
        private const val FONT_SIZE_STEP = 2
    }
}