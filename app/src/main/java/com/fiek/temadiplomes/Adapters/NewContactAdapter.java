package com.fiek.temadiplomes.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.fiek.temadiplomes.Utils.Constants;
import com.fiek.temadiplomes.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.tapadoo.alerter.Alerter;
import java.util.ArrayList;
import java.util.List;

public class NewContactAdapter extends RecyclerView.Adapter<NewContactAdapter.ViewHolder>  {
    private List<String> mData;
    private LayoutInflater mInflater;
    private NewContactAdapter.ItemClickListener mClickListener;
    private Context mContext;
    private List<String> Uid = new ArrayList<>();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref = database.getReference();
    private Boolean isContact;

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

                Picasso.get().load(snapshot.child(Constants.IMAGE_FIELD).getValue().toString()).into(holder.profile_image);

                assert FirebaseAuth.getInstance().getUid() != null;

                ref.child(FirebaseAuth.getInstance().getUid()).child(Constants.FRIENDS_FILED).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(mData.get(position))){
                            holder.addIcon.setBackgroundResource(R.drawable.ic_check);
                            isContact = true;
                        } else {
                            holder.addIcon.setBackgroundResource(R.drawable.ic_addcontact);
                            isContact = false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

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
        de.hdodenhof.circleimageview.CircleImageView profile_image;

        ViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            callTime = itemView.findViewById(R.id.calltime);
            addIcon = itemView.findViewById(R.id.addIcon);
            profile_image = itemView.findViewById(R.id.profile_image);
            itemView.setOnClickListener(this);

            addIcon.setOnClickListener(v -> {
                try{
                    if (isContact){
                        ref.child(FirebaseAuth.getInstance().getUid()).child(Constants.FRIENDS_FILED).child(Uid.get(getAdapterPosition())).removeValue();
                        Alerter.create((Activity) mContext).setTitle(username.getText())
                                .setText("Has been removed from contacts")
                                .setBackgroundColorRes(R.color.twitterBlue)
                                .show();
                    } else {
                        ref.child(FirebaseAuth.getInstance().getUid()).child(Constants.FRIENDS_FILED).child(Uid.get(getAdapterPosition())).setValue(Uid.get(getAdapterPosition()));
                        Alerter.create((Activity) mContext).setTitle(username.getText())
                                .setText("Has been added to contacts")
                                .setBackgroundColorRes(R.color.twitterBlue)
                                .show();
                    }

                    isContact = !isContact;
                } catch (Exception ex){
                    Toast.makeText(mContext, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
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

    void setOnClickListener(NewContactAdapter.ItemClickListener itemClickListener){
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
