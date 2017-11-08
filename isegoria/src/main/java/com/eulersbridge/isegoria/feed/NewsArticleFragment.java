package com.eulersbridge.isegoria.feed;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.network.IgnoredCallback;
import com.eulersbridge.isegoria.network.LikedResponse;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.NewsArticle;
import com.eulersbridge.isegoria.utilities.TimeConverter;
import com.eulersbridge.isegoria.utilities.TintTransformation;

import org.parceler.Parcels;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsArticleFragment extends Fragment {
    private View rootView;

    private NewsArticle article;
    private Isegoria isegoria;

    private boolean setLiked = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.news_article_fragment, container, false);

        ImageView articleImageView = rootView.findViewById(R.id.article_image);
        ImageView authorImageView = rootView.findViewById(R.id.article_image_author);

        article = Parcels.unwrap(getArguments().getParcelable("article"));
        populateTextContent(article);

        isegoria = (Isegoria)getActivity().getApplication();

        isegoria.getAPI().getNewsArticleLiked(article.id, isegoria.getLoggedInUser().email).enqueue(new Callback<LikedResponse>() {
            @Override
            public void onResponse(Call<LikedResponse> call, Response<LikedResponse> response) {
                LikedResponse likedResponse = response.body();

                if (likedResponse != null && likedResponse.liked) initiallyLiked();
            }

            @Override
            public void onFailure(Call<LikedResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });

        GlideApp.with(this)
                .load(article.photos.get(0).thumbnailUrl)
                .transform(new TintTransformation())
                .into(articleImageView);

        String creatorPhotoURL = article.creator.profilePhotoURL;

        if (!TextUtils.isEmpty(creatorPhotoURL)) {
            GlideApp.with(this)
                    .load(creatorPhotoURL)
                    .transform(new TintTransformation())
                    .into(authorImageView);
        }

        return rootView;
    }

    private void initiallyLiked() {
        final ImageView starView = rootView.findViewById(R.id.article_star);
        starView.setImageResource(R.drawable.star);
        setLiked = true;
    }

    @UiThread
    private void populateTextContent(final NewsArticle article) {

        TextView newsTitle = rootView.findViewById(R.id.article_title);
        newsTitle.setText(article.title);

        final TextView likesTextView = rootView.findViewById(R.id.article_likes);
        likesTextView.setText(String.valueOf(article.likeCount));

        TextView contentTextView = rootView.findViewById(R.id.article_content);
        contentTextView.setText(article.content);

        TextView authorNameTextView = rootView.findViewById(R.id.article_author);
        authorNameTextView.setText(article.creator.getFullName());

        TextView dateTextView = rootView.findViewById(R.id.article_date);
        dateTextView.setText(TimeConverter.convertTimestampToString(article.dateTimestamp));

        final ImageView flagView = rootView.findViewById(R.id.article_flag);
        flagView.setOnClickListener(view -> flagView.setImageResource(R.drawable.flag));

        if (article.hasInappropriateContent) flagView.setImageResource(R.drawable.flagdefault);

        final ImageView starView = rootView.findViewById(R.id.article_star);
        starView.setOnClickListener(view -> {
            setLiked = !setLiked;

            if (setLiked) {
                isegoria.getAPI().likeArticle(article.id, isegoria.getLoggedInUser().email).enqueue(new IgnoredCallback<>());

                starView.setImageResource(R.drawable.star);

                int newLikes = Integer.parseInt(String.valueOf(likesTextView.getText())) + 1;
                likesTextView.setText(String.valueOf(newLikes));

            } else {
                isegoria.getAPI().unlikeArticle(article.id, isegoria.getLoggedInUser().email).enqueue(new IgnoredCallback<>());

                starView.setImageResource(R.drawable.stardefault);

                int newLikes = Integer.parseInt(String.valueOf(likesTextView.getText())) - 1;
                likesTextView.setText(String.valueOf(newLikes));
            }
        });

        //ImageView headShotImageView = (ImageView) rootView.findViewById(R.id.newsArticleHeadView);
        //network.getFirstPhotoImage(email, headShotImageView);

        //authorImageView.setVisibility(ViewGroup.VISIBLE);
    }
}