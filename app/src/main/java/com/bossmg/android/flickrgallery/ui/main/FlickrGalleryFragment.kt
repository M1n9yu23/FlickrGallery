package com.bossmg.android.flickrgallery.ui.main

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.bossmg.android.flickrgallery.viewmodel.FlickrGalleryViewModel
import com.bossmg.android.flickrgallery.ui.detail.FlickrPhotoPageActivity
import com.bossmg.android.flickrgallery.worker.PollWorker
import com.bossmg.android.flickrgallery.R
import com.bossmg.android.flickrgallery.util.VisibleFragment
import com.bossmg.android.flickrgallery.data.GalleryItem
import com.bossmg.android.flickrgallery.data.QueryPreferences
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.init
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import java.util.concurrent.TimeUnit
import com.bumptech.glide.request.target.Target


private const val TAG = "FlickrGalleryFragment"
private const val POLL_WORK = "POLL_WORK"

// Flickr에서 가져온 사진 목록을 표시하는 프래그먼트
// RecyclerView를 사용하여 사진을 그리드 형태로 보여줌
class FlickrGalleryFragment : VisibleFragment() {
    private lateinit var photoRecyclerView: RecyclerView
    private lateinit var flickrGalleryViewModel: FlickrGalleryViewModel

    /**
     * 프래그먼트의 뷰를 생성하는 메서드
     * @param inflater: XML 레이아웃을 View 객체로 변환하는 객체
     * @param container: 부모 컨테이너 뷰
     * @param savedInstanceState: 이전 저장된 상태 (null 가능)
     * @return 생성된 View 객체 반환
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 프래그먼트의 UI 레이아웃을 inflate하여 View 생성
        val view = inflater.inflate(R.layout.fragment_flickr_gallery, container, false)

        // UI 요소 초기화
        photoRecyclerView = view.findViewById(R.id.photo_recycler_view)

        photoRecyclerView.layoutManager = LinearLayoutManager(context)
        return view
    }

    /**
     * 프래그먼트의 뷰가 생성된 후 실행되는 메서드
     * RecyclerView 어댑터 설정 및 데이터 변경 감지
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel에서 데이터를 가져와 RecyclerView의 어댑터를 업데이트
        flickrGalleryViewModel.galleryItemLiveData.observe(
            viewLifecycleOwner
        ) { galleryItems ->
            photoRecyclerView.adapter = PhotoAdapter(galleryItems)
        }

        // ViewModel에서 검색어 상태를 가져와 제목을 설정
        flickrGalleryViewModel.galleryItemLiveData.observe(
            viewLifecycleOwner
        ) {
            val newTitle = if (flickrGalleryViewModel.searchTerm.isBlank()) {
                "최근 사진"
            } else {
                flickrGalleryViewModel.searchTerm // 검색어를 앱바 제목으로 설정
            }

            // 툴바(앱바)의 제목을 변경
            (activity as AppCompatActivity).supportActionBar?.title = newTitle
        }

        // onCreateOptionsMenu() & onOptionsItemSelected() 제거 후 MenuProvider 사용
        // 메뉴 설정을 위해 MenuProvider 사용 (AndroidX 최신 방식)
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_photo_gallery, menu)

                // 검색 기능 추가
                val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
                val searchView = searchItem.actionView as SearchView

                searchView.apply {
                    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String): Boolean {
                            Log.d(TAG, "검색어 입력됨: $query")
                            flickrGalleryViewModel.fetchPhotos(query)
                            return true
                        }

                        override fun onQueryTextChange(newText: String): Boolean {
                            Log.d(TAG, "검색어 변경됨: $newText")
                            return false
                        }
                    })
                }

                // toggleItem을 다시 추가하여 Polling 상태를 반영하도록 수정
                val toggleItem = menu.findItem(R.id.menu_item_toggle_polling)
                val isPolling = QueryPreferences.isPolling(requireContext())
                val toggleItemTitle = if (isPolling) {
                    R.string.stop_polling
                } else {
                    R.string.start_polling
                }
                toggleItem.setTitle(toggleItemTitle) // 메뉴 항목의 제목을 현재 Polling 상태에 맞게 설정

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_item_tag -> {
                        showTagSelectionDialog()
                        true
                    }
                    R.id.menu_item_clear -> {
                        flickrGalleryViewModel.fetchPhotos("")
                        true
                    }
                    /**
                     * Polling(백그라운드 업데이트) 활성화/비활성화 기능
                     */
                    R.id.menu_item_toggle_polling -> {
                        val workManager = WorkManager.getInstance(requireContext())
                        val isPolling = QueryPreferences.isPolling(requireContext())

                        if (isPolling) {
                            workManager.cancelUniqueWork(POLL_WORK)
                            QueryPreferences.setPolling(requireContext(), false)
                        } else {
                            val constraints = Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.UNMETERED)
                                .build()

                            val periodicRequest = PeriodicWorkRequest.Builder(PollWorker::class.java, 15, TimeUnit.MINUTES)
                                .setConstraints(constraints)
                                .build()

                            workManager.enqueueUniquePeriodicWork(
                                POLL_WORK,
                                ExistingPeriodicWorkPolicy.KEEP,
                                periodicRequest
                            )
                            QueryPreferences.setPolling(requireContext(), true)
                        }
                        requireActivity().invalidateOptionsMenu()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED) // 최신 API 대응을 위해 Lifecycle.State.RESUMED 추가

    }

    /**
     * 사진 RecyclerView의 개별 아이템을 관리하는 ViewHolder
     */
    private inner class PhotoHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener{
        private lateinit var galleryItem: GalleryItem

        private val imageView: ImageView = itemView.findViewById(R.id.item_image_view)
        private val ownerTextView: TextView = itemView.findViewById(R.id.item_owner_text_view)
        private val dateTextView: TextView = itemView.findViewById(R.id.item_date_text_view)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val tagTextView: TextView = itemView.findViewById(R.id.item_tags_text_view)

        init {
            itemView.setOnClickListener(this)

            // 이미지 길게 누르면 공유 기능 추가
            itemView.setOnLongClickListener {
                shareImage(galleryItem.url)
                true // true를 반환해야 롱 클릭 이벤트가 소비됨
            }
        }

        @Deprecated("사용 X")
        val bindDrawable: (Drawable) -> Unit = { drawable ->
            progressBar.visibility = View.GONE // 이미지 로딩 후 ProgressBar 숨기기
            imageView.setImageDrawable(drawable)
        }

        fun bind(galleryItem: GalleryItem) {
            this.galleryItem = galleryItem
            ownerTextView.text = "작성자: ${galleryItem.ownerName}"
            dateTextView.text = "업로드 날짜: ${convertTimestampToDate(galleryItem.dateUpload)}"
            tagTextView.text = if (galleryItem.tags.isNotEmpty()) {
                "태그: ${galleryItem.tags}"
            } else {
                "태그 없음"
            }

            // ProgressBar 보이기 (로딩 시작)
            progressBar.visibility = View.VISIBLE

            Glide.with(itemView.context)
                .load(galleryItem.url) // 가져올 이미지
                .listener(object : RequestListener<Drawable> { // 이미지 로딩 상태를 감지하고 특정 작업을 수행
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE // 로딩 실패 시 ProgressBar 숨기기
                        return false // false를 반환해야 계속 실행됨.
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE // 이미지 로딩 완료 후 ProgressBar 숨김
                        return false
                    }
                })
                .into(imageView) // 이미지를 보여줄 뷰
        }

        private fun convertTimestampToDate(timestamp: String): String {
            return try {
                val timeInMillis = timestamp.toLong() * 1000
                val date = java.util.Date(timeInMillis)
                val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                format.format(date)
            } catch (e: Exception) {
                "날짜 정보 없음"
            }
        }

        override fun onClick(p0: View?) {
            val intent =
                FlickrPhotoPageActivity.newIntent(requireContext(), galleryItem.photoPageUri)
            startActivity(intent)
        }

        // 공유 기능
        private fun shareImage(imageUrl: String) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Check out this photo: $imageUrl")
                type = "text/plain"
            }
            val chooser = Intent.createChooser(shareIntent, "Share Image")
            itemView.context.startActivity(chooser)
        }
    }

    /**
     * RecyclerView의 Adapter 클래스 (사진 목록을 관리)
     */
    private inner class PhotoAdapter(private val galleryItems: List<GalleryItem>): RecyclerView.Adapter<PhotoHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            val view = layoutInflater.inflate(
                R.layout.list_item_gallery,
                parent,
                false
            )
            return PhotoHolder(view)
        }

        override fun getItemCount(): Int = galleryItems.size

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val galleryItem = galleryItems[position]
            holder.bind(galleryItem)

        }

    }

    private fun showTagSelectionDialog() {
        val tags = arrayOf("Featured","Abstract & Art",
            "Animal & Wildlife", "Food & Drink", "Home & Garden", "Location",
            "Music & Performance", "Nature & Scenery", "Sports & Action",
            "Travel & Adventure", "Vehicles & Transportation")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Tag")
            .setItems(tags) { _, which ->
                val selectedTag = tags[which]
                fetchPhotosByTag(selectedTag)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun fetchPhotosByTag(tag: String) {
        flickrGalleryViewModel.fetchPhotosTag(tag)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        flickrGalleryViewModel = ViewModelProvider(this).get(FlickrGalleryViewModel::class.java)

    }

    companion object {
        fun newInstance() = FlickrGalleryFragment()
    }
}