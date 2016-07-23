/*
 * Copyright (C) 2016 Anaphase21
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tube.api;

import java.util.*;
import javax.swing.*;
import java.net.*;
import java.io.*;

/**
 *
 * @author Anaphase21
 */
public class SimpleYouTubeParser{
    public int pageNumber = 1;
    private String id;
    private String title;
    private String duration;
    private String uploader;
    private String uploadDate;
    private String numViews;
    private String watchTag;
    private String durationTag;
    private String uploaderTag;
    private String numResultsTag;
    private String tvdTag;
    private String url;
    String HTML;
    private int index = 0;
    private final String watch = "<a href=\"/watch?v=";
    String videoProperties;
    public HashMap<String, ImageIcon> video;
    ImageIcon thumbnail;
    public SimpleYouTubeParser(){
    }
    
    public boolean process(String url, YouTubeInfoFile yt){
        this.url = url;
        videoProperties = " ";
        int currentIndex = 0;
        try{
            HTML = yt.getPageSource(url);
        }catch(IOException ioe){
            return false;
        }
        video = new HashMap<>();
            while((currentIndex = HTML.indexOf(watch, currentIndex)) != -1){
                setWatchTag(HTML, currentIndex);
                setDurationTag(HTML, currentIndex);
                setUploaderTag(HTML, currentIndex);
                setTvdTag(HTML, currentIndex);
                id = getId();
                videoProperties = "["+id+"]-";
                title = getElementValue(watchTag, 0, true);
                videoProperties += title+"<br>";
                duration = getElementValue(durationTag, 0, true);
                videoProperties += duration+"<br>";
                uploader = getElementValue(uploaderTag, 0, true);
                videoProperties += "- Uploader: "+uploader+"<br>";
                uploadDate = getElementValue(tvdTag, index, false);
                videoProperties += "- Date: "+uploadDate+"<br>";
                index++;
                numViews = getElementValue(tvdTag, index, false);
                videoProperties += "- Views: "+numViews;
                if(videoProperties.contains("class=\"yt")){
                    currentIndex += 5;
                    continue;
                }
                currentIndex++;
                index = 0;
                thumbnail = getThumbnail("https://i.ytimg.com/vi/"+id+"/mqdefault.jpg");
                video.put(videoProperties, thumbnail);
            }
            return true;
    }

void setWatchTag(String html, int currentIndex){
         int i = (html.indexOf(watch, currentIndex));
         boolean flag = (i > -1);
         if(flag){
             watchTag = html.substring(i, html.indexOf("</a>", currentIndex+1)+4);
             return;
         }
         watchTag = null;
    }
    
    void setDurationTag(String html, int currentIndex){
        int i = (html.indexOf("<span class=\"accessible-description\"", currentIndex+1));
        boolean flag = (i > -1);
        if(flag){
            durationTag = html.substring(i, html.indexOf("</span", i+1)+7);
            return;
        }
        durationTag = null;
    }
    
    void setUploaderTag(String html, int currentIndex){
        int i = html.indexOf("<a href=\"/", currentIndex+1);
        boolean flag = (i > -1);
        if(flag){
            uploaderTag = html.substring(i, html.indexOf("</a>", i+1)+4);
            return;
        }
        uploaderTag = null;
    }
    
    void setTvdTag(String html, int currentIndex){
        int i = html.indexOf("<ul class=\"", currentIndex);
        boolean flag = (i > -1);
        if(flag){
            tvdTag = html.substring(i, html.indexOf("</ul>", i+1)+5);
            return;
        }
        tvdTag = null;
    }
    
    void setNumResultsTag(String html){
        int i = html.indexOf("<p class=\"num-results first-focus\">");
        boolean flag = (i > -1);
        if(flag){
            numResultsTag = html.substring(i, html.indexOf("</p>")+4);
            return;
        }
        numResultsTag = null;
    }
    
    String getElementValue(String element, int from, boolean single){
        if(single){
        int i = element.indexOf(">");
        return (element.substring(i+1, element.indexOf("<", i+1)));
        }else{
          int i = element.indexOf("<li>", from);
          index = i;
          return element.substring(i+4, element.indexOf("</li", i));
        }
    }
    
    String getBriefDescription(String html, int currentIndex){
        int i = html.indexOf("<div", currentIndex);
        int j = html.indexOf(">", i);
        String s = html.substring(j+1, html.indexOf("</div", j+1));
        return s;
    }
    
    String getId(){
        int i = watchTag.indexOf("watch?v=");
        return (watchTag.substring(i+8, watchTag.indexOf("\"", i)));
    }
    
   public boolean hasNextPage(){
        if(HTML == null){
            return false;
        }
        int i = HTML.indexOf("Next "+'\u00BB');
        return (i > -1);
    }
    
   public boolean hasPreviousPage(){
       if(HTML == null){
           return false;
       }
       int i = HTML.indexOf('\u00AB'+" Previous");
       return (i > -1);
    }
   
    ImageIcon getThumbnail(String url){
        ImageIcon icon = null;
        URL connection = null;
        byte[] data = null;
        URLConnection conn = null;
        DataInputStream inputStream = null;
        try{
            connection = new URL(url);
            conn = connection.openConnection();
            conn.setDoInput(true);
            data = new byte[conn.getContentLength()];
            inputStream = new DataInputStream(conn.getInputStream());
            inputStream.readFully(data);
            icon = new ImageIcon(data);
        }catch(IOException ioe){
        }finally{
            if(inputStream != null){
                try{
                inputStream.close();
                }catch(IOException ioe){}
            }
        }
        return icon;
    }
}