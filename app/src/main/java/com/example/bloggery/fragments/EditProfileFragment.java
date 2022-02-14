package com.example.bloggery.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bloggery.R;
import com.example.bloggery.activities.HomeActivity;
import com.example.bloggery.activities.LoginActivity;
import com.example.bloggery.activities.RegisterActivity;
import com.example.bloggery.cache.CacheUtilities;
import com.example.bloggery.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class EditProfileFragment extends Fragment {

    private User mUser;
    private final FirebaseFirestore mDB = FirebaseFirestore.getInstance();
    private Uri mProfileImageUri;
    private ProgressBar mProgressBar;
    private ImageView mCameraImageView;
    private EditText mNameEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_profile_fragment, container, false);
        mUser = CacheUtilities.getUser(getActivity());
        initViews(view);
        return view;
    }


    private void initViews(View view) {
        mProgressBar = view.findViewById(R.id.progressBar);
        mCameraImageView = view.findViewById(R.id.cameraImageView);
        mCameraImageView.setOnClickListener(e -> choosePictureFromGallery());
        mNameEditText = view.findViewById(R.id.name_edit_Text);
        view.findViewById(R.id.save_button).setOnClickListener(e -> updateInfo());

    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            mProfileImageUri = data.getData();
                            mCameraImageView.setImageURI(mProfileImageUri);
                        } else {
                            Toast.makeText(getActivity(), "No image was selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    public void choosePictureFromGallery() {
        Intent openGalleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        openGalleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        someActivityResultLauncher.launch(openGalleryIntent);
    }

    private void uploadUserData(final String username, final String email, Uri imageUri) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("images");
        String imageName = username + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(getActivity().getContentResolver().getType(imageUri));
        final StorageReference imageRef = storageReference.child(imageName);
        UploadTask uploadTask = imageRef.putFile(imageUri);
        uploadTask.continueWithTask(task -> imageRef.getDownloadUrl()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mUser.setProfileImageUrl(task.getResult().toString());
                mUser.setUserName(username);
                CacheUtilities.setUser(getActivity(), mUser);
                mDB.collection("userProfileData").document(email).set(mUser).addOnSuccessListener(unused -> {
                    Toast.makeText(getActivity(), "Profile updated", Toast.LENGTH_SHORT).show();
                    ((HomeActivity) getActivity()).switchToMainFragment();
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getActivity(), "Fails to update user date " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (!task.isSuccessful()) {
                mProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getActivity(), task.getException().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateInfo() {
        String name = mNameEditText.getText().toString();
        if (TextUtils.isEmpty(name) || mProfileImageUri == null) {
            Toast.makeText(getActivity(), "All fields and image must be provided", Toast.LENGTH_SHORT).show();
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);
        uploadUserData(name, FirebaseAuth.getInstance().getCurrentUser().getEmail(), mProfileImageUri);

    }
}
