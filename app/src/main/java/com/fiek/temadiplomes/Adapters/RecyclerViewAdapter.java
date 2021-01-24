package com.fiek.temadiplomes.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fiek.temadiplomes.ContactsActivity;
import com.fiek.temadiplomes.R;
import com.fiek.temadiplomes.VideoCallActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private List<String> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context mContext;

    // data is passed into the constructor
    public RecyclerViewAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mContext = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.contact, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String Uid = mData.get(position);
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(Uid);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        holder.username.setText(Objects.requireNonNull(documentSnapshot.get("username")).toString());
                        holder.calltime.setText(Uid);
                    } else {
                        return;
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(mContext, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView username, calltime;
        ImageView phoneIcon, videoIcon;

        ViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            calltime = itemView.findViewById(R.id.calltime);
            phoneIcon = itemView.findViewById(R.id.phoneIcon);
            videoIcon = itemView.findViewById(R.id.videoIcon);
            itemView.setOnClickListener(this);

//            videoIcon.setOnClickListener(v -> Toast.makeText( mContext , "Calling : " + Uid, Toast.LENGTH_SHORT).show());
            videoIcon.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, VideoCallActivity.class);
                intent.putExtra("friendUID", calltime.getText());
                mContext.startActivity(intent);
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

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
