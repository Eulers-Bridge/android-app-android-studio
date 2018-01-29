package com.eulersbridge.isegoria.profile.settings

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.util.transformation.BlurTransformation
import com.eulersbridge.isegoria.util.transformation.TintTransformation
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.settings_activity.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.settings_activity)

        window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        viewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)

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
            startActivity(
                Intent(this, AboutActivity::class.java)
            )
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
        viewModel.doNotTrackSwitchChecked.observe(this, observeBoolean { checked ->
            doNotTrackSwitch.isChecked = checked
        })

        viewModel.doNotTrackSwitchChecked.observe(this, observeBoolean { checked ->
            doNotTrackSwitch.isChecked = checked == true
        })

        viewModel.doNotTrackSwitchEnabled.observe(this, observeBoolean { enabled ->
            doNotTrackSwitch.isEnabled = enabled == true
        })

        viewModel.optOutDataCollectionSwitchChecked.observe(this, observeBoolean { checked ->
            optOutDataCollectionSwitch.isChecked = checked == true
        })

        viewModel.optOutDataCollectionSwitchEnabled.observe(this, observeBoolean { enabled ->
            optOutDataCollectionSwitch.isEnabled = enabled == true
        })
    }

    /**
     * Convenience function to map an optional boolean to a non-null boolean value
     */
    private inline fun observeBoolean(crossinline f: (value: Boolean) -> Unit) : Observer<Boolean?>
            = Observer { f(it == true) }

    private fun fetchData() {
        viewModel.userProfilePhotoURL.observe(this, Observer {
            if (!it.isNullOrBlank())
                GlideApp.with(this)
                    .load(it)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(smallImageView)
        })

        viewModel.getUserPhoto()?.observe(this, Observer {
            if (it != null)
                GlideApp.with(this)
                    .load(it.thumbnailUrl)
                    .transforms(BlurTransformation(this), TintTransformation(0.1))
                    .priority(Priority.HIGH)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(backgroundImageView)
        })
    }

    private fun showImagePicker() {
        CropImage.activity()
            .setAspectRatio(1, 1) // Force a square aspect ratio
            .setBackgroundColor(Color.BLACK)
            .setActivityTitle(getString(R.string.image_crop_title))

            // Minimum size (pixels)
            .setMinCropResultSize(150, 150)

            // Maximum size (pixels)
            .setMaxCropResultSize(1280, 1280)

            //Dimensions (dp)
            .setMinCropWindowSize(128, 128)

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

        viewModel.updateUserPhoto(imageUri).observe(this, Observer { success ->
            if (success != null)
                changePhotoButton.isEnabled = true
        })
    }

    public override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        returnedIntent: Intent
    ) {
        super.onActivityResult(requestCode, resultCode, returnedIntent)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(returnedIntent)

            if (resultCode == Activity.RESULT_OK) {
                val imageUri = result.uri
                updateUIWithImage(imageUri)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                error.printStackTrace()
            }
        }
    }
}
