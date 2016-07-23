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

import java.net.*;
import java.io.*;
import java.util.*;
import java.beans.*;

/**
 *
 * @author Anaphase21
 */

public class YouTubeInfoFile{

    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36 OPR/29.0.1795.47";
    private final String REQUEST_METHOD = "GET";
    public HashMap<String, String> links;
    public String response;
    public String[] resolutionsArray;
    ArrayList<String> resolutions;
    UrlConstructor constructor;
    PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public YouTubeInfoFile(){
    }
    
    public boolean parseInfoFile(String infoFile){
        constructor = new UrlConstructor();
        constructor.constructURL(infoFile);
        this.links = constructor.links;
        this.resolutions = constructor.resolutions;
        listToStringArray(resolutions);
        getTitle(infoFile);
        return links.isEmpty();
    }
        
    private void listToStringArray(ArrayList list){
        int length = list.size();
        resolutionsArray = new String[length];
        for(int i = 0; i < length; i++){
            resolutionsArray[i] = (String)list.get(i);
        }
    }
    
    private void getTitle(String infoFile){
        if(infoFile == null){
            return;
        }
        int start = infoFile.indexOf("\"title\":\"");
        String str = null;
        if(start == -1){
            java.util.Random random = new Random(System.currentTimeMillis());
            int rand = random.nextInt(2000);
            str = "file"+String.valueOf(rand);
        }else{
            int end = infoFile.indexOf("\"", start+10);
            str = infoFile.substring(start+9, end);
        }
        properties.AppProperties.title = str;
    }
    
    @Deprecated
    public String getInfoFile(String id) throws IOException{
        String file = null;
        String idHD = "OG2eGVt6v2o";
        String id4k = "6pxRHBw-k8M";
        String idJolie = "UA7vRJ_L2UY";
        StringBuilder buff = new StringBuilder();
        URL httpConnection = null;
        BufferedReader reader = null;
        OutputStream outputStream = null;
        String query = URLEncoder.encode("video_id="+id+"&el=vevo", "UTF-8");
        String url = "https://youtube.com/get_video_info?video_id="+id+"&el=vevo";
        try{
            httpConnection = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)httpConnection.openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(query.length()));
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36 OPR/29.0.1795.47");
            conn.setRequestMethod("POST");
            outputStream = conn.getOutputStream();
            outputStream.write(query.getBytes("UTF-8"));
            response = conn.getResponseMessage();
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String str = null;
            while((str = reader.readLine()) != null){
                if(str != null){
                 buff.append(str);   
                }
            }
            
        }finally{
            if(reader != null){
            reader.close();
            }
            if(outputStream != null){
            outputStream.close();
            }
        }
        file = URLDecoder.decode(buff.toString(), "UTF-8");
        return response = URLDecoder.decode(file, "UTF-8");
    }
    
        public String getPageSource(String url) throws IOException{
        URL connection = null;
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        StringBuilder buff = new StringBuilder();
        String str = null;
        try{
            connection = new URL(url);
            conn = (HttpURLConnection)connection.openConnection();
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestMethod(REQUEST_METHOD);
            conn.setConnectTimeout(60000);
            conn.setReadTimeout(60000);
            response = conn.getResponseMessage();
            if(conn.getResponseCode() == 200){
                pcs.firePropertyChange("HTTP_OK", " ", "OK");
            }
            response = conn.getHeaderField("Location");
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            while((str = reader.readLine()) != null){
                buff.append(str);
            }
                    
        }catch(IOException ioe){
            setErrorMessage(ioe.toString());
            String err = ioe.toString();
            if(err.startsWith("java.net.UnknownHostException")){
                return null;
            }else{
                return err+"error";
            }
        }finally{
            if(reader != null){
                try{
                reader.close();
                }catch(IOException ioe){
                    setErrorMessage(ioe.toString());
                    ioe.printStackTrace();
                    return ioe.toString();
                }
            }
        }
        String s = buff.toString();
        return s;
    }

    public void setErrorMessage(String message){
        pcs.firePropertyChange("error", " ", message);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener){
        pcs.addPropertyChangeListener(listener);
    }
}