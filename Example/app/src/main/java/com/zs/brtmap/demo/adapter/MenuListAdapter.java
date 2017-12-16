package com.zs.brtmap.demo.adapter;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

/**
 * Created by Administrator on 2016/8/25 0025.
 */
public class MenuListAdapter extends BaseExpandableListAdapter {

	private Context mContext = null;

	private String[] groups = { "显示地图","地图事件", "地图控件", "标注弹窗","POI搜索","路径规划","导航" };
	private String[] basemap = { "· 基础地图", "· 地图信息", "· 地图操作","· 图层控制","· 坐标转换","· 地图本地化","· 瓦片地图" };
	private String[] mapoper = { "· 拾取POI", "· 手势控制" };
	private String[] mapctl = { "· 指北针"};
	private String[] mapmark = { "· 图文点标注", "· 线标注", "· 形状标注","· 展示弹窗","· 围栏示例"};
	private String[] mapsearch = { "· 名称搜索", "· 设施搜索", "· 距离搜索" };
	private String[] maproute = { "· 路径规划", "· 距离计算", "· 路径提示" ,"· 设施禁行" , "· 仅路径" };
	private String[] mapnav = { "· 开始定位", "· 定位吸附", "· 导航示例" };

	private List<String> groupList = null;
	private List<List<String>> itemList = null;

	private int selectedGroup = -1;
	private int selectedChild = -1;

	public MenuListAdapter(Context context) {

		this.mContext = context;
		groupList = new ArrayList<String>();
		itemList = new ArrayList<List<String>>();
		initData();
	}
	private void initData() {
		for (int i = 0; i < groups.length; i++) {
			groupList.add(groups[i]);
			String[][] item = new String[][]{basemap,mapoper,mapctl,mapmark,mapsearch,maproute,mapnav};
			List<String> list = new ArrayList<String>();
			for (String it:item[i]) {
				list.add(it);
			}
			itemList.add(list);
		}
	}

	public boolean areAllItemsEnabled() {
		return false;
	}
	@Override
	public int getGroupCount() {
		return groupList.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return itemList.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groupList.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return itemList.get(groupPosition).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
							 View convertView, ViewGroup parent) {
		TextView text = null;
		if (convertView == null) {
			text = new TextView(mContext);
		} else {
			text = (TextView) convertView;
		}
		String name = (String) groupList.get(groupPosition);
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,60,this.mContext.getResources().getDisplayMetrics()));
		text.setLayoutParams(lp);
		text.setTextSize(20);
		text.setGravity(Gravity.CENTER);
		text.setPadding(0,20,0,20);
		text.setText(name);
		return text;
	}
	@Override
	public View getChildView(int groupPosition, int childPosition,
							 boolean isLastChild, View convertView, ViewGroup parent) {
		TextView text = null;
		if (convertView == null) {
			text = new TextView(mContext);
		} else {
			text = (TextView) convertView;
		}
		// 获取子节点要显示的名称
		String name = (String) itemList.get(groupPosition).get(childPosition);
		// 设置文本视图的相关属性
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,40,this.mContext.getResources().getDisplayMetrics()));
		text.setLayoutParams(lp);
		text.setTextSize(16);
		text.setTextColor(Color.DKGRAY);
		text.setGravity(Gravity.CENTER);
		text.setPadding(0,8,0,8);
		text.setText(name);
		if (groupPosition == this.selectedGroup&&childPosition == this.selectedChild) {
			text.setBackgroundColor(Color.LTGRAY);
		}else {
			text.setBackgroundColor(Color.WHITE);
		}
		return text;
	}

	@Override
	public boolean isChildSelectable(int i, int i1) {
		return true;
	}
	public boolean isEmpty() {
		return false;
	}
	public void setSelected(int groupPosition, int childPosition) {
		this.selectedGroup = groupPosition;
		this.selectedChild = childPosition;
	}
}
