package com.example.coat.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.coat.GroupCreateActivity;
import com.example.coat.MainActivity;
import com.example.coat.R;
import com.example.coat.SettingsActivity;
import com.example.coat.adapter.AdapterChatList;
import com.example.coat.adapter.AdapterPsyChatList;
import com.example.coat.adapter.AdapterUser;
import com.example.coat.model.BookingSession;
import com.example.coat.model.ChatList;
import com.example.coat.model.Chats;
import com.example.coat.model.Psychologist;
import com.example.coat.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PsychologistFragmnet extends Fragment {

    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ChatList> chatlistList;
    List<User> userList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    AdapterPsyChatList adapterChatlist;
    String timeStamp;
    private ArrayList<String> bloodPressureKey;

    public PsychologistFragmnet() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=  inflater.inflate(R.layout.fragment_psychologist_fragmnet, container, false);


        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        bloodPressureKey= new ArrayList<>();

        recyclerView = view.findViewById(R.id.PsyRecyclerView);

        chatlistList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatlistList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ChatList chatlist = ds.getValue(ChatList.class);
                    chatlistList.add(chatlist);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return view;
    }

    private void loadChats() {
        userList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Psychologist");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    final User user = ds.getValue(User.class);
                    for (ChatList chatlist: chatlistList){
                        if (user.getUid() != null && user.getUid().equals(chatlist.getId())){
                            //userList.add(user);
                            DatabaseReference reference1= FirebaseDatabase.getInstance().getReference("Bookings");
                            reference1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    userList.clear();
                                    for(DataSnapshot ds:dataSnapshot.getChildren()){
                                        //User user = new User();
                                        BookingSession bookingSession = ds.getValue(BookingSession.class);
                                        bloodPressureKey.add(ds.getKey());
                                        //userList.add(user);
                                        if(bookingSession.getStatus().equals("chatting")){
                                            if(bookingSession.getPsychologistId().equals(user.getUid())&&bookingSession.getUserId().equals(currentUser.getUid())){
                                                userList.add(user);
                                                timeStamp=bookingSession.getTimeStamp();
                                            }
                                        }

                                    }
                                    //adapter
                                    adapterChatlist = new AdapterPsyChatList(getContext(), userList,timeStamp);
                                    new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
                                    //setAdapter
                                    recyclerView.setAdapter(adapterChatlist);
                                    recyclerView.invalidate();
                                    //set last message
                                    for (int i=0; i<userList.size(); i++){
                                        lastMessage(userList.get(i).getUid());
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            break;
                        }

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void lastMessage(final String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String theLastMessage = "default";
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    com.example.coat.model.Chats chat = ds.getValue(Chats.class);
                    if (chat==null){
                        continue;
                    }
                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    if (sender == null || receiver == null){
                        continue;
                    }
                    if (chat.getReceiver().equals(currentUser.getUid()) &&
                            chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) &&
                                    chat.getSender().equals(currentUser.getUid())){
                        //instead of displaying url in message show "sent photo"
                        if (chat.getType().equals("image")){
                            theLastMessage = "Sent a photo";
                        }
                        else {
                            theLastMessage = chat.getMessage();
                        }
                    }
                }
                adapterChatlist.setLastMessageMap(userId, theLastMessage);
                adapterChatlist.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
        } else {
            //user not signed in, go to main acitivity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    private void searchUser(final String query) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Psychologist");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    final User user = ds.getValue(User.class);

                    for (ChatList chatlist: chatlistList){
                        if (user.getUid() != null && user.getUid().equals(chatlist.getId())){
                            //userList.add(user);
                            DatabaseReference reference1= FirebaseDatabase.getInstance().getReference("Bookings");
                            reference1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    //userList.clear();
                                    for(DataSnapshot ds:dataSnapshot.getChildren()){
                                        //User user = new User();
                                        BookingSession bookingSession = ds.getValue(BookingSession.class);
                                        bloodPressureKey.add(ds.getKey());
                                        //userList.add(user);
                                        if(bookingSession.getPsychologistId().equals(user.getUid())){
                                            if(bookingSession.getStatus().equals("chatting")){

                                                if(user.getFirstName().toLowerCase().contains(query.toLowerCase())||
                                                        user.getLastName().toLowerCase().contains(query.toLowerCase())||
                                                        user.getEmail().toLowerCase().contains(query.toLowerCase())){

                                                    userList.add(user);
                                                    timeStamp=bookingSession.getTimeStamp();
                                                }

                                            }else {

                                            }

                                        }
                                    }
                                    //adapter
                                    adapterChatlist = new AdapterPsyChatList(getContext(), userList,timeStamp);
                                    new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
                                    //setAdapter
                                    recyclerView.setAdapter(adapterChatlist);
                                    recyclerView.invalidate();
                                    //set last message
                                    for (int i=0; i<userList.size(); i++){
                                        lastMessage(userList.get(i).getUid());
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            break;
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*inflate options menu*/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflating menu
        inflater.inflate(R.menu.homescreen, menu);
        super.onCreateOptionsMenu(menu,inflater);
        //hide addpost icon from this fragment
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);

        MenuItem item = menu.findItem(R.id.action_search);

        SearchView searchView= (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!TextUtils.isEmpty(query.trim())){
                    searchUser(query);
                }else {
                    loadChats();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText.trim())){
                    searchUser(newText);
                }else {
                    loadChats();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    /*handle menu item clicks*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //get item id
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        else if (id==R.id.action_settings){
            //go to settings activity
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }
        else if (id==R.id.action_create_group){
            //go to GroupCreateActivity activity
            startActivity(new Intent(getActivity(), GroupCreateActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    ItemTouchHelper.SimpleCallback simpleCallback= new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT|ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {

            DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("Bookings")
                    .child(bloodPressureKey.get(viewHolder.getAdapterPosition()));

            Log.e("eee2",""+viewHolder.getAdapterPosition());
            Log.e("eee2",""+bloodPressureKey.get(viewHolder.getAdapterPosition()));
            HashMap<String,Object> hashMap= new HashMap<>();
            hashMap.put("status", "deleted");
            databaseReference.getRef().updateChildren(hashMap);
            loadChats();
        }
    };

}