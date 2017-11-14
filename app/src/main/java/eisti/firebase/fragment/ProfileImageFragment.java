package eisti.firebase.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import eisti.firebase.R;

/**
 * Created by ErwanLBP on 14/11/17.
 */

public class ProfileImageFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "ProfileImageFragment";
    private static final int PICK_IMAGE = 42;

    private Activity activity;

    private ImageView imgProfilePic;
    private Button btnProfilePic;

    private StorageReference imageReference;

    public static ProfileImageFragment newInstance() {
        ProfileImageFragment fragment = new ProfileImageFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emailsignin, container, false);

        this.activity = getActivity();

        this.imgProfilePic = view.findViewById(R.id.imgProfilePic);
        this.btnProfilePic = view.findViewById(R.id.btnUploadProfilePic);
        this.btnProfilePic.setOnClickListener(this);

        // DELETE START
        imageReference = FirebaseStorage.getInstance().getReference().child("images").child(FirebaseAuth.getInstance().getUid());
        // DELETE END

        return view;
    }

    // DELETE START
    @Override
    public void onStart() {
        super.onStart();
        loadImage();
    }
    // DELETE END

    @Override
    public void onClick(View view) {
        if (view.getId() == btnProfilePic.getId()) {
            chooseImage();
        }
    }

    public void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE) {
            // DELETE START
            Toast.makeText(activity, "Uploading ...", Toast.LENGTH_SHORT).show();
            try {
                UploadTask uploadTask = imageReference.putStream(activity.getContentResolver().openInputStream(data.getData()));

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_SHORT).show();
                        exception.printStackTrace();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(activity, "Upload successful", Toast.LENGTH_SHORT).show();
                        loadImage();
                    }
                });
            } catch (Exception e) {
                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            // DELETE END
        }
    }

    private void loadImage() {
        // DELETE START
        // Comment faire de base
/*
        final long ONE_MEGABYTE = 1024 * 1024;
        imageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data is returns, use this as needed
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
*/

        // SPECIAL pour les images
        Glide.with(activity)
                .using(new FirebaseImageLoader())
                .load(imageReference)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // Pour éviter le caching
                .skipMemoryCache(true)                     // Pour éviter le caching
                .into(imgProfilePic);
        // DELETE END
    }


}
