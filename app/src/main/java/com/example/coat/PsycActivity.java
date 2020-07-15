package com.example.coat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class PsycActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    FirebaseAuth firebaseAuth;
    String uid;
    private String timeStamp;
    String realTime;
    String timestamp;
    String keys;

    //views from xml
    private LinearLayout timeLayout,bookingsTabs;
    private Button bookPsychologist,cancelBookPsychologist,chatPsychologist;
    ImageView avatarIv, coverIv;
    private TextView firstNamePro,timeri,sessionTime,lastNamePro,addressPro,userNamePro,emailAddressPro,workExperience,schoolExperience,achievement,skills,medicationTime,dateMedi;

    //private String EVENT_DATE_TIME = "2020-12-31 10:30:00";
    private String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private LinearLayout  linear_layout_2;
    private TextView tv_days, tv_hour, tv_minute, tv_second;
    private Handler handler = new Handler();
    private Runnable runnable;
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
        timeri=findViewById(R.id.timeri);
        medicationTime= findViewById(R.id.medicationTime);
        dateMedi= findViewById(R.id.dateMedi);
        chatPsychologist= findViewById(R.id.chatPsychologist);
        firebaseAuth = FirebaseAuth.getInstance();

        //get uid of clicked user to retrieve his posts
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");

        System.out.println("hdhdh"+uid);

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
                        chatPsychologist.setVisibility(View.GONE);
                    }else if(status.equalsIgnoreCase("chatting")){
                        timeLayout.setVisibility(View.VISIBLE);
                        cancelBookPsychologist.setVisibility(View.GONE);
                        bookPsychologist.setVisibility(View.GONE);
                        chatPsychologist.setVisibility(View.VISIBLE);
                    }else{
                        timeLayout.setVisibility(View.GONE);
                        bookPsychologist.setVisibility(View.VISIBLE);
                        cancelBookPsychologist.setVisibility(View.GONE);
                        bookingsTabs.setVisibility(View.VISIBLE);
                        chatPsychologist.setVisibility(View.GONE);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final FirebaseUser firebaseUser2 = FirebaseAuth.getInstance().getCurrentUser();
        Query query2 = FirebaseDatabase.getInstance().getReference("Bookings").orderByChild("userId").equalTo(firebaseUser2.getUid());
        query2.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //check until required data get
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String time =""+ ds.child("time").getValue();
                    String userId =""+ ds.child("userId").getValue();
                    String psychologistId =""+ ds.child("psychologistId").getValue();
                    String status =""+ ds.child("status").getValue();
                    timeStamp=""+ ds.child("timeStamp").getValue();
                    long diff=0;
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        LocalDateTime dateTime = LocalDateTime.parse(time, formatter);
                        LocalDateTime dateTime2 = dateTime.plusDays(1);

                        LocalDateTime now = LocalDateTime.now();
                        if(now.isAfter(dateTime2)){
                            HashMap<String,Object> hashMap= new HashMap<>();
                            hashMap.put("status", "timeOut");
                            ds.getRef().updateChildren(hashMap);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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

                 timestamp = ""+System.currentTimeMillis();
                realTime=dateMedi.getText()+" "+medicationTime.getText();
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
                 keys=databaseReference.child("Bookings").push().getKey();

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
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bookings");
                Query query = databaseReference.orderByChild("timeStamp").equalTo(timeStamp);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            if(ds.child("userId").getValue().equals(firebaseAuth.getCurrentUser().getUid())){
                                HashMap<String,Object> hashMap= new HashMap<>();
                                hashMap.put("status", "chatting");
                                ds.getRef().updateChildren(hashMap);
                            }else{
                            }

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                Intent intent = new Intent(PsycActivity.this, MessagePsyActivity.class);
                intent.putExtra("hisUid", uid);
                startActivity(intent);
            }
        });

        initUI();
        countDownStart();
    }

    private void initUI() {
        //linear_layout_1 = findViewById(R.id.linear_layout_1);
        linear_layout_2 = findViewById(R.id.linear_layout_2);
        tv_days = findViewById(R.id.tv_days);
        tv_hour = findViewById(R.id.tv_hour);
        tv_minute = findViewById(R.id.tv_minute);
        tv_second = findViewById(R.id.tv_second);
    }

    private void countDownStart() {
        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    handler.postDelayed(this, 1000);
                    SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
                    Date event_date = dateFormat.parse(realTime);
                    Date current_date = new Date();
                    if (!current_date.after(event_date)) {
                        long diff = event_date.getTime() - current_date.getTime();
                        long Days = diff / (24 * 60 * 60 * 1000);
                        long Hours = diff / (60 * 60 * 1000) % 24;
                        long Minutes = diff / (60 * 1000) % 60;
                        long Seconds = diff / 1000 % 60;
                        //
                        tv_days.setText(String.format("%02d", Days));
                        tv_hour.setText(String.format("%02d", Hours));
                        tv_minute.setText(String.format("%02d", Minutes));
                        tv_second.setText(String.format("%02d", Seconds));
                    } else {
                        //linear_layout_1.setVisibility(View.VISIBLE);
                        linear_layout_2.setVisibility(View.GONE);
                        handler.removeCallbacks(runnable);
                        chatPsychologist.setVisibility(View.VISIBLE);
                        cancelBookPsychologist.setVisibility(View.GONE);
                        timeri.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 0);
    }

    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
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
        c.set(Calendar.DAY_OF_MONTH,dayOfMonth);

        Date date=c.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate=dateFormat.format(date);

        String currentDatePicker = DateFormat.getDateInstance(DateFormat.DATE_FIELD).format(c.getTime());
        dateMedi.setText(formattedDate);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        //String currentDatePicker =DateFormat.getDateInstance(DateFormat.DATE_FIELD).format(c.getTime());
        medicationTime.setText(""+hourOfDay+":"+minute+":"+ "00");
    }


}