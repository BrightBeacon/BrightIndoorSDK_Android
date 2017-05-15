package com.zs.brtmap.demo;

import java.util.Arrays;
import java.util.List;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.TextSymbol;
import com.ty.locationengine.ble.TYBLEEnvironment;
import com.ty.locationengine.ble.TYBeacon;
import com.ty.locationengine.ble.TYLocationManager;
import com.ty.locationengine.ble.TYLocationManager.TYLocationManagerListener;
import com.ty.locationengine.ble.TYPublicBeacon;
import com.ty.locationengine.ibeacon.BeaconRegion;
import com.ty.mapdata.TYLocalPoint;
import com.ty.mapsdk.TYMapEnvironment;
import com.ty.mapsdk.TYMapInfo;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYPictureMarkerSymbol;
import com.zs.brtmap.demo.utils.Constants;
import com.zs.brtmap.demo.utils.Utils;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class LocationActivity extends BaseMapViewActivity implements TYLocationManagerListener {

	private TYLocationManager locationManager;
	private boolean isShowLocation;

	GraphicsLayer hintLayer;

	static {
		System.loadLibrary("TYMapSDK");
		System.loadLibrary("TYLocationEngine");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		initView();
		Log.i(TAG,TYMapEnvironment.getSDKVersion()+TYBLEEnvironment.getSDKVersion());
	}

	@Override
	public void initContentViewID() {
		// TODO Auto-generated method stub
		contentViewID = R.layout.activity_location_view;
	}
	private void initView() {
		TextView btnLocation = (TextView) findViewById(R.id.show_location);
		btnLocation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (isShowLocation == true) {
					locationManager.stopUpdateLocation();
					isShowLocation = false;
					Utils.showToast(LocationActivity.this, "停止定位");
				}else {
					if (locationManager != null) {
						locationManager.startUpdateLocation();
						isShowLocation = true;
						Utils.showToast(LocationActivity.this, "开始定位");
					}else{
						Utils.showToast(LocationActivity.this, "定位初始化失败");
					}
				}
			}
		});
	}

	@Override
	public void mapViewDidLoad(TYMapView mapView,Error error) {
		super.mapViewDidLoad(mapView,error);
		if (error != null){
			return;
		}
		//mapView加载完毕,设置定位图片
		TYPictureMarkerSymbol pic = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.location_arrow));
		pic.setWidth(48);
		pic.setHeight(48);
		mapView.setLocationSymbol(pic);
	//设置地图跟随旋转
//		mapView.setMapMode(TYMapView.TYMapViewMode.TYMapViewModeFollowing);
		//currentBuilding被初始化
		initLocation();
	}

	private void initLocation() {
		try {
			locationManager = new TYLocationManager(this, Constants.BUILDING_ID, Constants.APP_KEY);
			locationManager.addLocationEngineListener(this);
//			BeaconRegion region = new BeaconRegion("demo",Constants.UUID,Constants.MAJOR,null);
//			locationManager.setBeaconRegion(Arrays.asList(new BeaconRegion[]{region}));
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, e.toString());
		}
	}

	@Override
	public void didRangedBeacons(TYLocationManager arg0, List<TYBeacon> arg1) {
		//  Beacon扫描结果事件回调，返回符合扫描参数的所有Beacon

	}

	@Override
	public void didRangedLocationBeacons(TYLocationManager arg0, List<TYPublicBeacon> arg1) {
		//  定位Beacon扫描结果事件回调，返回符合扫描参数的定位Beacon，定位Beacon包含坐标信息。此方法可用于辅助巡检，以及基于定位beacon的相关触发事件。
		if (hintLayer == null) {
			hintLayer = new GraphicsLayer();
			mapView.addLayer(hintLayer);
		}
		hintLayer.removeAll();
		Graphic[] graphics = new  Graphic[arg1.size()];
		int i = 0;
		for (TYPublicBeacon pb : arg1) {
			Graphic g = new Graphic(new Point(pb.getLocation().getX(),pb.getLocation().getY()),new TextSymbol(13,pb.getRssi()+"", Color.RED));
			graphics[i] = g;
			i++;
		}
		hintLayer.addGraphics(graphics);
	}

	@Override
	public void didUpdateDeviceHeading(TYLocationManager arg0, double newHeading) {
		//如方位有误，尝试打开手机校准指南针。
		// 设备方向改变事件回调。结合地图MapMode可以处理地图自动旋转，或箭头方向功能。
		//mapView.setMapMode(TYMapViewMode.TYMapViewModeDefault);
		//mapView.setMapMode(TYMapViewMode.TYMapViewModeFollowing);
		Log.i(TAG,"地图初始北偏角："+mapView.building.getInitAngle()+"；当前设备北偏角："+newHeading);
		mapView.processDeviceRotation(newHeading);
	}

	@Override
	public void didUpdateImmediateLocation(TYLocationManager arg0, TYLocalPoint  newLocalPoint) {
		//  *  位置更新事件回调，位置更新并返回新的位置结果。
		// 与[TYLocationManager:didUpdateLocatin:]方法相近，此方法回调结果未融合计步器信息，灵敏度较高，适合用于行车场景下
		mapView.showLocation(newLocalPoint);

	}

	@Override
	public void didFailUpdateLocation(TYLocationManager tyLocationManager, Error error) {
		if (error != null) Log.e(TAG,error.toString());
	}

	@Override
	public void didUpdateLocation(TYLocationManager arg0, TYLocalPoint newLocalPoint) {
		//  位置更新事件回调，位置更新并返回新的位置结果。
		//  与[TYLocationManager:didUpdateImmediationLocation:]方法相近，此方法回调结果融合计步器信息，稳定性较好，适合用于步行场景下
	}
}
