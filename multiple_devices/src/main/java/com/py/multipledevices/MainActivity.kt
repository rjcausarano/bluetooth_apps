package com.py.multipledevices

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*


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
    val btCallbacks : BtCallbacks = object: BtCallbacks{
        override fun onDeviceFound(device: BluetoothDevice) {
            adapter.add(device)
        }

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
    val btServices = BtServices(handler, this, btCallbacks)
    val adapter = BondedDevicesAdapter(ArrayList(), btServices)
    var vibrator : Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        configureReceiver()
        bluetooth_bonded_devices.adapter = adapter
        bluetooth_bonded_devices.layoutManager = LinearLayoutManager(this)
        btServices.setListeningOnDisconnect(true)
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
        unregisterReceiver(btServices)
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
        registerReceiver(btServices, filter)
    }

    private fun enableDiscovery(){
        // discoverable for 300 seconds. 0 for always discoverable
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        }
        startActivity(discoverableIntent)
    }

    private fun showToast(msg:String){
        vibrator?.vibrate(400)
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
