package zutt.protectme

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.VpnService
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_vpn.*


class VPN_Fragment : Fragment() {

    companion object {
        var NAME = "connection"
        var SERVER_ADDRESS = "server.address"
        var SERVER_PORT = "server.port"
        var SHARED_SECRET = "shared.secret"
        var PROXY_HOSTNAME = "proxyhost"
        var PROXY_PORT = "proxyport"
        var ALLOW = "allow"
        var PACKAGES = "packages"
    }

    private val PREFERENCE_CONFIG_NAME = "boxes"
    private var prefs: SharedPreferences? = null
    private var res: ressource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = context!!.getSharedPreferences(PREFERENCE_CONFIG_NAME, 0)
    }

    fun getContextOfApplication(): Context {
        return context!!
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        res = ressource(prefs!!)
        var listBoxes = res!!.getProtectMeBoxes()
        vpnConnect.setOnClickListener { view ->
            /*
            if (listBoxes.size == 0) {
                Toast.makeText(this.context, "CONFIGURE BEFORE CONNECT", Toast.LENGTH_SHORT).show()
            } else {
                vpnConnect.setBackgroundResource(R.drawable.circlebuttongreen)
                //TODO : Get params from ressource to companion object
                startVPN()
            }
            */
            startVPN()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_vpn, container, false)
    }


    private fun startVPN() {
        val vpnIntent = VpnService.prepare(this.context)
        if (vpnIntent != null)
            startActivityForResult(vpnIntent, 0) //Prepare to establish a VPN connection. This method returns null if the VPN application is already prepared or if the user has previously consented to the VPN application. Otherwise, it returns an Intent to a system activity.
        else
            onActivityResult(0, RESULT_OK, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            val vpnIntent = Intent(this.context, MyVPNServiceV2()::class.java)
            this.context!!.startService(vpnIntent.setAction(MyVPNServiceV2.ACTION_CONNECT))
        }
    }
}
