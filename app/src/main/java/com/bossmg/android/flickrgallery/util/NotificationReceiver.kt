package com.bossmg.android.flickrgallery.util

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
import com.bossmg.android.flickrgallery.worker.PollWorker

private const val TAG = "NotificationReceiver"

/**
 * 브로드캐스트 리시버 클래스
 * - PollWorker에서 보낸 브로드캐스트를 수신하여 알림(Notification)을 생성
 * - 액티비티가 포그라운드에 있을 경우 알림을 취소
 */
class NotificationReceiver: BroadcastReceiver() {

    /**
     * 브로드캐스트를 수신하면 호출되는 함수
     * @param context: 앱의 컨텍스트
     * @param intent: 전달된 인텐트 객체
     */
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "received broadcast: ${intent.action}")

        if(resultCode != Activity.RESULT_OK) {
            // 액티비티가 포그라운드에 있으면 브로드캐스트 인텐트를 취소한다.
            return
        }

        // PollWorker에서 설정한 요청 코드 가져오기
        val requestCode = intent.getIntExtra(PollWorker.REQUEST_CODE, 0)

        // Android 13(API 33) 이상에서는 getParcelableExtra()가 deprecated 되었음 대체 API 사용
        val notification: Notification? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(PollWorker.NOTIFICATION, Notification::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(PollWorker.NOTIFICATION) // API 32이하
        }

        // 알림이 null이면 로그 출력 후 종료
        if (notification == null) {
            Log.e(TAG, "Notification NUll")
            return
        }

        // 알림 매니저를 가져와서 알림을 표시
        with(NotificationManagerCompat.from(context)){
            // Android 13(API 33) 이상에서는 POST_NOTIFICATIONS 권한이 필요
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: 사용자에게 권한 요청 추가 필요
                return@with
            }

            // 알림을 생성
            notify(requestCode, notification)
        }
    }
}