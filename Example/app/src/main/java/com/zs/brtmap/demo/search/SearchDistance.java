package com.zs.brtmap.demo.search;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.ty.mapsdk.PoiEntity;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYPictureMarkerSymbol;
import com.ty.mapsdk.TYSearchAdapter;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.R;

import java.util.List;

public class SearchDistance extends BaseMapViewActivity {

    static {
        System.loadLibrary("TYMapSDK");
    }

    EditText editText;
    GraphicsLayer graphicsLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editText = (EditText)findViewById(R.id.editText);
    }

    @Override
    public void initContentViewID() {
        contentViewID = R.layout.activity_search_distance;
    }

    @Override
    public void mapViewDidLoad(TYMapView mapView, Error error) {
        super.mapViewDidLoad(mapView, error);
        if (error == null) {
            graphicsLayer = new GraphicsLayer();
            mapView.addLayer(graphicsLayer);
        }
    }

    @Override
    public void onClickAtPoint(TYMapView mapView, Point mappoint) {
        super.onClickAtPoint(mapView, mappoint);

        graphicsLayer.removeAll();
        Polygon polygon = new Polygon();
        double radius = Double.parseDouble(editText.getText().toString());
        getCircle(mappoint,radius,polygon);
        graphicsLayer.addGraphic(new Graphic(polygon,new SimpleFillSymbol(Color.argb(120,255,50,50))));

        TYSearchAdapter searchAdapter = new TYSearchAdapter(mapView.building.getBuildingID(),1.0);

        List<PoiEntity> searchList = searchAdapter.queryPoiByRadius(mappoint,radius,mapView.getCurrentMapInfo().getFloorNumber());
        for (PoiEntity entity : searchList) {
            Point point = new Point(entity.getLabelX(),entity.getLabelY());
            Graphic graphic = new Graphic(point,getGreenpinSymbol());
            graphicsLayer.addGraphic(graphic);

//            TYTextSymbol textSymbol = new TYTextSymbol(this,15,entity.getName(), Color.RED);
//            textSymbol.setOffsetX(-5);
//            textSymbol.setOffsetY(-5);
//            Graphic txtGraphic = new Graphic(point,textSymbol);
//            graphicsLayer.addGraphic(txtGraphic);
        }
    }

    //绿色图标
    private TYPictureMarkerSymbol getGreenpinSymbol() {
        TYPictureMarkerSymbol symbol = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.green_pushpin));
        symbol.setWidth(20);
        symbol.setHeight(20);
        symbol.setOffsetX(5);
        symbol.setOffsetY(10);
        return symbol;
    }

    //	画圆形
    private void getCircle(Point center, double radius, Polygon circle) {
        circle.setEmpty();
        Point[] points = getPoints(center, radius);
        circle.startPath(points[0]);
        for (int i = 1; i < points.length; i++)
            circle.lineTo(points[i]);
    }

    private Point[] getPoints(Point center, double radius) {
        Point[] points = new Point[50];
        double sin;
        double cos;
        double x;
        double y;
        for (double i = 0; i < 50; i++) {
            sin = Math.sin(Math.PI * 2 * i / 50);
            cos = Math.cos(Math.PI * 2 * i / 50);
            x = center.getX() + radius * sin;
            y = center.getY() + radius * cos;
            points[(int) i] = new Point(x, y);
        }
        return points;
    }
}
