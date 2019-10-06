package com.py.multipledevices

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BtBroadcastReceiver(val btAdapter: BondedDevicesAdapter? = null, val btCallbacks: BtCallbacks? = null) : BroadcastReceiver(){
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
                if(btAdapter == null)
                    return
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                btAdapter.add(device!!)
            }
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                if(btCallbacks == null)
                    return
                btCallbacks.onConnectStateChanged(true)
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                if(btCallbacks == null)
                    return
                btCallbacks.onConnectStateChanged(false)
            }
            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                if(btCallbacks == null)
                    return
                val bondState : Int = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                btAdapter?.notifyDataSetChanged()
                when(bondState){
                    BluetoothDevice.BOND_BONDED -> btCallbacks.onPairedStateChanged(true)
                    BluetoothDevice.BOND_NONE -> btCallbacks.onPairedStateChanged(false)
                }
            }
        }
    }

}