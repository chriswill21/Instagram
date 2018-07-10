package me.willch.instagram;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

import me.willch.instagram.model.Post;

public class ParseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Post.class);
        final Parse.Configuration configuration = new Parse.Configuration.Builder(this)
                .applicationId("cloutID")
                .clientKey("howmuchadollarreallycost")
                .server("http://chriswill21-fbu-instagram.herokuapp.com/parse")
                .build();


        Parse.initialize(configuration);
    }


}
