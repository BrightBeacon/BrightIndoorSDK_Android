package com.zs.brtmap.demo.map;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ty.mapsdk.TYMapEnvironment;
import com.ty.mapsdk.TYMapInfo;
import com.ty.mapsdk.TYMapView;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.R;

public class MapLocalActivity extends BaseMapViewActivity implements View.OnClickListener {

    static {
        System.loadLibrary("TYMapSDK");
        //System.loadLibrary("TYLocationEngine");
    }
    TextView text_settings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        text_settings = (TextView)findViewById(R.id.text_setting);
        text_settings.setOnClickListener(this);
    }

    @Override
    public void initContentViewID() {
        contentViewID = R.layout.activity_map_local;
    }

    @Override
    public void onClick(View view) {
        if (TYMapEnvironment.getMapCustomLocale()==null||TYMapEnvironment.getMapCustomLocale().equals("zh")){
            TYMapEnvironment.setMapCustomLanguage("en");
        }else{
            TYMapEnvironment.setMapCustomLanguage("zh");
        }
        this.mapView.reloadMapView();
    }

    @Override
    public void onFinishLoadingFloor(TYMapView mapView, TYMapInfo mapInfo) {
        super.onFinishLoadingFloor(mapView, mapInfo);
        Log.e(TAG,mapView.getLocalStringOnCurrentFloor());
    }
}
