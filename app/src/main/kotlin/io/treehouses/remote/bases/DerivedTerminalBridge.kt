package io.treehouses.remote.bases

import android.content.Context
import android.graphics.Color
import android.provider.Settings
import android.text.ClipboardManager
import android.util.Log
import io.treehouses.remote.SSH.Terminal.PatternHolder
import io.treehouses.remote.SSH.Terminal.TerminalView
import java.util.ArrayList

open class DerivedTerminalBridge: BaseTerminalBridge() {

    /**
     * Request a different font size. Will make call to parentChanged() to make
     * sure we resize PTY if needed.
     *
     * @param sizeDp Size of font in dp
     */
    var fontSize: Float
        get() = fontSizeDp
        protected set(sizeDp) {
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