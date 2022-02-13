package com.example.bloggery.fragments;

import static com.example.bloggery.fragments.ItemsFragment.ITEM_CATEGORY;

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
import com.example.bloggery.cache.CacheUtilities;
import com.example.bloggery.model.Item;
import com.example.bloggery.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddEditPostFragment extends Fragment {

    private String mItemCategory;
    private EditText mTitleEditText, mStoreEditText, mPriceEditText;
    private Uri mPhotoUri;
    private Button mSaveEditButton;
    private ImageView mItemImage;
    private TextView mHeaderTextView;
    private ProgressBar mProgressBar;
    private final FirebaseFirestore mDB = FirebaseFirestore.getInstance();

    private Item mEditItem;

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (data != null) {
                            mPhotoUri = data.getData();
                            mItemImage.setImageURI(mPhotoUri);
                        } else {
                            Toast.makeText(getActivity(), "No image was selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_post_fragment, container, false);
        mItemCategory = getArguments().getString(ITEM_CATEGORY);
        initViews(view);
        initEditItem();
        return view;
    }

    private boolean isEdit() {
        return mEditItem != null;
    }

    private void initEditItem() {
        String editItem = getArguments().getString(ItemsFragment.ITEM_KEY);
        if (!TextUtils.isEmpty(editItem)) {
            mEditItem = new Gson().fromJson(editItem, Item.class);
            mTitleEditText.setText(mEditItem.getTitle());
            mStoreEditText.setText(mEditItem.getStore());
            mPriceEditText.setText(mEditItem.getPrice());
            mSaveEditButton.setText("Edit");
            mHeaderTextView.setText("Edit item");
            Picasso.get().load(mEditItem.getPhotoUrl()).placeholder(R.drawable.profile_place_holder).into(mItemImage);
        }

    }


    private void initViews(View view) {
        mTitleEditText = view.findViewById(R.id.title_edit_Text);
        mStoreEditText = view.findViewById(R.id.store_edit_Text);
        mPriceEditText = view.findViewById(R.id.price_edit_text);
        mHeaderTextView = view.findViewById(R.id.header);
        view.findViewById(R.id.upload_photo_button).setOnClickListener(this::uploadPhoto);
        mSaveEditButton = view.findViewById(R.id.save_button);
        mSaveEditButton.setOnClickListener(this::saveEditItem);
        mItemImage = view.findViewById(R.id.image_item);
        mProgressBar = view.findViewById(R.id.progressBar);
    }

    private void editItem() {
        if (mPhotoUri != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("items");
            String imageName = UUID.randomUUID().toString() + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(getActivity().getContentResolver().getType(mPhotoUri));
            final StorageReference imageRef = storageReference.child(imageName);
            UploadTask uploadTask = imageRef.putFile(mPhotoUri);
            uploadTask.continueWithTask(task -> imageRef.getDownloadUrl()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    mDB.collection("items").document(mItemCategory).collection("subitems").document(mEditItem.documentId).set(genereateOverrideData(task.getResult().toString()), SetOptions.merge()).addOnSuccessListener(unused -> {
                        Toast.makeText(getActivity(), "Item Edited", Toast.LENGTH_SHORT).show();
                        ((HomeActivity) getActivity()).switchToMainFragment();
                    }).addOnFailureListener(e -> {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getActivity(), "Fails to upload item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                } else if (!task.isSuccessful()) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getActivity(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            mDB.collection("items").document(mItemCategory).collection("subitems").document(mEditItem.documentId).set(genereateOverrideData(""), SetOptions.merge()).addOnSuccessListener(unused -> {
                Toast.makeText(getActivity(), "Item Edited", Toast.LENGTH_SHORT).show();
                ((HomeActivity) getActivity()).switchToMainFragment();
            }).addOnFailureListener(e -> {
                mProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getActivity(), "Fails to upload item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void saveEditItem(View view) {
        if (isEdit()) {
            editItem();
        } else {
            saveItem();
        }
    }

    private Map<String, Object> genereateOverrideData(String imageUrl) {
        Map<String, Object> data = new HashMap<>();
        String title = mTitleEditText.getText().toString();
        String store = mStoreEditText.getText().toString();
        String price = mPriceEditText.getText().toString();
        if (!TextUtils.isEmpty(title)) {
            data.put("title", title);
        }
        if (!TextUtils.isEmpty(store)) {
            data.put("store", store);
        }
        if (!TextUtils.isEmpty(price)) {
            data.put("price", price);
        }
        if (!TextUtils.isEmpty(imageUrl)) {
            data.put("photoUrl", price);
        }
        return data;
    }

    private void saveItem() {
        String title = mTitleEditText.getText().toString();
        String store = mStoreEditText.getText().toString();
        String price = mPriceEditText.getText().toString();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(store) || TextUtils.isEmpty(price) || mPhotoUri == null) {
            Toast.makeText(getActivity(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        } else {
            User user = CacheUtilities.getUser(getActivity());
            mProgressBar.setVisibility(View.VISIBLE);
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("items");
            String imageName = UUID.randomUUID().toString() + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(getActivity().getContentResolver().getType(mPhotoUri));
            final StorageReference imageRef = storageReference.child(imageName);

            UploadTask uploadTask = imageRef.putFile(mPhotoUri);
            uploadTask.continueWithTask(task -> imageRef.getDownloadUrl()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Item item = new Item(user.getUserName(), title, store, price, task.getResult().toString(), user.getEmail());
                    mDB.collection("items").document(mItemCategory).collection("subitems").document().set(item).addOnSuccessListener(unused -> {
                        Toast.makeText(getActivity(), "Item added", Toast.LENGTH_SHORT).show();
                        ((HomeActivity) getActivity()).switchToMainFragment();
                    }).addOnFailureListener(e -> {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getActivity(), "Fails to upload item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                } else if (!task.isSuccessful()) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getActivity(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void uploadPhoto(View view) {
        Intent openGalleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        openGalleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        someActivityResultLauncher.launch(openGalleryIntent);
    }


}
