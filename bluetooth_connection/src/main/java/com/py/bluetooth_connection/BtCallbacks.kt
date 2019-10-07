package com.py.bluetooth_connection

import android.bluetooth.BluetoothDevice

interface BtCallbacks {
    fun onConnectStateChanged(connected : Boolean)
    fun onPairedStateChanged(paired : Boolean)
    fun onDeviceFound(device : BluetoothDevice)
}