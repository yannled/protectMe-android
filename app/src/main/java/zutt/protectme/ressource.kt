package zutt.protectme

import android.content.SharedPreferences
import org.json.JSONObject
import java.io.InputStream
import java.util.zip.CheckedInputStream

class ressource(preferencesFile : SharedPreferences){
    companion object {
        lateinit var pref : SharedPreferences
    }

    init {
        pref = preferencesFile
    }

    fun getProtectMeBoxes(): ArrayList<String> {
        var list =  pref.all.toList()
        val listBoxes : ArrayList<String> = ArrayList()
        for (box in list){
            listBoxes.add(box.second.toString())
        }
        return listBoxes
    }

    fun getProtectMeBoxe(mac : String): MutableSet<String>? {
       return pref.getStringSet(mac, null)
    }

    fun addProtectMeBoxe(mac : String, name : String, ssid : String, ip : String){
        val editor = pref.edit()
        var content : Set<String> = setOf(mac,name,ssid,ip)
        editor.putStringSet(mac,content)
        editor.apply()
    }

    fun modifyProctectMeBoxe(mac : String, name : String, ssid : String, ip : String){
        addProtectMeBoxe(mac, name, ssid, ip)
    }
}