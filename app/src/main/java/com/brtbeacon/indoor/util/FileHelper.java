package com.brtbeacon.indoor.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileHelper {

	static final String TAG = FileHelper.class.getSimpleName();

	public static void copyFolderFromAsset(Context context, String sourcePath,
										   String targetPath) {
		try {
			(new File(targetPath)).mkdirs();

			String[] childs = context.getAssets().list(sourcePath);

			File temp = null;

			for (int i = 0; i < childs.length; i++) {
				if (sourcePath.endsWith(File.separator)) {
					temp = new File(sourcePath + childs[i]);
				} else {
					temp = new File(sourcePath + File.separator + childs[i]);
				}

				int lastIndex = temp.toString().lastIndexOf(".");

				if (lastIndex != -1) {
					InputStream input = context.getAssets().open(
							temp.toString());

					FileOutputStream output = new FileOutputStream(targetPath
							+ File.separator + temp.getName());

					byte[] b = new byte[1024 * 5];
					int length;
					while ((length = input.read(b)) != -1) {
						output.write(b, 0, length);
					}

					output.flush();
					output.close();
					input.close();
				}

				if (lastIndex == -1) {
					copyFolderFromAsset(context, sourcePath + File.separator
							+ childs[i], targetPath + File.separator
							+ childs[i]);
				}
			}

		} catch (Exception e) {
			System.out.println("复制整个文件夹内容操作出错");
			e.printStackTrace();
		}
	}

	public static void copyFolder(Context context, String sourcePath,
								  String targetPath) {
		try {
			(new File(targetPath)).mkdirs();

			File a = new File(sourcePath);
			String[] childs = a.list();

			File temp = null;

			for (int i = 0; i < childs.length; i++) {
				if (sourcePath.endsWith(File.separator)) {
					temp = new File(sourcePath + childs[i]);
				} else {
					temp = new File(sourcePath + File.separator + childs[i]);
				}

				if (temp.isFile()) {
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(targetPath
							+ File.separator + temp.getName());

					byte[] b = new byte[1024 * 5];
					int length;
					while ((length = input.read(b)) != -1) {
						output.write(b, 0, length);
					}

					output.flush();
					output.close();
					input.close();
				}

				if (temp.isDirectory()) {
					copyFolder(context,
							sourcePath + File.separator + childs[i], targetPath
									+ File.separator + childs[i]);
				}
			}

		} catch (Exception e) {
			System.out.println("复制整个文件夹内容操作出错");
			e.printStackTrace();
		}
	}

	public static void deleteFile(File file) {

		if (file.isFile()) {
			file.delete();
			return;
		}

		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (childFiles == null || childFiles.length == 0) {
				file.delete();
				return;
			}

			for (int i = 0; i < childFiles.length; i++) {
				deleteFile(childFiles[i]);
			}
			file.delete();
		}
	}
}
