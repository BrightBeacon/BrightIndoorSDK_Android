package com.zs.brtmap.demo.map;

import android.app.Activity;
import android.os.Bundle;

import com.ty.mapsdk.TYMapEnvironment;
import com.ty.mapsdk.TYMapInfo;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.tiled.TYTiledLayer;
import com.ty.mapsdk.tiled.TYTiledManager;
import com.zs.brtmap.demo.R;

public class TileActivity extends Activity {

    static final String TAG = TileActivity.class.getSimpleName();
    static {
        System.loadLibrary("TYMapSDK");
        //System.loadLibrary("TYLocationEngine");
    }
    TYTiledManager tiledManager;
    long layerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tiledManager = new TYTiledManager("00210025");//Constants.BUILDING_ID);
        setContentView(R.layout.activity_tile);

        TYMapEnvironment.initMapEnvironment();

        TYMapView mapView = (TYMapView) findViewById(R.id.map);

        //演示用，实际地图会由矢量地图调用。
        onFinishLoadingFloor(mapView,null);
    }

    public void onFinishLoadingFloor(final TYMapView mapView, TYMapInfo mapInfo) {
        //瓦片地图需要矢量地图配合才可以拾取POI信息等，所以可以在矢量楼层切换这里，加载瓦片数据
        if(layerID >0)mapView.removeLayer(mapView.getLayerByID(layerID));
        TYTiledLayer tiledLayer = new TYTiledLayer(tiledManager.tileInfoByFloor("1"));
        layerID = mapView.addLayer(tiledLayer);

        mapView.setExtent(tiledLayer.getExtent());
    }
}
