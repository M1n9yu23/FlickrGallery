package com.bossmg.android.flickrgallery.api

import com.bossmg.android.flickrgallery.data.GalleryItem
import com.google.gson.annotations.SerializedName

class PhotoResponse {
    @SerializedName("photo")
    lateinit var galleryItems: List<GalleryItem>
}