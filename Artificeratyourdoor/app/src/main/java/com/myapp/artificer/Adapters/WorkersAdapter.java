package com.myapp.artificer.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.myapp.artificer.Models.WorkerModel;
import com.myapp.artificer.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class WorkersAdapter extends RecyclerView.Adapter<WorkersAdapter.WorkersViewHolder> {
    private Context mContext;
    private List<WorkerModel> workerModels;
    private OnItemClickListener mListener;

    public WorkersAdapter(Context mContext, List<WorkerModel> workerModels) {
        this.mContext = mContext;
        this.workerModels = workerModels;
    }

    @NonNull
    @Override
    public WorkersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.single_worker_item, parent, false);
        return new WorkersViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull WorkersViewHolder holder, int position) {
        WorkerModel currentModel = workerModels.get(position);
        if(currentModel.getImageUrl()!=null) {
            Glide.with(mContext).load(currentModel.getImageUrl()).into(holder.imageView);
        }
        holder.name.setText(String.valueOf(currentModel.getName()));
        if (currentModel.getLat() != 0 && currentModel.getLon() != 0) {
            try {
                Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
                List<Address> addresses = null;
                addresses = geocoder.getFromLocation(currentModel.getLat(), currentModel.getLon(), 1);
                String subLocality = "";
                if(addresses.get(0).getSubLocality()!=null){
                    subLocality = addresses.get(0).getSubLocality() +", ";
                }
                String thoroughfare = "";
                if(addresses.get(0).getThoroughfare()!=null){
                    thoroughfare = addresses.get(0).getThoroughfare() +", ";
                }
                String locality = "";
                if(addresses.get(0).getLocality()!=null){
                    locality = addresses.get(0).getLocality();
                }
                holder.location.setText(thoroughfare+subLocality+locality);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            holder.location.setText("No location available");
        }
    }

    @Override
    public int getItemCount() {
        return workerModels.size();
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class WorkersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView name, location;
        ImageView imageView;

        public WorkersViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.nameTV);
            location = itemView.findViewById(R.id.locationTV);
            imageView = itemView.findViewById(R.id.imageView);
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
