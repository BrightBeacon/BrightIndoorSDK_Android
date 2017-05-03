package com.zs.brtmap.demo.utils;

import java.io.File;

import android.os.Environment;

public class Constants {

	 //**********************************以下必须修改***********************************
	//地图初始化参数参看：http://open.brtbeacon.com
//	public static final String BUILDING_ID = "05720001";
	public static final String BUILDING_ID = "08980002";
	 public static final String APP_KEY = "4899847d06be4c1cbd7ef05afc8a7d48";

	 public static final String UUID = "FDA50693-A4E2-4FB1-AFCF-C6EB07647825";
	 //public static final int MAJOR = 10046;
	 //**********************************以上必须修改***********************************

	 //本地数据总目录
	public static final String CACHE_DIR = Environment.getExternalStorageDirectory().getPath() + File.separator
			+ "BrtMapDemo";
}
