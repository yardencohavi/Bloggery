package com.example.bloggery.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bloggery.R;
import com.example.bloggery.activities.HomeActivity;
import com.example.bloggery.activities.LoginActivity;
import com.example.bloggery.cache.CacheUtilities;
import com.example.bloggery.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {


    private TextView mUserNameTextView, mEmailTextView;
    private ImageView mProfileImageView;
    private Button mSignOutButton;
    private User mUser;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        mUser = CacheUtilities.getUser(getActivity());
        initViews(view);
        return view;
    }


    private void initViews(View view) {
        mProfileImageView = view.findViewById(R.id.profile_image);
        mSignOutButton = view.findViewById(R.id.sign_out);
        mSignOutButton.setOnClickListener(v -> signOut());
        view.findViewById(R.id.edit_my_profile).setOnClickListener(e -> rediectToEditProfile());

        view.findViewById(R.id.my_posts_button).setOnClickListener(this::redirectToMyPosts);
        mUserNameTextView = view.findViewById(R.id.user_name_text_view);
        mEmailTextView = view.findViewById(R.id.email_text_view);
        mUserNameTextView.setText(Html.fromHtml(String.format("<b>Name :</b> %s", mUser.getUserName())));
        mEmailTextView.setText(Html.fromHtml(String.format("<b>Email :</b> %s", mUser.getEmail())));
        Picasso.get().load(mUser.getProfileImageUrl()).placeholder(R.drawable.profile_place_holder).into(mProfileImageView);

    }

    private void rediectToEditProfile() {
        ((HomeActivity) getActivity()).loadFragment(new EditProfileFragment(), true, false);
    }

    private void redirectToMyPosts(View view) {
        Fragment fragment = new ItemsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ItemsFragment.USER_EMAIL_KEY, mUser.getEmail());
        fragment.setArguments(bundle);
        ((HomeActivity) getActivity()).loadFragment(fragment, true, false);
    }


    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        CacheUtilities.clearUser(getActivity());
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
