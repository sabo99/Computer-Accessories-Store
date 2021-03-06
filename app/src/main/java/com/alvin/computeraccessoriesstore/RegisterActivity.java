package com.alvin.computeraccessoriesstore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.alvin.computeraccessoriesstore.Common.Common;
import com.alvin.computeraccessoriesstore.EventBus.SweetAlertDialogLogin;
import com.alvin.computeraccessoriesstore.Model.UserModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.text.Html.fromHtml;

public class RegisterActivity extends AppCompatActivity {

    SweetAlertDialog sweetAlertDialog;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    @BindView(R.id.tilEmail)
    TextInputLayout tilEmail;
    @BindView(R.id.tilPassword)
    TextInputLayout tilPassword;
    @BindView(R.id.tilConfirmPassword)
    TextInputLayout tilConfirmPassword;

    @BindView(R.id.etName)
    TextInputEditText etName;
    @BindView(R.id.etEmail)
    TextInputEditText etEmail;
    @BindView(R.id.etPhone)
    TextInputEditText etPhone;
    @BindView(R.id.tvFloatingLabelSpGender)
    TextView tvFloatingLabelSpGender;
    @BindView(R.id.spGender)
    Spinner spGender;
    @BindView(R.id.tvErrorSpinner)
    TextView tvErrorSpinner;
    @BindView(R.id.etPassword)
    TextInputEditText etPassword;
    @BindView(R.id.etConfirmPassword)
    TextInputEditText etConfirmPassword;
    @BindView(R.id.pbSignUp)
    ProgressBar pbSignUp;
    @BindView(R.id.tvSignIn)
    TextView tvSignIn;

    String userID, name, email, phone, gender, password, confirmPassword;

