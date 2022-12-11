package com.myapp.artificer.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.myapp.artificer.Models.JobsModel;
import com.myapp.artificer.Models.UserModel;
import com.myapp.artificer.R;

import java.util.List;

public class WorkerPreviousJobsAdapter extends RecyclerView.Adapter<WorkerPreviousJobsAdapter.WorkerPreviousJobsViewHolder> {

    private Context mContext;
    private List<JobsModel> jobsModels;
    private OnItemClickListener mListener;
    DatabaseReference mDatabaseRef;


    public WorkerPreviousJobsAdapter(Context mContext, List<JobsModel> jobsModels) {
        this.mContext = mContext;
        this.jobsModels = jobsModels;
    }

    @NonNull
    @Override
    public WorkerPreviousJobsAdapter.WorkerPreviousJobsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.single_previousjob_item, parent, false);
        return new WorkerPreviousJobsAdapter.WorkerPreviousJobsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkerPreviousJobsAdapter.WorkerPreviousJobsViewHolder holder, int position) {
        JobsModel currentModel = jobsModels.get(position);

        if (currentModel.getRatings() != null) {
            holder.rating.setText("Rating: " + currentModel.getRatings());
        }
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("user");
        mDatabaseRef.orderByChild("id").equalTo(currentModel.getUserId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("asdf", "onDataChange: 111"+dataSnapshot);
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    UserModel userModel = ds.getValue(UserModel.class);
                    holder.name.setText("Name: " + userModel.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }

    @Override
    public int getItemCount() {
        return jobsModels.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class WorkerPreviousJobsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name, rating;

        public WorkerPreviousJobsViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.nameTV);
            rating = itemView.findViewById(R.id.ratingCountText);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (mListener != null) {
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position);
                }
            }
        }
    }
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
}
