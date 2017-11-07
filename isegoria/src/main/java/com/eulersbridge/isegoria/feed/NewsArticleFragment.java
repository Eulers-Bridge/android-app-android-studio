package com.eulersbridge.isegoria.feed;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.Network;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.NewsArticle;
import com.eulersbridge.isegoria.utilities.TimeConverter;
import com.eulersbridge.isegoria.utilities.Utils;

public class NewsArticleFragment extends Fragment {
    private View rootView;
    private RelativeLayout backgroundLayout;
    private ImageView authorImageView;

    private NewsArticle article;

    private boolean setLiked = false;
    private Network network;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.news_article_fragment, container, false);

        backgroundLayout = rootView.findViewById(R.id.topBackgroundNews);
        authorImageView = rootView.findViewById(R.id.newsArticleAuthorImage);

        article = getArguments().getParcelable("article");
        populateTextContent(article);

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getNewsArticleLiked(article.getId(), new Network.NewsArticleLikedListener() {
            @Override
            public void onFetchSuccess(long articleId, boolean likedByUser) {
                if (likedByUser) initiallyLiked();
            }

            @Override
            public void onFetchFailure(long articleId, Exception e) {}
        });

        network.getPicture(article.getPhotoURL(), new Network.PictureDownloadListener() {
            @Override
            public void onDownloadFinished(String url, @Nullable Bitmap bitmap) {
                populateArticlePhoto(bitmap);
            }

            @Override
            public void onDownloadFailed(String url, VolleyError error) {}
        });

        final String creatorPhotoURL = article.getCreator().getProfilePhotoURL();

        if (!TextUtils.isEmpty(creatorPhotoURL)) {
            network.getPicture(creatorPhotoURL, new Network.PictureDownloadListener() {
                @Override
                public void onDownloadFinished(String url, @Nullable Bitmap bitmap) {
                    populateCreatorPhoto(bitmap);
                }

                @Override
                public void onDownloadFailed(String url, VolleyError error) {}
            });

        }

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

    public long getArticleId() {
        return article.getId();
    }

    @UiThread
    void populateArticlePhoto(@Nullable Bitmap bitmap) {
        if (bitmap != null) {
            getActivity().runOnUiThread(() -> {
                Bitmap tintedPicture = Utils.tintBitmap(bitmap, Color.argb(128, 0, 0, 0));
                Drawable d = new BitmapDrawable(getActivity().getResources(), tintedPicture);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    backgroundLayout.setBackground(d);
                } else {
                    backgroundLayout.setBackgroundDrawable(d);
                }
            });
        }
    }

    @UiThread
    void populateCreatorPhoto(@Nullable Bitmap bitmap) {
        if (bitmap != null) {
            getActivity().runOnUiThread(() -> {
                Bitmap tintedBitmap = Utils.tintBitmap(bitmap, Color.argb(128, 0, 0, 0));
                authorImageView.setImageBitmap(tintedBitmap);
            });
        }
    }


    @UiThread
    void populateTextContent(final NewsArticle article) {
        int imageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 250, getResources().getDisplayMetrics());

        backgroundLayout.getLayoutParams().height = imageHeight;

        TextView newsTitle = rootView.findViewById(R.id.newsArticleTitle);
        newsTitle.setText(article.getTitle());

        final TextView newsArticleLikesView = rootView.findViewById(R.id.newsArticleLikes);
        newsArticleLikesView.setText(String.valueOf(article.getLikeCount()));

        TextView newsText = rootView.findViewById(R.id.textNews);
        newsText.setText(article.getContent());

        TextView newsArticleName = rootView.findViewById(R.id.photoTitle);
        newsArticleName.setText(article.getCreator().getFullName());

        TextView newsArticleDate = rootView.findViewById(R.id.newsArticleDate);
        newsArticleDate.setText(TimeConverter.convertTimestampToString(article.getDateTimestamp()));

        final ImageView flagView = rootView.findViewById(R.id.flagView);
        flagView.setOnClickListener(view -> flagView.setImageResource(R.drawable.flag));

        if (article.hasInappropriateContent()) flagView.setImageResource(R.drawable.flagdefault);

        final ImageView starView = rootView.findViewById(R.id.starView);
        starView.setOnClickListener(view -> {
            setLiked = !setLiked;

            if (setLiked) {
                starView.setImageResource(R.drawable.star);
                network.likeArticle(article.getId());
                int newLikes = Integer.parseInt(String.valueOf(newsArticleLikesView.getText())) + 1;
                newsArticleLikesView.setText(String.valueOf(newLikes));
            } else {
                starView.setImageResource(R.drawable.stardefault);
                network.unlikeArticle(article.getId());
                int newLikes = Integer.parseInt(String.valueOf(newsArticleLikesView.getText())) - 1;
                newsArticleLikesView.setText(String.valueOf(newLikes));
            }
        });

        //ImageView headShotImageView = (ImageView) rootView.findViewById(R.id.newsArticleHeadView);
        //network.getFirstPhotoImage(email, headShotImageView);

        //authorImageView.setVisibility(ViewGroup.VISIBLE);
    }
}