package com.eulersbridge.isegoria.feed;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.Network;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.utilities.TimeConverter;
import com.eulersbridge.isegoria.utilities.Utils;

public class NewsArticleFragment extends Fragment {
    private View rootView;
    private View newsArticleDivider;
    private ImageView newsArticleAuthorImage;
    private int articleId;

    private boolean setLiked = false;
    private Network network;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.news_article_fragment, container, false);

        Isegoria isegoria = (Isegoria) getActivity().getApplication();
        Bundle bundle = this.getArguments();

        isegoria.getNetwork().getNewsArticle(this, bundle.getInt("ArticleId"));
        articleId = bundle.getInt("ArticleId");

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getNewsArticleLiked(this);

        newsArticleAuthorImage = rootView.findViewById(R.id.newsArticleAuthorImage);

        return rootView;
    }

    public void initiallyLiked() {
        final ImageView starView = rootView.findViewById(R.id.starView);
        starView.setImageResource(R.drawable.star);
        setLiked = true;
    }

    public boolean isSetLiked() {
        return setLiked;
    }

    public int getArticleId() {
        return articleId;
    }

    public void setArticleId(int articleId) {
        this.articleId = articleId;
    }

    @SuppressWarnings("deprecation")
    public void populateContent(final String title, final String content, final String likes, final long date, @Nullable final Bitmap picture, final String email, final boolean inappropriateContent) {
        try {
            getActivity().runOnUiThread(() -> {
                DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();

                int paddingMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        (float) 83.33333333, getResources().getDisplayMetrics());
                int paddingMargin2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        (float) 23.33333333, getResources().getDisplayMetrics());

                int imageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        (float) 250, getResources().getDisplayMetrics());

                LinearLayout backgroundLinearLayout = rootView.findViewById(R.id.topBackgroundNews);
                backgroundLinearLayout.getLayoutParams().height = imageHeight;
                if (picture != null) {
                    Bitmap tintedPicture = Utils.tintBitmap(picture, Color.argb(128, 0, 0, 0));
                    Drawable d = new BitmapDrawable(getActivity().getResources(), tintedPicture);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        backgroundLinearLayout.setBackground(d);
                    } else {
                        backgroundLinearLayout.setBackgroundDrawable(d);
                    }
                }

                TextView newsTitle = rootView.findViewById(R.id.newsArticleTitle);
                newsTitle.setText(title);

                final TextView newsArticleLikesView = rootView.findViewById(R.id.newsArticleLikes);
                newsArticleLikesView.setText(likes);

                TextView newsText = rootView.findViewById(R.id.textNews);
                newsText.setText(content);

                TextView newsArticleDate = rootView.findViewById(R.id.newsArticleDate);
                newsArticleDate.setText(TimeConverter.convertTimestampToString(date));

                final ImageView flagView = rootView.findViewById(R.id.flagView);
                flagView.setOnClickListener(view -> flagView.setImageResource(R.drawable.flag));

                if(inappropriateContent) {
                    flagView.setImageResource(R.drawable.flagdefault);
                }

                final ImageView starView = rootView.findViewById(R.id.starView);
                starView.setOnClickListener(view -> {
                    if(!setLiked) {
                        setLiked = true;
                        starView.setImageResource(R.drawable.star);
                        network.likeArticle(articleId, NewsArticleFragment.this);
                        int likes1 = Integer.parseInt(String.valueOf(newsArticleLikesView.getText()));
                        likes1 = likes1 + 1;
                        newsArticleLikesView.setText(String.valueOf(likes1));
                    }
                    else {
                        setLiked = false;
                        starView.setImageResource(R.drawable.stardefault);
                        network.unlikeArticle(articleId, NewsArticleFragment.this);
                        int likes1 = Integer.parseInt(String.valueOf(newsArticleLikesView.getText()));
                        likes1 = likes1 - 1;
                        newsArticleLikesView.setText(String.valueOf(likes1));
                    }
                });

                //ImageView headShotImageView = (ImageView) rootView.findViewById(R.id.newsArticleHeadView);
                //network.getFirstPhotoImage(email, headShotImageView);

                newsArticleAuthorImage.setVisibility(ViewGroup.VISIBLE);
            });
        } catch(Exception ignored) {

        }
    }

    public void populateUserContent(final String name, final String photoURL) {
        try {
            getActivity().runOnUiThread(() -> {
                TextView newsArticleName = rootView.findViewById(R.id.photoTitle);
                newsArticleName.setText(name);
            });
        } catch(Exception ignored) {

        }

        ImageView newsArticleAuthorImage = rootView.findViewById(R.id.newsArticleAuthorImage);
        network.getPicture(photoURL, new Network.PictureDownloadListener() {
            @Override
            public void onDownloadFinished(String url, @Nullable Bitmap bitmap) {
                Bitmap tintedBitmap = Utils.tintBitmap(bitmap, Color.argb(128, 0, 0, 0));
                newsArticleAuthorImage.setImageBitmap(tintedBitmap);
            }

            @Override
            public void onDownloadFailed(String url, VolleyError error) {}
        });
    }
}