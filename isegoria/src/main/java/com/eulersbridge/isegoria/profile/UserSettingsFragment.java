package com.eulersbridge.isegoria.profile;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.models.Photo;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.models.UserSettings;
import com.eulersbridge.isegoria.network.IgnoredCallback;
import com.eulersbridge.isegoria.network.NetworkService;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.PhotosResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.utilities.Utils;

import java.io.File;

import retrofit2.Response;

public class UserSettingsFragment extends Fragment {
    private static final int PICK_IMAGE = 1;
    private final static int REQ_CODE_PICK_IMAGE = 1;

    private ImageView photoImageView;
    private LinearLayout backgroundLinearLayout;

    private MainActivity mainActivity;
    private NetworkService network;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.user_settings_fragment, container, false);

        // Ensure options menu from another fragment is not carried over
        getActivity().invalidateOptionsMenu();

        mainActivity = (MainActivity) getActivity();
        mainActivity.setToolbarTitle(getString(R.string.section_title_settings));

        Isegoria isegoria = (Isegoria)getActivity().getApplication();

        User loggedInUser = ((Isegoria)getActivity().getApplication()).getLoggedInUser();

        network = isegoria.getNetworkService();

        photoImageView = rootView.findViewById(R.id.settings_image_small);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(150, 150);
        photoImageView.setLayoutParams(layoutParams);

        final Switch doNotTrackSwitch = rootView.findViewById(R.id.doNotTrackSwitch);
        doNotTrackSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isegoria.setTrackingOff(isChecked);

            UserSettings userSettings = new UserSettings(isChecked, loggedInUser.isOptedOutOfDataCollection);

            isegoria.getAPI().updateUserDetails(loggedInUser.email, userSettings).enqueue(new IgnoredCallback<>());
        });

        final Switch optOutDataCollectionSwitch = rootView.findViewById(R.id.optOutDataCollectionSwitch);
        optOutDataCollectionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isegoria.setOptedOutOfDataCollection(isChecked);

            UserSettings userSettings = new UserSettings(loggedInUser.trackingOff, isChecked);

            isegoria.getAPI().updateUserDetails(loggedInUser.email, userSettings).enqueue(new IgnoredCallback<>());
        });

        doNotTrackSwitch.setChecked(loggedInUser.isOptedOutOfDataCollection);
        optOutDataCollectionSwitch.setChecked(loggedInUser.trackingOff);

        if (!TextUtils.isEmpty(loggedInUser.profilePhotoURL)) {
            GlideApp.with(this)
                    .load(loggedInUser.profilePhotoURL)
                    .into(photoImageView);
        }

        ImageView backgroundImageView = rootView.findViewById(R.id.settings_image_background);

        isegoria.getAPI().getPhotos(loggedInUser.email).enqueue(new SimpleCallback<PhotosResponse>() {
            @Override
            protected void handleResponse(Response<PhotosResponse> response) {
                PhotosResponse body = response.body();
                if (body != null && body.totalPhotos > 0) {
                    Photo photo = body.photos.get(0);

                    GlideApp.with(UserSettingsFragment.this)
                            .load(photo.thumbnailUrl)
                            .into(backgroundImageView);
                }
            }
        });

        final TextView aboutThisAppButton = rootView.findViewById(R.id.aboutThisAppButton);
        aboutThisAppButton.setOnClickListener(view -> {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

            fragmentManager
                    .beginTransaction()
                    .add(R.id.container, new AboutScreenFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();
        });

        final TextView changePhotoButton = rootView.findViewById(R.id.changePhotoButton);
        changePhotoButton.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        });

       // network.s3Auth();

		return rootView;
	}

    public void setTabLayout(TabLayout tabLayout) {
        tabLayout.setVisibility(View.GONE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case REQ_CODE_PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getActivity().getContentResolver().query(
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

                    Drawable d = new BitmapDrawable(mainActivity.getResources(),
                            Utils.fastBlur(bitmap, 25));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        backgroundLinearLayout.setBackground(d);
                    } else {
                        backgroundLinearLayout.setBackgroundDrawable(d);
                    }

                    network.s3Upload(file);
                }
        }
    }
}