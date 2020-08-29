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

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix.ScaleToFit
import android.graphics.Paint
import android.graphics.Path
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.RelativeLayout
import android.widget.Toast
import io.treehouses.remote.PreferenceConstants
import io.treehouses.remote.Views.terminal.VDUBuffer
import io.treehouses.remote.Views.terminal.vt320
import io.treehouses.remote.bases.BaseTerminalKeyListener
import io.treehouses.remote.bases.BaseTerminalView
import java.io.IOException
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * User interface [View] for showing a TerminalBridge in an
 * [android.app.Activity]. Handles drawing bitmap updates and passing keystrokes down
 * to terminal.
 *
 * @author jsharkey
 */
class TerminalView(context: Context, bridge: TerminalBridge, pager: TerminalViewPager) : BaseTerminalView(context, bridge, pager) {


    @Volatile
    private var notifications = true

    // Related to Accessibility Features

    private val mAccessibilityLock = arrayOfNulls<Any>(0)
    private var mEventSender: TerminalView.AccessibilityEventSender? = null
    private var mControlCodes: Pattern? = null
    private var mCodeMatcher: Matcher? = null
    private val singleDeadKey = CharArray(1)
    private var terminalTextViewOverlay: TerminalTextViewOverlay?
    private var gestureDetector: GestureDetector?


