package eisti.firebase.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import eisti.firebase.activity.ProfileActivity;
import eisti.firebase.R;

/**
 * Created by ErwanLBP on 14/11/17.
 */

public class EmailSigninFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "EmailSigninFragment";

    private Activity activity;

    private ProgressDialog progressDialog;

    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnSignIn;
    private Button btnSignUp;

    public static EmailSigninFragment newInstance() {
        EmailSigninFragment fragment = new EmailSigninFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emailsignin, container, false);

        this.activity = getActivity();

        this.edtEmail = view.findViewById(R.id.editTextEmailSignIn);
        this.edtPassword = view.findViewById(R.id.editTextPasswordSignIn);
        this.btnSignIn = view.findViewById(R.id.buttonSignIn);
        this.btnSignIn.setOnClickListener(this);
        this.btnSignUp = view.findViewById(R.id.buttonSignUp);
        this.btnSignUp.setOnClickListener(this);

        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Signing in ...");

        return view;
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == btnSignIn.getId()) {
            signIn();
        } else if (view.getId() == btnSignUp.getId()) {
            signUp();
        }
    }

    public void signIn() {

        if (!areFieldsValid())
            return;

        showProgressDialog();

        // DELETE START
        FirebaseAuth.getInstance().signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressDialog();
                        if (task.isSuccessful()) {
                            startActivity(new Intent(activity, ProfileActivity.class));
                        } else {
                            Toast.makeText(activity, "Couldn't sign in, please try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // DELETE END
    }

    public void signUp() {

        if (!areFieldsValid())
            return;

        showProgressDialog();

        // DELETE START
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressDialog();

                        if (task.isSuccessful()) {
                            startActivity(new Intent(activity, ProfileActivity.class));
                        } else {
                            Toast.makeText(activity, "Couldn't register, please try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // DELETE END
    }

    public boolean areFieldsValid() {
        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(activity, "Please enter email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(activity, "Please enter password", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(activity);
            progressDialog.setMessage("Signing in ...");
        }
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }
}
