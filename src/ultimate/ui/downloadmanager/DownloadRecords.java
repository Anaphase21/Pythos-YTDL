/*
 * Copyright (C) 2015 Anaphase21
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.ArrayList;
import javax.swing.JOptionPane;
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
    public static Long[] creationTimes;
    
    public DownloadRecords(){
        
    }
    
    public static void openDownloadRecords(){
        ArrayList<String> list = null;
        downloadRecords = new HashMap<>(20);
        Preferences downloads = Preferences.userRoot().node("pythos/downloads");//.node("properties.AppProperties.class");
        Preferences dwl = null;
        try{
            String[] children = downloads.childrenNames();
            creationTimes = new Long[children.length];
            String title = null;
            String url = null;
            String path = null;
            String trunc = null;
            long creationTime = 0;
            long fileSize = 0;
            long downloaded = 0;
            ArrayList<Long> times = new ArrayList<>(30);
            for(String child : children){
                dwl = Preferences.userRoot().node("pythos").node("downloads").node(child);
                title = dwl.get("title", " ");
                url = dwl.get("url", " ");
                path = dwl.get("path", " ");
                trunc = dwl.get("trunc", " ");
                creationTime = dwl.getLong("creationTime", 0);
                fileSize = dwl.getLong("size", 0);
                downloaded = dwl.getLong("downloaded", 0);
                list = new ArrayList<>(7);
                list.add(url);
                list.add(path);
                list.add(trunc);
                list.add(String.valueOf(creationTime));
                list.add(String.valueOf(fileSize));
                list.add(String.valueOf(downloaded));
                downloadRecords.put(title, list);
                times.add(creationTime);
            }
            creationTimes = times.toArray(creationTimes);
            java.util.Arrays.sort(creationTimes);
        }catch(BackingStoreException bse){
            
        }
    }
    
    public static void saveDownload(String title, String url, String path, String trunc, long creationTime, long fileSize, long downloaded){
        Preferences downloads = Preferences.userRoot().node("pythos").node("downloads").node(trunc);
        downloads.put("title", title);
        downloads.put("url", url);
        downloads.put("path", path);
        downloads.put("trunc", trunc);
        downloads.putLong("creationtime", creationTime);
        downloads.putLong("size", fileSize);
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