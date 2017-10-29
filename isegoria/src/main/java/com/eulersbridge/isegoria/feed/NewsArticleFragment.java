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
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.Network;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.utilities.TimeConverter;
import com.eulersbridge.isegoria.utilities.Utils;

public class NewsArticleFragment extends Fragment {
    private View rootView;
    private ImageView authorImageView;
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

        authorImageView = rootView.findViewById(R.id.newsArticleAuthorImage);

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
        getActivity().runOnUiThread(() -> {

            int imageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    (float) 250, getResources().getDisplayMetrics());

            RelativeLayout backgroundLayout = rootView.findViewById(R.id.topBackgroundNews);
            backgroundLayout.getLayoutParams().height = imageHeight;

            if (picture != null) {
                Bitmap tintedPicture = Utils.tintBitmap(picture, Color.argb(128, 0, 0, 0));
                Drawable d = new BitmapDrawable(getActivity().getResources(), tintedPicture);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    backgroundLayout.setBackground(d);
                } else {
                    backgroundLayout.setBackgroundDrawable(d);
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

            if (inappropriateContent) flagView.setImageResource(R.drawable.flagdefault);

            final ImageView starView = rootView.findViewById(R.id.starView);
            starView.setOnClickListener(view -> {
                setLiked = !setLiked;

                if (setLiked) {
                    starView.setImageResource(R.drawable.star);
                    network.likeArticle(articleId);
                    int newLikes = Integer.parseInt(String.valueOf(newsArticleLikesView.getText())) + 1;
                    newsArticleLikesView.setText(String.valueOf(newLikes));
                } else {
                    starView.setImageResource(R.drawable.stardefault);
                    network.unlikeArticle(articleId);
                    int newLikes = Integer.parseInt(String.valueOf(newsArticleLikesView.getText())) - 1;
                    newsArticleLikesView.setText(String.valueOf(newLikes));
                }
            });

            //ImageView headShotImageView = (ImageView) rootView.findViewById(R.id.newsArticleHeadView);
            //network.getFirstPhotoImage(email, headShotImageView);

            authorImageView.setVisibility(ViewGroup.VISIBLE);
        });
    }

    public void populateUserContent(User user) {
        getActivity().runOnUiThread(() -> {
            TextView newsArticleName = rootView.findViewById(R.id.photoTitle);
            newsArticleName.setText(user.getFullName());
        });

        if (!TextUtils.isEmpty(user.getProfilePhotoURL())) {
            network.getPicture(user.getProfilePhotoURL(), new Network.PictureDownloadListener() {
                @Override
                public void onDownloadFinished(String url, @Nullable Bitmap bitmap) {
                    Bitmap tintedBitmap = Utils.tintBitmap(bitmap, Color.argb(128, 0, 0, 0));
                    authorImageView.setImageBitmap(tintedBitmap);
                }

                @Override
                public void onDownloadFailed(String url, VolleyError error) {}
            });

        }
    }
}