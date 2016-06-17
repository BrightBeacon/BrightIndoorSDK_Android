package com.brtbeacon.indoor.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.brtbeacon.indoor.R;
import com.brtbeacon.indoor.util.FileHelper;
import com.esri.android.map.Callout;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.ty.locationengine.ble.TYBeacon;
import com.ty.locationengine.ble.TYLocationManager;
import com.ty.locationengine.ble.TYPublicBeacon;
import com.ty.locationengine.ibeacon.BeaconRegion;
import com.ty.mapdata.TYBuilding;
import com.ty.mapdata.TYCity;
import com.ty.mapdata.TYLocalPoint;
import com.ty.mapsdk.TYBuildingManager;
import com.ty.mapsdk.TYCityManager;
import com.ty.mapsdk.TYMapEnvironment;
import com.ty.mapsdk.TYMapInfo;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYOfflineRouteManager;
import com.ty.mapsdk.TYPictureMarkerSymbol;
import com.ty.mapsdk.TYPoi;
import com.ty.mapsdk.TYRouteResult;

import java.io.File;
import java.util.List;

public class NavActivity extends AppCompatActivity implements TYMapView.TYMapViewListenser,TYOfflineRouteManager.TYOfflineRouteManagerListener, TYLocationManager.TYLocationManagerListener {

    static {
        System.loadLibrary("TYMapSDK");
        System.loadLibrary("TYLocationEngine");
    }

    private TYMapView mapView;  //主显示地图view

    private String CITY_ID = "0021";            //城市编号
    private final String BUILD_ID= "00210018";//建筑编号

    private String mapRootDir; //地图在SD卡的目录

    private TYCity currentCity;           //城市数据
    private TYBuilding currentBuilding;  //建筑数据
    private TYMapInfo currentMapInfo;    //地图信息
    private List<TYMapInfo> allMapInfos; //地图信息列表

    protected TYLocationManager locationManager;//位置管理器
    protected TYOfflineRouteManager routeManager;//线路管理器

    private int currentFloor = 0;

    private Point tempPoint;//保存用户点击的点
    private Callout popview;//弹出视图

    private Point startPoint;//线路开始点
    private Point endPoint;  //线路结束点

    private TYLocalPoint pt1;
    private TYLocalPoint pt2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapView = (TYMapView) findViewById(R.id.map);
        mapView.addMapListener(this);

