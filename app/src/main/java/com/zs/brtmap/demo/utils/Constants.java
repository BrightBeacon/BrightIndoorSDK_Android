package com.zs.brtmap.demo.utils;

import java.io.File;

import android.os.Environment;

public class Constants {

	 //**********************************以下必须修改***********************************
	//地图初始化参数参看：http://open.brtbeacon.com
//	public static final String BUILDING_ID = "ZS020006";
//	public static final String APP_KEY = "efef3dbde9dd416bb24b213ed546584f";

	public static final String BUILDING_ID = "05720001";
	public static final String APP_KEY = "7befe0e6efbb42d49b8af258d328d7a5";

	 public static final String UUID = "FDA50693-A4E2-4FB1-AFCF-C6EB07647825";
	 //public static final int MAJOR = 10046;
	 //**********************************以上必须修改***********************************

	 //本地数据总目录
	public static final String CACHE_DIR = Environment.getExternalStorageDirectory().getPath() + File.separator
			+ "BrtMapDemo";
}
