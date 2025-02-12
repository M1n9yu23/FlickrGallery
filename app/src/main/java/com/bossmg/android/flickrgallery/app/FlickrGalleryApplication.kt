package com.bossmg.android.flickrgallery.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.bossmg.android.flickrgallery.R

const val NOTIFICATION_CHANNEL_ID = "flickr_poll"

// 애플리케이션 전역 설정을 담당하는 클래스
// AndroidManifest.xml에서 application 태그에 android:name=".FlickrGalleryApplication"으로 등록해야 함
class FlickrGalleryApplication : Application() {

    // 애플리케이션이 시작될 때 호출되는 함수
    override fun onCreate() {
        super.onCreate()

        // Android 8.0 (Oreo, API 26) 이상에서는 알림 채널을 반드시 생성해야 함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 알림 채널의 이름을 문자열 리소스에서 가져옴
            val name = getString(R.string.notification_channel_name)

            // 알림 중요도를 기본 수준(IMPORTANCE_DEFAULT)으로 설정
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            // 알림 채널 객체 생성
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)

            // 시스템의 NotificationManager 가져오기
            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)

            // 알림 채널을 시스템에 등록
            notificationManager.createNotificationChannel(channel)
        }
    }
}
