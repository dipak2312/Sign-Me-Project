package com.app.signme.util

class Util {

    companion object {
        fun waitThread(seconds: Int) {
            Thread.sleep(seconds * 1000L)
        }
    }
}