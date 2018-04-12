package com.zs.brtmap.demo.map;

import android.os.Bundle;
import android.widget.TextView;

import com.esri.core.geometry.Point;
import com.ty.mapsdk.TYMapView;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.R;

public class MapCoordinate extends BaseMapViewActivity {

    TextView text_coord;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        text_coord = (TextView)findViewById(R.id.text_coordinate);
    }

    @Override
    public void initContentViewID() {
        contentViewID = R.layout.activity_map_coordinate;
    }

    @Override
    public void onClickAtPoint(TYMapView mapView, Point mappoint) {
        //地图点击
        Point screenPoint = mapView.toScreenPoint(mappoint);
        Point mapPoint = mapView.toMapPoint(screenPoint);

        text_coord.setText("屏幕坐标：x:"+
                String.format("%.2f",screenPoint.getX())
                +",y:"+String.format("%.2f",screenPoint.getY())
                +"\n地图坐标：x:"+String.format("%.2f",mapPoint.getX())
                +",y:"+String.format("%.2f",mapPoint.getY())
                + "\n转换右下角0,0坐标:x:" + String.format("%.2f",mapPoint.getX() - mapView.currentMapInfo.getMapExtent().getXmin())
                +",y:"+String.format("%.2f",mapPoint.getY() - mapView.currentMapInfo.getMapExtent().getYmin())
        );
    }

    static {
        System.loadLibrary("TYMapSDK");
        //System.loadLibrary("TYLocationEngine");
    }
}