package com.py.multipledevices

import android.bluetooth.BluetoothDevice

interface BtCallbacks {
    fun onConnectStateChanged(connected : Boolean)
    fun onPairedStateChanged(paired : Boolean)
    fun onDeviceFound(device : BluetoothDevice)
}