package com.example.sticky.Overview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sticky.R;
import bistu.share.Overview;

import java.util.List;

public class StickyOverviewAdapter extends RecyclerView.Adapter<StickyOverviewAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView content, modify;
        View wholeView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.wholeView = itemView;
            this.content = itemView.findViewById(R.id.textView_sticky_overview_content);
            this.modify = itemView.findViewById(R.id.textView_sticky_overview_modify);
        }
    }

    private List<Overview> overviewList;
    private OverviewActivity activity;

    StickyOverviewAdapter(OverviewActivity activity, List<Overview> overviewList) {
        this.activity = activity;
        this.overviewList = overviewList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sticky_overview, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Overview o = this.overviewList.get(position);
        holder.content.setText(o.getSummary());
        holder.modify.setText(o.getModifyTime().toString());

        holder.wholeView.setOnClickListener((View v) ->
            activity.viewDetail(this.overviewList.get(holder.getAdapterPosition()).getId())
        );
    }

    @Override
    public int getItemCount() {
        return this.overviewList.size();
    }
}
