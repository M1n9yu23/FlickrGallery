package com.bossmg.android.flickrgallery

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat

private const val TAG = "NotificationReceiver"

class NotificationReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "received broadcast: ${intent.action}")
        if(resultCode != Activity.RESULT_OK) {
            // 액티비티가 포그라운드에 있으면 브로드캐스트 인텐트를 취소한다.
            return
        }

        val requestCode = intent.getIntExtra(PollWorker.REQUEST_CODE, 0)

        // Android 13(API 33) 이상에서는 getParcelableExtra()가 deprecated 되었음 대체 API 사용
        val notification: Notification? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(PollWorker.NOTIFICATION, Notification::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(PollWorker.NOTIFICATION) // API 32이하
        }

        if (notification == null) {
            Log.e(TAG, "Notification NUll")
            return
        }

        with(NotificationManagerCompat.from(context)){
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return@with
            }
            notify(requestCode, notification)
        }
    }
}