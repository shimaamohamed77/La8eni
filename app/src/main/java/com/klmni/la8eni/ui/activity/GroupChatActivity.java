package com.klmni.la8eni.ui.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.klmni.la8eni.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity
{

    private Toolbar toolBar;
    private EditText userMessageInput;
    private ImageButton sendMessageButton;
    private TextView displayTextMessage;
    private ScrollView scrollView;
    private Intent getDataIntent;

    private FirebaseAuth mAuth;
    private DatabaseReference usersReference, groupNameReference, groupMessageKeyReference;

    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        getDataIntent = getIntent();
        currentGroupName = getDataIntent.getStringExtra("groupName");
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();



        initializeViews();

        initializeDatabase();

        getUserInformation();

        clickedView();

    }


    @Override
    protected void onStart()
    {
        super.onStart();

        groupNameReference.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
                if (snapshot.exists())
                {
                    displayMessage(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
                if (snapshot.exists())
                {
                    displayMessage(snapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot)
            {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(GroupChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeViews()
    {
        toolBar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle(currentGroupName);

        userMessageInput = findViewById(R.id.input_group_message);
        sendMessageButton = findViewById(R.id.send_message_button);
        displayTextMessage = findViewById(R.id.group_chat_txt_display);
        scrollView = findViewById(R.id.my_scroll_view);
    }

    private void initializeDatabase()
    {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersReference = FirebaseDatabase.getInstance().getReference();
        groupNameReference = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
    }

    private void getUserInformation()
    {
        usersReference.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.exists())
                {
                    currentUserName = snapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(GroupChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clickedView()
    {
        sendMessageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                saveInformationToDatabase();

                userMessageInput.setText("");

                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void saveInformationToDatabase()
    {
        String message = userMessageInput.getText().toString();
        String messageKEY = groupNameReference.push().getKey();

        if (TextUtils.isEmpty(message))
        {
            Toast.makeText(GroupChatActivity.this, "Please write message first", Toast.LENGTH_SHORT).show();
            userMessageInput.requestFocus();
            return;
        }
        else
        {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MM/dd/yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh-mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupNameReference.updateChildren(groupMessageKey);


            groupMessageKeyReference = groupNameReference.child(messageKEY);

            HashMap<String, Object> messageInfromationMap = new HashMap<>();
            messageInfromationMap.put("name", currentUserName);
            messageInfromationMap.put("message", message);
            messageInfromationMap.put("date", currentDate);
            messageInfromationMap.put("time", currentTime);

            groupMessageKeyReference.updateChildren(messageInfromationMap);
        }
    }

    private void displayMessage(DataSnapshot snapshot)
    {
        Iterator iterator = snapshot.getChildren().iterator();

        while (iterator.hasNext())
        {
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessage.append(chatName +" :\n"+ chatMessage +"\n"+ chatTime +"     "+ chatDate +"\n\n\n");

            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

}