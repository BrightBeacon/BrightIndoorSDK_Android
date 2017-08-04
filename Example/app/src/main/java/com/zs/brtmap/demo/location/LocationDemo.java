package com.zs.brtmap.demo.location;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.esri.android.map.Callout;
import com.esri.android.map.Layer;
import com.esri.android.map.event.OnPinchListener;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.ty.locationengine.ble.TYBeacon;
import com.ty.locationengine.ble.TYLocationManager;
import com.ty.locationengine.ble.TYLocationManager.TYLocationManagerListener;
import com.ty.locationengine.ble.TYPublicBeacon;
import com.ty.mapdata.TYLocalPoint;
import com.ty.mapsdk.TYDirectionalHint;
import com.ty.mapsdk.TYMapInfo;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYOfflineRouteManager;
import com.ty.mapsdk.TYPictureMarkerSymbol;
import com.ty.mapsdk.TYPoi;
import com.ty.mapsdk.TYRoutePart;
import com.ty.mapsdk.TYRouteResult;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.R;
import com.zs.brtmap.demo.utils.Constants;
import com.zs.brtmap.demo.utils.Utils;

import java.util.List;

public class LocationDemo extends BaseMapViewActivity implements View.OnClickListener,TYLocationManagerListener, TYOfflineRouteManager.TYOfflineRouteManagerListener {


    ImageView northArrow;
    TextView btnLocation;
    TextView textTips;
    TYLocationManager locationManager;

    TYLocalPoint startPoint,endPoint;
    boolean isRouting;

    Callout mapCallout;

    static {
        System.loadLibrary("TYMapSDK");
        System.loadLibrary("TYLocationEngine");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //初始化定位按钮
        btnLocation = (TextView)findViewById(R.id.btn_location);
        btnLocation.setOnClickListener(this);

        //初始化指南针
        northArrow = (ImageView)findViewById(R.id.north_arrow);
        rotateNorthOnPinch();

        //初始化弹窗
        mapCallout = mapView.getCallout();
        mapCallout.setStyle(R.xml.callout_style);
        mapCallout.setMaxWidth(Utils.dip2px(this,300));
        mapCallout.setMaxHeight(Utils.dip2px(this,300));

        //提示信息
        textTips = (TextView)findViewById(R.id.text_tips);

    }

    @Override
    public void initContentViewID() {
        contentViewID = R.layout.activity_location_demo;
    }

    private void rotateNorthOnPinch () {
        mapView.setOnPinchListener(new OnPinchListener() {
            @Override
            public void prePointersMove(float v, float v1, float v2, float v3, double v4) {

            }

            @Override
            public void postPointersMove(float v, float v1, float v2, float v3, double v4) {
                northArrow.post(new Runnable() {
                    @Override
                    public void run() {
                        double angle = mapView.getRotationAngle() + mapView.building.getInitAngle();
                        northArrow.setRotation((float) -angle);
                    }
                });
            }

            @Override
            public void prePointersDown(float v, float v1, float v2, float v3, double v4) {

            }

            @Override
            public void postPointersDown(float v, float v1, float v2, float v3, double v4) {
            }

            @Override
            public void prePointersUp(float v, float v1, float v2, float v3, double v4) {

            }

            @Override
            public void postPointersUp(float v, float v1, float v2, float v3, double v4) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_location:
            {
                if (locationManager == null){
                    return;
                }
                view.setSelected(!view.isSelected());
                if (view.isSelected()){
                    locationManager.startUpdateLocation();
                    view.setBackground(getResources().getDrawable(R.drawable.btn_locate));
                }else {
                    locationManager.stopUpdateLocation();
                    view.setBackground(getResources().getDrawable(R.drawable.btn_locate_gray));
                }
            }
            break;
        }
    }

    private void  initSymbols () {
        TYPictureMarkerSymbol pms = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.location_arrow));
        pms.setHeight(30);
        pms.setWidth(30);
        mapView.setLocationSymbol(pms);

