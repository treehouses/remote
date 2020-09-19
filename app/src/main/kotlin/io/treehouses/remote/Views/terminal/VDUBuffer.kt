package io.treehouses.remote.Views.terminal

import java.util.*

/*
 * This file is part of "JTA - Telnet/SSH for the JAVA(tm) platform".
 *
 * (c) Matthias L. Jugel, Marcus Mei�ner 1996-2005. All Rights Reserved.
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
 * Implementation of a Video Display Unit (VDU) buffer. This class contains
 * all methods to manipulate the buffer that stores characters and their
 * attributes as well as the regions displayed.
 *
 * @author Matthias L. Jugel, Marcus Meißner
 * @version $Id: io.treehouses.remote.Views.terminal.VDUBuffer.java 503 2005-10-24 07:34:13Z marcus $
 */
open class VDUBuffer @JvmOverloads constructor(width: Int = 80, height: Int = 24) {
    /**
     * Get amount of rows on the screen.
     */
    var rows = 0

    /**
     * Get amount of columns on the screen.
     */
    var columns /* rows and columns */ = 0
    lateinit var update /* contains the lines that need update */: BooleanArray
    var charArray /* contains the characters */: Array<CharArray?>? = null
    var charAttributes /* contains character attrs */: Array<LongArray>? = null
    var bufSize = 0

    /**
     * Retrieve maximum buffer Size.
     *
     * @see .getBufferSize
     */
    var maxBufferSize /* buffer sizes */ = 0
    var screenBase /* the actual screen start */ = 0
    var windowBase /* where the start displaying */ = 0
    var scrollMarker /* marks the last line inserted */ = 0
    var topMargin /* top scroll margin */ = 0
    var bottomMargin /* bottom scroll margin */ = 0

    /**
     * Check whether the cursor is currently visible.
     *
     * @return visibility
     */
    // cursor variables
    var isCursorVisible = true
        protected set

    /**
     * Get the current column of the cursor position.
     */
    var cursorColumn = 0
        protected set

    /**
     * Get the current line of the cursor position.
     */
    var cursorRow = 0
        protected set
    /**
     * Put a character on the screen with specific font and outline.
     * The character previously on that position will be overwritten.
     * You need to call redraw() to update the screen.
     *
     * @param c          x-coordinate (column)
     * @param l          y-coordinate (line)
     * @param ch         the character to show on the screen
     * @param attributes the character attributes
     * @see .BOLD
     *
     * @see .UNDERLINE
     *
     * @see .INVERT
     *
     * @see .INVISIBLE
     *
     * @see .NORMAL
     *
     * @see .LOW
     *
     * @see .insertChar
     *
     * @see .deleteChar
     *
     * @see .redraw
     */
    /**
     * Put a character on the screen with normal font and outline.
     * The character previously on that position will be overwritten.
     * You need to call redraw() to update the screen.
     *
     * @param c  x-coordinate (column)
     * @param l  y-coordinate (line)
     * @param ch the character to show on the screen
     * @see .insertChar
     *
     * @see .deleteChar
     *
     * @see .redraw
     */
    @JvmOverloads
    fun putChar(c: Int, l: Int, ch: Char, attributes: Long = NORMAL) {
        charArray!![screenBase + l]!![c] = ch
        charAttributes!![screenBase + l][c] = attributes
        if (l < rows) update[l + 1] = true
    }

    /**
     * Get the character at the specified position.
     *
     * @param c x-coordinate (column)
     * @param l y-coordinate (line)
     * @see .putChar
     */
    fun getChar(c: Int, l: Int): Char {
        return charArray!![screenBase + l]!![c]
    }

    /**
     * Get the attributes for the specified position.
     *
     * @param c x-coordinate (column)
     * @param l y-coordinate (line)
     * @see .putChar
     */
    fun getAttributes(c: Int, l: Int): Long {
        return charAttributes!![screenBase + l][c]
    }

