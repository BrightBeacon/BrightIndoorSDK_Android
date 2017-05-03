package com.zs.brtmap.demo;
import java.util.List;

import com.esri.android.map.Layer;
import com.esri.core.geometry.Point;
import com.ty.mapsdk.TYMapEnvironment;
import com.zs.brtmap.demo.adapter.MenuListAdapter;
import com.ty.mapsdk.TYMapInfo;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYMapView.TYMapViewListenser;
import com.ty.mapsdk.TYPoi;
import com.zs.brtmap.demo.utils.Constants;
import com.zs.brtmap.demo.utils.Utils;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public abstract class BaseMapViewActivity extends Activity
		implements TYMapViewListenser {
	public String TAG = this.getClass().getSimpleName();
	public TYMapView mapView;

	//楼层控件
	private PopupWindow pw;
	private MenuListAdapter menuListAdapter;
	private int offset;

	public int contentViewID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initContentViewID();
		setContentView(contentViewID);

		TYMapEnvironment.initMapEnvironment();
		mapView = (TYMapView) findViewById(R.id.map);
		//隐藏地图，楼层加载完成前黑屏
		//mapView.setVisibility(View.INVISIBLE);

		mapView.addMapListener(this);
		mapView.init(Constants.BUILDING_ID,Constants.APP_KEY);
		mapView.setHighlightPoiOnSelection(false);
		mapView.setAllowRotationByPinch(true);

		//mapView.setMapBackground(网格颜色, 线条颜色, 网格宽度, 线条宽度);
		//mapView.setMapBackground(Color.BLACK, Color.BLACK, 20, 10);
	}
	// 用于子类设置界面元素初始化
	public abstract void initContentViewID();


	@Override
	public void  mapViewDidLoad(TYMapView mapView,Error error) {
		if(error == null){
			showMapControl();
			mapView.setFloor(mapView.allMapInfo().get(0));
		}else {
			Utils.showToast(this, error.toString());
		}
	}
	public void showMapControl() {
		showFloorControl();
		showZoomControl();
		setMinMaxScale(1, 1000);
	}
	public void showFloorControl() {
		TextView btnFloor = (TextView) findViewById(R.id.btn_floor);
		ImageView btnFloorArrow = (ImageView) findViewById(R.id.btn_floor_arrow);
		if (mapView.allMapInfo().isEmpty()) {
			btnFloor.setVisibility(View.GONE);
			btnFloorArrow.setVisibility(View.VISIBLE);
			return;
		}
		btnFloor.setVisibility(View.VISIBLE);
		btnFloorArrow.setVisibility(View.VISIBLE);
		btnFloor.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				createPopwMenu(v);
			}
		});
	}

	private void createPopwMenu(View v) {
		final TextView btnFloor = (TextView) findViewById(R.id.btn_floor);
		final ImageView btnFloorArrow = (ImageView) findViewById(R.id.btn_floor_arrow);
		if (pw == null) {
			View view = getLayoutInflater().inflate(R.layout.popwindow_menu_layout, null);
			view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			int height = view.getMeasuredHeight();
			int offset1 = Utils.dip2px(this, 10);
			offset = height - offset1;
			ListView lv = (ListView) view.findViewById(R.id.menu_list);

			menuListAdapter = new MenuListAdapter(mapView.allMapInfo());
			lv.setAdapter(menuListAdapter);
			lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					TYMapInfo currentMapInfo = (TYMapInfo) parent.getItemAtPosition(position);
					mapView.setFloor(currentMapInfo);
					pw.dismiss();
					menuListAdapter.setSelected(currentMapInfo);
					btnFloor.setText(currentMapInfo.getFloorName());
				}
			});

			pw = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
			pw.setOutsideTouchable(true);
			pw.setBackgroundDrawable(new ColorDrawable(0));
			pw.setOnDismissListener(new PopupWindow.OnDismissListener() {
				@Override
				public void onDismiss() {
					Utils.rotationArrow(btnFloorArrow, 0, 180);
				}
			});
		}

		if (menuListAdapter != null) {
			menuListAdapter.setSelected(mapView.getCurrentMapInfo());
			menuListAdapter.notifyDataSetChanged();
		}

		if (!pw.isShowing()) {
			Utils.rotationArrow(btnFloorArrow, 180, 0);
			pw.showAtLocation(v, Gravity.BOTTOM, 0, offset);
		} else {
			pw.dismiss();
		}
	}

	public void showZoomControl() {
		TextView btnZoomIn = (TextView) findViewById(R.id.btn_zoomin);
		TextView btnZoomOut = (TextView) findViewById(R.id.btn_zoomout);
		btnZoomIn.setVisibility(View.VISIBLE);
		btnZoomOut.setVisibility(View.VISIBLE);
		btnZoomIn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mapView.zoomin();
			}
		});
		btnZoomOut.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mapView.zoomout();
			}
		});
	}
	//设置当前屏幕宽能显示的最小、最大实际距离
	public void setMinMaxScale(double min,double max) {
		//scale 实际距离（米）/屏幕距离（米）
		//屏幕总距离(米) = (屏幕像素个数/dpi)*0.0254(米/inch)
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		double deviceDistance = metrics.widthPixels/metrics.xdpi*0.0254;
		Log.i(TAG, deviceDistance+"/"+metrics.widthPixels+"/"+metrics.xdpi+"/"+mapView.getScale());
		mapView.setMaxScale(min/deviceDistance);//比例尺：1米/屏幕总宽
		mapView.setMinScale(max/deviceDistance);//比例尺：100米/屏幕总宽
	}
	@Override
	public void onFinishLoadingFloor(final TYMapView mapView, TYMapInfo mapInfo) {
		//显示地图
//		runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				mapView.setVisibility(View.VISIBLE);
//			}
//		});
		//地图楼层切换
		//设置比例尺让：地图宽==屏幕宽
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		Log.i(TAG,metrics.widthPixels+"/"+metrics.heightPixels+"/"+metrics.xdpi+"/"+metrics.ydpi+"/"+metrics.density);
		double deviceDistance = mapView.getMeasuredWidth()/metrics.xdpi*0.0254;
		double mapDistance = mapInfo.getMapExtent().getXmax() - mapInfo.getMapExtent().getXmin();
		mapView.setScale(mapDistance/deviceDistance);

		//移动到地图中心点
		double centerX = mapInfo.getMapExtent().getXmax()+mapInfo.getMapExtent().getXmin();
		double centerY = mapInfo.getMapExtent().getYmax()+mapInfo.getMapExtent().getYmin();
		mapView.centerAt(new Point(centerX/2,centerY/2),false);
	}


	@Override
	public void onClickAtPoint(TYMapView mapView, Point mappoint) {
		//地图点击
	}

	@Override
	public void onPoiSelected(TYMapView mapView, List<TYPoi> poiList) {
		//poi选中
	}
	@Override
	public void mapViewDidZoomed(TYMapView mapView) {
		//地图缩放
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.destroyDrawingCache();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapView.unpause();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapView.pause();
	}
}
