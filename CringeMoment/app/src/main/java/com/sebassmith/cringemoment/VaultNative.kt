package com.sebassmith.cringemoment

object VaultNative {
    init {
        System.loadLibrary("cringemoment")
    }
    external fun decodeSecret(): String
}