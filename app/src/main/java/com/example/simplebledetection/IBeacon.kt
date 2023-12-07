package com.example.simplebledetection

import android.bluetooth.le.ScanResult
import com.example.simplebledetection.utils.ConversionUtils


class IBeacon(scanResult: ScanResult, packetData: ByteArray) : BLEDevice(scanResult) {

    /**
     * beacon UUID
     */
    private var uuid: String = ""

    /**
     * packet raw data
     */
    private var rawByteData: ByteArray = ByteArray(30)

    /**
     * major minor, and their position (based on iBeacon specs)
     */
    private var major: Int? = null
    private val majorPosStart = 25
    private val majorPosEnd = 26

    private var minor: Int? = null
    private val minorPosStart = 27
    private val minorPosEnd = 28

    private var txPower: Int? = null
    private val txPowerPosStart = 29
    private val txPowerPosEnd = 29

    init {
        rawByteData = packetData

    }

    /**
     * Parse iBeacon UUID from packet
     */
    private fun parseUUID() {
        var startByte = 2
        while (startByte <= 5) {
            if (rawByteData[startByte + 2].toInt() and 0xff == 0x02 && rawByteData[startByte + 3].toInt() and 0xff == 0x15) {
                val uuidBytes = ByteArray(16)
                System.arraycopy(rawByteData, startByte + 4, uuidBytes, 0, 16)
                val hexString = ConversionUtils.bytesToHex(uuidBytes)
                if (!hexString.isNullOrEmpty()) {
                    uuid = hexString.substring(0, 8) + "-" +
                            hexString.substring(8, 12) + "-" +
                            hexString.substring(12, 16) + "-" +
                            hexString.substring(16, 20) + "-" +
                            hexString.substring(20, 32)
                    return
                }
            }
            startByte++
        }
    }

    /**
     * UUID getter method
     * if UUID is not calculated, calculate from packet raw data, then store to property
     */
    fun getUUID(): String {
        if (uuid.isNullOrEmpty()) {
            parseUUID()
        }
        return uuid
    }

    /**
     * Get iBeacon major
     * if major is not calculated, calculate from packet raw data, then store to property
     */
    fun getMajor(): Int {
        if (major == null)
            major = (rawByteData[majorPosStart].toInt() and 0xff) * 0x100 + (rawByteData[majorPosEnd].toInt() and 0xff)
        return major as Int
    }

    /**
     * Get iBeacon minor
     * if minor is not calculated, calculate from packet raw data, then store to property
     */
    fun getMinor(): Int {
        if (minor == null)
            minor = (rawByteData[minorPosStart].toInt() and 0xff) * 0x100 + (rawByteData[minorPosEnd].toInt() and 0xff)
        return minor as Int
    }

    /**
     * Get TxPower
     * if minor is not calculated, calculate from packet raw data, then store to property
     */

    /**
     * Get iBeacon TxPower
     * Extracts the transmission power value from the iBeacon packet
     */
    fun getTxPower(): Int {
        // The TxPower is located at the 29th byte of the iBeacon packet
        // It's an 8-bit signed value
        txPower = rawByteData[28].toInt()
        return txPower as Int
    }

    override fun toString(): String {
        return "Major= " + getMajor() + " Minor= " + getMinor() + " rssi=" + getRssi() + " txPower " + getTxPower()
    }
}