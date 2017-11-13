package eisti.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.firebase.ui.storage.images.FirebaseImageLoader;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PICK_IMAGE = 42;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private StorageReference imageReference;

    private EditText editTextNameProfile;
    private EditText editTextAddressProfile;
    private TextView textViewProfile;
    private TextView textViewNameProfile;
    private TextView textViewAddressProfile;
    private Button buttonSaveProfile;
    private Button buttonDeleteProfile;
    private Button buttonLogOut;
    private ImageView image;
    private Button btnImage;

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        imageReference = FirebaseStorage.getInstance().getReference().child("images").child(FirebaseAuth.getInstance().getUid());

        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        this.user = firebaseAuth.getCurrentUser();

        textViewProfile = findViewById(R.id.textViewProfile);
        textViewNameProfile = findViewById(R.id.textViewNameProfile);
        textViewAddressProfile = findViewById(R.id.textViewAddressProfile);

        buttonLogOut = findViewById(R.id.buttonLogOut);
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile);
        buttonDeleteProfile = findViewById(R.id.buttonDeleteProfile);

        image = findViewById(R.id.imageView);
        btnImage = findViewById(R.id.btnImage);

        buttonSaveProfile.setOnClickListener(this);
        buttonDeleteProfile.setOnClickListener(this);
        buttonLogOut.setOnClickListener(this);
        btnImage.setOnClickListener(this);

        editTextNameProfile = findViewById(R.id.editTextNameProfile);
        editTextAddressProfile = findViewById(R.id.editTextAddressProfile);

        this.getUserInformation();
        textViewProfile.setText(getString(R.string.welcome) + " " + this.user.getEmail());
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadImage();
    }

    private void updateTextView(DataSnapshot dataSnapshot) {
        String key = dataSnapshot.getKey();

        switch (key) {
            case "name":
                textViewNameProfile.setText(dataSnapshot.getValue(String.class));
                break;
            case "address":
                textViewAddressProfile.setText(dataSnapshot.getValue(String.class));
                break;
        }
    }

    private void getUserInformation() {
        Query users = this.databaseReference.child("users").child(this.user.getUid());

/*        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserInformation userInformation = dataSnapshot.getValue(UserInformation.class);

                editTextNameProfile.setText("Name : " + userInformation.getName());
                editTextAddressProfile.setText("Address : " + userInformation.getAddress());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

        users.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
/*                String key = dataSnapshot.getKey();

                switch (key) {
                    case "name":
                        editTextNameProfile.setText(dataSnapshot.getValue(String.class));
                        break;
                    case "address":
                        editTextAddressProfile.setText(dataSnapshot.getValue(String.class));
                        break;
                }

                userInfos.add(dataSnapshot.getValue(String.class));
                arrayAdapter.notifyDataSetChanged();*/

                updateTextView(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                updateTextView(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void saveUserInformation() {
        String name = editTextNameProfile.getText().toString().trim();
        String address = editTextAddressProfile.getText().toString().trim();

        UserInformation userInformation = new UserInformation(name, address);

        databaseReference.child("users").child(this.user.getUid()).setValue(userInformation);
        Toast.makeText(this, "Information saved", Toast.LENGTH_SHORT).show();
    }

    private void deleteUser() {

        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    databaseReference.child("users").child(ProfileActivity.this.user.getUid()).setValue(null);
                    Toast.makeText(ProfileActivity.this, "User deleted", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, "Please sign out and re sign in to delete", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadImage() {
        // Comment faire de base

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


        // SPECIAL pour les images
        Glide.with(this.getApplicationContext())
                .using(new FirebaseImageLoader())
                .load(imageReference)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // Pour éviter le caching
                .skipMemoryCache(true)                     // Pour éviter le caching
                .into(image);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE) {
            try {
                Toast.makeText(ProfileActivity.this, "Uploading ...", Toast.LENGTH_SHORT).show();
                UploadTask uploadTask = imageReference.putStream(this.getContentResolver().openInputStream(data.getData()));
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(ProfileActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                        exception.printStackTrace();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        loadImage();
                        Toast.makeText(ProfileActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == buttonLogOut) {
            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        if (v == buttonSaveProfile) {
            saveUserInformation();
        }

        if (v == buttonDeleteProfile) {
            deleteUser();
        }

        if (v.getId() == R.id.btnImage) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        }
    }
}
