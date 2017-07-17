package com.zs.brtmap.demo.ctl;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.esri.android.map.event.OnPinchListener;
import com.ty.mapsdk.TYMapView;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.R;

public class CtlNorth extends BaseMapViewActivity {

    static {
        System.loadLibrary("TYMapSDK");
        //System.loadLibrary("TYLocationEngine");
    }

    ImageView northArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        northArrow = (ImageView)findViewById(R.id.north_arrow);

        TextView btn_rotate = (TextView)findViewById(R.id.btn_rotate);
        btn_rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapView.setRotationAngle(10+mapView.getRotationAngle(),true);
                double angle = mapView.getRotationAngle() + mapView.building.getInitAngle();
                northArrow.setRotation((float) -angle);
            }
        });
    }

    @Override
    public void initContentViewID() {
        contentViewID = R.layout.activity_ctl_north;
    }

    @Override
    public void mapViewDidLoad(final TYMapView mapView, Error error) {
        super.mapViewDidLoad(mapView, error);
        if (error == null) {
            northArrow.setRotation((float) -mapView.building.getInitAngle());
            mapView.setOnPinchListener(new OnPinchListener() {
                @Override
                public void prePointersMove(float v, float v1, float v2, float v3, double v4) {

                }

                @Override
                public void postPointersMove(float v, float v1, float v2, float v3, double v4) {
                    northArrow.post(new Runnable() {
                        @Override
                        public void run() {
                            double angle = mapView.getRotationAngle() + mapView.building.getInitAngle();
                            northArrow.setRotation((float) -angle);
                        }
                    });
                }

                @Override
                public void prePointersDown(float v, float v1, float v2, float v3, double v4) {

                }

                @Override
                public void postPointersDown(float v, float v1, float v2, float v3, double v4) {

                }

                @Override
                public void prePointersUp(float v, float v1, float v2, float v3, double v4) {

                }

                @Override
                public void postPointersUp(float v, float v1, float v2, float v3, double v4) {

                }
            });
        }
    }
}
