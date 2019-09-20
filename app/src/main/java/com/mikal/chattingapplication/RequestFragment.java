package com.mikal.chattingapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RequestFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RequestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestFragment extends Fragment {

    private View requestFragmentView;
    private RecyclerView myRequestList;

    private DatabaseReference ChatRequestRef,UsersRef,ContactsRef;
    private FirebaseAuth mAuth;
    private  String currentUserID;



    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestFragmentView=inflater.inflate(R.layout.fragment_request, container, false);

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();


        ChatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");
        ContactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");

        myRequestList=(RecyclerView) requestFragmentView.findViewById(R.id.chat_requests_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));


        return requestFragmentView;
    }


    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Contatcs> options=
                new FirebaseRecyclerOptions.Builder<Contatcs>()
                .setQuery(ChatRequestRef.child(currentUserID),Contatcs.class)
                .build();

        FirebaseRecyclerAdapter<Contatcs,RequestsViewHolder> adapter=
                new FirebaseRecyclerAdapter<Contatcs, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Contatcs model) {

                        holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);



                        final String list_user_id=getRef(position).getKey();


                        DatabaseReference getTypeRef=getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists())
                                {
                                    String type=dataSnapshot.getValue().toString();

                                    if (type.equals("received"))
                                    {
                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                if (dataSnapshot.hasChild("image"))
                                                {
                                                    final String requestProfileImage=dataSnapshot.child("image").getValue().toString();

                                                    Picasso.get().load(requestProfileImage).into(holder.profileImage);

                                                }
                                                final String requestUserName=dataSnapshot.child("name").getValue().toString();
                                                final String requestUserStatus=dataSnapshot.child("status").getValue().toString();


                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText("Wants to connect with you");

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {


                                                        CharSequence options[]=new CharSequence[]
                                                                {
                                                                        "Accept",
                                                                        "Cancel"
                                                                };

                                                        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                        builder.setTitle(requestUserName + " Chat Request");


                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {

                                                                if (which==0)
                                                                {

                                                                    ContactsRef.child(currentUserID).child(list_user_id).child("Contact")
                                                                            .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            if (task.isSuccessful())
                                                                            {
                                                                                ContactsRef.child(list_user_id).child(currentUserID).child("Contact")
                                                                                        .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                        if (task.isSuccessful())
                                                                                        {

                                                                                            ChatRequestRef.child(currentUserID).child(list_user_id)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                                                            if (task.isSuccessful())
                                                                                                            {

                                                                                                                ChatRequestRef.child(list_user_id).child(currentUserID)
                                                                                                                        .removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {

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

                                                                if (which==1)
                                                                {
                                                                    ChatRequestRef.child(currentUserID).child(list_user_id)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if (task.isSuccessful())
                                                                                    {

                                                                                        ChatRequestRef.child(list_user_id).child(currentUserID)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {

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
                                            public void onCancelled(DatabaseError databaseError) {



                                            }
                                        });
                                    }

                                    else if (type.equals("sent"))
                                    {
                                        Button request_sent_btn=holder.itemView.findViewById(R.id.request_accept_btn);
                                        request_sent_btn.setText("Request Sent");


                                        holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.INVISIBLE);


                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                if (dataSnapshot.hasChild("image"))
                                                {
                                                    final String requestProfileImage=dataSnapshot.child("image").getValue().toString();

                                                    Picasso.get().load(requestProfileImage).into(holder.profileImage);

                                                }
                                                final String requestUserName=dataSnapshot.child("name").getValue().toString();
                                                final String requestUserStatus=dataSnapshot.child("status").getValue().toString();


                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText("you have sent a request to" + requestUserName);

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {


                                                        CharSequence options[]=new CharSequence[]
                                                                {
                                                                        "Cancel Chat Request"
                                                                };

                                                        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                        builder.setTitle("Already Sent Request");


                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {


                                                                if (which==0)
                                                                {
                                                                    ChatRequestRef.child(currentUserID).child(list_user_id)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if (task.isSuccessful())
                                                                                    {

                                                                                        ChatRequestRef.child(list_user_id).child(currentUserID)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                                        if (task.isSuccessful())
                                                                                                        {

                                                                                                            Toast.makeText(getContext(), "you have cancel the chat request", Toast.LENGTH_SHORT).show();

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
                                            public void onCancelled(DatabaseError databaseError) {



                                            }
                                        });
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {



                            }
                        });

                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {


                        View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_display_layout,viewGroup,false);

                        RequestsViewHolder holder=new RequestsViewHolder(view);
                        return holder;

                    }
                };

        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }


    public  static class  RequestsViewHolder extends RecyclerView.ViewHolder
    {

        TextView userName,userStatus;
        CircleImageView profileImage;
        Button acceptButton,cancelButton;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            acceptButton=itemView.findViewById(R.id.request_accept_btn);
            cancelButton=itemView.findViewById(R.id.request_cancel_btn);

        }
    }
}
