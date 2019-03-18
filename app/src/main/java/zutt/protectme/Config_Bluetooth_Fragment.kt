package zutt.protectme

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
import java.io.IOException
import java.security.KeyPair
import java.security.KeyPairGenerator
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

    fun connectToPBOX(){
        button_config_Bluetooth.setText(R.string.start_config)
        button_config_Bluetooth.setBackgroundResource(R.color.myBlueDark)
        button_config_Bluetooth.isClickable = true

        ConnectToDevice(this.context!!).execute()
        button_config_Bluetooth.setOnClickListener { view ->
            sendCommand("hello")
            var message = receiveCommand()
            disconnect()
        }
    }

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device = intent
                        .getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                if (!device.name.isNullOrEmpty() && device.name == PBOXNAME) {
                    configurationModel.bluetoothName = device.name
                    configurationModel.bluetoothMac = device.address
                    progressBar.visibility = View.INVISIBLE
                    connectToPBOX()
                }
            }
        }
    }

    private fun sendCommand(input: String){
        if(m_bluetoothSocket != null){
            try {
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun receiveCommand() : String?{
        if(m_bluetoothSocket != null){

                val buffer = ByteArray(256)
                var msg = ""
                while(true) {
                    try {
                        val length = m_bluetoothSocket!!.inputStream.read()
                        msg += String(buffer, 0, length)
                    } catch (e: IOException) {
                        break;
                    }
                }
                return msg
        }
                return null
    }

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

    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true
        private var context: Context

        init {
            this.context = c
        }

        /*
        override fun onPreExecute() {
            super.onPreExecute()
            m_progress = ProgressDialog.show(context, "Connecting...", "please wait")
        }*/

        override fun doInBackground(vararg params: Void?): String? {
             try {
                 if (m_bluetoothSocket == null || !m_isConnected){
                    val device : BluetoothDevice = mBluetoothAdapter!!.getRemoteDevice(configurationModel.bluetoothMac)
                     //m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_UUID)
                     m_bluetoothSocket = device.createRfcommSocketToServiceRecord(m_UUID)
                     mBluetoothAdapter!!.cancelDiscovery()
                     m_bluetoothSocket!!.connect()

                     var dh : DiffieHellmann = DiffieHellmann()
                     dh.generateKeys()
                     //TODO :  recevoir public key
                     //dh.setReceivePublicKey(publicKey)
                     dh.generateCommonSecretKey()
                     dh.encryptMessage("Hello")

                     //TODO envoyer

                     //TODO recevoir

                     //dh.decryptMessage()
                 }

             } catch (e: IOException) {
                 connectSuccess = false
                 e.printStackTrace()
             }

            return null
        }

        /*
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(!connectSuccess){
                Log.i("data", "couldn't connect")
            }
            else{
                m_isConnected = true
            }

            m_progress.dismiss()
        }
        */
    }

}