    fun copyCurrentSelectionToClipboard() {
        terminalTextViewOverlay?.copyCurrentSelectionToClipboard()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gestureDetector != null && gestureDetector!!.onTouchEvent(event)) return true
        return super.onTouchEvent(event)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bridge.parentChanged(this)
        scaleCursors()
    }

    fun propagateConsoleText(rawText: CharArray?, length: Int) {
        if (mAccessibilityActive) {
            synchronized(mAccessibilityLock) { mAccessibilityBuffer.append(rawText, 0, length) }
            if (mAccessibilityInitialized) {
                if (mEventSender != null) {
                    removeCallbacks(mEventSender)
                } else {
                    mEventSender = AccessibilityEventSender()
                }
                postDelayed(mEventSender, TerminalView.ACCESSIBILITY_EVENT_THRESHOLD.toLong())
            }
        }
        (context as Activity).runOnUiThread { terminalTextViewOverlay?.onBufferChanged() }
    }

    override fun onFontSizeChanged(size: Float) {
        scaleCursors()
        (context as Activity).runOnUiThread {
            if (terminalTextViewOverlay != null) {
                terminalTextViewOverlay!!.textSize = size

                // For the TextView to line up with the bitmap text, lineHeight must be equal to
                // the bridge's charHeight. See TextView.getLineHeight(), which has been reversed to
                // derive lineSpacingMultiplier.
                val lineSpacingMultiplier = bridge.charHeight.toFloat() / terminalTextViewOverlay!!.paint.getFontMetricsInt(null)
                terminalTextViewOverlay!!.setLineSpacing(0.0f, lineSpacingMultiplier)
            }
        }
    }

    private fun scaleCursors() {
        // Create a scale matrix to scale our 1x1 representation of the cursor
        tempDst[0.0f, 0.0f, bridge.charWidth.toFloat()] = bridge.charHeight.toFloat()
        scaleMatrix.setRectToRect(tempSrc, tempDst, scaleType)
    }

    public override fun onDraw(canvas: Canvas) {
        if (bridge.bitmap != null) {
            // draw the bitmap
            bridge.onDraw()

            // draw the bridge bitmap if it exists
            canvas.drawBitmap(bridge.bitmap!!, 0f, 0f, paint)

            // also draw cursor if visible
            if (bridge.vDUBuffer!!.isCursorVisible) {
                drawCursor(canvas)
                // Restore previous clip region
                canvas.restore()
            }

            // draw any highlighted area
            if (terminalTextViewOverlay == null && bridge.isSelectingForCopy) {
                drawHighlightedArea(canvas)
            }
        }
    }

    fun drawCursor(canvas: Canvas) {
        var cursorColumn = bridge.vDUBuffer!!.cursorColumn
        val cursorRow = bridge.vDUBuffer!!.cursorRow
        val columns = bridge.vDUBuffer!!.columns
        if (cursorColumn == columns) cursorColumn = columns - 1
        if (cursorColumn < 0 || cursorRow < 0) return
        val currentAttribute = bridge.vDUBuffer!!.getAttributes(
                cursorColumn, cursorRow)
        val onWideCharacter = currentAttribute and VDUBuffer.FULLWIDTH != 0L
        val x = cursorColumn * bridge.charWidth
        val y = ((bridge.vDUBuffer!!.cursorRow
                + bridge.vDUBuffer!!.screenBase - bridge.vDUBuffer!!.windowBase)
                * bridge.charHeight)
        // Save the current clip and translation
        val metaState = saveCanvasInfo(canvas, x, y, onWideCharacter)
        // Make sure we scale our decorations to the correct size.
        scaleDecorations(canvas, metaState)
    }

    fun saveCanvasInfo(canvas: Canvas, x: Int, y: Int, onWideCharacter: Boolean) : Int {
        canvas.save()
        canvas.translate(x.toFloat(), y.toFloat())
        canvas.clipRect(0, 0,
                bridge.charWidth * if (onWideCharacter) 2 else 1,
                bridge.charHeight)
        val metaState = bridge.keyHandler.metaState
        if (y + bridge.charHeight < bridge.bitmap!!.height) {
            val underCursor = Bitmap.createBitmap(bridge.bitmap!!, x, y,
                    bridge.charWidth * if (onWideCharacter) 2 else 1, bridge.charHeight)
            if (metaState == 0) canvas.drawBitmap(underCursor, 0f, 0f, cursorInversionPaint) else canvas.drawBitmap(underCursor, 0f, 0f, cursorMetaInversionPaint)
        } else {
            canvas.drawPaint(cursorPaint)
        }
        val deadKey = bridge.keyHandler.deadKey
        if (deadKey != 0) {
            singleDeadKey[0] = deadKey.toChar()
            canvas.drawText(singleDeadKey, 0, 1, 0f, 0f, cursorStrokePaint)
        }
        return metaState
    }

    fun drawHighlightedArea(canvas: Canvas) {
        val area = bridge.selectionArea
        canvas.save()
        canvas.clipRect(
                area.getLeft() * bridge.charWidth,
                area.getTop() * bridge.charHeight,
                (area.getRight() + 1) * bridge.charWidth,
                (area.getBottom() + 1) * bridge.charHeight
        )
        canvas.drawPaint(cursorPaint)
        canvas.restore()
    }

    fun scaleDecorations(canvas: Canvas, metaState: Int) {
        canvas.concat(scaleMatrix)
        val a = metaState and BaseTerminalKeyListener.OUR_SHIFT_ON != 0
        val b = metaState and BaseTerminalKeyListener.OUR_SHIFT_LOCK != 0
        val c = metaState and BaseTerminalKeyListener.OUR_ALT_ON != 0
        val d = metaState and BaseTerminalKeyListener.OUR_ALT_LOCK != 0
        val e = metaState and BaseTerminalKeyListener.OUR_CTRL_ON != 0
        val f = metaState and BaseTerminalKeyListener.OUR_CTRL_LOCK != 0
        var paint:Paint = cursorInversionPaint
        var cursor:Path = shiftCursor
        if(c || d) cursor = altCursor
        else if(e || f) cursor = ctrlCursor
        if (a || c || e) {
            paint = cursorStrokePaint
            canvas.drawPath(cursor, paint)
        }
        else if(b || d || f) canvas.drawPath(cursor, paint)
    }

    fun notifyUser(message: String) {
        if (!notifications) return
        if (notification != null) {
            // Don't keep telling the user the same thing.
            if (lastNotification != null && lastNotification == message) return
            notification!!.setText(message)
            notification!!.show()
        } else {
            notification = Toast.makeText(context, message, Toast.LENGTH_SHORT)
            notification?.show()
        }
        lastNotification = message
    }

    override fun onCheckIsTextEditor(): Boolean {
        return true
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        outAttrs.imeOptions = outAttrs.imeOptions or (EditorInfo.IME_FLAG_NO_EXTRACT_UI or
                EditorInfo.IME_FLAG_NO_ENTER_ACTION or
                EditorInfo.IME_ACTION_NONE)
        outAttrs.inputType = EditorInfo.TYPE_NULL
        return object : BaseInputConnection(this, false) {
            override fun deleteSurroundingText(leftLength: Int, rightLength: Int): Boolean {
                if (rightLength == 0 && leftLength == 0) {
                    return sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                }
                for (i in 0 until leftLength) {
                    sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                }
                // TODO: forward delete
                return true
            }
        }
    }



    private inner class AccessibilityEventSender : Runnable {
        override fun run() {
            synchronized(mAccessibilityLock) {
                if (mCodeMatcher == null) mCodeMatcher = mControlCodes!!.matcher(mAccessibilityBuffer.toString())
                else mCodeMatcher!!.reset(mAccessibilityBuffer.toString())

                // Strip all control codes out.
                mAccessibilityBuffer.setLength(0)
                while (mCodeMatcher!!.find()) mCodeMatcher!!.appendReplacement(mAccessibilityBuffer, " ")

                // Apply Backspaces using backspace character sequence
                var i = mAccessibilityBuffer.indexOf(BACKSPACE_CODE)
                while (i != -1) {
                    mAccessibilityBuffer.replace(if (i == 0) 0 else i - 1, i + BACKSPACE_CODE.length, "")
                    i = mAccessibilityBuffer.indexOf(BACKSPACE_CODE)
                }
                if (mAccessibilityBuffer.isNotEmpty()) {
                    val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED)
                    event.fromIndex = 0
                    event.addedCount = mAccessibilityBuffer.length
                    event.text.add(mAccessibilityBuffer)
                    sendAccessibilityEventUnchecked(event)
                    mAccessibilityBuffer.setLength(0)
                }
            }
        }
    }

