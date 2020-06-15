package com.example.coat.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coat.AddPostActivity;
import com.example.coat.R;
import com.example.coat.ThereProfileActivity;
import com.example.coat.model.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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

    String myUid;

    private DatabaseReference likesRef; //for likes database node
    private DatabaseReference postsRef; //reference of posts

    boolean mProcessLike=false;

    public AdapterPost(Context context, List<Post> postList) {
        this.context = context;
        this.postList=postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_post,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {

        final String uid= postList.get(position).getUid();
        String email= postList.get(position).getuEmail();
        String name= postList.get(position).getuName();
        String uDp= postList.get(position).getuDp();
        final String pid= postList.get(position).getPid();
        String pTitle= postList.get(position).getpTitle();
        String pDescription= postList.get(position).getpDescr();
        final String pImage= postList.get(position).getpImage();
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
            holder.pImageIv.setVisibility(View.VISIBLE);
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
                showMoreOptions(holder.moreBtn, uid, myUid, pid, pImage);
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

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, final String pId, final String pImage) {
        //creating popup menu currently having option Delete, we will add more options later
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        //show delete option in only post(s) of currently signed-in user
        if (uid.equals(myUid)){
            //add items in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }
        popupMenu.getMenu().add(Menu.NONE, 2, 0, "View Detail");

        //item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id==0){
                    //delete is clicked
                    beginDelete(pId, pImage);
                }
                else if (id==1){
                    //Edit is clicked
                    //start AddPostActivity with key "editPost" and the id of the post clicked
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);
                }
                else if (id==2){
                    //start PostDetailActivity
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId", pId); //will get detail of post using this id, its id of the post clicked
                    context.startActivity(intent);
                }
                return false;
            }
        });
        //show menu
        popupMenu.show();

    }

    private void beginDelete(String pId, String pImage) {
        //post can be with or without image

        if (pImage.equals("noImage")){
            //post is without image
            deleteWithoutImage(pId);
        }
        else {
            //post is with image
            deleteWithImage(pId, pImage);
        }
    }

    private void deleteWithoutImage(String pId) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ds.getRef().removeValue(); // remove values from firebase where pid matches
                }
                //deleted
                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deleteWithImage(final String pId, String pImage) {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        /*Steps:
          1) Delete Image using url
          2) Delete from database using post id*/

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image deleted, now delete database

                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pid").equalTo(pId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                    ds.getRef().removeValue(); // remove values from firebase where pid matches
                                }
                                //deleted
                                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed, can't go further
                        pd.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
