/**
 * Auteur: Yann Lederrey
 * Date : 19 Juillet 2019
 * Cadre : Travail de Bachelor, Heig-VD, Securite de l'information
 * Projet : ProtectMe! VPN for everyone !
 * Github : https://github.com/yannled/protectMe-android
 * sources : ---
 *
 * Type de classe : Activity
 * Vue correspondantes : ---
 * Explication : L'activité principale nous permet de définir les fragments chargé par les boutons de
 * menu ainsi que l'icone d'information. Par défaut le fragment VPN est chargé.
 */

package zutt.protectme

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.transition.TransitionManager
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.transition.Slide
import android.view.*
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

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
                val updateFragement = Update_Fragment()
                openFragment(updateFragement)
            }
            R.id.nav_list -> {
                val listBoxFragment = ListBox_Fragment()
                openFragment(listBoxFragment)
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
}
