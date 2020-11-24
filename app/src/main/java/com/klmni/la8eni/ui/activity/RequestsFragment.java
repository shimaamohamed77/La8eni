package com.klmni.la8eni.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.klmni.la8eni.R;
import com.klmni.la8eni.model.Contacts;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsFragment extends Fragment
{
    private View requestView;
    private RecyclerView requestList;

    private String currentUserID;

    private DatabaseReference chatRequestReference, userReference, contactReference;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        requestView = inflater.inflate(R.layout.fragment_requests , container , false);
        return requestView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);


        initialView();

        initialDatabase();

    }

    private void initialView()
    {
        requestList = requestView.findViewById(R.id.chat_request_list);
        requestList.setLayoutManager(new LinearLayoutManager(getContext() , RecyclerView.VERTICAL , false));
    }

    private void initialDatabase()
    {
        chatRequestReference = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        contactReference = FirebaseDatabase.getInstance().getReference().child("Contacts");
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions
                .Builder<Contacts>()
                .setQuery(chatRequestReference.child(currentUserID) , Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, requestViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, requestViewHolder>(options)
        {
            @NonNull
            @Override
            public requestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.users_display_layout , parent , false);
                return new requestViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final requestViewHolder holder, int position, @NonNull Contacts model)
            {
                holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                final String listUserID = getRef(position).getKey();

                DatabaseReference getTypeReference = getRef(position).child("request_type").getRef();
                getTypeReference.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if (snapshot.exists())
                        {
                            String type = snapshot.getValue().toString();

                            if (type.equals("received"))
                            {
                                userReference.child(listUserID).addValueEventListener(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot)
                                    {
                                        if (snapshot.hasChild("image"))
                                        {
                                            String requestProfileImage = snapshot.child("image").getValue().toString();

                                            Picasso.get()
                                                    .load(requestProfileImage)
                                                    .placeholder(R.drawable.profile_image)
                                                    .into(holder.profileImage);

                                        }

                                            String requestUserStatus = snapshot.child("status").getValue().toString();
                                            final String requestUserName = snapshot.child("name").getValue().toString();


                                            holder.userName.setText(requestUserName);
                                            holder.userStatus.setText( requestUserStatus /*+ "connect with you" */ );


                                        holder.itemView.setOnClickListener(new View.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View view)
                                            {
                                                CharSequence option[] = new CharSequence[]
                                                        {
                                                                "Accept",
                                                                "Cancel"
                                                        };

                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle(requestUserName + " Chat Request");
                                                builder.setItems(option, new DialogInterface.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i)
                                                    {
                                                        if (i == 0)
                                                        {
                                                            contactReference.child(currentUserID)
                                                                    .child(listUserID)
                                                                    .child("Contact")
                                                                    .setValue("saved")
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                    {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                        {
                                                                            if (task.isSuccessful())
                                                                            {
                                                                                contactReference.child(listUserID)
                                                                                        .child(currentUserID)
                                                                                        .child("Contact")
                                                                                        .setValue("saved")
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                        {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                                            {
                                                                                                if (task.isSuccessful())
                                                                                                {
                                                                                                    chatRequestReference.child(currentUserID)
                                                                                                            .child(listUserID)
                                                                                                            .removeValue()
                                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                                            {
                                                                                                                @Override
                                                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                                                {
                                                                                                                    if (task.isSuccessful())
                                                                                                                    {
                                                                                                                        chatRequestReference.child(listUserID)
                                                                                                                                .child(currentUserID)
                                                                                                                                .removeValue()
                                                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                                                                {
                                                                                                                                    @Override
                                                                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                                                                    {
                                                                                                                                        if (task.isSuccessful())
                                                                                                                                        {
                                                                                                                                            Toast.makeText(getContext(), "New Contact Saved", Toast.LENGTH_SHORT).show();
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
                                                                    });
                                                        }
                                                        if (i == 1)
                                                        {
                                                            chatRequestReference.child(currentUserID)
                                                                    .child(listUserID)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                    {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                        {
                                                                            if (task.isSuccessful())
                                                                            {
                                                                                chatRequestReference.child(listUserID)
                                                                                        .child(currentUserID)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                        {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                                            {
                                                                                                if (task.isSuccessful())
                                                                                                {
                                                                                                    Toast.makeText(getContext(), "Contact Deleted", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });

                                                builder.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error)
                                    {
                                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            else if (type.equals("sent"))
                            {
                                Button request_sent_btn = holder.itemView.findViewById(R.id.request_accept_btn);
                                request_sent_btn.setText("Req Sent");

                                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.INVISIBLE);

                                userReference.child(listUserID).addValueEventListener(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot)
                                    {
                                        if (snapshot.hasChild("image"))
                                        {
                                            String requestProfileImage = snapshot.child("image").getValue().toString();

                                            Picasso.get()
                                                    .load(requestProfileImage)
                                                    .placeholder(R.drawable.profile_image)
                                                    .into(holder.profileImage);

                                        }

                                        String requestUserStatus = snapshot.child("status").getValue().toString();
                                        final String requestUserName = snapshot.child("name").getValue().toString();


                                        holder.userName.setText(requestUserName);
                                        holder.userStatus.setText( "You have sent a request to "+ requestUserName /*+ "connect with you" */ );


                                        holder.itemView.setOnClickListener(new View.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View view)
                                            {
                                                CharSequence option[] = new CharSequence[]
                                                        {
                                                                "Cancel Chat Request"
                                                        };

                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Already Sent Request");
                                                builder.setItems(option, new DialogInterface.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i)
                                                    {

                                                        if (i == 0)
                                                        {
                                                            chatRequestReference.child(currentUserID)
                                                                    .child(listUserID)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                    {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                        {
                                                                            if (task.isSuccessful())
                                                                            {
                                                                                chatRequestReference.child(listUserID)
                                                                                        .child(currentUserID)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                        {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                                            {
                                                                                                if (task.isSuccessful())
                                                                                                {
                                                                                                    Toast.makeText(getContext(), "You have cancelled the chat request", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });

                                                builder.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error)
                                    {
                                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        requestList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class requestViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profileImage;
        Button acceptButton, cancelButton;

        public requestViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            acceptButton = itemView.findViewById(R.id.request_accept_btn);
            cancelButton = itemView.findViewById(R.id.request_cancel_btn);

        }
    }
}