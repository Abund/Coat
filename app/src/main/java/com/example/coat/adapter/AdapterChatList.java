package com.example.coat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coat.MessageActivity;
import com.example.coat.R;
import com.example.coat.model.User;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.MyHolder> {


    Context context;
    List<User> userList; //get user info
    private HashMap<String, String> lastMessageMap;

    private byte encryptionKey[]={9,115,51,86,105,4,-31,-23,-68,88,17,20,3,-105,119,-53};
    private Cipher cipher,decipher;
    private SecretKeySpec secretKeySpec;

    //constructor
    public AdapterChatList(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();

        try {
            cipher =Cipher.getInstance("AES");
            decipher=Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        secretKeySpec = new SecretKeySpec(encryptionKey,"AES");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout row_chatlist.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, viewGroup, false);
        return new MyHolder(view);
    }

    private String AESDecryption(String string) throws UnsupportedEncodingException {
        byte[] encryptedByte=string.getBytes("ISO-8859-1");
        String decryptedString =string;
        byte[] decryption;
        try {
            decipher.init(Cipher.DECRYPT_MODE,secretKeySpec);
            decryption=decipher.doFinal(encryptedByte);
            decryptedString= new String(decryption);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return decryptedString;
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, final int i) {
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
            try {
                myHolder.lastMessageTv.setText(AESDecryption(lastMessage));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
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
        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start chat activity with that user
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("hisUid", hisUid);
                context.startActivity(intent);
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
