package zutt.protectme

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.VpnService
import android.os.Handler
import android.os.Message
import android.os.ParcelFileDescriptor
import android.util.Log
import android.util.Pair
import android.widget.Toast
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference


class MyVPNServiceV2 : VpnService() {

    companion object {
        val ACTION_CONNECT = "START"
        val ACTION_DISCONNECT = "STOP"
    }

    private val TAG = MyVPNServiceV2::class.java.getSimpleName()

    private class Connection(thread: Thread, pfd: ParcelFileDescriptor) : Pair<Thread, ParcelFileDescriptor>(thread, pfd)

    private val mConnectingThread = AtomicReference<Thread>()
    private val mConnection = AtomicReference<Connection>()

    private val mNextConnectionId = AtomicInteger(1)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && ACTION_DISCONNECT == intent.action) {
            disconnect()
            return Service.START_NOT_STICKY
        } else {
            connect()
            return Service.START_STICKY
        }
    }

    override fun onDestroy() {
        disconnect()
    }

    private fun connect() {
        // Become a foreground service. Background services can be VPN services too, but they can
        // be killed by background check before getting a chance to receive onRevoke().

        // TODO : Extract information from the ressource.
        /*
        val prefs = getSharedPreferences(ToyVpnClient.Prefs.NAME, Context.MODE_PRIVATE)
        val server = prefs.getString(ToyVpnClient.Prefs.SERVER_ADDRESS, "")
        val secret = prefs.getString(ToyVpnClient.Prefs.SHARED_SECRET, "")!!.toByteArray()
        val allow = prefs.getBoolean(ToyVpnClient.Prefs.ALLOW, true)
        val packages = prefs.getStringSet(ToyVpnClient.Prefs.PACKAGES, emptySet<Any>())
        val port = prefs.getInt(ToyVpnClient.Prefs.SERVER_PORT, 0)
        val proxyHost = prefs.getString(ToyVpnClient.Prefs.PROXY_HOSTNAME, "")
        val proxyPort = prefs.getInt(ToyVpnClient.Prefs.PROXY_PORT, 0)
        */
        val server = "10.0.0.9"
        val secret = "test".toByteArray()
        val allow = true
        val packages = hashSetOf("a", "b", "c", "c")
        val port = 1194
        startConnection(VPNConnection(
                this, mNextConnectionId.getAndIncrement(), server, port, secret,
                allow, packages))
    }

    private fun startConnection(connection: VPNConnection) {
        // Replace any existing connecting thread with the  new one.
        val thread = Thread(connection, "VpnThread")
        setConnectingThread(thread)
        thread.start()
    }

    private fun setConnectingThread(thread: Thread?) {
        val oldThread = mConnectingThread.getAndSet(thread)
        oldThread?.interrupt()
    }

    private fun setConnection(connection: Connection?) {
        val oldConnection = mConnection.getAndSet(connection)
        if (oldConnection != null) {
            try {
                oldConnection.first.interrupt()
                oldConnection.second.close()
            } catch (e: IOException) {
                Log.e(TAG, "Closing VPN interface", e)
            }

        }
    }

    private fun disconnect() {
        setConnectingThread(null)
        setConnection(null)
        stopForeground(true)
    }

}