    /**
     * Insert a character at a specific position on the screen.
     * All character right to from this position will be moved one to the right.
     * You need to call redraw() to update the screen.
     *
     * @param c          x-coordinate (column)
     * @param l          y-coordinate (line)
     * @param ch         the character to insert
     * @param attributes the character attributes
     * @see .BOLD
     *
     * @see .UNDERLINE
     *
     * @see .INVERT
     *
     * @see .INVISIBLE
     *
     * @see .NORMAL
     *
     * @see .LOW
     *
     * @see .putChar
     *
     * @see .deleteChar
     *
     * @see .redraw
     */
    fun insertChar(c: Int, l: Int, ch: Char, attributes: Long) {
        System.arraycopy(charArray!![screenBase + l]!!, c,
                charArray!![screenBase + l], c + 1, columns - c - 1)
        System.arraycopy(charAttributes!![screenBase + l], c,
                charAttributes!![screenBase + l], c + 1, columns - c - 1)
        putChar(c, l, ch, attributes)
    }

    /**
     * Delete a character at a given position on the screen.
     * All characters right to the position will be moved one to the left.
     * You need to call redraw() to update the screen.
     *
     * @param c x-coordinate (column)
     * @param l y-coordinate (line)
     * @see .putChar
     *
     * @see .insertChar
     *
     * @see .redraw
     */
    fun deleteChar(c: Int, l: Int) {
        if (c < columns - 1) {
            System.arraycopy(charArray!![screenBase + l]!!, c + 1,
                    charArray!![screenBase + l]!!, c, columns - c - 1)
            System.arraycopy(charAttributes!![screenBase + l], c + 1,
                    charAttributes!![screenBase + l], c, columns - c - 1)
        }
        putChar(columns - 1, l, 0.toChar())
    }
    /**
     * Put a String at a specific position giving all characters the same
     * attributes. Any characters previously on that position will be
     * overwritten. You need to call redraw() to update the screen.
     *
     * @param c          x-coordinate (column)
     * @param l          y-coordinate (line)
     * @param s          the string to be shown on the screen
     * @param attributes character attributes
     * @see .BOLD
     *
     * @see .UNDERLINE
     *
     * @see .INVERT
     *
     * @see .INVISIBLE
     *
     * @see .NORMAL
     *
     * @see .LOW
     *
     * @see .putChar
     *
     * @see .insertLine
     *
     * @see .deleteLine
     *
     * @see .redraw
     */
    /**
     * Put a String at a specific position. Any characters previously on that
     * position will be overwritten. You need to call redraw() for screen update.
     *
     * @param c x-coordinate (column)
     * @param l y-coordinate (line)
     * @param s the string to be shown on the screen
     * @see .BOLD
     *
     * @see .UNDERLINE
     *
     * @see .INVERT
     *
     * @see .INVISIBLE
     *
     * @see .NORMAL
     *
     * @see .LOW
     *
     * @see .putChar
     *
     * @see .insertLine
     *
     * @see .deleteLine
     *
     * @see .redraw
     */
    @JvmOverloads
    fun putString(c: Int, l: Int, s: String, attributes: Long = NORMAL) {
        var i = 0
        while (i < s.length && c + i < columns) {
            putChar(c + i, l, s[i], attributes)
            i++
        }
    }

    /**
     * Insert a blank line at a specific position.
     * The current line and all previous lines are scrolled one line up. The
     * top line is lost. You need to call redraw() to update the screen.
     *
     * @param l the y-coordinate to insert the line
     * @see .deleteLine
     *
     * @see .redraw
     */
    fun insertLine(l: Int) {
        insertLine(l, 1, SCROLL_UP)
    }

    /**
     * Insert blank lines at a specific position.
     * You need to call redraw() to update the screen
     *
     * @param l the y-coordinate to insert the line
     * @param n amount of lines to be inserted
     * @see .deleteLine
     *
     * @see .redraw
     */
    fun insertLine(l: Int, n: Int) {
        insertLine(l, n, SCROLL_UP)
    }

