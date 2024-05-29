package com.rn.sosnow.map

object Keys {
    init {
        System.loadLibrary("native-lib")
    }

    external fun apiKey(): String
}