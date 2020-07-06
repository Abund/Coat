package com.example.coat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coat.adapter.AdapterPost;
import com.example.coat.model.Post;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PsycActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    String uid;

    //views from xml
    private LinearLayout timeLayout;
    private Button bookPsychologist,cancelBookPsychologist;
    ImageView avatarIv, coverIv;
    private TextView firstNamePro,sessionTime,lastNamePro,addressPro,userNamePro,emailAddressPro,workExperience,schoolExperience,achievement,skills;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_psyc);

        bookPsychologist = findViewById(R.id.bookPsychologist);
        cancelBookPsychologist = findViewById(R.id.cancelBookPsychologist);
        sessionTime = findViewById(R.id.sessionTime);
        timeLayout = findViewById(R.id.timeLayout);
        firstNamePro = findViewById(R.id.firstNamePro);
        lastNamePro =findViewById(R.id.lastNamePro);
        addressPro = findViewById(R.id.addressPro);
        userNamePro =findViewById(R.id.userNamePro);
        emailAddressPro = findViewById(R.id.emailAddressPro);
        avatarIv = findViewById(R.id.avatarIv);
        coverIv = findViewById(R.id.coverIv);
        workExperience= findViewById(R.id.workExperience);
        schoolExperience= findViewById(R.id.schoolExperience);
        achievement= findViewById(R.id.achievement);
        skills= findViewById(R.id.skills);
        firebaseAuth = FirebaseAuth.getInstance();

        //get uid of clicked user to retrieve his posts
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");

        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //check until required data get
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String firstName =""+ ds.child("firstName").getValue();
                    String lastName =""+ ds.child("lastName").getValue();
                    String address =""+ ds.child("address").getValue();
                    String UserName =""+ ds.child("userName").getValue();
                    String email =""+ ds.child("email").getValue();
                    String cover =""+ ds.child("image").getValue();
                    String image =""+ ds.child("cover").getValue();
                    String workExperience1 =""+ ds.child("workExperience").getValue();
                    String schoolExperience1 =""+ ds.child("school").getValue();
                    String achievement1 =""+ ds.child("achievement").getValue();
                    String skills1 =""+ ds.child("skills").getValue();

                    firstNamePro.setText(firstName);
                    lastNamePro.setText(lastName);
                    addressPro.setText(address);
                    userNamePro.setText(UserName);
                    emailAddressPro.setText(email);

                    workExperience.setText(workExperience1);
                    schoolExperience.setText(schoolExperience1);
                    achievement.setText(achievement1);
                    skills.setText(skills1);

                    try {
                        //if image is received then set
                        Picasso.get().load(image).into(avatarIv);
                    } catch (Exception e) {
                        //if there is any exception while getting image then set default
                        //Picasso.get().load(R.drawable.ic_default_img_white).into(avatarIv);
                    }

                    try {
                        //if image is received then set
                        Picasso.get().load(cover).into(coverIv);
                    } catch (Exception e) {
                        //if there is any exception while getting image then set default
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Query query1 = FirebaseDatabase.getInstance().getReference("Bookings").orderByChild("userId").equalTo(uid);
        query1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //check until required data get
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String time =""+ ds.child("time").getValue();
                    String userId =""+ ds.child("userId").getValue();
                    String psychologistId =""+ ds.child("psychologistId").getValue();
                    String status =""+ ds.child("status").getValue();

                    sessionTime.setText(time);
                    if(status.equalsIgnoreCase("booked")){
                        timeLayout.setVisibility(View.VISIBLE);
                        bookPsychologist.setVisibility(View.GONE);
                        cancelBookPsychologist.setVisibility(View.VISIBLE);
                    }else if(status.equalsIgnoreCase("cancelled")){
                        timeLayout.setVisibility(View.GONE);
                        bookPsychologist.setVisibility(View.VISIBLE);
                        cancelBookPsychologist.setVisibility(View.GONE);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        checkUserStatus();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.homescreen, menu);
        menu.findItem(R.id.action_add_post).setVisible(false); //hide add post from this activiyt
        menu.findItem(R.id.action_create_group).setVisible(false); //hide add post from this activiyt
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            firebaseAuth = FirebaseAuth.getInstance();
            LoginManager.getInstance().logOut();
            firebaseAuth.signOut();
            Intent at = new Intent(this, MainActivity.class);
            startActivity(at);
        }else if (id == R.id.action_add_post) {
            startActivity(new Intent(this, AddPostActivity.class));

        }

        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //user is signed in stay here
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
        }
        else {
            //user not signed in, go to main acitivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}