    /**
     * Insert a blank line at a specific position. Scroll text according to
     * the argument.
     * You need to call redraw() to update the screen
     *
     * @param l          the y-coordinate to insert the line
     * @param scrollDown scroll down
     * @see .deleteLine
     *
     * @see .SCROLL_UP
     *
     * @see .SCROLL_DOWN
     *
     * @see .redraw
     */
    fun insertLine(l: Int, scrollDown: Boolean) {
        insertLine(l, 1, scrollDown)
    }

    /**
     * Insert blank lines at a specific position.
     * The current line and all previous lines are scrolled one line up. The
     * top line is lost. You need to call redraw() to update the screen.
     *
     * @param l          the y-coordinate to insert the line
     * @param n          number of lines to be inserted
     * @param scrollDown scroll down
     * @see .deleteLine
     *
     * @see .SCROLL_UP
     *
     * @see .SCROLL_DOWN
     *
     * @see .redraw
     */
    @Synchronized
    fun insertLine(l: Int, n: Int, scrollDown: Boolean) {
        var num = n
        var cbuf: Array<CharArray?>? = null
        var abuf: Array<LongArray>? = null
        var offset = 0
        val oldBase = screenBase
        var newScreenBase = screenBase
        var newWindowBase = windowBase
        var newBufSize = bufSize
        if (l > bottomMargin) /* We do not scroll below bottom margin (below the scrolling region). */ return
        val top = if (l < topMargin) 0 else if (l > bottomMargin) if (bottomMargin + 1 < rows) bottomMargin + 1 else rows - 1 else topMargin
        val bottom = if (l > bottomMargin) rows - 1 else if (l < topMargin) if (topMargin > 0) topMargin - 1 else 0 else bottomMargin

        // System.out.println("l is "+l+", top is "+top+", bottom is "+bottom+", bottomargin is "+bottomMargin+", topMargin is "+topMargin);
        if (scrollDown) {
            if (num > bottom - top) num = bottom - top
            var size = bottom - l - (num - 1)
            if (size < 0) size = 0
            cbuf = Array(size) { CharArray(columns) }
            abuf = Array(size) { LongArray(columns) }
            System.arraycopy(charArray!!, oldBase + l, cbuf, 0, bottom - l - (num - 1))
            System.arraycopy(charAttributes!!, oldBase + l,
                    abuf, 0, bottom - l - (num - 1))
            System.arraycopy(cbuf, 0, charArray!!, oldBase + l + num,
                    bottom - l - (num - 1))
            System.arraycopy(abuf, 0, charAttributes!!, oldBase + l + num,
                    bottom - l - (num - 1))
            cbuf = charArray
            abuf = charAttributes
        } else {
            try {
                if (num > bottom - top + 1) num = bottom - top + 1
                if (bufSize < maxBufferSize) {
                    if (bufSize + num > maxBufferSize) {
                        offset = num - (maxBufferSize - bufSize)
                        scrollMarker += offset
                        newBufSize = maxBufferSize
                        newScreenBase = maxBufferSize - rows - 1
                        newWindowBase = screenBase
                    } else {
                        scrollMarker += num
                        newScreenBase += num
                        newWindowBase += num
                        newBufSize += num
                    }
                    cbuf = Array(newBufSize) { CharArray(columns) }
                    abuf = Array(newBufSize) { LongArray(columns) }
                } else {
                    offset = num
                    cbuf = charArray
                    abuf = charAttributes
                }
                // copy anything from the top of the buffer (+offset) to the new top
                // up to the screenBase.
                if (oldBase > 0) {
                    System.arraycopy(charArray!!, offset,
                            cbuf!!, 0,
                            oldBase - offset)
                    System.arraycopy(charAttributes!!, offset,
                            abuf!!, 0,
                            oldBase - offset)
                }
                // copy anything from the top of the screen (screenBase) up to the
                // topMargin to the new screen
                if (top > 0) {
                    System.arraycopy(charArray!!, oldBase,
                            cbuf!!, newScreenBase, top)

                    System.arraycopy(charAttributes!!, oldBase,
                            abuf!!, newScreenBase, top)
                }
                // copy anything from the topMargin up to the amount of lines inserted
                // to the gap left over between scrollback buffer and screenBase
                if (oldBase >= 0) {
                    System.arraycopy(charArray!!, oldBase + top,
                            cbuf!!, oldBase - offset, num)

                    System.arraycopy(charAttributes!!, oldBase + top,
                            abuf!!, oldBase - offset, num)
                }
                // copy anything from topMargin + n up to the line linserted to the
                // topMargin
                System.arraycopy(charArray!!, oldBase + top + num,
                        cbuf!!, newScreenBase + top,
                        l - top - (num - 1))
                System.arraycopy(charAttributes!!, oldBase + top + num,
                        abuf!!, newScreenBase + top,
                        l - top - (num - 1))
                //
                // copy the all lines next to the inserted to the new buffer
                if (l < rows - 1) {
                    System.arraycopy(charArray!!, oldBase + l + 1,
                            cbuf, newScreenBase + l + 1,
                            rows - 1 - l)
                    System.arraycopy(charAttributes!!, oldBase + l + 1,
                            abuf, newScreenBase + l + 1,
                            rows - 1 - l)
                }
            } catch (e: ArrayIndexOutOfBoundsException) {
                // this should not happen anymore, but I will leave the code
                // here in case something happens anyway. That code above is
                // so complex I always have a hard time understanding what
                // I did, even though there are comments
                System.err.println("*** Error while scrolling up:")
                System.err.println("--- BEGIN STACK TRACE ---")
                e.printStackTrace()
                System.err.println("--- END STACK TRACE ---")
                System.err.println("bufSize=$bufSize, maxBufSize=$maxBufferSize")
                System.err.println("top=$top, bottom=$bottom")
                System.err.println("n=$num, l=$l")
                System.err.println("screenBase=$screenBase, windowBase=$windowBase")
                System.err.println("newScreenBase=$newScreenBase, newWindowBase=$newWindowBase")
                System.err.println("oldBase=$oldBase")
                System.err.println("size.width=$columns, size.height=$rows")
                System.err.println("abuf.length=" + abuf!!.size + ", cbuf.length=" + cbuf!!.size)
                System.err.println("*** done dumping debug information")
            }
        }

        // this is a little helper to mark the scrolling
        scrollMarker -= num
        for (i in 0 until num) {
            cbuf!![newScreenBase + l + (if (scrollDown) i else -i)] = CharArray(columns)
            Arrays.fill(cbuf[newScreenBase + l + (if (scrollDown) i else -i)]!!, ' ')
            abuf!![newScreenBase + l + (if (scrollDown) i else -i)] = LongArray(columns)
        }
        charArray = cbuf
        charAttributes = abuf
        screenBase = newScreenBase
        windowBase = newWindowBase
        bufSize = newBufSize
        if (scrollDown) markLine(l, bottom - l + 1) else markLine(top, l - top + 1)
        display!!.updateScrollBar()
    }

