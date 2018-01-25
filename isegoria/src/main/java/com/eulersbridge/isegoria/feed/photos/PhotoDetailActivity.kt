package com.eulersbridge.isegoria.feed.photos

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Bitmap
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
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
import com.eulersbridge.isegoria.network.api.models.Photo
import com.eulersbridge.isegoria.util.Constants
import com.eulersbridge.isegoria.util.Strings
import kotlinx.android.synthetic.main.photo_detail_activity.*
import org.parceler.Parcels
import java.util.*

class PhotoDetailActivity : AppCompatActivity(), ViewPager.OnPageChangeListener {

    private var userLikedCurrentPhoto: Boolean = false
    private lateinit var viewModel: PhotoDetailViewModel

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.photo_detail_activity)

        viewModel = ViewModelProviders.of(this).get(PhotoDetailViewModel::class.java)
        setupModelObservers()

        val photos = Parcels.unwrap<ArrayList<Photo>>(intent.getParcelableExtra(Constants.ACTIVITY_EXTRA_PHOTOS))
        val startIndex = intent.getIntExtra(Constants.ACTIVITY_EXTRA_PHOTOS_POSITION, 0)

        viewModel.setPhotos(photos, startIndex)

        starImageView.setOnClickListener { view ->
            userLikedCurrentPhoto = !userLikedCurrentPhoto

            view.isEnabled = false

            if (userLikedCurrentPhoto) {
                viewModel.likePhoto().observe(this, Observer { success ->
                    view.isEnabled = true

                    if (success == true) {
                        starImageView.setColorFilter(ContextCompat.getColor(this, R.color.star_active))

                        val likes = Integer.parseInt(likesTextView.text.toString()) + 1
                        likesTextView.text = likes.toString()
                    }
                })
            } else {
                viewModel.unlikePhoto().observe(this, Observer { success ->
                    view.isEnabled = true

                    if (success == true) {
                        starImageView.colorFilter = null

                        val likes = Integer.parseInt(likesTextView.text.toString()) - 1
                        likesTextView.text = likes.toString()
                    }
                })
            }
        }

        setupPager(startIndex)
    }

    private fun setupModelObservers() {
        viewModel.currentPhoto.observe(this, Observer { photo ->
            userLikedCurrentPhoto = false

            runOnUiThread {
                if (photo != null) {
                    titleTextView.text = photo.title

                    val dateStr = Strings.fromTimestamp(this, photo.dateTimestamp)
                    dateTextView.text = dateStr.toUpperCase()

                    likesTextView.text = photo.likeCount.toString()

                    @DrawableRes val flagImage = if (photo.hasInappropriateContent) R.drawable.flag else R.drawable.flagdefault
                    flagImageView.setImageResource(flagImage)
                }
            }
        })

        viewModel.photoLikes.observe(this, Observer { likes ->
            if (likes != null)
                runOnUiThread { likesTextView.text = likes.size.toString() }
        })

        viewModel.photoLikedByUser.observe(this, Observer { likedByUser ->
            if (likedByUser == true) {
                runOnUiThread {
                    userLikedCurrentPhoto = true
                    starImageView.setImageResource(R.drawable.star)
                }
            }
        })
    }

    private fun setupPager(startIndex: Int) {
        val pagerAdapter = object : PagerAdapter() {

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val photo = viewModel.currentPhoto.value

                val context = this@PhotoDetailActivity

                val imageView = SubsamplingScaleImageView(context)

                val layoutParams = ViewPager.LayoutParams()
                imageView.layoutParams = layoutParams

                if (photo != null) {
                    GlideApp.with(context)
                            .asBitmap()
                            .load(photo.thumbnailUrl)
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

        viewPager.adapter = pagerAdapter

        val marginDp = 8
        val marginPixels = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginDp.toFloat(), resources.displayMetrics))
        viewPager.pageMargin = marginPixels

        viewPager.addOnPageChangeListener(this)

        viewPager.currentItem = startIndex
        onPageSelected(startIndex)
    }

    /**
     * When a new 'page' is selected, fetch the corresponding photo and populate text fields
     * and like/flag button states.
     */
    override fun onPageSelected(position: Int) {
        viewModel.changePhoto(position)
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageScrollStateChanged(state: Int) {

        val dragging = state == ViewPager.SCROLL_STATE_DRAGGING
        val durationMillis = (if (dragging) 100 else 250).toLong()
        val alpha = if (dragging) 0.3f else 1.0f

        detailsContainer.animate().setDuration(durationMillis).alpha(alpha)
    }
}