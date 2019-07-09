/**
 * Auteur: Yann Lederrey
 * Date : 19 Juillet 2019
 * Cadre : Travail de Bachelor, Heig-VD, Securite de l'information
 * Projet : ProtectMe! VPN for everyone !
 * Github : https://github.com/yannled/protectMe-android
 * sources : https://github.com/schwabe/ics-openvpn/tree/master/remoteExample
 *
 * Type de classe : Fragment
 * Vue correspondantes : fragment_vpn
 * Explication : Ce fragment utilise l'IDL API du projet ics-openVPN et l'application "OpenVPN for Android"
 * pour ouvrir une connexion VPN en utilisant le profile précédement chargé.
 */

package zutt.protectme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.blinkt.openvpn.api.IOpenVPNAPIService
import java.io.IOException
import android.content.ComponentName
import android.app.Activity
import android.content.ServiceConnection
import android.os.*
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_vpn.*
import de.blinkt.openvpn.api.IOpenVPNStatusCallback

class VPN_Fragment : Fragment() {

    private val START_PROFILE_EMBEDDED = 2
    private val START_PROFILE_BYUUID = 3
    private val ICS_OPENVPN_PERMISSION = 7
    private val PROFILE_ADD_NEW = 8
    private val MSG_UPDATE_STATE = 0

    private var mHandler: Handler? = null
    private var connected : Boolean = false
    private var mStartUUID: String? = null
    protected var vpnService : IOpenVPNAPIService? = null
    private val PREFERENCE_CONFIG_NAME = "boxes"
    private var prefs: SharedPreferences? = null
    private var configurationOK = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = context!!.getSharedPreferences(PREFERENCE_CONFIG_NAME, 0)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val files = FileManager(this.context!!)
        val filename = files.getDefaultFileName()
        if (!filename.isNullOrBlank()) {
            configurationOK = true
            var configurationName = filename.substringAfter(FileManager.DEFAULT)
            configurationName = configurationName.substringBefore(FileManager.FILE_EXTENSION)
            vpn_connectTo.text = configurationName
        }
        vpnConnect.setOnClickListener { view ->

            if (configurationOK) {
                vpnButtonDefineUI(false)
                if (connected) {
                    try {
                        vpnService!!.disconnect()
                        if(mStartUUID != null)
                            vpnService!!.removeProfile(mStartUUID)
                        connected = false
                        vpnButtonDefineUI(false)
                    } catch (e: RemoteException) {
                        e.printStackTrace()
                    }
                } else {
                    try {
                        prepareStartProfile(PROFILE_ADD_NEW)
                        prepareStartProfile(START_PROFILE_EMBEDDED)
                        connected = true
                        vpnButtonDefineUI(connected)
                    } catch (e: RemoteException) {
                        e.printStackTrace()
                    }
                }
            }
            else{
                Toast.makeText(this.context, "CONFIGURE BEFORE CONNECT", Toast.LENGTH_SHORT).show()
            }
        }

        mHandler = Handler{
            if(it.obj.toString().contains("CONNECTED") && connected)
                Toast.makeText(this.context,"Connected", Toast.LENGTH_SHORT).show()
            if(it.obj.toString().contains("NOPROCESS") && !connected)
                Toast.makeText(this.context,"Disconnected", Toast.LENGTH_SHORT).show()
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        vpnService!!.disconnect()
        if(mStartUUID != null)
            vpnService!!.removeProfile(mStartUUID)
    }

    fun vpnButtonDefineUI(vpnStart : Boolean){
        if(vpnStart){
            vpnConnect.setBackgroundResource(R.drawable.circlebuttonred)
            vpnConnect.setText(R.string.VPN_connectStop)
        }
        else{
            vpnConnect.setBackgroundResource(R.drawable.circlebuttongreen)
            vpnConnect.setText(R.string.VPN_connectStart)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_vpn, container, false)
    }

    private fun startEmbeddedProfile(addNew: Boolean) {
        try {

            val files  = FileManager(this.context!!)
            val configName = files.getDefaultFileName()
            val config = files.readDefaultFile()
           if (addNew)
                vpnService!!.addNewVPNProfile(configName, false, config)
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

    private val mCallback = object : IOpenVPNStatusCallback.Stub() {

        @Throws(RemoteException::class)
        override fun newStatus(uuid: String, state: String, message: String, level: String) {
            val msg = Message.obtain(mHandler, MSG_UPDATE_STATE, "$state|$message")
            msg.sendToTarget()
        }

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
                try {
                    vpnService!!.registerStatusCallback(mCallback)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }

            }
            if (requestCode == PROFILE_ADD_NEW) {
                startEmbeddedProfile(true)
            }
        }
    }
}
