package com.example.bloggery.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloggery.ItemRecyclerViewAdapter;
import com.example.bloggery.R;
import com.example.bloggery.activities.HomeActivity;
import com.example.bloggery.model.Item;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ItemsFragment extends Fragment implements ItemRecyclerViewAdapter.ItemClickListener {
    public static final String Pants = "Pants";
    public static final String Shirts = "Shirts";
    public static final String Coats = "Coats";
    public static final String Dresses = "Dresses";
    public static final String Hats = "Hats";
    public static final String Shoes = "Shoes";

    public static final String ITEM_KEY = "item";
    public static final String ITEM_CATEGORY = "category";
    public static final String USER_EMAIL_KEY = "user";

    private String mItemCategory;
    private String mEmailUser;

    private RecyclerView mRecyclerView;
    private ItemRecyclerViewAdapter mAdapter;
    private final FirebaseFirestore mDB = FirebaseFirestore.getInstance();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_fragment, container, false);
        mItemCategory = getArguments().getString(ITEM_CATEGORY);
        mEmailUser = getArguments().getString(USER_EMAIL_KEY);
        initViews(view);
        fetchItems();
        return view;
    }

    private void fetchItems() {
        if (TextUtils.isEmpty(mEmailUser)) {
            mDB.collection("items").document(mItemCategory).collection("subitems").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<Item> itemList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Item item = document.toObject(Item.class);
                            item.documentId = document.getId();
                            item.category = mItemCategory;
                            itemList.add(item);
                        }
                        mAdapter.addItems(itemList);
                    }
                }
            });
        } else {
            mDB.collectionGroup("subitems").whereEqualTo("emailAuthor", mEmailUser).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<Item> itemList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Item item = document.toObject(Item.class);
                        item.category = document.getReference().getParent().getParent().getId();
                        item.documentId = document.getId();
                        itemList.add(item);
                    }
                    mAdapter.addItems(itemList);
                }
            });
        }

    }

    private void initViews(View view) {
        ((TextView) view.findViewById(R.id.header_item)).setText(TextUtils.isEmpty(mItemCategory) ? "My posts" : mItemCategory);
        if (TextUtils.isEmpty(mEmailUser)) {
            ((Button) view.findViewById(R.id.add_reomendation_button)).setOnClickListener(e -> redirectToAddArecomandtionFragment());
        } else {
            ((Button) view.findViewById(R.id.add_reomendation_button)).setVisibility(View.INVISIBLE);
        }

        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new ItemRecyclerViewAdapter(getContext());
        mAdapter.setClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void redirectToAddArecomandtionFragment() {
        Fragment fragment = new AddEditPostFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ItemsFragment.ITEM_CATEGORY, mItemCategory);
        fragment.setArguments(bundle);
        ((HomeActivity) getActivity()).loadFragment(fragment, true, false);
    }

    @Override
    public void onItemClick(View view, Item item) {
        String itemJson = new Gson().toJson(item);
        Fragment fragment = new ItemInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ItemsFragment.ITEM_KEY, itemJson);
        bundle.putString(ItemsFragment.ITEM_CATEGORY, item.category);
        fragment.setArguments(bundle);
        ((HomeActivity) getActivity()).loadFragment(fragment, true, false);
    }
}