    /**
     * Delete a line at a specific position. Subsequent lines will be scrolled
     * up to fill the space and a blank line is inserted at the end of the
     * screen.
     *
     * @param l the y-coordinate to insert the line
     * @see .deleteLine
     */
    fun deleteLine(l: Int) {
        val bottom = if (l > bottomMargin) rows - 1 else if (l < topMargin) topMargin else bottomMargin + 1
        val numRows = bottom - l - 1
        val discardedChars = charArray!![screenBase + l]
        val discardedAttributes = charAttributes!![screenBase + l]
        if (numRows > 0) {
            System.arraycopy(charArray!!, screenBase + l + 1,
                    charArray!!, screenBase + l, numRows)
            System.arraycopy(charAttributes!!, screenBase + l + 1,
                    charAttributes!!, screenBase + l, numRows)
        }
        val newBottomRow = screenBase + bottom - 1
        charArray!![newBottomRow] = discardedChars
        charAttributes!![newBottomRow] = discardedAttributes
        Arrays.fill(charArray!![newBottomRow]!!, ' ')
        Arrays.fill(charAttributes!![newBottomRow], 0)
        markLine(l, bottom - l)
    }
    /**
     * Delete a rectangular portion of the screen.
     * You need to call redraw() to update the screen.
     *
     * @param c       x-coordinate (column)
     * @param l       y-coordinate (row)
     * @param w       with of the area in characters
     * @param h       height of the area in characters
     * @param curAttr attribute to fill
     * @see .deleteChar
     *
     * @see .deleteLine
     *
     * @see .redraw
     */
    /**
     * Delete a rectangular portion of the screen.
     * You need to call redraw() to update the screen.
     *
     * @param c x-coordinate (column)
     * @param l y-coordinate (row)
     * @param w with of the area in characters
     * @param h height of the area in characters
     * @see .deleteChar
     *
     * @see .deleteLine
     *
     * @see .redraw
     */
    @JvmOverloads
    fun deleteArea(c: Int, l: Int, w: Int, h: Int, curAttr: Long = 0) {
        val endColumn = c + w
        var targetRow = screenBase + l
        var i = 0
        while (i < h && l + i < rows) {
            Arrays.fill(charAttributes!![targetRow], c, endColumn, curAttr)
            Arrays.fill(charArray!![targetRow], c, endColumn, ' ')
            targetRow++
            i++
        }
        markLine(l, h)
    }

