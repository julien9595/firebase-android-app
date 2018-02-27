package eisti.firebase.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import eisti.firebase.R;
import eisti.firebase.fragment.EmailSigninFragment;
import eisti.firebase.fragment.GoogleSigninFragment;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getFragmentManager().beginTransaction()
                .add(R.id.emailSignInFragment, EmailSigninFragment.newInstance())
                .add(R.id.googleSignInFragment, GoogleSigninFragment.newInstance())
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, ProfileActivity.class));
        }
    }
}
