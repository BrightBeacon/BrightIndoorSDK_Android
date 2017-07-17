package com.zs.brtmap.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;

import com.zs.brtmap.demo.adapter.MenuListAdapter;
import com.zs.brtmap.demo.ctl.CtlNorth;
import com.zs.brtmap.demo.location.LocationActivity;
import com.zs.brtmap.demo.location.LocationDemo;
import com.zs.brtmap.demo.location.LocationSnap;
import com.zs.brtmap.demo.map.MapActivity;
import com.zs.brtmap.demo.map.MapCoordinate;
import com.zs.brtmap.demo.map.MapLayers;
import com.zs.brtmap.demo.map.MapOperation;
import com.zs.brtmap.demo.map.MapSetting;
import com.zs.brtmap.demo.map.TileActivity;
import com.zs.brtmap.demo.mark.CalloutActivity;
import com.zs.brtmap.demo.mark.MarkerArea;
import com.zs.brtmap.demo.mark.MarkerFence;
import com.zs.brtmap.demo.mark.MarkerImageTextPoint;
import com.zs.brtmap.demo.mark.MarkerLine;
import com.zs.brtmap.demo.oper.OperGesture;
import com.zs.brtmap.demo.oper.OperPoi;
import com.zs.brtmap.demo.route.RouteActivity;
import com.zs.brtmap.demo.route.RouteDistance;
import com.zs.brtmap.demo.route.RouteHint;
import com.zs.brtmap.demo.search.SearchActivity;
import com.zs.brtmap.demo.search.SearchCatorgery;
import com.zs.brtmap.demo.search.SearchDistance;

/**
 * 导航菜单页
 * 
 * @author thomasho
 *
 */
public class MenuActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_layout);
        ExpandableListView listView = (ExpandableListView)findViewById(R.id.list);
        final MenuListAdapter adapter = new MenuListAdapter(this);
        listView.setAdapter(adapter);
        listView.setGroupIndicator(null);
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
                adapter.setSelected(groupPosition,childPosition);
                adapter.notifyDataSetInvalidated();
                Intent intent = null;
                if (groupPosition == 0) {
                    switch (childPosition) {
                        case 0:
                            intent = new Intent(MenuActivity.this,MapActivity.class);
                            break;
                        case 1:
                            intent = new Intent(MenuActivity.this,MapSetting.class);
                            break;
                        case 2:
                            intent = new Intent(MenuActivity.this,MapOperation.class);
                            break;
                        case 3:
                            intent = new Intent(MenuActivity.this,MapLayers.class);
                            break;
                        case 4:
                            intent = new Intent(MenuActivity.this,MapCoordinate.class);
                            break;
                        case 5:
                            intent = new Intent(MenuActivity.this,TileActivity.class);
                            break;
                    }
                }else  if(groupPosition ==1){
                    switch (childPosition) {
                        case 0:
                            intent = new Intent(MenuActivity.this, OperPoi.class);
                            break;
                        case 1:
                            intent = new Intent(MenuActivity.this, OperGesture.class);
                            break;
                    }
                }else  if(groupPosition == 2){
                    switch (childPosition) {
                        case 0:
                            intent = new Intent(MenuActivity.this, CtlNorth.class);
                            break;
                    }
                }else  if (groupPosition == 3) {
                    switch (childPosition) {
                        case 0:
                            intent = new Intent(MenuActivity.this, MarkerImageTextPoint.class);
                            break;
                        case 1:
                            intent = new Intent(MenuActivity.this, MarkerLine.class);
                            break;
                        case 2:
                            intent = new Intent(MenuActivity.this, MarkerArea.class);
                            break;
                        case 3:
                            intent = new Intent(MenuActivity.this, CalloutActivity.class);
                            break;
                        case 4:
                            intent = new Intent(MenuActivity.this, MarkerFence.class);
                            break;
                    }
                }else if (groupPosition == 4) {
                    switch (childPosition) {
                        case 0:
                            intent = new Intent(MenuActivity.this, SearchActivity.class);
                            break;
                        case 1:
                            intent = new Intent(MenuActivity.this, SearchCatorgery.class);
                            break;
                        case 2:
                            intent = new Intent(MenuActivity.this, SearchDistance.class);
                            break;
                    }
                }else if (groupPosition == 5) {
                    switch (childPosition) {
                        case 0:
                            intent = new Intent(MenuActivity.this, RouteActivity.class);
                            break;
                        case 1:
                            intent = new Intent(MenuActivity.this, RouteDistance.class);
                            break;
                        case 2:
                            intent = new Intent(MenuActivity.this, RouteHint.class);
                            break;
                    }
                }else if (groupPosition == 6) {
                    switch (childPosition) {
                        case 0:
                            intent = new Intent(MenuActivity.this, LocationActivity.class);
                            break;
                        case 1:
                            intent = new Intent(MenuActivity.this, LocationSnap.class);
                            break;
                        case 2:
                            intent = new Intent(MenuActivity.this, LocationDemo.class);
                            break;
                    }
                }

                startActivity(intent);
                return false;
            }
        });
	}
}
