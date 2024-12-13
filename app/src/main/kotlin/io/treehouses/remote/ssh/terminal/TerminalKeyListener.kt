package io.treehouses.remote.ssh.terminal

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.res.Configuration
import android.view.KeyEvent
import android.view.View
import io.treehouses.remote.PreferenceConstants
import io.treehouses.remote.views.terminal.VDUBuffer
import io.treehouses.remote.views.terminal.vt320
import io.treehouses.remote.bases.BaseTerminalKeyListener
import java.io.IOException

class TerminalKeyListener(tm: TerminalManager?,
                          tb: TerminalBridge,
                          var b: VDUBuffer,
                          var e: String?) : BaseTerminalKeyListener(tm, tb, b, e), View.OnKeyListener, OnSharedPreferenceChangeListener {

    private fun specialKeys(keyCode: Int, left: Boolean, right: Boolean): Boolean {
        // Ignore all key-up events except for the special keys

        val altRight = keyCode == KeyEvent.KEYCODE_ALT_RIGHT
        val altLeft = keyCode == KeyEvent.KEYCODE_ALT_LEFT
        val shiftRight = keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT
        val shiftLeft = keyCode == KeyEvent.KEYCODE_SHIFT_LEFT

        return if ((right && altRight || left && altLeft) && metaState and OUR_SLASH != 0) {
            metaState = metaState and OUR_TRANSIENT.inv()
            bridge.transport!!.write('/'.code)
            true
        } else if ((right && shiftRight || left && shiftLeft) && metaState and OUR_TAB != 0) {
            metaState = metaState and OUR_TRANSIENT.inv()
            bridge.transport!!.write(0x09)
            true
        } else false
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?,
                                           key: String?) {
        if (PreferenceConstants.KEYMODE == key || PreferenceConstants.SHIFT_FKEYS == key || PreferenceConstants.CTRL_FKEYS == key || PreferenceConstants.VOLUME_FONT == key || PreferenceConstants.STICKY_MODIFIERS == key) {
            updatePrefs()
        }
    }

    /**
     * Handle onKey() events coming down from a [TerminalView] above us.
     * Modify the keys to make more sense to a host then pass it to the transport.
     */
    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        try {
            // skip keys if we aren't connected yet or have been disconnected
            if (bridge.isDisconnected || bridge.transport == null) return false
            checkOptions()
            if (event.action == KeyEvent.ACTION_UP) return specialKeys(keyCode, leftModifiersAreSlashAndTab, rightModifiersAreSlashAndTab)

            //Log.i("CBKeyDebug", KeyEventUtil.describeKeyEvent(keyCode, event));
            bridge.resetScrollPosition()
            getUnicode(event)

            // Handle potentially multi-character IME input.
            var handleInput = hasMultiCharInput(event, keyCode) || flagRaised(event, keyCode) || needReturn(interpretAsHardKeyboard, keyCode, event)
            handleInput = handleInput || hasShift(keyCode) || hasNonCtrlChar() || hasKeyCode(keyCode)
            if (handleInput) return true
        } catch (e: IOException) {
            handleProblem(e)
        } catch (npe: NullPointerException) {
            npe.printStackTrace()
        }
        return false
    }

    private fun hasKeyCode(keyCode: Int): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_ESCAPE -> sendEscape()
            KeyEvent.KEYCODE_TAB -> bridge.transport!!.write(0x09)
            KeyEvent.KEYCODE_CAMERA -> handleCamera()
            KeyEvent.KEYCODE_DEL -> sendPressedKey(vt320.KEY_BACK_SPACE)
            KeyEvent.KEYCODE_ENTER -> {
                val vt = (buffer as vt320)
                vt.keyTyped(vt320.KEY_ENTER, ' ', 0)
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> handleDpad("left", vt320.KEY_LEFT)
            KeyEvent.KEYCODE_DPAD_UP -> handleDpad("up", vt320.KEY_UP)
            KeyEvent.KEYCODE_DPAD_DOWN -> handleDpad("down", vt320.KEY_DOWN)
            KeyEvent.KEYCODE_DPAD_RIGHT -> handleDpad("right", vt320.KEY_RIGHT)
            KEYCODE_INSERT -> sendPressedKey(vt320.KEY_INSERT)
            KEYCODE_FORWARD_DEL -> sendPressedKey(vt320.KEY_DELETE)
            KEYCODE_MOVE_HOME -> sendPressedKey(vt320.KEY_HOME)
            KEYCODE_MOVE_END -> sendPressedKey(vt320.KEY_END)
            KEYCODE_PAGE_UP -> sendPressedKey(vt320.KEY_PAGE_UP)
            KEYCODE_PAGE_DOWN -> sendPressedKey(vt320.KEY_DOWN)
            else -> return false
        }
        return true
    }

    private fun handleModifierKeys(event: KeyEvent, keyCode: Int, flag: Boolean): Boolean {
        var newFlag = flag
        /// Handle alt and shift keys if they aren't repeating
        if (event.repeatCount == 0) {
            when {
                rightModifiersAreSlashAndTab -> newFlag = handleModifierKeyCode(keyCode)
                leftModifiersAreSlashAndTab -> newFlag = handleModifierKeyCode(keyCode)
                else -> {
                    when (keyCode) {
                        KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.KEYCODE_ALT_RIGHT -> metaPress(OUR_ALT_ON)
                        KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> metaPress(OUR_SHIFT_ON)
                        else -> newFlag = false
                    }
                }
            }
            if (!newFlag && (keyCode == KEYCODE_CTRL_LEFT || keyCode == KEYCODE_CTRL_RIGHT)) metaPress(OUR_CTRL_ON)
            else newFlag = false
        }
        return newFlag
    }

    private fun handleModifierKeyCode(keyCode: Int): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_ALT_RIGHT -> metaState = metaState or OUR_SLASH
            KeyEvent.KEYCODE_SHIFT_RIGHT -> metaState = metaState or OUR_TAB
            KeyEvent.KEYCODE_SHIFT_LEFT -> metaPress(OUR_SHIFT_ON)
            KeyEvent.KEYCODE_ALT_LEFT -> metaPress(OUR_ALT_ON)
            else -> return false
        }
        return true
    }

    private fun flagRaised(event: KeyEvent, keyCode: Int): Boolean {
        var flag = true
        flag = handleModifierKeys(event, keyCode, flag)
        flag = handleDpadCenter(keyCode, flag)
        return flag
    }

    private fun handleCamera() {
        // check to see which shortcut the camera button triggers
        when (manager!!.prefs!!.getString(PreferenceConstants.CAMERA, PreferenceConstants.CAMERA_CTRLA_SPACE)) {
            PreferenceConstants.CAMERA_CTRLA_SPACE -> {
                bridge.transport!!.write(0x01)
                bridge.transport!!.write(' '.code)
            }
            PreferenceConstants.CAMERA_CTRLA -> bridge.transport!!.write(0x01)
            PreferenceConstants.CAMERA_ESC -> (buffer as vt320).keyTyped(vt320.KEY_ESCAPE, ' ', 0)
            PreferenceConstants.CAMERA_ESC_A -> {
                sendEscape()
                bridge.transport!!.write('a'.code)
            }
        }
    }

    private fun handleDpad(direction: String, key: Int) {
        if (selectingForCopy) {
            when (direction) {
                "up" -> selectionArea.decrementRow()
                "down" -> selectionArea.incrementRow()
                "left" -> selectionArea.decrementColumn()
                "right" -> selectionArea.incrementColumn()
            }
            bridge.redraw()
        } else {
            sendPressedKey(key)
            bridge.tryKeyVibrate()
        }
    }

    protected fun updatePrefs() {
        keymode = prefs.getString(PreferenceConstants.KEYMODE, PreferenceConstants.KEYMODE_NONE)
        shiftedNumbersAreFKeysOnHardKeyboard = prefs.getBoolean(PreferenceConstants.SHIFT_FKEYS, false)
        controlNumbersAreFKeysOnSoftKeyboard = prefs.getBoolean(PreferenceConstants.CTRL_FKEYS, false)
        volumeKeysChangeFontSize = prefs.getBoolean(PreferenceConstants.VOLUME_FONT, true)
        val stickyModifiers = prefs.getString(PreferenceConstants.STICKY_MODIFIERS,
                PreferenceConstants.NO)
        stickyMetas = when (stickyModifiers) {
            PreferenceConstants.ALT -> OUR_ALT_ON
            PreferenceConstants.YES -> OUR_SHIFT_ON or OUR_CTRL_ON or OUR_ALT_ON
            else -> 0
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(this)
        deviceHasHardKeyboard = (manager!!.res!!.configuration.keyboard
                == Configuration.KEYBOARD_QWERTY)
        updatePrefs()
    }
}