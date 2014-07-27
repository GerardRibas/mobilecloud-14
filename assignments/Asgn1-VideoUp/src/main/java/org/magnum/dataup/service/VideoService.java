package org.magnum.dataup.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.magnum.dataup.model.Video;

public interface VideoService {

    Collection<Video> getVideos();

    Video addVideo(Video video, String baseUrl);

    void getVideoData(long id, OutputStream outputStream);

    void saveVideoData(long id, InputStream videoData);

}
