package com.zs.brtmap.demo.http;

import java.io.File;

import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import com.zs.brtmap.demo.utils.Constants;

public class HttpHandler {

	public static Callback.Cancelable downloadMapData(Callback.ProgressCallback<File> callback) {
		String fileName = Constants.BUILDING_ID + ".zip";
		RequestParams params = new RequestParams(Constants.DOWNLOAD_MAP_DATA);
		params.addQueryStringParameter("appkey", Constants.APP_KEY);
		params.addQueryStringParameter("buildingID", Constants.BUILDING_ID);
		params.addQueryStringParameter("license", Constants.LICENSE);

		params.setAutoResume(true);
		params.setAutoRename(false);
		params.setMultipart(true);
		params.setSaveFilePath(Constants.CACHE_DIR + File.separator + fileName);
		return x.http().get(params, callback);
	}

	public static Callback.Cancelable downloadLocationData(Callback.ProgressCallback<File> callback) {
		String fileName = Constants.BUILDING_ID + "_point.zip";
		RequestParams params = new RequestParams(Constants.DOWNLOAD_LOCATION_DATA);
		params.addQueryStringParameter("appkey", Constants.APP_KEY);
		params.addQueryStringParameter("buildingID", Constants.BUILDING_ID);
		params.addQueryStringParameter("license", Constants.LICENSE);

		params.setAutoResume(true);
		params.setAutoRename(false);
		params.setMultipart(true);
		params.setSaveFilePath(Constants.CACHE_DIR + File.separator + fileName);
		return x.http().get(params, callback);
	}
	
    public static Callback.Cancelable updateBeacon(Callback.ProgressCallback<String> callback) {
        RequestParams params = new RequestParams(Constants.BEACON_UPDATE_URL);
        params.addHeader("Content-Type", "application/json");
        params.addHeader("Charset", "utf-8");

        params.addQueryStringParameter("appkey", Constants.APP_KEY);
        params.addQueryStringParameter("buildingID", Constants.BUILDING_ID);
        params.addQueryStringParameter("license", Constants.LICENSE);
        return x.http().post(params, callback);
    }
    

    public static Callback.Cancelable updateMap(Callback.ProgressCallback<String> callback) {
        RequestParams params = new RequestParams(Constants.MAP_UPDATE_URL);
        params.addHeader("Content-Type", "application/json");
        params.addHeader("Charset", "utf-8");

        params.addQueryStringParameter("appkey", Constants.APP_KEY);
        params.addQueryStringParameter("buildingID", Constants.BUILDING_ID);
        params.addQueryStringParameter("license", Constants.LICENSE);
        return x.http().post(params, callback);
    }
    
	public static Callback.Cancelable downloadZip(String zipUrl,Callback.ProgressCallback<File> callback) {
		String fileName = Constants.BUILDING_ID + "data.zip";
		RequestParams params = new RequestParams(zipUrl);
		params.setAutoResume(true);
		params.setAutoRename(false);
		params.setMultipart(true);
		params.setSaveFilePath(Constants.CACHE_DIR + File.separator + fileName);
		return x.http().get(params, callback);
	}

}
