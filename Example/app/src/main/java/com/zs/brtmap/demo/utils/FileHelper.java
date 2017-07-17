package com.zs.brtmap.demo.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.Context;
import android.text.TextUtils;

public class FileHelper {

	static final String TAG = FileHelper.class.getSimpleName();

	public static String readStringFromAsset(Context context, String sourcePath) {
		StringBuffer buffer = new StringBuffer();
		try {
			InputStream input = context.getAssets().open(sourcePath);
			InputStreamReader in = new InputStreamReader(input);
			BufferedReader reader = new BufferedReader(in);

			// byte[] b = new byte[1024 * 5];
			// int length;
			// while ((length = input.read(b)) != -1) {
			//
			// }

			String line = "";
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return buffer.toString();
	}

	public static void copyFolderFromAsset(Context context, String sourcePath,
			String targetPath) {
		try {

			String[] childs = context.getAssets().list(sourcePath);

            if (childs.length > 0) {
                (new File(targetPath)).mkdirs();
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
            }else{
                InputStream is = context.getAssets().open(sourcePath);
                FileOutputStream fos = new FileOutputStream(new File(targetPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                }
                fos.flush();// 刷新缓冲区
                is.close();
                fos.close();
            }

		} catch (Exception e) {
			System.out.println("复制整个文件内容操作出错");
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
	public static boolean unZipFile(String filePath,String toDir) {
        boolean result = false;
        if (TextUtils.isEmpty(filePath)) {
            return result;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            return result;
        }

        OutputStream out = null;
        InputStream in = null;
        ZipFile zip = null;
        try {
            zip = new ZipFile(file);
            for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String zipEntryName = entry.getName().replace("\\", "/");
                String outPath = (toDir + File.separator + zipEntryName);
                // 输出文件路径信息
                System.out.println(zipEntryName+" to "+outPath);

                File outFile = new File(outPath);
                if (entry.isDirectory()) {
                    if (!outFile.exists()) {
                        outFile.mkdirs();
                    }
                } else {
                    File parentPath = new File(outFile.getParent());
                    if (!parentPath.exists()) {
                        parentPath.mkdirs();
                    }
                    in = zip.getInputStream(entry);
                    out = new FileOutputStream(outFile);
                    byte[] buf1 = new byte[1024];
                    int len;
                    while ((len = in.read(buf1)) > 0) {
                        out.write(buf1, 0, len);
                    }
                    out.flush();
                }
            }
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static boolean makeDir(String path) {
        boolean result = false;
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            result = true;
        }
        return result;
    }

    public static void clearCacheDir(String dir) {
        if (TextUtils.isEmpty(dir)) {
            return;
        }

        File file = new File(dir);
        if (!file.exists()) {
            return;
        }

        clearCacheDir(file);
    }

    private static void clearCacheDir(File file) {
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            for (File chile : childFile) {
                clearCacheDir(chile);
            }
            file.delete();
        } else {
            file.delete();
        }
    }

    public static String readFileToString(String filePath) {
        StringBuilder result = new StringBuilder();
        if (TextUtils.isEmpty(filePath)) {
            return result.toString();
        }
        if (!fileExists(filePath)) {
            return result.toString();
        }

        BufferedReader sr = null;
        try {
            sr = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))));
            String line = "";
            while (!TextUtils.isEmpty(line = sr.readLine())) {
                result.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sr != null) {
                try {
                    sr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result.toString();
    }
}
