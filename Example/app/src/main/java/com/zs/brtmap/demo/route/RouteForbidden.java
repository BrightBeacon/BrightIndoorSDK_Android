package com.zs.brtmap.demo.route;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.esri.core.geometry.Point;
import com.ty.mapdata.TYLocalPoint;
import com.ty.mapsdk.PoiEntity;
import com.ty.mapsdk.TYMapInfo;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYOfflineRouteManager;
import com.ty.mapsdk.TYPictureMarkerSymbol;
import com.ty.mapsdk.TYRouteResult;
import com.ty.mapsdk.TYSearchAdapter;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.R;
import com.zs.brtmap.demo.utils.Utils;

import java.util.List;

public class RouteForbidden extends BaseMapViewActivity implements TYOfflineRouteManager.TYOfflineRouteManagerListener {

    static {
        System.loadLibrary("TYMapSDK");
        // System.loadLibrary("TYLocationEngine");
    }

    CheckBox ck_forbidden;

    @Override
    public void initContentViewID() {
        contentViewID = R.layout.activity_route_forbidden;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ck_forbidden = (CheckBox) findViewById(R.id.checkbox);
        ck_forbidden.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean ischecked) {
                mapView.routeManager().removeForbiddenPoints();
                String cid = "150014";
                if (ischecked){
                    ck_forbidden.setText("扶梯禁行");
                }else {
                    ck_forbidden.setText("电梯禁行");
                    cid = "150013";
                }
                TYSearchAdapter searchAdapter = new TYSearchAdapter(mapView.building.getBuildingID());
                List<PoiEntity> list = searchAdapter.queryPoiByCategoryID(cid);
                for (PoiEntity pe : list) {
                    mapView.routeManager().addForbiddenPoint(new TYLocalPoint(pe.getLabelX(),pe.getLabelY(),pe.getFloorNumber()));
                }
            }
        });
    }

    @Override
    public void mapViewDidLoad(final TYMapView mapView, Error error) {
        super.mapViewDidLoad(mapView, error);
        if (error != null) return;

        initSymbols();
    }



    private void initSymbols() {
        TYPictureMarkerSymbol startSymbol = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.start));
        startSymbol.setWidth(34);
        startSymbol.setHeight(43);
        startSymbol.setOffsetX(0);
        startSymbol.setOffsetY(22);
        mapView.setStartSymbol(startSymbol);

        TYPictureMarkerSymbol endSymbol = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.end));
        endSymbol.setWidth(34);
        endSymbol.setHeight(43);
        endSymbol.setOffsetX(0);
        endSymbol.setOffsetY(22);
        mapView.setEndSymbol(endSymbol);

        TYPictureMarkerSymbol switchSymbol = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.nav_exit));
        switchSymbol.setWidth(37);
        switchSymbol.setHeight(37);
        mapView.setSwitchSymbol(switchSymbol);
    }

    @Override
    public void onClickAtPoint(TYMapView mapView, Point mappoint) {
        super.onClickAtPoint(mapView, mappoint);

        TYLocalPoint lp = new TYLocalPoint(mappoint.getX(),mappoint.getY(),mapView.currentMapInfo.getFloorNumber());
        if (mapView.getRouteStartPoint() == null) {
            mapView.setRouteStart(lp);
            mapView.showRouteStartSymbolOnCurrentFloor(lp);
        }else {
            mapView.setRouteEnd(lp);
            mapView.showRouteEndSymbolOnCurrentFloor(lp);
            mapView.routeManager().addRouteManagerListener(this);
            mapView.routeManager().requestRoute(mapView.getRouteStartPoint(),mapView.getRouteEndPoint());
        }
    }

    @Override
    public void onFinishLoadingFloor(TYMapView mapView, TYMapInfo mapInfo) {
        super.onFinishLoadingFloor(mapView, mapInfo);
        mapView.showRouteResultOnCurrentFloor();
    }

    @Override
    public void didSolveRouteWithResult(TYOfflineRouteManager tyOfflineRouteManager, TYRouteResult tyRouteResult) {
        mapView.resetRouteLayer();
        mapView.showRouteStartSymbolOnCurrentFloor(mapView.getRouteStartPoint());
        mapView.showRouteEndSymbolOnCurrentFloor(mapView.getRouteEndPoint());
        mapView.setRouteResult(tyRouteResult);
        mapView.showRouteResultOnCurrentFloor();
    }

    @Override
    public void didFailSolveRouteWithError(TYOfflineRouteManager tyOfflineRouteManager, Exception e) {
        Utils.showToast(this,"未找到路径");
    }
}
