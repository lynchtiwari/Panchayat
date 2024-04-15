package com.example.panchayat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.panchayat.Adapters.ChatAdapter;
import com.example.panchayat.Models.MessageModel;
import com.example.panchayat.Models.users;
import com.example.panchayat.databinding.ActivityChatDetailsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatDetailsActivity extends AppCompatActivity {

    ActivityChatDetailsBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChatDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();

        final String senderId=auth.getUid();
        String receiverId=getIntent().getStringExtra("userId");
        String userName=getIntent().getStringExtra("userName");
        String profilePic=getIntent().getStringExtra("profilePic");

        binding.userName.setText(userName);
        Picasso.get().load(profilePic).placeholder(R.drawable.avatar).into(binding.profileImage);

        binding.btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ChatDetailsActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        final ArrayList<MessageModel> messageModels=new ArrayList<>();
        final ChatAdapter chatAdapter= new ChatAdapter(messageModels,this,receiverId);



        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        binding.MessagesRecyclerView.setLayoutManager(layoutManager);

        binding.MessagesRecyclerView.setAdapter(chatAdapter);



        final String senderRoom=senderId+receiverId;
        final String receiverRoom=receiverId+senderId;

        database.getReference().child("Chats").child(senderRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageModels.clear();
                for(DataSnapshot snapshot1: snapshot.getChildren()){
                    MessageModel model=snapshot1.getValue(MessageModel.class);

                    model.setMessageId(snapshot1.getKey());

                    messageModels.add(model);



                }
                chatAdapter.notifyDataSetChanged();
                binding.MessagesRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        binding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message=binding.edtMessage.getText().toString();
                if(message.isEmpty())
                    return;


                final MessageModel model=new MessageModel(senderId,message);
                model.setTimestamp(new Date().getTime());

                binding.edtMessage.setText("");
                sendNotification(database.getReference().child("Users").child(receiverId).child("userName").getKey(),userName,message);

               Log.d("receiver",database.getReference().child("Users").child(receiverId).child("userName").getKey());
                database.getReference().child("Chats").child(senderRoom).push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void avoid) {
                        database.getReference().child("Chats").child(receiverRoom).push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void avoid) {

                            }
                        });
                    }
                });





            }
        });
    }

    public static void sendNotification(String fcmToken, String title, String body) {
        // Create OkHttpClient instance
        OkHttpClient client = new OkHttpClient();

        // Prepare the FCM API URL
        String apiUrl = "https://fcm.googleapis.com/fcm/send";

        // Prepare the JSON payload for the FCM notification
        JSONObject notification = new JSONObject();
        try {
            notification.put("title", title);
            notification.put("body", body);
        } catch (JSONException e) {
            e.printStackTrace();
            return; // Exit if JSON payload creation fails
        }

        JSONObject message = new JSONObject();
        try {
            message.put("to", fcmToken);
            message.put("notification", notification);
        } catch (JSONException e) {
            e.printStackTrace();
            return; // Exit if JSON message creation fails
        }

        // Create the request body with the JSON message
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), message.toString());

        // Create the HTTP request to send to FCM API
        Request request = new Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .addHeader("Authorization", "Bearer AAAAJcIqw9o:APA91bHvscC808w7ngEwrUtZl23NnKqDf5brOVeFYZuBGEAnJneromcRPXn2AXwXHpGHE_bLx8jJ9ZkAonjrjYlroSTVpe4v6aTPYmTpWDvR6kdokV9KZ87fdLUsppB89-46xY_42X4z") // Replace YOUR_SERVER_KEY with your FCM server key
                .build();

        // Send the HTTP request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle failure to send notification
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Handle response from FCM API (optional)
                String responseBody = response.body().string();
                System.out.println("FCM Notification Response: " + responseBody);
            }
        });
    }

}