package com.py.multipledevices

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.content.IntentFilter
import android.os.Handler
import android.os.Message
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager


class MainActivity : AppCompatActivity() {
    val handler = object:Handler(){
        override fun handleMessage(msg: Message) {
            when(msg.what){
                Constants.MESSAGE_READ -> {
                    val txt = String(msg.obj as ByteArray, 0, msg.arg1)
                    showToast(txt)
                }
            }
        }
    }
    val btServices = BtServices(handler, this)
    val adapter = BondedDevicesAdapter(ArrayList(), btServices)
    val btCallbacks = object: BtCallbacks{
        override fun onConnectStateChanged(connected: Boolean) {
            if(connected)
                showToast("Bluetooth connected!!")
            else
                showToast("Bluetooth disconnected..")
        }

        override fun onPairedStateChanged(paired: Boolean) {
            if(paired)
                showToast("Bluetooth paired!!")
            else
                showToast("Bluetooth not paired..")
        }
    }
    val btBroadcastReceiver = BtBroadcastReceiver(adapter, btCallbacks)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        configureReceiver()
        bluetooth_bonded_devices.adapter = adapter
        bluetooth_bonded_devices.layoutManager = LinearLayoutManager(this)

        bluetooth_enable_button.setOnClickListener {
            btServices.enable()
            enableDiscovery()
            btServices.acceptConnections()
        }

        bluetooth_scan_button.setOnClickListener {
            btServices.scanDevices()
            getDevices()
        }

        send_text.setOnClickListener {
            btServices.write(message_tv.text.toString())
            message_tv.text.clear()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            BtServices.REQUEST_ENABLE_BT -> {
                if(resultCode == Activity.RESULT_OK){
                    showToast("Bluetooth Enabled!")
                }
            }
            BtServices.REQUEST_LOCATION_PERMISSION -> {
                if(resultCode == Activity.RESULT_OK)
                    btServices.scanDevices()
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(btBroadcastReceiver)
    }

    private fun getDevices(){
        adapter.getDevices()
    }

    private fun configureReceiver(){
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(btBroadcastReceiver, filter)
    }

    private fun enableDiscovery(){
        // discoverable for 300 seconds. 0 for always discoverable
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        }
        startActivity(discoverableIntent)
    }

    private fun showToast(msg:String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
