package me.willch.instagram;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.parceler.Parcels;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import me.willch.instagram.model.Post;

// Make sure to import this line at the top!

public class DetailsActivity extends AppCompatActivity {


    public ImageView ivProfileImage;
    public ImageView ivPostImage;
    public TextView tvUsername;
    public TextView tvBody;
    public TextView tvDate;
    public Post post;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        tvUsername = (TextView) findViewById(R.id.tvUserName);
        tvBody = (TextView) findViewById(R.id.tvBody);
        tvDate = (TextView) findViewById(R.id.tvDate);
        ivProfileImage = (ImageView) findViewById(R.id.ivProfileImage);
        ivPostImage = (ImageView) findViewById(R.id.ivPostImage);

        tvUsername.setText(getIntent().getStringExtra("UserName"));
        tvBody.setText(getIntent().getStringExtra("Body"));
        tvDate.setText(getIntent().getStringExtra("Date"));
        post = Parcels.unwrap(getIntent().getParcelableExtra("Post"));


        int round_radius = getBaseContext().getResources().getInteger(R.integer.radius);
        int round_margin = getBaseContext().getResources().getInteger(R.integer.margin);

        final RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(round_radius, round_margin);

        final RequestOptions requestOptions = RequestOptions.bitmapTransform(
                roundedCornersTransformation
        );


        // get the correct place holder and image view for the current orientation
        int placeholderId = R.drawable.ic_instagram_profile;

        try {
            Glide.with(ivProfileImage.getContext())
                    .load(post.getUser().fetchIfNeeded().getParseFile("profilePicture"))
                    .apply(
                            RequestOptions.placeholderOf(placeholderId)
                                    .error(placeholderId)
                                    .fitCenter()
                    )
                    .apply(requestOptions)
                    .into(ivProfileImage);
        } catch (com.parse.ParseException e) {
            e.printStackTrace();
        }


        Glide.with(ivPostImage.getContext())
                .load(post.getMedia().getUrl())
                .apply(requestOptions)
                .into(ivPostImage);


    }


}
