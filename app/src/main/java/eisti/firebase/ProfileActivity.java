package eisti.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private EditText editTextNameProfile;
    private EditText editTextAddressProfile;
    private TextView textViewProfile;
    private TextView textViewNameProfile;
    private TextView textViewAddressProfile;
    private Button buttonSaveProfile;
    private Button buttonDeleteProfile;
    private Button buttonLogOut;

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

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

        buttonSaveProfile.setOnClickListener(this);
        buttonDeleteProfile.setOnClickListener(this);
        buttonLogOut.setOnClickListener(this);

        editTextNameProfile = findViewById(R.id.editTextNameProfile);
        editTextAddressProfile = findViewById(R.id.editTextAddressProfile);

        this.getUserInformation();
        textViewProfile.setText(getString(R.string.welcome) + this.user.getEmail());
    }

    private void updateTextView(DataSnapshot dataSnapshot) {
        String key = dataSnapshot.getKey();

        switch (key) {
            case "name":
                textViewNameProfile.setText(getString(R.string.yourName)+" "+dataSnapshot.getValue(String.class));
                break;
            case "address":
                textViewAddressProfile.setText(getString(R.string.yourAddress)+" "+dataSnapshot.getValue(String.class));
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
    }
}
