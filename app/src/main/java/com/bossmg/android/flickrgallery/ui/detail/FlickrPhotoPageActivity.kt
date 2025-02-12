package com.bossmg.android.flickrgallery.ui.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bossmg.android.flickrgallery.R

// Flickr 사진 상세 페이지를 보여주는 Activity
class FlickrPhotoPageActivity : AppCompatActivity() {

    // 액티비티가 생성될 때 호출되는 생명주기 함수
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flickr_photo_page)

        // FragmentManager를 가져옴 (프래그먼트 관리)
        val fm = supportFragmentManager

        // 현재 fragment_container에 추가된 프래그먼트가 있는지 확인
        val currentFragment = fm.findFragmentById(R.id.fragment_container)

        // 만약 프래그먼트가 없다면 새로 생성하여 추가
        if(currentFragment == null) {
            // Intent에서 전달된 데이터(Uri)를 사용하여 FlickrPhotoPageFragment 생성
            val fragment = FlickrPhotoPageFragment.newInstance(intent.data!!)

            // 프래그먼트를 fragment_container에 추가
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit()
        }
    }

    companion object{

        /**
         * FlickrPhotoPageActivity를 실행하기 위한 인텐트를 생성하는 함수
         * @param context: 호출하는 액티비티의 컨텍스트
         * @param photoPageUri: 사진의 웹페이지 URL (예: Flickr 이미지 페이지)
         * @return Intent: FlickrPhotoPageActivity를 실행하는 인텐트
         */
        fun newIntent(context: Context, photoPageUri: Uri) : Intent {
            return Intent(context, FlickrPhotoPageActivity::class.java).apply {
                data = photoPageUri // 인텐트에 URI 데이터 추가
            }
        }
    }
}