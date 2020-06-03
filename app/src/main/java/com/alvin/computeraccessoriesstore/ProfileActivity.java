package com.alvin.computeraccessoriesstore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.Activity;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alvin.computeraccessoriesstore.Common.Common;
import com.alvin.computeraccessoriesstore.Model.UserModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.supercharge.shimmerlayout.ShimmerLayout;

public class ProfileActivity extends AppCompatActivity {

    AlertDialog.Builder builder, builderUpdate;
    AlertDialog dialog, dialogUpdate;

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

            builder = new AlertDialog.Builder(this).setCancelable(false);
            builder.setTitle("Email is not Verified!")
                    .setMessage("Please verified your account, before change photo profile!")
                    .setPositiveButton("Continue", (dialog1, which) -> {
                      dialog1.dismiss();
                    });
            dialog = builder.create();
            dialog.show();
            displayDialog(dialog);

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, Common.REQUEST_WRITE_PERMISSION);
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

        if (requestCode == Common.REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
                showLoadingUpload();
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception e = result.getError();
                pbUploadImage.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "Error: " + e, Toast.LENGTH_SHORT).show();
            } else
                pbUploadImage.setVisibility(View.INVISIBLE);
        }

    }

    private void showLoadingUpload() {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_upload_loading, null);
        builder = new AlertDialog.Builder(this).setCancelable(false).setView(view);
        dialog = builder.create();
        dialog.show();
    }


    private void uploadImageToFirebase(Uri imageUri) {

        StorageReference fileRef = storageReference.child(Common.USER_REF + "/" + firebaseUser.getUid() + "/profile.jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Picasso.get().load(uri).into(imgPhoto);
                        pbUploadImage.setVisibility(View.INVISIBLE);
                        dialog.dismiss();

                        // File Path Image or Link Image
                        filePath = uri.toString();
                        Log.d("url", filePath);

                    });
                    Toast.makeText(this, "Image Upload Success.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
                    pbUploadImage.setVisibility(View.INVISIBLE);
                    dialog.dismiss();
                });

    }
    // ---------------------------------------------------------------------------------------------

    @OnClick(R.id.btnEditProfile)
    void editProfile() {
        pbUploadImage.setVisibility(View.VISIBLE);

        if (!firebaseUser.isEmailVerified()) {
            pbUploadImage.setVisibility(View.INVISIBLE);

            builder = new AlertDialog.Builder(this).setCancelable(false);
            builder.setTitle("Email is not Verified!")
                    .setMessage("Please verified your account, before edit profile!")
                    .setPositiveButton("Continue", (dialog1, which) -> {
                       dialog1.dismiss();
                    });
            dialog = builder.create();
            dialog.show();
            displayDialog(dialog);
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


                        builderUpdate = new AlertDialog.Builder(ProfileActivity.this).setCancelable(false);
                        builderUpdate.setTitle("Update User Profile")
                                .setMessage("Are you sure change this profile?")
                                .setPositiveButton("OK", (dialog1, which) -> {
                                    dialog1.dismiss();
                                    dialog.dismiss();
                                    updateUserProfile(updateData);
                                })
                                .setNegativeButton("CANCEL", (dialog1, which) -> {
                                    dialog1.dismiss();
                                    pbUploadImage.setVisibility(View.INVISIBLE);
                                });
                        dialogUpdate = builderUpdate.create();
                        dialogUpdate.show();
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
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pbUploadImage.setVisibility(View.INVISIBLE);
                    dialog.dismiss();
                })
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Updated Successfully.", Toast.LENGTH_SHORT).show();
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

            builder = new AlertDialog.Builder(this).setCancelable(false);
            builder.setTitle("Email is not Verified!")
                    .setMessage("Please verified your account, before reset password!")
                    .setPositiveButton("Continue", (dialog1, which) -> {
                        dialog1.dismiss();
                    });
            dialog = builder.create();
            dialog.show();
            displayDialog(dialog);

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
                            firebaseUser = firebaseAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                firebaseUser.updatePassword(newPassword)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Password Reset Successfully", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                            pbUploadImage.setVisibility(View.INVISIBLE);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Password Reset Failed!", Toast.LENGTH_SHORT).show();
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
        builder = new AlertDialog.Builder(this).setCancelable(false);
        builder.setMessage("Sign Out?")
                .setNegativeButton("NO", (dialog1, which) -> {
                    dialog1.dismiss();
                })
                .setPositiveButton("YES", (dialog1, which) -> {
                    dialog1.dismiss();
                    // Clear Model
                    Common.currentUser = null;
                    Common.storeItemsSelected = null;
                    Common.selectedItems = null;
                    // Sign Out
                    firebaseAuth.signOut();
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                    finish();

                });
        dialog = builder.create();
        dialog.show();

        Button btnPos;
        btnPos = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        btnPos.setTextColor(Color.RED);
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
                        Toast.makeText(ProfileActivity.this, "Some error with database", Toast.LENGTH_SHORT).show();
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
        builder = new AlertDialog.Builder(this).setCancelable(false);
        builder.setTitle("Email Verification")
                .setMessage("Verification Email has been sent. You must be logout and verify email first")
                .setPositiveButton("Continue", (dialog1, which) -> {
                    firebaseUser.sendEmailVerification();
                    firebaseAuth.signOut();
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                    finish();
                    dialog1.dismiss();
                });
        dialog = builder.create();
        dialog.show();
        displayDialog(dialog);
    }

    private void displayDialog(AlertDialog dialog) {

        Button pos;
        TextView title, message;
        LinearLayout.LayoutParams paramsButton =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout.LayoutParams paramMessage =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        title = this.dialog.findViewById(getResources().getIdentifier("alertTitle", "id", "android"));
        title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        message = this.dialog.findViewById(android.R.id.message);
        paramMessage.setMargins(0, 20, 0, 0);
        message.setLayoutParams(paramMessage);
        message.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        pos = this.dialog.getButton(DialogInterface.BUTTON_POSITIVE);

        paramsButton.setMargins(0, 0, 0, 20);
        paramsButton.gravity = Gravity.CENTER;
        pos.setLayoutParams(paramsButton);
    }

}
