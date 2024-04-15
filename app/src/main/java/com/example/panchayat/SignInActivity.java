package com.example.panchayat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.panchayat.Models.users;
import com.example.panchayat.databinding.ActivitySignInBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class SignInActivity extends AppCompatActivity {

ActivitySignInBinding binding;
ProgressDialog progressDialog;

FirebaseAuth auth;
FirebaseDatabase database;

GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();




        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Login");
        progressDialog.setMessage("Logging in");

        binding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String email=binding.edtEmail.getText().toString();
                String password=binding.edtPassword.getText().toString();

                if(TextUtils.isEmpty(email)||TextUtils.isEmpty(password))
                    Toast.makeText(SignInActivity.this, "Empty credentials", Toast.LENGTH_SHORT).show();

                else {

                    auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {



                            if(task.isSuccessful())
                            {
                                progressDialog.show();

                                startActivity(new Intent(SignInActivity.this,MainActivity.class));
                                progressDialog.dismiss();
                                finish();
                            }

                            else
                                Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });

        binding.btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                GoogleSignInOptions gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                mGoogleSignInClient= GoogleSignIn.getClient(SignInActivity.this,gso);
                signIn();
                getFCMToken();

            }


        });



        if(auth.getCurrentUser()!=null)
        {
            getFCMToken();
            startActivity(new Intent(SignInActivity.this,MainActivity.class));
            finish();
        }

        binding.txtSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignInActivity.this,SignUpActivity.class));
                finish();
            }
        });


    }


    void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    try {
                        String token = task.getResult();
                        System.out.println(token);
                        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("FCMToken").setValue(token);
                    } catch (Exception e) {

                    }
                }
            }
        });
    }

    int RC_SIGN_IN=65;

    private void signIn()
    {
        Intent signInIntent=mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==RC_SIGN_IN)
        {
            Task<GoogleSignInAccount> task=GoogleSignIn.getSignedInAccountFromIntent(data);

            try{
                progressDialog.show();
                GoogleSignInAccount account =task.getResult(ApiException.class);
                Log.d("TAG","FirebaseAuthWithGoogle:"+account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e)
            {
                Log.w("TAG","Google sign in failed",e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken)
    {
        AuthCredential credential= GoogleAuthProvider.getCredential(idToken,null);

        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    Log.d("TAG","SignInWithCredentials:Success");
                    FirebaseUser fUser=auth.getCurrentUser();

                    users user=new users();
                    user.setUserId(fUser.getUid());
                    user.setUserName(fUser.getDisplayName());
                    user.setProfilePic(fUser.getPhotoUrl().toString());

                    database.getReference().child("Users").child(fUser.getUid()).setValue(user);

                    progressDialog.dismiss();
                    startActivity(new Intent(SignInActivity.this,MainActivity.class));
                    finish();
                }

                else {
                    Log.w("TAG","SignInWithCredentials:Failure",task.getException());
                    Snackbar.make(binding.getRoot(),"Authentication Failed",Snackbar.LENGTH_SHORT).show();

                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}