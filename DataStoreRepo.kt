package itech.pdfreader.editor.creator.uitilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class DataStoreRepo(val context: Context) {
    private val Context.preferences: DataStore<Preferences> by preferencesDataStore(name = "settings")

    private var DEFAULT_APP_IMAGEDATA_DIRECTORY: String = ""

    /**
     * Returns the String path of the last saved image
     * @return string path of the last saved image
     */
    var savedImagePath = ""
        private set

    /**
     * Decodes the Bitmap from 'path' and returns it
     * @param path image path
     * @return the Bitmap from 'path'
     */
    fun getImage(path: String?): Bitmap? {
        var bitmapFromPath: Bitmap? = null
        try {
            bitmapFromPath = BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            // TODO: handle exception
            e.printStackTrace()
        }
        return bitmapFromPath
    }

    /**
     * Saves 'theBitmap' into folder 'theFolder' with the name 'theImageName'
     * @param theFolder the folder path dir you want to save it to e.g "DropBox/WorkImages"
     * @param theImageName the name you want to assign to the image file e.g "MeAtLunch.png"
     * @param theBitmap the image you want to save as a Bitmap
     * @return returns the full path(file system address) of the saved image
     */
    fun putImage(theFolder: String?, theImageName: String?, theBitmap: Bitmap?): String? {
        if (theFolder == null || theImageName == null || theBitmap == null) return null
        DEFAULT_APP_IMAGEDATA_DIRECTORY = theFolder
        val mFullPath = setupFullPath(theImageName)
        if (mFullPath != "") {
            savedImagePath = mFullPath
            saveBitmap(mFullPath, theBitmap)
        }
        return mFullPath
    }

    /**
     * Saves 'theBitmap' into 'fullPath'
     * @param fullPath full path of the image file e.g. "Images/MeAtLunch.png"
     * @param theBitmap the image you want to save as a Bitmap
     * @return true if image was saved, false otherwise
     */
    fun putImageWithFullPath(fullPath: String?, theBitmap: Bitmap?): Boolean {
        return !(fullPath == null || theBitmap == null) && saveBitmap(fullPath, theBitmap)
    }

    /**
     * Creates the path for the image with name 'imageName' in DEFAULT_APP.. directory
     * @param imageName name of the image
     * @return the full path of the image. If it failed to create directory, return empty string
     */
    private fun setupFullPath(imageName: String): String {
        val mFolder = File(context.filesDir, DEFAULT_APP_IMAGEDATA_DIRECTORY)
        if (isExternalStorageReadable && isExternalStorageWritable && !mFolder.exists()) {
            if (!mFolder.mkdirs()) {
                Log.e("ERROR", "Failed to setup folder")
                return ""
            }
        }
        return mFolder.path + '/' + imageName
    }

    /**
     * Saves the Bitmap as a PNG file at path 'fullPath'
     * @param fullPath path of the image file
     * @param bitmap the image as a Bitmap
     * @return true if it successfully saved, false otherwise
     */
    private fun saveBitmap(fullPath: String?, bitmap: Bitmap?): Boolean {
        if (fullPath == null || bitmap == null) return false
        var fileCreated = false
        var bitmapCompressed = false
        var streamClosed = false
        val imageFile = File(fullPath)
        if (imageFile.exists()) if (!imageFile.delete()) return false
        try {
            fileCreated = imageFile.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(imageFile)
            bitmapCompressed = bitmap.compress(CompressFormat.PNG, 100, out)
        } catch (e: Exception) {
            e.printStackTrace()
            bitmapCompressed = false
        } finally {
            if (out != null) {
                try {
                    out.flush()
                    out.close()
                    streamClosed = true
                } catch (e: IOException) {
                    e.printStackTrace()
                    streamClosed = false
                }
            }
        }
        return fileCreated && bitmapCompressed && streamClosed
    }
    // Getters
    /**
     * Get int value from DataStore at 'key'. If key not found, return 0
     * @param key DataStore key
     * @return int value at 'key' or 0 if key not found
     */
    suspend fun getInt(key: String): Int {
        return context.preferences.data.first()[intPreferencesKey(key)] ?: 0
    }

    /**
     * Get parsed ArrayList of Integers from DataStore at 'key'
     * @param key DataStore key
     * @return ArrayList of Integers
     */
    suspend fun getListInt(key: String): ArrayList<Int> {
        val p = context.preferences.data.first()[stringPreferencesKey(key)] ?: ""
        val myList = TextUtils.split(p, "‚‗‚")
        val arrayToList = ArrayList(listOf(*myList))
        val newList = ArrayList<Int>()
        for (item in arrayToList) newList.add(item.toInt())
        return newList
    }

    /**
     * Get long value from DataStore at 'key'. If key not found, return 0
     * @param key DataStore key
     * @return long value at 'key' or 0 if key not found
     */
    suspend fun getLong(key: String): Long {
        return context.preferences.data.first()[longPreferencesKey(key)] ?: 0
    }

    /**
     * Get float value from DataStore at 'key'. If key not found, return 0
     * @param key DataStore key
     * @return float value at 'key' or 0 if key not found
     */
    suspend fun getFloat(key: String): Float {
        return context.preferences.data.first()[floatPreferencesKey(key)] ?: 0F
    }

    /**
     * Get double value from DataStore at 'key'. If exception thrown, return 0
     * @param key DataStore key
     * @return double value at 'key' or 0 if exception is thrown
     */
    suspend fun getDouble(key: String): Double {
        val number = getString(key)
        return try {
            number.toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }
    }

    /**
     * Get parsed ArrayList of Double from DataStore at 'key'
     * @param key DataStore key
     * @return ArrayList of Double
     */
    suspend fun getListDouble(key: String): ArrayList<Double> {
        val p = context.preferences.data.first()[stringPreferencesKey(key)] ?: ""
        val myList = TextUtils.split(p, "‚‗‚")
        val arrayToList = ArrayList(listOf(*myList))
        val newList = ArrayList<Double>()
        for (item in arrayToList) newList.add(item.toDouble())
        return newList
    }

    /**
     * Get parsed ArrayList of Integers from DataStore at 'key'
     * @param key DataStore key
     * @return ArrayList of Longs
     */
    suspend fun getListLong(key: String): ArrayList<Long> {
        val p = context.preferences.data.first()[stringPreferencesKey(key)] ?: ""
        val myList = TextUtils.split(p, "‚‗‚")
        val arrayToList = ArrayList(listOf(*myList))
        val newList = ArrayList<Long>()
        for (item in arrayToList) newList.add(item.toLong())
        return newList
    }

    /**
     * Get String value from DataStore at 'key'. If key not found, return ""
     * @param key DataStore key
     * @return String value at 'key' or "" (empty String) if key not found
     */
    suspend fun getString(key: String): String {
        return context.preferences.data.first()[stringPreferencesKey(key)]?: ""
    }

    /**
     * Get parsed ArrayList of String from DataStore at 'key'
     * @param key DataStore key
     * @return ArrayList of String
     */
    suspend fun getListString(key: String): ArrayList<String> {
        val p = context.preferences.data.first()[stringPreferencesKey(key)] ?: ""
        return ArrayList(listOf(*TextUtils.split(p, "‚‗‚")))
    }

    /**
     * Get boolean value from DataStore at 'key'. If key not found, return false
     * @param key DataStore key
     * @return boolean value at 'key' or false if key not found
     */
    suspend fun getBoolean(key: String): Boolean {
        return context.preferences.data.map {
            it[booleanPreferencesKey(key)] ?: false
        }.first()
    }

    /**
     * Get parsed ArrayList of Boolean from DataStore at 'key'
     * @param key DataStore key
     * @return ArrayList of Boolean
     */
    suspend fun getListBoolean(key: String): ArrayList<Boolean> {
        val myList = getListString(key)
        val newList = ArrayList<Boolean>()
        for (item in myList) {
            newList.add(item == "true")
        }
        return newList
    }

    suspend fun getListObject(key: String, mClass: Class<*>?): ArrayList<Any> {
        val gson = Gson()
        val objStrings = getListString(key)
        val objects = ArrayList<Any>()
        for (jObjString in objStrings) {
            val value = gson.fromJson(jObjString, mClass)
            objects.add(value)
        }
        return objects
    }

    suspend fun <T> getObject(key: String, classOfT: Class<T>?): T {
        val json = getString(key)
        val value: T = Gson().fromJson(json, classOfT) ?: throw NullPointerException()
        return value as T
    }

    // Put methods
    /**
     * Put int value into DataStore with 'key' and save
     * @param key DataStore key
     * @param value int value to be added
     */
    suspend fun putInt(key: String, value: Int) {
        checkForNullKey(key)
        context.preferences.edit {
            it[intPreferencesKey(key)] = value
        }
    }

    /**
     * Put ArrayList of Integer into DataStore with 'key' and save
     * @param key DataStore key
     * @param intList ArrayList of Integer to be added
     */
    suspend fun putListInt(key: String, intList: ArrayList<Int>) {
        checkForNullKey(key)
        val myIntList = intList.toTypedArray()
        context.preferences.edit {
            it[stringPreferencesKey(key)] = TextUtils.join("‚‗‚", myIntList)
        }
    }

    /**
     * Put long value into DataStore with 'key' and save
     * @param key DataStore key
     * @param value long value to be added
     */
    suspend fun putLong(key: String, value: Long) {
        checkForNullKey(key)
        context.preferences.edit {it[longPreferencesKey(key)] = value }
    }

    /**
     * Put ArrayList of Long into DataStore with 'key' and save
     * @param key DataStore key
     * @param longList ArrayList of Long to be added
     */
    suspend fun putListLong(key: String, longList: ArrayList<Long>) {
        checkForNullKey(key)
        val myLongList = longList.toTypedArray()
        context.preferences.edit{
            it[stringPreferencesKey(key)] = TextUtils.join("‚‗‚", myLongList)
        }
    }

    /**
     * Put float value into DataStore with 'key' and save
     * @param key DataStore key
     * @param value float value to be added
     */
    suspend fun putFloat(key: String, value: Float) {
        checkForNullKey(key)
        context.preferences.edit {
            it[floatPreferencesKey(key)] = value
        }
    }

    /**
     * Put double value into DataStore with 'key' and save
     * @param key DataStore key
     * @param value double value to be added
     */
    suspend fun putDouble(key: String, value: Double) {
        checkForNullKey(key)
        putString(key, value.toString())
    }

    /**
     * Put ArrayList of Double into DataStore with 'key' and save
     * @param key DataStore key
     * @param doubleList ArrayList of Double to be added
     */


    suspend fun putListDouble(key: String, doubleList: ArrayList<Double>) {
        checkForNullKey(key)
        val myDoubleList = doubleList.toTypedArray()
        context.preferences.edit {
            it[stringPreferencesKey(key)] = TextUtils.join("‚‗‚", myDoubleList)
        }
    }

    /**
     * Put String value into DataStore with 'key' and save
     * @param key DataStore key
     * @param value String value to be added
     */
    suspend fun putString(key: String, value: String) {
        checkForNullKey(key)
        checkForNullValue(value)
        context.preferences.edit {
            it[stringPreferencesKey(key)] = value
        }
    }

    /**
     * Put ArrayList of String into DataStore with 'key' and save
     * @param key DataStore key
     * @param stringList ArrayList of String to be added
     */
    suspend fun putListString(key: String, stringList: ArrayList<String>) {
        checkForNullKey(key)
        val myStringList = stringList.toTypedArray()
        context.preferences.edit {
            it[stringPreferencesKey(key)] =  TextUtils.join("‚‗‚", myStringList)
        }
    }

    /**
     * Put boolean value into DataStore with 'key' and save
     * @param key DataStore key
     * @param value boolean value to be added
     */
    suspend fun putBoolean(key: String, value: Boolean) {
        checkForNullKey(key)
        context.preferences.edit {
            it[booleanPreferencesKey(key)] = value
        }
    }

    /**
     * Put ArrayList of Boolean into DataStore with 'key' and save
     * @param key DataStore key
     * @param boolList ArrayList of Boolean to be added
     */
   suspend fun putListBoolean(key: String, boolList: ArrayList<Boolean>) {
        checkForNullKey(key)
        val newList = ArrayList<String>()
        for (item in boolList) {
            if (item) {
                newList.add("true")
            } else {
                newList.add("false")
            }
        }
        putListString(key, newList)
    }

    /**
     * Put ObJect any type into SharedPrefrences with 'key' and save
     * @param key DataStore key
     * @param obj is the Object you want to put
     */

    suspend fun putObject(key: String, obj: Any?) {
        checkForNullKey(key)
        val gson = Gson()
        putString(key, gson.toJson(obj))
    }

    suspend fun putListObject(key: String, objArray: ArrayList<Any>) {
        checkForNullKey(key)
        val gson = Gson()
        val objStrings = ArrayList<String>()
        for (obj in objArray) {
            objStrings.add(gson.toJson(obj))
        }
        putListString(key, objStrings)
    }

    /**
     * Remove DataStore item with 'key'
     * @param key DataStore key
     */
    suspend fun remove(key: String) {
        context.preferences.edit {
            it.remove(stringPreferencesKey(key))
        }
    }

    /**
     * Clear DataStore (remove everything)
     */
    suspend fun clear() {
        context.preferences.edit { it.clear() }
    }

    /**
     * null keys would corrupt the shared pref file and make them unreadable this is a preventive measure
     * @param key the pref key to check
     */
    private fun checkForNullKey(key: String?) {
        if (key == null) {
            throw NullPointerException()
        }
    }

    /**
     * null keys would corrupt the shared pref file and make them unreadable this is a preventive measure
     * @param value the pref value to check
     */
    private fun checkForNullValue(value: String?) {
        if (value == null) {
            throw NullPointerException()
        }
    }

    /**
     * Check if external storage is writable or not
     * @return true if writable, false otherwise
     */
    val isExternalStorageWritable: Boolean get() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

    /**
     * Check if external storage is readable or not
     * @return true if readable, false otherwise
     */
    val isExternalStorageReadable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
        }

}