        init();
    }

    private void init(){

        //1.设置地图数据保存在SD卡的位置
//        TYMapEnvironment.initMapEnvironment();
//        mapRootDir = Environment.getExternalStorageDirectory() + "/MapDemo/MapFiles";
//        TYMapEnvironment.setRootDirectoryForMapFiles(mapRootDir);

        //2.copy测试地图数据到SD卡
        //第一次进入程序时拷贝地图数据到SD卡
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
            Toast.makeText(NavActivity.this, "暂无定位", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }
        currentMapInfo = allMapInfos.get(currentFloor);

        //4.初始化地图并显示
        mapView.init(currentBuilding, "ty4e13f85911a44a75", "26db2af1g0772n53`dd9`666101ec55a");
        mapView.setFloor(currentMapInfo);

        popview = mapView.getCallout();//获取弹出view的容器

        //locationManager
        String uuid = "FDA50693-A4E2-4FB1-AFCF-C6EB07647825", major = "10046";
        // 初始化定位引擎
        locationManager = new TYLocationManager(this, currentBuilding);

        // 设置Beacon定位参数，并传递给定位引擎
        if (uuid != null && major != null) {
            locationManager.setBeaconRegion(new BeaconRegion("TuYa", uuid, Integer.parseInt(major), null));
        } else {
            locationManager.setBeaconRegion(new BeaconRegion("TuYa", null, null, null));
        }

        // 添加回调监听
        locationManager.addLocationEngineListener(this);

        //routeManager
        routeManager = new TYOfflineRouteManager(currentBuilding, allMapInfos);
        routeManager.addRouteManagerListener(this);//添加回调
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        locationManager.startUpdateLocation();//开始定位
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        locationManager.stopUpdateLocation();//开始定位
    }

    @Override
    public void onClickAtPoint(TYMapView tyMapView, Point point) {
        //地图上点击
        tempPoint = point;
    }

    @Override
    public void onPoiSelected(TYMapView tyMapView, List<TYPoi> list) {
        //选择poi

        popview.setStyle(R.xml.callout_style);
        if (popview != null && popview.isShowing()) {
            popview.hide();
        }
        if (list != null && list.size() > 0) {
            TYPoi poi = list.get(0);

            Point position;
            if (poi.getGeometry().getClass() == Polygon.class) {
                position = GeometryEngine.getLabelPointForPolygon((Polygon) poi.getGeometry(),TYMapEnvironment.defaultSpatialReference());
            } else {
                position = (Point) poi.getGeometry();
            }

            String title = poi.getName();
            String detail = poi.getPoiID();

            //设置弹出view的大小
            popview.setMaxWidth(dip2px(NavActivity.this, 230));
            popview.setMaxHeight(dip2px(NavActivity.this, 170));

            popview.setContent(poiView(title, detail));
            popview.show(position);//显示弹窗
        }
    }

    @Override
    public void onFinishLoadingFloor(TYMapView tyMapView, TYMapInfo tyMapInfo) {
        //当前楼层地图载入完毕
    }

    @Override
    public void mapViewDidZoomed(TYMapView tyMapView) {
        //地图将要缩放
    }

    /**
     * 生成弹出视图view
     * @param title
     * @return
     */
    public View poiView(String title, String content) {
        View view = getLayoutInflater().inflate(R.layout.poi_pop_layout, null);
        TextView titleText = (TextView) view.findViewById(R.id.poi_title_txt);
        TextView contentText = (TextView) view.findViewById(R.id.poi_content_txt);
        Button startBtn = (Button) view.findViewById(R.id.poi_start_btn);
        Button endBtn = (Button) view.findViewById(R.id.poi_end_btn);

        titleText.setText(title);
        contentText.setText(content);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPoint = tempPoint;
                mapView.setStartSymbol(null);
                pt1 = new TYLocalPoint(startPoint.getX(), startPoint.getY(), currentMapInfo.getFloorNumber());
                TYPictureMarkerSymbol start = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.qidian));
                start.setWidth(dip2px(NavActivity.this, 12)); //设置起点图标的宽、高
                start.setHeight(dip2px(NavActivity.this, 16));

                mapView.setRouteStart(pt1);  //设置起点 位置
                mapView.setStartSymbol(start);//设置起点位置的图标

                mapView.showRouteStartSymbolOnCurrentFloor(pt1);//显示起点图标在当前楼层地图上

                popview.hide();

                if (endPoint != null && pt2 != null) {
                    routeManager.requestRoute(pt1, pt2);//请求线路
                }
            }
        });

        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endPoint = tempPoint;
                mapView.setEndSymbol(null);
                if (startPoint == null) {
                    Toast.makeText(NavActivity.this, "请选择起点", Toast.LENGTH_SHORT).show();
                    return;
                }

                pt2 = new TYLocalPoint(endPoint.getX(), endPoint.getY(), currentMapInfo.getFloorNumber());

                if (pt1.getFloor() != pt2.getFloor()) {
                    TYPictureMarkerSymbol pic_floor = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.green_pushpin));
                    pic_floor.setWidth(dip2px(NavActivity.this, 12));
                    pic_floor.setHeight(dip2px(NavActivity.this, 12));
                    mapView.setSwitchSymbol(pic_floor);
                }

                //
                routeManager.requestRoute(pt1, pt2);


                TYPictureMarkerSymbol pic = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.zhongdian));
                pic.setWidth(dip2px(NavActivity.this, 12));
                pic.setHeight(dip2px(NavActivity.this, 16));
                mapView.setRouteEnd(pt2);
                mapView.setEndSymbol(pic);

                mapView.showRouteEndSymbolOnCurrentFloor(pt2);//显示终点图标在当前楼层地图上

                popview.hide();

            }
        });
        return view;
    }
    public int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    @Override
    public void didRangedBeacons(TYLocationManager tyLocationManager, List<TYBeacon> list) {

    }

    @Override
    public void didRangedLocationBeacons(TYLocationManager tyLocationManager, List<TYPublicBeacon> list) {

    }

    @Override
    public void didUpdateLocation(TYLocationManager tyLocationManager, TYLocalPoint tyLocalPoint) {
        if (mapView.getCurrentMapInfo().getFloorNumber() != tyLocalPoint.getFloor()) {
            TYMapInfo targetMapInfo = TYMapInfo.searchMapInfoFromArray(
                    allMapInfos, tyLocalPoint.getFloor());
            mapView.setFloor(targetMapInfo);
        }
        // 在地图当前楼层上显示定位结果
        mapView.showLocation(tyLocalPoint);
    }

    @Override
    public void didFailUpdateLocation(TYLocationManager tyLocationManager) {
        //更新位置错误
    }

    @Override
    public void didUpdateDeviceHeading(TYLocationManager tyLocationManager, double v) {
        //方向改变
        mapView.processDeviceRotation(v);
    }

    @Override
    public void didSolveRouteWithResult(TYOfflineRouteManager tyOfflineRouteManager, TYRouteResult tyRouteResult) {
        mapView.setRouteResult(tyRouteResult);//设置线路
        mapView.showRouteResultOnCurrentFloor();//地图上显示线路
    }

    @Override
    public void didFailSolveRouteWithError(TYOfflineRouteManager tyOfflineRouteManager, Exception e) {
        //线路规划错误
    }
}