package com.app.easyrecharge.model;

import com.app.easyrecharge.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ashmin on 3/30/2017.
 */
public class ExtraFeatures {
    private String title;
    private int imageId;

    public ExtraFeatures(String title, int imageId) {
        this.title = title;
        this.imageId = imageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public static List<ExtraFeatures> getData(){
        String[] titles=getTitles();
        int[] images=getImages();
        List<ExtraFeatures> data=new ArrayList<ExtraFeatures>();
        for(int i=0;i<titles.length;i++){
            data.add(new ExtraFeatures(titles[i],images[i]));
        }
        return data;
    }
    private static String[] getTitles(){
        return new String[]{"Balance","Call Center"};
    }
    private static int[] getImages(){
        return new int[]{R.drawable.ic_balance,R.drawable.ic_balance};
    }

}
