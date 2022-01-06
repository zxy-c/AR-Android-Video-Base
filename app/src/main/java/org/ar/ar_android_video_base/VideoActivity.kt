package org.ar.ar_android_video_base

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.isSuccessful
import com.github.kittinunf.fuel.jackson.objectBody
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.ar.ar_android_video_base.api.StartRecordRequest
import org.ar.ar_android_video_base.databinding.ActivityVideoBinding
import org.ar.rtc.Constants.LOG_FILTER_DEBUG
import org.ar.rtc.IRtcEngineEventHandler
import org.ar.rtc.RtcEngine
import kotlin.coroutines.suspendCoroutine

class VideoActivity : AppCompatActivity(), View.OnClickListener {

    private val viewBinding by lazy { ActivityVideoBinding.inflate(layoutInflater) }
    private var channelId: String = ""
    private var userId: String = ""
    private lateinit var mRtcEngine: RtcEngine
    private var isMic: Boolean = false
    private var isCamera: Boolean = false
    private lateinit var videoAdapter: MemberAdapter

    private inner class RtcEvent : IRtcEngineEventHandler() {

        override fun onJoinChannelSuccess(channel: String, uid: String, elapsed: Int) {
            super.onJoinChannelSuccess(channel, uid, elapsed)
            CoroutineScope(Dispatchers.IO).launch {
                Fuel.post("http://rtcback.zhiyashengya.com/cloudRecord/start")
                    .objectBody(StartRecordRequest(channel, uid))
                    .response { _, response, _ ->
                        if (response.isSuccessful) {
                            runOnUiThread {
                                Toast.makeText(
                                    applicationContext,
                                    "Start recording",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
            }
            runOnUiThread {
                val member = Member(uid)
                mRtcEngine.setupLocalVideo(member.getVideoCanvas(this@VideoActivity))
                videoAdapter.addData(member)
            }
        }

        override fun onFirstRemoteVideoDecoded(uid: String, width: Int, height: Int, elapsed: Int) {
            super.onFirstRemoteVideoDecoded(uid, width, height, elapsed)
            runOnUiThread {
                val member = Member(uid)
                mRtcEngine.setupRemoteVideo(member.getVideoCanvas(this@VideoActivity))
                videoAdapter.addData(member)
            }
        }

        override fun onUserOffline(uid: String, reason: Int) {
            super.onUserOffline(uid, reason)
            runOnUiThread {
                videoAdapter.remove(Member(uid))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        channelId = intent.getStringExtra("channelId").toString()
        userId = App.app.userId
        mRtcEngine = RtcEngine.create(this, getString(R.string.ar_appid), RtcEvent()).also {
            it.enableVideo()
            it.setEnableSpeakerphone(true)
            it.setLogFilter(LOG_FILTER_DEBUG)
            it.setLogFile(todayLogFilePath())
        }
        initView()
        joinChannel()
    }

    private fun initView() {
        viewBinding.run {
            videoAdapter = MemberAdapter(mRtcEngine!!)
            rvVideo.layoutManager = GridLayoutManager(this@VideoActivity, 2)
            rvVideo.adapter = videoAdapter
            mic.setOnClickListener(this@VideoActivity)
            camera.setOnClickListener(this@VideoActivity)
            leave.setOnClickListener(this@VideoActivity)
        }
    }

    private fun joinChannel() {
        mRtcEngine.joinChannel(getString(R.string.ar_token), channelId, "", userId)
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.mic -> {
                isMic = !isMic
                viewBinding.mic.isSelected = isMic
                mRtcEngine.muteLocalAudioStream(isMic)
            }
            R.id.camera -> {
                isCamera = !isCamera
                viewBinding.camera.isSelected = isCamera
                mRtcEngine.switchCamera()
            }
            R.id.leave -> {
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        RtcEngine.destroy()
    }

}