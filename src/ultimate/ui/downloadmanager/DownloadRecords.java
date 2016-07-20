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
package ultimate.ui.downloadmanager;

import java.util.HashMap;
import java.util.ArrayList;
import properties.AppProperties;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

/**
 *
 * @author Anaphase21
 */
public class DownloadRecords{
    public static HashMap<String, ArrayList<String>> downloadRecords;
    public static String downloadDirectory;
    
    public DownloadRecords(){
        
    }
    
    public static void openDownloadRecords(){
        ArrayList<String> list = null;
        downloadRecords = new HashMap<>(20);
        Preferences downloads = Preferences.userRoot().node("pythos/downloads");//.node("properties.AppProperties.class");
        try{
            String[] children = downloads.childrenNames();
            String title = null;
            String url = null;
            String path = null;
            String id = null;
            String resolution = null;
            long time = 0;
            long fileSize = 0;
            long downloaded = 0;
            for(String child : children){
                downloads = Preferences.userRoot().node("pythos/downloads").node(child);
                title = downloads.get("title", " ");
                url = downloads.get("url", " ");
                path = downloads.get("path", " ");
                time = downloads.getLong("time", 0);
                fileSize = downloads.getLong("size", 0);
                downloaded = downloads.getLong("downloaded", 0);
                id = downloads.get("id", " ");
                resolution = downloads.get("resolution", " ");
                list = new ArrayList<>(7);
                list.add(title);
                list.add(url);
                list.add(path);
                list.add(String.valueOf(time));
                list.add(String.valueOf(fileSize));
                list.add(String.valueOf(downloaded));
                list.add(id);
                list.add(resolution);
                downloadRecords.put(child, list);
            }
        }catch(BackingStoreException bse){
            
        }
    }
    
    public static void saveDownload(String title, String url, String path, String id, String resolution, long time, long fileSize, long downloaded){
        Preferences downloads = Preferences.userRoot().node("pythos").node("downloads").node(String.valueOf(time));
        downloads.put("title", title);
        downloads.put("url", url);
        downloads.put("path", path);
        downloads.put("time", String.valueOf(time));
        downloads.putLong("size", fileSize);
        downloads.putLong("downloaded", downloaded);
        downloads.put("id", id);
        downloads.put("resolution", resolution);
    }

    public static void saveDownload(long time, long downloaded){
        Preferences downloads = Preferences.userRoot().node("pythos/downloads").node(String.valueOf(time));
        downloads.putLong("downloaded", downloaded);
    }
    
    public static void deleteRecord(String title, String path){
        if(title == null){
            return;
        }
        String titl = null;
        String pathis = null;
        Preferences downloads = Preferences.userRoot().node("pythos/downloads");
        Preferences dwl = null;
        try{
            String[] children = downloads.childrenNames();
            for(String node : children){
                dwl = Preferences.userRoot().node("pythos/downloads").node(node);
                titl = dwl.get("title", " ");
                pathis = dwl.get("path", " ");
                if((titl.equals(title))&&(pathis.equals(path))){
                    dwl.removeNode();
                }
            }
        }catch(BackingStoreException bse){
            System.out.print(bse.toString());
        }
    }
    
    public static void saveDirectory(String directory){
        Preferences dir = Preferences.userRoot().node("pythos/directory");
        dir.put("dir", directory);
    }
    
    public static void openDirectory(){
        Preferences dir = Preferences.userRoot().node("pythos/directory");
        AppProperties.curDirectory = dir.get("dir", null);
    }
}