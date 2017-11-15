package eisti.firebase.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import eisti.firebase.R;
import eisti.firebase.fragment.ProfileImageFragment;
import eisti.firebase.fragment.ProfileInfosFragment;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getFragmentManager().beginTransaction()
                .add(R.id.profileInfosFragment, ProfileInfosFragment.newInstance())
                .add(R.id.profilePicFragment, ProfileImageFragment.newInstance())
                .commit();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }
    }
}
