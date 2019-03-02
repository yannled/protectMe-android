package zutt.protectme

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.net.wifi.WifiManager
import android.support.v4.content.ContextCompat
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.fragment_config.*
import kotlinx.android.synthetic.main.fragment_wifi_config.*
import zutt.protectme.R.id.textView




class Config_Wifi_Fragment : Fragment() {

    var ssid : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wifi_config, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        ssid = wifiInfo.ssid
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ssid_config_wifi.text = ssid

        password_wifi2.setOnEditorActionListener() { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE){

                val firstPassword = password_wifi1.text
                val secondPassword = password_wifi2.text

                if(!firstPassword.toString().equals(secondPassword.toString())) {
                    password_ok_config_wifi.visibility = View.VISIBLE
                    password_wifi1.text.clear()
                    password_wifi2.text.clear()
                }
                else{
                    password_ok_config_wifi.visibility = View.INVISIBLE

                    val color = ContextCompat.getColor(this.context!!,R.color.myBlueDark)
                    button_config_Wifi.setText(R.string.next_config)
                    button_config_Wifi.setBackgroundColor(color)
                    button_config_Wifi.isClickable = true

                    button_config_Wifi.setOnClickListener { view ->
                        val nextFrag = Config_Wifi_Fragment()
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
}
