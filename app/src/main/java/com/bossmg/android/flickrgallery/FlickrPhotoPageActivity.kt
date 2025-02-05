package com.bossmg.android.flickrgallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class FlickrPhotoPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flickr_photo_page)

        val fm = supportFragmentManager
        val currentFragment = fm.findFragmentById(R.id.fragment_container)

        if(currentFragment == null) {
            val fragment = FlickrPhotoPageFragment.newInstance(intent.data!!)
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit()
        }
    }

    companion object{
        fun newIntent(context: Context, photoPageUri: Uri) : Intent {
            return Intent(context, FlickrPhotoPageActivity::class.java).apply {
                data = photoPageUri
            }
        }
    }
}