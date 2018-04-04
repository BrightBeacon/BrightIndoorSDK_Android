package com.zs.brtmap.demo.route;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.ty.mapdata.TYLocalPoint;
import com.ty.mapsdk.TYDirectionalHint;
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

import java.util.List;

public class RouteHint extends BaseMapViewActivity implements TYOfflineRouteManager.TYOfflineRouteManagerListener {

    static {
        System.loadLibrary("TYMapSDK");
    }

    TextView textHint;
    TextView btnHint;

    GraphicsLayer hintLayer;
    int graphicID;
    int pointIndex;

    boolean isMoniLocation;

    TYRouteResult routeResult;
    TYLocalPoint startPoint, endPoint;
    boolean isRouting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        textHint = (TextView) findViewById(R.id.text_hint);
        btnHint = (TextView) findViewById(R.id.btn_hint);

        btnHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRouting) {
                    view.setEnabled(false);
                    mapView.setFloor(startPoint.getFloor()+"");
                    mapView.setScale(mapView.getXScaleFactor(3),true);
                    mapView.centerAt(new Point(startPoint.getX(),startPoint.getY()),false);
                    showHints(startPoint);
                } else {
                    Utils.showToast(RouteHint.this, "请选择起点、终点");
                }
            }
        });
    }

    @Override
    public void mapViewDidLoad(TYMapView mapView, Error error) {
        super.mapViewDidLoad(mapView, error);
        if (error == null) {
            hintLayer = new GraphicsLayer();
            mapView.addLayer(hintLayer);
        }
    }

    @Override
    public void initContentViewID() {
        contentViewID = R.layout.activity_route_hint;
    }

    @Override
    public void onClickAtPoint(TYMapView mapView, Point mappoint) {
        super.onClickAtPoint(mapView, mappoint);

        //模拟过程中，不响应
        if (btnHint.isEnabled() == false) return;

        TYPoi poi = mapView.extractRoomPoiOnCurrentFloor(mappoint.getX(),mappoint.getY());
        if (poi == null) {
            Utils.showToast(this, "请选择地图范围内的区域");
            return;
        }
        Point centerPt = null;
        Geometry geom = poi.getGeometry();
        if (geom instanceof Polygon) {
            //获取标点
            centerPt = GeometryEngine.getLabelPointForPolygon((Polygon)geom,mapView.getSpatialReference());
        }else {
            centerPt = (Point)geom;
        }
        startPoint = endPoint;
        endPoint = new TYLocalPoint(centerPt.getX(), centerPt.getY(), mapView.getCurrentMapInfo().getFloorNumber());
        mapView.showRouteStartSymbolOnCurrentFloor(startPoint);
        mapView.showRouteEndSymbolOnCurrentFloor(endPoint);
        if (startPoint != null) {
            mapView.clearRouteLayer();
            mapView.routeManager().addRouteManagerListener(RouteHint.this);
            mapView.routeManager().requestRoute(startPoint, endPoint);
        }
    }

    @Override
    public void didSolveRouteWithResult(TYOfflineRouteManager tyOfflineRouteManager, TYRouteResult tyRouteResult) {
        routeResult = tyRouteResult;
        mapView.setRouteResult(tyRouteResult);
        mapView.setRouteStart(startPoint);
        mapView.setRouteEnd(endPoint);
        mapView.showRouteResultOnCurrentFloor();
        isRouting = true;
    }

    @Override
    public void didFailSolveRouteWithError(TYOfflineRouteManager tyOfflineRouteManager, Exception e) {
        Utils.showToast(this, "未找到路径");
        isRouting = false;
    }

    @Override
    public void onFinishLoadingFloor(TYMapView mapView, TYMapInfo mapInfo) {
        super.onFinishLoadingFloor(mapView, mapInfo);
        if (isRouting)
            mapView.showRouteResultOnCurrentFloor();
    }


    private void showHints(TYLocalPoint lp) {

        //新建、更新指示图标位置
        if (graphicID != 0) {
            hintLayer.updateGraphic(graphicID, new Point(lp.getX(), lp.getY()));
        } else {
            TYPictureMarkerSymbol pms = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.location_arrow));
            pms.setWidth(50);
            pms.setHeight(50);
            graphicID = hintLayer.addGraphic(new Graphic(new Point(lp.getX(), lp.getY()), pms));
        }

        if (routeResult.distanceToRouteEnd(lp) < 0.5) {
            //已到达目的地
            return;
        }

        //以下代码仅用于模拟整条线路点位移动，实际场景可直接使用定位回调
        //取出本段路线上各个点
        TYRoutePart part = routeResult.getNearestRoutePart(lp);
        if (part == null) {
            return;
        }
        Polyline line = part.getRoute();
        Point pt = line.getPoint(pointIndex);
        pointIndex++;
        int floor = part.getMapInfo().getFloorNumber();
        //是否为本段结束点
        if (pt.equals(part.getLastPoint())) {
            pointIndex = 0;
            //是否为终段
            if (part.isLastPart()) {
                btnHint.setEnabled(true);
            } else {
                //取下一段路线起点
                TYRoutePart nextPart = part.getNextPart();
                pt = nextPart.getFirstPoint();
                floor = nextPart.getMapInfo().getFloorNumber();
                if (floor != mapView.currentMapInfo.getFloorNumber()) {
                    mapView.setFloor(nextPart.getMapInfo());
                }
            }
        }
        TYLocalPoint localPoint = new TYLocalPoint(pt.getX(), pt.getY(), floor);
        animateUpdateGraphic(0, lp, localPoint);


        List<TYDirectionalHint> hints = routeResult.getRouteDirectionalHint(part);
        TYDirectionalHint hint = routeResult.getDirectionalHintForLocationFromHints(localPoint, hints);
        if (hint != null) {
            mapView.setRotationAngle(hint.getCurrentAngle(), true);
            mapView.showRouteHint(hint, false);
        }
    }

    private void showCurrentHint(TYLocalPoint lp) {
        TYRoutePart part = routeResult.getNearestRoutePart(lp);
        if (part != null) {
            List<TYDirectionalHint> hints = routeResult.getRouteDirectionalHint(part);
            TYDirectionalHint hint = routeResult.getDirectionalHintForLocationFromHints(lp, hints);
            if (hint != null) {
                mapView.showRouteHint(hint,true);
                textHint.setText("方向：" + hint.getDirectionString() + hint.getRelativeDirection()
                        + "\n本段长度：" + String.format("%.2f", hint.getLength())
                        + "\n本段角度：" + String.format("%.2f", hint.getCurrentAngle())
                        + "\n剩余/全长：" + String.format("%.2f", routeResult.distanceToRouteEnd(lp))
                        + "/" + String.format("%.2f", routeResult.length));
            }
        }
    }


    private void animateUpdateGraphic(final double offset, final TYLocalPoint lp1, final TYLocalPoint lp2) {
        mapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                double distance = lp1.distanceWithPoint(lp2);
                if (distance > 0 && offset<distance) {
                    Point tmp = getPointWithLengthAndOffset(lp1, lp2, offset / distance);
                    mapView.centerAt(tmp,false);
                    showCurrentHint(new TYLocalPoint(tmp.getX(), tmp.getY(), lp1.getFloor()));
                    hintLayer.updateGraphic(graphicID, tmp);
                    animateUpdateGraphic(offset + 0.1, lp1, lp2);
                }else {
                    showCurrentHint(lp2);
                    showHints(lp2);
                }

            }
        }, 10);
    }

    private Point getPointWithLengthAndOffset(TYLocalPoint start, TYLocalPoint end,double per) {
        double scale = per;

        double x = start.getX() * (1 - scale) + end.getX() * scale;
        double y = start.getY() * (1 - scale) + end.getY() * scale;

        return new Point(x, y);
    }
}
