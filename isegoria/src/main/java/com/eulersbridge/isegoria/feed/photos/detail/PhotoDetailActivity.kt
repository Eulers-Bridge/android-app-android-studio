package com.eulersbridge.isegoria.feed.photos.detail

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.DrawableRes
import android.support.annotation.Px
import android.support.v4.content.ContextCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Priority
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.feed.photos.ACTIVITY_EXTRA_PHOTOS
import com.eulersbridge.isegoria.feed.photos.ACTIVITY_EXTRA_PHOTOS_POSITION
import com.eulersbridge.isegoria.network.api.models.Photo
import com.eulersbridge.isegoria.observe
import com.eulersbridge.isegoria.toDateString
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.photo_detail_activity.*
import javax.inject.Inject

class PhotoDetailActivity : DaggerAppCompatActivity(), ViewPager.OnPageChangeListener {

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: PhotoDetailViewModel

    private var userLikedCurrentPhoto: Boolean = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.photo_detail_activity)

        viewModel = ViewModelProviders.of(this, modelFactory)[PhotoDetailViewModel::class.java]

        val photosList = intent.getParcelableArrayListExtra<Parcelable>(ACTIVITY_EXTRA_PHOTOS) as? ArrayList<Photo>
        val startIndex = intent.getIntExtra(ACTIVITY_EXTRA_PHOTOS_POSITION, 0)

        photosList?.let { viewModel.setPhotos(it, startIndex) }

        starImageView.setOnClickListener { onStar(it) }

        setupPager(startIndex)

        createViewModelObservers()
    }

    private fun createViewModelObservers() {
        observe(viewModel.currentPhoto) {
            userLikedCurrentPhoto = false

            runOnUiThread {
                if (it != null) {
                    titleTextView.text = it.title

                    val dateStr = it.date.toDateString(this)
                    dateTextView.text = dateStr.toUpperCase()

                    likesTextView.text = it.likeCount.toString()

                    @DrawableRes val flagImage = if (it.hasInappropriateContent) R.drawable.flag else R.drawable.flag_default
                    flagImageView.setImageResource(flagImage)
                }
            }
        }

        observe(viewModel.getPhotoLikeCount()) {
            runOnUiThread { likesTextView.text = it.toString() }
        }

        observe(viewModel.getPhotoLikedByUser()) {
            if (it == true)
                runOnUiThread {
                    userLikedCurrentPhoto = true
                    starImageView.setImageResource(R.drawable.star)
                }
        }
    }

    private fun setupPager(startIndex: Int) {
        val pagerAdapter = object : PagerAdapter() {

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val imageView = SubsamplingScaleImageView(this@PhotoDetailActivity)
                imageView.layoutParams = ViewPager.LayoutParams()

                val photoUrl = viewModel.getPhotoUrl(position)
                if (photoUrl != null) {
                    GlideApp.with(imageView.context)
                        .asBitmap()
                        .load(photoUrl)
                        .priority(Priority.HIGH)
                        .placeholder(R.color.black)
                        .override(Target.SIZE_ORIGINAL)
                        .into(object: SimpleTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                imageView.setImage(ImageSource.bitmap(resource))
                            }
                        })
                }

                container.addView(imageView)

                return imageView
            }

            override fun destroyItem(container: ViewGroup, position: Int, item: Any) =
                container.removeView(item as View)

            override fun getCount() = viewModel.photoCount

            override fun isViewFromObject(view: View, obj: Any) = view === obj
        }

        viewPager.apply {
            adapter = pagerAdapter

            val marginDp = 8
            @Px val marginPx = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                marginDp.toFloat(), resources.displayMetrics))
            pageMargin = marginPx

            addOnPageChangeListener(this@PhotoDetailActivity)
            currentItem = startIndex
        }

        onPageSelected(startIndex)
    }

    /**
     * When a new 'page' is selected, fetch the corresponding photo and populate text fields
     * and like/flag button states.
     */
    override fun onPageSelected(position: Int) = viewModel.changePhoto(position)

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageScrollStateChanged(state: Int) {
        val isUserDragging = state == ViewPager.SCROLL_STATE_DRAGGING
        val durationMillis = (if (isUserDragging) 100 else 250).toLong()
        val alpha = if (isUserDragging) 0.3f else 1.0f

        detailsContainer
            .animate()
            .setDuration(durationMillis)
            .alpha(alpha)
    }

    private fun onStar(view: View) {
        userLikedCurrentPhoto = !userLikedCurrentPhoto

        view.isEnabled = false

        if (userLikedCurrentPhoto) {
            observe(viewModel.likePhoto()) { success ->
                view.isEnabled = true

                if (success == true) {
                    starImageView.setColorFilter(ContextCompat.getColor(this, R.color.star_active))

                    val likes = Integer.parseInt(likesTextView.text.toString()) + 1
                    likesTextView.text = likes.toString()
                }
            }
        } else {
            observe(viewModel.unlikePhoto()) { success ->
                view.isEnabled = true

                if (success == true) {
                    starImageView.colorFilter = null

                    val likes = Integer.parseInt(likesTextView.text.toString()) - 1
                    likesTextView.text = likes.toString()
                }
            }
        }
    }
}