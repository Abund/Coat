package com.example.coat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coat.adapter.AdapterChat;
import com.example.coat.model.Chats;
import com.example.coat.model.User;
import com.example.coat.notifications.APIService;
import com.example.coat.notifications.Client;
import com.example.coat.notifications.Data;
import com.example.coat.notifications.Response;
import com.example.coat.notifications.Sender;
import com.example.coat.notifications.Token;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class MessageActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView imageView;
    TextView nameTv,userStatusTv;
    EditText messageEt;
    ImageButton imageButton;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    ValueEventListener valueEventListener;
    DatabaseReference getDatabaseReference;

    List<Chats> chatsList;
    AdapterChat adapterChat;

    String hisUid;
    String myUid;
    String hisImage;

    APIService apiService;
    boolean notify =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView = findViewById(R.id.chat_recycler);
        imageView= findViewById(R.id.profileIv);
        nameTv= findViewById(R.id.nameChatTv);
        userStatusTv= findViewById(R.id.userStatusTv);
        messageEt= findViewById(R.id.messageEt);
        imageButton= findViewById(R.id.sendBtn);

        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        Intent intent = getIntent();
        hisUid= intent.getStringExtra("hisUid");

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase= FirebaseDatabase.getInstance();
        databaseReference= firebaseDatabase.getReference("users");

        Query query = databaseReference.orderByChild("uid").equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    String UserName =""+ ds.child("firstName").getValue();
                    String email =""+ ds.child("email").getValue();
                    String onlineStatus=""+ ds.child("onlineStatus").getValue();
                    String typingStatus =""+ ds.child("typingTo").getValue();
                    if(typingStatus.equals(myUid)){
                        userStatusTv.setText("typing...");
                    }else {
                        if(onlineStatus.equalsIgnoreCase("online")){
                            userStatusTv.setText(onlineStatus);
                        }else{
                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                            calendar.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();
                            userStatusTv.setText("Last seen at: "+dateTime);
                        }
                    }
                    nameTv.setText(UserName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        imageButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                notify=true;
                String message = messageEt.getText().toString().trim();
                if(TextUtils.isEmpty(message)){
                    Toast.makeText(MessageActivity.this,"Cannot send empty messsage",Toast.LENGTH_SHORT).show();
                }else{
                    sendMessage(message);
                }
                messageEt.setText("");
            }
        });

        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length()==0){
                    checkTypingStatus("noOne");
                }else{
                    checkTypingStatus(hisUid);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        readMessage();
        seenMessage();
    }

    private void seenMessage() {
        getDatabaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        valueEventListener = getDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Chats chats = ds.getValue(Chats.class);
                    if(chats.getReceiver().equals(myUid)&& chats.getSender().equals(hisUid)){
                        HashMap<String,Object> hasSeenHashMap= new HashMap<>();
                        hasSeenHashMap.put("isSeen",true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    protected  void onStart(){
        checkUserStatus();
        //set online
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause(){
        super.onPause();
        //get time stamp
        String timeStamp= String.valueOf(System.currentTimeMillis());
        //set offline with last seen time stamp
        checkOnlineStatus("timeStamp");
        checkTypingStatus("noOne");
        getDatabaseReference.removeEventListener(valueEventListener);
    }

    @Override
    protected void onResume(){
        //set online
        checkOnlineStatus("online");
        super.onResume();
    }

    private void readMessage() {
        chatsList = new ArrayList<>();
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatsList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Chats chats = ds.getValue(Chats.class);
                    if(chats.getReceiver().equals(myUid)&&chats.getSender().equals(hisUid)||
                            chats.getReceiver().equals(hisUid)&&chats.getSender().equals(myUid)){
                        chatsList.add(chats);
                    }
                    adapterChat = new AdapterChat(MessageActivity.this,chatsList,hisImage);
                    adapterChat.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(final String message) {
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();

        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String,Object> hashMap= new HashMap<>();
        hashMap.put("sender",myUid);
        hashMap.put("receiver",hisUid);
        hashMap.put("message",message);
        hashMap.put("timeStamp",timeStamp);
        hashMap.put("isSeen",false);
        databaseReference.child("Chats").push().setValue(hashMap);
//        messageEt.setText("");
//
        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("users").child(myUid);
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(notify){
                    sendNotification(hisUid,user.getFirstName(),message);
                }
                notify=false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendNotification(final String hisUid, final String firstName, final String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Token token =ds.getValue(Token.class);
                    Data data = new Data(myUid,firstName+":"+message,"New Message",hisUid,R.drawable.ic_account_circle_black_24dp);

                    Sender sender = new Sender(data,token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    Toast.makeText(MessageActivity.this,""+response.message(),Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkOnlineStatus(String status){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(myUid);
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        databaseReference.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(myUid);
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        databaseReference.updateChildren(hashMap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.homescreen,menu);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        checkUserStatus();
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus(){
        FirebaseUser user= firebaseAuth.getCurrentUser();
        if(user !=null){
            myUid= user.getUid();
        }else{
            Intent at = new Intent(MessageActivity.this, MainActivity.class);
            startActivity(at);
            finish();
        }
    }
}
