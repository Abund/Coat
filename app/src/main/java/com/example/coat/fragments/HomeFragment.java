package com.example.coat.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.coat.MainActivity;
import com.example.coat.R;
import com.example.coat.adapter.AdapterPost;
import com.example.coat.model.Post;
import com.example.coat.notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseUser userf;
    DatabaseReference myRef;
    private  static final String TAG="HomeScreenActivity";
    FirebaseUser user;

    RecyclerView recyclerView;
    List<Post> postList;
    AdapterPost adapterPost;

    View view;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.postRecyclerView1);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        postList= new ArrayList<>();
        loadPost();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference=database.getReference("users");
        databaseReference.keepSynced(true);

        firebaseAuth =FirebaseAuth.getInstance();
        userf=firebaseAuth.getCurrentUser();

        user = FirebaseAuth.getInstance().getCurrentUser();

        checkUserStatus();
        //update token
        updateToken(FirebaseInstanceId.getInstance().getToken());
        return view;
    }

    private void loadPost() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Post");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    Post post = ds.getValue(Post.class);
                    postList.add(post);

                    adapterPost = new AdapterPost(getActivity(),postList);
                    recyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(),""+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateToken(String token){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        databaseReference.child(userf.getUid()).setValue(mToken);
    }

    private void checkUserStatus(){
        FirebaseUser user= firebaseAuth.getCurrentUser();
        if(user !=null){
        }else{
            Intent at = new Intent(getActivity(), MainActivity.class);
            startActivity(at);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume(){
        checkUserStatus();
        super.onResume();
    }
}
