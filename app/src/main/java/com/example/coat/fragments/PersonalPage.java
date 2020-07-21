package com.example.coat.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coat.AddPostActivity;
import com.example.coat.HomeScreen;
import com.example.coat.MainActivity;
import com.example.coat.R;
import com.example.coat.SettingsActivity;
import com.example.coat.adapter.AdapterPost;
import com.example.coat.model.Post;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class PersonalPage extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //storage
    StorageReference storageReference;
    //path where images of user profile and cover will be stored
    String storagePath = "Users_Profile_Cover_Imgs/";

    //views from xml
    ImageView avatarIv, coverIv;
    RecyclerView postsRecyclerView;
    private TextView firstNamePro,lastNamePro,addressPro,userNamePro,emailAddressPro;
    FloatingActionButton floatingActionButton;

    //progress dialog
    ProgressDialog progressDialog;


    //permissions constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    //arrays of permissions to be requested
    String cameraPermissions[];
    String storagePermissions[];

    List<Post> postList;
    AdapterPost adapterPosts;
    String uid;

    //uri of picked image
    Uri image_uri;

    //for checking profile or cover photo
    String profileOrCoverPhoto;

    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater , ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_personalpage, container, false);

        firebaseAuth= FirebaseAuth.getInstance();
        user= firebaseAuth.getCurrentUser();
        firebaseDatabase= FirebaseDatabase.getInstance();
        databaseReference= firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference(); //firebase storage reference

        firstNamePro = view.findViewById(R.id.firstNamePro);
        lastNamePro = view.findViewById(R.id.lastNamePro);
        addressPro = view.findViewById(R.id.addressPro);
        userNamePro = view.findViewById(R.id.userNamePro);
        emailAddressPro = view.findViewById(R.id.emailAddressPro);
        floatingActionButton = view.findViewById(R.id.actionUpdate);
        avatarIv = view.findViewById(R.id.avatarIv);
        coverIv = view.findViewById(R.id.coverIv);
        postsRecyclerView = view.findViewById(R.id.recyclerview_posts);

        //init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        progressDialog = new ProgressDialog(getActivity());

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String firstName =""+ ds.child("firstName").getValue();
                    String lastName =""+ ds.child("lastName").getValue();
                    String address =""+ ds.child("address").getValue();
                    String UserName =""+ ds.child("userName").getValue();
                    String email =""+ ds.child("email").getValue();
                    String cover =""+ ds.child("cover").getValue();
                    String image =""+ ds.child("image").getValue();

                    firstNamePro.setText(firstName);
                    lastNamePro.setText(lastName);
                    addressPro.setText(email);
                    userNamePro.setText(email);
                    emailAddressPro.setText(email);

                    try {
                        //if image is received then set
                        //Picasso.get().load(image).into(avatarIv);

//                        if(image==""){
//                            Picasso.get().load(R.drawable.ic_default_img).into(avatarIv);
//                        }else {
//                            Picasso.get().load(image).into(avatarIv);
//                        }

                        Picasso.get().load(image)
                                .placeholder(R.drawable.ic_account_circle_black_24dp)
                                .into(avatarIv);
                    } catch (Exception e) {
                        //if there is any exception while getting image then set default
                        Picasso.get().load(R.drawable.ic_account_circle_black_24dp).into(avatarIv);
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

        floatingActionButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                showEditProfileDialogue();
            }
        });

        super.onCreate(savedInstanceState);
        postList = new ArrayList<>();
        checkUserStatus();
        loadMyPosts();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstances){
        setHasOptionsMenu(true);
        super.onCreate(savedInstances);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.homescreen, menu);
        super.onCreateOptionsMenu(menu,inflater);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        MenuItem item = menu.findItem(R.id.action_search);

        //v7 searchview ot search user specific posts
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button
                if (!TextUtils.isEmpty(s)) {
                    //search
                    searchMyPosts(s);
                } else {
                    loadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called whenever user type any letter
                if (!TextUtils.isEmpty(s)) {
                    //search
                    searchMyPosts(s);
                } else {
                    loadMyPosts();
                }
                return false;
            }
        });
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
            //LoginManager.getInstance().logOut();
            firebaseAuth.signOut();
            Intent at = new Intent(getActivity(), MainActivity.class);
            startActivity(at);
        }else if (id == R.id.action_add_post) {
            startActivity(new Intent(getActivity(), AddPostActivity.class));

        }else if (id==R.id.actionSetting){
            //go to settings activity
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*This method will be called after picking image from Camera or Gallery*/
        if (resultCode == RESULT_OK) {

            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //image is picked from gallery, get uri of image
                image_uri = data.getData();

                uploadProfileCoverPhoto(image_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //image is picked from camera, get uri of image

                uploadProfileCoverPhoto(image_uri);

            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void showEditProfileDialogue() {
        String options[] = {"Edit Profile Picture", "Edit Cover Photo","Edit first name","Edit last name","Edit address","Edit user name"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");
        builder.setCancelable(true);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    //Edit Profile clicked
                    progressDialog.setMessage("Updating Profile Picture");
                    profileOrCoverPhoto = "image"; //i.e. changing profile picture, make sure to assign same value
                    showImagePicDialog();
                } else if (i == 1) {
                    //Edit Cover clicked
                    progressDialog.setMessage("Updating Cover Photo");
                    profileOrCoverPhoto = "cover"; //i.e. changing cover photo, make sure to assign same value
                    showImagePicDialog();
                }else if(i==0){
                    progressDialog.setMessage("Updating first name");
                    showFirstNameUpdateDialogue("firstName");

                }else if(i==1){
                    progressDialog.setMessage("Updating last name");
                    showFirstNameUpdateDialogue("lastName");

                }else if(i==2){
                    progressDialog.setMessage("Updating address");
                    showFirstNameUpdateDialogue("address");

                }else if(i==3){
                    progressDialog.setMessage("Updating user name");
                    showFirstNameUpdateDialogue("userName");

                }else if (i == 4) {
                    //Edit Phone clicked
                    progressDialog.setMessage("Changing Password");
                    showChangePasswordDialog();
                }
            }
        });
        builder.create().show();
    }

    private void showChangePasswordDialog() {
        //password change dialog with custom layout having currentPassword, newPassword and update button

        //inflate layout for dialog
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_update_password, null);
        final EditText passwordEt = view.findViewById(R.id.passwordEt);
        final EditText newPasswordEt = view.findViewById(R.id.newPasswordEt);
        Button updatePasswordBtn = view.findViewById(R.id.updatePasswordBtn);

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setView(view); //set view to dialog

        final android.app.AlertDialog dialog = builder.create();
        dialog.show();

        updatePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate data
                String oldPassword = passwordEt.getText().toString().trim();
                String newPassword = newPasswordEt.getText().toString().trim();
                if (TextUtils.isEmpty(oldPassword)){
                    Toast.makeText(getActivity(), "Enter your current password...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPassword.length()<6){
                    Toast.makeText(getActivity(), "Password length must atleast 6 characters...", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.dismiss();
                updatePassword(oldPassword, newPassword);
            }
        });
    }

    private void updatePassword(String oldPassword, final String newPassword) {
        progressDialog.show();

        //get current user
        final FirebaseUser user = firebaseAuth.getCurrentUser();

        //before changing password re-authenticate the user
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //successfully authenticated, begin update

                        user.updatePassword(newPassword)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //password updated
                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(), "Password Updated...", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed updating password, show reason
                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //authentication failed, show reason
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showFirstNameUpdateDialogue(final String key) {

        final AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+ key);
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);

        final EditText editText= new EditText(getActivity());
        editText.setHint("Enter "+key);
        linearLayout.addView(editText);
        builder.setView(linearLayout);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String value = editText.getText().toString().trim();

                if(!TextUtils.isEmpty(value)){
                    progressDialog.show();
                    HashMap<String,Object> result = new HashMap<>();
                    result.put(key,value);
                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(),"Updated... ",Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity()," "+e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });
                }else{
                    Toast.makeText(getActivity(),"Please Enter "+key+"",Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void loadMyPosts() {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show newest post first, for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview
        postsRecyclerView.setLayoutManager(layoutManager);

        //init posts list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //qurey to load posts
        /*whenever user publishes a post the uid of this user is also saved as info of post
         * so we're retrieving posts having uid equals to uid of current user*/
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Post myPosts = ds.getValue(Post.class);

                    //add to list
                    postList.add(myPosts);

                    //adapter
                    adapterPosts = new AdapterPost(getActivity(), postList);
                    //set this adapter to recyclerview
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
            uid = user.getUid();
        } else {
            //user not signed in, go to main acitivity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    private void pickFromCamera() {
        //Intent of picking image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        //put image uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        //pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void requestCameraPermission() {
        //request runtime storage permission
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void showImagePicDialog() {
        //show dialog containing options Camera and Gallery to pick the image

        String options[] = {"Camera", "Gallery"};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Pick Image From");
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog item clicks
                if (which == 0) {
                    //Camera clicked
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                } else if (which == 1) {
                    //Gallery clicked
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });
        //create and show dialog
        builder.create().show();


    }

    private boolean checkCameraPermission() {
        //check if storage permission is enabled or not
        //return true if enabled
        //return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestStoragePermission() {
        //request runtime storage permission
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        //check if storage permission is enabled or not
        //return true if enabled
        //return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void uploadProfileCoverPhoto(final Uri uri) {
        //show progress
        progressDialog.show();

        /*Instead of creating separate function for Profile Picture and Cover Photo
         * i'm doing work for both in same function
         *
         * To add check ill add a string variable and assign it value "image" when user clicks
         * "Edit Profile Pic", and assign it value "cover" when user clicks "Edit Cover Photo"
         * Here: image is the key in each user containing url of user's profile picture
         *       cover is the key in each user containing url of user's cover photo */

        /*The parameter "image_uri" contains the uri of image picked either from camera or gallery
         * We will use UID of the currently signed in user as name of the image so there will be only one image for
         * profile and one image for cover for each user*/

        //path and name of image to be stored in firebase storage
        //e.g. Users_Profile_Cover_Imgs/image_e12f3456f789.jpg
        //e.g. Users_Profile_Cover_Imgs/cover_c123n4567g89.jpg
        String filePathAndName = storagePath + "" + profileOrCoverPhoto + "_" + user.getUid();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image is uploaded to storage, now get it's url and store in user's database
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        final Uri downloadUri = uriTask.getResult();

                        //check if image is uploaded or not and url is received
                        if (uriTask.isSuccessful()) {
                            //image uploaded
                            //add/update url in user's database
                            HashMap<String, Object> results = new HashMap<>();
                            /*First Parameter is profileOrCoverPhoto that has value "image" or "cover"
                              which are keys in user's database where url of image will be saved in one
                              of them
                              Second Parameter contains the url of the image stored in firebase storage, this
                              url will be saved as value against key "image" or "cover"*/
                            results.put(profileOrCoverPhoto, downloadUri.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //url in database of user is added successfully
                                            //dismiss progress bar
                                            progressDialog.dismiss();
                                            Toast.makeText(getActivity(), "Image Updated...", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //error adding url in database of user
                                            //dismiss progress bar
                                            progressDialog.dismiss();
                                            Toast.makeText(getActivity(), "Erro Updating Image...", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            //if user edit his name, also change it from hist posts
                            if (profileOrCoverPhoto.equals("image")) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                Query query = ref.orderByChild("uid").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String child = ds.getKey();
                                            dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users");
                                Query query1 = ref1.orderByChild("uid").equalTo(uid);
                                query1.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String child = ds.getKey();
                                            dataSnapshot.getRef().child(child).child("imageUrl").setValue(downloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                //update user image in current users comments on posts
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String child = ds.getKey();
                                            if (dataSnapshot.child(child).hasChild("Comments")) {
                                                String child1 = "" + dataSnapshot.child(child).getKey();
                                                Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                                child2.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                            String child = ds.getKey();
                                                            dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                        } else {
                            //error
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Some error occured", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //there were some error(s), get and show error message, dismiss progress dialog
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void searchMyPosts(final String searchQuery) {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show newest post first, for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview
        postsRecyclerView.setLayoutManager(layoutManager);

        //init posts list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //qurey to load posts
        /*whenever user publishes a post the uid of this user is also saved as info of post
         * so we're retrieving posts having uid equals to uid of current user*/
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Post myPosts = ds.getValue(Post.class);


                    if (myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        //add to list
                        postList.add(myPosts);
                    }

                    //adapter
                    adapterPosts = new AdapterPost(getActivity(), postList);
                    //set this adapter to recyclerview
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
