package zutt.protectme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.RemoteException
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.blinkt.openvpn.api.IOpenVPNAPIService
import java.io.IOException
import android.content.ComponentName
import android.app.Activity
import android.os.IBinder
import android.content.ServiceConnection
import kotlinx.android.synthetic.main.fragment_vpn.*
import java.io.BufferedReader
import java.io.InputStreamReader
import de.blinkt.openvpn.api.APIVpnProfile


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

    private val MSG_UPDATE_STATE = 0
    private val MSG_UPDATE_MYIP = 1
    private val START_PROFILE_EMBEDDED = 2
    private val START_PROFILE_BYUUID = 3
    private val ICS_OPENVPN_PERMISSION = 7
    private val PROFILE_ADD_NEW = 8

    private var connected : Boolean = false
    private var mStartUUID: String? = null
    protected var vpnService : IOpenVPNAPIService? = null
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
            if(connected){
                try {
                    vpnService!!.disconnect()
                    connected = false
                }
                catch (e : RemoteException){
                    e.printStackTrace()
                }
            }
            else{
                try {
                    prepareStartProfile(PROFILE_ADD_NEW)
                    prepareStartProfile(START_PROFILE_EMBEDDED)
                    connected = true
                }
                catch (e : RemoteException){
                    e.printStackTrace()
                }
            }
            /*
            if (listBoxes.size == 0) {
                Toast.makeText(this.context, "CONFIGURE BEFORE CONNECT", Toast.LENGTH_SHORT).show()
            } else {
                vpnConnect.setBackgroundResource(R.drawable.circlebuttongreen)
                //TODO : Get params from ressource to companion object
                startVPN()
            }
            */

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_vpn, container, false)
    }

    private fun startEmbeddedProfile(addNew: Boolean) {
        try {

            val conf = activity!!.assets.open("test.conf")
            val isr = InputStreamReader(conf)
            val br = BufferedReader(isr)
            var config : String = ""
            var line: String?
            while (true) {
                line = br.readLine()
                if (line == null)
                    break
                config += line + "\n"
            }
            br.readLine()

            if (addNew)
                vpnService!!.addNewVPNProfile("nonEditable", false, config)
            else
                vpnService!!.startVPN(config)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }

    override fun onStart() {
        super.onStart()
        bindService()
    }

    protected fun listVPNs() {

        try {
            val list = vpnService!!.getProfiles()
            var all = "List:"
            for (vp in list.subList(0, Math.min(5, list.size))) {
                all = all + vp.mName + ":" + vp.mUUID + "\n"
            }

            if (list.size > 5)
                all += "\n And some profiles...."

            if (list.size > 0) {
                mStartUUID = list.get(0).mUUID
            }

        } catch (e: RemoteException) {
           e.printStackTrace()
        }

    }


    /**
     * Class for interacting with the main interface of the service.
     */
    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.

            vpnService = IOpenVPNAPIService.Stub.asInterface(service)

            try {
                // Request permission to use the API
                val i = vpnService!!.prepare(activity!!.packageName)
                if (i != null) {
                    startActivityForResult(i, ICS_OPENVPN_PERMISSION)
                } else {
                    onActivityResult(ICS_OPENVPN_PERMISSION, Activity.RESULT_OK, null)
                }

            } catch (e: RemoteException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }

        }

        override fun onServiceDisconnected(className: ComponentName) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            vpnService = null

        }
    }

    private fun bindService() {

        val icsopenvpnService = Intent(IOpenVPNAPIService::class.java.name)
        icsopenvpnService.setPackage("de.blinkt.openvpn")

        activity!!.bindService(icsopenvpnService, mConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindService() {
        activity!!.unbindService(mConnection)
    }

    override fun onStop() {
        super.onStop()
        unbindService()
    }

    @Throws(RemoteException::class)
    private fun prepareStartProfile(requestCode: Int) {
        val requestpermission = vpnService!!.prepareVPNService()
        if (requestpermission == null) {
            onActivityResult(requestCode, Activity.RESULT_OK, null)
        } else {
            // Have to call an external Activity since services cannot used onActivityResult
            startActivityForResult(requestpermission, requestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == START_PROFILE_EMBEDDED)
                startEmbeddedProfile(false)
            if (requestCode == START_PROFILE_BYUUID)
                try {
                    vpnService!!.startProfile(mStartUUID)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            if (requestCode == ICS_OPENVPN_PERMISSION){
                listVPNs()
            }
            if (requestCode == PROFILE_ADD_NEW) {
                startEmbeddedProfile(true)
            }
        }
    }
}
