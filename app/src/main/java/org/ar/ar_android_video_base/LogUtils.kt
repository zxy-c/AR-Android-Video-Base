package org.ar.ar_android_video_base

import android.app.Activity
import java.text.SimpleDateFormat
import java.util.*

fun Activity.todayLogFilePath(): String {
    return filesDir.absolutePath + "/logs/" + SimpleDateFormat("yyyy-mm-dd", Locale.getDefault()).format(
        Date()
    )
}