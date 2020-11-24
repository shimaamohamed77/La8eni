package com.klmni.la8eni.ui.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.klmni.la8eni.R;
import com.klmni.la8eni.model.Messages;
import com.klmni.la8eni.view.MessageAdapter;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{

    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private ImageButton sendMessageButton, sendFilesButton;
    private EditText messageInputText;

    private Toolbar chatToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference rootReference;

    private RecyclerView userMessagesList;
    private final List<Messages> messagesList = new ArrayList<>();
    private MessageAdapter messageAdapter;

    private String saveCurrentTime, saveCurrentDate;

    private String currentUserID;
    private String downloadUrl;

    private String checker = "";

    private Uri photoPathUri;

    private static final int GALLERY_PICK = 1;

    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        messageReceiverID = getIntent().getStringExtra("Visit_User_ID");
        messageReceiverName = getIntent().getStringExtra("Visit_User_Name");
        messageReceiverImage = getIntent().getStringExtra("Visit_User_Image");

        initialView();

        initialDatabase();

        initializeControllers();

        getData();

        clickedView();

        displayLastSeen();






    }

    private void initialView()
    {
        loadingDialog = new ProgressDialog(ChatActivity.this);
    }

    private void initialDatabase()
    {
        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootReference = FirebaseDatabase.getInstance().getReference();
    }

    private void initializeControllers()
    {
        chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //3lashan el-custom-view yeshta8l lazem el-line dah
        //First-Line
        actionBar.setDisplayShowCustomEnabled(true);
        //End-Line
        actionBar.setTitle(messageReceiverName);

        LayoutInflater layoutInflater = (LayoutInflater) ChatActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar , null);
        actionBar.setCustomView(actionBarView);

        userImage = findViewById(R.id.custom_profile_image);
        userName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_last_seen);

        sendMessageButton = findViewById(R.id.send_message_btn);
        sendFilesButton = findViewById(R.id.send_files_btn);
        messageInputText = findViewById(R.id.input_message);


        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = findViewById(R.id.private_message_of_users);
        userMessagesList.setLayoutManager(new LinearLayoutManager(getApplicationContext() , RecyclerView.VERTICAL , false));
        userMessagesList.setAdapter(messageAdapter);


        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat(" MMM:dd:yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat(" hh-mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

    }

    private void getData()
    {
        userName.setText(messageReceiverName);
        userLastSeen.setText("Last Seen");
        Picasso.get()
                .load(messageReceiverImage)
                .placeholder(R.drawable.profile_image)
                .into(userImage);
    }

    private void clickedView()
    {
        sendMessageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                sendMessage();
                messageInputText.setText("");

            }
        });

        sendFilesButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "PDF Files",
                                "Ms Word Files"
                        };

                AlertDialog.Builder builder1 = new AlertDialog.Builder(ChatActivity.this);
                builder1.setTitle("Select the File");
                builder1.setItems(options, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        if (i == 0)
                        {
                            checker = "image";
                            openGallery();
                        }

                        if (i == 1)
                        {
                            checker = "pdf";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select PDF File"), 438);
                        }

                        if (i == 2)
                        {
                            checker = "docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select Ms Word File"), 438);
                        }
                    }
                });

                builder1.show();

            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            if (!checker.equals("image"))
            {
                final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files") /*+ ".jpg" */;

                final String messageSenderReference = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverReference = "Messages/" + messageReceiverID + "/" + messageSenderID;

                final DatabaseReference userMessageKeyReference = rootReference
                        .child("Messages")
                        .child(messageSenderID)
                        .child(messageReceiverID)
                        .push();

                final String messagePushID = userMessageKeyReference.getKey();

                final StorageReference filePath = storageReference.child(messagePushID);

                filePath.putFile(photoPathUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            HashMap<String, Object> messageTextBody = new HashMap();
                            messageTextBody.put("message", task.getResult().getStorage().getDownloadUrl().toString());
                            messageTextBody.put("name", photoPathUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("to", messageReceiverID);
                            messageTextBody.put("messageID", messagePushID);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);

                            HashMap<String, Object> messageBodyDetails = new HashMap<>();
                            messageBodyDetails.put(messageSenderReference + "/" + messagePushID, messageTextBody);
                            messageBodyDetails.put(messageReceiverReference + "/" + messagePushID, messageTextBody);

                            rootReference.updateChildren(messageBodyDetails);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot)
                    {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        loadingDialog.setMessage((int)progress + " % ");
                    }
                });
            }

            else if (checker.equals("image"))
            {

                final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files") /*+ ".jpg" */;

                final String messageSenderReference = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverReference = "Messages/" + messageReceiverID + "/" + messageSenderID;

            final DatabaseReference userMessageKeyReference = rootReference
                        .child("Messages")
                        .child(messageSenderID)
                        .child(messageReceiverID)
                        .push();

            final String messagePushID = userMessageKeyReference.getKey();

            final StorageReference filePath = storageReference.child(messagePushID);


            CropImage.ActivityResult activityResult = CropImage.getActivityResult(data);

            photoPathUri = activityResult.getUri();


                final UploadTask task = filePath.putFile(photoPathUri);
                task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
                {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                    {
                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task)
                    {
                        if (task.isSuccessful())
                        {
                            Uri downloadUri = task.getResult();

                            downloadUrl = downloadUri.toString();

                            HashMap<String, Object> messageTextBody = new HashMap();
                            messageTextBody.put("message", downloadUrl);
                            messageTextBody.put("name", photoPathUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("to", messageReceiverID);
                            messageTextBody.put("messageID", messagePushID);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);

                            HashMap<String, Object> messageBodyDetails = new HashMap<>();
                            messageBodyDetails.put(messageSenderReference + "/" + messagePushID, messageTextBody);
                            messageBodyDetails.put(messageReceiverReference + "/" + messagePushID, messageTextBody);

                            rootReference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(ChatActivity.this, "Message Sent Successfully, Done ya Desha", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        Toast.makeText(ChatActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }

                        else
                        {
                            Toast.makeText(ChatActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            else
            {
                Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void displayLastSeen()
    {
        rootReference
                .child("Users")
                .child(messageReceiverID)
                .addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.child("userState").hasChild("state"))
                {
                    String date = snapshot.child("userState").child("date").getValue().toString();
                    String time = snapshot.child("userState").child("time").getValue().toString();
                    String state = snapshot.child("userState").child("state").getValue().toString();

                    if (state.equals("online"))
                    {
                        userLastSeen.setText("online");
                    }
                    else if (state.equals("offline"))
                    {
                        userLastSeen.setText("Last seen: " + date + " " + time);
                    }
                }
                else
                {
                    userLastSeen.setText("offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(ChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openGallery()
    {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .setAspectRatio(1   , 1)
                .start(ChatActivity.this);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        Toast.makeText(ChatActivity.this, messageSenderID + "\n" + messageReceiverID, Toast.LENGTH_LONG).show();

        rootReference
                .child("Messages")
                .child(messageSenderID)
                .child(messageReceiverID)
                .addChildEventListener(new ChildEventListener()
                {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
                    {
                        Messages messages = snapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        //3a4an y3ml scroll le el-Chat
                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
                    {
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
                        Toast.makeText(ChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void sendMessage()
    {
        String messageText = messageInputText.getText().toString();


        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(ChatActivity.this, "Please input message here", Toast.LENGTH_SHORT).show();
            messageInputText.requestFocus();
            return;
        }
        else
        {

            String messageSenderReference = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverReference = "Messages/" + messageReceiverID + "/" + messageSenderID;

          DatabaseReference userMessageKeyReference = rootReference
                    .child("Messages")
                    .child(messageSenderID)
                    .child(messageReceiverID)
                    .push();

            String messagePushID = userMessageKeyReference.getKey();
            HashMap<String, Object> messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);

            HashMap<String, Object> messageBodyDetails = new HashMap<>();
            messageBodyDetails.put(messageSenderReference + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverReference + "/" + messagePushID, messageTextBody);

            rootReference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "Message Sent Successfully, Done ya Desha", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}