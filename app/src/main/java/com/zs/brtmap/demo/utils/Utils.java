package com.zs.brtmap.demo.utils;

import java.util.Set;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

public class Utils {
	private static SharedPreferences sp;
	private static final String SHARD_FILE_NAME = "TYData";


	public static void rotationArrow(ImageView view, float startDegress, float endDegress) {
		ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(view, "rotation", startDegress, endDegress);
		rotationAnim.setDuration(500);
		rotationAnim.setInterpolator(new BounceInterpolator());
		rotationAnim.start();
	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static void showToast(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
	
	public static <E> void saveValue(Context context, String key, E value) {
		SharedPreferences sp = getSharedPreferences(context);
		SharedPreferences.Editor editor = sp.edit();
		if (value instanceof String) {
			editor.putString(key, (String) value);
		} else if (value instanceof Integer) {
			editor.putInt(key, (Integer) value);
		} else if (value instanceof Boolean) {
			editor.putBoolean(key, (Boolean) value);
		} else if (value instanceof Float) {
			editor.putFloat(key, (Float) value);
		} else if (value instanceof Long) {
			editor.putLong(key, (Long) value);
		} else if (value instanceof Set) {
			editor.putStringSet(key, (Set) value);
		}
		editor.commit();
	}

	public static <E> E getValue(Context context, String key, E defaultValue, Class<E> value) {
		SharedPreferences sp = getSharedPreferences(context);
		Object obj = null;
		if (value == String.class) {
			obj = sp.getString(key, (String) defaultValue);
		} else if (value == Integer.class) {
			obj = sp.getInt(key, (Integer) defaultValue);
		} else if (value == Boolean.class) {
			obj = sp.getBoolean(key, (Boolean) defaultValue);
		} else if (value == Float.class) {
			obj = sp.getFloat(key, (Float) defaultValue);
		} else if (value == Long.class) {
			obj = sp.getLong(key, (Long) defaultValue);
		} else if (value == Set.class) {
			obj = sp.getStringSet(key, (Set<String>) defaultValue);
		}
		return (E) obj;
	}

	public static SharedPreferences getSharedPreferences(Context context) {
		if (sp == null) {
			sp = context.getSharedPreferences(SHARD_FILE_NAME, Context.MODE_PRIVATE);
		}
		return sp;
	}
}
