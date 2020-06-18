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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coat.MessageActivity;
import com.example.coat.R;
import com.example.coat.ThereProfileActivity;
import com.example.coat.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.MyHolder>{

    Context context;
    List<User> userList;

    FirebaseAuth firebaseAuth;
    String myUid;

    public  AdapterUser(Context context,List<User> userList){
        this.context=context;
        this.userList=userList;

        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType ) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_user,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        final String hisUid = userList.get(position).getUid();
        String firstName= userList.get(position).getFirstName();
        String lastName= userList.get(position).getLastName();
        final String email= userList.get(position).getEmail();
        String image= userList.get(position).getImageUrl();

        holder.nNameTv.setText(firstName+" "+lastName);
        holder.mEmailTv.setText(email);
        try{
            Picasso.get().load(image)
                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                    .into(holder.mAvatar);
        }catch (Exception e){

        }


        holder.blockIv.setImageResource(R.drawable.ic_unblock);
        //check if each user if is blocked or not
        checkIsBlocked(hisUid, holder, position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(context,""+email,Toast.LENGTH_SHORT).show();

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0){
                            Intent intent = new Intent(context, ThereProfileActivity.class);
                            intent.putExtra("uid",hisUid);
                            context.startActivity(intent);
                        }
                        if(i==1){
//                            Intent intent= new Intent(context, MessageActivity.class);
//                            intent.putExtra("hisUid",hisUid);
//                            context.startActivity(intent);
                            imBlockedORNot(hisUid);
                        }
                    }
                });
                builder.create().show();
            }
        });

        //click to block unblock user
        holder.blockIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userList.get(position).isBlocked()){
                    unBlockUser(hisUid);
                }
                else {
                    blockUser(hisUid);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    private void imBlockedORNot(final String hisUID){
        //first check if sender(current user) is blocked by receiver or not
        //Logic: if uid of the sender(current user) exists in "BlockedUsers" of receiver then sender(current user) is blocked, otherwise not
        //if blocked then just display a message e.g. You're blocked by that user, can't send message
        //if not blocked then simply start the chat activity
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUID).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            if (ds.exists()){
                                Toast.makeText(context, "You're blocked by that user, can't send message", Toast.LENGTH_SHORT).show();
                                //bocked, dont proceed further
                                return;
                            }
                        }
                        //not blocked, start activity
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("hisUid", hisUID);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void checkIsBlocked(String hisUID, final MyHolder myHolder, final int i) {
        //check each user, if blocked or not
        //if uid of the user exists in "BlockedUsers" then that user is blocked, otherwise not

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            if (ds.exists()){
                                myHolder.blockIv.setImageResource(R.drawable.ic_block_black_24dp);
                                userList.get(i).setBlocked(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void blockUser(String hisUID) {
        //block the user, by adding uid to current user's "BlockedUsers" node


        //put values in hasmap to put in db
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUID);

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hisUID).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //blocked successfully
                        Toast.makeText(context, "Blocked Successfully...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to block
                        Toast.makeText(context, "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void unBlockUser(String hisUID) {
        //unblock the user, by removing uid from current user's "BlockedUsers" node

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            if (ds.exists()){
                                //remove blocked user data from current user's BlockedUsers list
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //unblocked successfully
                                                Toast.makeText(context, "Unbloked Successfully...", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed to unblock
                                                Toast.makeText(context, "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView mAvatar,blockIv;
        TextView nNameTv,mEmailTv;

        public MyHolder(@NonNull View itemView){
            super(itemView);
            mAvatar= itemView.findViewById(R.id.avatarIV);
            nNameTv= itemView.findViewById(R.id.nameTv);
            mEmailTv= itemView.findViewById(R.id.emailTv);
            blockIv = itemView.findViewById(R.id.blockIv);
        }
    }

}
