package io.treehouses.remote.bases

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import io.treehouses.remote.ssh.Colors
import io.treehouses.remote.ssh.PromptHelper
import io.treehouses.remote.ssh.Relay
import io.treehouses.remote.ssh.SSH
import io.treehouses.remote.ssh.terminal.TerminalKeyListener
import io.treehouses.remote.ssh.terminal.TerminalManager
import io.treehouses.remote.ssh.terminal.TerminalView
import io.treehouses.remote.ssh.beans.HostBean
import io.treehouses.remote.ssh.beans.SelectionArea
import io.treehouses.remote.ssh.interfaces.BridgeDisconnectedListener
import io.treehouses.remote.ssh.interfaces.FontSizeChangedListener
import io.treehouses.remote.views.terminal.VDUBuffer
import io.treehouses.remote.views.terminal.VDUDisplay
import io.treehouses.remote.views.terminal.vt320
import io.treehouses.remote.utils.logD
import io.treehouses.remote.utils.logE
import java.io.IOException

open class BaseTerminalBridge : VDUDisplay {
    protected var displayDensity = 0f
    protected var systemFontScale = 0f
    var color = Colors.defaults
    var defaultFg = 7
    var defaultBg = 0
    protected var manager: TerminalManager? = null
    var host: HostBean? = null

    /* package */
    var transport: SSH? = null
    lateinit var defaultPaint: Paint
    protected var relay: Relay? = null
    protected var emulation: String? = null
    protected var scrollback: Int = 0
    var bitmap: Bitmap? = null
    override var vDUBuffer: VDUBuffer? = null
    protected var parent: TerminalView? = null
    protected val canvas = Canvas()

    /**
     * @return whether this connection had started and subsequently disconnected
     */
    var isDisconnected = false
        protected set

    /**
     * @return whether the TerminalBridge should close
     */
    var isAwaitingClose = false
        protected set
    protected var forcedSize = false
    protected var columns = 0
    protected var rows = 0

    /**
     * @return
     */
    lateinit var keyHandler: TerminalKeyListener
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
    lateinit var selectionArea: SelectionArea
    var charWidth = -1
    var charHeight = -1
    protected var charTop = -1
    protected var fontSizeDp = -1f
    protected lateinit var fontSizeChangedListeners: MutableList<FontSizeChangedListener>
    protected lateinit var localOutput: MutableList<String>

    /**
     * Flag indicating if we should perform a full-screen redraw during our next
     * rendering pass.
     */
    protected var fullRedraw = false
    @JvmField
    var promptHelper: PromptHelper? = null
    protected var disconnectListener: BridgeDisconnectedListener? = null

