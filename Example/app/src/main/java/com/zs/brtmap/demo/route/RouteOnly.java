package com.zs.brtmap.demo.route;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;

import com.ty.mapdata.TYBuilding;
import com.ty.mapdata.TYLocalPoint;
import com.ty.mapsdk.TYDirectionalHint;
import com.ty.mapsdk.TYDownloader;
import com.ty.mapsdk.TYMapInfo;
import com.ty.mapsdk.TYOfflineRouteManager;
import com.ty.mapsdk.TYRoutePart;
import com.ty.mapsdk.TYRouteResult;
import com.zs.brtmap.demo.R;
import com.zs.brtmap.demo.utils.Constants;

import java.util.List;

public class RouteOnly extends Activity implements TYOfflineRouteManager.TYOfflineRouteManagerListener {

    static {
        System.loadLibrary("TYMapSDK");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_only);

        //仅使用数据进行路径规划，定位等，不使用地图模式
        TYDownloader.loadMap(this, Constants.BUILDING_ID, Constants.APP_KEY, new TYDownloader.mapdataLoadListener() {
            @Override
            public void onMapCompetion(TYBuilding tyBuilding, List<TYMapInfo> list) {
                TYOfflineRouteManager rm = new TYOfflineRouteManager(tyBuilding,list);
                rm.addRouteManagerListener(RouteOnly.this);
                rm.requestRoute(new TYLocalPoint(13532005.693566,3639175.863626,1),new TYLocalPoint(13532014.059454,3639181.011865,1));
            }

            @Override
            public void onError(Error error) {
                Log.e("msg",error.toString());
            }
        });
    }

    @Override
    public void didSolveRouteWithResult(TYOfflineRouteManager tyOfflineRouteManager, TYRouteResult tyRouteResult) {
        TYRoutePart part = tyRouteResult.getAllRouteParts().get(0);
        do {
            Log.e("msg",part.getMapInfo().getFloorName());
            List<TYDirectionalHint> hints = tyRouteResult.getRouteDirectionalHint(part);
            TYDirectionalHint hint = hints.get(0);
            do {
                Log.e("msg",hint.getDirectionString());
                hint = hint.getNextHint();
            }while (hint != null);
            part = part.getNextPart();
        }while (part!=null);
    }

    @Override
    public void didFailSolveRouteWithError(TYOfflineRouteManager tyOfflineRouteManager, Exception e) {
        Log.e("msg",e.toString());
    }
}
