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
package io.treehouses.remote.SSH

import android.os.Handler
import android.os.Message
import java.util.concurrent.Semaphore

/**
 * Helps provide a relay for prompts and responses between a possible user
 * interface and some underlying service.
 *
 * @author jsharkey
 */
class PromptHelper(private val tag: Any) {
    private var handler: Handler? = null
    private val promptToken: Semaphore = Semaphore(1)
    private val promptResponse: Semaphore = Semaphore(0)

    @JvmField
	var promptInstructions: String? = null
    @JvmField
	var promptHint: String? = null
    @JvmField
	var promptRequested: Any? = null
    private var response: Any? = null

    /**
     * Register a user interface handler, if available.
     */
    fun setHandler(handler: Handler?) {
        this.handler = handler
    }

    /**
     * Set an incoming value from an above user interface. Will automatically
     * notify any waiting requests.
     */
    fun setResponse(value: Any?) {
        response = value
        promptRequested = null
        promptInstructions = null
        promptHint = null
        promptResponse.release()
    }

    /**
     * Return the internal response value just before erasing and returning it.
     */
    protected fun popResponse(): Any? {
        val value = response
        response = null
        return value
    }

    /**
     * Request a prompt response from parent. This is a blocking call until user
     * interface returns a value.
     * Only one thread can call this at a time. cancelPrompt() will force this to
     * immediately return.
     */
    @Throws(InterruptedException::class)
    private fun requestPrompt(instructions: String?, hint: String, type: Any): Any? {
        var response: Any? = null
        promptToken.acquire()
        try {
            promptInstructions = instructions
            promptHint = hint
            promptRequested = type

            // notify any parent watching for live events
            if (handler != null) Message.obtain(handler, -1, tag).sendToTarget()

            // acquire lock until user passes back value
            promptResponse.acquire()
            response = popResponse()
        } finally {
            promptToken.release()
        }
        return response
    }


     // Request a string response from parent. This is a blocking call until user
     // interface returns a value.
     // @param hint prompt hint for user to answer
     // @return string user has entered

    fun requestStringPrompt(instructions: String?, hint: String): String? {
        return request(instructions, hint) as String?
    }

    fun <T> request(instructions: String?, hint: String): T? {
        var value: T? = null
        try { value = requestPrompt(instructions, hint, Boolean::class.java) as T }
        catch (e: Exception) { }
        return value
    }

     // Request a boolean response from parent. This is a blocking call until user
     // interface returns a value.
     // @param hint prompt hint for user to answer
     // @return choice user has made (yes/no)

    fun requestBooleanPrompt(instructions: String?, hint: String): Boolean? {
        return request(instructions, hint) as Boolean?
    }

    /**
     * Cancel an in-progress prompt.
     */
    fun cancelPrompt() {
        if (!promptToken.tryAcquire()) {
            // A thread has the token, so try to interrupt it
            response = null
            promptResponse.release()
        } else {
            // No threads have acquired the token
            promptToken.release()
        }
    }

    init {

        // Threads must acquire this before they can send a prompt.

        // Responses will release this semaphore.
    }
}