/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2015 Kenny Root, Jeffrey Sharkey
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

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.ClipboardManager
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.MotionEventCompat
import io.treehouses.remote.Views.terminal.vt320
import kotlin.math.floor

/**
 * Custom TextView [TextView] which is intended to (invisibly) be on top of the TerminalView
 * (@link TerminalView) in order to allow the user to select and copy the text of the bitmap below.
 *
 * @author rhansby
 */
@TargetApi(11)
class TerminalTextViewOverlay(context: Context?, var terminalView: TerminalView) : AppCompatTextView(context) {
    private var currentSelection = ""
    private var selectionActionMode: ActionMode? = null
    private val clipboard: ClipboardManager = getContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private var oldBufferHeight = 0
    private var oldScrollY = -1
    fun refreshTextFromBuffer() {
        val vb = terminalView.bridge.vDUBuffer
        val numRows = vb!!.bufferSize
        val numCols = vb.columns
        oldBufferHeight = numRows
        val buffer = StringBuilder()
        var previousTotalLength = 0
        var r = 0
        while (r < numRows && vb.charArray?.get(r) != null) {
            for (c in 0 until numCols) {
                buffer.append(vb.charArray!![r]!![c])
            }

            // Truncate all the new whitespace without removing the old data.
            while (buffer.length > previousTotalLength && Character.isWhitespace(buffer[buffer.length - 1])) {
                buffer.setLength(buffer.length - 1)
            }

            // Make sure each line ends with a carriage return and then remember the buffer
            // at that length.
            buffer.append('\n')
            previousTotalLength = buffer.length
            r++
        }
        oldScrollY = vb.getBaseWindow() * lineHeight
        text = buffer
    }

    /**
     * If there is a new line in the buffer, add an empty line
     * in this TextView, so that selection seems to move up with the
     * rest of the buffer.
     */
    fun onBufferChanged() {
        val vb = terminalView.bridge.vDUBuffer
        val numRows = vb!!.bufferSize
        val numNewRows = numRows - oldBufferHeight
        if (numNewRows <= 0) return
        val newLines = StringBuilder(numNewRows)
        for (i in 0 until numNewRows) newLines.append('\n')
        oldScrollY = (vb.getBaseWindow() + numNewRows) * lineHeight
        oldBufferHeight = numRows
        append(newLines)
    }

    override fun onPreDraw(): Boolean {
        val superResult = super.onPreDraw()
        if (oldScrollY >= 0) {
            scrollTo(0, oldScrollY)
            oldScrollY = -1
        }
        return superResult
    }

    private fun closeSelectionActionMode() {
        if (selectionActionMode != null) {
            selectionActionMode!!.finish()
            selectionActionMode = null
        }
    }

    fun copyCurrentSelectionToClipboard() {
        if (currentSelection.isNotEmpty()) {
            clipboard.text = currentSelection
        }
        closeSelectionActionMode()
    }

    private fun pasteClipboard() {
        var clip = ""
        if (clipboard.hasText()) clip = clipboard.text.toString()
        terminalView.bridge.injectString(clip)
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        if (selStart >= 0 && selEnd >= 0 && selStart <= selEnd) {
            currentSelection = text.toString().substring(selStart, selEnd)
        }
        super.onSelectionChanged(selStart, selEnd)
    }

    override fun scrollTo(x: Int, y: Int) {
        val lineMultiple = (y * 2 + 1) / (lineHeight * 2)
        val bridge = terminalView.bridge
        bridge.vDUBuffer!!.setBaseWindow(lineMultiple)
        super.scrollTo(0, y)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) refreshTextFromBuffer()
        else if (event.action == MotionEvent.ACTION_UP) super.scrollTo(0, terminalView.bridge.vDUBuffer!!.getBaseWindow() * lineHeight)

