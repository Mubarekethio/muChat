package org.mukee.chatapp.muchat.Search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.mukee.chatapp.muchat.Profile.ProfileActivity;
import org.mukee.chatapp.muchat.R;
import org.mukee.chatapp.muchat.Model.ProfileInfo;

import cn.zhaiyifan.rememberedittext.RememberEditText;
import de.hdodenhof.circleimageview.CircleImageView;
import xyz.hasnat.sweettoast.SweetToast;

public class SearchActivity extends AppCompatActivity {

    private EditText searchInput;

    private RecyclerView peoples_list;
    private DatabaseReference peoplesDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // appbar / toolbar
        Toolbar toolbar = findViewById(R.id.search_appbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater)
                this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.appbar_search, null);
        actionBar.setCustomView(view);

        searchInput = findViewById(R.id.serachInput);
        TextView notFoundTV = findViewById(R.id.notFoundTV);
        ImageView backButton = findViewById(R.id.backButton);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchPeopleProfile(searchInput.getText().toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        // Setup recycler view
        peoples_list = findViewById(R.id.SearchList);
        peoples_list.setHasFixedSize(true);
        peoples_list.setLayoutManager(new LinearLayoutManager(this));

        peoplesDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        peoplesDatabaseReference.keepSynced(true); // for offline

    }


    /**
     *  FirebaseUI for Android — UI Bindings for Firebase
     *  Library link- https://github.com/firebase/FirebaseUI-Android
     */
    private void searchPeopleProfile(final String searchString) {
        Query searchQuery = peoplesDatabaseReference.orderByChild("search_name")
                .startAt(searchString).endAt(searchString + "\uf8ff");
        //searchQuery = peoplesDatabaseReference.orderByChild("search_name").equalTo(searchString);

        FirebaseRecyclerOptions<ProfileInfo> recyclerOptions = new FirebaseRecyclerOptions.Builder<ProfileInfo>()
                .setQuery(searchQuery, ProfileInfo.class)
                .build();

        FirebaseRecyclerAdapter<ProfileInfo, SearchPeopleVH> adapter = new FirebaseRecyclerAdapter<ProfileInfo, SearchPeopleVH>(recyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull SearchPeopleVH holder, @SuppressLint("RecyclerView") final int position, @NonNull ProfileInfo model) {
                holder.name.setText(model.getUser_name());
                holder.status.setText(model.getUser_status());

                Picasso.get()
                        .load(model.getUser_image())
                        .networkPolicy(NetworkPolicy.OFFLINE) // for Offline
                        .placeholder(R.drawable.default_profile_image)
                        .into(holder.profile_pic);

                holder.verified_icon.setVisibility(View.GONE);
                if (model.getVerified().contains("true")){
                    holder.verified_icon.setVisibility(View.VISIBLE);
                } else {
                    holder.verified_icon.setVisibility(View.GONE);
                }

                /**on list >> clicking item, then, go to single user profile*/
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(position).getKey();
                        Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
                        intent.putExtra("visitUserId", visit_user_id);
                        startActivity(intent);
                    }
                });


            }

            @NonNull
            @Override
            public SearchPeopleVH onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_single_profile_display, viewGroup, false);
                return new SearchPeopleVH(view);
            }
        };
        peoples_list.setAdapter(adapter);
        adapter.startListening();
    }

    public static class SearchPeopleVH extends RecyclerView.ViewHolder{
        TextView name, status;
        CircleImageView profile_pic;
        ImageView verified_icon;
        public SearchPeopleVH(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.all_user_name);
            status = itemView.findViewById(R.id.all_user_status);
            profile_pic = itemView.findViewById(R.id.all_user_profile_img);
            verified_icon = itemView.findViewById(R.id.verifiedIcon);
        }
    }


    // Toolbar menu for clearing search history
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.menu_clear_search){
            RememberEditText.clearCache(SearchActivity.this);
            SweetToast.info(this, "Search history cleared successfully.");
            this.finish();
        }
        return true;
    }
}
