package com.lambikene

import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var checkBox1: CheckBox
    private lateinit var checkBox2: CheckBox
    private lateinit var mainLayout: View
    private lateinit var imageButton: ImageButton
    private var isFlashOn = false
    private var isCheckBox1Checked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val batteryPercentageTextView = findViewById<TextView>(R.id.batteryPercentageTextView)

        checkBox1 = findViewById(R.id.checkBox1)
        checkBox2 = findViewById(R.id.checkBox2)
        mainLayout = findViewById(R.id.mainLayout)

        imageButton = findViewById<ImageButton>(R.id.imageButton)

        imageButton.setOnClickListener {
            val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]

            try {
                isFlashOn = !isFlashOn

                if (isFlashOn) {
                    cameraManager.setTorchMode(cameraId, true)
                    imageButton.setImageResource(R.drawable.pilt_1)
                } else {
                    cameraManager.setTorchMode(cameraId, false)
                    imageButton.setImageResource(R.drawable.pilt_2)
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

        checkBox1.setOnCheckedChangeListener { _, isChecked ->
            isCheckBox1Checked = isChecked

            if (isChecked) {
                mainLayout.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
                if (hasWriteSettingsPermission()) {
                    // Muuda ekraani heledust
                    setScreenBrightness(1.0f)
                } else {
                    // Küsi kasutajalt luba
                    requestWriteSettingsPermission()
                }
            } else {
                mainLayout.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
                // Muuda ekraani heledust tagasi vaikimisi väärtuseks
                setScreenBrightness(-1f)
            }
        }

        checkBox2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val batteryLevel = getBatteryLevel() // Saate akulaetuse protsendi
                batteryPercentageTextView.text = "Akulaetus: $batteryLevel%" // Värskendage TextView teksti
            } else {
                batteryPercentageTextView.text = "" // Kui checkbox 2 ei ole märgitud, eemaldage akulaetuse protsendi kuvamine
            }
        }
    }

    private fun getBatteryLevel(): Int {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return (level.toFloat() / scale * 100).toInt()
    }

    private fun hasWriteSettingsPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(this)
        } else {
            // Enne Android M-i pole luba vaja
            true
        }
    }

    private fun requestWriteSettingsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS)
        }
    }

    private fun setScreenBrightness(brightness: Float) {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = brightness
        window.attributes = layoutParams
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
            if (hasWriteSettingsPermission()) {
                // Kui kasutaja andis luba, muuda ekraani heledust
                setScreenBrightness(1.0f)
            } else {
                // Kui kasutaja ei andnud luba, tühista CheckBox
                checkBox1.isChecked = false
                Toast.makeText(this, "Ekraani heleduse muutmiseks tuleb anda luba", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_WRITE_SETTINGS = 1001
    }
}