        pms = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.start));
        pms.setHeight(40);
        pms.setWidth(30);
        pms.setOffsetY(20);
        mapView.setStartSymbol(pms);

        pms = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.end));
        pms.setHeight(40);
        pms.setWidth(30);
        pms.setOffsetY(20);
        mapView.setEndSymbol(pms);

        pms = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.nav_exit));
        pms.setHeight(24);
        pms.setWidth(24);
        mapView.setSwitchSymbol(pms);

    }

    private void showTextTips(final String tips) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textTips.setText(tips);
            }
        });
    }

    @Override
    public void mapViewDidLoad(TYMapView mapView, Error error) {
        super.mapViewDidLoad(mapView, error);

        //设置指南针初始为地图北偏角
        northArrow.setRotation((float) -mapView.building.getInitAngle());

        //初始化定位引擎
        locationManager = new TYLocationManager(this,mapView.building.getBuildingID(), Constants.APP_KEY);
        locationManager.addLocationEngineListener(this);

        //设置定位图标、路径图标
        initSymbols();
    }

    @Override
    public void onFinishLoadingFloor(TYMapView mapView, TYMapInfo mapInfo) {
        super.onFinishLoadingFloor(mapView, mapInfo);
        if (isRouting){
            mapView.showRouteResultOnCurrentFloor();
        }
    }

    //地图点击
    @Override
    public void onClickAtPoint(TYMapView mapView, Point mappoint) {
        super.onClickAtPoint(mapView, mappoint);

        if (isRouting) {
            didUpdateLocation(null,new TYLocalPoint(mappoint.getX(),mappoint.getY(),mapView.getCurrentMapInfo().getFloorNumber()));
            return;
        }

        TYPoi poi = mapView.extractRoomPoiOnCurrentFloor(mappoint.getX(),mappoint.getY());
        if (poi == null) {
            Utils.showToast(LocationDemo.this, "请选择地图范围内的点");
            return;
        }
        String title = (poi!=null&&poi.getName()!=null)?poi.getName():"未知道路";
        mapCallout.show(mappoint,loadCalloutView(title, mappoint));
    }

    // 加载自定义弹出框内容
    private View loadCalloutView(final String title, final Point pt) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.layout_callout, null);
        TextView titleView = (TextView) view.findViewById(R.id.callout_title);
        titleView.setText(title);
        TextView detailView = (TextView) view.findViewById(R.id.callout_detail);
        detailView.setText("x:" + pt.getX() + "\ny:" + pt.getY());
        TextView cancelBtn = (TextView) view.findViewById(R.id.callout_cancel);
        cancelBtn.setText("起点");
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapCallout.hide();
                setStartPoint(pt);
            }
        });
        TextView doneBtn = (TextView) view.findViewById(R.id.callout_done);
        doneBtn.setText("终点");
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapCallout.hide();
                setEndPoint(pt);
            }
        });
        return view;
    }
    private void setStartPoint(Point currentPoint) {
        startPoint = new TYLocalPoint(currentPoint.getX(), currentPoint.getY(),
                mapView.getCurrentMapInfo().getFloorNumber());
        mapView.showRouteStartSymbolOnCurrentFloor(startPoint);
        requestRoute();
    }

    private void setEndPoint(Point currentPoint) {
        endPoint = new TYLocalPoint(currentPoint.getX(), currentPoint.getY(),
                mapView.getCurrentMapInfo().getFloorNumber());
        mapView.showRouteEndSymbolOnCurrentFloor(endPoint);
        requestRoute();
    }

    private void requestRoute() {
        if (startPoint == null || endPoint == null) {
            Utils.showToast(LocationDemo.this, "需要两个点请求路径！");
            return;
        }
        //移除之前，规划路线
        mapView.resetRouteLayer();
        mapView.routeManager().addRouteManagerListener(this);
        mapView.routeManager().requestRoute(startPoint, endPoint);
    }

    //以下定位回调
    @Override
    public void didRangedBeacons(TYLocationManager tyLocationManager, List<TYBeacon> list) {

    }

    @Override
    public void didRangedLocationBeacons(TYLocationManager tyLocationManager, List<TYPublicBeacon> list) {

    }

    @Override
    public void didUpdateLocation(TYLocationManager tyLocationManager, TYLocalPoint tyLocalPoint) {

        //自动切换到定位楼层
        if (tyLocalPoint.getFloor() != mapView.currentMapInfo.getFloorNumber()) {
            mapView.setFloor(tyLocalPoint.getFloor()+"");
        }
        mapView.showLocation(tyLocalPoint);
        mapView.showPassedAndRemainingRouteResultOnCurrentFloor(tyLocalPoint);

        if (!isRouting) return;

        //终点判断，示例5米以内到达终点
        if (mapView.getRouteResult().distanceToRouteEnd(tyLocalPoint)<5){
            mapView.resetRouteLayer();
            isRouting = false;
            startPoint = null;
            endPoint = null;
            showTextTips("已到达终点附近。");
            return;
        }

        //偏航判断，示例5米偏航
        if (mapView.getRouteResult().isDeviatingFromRoute(tyLocalPoint,5)) {
            mapView.clearRouteLayer();
            startPoint = tyLocalPoint;
            mapView.routeManager().requestRoute(startPoint,endPoint);
            showTextTips("已偏航5米，重新规划路径。");
            return;
        }

        String tips = "前行";
        TYRoutePart part = mapView.getRouteResult().getNearestRoutePart(tyLocalPoint);
        if (part != null) {
            List<TYDirectionalHint> hints = mapView.getRouteResult().getRouteDirectionalHint(part);
            TYDirectionalHint hint = mapView.getRouteResult().getDirectionalHintForLocationFromHints(tyLocalPoint,hints);
            if (hint != null){
                mapView.showRouteHint(hint,false);
                tips = "剩余：" + String.format("%.2f", mapView.getRouteResult().distanceToRouteEnd(tyLocalPoint))
                        + "(" + String.format("%.2f", mapView.getRouteResult().length)+")"+"\n"+hint.getDirectionString();
                mapView.setRotationAngle(hint.getCurrentAngle(),true);
                northArrow.setRotation((float) (-mapView.getRotationAngle()-mapView.building.getInitAngle()));
            }else {
                tips = "剩余：" + String.format("%.2f", mapView.getRouteResult().distanceToRouteEnd(tyLocalPoint))
                        + "(" + String.format("%.2f", mapView.getRouteResult().length)+")"+"\n沿路前行";
            }
        }
        showTextTips(tips);
    }

    @Override
    public void didUpdateImmediateLocation(TYLocationManager tyLocationManager, TYLocalPoint tyLocalPoint) {
    }

    @Override
    public void didFailUpdateLocation(TYLocationManager tyLocationManager, Error error) {

    }

    @Override
    public void didUpdateDeviceHeading(TYLocationManager tyLocationManager, double v) {
    //根据TYMapView.TYMapViewMode处理定位图标旋转、或地图旋转
        mapView.processDeviceRotation(v);
    }

    //路线规划回调
    @Override
    public void didSolveRouteWithResult(TYOfflineRouteManager tyOfflineRouteManager, TYRouteResult tyRouteResult) {
        mapView.setRouteResult(tyRouteResult);
        mapView.setRouteStart(startPoint);
        mapView.setRouteEnd(endPoint);
        mapView.showRouteResultOnCurrentFloor();

        isRouting = true;

        String tips = "全程："+String.format("%.2f",tyRouteResult.length)+"米"+"预计耗时："+String.format("%.2f",tyRouteResult.length/80)+"分钟";
        showTextTips(tips);
    }

    @Override
    public void didFailSolveRouteWithError(TYOfflineRouteManager tyOfflineRouteManager, Exception e) {
        isRouting = false;
        Utils.showToast(this,"未找到路线");
    }
}
