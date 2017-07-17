package com.zs.brtmap.demo.mark;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.ty.mapsdk.TYMapInfo;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYPictureMarkerSymbol;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.R;

public class MarkerFence extends BaseMapViewActivity implements View.OnClickListener {

    static {
        System.loadLibrary("TYMapSDK");
    }

    GraphicsLayer drawLayer;
    int graphicID;
    Polygon polygon;
    TextView btn_man,text_tip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        btn_man = (TextView)findViewById(R.id.btn_man);
        text_tip = (TextView )findViewById(R.id.text_tip);

        btn_man.setOnClickListener(this);
    }

    @Override
    public void initContentViewID() {
        contentViewID = R.layout.activity_marker_fence;
    }


    @Override
    public void onClick(View view) {
        if (graphicID != 0) {
            drawLayer.updateGraphic(graphicID,new Point(mapView.currentMapInfo.getMapExtent().getXmin(),mapView.currentMapInfo.getMapExtent().getYmax()));
            movePoint(1);
            view.setEnabled(false);
        }
    }

    private void  movePoint(final double delta) {
        //移动点位
        Graphic graphic = drawLayer.getGraphic(graphicID);
        if (graphic == null) return;
        boolean isOldIn = GeometryEngine.contains(polygon,graphic.getGeometry(),mapView.getSpatialReference());
        Point point = (Point) graphic.getGeometry();
        point.setX(point.getX()+delta);
        point.setY(point.getY()-delta);
        drawLayer.updateGraphic(graphicID,point);

        double distance =  GeometryEngine.distance(polygon,point,mapView.getSpatialReference());
        text_tip.setText(String.format("距离区域%.2f米",distance));
        boolean isNowIn = GeometryEngine.contains(polygon,point,mapView.getSpatialReference());
        if (isNowIn != isOldIn){
            Toast.makeText(MarkerFence.this,isNowIn?"进入区域":"离开区域",Toast.LENGTH_SHORT).show();
            if (isOldIn) {
                btn_man.setEnabled(true);
                return;
            }
        }
        mapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                movePoint(delta);
            }
        },100);
    }


    @Override
    public void mapViewDidLoad(TYMapView mapView, Error error) {
        super.mapViewDidLoad(mapView, error);
        if (error == null) {
            TYMapInfo info = mapView.allMapInfo().get(0);
            Envelope envelope = new Envelope(info.getMapExtent().getXmin(),info.getMapExtent().getYmin(),info.getMapExtent().getXmax(),info.getMapExtent().getYmax());
            Point center = envelope.getCenter();
            polygon = new Polygon();
            getCircle(center,envelope.getWidth()/3.0,polygon);

            drawLayer = new GraphicsLayer(GraphicsLayer.RenderingMode.STATIC);
            drawLayer.addGraphic(new Graphic(polygon,new SimpleFillSymbol(Color.argb(120,0,200,0))));
            mapView.addLayer(drawLayer);

            Graphic moni_man = new Graphic(envelope.getUpperLeft(),new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.location_arrow)));
            graphicID = drawLayer.addGraphic(moni_man);
        }
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
