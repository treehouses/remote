package io.treehouses.remote.views.terminal

import java.util.*

/*
 * This file is part of "JTA - Telnet/SSH for the JAVA(tm) platform".
 *
 * (c) Matthias L. Jugel, Marcus Meißner 1996-2005. All Rights Reserved.
 *
 * Please visit http://javatelnet.org/ for updates and contact.
 *
 * --LICENSE NOTICE--
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * --LICENSE NOTICE--
 *
 */ /**
 * An interface for a terminal that accepts input from keyboard and mouse.
 *
 * @author Matthias L. Jugel, Marcus Meißner
 * @version $Id: io.treehouses.remote.Views.terminal.VDUInput.java 499 2005-09-29 08:24:54Z leo $
 */
interface VDUInput {
    /**
     * Direct access to writing data ...
     *
     * @param b
     */
    fun write(b: ByteArray?)

    /**
     * Terminal is mouse-aware and requires (x,y) coordinates of
     * on the terminal (character coordinates) and the button clicked.
     *
     * @param x
     * @param y
     * @param modifiers
     */
    fun mousePressed(x: Int, y: Int, modifiers: Int)

    /**
     * Passes mouse wheel events to the terminal.
     *
     * @param down  True if scrolling down the page. False if scrolling up.
     * @param x
     * @param y
     * @param ctrl
     * @param shift
     * @param meta
     */
    fun mouseWheel(down: Boolean, x: Int, y: Int, ctrl: Boolean, shift: Boolean, meta: Boolean)

    /**
     * Terminal is mouse-aware and requires the coordinates and button
     * of the release.
     *
     * @param x
     * @param y
     */
    fun mouseReleased(x: Int, y: Int)

    /**
     * Override the standard key codes used by the terminal emulation.
     *
     * @param codes a properties object containing key code definitions
     */
    fun setKeyCodes(codes: Properties)

    /**
     * main keytyping event handler...
     *
     * @param keyCode   the key code
     * @param keyChar   the character represented by the key
     * @param modifiers shift/alt/control modifiers
     */
    fun keyPressed(keyCode: Int, keyChar: Char, modifiers: Int)

    /**
     * Handle key Typed events for the terminal, this will get
     * all normal key types, but no shift/alt/control/numlock.
     *
     * @param keyCode   the key code
     * @param keyChar   the character represented by the key
     * @param modifiers shift/alt/control modifiers
     */
    fun keyTyped(keyCode: Int, keyChar: Char, modifiers: Int)

    companion object {
        const val KEY_CONTROL = 0x01
        const val KEY_SHIFT = 0x02
        const val KEY_ALT = 0x04
        const val KEY_ACTION = 0x08
    }
}