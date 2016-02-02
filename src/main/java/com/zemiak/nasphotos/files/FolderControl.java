package com.zemiak.nasphotos.files;

import java.io.Serializable;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class FolderControl implements Serializable {
    @Inject
    CoverControl covers;

    public PictureData convertFolderToPictureData(String path) {
        PictureData data = new PictureData();
        data.setPath(path);

        String title = path;
        int pos = title.lastIndexOf("/");
        data.setTitle(-1 == pos ? title : title.substring(pos + 1));
        data.setCoverUrl(covers.getFolderCoverUrl(path));
        data.setFullSizeUrl(data.getCoverUrl());

        return data;
    }
}
