package com.zs.brtmap.demo.mark;

import android.graphics.Color;
import android.os.Bundle;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Line;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.ty.mapsdk.TYMapView;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.R;
import com.zs.brtmap.demo.utils.Utils;

public class MarkerArea extends BaseMapViewActivity {

    static {
        System.loadLibrary("TYMapSDK");
        //System.loadLibrary("TYLocationEngine");
    }

    GraphicsLayer drawLayer;
    Polygon tempPolygon;
    Point ptPrevious;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initContentViewID() {
        contentViewID = R.layout.activity_marker_area;
    }
    @Override
    public void onClickAtPoint(TYMapView mapView, Point mappoint) {
        super.onClickAtPoint(mapView, mappoint);

        testDrawLayer(mappoint);
    }

    private void  testDrawLayer(Point mappoint) {
        if (drawLayer == null) {
            drawLayer = new GraphicsLayer();
            mapView.addLayer(drawLayer);
        }
        // 点标记
        SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(Color.RED, 15, SimpleMarkerSymbol.STYLE.CIRCLE);
        // 线标记
        SimpleLineSymbol lineSymbol = new SimpleLineSymbol(Color.GREEN, 10, SimpleLineSymbol.STYLE.SOLID);
        // 面填充
        SimpleFillSymbol fillSymbol = new SimpleFillSymbol(Color.YELLOW);
        fillSymbol.setOutline(lineSymbol);

        if (drawLayer.getNumberOfGraphics() == 0) {
            // 绘制第一个点
            Graphic graphic = new Graphic(mappoint, markerSymbol);
            drawLayer.addGraphic(graphic);
        } else {
            // 生成当前线段（由当前点和上一个点构成）
            Line line = new Line();
            line.setStart(ptPrevious);
            line.setEnd(mappoint);
            // 绘制临时多边形
            if (tempPolygon == null)
                tempPolygon = new Polygon();
            tempPolygon.addSegment(line, false);

            drawLayer.removeAll();
            Graphic g = new Graphic(tempPolygon, fillSymbol);
            drawLayer.addGraphic(g);

            // 计算当前面积
            String sArea = getAreaString(tempPolygon.calculateArea2D());

            Utils.showToast(MarkerArea.this, "总面积:" + sArea);
        }
        ptPrevious = mappoint;
    }

        private String getAreaString(double dValue) {
            long area = Math.abs(Math.round(dValue));
            String sArea = "";
            // 顺时针绘制多边形，面积为正，逆时针绘制，则面积为负
            if (area >= 1000000) {
                double dArea = area / 1000000.0;
                sArea = Double.toString(dArea) + " 平方公里";
            } else
                sArea = Double.toString(area) + " 平方米";

            return sArea;
        }

}
