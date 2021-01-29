package com.fiek.temadiplomes.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fiek.temadiplomes.ContactsActivity;
import com.fiek.temadiplomes.Model.User;
import com.fiek.temadiplomes.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NewContactAdapter extends RecyclerView.Adapter<NewContactAdapter.ViewHolder>  {
    private List<String> mData;
    private LayoutInflater mInflater;
    private NewContactAdapter.ItemClickListener mClickListener;
    private Context mContext;
    private List<String> Uid = new ArrayList<>();
    private CollectionReference firebaseRef = FirebaseFirestore.getInstance().collection("users");
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref = database.getReference();

    public NewContactAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mContext = context;
    }

    @NonNull
    @Override
    public NewContactAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.contact_add, parent, false);
        return new NewContactAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewContactAdapter.ViewHolder holder, int position) {
        Uid.add(mData.get(position));


//        Toast.makeText(mContext, mData.get(position), Toast.LENGTH_SHORT).show();
//
        ref.child(mData.get(position)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.username.setText(snapshot.child("username").getValue().toString());
                if (Boolean.parseBoolean(String.valueOf(snapshot.child("available").getValue()))){
                    holder.callTime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_available, 0, 0, 0);
                    holder.callTime.setText(" Online");
                } else {
                    holder.callTime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_unavailable, 0, 0, 0);
                    holder.callTime.setText(" Offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView username, callTime;
        ImageView addIcon;

        ViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            callTime = itemView.findViewById(R.id.calltime);
            addIcon = itemView.findViewById(R.id.addIcon);
            itemView.setOnClickListener(this);

//            addIcon.setOnClickListener(v -> firebaseRef.document().update("incoming", ""));
            addIcon.setOnClickListener(v -> {
                firebaseRef.document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                        .update("friends", FieldValue.arrayUnion(Uid.get(getAdapterPosition())));
            });
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id);
    }

    void setmClickListener(NewContactAdapter.ItemClickListener itemClickListener){
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
