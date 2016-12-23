package com.zs.brtmap.demo;

import java.io.File;
import java.util.List;

import org.json.JSONObject;
import org.xutils.common.Callback.ProgressCallback;

import com.ty.locationengine.ble.TYBLEEnvironment;
import com.ty.locationengine.ble.TYBeacon;
import com.ty.locationengine.ble.TYLocationManager;
import com.ty.locationengine.ble.TYLocationManager.TYLocationManagerListener;
import com.ty.locationengine.ble.TYPublicBeacon;
import com.ty.locationengine.ibeacon.BeaconRegion;
import com.ty.mapdata.TYLocalPoint;
import com.ty.mapsdk.TYMapEnvironment;
import com.ty.mapsdk.TYPictureMarkerSymbol;
import com.zs.brtmap.demo.http.HttpHandler;
import com.zs.brtmap.demo.utils.Constants;
import com.zs.brtmap.demo.utils.FileHelper;
import com.zs.brtmap.demo.utils.Utils;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class LocationActivity extends BaseMapViewActivity implements TYLocationManagerListener {

	private TYLocationManager locationManager;
	private boolean isShowLocation;
	
	static {
		System.loadLibrary("TYMapSDK");
		System.loadLibrary("TYLocationEngine");
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		initView();
		setBLEEnvironment();
		updateBeacon();
	}
	
	@Override
	public void initContentViewID() {
		// TODO Auto-generated method stub
		contentViewID = R.layout.activity_location_view;
	}
	
	private void setBLEEnvironment() {
		//设置使用、下载定位数据通用目录
		String dir = TYMapEnvironment.getRootDirectoryForMapFiles();
		TYBLEEnvironment.setRootDirectoryForFiles(dir);
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
	public void mapViewDidLoad() {
		// TODO Auto-generated method stub
		super.mapViewDidLoad();        
		//mapView加载完毕
		TYPictureMarkerSymbol pic = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.l7));
        pic.setWidth(48);
        pic.setHeight(48);
		mapView.setLocationSymbol(pic);
		//currentBuilding被初始化
		initLocation();
	}
	
	private void initLocation() {
		try {
			if (currentBuilding != null&&locationManager == null) {
				locationManager = new TYLocationManager(this, currentBuilding);
				locationManager.setBeaconRegion(new BeaconRegion("BrightBeacon", Constants.UUID, Constants.MAJOR, null));
				locationManager.addLocationEngineListener(this);
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, e.toString());
		}
	}
	@Override
	public void didFailUpdateLocation(TYLocationManager arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void didRangedBeacons(TYLocationManager arg0, List<TYBeacon> arg1) {
		//  Beacon扫描结果事件回调，返回符合扫描参数的所有Beacon
		
	}

	@Override
	public void didRangedLocationBeacons(TYLocationManager arg0, List<TYPublicBeacon> arg1) {
		//  定位Beacon扫描结果事件回调，返回符合扫描参数的定位Beacon，定位Beacon包含坐标信息。此方法可用于辅助巡检，以及基于定位beacon的相关触发事件。
		
	}

	@Override
	public void didUpdateDeviceHeading(TYLocationManager arg0, double newHeading) {
		// 设备方向改变事件回调。结合地图MapMode可以处理地图自动旋转，或箭头方向功能。
		//mapView.setMapMode(TYMapViewMode.TYMapViewModeDefault);
		//mapView.setMapMode(TYMapViewMode.TYMapViewModeFollowing);
		Log.i(TAG,"地图初始北偏角："+currentBuilding.getInitAngle()+"；当前设备北偏角："+newHeading);
		 mapView.processDeviceRotation(newHeading);
	}

	@Override
	public void didUpdateImmediateLocation(TYLocationManager arg0, TYLocalPoint arg1) {
		//  *  位置更新事件回调，位置更新并返回新的位置结果。
		// 与[TYLocationManager:didUpdateLocatin:]方法相近，此方法回调结果未融合计步器信息，灵敏度较高，适合用于行车场景下
		
	}

	@Override
	public void didUpdateLocation(TYLocationManager arg0, TYLocalPoint newLocalPoint) {
		//  位置更新事件回调，位置更新并返回新的位置结果。
		//  与[TYLocationManager:didUpdateImmediationLocation:]方法相近，此方法回调结果融合计步器信息，稳定性较好，适合用于步行场景下。
		Log.i(TAG, newLocalPoint.getX()+" "+newLocalPoint.getY());
		mapView.showLocation(newLocalPoint);
	}
	
	
	private void updateBeacon() {
		HttpHandler.updateBeacon(new ProgressCallback<String>() {
			@Override
			public void onSuccess(String result) {
				// TODO Auto-generated method stub
				if (!TextUtils.isEmpty(result)) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        int code = obj.getInt("code");
                        if (code == 1) {
							String version = obj.getJSONObject("rlt").getString("version");
							String localVersion = Utils.getValue(LocationActivity.this, Constants.VER_LOCATION_DATA, "", String.class);
							if (!localVersion.equals(version)) {
								downloadBeaconData(version);
							}else {
								//Utils.showToast(BaseMapViewActivity.this,"无数据更新");
							}
						}else {
							//Utils.showToast(BaseMapViewActivity.this,"检查数据更新失败");
						}
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
			}
			
			@Override
			public void onFinished() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onError(Throwable arg0, boolean arg1) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onCancelled(CancelledException arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onWaiting() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStarted() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onLoading(long arg0, long arg1, boolean arg2) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	private void downloadBeaconData(final String version) {
		HttpHandler.downloadLocationData(new ProgressCallback<File>() {

			@Override
			public void onCancelled(CancelledException arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onFinished() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(File result) {
				// TODO Auto-generated method stub
				String filePath = Constants.CACHE_DIR + File.separator
						+ Constants.BUILDING_ID;
				if (!FileHelper.unZipFile(result.getPath(),filePath)) {
					Utils.showToast(LocationActivity.this, "文件解压失败.");
					Utils.closeProgressDialog();
				} else {
					Utils.closeProgressDialog();
					Utils.saveValue(LocationActivity.this, Constants.VER_LOCATION_DATA,version);
					initLocation();
				}
			}

			@Override
			public void onLoading(long arg0, long arg1, boolean arg2) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStarted() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onWaiting() {
				// TODO Auto-generated method stub
				
			}
		});
	}
}
