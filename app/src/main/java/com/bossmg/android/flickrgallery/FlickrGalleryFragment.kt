package com.bossmg.android.flickrgallery

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
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import java.util.concurrent.TimeUnit
import com.bumptech.glide.request.target.Target


private const val TAG = "FlickrGalleryFragment"
private const val POLL_WORK = "POLL_WORK"

class FlickrGalleryFragment : VisibleFragment() {
    private lateinit var photoRecyclerView: RecyclerView
    private lateinit var flickrGalleryViewModel: FlickrGalleryViewModel
    private lateinit var titleTextView: TextView
//    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Fragment의 레이아웃을 인플레이트하고 뷰를 반환
        val view = inflater.inflate(R.layout.fragment_flickr_gallery, container, false)

        titleTextView = view.findViewById(R.id.title_text_view)
        photoRecyclerView = view.findViewById(R.id.photo_recycler_view)
        photoRecyclerView.layoutManager = GridLayoutManager(context, 3) // 3개의 열을 가진 GridLayout 사용

        // ThumbnailDownloader의 생명주기를 관리하도록 옵저버 추가
//        viewLifecycleOwner.lifecycle.addObserver(thumbnailDownloader.viewLifecycleObserver)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel에서 데이터를 가져와 RecyclerView의 어댑터를 업데이트
        flickrGalleryViewModel.galleryItemLiveData.observe(
            viewLifecycleOwner,
            Observer { galleryItems ->
                photoRecyclerView.adapter = PhotoAdapter(galleryItems)
            })

        flickrGalleryViewModel.galleryItemLiveData.observe(
            viewLifecycleOwner,
            Observer { items ->
                titleTextView.text = if (flickrGalleryViewModel.searchTerm.isBlank()) "최근 사진"
                else flickrGalleryViewModel.searchTerm
            })

        // onCreateOptionsMenu() & onOptionsItemSelected() 제거 후 MenuProvider 사용
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_photo_gallery, menu)

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

//                    검색 아이콘을 다시 눌렀을 때, 이전 검색어를 유지하도록 설정
//                    setOnSearchClickListener {
//                        searchView.setQuery(flickrGalleryViewModel.searchTerm, false)
//                    }

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
                    R.id.menu_item_clear -> {
                        flickrGalleryViewModel.fetchPhotos("")
                        true
                    }
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

    private inner class PhotoHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener{
        private lateinit var galleryItem: GalleryItem

        val imageView: ImageView = itemView.findViewById(R.id.item_image_view)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

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

        fun bindGalleryItem(item: GalleryItem) {
            galleryItem = item
        }

        override fun onClick(p0: View?) {
            val intent = FlickrPhotoPageActivity.newIntent(requireContext(), galleryItem.photoPageUri)
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
            holder.bindGalleryItem(galleryItem)

            // ProgressBar 보이기 (로딩 시작)
            holder.progressBar.visibility = View.VISIBLE

            Glide.with(holder.itemView.context)
                .load(galleryItem.url) // 가져올 이미지
                .error(android.R.drawable.stat_notify_error) // 에러 이미지
                .listener(object : RequestListener<Drawable> { // 이미지 로딩 상태를 감지하고 특정 작업을 수행
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.progressBar.visibility = View.GONE // 로딩 실패 시 ProgressBar 숨기기
                        return false // false를 반환해야 계속 실행됨.
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.progressBar.visibility = View.GONE // 이미지 로딩 완료 후 ProgressBar 숨김
                        return false // false를 반환해야 계속 실행됨.
                    }
                })
                .into(holder.imageView) // 이미지를 보여줄 뷰

//            thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // retainInstance = true 이렇게 유보 하는건 피해야함. FragmentManager가 자동으로 Fragment 상태를 복원하므로 불필요함.
        // setHasOptionsMenu(true) MenuProvider를 사용하므로 불필요

        flickrGalleryViewModel = ViewModelProvider(this).get(FlickrGalleryViewModel::class.java)

        // Handler 생성 방식을 최신 방식으로 변경 (deprecated된 기본 생성자 제거)
        /*
        val responseHandler = Handler(Looper.getMainLooper())
        thumbnailDownloader = ThumbnailDownloader(responseHandler) { photoHandler, bitmap ->
            val drawable = BitmapDrawable(resources, bitmap)
            photoHandler.bindDrawable(drawable)
        }

        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver) // 프래그먼트 생명주기와 함께 옵저버 등록
        */
    }

    override fun onDestroy() {
        super.onDestroy()
//      lifecycle.removeObserver(thumbnailDownloader.fragmentLifecycleObserver) // 리소스 정리
    }

    /**
     * @deprecated - 사용 안함.
     *
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_photo_gallery, menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView

        searchView.apply {
            setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    Log.d(TAG, "QueryTextSubmit: $query")
                    flickrGalleryViewModel.fetchPhotos(query)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    Log.d(TAG, "QueryTextChange: $newText")
                    return false
                }
            })
            setOnSearchClickListener {
                searchView.setQuery(flickrGalleryViewModel.searchTerm, false)
            }
        }

        val toggleItem= menu.findItem(R.id.menu_item_toggle_polling)
        val isPolling = QueryPreferences.isPolling(requireContext())
        val toggleItemTitle = if (isPolling) {
            R.string.stop_polling
        } else {
            R.string.start_polling
        }
        toggleItem.setTitle(toggleItemTitle)
    }
    */

    /**
     * @deprecated - 사용 안함
     *
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_item_clear -> {
                flickrGalleryViewModel.fetchPhotos("")
                true
            }
            R.id.menu_item_toggle_polling -> {
                val workManager = WorkManager.getInstance(requireContext())
                val isPolling = QueryPreferences.isPolling(requireContext())
                if(isPolling){
                    workManager.cancelUniqueWork(POLL_WORK)
                    QueryPreferences.setPolling(requireContext(), false)
                } else {
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .build()
                    val periodicRequest = PeriodicWorkRequest
                        .Builder(PollWorker::class.java, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build()
                    workManager.enqueueUniquePeriodicWork(
                        POLL_WORK,
                        ExistingPeriodicWorkPolicy.KEEP,
                        periodicRequest
                    )
                    QueryPreferences.setPolling(requireContext(), true)
                }
                activity?.invalidateOptionsMenu()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    */

    override fun onDestroyView() {
        super.onDestroyView()
//      viewLifecycleOwner.lifecycle.removeObserver(thumbnailDownloader.viewLifecycleObserver)
    }

    companion object {
        fun newInstance() = FlickrGalleryFragment()
    }
}