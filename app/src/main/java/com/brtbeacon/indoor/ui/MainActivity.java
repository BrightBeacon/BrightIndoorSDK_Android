package com.brtbeacon.indoor.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.brtbeacon.indoor.R;
import com.brtbeacon.indoor.bean.Menu;
import com.brtbeacon.indoor.util.FileHelper;
import com.brtbeacon.indoor.util.VersionUtil;
import com.ty.mapsdk.TYMapEnvironment;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final String SP = "brtbeacon_copy_tag";

    private final int RESP_WRITE_REQUEST    = 0x0;//访问SD卡的请求码
    private final int RESP_LOCATION_REQUEST = 0x1;//访问位置信息的请求码

    private ListView listView;
    private ArrayList<Menu> menuList = new ArrayList<>();

    private String mapRootDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent intentBase = new Intent(MainActivity.this, BaseMapActivity.class);
                        startActivity(intentBase);
                        break;
                    case 1:
                        Intent intentPop = new Intent(MainActivity.this, PopviewActivity.class);
                        startActivity(intentPop);
                        break;
                    case 2:
                        Intent intentPoi = new Intent(MainActivity.this, PoiActivity.class);
                        startActivity(intentPoi);
                        break;
                    case 3: {
                        if (VersionUtil.isV23Plus()) {
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                    && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                //"没有授权"
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,  Manifest.permission.ACCESS_COARSE_LOCATION}, RESP_LOCATION_REQUEST);
                            }else {
                                //S.i("授权");
                                Intent intentNav = new Intent(MainActivity.this, NavActivity.class);
                                startActivity(intentNav);
                            }
                        }else {
                            Intent intentNav = new Intent(MainActivity.this, NavActivity.class);
                            startActivity(intentNav);
                        }
                    }
                        break;
                }
            }
        });

        menuList.add(new Menu("显示地图", "一个基础地图的展示"));
        menuList.add(new Menu("地图覆盖物", "在地图上显示弹框"));
        menuList.add(new Menu("路径规划", "在地图上显示路径规划"));
        menuList.add(new Menu("导航", "在地图上显示即时位置信息"));
        listView.setAdapter(new MyAdapter());

        if(isFirst()){
            if (VersionUtil.isV23Plus()){//判断版本号，23的要检查权限
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    System.out.println("没有授权");
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RESP_WRITE_REQUEST);
                }else {
                    System.out.println("授权");
                    setTag();
                    copyMapFiles();
                }
            }else {
                setTag();
                copyMapFiles();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RESP_WRITE_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "授权成功", Toast.LENGTH_SHORT).show();
                    setTag();
                    copyMapFiles();
                } else {
                    Toast.makeText(MainActivity.this, "授权失败", Toast.LENGTH_SHORT).show();
                   finish();
                }
            }
            break;
            case RESP_LOCATION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intentNav = new Intent(MainActivity.this, NavActivity.class);
                    startActivity(intentNav);
                }else {
                    Toast.makeText(MainActivity.this, "授权失败", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void setTag() {
        SharedPreferences settings = getSharedPreferences(SP, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("first", false);
    }

    /**
     * 判断是否拷贝过
     * @return
     */
    private boolean isFirst(){
        SharedPreferences settings = getSharedPreferences(SP, MODE_PRIVATE);
        return settings.getBoolean("first", true);
    }

    /**
     * 拷贝数据到SD卡
     */
    void copyMapFiles() {
        TYMapEnvironment.initMapEnvironment();
        mapRootDir = Environment.getExternalStorageDirectory() + "/MapDemo/MapFiles";
        TYMapEnvironment.setRootDirectoryForMapFiles(mapRootDir);

        String sourcePath = "MapResource";
        String targetPath = TYMapEnvironment.getRootDirectoryForMapFiles();
        FileHelper.deleteFile(new File(targetPath));
        FileHelper.copyFolderFromAsset(this, sourcePath, targetPath);
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return menuList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Menu menu = menuList.get(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.two_line_layout, parent, false);
            }
            TextView text1 = (TextView)  convertView.findViewById(R.id.text1);
            TextView text2 = (TextView) convertView.findViewById(R.id.text2);

            text1.setText(menu.getTitle());
            text2.setText(menu.getDesc());

            return convertView;
        }
    }
}
