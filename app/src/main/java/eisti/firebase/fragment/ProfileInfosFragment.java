package eisti.firebase.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import eisti.firebase.R;
import eisti.firebase.UserInformation;

/**
 * Created by ErwanLBP on 14/11/17.
 */

public class ProfileInfosFragment extends Fragment implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = "ProfileInfosFragment";

    private Activity activity;

    private ProgressDialog progressDialog;

    private GoogleApiClient gac;
    private DatabaseReference userDBReference;
    private FirebaseUser user;

    private TextView tvWelcome;
    private EditText edtName;
    private EditText edtCBcode;
    private TextView tvName;
    private TextView tvCbCode;
    private Button btnSave;
    private Button btnLogout;
    private Button btnRageQuit;

    public static ProfileInfosFragment newInstance() {
        ProfileInfosFragment fragment = new ProfileInfosFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profileinfos, container, false);

        this.activity = getActivity();

        this.user = FirebaseAuth.getInstance().getCurrentUser();

        this.tvWelcome = view.findViewById(R.id.tvWelcome);
        this.edtName = view.findViewById(R.id.editTextNameProfile);
        this.edtCBcode = view.findViewById(R.id.editTextCBProfile);
        this.tvName = view.findViewById(R.id.textViewNameProfile);
        this.tvCbCode = view.findViewById(R.id.textViewCbCodeProfile);
        this.btnSave = view.findViewById(R.id.buttonSaveProfile);
        this.btnLogout = view.findViewById(R.id.buttonLogOutProfile);
        this.btnRageQuit = view.findViewById(R.id.buttonDeleteProfile);

        this.tvWelcome.setText("Welcome " + FirebaseAuth.getInstance().getCurrentUser().getEmail());

        this.btnSave.setOnClickListener(this);
        this.btnLogout.setOnClickListener(this);
        this.btnRageQuit.setOnClickListener(this);

        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Signing out ...");

        this.userDBReference = FirebaseDatabase.getInstance().getReference().child("users");

        // Fill user informations fields
        getUserInformation();

        // Google Client for Signout and Rage quit

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getResources().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the options specified by gso.
        gac = new GoogleApiClient.Builder(activity)
                .enableAutoManage((FragmentActivity) activity, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        return view;
    }

    private void getUserInformation() {
        Query users = this.userDBReference.child(this.user.getUid());

        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserInformation userInformation = dataSnapshot.getValue(UserInformation.class);

                if (userInformation == null) {
                    return;
                }
                tvName.setText("Name : " + userInformation.getName());
                tvCbCode.setText("CB code : " + userInformation.getCbCode());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonSaveProfile:
                saveUserInfos();
                break;
            case R.id.buttonLogOutProfile:
                logout();
                break;
            case R.id.buttonDeleteProfile:
                deleteProfile();
                break;
        }
    }

    private void saveUserInfos() {
        String name = edtName.getText().toString();
        int cbCode = Integer.parseInt(edtCBcode.getText().toString());

        UserInformation userInformation = new UserInformation(name, cbCode);

        this.userDBReference.child(this.user.getUid()).setValue(userInformation);
        Toast.makeText(activity, "Informations saved", Toast.LENGTH_SHORT).show();
    }

    public void logout() {
        showProgressDialog();

        // Google SignOut then firebase signout if it worked

        Auth.GoogleSignInApi.signOut(gac).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        hideProgressDialog();
                        if (status.isSuccess()) {
                            // Firebase SignOut
                            FirebaseAuth.getInstance().signOut();
                            activity.finish();
                        } else {
                            Toast.makeText(activity.getApplicationContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void deleteProfile() {
        deleteGoogleAccess();
        deleteUserData();
        deleteFirebaseProfile();

        activity.finish();
    }

    private void deleteGoogleAccess() {
        // Delete Google Access
        Auth.GoogleSignInApi.revokeAccess(gac).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (!status.isSuccess()) {
                            Toast.makeText(activity.getApplicationContext(), status.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void deleteFirebaseProfile() {

        // Delete Firebase user
        user.delete().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity.getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }

    public void deleteUserData() {

        // Delete user datas
        final String userID = user.getUid();
        userDBReference.child(userID).setValue(null).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity.getApplicationContext(), "Failed deleting user data", Toast.LENGTH_SHORT).show();
            }
        });
        FirebaseStorage.getInstance().getReference().child("images").child(userID).delete().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity.getApplicationContext(), "Failed deleting user image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        hideProgressDialog();
        Toast.makeText(activity.getApplicationContext(), connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(activity);
            progressDialog.setMessage("Signing out ...");
        }
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }
}
