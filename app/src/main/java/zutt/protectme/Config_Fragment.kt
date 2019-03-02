package zutt.protectme

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.net.wifi.WifiManager
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.widget.*
import kotlinx.android.synthetic.main.fragment_config.*


class Config_Fragment : Fragment() {
    private val REQUEST_ENABLE_BT = 1
    private var enableBtIntent = Intent()
    private var mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    fun Toast.createToast(context: Context, message: String, gravity: Int, duration: Int) {
        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        /*first parameter is the layout you made
        second parameter is the root view in that xml
         */
        val layout = inflater.inflate(R.layout.custom_toast, (context as Activity).findViewById(R.id.custom_toast_container))

        layout.findViewById<TextView>(R.id.textToast).text = message
        setGravity(gravity, 0, 10)
        setDuration(Toast.LENGTH_LONG)

        view = layout
        show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_config, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val wifiManager: WifiManager = this.context?.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if(!wifiManager.isWifiEnabled){
            wifiManager.isWifiEnabled = true
        }

        if (mBluetoothAdapter == null) {
            val toast: Toast = Toast(context)
            toast.createToast(context, getString(R.string.no_Bluetooth_config), Gravity.BOTTOM, 10)
        }
        else {
            if(!mBluetoothAdapter.isEnabled) {
                this.enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(this.enableBtIntent, this.REQUEST_ENABLE_BT)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == this.REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                startConfig()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if(mBluetoothAdapter!=null && mBluetoothAdapter.isEnabled){
            startConfig()
        }
    }

    fun startConfig(){
        val color = ContextCompat.getColor(this.context!!,R.color.myBlueDark)
        button_config.setText(R.string.begin_config)
        button_config.setBackgroundColor(color)
        button_config.isClickable = true

        button_config.setOnClickListener { view ->
            val nextFrag = Config_Wifi_Fragment()
            activity!!.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, nextFrag)
                    .addToBackStack(null)
                    .commit()
        }
    }
}
