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
import io.treehouses.remote.Views.terminal.VDUBuffer
import io.treehouses.remote.Views.terminal.VDUDisplay
import io.treehouses.remote.Views.terminal.vt320
import io.treehouses.remote.R
import io.treehouses.remote.SSH.Colors
import io.treehouses.remote.SSH.PromptHelper
import io.treehouses.remote.SSH.Relay
import io.treehouses.remote.SSH.SSH
import io.treehouses.remote.SSH.beans.HostBean
import io.treehouses.remote.SSH.beans.SelectionArea
import io.treehouses.remote.SSH.interfaces.BridgeDisconnectedListener
import io.treehouses.remote.SSH.interfaces.FontSizeChangedListener
import io.treehouses.remote.bases.BaseTerminalBridge
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
class TerminalBridge : BaseTerminalBridge {

    /**
     * Create a new terminal bridge suitable for unit testing.
     */
    constructor(): super() {
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
    constructor(manager: TerminalManager, host: HostBean): super() {
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
//    val charset: Charset?
//        get() = relay!!.charset

    /**
     * Sets the encoding used by the terminal. If the connection is live,
     * then the character set is changed for the next read.
     *
     * @param encoding the canonical name of the character encoding
     */
//    fun setCharset(encoding: String?) {
//        if (relay != null) relay!!.setCharset(encoding!!)
//        keyHandler.setCharset(encoding!!)
//    }

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
            startDisconnectPromptThread()
        }
    }

    fun startDisconnectPromptThread() {
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
//    fun removeFontSizeChangedListener(listener: FontSizeChangedListener?) {
//        fontSizeChangedListeners.remove(listener)
//    }

    /**
     * Something changed in our parent [TerminalView], maybe it's a new
     * parent, or maybe it's an updated font size. We should recalculate
     * terminal size information and request a PTY resize.
     */
    @Synchronized
    fun parentChanged(parent: TerminalView) {
        if (manager != null && !manager!!.isResizeAllowed) {
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
        if (!forcedSize && checkDimensions(width, height)) return

        checkBitMap(width, height)

        // clear out any old buffer information
        defaultPaint.color = Color.BLACK
        canvas.drawPaint(defaultPaint)

        if (forcedSize) strokeBorder(width, height)

        requestResize(width, height)

        // redraw local output if we don't have a sesson to receive our resize request
        if (transport == null) redrawLocal()

        forceRedraw(parent)
    }

    fun checkDimensions(width: Int, height: Int) : Boolean {
        // recalculate buffer size
        val newColumns: Int
        val newRows: Int
        newColumns = width / charWidth
        newRows = height / charHeight

        // If nothing has changed in the terminal dimensions and not an intial
        // draw then don't blow away scroll regions and such.
        if (newColumns == columns && newRows == rows) return true
        columns = newColumns
        rows = newRows
        refreshOverlayFontSize()
        return false
    }

    fun requestResize(width: Int, height: Int) {
        try {
            // request a terminal pty resize
            synchronized(vDUBuffer!!) { vDUBuffer!!.setScreenSize(columns, rows, true) }
            if (transport != null) transport!!.setDimensions(columns, rows, width, height)
        } catch (e: Exception) {
            Log.e(TAG, "Problem while trying to resize screen or PTY", e)
        }
    }

    /**
     * Somehow our parent [TerminalView] was destroyed. Now we don't need
     * to redraw anywhere, and we can recycle our internal bitmap.
     */
//    @Synchronized
//    fun parentDestroyed() {
//        parent = null
//        discardBitmap()
//    }

    fun onDraw() {
        synchronized(vDUBuffer!!) {
            val entireDirty = vDUBuffer!!.update[0] || fullRedraw
            walkThroughLines(entireDirty)
            // reset entire-buffer flags
            vDUBuffer!!.update[0] = false
        }
        fullRedraw = false
    }

    /**
     * Resize terminal to fit [rows]x[cols] in screen of size [width]x[height]
     *
     * @param rows   desired number of text rows
     * @param cols   desired numbor of text colums
     * @param width  width of screen in pixels
     * @param height height of screen in pixels
     */
//    @Synchronized
//    fun resizeComputed(cols: Int, rows: Int, width: Int, height: Int) {
//        var sizeDp = 8.0f
//        var step = 8.0f
//        val limit = 0.125f
//        var direction: Int
//        while (fontSizeCompare(sizeDp, cols, rows, width, height).also { direction = it } < 0) sizeDp += step
//        if (direction == 0) return
//        step /= 2.0f
//        sizeDp -= step
//        while (fontSizeCompare(sizeDp, cols, rows, width, height).also { direction = it } != 0 && step >= limit) {
//            step /= 2.0f
//            if (direction > 0) sizeDp -= step
//            else sizeDp += step
//        }
//        if (direction > 0) sizeDp -= step
//        columns = cols
//        this.rows = rows
//        fontSize = sizeDp
//        forcedSize = true
//    }

//    private fun fontSizeCompare(sizeDp: Float, cols: Int, rows: Int, width: Int, height: Int): Int {
//        // read new metrics to get exact pixel dimensions
//        defaultPaint.setTextSize((sizeDp * displayDensity * systemFontScale + 0.5f))
//        val fm = defaultPaint.fontMetrics
//        val widths = FloatArray(1)
//        defaultPaint.getTextWidths("X", widths)
//        val termWidth = widths[0].toInt() * cols
//        val termHeight = Math.ceil(fm.descent - fm.top.toDouble()).toInt() * rows
//        Log.d("fontsize", String.format("font size %fdp resulted in %d x %d", sizeDp, termWidth, termHeight))
//
//        // Check to see if it fits in resolution specified.
//        if (termWidth > width || termHeight > height) return 1
//        return if (termWidth == width || termHeight == height) 0 else -1
//    }

    fun refreshOverlayFontSize() {
        val newDensity = manager!!.resources.displayMetrics.density
        val newFontScale = Settings.System.getFloat(manager!!.contentResolver,
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

}