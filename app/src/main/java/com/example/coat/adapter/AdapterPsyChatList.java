package com.example.coat.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coat.MainActivity;
import com.example.coat.MessageActivity;
import com.example.coat.MessagePsyActivity;
import com.example.coat.R;
import com.example.coat.ThereProfileActivity;
import com.example.coat.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterPsyChatList extends RecyclerView.Adapter<AdapterPsyChatList.MyHolder> {


    Context context;
    List<User> userList; //get user info
    private HashMap<String, String> lastMessageMap;
    private String timeStamp;

    //constructor
    public AdapterPsyChatList(Context context, List<User> userList,String timeStamp) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
        this.timeStamp=timeStamp;
    }

    @NonNull
    @Override
    public AdapterPsyChatList.MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout row_chatlist.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, viewGroup, false);
        return new AdapterPsyChatList.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPsyChatList.MyHolder myHolder, final int i) {
        //get data
        final String hisUid = userList.get(i).getUid();
        String userImage = userList.get(i).getImageUrl();
        String userName = userList.get(i).getFirstName();
        String lastName = userList.get(i).getLastName();
        String lastMessage = lastMessageMap.get(hisUid);

        //set data
        myHolder.nameTv.setText(userName+" "+lastName);
        if (lastMessage==null || lastMessage.equals("default")){
            myHolder.lastMessageTv.setVisibility(View.GONE);
        }
        else {
            myHolder.lastMessageTv.setVisibility(View.VISIBLE);
            myHolder.lastMessageTv.setText(lastMessage);
        }
        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img).into(myHolder.profileIv);
        }
        catch (Exception e){
            Picasso.get().load(R.drawable.ic_default_img).into(myHolder.profileIv);
        }
        //set online status of other users in chatlist
        if (userList.get(i).getOnlineStatus().equals("online")){
            //online
            myHolder.onlineStatusIv.setImageResource(R.drawable.circle_online);
        }
        else {
            //offline
            myHolder.onlineStatusIv.setImageResource(R.drawable.circle_offline);
        }

        //handle click of user in chatlist
//        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //start chat activity with that user
//                Intent intent = new Intent(context, MessagePsyActivity.class);
//                intent.putExtra("hisUid", hisUid);
//                context.startActivity(intent);
//            }
//        });

        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(context,""+email,Toast.LENGTH_SHORT).show();

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0){
                            //start chat activity with that user
                            Intent intent = new Intent(context, MessagePsyActivity.class);
                            intent.putExtra("hisUid", hisUid);
                            context.startActivity(intent);
                        }
                        if(i==1){
                            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bookings");
                            Query query = databaseReference.orderByChild("timeStamp").equalTo(timeStamp);
                            query.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for(DataSnapshot ds: dataSnapshot.getChildren()){
                                        if(ds.child("userId").getValue().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                            HashMap<String,Object> hashMap= new HashMap<>();
                                            hashMap.put("status", "deleted");
                                            ds.getRef().updateChildren(hashMap);
                                        }else{
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                });
                builder.create().show();
            }
        });



    }



    public void setLastMessageMap(String userId, String lastMessage){
        lastMessageMap.put(userId, lastMessage);
    }

    @Override
    public int getItemCount() {
        return userList.size(); //size of the list
    }


    class MyHolder extends RecyclerView.ViewHolder{
        //views of row_chatlist.xml
        ImageView profileIv, onlineStatusIv;
        TextView nameTv, lastMessageTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            profileIv = itemView.findViewById(R.id.profileIv);
            onlineStatusIv = itemView.findViewById(R.id.onlineStatusIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
        }
    }
}
