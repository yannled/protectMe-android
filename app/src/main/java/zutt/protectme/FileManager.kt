package zutt.protectme

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/*
Format Filename : DEFAULT_<WifiSSID>_<boxName>_<boxMac>
DEFAULT is ad only if first file or if user has change default configuration.
 */
class FileManager(c : Context){
    private var context : Context? = null
    private val DIRECTORY_NAME = "ProtectMeConfig"
    private val FILE_EXTENSION = ".txt"
    private val DEFAULT = "DEFAULT_"
    init {
        this.context = c
    }

    fun addFile(filename : String, content : String): Boolean {

        val path = context!!.filesDir
        val directory = File(path, DIRECTORY_NAME)
        if(!directory.exists()){
            directory.mkdirs()
        }

        var file : File? = null

        //TODO : ENLEVER LA SUITE
        val files = directory.listFiles()
        for (file : File in files){
            file.delete()
        }
        // this is the first file configuration, Default configuration
        if(directory.listFiles().isEmpty()){
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
        val path = context!!.filesDir
        val directory = File(path, DIRECTORY_NAME)

        if(!directory.exists()){
            return null
        }

        var filename : String? = null
        val files = directory.listFiles()
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
}