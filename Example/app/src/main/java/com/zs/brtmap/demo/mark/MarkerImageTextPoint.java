package com.zs.brtmap.demo.mark;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.MarkerSymbol;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYPictureMarkerSymbol;
import com.ty.mapsdk.TYTextSymbol;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.R;
import com.zs.brtmap.demo.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class MarkerImageTextPoint extends BaseMapViewActivity implements View.OnClickListener{


    GraphicsLayer hintLayer;
    GraphicsLayer rotateLayer;
    Callout mapCallout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView btn_scale = (TextView)findViewById(R.id.btn_image);
        btn_scale.setOnClickListener(this);

        TextView btn_angle = (TextView)findViewById(R.id.btn_text);
        btn_angle.setOnClickListener(this);

        TextView btn_center = (TextView)findViewById(R.id.btn_point);
        btn_center.setOnClickListener(this);
    }

    @Override
    public void initContentViewID() {
        contentViewID = R.layout.activity_marker_image_text_point;
    }

    @Override
    public void mapViewDidLoad(TYMapView mapView, Error error) {
        super.mapViewDidLoad(mapView, error);
        if (error == null) {
            //GraphicsLayer.RenderingMode.DYNAMIC 地图旋转时，内容保持不动
            hintLayer = new GraphicsLayer(GraphicsLayer.RenderingMode.STATIC);
            mapView.addLayer(hintLayer);

            rotateLayer = new GraphicsLayer(GraphicsLayer.RenderingMode.DYNAMIC);
            mapView.addLayer(rotateLayer);
        }
    }

    @Override
    public void onClickAtPoint(TYMapView mapView, Point mappoint) {
        super.onClickAtPoint(mapView, mappoint);

        Point screenPoint = mapView.toScreenPoint(mappoint);
        //获取屏幕点?dp范围内的Graphic
        int[] gids = hintLayer.getGraphicIDs((float) screenPoint.getX(),(float) screenPoint.getY(),25);
        if (gids.length > 0) {
            Graphic graphic = hintLayer.getGraphic(gids[0]);
            mapCallout = mapView.getCallout();
            mapCallout.setStyle(R.xml.callout_style);
            mapCallout.setMaxWidthDp(320);
            mapCallout.setMaxHeightDp(320);
            mapCallout.setOffset(0, -15);
            mapCallout.show((Point) graphic.getGeometry(), loadCalloutView((String) graphic.getAttributeValue("name"),"副标题"));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case  R.id.btn_image: {
                TYPictureMarkerSymbol pictureMarkerSymbol = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.red_pushpin));
                pictureMarkerSymbol.setWidth(24);
                pictureMarkerSymbol.setHeight(24);
                pictureMarkerSymbol.setOffsetX(6);
                pictureMarkerSymbol.setOffsetY(12);

                Map<String, Object> attr = new HashMap<String, Object>();
                attr.put("name", "这是一张图片");
                hintLayer.addGraphic(new Graphic(mapView.getCenter(), pictureMarkerSymbol, attr));
            }
                break;
            case  R.id.btn_text: {
                Map<String, Object> attr = new HashMap<String, Object>();
                attr.put("name", "这是一文字");
                TextSymbol textSymbol = new TextSymbol(15, "这是文字，DroidSansFallback字体可能缺失", Color.RED);
                textSymbol.setFontFamily("font/DroidSansFallback.ttf");
                hintLayer.addGraphic(new Graphic(mapView.getCenter(), textSymbol,attr));

                TYTextSymbol textPic = new TYTextSymbol(this,15,"中文字体转换显示,中文推荐使用",Color.RED);
                textPic.setOffsetY(20);
                hintLayer.addGraphic(new Graphic(mapView.getCenter(), textPic,attr));

                TYTextSymbol rotateTextPic = new TYTextSymbol(this,15,"这段文字不跟随地图，旋转试试！",Color.BLUE);
                rotateTextPic.setOffsetY(-20);
                rotateLayer.addGraphic(new Graphic(mapView.getCenter(),rotateTextPic,attr));
            }
                break;
            case  R.id.btn_point: {
                Map<String, Object> attr = new HashMap<String, Object>();
                attr.put("name", "这是一点");
                MarkerSymbol markerSymbol = new SimpleMarkerSymbol(Color.BLUE, 15, SimpleMarkerSymbol.STYLE.CIRCLE);
                hintLayer.addGraphic(new Graphic(mapView.getCenter(), markerSymbol,attr));
            }
                break;
        }
    }

    static {
        System.loadLibrary("TYMapSDK");
        //System.loadLibrary("TYLocationEngine");
    }

    // 加载自定义弹出框内容
    private View loadCalloutView(final String title, String detail) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.layout_callout, null);
        TextView titleView = (TextView) view.findViewById(R.id.callout_title);
        titleView.setText(title);
        TextView detailView = (TextView) view.findViewById(R.id.callout_detail);
        detailView.setText(detail);
        TextView cancelBtn = (TextView) view.findViewById(R.id.callout_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapCallout.hide();
            }
        });
        TextView doneBtn = (TextView) view.findViewById(R.id.callout_done);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showToast(MarkerImageTextPoint.this, title);
                mapCallout.hide();
            }
        });
        return view;
    }
}
