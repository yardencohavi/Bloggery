package com.example.bloggery.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.bloggery.R;
import com.example.bloggery.cache.CacheUtilities;
import com.example.bloggery.model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {


    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private Button mRegisterButton, mAlreadyHaveAccountButton;
    private ImageView mCameraImageView;
    private Uri mProfileImageUri;
    private final FirebaseFirestore mDB = FirebaseFirestore.getInstance();
    private EditText mEmailEditText, mPasswordEditText, mNameEditText;
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        initViews();
    }

    private void initViews() {
        mAlreadyHaveAccountButton = findViewById(R.id.alreadyHaveAccountButton);
        mCameraImageView = findViewById(R.id.cameraImageView);
        mCameraImageView.setOnClickListener(v -> choosePictureFromGallery());
        mAlreadyHaveAccountButton.setOnClickListener(v -> finish());
        mRegisterButton = findViewById(R.id.signUpButton);
        mRegisterButton.setOnClickListener(v -> register());
        mProgressBar = findViewById(R.id.progressBar);
        mEmailEditText = findViewById(R.id.emailEditText);
        mPasswordEditText = findViewById(R.id.passwordEditText);
        mNameEditText = findViewById(R.id.nameEditText);
    }


    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (data != null) {
                            mProfileImageUri = data.getData();
                            mCameraImageView.setImageURI(mProfileImageUri);
                        } else {
                            Toast.makeText(RegisterActivity.this, "No image was selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    public void choosePictureFromGallery() {
        Intent openGalleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        openGalleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        someActivityResultLauncher.launch(openGalleryIntent);
    }


    private void register() {
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        String name = mNameEditText.getText().toString();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(name) || mProfileImageUri == null) {
            Toast.makeText(this, "All fields and image must be provided", Toast.LENGTH_SHORT).show();
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        uploadUserData(name, email, mProfileImageUri);
                    } else {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(RegisterActivity.this, "Register failed - " + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void redirectToHomeActivity() {
        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }


    private void uploadUserData(final String username, final String email, Uri imageUri) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("images");
        String imageName = username + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(imageUri));
        final StorageReference imageRef = storageReference.child(imageName);
        UploadTask uploadTask = imageRef.putFile(imageUri);
        uploadTask.continueWithTask(task -> imageRef.getDownloadUrl()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = new User(username, email, task.getResult().toString());
                CacheUtilities.setUser(RegisterActivity.this, user);
                mDB.collection("userProfileData").document(email).set(user).addOnSuccessListener(unused -> redirectToHomeActivity()).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(RegisterActivity.this, "Fails to create user and upload data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (!task.isSuccessful()) {
                mProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(RegisterActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}