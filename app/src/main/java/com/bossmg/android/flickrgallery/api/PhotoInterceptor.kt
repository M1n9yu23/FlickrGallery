package com.bossmg.android.flickrgallery.api

import com.bossmg.android.flickrgallery.BuildConfig
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

private const val API_KEY = BuildConfig.MY_API

// HTTP 요청을 가로채어(API Interceptor), 모든 Flickr API 요청에 기본 파라미터를 자동으로 추가하는 클래스
class PhotoInterceptor: Interceptor {
    // Retrofit/OkHttp 요청을 가로채어 새로운 URL을 생성하고 API 호출을 진행하는 함수
    override fun intercept(chain: Interceptor.Chain): Response {
        // 원래의 요청 객체를 가져옴
        val originalRequest: Request = chain.request()

        // 원래 요청의 URL을 기반으로 새로운 URL을 생성
        val newUrl: HttpUrl = originalRequest.url().newBuilder()
            .addQueryParameter("api_key", API_KEY) // Flickr API 키 추가 (필수)
            .addQueryParameter("format", "json") // 응답 형식을 JSON으로 지정
            .addQueryParameter("nojsoncallback", "1") // 콜백 제거 (JSON 형식 깨짐 방지)
            .addQueryParameter("extras", "url_s") // 추가 정보 요청 (예: 작은 크기의 이미지 URL)
            .addQueryParameter("safesearch","1") // 안전 검색 기능 활성화 (부적절한 이미지 제외)
            .build()

        // 새롭게 만든 URL을 사용하여 요청을 생성
        val newRequest: Request = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        // 새로운 요청을 실행하여 응답을 반환
        return chain.proceed(newRequest)
    }
}