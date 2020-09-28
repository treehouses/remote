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
package io.treehouses.remote.SSH.beans

import io.treehouses.remote.Views.terminal.VDUBuffer

/**
 * @author Kenny Root
 * Keep track of a selection area for the terminal copying mechanism.
 * If the orientation is flipped one way, swap the bottom and top or
 * left and right to keep it in the correct orientation.
 */
class SelectionArea {
    private var top = 0
    private var bottom = 0
    private var left = 0
    private var right = 0
    private var maxColumns = 0
    private var maxRows = 0
    var isSelectingOrigin = false
        private set

    fun reset() {
        right = 0
        bottom = right
        left = bottom
        top = left
        isSelectingOrigin = true
    }

    /**
     * @param columns
     * @param rows
     */
    fun setBounds(columns: Int, rows: Int) {
        maxColumns = columns - 1
        maxRows = rows - 1
    }

    private fun checkBounds(value: Int, max: Int): Int {
        return if (value < 0) 0 else if (value > max) max else value
    }

    fun finishSelectingOrigin() {
        isSelectingOrigin = false
    }

    fun decrementRow() {
        changeRow(-1)
    }

    fun incrementRow() {
        changeRow(1)
    }

    private fun changeRow(change: Int) {
        if (isSelectingOrigin) setTop(top + change) else setBottom(bottom + change)
    }

    private fun setTop(top: Int) {
        setInverses("top", top, maxRows)
    }

    private fun setInverses(kind: String, value: Int, max: Int) {
        val result = checkBounds(value, max)
        if (kind == "top") {
            bottom = result
            this.top = bottom
        } else {
            right = result
            this.left = right
        }
    }

    fun getTop(): Int {
        return Math.min(top, bottom)
    }

    private fun setBottom(bottom: Int) {
        this.bottom = checkBounds(bottom, maxRows)
    }

    fun getBottom(): Int {
        return Math.max(top, bottom)
    }

    fun decrementColumn() {
        changeColumn(-1)
    }

    fun incrementColumn() {
        changeColumn(1)
    }

    private fun changeColumn(change: Int) {
        if (isSelectingOrigin) setLeft(left + change) else setRight(right + change)
    }

    private fun setLeft(left: Int) {
        setInverses("left", left, maxColumns)
    }

    fun getLeft(): Int {
        return Math.min(left, right)
    }

    private fun setRight(right: Int) {
        this.right = checkBounds(right, maxColumns)
    }

    fun getRight(): Int {
        return Math.max(left, right)
    }

    fun copyFrom(vb: VDUBuffer): String {
        val size = (getRight() - getLeft() + 1) * (getBottom() - getTop() + 1)
        val buffer = StringBuilder(size)
        for (y in getTop()..getBottom()) {
            var lastNonSpace = buffer.length
            for (x in getLeft()..getRight()) {
                // only copy printable chars
                var c = vb.getChar(x, y)
                if (!Character.isDefined(c) ||
                        Character.isISOControl(c) && c != '\t') c = ' '
                if (c != ' ') lastNonSpace = buffer.length
                buffer.append(c)
            }

            // Don't leave a bunch of spaces in our copy buffer.
            if (buffer.length > lastNonSpace) buffer.delete(lastNonSpace + 1, buffer.length)
            if (y != bottom) buffer.append('\n')
        }
        return buffer.toString()
    }

    override fun toString(): String {
        val buffer = StringBuilder()
        buffer.append("SelectionArea[top=")
        buffer.append(top)
        buffer.append(", bottom=")
        buffer.append(bottom)
        buffer.append(", left=")
        buffer.append(left)
        buffer.append(", right=")
        buffer.append(right)
        buffer.append(", maxColumns=")
        buffer.append(maxColumns)
        buffer.append(", maxRows=")
        buffer.append(maxRows)
        buffer.append(", isSelectingOrigin=")
        buffer.append(isSelectingOrigin)
        buffer.append(']')
        return buffer.toString()
    }

    init {
        reset()
    }
}