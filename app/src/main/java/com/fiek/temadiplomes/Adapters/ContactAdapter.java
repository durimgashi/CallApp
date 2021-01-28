package com.fiek.temadiplomes.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.fiek.temadiplomes.R;
import com.fiek.temadiplomes.VideoCallActivity;
import com.fiek.temadiplomes.VoiceCallActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private List<String> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context mContext;
    private List<String> Uid = new ArrayList<>();

    public ContactAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.contact, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Uid.add(mData.get(position));
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(Uid.get(position));
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        holder.username.setText(Objects.requireNonNull(documentSnapshot.get("username")).toString());
                        if (Objects.requireNonNull(documentSnapshot.getBoolean("available"))){
                            holder.callTime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_available, 0, 0, 0);
                            holder.callTime.setText(" Online");
                        } else {
                            holder.callTime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_unavailable, 0, 0, 0);
                            holder.callTime.setText(" Offline");
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(mContext, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView username, callTime;
        ImageView phoneIcon, videoIcon;

        ViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            callTime = itemView.findViewById(R.id.calltime);
            phoneIcon = itemView.findViewById(R.id.phoneIcon);
            videoIcon = itemView.findViewById(R.id.videoIcon);
            itemView.setOnClickListener(this);

            videoIcon.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, VideoCallActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("friendUID", Uid.get(getAdapterPosition()));
                mContext.startActivity(intent);
            });

            phoneIcon.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, VoiceCallActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("friendUID", Uid.get(getAdapterPosition()));
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

    public void filterList(ArrayList<String> filterdNames) {
        this.mData = filterdNames;
        notifyDataSetChanged();
    }
}
