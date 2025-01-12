package com.bignerdranch.android.photogallery.api

import com.bignerdranch.android.photogallery.BuildConfig
import retrofit2.Call
import retrofit2.http.GET

private const val apiKey = BuildConfig.MY_API

interface FlickrApi {

    @GET("services/rest/?method=flickr.interestingness.getList" +
        "&api_key=$apiKey" +
        "&format=json" +
        "&nojsoncallback=1" +
        "&extras=url_s"
    )
    fun fetchPhotos(): Call<FlickrResponse>
}