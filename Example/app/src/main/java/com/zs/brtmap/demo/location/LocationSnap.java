package com.zs.brtmap.demo.location;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.ty.locationengine.ble.TYBeacon;
import com.ty.locationengine.ble.TYLocationManager;
import com.ty.locationengine.ble.TYPublicBeacon;
import com.ty.mapdata.TYLocalPoint;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYOfflineRouteManager;
import com.ty.mapsdk.TYPictureMarkerSymbol;
import com.ty.mapsdk.TYRoutePart;
import com.ty.mapsdk.TYRouteResult;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.R;
import com.zs.brtmap.demo.utils.Constants;
import com.zs.brtmap.demo.utils.Utils;

import java.util.List;

public class LocationSnap extends BaseMapViewActivity implements TYLocationManager.TYLocationManagerListener, TYOfflineRouteManager.TYOfflineRouteManagerListener {

    static {
        System.loadLibrary("TYMapSDK");
        System.loadLibrary("TYLocationEngine");
    }

    TYLocationManager locationManager;
    GraphicsLayer graphicsLayer;
    int graphicID;
    TextView btnSnap;

    TYLocalPoint startPoint;
    TYLocalPoint endPoint;
    boolean isRouting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btnSnap = (TextView)findViewById(R.id.btn_snap);
        btnSnap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                simulateLocation();
            }
        });


        TextView btnLocation = (TextView) findViewById(R.id.show_location);
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    locationManager.stopUpdateLocation();
                    Utils.showToast(LocationSnap.this, "停止定位");
                    mapView.setBackground(getResources().getDrawable(R.drawable.btn_locate_gray));
                } else {
                    locationManager.startUpdateLocation();
                    Utils.showToast(LocationSnap.this, "开始定位");
                    v.setBackground(getResources().getDrawable(R.drawable.btn_locate));
                }
                v.setSelected(!v.isSelected());
            }
        });
    }

    @Override
    public void initContentViewID() {
        contentViewID = R.layout.activity_location_snap;
    }

    //地图回调
    @Override
    public void mapViewDidLoad(TYMapView mapView, Error error) {
        super.mapViewDidLoad(mapView, error);
        if (error == null) {
            locationManager = new TYLocationManager(LocationSnap.this,mapView.building.getBuildingID(), Constants.APP_KEY);
            locationManager.addLocationEngineListener(LocationSnap.this);

            graphicsLayer = new GraphicsLayer();
            mapView.addLayer(graphicsLayer);
        }
    }

    @Override
    public void onClickAtPoint(TYMapView mapView, Point mappoint) {
        super.onClickAtPoint(mapView, mappoint);
        //路径规划
        endPoint = startPoint;
        startPoint = new TYLocalPoint(mappoint.getX(),mappoint.getY(),mapView.currentMapInfo.getFloorNumber());
        mapView.showRouteStartSymbolOnCurrentFloor(startPoint);
        mapView.showRouteEndSymbolOnCurrentFloor(endPoint);
        if (null!=endPoint) {
            mapView.routeManager().addRouteManagerListener(this);
            mapView.routeManager().requestRoute(startPoint,endPoint);
        }
    }


    //定位回调
    @Override
    public void didRangedBeacons(TYLocationManager tyLocationManager, List<TYBeacon> list) {

    }

    @Override
    public void didRangedLocationBeacons(TYLocationManager tyLocationManager, List<TYPublicBeacon> list) {

    }

    @Override
    public void didUpdateLocation(TYLocationManager tyLocationManager, TYLocalPoint tyLocalPoint) {

    }

    @Override
    public void didUpdateImmediateLocation(TYLocationManager tyLocationManager, TYLocalPoint tyLocalPoint) {
        snapToRoute(tyLocalPoint);
    }

    @Override
    public void didFailUpdateLocation(TYLocationManager tyLocationManager, Error error) {

    }

    @Override
    public void didUpdateDeviceHeading(TYLocationManager tyLocationManager, double v) {
        mapView.processDeviceRotation(v);
    }

    //路径规划回调
    @Override
    public void didSolveRouteWithResult(TYOfflineRouteManager tyOfflineRouteManager, TYRouteResult tyRouteResult) {
        mapView.setRouteResult(tyRouteResult);
        mapView.showRouteResultOnCurrentFloor();
        isRouting = true;
    }

    @Override
    public void didFailSolveRouteWithError(TYOfflineRouteManager tyOfflineRouteManager, Exception e) {
        isRouting = false;
    }

    private void snapToRoute (TYLocalPoint lp) {

        //显示原始点
        if (graphicID !=0) {
            graphicsLayer.updateGraphic(graphicID,new Point(lp.getX(),lp.getY()));
        }else {
            TYPictureMarkerSymbol pms = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.location_arrow));
            pms.setWidth(24);
            pms.setHeight(24);
            graphicID = graphicsLayer.addGraphic(new Graphic(new Point(lp.getX(),lp.getY()),pms));
        }

        //如果点距离规划路径5米内，吸附到规划路线；否则吸附到地图路网。吸附仅是可选方案，根据实际情况参考是否使用。
        if (isRouting&&mapView.getRouteResult().isDeviatingFromRoute(lp,5)==false) {
            lp = mapView.getRouteResult().getNearestPointOnRoute(lp);
        }else {
            lp = mapView.routeManager().getNearestRoutePoint(lp);
        }
        mapView.showLocation(lp);
    }

    //产生一个随机地图点、或随机路线上的点
    private void simulateLocation() {
        Point random = isRouting?getRandomPointAroudRoute():getRandomPoint();
        snapToRoute(new TYLocalPoint(random.getX(),random.getY(),mapView.currentMapInfo.getFloorNumber()));
    }

    private Point getRandomPoint() {
        Point pt = new Point(mapView.currentMapInfo.getMapExtent().getXmin(),mapView.currentMapInfo.getMapExtent().getYmax());
        pt.setXY(pt.getX()+Math.random()*mapView.currentMapInfo.getMapSize().getX(),pt.getY()-Math.random()*mapView.currentMapInfo.getMapSize().getY());
        return pt;
    }

    private Point getRandomPointAroudRoute() {
        TYRouteResult routeResult = mapView.getRouteResult();
        Point pt = new Point();
        for (TYRoutePart part:routeResult.getRoutePartsOnFloor(mapView.currentMapInfo.getFloorNumber())) {
            pt = part.getRoute().getPoint((int) (Math.random()*part.getRoute().getPointCount()));
        }
        pt.setXY(pt.getX()+Math.random()*10-5,pt.getY()+Math.random()*10-5);
        return pt;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null)
            locationManager.stopUpdateLocation();
    }
}