    /**
     * Sets whether the cursor is visible or not.
     *
     * @param doshow
     */
    fun showCursor(doshow: Boolean) {
        isCursorVisible = doshow
    }

    /**
     * Puts the cursor at the specified position.
     *
     * @param c column
     * @param l line
     */
    fun setCursorPosition(c: Int, l: Int) {
        cursorColumn = c
        cursorRow = l
    }

    /**
     * Set the current window base. This allows to view the scrollback buffer.
     *
     * @param line the line where the screen window starts
     * @see .setBufferSize
     *
     * @see .getBufferSize
     */
    fun setBaseWindow(line: Int) {
        var newLine = line
        if (newLine > screenBase) newLine = screenBase else if (newLine < 0) newLine = 0
        windowBase = newLine
        update[0] = true
        redraw()
    }

    /**
     * Get the current window base.
     *
     * @see .setWindowBase
     */
    fun getBaseWindow(): Int {
        return windowBase
    }

    /**
     * Set the scroll margins simultaneously.  If they're out of bounds, trim them.
     *
     * @param l1 line that is the top
     * @param l2 line that is the bottom
     */
    fun setMargins(l1: Int, l2: Int) {
        var newL1 = l1
        var newL2 = l2
        if (newL1 > newL2) return
        if (newL1 < 0) newL1 = 0
        if (newL2 >= rows) newL2 = rows - 1
        topMargin = newL1
        bottomMargin = newL2
    }

    /**
     * Set the top scroll margin for the screen. If the current bottom margin
     * is smaller it will become the top margin and the line will become the
     * bottom margin.
     *
     * @param l line that is the margin
     */
    fun setMarginTop(l: Int) {
        if (l > bottomMargin) {
            topMargin = bottomMargin
            bottomMargin = l
        } else topMargin = l
        if (topMargin < 0) topMargin = 0
        if (bottomMargin >= rows) bottomMargin = rows - 1
    }

    /**
     * Get the top scroll margin.
     */
    fun getMarginTop(): Int {
        return topMargin
    }

    /**
     * Set the bottom scroll margin for the screen. If the current top margin
     * is bigger it will become the bottom margin and the line will become the
     * top margin.
     *
     * @param l line that is the margin
     */
    fun setMarginBottom(l: Int) {
        if (l < topMargin) {
            bottomMargin = topMargin
            topMargin = l
        } else bottomMargin = l
        if (topMargin < 0) topMargin = 0
        if (bottomMargin >= rows) bottomMargin = rows - 1
    }

    /**
     * Get the bottom scroll margin.
     */
    fun getMarginBottom(): Int {
        return bottomMargin
    }

