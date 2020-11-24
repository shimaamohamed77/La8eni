package com.klmni.la8eni.ui.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

public class ContactsFragment extends Fragment
{

    private View contactView;

    private RecyclerView myContactList;

    private DatabaseReference contactReference, userReference;
    private FirebaseAuth mAuth;

    private String currentUserID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        contactView = inflater.inflate(R.layout.fragment_contacts, container, false);
        return contactView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        iniitialView();
        initialDatabase();

    }

    private void iniitialView()
    {
        myContactList = contactView.findViewById(R.id.contact_list);
        myContactList.setLayoutManager(new LinearLayoutManager(getContext() , RecyclerView.VERTICAL , false));
    }

    private void initialDatabase()
    {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        contactReference = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    public void onStart()
    {
        super.onStart();


        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions
                .Builder<Contacts>()
                .setQuery(contactReference , Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, contactViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, contactViewHolder>(options)
        {

            @Override
            protected void onBindViewHolder(@NonNull final contactViewHolder holder, int position, @NonNull Contacts model)
            {
                String userID = getRef(position).getKey();

                userReference.child(userID).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if (snapshot.exists())
                        {

                            if (snapshot.child("userState").hasChild("state"))
                            {
                                String date = snapshot.child("userState").child("date").getValue().toString();
                                String time = snapshot.child("userState").child("time").getValue().toString();
                                String state = snapshot.child("userState").child("state").getValue().toString();

                                if (state.equals("online"))
                                {
                                    holder.onlineIcon.setVisibility(View.VISIBLE);
                                }
                                else if (state.equals("offline"))
                                {
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);
                                }
                            }
                            else
                            {
                                holder.onlineIcon.setVisibility(View.INVISIBLE);
                            }

                            if (snapshot.hasChild("image"))
                            {
                                String userImage = snapshot.child("image").getValue().toString();
                                String profileStatus = snapshot.child("status").getValue().toString();
                                String profileName = snapshot.child("name").getValue().toString();

                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
                                Picasso.get()
                                        .load(userImage)
                                        .placeholder(R.drawable.profile_image)
                                        .into(holder.profileImage);
                            }
                            else
                            {
                                String profileStatus = snapshot.child("status").getValue().toString();
                                String profileName = snapshot.child("name").getValue().toString();

                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
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

            @NonNull
            @Override
            public contactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout , parent , false);
                return new contactViewHolder(view);
            }

        };

        myContactList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class contactViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profileImage;
        ImageView onlineIcon;

        public contactViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            onlineIcon = itemView.findViewById(R.id.user_online_status);

        }
    }
}