package com.example.coat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coat.MainActivity;
import com.example.coat.R;
import com.example.coat.SettingsActivity;
import com.example.coat.adapter.AdapterPsychologist;
import com.example.coat.adapter.AdapterUser;
import com.example.coat.model.Psychologist;
import com.example.coat.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewPsychologist extends Fragment {

    RecyclerView recyclerView;
    AdapterPsychologist adapterPsychologist;
    List<Psychologist> psychologistList;
    FirebaseAuth firebaseAuth;

    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater , ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_viewpsychologist, container, false);
        super.onCreate(savedInstanceState);

        recyclerView= view.findViewById(R.id.psychologist_recyclerView);
        firebaseAuth =FirebaseAuth.getInstance();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        psychologistList= new ArrayList<>();
        getAllUsers();
        return view;
    }

    private void searchUser(final String query) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Psychologist");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                psychologistList.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    Psychologist user = ds.getValue(Psychologist.class);


                    if(!user.getUid().equals(firebaseUser.getUid())){

                        if(user.getFirstName().toLowerCase().contains(query.toLowerCase())||
                                user.getLastName().toLowerCase().contains(query.toLowerCase())||
                                user.getEmail().toLowerCase().contains(query.toLowerCase())){

                            psychologistList.add(user);
                        }

                    }

                    adapterPsychologist = new AdapterPsychologist(getActivity(),psychologistList);
                    adapterPsychologist.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterPsychologist);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void getAllUsers() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Psychologist");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                psychologistList.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    Psychologist user = new Psychologist();
                    user = ds.getValue(Psychologist.class);
                    if(!user.getUid().equals(firebaseUser.getUid())){
                        psychologistList.add(user);
                    }

                    adapterPsychologist = new AdapterPsychologist(getActivity(),psychologistList);
                    recyclerView.setAdapter(adapterPsychologist);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstances){
        setHasOptionsMenu(true);
        super.onCreate(savedInstances);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.homescreen, menu);

        menu.findItem(R.id.action_add_post).setVisible(false);
        MenuItem item = menu.findItem(R.id.action_search);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        SearchView searchView= (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!TextUtils.isEmpty(query.trim())){
                    searchUser(query);
                }else {
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText.trim())){
                    searchUser(newText);
                }else {
                    getAllUsers();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu,inflater);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.signOut();
            checkUserStatus();
        }else if (id==R.id.actionSetting){
            //go to settings activity
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
