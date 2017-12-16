package com.zs.brtmap.demo;

import java.util.List;
import java.util.Locale;

import com.esri.core.geometry.Point;
import com.ty.mapsdk.TYMapEnvironment;
import com.zs.brtmap.demo.adapter.FloorListAdapter;
import com.ty.mapsdk.TYMapInfo;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYMapView.TYMapViewListenser;
import com.ty.mapsdk.TYPoi;
import com.zs.brtmap.demo.utils.Constants;
import com.zs.brtmap.demo.utils.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public abstract class BaseMapViewActivity extends Activity
        implements TYMapViewListenser {

    public String TAG = this.getClass().getSimpleName();

    private static final int BLE_LOCATION_STATE = 100;
    public TYMapView mapView;

    //楼层控件
    private PopupWindow pw;
    private FloorListAdapter floorListAdatper;
    private int offset;

    public int contentViewID;

    Locale mCurLocale = null;

    Resources getResourcesByLocale( Resources res, String localeName ) {
        Configuration conf = new Configuration(res.getConfiguration());
        conf.locale = new Locale(localeName);
        return new Resources(res.getAssets(), res.getDisplayMetrics(), conf);
    }
    private void resetLocale(Resources res){
        Configuration conf = new Configuration(res.getConfiguration());
        conf.locale = mCurLocale;
        new Resources(res.getAssets(), res.getDisplayMetrics(), conf);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContentViewID();
        setContentView(contentViewID);

        mapView = (TYMapView) findViewById(R.id.map);

        //初始化地图环境
        TYMapEnvironment.initMapEnvironment();

        //隐藏地图，避免楼层加载完成前黑屏
        mapView.setVisibility(View.INVISIBLE);

        //添加地图回调
        mapView.addMapListener(this);

        //初始化建筑数据，授权appKey
        mapView.init(Constants.BUILDING_ID, Constants.APP_KEY);

        //请求定位相关权限
        checkPermission();
    }

    private void onPermissionGranted() {
        //开始定位等操作
    }

    // 仅用于本例子类设置界面元素
    public abstract void initContentViewID();

    @Override
    public void mapViewDidLoad(TYMapView mapView, Error error) {
        //地图数据需要网络加载，加载完成后回调
        if (error == null) {
            //设置显示楼层
            mapView.setFloor(mapView.allMapInfo().get(0));

            //显示楼层控件
            showFloorControl(mapView.allMapInfo());

            //显示放大缩小
            showZoomControl();
        } else {
            Utils.showToast(this, error.toString());
        }
    }

    @Override
    public void onFinishLoadingFloor(final TYMapView mapView, TYMapInfo mapInfo) {
        //显示地图
        if (mapView.getVisibility() != View.VISIBLE) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public void showZoomControl() {
        TextView btnZoomIn = (TextView) findViewById(R.id.btn_zoomin);
        TextView btnZoomOut = (TextView) findViewById(R.id.btn_zoomout);
        btnZoomIn.setVisibility(View.VISIBLE);
        btnZoomOut.setVisibility(View.VISIBLE);
        btnZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.zoomin();
            }
        });
        btnZoomOut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mapView.zoomout();
            }
        });
    }

    public void showFloorControl(final List<TYMapInfo> mapInfos) {
        TextView btnFloor = (TextView) findViewById(R.id.btn_floor);
        ImageView btnFloorArrow = (ImageView) findViewById(R.id.btn_floor_arrow);
        if (mapInfos.isEmpty()) {
            btnFloor.setVisibility(View.GONE);
            btnFloorArrow.setVisibility(View.GONE);
            return;
        }
        btnFloor.setVisibility(View.VISIBLE);
        btnFloorArrow.setVisibility(View.VISIBLE);
        btnFloor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPopwMenu(v, mapInfos);
            }
        });
    }

    private void createPopwMenu(View v, List<TYMapInfo> mapInfos) {
        final TextView btnFloor = (TextView) findViewById(R.id.btn_floor);
        final ImageView btnFloorArrow = (ImageView) findViewById(R.id.btn_floor_arrow);
        if (pw == null) {
            View view = getLayoutInflater().inflate(R.layout.popwindow_menu_layout, null);
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int height = view.getMeasuredHeight();
            int offset1 = Utils.dip2px(this, 10);
            offset = height - offset1;
            ListView lv = (ListView) view.findViewById(R.id.menu_list);

            floorListAdatper = new FloorListAdapter(this, R.layout.pop_list_item, mapInfos);
            lv.setAdapter(floorListAdatper);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TYMapInfo currentMapInfo = (TYMapInfo) parent.getItemAtPosition(position);
                    mapView.setFloor(currentMapInfo);
                    pw.dismiss();
                    floorListAdatper.setSelected(currentMapInfo);
                    btnFloor.setText(currentMapInfo.getFloorName());
                }
            });

            pw = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            pw.setOutsideTouchable(true);
            pw.setBackgroundDrawable(new ColorDrawable(0));
            pw.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    Utils.rotationArrow(btnFloorArrow, 0, 180);
                }
            });
        }

        if (floorListAdatper != null) {
            floorListAdatper.setSelected(mapView.getCurrentMapInfo());
            floorListAdatper.notifyDataSetChanged();
        }

        if (!pw.isShowing()) {
            Utils.rotationArrow(btnFloorArrow, 180, 0);
            pw.showAtLocation(v, Gravity.BOTTOM, 0, offset);
        } else {
            pw.dismiss();
        }
    }

    @Override
    public void onClickAtPoint(TYMapView mapView, Point mappoint) {
        //地图点击
        Log.e(TAG, mappoint.toString());
    }

    @Override
    public void onPoiSelected(TYMapView mapView, List<TYPoi> poiList) {
        //poi选中
    }

    @Override
    public void mapViewDidZoomed(TYMapView mapView) {
        //地图缩放
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.destroyDrawingCache();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.unpause();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.pause();
    }


    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//判断当前系统的SDK版本是否大于23
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                //第一次请求权限的时候返回false,第二次shouldShowRequestPermissionRationale返回true
                //如果用户选择了“不再提醒”永远返回false。
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_COARSE_LOCATION)){
                    Toast.makeText(this, "需要开启蓝牙定位权限，才能进行室内定位导航", Toast.LENGTH_LONG).show();
                    // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, BLE_LOCATION_STATE);
                }else{
                    Toast.makeText(getApplicationContext(), "无法请求权限,请手动前往设置开启蓝牙定位权限", Toast.LENGTH_SHORT).show();
                }
            } else {
                //已有权限
                onPermissionGranted();
            }
        }
    }
    //Android6.0申请权限的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // requestCode即所声明的权限获取码，在requestPermissions时传入
            case BLE_LOCATION_STATE:
                if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获取到权限，作相应处理（调用定位SDK应当确保相关权限均被授权，否则可能引起定位失败）
                    onPermissionGranted();
                } else {
                    // 没有获取到权限，做特殊处理
                    Toast.makeText(getApplicationContext(), "获取位置权限失败，请手动前往设置开启", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
