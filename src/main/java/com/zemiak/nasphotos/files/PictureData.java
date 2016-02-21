package com.zemiak.nasphotos.files;

import java.io.File;

public class PictureData {
    private String path;
    private String title;
    private File file;
    private String coverUrl;
    private String fullSizeUrl;
    private ImageInformation info;

    public PictureData() {
        info = new ImageInformation();
    }

    public PictureData(PictureData pic) {
        path = pic.path;
        title = pic.title;
        file = pic.file;
        coverUrl = pic.coverUrl;
        fullSizeUrl = pic.fullSizeUrl;
        info = new ImageInformation(pic.info);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getWidth() {
        return info.getWidth();
    }

    public long getHeight() {
        return info.getHeight();
    }

    public int getOrientation() {
        return info.getOrientation();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getFullSizeUrl() {
        return fullSizeUrl;
    }

    public void setFullSizeUrl(String fullSizeUrl) {
        this.fullSizeUrl = fullSizeUrl;
    }

    public ImageInformation getInfo() {
        return info;
    }

    public void setInfo(ImageInformation info) {
        this.info = info;
    }

    public void setHeight(long height) {
        info.setHeight(height);
    }

    public void setWidth(long width) {
        info.setWidth(width);
    }
}
