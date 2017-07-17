package com.zs.brtmap.demo.oper;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.R;

public class OperGesture  extends BaseMapViewActivity implements View.OnClickListener {

    GraphicsLayer hintLayer;
    CheckBox ck_pinch,ck_pan,ck_zoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView btn_scale = (TextView)findViewById(R.id.btn_pinch);
        btn_scale.setOnClickListener(this);

        TextView btn_angle = (TextView)findViewById(R.id.btn_pan);
        btn_angle.setOnClickListener(this);

        TextView btn_center = (TextView)findViewById(R.id.btn_zoom);
        btn_center.setOnClickListener(this);
        ck_pinch = (CheckBox)findViewById(R.id.ck_pinch);
        ck_pan = (CheckBox)findViewById(R.id.ck_pan);
        ck_zoom = (CheckBox)findViewById(R.id.ck_zoom);


        TouchListener touchListener = new TouchListener(this, mapView);
        mapView.setOnTouchListener(touchListener);
    }

    @Override
    public void initContentViewID() {
        contentViewID = R.layout.activity_oper_gesture;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case  R.id.btn_pinch:
                ck_pinch.setChecked(!ck_pinch.isChecked());
                mapView.setAllowRotationByPinch(!ck_pinch.isChecked());
                break;
            case  R.id.btn_zoom:
                ck_zoom.setChecked(!ck_zoom.isChecked());
                if (ck_zoom.isChecked()) {
                    mapView.setMinScale(mapView.getXScaleFactor(1));
                    mapView.setMaxScale(mapView.getXScaleFactor(1));
                }else {
                    mapView.setMinScale(mapView.getXScaleFactor(0.5));
                    mapView.setMaxScale(mapView.getXScaleFactor(5));
                }
                break;
            case  R.id.btn_pan:
                ck_pan.setChecked(!ck_pan.isChecked());
                break;
        }
    }

    public class TouchListener extends MapOnTouchListener {

        public TouchListener(Context context, MapView view) {
            super(context, view);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (ck_pan.isChecked()){
                return false;
            }
            return super.onTouch(v, event);
        }
    }

    static {
        System.loadLibrary("TYMapSDK");
        //System.loadLibrary("TYLocationEngine");
    }
}