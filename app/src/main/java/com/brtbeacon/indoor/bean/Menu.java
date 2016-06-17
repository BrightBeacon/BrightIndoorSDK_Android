package com.brtbeacon.indoor.bean;

/**
 * 主界面显示的菜单数据
 * Created by BrightBeacon on 2016/5/31 0031.
 */
public class Menu {

    private String title;
    private String desc;

    public Menu() {
    }

    public Menu(String title, String desc) {
        this.title = title;
        this.desc  = desc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
