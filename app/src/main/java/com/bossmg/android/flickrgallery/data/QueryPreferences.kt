package com.bossmg.android.flickrgallery.data

import android.content.Context
import android.content.SharedPreferences

private const val PREF_SEARCH_QUERY = "searchQuery"
private const val PREF_LAST_RESULT_ID = "lastResultId"
private const val PREF_IS_POLLING = "isPolling"
// referenceManager.getDefaultSharedPreferences(context)는 내부적으로 "<패키지명>_preferences.xml" 파일을 사용했음.
// 최신 방식에서는 사용자가 원하는 이름을 직접 정할 수 있음.
private const val PREF_NAME = "app_prefs"

object QueryPreferences {

    // PreferenceManager.getDefaultSharedPreferences(context) 대체
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getStoredQuery(context: Context): String {
        return getPreferences(context).getString(PREF_SEARCH_QUERY, "")!!
    }

    fun setStoredQuery(context: Context, query: String) {
        getPreferences(context)
            .edit()
            .putString(PREF_SEARCH_QUERY, query)
            .apply()
    }

    fun isPolling(context: Context): Boolean {
        return getPreferences(context).getBoolean(PREF_IS_POLLING, false)
    }

    fun setPolling(context: Context, isOn: Boolean) {
        getPreferences(context)
            .edit()
            .putBoolean(PREF_IS_POLLING, isOn)
            .apply()
    }

    fun getLastResultId(context: Context): String {
        return getPreferences(context).getString(PREF_LAST_RESULT_ID, "")!!
    }

    fun setLastResultId(context: Context, lastResultId: String) {
        getPreferences(context)
            .edit()
            .putString(PREF_LAST_RESULT_ID, lastResultId)
            .apply()
    }

}