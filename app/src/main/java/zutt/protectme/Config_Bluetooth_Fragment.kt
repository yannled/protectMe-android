package zutt.protectme

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.bluetooth.*
import android.content.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.os.AsyncTask
import kotlinx.android.synthetic.main.fragment_bluetooth_config.*
import java.io.IOException
import java.util.*
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar


class Config_Bluetooth_Fragment : Fragment() {

    companion object {
        var PBOXNAME = "ProtectMe"
        lateinit var configurationModel: SharedViewModel
        var mBluetoothAdapter: BluetoothAdapter? = null
        var m_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        var m_isConnected: Boolean = false
    }
    lateinit var progressBar : ProgressBar
    lateinit var button_config_Bluetooth : Button

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

        progressBar = view!!.findViewById(R.id.progressBar)
        button_config_Bluetooth = view!!.findViewById(R.id.button_config_Bluetooth)

        configurationModel = activity?.run {
            ViewModelProviders.of(this).get(SharedViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        // Start bluetooth discovery
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        mBluetoothAdapter!!.startDiscovery()

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        activity!!.registerReceiver(mReceiver, filter)

    }

    override fun onDestroy() {
        activity!!.unregisterReceiver(mReceiver)
        super.onDestroy()
    }

    @SuppressLint("NewApi")
// create the communication canal with the Protect Me Box
    fun contactDevice() {
        ConnectToDevice(this.context!!).execute()

        // Start communication when click on button
        button_config_Bluetooth.setOnClickListener { view ->
            progressBar.visibility = View.VISIBLE
            button_config_Bluetooth.visibility = View.INVISIBLE
            textView_bluetooth.setText(R.string.wait_Bluetooth_conf)

            Communicate(this.context!!).execute()
        }
    }

    // disconnect bluetooth Socket
    private fun disconnect() {
        if (m_bluetoothSocket != null) {
            try {
                m_bluetoothSocket!!.close()
                m_bluetoothSocket = null
                m_isConnected = false
                mBluetoothAdapter!!.disable()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun sendCommand(input: String?) {
        if (m_bluetoothSocket != null && input != null) {
            try {
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun receiveCommand(lengthToRead: Int?): String? {
        if (m_bluetoothSocket != null) {
            var bufferSize = 1024

            if(lengthToRead != null)
                bufferSize = lengthToRead

            val buffer = ByteArray(bufferSize)
            val length = m_bluetoothSocket!!.inputStream.read(buffer)
            val msg = String(buffer, Charsets.UTF_8)
            return msg.substring(0, length)
        }
        return null
    }

    private fun writeVpnProfileToFile(data: String, context: Context) {
        val files = FileManager(context)
        val filename = configurationModel.wifiSsid!! + "_" + configurationModel.bluetoothName!! + "_" + configurationModel.bluetoothMac!!
        if (!files.addFile(filename, data)) {
            Log.d("FileManagerError", "ERROR FILE CREATION")
        }
    }

    // Handling the bluetooth discovery informations
    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            progressBar.visibility = View.VISIBLE
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device = intent
                        .getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                // if one bluetooth device find is our ProtectMe Box
                if (!device.name.isNullOrEmpty() && device.name == PBOXNAME) {
                    // We add device information on our shared model
                    configurationModel.bluetoothName = device.name
                    configurationModel.bluetoothMac = device.address

                    // Start connection then communication with the ProtectMe Box
                    contactDevice()
                }
            }
        }
    }

    // create the communication canal between the pair Bluetooth devices
    inner class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true

        override fun doInBackground(vararg params: Void?): String? {
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {
                    val device: BluetoothDevice = mBluetoothAdapter!!.getRemoteDevice(configurationModel.bluetoothMac)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_UUID)
                    mBluetoothAdapter!!.cancelDiscovery()
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
            // update the interface to begin communication
            progressBar.visibility = View.INVISIBLE
            button_config_Bluetooth.setText(R.string.start_config)
            button_config_Bluetooth.setBackgroundResource(R.color.myBlueDark)
            button_config_Bluetooth.isClickable = true
        }
    }

    inner class Communicate(c: Context) : AsyncTask<Void, Void, String>() {
        var ovpnProfile: String? = null
        var context: Context? = null

        init {
            this.context = c
        }

        override fun doInBackground(vararg params: Void?): String? {
            var dh = DiffieHellmann()

            //Send the choice action between configure box for the first use or add phone to box
            sendCommand(configurationModel.action)

            // Generation of private and public keys
            dh.generateKeys()

            // We send our publicKey, p and g
            sendCommand(dh.getPublicKey())
            sendCommand("endCryptoExchange")

            // we receive PublicKey from the ProtectMe Box
            val lengthPublicKey = receiveCommand(null)
            val receivedPublicKey = receiveCommand(lengthPublicKey!!.toInt())

            // We set the receivedPublicKey
            dh.setReceivePublicKey(receivedPublicKey)

            // We Generate SecretSharedKey
            dh.generateCommonSecretKey()

            var plaintext = ""
            if(configurationModel.action.equals("Update")){
                var plaintext = "{" + configurationModel.hash + "}"
                val cypherText = dh.encryptMessage(plaintext)
                sendCommand(cypherText)
            }
            else {
                // We send Wifi configurations
                plaintext = "{" + configurationModel.wifiSsid + "}{" + configurationModel.wifiPassword + "}"
                val cypherText = dh.encryptMessage(plaintext)
                sendCommand(cypherText)

                // We get the OpenVPN profile from the ProtectMe Box(.ovpn file)
                val lengthCipherText = receiveCommand(null)
                sendCommand("Received")
                //Sleep 2 second during the box send the entiere ovpn file
                Thread.sleep(2000)
                //val ovpnProfile = receiveCommand(lengthCipherText!!.toInt())
                val cipherOvpnFile = receiveCommand(lengthCipherText!!.toInt())
                ovpnProfile = dh.decryptMessage(cipherOvpnFile!!)

                writeVpnProfileToFile(ovpnProfile!!, this.context!!)
            }
            disconnect()

            return null
        }

        override fun onPostExecute(result: String?) {
            //Change display informations
            progressBar.visibility = View.INVISIBLE
            textView_bluetooth.visibility =  View.INVISIBLE
            Bluetooth_config_title.text = getString(R.string.title_Bluetooth_conf_Ok)
            textView_bluetooth_Information.visibility = View.VISIBLE
        }
    }
}


