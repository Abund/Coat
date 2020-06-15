package com.example.coat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coat.adapter.AdapterPost;
import com.example.coat.fragments.BlogPost;
import com.example.coat.fragments.ChatRooms;
import com.example.coat.fragments.Chats;
import com.example.coat.fragments.HomeFragment;
import com.example.coat.fragments.PersonalPage;
import com.example.coat.fragments.ViewPsychologist;
import com.example.coat.model.Post;
import com.example.coat.model.User;
import com.example.coat.notifications.Token;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeScreen extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener  {

    private TextView clientName,profileName;
    private ImageView imageViewProfile;
    FirebaseAuth firebaseAuth;
    FirebaseUser userf;
    DatabaseReference myRef;
    boolean closeApp;
    private int TAKE_IMAGE_CODE=10001;
    private static  final  int PICK_IMAGE=1;
    Uri imageuri;
    private  static final String TAG="HomeScreenActivity";

    String mUID;
    FirebaseUser user;

    RecyclerView recyclerView;
    List<Post> postList;
    AdapterPost adapterPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        recyclerView = findViewById(R.id.postRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        postList= new ArrayList<>();
        loadPost();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference=database.getReference("users");
        databaseReference.keepSynced(true);
        myRef = FirebaseDatabase.getInstance().getReference().child("users");

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        profileName = navigationView.getHeaderView(0).findViewById(R.id.profileName);
        imageViewProfile = navigationView.getHeaderView(0).findViewById(R.id.imageViewProfile);
        //imageViewHomePageProfile = (ImageView) findViewById(R.id.imageViewHomePageProfile);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        firebaseAuth =FirebaseAuth.getInstance();
        userf=firebaseAuth.getCurrentUser();


        Fragment newFragment =  new HomeFragment();
        replaceFragment(newFragment);

        ListView listView = new ListView(this);
        List<String> data = new ArrayList<>();
        data.add("Camera");
        data.add("Gallery");
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,data);
        listView.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen.this);
        builder.setCancelable(true);
        builder.setView(listView);
        final AlertDialog dialog=builder.create();

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null){
            if(user.getPhotoUrl()!=null){
//                Glide.with(this)
//                        .load(user.getPhotoUrl())
//                        .into(imageViewProfile);
//                Glide.with(this)
//                        .load(user.getPhotoUrl())
//                        .into(imageViewHomePageProfile);
                Picasso.get().load(user.getPhotoUrl()).networkPolicy(NetworkPolicy.OFFLINE).into(imageViewProfile, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(user.getPhotoUrl()).into(imageViewProfile);
                    }
                });

//                Picasso.get().load(user.getPhotoUrl()).networkPolicy(NetworkPolicy.OFFLINE).into(imageViewHomePageProfile, new Callback() {
//                    @Override
//                    public void onSuccess() {
//
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        Picasso.get().load(user.getPhotoUrl()).into(imageViewHomePageProfile);
//                    }
//                });
            }
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> keys = new ArrayList<>();
                User user= new User();
                user=dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getValue(User.class);
                //clientName.setText(user.getFirstName());
                profileName.setText(user.getFirstName()+" "+user.getLastName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeScreen.this,"Oppss... something went wrong",Toast.LENGTH_SHORT).show();
            }
        });

        imageViewProfile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dialog.show();
