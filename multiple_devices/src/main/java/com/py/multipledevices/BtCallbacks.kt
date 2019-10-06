package com.py.multipledevices

interface BtCallbacks {
    fun onConnectStateChanged(connected : Boolean)
    fun onPairedStateChanged(paired : Boolean)
}