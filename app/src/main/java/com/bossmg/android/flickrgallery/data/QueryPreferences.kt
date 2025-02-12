package com.bossmg.android.flickrgallery.data

import android.content.Context
import android.content.SharedPreferences

// SharedPreferences 키 (앱에서 저장할 값의 식별자)
private const val PREF_SEARCH_QUERY = "searchQuery" // 마지막 검색어 저장
private const val PREF_LAST_RESULT_ID = "lastResultId" // 마지막 검색 결과 ID 저장
private const val PREF_IS_POLLING = "isPolling" // 백그라운드 폴링 여부 저장
// referenceManager.getDefaultSharedPreferences(context)는 내부적으로 "<패키지명>_preferences.xml" 파일을 사용했음.
// 최신 방식에서는 사용자가 원하는 이름을 직접 정할 수 있음.
private const val PREF_NAME = "app_prefs"

// SharedPreferences를 이용해 앱의 설정 및 검색 정보를 저장하고 불러오는 유틸리티 객체
object QueryPreferences {

    /**
     * SharedPreferences 객체를 반환하는 함수
     * - 기존의 `PreferenceManager.getDefaultSharedPreferences(context)`를 대체
     * - `context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)`를 사용하여 직접 파일명을 지정
     * - Context.MODE_PRIVATE: 해당 앱에서만 접근 가능한 저장소 모드
     */
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 저장된 검색어를 가져오는 함수
     * - 저장된 값이 없으면 빈 문자열("")을 반환
     */
    fun getStoredQuery(context: Context): String {
        return getPreferences(context).getString(PREF_SEARCH_QUERY, "")!!
    }

    /**
     * 검색어를 SharedPreferences에 저장하는 함수
     * - `apply()`를 사용하여 비동기적으로 저장 (즉시 반영 X, 백그라운드에서 수행)
     */
    fun setStoredQuery(context: Context, query: String) {
        getPreferences(context)
            .edit()
            .putString(PREF_SEARCH_QUERY, query)
            .apply()
    }

    /**
     * 백그라운드 폴링(자동 새로고침)이 활성화되어 있는지 확인하는 함수
     * - 저장된 값이 없으면 기본값(false)을 반환
     */
    fun isPolling(context: Context): Boolean {
        return getPreferences(context).getBoolean(PREF_IS_POLLING, false)
    }

    /**
     * 백그라운드 폴링(자동 새로고침) 여부를 SharedPreferences에 저장하는 함수
     */
    fun setPolling(context: Context, isOn: Boolean) {
        getPreferences(context)
            .edit()
            .putBoolean(PREF_IS_POLLING, isOn)
            .apply()
    }

    /**
     * 마지막 검색 결과 ID를 가져오는 함수
     * - 저장된 값이 없으면 빈 문자열("")을 반환
     */
    fun getLastResultId(context: Context): String {
        return getPreferences(context).getString(PREF_LAST_RESULT_ID, "")!!
    }

    /**
     * 마지막 검색 결과 ID를 SharedPreferences에 저장하는 함수
     */
    fun setLastResultId(context: Context, lastResultId: String) {
        getPreferences(context)
            .edit()
            .putString(PREF_LAST_RESULT_ID, lastResultId)
            .apply()
    }

}