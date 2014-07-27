/**
 * 
 */
package org.magnum.dataup.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.magnum.dataup.VideoFileManager;
import org.magnum.dataup.model.Video;
import org.springframework.stereotype.Service;

/**
 * @author GR001
 *
 */
@Service
public class VideoServiceImpl implements VideoService {

    private VideoFileManager videoFileManager;

    private Collection<Video> videos;

    @PostConstruct
    public void init() throws IOException {
        videoFileManager = VideoFileManager.get();
        videos = new ArrayList<Video>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.magnum.dataup.service.VideoService#getVideos()
     */
    @Override
    public Collection<Video> getVideos() {
        return videos;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.magnum.dataup.service.VideoService#addVideo(org.magnum.dataup.model
     * .Video)
     */
    @Override
    public Video addVideo(Video video, String baseUrl) {
        videos.add(video);
        video.setId(videos.size() - 1);
        video.setDataUrl(baseUrl + "/video/" + video.getId() + "/data");
        return video;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.magnum.dataup.service.VideoService#getVideoData(long,
     * java.io.OutputStream)
     */
    @Override
    public void getVideoData(long id, OutputStream outputStream) {
        Video video = getVideo(id);
        if (video == null) {
            throw new VideoNotFoundException(String.format(
                    "Video does not exists for id=%s", id));
        } else if (!videoFileManager.hasVideoData(video)) {
            throw new VideoDataNotFoundException(String.format(
                    "Video data not found for id=%s", id));
        }
        try {
            videoFileManager.copyVideoData(video, outputStream);
        } catch (IOException e) {
            throw new VideoServiceException(String.format(
                    "Error copying file to outputStream for id %s", id), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.magnum.dataup.service.VideoService#saveVideoData(long,
     * java.io.InputStream)
     */
    @Override
    public void saveVideoData(long id, InputStream videoData) {
        Video video = getVideo(id);
        if (video == null) {
            throw new VideoNotFoundException(String.format(
                    "Video does not exists for id=%s", id));
        }
        try {
            videoFileManager.saveVideoData(video, videoData);
        } catch (IOException e) {
            throw new VideoServiceException(String.format(
                    "Error saving video to file for id %s", id), e);
        }
    }

    private Video getVideo(long id) {
        Video result = null;
        for (Iterator<Video> iterator = videos.iterator(); iterator.hasNext()
                && result == null;) {
            Video video = iterator.next();
            if (id == video.getId()) {
                result = video;
            }
        }
        return result;
    }

    public static class VideoServiceException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public VideoServiceException(String message, Throwable cause) {
            super(message, cause);
        }

        public VideoServiceException(String message) {
            super(message);
        }

    }

    public static class VideoNotFoundException extends VideoServiceException {

        private static final long serialVersionUID = 1L;

        VideoNotFoundException(String message) {
            super(message);
        }
    }

    public static class VideoDataNotFoundException extends
            VideoServiceException {

        private static final long serialVersionUID = 1L;

        VideoDataNotFoundException(String message) {
            super(message);
        }
    }

}
