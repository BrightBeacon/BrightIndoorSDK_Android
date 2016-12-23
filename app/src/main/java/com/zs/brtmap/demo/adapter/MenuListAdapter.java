package com.zs.brtmap.demo.adapter;

import java.util.List;

import com.ty.mapsdk.TYMapInfo;
import com.zs.brtmap.demo.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Administrator on 2016/8/25 0025.
 */
public class MenuListAdapter extends BaseAdapter {

	private List<TYMapInfo> allMapInfos;

	private TYMapInfo selected;

	public MenuListAdapter(List<TYMapInfo> allMapInfos) {
		this.allMapInfos = allMapInfos;
	}

	@Override
	public int getCount() {
		return allMapInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return allMapInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MenuListViewHodler holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_list_item, parent, false);
			holder = new MenuListViewHodler(convertView);

			convertView.setTag(holder);
		} else {
			holder = (MenuListViewHodler) convertView.getTag();
		}

		TYMapInfo entry = allMapInfos.get(position);
		holder.name.setText(entry.getFloorName());
		holder.selected.setVisibility(View.GONE);

		if (selected != null && selected.getFloorNumber() == entry.getFloorNumber()) {
			holder.selected.setVisibility(View.VISIBLE);
		}

		return convertView;
	}

	public static class MenuListViewHodler {

		private TextView name;
		private ImageView selected;

		public MenuListViewHodler(View itemView) {
			name = (TextView) itemView.findViewById(R.id.menu_list_item_title);
			selected = (ImageView) itemView.findViewById(R.id.menu_list_item_selected);
		}
	}

	public void setSelected(TYMapInfo selected) {
		this.selected = selected;
	}
}
