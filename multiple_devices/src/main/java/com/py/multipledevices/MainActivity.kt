package com.py.multipledevices

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.content.IntentFilter
import androidx.recyclerview.widget.LinearLayoutManager


class MainActivity : AppCompatActivity() {
    val REQUEST_ENABLE_BT = 980
    var bluetoothAdapter : BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val btBroadcastReceiver = BtBroadcastReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        configureReceiver()

        bluetooth_connect_button.setOnClickListener {
            connect()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            connect()
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(btBroadcastReceiver)
    }

    private fun connect(){
        if (bluetoothAdapter == null)
            throw Exception("Doesn't support bluetooth")
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        getDevices()
    }

    private fun getDevices(){
        val devicesList = ArrayList<BluetoothDevice>(bluetoothAdapter.bondedDevices)
        val adapter = BondedDevicesAdapter(devicesList)
        bluetooth_bonded_devices.adapter = adapter
        bluetooth_bonded_devices.layoutManager = LinearLayoutManager(this)
    }

    private fun configureReceiver(){
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        registerReceiver(btBroadcastReceiver, filter)
    }
}