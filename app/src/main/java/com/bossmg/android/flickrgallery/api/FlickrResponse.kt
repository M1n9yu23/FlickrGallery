package com.bossmg.android.flickrgallery.api

// Flickr API의 응답을 매핑하기 위한 데이터 모델 클래스
class FlickrResponse {
    // Flickr API 응답에서 "photos"라는 필드를 파싱하여 저장하는 변수
    lateinit var photos: PhotoResponse
    // - lateinit: 객체를 즉시 초기화하지 않고, 나중에 반드시 초기화될 것을 보장하기 위해 사용
    // - PhotoResponse: "photos" 객체를 저장하는 데이터 클래스
}