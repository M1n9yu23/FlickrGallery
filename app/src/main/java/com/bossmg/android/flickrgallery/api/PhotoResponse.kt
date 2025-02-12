package com.bossmg.android.flickrgallery.api

import com.bossmg.android.flickrgallery.data.GalleryItem
import com.google.gson.annotations.SerializedName

// Flickr API 응답에서 "photo" 배열을 파싱하는 클래스
class PhotoResponse {

    // JSON 응답에서 "photo" 필드를 "galleryItems" 리스트로 매핑
    @SerializedName("photo")
    lateinit var galleryItems: List<GalleryItem>
    // - @SerializedName("photo"): JSON의 "photo" 필드를 `galleryItems` 변수에 매핑
    // - List<GalleryItem>: 여러 개의 사진 데이터를 저장하는 리스트
}