package com.zs.brtmap.demo.map;

import android.os.Bundle;
import android.util.Log;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYPictureMarkerSymbol;
import com.ty.mapsdk.TYPoi;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.R;

public class MapActivity extends BaseMapViewActivity {
	static {
		System.loadLibrary("TYMapSDK");
		//System.loadLibrary("TYLocationEngine");
	}
	GraphicsLayer graphicsLayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void initContentViewID() {
		contentViewID = R.layout.activity_map_view;
	}

	@Override
	public void onClickAtPoint(TYMapView mapView, Point mappoint) {
		Log.i(TAG, "Clicked Point: " + mappoint.getX() + ", " +  mappoint.getY());

		//根据x,y获取本层房间POI
		TYPoi poi = mapView.extractRoomPoiOnCurrentFloor(mappoint.getX(),
				mappoint.getY());

		if (poi != null) {
			mapView.highlightPoi(poi);
		}
		TYPictureMarkerSymbol pms = new TYPictureMarkerSymbol(getResources()
				.getDrawable(R.drawable.location));
		pms.setWidth(20.0f);
		pms.setHeight(20.0f);

		if (graphicsLayer == null) {
			graphicsLayer = new GraphicsLayer();
			mapView.addLayer(graphicsLayer);
		}
		graphicsLayer.removeAll();
		graphicsLayer.addGraphic(new Graphic(new Point(mappoint.getX(),
				mappoint.getY()), pms));

	}
}
