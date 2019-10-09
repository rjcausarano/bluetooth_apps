package com.py.bluetooth_connection

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.*

class BtServices(val btHandler : Handler, val activity: Activity, val btCallbacks: BtCallbacks? = null) : BroadcastReceiver(){
    companion object{
        private val TAG = BtServices::class.simpleName
        private val NAME_SECURE = "BTServices Secure"
        private val NAME_INSECURE = "BTServices Insecure"
        private val UUID_SECURE = UUID.fromString("5768b4a8-f0ec-42f8-992d-253e1bde669c")
        private val UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")
        val REQUEST_ENABLE_BT = 980
        val REQUEST_LOCATION_PERMISSION = 283
    }
    private var connectedThread : ConnectedThread? = null
    private var connectThread : ConnectThread? = null
    private var acceptThread : AcceptThread? = null
    private val bluetoothAdapter : BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var shouldListenOnDisconnect = false
    private var secureConnection = true

    fun isConnected() : Boolean{
        return connectedThread != null
    }

    private fun cleanThreads(){
        connectThread?.cancel()
        connectThread = null
        connectedThread?.cancel()
        connectedThread = null
        acceptThread?.cancel()
        acceptThread = null
    }

    fun connect(btDevice: BluetoothDevice, uuid: UUID){
        //TODO check for nullness and end thread if already running before running below line
        cleanThreads()
        connectThread = ConnectThread(btDevice, uuid)
        connectThread!!.start()
    }

    fun acceptConnections(){
        cleanThreads()
        acceptThread = AcceptThread()
        acceptThread!!.start()
    }

    fun write(msg : String){
        if(isConnected()){
            connectedThread?.write(msg.toByteArray(StandardCharsets.US_ASCII))
        }
    }

    fun pair(device: BluetoothDevice){
        if(device.bondState != BluetoothDevice.BOND_BONDED)
            device.createBond()
    }

    fun enable(){
        if (bluetoothAdapter == null)
            throw Exception("Doesn't support bluetooth")
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBtIntent,
                REQUEST_ENABLE_BT
            )
        }
    }

    fun scanDevices(){
        if(ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }
        bluetoothAdapter.startDiscovery()
    }

    fun getBluetoothAdapter() : BluetoothAdapter{
        return bluetoothAdapter
    }

    private fun showToast(msg : String){
        activity.runOnUiThread{
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
        }
    }

    fun isListeningOnDisconnect() : Boolean{
        return shouldListenOnDisconnect
    }

    fun setListeningOnDisconnect(shouldListen : Boolean){
        shouldListenOnDisconnect = shouldListen
    }

    fun isSecureConnection() : Boolean{
        return secureConnection
    }

    fun setSecureConnection(secureConnection : Boolean){
        this.secureConnection = secureConnection
    }

    private inner class ConnectThread(device: BluetoothDevice, uuid : UUID) : Thread() {
        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            if(secureConnection)
                device.createRfcommSocketToServiceRecord(uuid)
            else
                device.createInsecureRfcommSocketToServiceRecord(uuid)
        }

        override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery()
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket!!.connect()
            } catch (e: IOException) {
                // Close the socket
                try {
                    mmSocket!!.close()
                    showToast("Connection timeout!")
                } catch (e2: IOException) {
                    Log.e(
                        TAG, "unable to close() socket during connection failure", e2
                    )
                }
                return
            }

            synchronized(this@BtServices){
                connectThread = null
            }

            connectedThread = ConnectedThread(mmSocket!!)
            connectedThread!!.start()
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                Log.d(TAG, "closing client socket")
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024)

        override fun run() {
            var numBytes: Int // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    Log.e(TAG, "Input stream was disconnected", e)
                    break
                }

                // Send the obtained bytes to the UI activity.
                val readMsg = btHandler.obtainMessage(
                    Constants.MESSAGE_READ, numBytes, -1,
                    mmBuffer
                )
                readMsg.sendToTarget()
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)

                // Send a failure message back to the activity.
                val writeErrorMsg = btHandler.obtainMessage(Constants.MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                btHandler.sendMessage(writeErrorMsg)
                return
            }

            // Share the sent message with the UI activity.
            val writtenMsg = btHandler.obtainMessage(
                Constants.MESSAGE_WRITE, -1, -1, mmBuffer)
            writtenMsg.sendToTarget()
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                Log.d(TAG, "shutting down connection")
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

    private inner class AcceptThread : Thread() {

        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            if(secureConnection)
                bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, UUID_SECURE)
            else
                bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE, UUID_INSECURE)
        }

        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                    mmServerSocket?.close()
                    connectedThread = ConnectedThread(it)
                    connectedThread!!.start()
                    shouldLoop = false
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                Log.d(TAG, "closing connect socket")
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

    // Broadcast receiver
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
                if(btCallbacks == null)
                    return
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                btCallbacks.onDeviceFound(device!!)
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
                if(shouldListenOnDisconnect)
                    acceptConnections()
            }
            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                if(btCallbacks == null)
                    return
                val bondState : Int = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                when(bondState){
                    BluetoothDevice.BOND_BONDED -> btCallbacks.onPairedStateChanged(true)
                    BluetoothDevice.BOND_NONE -> btCallbacks.onPairedStateChanged(false)
                }
            }
        }
    }
}