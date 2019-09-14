package com.py.multipledevices

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.bluetooth_device_name.view.*

class BondedDevicesAdapter(val btDevices : ArrayList<BluetoothDevice>, val btAdapter: BluetoothAdapter) : RecyclerView.Adapter<BtViewHolder>(){
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
        val deviceMac = device.address
        holder.view.bt_device_name.text = deviceName
        holder.view.bt_device_mac.text = deviceMac
        holder.view.setOnClickListener {
            btAdapter.conn
        }
    }

    fun add(device: BluetoothDevice){
        btDevices.add(device)
        notifyItemInserted(btDevices.size - 1)
    }

    fun add(devices : List<BluetoothDevice>){
        btDevices.addAll(devices)
        notifyDataSetChanged()
    }
}

class BtViewHolder(val view : LinearLayout) : RecyclerView.ViewHolder(view)