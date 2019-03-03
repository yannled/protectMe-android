package zutt.protectme

import android.arch.lifecycle.ViewModelProviders
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.fragment_bluetooth_config.*

class Config_Bluetooth_Fragment : Fragment() {

    private lateinit var configurationModel: SharedViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bluetooth_config, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        requestPermissions(permissions,0)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        configurationModel = activity?.run {
            ViewModelProviders.of(this).get(SharedViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    private val bleScanner = object :ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            devices_bluetooth.text = "onScanResult: ${result?.device?.address} - ${result?.device?.name}"
            Log.d("DeviceListActivity","onScanResult: ${result?.device?.address} - ${result?.device?.name}")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            devices_bluetooth.text = results.toString()
            Log.d("DeviceListActivity","onBatchScanResults:${results.toString()}")
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            devices_bluetooth.text = "onScanFailed: $errorCode"
            Log.d("DeviceListActivity", "onScanFailed: $errorCode")
        }

    }

    private val bluetoothLeScanner: BluetoothLeScanner
        get() {
            val bluetoothManager = context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter
            return bluetoothAdapter.bluetoothLeScanner
        }

    class ListDevicesAdapter(context: Context?, resource: Int) : ArrayAdapter<String>(context, resource)

    override fun onStart() {
        devices_bluetooth.text = "onStart()"
                Log.d("DeviceListActivity","onStart()")
        super.onStart()

        bluetoothLeScanner.startScan(bleScanner)

    }

    override fun onStop() {
        bluetoothLeScanner.stopScan(bleScanner)
        super.onStop()
    }
}
