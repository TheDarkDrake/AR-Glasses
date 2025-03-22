package com.augmenters.arglasses

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.util.Log
import android.widget.*
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import com.augmenters.arglasses.R
import org.json.JSONObject

class MainActivity : Activity() {
    private val deviceName = "SmartGlass"
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val REQUEST_PERMISSIONS = 1

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechButton: Button
    private lateinit var statusText: TextView
    private lateinit var translateSwitch: Switch
    private lateinit var notificationSwitch: Switch
    private lateinit var sourceLanguageSpinner: Spinner
    private lateinit var targetLanguageSpinner: Spinner

    private val languages = arrayOf(
        "en", "sq", "ar", "az", "eu", "bn", "bg", "ca", "zh", "zh-TW", "cs",
        "da", "nl", "eo", "et", "fi", "fr", "gl", "de", "el", "he", "hi", "hu",
        "id", "ga", "it", "ja", "ko", "lt", "lv", "ms", "nb", "pl", "pt", "ro",
        "ru", "sk", "sl", "es", "sv", "th", "tl", "tr", "uk", "ur"
    )

    private val languageNames = arrayOf(
        "English", "Albanian", "Arabic", "Azerbaijani", "Basque", "Bengali", "Bulgarian",
        "Catalan", "Chinese", "Chinese (Traditional)", "Czech", "Danish", "Dutch",
        "Esperanto", "Estonian", "Finnish", "French", "Galician", "German", "Greek",
        "Hebrew", "Hindi", "Hungarian", "Indonesian", "Irish", "Italian", "Japanese",
        "Korean", "Lithuanian", "Latvian", "Malay", "Norwegian Bokmål", "Polish",
        "Portuguese", "Romanian", "Russian", "Slovak", "Slovenian", "Spanish", "Swedish",
        "Thai", "Tagalog", "Turkish", "Ukrainian", "Urdu"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        speechButton = findViewById(R.id.speechButton)
        statusText = findViewById(R.id.statusText)
        translateSwitch = findViewById(R.id.translateSwitch)
        notificationSwitch = findViewById(R.id.notificationSwitch)
        sourceLanguageSpinner = findViewById(R.id.sourceLanguageSpinner)
        targetLanguageSpinner = findViewById(R.id.targetLanguageSpinner)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languageNames)
        sourceLanguageSpinner.adapter = adapter
        targetLanguageSpinner.adapter = adapter

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        connectToSmartGlass()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    val recognizedText = matches[0]
                    statusText.text = recognizedText
                    if (translateSwitch.isChecked) {
                        val sourceLang = languages[sourceLanguageSpinner.selectedItemPosition]
                        val targetLang = languages[targetLanguageSpinner.selectedItemPosition]
                        translateText(recognizedText, sourceLang, targetLang)
                    } else {
                        sendToSmartGlass(recognizedText)
                    }
                }
                startListening()
            }
            override fun onError(error: Int) { startListening() }
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechButton.setOnClickListener { startListening() }
        translateSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                statusText.text = "Translation Enabled"
            } else {
                statusText.text = "Translation Disabled"
            }
        }

        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            val prefs = getSharedPreferences("com.augmenters.arglasses", MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putBoolean("read_notifications", isChecked)
            editor.apply()
        }

        requestPermissions()
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE
        )
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS)
    }

    private fun connectToSmartGlass() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothAdapter?.bondedDevices?.forEach { device ->
            if (device.name == deviceName) {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream
                Log.d("Bluetooth", "Connected to SmartGlass")
            }
        }
    }

    private fun sendToSmartGlass(text: String) {
        outputStream?.write((text + "\n").toByteArray())
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        val sourceLang = languages[sourceLanguageSpinner.selectedItemPosition]
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, sourceLang)
        speechRecognizer.startListening(intent)
    }

    private fun translateText(text: String, sourceLang: String, targetLang: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("http://192.168.149.25:5000/translate")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")

                val jsonInputString = JSONObject()
                    .put("q", text)
                    .put("source", sourceLang)
                    .put("target", targetLang)
                    .put("format", "text")
                    .toString()

                connection.doOutput = true
                connection.outputStream.use { os ->
                    val input = jsonInputString.toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                connection.inputStream.use { `is` ->
                    val response = `is`.bufferedReader().use { it.readText() }
                    val translatedText = JSONObject(response).getString("translatedText")

                    withContext(Dispatchers.Main) {
                        // Update the UI to show translated text
                        statusText.text = "Translated: $translatedText"
                        sendToSmartGlass(translatedText)
                    }
                }
            } catch (e: Exception) {
                Log.e("Translation", "Translation failed: ${e.message}")

                withContext(Dispatchers.Main) {
                    statusText.text = "Translation Error"
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
        bluetoothSocket?.close()
    }
}