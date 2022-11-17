package com.peanut.ted.ed.lopper

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import java.lang.StringBuilder

internal class CheckTask {
    private val mHandlerThread = HandlerThread("卡顿检测")
    private val mHandler: Handler
    private val THREAD_HOLD = 200
    private val mRunnable = Runnable { log() }

    private fun log() {
        val sb = StringBuilder()
        val stackTrace = Looper.getMainLooper().thread.stackTrace
        for (s in stackTrace) { sb.append(s.toString().trimIndent()) }
        Log.w("Slow Looper", sb.toString())
    }

    fun start() {
        mHandler.postDelayed(mRunnable, THREAD_HOLD.toLong())
    }

    fun end() {
        mHandler.removeCallbacks(mRunnable)
    }

    init {
        mHandlerThread.start()
        mHandler = Handler(mHandlerThread.looper)
    }
}