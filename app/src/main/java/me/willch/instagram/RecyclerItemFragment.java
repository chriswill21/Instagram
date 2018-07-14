package me.willch.instagram;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

import me.willch.instagram.model.Post;

public class RecyclerItemFragment extends Fragment {



    // list of posts
    final List<Post> posts = new ArrayList<>();

    // the recycler view
    RecyclerView rvPosts;

    // the adapter wired to the recycler view
    InstagramFeedAdapter adapter;

    public RecyclerItemFragment() {
    }

    private SwipeRefreshLayout swipeContainer;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvPosts = view.findViewById(R.id.rvPosts);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new InstagramFeedAdapter(getActivity(), posts);
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
//                    for (int i = 0; i < objects.size(); i++){
//                        try {
//                            listOfPosts.add(objects.get(i));
//                            ParseUser user = objects.get(i).getUser().fetchIfNeeded();
//                            Log.d("Home Activity", "Post[" + i + "] = " + objects.get(i).getDescription()
//                                    + "\nusername = " + user.getUsername());
//                        } catch (ParseException e1) {
//                            e1.printStackTrace();
//                        }
//                    }

                } else {
                    e.printStackTrace();
                }
                swipeContainer.setRefreshing(false);
            }
        });
    }


}
