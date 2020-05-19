package com.alvin.computeraccessoriesstore;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alvin.computeraccessoriesstore.Common.Common;
import com.alvin.computeraccessoriesstore.Model.UserModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.text.Html.fromHtml;

public class LoginActivity extends AppCompatActivity {

    AlertDialog.Builder builder;
    AlertDialog dialog;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    Button negatif, positif;
    TextInputEditText resetEmail;
    TextInputLayout tilResetEmail;

    @BindView(R.id.pbSignIn)
    ProgressBar pbSignIn;
    @BindView(R.id.tilEmail)
    TextInputLayout tilEmail;
    @BindView(R.id.tilPassword)
    TextInputLayout tilPassword;
    @BindView(R.id.etEmail)
    TextInputEditText etEmail;
    @BindView(R.id.etPassword)
    TextInputEditText etPassword;
    @BindView(R.id.tvForgotPassword)
    TextView tvForgotPassword;
    @BindView(R.id.tvSignUp)
    TextView tvSignUp;

    String email, password, getPassword;

    LinearLayout.LayoutParams paramsEmail =
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, // Width
                    LinearLayout.LayoutParams.WRAP_CONTENT);   // Height

    LinearLayout.LayoutParams paramsPassword =
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, // Width
                    LinearLayout.LayoutParams.WRAP_CONTENT);   // Height

    @OnClick(R.id.tvForgotPassword)
    void forgotPassword() {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_resetpassword, null);
        resetEmail = view.findViewById(R.id.etResetEmail);
        tilResetEmail = view.findViewById(R.id.tilResetEmail);
        negatif = view.findViewById(R.id.btnNegatif);
        positif = view.findViewById(R.id.btnPositif);

        builder = new AlertDialog.Builder(this).setCancelable(false).setView(view);
        builder.setTitle("Reset Password");

        dialog = builder.create();
        dialog.setOnShowListener(dialog -> {
            negatif.setOnClickListener(v -> {
                pbSignIn.setVisibility(View.INVISIBLE);
                dialog.dismiss();
            });

            positif.setOnClickListener(v -> {
                pbSignIn.setVisibility(View.VISIBLE);
                email = resetEmail.getText().toString();
                if (checkEmail(true, email)) {
                    firebaseAuth.sendPasswordResetEmail(email)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Reset password link sent to your email!", Toast.LENGTH_SHORT).show();
                                pbSignIn.setVisibility(View.INVISIBLE);
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "There is no user record!", Toast.LENGTH_SHORT).show();
                                pbSignIn.setVisibility(View.INVISIBLE);
                            });
                }
            });
        });
        dialog.show();
    }

    private boolean checkEmail(boolean check, String email) {

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            check = false;
            tilResetEmail.setHelperTextEnabled(true);
            tilResetEmail.setHelperText("Email is not valid!");
            tilResetEmail.setHelperTextColor(ColorStateList.valueOf(Color.RED));
        }
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            check = true;
            tilResetEmail.setHelperTextEnabled(false);
        }

        return check;
    }

    @OnClick(R.id.btnSignIn)
    void signIn() {

        pbSignIn.setVisibility(View.VISIBLE);

        //Get Text
        email = etEmail.getText().toString();
        password = etPassword.getText().toString();

        if (checkValidForm(true, email, password)) {
            if (checkPattern(true, email, password)) {

                pbSignIn.setVisibility(View.VISIBLE);

                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {

//                            if (firebaseUser.isEmailVerified()){
//                            }
//                            else{
//                                pbSignIn.setVisibility(View.INVISIBLE);
//                                Toast.makeText(this, "Please Verified your account before login!", Toast.LENGTH_SHORT).show();
//                            }

                            if (task.isSuccessful()) {
                                pbSignIn.setVisibility(View.INVISIBLE);
                                initClear();
                                Toast.makeText(this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                finish();
                            } else {
                                pbSignIn.setVisibility(View.INVISIBLE);
                                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                Log.d("task", task.getException().getMessage());
                            }

                        })
                        .addOnFailureListener(e -> {
                            pbSignIn.setVisibility(View.INVISIBLE);
                            Log.d("error", e.getMessage());
                        });
            }
        }
    }

    private boolean checkPattern(boolean check, String email, String password) {

        pbSignIn.setVisibility(View.INVISIBLE);

        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            check = true;
            tilEmail.setHelperTextEnabled(false);
            paramsEmail.setMargins(0, 0, 0, 0);
            tilEmail.setLayoutParams(paramsEmail);
        }

//        if (password.length() < 6) {
//            check = false;
//            tilPassword.setHelperTextEnabled(true);
//            tilPassword.setHelperText("Password must be >= 6 character!");
//            tilPassword.setHelperTextColor(ColorStateList.valueOf(Color.RED));
//            paramsPassword.setMargins(0, 0, 0, 50);
//            tilPassword.setLayoutParams(paramsPassword);
//        }

        if (password.length() >= 6) {
            check = true;
            tilPassword.setHelperTextEnabled(false);
            paramsPassword.setMargins(0, 0, 0, 0);
            tilPassword.setLayoutParams(paramsPassword);
        }


        return check;
    }

    private boolean checkValidForm(boolean check, String email, String password) {

        pbSignIn.setVisibility(View.INVISIBLE);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            check = false;
            tilEmail.setHelperTextEnabled(true);
            tilEmail.setHelperText("Email is not valid!");
            tilEmail.setHelperTextColor(ColorStateList.valueOf(Color.RED));

            paramsEmail.setMargins(0, 0, 0, 50);
            tilEmail.setLayoutParams(paramsEmail);
        }
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setHelperTextEnabled(false);
            paramsEmail.setMargins(0, 0, 0, 0);
            tilEmail.setLayoutParams(paramsEmail);
        }
        if (TextUtils.isEmpty(email) || email.equals("")) {
            check = false;
            etEmail.setError("Email is Required");
        }
        if (TextUtils.isEmpty(password) || password.equals("")) {
            check = false;
            tilPassword.setHelperTextEnabled(true);
            tilPassword.setHelperText("Password is Required");
            tilPassword.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            paramsPassword.setMargins(0, 0, 0, 50);
            tilPassword.setLayoutParams(paramsPassword);
        }

        return check;
    }


    private void initClear() {
        tilEmail.setHelperTextEnabled(false);
        tilPassword.setHelperTextEnabled(false);
        etEmail.setError(null);

        etEmail.setText(null);
        etPassword.setText(null);
        etPassword.clearFocus();
        etEmail.clearFocus();
    }

    @OnClick(R.id.tvSignUp)
    void signUp() {
        initClear();
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //getSupportActionBar().setTitle("Sign In");

        ButterKnife.bind(this);
        pbSignIn.setVisibility(View.INVISIBLE);
        tvSignUp.setText(fromHtml("<font color='#000000'>Not a member yet? " +
                "</font><font color='#0099cc'>Sign Up</font>"));

        tvForgotPassword.setText(fromHtml("<font color='#000000'>Forgot Password? </font>"));

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        initClear();

        if (firebaseAuth.getCurrentUser() != null) {
            Toast.makeText(this, "User is Logged in Already", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }
    }
}
