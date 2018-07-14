package me.willch.instagram;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.parse.GetCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import me.willch.instagram.model.Post;

public class HomeActivity extends AppCompatActivity implements ComposeFragment.Callback{

    ParseUser user;

    /**
     * The list of fragments used in the view pager. They live in the activity and we pass them down
     * to the adapter upon creation.
     */
    private final List<Fragment> fragments = new ArrayList<>();


    /**
     * A reference to our view pager.
     */
    private ViewPager viewPager;

    /**
     * A reference to our bottom navigation view.
     */
    private BottomNavigationView bottomNavigation;

    /**
     * The adapter used to display information for our bottom navigation view.
     */
    private InstagramFragmentAdapter adapter;


    public RecyclerItemFragment homePageRecycler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Create the placeholder fragments to be passed to the ViewPager
        homePageRecycler = new RecyclerItemFragment();
//        homePageRecycler.setArguments(args);
        final ComposeFragment composePostFrag = new ComposeFragment();

        fragments.add(homePageRecycler);
        fragments.add(composePostFrag);
        fragments.add(new ProfileFragment());


        // Grab a reference to our view pager.
        viewPager = findViewById(R.id.pager);

        // Instantiate our ExampleAdapter which we will use in our ViewPager
        adapter = new InstagramFragmentAdapter(getSupportFragmentManager(), fragments);

        // Attach our adapter to our view pager.
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomNavigation.setSelectedItemId(R.id.action_home);
//                        homePageRecycler.refresh();
                        break;
                    case 1:
                        bottomNavigation.setSelectedItemId(R.id.action_discover);
                        break;
                    case 2:
                        bottomNavigation.setSelectedItemId(R.id.action_profile);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // Grab a reference to our bottom navigation view
        bottomNavigation = findViewById(R.id.bottom_navigation);

        // Handle the click for each item on the bottom navigation view.
        // we then delegate this out to the view pager adapter such that it can switch the
        // page which we are currently displaying
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        // Set the item to the first item in our list.
                        // This is the home placeholder fragment.
                        viewPager.setCurrentItem(0);
//                        homePageRecycler.refresh();
                        return true;
                    case R.id.action_discover:
                        // Set the item to the first item in our list.
                        // This is the discovery placeholder fragment.
                        viewPager.setCurrentItem(1);
                        return true;
                    case R.id.action_profile:
                        // Set the current item to the third item in our list
                        // which is the profile fragment placeholder
                        viewPager.setCurrentItem(2);
                        return true;
                    default:
                        return false;
                }
            }
        });

        if (Build.VERSION.SDK_INT >= 23) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        ImageView cameraButton = (ImageView) findViewById(R.id.ivCamera);
        cameraButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewPager thisPager = (ViewPager) findViewById(R.id.pager);
                thisPager.setCurrentItem(1);

            }
        });


        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        final Post.Query postQuery = new Post.Query();

        postQuery.getTop().withUser();

        setUser();

    }

    public void onPostCreated() {
        bottomNavigation.setSelectedItemId(R.id.action_home);
        homePageRecycler.refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//         Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /**
     * The example view pager which we use in combination with the bottom navigation view to make
     * a smooth horizontal sliding transition.
     */
    static class InstagramFragmentAdapter extends FragmentPagerAdapter {

        /**
         * The list of fragments which we are going to be displaying in the view pager.
         */
        private final List<Fragment> fragments;

        public InstagramFragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);

            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    public void logOutOption(MenuItem item) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Would you like to log out?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                        logOut();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public void  logOut() {
        Log.d("Logout", "Logged out");
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    final Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.e("Log Out Error!", "User wasn't logged out!");
                }
            }
        });

    }

    public  void setUser() {
        String userId = getIntent().getStringExtra("userId");
        ParseQuery query = ParseUser.getQuery();
        query.whereEqualTo("objectId", userId).getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    user = (ParseUser) object;
                }
            }
            @Override
            public void done(Object o, Throwable throwable) {
            }
        });
    }


    /**
     * This method will be called when the UserInputFragment has detected a text change.
     *
     */
    @Override
    public void refreshMyRecycler() {
        homePageRecycler.refresh();
    }

}
