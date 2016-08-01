
package com.pulkit.musicplayer.pojo;


import android.graphics.drawable.Drawable;

/**
 * @author PULKIT MITAL
 * @date 31/7/2016
 */
public class DrawerItem {
    String title;
    Drawable icon;

    public DrawerItem(String title, Drawable icon) {
        this.title = title;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public Drawable getIcon() {
        return icon;
    }
}
