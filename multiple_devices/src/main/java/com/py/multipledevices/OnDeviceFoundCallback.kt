package com.py.multipledevices

import android.bluetooth.BluetoothDevice

interface OnDeviceFoundCallback{
    fun execute(btDevice: BluetoothDevice)
}