package zutt.protectme

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.arch.lifecycle.ViewModelProviders
import android.bluetooth.*
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.fragment_bluetooth_config.*
import android.content.IntentFilter
import android.content.Intent
import android.content.BroadcastReceiver
import android.opengl.Visibility
import android.os.AsyncTask
import android.widget.AdapterView
import android.util.Base64
import java.io.IOException
import java.util.*
import javax.crypto.spec.SecretKeySpec


class Config_Bluetooth_Fragment : Fragment() {

    companion object {
        var PBOXNAME = "ProtectMe"
        lateinit var configurationModel: SharedViewModel
        var mBluetoothAdapter: BluetoothAdapter? = null
        var m_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        var m_isConnected: Boolean = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bluetooth_config, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        requestPermissions(permissions, 0)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        configurationModel = activity?.run {
            ViewModelProviders.of(this).get(SharedViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        // Start bluetooth discovery
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        mBluetoothAdapter!!.startDiscovery()

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        activity!!.registerReceiver(mReceiver, filter)

        // show circle waiting progress bar
        progressBar.visibility = View.VISIBLE

    }

    override fun onDestroy() {
        activity!!.unregisterReceiver(mReceiver)
        super.onDestroy()
    }

    @SuppressLint("NewApi")
// create the communication canal with the Protect Me Box
    fun communicate(){
        ConnectToDevice(this.context!!).execute()

        // Start communication when click on button
        button_config_Bluetooth.setOnClickListener { view ->

            // Start crypted communication
            var dh  = DiffieHellmann()

            // Generation of private and public keys
            dh.generateKeys()

            // TODO : Store the keys in properties file the change getPublicKey to store PublicKey
            // TODO : retrive the publickey to send it
            // We send our publicKey, p and g

            sendCommand(dh.getPublicKey())
            sendCommand("endCryptoExchange")

            var receivedPublicKey = receiveCommand()

            dh.setReceivePublicKey(receivedPublicKey)

            dh.generateCommonSecretKey()

            var plaintext = "{" + configurationModel.wifiSsid + "}{" + configurationModel.wifiPassword + "}"
            val cypherText = dh.encryptMessage(plaintext)
            var test = cypherText.toString()
            //var test2 = Base64.encodeToString(cypherText.toByteArray(Charsets.UTF_8), Base64.NO_WRAP
            sendCommand(cypherText)

            disconnect()
        }
    }

    // disconnect bluetooth Socket
    private fun disconnect(){
        if (m_bluetoothSocket != null){
            try {
                m_bluetoothSocket!!.close()
                m_bluetoothSocket = null
                m_isConnected = false
            } catch (e: IOException){
                e.printStackTrace()
            }
        }
    }

    private fun sendCommand(input: String?){
        if(m_bluetoothSocket != null && input != null){
            try {
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun receiveCommand() : String?{
        if(m_bluetoothSocket != null){
            var buffer : ByteArray = ByteArray(1024)
            val length = m_bluetoothSocket!!.inputStream.read(buffer)
            var msg = String(buffer, Charsets.UTF_8)
            return msg.substring(0, length)
        }
        return null
    }

    // Handling the bluetooth discovery informations
    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device = intent
                        .getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                // if one bluetooth device find is our ProtectMe Box
                if (!device.name.isNullOrEmpty() && device.name == PBOXNAME) {
                    // We add device information on our shared model
                    configurationModel.bluetoothName = device.name
                    configurationModel.bluetoothMac = device.address

                    // hide the cicrcle waiting progress bar
                    progressBar.visibility = View.INVISIBLE

                    // update the button interface to begin communication
                    button_config_Bluetooth.setText(R.string.start_config)
                    button_config_Bluetooth.setBackgroundResource(R.color.myBlueDark)
                    button_config_Bluetooth.isClickable = true

                    // Start connection then communication with the ProtectMe Box
                    communicate()
                }
            }
        }
    }

    // create the communication canal between the pair Bluetooth devices
    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true
        private var context: Context

        init {
            this.context = c
        }

        override fun doInBackground(vararg params: Void?): String? {
             try {
                 if (m_bluetoothSocket == null || !m_isConnected){
                    val device : BluetoothDevice = mBluetoothAdapter!!.getRemoteDevice(configurationModel.bluetoothMac)
                     //m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_UUID)
                     m_bluetoothSocket = device.createRfcommSocketToServiceRecord(m_UUID)
                     mBluetoothAdapter!!.cancelDiscovery()
                     m_bluetoothSocket!!.connect()
                 }

             } catch (e: IOException) {
                 connectSuccess = false
                 e.printStackTrace()
             }

            return null
        }

    }

}
