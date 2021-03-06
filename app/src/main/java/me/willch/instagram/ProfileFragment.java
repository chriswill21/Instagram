package me.willch.instagram;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import me.willch.instagram.model.Post;

public class ProfileFragment extends Fragment {

    // list of posts
    final List<Post> posts = new ArrayList<>();

    // the recycler view
    RecyclerView rvPosts;

    // profile pic
    ImageView profilePic;

    // parse file for profile pic
    ParseFile profilePictureFile;

    // profile name
    TextView profileName;

    // the adapter wired to the recycler view
    ProfilePagePostsAdapter adapter;

    ParseUser currentUser;

    public ProfileFragment() {
    }

    private SwipeRefreshLayout swipeContainer;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profilePic = getActivity().findViewById(R.id.iv_profile_pic);

        currentUser = ParseUser.getCurrentUser();

        currentUser.get("profilePicture");

        profilePictureFile = (ParseFile) currentUser.get("profilePicture");




        // Set profile pic

        int round_radius = 5;
        int round_margin = 5;

        final RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(round_radius, round_margin);

        final RequestOptions requestOptions = RequestOptions.bitmapTransform(
                roundedCornersTransformation
        );

        // get the correct place holder and image view for the current orientation
        int placeholderId = R.drawable.ic_instagram_profile;

        if (profilePictureFile!= null) {
            Glide.with(profilePic.getContext())
                    .load(profilePictureFile.getUrl())
                    .apply(
                            RequestOptions.placeholderOf(placeholderId)
                                    .error(placeholderId)
                                    .fitCenter()
                    )
                    .apply(requestOptions)
                    .into(profilePic);
        } else {
            Glide.with(profilePic.getContext())
                    .load(profilePictureFile)
                    .apply(
                            RequestOptions.placeholderOf(placeholderId)
                                    .error(placeholderId)
                                    .fitCenter()
                    )
                    .apply(requestOptions)
                    .into(profilePic);
        }

        // Set users name
        profileName = getActivity().findViewById(R.id.my_name);
        profileName.setText(currentUser.get("Name").toString());

        // Set up recycler view and adapter
        rvPosts = view.findViewById(R.id.rv_profile_posts);
        StaggeredGridLayoutManager gridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvPosts.setLayoutManager(gridLayoutManager);

        adapter = new ProfilePagePostsAdapter(getActivity(), posts);
        rvPosts.setAdapter(adapter);

        loadTopPosts();

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                loadTopPosts();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


    }


    public void refresh() {
        loadTopPosts();
    }

    public void loadTopPosts() {
        final Post.Query postsQuery = new Post.Query();
        postsQuery.getTop()
                .withUser()
                .findInBackground(new FindCallback<Post>() {
                    @Override
                    public void done(List<Post> objects, ParseException e) {
                        if (e == null) {
                            posts.clear();
                            posts.addAll(objects);
                            adapter.notifyDataSetChanged();

                        } else {
                            e.printStackTrace();
                        }
                        swipeContainer.setRefreshing(false);
                    }
                });
    }

}
