package com.zs.brtmap.demo.location;

import java.util.List;

import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.TextSymbol;
import com.ty.locationengine.ble.TYBLEEnvironment;
import com.ty.locationengine.ble.TYBeacon;
import com.ty.locationengine.ble.TYLocationManager;
import com.ty.locationengine.ble.TYLocationManager.TYLocationManagerListener;
import com.ty.locationengine.ble.TYPublicBeacon;
import com.ty.mapdata.TYLocalPoint;
import com.ty.mapsdk.TYMapEnvironment;
import com.ty.mapsdk.TYMapInfo;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYPictureMarkerSymbol;
import com.ty.mapsdk.TYPoi;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.R;
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
    TYPictureMarkerSymbol[] pics;
    int picIndex;

    GraphicsLayer hintLayer;
    GraphicsLayer locLayer;

    static {
        System.loadLibrary("TYMapSDK");
        System.loadLibrary("TYLocationEngine");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, TYMapEnvironment.getSDKVersion() + TYBLEEnvironment.getSDKVersion());

        locationManager = new TYLocationManager(this, Constants.BUILDING_ID, Constants.APP_KEY);
        locationManager.addLocationEngineListener(this);
        //是否启用热力数据
        locationManager.enableHeatData(true);

        TextView btnLocation = (TextView) findViewById(R.id.show_location);
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowLocation == true) {
                    locationManager.stopUpdateLocation();
                    isShowLocation = false;
                    Utils.showToast(LocationActivity.this, "停止定位");
                    locLayer.removeAll();
                    mapView.setBackground(getResources().getDrawable(R.drawable.btn_locate_gray));
                } else {
                    locationManager.startUpdateLocation();
                    isShowLocation = true;
                    Utils.showToast(LocationActivity.this, "开始定位，需配置定位设备。查看README.md。");
                    v.setBackground(getResources().getDrawable(R.drawable.btn_locate));
                }
            }
        });

        //定位动画图片组
        Integer[] picids = new Integer[]{
                R.drawable.l0,
                R.drawable.l1,
                R.drawable.l2,
                R.drawable.l3,
                R.drawable.l4,
                R.drawable.l5,
                R.drawable.l6,
                R.drawable.l7};
        pics = new TYPictureMarkerSymbol[8];
        for (int i = 0; i < 8; i++) {
            TYPictureMarkerSymbol pms = new TYPictureMarkerSymbol(getResources().getDrawable(picids[i]));
            pms.setHeight(44);
            pms.setWidth(44);
            pics[i] = pms;
        }
    }

    @Override
    public void initContentViewID() {
        contentViewID = R.layout.activity_location_view;
    }

    @Override
    public void mapViewDidLoad(TYMapView mapView, Error error) {
        super.mapViewDidLoad(mapView, error);
        if (error != null) {
            return;
        }

        if (locLayer == null) {
            locLayer = new GraphicsLayer();
            mapView.addLayer(locLayer);
        }
    }

    @Override
    public void onClickAtPoint(TYMapView mapView, Point mappoint) {
        super.onClickAtPoint(mapView, mappoint);

        String title = "未知位置";
        TYPoi poi = mapView.extractRoomPoiOnCurrentFloor(mappoint.getX(),mappoint.getY());
        if (poi!=null&&poi.getName()!=null) {
            title = poi.getName();
        }
        TextView textView =new TextView(this);
        textView.setText(title);
        Callout callout = mapView.getCallout();
        callout.setStyle(R.xml.callout_style);
        callout.setMaxWidthDp(300);
        callout.setMaxHeightDp(300);
        callout.setContent(textView);
        callout.show(mappoint);
    }

    private void animateLocationSymbol() {
        mapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mapView.setLocationSymbol(pics[picIndex]);
                picIndex++;
                if (picIndex < 7) animateLocationSymbol();
                else picIndex = 0;
            }
        }, 100);
    }

    @Override
    public void didRangedBeacons(TYLocationManager arg0, List<TYBeacon> arg1) {
        //  Beacon扫描结果事件回调，返回符合扫描参数的所有Beacon

    }

    @Override
    public void didRangedLocationBeacons(TYLocationManager arg0, List<TYPublicBeacon> beacons) {
        //  定位Beacon扫描结果事件回调，返回符合扫描参数的定位Beacon，定位Beacon包含坐标信息。此方法可用于辅助巡检，以及基于定位beacon的相关触发事件。

        if (hintLayer == null) {
            hintLayer = new GraphicsLayer();
            mapView.addLayer(hintLayer);
        } else {
            hintLayer.removeAll();
        }
        Graphic[] graphics = new Graphic[beacons.size()];
        int i = 0;
        for (TYPublicBeacon pb : beacons) {
            if (pb.getLocation().getFloor() == mapView.currentMapInfo.getFloorNumber()) {
                Graphic g = new Graphic(new Point(pb.getLocation().getX(), pb.getLocation().getY()), new TextSymbol(13, pb.getMinor() + "," + pb.getRssi() + "", Color.RED));
                graphics[i] = g;
            }
            i++;
        }
        hintLayer.addGraphics(graphics);
    }

    @Override
    public void didUpdateDeviceHeading(TYLocationManager arg0, double newHeading) {
        //如方位有误，尝试打开手机校准指南针或提醒用户直接转8字。
        // 设备方向改变事件回调。结合地图MapMode可以处理地图自动旋转，或箭头方向功能。
        //mapView.setMapMode(TYMapViewMode.TYMapViewModeDefault);
//		mapView.setMapMode(TYMapView.TYMapViewMode.TYMapViewModeFollowing);
        Log.i(TAG, "地图初始北偏角：" + mapView.building.getInitAngle() + "；当前设备北偏角：" + newHeading);
        mapView.processDeviceRotation(newHeading);
    }

    @Override
    public void didUpdateImmediateLocation(TYLocationManager arg0, TYLocalPoint newLocalPoint) {
        //  *  位置更新事件回调，位置更新并返回新的位置结果。
        // 与[TYLocationManager:didUpdateLocatin:]方法相近，此方法回调结果未融合手机传感器信息，灵敏度较高，适合用于行车场景下或传感器无效场景
        if (newLocalPoint.getFloor() != mapView.currentMapInfo.getFloorNumber()) {
            mapView.setFloor(TYMapInfo.searchMapInfoFromArray(mapView.allMapInfo(), newLocalPoint.getFloor()));
            return;
        }
        if (picIndex == 0) animateLocationSymbol();
        mapView.showLocation(newLocalPoint);
    }
    @Override
    public void didUpdateLocation(TYLocationManager arg0, TYLocalPoint newLocalPoint) {
        //  位置更新事件回调，位置更新并返回新的位置结果。
        //  与[TYLocationManager:didUpdateImmediationLocation:]方法相近，此方法回调结果融合手机设备传感器信息，稳定性较好，适合用于步行场景下
        //mapView.showLocation(newLocalPoint);
    }


    @Override
    public void didFailUpdateLocation(TYLocationManager tyLocationManager, Error error) {
        //定位失败，定位数据失败
        if (error != null) Log.e(TAG, error.toString());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //定位必须停止
        if (locationManager != null)
            locationManager.stopUpdateLocation();
    }
}
