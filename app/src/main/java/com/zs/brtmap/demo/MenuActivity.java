package com.zs.brtmap.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import org.xutils.x;
/**
 * 导航菜单页
 * 
 * @author zhiqiang
 *
 */
public class MenuActivity extends Activity implements OnClickListener {

	private TextView baseMap;
	private TextView showCallout;
	private TextView showMapLayer;
	private TextView showRoute;
	private TextView showLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_layout);
// 初始化Xutils
		x.Ext.init(getApplication());
		x.Ext.setDebug(true);
		initView();
		initListener();
	}

	private void initListener() {
		baseMap.setOnClickListener(this);
		showCallout.setOnClickListener(this);
		showMapLayer.setOnClickListener(this);
		showRoute.setOnClickListener(this);
		showLocation.setOnClickListener(this);
	}

	private void initView() {
		baseMap = (TextView) findViewById(R.id.show_base_map);
		showCallout = (TextView) findViewById(R.id.show_map_callout);
		showMapLayer = (TextView) findViewById(R.id.show_map_layer);
		showRoute = (TextView) findViewById(R.id.show_map_route);
		showLocation = (TextView) findViewById(R.id.show_location);
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.show_base_map:
			intent = new Intent(this, MapActivity.class);
			break;
		case R.id.show_map_callout:
			intent = new Intent(this, CalloutActivity.class);
			break;
		case R.id.show_map_layer:
			intent = new Intent(this, LayerActivity.class);
			break;
		case R.id.show_map_route:
			intent = new Intent(this, RouteActivity.class);
			break;
		case R.id.show_location:
			intent = new Intent(this, LocationActivity.class);
			break;
		}
		if (intent != null) {
			startActivity(intent);
		}
	}

}
