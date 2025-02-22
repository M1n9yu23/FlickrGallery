package com.bossmg.android.flickrgallery.api

import com.bossmg.android.flickrgallery.BuildConfig
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

private const val API_KEY = BuildConfig.MY_API

/**
 * Flickr API와 통신하기 위한 Retrofit Interface
 */
interface FlickrApi {

    // 플리커에서 최근 사진 목록을 가져오는 API 호출
    // HTTP GET 요청을 보냄
    @GET("services/rest?method=flickr.interestingness.getList")
    fun fetchPhotos(): Call<FlickrResponse>
    // 반환 타입: Call<FlickrResponse>
    // - Retrofit이 비동기 네트워크 요청을 처리하는 Call 객체 반환
    // - FlickrResponse: API 응답을 저장할 클래스

    // Flickr에서 검색어에 해당 되는 사진을 검색하는 API 호출
    @GET("services/rest?method=flickr.photos.search")
    fun searchPhotos(@Query("text") query: String): Call<FlickrResponse>
    // 반환 타입: Call<FlickrResponse>
    // - 검색어를 쿼리 매개변수(text)로 전달하여 검색 결과를 가져옴
    // - @Query("text"): URL의 쿼리 파라미터로 `text=검색어` 형태로 변환됨
}