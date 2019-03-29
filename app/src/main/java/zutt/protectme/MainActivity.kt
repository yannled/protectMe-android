package zutt.protectme


import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.transition.TransitionManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.transition.Slide
import android.view.*
import android.util.Base64
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import java.nio.charset.Charset
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val fragmentManager = supportFragmentManager
    private val fragmentTransaction = fragmentManager.beginTransaction()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        // Information button, Create a PopupWindow
        // Source : https://android--code.blogspot.com/2018/02/android-kotlin-popup-window-example.html
        fab.setOnClickListener { view ->
            // Initialize a new layout inflater instance
            val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            // Inflate a custom view using layout inflater
            val view = inflater.inflate(R.layout.view_information,null)

            // Initialize a new instance of popup window
            val popupWindow = PopupWindow(

                    view, // Custom view to show in popup window
                    LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
                    LinearLayout.LayoutParams.WRAP_CONTENT // Window height
            )

            // Set an elevation for the popup window
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.elevation = 10.0F
            }

            // If API level 23 or higher then execute the code
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                // Create a new slide animation for popup window enter transition
                val slideIn = Slide()
                slideIn.slideEdge = Gravity.TOP
                popupWindow.enterTransition = slideIn

                // Slide animation for popup window exit transition
                val slideOut = Slide()
                slideOut.slideEdge = Gravity.RIGHT
                popupWindow.exitTransition = slideOut

            }

            popupWindow.isOutsideTouchable = true

            // Get the widgets reference from custom view
            val buttonPopup = view.findViewById<Button>(R.id.button_popup)

            // Set a click listener for popup's button widget
            buttonPopup.setOnClickListener{
                // Dismiss the popup window
                popupWindow.dismiss()
            }

            val mainView = findViewById<ViewGroup>(R.id.fragment_container)
            // Finally, show the popup window on app
            TransitionManager.beginDelayedTransition(mainView)
            popupWindow.showAtLocation(
                    mainView, // Location to display popup window
                    Gravity.CENTER, // Exact position of layout to display popup
                    0, // X offset
                    0 // Y offset
            )

        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        //define default fragment
        val VPNfragment = VPN_Fragment()
        fragmentTransaction.add(R.id.fragment_container, VPNfragment)
        fragmentTransaction.commit()

    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_vpn -> {
                val vpnFragment = VPN_Fragment()
                openFragment(vpnFragment)
            }
            R.id.nav_configuration -> {
                val configFragment = Config_Fragment()
                openFragment(configFragment)
            }
            R.id.nav_update -> {

            }
            R.id.nav_list -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    //Method to load the choose fragment
    protected fun openFragment(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(fragment.javaClass.getSimpleName())
                .commit()
    }

    /*fun testfunction(){
        // Start crypted communication
        var dh  = DiffieHellmann()

        // Generation of private and public keys
        dh.generateKeys()

        var receivedPublicKey : String = "3506e593dce4d281fc760d9546fb336fb6862c3d143c7bf8dea422c303505e4880a914e563ab37a7698b78558c31ded2699954e89160967aa2b6406a8818b917b3a4b994637e13799ba8f30a63a8e03a03f924b3806d5dbc28324f271f11119d0100277448e2e6fbc085cedcfe621a2cb2ba4b51e78edbb3de4a1149410b69fe9a6d20f9188042e4e13ff73c18e1f2789131412bbe614c9b46b050d34d7bb38aa45da4c2af61bf34f431b82ce0db62b4740af944f4f294d2e7a6a9c9e7143629ce66992e955a8baa0c19a3a211c2c9f6cb67b5712b4caf24ab1a34f88edb199a36b6de5696a315c6cd51a4f26d9e227d9eb3cfb6d4e1f475630eae1cceb279799b0ac53e6831b3927c5fc1d9d0cec3b2caf9bbdc465eb2c4e9228663d0006274219a6905590b0a68708fb43e304f59bce583f4a5a5acbb9590d31dcc8ddd62492b5ba2677c46490c61a73fc32091a69ced1fb09edbf80819a9fda8bb2893411310aafd2cd7afab1ccdf7c4d4e01992fe25afe307cbb2d7ac6eb6fe6c3689bbb6"
        //var receivedPublicKey : String = "OpenSSLDHPublicKey{Y=3506e593dce4d281fc760d9546fb336fb6862c3d143c7bf8dea422c303505e4880a914e563ab37a7698b78558c31ded2699954e89160967aa2b6406a8818b917b3a4b994637e13799ba8f30a63a8e03a03f924b3806d5dbc28324f271f11119d0100277448e2e6fbc085cedcfe621a2cb2ba4b51e78edbb3de4a1149410b69fe9a6d20f9188042e4e13ff73c18e1f2789131412bbe614c9b46b050d34d7bb38aa45da4c2af61bf34f431b82ce0db62b4740af944f4f294d2e7a6a9c9e7143629ce66992e955a8baa0c19a3a211c2c9f6cb67b5712b4caf24ab1a34f88edb199a36b6de5696a315c6cd51a4f26d9e227d9eb3cfb6d4e1f475630eae1cceb279799b0ac53e6831b3927c5fc1d9d0cec3b2caf9bbdc465eb2c4e9228663d0006274219a6905590b0a68708fb43e304f59bce583f4a5a5acbb9590d31dcc8ddd62492b5ba2677c46490c61a73fc32091a69ced1fb09edbf80819a9fda8bb2893411310aafd2cd7afab1ccdf7c4d4e01992fe25afe307cbb2d7ac6eb6fe6c3689bbb6,P=9316e84220bb2242085126bffbefffafab307a61b5141617d4aac956c530e4860453dc31faddc5b60230082709ef09e8095ae5b71893121f7abbb1f4cb8b2567b3d3fcd48f82c0405a617be3574e5cd4425afa80b415f1d7c335a46d6eaa7d51b6dda4759ff07786c475969"
        var key = Base64.encodeToString(receivedPublicKey.toByteArray(),0)

        val encodedPublicKey = Base64.decode(key,0)

        //val spec = X509EncodedKeySpec(encodedPublicKey)
        dh.setReceivePublicKey(receivedPublicKey)

        dh.generateCommonSecretKey()
    }*/
}
