package com.achub.hram

import android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED
import android.bluetooth.BluetoothAdapter.EXTRA_STATE
import android.bluetooth.BluetoothAdapter.STATE_OFF
import android.bluetooth.BluetoothAdapter.STATE_ON
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BluetoothStateAndroid(val context: Context) : BluetoothState {
    override val isBluetoothOn: StateFlow<Boolean> = MutableStateFlow(getBluetoothAdapterOrNull()?.state == STATE_ON)

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isBluetoothOnState = STATE_ON == intent?.getIntExtra(EXTRA_STATE, STATE_OFF)
            (isBluetoothOn as MutableStateFlow).value = isBluetoothOnState
        }

    }

    override fun init() {
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(ACTION_STATE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }


    override fun release() = context.unregisterReceiver(receiver)

    fun getBluetoothAdapterOrNull() = ContextCompat.getSystemService(context, BluetoothManager::class.java)?.adapter
}
