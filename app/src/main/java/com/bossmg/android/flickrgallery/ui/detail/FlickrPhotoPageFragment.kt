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

// 웹페이지에서 Flickr 사진을 표시하는 프래그먼트
// 사용자가 선택한 사진의 웹페이지를 WebView를 통해 보여줌
class FlickrPhotoPageFragment: VisibleFragment() {

    // 사진 웹페이지 URL을 저장하는 Uri 객체
    private lateinit var uri: Uri

    // 웹페이지를 로드할 WebView
    private lateinit var webView: WebView

    // 웹페이지 로딩 진행률을 표시하는 ProgressBar
    private lateinit var progressBar: ProgressBar

    /**
     * 프래그먼트가 생성될 때 호출되는 메서드 (초기 데이터 설정)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // API 33 (Android 13, Tiramisu) 이상에서는 getParcelable()의 두 번째 매개변수로 클래스를 전달해야 함
        uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_URI, Uri::class.java) ?: Uri.EMPTY
        } else {
            @Suppress("DEPRECATION") // API 32 이하
            arguments?.getParcelable(ARG_URI) ?: Uri.EMPTY
        }
    }

    /**
     * 프래그먼트의 UI를 생성하는 함수
     * @param inflater 레이아웃 XML을 View 객체로 변환하는 LayoutInflater
     * @param container 프래그먼트의 부모 뷰 (null 가능)
     * @param savedInstanceState 이전 상태 저장 (null 가능)
     * @return 생성된 View 객체 반환
     */
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 프래그먼트의 레이아웃을 inflate하여 View 생성
        val view = inflater.inflate(R.layout.fragment_flickr_photo_page, container, false)

        // ProgressBar 초기화 및 최대 값 설정 (100%)
        progressBar = view.findViewById(R.id.progress_bar)
        progressBar.max = 100

        // WebView 초기화 및 설정
        webView = view.findViewById(R.id.web_view)
        webView.settings.javaScriptEnabled = true // JavaScript 활성화

        // WebView의 WebChromeClient 설정 (웹페이지 로딩 진행률 및 제목 처리)
        webView.webChromeClient = object: WebChromeClient() {
            override fun onProgressChanged(webView: WebView, newProgress: Int) {
                if(newProgress == 100) {
                    // 로딩 완료 시 ProgressBar 숨김
                    progressBar.visibility = View.GONE
                } else {
                    // 로딩 중에는 ProgressBar 표시 및 진행률 업데이트
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = newProgress
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                // 웹페이지 제목을 액션바의 서브타이틀로 설정
                (activity as AppCompatActivity).supportActionBar?.subtitle = title
            }
        }

        // WebViewClient 설정 (새 창이 열리지 않고 WebView 내에서 로딩되도록 함)
        webView.webViewClient = WebViewClient()

        // 전달받은 URI를 사용하여 웹페이지 로드
        webView.loadUrl(uri.toString())

        return view
    }

    /**
     * 프래그먼트가 제거될 때 호출되는 함수 (메모리 누수 방지)
     */
    override fun onDestroyView() {
        super.onDestroyView()
        // WebView가 Fragment에서 제거될 때 정리 (메모리 누수 방지)
        webView.apply {
            stopLoading() // 현재 로딩 중인 페이지 중지
            clearHistory() // 방문 기록 삭제
            removeAllViews() // 모든 자식 뷰 제거
            destroy() // WebView 자체 제거
        }
    }

    companion object {

        /**
         * FlickrPhotoPageFragment의 인스턴스를 생성하는 함수
         * @param uri Flickr 사진의 웹페이지 URL
         * @return FlickrPhotoPageFragment 인스턴스
         */
        fun newInstance(uri: Uri): FlickrPhotoPageFragment {
            return FlickrPhotoPageFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_URI, uri) // 프래그먼트에 URI 전달
                }
            }
        }
    }

}