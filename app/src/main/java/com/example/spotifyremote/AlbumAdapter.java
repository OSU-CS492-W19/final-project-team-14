package com.example.spotifyremote;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.spotifyremote.utils.SpotifyUtils;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {
    private ArrayList<SpotifyUtils.SpotifyAlbum> mSpotifyAlbums;
    private OnAlbumClickListener mOnAlbumClickListener;

    public interface OnAlbumClickListener {
        void onAlbumClick(SpotifyUtils.SpotifyAlbum album);
    }

    public AlbumAdapter(OnAlbumClickListener listener) {
        mOnAlbumClickListener = listener;
    }


    public void updateAlbums(ArrayList<SpotifyUtils.SpotifyAlbum> albums) {
        mSpotifyAlbums = albums;
        notifyDataSetChanged();
    }

    public int getItemCount() {
        if (mSpotifyAlbums != null) {
            return mSpotifyAlbums.size();
        } else {
            return 0;
        }
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.album_list_item, parent, false);
        return new AlbumViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        holder.bind(mSpotifyAlbums.get(position));
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mAlbumArtIV;
        private TextView mAlbumTitleTV;
        private TextView mAlbumArtistTV;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            mAlbumArtIV = itemView.findViewById(R.id.iv_album_art);
            mAlbumTitleTV = itemView.findViewById(R.id.tv_album_title);
            mAlbumArtistTV = itemView.findViewById(R.id.tv_album_artist);
            itemView.setOnClickListener(this);
        }

        public void bind(SpotifyUtils.SpotifyAlbum album) {
            mAlbumTitleTV.setText(album.name);
            mAlbumArtistTV.setText(album.artists[0].name);
            Glide.with(mAlbumArtIV.getContext()).load(album.images[1].url).into(mAlbumArtIV);
        }

        @Override
        public void onClick(View v) {
            mOnAlbumClickListener.onAlbumClick(mSpotifyAlbums.get(getAdapterPosition()));
        }
    }

}
