package com.klmni.la8eni.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.klmni.la8eni.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity
{

    private String receiverUserID, Current_State, senderUserID;

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestButton, declineMessageRequestButton;

    private DatabaseReference userReference, chatRequestReference, contactsReference, notificationReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        initialViews();

        initialDatabase();

        toastUserID();

        Current_State = "new";

        retrieveUserInformation();

    }

    private void initialViews()
    {
        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        sendMessageRequestButton = findViewById(R.id.send_message_request_button);
        declineMessageRequestButton = findViewById(R.id.decline_message_request_button);

        senderUserID = mAuth.getCurrentUser().getUid();
    }

    private void initialDatabase()
    {
        mAuth = FirebaseAuth.getInstance();
        userReference = FirebaseDatabase.getInstance().getReference();
        chatRequestReference = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsReference = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationReference = FirebaseDatabase.getInstance().getReference().child("Notifications");
    }

    private void toastUserID()
    {
        receiverUserID = getIntent().getStringExtra("visit_user_id");

        Toast.makeText(ProfileActivity.this, "User ID =" + receiverUserID, Toast.LENGTH_SHORT).show();
    }

    private void retrieveUserInformation()
    {
        userReference.child("Users").child(receiverUserID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if ((snapshot.exists()) && (snapshot.hasChild("image")))
                {
                    String userImage = snapshot.child("image").getValue().toString();
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    Picasso.get()
                            .load(userImage)
                            .placeholder(R.drawable.profile_image)
                            .into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                   manageChatRequests();

                }
                else
                {
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                   manageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(ProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void manageChatRequests()
    {
        chatRequestReference.child(senderUserID).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if (snapshot.hasChild(receiverUserID))
                        {
                            String request_type = snapshot.child(receiverUserID)
                                    .child("request_type")
                                    .getValue()
                                    .toString();

                            if (request_type.equals("sent"))
                            {
                                Current_State = "request_sent";
                                sendMessageRequestButton.setText("Cancel Chat Request");
                            }
                            else if (request_type.equals("received"))
                            {
                                Current_State = "request_received";
                                sendMessageRequestButton.setText("Accept Chat Request");

                                declineMessageRequestButton.setVisibility(View.VISIBLE);
                                declineMessageRequestButton.setEnabled(true);

                                declineMessageRequestButton.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        cancelChatRequest();
                                    }
                                });
                            }
                        }
                        else
                        {
                            chatRequestReference.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot)
                                        {
                                            if (snapshot.hasChild(receiverUserID))
                                            {
                                                Current_State = "friends";
                                                sendMessageRequestButton.setText("Remove this contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error)
                                        {
                                            Toast.makeText(ProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        Toast.makeText(ProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        if (!senderUserID.equals(receiverUserID))
        {
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    sendMessageRequestButton.setEnabled(false);

                    if (Current_State.equals("new"))
                    {
                        sendChatRequest();
                    }
                    if (Current_State.equals("request_sent"))
                    {
                        cancelChatRequest();
                    }
                    if (Current_State.equals("request_received"))
                    {
                        acceptChatRequest();
                    }
                    if (Current_State.equals("friends"))
                    {
                        removeSpecificContacts();
                    }
                }
            });
        }
        else
        {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void removeSpecificContacts()
    {
        contactsReference.child(senderUserID)
                .child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            contactsReference.child(receiverUserID)
                                    .child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                Current_State = "new";
                                                sendMessageRequestButton.setText("Sent Message");

                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptChatRequest()
    {
        contactsReference.child(senderUserID)
                .child(receiverUserID)
                .child("Contacts")
                .setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            contactsReference.child(receiverUserID)
                                    .child(senderUserID)
                                    .child("Contacts")
                                    .setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                chatRequestReference.child(senderUserID)
                                                        .child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    chatRequestReference.child(receiverUserID)
                                                                            .child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                            {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    sendMessageRequestButton.setEnabled(true);
                                                                                    Current_State = "friends";
                                                                                    sendMessageRequestButton.setText("Remove this contact");

                                                                                    declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                                    declineMessageRequestButton.setEnabled(false);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelChatRequest()
    {
        chatRequestReference.child(senderUserID)
                .child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            chatRequestReference.child(receiverUserID)
                                    .child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                Current_State = "new";
                                                sendMessageRequestButton.setText("Sent Message");

                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void sendChatRequest()
    {
        chatRequestReference.child(senderUserID)
                .child(receiverUserID)
                .child("request_type")
                .setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            chatRequestReference.child(receiverUserID)
                                    .child(senderUserID)
                                    .child("request_type")
                                    .setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {

                                                HashMap<String, String> mapChatNotification = new HashMap<>();
                                                mapChatNotification.put("from", senderUserID);
                                                mapChatNotification.put("type", "request");

                                                notificationReference
                                                        .child(receiverUserID)
                                                        .push()
                                                        .setValue(mapChatNotification).addOnCompleteListener(new OnCompleteListener<Void>()
                                                {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if (task.isSuccessful())
                                                        {
                                                            sendMessageRequestButton.setEnabled(true);
                                                            Current_State = "request_sent";
                                                            sendMessageRequestButton.setText("Cancel Chat Request");
                                                        }
                                                        else
                                                        {
                                                            Toast.makeText(ProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}