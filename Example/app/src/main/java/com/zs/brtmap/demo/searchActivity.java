package com.zs.brtmap.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.ty.mapsdk.PoiEntity;
import com.ty.mapsdk.TYMapInfo;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYPictureMarkerSymbol;
import com.ty.mapsdk.TYSearchAdapter;
import com.zs.brtmap.demo.utils.Utils;

import java.util.List;

public class searchActivity extends BaseMapViewActivity {

    List<PoiEntity> searchList;
    GraphicsLayer poiLayer;
    static final String TAG = CalloutActivity.class.getSimpleName();
    static {
        System.loadLibrary("TYMapSDK");
        // System.loadLibrary("TYLocationEngine");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initContentViewID() {
        contentViewID = R.layout.activity_search;
    }

    @Override
    public void mapViewDidLoad(TYMapView mapView, Error error) {
        super.mapViewDidLoad(mapView, error);
        if (error == null){
            poiLayer = new GraphicsLayer();
            mapView.addLayer(poiLayer);
        }
    }

    @Override
    public void onFinishLoadingFloor(TYMapView mapView, TYMapInfo mapInfo) {
        super.onFinishLoadingFloor(mapView, mapInfo);
        poiLayer.removeAll();
        TYSearchAdapter searchAdapter = new TYSearchAdapter(mapView.building.getBuildingID());
        searchList = searchAdapter.queryPoi("",mapInfo.getFloorNumber());
        for (PoiEntity entity : searchList) {
            TYPictureMarkerSymbol symbol = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.green_pushpin));
            symbol.setWidth(20);
            symbol.setHeight(20);
            symbol.setOffsetX(5);
            symbol.setOffsetY(10);
            Point point = new Point(entity.getLabelX(),entity.getLabelY());
            Graphic graphic = new Graphic(point,symbol);
            poiLayer.addGraphic(graphic);

            TextSymbol textSymbol = new TextSymbol("DroidSansFallback.ttf",entity.getName(), Color.BLACK);
            textSymbol.setOffsetX(-5);
            textSymbol.setOffsetY(-5);
            Graphic txtGraphic = new Graphic(point,textSymbol);
            poiLayer.addGraphic(txtGraphic);
        }
    }
}
