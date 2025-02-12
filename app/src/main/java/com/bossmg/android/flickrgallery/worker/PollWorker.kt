package com.bossmg.android.flickrgallery.worker

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bossmg.android.flickrgallery.util.FlickrFetchr
import com.bossmg.android.flickrgallery.app.NOTIFICATION_CHANNEL_ID
import com.bossmg.android.flickrgallery.R
import com.bossmg.android.flickrgallery.data.GalleryItem
import com.bossmg.android.flickrgallery.data.QueryPreferences
import com.bossmg.android.flickrgallery.ui.main.FlickrGalleryActivity

private const val TAG = "PollWorker"

/**
 * WorkManager를 사용하여 주기적으로 Flickr에서 새로운 사진을 가져오는 작업을 수행하는 클래스
 * - 백그라운드에서 실행되며, 새로운 사진이 있는 경우 알림(Notification)을 생성하여 사용자에게 알려줌
 */
class PollWorker(private val context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    /**
     * 백그라운드에서 실행되는 작업
     * - Flickr API에서 새로운 사진 데이터를 가져와 기존 데이터와 비교
     * - 새로운 데이터가 있으면 알림(Notification)을 생성
     */
    override fun doWork(): Result {
        // 저장된 검색어 가져오기 (SharedPreferences)
        val query = QueryPreferences.getStoredQuery(context)

        // 마지막으로 가져온 사진 ID 가져오기 (SharedPreferences)
        val lastResultId = QueryPreferences.getLastResultId(context)

        // Flickr API를 호출하여 새로운 사진 목록 가져오기
        val items: List<GalleryItem> = if (query.isEmpty()) {
            FlickrFetchr().fetchPhotosRequest()
                .execute()  // 네트워크 요청을 동기적으로 실행
                .body()     // 응답 바디 가져오기
                ?.photos    // FlickrResponse 객체에서 PhotoResponse 가져오기
                ?.galleryItems  // PhotoResponse에서 사진 리스트 가져오기
        } else {
            FlickrFetchr().searchPhotosRequest(query)
                .execute()
                .body()
                ?.photos
                ?.galleryItems
        } ?: emptyList()    // API 요청이 실패하면 빈 리스트 반환

        // 새로운 데이터가 없는 경우
        if (items.isEmpty()) {
            return Result.success() // WorkManager에서 작업 성공으로 기록
        }

        // 새로 가져온 첫 번째 사진의 ID 가져오기
        val resultId = items.first().id

        // 마지막으로 가져온 사진과 ID가 동일하면 "새로운 데이터 없음" 처리
        if (resultId == lastResultId) {
            Log.i(TAG, "Got an old result: $resultId")
        } else {
            // 새로운 데이터가 있는 경우
            Log.i(TAG, "Got a new result: $resultId")

            // 새로운 결과 ID를 SharedPreferences에 저장
            QueryPreferences.setLastResultId(context, resultId)

            // 알림을 표시할 PendingIntent 생성
            val intent = FlickrGalleryActivity.newIntent(context)
            val pendingIntent = PendingIntent.getActivity(context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE    // Android 12(API 31) 이상에서 필수
            )

            // 알림(Notification) 생성
            val resources = context.resources
            val notification = NotificationCompat
                .Builder(context, NOTIFICATION_CHANNEL_ID)
                .setTicker(resources.getString(R.string.new_pictures_title))    // 상태바에 표시될 짧은 메시지
                .setSmallIcon(android.R.drawable.ic_menu_report_image)          // 작은 아이콘 설정
                .setContentTitle(resources.getString(R.string.new_pictures_title))  // 알림 제목
                .setContentText(resources.getString(R.string.new_pictures_text))    // 알림 내용
                .setContentIntent(pendingIntent)    // 클릭 시 실행할 Intent 설정
                .setAutoCancel(true)    // 클릭 시 자동으로 알림 제거
                .build()

            // 백그라운드에서 알림 전송
            showBackgroundNotification(0, notification)
        }

        return Result.success() // WorkManager에서 작업 성공으로 기록
    }

    /**
     * 백그라운드에서 알림을 전송하는 함수
     * @param requestCode 요청 코드
     * @param notification 표시할 알림 객체
     */
    private fun showBackgroundNotification(
        requestCode: Int,
        notification: Notification
    ){
        val intent = Intent(ACTION_SHOW_NOTIFICATION).apply{
            putExtra(REQUEST_CODE, requestCode)
            putExtra(NOTIFICATION, notification)
        }

        // Ordered Broadcast를 사용하여 NotificationReceiver에 전달
        context.sendOrderedBroadcast(intent, PERM_PRIVATE)

    }

    companion object {
        // Broadcast 액션 정의 (알림을 표시할 때 사용)
        const val ACTION_SHOW_NOTIFICATION = "com.bossmg.android.flickrgallery.SHOW_NOTIFICATION"

        // Broadcast 수신 권한 (앱 내부에서만 사용)
        const val PERM_PRIVATE = "com.bossmg.android.flickrgallery.PRIVATE"

        // Intent에서 사용할 요청 코드 및 알림 객체 키
        const val REQUEST_CODE = "REQUEST_CODE"
        const val NOTIFICATION = "NOTIFICATION"
    }
}