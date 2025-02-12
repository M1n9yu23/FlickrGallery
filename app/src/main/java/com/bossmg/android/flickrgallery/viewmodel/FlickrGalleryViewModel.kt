package com.bossmg.android.flickrgallery.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.bossmg.android.flickrgallery.util.FlickrFetchr
import com.bossmg.android.flickrgallery.data.GalleryItem
import com.bossmg.android.flickrgallery.data.QueryPreferences

// 안드로이드 앱에서 Flickr API를 통해 사진 데이터를 관리하는 ViewModel
// - LiveData를 활용하여 UI와 데이터 흐름을 효율적으로 연결
// - 사용자가 검색어를 입력하면 해당 검색어를 기반으로 Flickr API 요청을 보냄
// - 안드로이드 애플리케이션 컨텍스트를 필요로 하기 때문에 AndroidViewModel을 상속받음
class FlickrGalleryViewModel(private val app: Application) : AndroidViewModel(app) {

    // UI에서 관찰할 수 있는 LiveData, Flickr에서 가져온 사진 데이터를 저장
    val galleryItemLiveData: LiveData<List<GalleryItem>>

    // Flickr API와 통신하는 리포지토리 객체
    private val flickrFetchr = FlickrFetchr()

    // 사용자의 검색어를 저장하는 MutableLiveData (변경 가능)
    private val mutableSearchTerm = MutableLiveData<String>()

    // 검색어를 가져오는 속성 (검색어가 없으면 기본값은 빈 문자열)
    val searchTerm: String
        get() = mutableSearchTerm.value ?: ""

    /**
     * ViewModel 초기화 블록
     * - 이전 검색어를 SharedPreferences에서 불러와서 mutableSearchTerm에 설정
     * - mutableSearchTerm이 변경될 때마다 Flickr API에서 데이터를 불러오도록 switchMap 설정
     */
    init {
        // 앱 실행 시 SharedPreferences에서 저장된 검색어를 불러옴
        mutableSearchTerm.value = QueryPreferences.getStoredQuery(app)

        // mutableSearchTerm 값이 변경될 때마다 새로운 API 요청을 실행
        galleryItemLiveData = mutableSearchTerm.switchMap { searchTerm ->
            liveData {
                val result = if (searchTerm.isBlank()) {
                    // 검색어가 없을 경우 최근 사진 리스트 가져오기
                    flickrFetchr.fetchPhotos()
                } else {
                    // 검색어가 있는 경우 해당 검색어를 기반으로 검색 결과 가져오기
                    flickrFetchr.searchPhotos(searchTerm)
                }
                emitSource(result) // 결과를 LiveData로 방출하여 UI에서 감지할 수 있도록 함
            }
        }
    }

    /**
     * 사용자가 새로운 검색어를 입력했을 때 실행되는 함수
     * - 검색어를 SharedPreferences에 저장하고 MutableLiveData를 업데이트하여 새로운 검색 수행
     * @param query 사용자가 입력한 검색어 (기본값은 빈 문자열)
     */
    fun fetchPhotos(query: String = "") {
        QueryPreferences.setStoredQuery(app, query) // 검색어를 SharedPreferences에 저장
        mutableSearchTerm.value = query // 검색어 업데이트 (LiveData가 감지하여 새로운 데이터 요청)
    }

    /**
     * 특정 태그를 기반으로 Flickr API에서 사진을 가져오는 함수
     * - UI에서 특정 태그를 선택하면 해당 태그를 검색어로 설정하여 API 요청 수행
     * @param tag 검색할 태그
     */
    fun fetchPhotosTag(tag: String) {
        mutableSearchTerm.value = tag  // 검색어 대신 태그를 설정하여 LiveData 갱신
    }
}