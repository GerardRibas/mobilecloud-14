/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import java.io.IOException;
import java.util.Collection;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.magnum.dataup.service.VideoService;
import org.magnum.dataup.service.VideoServiceImpl.VideoServiceException;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Controller
public class VideoController {

    private transient VideoService service;

    @RequestMapping(value = "/video", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody Collection<Video> listVideos() {
        return service.getVideos();
    }

    @RequestMapping(value = "/video", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public @ResponseBody Video postVideo(@RequestBody Video video) {
        return service.addVideo(video, getUrlBaseForLocalServer());
    }

    @RequestMapping(value = "/video/{id}/data", method = RequestMethod.GET)
    public void getVideoData(@PathVariable long id, HttpServletResponse response)
            throws IOException {
        try {
            service.getVideoData(id, response.getOutputStream());
        } catch (VideoServiceException e) {
            throw new ResourceNotFoundException(e.getMessage());
        }
    }

    @RequestMapping(value = "/video/{id}/data", method = RequestMethod.POST, consumes = "multipart/form-data", produces = "application/json")
    public @ResponseBody VideoStatus postVideoData(@PathVariable long id,
            @RequestParam("data") Part data) throws IOException {
        try {
            service.saveVideoData(id, data.getInputStream());
        } catch (VideoServiceException e) {
            throw new ResourceNotFoundException(e.getMessage());
        }
        return new VideoStatus(VideoState.READY);
    }

    @Resource
    public void setVideoService(VideoService videoService) {
        this.service = videoService;
    }

    private String getUrlBaseForLocalServer() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getRequest();
        String base = "http://"
                + request.getServerName()
                + ((request.getServerPort() != 80) ? ":"
                        + request.getServerPort() : "");
        return base;
    }

}
