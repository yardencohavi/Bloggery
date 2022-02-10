package com.example.bloggery.activities;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.bloggery.R;
import com.example.bloggery.fragments.MainFragment;
import com.example.bloggery.fragments.ProfileFragment;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        findViewById(R.id.icon).setOnClickListener(e -> switchToMainFragment());
        findViewById(R.id.profile_image_view).setOnClickListener(e -> loadFragment(new ProfileFragment(), true, true));
        switchToMainFragment();
    }


    public void loadFragment(Fragment fragment, boolean shouldAddToBacKStack, boolean shouldEmptyAllStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (shouldEmptyAllStack) {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        transaction.replace(R.id.container, fragment);
        if (shouldAddToBacKStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    public void switchToMainFragment() {
        loadFragment(new MainFragment(), false, true);
    }


}