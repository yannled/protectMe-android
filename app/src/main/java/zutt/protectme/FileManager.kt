/**
 * Auteur: Yann Lederrey
 * Date : 19 Juillet 2019
 * Cadre : Travail de Bachelor, Heig-VD, Securite de l'information
 * Projet : ProtectMe! VPN for everyone !
 * Github : https://github.com/yannled/protectMe-android
 * sources : ---
 *
 * Type de classe : Classe simple
 * Vue correspondantes : ---
 * Explication : Cette classe permet de gérer les différents fichier openvpn inscrit sur le smartphone
 * le nom des fichiers suit le format suivant : DEFAULT_<WifiSSID>_<boxName>_<boxMac>
 * DEFAULT_ est ajouté seulement dans le cas ou c'est le premier profile ajouté au smartphone ou que
 * l'utilisateur à changé le profile par défaut. C'est ce profile qui sera utilisé lors de la connexion VPN
 */

package zutt.protectme

import android.content.Context
import java.io.*

class FileManager(c : Context){

    companion object {
        val DEFAULT = "DEFAULT_"
        val FILE_EXTENSION = ".txt"
        val DIRECTORY_NAME = "ProtectMeConfig"
        val DIRECTORY_VERSION_NAME = "ProtectMeConfig"
    }

    private var context : Context? = null
    private var path : File? = null
    private var directory : File? = null
    private var directoryVersion : File? = null

    init {
        this.context = c
        path = context!!.filesDir
        directory = File(path, DIRECTORY_NAME)
        directoryVersion = File(path, DIRECTORY_VERSION_NAME)
    }

    fun addFile(filename : String, content : String): Boolean {
        if(!directory!!.exists()){
            directory!!.mkdirs()
        }

        var file : File? = null

        // this is the first file configuration, Default configuration
        if(directory!!.listFiles().isEmpty()){
            file = File(directory, DEFAULT + filename + FILE_EXTENSION)
        }
        else{
            deleteFileIfExist_ByFileName(filename+FILE_EXTENSION)
            file = File(directory, filename+FILE_EXTENSION)
        }

        FileOutputStream(file).use {
            it.write(content.toByteArray())
        }
        return file.exists()
    }

    fun readDefaultFile(): String? {
        if(!directory!!.exists()){
            return null
        }

        var filename : String? = null
        val files = directory!!.listFiles()
        for (file : File in files){
            if(file.name.contains(DEFAULT)){
                filename = file.name
            }
        }

        if(filename.isNullOrBlank())
            return null

        val file = File(directory, filename)
        val content = FileInputStream(file).bufferedReader().use { it.readText() }
        return content

    }

    fun getDefaultFileName(): String? {
        if(!directory!!.exists()){
            return null
        }

        var filename : String? = null
        val files = directory!!.listFiles()
        for (file : File in files){
            if(file.name.contains(DEFAULT)){
                filename = file.name
            }
        }

        if(filename.isNullOrBlank())
            return null

        return filename
    }

    fun getFileNames(): ArrayList<String> {
        val list: ArrayList<String> = arrayListOf()
        if(!directory!!.exists()){
            return list
        }

        val files = directory!!.listFiles()
        for (file : File in files){
                list.add(file.name)
        }
        return list
    }

    fun deleteFile(position : Int){
        if(!directory!!.exists()){
            return
        }

        val files = directory!!.listFiles()
        var file = files.get(position)
        file.delete()
    }

    fun deleteFileIfExist_ByFileName(filenameAndExtension : String){
        if(!directory!!.exists()){
            return
        }

        val files = directory!!.listFiles()
        for (file: File in files){
            if (file.name == filenameAndExtension)
                file.delete()
        }
    }

    fun deleteAllProfileFiles(){
        if(!directory!!.exists()){
            return
        }

        val files = directory!!.listFiles()
        for (file : File in files){
            file.delete()
        }
    }

    fun deleteVersionFile(){
        if(!directoryVersion!!.exists()){
            return
        }

        val files = directoryVersion!!.listFiles()
        for (file : File in files){
            file.delete()
        }
    }

    fun renameFile(position: Int, fileName: String, KeepDefault : Boolean = true){
        var newFileName = fileName
        if(!directory!!.exists()){
            return
        }

        val files = directory!!.listFiles()
        val old = files.get(position)
        if(old.name.contains(DEFAULT) && KeepDefault)
        {
            newFileName = DEFAULT + newFileName
        }
        if(!fileName.contains(FILE_EXTENSION))
        {
            newFileName = newFileName + FILE_EXTENSION
        }
        val new = File(directory, newFileName)
        if(old.exists())
            old.renameTo(new)
    }

    fun changeDefault(position: Int, defaultPosition : Int){
        if(!directory!!.exists()){
            return
        }

        val files = directory!!.listFiles()

        // Rename old default file to normal name (without DEFAULT_)
        val oldDefault = files.get(defaultPosition)
        var defaultFileName = oldDefault.name
        defaultFileName = defaultFileName.substringAfter(DEFAULT)

        renameFile(defaultPosition, defaultFileName, false)

        // Rename new default file with DEFAULT_
        val newDefault = files.get(position)
        defaultFileName = newDefault.name
        defaultFileName = DEFAULT + defaultFileName

        renameFile(position, defaultFileName)
    }

    fun readFileVersion(filename: String): String {
        if(!directoryVersion!!.exists()){
            return "NO_FILES"
        }

        val files = directoryVersion!!.listFiles()
        for (file: File in files){
            if(file.name.equals(filename)){
                val content = FileInputStream(file).bufferedReader().use { it.readText() }
                return content
            }
        }
        return "NO_FILES"
    }

    fun writeFileVersion(filename: String, content: String): Boolean {
        if(!directoryVersion!!.exists()){
            directoryVersion!!.mkdirs()
        }

        var file = File(directoryVersion, filename)

        FileOutputStream(file).use {
            it.write(content.toByteArray())
        }
        return file.exists()
    }
}