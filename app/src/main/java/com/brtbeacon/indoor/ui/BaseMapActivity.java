package com.brtbeacon.indoor.ui;

import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.brtbeacon.indoor.R;
import com.brtbeacon.indoor.util.FileHelper;
import com.ty.mapdata.TYBuilding;
import com.ty.mapdata.TYCity;
import com.ty.mapsdk.TYBuildingManager;
import com.ty.mapsdk.TYCityManager;
import com.ty.mapsdk.TYMapEnvironment;
import com.ty.mapsdk.TYMapInfo;
import com.ty.mapsdk.TYMapView;

import java.io.File;
import java.util.List;

/**
 * 基础地图展示
 */
public class BaseMapActivity extends AppCompatActivity {

    static {
        System.loadLibrary("TYMapSDK");
    }

    private TYMapView mapView;

    private final String CITY_ID = "0021";     //城市编号
    private final String BUILD_ID= "00210018";//建筑编号

    private String mapRootDir;

    protected TYCity currentCity;
    protected TYBuilding currentBuilding;
    protected TYMapInfo currentMapInfo;
    protected List<TYMapInfo> allMapInfos;

    protected int currentFloor = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapView = (TYMapView) findViewById(R.id.map);

        init();
    }

    private void init(){

        //1.设置地图数据保存在SD卡的位置
//        TYMapEnvironment.initMapEnvironment();
//        mapRootDir = Environment.getExternalStorageDirectory() + "/MapDemo/MapFiles";
//        TYMapEnvironment.setRootDirectoryForMapFiles(mapRootDir);

        //2.copy测试地图数据到SD卡
//        if(isFirst()){
//            copyMapFiles();
//        }

        //3.获得城市数据、建筑数据
        //获得城市信息
        currentCity = TYCityManager.parseCityFromFilesById(this, CITY_ID);

        //获得建筑信息
        currentBuilding = TYBuildingManager.parseBuildingFromFilesById(this, CITY_ID, BUILD_ID);

        //获得建筑的所有信息列表
        try{
            allMapInfos = TYMapInfo.parseMapInfoFromFiles(this, CITY_ID, BUILD_ID);
        }catch (Exception e){
            Toast.makeText(BaseMapActivity.this, "暂无定位", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }
        currentMapInfo = allMapInfos.get(currentFloor);

        //4.初始化地图并显示
        mapView.init(currentBuilding, "ty4e13f85911a44a75", "26db2af1ZzA3NzJuNTM#YGRkOWA2NjY#101ec55a");

        mapView.setFloor(currentMapInfo);
    }
}
