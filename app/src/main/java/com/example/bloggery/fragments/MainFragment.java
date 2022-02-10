package com.example.bloggery.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bloggery.R;
import com.example.bloggery.activities.HomeActivity;

public class MainFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);
        view.findViewById(R.id.rel1).setOnClickListener(this::openItemFragment);
        view.findViewById(R.id.rel2).setOnClickListener(this::openItemFragment);
        view.findViewById(R.id.rel3).setOnClickListener(this::openItemFragment);
        view.findViewById(R.id.rel4).setOnClickListener(this::openItemFragment);
        view.findViewById(R.id.rel5).setOnClickListener(this::openItemFragment);
        view.findViewById(R.id.rel6).setOnClickListener(this::openItemFragment);
        return view;
    }


    private void openItemFragment(View view) {
        Fragment fragment = new ItemsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ItemsFragment.ITEM_CATEGORY, convertIdToItemKey(view));
        fragment.setArguments(bundle);
        ((HomeActivity) getActivity()).loadFragment(fragment, true, false);
    }

    private String convertIdToItemKey(View view) {
        switch (view.getId()) {
            case R.id.rel1: {
                return ItemsFragment.Pants;
            }
            case R.id.rel2: {
                return ItemsFragment.Shirts;
            }
            case R.id.rel3: {
                return ItemsFragment.Coats;
            }
            case R.id.rel4: {
                return ItemsFragment.Dresses;
            }
            case R.id.rel5: {
                return ItemsFragment.Hats;
            }
            case R.id.rel6: {
                return ItemsFragment.Shoes;
            }
        }
        return "";

    }


}
