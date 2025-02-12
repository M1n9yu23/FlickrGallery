package com.bossmg.android.flickrgallery.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bossmg.android.flickrgallery.R

// Flickr에서 가져온 사진 목록을 표시하는 액티비티
class FlickrGalleryActivity : AppCompatActivity() {

    /**
     * 액티비티가 생성될 때 호출되는 함수
     * @param savedInstanceState: 이전 상태 저장 (null이면 처음 실행된 것)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flickr_gallery)

        // 액티비티가 처음 실행된 경우에만 프래그먼트를 추가
        val isFragmentContainerEmpty = savedInstanceState == null
        if (isFragmentContainerEmpty) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragmentContainer, FlickrGalleryFragment.newInstance()) // 프래그먼트 추가
                .commit()
        }
    }

    companion object{
        /**
         * FlickrGalleryActivity를 실행하기 위한 인텐트를 생성하는 함수
         * @param context: 호출하는 액티비티의 컨텍스트
         * @return Intent: FlickrGalleryActivity를 실행하는 인텐트
         */
        fun newIntent(context: Context): Intent {
            return Intent(context, FlickrGalleryActivity::class.java)
        }
    }
}