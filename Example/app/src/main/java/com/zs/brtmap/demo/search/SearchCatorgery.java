package com.zs.brtmap.demo.search;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.ty.mapsdk.PoiEntity;
import com.ty.mapsdk.TYMapInfo;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYPictureMarkerSymbol;
import com.ty.mapsdk.TYSearchAdapter;
import com.zs.brtmap.demo.BaseMapViewActivity;
import com.zs.brtmap.demo.R;

import java.util.List;

public class SearchCatorgery extends BaseMapViewActivity {

    static {
        System.loadLibrary("TYMapSDK");
    }

    ListView categoryList;
    GraphicsLayer poiLayer;

    @Override
    public void initContentViewID() {
        contentViewID = R.layout.activity_search_catergory;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        categoryList = (ListView) findViewById(R.id.category_list);
    }

    @Override
    public void mapViewDidLoad(TYMapView mapView, Error error) {
        super.mapViewDidLoad(mapView, error);
        if (error == null) {
            poiLayer = new GraphicsLayer();
            mapView.addLayer(poiLayer);

            //点击高亮
            //mapView.setHighlightPoiOnSelection(true);
        }
    }

    @Override
    public void onFinishLoadingFloor(final TYMapView mapView, final TYMapInfo mapInfo) {
        super.onFinishLoadingFloor(mapView, mapInfo);
        final List<Integer> catergoryIDs = mapView.getAllFacilityCategoryIDOnCurrentFloor();
        final ArrayAdapter<Integer>  adapter = new ArrayAdapter<Integer>(SearchCatorgery.this,android.R.layout.simple_list_item_1,catergoryIDs);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                categoryList.setAdapter(adapter);
            }
        });
        categoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mapView.clearSelection();
                mapView.showFacilityOnCurrentWithCategory(catergoryIDs.get(position));
                showPoiByCID(catergoryIDs.get(position)+"");
            }
        });
    }

    private void showPoiByCID(String cid) {
        TYSearchAdapter searchAdapter = new TYSearchAdapter(mapView.building.getBuildingID(),1.0);
        List<PoiEntity> poiEntities = searchAdapter.queryPoiByCategoryID(cid+"",mapView.currentMapInfo.getFloorNumber());
        poiLayer.removeAll();
        Graphic[] graphics = new Graphic[poiEntities.size()];
        int i=0;
        for (PoiEntity entity:poiEntities) {
            if (entity.getFloorNumber() != mapView.currentMapInfo.getFloorNumber())
                return;
            Point point = new Point(entity.getLabelX(),entity.getLabelY());
            Graphic graphic = new Graphic(point,getGreenpinSymbol());
            graphics[i++] = graphic;
        }
        poiLayer.addGraphics(graphics);
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

}
