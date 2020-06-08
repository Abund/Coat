package com.example.coat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coat.R;
import com.example.coat.ThereProfileActivity;
import com.example.coat.model.Post;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import android.text.format.DateFormat;
import android.widget.Toast;

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.MyHolder>{

    Context context;
    List<Post> postList;

    public AdapterPost(Context context, List<Post> postList) {
        this.context = context;
        this.postList=postList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_post,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        final String uid= postList.get(position).getUid();
        String email= postList.get(position).getuEmail();
        String name= postList.get(position).getuName();
        String uDp= postList.get(position).getuDp();
        String pid= postList.get(position).getPid();
        String pTitle= postList.get(position).getpTitle();
        String pDescription= postList.get(position).getpDescr();
        String pImage= postList.get(position).getpImage();
        String pTimeStamp= postList.get(position).getpTime();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String time = DateFormat.format("dd/MM/yyyy hh:mm:aa",calendar).toString();

        holder.uNameTv.setText(name);
        holder.pTimeTv.setText(time);
        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescription);

        if(pImage.equals("noImage")){
            holder.pImageIv.setVisibility(View.GONE);
        }else{
            try{
                Picasso.get().load(pImage)
                        .into(holder.pImageIv);
            }catch (Exception e){

            }
        }

        try{
            Picasso.get().load(uDp)
                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                    .into(holder.uPictureIv);
        }catch (Exception e){

        }





        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"More ",Toast.LENGTH_SHORT).show();
            }
        });

        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"Like ",Toast.LENGTH_SHORT).show();
            }
        });

        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"Comment ",Toast.LENGTH_SHORT).show();
            }
        });

        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"share",Toast.LENGTH_SHORT).show();
            }
        });

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid",uid);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView uPictureIv,pImageIv;
        TextView uNameTv,pTimeTv,pTitleTv,pDescriptionTv,pLikes;
        ImageButton moreBtn;
        Button likeBtn,commentBtn,shareBtn;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            uPictureIv= itemView.findViewById(R.id.uPictureIv);
            pImageIv= itemView.findViewById(R.id.pImageTv);
            uNameTv= itemView.findViewById(R.id.uNameTv);
            pTimeTv= itemView.findViewById(R.id.pTimeTv);
            pTitleTv= itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv= itemView.findViewById(R.id.pDescriptionTv);
            pLikes= itemView.findViewById(R.id.pLikeTv);
            moreBtn= itemView.findViewById(R.id.moreButton);
            likeBtn= itemView.findViewById(R.id.likeBtn);
            commentBtn= itemView.findViewById(R.id.commentBtn);
            shareBtn= itemView.findViewById(R.id.shareBtn);
            profileLayout= itemView.findViewById(R.id.postLayout);
        }
    }
}
