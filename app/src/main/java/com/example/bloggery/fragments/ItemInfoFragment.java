package com.example.bloggery.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bloggery.R;
import com.example.bloggery.activities.HomeActivity;
import com.example.bloggery.cache.CacheUtilities;
import com.example.bloggery.model.Item;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import stream.customalert.CustomAlertDialogue;

public class ItemInfoFragment extends Fragment {


    private Item mItem;
    private String mCategory;
    private final FirebaseFirestore mDB = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_info_fragment, container, false);
        mItem = new Gson().fromJson(getArguments().getString(ItemsFragment.ITEM_KEY), Item.class);
        mCategory = getArguments().getString(ItemsFragment.ITEM_CATEGORY);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        ((Button) view.findViewById(R.id.header_category)).setText(String.format("Category: %s", mCategory));
        ((TextView) view.findViewById(R.id.item_title)).setText(mItem.getTitle());
        ImageView itemImage = (ImageView) view.findViewById(R.id.item_image);
        Picasso.get().load(mItem.getPhotoUrl()).placeholder(R.drawable.profile_place_holder).into(itemImage);

        ((TextView) view.findViewById(R.id.item_store)).setText(Html.fromHtml(String.format("<b>Store:</b> %s", mItem.getStore())));
        ((TextView) view.findViewById(R.id.item_price)).setText(Html.fromHtml(String.format("<b>Price:</b> %s$", mItem.getPrice())));
        if (mItem.getAuthor().equalsIgnoreCase(CacheUtilities.getUser(getActivity()).getUserName())) {
            view.findViewById(R.id.delete_button).setOnClickListener(this::onDeleteButtonCLicked);
            view.findViewById(R.id.edit_button).setOnClickListener(this::onEditButtonClicked);
        } else {
            view.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.edit_button).setVisibility(View.INVISIBLE);
        }

    }

    private void onEditButtonClicked(View view) {
        String itemJson = new Gson().toJson(mItem);
        Fragment fragment = new AddEditPostFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ItemsFragment.ITEM_KEY, itemJson);
        bundle.putString(ItemsFragment.ITEM_CATEGORY, mCategory);
        fragment.setArguments(bundle);
        ((HomeActivity) getActivity()).loadFragment(fragment, true, false);
    }

    private void onDeleteButtonCLicked(View view) {
        CustomAlertDialogue.Builder alert = new CustomAlertDialogue.Builder(getActivity())
                .setStyle(CustomAlertDialogue.Style.DIALOGUE)
                .setCancelable(false)
                .setTitle("Delete Item")
                .setMessage("Are you sure?")
                .setPositiveText("Confirm")
                .setPositiveColor(R.color.negative)
                .setPositiveTypeface(Typeface.DEFAULT_BOLD)
                .setOnPositiveClicked((view12, dialog) -> mDB.collection("items").document(mCategory).collection("subitems").document(mItem.documentId).delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Items Deleted", Toast.LENGTH_SHORT).show();
                        ((HomeActivity) getActivity()).switchToMainFragment();
                    } else {
                        Toast.makeText(getActivity(), "Item delete failed", Toast.LENGTH_SHORT).show();
                    }

                    dialog.dismiss();
                }))
                .setNegativeText("Cancel")
                .setNegativeColor(R.color.positive)
                .setOnNegativeClicked((view1, dialog) -> dialog.dismiss())
                .setDecorView(getActivity().getWindow().getDecorView())
                .build();
        alert.show();
    }

}
