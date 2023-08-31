package org.mukee.chatapp.muchat.Fragments;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.mukee.chatapp.muchat.Profile.ProfileActivity;
import org.mukee.chatapp.muchat.R;
import org.mukee.chatapp.muchat.Model.Requestss;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;



/**
 * A simple {@link Fragment} subclass.
 */

public class RequestsFragment extends Fragment {

    private View view;
    private RecyclerView request_list;
    private Context context;

    private DatabaseReference databaseReference;
    String user_UId;
    private DatabaseReference userDatabaseReference;

    // for accept and cancel mechanism
    private DatabaseReference friendsDatabaseReference;
    private DatabaseReference friendReqDatabaseReference;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_requests, container, false);

        request_list = view.findViewById(R.id.requestList);
        request_list.setHasFixedSize(true);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user_UId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("friend_requests").child(user_UId);

        friendsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("friends");
        friendReqDatabaseReference = FirebaseDatabase.getInstance().getReference().child("friend_requests");


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        //linearLayoutManager.setStackFromEnd(true);
        request_list.setHasFixedSize(true);
        request_list.setLayoutManager(linearLayoutManager);


        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Requestss> recyclerOptions = new FirebaseRecyclerOptions.Builder<Requestss>()
                .setQuery(databaseReference, Requestss.class)
                .build();

        FirebaseRecyclerAdapter<Requestss, RequestsVH> adapter = new FirebaseRecyclerAdapter<Requestss, RequestsVH>(recyclerOptions) {
            @Override
            public void onBindViewHolder(@NonNull final RequestsVH holder, int position, @NonNull Requestss model) {
               final String userID = getRef(position).getKey();
                // handling accept/cancel button
                DatabaseReference getTypeReference = getRef(position).child("request_type").getRef();
                getTypeReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String requestType = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                            holder.verified_icon.setVisibility(View.GONE);

                            if (requestType.equals("received")){
                                holder.re_icon.setVisibility(View.VISIBLE);
                                holder.se_icon.setVisibility(View.GONE);
                                assert userID != null;
                                userDatabaseReference.child(userID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        final String userName = Objects.requireNonNull(dataSnapshot.child("user_name").getValue()).toString();
                                        final String userVerified = Objects.requireNonNull(dataSnapshot.child("verified").getValue()).toString();
                                        final String userThumbPhoto = Objects.requireNonNull(dataSnapshot.child("user_thumb_image").getValue()).toString();
                                        final String user_status = Objects.requireNonNull(dataSnapshot.child("user_status").getValue()).toString();

                                        holder.name.setText(userName);
                                        holder.status.setText(user_status);

                                        if(!userThumbPhoto.equals("default_image")) { // default image condition for new user
                                            Picasso.get()
                                                    .load(userThumbPhoto)
                                                    .networkPolicy(NetworkPolicy.OFFLINE) // for Offline
                                                    .placeholder(R.drawable.default_profile_image)
                                                    .into(holder.user_photo, new Callback() {
                                                        @Override
                                                        public void onSuccess() {
                                                        }
                                                        @Override
                                                        public void onError(Exception e) {
                                                            Picasso.get()
                                                                    .load(userThumbPhoto)
                                                                    .placeholder(R.drawable.default_profile_image)
                                                                    .into(holder.user_photo);
                                                        }
                                                    });
                                        }

                                        if (userVerified.contains("true")){
                                            holder.verified_icon.setVisibility(View.VISIBLE);
                                        }

                                        Objects.requireNonNull(holder).itemView.setOnClickListener(v -> {
                                            CharSequence[] options =  new CharSequence[]{"Accept Request", "Cancel Request", String.format("%s's profile", userName)};

                                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                            builder.setItems(options, (dialog, which) -> {

                                                if (which == 0){
                                                    Calendar myCalendar = Calendar.getInstance();
                                                    @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate = new SimpleDateFormat("EEEE, dd MMM, yyyy");
                                                    final String friendshipDate = currentDate.format(myCalendar.getTime());

                                                    friendsDatabaseReference.child(user_UId).child(userID).child("date").setValue(friendshipDate)
                                                            .addOnCompleteListener(task -> friendsDatabaseReference.child(userID).child(user_UId).child("date").setValue(friendshipDate)
                                                                    .addOnCompleteListener(task1 -> {
                                                                        /*
                                                                           because of accepting friend request,
                                                                           there have no more request them. So, for delete these node
                                                                         */
                                                                        friendReqDatabaseReference.child(user_UId).child(userID).removeValue()
                                                                                .addOnCompleteListener(task11 -> {
                                                                                    if (task11.isSuccessful()){
                                                                                        // delete from users friend_requests node, receiver >> sender > values
                                                                                        friendReqDatabaseReference.child(userID).child(user_UId).removeValue()
                                                                                                .addOnCompleteListener(task111 -> {
                                                                                                    if (task111.isSuccessful()){
                                                                                                        // after deleting data
                                                                                                        Snackbar snackbar = Snackbar
                                                                                                                .make(view, "This person is now your friend", Snackbar.LENGTH_LONG);
                                                                                                        // Changing message text color
                                                                                                        View sView = snackbar.getView();
                                                                                                        assert getContext() != null;
                                                                                                        sView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                                                                                                        TextView textView = sView.findViewById(com.google.android.material.R.id.snackbar_text);//r_profileImage);//);
                                                                                                        textView.setTextColor(Color.WHITE);
                                                                                                        snackbar.show();
                                                                                                    }
                                                                                                });

                                                                                    }
                                                                                }); //

                                                                    }));
                                                    }


                                                if (which == 1){
                                                    //for cancellation, delete data from user nodes
                                                    // delete from, sender >> receiver > values
                                                    friendReqDatabaseReference.child(user_UId).child(userID).removeValue()
                                                            .addOnCompleteListener(task -> {
                                                                if (task.isSuccessful()){
                                                                    // delete from, receiver >> sender > values
                                                                    friendReqDatabaseReference.child(userID).child(user_UId).removeValue()
                                                                            .addOnCompleteListener(task12 -> {
                                                                                if (task12.isSuccessful()){
                                                                                    Toast.makeText(getActivity(), "Cancel Request", Toast.LENGTH_SHORT).show();
                                                                                    Snackbar snackbar = Snackbar
                                                                                            .make(view, "Canceled Request", Snackbar.LENGTH_LONG);
                                                                                    // Changing message text color
                                                                                    View sView = snackbar.getView();
                                                                                    assert getContext() != null;
                                                                                    sView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                                                                                    TextView textView = sView.findViewById(com.google.android.material.R.id.snackbar_text); //r_profileName);//snackbar_text);
                                                                                    textView.setTextColor(Color.WHITE);
                                                                                    snackbar.show();

                                                                                }
                                                                            });

                                                                }
                                                            });
                                                }
                                                if (which == 2){
                                                    Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                                    profileIntent.putExtra("visitUserId", userID);
                                                    startActivity(profileIntent);
                                                }

                                            });
                                            builder.show();
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                            if (requestType.equals("sent")){
                                holder.re_icon.setVisibility(View.GONE);
                                holder.se_icon.setVisibility(View.VISIBLE);
                                assert userID != null;
                                userDatabaseReference.child(userID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        final String userName = Objects.requireNonNull(dataSnapshot.child("user_name").getValue()).toString();
                                        final String userVerified = Objects.requireNonNull(dataSnapshot.child("verified").getValue()).toString();
                                        final String userThumbPhoto = Objects.requireNonNull(dataSnapshot.child("user_thumb_image").getValue()).toString();
                                        final String user_status = Objects.requireNonNull(dataSnapshot.child("user_status").getValue()).toString();

                                        holder.name.setText(userName);
                                        holder.status.setText(user_status);

                                        if(!userThumbPhoto.equals("default_image")) { // default image condition for new user
                                            Picasso.get()
                                                    .load(userThumbPhoto)
                                                    .networkPolicy(NetworkPolicy.OFFLINE) // for Offline
                                                    .placeholder(R.drawable.default_profile_image)
                                                    .into(holder.user_photo, new Callback() {
                                                        @Override
                                                        public void onSuccess() {
                                                        }
                                                        @Override
                                                        public void onError(Exception e) {
                                                            Picasso.get()
                                                                    .load(userThumbPhoto)
                                                                    .placeholder(R.drawable.default_profile_image)
                                                                    .into(holder.user_photo);
                                                        }
                                                    });
                                        }

                                        if (userVerified.contains("true")){
                                            holder.verified_icon.setVisibility(View.VISIBLE);
                                        }

                                        holder.itemView.setOnClickListener(v -> {
                                            CharSequence[] options = new CharSequence[]{"Cancel Sent Request", userName + "'s profile"};

                                            if (getActivity() != null) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                                builder.setItems(options, (dialog, which) -> {
                                                    if (which == 0) {
                                                        //for cancellation, delete data from user nodes
                                                        // delete from, sender >> receiver > values
                                                        friendReqDatabaseReference.child(user_UId).child(userID).removeValue()
                                                                .addOnCompleteListener(task -> {
                                                                    if (task.isSuccessful()) {
                                                                        // delete from, receiver >> sender > values
                                                                        friendReqDatabaseReference.child(userID).child(user_UId).removeValue()
                                                                                .addOnCompleteListener(task13 -> {
                                                                                    if (task13.isSuccessful()) {
                                                                                        Snackbar snackbar = Snackbar
                                                                                                .make(view, "Cancel Sent Request", Snackbar.LENGTH_LONG);
                                                                                        // Changing message text color
                                                                                        View sView = snackbar.getView();
                                                                                        assert getContext() != null;
                                                                                        sView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                                                                                        TextView textView = sView.findViewById(com.google.android.material.R.id.snackbar_text);//r_profileName);//snackbar_text);
                                                                                        textView.setTextColor(Color.WHITE);
                                                                                        snackbar.show();
                                                                                    }
                                                                                });

                                                                    }
                                                                });
                                                    }
                                                    if (which == 1) {
                                                        Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                                        profileIntent.putExtra("visitUserId", userID);
                                                        startActivity(profileIntent);
                                                    }

                                                });
                                                builder.show();
                                            }else {
                                                Toast.makeText(getActivity(), "Errorrr makkk", Toast.LENGTH_SHORT).show();
                                            }




                                        });
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public RequestsVH onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.request_single, viewGroup, false);
                return new RequestsVH(view);
            }
        };
        request_list.setAdapter(adapter);
        adapter.startListening();
    }

    @Nullable
    @Override
    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public static class RequestsVH extends RecyclerView.ViewHolder{
        TextView name, status;
        CircleImageView user_photo;
        ImageView re_icon, se_icon, verified_icon;
        public RequestsVH(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.r_profileName);
            user_photo = itemView.findViewById(R.id.r_profileImage);
            status = itemView.findViewById(R.id.r_profileStatus);
            re_icon = itemView.findViewById(R.id.receivedIcon);
            se_icon = itemView.findViewById(R.id.sentIcon);
            verified_icon = itemView.findViewById(R.id.verifiedIcon);
        }
    }

}
