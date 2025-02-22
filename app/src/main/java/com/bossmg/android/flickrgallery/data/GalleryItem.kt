package com.bossmg.android.flickrgallery.data

import android.net.Uri
import com.google.gson.annotations.SerializedName

data class GalleryItem(
    var title: String = "",
    var id: String = "",
    @SerializedName("url_s")
    var url: String = "",
    @SerializedName("owner")
    var owner: String = "", // 작성자 고유 ID
    @SerializedName("ownername")
    var ownerName: String = "", // 작성자 이름
    @SerializedName("dateupload")
    var dateUpload: String = "", // 업로드 날짜
    @SerializedName("datetaken")
    var dateTaken: String = "", // 촬영 날짜
    @SerializedName("tags")
    var tags: String = "" // 사진 태그
) {
    // 사진 상세 페이지 URI 생성
    val photoPageUri: Uri
        get() {
            return Uri.parse("https://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(owner) // 작성자의 고유 ID
                .appendPath(id) // 사진 ID
                .build()
        }
}