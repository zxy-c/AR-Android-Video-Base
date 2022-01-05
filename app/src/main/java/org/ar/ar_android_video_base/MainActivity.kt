package org.ar.ar_android_video_base

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.runtime.Permission
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.ar.ar_android_video_base.databinding.ActivityMainBinding
import org.ar.ar_android_video_base.java.JavaVideoActivity
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset

class MainActivity : AppCompatActivity(){

    private val viewBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.run {
            input.addTextChangedListener(textWatcher)
            join.isEnabled = false
            join.setOnClickListener {
                startActivity(Intent().apply {
                    setClass(this@MainActivity,VideoActivity::class.java)
                    putExtra("channelId",viewBinding.input.text.toString())
                })
            }

        }

        if (!AndPermission.hasPermissions(this, Permission.Group.STORAGE,
                        Permission.Group.MICROPHONE)){
            AndPermission.with(this)
                    .runtime()
                    .permission(
                    Permission.Group.STORAGE,
                    Permission.Group.MICROPHONE,
                    Permission.Group.CAMERA
            ).onGranted {}.start();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()
        val logFile = File(todayLogFilePath() + "/ar_sdk.log")
        if (logFile.exists()){
            Log.i("log",logFile.path)
            viewBinding.log.text = IOUtils.toString(FileInputStream(logFile), Charset.defaultCharset())
            viewBinding.log.setOnTouchListener { v, event ->
                object : GestureDetector(applicationContext,
                    object : GestureDetector.SimpleOnGestureListener() {
                        override fun onLongPress(e: MotionEvent?) {
                            (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
                                ClipData.newPlainText("log", viewBinding.log.text)
                            )
                            Toast.makeText(applicationContext,"Copied to Clipboard",Toast.LENGTH_SHORT).show()
                        }
                    }) {}.onTouchEvent(event)
            }
        }
    }

    private var textWatcher: TextWatcher =object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
        }
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            p0?.let {
                viewBinding.join.isEnabled = !it.toString().isNullOrEmpty()
            }
        }
    }

}