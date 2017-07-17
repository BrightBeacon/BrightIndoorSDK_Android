package com.zs.brtmap.demo.map;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.ty.mapsdk.TYMapInfo;
import com.ty.mapsdk.TYMapView;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.R;

public class MapSetting extends BaseMapViewActivity {

    TextView text_settings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mapView.setMapBackground(网格颜色, 线条颜色, 网格宽度, 线条宽度);
        mapView.setMapBackground(Color.WHITE, Color.LTGRAY, 20, 10);

        text_settings = (TextView)findViewById(R.id.text_setting);
    }

    @Override
    public void initContentViewID() {
        contentViewID = R.layout.activity_map_setting;
    }

    @Override
    public void mapViewDidLoad(TYMapView mapView, Error error) {
        super.mapViewDidLoad(mapView, error);
        //mapView.building
    }

    @Override
    public void mapViewDidZoomed(TYMapView mapView) {
        super.mapViewDidZoomed(mapView);

    }

    @Override
    public void onFinishLoadingFloor(final TYMapView mapView, final TYMapInfo mapInfo) {
        super.onFinishLoadingFloor(mapView, mapInfo);

        //首次加载楼层，会自动设置地图比例尺
        //以下修改比例尺上下限（0.5~5倍，亦可自行计算：实际距离／屏幕距离）
        mapView.setMinScale(mapView.getXScaleFactor(0.5f));//设置最小占半屏
        mapView.setMaxScale(mapView.getXScaleFactor(5));//设置最大5倍屏幕

        //设置当前与屏等宽
        mapView.setScale(mapView.getXScaleFactor(1),true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text_settings.setText("地图信息："+ mapView.building.getName()+",北偏角："+mapView.building.getInitAngle()+"\n楼层信息："+mapInfo.getMapID()+ "\n旋转角度：" + mapView.getRotationAngle()+"\n比例尺："+mapView.getScale());
            }
        });

    }

    static {
        System.loadLibrary("TYMapSDK");
        //System.loadLibrary("TYLocationEngine");
    }
}
