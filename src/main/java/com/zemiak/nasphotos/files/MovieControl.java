package com.zemiak.nasphotos.files;

import com.zemiak.nasphotos.lookup.ConfigurationProvider;
import com.zemiak.nasphotos.thumbnails.ImageInformation;
import com.zemiak.nasphotos.thumbnails.ThumbnailService;
import com.zemiak.nasphotos.thumbnails.ThumbnailSize;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class MovieControl {
    private static final Logger LOG = Logger.getLogger(MovieControl.class.getName());
    private static final Pattern VALID_MOVIE = Pattern.compile("^\\d\\d\\d\\d\\/\\d\\d\\d\\d .+\\/.+(\\.mov|\\.mp4|\\.m4v)");

    final private String photoPath = ConfigurationProvider.getPhotoPath();
    final private String externalUrl = ConfigurationProvider.getExternalUrl();
    final private String tempPath = ConfigurationProvider.getTempPath();

    @Inject CoverControl covers;
    @Inject MetadataReader metaData;

    public List<PictureData> getMovies(String pathName) {
        if (FileService.isRoot(pathName)) {
            return Collections.EMPTY_LIST;
        }

        List<String> files;
        try {
            files = Files.walk(Paths.get(photoPath, pathName), 1, FileVisitOption.FOLLOW_LINKS)
                    .skip(1)
                    .filter(path -> !path.toFile().isDirectory())
                    .filter(path -> path.toFile().canRead())
                    .filter(path -> isMovieFile(path) && !pictureAssociatedToMovieExists(path))
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "getMovies IO/Exception" + ex.getMessage(), ex);
            return Collections.EMPTY_LIST;
        }

        Collections.sort(files);
        return files
                .stream()
                .map(n -> getPictureData(Paths.get(photoPath, pathName, n).toFile(), pathName))
                .collect(Collectors.toList());
    }

    public List<LivePhotoData> getLivePhotos(String pathName) {
        if (FileService.isRoot(pathName)) {
            return Collections.EMPTY_LIST;
        }

        List<String> files;
        try {
            files = Files.walk(Paths.get(photoPath, pathName), 1, FileVisitOption.FOLLOW_LINKS)
                    .skip(1)
                    .filter(path -> !path.toFile().isDirectory())
                    .filter(path -> path.toFile().canRead())
                    .filter(this::isLivePhotoMovieFile)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "getLivePhotos IO/Exception" + ex.getMessage(), ex);
            return Collections.EMPTY_LIST;
        }

        Collections.sort(files);
        return files
                .stream()
                .map(n -> getPictureData(Paths.get(photoPath, pathName, n).toFile(), pathName))
                .map(data -> {
                    Path path = Paths.get(photoPath, data.getPath());
                    String picturePath = getPictureNameAssociatedToMovie(path);
                    data.setImageUrl(covers.getPictureFullSizeUrl(picturePath));
                    data.setInfo(metaData.getImageInfo(Paths.get(photoPath, picturePath).toFile()));
                    return data;
                })
                .collect(Collectors.toList());
    }

    public boolean isMovieFile(Path path) {
        if (PictureControl.isHidden(path)) {
            LOG.log(Level.FINE, "isMovieFile: hidden {0}", path.toString());
            return false;
        }

        String name = path.toAbsolutePath().toString();
        if (name.startsWith(photoPath)) {
            name = name.substring(photoPath.length());
        }
        if (name.startsWith("/")) {
            name = name.substring(1);
        }

        boolean matches = VALID_MOVIE.matcher(name.toLowerCase()).matches();
        return matches;
    }

    public boolean isLivePhotoMovieFile(Path path) {
        return isMovieFile(path) && pictureAssociatedToMovieExists(path);
    }

    private boolean pictureAssociatedToMovieExists(Path path) {
        return null != getPictureNameAssociatedToMovie(path);
    }

    private String getPictureNameAssociatedToMovie(Path path) {
        String name = path.toAbsolutePath().toString();
        int pos = name.lastIndexOf(".");
        if (pos > -1) {
            name = name.substring(0, pos);
        }
        String nameWithoutExt = name;

        Optional<String> found = Arrays.asList("jpg", "JPG", "png", "PNG").stream()
                .filter(ext -> pictureExists(nameWithoutExt, ext))
                .findFirst();

        if (found.isPresent()) {
            name = nameWithoutExt.substring(photoPath.length());
            return name + "." + found.get();
        }

        if (! nameWithoutExt.endsWith("-2")) {
            return null;
        }

        String nameWithoutExt2 = name.substring(0, name.length() - 2);
        found = Arrays.asList("jpg", "JPG", "png", "PNG").stream()
                .filter(ext -> pictureExists(nameWithoutExt2, ext))
                .findFirst();

        name = found.isPresent() ? nameWithoutExt2 + "." + found.get() : null;

        if (null != name) {
            name = name.substring(photoPath.length());
        }

        return name;
    }

    private boolean pictureExists(String name, String ext) {
        File file = new File(name + "." + ext);
        return file.canRead();
    }

    private LivePhotoData getPictureData(File file, String relativePath) {
        if (null == file) {
            return null;
        }

        LivePhotoData data = new LivePhotoData();
        data.setFile(file);
        data.setPath(relativePath + "/" + file.getName());

        String name = file.getAbsolutePath();
        name = name.contains("/") ? name.substring(name.lastIndexOf("/") + 1) : name;
        name = name.contains(".") ? name.substring(0, name.indexOf(".")) : name;
        data.setTitle(name);
        data.setInfo(metaData.getImageInfo(file));

        data.setCoverUrl(covers.getMovieCoverUrl(data.getPath()));
        data.setFullSizeUrl(getFullMovieSizeUrl(data.getPath()));

        File thumbnail = Paths.get(tempPath, ThumbnailService.getThumbnailFileName(Paths.get(file.getAbsolutePath())) + ".jpg").toFile();
        ImageInformation thumbnailInfo = metaData.getImageInfo(thumbnail);
        if (null != thumbnailInfo) {
            if (thumbnailInfo.getWidth() > thumbnailInfo.getHeight()) {
                data.setCoverWidth(Math.min(ThumbnailSize.WIDTH, thumbnailInfo.getWidth()));
                data.setCoverHeight(Math.min(ThumbnailSize.HEIGHT, thumbnailInfo.getHeight()));
            } else {
                data.setCoverWidth(Math.min(ThumbnailSize.HEIGHT, thumbnailInfo.getWidth()));
                data.setCoverHeight(Math.min(ThumbnailSize.WIDTH, thumbnailInfo.getHeight()));
            }

            if (null == data.getInfo()) {
                data.setInfo(thumbnailInfo);
            }
        }

        return data;
    }

    private String getFullMovieSizeUrl(String path) {
        try {
            return externalUrl + "stream/" + URLEncoder.encode(path, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException("UTF-8 encoding not supported");
        }
    }
}