package com.py.multipledevices

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.bluetooth_device_name.view.*

class BondedDevicesAdapter(val btDevices : ArrayList<BluetoothDevice>, val btServices: com.py.bluetooth_connection.BtServices) : RecyclerView.Adapter<BtViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BtViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.bluetooth_device_name, parent, false) as LinearLayout

        return BtViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return btDevices.size
    }

    override fun onBindViewHolder(holder: BtViewHolder, position: Int) {
        val device = btDevices.get(position)
        val deviceName = device.name
        holder.view.bt_device_name.text = deviceName
        if(device.bondState != BluetoothDevice.BOND_BONDED) {
            holder.view.bt_device_pair.visibility = View.VISIBLE
            holder.view.bt_device_pair.setOnClickListener {
                btServices.pair(device)
            }
        }
        holder.view.setOnClickListener {
            btServices.connect(device, device.uuids.get(0).uuid)
        }
    }

    fun getDevices(){
        val devicesList = ArrayList<BluetoothDevice>(btServices.getBluetoothAdapter().bondedDevices)
        add(devicesList)
    }

    fun add(device: BluetoothDevice){
        btDevices.add(device)
        notifyItemInserted(btDevices.size - 1)
    }

    private fun add(devices : List<BluetoothDevice>){
        btDevices.clear()
        btDevices.addAll(devices)
        notifyDataSetChanged()
    }
}

class BtViewHolder(val view : LinearLayout) : RecyclerView.ViewHolder(view)