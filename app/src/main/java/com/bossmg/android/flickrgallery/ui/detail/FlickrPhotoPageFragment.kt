package com.bossmg.android.flickrgallery.ui.detail

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.bossmg.android.flickrgallery.R
import com.bossmg.android.flickrgallery.util.VisibleFragment


private const val ARG_URI = "photo_page_url"

class FlickrPhotoPageFragment: VisibleFragment() {

    private lateinit var uri: Uri
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_URI, Uri::class.java) ?: Uri.EMPTY
        } else {
            @Suppress("DEPRECATION") // API 32 이하
            arguments?.getParcelable(ARG_URI) ?: Uri.EMPTY
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_flickr_photo_page, container, false)

        progressBar = view.findViewById(R.id.progress_bar)
        progressBar.max = 100

        webView = view.findViewById(R.id.web_view)
        webView.settings.javaScriptEnabled = true
        webView.webChromeClient = object: WebChromeClient() {
            override fun onProgressChanged(webView: WebView, newProgress: Int) {
                if(newProgress == 100) {
                    progressBar.visibility = View.GONE
                } else {
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = newProgress
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                (activity as AppCompatActivity).supportActionBar?.subtitle = title
            }
        }
        webView.webViewClient = WebViewClient()
        webView.loadUrl(uri.toString())

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // WebView가 Fragment에서 제거될 때 정리 (메모리 누수 방지)
        webView.apply {
            stopLoading()
            clearHistory()
            removeAllViews()
            destroy()
        }
    }

    companion object {
        fun newInstance(uri: Uri): FlickrPhotoPageFragment {
            return FlickrPhotoPageFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_URI, uri)
                }
            }
        }
    }

}