package com.zs.brtmap.demo.mark;

import android.graphics.Color;
import android.os.Bundle;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Line;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.ty.mapsdk.TYMapView;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.R;
import com.zs.brtmap.demo.utils.Utils;

public class MarkerLine extends BaseMapViewActivity {

    static {
        System.loadLibrary("TYMapSDK");
        //System.loadLibrary("TYLocationEngine");
    }

    GraphicsLayer drawLayer;
    Point ptPrevious;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initContentViewID() {
        contentViewID = R.layout.activity_marker_line;
    }

    @Override
    public void onClickAtPoint(TYMapView mapView, Point mappoint) {
        super.onClickAtPoint(mapView, mappoint);

        testDrawLayer(mappoint);
    }

    /**
     * 连续绘制线演示
     *
     * Polyline.addSegment(line);是连笔绘制，保持所有线段间相连
     * Polyline.lineTo(mappoint);提笔重新画，不相连会保持多线段
     *
     * @param mappoint
     */
    private void testDrawLayer(Point mappoint) {
        if (drawLayer == null) {
            drawLayer = new GraphicsLayer();
            mapView.addLayer(drawLayer);
        }
        // 点标记
        SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(Color.RED, 15, SimpleMarkerSymbol.STYLE.CIRCLE);
        // 线标记
        SimpleLineSymbol lineSymbol = new SimpleLineSymbol(Color.GREEN, 10, SimpleLineSymbol.STYLE.SOLID);

        if (drawLayer.getNumberOfGraphics() == 0) {
            // 绘制第一个点
            Graphic graphic = new Graphic(mappoint, markerSymbol);
            drawLayer.addGraphic(graphic);
        } else {
            // 生成当前线段（由当前点和上一个点构成）
            Line line = new Line();
            line.setStart(ptPrevious);
            line.setEnd(mappoint);
            // 绘制当前线段
            Polyline polyline = new Polyline();
            polyline.addSegment(line, true);
            Graphic g = new Graphic(polyline, lineSymbol);
            drawLayer.addGraphic(g);
            // 计算当前线段的长度
            String length = Double.toString(Math.round(line.calculateLength2D())) + " 米";
            Utils.showToast(MarkerLine.this, "当前长度:" + length);
        }
        ptPrevious = mappoint;
    }
}
