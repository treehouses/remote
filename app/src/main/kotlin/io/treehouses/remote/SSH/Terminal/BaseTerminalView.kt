package io.treehouses.remote.SSH.Terminal

import android.annotation.TargetApi
import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.preference.PreferenceManager
import android.text.ClipboardManager
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.FrameLayout
import android.widget.Toast
import io.treehouses.remote.SSH.interfaces.FontSizeChangedListener
import io.treehouses.remote.bases.BaseTerminalKeyListener

open class BaseTerminalView(context: Context, bridge: TerminalBridge, pager: TerminalViewPager) : FrameLayout(context), FontSizeChangedListener {

    val bridge: TerminalBridge
    val viewPager: TerminalViewPager
    val prefs: SharedPreferences

    private val clipboard: ClipboardManager
    val paint: Paint
    val cursorPaint: Paint
    val cursorStrokePaint: Paint
    val cursorInversionPaint: Paint
    val cursorMetaInversionPaint: Paint

    // Cursor paints to distinguish modes
    val ctrlCursor: Path
    val altCursor: Path
    val shiftCursor: Path
    val tempSrc: RectF
    val tempDst: RectF
    val scaleMatrix: Matrix
    var notification: Toast? = null
    var lastNotification: String? = null

    val mAccessibilityBuffer: StringBuffer
    var mAccessibilityInitialized = false
    var mAccessibilityActive = true


    @TargetApi(11)
    fun setLayerTypeToSoftware() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }


    override fun onFontSizeChanged(sizeDp: Float) {}


    init {
        setWillNotDraw(false)
        this.bridge = bridge
        viewPager = pager
        mAccessibilityBuffer = StringBuffer()
        layoutParams = LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT)
        isFocusable = true
        isFocusableInTouchMode = true

        // Some things TerminalView uses is unsupported in hardware acceleration
        // so this is using software rendering until we can replace all the
        // instances.
        // See: https://developer.android.com/guide/topics/graphics/hardware-accel.html#unsupported
        val colorFilter = ColorMatrixColorFilter(ColorMatrix(floatArrayOf(-1f, 0f, 0f, 0f, 255f, 0f, -1f, 0f, 0f, 255f, 0f, 0f, -1f, 0f, 255f, 0f, 0f, 0f, 1f, 0f)))
        setLayerTypeToSoftware()
        paint = Paint()
        cursorPaint = Paint()
        cursorPaint.color = bridge.color[bridge.defaultFg]
        cursorPaint.isAntiAlias = true
        cursorInversionPaint = Paint()
        cursorInversionPaint.colorFilter = colorFilter
        cursorInversionPaint.isAntiAlias = true
        cursorMetaInversionPaint = Paint()
        cursorMetaInversionPaint.colorFilter = colorFilter
        cursorMetaInversionPaint.isAntiAlias = true
        cursorStrokePaint = Paint(cursorInversionPaint)
        cursorStrokePaint.strokeWidth = 0.1f
        cursorStrokePaint.style = Paint.Style.STROKE

        /*
         * Set up our cursor indicators on a 1x1 Path object which we can later
         * transform to our character width and height
         */
        // TODO make this into a resource somehow
        shiftCursor = Path()
        shiftCursor.lineTo(0.5f, 0.33f)
        shiftCursor.lineTo(1.0f, 0.0f)
        altCursor = Path()
        altCursor.moveTo(0.0f, 1.0f)
        altCursor.lineTo(0.5f, 0.66f)
        altCursor.lineTo(1.0f, 1.0f)
        ctrlCursor = Path()
        ctrlCursor.moveTo(0.0f, 0.25f)
        ctrlCursor.lineTo(1.0f, 0.5f)
        ctrlCursor.lineTo(0.0f, 0.75f)

        // For creating the transform when the terminal resizes
        tempSrc = RectF()
        tempSrc[0.0f, 0.0f, 1.0f] = 1.0f
        tempDst = RectF()
        scaleMatrix = Matrix()

        // connect our view up to the bridge
        setOnKeyListener(bridge.keyHandler)

        clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        bridge.addFontSizeChangedListener(this)
        onFontSizeChanged(bridge.fontSize)

    }

    fun scaleDecorations(canvas: Canvas, metaState: Int) {
        canvas.concat(scaleMatrix)
        val a = metaState and BaseTerminalKeyListener.OUR_SHIFT_ON != 0
        val b = metaState and BaseTerminalKeyListener.OUR_SHIFT_LOCK != 0
        val c = metaState and BaseTerminalKeyListener.OUR_ALT_ON != 0
        val d = metaState and BaseTerminalKeyListener.OUR_ALT_LOCK != 0
        val e = metaState and BaseTerminalKeyListener.OUR_CTRL_ON != 0
        val f = metaState and BaseTerminalKeyListener.OUR_CTRL_LOCK != 0
        var paint: Paint = cursorInversionPaint
        var cursor: Path = shiftCursor
        if (c || d) cursor = altCursor
        else if (e || f) cursor = ctrlCursor
        if (a || c || e) {
            paint = cursorStrokePaint
            canvas.drawPath(cursor, paint)
        } else if (b || d || f) canvas.drawPath(cursor, paint)
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

}