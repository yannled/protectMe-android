/**
 * Auteur: Yann Lederrey
 * Date : 19 Juillet 2019
 * Cadre : Travail de Bachelor, Heig-VD, Securite de l'information
 * Projet : ProtectMe! VPN for everyone !
 * Github : https://github.com/yannled/protectMe-android
 * sources : ---
 *
 * Type de classe : Fragment
 * Vue correspondantes : fragment_wifi_config.xml
 * Explication : ce fragment permet de récupérer le SSID du wifi actuellement connecté ainsi que le
 * mot de passe correspondant afin de passer au fragment suivant (Config_Bluetooth_Fragment)
 */

package zutt.protectme

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.net.wifi.WifiManager
import android.support.v4.content.ContextCompat
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.fragment_wifi_config.*


class Config_Wifi_Fragment : Fragment() {

    private var ssid : String? = null
    private lateinit var ConfigurationModel: SharedViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wifi_config, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // Get the SSID of the connected wifi
        val wifiManager = context!!.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        //get SSID and escape the " "
        ssid = wifiInfo.ssid.substring(1, wifiInfo.ssid.length-1)
        wifiManager.disconnect()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Load the shared Model
        ConfigurationModel = activity?.run {
            ViewModelProviders.of(this).get(SharedViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
        ConfigurationModel.wifiSsid = ssid

        // if this is a FirstConfiguration we ask password to the user
        if(ConfigurationModel.action!!.equals("FirstConfig")) {
            ssid_config_wifi.text = ssid

            // When user has finish to edit the second password EditText
            password_wifi2.setOnEditorActionListener() { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    //we verify that password are the same
                    val firstPassword = password_wifi1.text
                    val secondPassword = password_wifi2.text

                    if (!firstPassword.toString().equals(secondPassword.toString())) {
                        //error message
                        password_ok_config_wifi.visibility = View.VISIBLE
                        password_wifi1.text.clear()
                        password_wifi2.text.clear()
                    } else {
                        ConfigurationModel.wifiPassword = secondPassword.toString()

                        password_ok_config_wifi.visibility = View.INVISIBLE

                        //modify button to continue configuration
                        val color = ContextCompat.getColor(this.context!!, R.color.myBlueDark)
                        button_config_Wifi.setText(R.string.next_config)
                        button_config_Wifi.setBackgroundColor(color)
                        button_config_Wifi.isClickable = true

                        //On click, go to the next fragment configuration
                        button_config_Wifi.setOnClickListener { view ->
                            val nextFrag = Config_Bluetooth_Fragment()
                            activity!!.supportFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, nextFrag)
                                    .addToBackStack(null)
                                    .commit()
                        }
                    }
                    false
                } else {
                    false
                }
            }
        }
        // if this is a Adding Phone profile or update ProtectMe! we pass directly to the next Fragment
        else{
            val nextFrag = Config_Bluetooth_Fragment()
            activity!!.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, nextFrag)
                    .addToBackStack(null)
                    .commit()
        }
    }
}
