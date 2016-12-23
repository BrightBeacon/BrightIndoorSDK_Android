package com.zs.brtmap.demo;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Line;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYPictureMarkerSymbol;
import com.zs.brtmap.demo.utils.Utils;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

public class LayerActivity extends BaseMapViewActivity {
	static {
		System.loadLibrary("TYMapSDK");
		// System.loadLibrary("TYLocationEngine");
	}
	private GraphicsLayer drawLayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public void initContentViewID() {
		// TODO Auto-generated method stub
		contentViewID = R.layout.activity_map_layer;
	}

	protected void testLayer(Point mappoint) {

		TYPictureMarkerSymbol pms = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.green_pushpin));
		pms.setWidth(40.0f);
		pms.setHeight(40.0f);
		pms.setOffsetX(-20.0f);
		pms.setOffsetY(20.0f);
		// 方向固定
		GraphicsLayer graphicsLayer = new GraphicsLayer(GraphicsLayer.RenderingMode.STATIC);
		mapView.addLayer(graphicsLayer);
		graphicsLayer.addGraphic(new Graphic(mappoint, pms));

		TextSymbol txtSymbol = new TextSymbol("DroidSansFallback.ttf", "旋转地图试试", Color.RED);
		txtSymbol.setSize(15);
		// 方向自适应
		GraphicsLayer dynamicLayer = new GraphicsLayer(GraphicsLayer.RenderingMode.DYNAMIC);
		mapView.addLayer(dynamicLayer);
		dynamicLayer.addGraphic(new Graphic(mappoint, txtSymbol));
	}

	private void testGeometry(Point startPoint,Point nextPoint) {

		// 线标记
		SimpleLineSymbol lineSymbol = new SimpleLineSymbol(Color.BLUE, 10, SimpleLineSymbol.STYLE.SOLID);
		// 面填充
		SimpleFillSymbol fillSymbol = new SimpleFillSymbol(Color.RED);
		fillSymbol.setOutline(lineSymbol);
		fillSymbol.setAlpha(90);
		
		
		GraphicsLayer layer = new GraphicsLayer();
		mapView.addLayer(layer);
		
		//线段
		Polyline polyline = new Polyline();
		polyline.startPath(startPoint);
		polyline.lineTo(nextPoint);
		layer.addGraphic(new Graphic(polyline, lineSymbol));

		//区域
		Polygon polygon = new Polygon();
		polygon.startPath(startPoint);
		polygon.lineTo(nextPoint);
		polygon.lineTo((startPoint.getX()+nextPoint.getX())/2,(startPoint.getY()+nextPoint.getY())/2);
		layer.addGraphic(new Graphic(polygon, fillSymbol));
		
		//圆
		Polygon circle= new Polygon();
		double radius = Math.sqrt(Math.pow(startPoint.getX()
				- nextPoint.getX(), 2)
				+ Math.pow(startPoint.getY() - nextPoint.getY(), 2));
		getCircle(startPoint, radius, circle);
		layer.addGraphic(new Graphic(circle, fillSymbol));
		
	}

	private Point ptPrevious = null;// 上一个点
	private Polygon tempPolygon = null;// 记录绘制过程中的多边形

	private void testDrawLayer(Point mappoint) {
		if (drawLayer == null) {
			drawLayer = new GraphicsLayer();
			mapView.addLayer(drawLayer);
		}
		// 点标记
		SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(Color.RED, 15, SimpleMarkerSymbol.STYLE.CIRCLE);
		// 线标记
		SimpleLineSymbol lineSymbol = new SimpleLineSymbol(Color.GREEN, 10, SimpleLineSymbol.STYLE.SOLID);
		// 面填充
		SimpleFillSymbol fillSymbol = new SimpleFillSymbol(Color.YELLOW);
		fillSymbol.setOutline(lineSymbol);
		
		if (drawLayer.getNumberOfGraphics() == 0) {
			// 绘制第一个点
			Graphic graphic = new Graphic(mappoint, markerSymbol);
			drawLayer.addGraphic(graphic);
		} else {
			// 生成当前线段（由当前点和上一个点构成）
			Line line = new Line();
			line.setStart(ptPrevious);
			line.setEnd(mappoint);
			//连续画线、或面
			if (1 == 1) {
				// 绘制当前线段
				Polyline polyline = new Polyline();
				polyline.addSegment(line, true);

				Graphic g = new Graphic(polyline, lineSymbol);
				drawLayer.addGraphic(g);

				// 计算当前线段的长度
				String length = Double.toString(Math.round(line.calculateLength2D())) + " 米";

				Utils.showToast(LayerActivity.this, "当前长度:" + length);
			} else {
				// 绘制临时多边形
				if (tempPolygon == null)
					tempPolygon = new Polygon();
				tempPolygon.addSegment(line, false);

				drawLayer.removeAll();
				Graphic g = new Graphic(tempPolygon, fillSymbol);
				drawLayer.addGraphic(g);

				// 计算当前面积
				String sArea = getAreaString(tempPolygon.calculateArea2D());

				Utils.showToast(LayerActivity.this, "总面积:" + sArea);
			}
		}
		ptPrevious = mappoint;
	}
	
	@Override
	public void onClickAtPoint(TYMapView mapView, Point mappoint) {
		Log.i(TAG, "Clicked Point: " + mappoint.getX() + ", " + mappoint.getY());
		//演示两种不同layer+显示图文
		if(ptPrevious==null)testLayer(mappoint);
		
		//演示通过两点画线、面、圆
		//if(ptPrevious!=null)testGeometry(ptPrevious, mappoint);
		
		//演示连续绘制线段、或面
		testDrawLayer(mappoint);
	}

	private String getAreaString(double dValue) {
		long area = Math.abs(Math.round(dValue));
		String sArea = "";
		// 顺时针绘制多边形，面积为正，逆时针绘制，则面积为负
		if (area >= 1000000) {
			double dArea = area / 1000000.0;
			sArea = Double.toString(dArea) + " 平方公里";
		} else
			sArea = Double.toString(area) + " 平方米";

		return sArea;
	}

//	画圆形
	private void getCircle(Point center, double radius, Polygon circle) {
		circle.setEmpty();
		Point[] points = getPoints(center, radius);
		circle.startPath(points[0]);
		for (int i = 1; i < points.length; i++)
			circle.lineTo(points[i]);
	}

	private Point[] getPoints(Point center, double radius) {
		Point[] points = new Point[50];
		double sin;
		double cos;
		double x;
		double y;
		for (double i = 0; i < 50; i++) {
			sin = Math.sin(Math.PI * 2 * i / 50);
			cos = Math.cos(Math.PI * 2 * i / 50);
			x = center.getX() + radius * sin;
			y = center.getY() + radius * cos;
			points[(int) i] = new Point(x, y);
		}
		return points;
	}
}
