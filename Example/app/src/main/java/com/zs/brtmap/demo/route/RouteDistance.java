package com.zs.brtmap.demo.route;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.Callout;
import com.esri.core.geometry.Point;
import com.ty.mapdata.TYLocalPoint;
import com.ty.mapsdk.TYMapInfo;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYOfflineRouteManager;
import com.ty.mapsdk.TYPictureMarkerSymbol;
import com.ty.mapsdk.TYPoi;
import com.ty.mapsdk.TYRoutePart;
import com.ty.mapsdk.TYRouteResult;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.R;
import com.zs.brtmap.demo.utils.Utils;

public class RouteDistance extends BaseMapViewActivity implements TYOfflineRouteManager.TYOfflineRouteManagerListener {

    static {
        System.loadLibrary("TYMapSDK");
    }

    private Callout mapCallout;
    TYLocalPoint startPoint,endPoint;
    boolean isRouting;
    TextView textDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        textDistance = (TextView)findViewById(R.id.text_distance);
    }

    @Override
    public void initContentViewID() {
        contentViewID = R.layout.activity_route_distance;
    }

    @Override
    public void mapViewDidLoad(final TYMapView mapView, Error error) {
        super.mapViewDidLoad(mapView, error);

        initSymbols();

        mapCallout = mapView.getCallout();
        mapCallout.setStyle(R.xml.callout_style);
        //default is pixels.convert to dp.
        mapCallout.setMaxHeight(Utils.dip2px(this,300));
        mapCallout.setMaxWidth(Utils.dip2px(this,500));

        new Thread(new Runnable() {
            @Override
            public void run() {
                mapView.routeManager().addRouteManagerListener(RouteDistance.this);
            }
        }).start();
    }

    private void initSymbols() {
        TYPictureMarkerSymbol startSymbol = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.start));
        startSymbol.setWidth(34);
        startSymbol.setHeight(43);
        startSymbol.setOffsetX(0);
        startSymbol.setOffsetY(22);
        mapView.setStartSymbol(startSymbol);

        TYPictureMarkerSymbol endSymbol = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.end));
        endSymbol.setWidth(34);
        endSymbol.setHeight(43);
        endSymbol.setOffsetX(0);
        endSymbol.setOffsetY(22);
        mapView.setEndSymbol(endSymbol);

        TYPictureMarkerSymbol switchSymbol = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.nav_exit));
        switchSymbol.setWidth(37);
        switchSymbol.setHeight(37);
        mapView.setSwitchSymbol(switchSymbol);
    }

    @Override
    public void onClickAtPoint(TYMapView mapView, Point mappoint) {
        super.onClickAtPoint(mapView, mappoint);

        TYPoi poi = mapView.extractRoomPoiOnCurrentFloor(mappoint.getX(),mappoint.getY());
        String name = (poi==null||poi.getName()==null)?"未知点":poi.getName();
        mapCallout.animatedShow(mappoint,loadCalloutView(name,mappoint));
    }

    // 加载自定义弹出框内容
    private View loadCalloutView(final String title, final Point pt) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.layout_callout, null);
        TextView titleView = (TextView) view.findViewById(R.id.callout_title);
        titleView.setText(title);
        TextView detailView = (TextView) view.findViewById(R.id.callout_detail);
        detailView.setText("x:" + pt.getX() + "\ny:" + pt.getY());
        TextView cancelBtn = (TextView) view.findViewById(R.id.callout_cancel);
        cancelBtn.setText("起点");
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapCallout.hide();
                setStartPoint(pt);
            }
        });
        TextView doneBtn = (TextView) view.findViewById(R.id.callout_done);
        doneBtn.setText("终点");
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapCallout.hide();
                setEndPoint(pt);
            }
        });
        return view;
    }

    private void setStartPoint(Point currentPoint) {
        startPoint = new TYLocalPoint(currentPoint.getX(), currentPoint.getY(),
                mapView.getCurrentMapInfo().getFloorNumber());
        mapView.showRouteStartSymbolOnCurrentFloor(startPoint);
        requestRoute();
    }

    private void setEndPoint(Point currentPoint) {
        endPoint = new TYLocalPoint(currentPoint.getX(), currentPoint.getY(),
                mapView.getCurrentMapInfo().getFloorNumber());
        mapView.showRouteEndSymbolOnCurrentFloor(endPoint);
        requestRoute();
    }

    private void requestRoute() {
        if (startPoint == null || endPoint == null) {
            Utils.showToast(RouteDistance.this, "需要两个点请求路径！");
            return;
        }
        mapView.resetRouteLayer();
        mapView.routeManager().requestRoute(startPoint, endPoint);
    }

    @Override
    public void didSolveRouteWithResult(TYOfflineRouteManager tyOfflineRouteManager, TYRouteResult tyRouteResult) {
        mapView.setRouteResult(tyRouteResult);
        mapView.setRouteStart(startPoint);
        mapView.setRouteEnd(endPoint);
        mapView.showRouteResultOnCurrentFloor();
        isRouting = true;

        double distance = tyRouteResult.distanceToRouteEnd(startPoint);
        int numOfPart = tyRouteResult.getAllRouteParts().size();
        String partDistance = "各段长度：";
        for (TYRoutePart part : tyRouteResult.getAllRouteParts()) {
            partDistance += String.format("%.2f",part.getRoute().calculateLength2D())+"米\n";
        }
        //普通人每分钟走80米
        textDistance.setText("全程："+String.format("%.2f",distance)+"米\n共"+numOfPart+"段\n"+partDistance+"预计耗时："+String.format("%.2f",distance/80)+"分钟");
    }

    @Override
    public void didFailSolveRouteWithError(TYOfflineRouteManager tyOfflineRouteManager, Exception e) {
        Toast.makeText(this,"未找到路径",Toast.LENGTH_SHORT).show();
        isRouting = false;
    }

    @Override
    public void onFinishLoadingFloor(TYMapView mapView, TYMapInfo mapInfo) {
        super.onFinishLoadingFloor(mapView, mapInfo);
        if (isRouting) {
            mapView.showRouteResultOnCurrentFloor();
        }
    }
}
