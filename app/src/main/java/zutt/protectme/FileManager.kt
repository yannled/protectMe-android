package zutt.protectme

import android.content.Context
import java.io.*

/*
Format Filename : DEFAULT_<WifiSSID>_<boxName>_<boxMac>
DEFAULT is ad only if first file or if user has change default configuration.
 */
class FileManager(c : Context){

    companion object {
        val DEFAULT = "DEFAULT_"
        val FILE_EXTENSION = ".txt"
        val DIRECTORY_NAME = "ProtectMeConfig"
    }

    private var context : Context? = null
    private var path : File? = null
    private var directory : File? = null

    init {
        this.context = c
        path = context!!.filesDir
        directory = File(path, DIRECTORY_NAME)
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
        var content = FileInputStream(file).bufferedReader().use { it.readText() }
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

    fun deleteAllFiles(){
        if(!directory!!.exists()){
            return
        }

        val files = directory!!.listFiles()
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
}