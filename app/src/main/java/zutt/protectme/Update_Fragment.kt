/**
 * Auteur: Yann Lederrey
 * Date : 19 Juillet 2019
 * Cadre : Travail de Bachelor, Heig-VD, Securite de l'information
 * Projet : ProtectMe! VPN for everyone !
 * Github : https://github.com/yannled/protectMe-android
 * sources : ---
 *
 * Type de classe : Fragment
 * Vue correspondantes : fragment_update
 * Explication : Ce fragment permet à l'utilisateur de savoir si lors de la dernière mise à jour de
 * son application smartphone, une nouvelle version du serveur est alors disponible. Si c'est le cas,
 * l'utilisateur peut initer la mise à jour du boitier.
 */

package zutt.protectme


import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.util.Base64
import android.view.Gravity
import kotlinx.android.synthetic.main.fragment_update.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.nio.charset.Charset

class Update_Fragment : Fragment() {
    val AVAILABLE_VERSION = "availableVersion.txt"
    val CURRENT_VERSION = "currentVersion.txt"

    private val REQUEST_ENABLE_BT = 1
    private var enableBtIntent = Intent()
    private var mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private lateinit var ConfigurationModel: SharedViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update, container, false)
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        // Load the shared Model
        ConfigurationModel = activity?.run {
            ViewModelProviders.of(this).get(SharedViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        super.onActivityCreated(savedInstanceState)
        //Get the currentVersion, last updated version in the android files
        var filemanager = FileManager(this.context!!)
        //Check in android file lastUpdatedVersion, currentVersion
        val currentVersion = filemanager.readFileVersion(CURRENT_VERSION)
        update_currentVersion_field.text = currentVersion

        //Read the last push version on asset file (updated when load new android App from app Store)
        val newVersion = readAvailableVersion()

        //Check if currentVersion is different of the asset File availableVersion.txt, if yes, a new update is available
        if (newVersion != currentVersion) {
            update_applyUpdate.isClickable = true
            val color = ContextCompat.getColor(this.context!!, R.color.myBlueDark)
            update_applyUpdate.setBackgroundColor(color)
            update_available.text = getText(R.string.something)
            update_info.visibility = View.VISIBLE

            update_applyUpdate.setOnClickListener { view ->
                // verify that Bluetooth is enable, if not, ask to active it
                if (mBluetoothAdapter == null) {
                    val toast = Toast(context)
                    toast.createToast(context!!, getString(R.string.no_Bluetooth_config), Gravity.BOTTOM, 10)
                } else {
                    if (!mBluetoothAdapter!!.isEnabled) {
                        this.enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(this.enableBtIntent, this.REQUEST_ENABLE_BT)
                    }
                }

                // if bluetooth is already enable, start updating
                if(mBluetoothAdapter!=null && mBluetoothAdapter.isEnabled){
                    startUpdating()
                }
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == this.REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                // After bluetooth is activated start configuration
                startUpdating()
            }
        }
    }

    fun startUpdating() {
        // allow to request api
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        //Do a Call on the Github API to know hash of new version
        var result = URL("https://api.github.com/repos/yannled/protectMe-Update/contents/hash.txt").readText()
        var resultJson = JSONObject(result)
        val encodedHash = resultJson.getString("content")
        val decodedBytes = Base64.decode(encodedHash, Base64.DEFAULT)
        var decodedHash = String(decodedBytes, Charset.defaultCharset())
        ConfigurationModel.hash = decodedHash.substring(0, decodedHash.length - 1)
        var toast = Toast(context)
        toast.createToast(context!!, getString(R.string.hashRecovered), Gravity.BOTTOM, 10)

        //Do a Call on the GIthub API to know the name of new version
        var nameUpdateFile = ""
        result = URL("https://api.github.com/repos/yannled/protectMe-Update/contents/").readText()
        var resultJsonArray = JSONArray(result)
        var jsonObject: JSONObject
        for (i in 0 until resultJsonArray.length()) {
            jsonObject = resultJsonArray.getJSONObject(i)
            if (".img" in jsonObject.getString("name")) {
                nameUpdateFile = jsonObject.getString("name")
                toast = Toast(context)
                toast.createToast(context!!, getString(R.string.UpdateNameRecovered), Gravity.BOTTOM, 10)
            }
        }

        // we update the current version in files of android by the name of the new version
        var filemanager = FileManager(this.context!!)
        filemanager.writeFileVersion(CURRENT_VERSION, nameUpdateFile)

        //On click, go to the next fragment configuration
        ConfigurationModel.action = "Update"
        val nextFrag = Config_Wifi_Fragment()
        activity!!.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, nextFrag)
                .addToBackStack(null)
                .commit()

    }


    fun readAvailableVersion(): String {
        var availableVersion: String
        this.context!!.assets.open(AVAILABLE_VERSION).apply {
            availableVersion = this.readBytes().toString(Charsets.UTF_8)
        }.close()
        return availableVersion
    }
}


