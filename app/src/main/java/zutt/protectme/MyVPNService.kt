package zutt.protectme

import android.app.Service
import android.content.Intent
import android.net.VpnService
import android.net.VpnService.Builder
import android.os.ParcelFileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel


class MyVPNService : VpnService() {

    private var mThread: Thread? = null
    private var mInterface: ParcelFileDescriptor? = null
    val builder = Builder()

    // Services interface
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Start a new session by creating a new thread.
        mThread = Thread(Runnable {
            try {
                //a. Configure the TUN and get the interface.
                mInterface = builder.setSession("MyVPNService")
                        .addAddress("192.168.0.1", 24)
                        .addDnsServer("8.8.8.8")
                        .addRoute("0.0.0.0", 0).establish()
                //b. Packets to be sent are queued in this input stream.
                val input = FileInputStream(
                        mInterface!!.fileDescriptor)
                //b. Packets received need to be written to this output stream.
                val out = FileOutputStream(
                        mInterface!!.fileDescriptor)
                //c. The UDP channel can be used to pass/get ip package to/from server
                val tunnel = DatagramChannel.open()
                // Connect to the server, localhost is used for demonstration only.
                tunnel.connect(InetSocketAddress("127.0.0.1", 8087))
                //d. Protect this socket, so package send by it will not be feedback to the vpn service.
                protect(tunnel.socket())
                //e. Use a loop to pass packets.
                while (true) {
                    //get packet with in
                    //put packet to tunnel
                    //get packet form tunnel
                    //return packet with out
                    //sleep is a must
                    Thread.sleep(100)
                }

            } catch (e: Exception) {
                // Catch any exception
                e.printStackTrace()
            } finally {
                try {
                    if (mInterface != null) {
                        mInterface!!.close()
                        mInterface = null
                    }
                } catch (e: Exception) {

                }

            }
        }, "MyVpnRunnable")

        //start the service
        mThread!!.start()
        return Service.START_STICKY
    }

    override fun onDestroy() {
        // TODO Auto-generated method stub
        if (mThread != null) {
            mThread!!.interrupt()
        }
        super.onDestroy()
    }
}

