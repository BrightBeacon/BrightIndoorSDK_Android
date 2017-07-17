package com.zs.brtmap.demo.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ty.mapsdk.TYMapInfo;
import com.zs.brtmap.demo.R;

import java.util.List;

/**
 * Created by thomasho on 2017/7/4.
 */

public class FloorListAdapter extends ArrayAdapter {

    private List<TYMapInfo> mapInfos;
    private TYMapInfo selected;

    public FloorListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List objects) {
        super(context, resource, objects);
        mapInfos = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        FloorListViewHodler holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.pop_list_item, parent, false);
            holder = new FloorListViewHodler(convertView);

            convertView.setTag(holder);
        } else {
            holder = (FloorListViewHodler) convertView.getTag();
        }

        TYMapInfo entry = mapInfos.get(position);
        holder.name.setText(entry.getFloorName());
        holder.selected.setVisibility(View.GONE);

        if (selected != null && selected.getFloorNumber() == entry.getFloorNumber()) {
            holder.selected.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    public static class FloorListViewHodler {

        private TextView name;
        private ImageView selected;

        public FloorListViewHodler(View itemView) {
            name = (TextView) itemView.findViewById(R.id.pop_list_item_title);
            selected = (ImageView) itemView.findViewById(R.id.pop_list_item_selected);
        }
    }

    public void setSelected(TYMapInfo info) {
        this.selected = info;
    }
}
