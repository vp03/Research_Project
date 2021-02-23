package com.pentakotavishu.researchproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import androidx.annotation.NonNull;


import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * A fragment representing a list of Items.
 */
public class Refresh_Fragment extends Fragment {

    public RecyclerView recyclerView;
    public RecyclerView.Adapter mAdapter;
    public RecyclerView.LayoutManager layoutManager;
    public List<String> input;
    public SwipeRefreshLayout mySwipeRefreshLayout;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public Refresh_Fragment(List<String> random) {
        input = random;
    }

    public Refresh_Fragment() {
    }

    public List<String> getInput() {
        return input;
    }

    public void setInput(List<String> input) {
        this.input = input;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_refresh_, container, false);
        recyclerView = view.findViewById(R.id.my_recycler_view);
        mySwipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        setUp();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        // EditText etFoo = (EditText) view.findViewById(R.id.etFoo);
    }

    private void setUp() {

        // define an adapter
        //includesForDownloadFiles();
        mAdapter = new com.pentakotavishu.researchproject.MyItemRecyclerViewAdapter(input);
        recyclerView.setAdapter(mAdapter);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder
                            target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        input.remove(viewHolder.getAdapterPosition());
                        mAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                    }
                };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        doYourUpdate();
                    }
                }
        );
    }

    private void doYourUpdate() {
        setUp();
        System.out.println("refreshed");
        mySwipeRefreshLayout.setRefreshing(false); // Disables the refresh icon
    }

    public void includesForDownloadFiles() throws IOException {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // [START download_create_reference]
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();


        // [START download_to_memory]
        StorageReference islandRef = storageRef.child("audio/1612465874583.3gp");

        final long ONE_MEGABYTE = 1024 * 1024;
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }
}
