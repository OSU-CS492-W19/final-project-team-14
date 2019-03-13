package com.example.spotifyremote;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.spotifyremote.utils.SpotifyUtils;

import androidx.recyclerview.widget.RecyclerView;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchResultViewHolder> {
    private SpotifyUtils.SpotifyAlbumsItem[] mItems
    OnSearchItemClickListener mSeachItemClickListener;

    public interface OnSearchItemClickListener {
        void onSearchItemClick();
    }

    SearchAdapter(OnSearchItemClickListener searchItemClickListener) {
        mSeachItemClickListener = searchItemClickListener;
    }

    public void updateSearchResults(SpotifyUtils.SpotifyAlbumsItem[] items) {
        mItems = items;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mItems != null) {
            return mItems.length;
        } else {
            return 0;
        }
    }

    @Override
    public SearchResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.search_result_item, parent, false);
        return new SearchResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SearchResultViewHolder holder, int position) {
        holder.bind(mItems[position]);
    }

    class SearchResultViewHolder extends RecyclerView.ViewHolder {
        private TextView mSearchResultTV;

        public SearchResultViewHolder(View itemView) {
            super(itemView);
            mSearchResultTV = itemView.findViewById(R.id.tv_search_result);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SpotifyUtils.SpotifyAlbumsItem searchResult = mItems[getAdapterPosition()];
                    mSeachItemClickListener.onSearchItemClick(searchResult);
                }
            });
        }

        public void bind() {
            mSearchResultTV.setText(item.items[0].name);
        }
    }
}