    /**
     * Retrieve current scrollback buffer size.
     *
     * @see .setBufferSize
     */
    /**
     * Set scrollback buffer size.
     *
     * @param amount new size of the buffer
     */
    var bufferSize: Int
        get() = bufSize
        set(amount) {
            var newAmount = amount
            if (newAmount < rows) newAmount = rows
            if (newAmount < maxBufferSize) {
                val cbuf = Array<CharArray?>(newAmount) { CharArray(columns) }
                val abuf = Array(newAmount) { LongArray(columns) }
                val copyStart = if (bufSize - newAmount < 0) 0 else bufSize - newAmount
                val copyCount = if (bufSize - newAmount < 0) bufSize else newAmount
                if (charArray != null) System.arraycopy(charArray!!, copyStart, cbuf, 0, copyCount)
                if (charAttributes != null) System.arraycopy(charAttributes!!, copyStart, abuf, 0, copyCount)
                charArray = cbuf
                charAttributes = abuf
                bufSize = copyCount
                screenBase = bufSize - rows
                windowBase = screenBase
            }
            maxBufferSize = newAmount
            update[0] = true
            redraw()
        }

    /**
     * Change the size of the screen. This will include adjustment of the
     * scrollback buffer.
     *
     * @param w of the screen
     * @param h of the screen
     */
    open fun setScreenSize(w: Int, h: Int, broadcast: Boolean) {
        val cbuf: Array<CharArray?>
        val abuf: Array<LongArray>
        var maxSize = bufSize
        val oldAbsR = screenBase + cursorRow
        if (w < 1 || h < 1) return
        if (debug > 0) System.err.println("VDU: screen size [$w,$h]")
        if (h > maxBufferSize) maxBufferSize = h
        if (h > bufSize) {
            bufSize = h
            screenBase = 0
            windowBase = 0
        }
        if (windowBase + h >= bufSize) windowBase = bufSize - h
        if (screenBase + h >= bufSize) screenBase = bufSize - h
        cbuf = Array(bufSize) { CharArray(w) }
        abuf = Array(bufSize) { LongArray(w) }
        for (i in 0 until bufSize) {
            Arrays.fill(cbuf[i], ' ')
        }
        if (bufSize < maxSize) maxSize = bufSize
        var rowLength: Int
        if (charArray != null && charAttributes != null) {
            var i = 0
            while (i < maxSize && charArray!![i] != null) {
                rowLength = charArray!![i]!!.size
                System.arraycopy(charArray!![i]!!, 0, cbuf[i], 0,
                        if (w < rowLength) w else rowLength)
                System.arraycopy(charAttributes!![i], 0, abuf[i], 0,
                        if (w < rowLength) w else rowLength)
                i++
            }
        }
        var C = cursorColumn
        if (C < 0) C = 0 else if (C >= w) C = w - 1
        var R = cursorRow
        // If the screen size has grown and now there are more rows on the screen,
        // slide the cursor down to the end of the text.
        if (R + screenBase <= oldAbsR) R = oldAbsR - screenBase
        if (R < 0) R = 0 else if (R >= h) R = h - 1
        setCursorPosition(C, R)
        charArray = cbuf
        charAttributes = abuf
        columns = w
        rows = h
        topMargin = 0
        bottomMargin = h - 1
        update = BooleanArray(h + 1)
        update[0] = true
        /*  FIXME: ???
    if(resizeStrategy == RESIZE_FONT)
      setBounds(getBounds());
    */
    }

    /**
     * Mark lines to be updated with redraw().
     *
     * @param l starting line
     * @param n amount of lines to be updated
     * @see .redraw
     */
    fun markLine(l: Int, n: Int) {
        var i = 0
        while (i < n && l + i < rows) {
            update[l + i + 1] = true
            i++
        }
    }
    //  private static int checkBounds(int value, int lower, int upper) {
    //    if (value < lower)
    //      return lower;
    //    else if (value > upper)
    //      return upper;
    //    else
    //      return value;
    //  }
    /**
     * a generic display that should redraw on demand
     */
    var display: VDUDisplay? = null

    /**
     * Trigger a redraw on the display.
     */
    protected fun redraw() {
        if (display != null) display!!.redraw()
    }

