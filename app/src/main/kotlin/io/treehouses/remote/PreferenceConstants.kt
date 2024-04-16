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
package io.treehouses.remote

/**
 * @author Kenny Root
 */
object PreferenceConstants {
    const val MEMKEYS = "memkeys"
    const val SCROLLBACK = "scrollback"
    const val EMULATION = "emulation"
    const val ROTATION = "rotation"
    const val BACKUP_KEYS = "backupkeys"
    const val BACKUP_KEYS_DEFAULT = false
    const val ROTATION_DEFAULT = "Default"
    const val ROTATION_LANDSCAPE = "Force landscape"
    const val ROTATION_PORTRAIT = "Force portrait"
    const val FULLSCREEN = "fullscreen"
    const val TITLEBARHIDE = "titlebarhide"
    const val PG_UPDN_GESTURE = "pgupdngesture"
    const val KEYMODE = "keymode"
    const val KEY_ALWAYS_VISIBLE = "alwaysvisible"
    const val KEYMODE_RIGHT = "Use right-side keys"
    const val KEYMODE_LEFT = "Use left-side keys"
    const val KEYMODE_NONE = "none"
    const val CAMERA = "camera"
    const val CAMERA_CTRLA_SPACE = "Ctrl+A then Space"
    const val CAMERA_CTRLA = "Ctrl+A"
    const val CAMERA_ESC = "Esc"
    const val CAMERA_ESC_A = "Esc+A"
    const val KEEP_ALIVE = "keepalive"
    const val WIFI_LOCK = "wifilock"
    const val BUMPY_ARROWS = "bumpyarrows"
    const val SORT_BY_COLOR = "sortByColor"
    const val BELL = "bell"
    const val BELL_VOLUME = "bellVolume"
    const val BELL_VIBRATE = "bellVibrate"
    const val BELL_NOTIFICATION = "bellNotification"
    const val DEFAULT_BELL_VOLUME = 0.25f
    const val CONNECTION_PERSIST = "connPersist"
    const val SHIFT_FKEYS = "shiftfkeys"
    const val CTRL_FKEYS = "ctrlfkeys"
    const val VOLUME_FONT = "volumefont"
    const val STICKY_MODIFIERS = "stickymodifiers"
    const val YES = "yes"
    const val NO = "no"
    const val ALT = "alt"

    /* Backup identifiers */
    const val BACKUP_PREF_KEY = "prefs"
}