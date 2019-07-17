/**
 * Auteur: Yann Lederrey
 * Date : 19 Juillet 2019
 * Cadre : Travail de Bachelor, Heig-VD, Securite de l'information
 * Projet : ProtectMe! VPN for everyone !
 * Github : https://github.com/yannled/protectMe-android
 * sources : ---
 *
 * Type de classe : Fragment
 * Vue correspondantes : fragment_config.xml
 * Explication : ce fragment correspond au premier appelé lors de la configuration du boitier
 * ProtectMe! ou lors de la récupération d'un profile openVPN. Ensuite est appelé le fragment
 * suivant (Config_Wifi_Fragment)
 *
 * Permissions demandees : Acces Wifi, lancement du Wifi, Acces Bluetooth, Acces Localisation
 * (si nécessaire si version >= Android 9)
 */

package zutt.protectme

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.widget.*
import kotlinx.android.synthetic.main.fragment_config.*

/**
 * Class Model to share configurations informations for the next configurations fragments
 */
class SharedViewModel : ViewModel() {
    var wifiSsid: String? = null
    var wifiPassword: String? = null
    var bluetoothName: String? = null
    var bluetoothMac: String? = null
    var action: String? = null
    var hash: String? = null
}

class Config_Fragment : Fragment() {
    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_LOCATION = 123
    private var enableBtIntent = Intent()
    private var mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private lateinit var ConfigurationModel: SharedViewModel

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

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onAttach(context: Context) {
        super.onAttach(context)

        // verify that location authorization are enable, if not active it
        // needed to get the ssid of the wifi with Oreo 8.1 android
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(this.activity!!, permissions, REQUEST_LOCATION)
        }

        // verify that Location is enable, needed to get Wifi ssid on android 8.1 to 9.
        val locationManager: LocationManager = this.context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager


        if (!locationManager.isLocationEnabled) {
            var alertDialog: AlertDialog.Builder = AlertDialog.Builder(context)
            alertDialog.setTitle(R.string.enable_LocationTitle)
            alertDialog.setMessage(R.string.enable_Location)
            alertDialog.setPositiveButton("Setting") { dialog, which ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent)
            }
            alertDialog.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }
            alertDialog.show()
        }

        // verify that wifi is enable, if not active it
        val wifiManager: WifiManager = this.context?.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }

        // verify that Bluetooth is enable, if not, ask to active it
        if (mBluetoothAdapter == null) {
            val toast: Toast = Toast(context)
            toast.createToast(context, getString(R.string.no_Bluetooth_config), Gravity.BOTTOM, 10)
        } else {
            if (!mBluetoothAdapter.isEnabled) {
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
        if (requestCode == this.REQUEST_LOCATION) {
            // Make sur the request was successful
            if (resultCode != Activity.RESULT_OK) {
                val toast = Toast(context)
                toast.createToast(this.context!!, getString(R.string.no_Location_config), Gravity.BOTTOM, 10)
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
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled) {
            startConfig()
        }
    }

    fun startConfig() {
        //modify button to continue configuration
        val color = ContextCompat.getColor(this.context!!, R.color.myBlueDark)
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
