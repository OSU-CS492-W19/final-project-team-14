package com.example.spotifyremote;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.spotifyremote.utils.SpotifyUtils;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.DeviceViewHolder> {
    private ArrayList<SpotifyUtils.SpotifyDevice> mSpotifyDevices;
    private OnDeviceClickListener mOnDeviceClickListener;

    public interface OnDeviceClickListener {
        void onDeviceClick(SpotifyUtils.SpotifyDevice device);
    }

    public DevicesAdapter(OnDeviceClickListener listener) {
        mOnDeviceClickListener = listener;
    }

    public void updateDevices(ArrayList<SpotifyUtils.SpotifyDevice> devices) {
        mSpotifyDevices = devices;
        notifyDataSetChanged();
    }

    public int getItemCount() {
        if (mSpotifyDevices != null) {
            return mSpotifyDevices.size();
        } else {
            return 0;
        }
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.device_list_item, parent, false);
        return new DeviceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        holder.bind(mSpotifyDevices.get(position));
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mDeviceNameTV;
        private TextView mDeviceTypeTV;

        public DeviceViewHolder(View itemView) {
            super(itemView);
            mDeviceNameTV = itemView.findViewById(R.id.tv_device_name);
            mDeviceTypeTV = itemView.findViewById(R.id.tv_device_type);
            itemView.setOnClickListener(this);
        }

        public void bind(SpotifyUtils.SpotifyDevice device) {
            mDeviceNameTV.setText(device.name);
            mDeviceTypeTV.setText(device.type);
        }

        @Override
        public void onClick(View v) {
            mOnDeviceClickListener.onDeviceClick(mSpotifyDevices.get(getAdapterPosition()));
        }
    }
}
