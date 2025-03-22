package com.augmenters.arglasses

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.annotation.RequiresPermission
import java.io.OutputStream
import java.util.*

class BluetoothHelper {
    private val deviceName = "SmartGlass"
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice() {
        bluetoothAdapter?.bondedDevices?.forEach { device ->
            if (device.name == deviceName) {
                try {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                    bluetoothSocket?.connect()
                    outputStream = bluetoothSocket?.outputStream
                    Log.d("BluetoothHelper", "Connected to $deviceName")
                } catch (e: Exception) {
                    Log.e("BluetoothHelper", "Connection failed: ${e.message}")
                }
            }
        }
    }

    fun sendMessage(message: String) {
        try {
            outputStream?.write((message + "\n").toByteArray())
            Log.d("BluetoothHelper", "Sent: $message")
        } catch (e: Exception) {
            Log.e("BluetoothHelper", "Failed to send message: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
            Log.d("BluetoothHelper", "Disconnected from $deviceName")
        } catch (e: Exception) {
            Log.e("BluetoothHelper", "Failed to disconnect: ${e.message}")
        }
    }
}