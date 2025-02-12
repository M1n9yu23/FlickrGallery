package com.bossmg.android.flickrgallery.util

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bossmg.android.flickrgallery.worker.PollWorker

private const val TAG = "VisibleFragment"

/**
 * 사용자가 현재 보고 있는 프래그먼트
 * - PollWorker에서 알림을 표시하려 할 때, 앱이 포그라운드에 있으면 알림을 취소하도록 처리
 * - PollWorker에서 `ACTION_SHOW_NOTIFICATION` 브로드캐스트를 받을 경우 알림을 취소
 */
abstract class VisibleFragment: Fragment() {

    /**
     * 알림 브로드캐스트를 수신하는 리시버
     * - 앱이 포그라운드에 있으면 알림(Notification)을 취소함
     */
    private val onShowNotification = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            Log.i(TAG, "canceling notification")
            resultCode = Activity.RESULT_CANCELED // 알림을 취소하기 위해 결과 코드 변경
        }
    }

    /**
     * 프래그먼트가 시작될 때(화면에 보일 때) 실행됨
     * - `PollWorker.ACTION_SHOW_NOTIFICATION` 브로드캐스트를 감지하는 리시버를 등록
     */
    override fun onStart() {
        super.onStart()

        // 특정 이벤트(ACTION_SHOW_NOTIFICATION) 필터 생성
        val filter = IntentFilter(PollWorker.ACTION_SHOW_NOTIFICATION)

        // 브로드캐스트 리시버 등록 (앱이 실행 중이면 알림을 취소)
        ContextCompat.registerReceiver(
            requireActivity(),          // 수신 대상 액티비티
            onShowNotification,         // 브로드캐스트 리시버
            filter,                     // 필터 (ACTION_SHOW_NOTIFICATION 감지)
            PollWorker.PERM_PRIVATE,    // 권한 설정 (앱 내부에서만 사용)
            null,              // 핸들러 (사용하지 않음)
            ContextCompat.RECEIVER_NOT_EXPORTED // 외부 앱에서 호출할 수 없도록 제한
        )
    }

    /**
     * 프래그먼트가 종료될 때 실행됨
     * - 등록한 브로드캐스트 리시버를 해제하여 메모리 누수를 방지
     */
    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(onShowNotification)
    }
}