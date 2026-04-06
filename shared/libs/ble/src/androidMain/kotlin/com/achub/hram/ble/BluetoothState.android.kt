@file:Suppress("ktlint:standard:filename", "detekt:Filename")

package com.achub.hram.ble

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
import com.achub.hram.Logger
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

class BluetoothStateAndroid(context: Context) : BluetoothState {
    companion object {
        private const val TAG = "BluetoothStateAndroid"
    }

    override val isBluetoothOn: Flow<Boolean> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val isBluetoothOnState = STATE_ON == intent?.getIntExtra(EXTRA_STATE, STATE_OFF)
                trySendBlocking(isBluetoothOnState)
                    .onFailure { Logger.e(TAG) { "BluetoothState: failed: $it" } }
            }
        }

        val adapter = ContextCompat.getSystemService(context, BluetoothManager::class.java)?.adapter
        trySendBlocking(adapter?.state == STATE_ON)
            .onFailure { Logger.e(TAG) { "BluetoothState initial: failed: $it" } }

        context.registerReceiver(receiver, IntentFilter(ACTION_STATE_CHANGED))

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }.buffer(CONFLATED)
}
