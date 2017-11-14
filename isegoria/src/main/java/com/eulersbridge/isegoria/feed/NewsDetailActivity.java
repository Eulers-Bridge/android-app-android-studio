package com.eulersbridge.isegoria.feed;

import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.Constant;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.NewsArticle;
import com.eulersbridge.isegoria.network.IgnoredCallback;
import com.eulersbridge.isegoria.network.LikedResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.utilities.TintTransformation;
import com.eulersbridge.isegoria.utilities.Utils;

import org.parceler.Parcels;

import retrofit2.Response;

public class NewsDetailActivity extends AppCompatActivity {

    private Isegoria isegoria;

    private boolean setLiked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.news_detail_activity);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        findViewById(R.id.back_button).setOnClickListener(view -> onBackPressed());

        ImageView articleImageView = findViewById(R.id.article_image);

        ImageView authorImageView = findViewById(R.id.article_image_author);

        NewsArticle article = Parcels.unwrap(getIntent().getExtras().getParcelable(Constant.ACTIVITY_EXTRA_NEWS_ARTICLE));
        populateTextContent(article);

        isegoria = (Isegoria)getApplication();

        isegoria.getAPI().getNewsArticleLiked(article.id, isegoria.getLoggedInUser().email).enqueue(new SimpleCallback<LikedResponse>() {
            @Override
            public void handleResponse(Response<LikedResponse> response) {
                LikedResponse likedResponse = response.body();

                if (likedResponse != null && likedResponse.liked) initiallyLiked();
            }
        });

        GlideApp.with(this)
                .load(article.getPhotoUrl())
                .transforms(new CenterCrop(), new TintTransformation(0.6))
                .placeholder(R.color.grey)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(articleImageView);

        String creatorPhotoURL = article.creator.profilePhotoURL;

        GlideApp.with(this)
                .load(creatorPhotoURL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(authorImageView);
    }

    private void initiallyLiked() {
        final ImageView starView = findViewById(R.id.article_star);
        starView.setImageResource(R.drawable.star);
        setLiked = true;
    }

    @UiThread
    private void populateTextContent(final NewsArticle article) {

        TextView newsTitle = findViewById(R.id.article_title);
        newsTitle.setText(article.title);

        final TextView likesTextView = findViewById(R.id.article_likes);
        likesTextView.setText(String.valueOf(article.likeCount));

        TextView contentTextView = findViewById(R.id.article_content);
        contentTextView.setText(article.content);

        TextView authorNameTextView = findViewById(R.id.article_author);
        authorNameTextView.setText(article.creator.getFullName());

        TextView dateTextView = findViewById(R.id.article_date);
        dateTextView.setText(Utils.convertTimestampToString(this, article.dateTimestamp));

        final ImageView flagView = findViewById(R.id.article_flag);
        flagView.setOnClickListener(view -> flagView.setImageResource(R.drawable.flag));

        if (article.hasInappropriateContent) flagView.setImageResource(R.drawable.flagdefault);

        final ImageView starView = findViewById(R.id.article_star);
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
