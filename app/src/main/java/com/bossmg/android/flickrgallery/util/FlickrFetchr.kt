package com.bossmg.android.flickrgallery.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bossmg.android.flickrgallery.api.FlickrApi
import com.bossmg.android.flickrgallery.api.FlickrResponse
import com.bossmg.android.flickrgallery.api.PhotoInterceptor
import com.bossmg.android.flickrgallery.api.PhotoResponse
import com.bossmg.android.flickrgallery.data.GalleryItem
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "FlickrFetchr"

/**
 * Flickr API에서 데이터를 가져오는 리포지터리 클래스
 * - Retrofit을 사용하여 네트워크 요청을 수행
 * - 사진 데이터를 가져와 `LiveData<List<GalleryItem>>`로 변환하여 UI에서 사용할 수 있도록 제공
 */
class FlickrFetchr {

    private val flickrApi: FlickrApi

    init {
        // OkHttpClient 설정: API 요청 시 Interceptor 추가
        val client = OkHttpClient.Builder()
            .addInterceptor(PhotoInterceptor()) // API 요청에 공통적으로 필요한 파라미터 추가 (API 키 등)
            .build()

        // Retrofit 설정: Base URL 및 JSON 변환기 추가
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/") // Flickr API의 기본 URL
            .addConverterFactory(GsonConverterFactory.create()) // JSON 데이터를 객체로 변환
            .client(client) // OkHttpClient 적용
            .build()

        // Retrofit을 사용하여 FlickrApi 인터페이스 구현체 생성
        flickrApi = retrofit.create(FlickrApi::class.java)

    }

    /**
     * 최근 사진 목록을 요청하는 Call 객체 반환
     */
    fun fetchPhotosRequest(): Call<FlickrResponse> {
        return flickrApi.fetchPhotos()
    }

    /**
     * 최근 사진 목록을 가져와 `LiveData<List<GalleryItem>>`로 반환
     * - fetchPhotoMetadata()를 호출하여 API 응답을 변환
     */
    fun fetchPhotos(): LiveData<List<GalleryItem>> {
        return fetchPhotoMetadata(fetchPhotosRequest())
    }

    /**
     * 검색어를 이용하여 Flickr API에서 사진을 검색하는 Call 객체 반환
     * @param query: 검색어
     */
    fun searchPhotosRequest(query: String): Call<FlickrResponse> {
        return flickrApi.searchPhotos(query)
    }

    /**
     * 검색어를 이용하여 Flickr API에서 사진을 검색하고 `LiveData<List<GalleryItem>>`로 반환
     * - fetchPhotoMetadata()를 호출하여 API 응답을 변환
     * @param query: 검색어
     */
    fun searchPhotos(query: String): LiveData<List<GalleryItem>> {
        return fetchPhotoMetadata(searchPhotosRequest(query))
    }

    /**
     * API에서 받아온 Flickr 사진 데이터를 `LiveData<List<GalleryItem>>`로 변환
     * - 네트워크 요청을 비동기적으로 실행하고, 응답을 LiveData로 반환
     * @param flickrRequest: API 호출 객체
     */
    private fun fetchPhotoMetadata(flickrRequest: Call<FlickrResponse>): LiveData<List<GalleryItem>> {
        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()

        // 네트워크 요청을 비동기적으로 실행 (enqueue 사용)
        flickrRequest.enqueue(object : Callback<FlickrResponse> {
            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
                Log.e(TAG, "Failed to fetch photos", t)
            }

            override fun onResponse(call: Call<FlickrResponse>, response: Response<FlickrResponse>) {
                Log.e(TAG, "Response received")

                // 응답에서 FlickrResponse 객체 추출
                val flickrResponse: FlickrResponse? = response.body()
                val photoResponse: PhotoResponse? = flickrResponse?.photos

                // photoResponse가 null이면 빈 리스트 반환
                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems ?: mutableListOf()

                // url이 비어 있는 항목을 필터링하여 제거
                galleryItems = galleryItems.filterNot {
                    it.url.isBlank()
                }

                // 변환된 데이터 LiveData에 저장
                responseLiveData.value = galleryItems
            }
        })

        return responseLiveData
    }

    /**
     * 특정 URL에서 사진을 가져와 Bitmap으로 변환
     * - 이 함수는 백그라운드 스레드에서 실행되어야 함 (`@WorkerThread` 어노테이션 추가)
     * @param url: 사진의 원본 URL
     * @return Bitmap 객체 (다운로드 실패 시 null 반환)
     */
    @WorkerThread
    fun fetchPhoto(url: String): Bitmap? {
        // 네트워크 요청을 동기적으로 실행 (execute 사용)
        val response: Response<ResponseBody> = flickrApi.fetchUrlBytes(url).execute()

        // 응답 본문에서 InputStream을 가져와 Bitmap으로 변환
        val bitmap = response.body()?.byteStream()?.use(BitmapFactory::decodeStream)

        Log.i(TAG, "Decoded bitmap=$bitmap from Response=$response")

        return bitmap
    }
}