    companion object {
        /**
         * The current version id tag
         */
        const val ID = "\$Id: io.treehouses.remote.Views.terminal.VDUBuffer.java 503 2005-10-24 07:34:13Z marcus $"

        /**
         * Enable debug messages.
         */
        const val debug = 0

        /**
         * Scroll up when inserting a line.
         */
        const val SCROLL_UP = false

        /**
         * Scroll down when inserting a line.
         */
        const val SCROLL_DOWN = true
        /*  Attributes bit-field usage:
     *
     *  8421 8421 8421 8421 8421 8421 8421 8421  8421 8421 8421 8421 8421 8421 8421 8421
     *  |||| |||| |||| |||| |||| |||| |||| ||||  |||| |||| |||| |||| |||| |||| |||| |||`- Bold
     *  |||| |||| |||| |||| |||| |||| |||| ||||  |||| |||| |||| |||| |||| |||| |||| ||`-- Underline
     *  |||| |||| |||| |||| |||| |||| |||| ||||  |||| |||| |||| |||| |||| |||| |||| |`--- Invert
     *  |||| |||| |||| |||| |||| |||| |||| ||||  |||| |||| |||| |||| |||| |||| |||| `---- Low
     *  |||| |||| |||| |||| |||| |||| |||| ||||  |||| |||| |||| |||| |||| |||| |||`------ Invisible
     *  |||| |||| |||| |||| |||| |||| |||| ||||  |||| |||| |||| |||| |||| |||| ||`------- Fullwidth character
     *  |||| |||| |||| |||| |||| |||| |||| ||||  |`++-++++-++++-++++-++++-++++-++-------- Foreground Color
     *  |||| |||| `+++-++++-++++-++++-++++-++++--+--------------------------------------- Background Color
     *  `+++-++++------------------------------------------------------------------------ Reserved for future use
     */
        /**
         * Make character normal.
         */
        const val NORMAL: Long = 0x00

        /**
         * Make character bold.
         */
        const val BOLD: Long = 0x01

        /**
         * Underline character.
         */
        const val UNDERLINE: Long = 0x02

        /**
         * Invert character.
         */
        const val INVERT: Long = 0x04

        /**
         * Lower intensity character.
         */
        const val LOW: Long = 0x08

        /**
         * Invisible character.
         */
        const val INVISIBLE: Long = 0x10

        /**
         * Unicode full-width character (CJK, et al.)
         */
        const val FULLWIDTH: Long = 0x20

        /**
         * how much to left shift the foreground color
         */
        const val COLOR_FG_SHIFT = 6

        /**
         * how much to left shift the background color
         */
        const val COLOR_BG_SHIFT = 31

        /**
         * color mask
         */
        const val COLOR = 0xffffffffffffc0L /* 0000 0000 1111 1111 1111 1111 1111 1111  1111 1111 1111 1111 1111 1111 1100 0000 */

        /**
         * foreground color mask
         */
        const val COLOR_FG = 0x7fffffc0L /* 0000 0000 0000 0000 0000 0000 0000 0000  0111 1111 1111 1111 1111 1111 1100 0000 */

        /**
         * background color mask
         */
        const val COLOR_BG = 0xffffff80000000L /* 0000 0000 1111 1111 1111 1111 1111 1111  1000 0000 0000 0000 0000 0000 0000 0000 */

        /**
         * how much to left shift the red component
         */
        const val COLOR_RED_SHIFT = 16

        /**
         * how much to left shift the green component
         */
        const val COLOR_GREEN_SHIFT = 8

        /**
         * how much to left shift the blue component
         */
        const val COLOR_BLUE_SHIFT = 0
    }
    /**
     * Create a new video display buffer with the passed width and height in
     * characters.
     *
     * @param width  the length of the character lines
     * @param height the amount of lines on the screen
     */
    /**
     * Create a standard video display buffer with 80 columns and 24 lines.
     */
    init {
        // set the display screen size
        setScreenSize(width, height, false)
    }
}