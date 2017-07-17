package com.zs.brtmap.demo.map;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ty.mapsdk.TYMapInfo;
import com.ty.mapsdk.TYMapView;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.R;
import com.zs.brtmap.demo.utils.Utils;

public class MapLayers extends BaseMapViewActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView btn_priority = (TextView)findViewById(R.id.btn_priority);
        btn_priority.setOnClickListener(this);

        TextView btn_text = (TextView)findViewById(R.id.btn_text);
        btn_text.setOnClickListener(this);

        TextView btn_facility = (TextView)findViewById(R.id.btn_facility);
        btn_facility.setOnClickListener(this);
    }

    @Override
    public void initContentViewID() {
        contentViewID = R.layout.activity_map_layers;
    }

    @Override
    public void onFinishLoadingFloor(TYMapView mapView, TYMapInfo mapInfo) {
        super.onFinishLoadingFloor(mapView, mapInfo);

        mapView.setMinScale(mapView.getXScaleFactor(0.1));
        mapView.setMaxScale(mapView.getXScaleFactor(10));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case  R.id.btn_priority:
                view.setSelected(!view.isSelected());
                mapView.setFacilityPriority(view.isSelected());
                Utils.showToast(this,"缩小地图，让文字和图标碰撞试试");
                break;
            case  R.id.btn_text:
                view.setSelected(!view.isSelected());
                mapView.getLabelGroupLayer().getLayer(1).setVisible(view.isSelected());
                break;
            case  R.id.btn_facility:
                view.setSelected(!view.isSelected());
                mapView.getLabelGroupLayer().getLayer(0).setVisible(view.isSelected());
                break;
        }
    }

    static {
        System.loadLibrary("TYMapSDK");
        //System.loadLibrary("TYLocationEngine");
    }
}
