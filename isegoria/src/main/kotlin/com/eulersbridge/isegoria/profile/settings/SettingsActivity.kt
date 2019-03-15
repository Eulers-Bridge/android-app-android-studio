package com.eulersbridge.isegoria.profile.settings

import android.app.Activity
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.util.extension.observe
import com.eulersbridge.isegoria.util.extension.observeBoolean
import com.eulersbridge.isegoria.util.transformation.BlurTransformation
import com.eulersbridge.isegoria.util.transformation.TintTransformation
import com.theartofdev.edmodo.cropper.CropImage
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.settings_activity.*
import javax.inject.Inject

class SettingsActivity : DaggerAppCompatActivity() {

    @Inject
    internal lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.settings_activity)

        window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        viewModel = ViewModelProviders.of(this, modelFactory)[SettingsViewModel::class.java]

        backButton.setOnClickListener { onBackPressed() }

        doNotTrackSwitch.apply {
            isEnabled = false
            setOnCheckedChangeListener { view, isChecked ->
                /* Return to avoid setChecked() inside the callback below triggering the listener again,
                 * entering a loop */
                if (view.isEnabled)
                    viewModel.onTrackingChange(isChecked)
            }
        }

        optOutDataCollectionSwitch.apply {
            isEnabled = false
            setOnCheckedChangeListener { view, isChecked ->
                /* Return to avoid setChecked() inside the callback below triggering the listener again,
                 * entering a loop */
                if (view.isEnabled)
                    viewModel.onOptOutDataCollectionChange(isChecked)
            }
        }

        aboutButton.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        changePhotoButton.setOnClickListener { view ->
            view.isEnabled = false
            showImagePicker()
        }

        createViewModelObservers()
        fetchData()
    }

    private fun createViewModelObservers() {
        // Checked deliberately observed before enabled, to avoid changing switch state
        // whilst being enabled, causing callback to change view model and enter loop.
        observeBoolean(viewModel.doNotTrackSwitchChecked) {
            doNotTrackSwitch.isChecked = it
        }

        observeBoolean(viewModel.doNotTrackSwitchEnabled) {
            doNotTrackSwitch.isEnabled = it
        }

        observeBoolean(viewModel.optOutDataCollectionSwitchChecked) {
            optOutDataCollectionSwitch.isChecked = it
        }

        observeBoolean(viewModel.optOutDataCollectionSwitchEnabled) {
            optOutDataCollectionSwitch.isEnabled = it
        }
    }


    private fun fetchData() {
        viewModel.profilePhotoUrl?.let {
            GlideApp.with(this)
                    .load(it)
                    .transform(CircleCrop())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(smallImageView)
        }

        observe(viewModel.userPhoto) {
            if (it != null)
                GlideApp.with(this)
                    .load(it.getPhotoUrl())
                    .transforms(BlurTransformation(this), TintTransformation(0.1))
                    .priority(Priority.HIGH)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(backgroundImageView)
        }
    }

    private fun showImagePicker() {
        CropImage.activity()
            .setAspectRatio(1, 1) // Force a square aspect ratio
            .setBackgroundColor(Color.BLACK)
            .setActivityTitle(getString(R.string.image_crop_title))
            .setMinCropResultSize(150, 150) // Minimum size (pixels)
            .setMaxCropResultSize(1280, 1280) // Maximum size (pixels)
            .setMinCropWindowSize(128, 128) //Dimensions (dp)
            .setOutputCompressQuality(60)
            .start(this)
    }

    private fun updateUIWithImage(imageUri: Uri?) {
        if (imageUri == null) return

        GlideApp.with(this)
            .load(imageUri)
            .priority(Priority.HIGH)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(smallImageView)

        GlideApp.with(this)
            .load(imageUri)
            .transforms(BlurTransformation(this), TintTransformation(0.1))
            .placeholder(R.color.profileImageBackground)
            .priority(Priority.HIGH)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(backgroundImageView)

        observe(viewModel.updateUserPhoto(imageUri)) {
            changePhotoButton.isEnabled = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)

            when (resultCode) {
                Activity.RESULT_OK -> updateUIWithImage(result.uri)
                Activity.RESULT_CANCELED -> changePhotoButton.isEnabled = true
                CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                    changePhotoButton.isEnabled = true
                    result.error.printStackTrace()
                }
            }
        }
    }
}
