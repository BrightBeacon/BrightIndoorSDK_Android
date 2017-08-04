package com.zs.brtmap.demo.mark;

import java.util.List;

import com.esri.android.map.Callout;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.ty.mapsdk.TYMapEnvironment;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYPoi;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.R;
import com.zs.brtmap.demo.utils.Utils;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class CalloutActivity extends BaseMapViewActivity {
	static final String TAG = CalloutActivity.class.getSimpleName();
	static {
		System.loadLibrary("TYMapSDK");
		// System.loadLibrary("TYLocationEngine");
	}
	protected Callout mapCallout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 获取地图Callout组件
		mapCallout = mapView.getCallout();
	}

	@Override
	public void initContentViewID() {
		contentViewID = R.layout.activity_callout_view;
	}

	// 点击地图回调方法
	@Override
	public void onClickAtPoint(TYMapView mapView, Point mappoint) {
		Log.i(TAG, "onClickAtPoint: " + mappoint);
		TYPoi poi = mapView.extractRoomPoiOnCurrentFloor(mappoint.getX(), mappoint.getY());
		if (poi != null) {
			Log.i(TAG, "点击到poi: " + poi);
		}

		mapCallout.hide();
	}

	// 加载自定义弹出框内容
	private View loadCalloutView(final String title, String detail) {
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.layout_callout, null);
		TextView titleView = (TextView) view.findViewById(R.id.callout_title);
		titleView.setText(title);
		TextView detailView = (TextView) view.findViewById(R.id.callout_detail);
		detailView.setText(detail);
		TextView cancelBtn = (TextView) view.findViewById(R.id.callout_cancel);
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mapCallout.hide();
			}
		});
		TextView doneBtn = (TextView) view.findViewById(R.id.callout_done);
		doneBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.showToast(CalloutActivity.this, title);
				mapCallout.hide();
			}
		});
		return view;
	}

	// 点击选中POI回调方法
	@Override
	public void onPoiSelected(TYMapView mapView, List<TYPoi> poiList) {
		Log.i(TAG, "onPoiSelected: " + poiList.size());
		Log.i(TAG, "" + poiList);

		if (mapCallout != null && mapCallout.isShowing()) {
			mapCallout.hide();
		}

		if (poiList != null && poiList.size() > 0) {
			TYPoi poi = poiList.get(0);

			Point location;
			if (poi.getGeometry().getClass() == Polygon.class) {
				location = GeometryEngine.getLabelPointForPolygon((Polygon) poi.getGeometry(),
						TYMapEnvironment.defaultSpatialReference());
			} else {
				location = (Point) poi.getGeometry();
			}

			Log.i(TAG, location.toString());

			String title = poi.getName();
			String detail = poi.getPoiID();

			mapCallout.setStyle(R.xml.callout_style);
			mapCallout.setMaxWidthDp(300);
			mapCallout.setMaxHeightDp(300);
			mapCallout.setContent(loadCalloutView(title, detail));
			mapCallout.show(location);
		}
	}

	@Override
	public void mapViewDidZoomed(TYMapView mapView) {
		Log.i(TAG, "mapViewDidZoomed");
	}
}