    LinearLayout.LayoutParams paramsEmail =
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, // Width
                    LinearLayout.LayoutParams.WRAP_CONTENT);   // Height

    LinearLayout.LayoutParams paramsPassword =
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, // Width
                    LinearLayout.LayoutParams.WRAP_CONTENT);   // Height

    LinearLayout.LayoutParams paramsConfirmPassword =
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, // Width
                    LinearLayout.LayoutParams.WRAP_CONTENT);   // Height

    @OnClick(R.id.btnSignUp)
    void signUp() {

        pbSignUp.setVisibility(View.VISIBLE);
        sweetAlertDialog.show();

        // Get Text
        name = etName.getText().toString();
        email = etEmail.getText().toString();
        phone = etPhone.getText().toString();
        gender = spGender.getSelectedItem().toString();
        password = etPassword.getText().toString();
        confirmPassword = etConfirmPassword.getText().toString();

        if (checkValidForm(true, name, email, gender, password, confirmPassword)) {
            if (checkPattern(true, email, password, confirmPassword)) {

                pbSignUp.setVisibility(View.VISIBLE);

                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {

                            pbSignUp.setVisibility(View.VISIBLE);
                            if (task.isSuccessful()) {
                                firebaseUser = firebaseAuth.getCurrentUser();
                                userID = firebaseUser.getUid();
                                UserModel userModel = new UserModel();
                                userModel.setName(name);
                                userModel.setEmail(email);
                                userModel.setPhone(phone);
                                userModel.setGender(gender);
                                userModel.setPassword(password);
                                userModel.setStatus("on"); /** Default Status - off */

                                FirebaseDatabase.getInstance()
                                        .getReference(Common.USER_REF)
                                        .child(userID)
                                        .setValue(userModel)
                                        .addOnFailureListener(e -> {
                                            pbSignUp.setVisibility(View.INVISIBLE);
                                            Log.d("error", e.getMessage());
                                            new SweetAlertDialog(RegisterActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                    .setTitleText("Oops...")
                                                    .setContentText("Something went wrong!")
                                                    .show();
                                        })
                                        .addOnSuccessListener(aVoid -> {

                                            // Send Verification Email
                                            firebaseUser.sendEmailVerification();

                                            pbSignUp.setVisibility(View.INVISIBLE);

                                            sweetAlertDialog.dismissWithAnimation();
                                            new SweetAlertDialog(RegisterActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                                    .setTitleText("Sign Up Successfully!")
                                                    .setContentText("Verification Email has been sent.")
                                                    .setConfirmText("Continue")
                                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                        @Override
                                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                            initClear();
                                                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                                            EventBus.getDefault().postSticky(new SweetAlertDialogLogin(true, false));
                                                        }
                                                    })
                                                    .show();


                                        });
                            } else {
                                sweetAlertDialog.dismissWithAnimation();
                                new SweetAlertDialog(RegisterActivity.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Oops...")
                                        .setContentText("Something went wrong!")
                                        .show();
                            }
                            pbSignUp.setVisibility(View.INVISIBLE);
                        })
                        .addOnFailureListener(e -> {
                            Log.d("error", e.getMessage());
                            new SweetAlertDialog(RegisterActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Oops...")
                                    .setContentText("Something went wrong!")
                                    .show();
                            pbSignUp.setVisibility(View.INVISIBLE);
                        });
            }
        }
    }

    private boolean checkPattern(boolean check, String email, String password, String confirmPass) {

        pbSignUp.setVisibility(View.INVISIBLE);

        // Check Pattern Email (TRUE)
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            check = true;
            tilEmail.setHelperTextEnabled(false);
            paramsEmail.setMargins(0, 0, 0, 0);
            tilEmail.setLayoutParams(paramsEmail);
        }

        // Check Empty Password (TRUE)
        if (!TextUtils.isEmpty(password) || !password.equals("")) {
            check = true;
            tilPassword.setHelperTextEnabled(false);
            paramsPassword.setMargins(0, 0, 0, 0);
            tilPassword.setLayoutParams(paramsPassword);
        }

        // Check Empty Confirm Password (TRUE)
        if (!TextUtils.isEmpty(confirmPass) || !confirmPass.equals("")) {
            check = true;
            tilConfirmPassword.setHelperTextEnabled(false);
            paramsConfirmPassword.setMargins(0, 0, 0, 0);
            tilConfirmPassword.setLayoutParams(paramsConfirmPassword);
        }

        // Check Confirm Password (TRUE)
        if (confirmPass.equals(password)) {
            check = true;
            tilConfirmPassword.setHelperTextEnabled(false);
            paramsConfirmPassword.setMargins(0, 0, 0, 0);
            tilConfirmPassword.setLayoutParams(paramsConfirmPassword);
        }

        // Check Password Length (FALSE)
        if (password.length() < 6) {
            check = false;
            tilPassword.setHelperTextEnabled(true);
            tilPassword.setHelperText("Password must be >= 6 character!");
            tilPassword.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            paramsPassword.setMargins(0, 0, 0, 50);
            tilPassword.setLayoutParams(paramsPassword);
        }
        // Check Password Length (TRUE)
        if (password.length() >= 6) {
            check = true;
            tilPassword.setHelperTextEnabled(false);
            paramsPassword.setMargins(0, 0, 0, 0);
            tilPassword.setLayoutParams(paramsPassword);
        }

        return check;
    }

    private boolean checkValidForm(boolean check, String name, String email, String gender, String password, String confirmPass) {

        pbSignUp.setVisibility(View.INVISIBLE);

        // Check Empty Name (FALSE)
        if (TextUtils.isEmpty(name) || name.equals("")) {
            check = false;
            etName.setError("Name is Required.");
        }

        // Check Empty Email - Pattern Email (FALSE)
        if (TextUtils.isEmpty(email) || email.equals("")) {
            check = false;
            etEmail.setError("Email is Required.");
        }
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

        if (gender.equals("Gender")) {
            check = false;

            tvErrorSpinner.setVisibility(View.VISIBLE);
            tvErrorSpinner.setText("Please Select your gender!");
            tvFloatingLabelSpGender.setVisibility(View.INVISIBLE);
        }
        if (!gender.equals("Gender")) {
            tvErrorSpinner.setVisibility(View.GONE);
            tvFloatingLabelSpGender.setVisibility(View.VISIBLE);
        }

        // Check Empty Password (FALSE)
        if (TextUtils.isEmpty(password) || password.equals("")) {
            check = false;
            tilPassword.setHelperTextEnabled(true);
            tilPassword.setHelperText("Password is Required.");
            tilPassword.setHelperTextColor(ColorStateList.valueOf(Color.RED));
            paramsPassword.setMargins(0, 0, 0, 50);
            tilPassword.setLayoutParams(paramsPassword);
        }
        if (!TextUtils.isEmpty(password) || !password.equals("") || password.length() >= 6) {
            tilPassword.setHelperTextEnabled(false);
            paramsPassword.setMargins(0, 0, 0, 0);
            tilPassword.setLayoutParams(paramsPassword);
        }

        // Check Empty Confirm Password (FALSE)
        if (TextUtils.isEmpty(confirmPass) || confirmPass.equals("")) {
            check = false;
            tilConfirmPassword.setHelperTextEnabled(true);
            tilConfirmPassword.setHelperText("Please Confirm Password!");
            tilConfirmPassword.setHelperTextColor(ColorStateList.valueOf(Color.RED));

            paramsConfirmPassword.setMargins(0, 0, 0, 50);
            tilConfirmPassword.setLayoutParams(paramsConfirmPassword);
        }
        if (!TextUtils.isEmpty(confirmPass) || !confirmPass.equals("")) {
            tilConfirmPassword.setHelperTextEnabled(false);
            paramsConfirmPassword.setMargins(0, 0, 0, 0);
            tilConfirmPassword.setLayoutParams(paramsConfirmPassword);
        }

        // Check Confirm Password (FALSE)
        if (!confirmPass.equals(password)) {
            check = false;
            tilConfirmPassword.setHelperTextEnabled(true);
            tilConfirmPassword.setHelperText("Password not match!");
            tilConfirmPassword.setHelperTextColor(ColorStateList.valueOf(Color.RED));

            paramsConfirmPassword.setMargins(0, 0, 0, 50);
            tilConfirmPassword.setLayoutParams(paramsConfirmPassword);
        }
        if (confirmPass.equals(password)) {
            tilConfirmPassword.setHelperTextEnabled(false);
        }

        return check;
    }

    private void initClear() {
        etName.setError(null);
        etName.setText(null);
        etName.clearFocus();
        etEmail.setError(null);
        etEmail.setText(null);
        etEmail.clearFocus();
        tilEmail.setHelperTextEnabled(false);
        etPhone.setText(null);
        spGender.setSelection(0);
        etPassword.setText(null);
        etPassword.clearFocus();
        tilPassword.setHelperTextEnabled(false);
        etConfirmPassword.setText(null);
        etConfirmPassword.clearFocus();
        tilConfirmPassword.setHelperTextEnabled(false);
    }

    @OnClick(R.id.tvSignIn)
    void signIn() {
        initClear();
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        initClear();


        tvSignIn.setText(fromHtml("<font color='#000000'>Already have an account? " +
                "</font><font color='#0099cc'>Sign In</font>"));

        initSweetAlertDialog();
    }

    private void initSweetAlertDialog() {
        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        sweetAlertDialog.setTitleText("Loading");
        sweetAlertDialog.setCancelable(false);
    }
}
