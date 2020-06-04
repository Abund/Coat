package com.example.coat;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.coat.model.User;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class RegistrationPage extends AppCompatActivity {

    Button signUp;
    FirebaseAuth firebaseAuth;
    EditText firstName,lastName,email,password;
    SignInButton signInButton;
    GoogleSignInClient mGoogleSignInClient;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private int RC_SIGN_IN=1;
    private String TAG="MainActivity";
    private AccessTokenTracker accessTokenTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration_page);


        signUp = (Button) findViewById(R.id.signup);
        firstName = (EditText) findViewById(R.id.firstName);
        lastName = (EditText) findViewById(R.id.lastName);
        email = (EditText) findViewById(R.id.email);
        signInButton = (SignInButton) findViewById(R.id.ggoglr);
        password = (EditText) findViewById(R.id.password);
        loginButton = findViewById(R.id.facey);
        loginButton.setReadPermissions(Arrays.asList("email", "public_profile"));

        accessTokenTracker = new AccessTokenTracker() {
            // This method is invoked everytime access token changes
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                useLoginInformation(currentAccessToken);

            }
        };



        // Creating CallbackManager
        callbackManager = CallbackManager.Factory.create();

        // Registering CallbackManager with the LoginButton
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // Retrieving access token using the LoginResult
                //AccessToken accessToken = loginResult.getAccessToken();
                AccessToken accessToken = loginResult.getAccessToken();
                useLoginInformation(accessToken);
                Intent at = new Intent(RegistrationPage.this, HomeScreen.class);
                startActivity(at);
            }
            @Override
            public void onCancel() {
            }
            @Override
            public void onError(FacebookException error) {
            }
        });

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                // currentAccessToken is null if the user is logged out
                if (currentAccessToken != null) {
                    // AccessToken is not null implies user is logged in and hence we sen the GraphRequest
                    useLoginInformation(currentAccessToken);
                }else{
                    //displayName.setText("Not Logged In");
                }
            }
        };

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("users");

        if(firebaseAuth.getCurrentUser()!=null){
            Intent at = new Intent(RegistrationPage.this, HomeScreen.class);
            startActivity(at);
        }

        signUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                String email1 = email.getText().toString().trim();
                String password1 = password.getText().toString().trim();

                if(TextUtils.isEmpty(email.getText().toString().trim())){
                    email.setError("Email is required");
                    return;
                }
                if(TextUtils.isEmpty(password1)){
                    password.setError("Password is required");
                    return;
                }

                if(password1.length()<6){
                    password.setError("Password must be greater than 6 characters long");
                    return;
                }

                // register the user in firebase
                firebaseAuth.createUserWithEmailAndPassword(email1,password1).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            User user = new User();
                            user.setEmail(email.getText().toString().trim());
                            user.setFirstName(firstName.getText().toString().trim());
                            user.setLastName(lastName.getText().toString().trim());
                            user.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            String timeStamp= String.valueOf(System.currentTimeMillis());
                            user.setOnlineStatus(timeStamp);
                            user.setTypingTo("noOne");
                            user.setImageUrl("");

                            myRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(RegistrationPage.this,"Registration successful",Toast.LENGTH_SHORT).show();
                                    Intent at = new Intent(RegistrationPage.this, HomeScreen.class);
                                    startActivity(at);
                                }
                            });
                            myRef.keepSynced(true);



                        }else{
                            System.out.println(task.getException());
                            Toast.makeText(RegistrationPage.this,"Error in registration",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

//        myRef.addValueEventListener(new ValueEventListener() {
//            @Override public void onDataChange(DataSnapshot dataSnapshot) {
//                //String value = dataSnapshot.getValue(String.class);
//                // viewText.setText(value);
//            }
//            @Override public void onCancelled(DatabaseError error) {
//                // Failed to read value
//                Log.w("Ch3", "Failed to read value.", error.toException());
//            }
//        });

        signInButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                signIn();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    public void onStart() {
        super.onStart();
        //This starts the access token tracking
        accessTokenTracker.startTracking();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            useLoginInformation(accessToken);
        }
    }
    public void onDestroy() {
        super.onDestroy();
        // We stop the tracking before destroying the activity
        accessTokenTracker.stopTracking();
    }

    private void useLoginInformation(AccessToken accessToken) {
        /**
         Creating the GraphRequest to fetch user details
         1st Param - AccessToken
         2nd Param - Callback (which will be invoked once the request is successful)
         **/
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            //OnCompleted is invoked once the GraphRequest is successful
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String name = object.getString("name");
                    String email = object.getString("email");
                    String image = object.getJSONObject("picture").getJSONObject("data").getString("url");
//                    displayName.setText(name);
//                    emailID.setText(email);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        // We set parameters to the GraphRequest using a Bundle.
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,picture.width(200)");
        request.setParameters(parameters);
        // Initiate the GraphRequest
        request.executeAsync();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(RegistrationPage.this,"Login failed",Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    public  void updateUI(FirebaseUser user1){
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personGivenName = acct.getGivenName();
            String personFamilyName = acct.getFamilyName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            Uri personPhoto = acct.getPhotoUrl();
            Toast.makeText(RegistrationPage.this,personFamilyName+"Registration Successful",Toast.LENGTH_SHORT).show();

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myRef = database.getReference("users");

            User user = new User();
            user.setEmail(personEmail);
            user.setFirstName(personGivenName);
            user.setLastName(personFamilyName);
            user.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
            String timeStamp= String.valueOf(System.currentTimeMillis());
            user.setOnlineStatus(timeStamp);
            user.setTypingTo("noOne");
            user.setImageUrl("");

            myRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(RegistrationPage.this,"successful",Toast.LENGTH_SHORT).show();
                    Intent at = new Intent(RegistrationPage.this, HomeScreen.class);
                    startActivity(at);
                }
            });
            myRef.keepSynced(true);


        }

    }
}