    /* (non-Javadoc)
     * @see io.treehouses.remote.Views.terminal.VDUDisplay#setColor(byte, byte, byte, byte)
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

    override fun redraw() {
        if (parent != null) parent!!.postInvalidate()
    }

    // We don't have a scroll bar.
    override fun updateScrollBar() {}

    fun checkBitMap(width: Int, height: Int) {
        // reallocate new bitmap if needed
        var newBitmap = bitmap == null
        if (bitmap != null) newBitmap = bitmap!!.width != width || bitmap!!.height != height
        if (newBitmap) {
            if (bitmap != null) bitmap!!.recycle()
            bitmap = null
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            canvas.setBitmap(bitmap)
        }
    }

    fun propagateConsoleText(rawText: CharArray?, length: Int) {
        if (parent != null) {
            parent!!.propagateConsoleText(rawText, length)
        }
    }

    /**
     * Convenience method for writing text into the underlying terminal buffer.
     * Should never be called once the session is established.
     */
    fun outputLine(output: String) {
        if (transport != null && transport!!.isSessionOpen) {
            logD("Session established, cannot use outputLine! ${IOException("outputLine call traceback")}")
        }
        synchronized(localOutput) {
            for (line in output.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                var line = line
                if (line.isNotEmpty() && line[line.length - 1] == '\r') {
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

    private fun writeText(c: Int, l: Int, addr: Int, currAttr: Long) {
        // write the text string starting at 'c' for 'addr' number of characters
        if (currAttr and VDUBuffer.INVISIBLE == 0L) vDUBuffer!!.charArray!![vDUBuffer!!.windowBase + l]?.let {
            canvas.drawText(it, c,
                    addr, c * charWidth.toFloat(), l * charHeight - charTop.toFloat(),
                    defaultPaint)
        }
    }

    private fun clearDirtyArea(c: Int, l: Int, addr: Int, isWideCharacter: Boolean) {
        // clear this dirty area with background color
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
    }

    private fun setColors(currAttr: Long) : Pair<Int, Int> {
        var fgcolor = defaultFg
        var bgcolor = defaultBg

        val newFg = (currAttr and VDUBuffer.COLOR_FG shr VDUBuffer.COLOR_FG_SHIFT).toInt() - 1
        val newBg = (currAttr and VDUBuffer.COLOR_BG shr VDUBuffer.COLOR_BG_SHIFT).toInt() - 1
        // check if foreground color attribute is set
        if (currAttr and VDUBuffer.COLOR_FG != 0L) fgcolor = newFg
        val fg = if (fgcolor < 8 && currAttr and VDUBuffer.BOLD != 0L) color[fgcolor + 8] else if (fgcolor < 256) color[fgcolor] else -0x1000000 or fgcolor - 256

        // check if background color attribute is set
        if (currAttr and VDUBuffer.COLOR_BG != 0L) bgcolor = newBg
        val bg = if (bgcolor < 256) color[bgcolor] else -0x1000000 or bgcolor - 256
        return Pair(fg, bg)
    }

    private fun setAttributes(c: Int, addr: Int, l: Int, currAttr: Long) : Pair<Int, Boolean> {
        // set underlined attributes if requested
        var newAddr = addr
        defaultPaint.isUnderlineText = currAttr and VDUBuffer.UNDERLINE != 0L
        val isWideCharacter = currAttr and VDUBuffer.FULLWIDTH != 0L
        if (isWideCharacter) newAddr++ else {
            // determine the amount of continuous characters with the same settings and print them all at once
            while (c + newAddr < vDUBuffer!!.columns
                    && vDUBuffer!!.charAttributes!![vDUBuffer!!.windowBase + l][c + newAddr] == currAttr) {
                newAddr++
            }
        }
        return Pair(newAddr, isWideCharacter)
    }

    fun redrawLocal() {
        synchronized(localOutput) {
            (vDUBuffer as vt320?)!!.reset()
            for (line in localOutput) (vDUBuffer as vt320?)!!.putString(line)
        }
    }

    fun forceRedraw(parent: TerminalView) {
        // force full redraw with new buffer size
        fullRedraw = true
        redraw()
        parent.notifyUser(String.format("%d x %d", columns, rows))
    }

    fun walkThroughLines(entireDirty: Boolean) {
        // walk through all lines in the buffer
        for (l in 0 until vDUBuffer!!.rows) {

            // check if this line is dirty and needs to be repainted
            // also check for entire-buffer dirty flags
            if (!entireDirty && !vDUBuffer!!.update[l + 1]) continue

            // reset dirty flag for this line
            vDUBuffer!!.update[l + 1] = false

            walkThroughLine(l)
        }
    }

    private fun walkThroughLine(l: Int) {
        // walk through all characters in this line
        var fg: Int; var bg: Int; var isWideCharacter: Boolean; var c = 0
        while (c < vDUBuffer!!.columns) {
            var addr = 0
            val currAttr = vDUBuffer!!.charAttributes!![vDUBuffer!!.windowBase + l][c]
            run {
                val (newFg, newBg) = setColors(currAttr)
                fg = newFg; bg = newBg
            }

            val (newBg, newFg) = checkAndSwap(currAttr, bg, fg)
            bg = newBg; fg = newFg

            val (newAddr, newIsWideCharacter) = setAttributes(c, addr, l, currAttr)
            addr = newAddr; isWideCharacter = newIsWideCharacter
            // Save the current clip region
            canvas.save()

            defaultPaint.color = bg
            clearDirtyArea(c, l, addr, isWideCharacter)

            defaultPaint.color = fg
            writeText(c, l, addr, currAttr)

            // Restore the previous clip region
            canvas.restore()

            // advance to the next text block with different characteristics
            c += addr - 1
            if (isWideCharacter) c++
            c++
        }
    }

    private fun checkAndSwap(currAttr: Long, bg: Int, fg: Int) : Pair<Int, Int> {
        var newBg = bg
        var newFg = fg
        // support character inversion by swapping background and foreground color
        if (currAttr and VDUBuffer.INVERT != 0L) {
            val swapc = bg
            newBg = fg
            newFg = swapc
        }
        return Pair(newBg, newFg)
    }

    fun strokeBorder(width: Int, height: Int) {
        // Stroke the border of the terminal if the size is being forced;
        val borderX = columns * charWidth + 1
        val borderY = rows * charHeight + 1
        defaultPaint.color = Color.GRAY
        defaultPaint.strokeWidth = 0.0f
        if (width >= borderX) canvas.drawLine(borderX.toFloat(), 0f, borderX.toFloat(), borderY + 1.toFloat(), defaultPaint)
        if (height >= borderY) canvas.drawLine(0f, borderY.toFloat(), borderX + 1.toFloat(), borderY.toFloat(), defaultPaint)
    }

    fun copyCurrentSelection() {
        if (parent != null) {
            parent!!.copyCurrentSelectionToClipboard()
        }
    }

    /**
     *
     */
    fun resetScrollPosition() {
        // if we're in scrollback, scroll to bottom of window on input
        if (vDUBuffer!!.windowBase != vDUBuffer!!.screenBase) vDUBuffer!!.setBaseWindow(vDUBuffer!!.screenBase)
    }

    /**
     * Inject a specific string into this terminal. Used for post-login strings
     * and pasting clipboard.
     */
    fun injectString(string: String?) {
        if (string.isNullOrEmpty()) return
        val injectStringThread = Thread {
            try {
                transport!!.write(string.toByteArray(charset(host!!.encoding)))
            } catch (e: Exception) {
                logE("Couldn't inject string to remote host: $e")
            }
        }
        injectStringThread.name = "InjectString"
        injectStringThread.start()
    }

    companion object {
        const val TAG = "CB.TerminalBridge"
        const val DEFAULT_FONT_SIZE_DP = 10
        const val FONT_SIZE_STEP = 2
    }
}