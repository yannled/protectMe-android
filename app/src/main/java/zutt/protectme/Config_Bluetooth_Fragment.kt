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
import android.os.AsyncTask
import android.widget.AdapterView
import java.io.IOException
import java.util.*


class Config_Bluetooth_Fragment : Fragment() {

    companion object {
        lateinit var configurationModel: SharedViewModel
        var mBluetoothAdapter: BluetoothAdapter? = null
        val DeviceList = arrayListOf<BluetoothDevice>() //list to get informations from selected bluetooth host
        val mDeviceList = arrayListOf<String>() //list to show name in ListView
        var m_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        var m_isConnected: Boolean = false
        var m_device: BluetoothDevice? = null
        lateinit var m_address: String
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

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        mBluetoothAdapter!!.startDiscovery()

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        activity!!.registerReceiver(mReceiver, filter)

        listBlueTooth.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            // This is your listview's selected item
            val item = parent.getItemAtPosition(position)

            for (device in DeviceList){
                if(device.name == item.toString()){
                    configurationModel.bluetoothName = device.name
                    configurationModel.bluetoothMac = device.address

                    button_config_Bluetooth.setText(R.string.start_config)
                    button_config_Bluetooth.setBackgroundResource(R.color.myBlueDark)
                    button_config_Bluetooth.isClickable = true

                    //On click, go to the next fragment configuration
                    button_config_Bluetooth.setOnClickListener { view ->
                        ConnectToDevice(this.parentFragment!!.context!!).execute()
                        sendCommand("hello")

                    }
                }
            }
        }
    }

    override fun onDestroy() {
        activity!!.unregisterReceiver(mReceiver)
        super.onDestroy()
    }

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device = intent
                        .getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                if (!device.name.isNullOrEmpty()) {
                    if(!mDeviceList.contains(device.name)) {
                        DeviceList.add(device)
                        mDeviceList.add(device.name)
                    }
                }

                listBlueTooth.adapter = ArrayAdapter<String>(context,
                        android.R.layout.simple_list_item_1, mDeviceList)
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

        override fun onPreExecute() {
            super.onPreExecute()
            m_progress = ProgressDialog.show(context, "Connecting...", "please wait")
        }

        override fun doInBackground(vararg params: Void?): String? {
             try {
                 if (m_bluetoothSocket == null || !m_isConnected){
                    val device : BluetoothDevice = mBluetoothAdapter!!.getRemoteDevice(configurationModel.bluetoothMac)
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
            if(!connectSuccess){
                Log.i("data", "couldn't connect")
            }
            else{
                m_isConnected = true
            }

            m_progress.dismiss()
        }
    }

}
