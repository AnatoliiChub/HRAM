package com.achub.hram.ext

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable

private const val ENABLE_BLE_REQUEST_CODE = 19999

@SuppressLint("MissingPermission", "ComposableNaming")
@Composable
actual fun requestBluetooth() {
    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    LocalActivity.current?.startActivityForResult(intent, ENABLE_BLE_REQUEST_CODE)
}
