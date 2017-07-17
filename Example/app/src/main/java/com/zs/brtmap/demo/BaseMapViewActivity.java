package com.zs.brtmap.demo;

import java.util.List;

import com.esri.core.geometry.Point;
import com.ty.mapsdk.TYMapEnvironment;
import com.zs.brtmap.demo.adapter.FloorListAdapter;
import com.ty.mapsdk.TYMapInfo;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYMapView.TYMapViewListenser;
import com.ty.mapsdk.TYPoi;
import com.zs.brtmap.demo.utils.Constants;
import com.zs.brtmap.demo.utils.Utils;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public abstract class BaseMapViewActivity extends Activity
        implements TYMapViewListenser {

    public String TAG = this.getClass().getSimpleName();
    public TYMapView mapView;

    //楼层控件
    private PopupWindow pw;
    private FloorListAdapter floorListAdatper;
    private int offset;

    public int contentViewID;

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
}
