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
import com.example.coat.PsycActivity;
import com.example.coat.R;
import com.example.coat.ThereProfileActivity;
import com.example.coat.model.Psychologist;
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

public class AdapterPsychologist extends RecyclerView.Adapter<AdapterPsychologist.MyHolder>{

    Context context;
    List<Psychologist> userList;

    FirebaseAuth firebaseAuth;
    String myUid;

    public  AdapterPsychologist(Context context,List<Psychologist> userList){
        this.context=context;
        this.userList=userList;

        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public AdapterPsychologist.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType ) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_psy,parent,false);
        return new AdapterPsychologist.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPsychologist.MyHolder holder, final int position) {
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
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(context,""+email,Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(context, PsycActivity.class);
                intent.putExtra("uid",hisUid);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView mAvatar;
        TextView nNameTv,mEmailTv;

        public MyHolder(@NonNull View itemView){
            super(itemView);
            mAvatar= itemView.findViewById(R.id.avatarIV);
            nNameTv= itemView.findViewById(R.id.nameTv);
            mEmailTv= itemView.findViewById(R.id.emailTv);

        }
    }
}
