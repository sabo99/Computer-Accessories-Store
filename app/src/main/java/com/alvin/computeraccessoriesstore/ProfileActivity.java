package com.alvin.computeraccessoriesstore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
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

import com.alvin.computeraccessoriesstore.Common.Common;
import com.alvin.computeraccessoriesstore.Model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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
    SweetAlertDialog sweetAlertDialogLoading;

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

    TextView tvNotif, title;
    TextInputLayout tilResetEmail, tilResetPasswordOld, tilResetPasswordNew;
    TextInputEditText etResetPasswordOld, etResetPasswordNew, etEmail, etName, etPhone, etPass;
    Button positif, negatif, btnCancel, btnUpdate, btnConfirm;

    String userId, name, phone;
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
                sweetAlertDialogLoading.show();
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

    private void initSweetAlertDialog() {
        sweetAlertDialogLoading = new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialogLoading.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        sweetAlertDialogLoading.setTitleText("Uploading...");
        sweetAlertDialogLoading.setCancelable(false);
    }


    private void uploadImageToFirebase(Uri imageUri) {

        StorageReference fileRef = storageReference.child(Common.USER_REF + "/" + firebaseUser.getUid() + "/profile.jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Picasso.get().load(uri).into(imgPhoto);
                        pbUploadImage.setVisibility(View.INVISIBLE);

                        sweetAlertDialogLoading.dismiss();
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
                    sweetAlertDialogLoading.dismiss();
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

            etName = view.findViewById(R.id.etName);
            etPhone = view.findViewById(R.id.etPhone);
            btnCancel = view.findViewById(R.id.btnCancel);
            btnUpdate = view.findViewById(R.id.btnUpdate);

            // Get Data
            etName.setText(tvName.getText().toString());
            etPhone.setText(tvPhone.getText().toString());

            dialog = builder.create();
            dialog.setOnShowListener(dialog -> {
                btnCancel.setOnClickListener(v -> {
                    dialog.dismiss();
                    pbUploadImage.setVisibility(View.INVISIBLE);
                });

                btnUpdate.setOnClickListener(v -> {
                    // Get Text
                    name = etName.getText().toString();
                    phone = etPhone.getText().toString();

                    if (checkFromValid(true, name)) {

                        Map<String, Object> updateData = new HashMap<>();
                        updateData.put(Common.F_NAME, name);
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
                    new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.SUCCESS_TYPE)
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

                                userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REF)
                                        .child(firebaseUser.getUid());
                                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            UserModel checkUserModel = dataSnapshot.getValue(UserModel.class);

                                            if (!checkUserModel.getPassword().equals(oldPassword)) {
                                                new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.WARNING_TYPE)
                                                        .setTitleText("Old Password Incorrect!")
                                                        .show();
                                                etResetPasswordOld.requestFocus();
                                            } else {
                                                AuthCredential credential = EmailAuthProvider.getCredential(checkUserModel.getEmail(),
                                                        checkUserModel.getPassword());
                                                firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            dialog.dismiss();
                                                            updatePasswordUser(newPassword);
                                                        }
                                                        else {
                                                            new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                                    .setTitleText("Oops...")
                                                                    .setContentText("Something went wrong!")
                                                                    .show();
                                                            dialog.dismiss();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                .setTitleText("Oops...")
                                                .setContentText("Something went wrong!")
                                                .show();
                                        dialog.dismiss();
                                    }
                                });

                            }
                        }
                    }
                });
            });
            dialog.show();
        }
    }

    private void updatePasswordUser(String newPassword) {
        sweetAlertDialogLoading = new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialogLoading.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        sweetAlertDialogLoading.setTitleText("Waiting...");
        sweetAlertDialogLoading.setCancelable(false);
        sweetAlertDialogLoading.show();

        firebaseUser.updatePassword(newPassword)
                .addOnSuccessListener(aVoid -> {

                    Map<String, Object> updateUser = new HashMap<>();
                    updateUser.put(Common.F_PASS, newPassword);

                    FirebaseDatabase.getInstance().getReference(Common.USER_REF)
                            .child(firebaseUser.getUid())
                            .updateChildren(updateUser)
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    sweetAlertDialogLoading.dismissWithAnimation();
                                    new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.ERROR_TYPE)
                                            .setTitleText("Oops...")
                                            .setContentText("Something went wrong!")
                                            .show();
                                    pbUploadImage.setVisibility(View.INVISIBLE);
                                }
                            })
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    sweetAlertDialogLoading.dismissWithAnimation();
                                    new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                            .setTitleText("Success")
                                            .setContentText("Password Reset Successfully!")
                                            .show();
                                    pbUploadImage.setVisibility(View.INVISIBLE);
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    sweetAlertDialogLoading.dismissWithAnimation();
                    new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Something went wrong!")
                            .show();
                    Log.d("Tag", e.getMessage());
                    pbUploadImage.setVisibility(View.INVISIBLE);
                });
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

    @OnClick(R.id.btnChangeEmail)
    void changeEmail() {

        if (!firebaseUser.isEmailVerified()) {
            pbUploadImage.setVisibility(View.INVISIBLE);

            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Email is not Verified!")
                    .setContentText("Please verified your account, before change the email!")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    })
                    .show();

        } else {
            new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                    .setCustomImage(getResources().getDrawable(R.drawable.ic_email_blue_24dp))
                    .setTitleText("Change Email")
                    .setContentText("Do you really want to change Email?")
                    .setConfirmText("Yes")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();

                            View view = LayoutInflater.from(ProfileActivity.this).inflate(R.layout.layout_change_email, null);

                            etEmail = view.findViewById(R.id.etEmail);
                            etPass = view.findViewById(R.id.etPassword);
                            btnCancel = view.findViewById(R.id.btnCancel);
                            btnConfirm = view.findViewById(R.id.btnConfirm);

                            builder = new AlertDialog.Builder(ProfileActivity.this)
                                    .setCancelable(false)
                                    .setView(view)
                                    .setTitle("Change Email");
                            dialog = builder.create();
                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    btnCancel.setOnClickListener(v -> {
                                        dialog.dismiss();
                                    });
                                    btnConfirm.setOnClickListener(v -> {

                                        String newEmail, oldEmail, pass;
                                        oldEmail = tvEmail.getText().toString();
                                        newEmail = etEmail.getText().toString();
                                        pass = etPass.getText().toString();

                                        if (checkChangeEmail(true, newEmail, pass)) {
                                            if (newEmail.equals(oldEmail)) {
                                                new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.WARNING_TYPE)
                                                        .setTitleText("Check your email!")
                                                        .setContentText("The same Email as the old one!")
                                                        .show();
                                                etEmail.requestFocus();
                                            } else {
                                                FirebaseDatabase.getInstance().getReference(Common.USER_REF).child(firebaseUser.getUid())
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                if (dataSnapshot.exists()) {
                                                                    UserModel userModel = dataSnapshot.getValue(UserModel.class);
                                                                    if (!pass.equals(userModel.getPassword())) {
                                                                        new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.WARNING_TYPE)
                                                                                .setTitleText("Password wrong!")
                                                                                .show();
                                                                        etPass.requestFocus();
                                                                    } else {
                                                                        AuthCredential credential = EmailAuthProvider.getCredential(oldEmail, pass);
                                                                        firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    updateEmailUser(newEmail);
                                                                                    dialog.dismiss();
                                                                                }
                                                                                else {
                                                                                    dialog.dismiss();
                                                                                    new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                                                            .setTitleText("Oops...")
                                                                                            .setContentText("Something went wrong!")
                                                                                            .show();
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                dialog.dismiss();
                                                                new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                                        .setTitleText("Oops...")
                                                                        .setContentText("Something went wrong!")
                                                                        .show();
                                                            }
                                                        });
                                            }
                                        }


                                    });
                                }
                            });

                            dialog.show();
                            title = dialog.findViewById(getResources().getIdentifier("alertTitle", "id", "android"));
                            title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
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

    private void updateEmailUser(String newEmail) {
        sweetAlertDialogLoading = new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialogLoading.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        sweetAlertDialogLoading.setTitleText("Waiting...");
        sweetAlertDialogLoading.setCancelable(false);
        sweetAlertDialogLoading.show();

        firebaseUser.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> updateEmail = new HashMap<>();
                    updateEmail.put(Common.F_EMAIL, newEmail);

                    FirebaseDatabase.getInstance().getReference(Common.USER_REF)
                            .child(firebaseUser.getUid())
                            .updateChildren(updateEmail)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                            .setTitleText("Success")
                                            .setContentText("Email Successfully Changed!")
                                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                @Override
                                                public void onClick(SweetAlertDialog sw) {
                                                    sweetAlertDialogLoading.dismissWithAnimation();
                                                    sw.setTitleText("Email has Changed!")
                                                            .setContentText("Re-login, Please Continue!")
                                                            .setConfirmText("Confirm")
                                                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                                @Override
                                                                public void onClick(SweetAlertDialog sw) {
                                                                    sw.dismissWithAnimation();
                                                                    // Clear Model
                                                                    Common.currentUser = null;
                                                                    Common.storeItemsSelected = null;
                                                                    Common.selectedItems = null;
                                                                    firebaseAuth.signOut();
                                                                    firebaseUser.sendEmailVerification();
                                                                    Intent i = new Intent(ProfileActivity.this, LoginActivity.class);
                                                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                    startActivity(i);
                                                                    finish();
                                                                }
                                                            })
                                                            .changeAlertType(SweetAlertDialog.NORMAL_TYPE);
                                                }
                                            })
                                            .show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    sweetAlertDialogLoading.dismissWithAnimation();
                                    new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.ERROR_TYPE)
                                            .setTitleText("Oops...")
                                            .setContentText("Something went wrong!")
                                            .show();
                                }
                            });
                } else {
                    sweetAlertDialogLoading.dismissWithAnimation();
                    new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Something went wrong! \nPlease Re-login.")
                            .show();
                }
            }
        });
    }

    private boolean checkChangeEmail(boolean check, String email, String pass) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            check = false;
            etEmail.setError("Email is not valid!");
        }
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(null);
        }

        if (TextUtils.isEmpty(email) || email.equals("")) {
            check = false;
            etEmail.setError("Email is not valid!");
        }
        if (!TextUtils.isEmpty(email) || !email.equals("")) {
            etEmail.setError(null);
        }

        if (pass.length() < 6) {
            check = false;
            new SweetAlertDialog(ProfileActivity.this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Password is Empty!")
                    .setContentText("Please confirm the password!")
                    .show();
        }
        if (pass.length() >= 6) {
            //
        }
        return check;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        initSweetAlertDialog();

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

        if (!firebaseUser.isEmailVerified()) {
            tvNotifVerified.setVisibility(View.VISIBLE);
            tvVerify.setVisibility(View.VISIBLE);
        } else {
            tvNotifVerified.setVisibility(View.GONE);
            tvVerify.setVisibility(View.GONE);
        }

    }

    @OnClick(R.id.tvVerify)
    void onVerifyEmail() {

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
