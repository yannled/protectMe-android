package zutt.protectme

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class VerificationDuContext {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("zutt.protectme", appContext.packageName)
    }
}

@RunWith(AndroidJUnit4::class)
class FileManagerTests {
    lateinit var fileManager : FileManager

    @Before
    fun instantiateFileManager() {
        //Instantiate the FileManager and get the context
        val appContext = InstrumentationRegistry.getTargetContext()
        fileManager = FileManager(appContext)
    }

    @After
    fun deleteFiles() {
        fileManager.deleteAllFiles()
    }


    @Test
    fun deleteAllFiles() {
        fileManager.deleteAllFiles()
    }

    @Test
    fun thisTestShouldCreateAFile_AddingTheExtension_ThenVerifyThatFileExist() {
        val name = "testAddingFile"
        val nameAndExtension = name+FileManager.FILE_EXTENSION
        val defaultNameAndExtension =FileManager.DEFAULT+name+FileManager.FILE_EXTENSION
        val contentFile = "This is a test file."
        fileManager.addFile(name, contentFile)
        val filenames = fileManager.getFileNames()

        val exist : Boolean = filenames.contains(nameAndExtension) || filenames.contains(defaultNameAndExtension)
        assertTrue(exist)
    }

    @Test
    fun thisTestShouldCreateFile_AddingTheExtension_AddingTheDefault_ThenVerifyThatFileExist() {
        //First we delete all files because if the folder is not empty, the Default inscirption
        // will not be added to the File
        fileManager.deleteAllFiles()

        val name = "testAddingDefaultFile"
        val defaultNameAndExtension =FileManager.DEFAULT+name+FileManager.FILE_EXTENSION
        val contentFile = "This is a test file."
        fileManager.addFile(name, contentFile)
        val filenames = fileManager.getFileNames()
        assertTrue(filenames.contains(defaultNameAndExtension))
    }


    @Test
    fun thisTestShouldCreateAFile_ThenDeleteTheFile_ThenVerifyThatFileNotExist() {
        val name = "testDeleteFile"
        val nameAndExtension = name+FileManager.FILE_EXTENSION
        val defaultNameAndExtension =FileManager.DEFAULT+name+FileManager.FILE_EXTENSION
        val contentFile = "This is a test file."
        fileManager.addFile(name, contentFile)
        var filenames = fileManager.getFileNames()
        var exist : Boolean = filenames.contains(nameAndExtension) || filenames.contains(defaultNameAndExtension)
        //If the file is created we delete it
        if(exist){
            var position = filenames.indexOf(nameAndExtension)
            if(position == -1)
                position = filenames.indexOf(defaultNameAndExtension)
            fileManager.deleteFile(position)
            //Then we get the fileNames a second time
            filenames = fileManager.getFileNames()
            exist = filenames.contains(nameAndExtension) || filenames.contains(defaultNameAndExtension)
            assertFalse(exist)
        }
        else
            fail("The file was not created")

    }

    @Test
    fun thisTestShouldCreateFile_AddingTheExtension_AddingTheDefault_ThenGetTheDefaultFileName_ThenVerifyTheDefaultFileContent() {
        //First we delete all files because if the folder is not empty, the Default inscirption
        // will not be added to the File
        fileManager.deleteAllFiles()

        //verify that we could get the default file
        val name = "testGetDefaultFile"
        val defaultNameAndExtension =FileManager.DEFAULT+name+FileManager.FILE_EXTENSION
        val contentFile = "This is a test file."
        fileManager.addFile(name, contentFile)
        val filename = fileManager.getDefaultFileName()
        assertTrue(filename.equals(defaultNameAndExtension))

        //verify that we could get the content
        val content = fileManager.readDefaultFile()
        assertTrue(content.equals(contentFile))
    }

    @Test
    fun thisTestShouldCreateFile_AddingTheExtension_RenameItWithoutDefault_ThenVerifyThatFileExist() {
        val name = "testRenameFile"
        val nameAndExtension = name+FileManager.FILE_EXTENSION
        val defaultNameAndExtension =FileManager.DEFAULT+name+FileManager.FILE_EXTENSION
        val newName = "fileIsRename"
        val contentFile = "This is a test file."
        fileManager.addFile(name, contentFile)
        var filenames = fileManager.getFileNames()
        var exist : Boolean = filenames.contains(nameAndExtension) || filenames.contains(defaultNameAndExtension)
        //If the file is created we rename it
        if(exist){
            var position = filenames.indexOf(nameAndExtension)
            if(position == -1)
                position = filenames.indexOf(defaultNameAndExtension)
            fileManager.renameFile(position,newName,false)
            //Then we get the fileNames a second time
            filenames = fileManager.getFileNames()
            exist = filenames.contains(newName+FileManager.FILE_EXTENSION)
            assertTrue(exist)
        }
        else
            fail("The file was not created")
    }

    @Test
    fun thisTestShouldCreateFile_AddingTheExtension_AddingTheDefault_RenameItKeepDefault_ThenVerifyThatFileExist() {
        //First we delete all files because if the folder is not empty, the Default inscirption
        // will not be added to the File
        fileManager.deleteAllFiles()

        val name = "testRenameDefaultFile"
        val defaultNameAndExtension =FileManager.DEFAULT+name+FileManager.FILE_EXTENSION
        val newName = "defaultFileIsRename"
        val contentFile = "This is a test file."
        fileManager.addFile(name, contentFile)
        val filenames = fileManager.getFileNames()
        var exist : Boolean = filenames.contains(defaultNameAndExtension)
        //If the file is created we rename it
        if(exist){
            val position = filenames.indexOf(defaultNameAndExtension)
            fileManager.renameFile(position,newName,true)
            //Then we get the fileNames a second time
            val filename = fileManager.getDefaultFileName()
            assertTrue(filename.equals(FileManager.DEFAULT+newName+FileManager.FILE_EXTENSION))
        }
        else
            fail("The file was not created")
    }

    @Test
    fun thisTestShouldCreateTwoFiles_OneDefault_OneNormal_ThenChangeDefaultFile_ThenVerifyThatFileExist() {
        //First we delete all files because if the folder is not empty, the Default inscirption
        // will not be added to the File
        fileManager.deleteAllFiles()

        val namef1 = "testDefaultFile"
        val defaultNameAndExtensionf1 =FileManager.DEFAULT+namef1+FileManager.FILE_EXTENSION
        val namef2 = "testFile"
        val nameAndExtensionf2 =namef2+FileManager.FILE_EXTENSION
        val contentFile = "This is a test file."
        fileManager.addFile(namef1, contentFile)
        fileManager.addFile(namef2, contentFile)
        val filenames = fileManager.getFileNames()
        var existf1 : Boolean = filenames.contains(defaultNameAndExtensionf1)
        var existf2 : Boolean = filenames.contains(nameAndExtensionf2)
        //If the file is created we rename it
        if(existf1 and existf2){
            val positionNormal = filenames.indexOf(nameAndExtensionf2)
            val positionDefault = filenames.indexOf(defaultNameAndExtensionf1)
            fileManager.changeDefault(positionNormal,positionDefault)
            //Then we get the fileName a second time to verify that the default has change
            val filename = fileManager.getDefaultFileName()
            assertTrue(filename.equals(FileManager.DEFAULT+namef2+FileManager.FILE_EXTENSION))
        }
        else
            fail("The file was not created")
    }
}


