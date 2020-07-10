package com.example.coat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.coat.adapter.AdapterPost;
import com.example.coat.fragments.DatePickerFragment;
import com.example.coat.fragments.TimePickerFragment;
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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PsycActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    FirebaseAuth firebaseAuth;
    String uid;
    private String timeStamp;

    //views from xml
    private LinearLayout timeLayout,bookingsTabs;
    private Button bookPsychologist,cancelBookPsychologist,chatPsychologist;
    ImageView avatarIv, coverIv;
    private TextView firstNamePro,sessionTime,lastNamePro,addressPro,userNamePro,emailAddressPro,workExperience,schoolExperience,achievement,skills,medicationTime,dateMedi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_psyc);

        bookPsychologist = findViewById(R.id.bookPsychologist);
        cancelBookPsychologist = findViewById(R.id.cancelBookPsychologist);
        bookingsTabs = findViewById(R.id.bookingsTabs);
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
        medicationTime= findViewById(R.id.medicationTime);
        dateMedi= findViewById(R.id.dateMedi);
        chatPsychologist= findViewById(R.id.chatPsychologist);
        firebaseAuth = FirebaseAuth.getInstance();

        //get uid of clicked user to retrieve his posts
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");

        dateMedi.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(),"date picker");
            }
        });
        medicationTime.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(),"time picker");
            }
        });

        Query query = FirebaseDatabase.getInstance().getReference("Psychologist").orderByChild("uid").equalTo(uid);
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
                    String achievement1 =""+ ds.child("achievements").getValue();
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

        final FirebaseUser firebaseUser1 = FirebaseAuth.getInstance().getCurrentUser();
        Query query1 = FirebaseDatabase.getInstance().getReference("Bookings").orderByChild("userId").equalTo(firebaseUser1.getUid());
        query1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //check until required data get
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String time =""+ ds.child("time").getValue();
                    String userId =""+ ds.child("userId").getValue();
                    String psychologistId =""+ ds.child("psychologistId").getValue();
                    String status =""+ ds.child("status").getValue();
                    timeStamp=""+ ds.child("timeStamp").getValue();
                    sessionTime.setText(time);
                    if(status.equalsIgnoreCase("booked")){
                        timeLayout.setVisibility(View.VISIBLE);
                        cancelBookPsychologist.setVisibility(View.VISIBLE);
                        bookPsychologist.setVisibility(View.GONE);

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

        bookPsychologist.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                String medicationN= medicationTime.getText().toString();
                String startD = dateMedi.getText().toString();

                if(startD.isEmpty()){
                    dateMedi.setError("Please enter date");
                    dateMedi.requestFocus();
                    return;
                }
                else if(medicationN.isEmpty()){
                    medicationTime.setError("Please enter the start time");
                    medicationTime.requestFocus();
                    return;
                }

                String timestamp = ""+System.currentTimeMillis();
                String realTime=dateMedi.getText()+" "+medicationTime.getText();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                //setup required data
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("time", realTime);
                hashMap.put("userId", firebaseAuth.getCurrentUser().getUid());
                hashMap.put("timeStamp", timestamp);
                hashMap.put("psychologistId", uid);
                hashMap.put("status", "Booked");
                //put this data to firebase
                databaseReference.child("Bookings").push().setValue(hashMap);

            }
        });

        cancelBookPsychologist.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bookings");
                Query query = databaseReference.orderByChild("timeStamp").equalTo(timeStamp);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            if(ds.child("userId").getValue().equals(firebaseAuth.getCurrentUser().getUid())){
                                ds.getRef().removeValue();
                                timeLayout.setVisibility(View.GONE);
                                bookPsychologist.setVisibility(View.VISIBLE);
                                cancelBookPsychologist.setVisibility(View.GONE);
                                medicationTime.setText("");
                                dateMedi.setText("");
                                Toast.makeText(PsycActivity.this,"Booking was deleted ",Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(PsycActivity.this,"You can only delete your booking ",Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });


        chatPsychologist.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(PsycActivity.this, MessageActivity.class);
                intent.putExtra("hisUid", uid);
                startActivity(intent);
            }
        });
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

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c =Calendar.getInstance();
        c.set(Calendar.YEAR,year);
        c.set(Calendar.MONTH,month);
        //c.set(Calendar.DAY_OF_MONTH,i2);
        String currentDatePicker = DateFormat.getDateInstance(DateFormat.DATE_FIELD).format(c.getTime());
        dateMedi.setText(currentDatePicker);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        //String currentDatePicker =DateFormat.getDateInstance(DateFormat.DATE_FIELD).format(c.getTime());
        medicationTime.setText(""+hourOfDay+":"+minute);
    }
}