package me.willch.instagram;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;

import me.willch.instagram.model.Post;

import static android.app.Activity.RESULT_OK;

public class ComposeFragment extends Fragment{

    public interface Callback {

        /**
         * This method will be implemented by my activity, and my fragment will call this
         * method when there is a text change event.
         */
        void refreshMyRecycler();
    }

    /**
     * Reference to something that implements my Callback.
     */
    private Callback inputCallback;



    /**
     * It is important to use onAttach(Context context) here instead of onAttach(Activity activity).
     *
     * Why? Well, onAttach(Activity activity) is deprecated, which means it will be removed in a
     * later iteration of the android SDK. We don't want to willingly use code that we know will
     * be removed because we are thoughtful engineers :-)
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // `instanceof` here is how we check if the containing context (in our case the activity)
        // implements the required callback interface.
        //
        // If it does not implement the required callback, we want
        if (context instanceof Callback) {

            // If it is an instance of our Callback then we want to cast the context to a Callback
            // and store it as a reference so we can later update the callback when there has been
            // a text change event.
            inputCallback = (Callback) context;
        } else {
            // Throwing an error and making your application crash instead of just sweeping it under
            // the rug is called being an "offensive" programmer.
            //
            // The best defense is a strong offense.
            throw new IllegalStateException("Containing context must implement UserInputFragment.Callback.");
        }
    }




    private static String imagePath;// = "/sdcard/DCIM/Camera/IMG_20180710_120811.jpg";
    private EditText imageDescription;
    private Button postButton;
    private Button selectPhotoButton;
    private ImageView image;
    Boolean imageChanged = false;
    Fragment thisFragment = this;

    final boolean[] descriptionStatus = new boolean[1];


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        imageDescription = view.findViewById(R.id.description_et);
        postButton = view.findViewById(R.id.post_btn);
        selectPhotoButton = view.findViewById(R.id.select_photo_btn);
        image = view.findViewById(R.id.ivImageToPost);

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkForImage())
                {
                    checkForDescription(imageDescription.getText().toString());
                }

            }
        });

        selectPhotoButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //loadTopPosts();
                onLaunchCamera(getView());

            }
        });
    }


    private boolean checkForImage() {
        if (!imageChanged) {
            // check if there's an image
            final AlertDialog.Builder alertNoImageBuilder = new AlertDialog.Builder(getContext());
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

    private void checkForDescription(String description) {
        // Check if description is empty

        if (description.equals("")) {
            final AlertDialog.Builder alertEmptyCaptionBuilder = new AlertDialog.Builder(getContext());
            alertEmptyCaptionBuilder.setMessage("Are you sure you don't want to add a caption?")
                    .setCancelable(false)
                    .setPositiveButton("Yes, Jesus take the wheel!", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                            postLogic();
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton("No, take me back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            AlertDialog alertEmptyCaption = alertEmptyCaptionBuilder.create();
            alertEmptyCaption.show();
        } else {
            postLogic();
        }

    }

    private void postLogic(){
        final String description = imageDescription.getText().toString();
        final ParseUser user = ParseUser.getCurrentUser();

        final File file = new File(imagePath);
        final ParseFile parseFile = new ParseFile(file);

        createPost(description, parseFile, user);
    }


    private void createPost(String description, ParseFile imageFile, ParseUser user) {

        final Post newPost = new Post();
        newPost.setDescription(description);
        newPost.setImage(imageFile);
        newPost.setUser(user);

                newPost.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
//                    inputCallback.refreshMyRecycler();
                    Log.d("HomeActivity", "Create post success");
                    imageDescription.setText("");
                    image.setImageResource(R.drawable.camera_shadow_fill);
                    final HomeActivity activity = ((HomeActivity) getActivity());
                    activity.onPostCreated();
                } else {
                    e.printStackTrace();
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
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

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
                ImageView ivPreview = (ImageView) getActivity().findViewById(R.id.ivImageToPost);
                ivPreview.setImageBitmap(takenImage);
                imageChanged = true;
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
