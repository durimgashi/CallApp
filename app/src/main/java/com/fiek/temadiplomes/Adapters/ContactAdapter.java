package com.fiek.temadiplomes.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.fiek.temadiplomes.Utils.Constants;
import com.fiek.temadiplomes.R;
import com.fiek.temadiplomes.VideoCallActivity;
import com.fiek.temadiplomes.VoiceCallActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private List<String> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context mContext;
    private List<String> Uid = new ArrayList<>();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref = database.getReference();

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

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Uid.add(mData.get(position));
        ref.child(mData.get(position)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.username.setText(snapshot.child(Constants.USERNAME_FIELD).getValue().toString());
                if (Boolean.parseBoolean(snapshot.child(Constants.AVAILABLE_FIELD).getValue().toString())) {
                    holder.callTime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_available, 0, 0, 0);
                    holder.callTime.setText(" Online");
                } else {
                    holder.callTime.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_unavailable, 0, 0, 0);
                    holder.callTime.setText(" Offline");
                }

                Picasso.get().load(snapshot.child(Constants.IMAGE_FIELD).getValue().toString()).into(holder.profile_image);
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
        ImageView phoneIcon, videoIcon;
        de.hdodenhof.circleimageview.CircleImageView profile_image;

        ViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            callTime = itemView.findViewById(R.id.calltime);
            phoneIcon = itemView.findViewById(R.id.phoneIcon);
            videoIcon = itemView.findViewById(R.id.videoIcon);
            profile_image = itemView.findViewById(R.id.profile_image);
            itemView.setOnClickListener(this);

            videoIcon.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, VideoCallActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("friendUID", Uid.get(getAdapterPosition()));
                mContext.startActivity(intent);
            });

            phoneIcon.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, VoiceCallActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("friendUID", Uid.get(getAdapterPosition()));
                mContext.startActivity(intent);
            });
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    String getItem(int id) {
        return mData.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}