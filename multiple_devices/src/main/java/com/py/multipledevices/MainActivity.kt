package com.py.multipledevices

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.content.IntentFilter
import android.os.Handler
import androidx.recyclerview.widget.LinearLayoutManager


class MainActivity : AppCompatActivity() {
    val handler = Handler()
    val btServices = BtServices(handler, this)
    val adapter = BondedDevicesAdapter(ArrayList(), btServices)
    val onDeviceFoundCallback = object : BtConnectionCallback{
        override fun onConnectionSuccessful() {
        }

        override fun onDeviceFound(btDevice: BluetoothDevice) {
            adapter.add(btDevice)
        }
    }
    val btBroadcastReceiver = BtBroadcastReceiver(onDeviceFoundCallback)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        configureReceiver()
        bluetooth_bonded_devices.adapter = adapter
        bluetooth_bonded_devices.layoutManager = LinearLayoutManager(this)

        bluetooth_connect_button.setOnClickListener {
            btServices.enable()
            getDevices()
        }

        bluetooth_scan_button.setOnClickListener {
            btServices.scanDevices()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == BtServices.REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            btServices.enable()
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
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
        registerReceiver(btBroadcastReceiver, filter)
    }

    private fun enableDiscovery(){
        // discoverable for 300 seconds. 0 for always discoverable
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        }
        startActivity(discoverableIntent)
    }
}
