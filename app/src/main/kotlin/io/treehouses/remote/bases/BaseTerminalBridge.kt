package io.treehouses.remote.bases

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import io.treehouses.remote.SSH.Colors
import io.treehouses.remote.SSH.PromptHelper
import io.treehouses.remote.SSH.Relay
import io.treehouses.remote.SSH.SSH
import io.treehouses.remote.SSH.Terminal.TerminalKeyListener
import io.treehouses.remote.SSH.Terminal.TerminalManager
import io.treehouses.remote.SSH.Terminal.TerminalView
import io.treehouses.remote.SSH.beans.HostBean
import io.treehouses.remote.SSH.beans.SelectionArea
import io.treehouses.remote.SSH.interfaces.BridgeDisconnectedListener
import io.treehouses.remote.SSH.interfaces.FontSizeChangedListener
import io.treehouses.remote.Views.terminal.VDUBuffer
import io.treehouses.remote.Views.terminal.VDUDisplay

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

    protected fun discardBitmap() {
        if (bitmap != null) bitmap!!.recycle()
        bitmap = null
    }

    fun propagateConsoleText(rawText: CharArray?, length: Int) {
        if (parent != null) {
            parent!!.propagateConsoleText(rawText, length)
        }
    }

    companion object {
        const val TAG = "CB.TerminalBridge"
        const val DEFAULT_FONT_SIZE_DP = 10
        const val FONT_SIZE_STEP = 2
    }

}