        // Mouse input is treated differently:
        if (MotionEventCompat.getSource(event) == InputDevice.SOURCE_MOUSE) {
            if (onMouseEvent(event, terminalView.bridge)) return true
            terminalView.viewPager.setPagingEnabled(true)
        } else if (terminalView.onTouchEvent(event)) return true
        return super.onTouchEvent(event)
    }

    @TargetApi(12)
    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (MotionEventCompat.getSource(event) and InputDevice.SOURCE_CLASS_POINTER != 0) {
            when (event.action) {
                MotionEvent.ACTION_SCROLL -> {
                    // Process scroll wheel movement:
                    val yDistance = MotionEventCompat.getAxisValue(event, MotionEvent.AXIS_VSCROLL)
                    val vtBuffer = terminalView.bridge.vDUBuffer as vt320
                    val mouseReport = vtBuffer.isMouseReportEnabled
                    if (mouseReport) {
                        val row = floor(event.y / terminalView.bridge.charHeight.toDouble()).toInt()
                        val col = floor(event.x / terminalView.bridge.charWidth.toDouble()).toInt()
                        vtBuffer.mouseWheel(
                                yDistance > 0,
                                col,
                                row,
                                event.metaState and KeyEvent.META_CTRL_ON != 0,
                                event.metaState and KeyEvent.META_SHIFT_ON != 0,
                                event.metaState and KeyEvent.META_META_ON != 0)
                        return true
                    }
                }
            }
        }
        return super.onGenericMotionEvent(event)
    }

    /**
     * @param event
     * @param bridge
     * @return True if the event is handled.
     */
    @TargetApi(14)
    private fun onMouseEvent(event: MotionEvent, bridge: TerminalBridge): Boolean {
        val row = Math.floor(event.y / bridge.charHeight.toDouble()).toInt()
        val col = Math.floor(event.x / bridge.charWidth.toDouble()).toInt()
        val meta = event.metaState
        val shiftOn = meta and KeyEvent.META_SHIFT_ON != 0
        val vtBuffer = bridge.vDUBuffer as vt320
        val mouseReport = vtBuffer.isMouseReportEnabled

        // MouseReport can be "defeated" using the shift key.
        if (!mouseReport || shiftOn) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (event.buttonState == MotionEvent.BUTTON_TERTIARY) {
                    // Middle click pastes.
                    pasteClipboard()
                }

                // Begin "selection mode"
                closeSelectionActionMode()
            } else if (event.action == MotionEvent.ACTION_MOVE) {
                // In the middle of selection.
                if (selectionActionMode == null) {
                    selectionActionMode = startActionMode(TextSelectionActionModeCallback())
                }
                var selectionStart = selectionStart
                var selectionEnd = selectionEnd
                if (selectionStart > selectionEnd) {
                    val tempStart = selectionStart
                    selectionStart = selectionEnd
                    selectionEnd = tempStart
                }
                currentSelection = text.toString().substring(selectionStart, selectionEnd)
                return false
            }
        } else if (event.action == MotionEvent.ACTION_DOWN) {
            terminalView.viewPager.setPagingEnabled(false)
            vtBuffer.mousePressed(
                    col, row, mouseEventToJavaModifiers(event))
        } else if (event.action == MotionEvent.ACTION_UP) {
            terminalView.viewPager.setPagingEnabled(true)
            vtBuffer.mouseReleased(col, row)
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            val buttonState = event.buttonState
            val button = if (buttonState and MotionEvent.BUTTON_PRIMARY != 0) 0 else if (buttonState and MotionEvent.BUTTON_SECONDARY != 0) 1 else if (buttonState and MotionEvent.BUTTON_TERTIARY != 0) 2 else 3
            vtBuffer.mouseMoved(
                    button,
                    col,
                    row,
                    meta and KeyEvent.META_CTRL_ON != 0,
                    meta and KeyEvent.META_SHIFT_ON != 0,
                    meta and KeyEvent.META_META_ON != 0)
        }
        return true
    }

    override fun onCheckIsTextEditor(): Boolean {
        // This prevents a cursor being displayed within the text.
        return false
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        return terminalView.onCreateInputConnection(outAttrs)
    }

    private inner class TextSelectionActionModeCallback : ActionMode.Callback {
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            selectionActionMode = mode
            menu.clear()
            addToMenu(menu, COPY, 0, "Copy") //					.setIcon(R.drawable.ic_action_copy)
            addToMenu(menu, PASTE, 1, "Paste") //					.setIcon(R.drawable.ic_action_paste)
            return true
        }

        private fun addToMenu(menu: Menu, itemId: Int, order: Int, title: String) {
            menu.add(0, itemId, order, title)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT or MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                COPY -> {
                    copyCurrentSelectionToClipboard()
                    return true
                }
                PASTE -> {
                    pasteClipboard()
                    mode.finish()
                    return true
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {}

        private val COPY = 0
        private val PASTE = 1

    }

    companion object {
        /**
         * Takes an android mouse event and produces a Java InputEvent modifiers int which can be
         * passed to vt320.
         *
         * @param mouseEvent The [MotionEvent] which should be a mouse click or release.
         * @return A Java InputEvent modifier int. See
         * http://docs.oracle.com/javase/7/docs/api/java/awt/event/InputEvent.html
         */
        @TargetApi(14)
        private fun mouseEventToJavaModifiers(mouseEvent: MotionEvent): Int {
            if (MotionEventCompat.getSource(mouseEvent) != InputDevice.SOURCE_MOUSE) return 0
            var mods = 0

            // See http://docs.oracle.com/javase/7/docs/api/constant-values.html
            val buttonState = mouseEvent.buttonState
            if (buttonState and MotionEvent.BUTTON_PRIMARY != 0) mods = mods or 16
            if (buttonState and MotionEvent.BUTTON_SECONDARY != 0) mods = mods or 8
            if (buttonState and MotionEvent.BUTTON_TERTIARY != 0) mods = mods or 4

            // Note: Meta and Ctrl are intentionally swapped here to keep logic in vt320 simple.
            val meta = mouseEvent.metaState
            if (meta and KeyEvent.META_META_ON != 0) mods = mods or 2
            if (meta and KeyEvent.META_SHIFT_ON != 0) mods = mods or 1
            if (meta and KeyEvent.META_CTRL_ON != 0) mods = mods or 4
            return mods
        }
    }

    init {
        setTextColor(Color.TRANSPARENT)
        typeface = Typeface.MONOSPACE
        setTextIsSelectable(true)
        customSelectionActionModeCallback = TextSelectionActionModeCallback()
    }
}