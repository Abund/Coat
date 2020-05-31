package com.example.coat.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coat.R;
import com.example.coat.model.Chats;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder>{

    private static final int MSG_TYPE_LEFT=0;
    private static final int MSG_TYPE_RIGHT=1;

    Context context;
    List<Chats> chatsList;
    String imageUrl;

    FirebaseUser firebaseUser;

    public AdapterChat(Context context, List<Chats> chatsList, String imageUrl) {
        this.context = context;
        this.chatsList = chatsList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right,parent,false);
            return new MyHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left,parent,false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        String message = chatsList.get(position).getMessage();
        String timeStamp = chatsList.get(position).getTimeStamp();

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        holder.message.setText(message);
        holder.timeStamp.setText(dateTime);

        //click to show delete dialogue
        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are u sure to delete this message");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteMessage(position);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        if(position==chatsList.size()-1) {
            if(chatsList.get(position).isSeen()){
                holder.isSeen.setText("Seen");
            }else {
                holder.isSeen.setText("Delivered");
            }
        }else {
            holder.isSeen.setVisibility(View.GONE);
        }
    }

    private void deleteMessage(int position) {
        final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String messageTimeStamp = chatsList.get(position).getTimeStamp();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("chats");
        Query query = databaseReference.orderByChild("timeStamp").equalTo(messageTimeStamp);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    if(ds.child("sender").getValue().equals(myUid)){
                        ds.getRef().removeValue();
//                        HashMap<String,Object> hashMap= new HashMap<>();
//                        hashMap.put("message","this message was deleted");
//                        ds.getRef().updateChildren(hashMap);

                        Toast.makeText(context,"Message deleted ",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(context,"You can only delete your messages ",Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatsList.size();
    }

    @Override
    public int getItemViewType(int position){
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        if(chatsList.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView profilePic;
        TextView message,timeStamp,isSeen;
        LinearLayout messageLayout;

        public MyHolder(@NonNull View itemView){
            super(itemView);
            profilePic= itemView.findViewById(R.id.profi);
            message= itemView.findViewById(R.id.messageTv);
            timeStamp= itemView.findViewById(R.id.messageTime1);
            isSeen= itemView.findViewById(R.id.isSeen);
            messageLayout= itemView.findViewById(R.id.messageLayout);
        }
    }
}
