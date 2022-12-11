package com.myapp.artificer.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.artificer.Models.JobsModel;
import com.myapp.artificer.R;

import java.util.List;

public class UserPreviousJobsAdapter extends RecyclerView.Adapter<UserPreviousJobsAdapter.PreviousJobsViewHolder>{

    private Context mContext;
    private List<JobsModel> jobsModels;
    private OnItemClickListener mListener;

    public UserPreviousJobsAdapter(Context mContext, List<JobsModel> jobsModels) {
        this.mContext = mContext;
        this.jobsModels = jobsModels;
    }

    @NonNull
    @Override
    public UserPreviousJobsAdapter.PreviousJobsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.single_previousjob_item, parent, false);
        return new PreviousJobsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserPreviousJobsAdapter.PreviousJobsViewHolder holder, int position) {
        JobsModel currentModel = jobsModels.get(position);
        holder.name.setText("Name: "+currentModel.getWorkerName());
        if (currentModel.getRatings() != null) {
            holder.rating.setText("Rating: " + currentModel.getRatings());
        }

    }

    @Override
    public int getItemCount() {
        return jobsModels.size();
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class PreviousJobsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name,rating;
        public PreviousJobsViewHolder(@NonNull View itemView) {
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
