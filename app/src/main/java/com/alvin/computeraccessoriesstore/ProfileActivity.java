package com.alvin.computeraccessoriesstore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alvin.computeraccessoriesstore.Common.Common;
import com.alvin.computeraccessoriesstore.Model.UserModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.supercharge.shimmerlayout.ShimmerLayout;

public class ProfileActivity extends AppCompatActivity {

    AlertDialog.Builder builder;
    AlertDialog dialog;
    SweetAlertDialog sweetAlertDialog;

    DatabaseReference userRef;
    StorageReference storageReference;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    @BindView(R.id.mainLayout)
    LinearLayout mainLayout;
    @BindView(R.id.shimmerLayout)
    ShimmerLayout shimmerLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.pbUploadImage)
    ProgressBar pbUploadImage;
    @BindView(R.id.tvName)
    TextView tvName;
    @BindView(R.id.tvEmail)
    TextView tvEmail;
    @BindView(R.id.tvPhone)
    TextView tvPhone;
    @BindView(R.id.imgPhoto)
    CircleImageView imgPhoto;
    @BindView(R.id.tvNotifVerified)
    TextView tvNotifVerified;
    @BindView(R.id.tvVerify)
    TextView tvVerify;

    TextView tvNotif, title, message;
    TextInputLayout tilResetEmail, tilResetPasswordOld, tilResetPasswordNew;
    TextInputEditText etResetPasswordOld, etResetPasswordNew, etEmail, etName, etPhone;
    Button positif, negatif, btnCancel, btnUpdate;

    String userId, email, name, phone;
    Uri imageUri;

    String filePath;

    @OnClick(R.id.btnEditPhoto)
    void editPhoto() {
        pbUploadImage.setVisibility(View.VISIBLE);

        if (!firebaseUser.isEmailVerified()) {
            pbUploadImage.setVisibility(View.INVISIBLE);

            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Email is not Verified!")
                    .setContentText("Please verified your account, before change photo profile!")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    })
                    .show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, Common.REQUEST_WRITE_PERMISSION_GALLERY);
        }
    }

    private void chooseFile() {
        CropImage.activity()
                .setRequestedSize(500, 500)
                .setMultiTouchEnabled(true)
                .start(ProfileActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Common.REQUEST_WRITE_PERMISSION_GALLERY && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            chooseFile();
        } else
            pbUploadImage.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                uploadImageToFirebase(imageUri);
                showSweetAlertDialog();
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception e = result.getError();
                pbUploadImage.setVisibility(View.INVISIBLE);
                new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops...")
                        .setContentText("Something went wrong!")
                        .show();
            } else
                pbUploadImage.setVisibility(View.INVISIBLE);
        }

    }

    private void showSweetAlertDialog() {
        sweetAlertDialog = new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        sweetAlertDialog.setTitleText("Uploading...");
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();
    }


    private void uploadImageToFirebase(Uri imageUri) {

        StorageReference fileRef = storageReference.child(Common.USER_REF + "/" + firebaseUser.getUid() + "/profile.jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Picasso.get().load(uri).into(imgPhoto);
                        pbUploadImage.setVisibility(View.INVISIBLE);

                        sweetAlertDialog.dismiss();
                        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success")
                                .setContentText("Image Upload Success")
                                .show();

                        // File Path Image or Link Image
                        filePath = uri.toString();
                        Log.d("url", filePath);

                    });
                })
                .addOnFailureListener(e -> {
                    sweetAlertDialog.dismiss();
                    new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Something went wrong!")
                            .show();
                    pbUploadImage.setVisibility(View.INVISIBLE);
                });

    }
    // ---------------------------------------------------------------------------------------------

    @OnClick(R.id.btnEditProfile)
    void editProfile() {
        pbUploadImage.setVisibility(View.VISIBLE);

        if (!firebaseUser.isEmailVerified()) {
            pbUploadImage.setVisibility(View.INVISIBLE);

            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Email is not Verified!")
                    .setContentText("Please verified your account, before edit profile!")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    })
                    .show();

        } else {
            View view = LayoutInflater.from(this).inflate(R.layout.layout_profile_edit, null);
            builder = new AlertDialog.Builder(this).setCancelable(false).setView(view);
            builder.setTitle("Update Data Profile");

            etEmail = view.findViewById(R.id.etEmail);
            etName = view.findViewById(R.id.etName);
            etPhone = view.findViewById(R.id.etPhone);
            btnCancel = view.findViewById(R.id.btnCancel);
            btnUpdate = view.findViewById(R.id.btnUpdate);

            // Get Data
            etName.setText(tvName.getText().toString());
            etEmail.setText(tvEmail.getText().toString());
            etPhone.setText(tvPhone.getText().toString());

            dialog = builder.create();
            dialog.setOnShowListener(dialog -> {
                btnCancel.setOnClickListener(v -> {
                    dialog.dismiss();
                    pbUploadImage.setVisibility(View.INVISIBLE);
                });

                btnUpdate.setOnClickListener(v -> {
                    // Get Text
                    email = etEmail.getText().toString();
                    name = etName.getText().toString();
                    phone = etPhone.getText().toString();

                    if (checkFromValid(true, name)) {

                        Map<String, Object> updateData = new HashMap<>();
                        updateData.put(Common.F_NAME, name);
                        updateData.put(Common.F_EMAIL, email);
                        updateData.put(Common.F_PHONE, phone);


                        new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Update User Profile")
                                .setContentText("Are you sure change this profile?")
                                .setConfirmText("Yes")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        dialog.dismiss();
                                        sweetAlertDialog.dismissWithAnimation();
                                        updateUserProfile(updateData);
                                    }
                                })
                                .setCancelText("No")
                                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismissWithAnimation();
                                        pbUploadImage.setVisibility(View.INVISIBLE);
                                    }
                                })
                                .show();
                    }
                });
            });
            dialog.show();
            title = dialog.findViewById(getResources().getIdentifier("alertTitle", "id", "android"));
            title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
    }

    private void updateUserProfile(Map<String, Object> updateData) {
        userRef.child(firebaseUser.getUid())
                .updateChildren(updateData)
                .addOnFailureListener(e -> {
                    pbUploadImage.setVisibility(View.INVISIBLE);
                    new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Something went wrong!")
                            .show();
                })
                .addOnSuccessListener(aVoid -> {
                    new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Success")
                            .setContentText("Updated Successfully!")
                            .show();
                    pbUploadImage.setVisibility(View.INVISIBLE);
                });
    }

    private boolean checkFromValid(boolean check, String name) {
        if (TextUtils.isEmpty(name) || name.equals("")) {
            check = false;
            etName.setError("Name is Required.");
        }
        return check;
    }
    // ---------------------------------------------------------------------------------------------

    @OnClick(R.id.btnResetPassword)
    void resetPassword() {
        pbUploadImage.setVisibility(View.VISIBLE);

        if (!firebaseUser.isEmailVerified()) {
            pbUploadImage.setVisibility(View.INVISIBLE);

            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Email is not Verified!")
                    .setContentText("Please verified your account, before reset password!")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    })
                    .show();


        } else {
            View view = LayoutInflater.from(this).inflate(R.layout.layout_resetpassword, null);

            tvNotif = view.findViewById(R.id.tvNotif);
            tilResetEmail = view.findViewById(R.id.tilResetEmail);
            tilResetPasswordOld = view.findViewById(R.id.tilResetPasswordOld);
            tilResetPasswordNew = view.findViewById(R.id.tilResetPasswordNew);
            etResetPasswordOld = view.findViewById(R.id.etResetPasswordOld);
            etResetPasswordNew = view.findViewById(R.id.etResetPasswordNew);
            negatif = view.findViewById(R.id.btnNegatif);
            positif = view.findViewById(R.id.btnPositif);

            tvNotif.setText("Enter New Password");
            tilResetEmail.setVisibility(View.GONE);
            tilResetPasswordOld.setVisibility(View.VISIBLE);
            etResetPasswordOld.setVisibility(View.VISIBLE);
            tilResetPasswordNew.setVisibility(View.VISIBLE);
            etResetPasswordNew.setVisibility(View.VISIBLE);

            builder = new AlertDialog.Builder(this).setCancelable(false);
            builder.setTitle("Reset Password")
                    .setView(view);

            dialog = builder.create();
            dialog.setOnShowListener(dialog -> {
                negatif.setOnClickListener(v -> {
                    dialog.dismiss();
                    pbUploadImage.setVisibility(View.INVISIBLE);
                });
                positif.setOnClickListener(v -> {
                    pbUploadImage.setVisibility(View.INVISIBLE);
                    String oldPassword = etResetPasswordOld.getText().toString();
                    String newPassword = etResetPasswordNew.getText().toString();
                    if (checkPassword(true, oldPassword, newPassword)) {
                        if (checkNewPassword(true, oldPassword, newPassword)) {
                            if (firebaseUser != null) {
                                firebaseUser.updatePassword(newPassword)
                                        .addOnSuccessListener(aVoid -> {
                                            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                                    .setTitleText("Success")
                                                    .setContentText("Password Reset Successfully!")
                                                    .show();
                                            dialog.dismiss();
                                            pbUploadImage.setVisibility(View.INVISIBLE);
                                        })
                                        .addOnFailureListener(e -> {
                                            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                                                    .setTitleText("Oops...")
                                                    .setContentText("Something went wrong! \n Please Re-Login!")
                                                    .show();
                                            Log.d("Tag", e.getMessage());
                                            dialog.dismiss();
                                            pbUploadImage.setVisibility(View.INVISIBLE);
                                        });
                            }
                        }
                    }
                });
            });
            dialog.show();
        }
    }

    private boolean checkNewPassword(boolean check, String oldPassword, String newPassword) {
        if (oldPassword.equalsIgnoreCase(newPassword)) {
            check = false;
            tilResetPasswordNew.setHelperTextEnabled(true);
            tilResetPasswordNew.setHelperText("New Password is same!");
            tilResetPasswordNew.setHelperTextColor(ColorStateList.valueOf(Color.RED));
        }
        if (!oldPassword.equalsIgnoreCase(newPassword)) {
            check = true;
            tilResetPasswordNew.setHelperTextEnabled(false);
        }
        return check;
    }

    private boolean checkPassword(boolean check, String oldPassword, String newPassword) {

        if (oldPassword.length() < 6) {
            check = false;
            tilResetPasswordOld.setHelperTextEnabled(true);
            tilResetPasswordOld.setHelperText("Enter Old Password >= 6 Characters");
            tilResetPasswordOld.setHelperTextColor(ColorStateList.valueOf(Color.RED));
        }
        if (oldPassword.length() >= 6) {
            check = true;
            tilResetPasswordOld.setHelperTextEnabled(false);
        }

        if (newPassword.length() < 6) {
            check = false;
            tilResetPasswordNew.setHelperTextEnabled(true);
            tilResetPasswordNew.setHelperText("Enter New Password >= 6 Characters");
            tilResetPasswordNew.setHelperTextColor(ColorStateList.valueOf(Color.RED));
        }
        if (newPassword.length() >= 6) {
            check = true;
            tilResetPasswordNew.setHelperTextEnabled(false);
        }

        return check;
    }
    // ---------------------------------------------------------------------------------------------

    @OnClick(R.id.btnSignOut)
    void signOut() {

        new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("Change Account")
                .setContentText("Do you really want to change account?")
                .setCustomImage(R.drawable.ic_account_circle_black_24dp)
                .setConfirmText("Yes")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        // Clear Model
                        Common.currentUser = null;
                        Common.storeItemsSelected = null;
                        Common.selectedItems = null;
                        // Sign Out
                        firebaseAuth.signOut();
                        Intent i = new Intent(ProfileActivity.this, LoginActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();
                    }
                })
                .setCancelText("No")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                    }
                })
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        shimmerLayout.startShimmerAnimation();

        firebaseDatabase = FirebaseDatabase.getInstance();
        userRef = firebaseDatabase.getReference(Common.USER_REF);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Set Image Profile
        userId = firebaseUser.getUid();
        StorageReference profileRef = storageReference.child(Common.USER_REF + "/" + userId + "/profile.jpg");
        if (profileRef != null) {
            profileRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        Picasso.get().load(uri).into(imgPhoto);
                    });
        }

        // Set Data Profile
        userRef.child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        UserModel userModel = dataSnapshot.getValue(UserModel.class);

                        Common.currentUser = userModel;
                        Common.currentUser.setUid(firebaseUser.getUid());

                        tvName.setText(Common.currentUser.getName());
                        tvEmail.setText(Common.currentUser.getEmail());
                        tvPhone.setText(Common.currentUser.getPhone());

                        shimmerLayout.stopShimmerAnimation();
                        shimmerLayout.setVisibility(View.GONE);
                        mainLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops...")
                                .setConfirmText("Something went wrong!")
                                .show();
                        Log.d("userRef", databaseError.getMessage());
                    }
                });
        
        if (!firebaseUser.isEmailVerified()){
            tvNotifVerified.setVisibility(View.VISIBLE);
            tvVerify.setVisibility(View.VISIBLE);
        }else {
            tvNotifVerified.setVisibility(View.GONE);
            tvVerify.setVisibility(View.GONE);
        }
    }
    
    @OnClick(R.id.tvVerify)
    void onVerifyEmail(){

        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Email Verification")
                .setContentText("Are you sure sent the link verification email?")
                .setConfirmText("Yes")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.setTitleText("Success!")
                                .setContentText("Verification Email has been sent. \nYou must be logout and verify email first!")
                                .setConfirmText("Continue")
                                .showCancelButton(false)
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismissWithAnimation();
                                        firebaseUser.sendEmailVerification();
                                        firebaseAuth.signOut();
                                        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                                        finish();
                                    }
                                })
                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);

                    }
                })
                .setCancelText("No")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                    }
                })
                .show();
    }
}
