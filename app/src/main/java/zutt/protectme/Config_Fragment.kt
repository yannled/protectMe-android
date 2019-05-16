package zutt.protectme

import android.app.Activity
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
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

/**
 * Class Model to share configurations informations for the next configurations fragments
 */
class SharedViewModel : ViewModel(){
    var wifiSsid : String? = null
    var wifiPassword : String? = null
    var bluetoothName : String? = null
    var bluetoothMac : String? = null
    var action : String? = null
}

class Config_Fragment : Fragment() {
    private val REQUEST_ENABLE_BT = 1
    private var enableBtIntent = Intent()
    private var mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private lateinit var ConfigurationModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    //create personalised Toast
    fun Toast.createToast(context: Context, message: String, gravity: Int, duration: Int) {
        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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

        // verify that wifi is enable, if not active it
        val wifiManager: WifiManager = this.context?.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if(!wifiManager.isWifiEnabled){
            wifiManager.isWifiEnabled = true
        }

        // verify that Bluetooth is enable, if not, ask to active it
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
                // After bluetooth is activated start configuration
                startConfig()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Load the shared Model
        ConfigurationModel = activity?.run {
            ViewModelProviders.of(this).get(SharedViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        // if bluetooth is already enable, start configuration
        if(mBluetoothAdapter!=null && mBluetoothAdapter.isEnabled){
            startConfig()
        }
    }

    fun startConfig(){
        //modify button to continue configuration
        val color = ContextCompat.getColor(this.context!!,R.color.myBlueDark)
        button_config.setText(R.string.begin_config)
        button_config.setBackgroundColor(color)
        button_config.isClickable = true

        //On click, go to the next fragment configuration
        button_config.setOnClickListener { view ->
            ConfigurationModel.action = "FirstConfig"
            val nextFrag = Config_Wifi_Fragment()
            activity!!.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, nextFrag)
                    .addToBackStack(null)
                    .commit()
        }

        button_addPhone.setText(R.string.begin_addPhone)
        button_addPhone.setBackgroundColor(color)
        button_addPhone.isClickable = true

        //On click, go to the next fragment configuration
        button_addPhone.setOnClickListener { view ->
            ConfigurationModel.action = "AddPhone"
            val nextFrag = Config_Wifi_Fragment()
            activity!!.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, nextFrag)
                    .addToBackStack(null)
                    .commit()
        }
    }
}
