package com.appsinthesky.bluetoothtutorial

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import kotlinx.android.synthetic.main.control_layout.*
import java.io.IOException
import java.util.*



class ControlActivity: AppCompatActivity() {

    enum class Commands(val com: String) {
        MANUAL_DRIVE("1"),
        AUTO_LF("2"),
        AUTO_UO("3"),
        BUZZ("4"),
        GO_FORWARD("5"),
        GO_BACK("6"),
        GO_LEFT("7"),
        GO_RIGHT("8"),
        PLAY_SONG("9"),
        STOP("13")
    }

    var global_angle : Int = 0
    var global_strength : Int = 0

    var tmp_angle : Int = 0
    var tmp_strength : Int = 0

    val gap : Int = 3

    companion object {
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        var m_isConnected: Boolean = false

        lateinit var m_bluetoothAdapter: BluetoothAdapter
        lateinit var m_address: String
    }

    private fun sendCommand(input: String) {
        if (m_bluetoothSocket != null) {
            try{
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch(e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun disconnect() {
        if (m_bluetoothSocket != null) {
            try {
                m_bluetoothSocket!!.close()
                m_bluetoothSocket = null
                m_isConnected = false
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control_layout)
        m_address = intent.getStringExtra(SelectDeviceActivity.EXTRA_ADDRESS)

        ConnectToDevice(this).execute()


        joystickView.setOnMoveListener { angle, strength ->

            tmp_angle = global_angle
            tmp_strength = global_strength

            if (angle >= global_angle + gap  || angle <= global_angle - gap)
                tmp_angle = angle

            if (strength >= global_strength + gap || strength <= global_strength - gap)
                tmp_strength = strength

            if (global_angle != tmp_angle || global_strength != tmp_strength)
            {
                global_angle = tmp_angle
                global_strength = tmp_strength

                /* Data format: <mode> + <angel> + <strength> */
                val data: String = Commands.MANUAL_DRIVE.com + "+" + angle.toString() + "+" + strength.toString()

                textView_data.text = data
                sendCommand(data)
                //if (global_strength >= gap) {}
                //else {
                //    textView_data.text = "STOP"
                //    sendCommand(Commands.STOP.com)
                //}
            }
        }


        button_linefollow.setOnClickListener { sendCommand(Commands.AUTO_LF.com) }
        button_voidance.setOnClickListener { sendCommand(Commands.AUTO_UO.com) }
        button_buzz.setOnClickListener { sendCommand(Commands.BUZZ.com) }
        button_stop.setOnClickListener { sendCommand(Commands.STOP.com) }

        button_up.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                sendCommand(Commands.GO_FORWARD.com)
            } else if (event.action == MotionEvent.ACTION_UP) {
                sendCommand(Commands.STOP.com)
            }
            true
        }


        button_down.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                sendCommand(Commands.GO_BACK.com)
            } else if (event.action == MotionEvent.ACTION_UP) {
                sendCommand(Commands.STOP.com)
            }
            true
        }

        button_left.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                sendCommand(Commands.GO_LEFT.com)
            } else if (event.action == MotionEvent.ACTION_UP) {
                sendCommand(Commands.STOP.com)
            }
            true
        }

        button_right.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                sendCommand(Commands.GO_RIGHT.com)
            } else if (event.action == MotionEvent.ACTION_UP) {
                sendCommand(Commands.STOP.com)
            }
            true
        }

        button_song.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                sendCommand(Commands.PLAY_SONG.com)
            }
            true
        }

        button_disconnect.setOnClickListener { disconnect() }
    }

    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true
        private val context: Context

        init {
            this.context = c
        }

        override fun doInBackground(vararg p0: Void?): String? {
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    m_bluetoothSocket!!.connect()
                }
            } catch (e: IOException) {
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess) {
                Log.i("data", "couldn't connect")
            } else {
                m_isConnected = true
            }
        }
    }
}