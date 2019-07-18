/**
 * Auteur: Yann Lederrey
 * Date : 19 Juillet 2019
 * Cadre : Travail de Bachelor, Heig-VD, Securite de l'information
 * Projet : ProtectMe! VPN for everyone !
 * Github : https://github.com/yannled/protectMe-android
 * sources : ---
 *
 * Type de classe : Fragment
 * Vue correspondantes : fragment_listbox
 * Explication : Ce fragment permet à l'utilisateur de visualiser les différents profiles openvpn
 * récupérés à partir du boiter ProtectME! ainsi que les renommer, supprimer ou passer en profile par
 * défaut. le profile par défaut sera le profile utilisé lors de la connexion VPN
 */

package zutt.protectme

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.fragment_listbox.*
import android.transition.Slide
import android.view.Gravity


class ListBox_Fragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_listbox, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val files = FileManager(this.context!!)

        val temp = files.getFileNames()
        val listVpnConfig: ArrayList<String> = arrayListOf()

        var counter = 0
        var positionOfDefault = 0
        for (filename: String in temp) {
            var finalFilename = filename

            if (filename.contains(FileManager.DEFAULT)) {
                finalFilename = filename.substringAfter(FileManager.DEFAULT)
                positionOfDefault = counter
            }
            finalFilename = finalFilename.substringBefore(FileManager.FILE_EXTENSION)
            listVpnConfig.add(finalFilename)
            counter++
        }

        val adapter = boxListAdapter(listVpnConfig, positionOfDefault, this.context!!)
        boxList.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    fun refreshFragment() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fragmentManager!!.beginTransaction().detach(this).commitNow();
            fragmentManager!!.beginTransaction().attach(this).commitNow();
        } else {
            fragmentManager!!.beginTransaction().detach(this).attach(this).commit();
        }
    }

    inner class boxListAdapter(list: ArrayList<String>, positionSelected: Int, context: Context) : BaseAdapter() {
        private var listBox: ArrayList<String>? = null
        private var mContext: Context? = null
        private var positionSelected: Int = 0

        init {
            listBox = list
            mContext = context
            this.positionSelected = positionSelected
        }

        override fun getItem(position: Int): Any {
            return position
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return listBox!!.size
        }

        @SuppressLint("ViewHolder", "NewApi")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            // inflate the layout for each item of listView
            val inflater = mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            var view = inflater.inflate(R.layout.listview_row, parent, false)

            val listview_row: TextView = view.findViewById(R.id.listview_row_text)
            val btndelete: ImageButton = view.findViewById(R.id.btnDelete)
            val btnrename: ImageButton = view.findViewById(R.id.btnRename)
            val btndefault: ImageButton = view.findViewById(R.id.btnDefault)

            if (position == positionSelected) {
                view.setBackgroundResource(R.color.myBlue)
                listview_row.setTextColor(resources.getColor(R.color.white))
            }
            val files = FileManager(mContext!!)
            listview_row.text = listBox!![position]

            btndelete.setOnClickListener {
                files.deleteFile(position)
                refreshFragment()
            }

            btndefault.setOnClickListener {
                files.changeDefault(position, positionSelected)
                refreshFragment()
            }

            btnrename.setOnClickListener {
                // LOAD popup to ask change Name
                // Initialize a new layout inflater instance
                val inflater: LayoutInflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

                // Inflate a custom view using layout inflater
                val view = inflater.inflate(R.layout.view_rename_box, null)

                // Initialize a new instance of popup window
                val popupWindow = PopupWindow(

                        view, // Custom view to show in popup window
                        LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
                        LinearLayout.LayoutParams.WRAP_CONTENT // Window height
                )
                popupWindow.setFocusable(true)
                popupWindow.update()

                // Set an elevation for the popup window
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    popupWindow.elevation = 10.0F
                }

                // If API level 23 or higher then execute the code
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
                val renameEditText = view.findViewById<EditText>(R.id.renametext)
                renameEditText.setText(listBox!![position])
                // Set a click listener for popup's button widget
                buttonPopup.setOnClickListener {
                    // Rename then Dismiss the popup window
                    val fileToRename = files.getFile(position)
                    files.renameFile(fileToRename!!, renameEditText.text.toString())
                    popupWindow.dismiss()
                    refreshFragment()
                }

                val mainView = activity!!.findViewById<ViewGroup>(R.id.fragmentListBox_container)
                // Finally, show the popup window on app
                TransitionManager.beginDelayedTransition(mainView)
                popupWindow.showAtLocation(
                        mainView, // Location to display popup window
                        Gravity.CENTER, // Exact position of layout to display popup
                        0, // X offset
                        0 // Y offset
                )
            }
            return view
        }

    }
}


