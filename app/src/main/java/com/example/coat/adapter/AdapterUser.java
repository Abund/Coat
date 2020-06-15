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
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.MyHolder>{

    Context context;
    List<User> userList;

    public  AdapterUser(Context context,List<User> userList){
        this.context=context;
        this.userList=userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType ) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_user,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
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
                            Intent intent= new Intent(context, MessageActivity.class);
                            intent.putExtra("hisUid",hisUid);
                            context.startActivity(intent);
                        }
                    }
                });
                builder.create().show();
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
