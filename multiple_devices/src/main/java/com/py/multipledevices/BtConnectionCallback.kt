package com.py.multipledevices

import android.bluetooth.BluetoothDevice

interface BtConnectionCallback{
    fun onDeviceFound(btDevice: BluetoothDevice)
    fun onConnectionSuccessful()
}