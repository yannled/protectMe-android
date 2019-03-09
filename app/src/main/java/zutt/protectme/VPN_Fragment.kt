package zutt.protectme

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.VpnService
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.net.VpnService.Builder
import android.widget.Toast
import android.os.ParcelFileDescriptor
import kotlinx.android.synthetic.main.fragment_vpn.*
import java.net.DatagramSocket
import android.content.BroadcastReceiver



class VPN_Fragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vpnConnect.setOnClickListener { view ->
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
            this.context!!.startService(Intent(this.context, MyVPNService::class.java))
        }
    }
}
