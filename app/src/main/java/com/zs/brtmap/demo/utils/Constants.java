package com.zs.brtmap.demo.utils;

import java.io.File;

import android.os.Environment;

public class Constants {

	 //**********************************以下必须修改***********************************
	//以下地图初始化、检查更新参数参看：http://developer.brtbeacon.com/map/myMapList
	public static final String CITY_ID = "ZS02";
	public static final String BUILDING_ID = "ZS020006";
	public static final String APP_KEY = "efef3dbde9dd416bb24b213ed546584f";
	public static final String LICENSE = "608d7b30DwYwMDM2MT8brtd_ZmY2YWYyNjQbrtd_c1de8012";
	
	//定位初始化参数查看：http://developer.brtbeacon.com/positive/getRegionList
	 public static final String UUID = "FDA50693-A4E2-4FB1-AFCF-C6EB07647825";
	 public static final int MAJOR = 10046;
	 //**********************************以上必须修改***********************************

	 //缓存本地zip版本号
	 public static final String VER_MAP_DATA = "mapVersion";
	 public static final String VER_LOCATION_DATA = "locationVersion";

	 //本地数据总目录
	public static final String CACHE_DIR = Environment.getExternalStorageDirectory().getPath() + File.separator
			+ "BrtMapDemo";
	
	//带版本检查更新接口（返回参数包含最新数据zip接口）
	public static final String MAP_UPDATE_URL = "http://service.map.brtbeacon.com/mobile/data/load/mapdata/new";
	public static final String BEACON_UPDATE_URL = "http://service.map.brtbeacon.com/mobile/data/load/beacon/new";
	
	//使用参数直接下载zip接口
	public static final String DOWNLOAD_MAP_DATA = "http://service.map.brtbeacon.com/mobile/data/download/mapdata/buildingId";
	public static final String DOWNLOAD_LOCATION_DATA = "http://service.map.brtbeacon.com/mobile/data/download/beacon/buildingId";
}
