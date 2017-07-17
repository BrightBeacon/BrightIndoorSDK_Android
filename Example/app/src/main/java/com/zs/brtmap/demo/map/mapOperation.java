package com.zs.brtmap.demo.map;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.esri.core.geometry.Point;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.R;

public class MapOperation extends BaseMapViewActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView btn_scale = (TextView)findViewById(R.id.btn_scale);
        btn_scale.setOnClickListener(this);

        TextView btn_angle = (TextView)findViewById(R.id.btn_angle);
        btn_angle.setOnClickListener(this);

        TextView btn_center = (TextView)findViewById(R.id.btn_center);
        btn_center.setOnClickListener(this);
    }

    @Override
    public void initContentViewID() {
        contentViewID = R.layout.activity_map_operation;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case  R.id.btn_scale:
                mapView.setScale(mapView.getMinScale(),true);
                break;
            case  R.id.btn_angle:
                mapView.setRotationAngle(180,true);
                break;
            case  R.id.btn_center:
                if (mapView.initialEnvelope != null) {
                    Point center = mapView.initialEnvelope.getCenter();
                    mapView.centerAt(center, true);
                }
                break;
        }
    }

    static {
        System.loadLibrary("TYMapSDK");
        //System.loadLibrary("TYLocationEngine");
    }
}
