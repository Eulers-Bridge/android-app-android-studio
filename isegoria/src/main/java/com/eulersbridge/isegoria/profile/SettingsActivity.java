package com.eulersbridge.isegoria.profile;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Photo;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.models.UserSettings;
import com.eulersbridge.isegoria.network.IgnoredCallback;
import com.eulersbridge.isegoria.network.NetworkService;
import com.eulersbridge.isegoria.network.PhotosResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.utilities.Utils;

import java.io.File;

import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private final static int REQ_CODE_PICK_IMAGE = 1;

    private ImageView photoImageView;
    private LinearLayout backgroundLinearLayout;

    private NetworkService network;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.user_settings_fragment);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        Utils.setMultitaskTitle(this, getString(R.string.settings_title));

        findViewById(R.id.back_button).setOnClickListener(view -> onBackPressed());

        Isegoria isegoria = (Isegoria)getApplication();
        network = isegoria.getNetworkService();

        photoImageView = findViewById(R.id.settings_image_small);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(150, 150);
        photoImageView.setLayoutParams(layoutParams);

        User loggedInUser = isegoria.getLoggedInUser();

        final Switch doNotTrackSwitch = findViewById(R.id.doNotTrackSwitch);
        doNotTrackSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isegoria.setTrackingOff(isChecked);

            UserSettings userSettings = new UserSettings(isChecked, loggedInUser.isOptedOutOfDataCollection);

            isegoria.getAPI().updateUserDetails(loggedInUser.email, userSettings).enqueue(new IgnoredCallback<>());
        });

        final Switch optOutDataCollectionSwitch = findViewById(R.id.optOutDataCollectionSwitch);
        optOutDataCollectionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isegoria.setOptedOutOfDataCollection(isChecked);

            UserSettings userSettings = new UserSettings(loggedInUser.trackingOff, isChecked);

            isegoria.getAPI().updateUserDetails(loggedInUser.email, userSettings).enqueue(new IgnoredCallback<>());
        });

        doNotTrackSwitch.setChecked(loggedInUser.isOptedOutOfDataCollection);
        optOutDataCollectionSwitch.setChecked(loggedInUser.trackingOff);

        GlideApp.with(this)
                .load(loggedInUser.profilePhotoURL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(photoImageView);

        ImageView backgroundImageView = findViewById(R.id.settings_image_background);

        isegoria.getAPI().getPhotos(loggedInUser.email).enqueue(new SimpleCallback<PhotosResponse>() {
            @Override
            protected void handleResponse(Response<PhotosResponse> response) {
                PhotosResponse body = response.body();
                if (body != null && body.totalPhotos > 0) {
                    Photo photo = body.photos.get(0);

                    GlideApp.with(SettingsActivity.this)
                            .load(photo.thumbnailUrl)
                            .priority(Priority.HIGH)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(backgroundImageView);
                }
            }
        });

        final TextView aboutThisAppButton = findViewById(R.id.aboutThisAppButton);
        aboutThisAppButton.setOnClickListener(view -> startActivity(new Intent(this, AboutActivity.class)));

        final TextView changePhotoButton = findViewById(R.id.changePhotoButton);
        changePhotoButton.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        });

        // network.s3Auth();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case REQ_CODE_PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(
                            selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();

                    File file = new File(filePath);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), options);
                    photoImageView.setImageBitmap(bitmap);

                    Drawable d = new BitmapDrawable(getResources(), Utils.fastBlur(bitmap, 25));
                    backgroundLinearLayout.setBackground(d);

                    network.s3Upload(file);
                }
        }
    }
}