//    private inner class AccessibilityStateTester : AsyncTask<Void?, Void?, Boolean>() {
//        override fun doInBackground(vararg params: Void?): Boolean {
//            /*
//             * Presumably if the accessibility manager is not enabled, we don't
//             * need to send accessibility events.
//             */
//            val accessibility = context
//                    .getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
//            if (!accessibility.isEnabled) {
//                return java.lang.Boolean.FALSE
//            }
//
//            /*
//             * Restrict the set of intents to only accessibility services that
//             * have the category FEEDBACK_SPOKEN (aka, screen readers).
//             */
//            val screenReaderIntent = Intent(SCREENREADER_INTENT_ACTION)
//            screenReaderIntent.addCategory(SCREENREADER_INTENT_CATEGORY)
//            val cr = context.contentResolver
//            val screenReaders = context.packageManager.queryIntentServices(
//                    screenReaderIntent, 0)
//            var foundScreenReader = false
//            for (screenReader in screenReaders) {
//                /*
//                 * All screen readers are expected to implement a content
//                 * provider that responds to:
//                 * content://<nameofpackage>.providers.StatusProvider
//                 */
//                val cursor = cr.query(
//                        Uri.parse("content://" + screenReader.serviceInfo.packageName
//                                + ".providers.StatusProvider"), null, null, null, null)
//                if (cursor != null) {
//                    try {
//                        if (!cursor.moveToFirst()) {
//                            continue
//                        }
//
//                        /*
//                         * These content providers use a special cursor that only has
//                         * one element, an integer that is 1 if the screen reader is
//                         * running.
//                         */
//                        val status = cursor.getInt(0)
//                        if (status == 1) {
//                            foundScreenReader = true
//                            break
//                        }
//                    } finally {
//                        cursor.close()
//                    }
//                }
//            }
//            if (foundScreenReader) {
//                mControlCodes = Pattern.compile(CONTROL_CODE_PATTERN)
//            }
//            return foundScreenReader
//        }
//
//        override fun onPostExecute(result: Boolean) {
//            mAccessibilityActive = result
//            mAccessibilityInitialized = true
//            if (result) {
//                mEventSender = AccessibilityEventSender()
//                postDelayed(mEventSender, ACCESSIBILITY_EVENT_THRESHOLD.toLong())
//            } else {
//                synchronized(mAccessibilityLock) {
//                    mAccessibilityBuffer.setLength(0)
//                    mAccessibilityBuffer.trimToSize()
//                }
//            }
//        }
//    }

    companion object {
        private val scaleType = ScaleToFit.FILL
        private const val BACKSPACE_CODE = "\\x08\\x1b\\[K"
        private const val CONTROL_CODE_PATTERN = "\\x1b\\[K[^m]+[m|:]"
        private const val ACCESSIBILITY_EVENT_THRESHOLD = 1000
        private const val SCREENREADER_INTENT_ACTION = "android.accessibilityservice.AccessibilityService"
        private const val SCREENREADER_INTENT_CATEGORY = "android.accessibilityservice.category.FEEDBACK_SPOKEN"
    }

    init {
        bridge.parentChanged(this)
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            // Only used for pre-Honeycomb devices.
            private val bridge = this@TerminalView.bridge
            private var totalY = 0f

            /**
             * This should only handle scrolling when terminalTextViewOverlay is `null`, but
             * we need to handle the page up/down gesture if it's enabled.
             */
            /**
             * This should only handle scrolling when terminalTextViewOverlay is `null`, but
             * we need to handle the page up/down gesture if it's enabled.
             */
            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                // activate consider if within x tolerance
                val touchSlop = ViewConfiguration.get(this@TerminalView.context).scaledTouchSlop
                if (Math.abs(e1.x - e2.x) < touchSlop * 4) {
                    // estimate how many rows we have scrolled through
                    // accumulate distance that doesn't trigger immediate scroll
                    totalY += distanceY
                    val moved = (totalY / bridge.charHeight).toInt()

                    // Consume as pg up/dn only if towards left third of screen with the gesture
                    // enabled.
                    val pgUpDnGestureEnabled = prefs.getBoolean(PreferenceConstants.PG_UPDN_GESTURE, false)
                    if (pgUpDnGestureEnabled && e2.x <= width / 3) {
                        // otherwise consume as pgup/pgdown for every 5 lines
                        if (moved > 5) consume(vt320.KEY_PAGE_DOWN)
                        else if (moved < -5) consume(vt320.KEY_PAGE_UP)
                        return true
                    }
                }
                return false
            }

            private fun consume(key: Int) {
                (bridge.vDUBuffer as vt320).keyPressed(key, ' ', 0)
                bridge.tryKeyVibrate()
                totalY = 0f
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                viewPager.performClick()
                return super.onSingleTapConfirmed(e)
            }

            override fun onDoubleTap(e: MotionEvent?): Boolean {
                try {
                    bridge.transport?.write(0x09)
                    bridge.tryKeyVibrate()
                }
                catch (e: IOException) {
                    e.printStackTrace()
                    try { bridge.transport?.flush() }
                    catch (ioe: IOException) { bridge.dispatchDisconnect(false) }
                }
                return super.onDoubleTap(e)
            }
        })

        terminalTextViewOverlay = TerminalTextViewOverlay(context, this)
        terminalTextViewOverlay!!.setLayoutParams(
                RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(terminalTextViewOverlay, 0)

        // Once terminalTextViewOverlay is active, allow it to handle key events instead.
        terminalTextViewOverlay!!.setOnKeyListener(bridge.keyHandler)

        // Enable accessibility features if a screen reader is active.
//        AccessibilityStateTester().execute(null as Void?)
    }
}