//                Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                if(intent.resolveActivity(getPackageManager())!=null){
//                    startActivityForResult(intent,TAKE_IMAGE_CODE);
//                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String choice = adapter.getItem(i);
                if(choice.equalsIgnoreCase("Camera")){
                    Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if(intent.resolveActivity(getPackageManager())!=null){
                        startActivityForResult(intent,TAKE_IMAGE_CODE);
                    }
                    dialog.dismiss();
                }else{
                    Intent gallery = new Intent();
                    gallery.setType("image/*");
                    gallery.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(gallery,"select picture"),PICK_IMAGE);
                    dialog.dismiss();
                }
            }
        });

        checkUserStatus();


    }

    private void loadPost() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Post");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    Post post = ds.getValue(Post.class);
                    postList.add(post);

                    adapterPost = new AdapterPost(HomeScreen.this,postList);
                    recyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeScreen.this,""+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchPost(final String search){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Post");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    Post post = ds.getValue(Post.class);

                    if(post.getpTitle().toLowerCase().contains(search.toLowerCase())||post.getpDescr().toLowerCase().contains(search.toLowerCase())){
                        postList.add(post);
                    }
                    adapterPost = new AdapterPost(HomeScreen.this,postList);
                    recyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeScreen.this,""+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateToken(String token){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        databaseReference.child(userf.getUid()).setValue(mToken);
    }

    @Override
    protected void onResume(){
        checkUserStatus();
        super.onResume();
    }

    private void checkUserStatus(){
        FirebaseUser user= firebaseAuth.getCurrentUser();
        if(user !=null){
            mUID= user.getUid();
            SharedPreferences sp = getSharedPreferences("SP_USER",MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID",mUID);
            editor.apply();

            //update token
            updateToken(FirebaseInstanceId.getInstance().getToken());
        }else{
            Intent at = new Intent(HomeScreen.this, MainActivity.class);
            startActivity(at);
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==TAKE_IMAGE_CODE){
            switch (resultCode){
                case RESULT_OK:
                    Bitmap bitmap =(Bitmap) data.getExtras().get("data");
                    imageViewProfile.setImageBitmap(bitmap);
                    handleUpload(bitmap);
            }
        }else if(requestCode==PICK_IMAGE){
            //if(data.getData()!= null){

            //}
            switch (resultCode){
                case RESULT_OK:
                    Bitmap bitmap = null;
                    try {
                        imageuri = data.getData();
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imageuri);
                        imageViewProfile.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    handleUpload(bitmap);
            }
        }
    }

    private void handleUpload(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("profileImages")
                .child(uid+".jpeg");
        reference.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        getDownloadUrl(reference);
                        // saves the user photo url to the database
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();
                        if(uriTask.isSuccessful()){
                            HashMap<String,Object> results = new HashMap<>();
                            results.put("imageUrl",downloadUri.toString());
                            myRef.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                        }else {

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"onFailure",e.getCause());
                    }
                });
    }

    private void getDownloadUrl(StorageReference reference){
        reference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.e(TAG,"onSuccess:"+uri);
                        setUserProfile(uri);
                    }
                });
    }

    private void setUserProfile(Uri uri){
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri).build();
        user.updateProfile(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(HomeScreen.this,"Updated successfully",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomeScreen.this,"profile image failed",Toast.LENGTH_SHORT).show();
                    }
                });
    }



    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //FragmentManager fragmentManager = getSupportFragmentManager();

        if (id == R.id.nav_home) {
            // Handle the camera action
            Fragment newFragment =  new HomeFragment();
//            FragmentTransaction ft = getFragmentManager().beginTransaction();
//            ft.add(R.id.content_frame, newFragment).commit();
            replaceFragment(newFragment);
        } else if (id == R.id.nav_personalPage) {
            Fragment newFragment =  new PersonalPage();
//            FragmentTransaction ft = getFragmentManager().beginTransaction();
//            ft.add(R.id.content_frame, newFragment).commit();
            replaceFragment(newFragment);

        }else if (id == R.id.nav_blogPost) {
            Fragment newFragment =  new BlogPost();
            replaceFragment(newFragment);

        }else if (id == R.id.nav_chats) {
            Fragment newFragment =  new Chats();
            replaceFragment(newFragment);

        } else if (id == R.id.nav_viewPsychologist) {
            Fragment newFragment =  new ViewPsychologist();
            replaceFragment(newFragment);

        } else if (id == R.id.nav_chatRooms) {
            Fragment newFragment =  new ChatRooms();
            replaceFragment(newFragment);

        }
        else if (id == R.id.sign_out) {

            firebaseAuth = FirebaseAuth.getInstance();
            LoginManager.getInstance().logOut();
            firebaseAuth.signOut();
            Intent at = new Intent(HomeScreen.this, MainActivity.class);
            startActivity(at);
        }
//
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void replaceFragment(Fragment destFragment)
    {
        // First get FragmentManager object.
        FragmentManager fragmentManager = this.getSupportFragmentManager();

        // Begin Fragment transaction.
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Replace the layout holder with the required Fragment object.
        fragmentTransaction.replace(R.id.content_frame, destFragment);

        // Commit the Fragment replace action.
        fragmentTransaction.addToBackStack(null).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.homescreen, menu);
        //menu.findItem(R.id.action_search).setVisible(false);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!TextUtils.isEmpty(query)){
                    searchPost(query);
                }else {
                    loadPost();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText)){
                    searchPost(newText);
                }else {
                    loadPost();
                }
                return false;
            }
        });
        return true;
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
            startActivity(new Intent(HomeScreen.this, AddPostActivity.class));

        }

        return super.onOptionsItemSelected(item);
    }
}
