package com.zs.brtmap.demo.search;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.SearchView;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.renderer.SimpleRenderer;
import com.ty.mapsdk.PoiEntity;
import com.ty.mapsdk.TYMapInfo;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYPictureMarkerSymbol;
import com.ty.mapsdk.TYSearchAdapter;
import com.ty.mapsdk.TYTextSymbol;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.mark.CalloutActivity;
import com.zs.brtmap.demo.R;

import java.util.List;

public class SearchActivity extends BaseMapViewActivity {

    List<PoiEntity> searchList;
    GraphicsLayer poiLayer;
    SearchView searchView;
    static final String TAG = CalloutActivity.class.getSimpleName();

    static {
        System.loadLibrary("TYMapSDK");
        // System.loadLibrary("TYLocationEngine");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchView = (SearchView) findViewById(R.id.searchView);
        // 设置搜索文本监听
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                poiLayer.removeAll();
                if (newText.length() > 0) {
                    showPoiByName(newText);
                }
                return false;
            }
        });
    }

    @Override
    public void initContentViewID() {
        contentViewID = R.layout.activity_search;
    }

    @Override
    public void mapViewDidLoad(TYMapView mapView, Error error) {
        super.mapViewDidLoad(mapView, error);
        if (error == null) {
            poiLayer = new GraphicsLayer();
            poiLayer.setSelectionColor(Color.GREEN);//选中光环颜色（可选）
            mapView.addLayer(poiLayer);
        }
    }

    //绿色图标
    private TYPictureMarkerSymbol getGreenpinSymbol() {
        TYPictureMarkerSymbol symbol = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.green_pushpin));
        symbol.setWidth(20);
        symbol.setHeight(20);
        symbol.setOffsetX(5);
        symbol.setOffsetY(10);
        return symbol;
    }

    //红色图标
    private TYPictureMarkerSymbol getRedpinSymbol() {
        TYPictureMarkerSymbol symbol = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.red_pushpin));
        symbol.setWidth(20);
        symbol.setHeight(20);
        symbol.setOffsetX(5);
        symbol.setOffsetY(10);
        return symbol;
    }

    @Override
    public void onFinishLoadingFloor(TYMapView mapView, TYMapInfo mapInfo) {
        super.onFinishLoadingFloor(mapView, mapInfo);
        poiLayer.removeAll();
    }

    @Override
    public void onClickAtPoint(TYMapView mapView, Point mappoint) {
        super.onClickAtPoint(mapView, mappoint);
        Point screenPoint = mapView.toScreenPoint(mappoint);
        int[] graphicIDs = poiLayer.getGraphicIDs((float) screenPoint.getX(), (float) screenPoint.getY(), 5);
        if (graphicIDs.length > 0) {
            //添加选中效果；修改选中symbol
            poiLayer.setSelectedGraphics(graphicIDs,true);
            poiLayer.updateGraphic(graphicIDs[0], getRedpinSymbol());
        }

    }

    private void showPoiByName(String name) {
        TYSearchAdapter searchAdapter = new TYSearchAdapter(mapView.building.getBuildingID(),0.5);
        searchList = searchAdapter.queryPoi(name, mapView.currentMapInfo.getFloorNumber());
        for (PoiEntity entity : searchList) {
            Point point = new Point(entity.getLabelX(), entity.getLabelY());
            Graphic graphic = new Graphic(point, getGreenpinSymbol());
            poiLayer.addGraphic(graphic);

//            TYTextSymbol textSymbol = new TYTextSymbol(this,15, entity.getName(), Color.BLACK);
//            textSymbol.setOffsetX(-5);
//            textSymbol.setOffsetY(-5);
//            Graphic txtGraphic = new Graphic(point, textSymbol);
//            poiLayer.addGraphic(txtGraphic);
        }
    }
}
