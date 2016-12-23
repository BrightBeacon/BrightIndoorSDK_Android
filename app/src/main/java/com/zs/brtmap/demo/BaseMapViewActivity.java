package com.zs.brtmap.demo;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import org.json.JSONObject;
import org.xutils.common.Callback;

import com.esri.core.geometry.Point;
import com.ty.mapdata.TYBuilding;
import com.zs.brtmap.demo.adapter.MenuListAdapter;
import com.ty.mapsdk.TYBuildingManager;
import com.ty.mapsdk.TYMapEnvironment;
import com.ty.mapsdk.TYMapInfo;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYMapView.TYMapViewListenser;
import com.ty.mapsdk.TYOfflineRouteManager;
import com.ty.mapsdk.TYOfflineRouteManager.TYOfflineRouteManagerListener;
import com.ty.mapsdk.TYPoi;
import com.ty.mapsdk.TYRouteResult;
import com.zs.brtmap.demo.utils.Constants;
import com.zs.brtmap.demo.utils.Utils;
import com.zs.brtmap.demo.http.HttpHandler;
import com.zs.brtmap.demo.utils.FileHelper;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
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
		implements TYMapViewListenser, TYOfflineRouteManagerListener {
	public String TAG = this.getClass().getSimpleName();
	public TYMapView mapView;
	public List<TYMapInfo> allMapInfo;
	public TYOfflineRouteManager routeManager;

	public TYBuilding currentBuilding;

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

		mapView = (TYMapView) findViewById(R.id.map);
		mapView.setEsriLogoVisible(false);
		//mapView.setVisibility(View.INVISIBLE);
		
		//设置地图加载目录、地图下载目录
		setMapEnvironment(Constants.BUILDING_ID);
		initMap();
		//检查更新
		updateMapData();

	}
	// 用于子类设置界面元素初始化
	public abstract void initContentViewID();

	// 处理地图License
	private String trimLicense(String license) {
		license = license.replaceAll("brtd_", "#");
		license = license.replaceAll("brtx_", ":");
		return license;
	}
	//设置地图加载目录、下载地图文件目录
	private void setMapEnvironment(String buildingid) {
		String dir = Constants.CACHE_DIR  + File.separator + buildingid;
		TYMapEnvironment.initMapEnvironment();
		TYMapEnvironment.setRootDirectoryForMapFiles(dir);
		if (!FileHelper.fileExists(dir)) {
			if(FileHelper.makeDir(dir)){
				copyFileIfNeeded(buildingid);
			}
		}
	}
	//拷贝默认地图
	void copyFileIfNeeded(String buildingid) {
		String sourcePath = buildingid;
		String targetPath = TYMapEnvironment.getRootDirectoryForMapFiles();

		Log.i(TAG, "source path: " + sourcePath);
		Log.i(TAG, "target path: " + targetPath);

		//FileHelper.deleteFile(new File(targetPath));
		FileHelper.copyFolderFromAsset(this, sourcePath, targetPath);
	}
	
	public void mapViewDidLoad() {
		showFloorControl();
		showZoomControl();
		setMinMaxScale(1, 1000);
	}

	private void initMap() {
		String filePath = Constants.CACHE_DIR + File.separator
				+ Constants.BUILDING_ID;
		if(FileHelper.fileExists(filePath)){
			try {
				currentBuilding = TYBuildingManager.parseBuildingFromFilesById(this, Constants.CITY_ID, Constants.BUILDING_ID);
				allMapInfo = TYMapInfo.parseAllMapInfo(currentBuilding);
				if (allMapInfo == null || allMapInfo.isEmpty()) {
					Utils.showToast(this, "解析楼层数据失败");
					return;
				}
				if (!mapView.isLoaded()) {
					mapView.init(currentBuilding, Constants.APP_KEY, trimLicense(Constants.LICENSE));
				}else {
					//更新数据等，重新加载地图
					mapView.switchBuilding(currentBuilding, Constants.APP_KEY, Constants.LICENSE);
				}
				//mapView.setMapBackground(网格颜色, 线条颜色, 网格宽度, 线条宽度);
				//mapView.setMapBackground(Color.BLACK, Color.BLACK, 20, 10);
				mapView.setFloor(allMapInfo.get(0));
				mapView.addMapListener(this);
				mapView.setHighlightPoiOnSelection(false);
				mapView.setAllowRotationByPinch(true);
				mapView.postDelayed(new Runnable() {
					@Override
					public void run() {
						mapViewDidLoad();
					}
				}, 100);
			} catch (Exception e) {
				// TODO: handle exception
				Log.e(TAG, e.toString());
			}
		}else {
			Utils.showToast(BaseMapViewActivity.this, "未找到本地地图文件");
		}
	}

	public void showFloorControl() {
		TextView btnFloor = (TextView) findViewById(R.id.btn_floor);
		ImageView btnFloorArrow = (ImageView) findViewById(R.id.btn_floor_arrow);
		if (allMapInfo == null || allMapInfo.isEmpty()) {
			btnFloor.setVisibility(View.GONE);
			btnFloorArrow.setVisibility(View.VISIBLE);
			return;
		}
		btnFloor.setVisibility(View.VISIBLE);
		btnFloorArrow.setVisibility(View.VISIBLE);
		btnFloor.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
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

			menuListAdapter = new MenuListAdapter(allMapInfo);
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
				// TODO Auto-generated method stub
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
	public void onClickAtPoint(TYMapView mapView, Point mappoint) {
		//地图点击
	}

	@Override
	public void onPoiSelected(TYMapView mapView, List<TYPoi> poiList) {
		//poi选中
	}

	@Override
	public void onFinishLoadingFloor(TYMapView mapView, TYMapInfo mapInfo) {
		//地图楼层切换
	}

	@Override
	public void mapViewDidZoomed(TYMapView mapView) {
		//地图缩放
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
	//路径规划回调
	@Override
	public void didFailSolveRouteWithError(TYOfflineRouteManager arg0, Exception arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void didSolveRouteWithResult(TYOfflineRouteManager arg0, TYRouteResult tyRouteResult) {
		// TODO Auto-generated method stub
	}
	
	//检查地图更新操作
	private void updateMapData() {
		HttpHandler.updateMap(new Callback.ProgressCallback<String>() {

			@Override
			public void onCancelled(CancelledException arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
				// TODO Auto-generated method stub
				//Utils.showToast(BaseMapViewActivity.this, "更新数据失败");
			}

			@Override
			public void onFinished() {
				// TODO Auto-generated method stub
			}

			@Override
			public void onSuccess(String result) {
				// TODO Auto-generated method stub
				if (!TextUtils.isEmpty(result)) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        int code = obj.getInt("code");
                        if (code == 1) {
							String version = obj.getJSONObject("rlt").getString("version");
							String localVersion = Utils.getValue(BaseMapViewActivity.this, Constants.VER_MAP_DATA, "", String.class);
							if (!localVersion.equals(version)) {
								downloadMapData(version);
							}else {
								Utils.showToast(BaseMapViewActivity.this,"无数据更新");
							}
						}else {
							Utils.showToast(BaseMapViewActivity.this,"检查数据更新失败");
						}
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
	private void downloadMapData(final String version) {
		Utils.showProgressDialog(this, "地图下载中...", true);
		HttpHandler.downloadMapData(new Callback.ProgressCallback<File>() {
			@Override
			public void onSuccess(File result) {
				String filePath = Constants.CACHE_DIR + File.separator
						+ Constants.BUILDING_ID;
				if (!FileHelper.unZipFile(result.getPath(),filePath)) {
					Utils.showToast(BaseMapViewActivity.this, "地图文件解压失败.");
					Utils.closeProgressDialog();
				} else {
					Utils.closeProgressDialog();
					Utils.saveValue(BaseMapViewActivity.this, Constants.VER_MAP_DATA,version);
					initMap();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				Utils.showToast(BaseMapViewActivity.this, "地图文件下载失败.");
				Utils.closeProgressDialog();
			}

			@Override
			public void onCancelled(CancelledException cex) {
				Utils.closeProgressDialog();
			}

			@Override
			public void onFinished() {
			}

			@Override
			public void onWaiting() {

			}

			@Override
			public void onStarted() {

			}

			@Override
			public void onLoading(long total, long current, boolean isDownloading) {
				BigDecimal totalNum = new BigDecimal(total);
				BigDecimal currentNum = new BigDecimal(current);
				BigDecimal result = currentNum.divide(totalNum, 2, BigDecimal.ROUND_HALF_EVEN);
				int rogress = (int) (result.doubleValue() * 100);
				System.out.println("progress:" + rogress);
			}
		});
	}
}
