package me.willch.instagram;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;

public class CreateProfileActivity extends AppCompatActivity {

    ParseUser currnetUser;

    // Parse user's Name
    String actualName;

    // Parse user's profile pic
    ParseFile profilePicParseFile;


    private static String imagePath;// = "/sdcard/DCIM/Camera/IMG_20180710_120811.jpg";
    private Button doneButton;
    private EditText createProfileName_et;
    private Button selectPhotoButton;
    private ImageView profilePicture;
    Boolean imageChanged = false;
    Activity thisActivity = this;



    public CreateProfileActivity() {
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        currnetUser = ParseUser.getCurrentUser();

        createProfileName_et = findViewById(R.id.create_profile_name_et);
        doneButton = findViewById(R.id.create_profile_done_btn);
        selectPhotoButton = findViewById(R.id.create_profile_select_photo_btn);
        profilePicture = findViewById(R.id.create_profile_ivImageProfilePic);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkForImage())
                {
                    checkForName(createProfileName_et.getText().toString());
                }

            }
        });

        selectPhotoButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //loadTopPosts();
                onLaunchCamera(findViewById(android.R.id.content));

            }
        });

        onLaunchCamera(findViewById(android.R.id.content));
    }

    private boolean checkForImage() {
        if (!imageChanged) {
            // check if there's an image
            final AlertDialog.Builder alertNoImageBuilder = new AlertDialog.Builder(getBaseContext());
            alertNoImageBuilder.setMessage("You must add a picture first")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                            return;
                        }
                    });
            AlertDialog alertNoImage = alertNoImageBuilder.create();
            alertNoImage.show();
            return false;
        } else {
            return true;
        }
    }

    private void checkForName(String description) {
        // Check if description is empty

        if (description.equals("")) {
            Toast message = Toast.makeText(getBaseContext(), "Please add a name", Toast.LENGTH_SHORT);
        } else {
            updateUserLogic();
        }

    }

    private void updateUserLogic(){
        actualName = createProfileName_et.getText().toString();
        final File file = new File(imagePath);
        final ParseFile parseFile = new ParseFile(file);

        updateUser(actualName, parseFile, currnetUser);
    }


    private void updateUser(String name, ParseFile imageFile, ParseUser user) {

        user.put("Name", name);
        user.put("profilePicture", imageFile);
        user.saveInBackground(new SaveCallback() {

            @Override
            public void done(ParseException e) {
                // TODO Auto-generated method stub
                if (e != null){
                    e.printStackTrace();
                }else{
                    //updated successfully
                    final Intent intent = new Intent(thisActivity, HomeActivity.class);
                    intent.putExtra("userId", currnetUser.getObjectId());
                    startActivity(intent);
                    finish();
                }
            }
        });

    }




    public final String APP_TAG = "MyCustomApp";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";
    File photoFile;

    public void onLaunchCamera(View view) {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference to access to future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getBaseContext(), "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getBaseContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getBaseContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                imagePath = photoFile.getAbsolutePath();
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                ImageView ivPreview = (ImageView) thisActivity.findViewById(R.id.create_profile_ivImageProfilePic);
                ivPreview.setImageBitmap(takenImage);
                imageChanged = true;
            } else { // Result was a failure
                Toast.makeText(getBaseContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }



}
