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
import android.widget.TextView;
import android.widget.Toast;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    Button signIn,googleSignIn;
    TextView register,forgetPassword;
    EditText email,password;
    FirebaseAuth firebaseAuth;
    SignInButton signInButton;
    GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN=1;
    private String TAG="MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signIn = (Button) findViewById(R.id.signIn);
        signInButton = (SignInButton) findViewById(R.id.googleSignInButton);
        register = (TextView) findViewById(R.id.register);
        email = (EditText) findViewById(R.id.loginEmail);
        forgetPassword=(TextView) findViewById(R.id.forgetPassword);
        password = (EditText) findViewById(R.id.loginPassword);







        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()!=null){
            Intent at = new Intent(MainActivity.this, HomeScreen.class);
            startActivity(at);
        }

        signIn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String email1 = email.getText().toString().trim();
                String password1 = password.getText().toString().trim();

                if(email1.isEmpty()){
                    email.setError("Email is required");
                    return;
                }
                else if(TextUtils.isEmpty(password1)){
                    password.setError("Password is required");
                    return;
                }
                else if(password1.length()<6){
                    password.setError("Password must be greater than 6 characters long");
                    return;
                }
                //authenticathe the user
                firebaseAuth.signInWithEmailAndPassword(email1,password1).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this,"Login successful",Toast.LENGTH_SHORT).show();
                            Intent at = new Intent(MainActivity.this, HomeScreen.class);
                            startActivity(at);
                        }else{
                            Toast.makeText(MainActivity.this,"Error in login",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        register.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent at = new Intent(MainActivity.this, RegistrationPage.class);
                startActivity(at);
            }
        });

        forgetPassword.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent at = new Intent(MainActivity.this, ForgetPassword.class);
                startActivity(at);
            }
        });

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
                            Toast.makeText(MainActivity.this,"Login failed",Toast.LENGTH_SHORT).show();
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
            Toast.makeText(MainActivity.this,personFamilyName+"Registration Successful",Toast.LENGTH_SHORT).show();




        }

    }
}

