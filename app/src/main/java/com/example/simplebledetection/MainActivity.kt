package com.example.simplebledetection

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.simplebledetection.databinding.ActivityMainBinding
import timber.log.Timber
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"

    private lateinit var scanService: ScanService
    private lateinit var adapter: DeviceListAdapter
    private lateinit var deviceList: ArrayList<Any>

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all { it.value }
            if (granted) {
                // All permissions are granted
                Timber.d("All permissions granted")
                scanService = ScanService(this, this.deviceList, this.adapter)
            } else {
                // Handle the case where permissions are denied
                Timber.d("Permissions denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.scanBtn.setOnClickListener { startScan() }
        binding.exitBtn.setOnClickListener { exitApp() }
        val recycleView: RecyclerView = findViewById(R.id.deviceList)
        deviceList = ArrayList()
        this.adapter = DeviceListAdapter(this.deviceList)
        recycleView.adapter = this.adapter

        // check for permission to scan BLE
        if (isPermissionGranted(this)) {
            Timber.d( ":-) - @onCreate init scan service")
            scanService = ScanService(this, this.deviceList, this.adapter)
        } else {
            requestPermissions()
        }
    }

    /**
     * exit application
     */
    private fun exitApp() {
        // if scanning service is running, stop scan then exit
        if (scanService.isScanning()) {
            binding.scanBtn.text = resources.getString(R.string.label_scan)
            scanService.stopBLEScan()
        }
        this@MainActivity.finish()
        exitProcess(0)
    }

    /**
     * Start BLE scan
     * Check Bluetooth before scanning.
     * If Bluetooth is disabled, request user to turn on Bluetooth
     */
    private fun startScan() {
        // check Bluetooth
        if (!scanService.isBluetoothEnabled()) {
            Timber.d(":-) - @startScan Bluetooth is disabled")
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(intent)
        } else {
            scanService.initScanner()
            // start scanning BLE device
            Timber.d( ":-) - scanService.initScanner()")
            if (scanService.isScanning()) {
                binding.scanBtn.text = resources.getString(R.string.label_scan)
                scanService.stopBLEScan()
            } else {
                scanService.startBLEScan()
                binding.scanBtn.text = resources.getString(R.string.label_scanning)
            }
        }
    }

    fun onRadioAllClicked(view: View) {

    }

    fun onRadioiBeaconClicked(view: View) {

    }

    // necessary permissions on Android <12
    private val BLE_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // necessary permissions on Android >=12
    private val ANDROID_12_BLE_PERMISSIONS = arrayOf(

        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    /**
     * Determine whether the location permission has been granted
     * if not, request the permission
     *
     * @param context
     * @return true if user has granted permission
     */

    private fun isPermissionGranted(context: Context): Boolean {
        Timber.d( ":-) - @isPermissionGranted: checking bluetooth")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if ((ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED)
            ) {
                Timber.d( "@isPermissionGranted: requesting Bluetooth on Android >= 12")
                ActivityCompat.requestPermissions(this, ANDROID_12_BLE_PERMISSIONS, 2)
                return false
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Timber.d("@isPermissionGranted: requesting Location on Android < 12")
                ActivityCompat.requestPermissions(this, BLE_PERMISSIONS, 3)
                return false
            }
        }
        Timber.d("@isPermissionGranted Bluetooth permission is ON")
        return true
    }


    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissionLauncher.launch(ANDROID_12_BLE_PERMISSIONS)
        } else {
            requestPermissionLauncher.launch(BLE_PERMISSIONS)
        }
    }

    private var requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Timber.d( "@requestBluetooth Bluetooth is enabled")
            } else {
                Timber.d( "@requestBluetooth Bluetooth usage is denied")
            }
        }
}