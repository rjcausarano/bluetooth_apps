package com.py.multipledevices

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BtBroadcastReceiver(val btConnectionCallback: BtConnectionCallback) : BroadcastReceiver(){
    companion object{
        val TAG = BtBroadcastReceiver::class.simpleName
    }
    override fun onReceive(p0: Context?, intent: Intent) {
        //TODO check these state values! this is just sample code
        val action = intent.action
        when(action){
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val currentState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)
                val previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.STATE_OFF)

                when(currentState){
                    BluetoothAdapter.STATE_OFF -> Log.d(TAG, "off")
                    BluetoothAdapter.STATE_ON -> Log.d(TAG, "on")
                    BluetoothAdapter.STATE_TURNING_OFF -> Log.d(TAG, "turning off")
                    BluetoothAdapter.STATE_TURNING_ON -> Log.d(TAG, "turning on")
                }
            }
            BluetoothDevice.ACTION_FOUND -> {
                val device: BluetoothDevice =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                btConnectionCallback.onDeviceFound(device)
                //TODO remove these comments
                //val deviceName = device.name
                //val deviceHardwareAddress = device.address
            }
        }
    }

}