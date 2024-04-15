package com.example.panchayat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.panchayat.Models.users;
import com.example.panchayat.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    ActivitySignUpBinding binding;

    private FirebaseAuth auth;
    FirebaseDatabase database;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Creating Account");
        progressDialog.setMessage("We are creating your account");

        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username=binding.edtUsername.getText().toString();
                String email=binding.edtEmail.getText().toString();
                String password=binding.edtPassword.getText().toString();

                if(TextUtils.isEmpty(username) ||TextUtils.isEmpty(email)||TextUtils.isEmpty(password))
                    Toast.makeText(SignUpActivity.this, "Empty credentials", Toast.LENGTH_SHORT).show();

                else if (password.length()<6)
                    Toast.makeText(SignUpActivity.this, "Password must be of atleast 6 digits", Toast.LENGTH_SHORT).show();

                else {
                    progressDialog.show();

                    auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();

                            if(task.isSuccessful())
                            {
                                Toast.makeText(SignUpActivity.this, "User Successfully created", Toast.LENGTH_SHORT).show();

                                users user=new users(username,email,password);
                                String id=task.getResult().getUser().getUid();
                                database.getReference().child("Users").child(id).setValue(user);

                                binding.edtEmail.setText(null);
                                binding.edtPassword.setText(null);
                                binding.edtUsername.setText(null);
                                startActivity(new Intent(SignUpActivity.this,SignInActivity.class));
                                finish();
                            }
                        }
                    });
                }


            }
        });

       binding.txtAlready.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               startActivity(new Intent(SignUpActivity.this,SignInActivity.class));
               finish();
           }
       });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}