package com.zs.brtmap.demo.route;

import java.util.List;

import com.esri.android.map.Callout;
import com.esri.android.map.Layer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.ty.mapdata.TYLocalPoint;
import com.ty.mapsdk.TYMapInfo;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYOfflineRouteManager;
import com.ty.mapsdk.TYPictureMarkerSymbol;
import com.ty.mapsdk.TYPoi;
import com.ty.mapsdk.TYRoutePart;
import com.ty.mapsdk.TYRouteResult;
import com.ty.mapsdk.TYOfflineRouteManager.TYOfflineRouteManagerListener;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.R;
import com.zs.brtmap.demo.utils.Utils;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class RouteActivity extends BaseMapViewActivity implements TYOfflineRouteManagerListener {
	static {
		System.loadLibrary("TYMapSDK");
		// System.loadLibrary("TYLocationEngine");
	}

	private TYOfflineRouteManager routeManager;
	private Boolean isRouteManagerReady;
	TYLocalPoint startPoint;
	TYLocalPoint endPoint;
	boolean isRouting;
	TYRouteResult routeResult;

	private Callout mapCallout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mapCallout = mapView.getCallout();
	}

	@Override
	public void initContentViewID() {
		contentViewID = R.layout.activity_map_route;
	}

	@Override
	public void mapViewDidLoad(final TYMapView mapView, Error error) {
		super.mapViewDidLoad(mapView,error);
		if (error != null) return;

		//设置起点、终点、楼层切换点图标
		initSymbols();

		// 初始化路径管理器
		new Thread(new Runnable() {
			@Override
			public void run() {
				mapView.routeManager().addRouteManagerListener(RouteActivity.this);
				isRouteManagerReady = true;
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

	public TYLocalPoint p2lp(Point pt) {
		return new TYLocalPoint(pt.getX(),pt.getY(),mapView.getCurrentMapInfo().getFloorNumber());
	}

	@Override
	public void onClickAtPoint(TYMapView mapView, Point mappoint) {

		Layer layer = mapView.getLayer(0);
		if (!GeometryEngine.contains(layer.getExtent(), mappoint, mapView.getSpatialReference())) {
			Utils.showToast(RouteActivity.this, "请选择地图范围内的点");
			return;
		}
		TYPoi poi = mapView.extractRoomPoiOnCurrentFloor(mappoint.getX(),mappoint.getY());
		String title = (poi.getName()!=null)?poi.getName():"未知道路";
		mapCallout.setStyle(R.xml.callout_style);
		mapCallout.setMaxWidth(Utils.dip2px(this,300));
		mapCallout.setMaxHeight(Utils.dip2px(this,300));
		mapCallout.setContent(loadCalloutView(title, mappoint));
		mapCallout.show(mappoint);
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
		if (currentPoint == null) {
			return;
		}
		startPoint = new TYLocalPoint(currentPoint.getX(), currentPoint.getY(),
				mapView.getCurrentMapInfo().getFloorNumber());
		mapView.showRouteStartSymbolOnCurrentFloor(startPoint);
		requestRoute();
	}

	private void setEndPoint(Point currentPoint) {
		if (currentPoint == null) {
			return;
		}
		endPoint = new TYLocalPoint(currentPoint.getX(), currentPoint.getY(),
				mapView.getCurrentMapInfo().getFloorNumber());
		mapView.showRouteEndSymbolOnCurrentFloor(endPoint);
		requestRoute();
	}

	private void requestRoute() {
		if (!isRouteManagerReady) {
            Utils.showToast(RouteActivity.this, "路径管理器未初始化完成！");
			return;
		}

		if (startPoint == null || endPoint == null) {
			Utils.showToast(RouteActivity.this, "需要两个点请求路径！");
			return;
		}

		mapView.resetRouteLayer();

		mapView.routeManager().requestRoute(startPoint, endPoint);
	}

	// 路径规划回调
	@Override
	public void didFailSolveRouteWithError(TYOfflineRouteManager arg0, Exception arg1) {
		Utils.showToast(RouteActivity.this,"路径规划失败");
		mapView.resetRouteLayer();
		isRouting = false;
		startPoint = null;
		endPoint = null;
	}

	@Override
	public void didSolveRouteWithResult(TYOfflineRouteManager arg0, TYRouteResult rs) {
		isRouting = true;
		routeResult = rs;
		mapView.setRouteResult(rs);
		mapView.setRouteStart(startPoint);
		mapView.setRouteEnd(endPoint);

		mapView.showRouteResultOnCurrentFloor();

		List<TYRoutePart> routePartArray = rs.getRoutePartsOnFloor(mapView.getCurrentMapInfo().getFloorNumber());
		if (routePartArray != null && routePartArray.size() > 0) {
			TYRoutePart currentRoutePart = routePartArray.get(0);
			Envelope env = new Envelope();
			currentRoutePart.getRoute().queryEnvelope(env);
			mapView.setExtent(env, 200, true);
		}
	}

	@Override
	public void onFinishLoadingFloor(TYMapView mapView, TYMapInfo mapInfo) {
		super.onFinishLoadingFloor(mapView, mapInfo);
		if (isRouting) {
			mapView.showRouteResultOnCurrentFloor();
		}
	}
}
