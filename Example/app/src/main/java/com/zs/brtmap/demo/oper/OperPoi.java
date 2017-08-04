package com.zs.brtmap.demo.oper;

import android.graphics.Color;
import android.os.Bundle;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYPoi;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.R;

import java.util.List;

public class OperPoi extends BaseMapViewActivity {

    GraphicsLayer hintLayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initContentViewID() {
        contentViewID = R.layout.activity_oper_poi;
    }


    @Override
    public void mapViewDidLoad(TYMapView mapView, Error error) {
        super.mapViewDidLoad(mapView, error);
        if (error == null) {
            hintLayer = new GraphicsLayer(GraphicsLayer.RenderingMode.DYNAMIC);
            mapView.addLayer(hintLayer);
        }
    }

    @Override
    public void onPoiSelected(TYMapView mapView, List<TYPoi> poiList) {
        super.onPoiSelected(mapView, poiList);

        hintLayer.removeAll();
        for (TYPoi poi:poiList) {
            //填充区域
            hintLayer.addGraphic(new Graphic(poi.getGeometry(),new SimpleFillSymbol(Color.argb(100,0,255,255))));
            //添加边线
            hintLayer.addGraphic(new Graphic(poi.getGeometry(),new SimpleLineSymbol(Color.RED,2)));

            Point centerPt = null;
            Geometry geom = poi.getGeometry();
            if (geom instanceof Polygon) {
                //获取标点
                centerPt = GeometryEngine.getLabelPointForPolygon((Polygon)geom,mapView.getSpatialReference());
            }else {
                centerPt = (Point)geom;
            }
            //显示点
            hintLayer.addGraphic(new Graphic(centerPt,new SimpleMarkerSymbol(Color.BLUE,5, SimpleMarkerSymbol.STYLE.CIRCLE)));
        }
    }

    static {
        System.loadLibrary("TYMapSDK");
        //System.loadLibrary("TYLocationEngine");
    }
}