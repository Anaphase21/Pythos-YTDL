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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import properties.AppProperties;

/**
 *
 * @author Anaphase21
 */
public class Downloader extends SwingWorker<Void, Void>{
        String title;
        String path;
        String id;
        String resolution;
        long time;
        volatile long fileSize;
        volatile long downloaded;
        String url;
        String redirectUrl;
        final int paused = 0;
        final int active = 1;
        final int complete = 2;
        volatile int STATE = paused;
        int responseCode = 0;
        volatile int bytesRead = 0;
        URL connection;
        HttpURLConnection urlConn;
        byte[] data;
        volatile BufferedInputStream din;
        volatile BufferedOutputStream out;
        File fileConnection = null;
        DownloadTaskPane taskPane;
        boolean fileAlreadyExists = false;
        volatile boolean closeOperationFinished = false;
        volatile boolean closed = false;
        volatile boolean downloadInited = false;
        volatile boolean isPausedBefore = false;//a check to ensure that getContentLength is not called more than once

        Downloader(String url, String title, String path, String id, String resolution, long time, long size, long downloaded){
            this.url = url;
            this.title = AppProperties.replaceInvalidCharacters(title);
            this.path = path;
            this.time = time;
            this.fileSize = size;
            this.downloaded = downloaded;
            this.id = id;
            this.resolution = resolution;
        }
        
        private void setState(int state){
            firePropertyChange("D_STATE", -1, state);
        }
        
        void setFileSize(long fileSize){
            firePropertyChange("fileSize", 0, fileSize);
        }
        
        void setDownloaded(long downloaded){
           firePropertyChange("downloaded", 0, downloaded);
        }
        
        void setException(String ex){
            firePropertyChange("Exception", null, ex);
        }
        
        void setTitle(String title){
            firePropertyChange("title", "empty", title);
        }
        
        void setRetry(boolean retry){
            firePropertyChange("retry", false, true);
        }
        
        @Override
        public Void doInBackground(){
            int k =0;
            try{
                initDownload();
                if(!closed){
                    STATE = active;
                    setState(active);
                }else{
                    STATE = paused;
                }
                int len = data.length;
                while(true){
                     if(STATE == paused){
                         if(k == 0){
                            setState(paused);
                            k = 1;
                         }
                         //doWait();
                         isPausedBefore = true;
                         if(closed){
                            initDownload();
                            closed = false;
                         }
                         continue;
                     }
                     if(closed){
                         closeStreams();
                         setState(paused);
                         STATE = paused;
                         return null;
                     }
                     k = 0;
                    bytesRead = din.read(data, 0, len);
                    if(bytesRead == -1){
                        break;
                    }
                    out.write(data, 0, bytesRead);
                    out.flush();
                    downloaded += bytesRead;
                    DownloadRecords.saveDownload(time, downloaded);
                    setDownloaded(downloaded);
                    setProgress((int)(100*downloaded/fileSize));
                }
                closeStreams();
                closed = true;
                if(STATE != paused){
                    setState(complete);
                }
            }catch(MalformedURLException murl){
                closeStreams();
                STATE = paused;
                setState(paused);
                closed = true;
                setException(murl.toString()+": Please try again.");
            }catch(IOException ioe){
                closeStreams();
                STATE = paused;
                setState(paused);
                closed = true;
                setException(ioe.toString()+": Please try again.");
            }
            return null;
        }
        
        private void doWait(){
            synchronized(this){
                try{
                    this.wait();
                }catch(InterruptedException ine){
                       
                }
            }
        }
        
        public void doNotify(){
            synchronized(taskPane){
            taskPane.notify();
           }
        }
        
        void closeStreams(){
            try{
            }
            finally{
            if(din != null){
                try{
                    din.close();
                    closed = true;
                    setState(paused);
                }catch(IOException ioe){setState(paused);}
            }
            if(out != null){
                try{
                    out.close();
                    closed = true;
                    doNotify();
                    setState(paused);
                }catch(IOException ioe){setState(paused);}
            }
        }
    }

    void initDownload() throws MalformedURLException, IOException{
        setState(4);
        try{
            connection = new URL(url);
            fileConnection = new File(path+title);
            if(!fileConnection.exists()){
                fileConnection.createNewFile();
            }else{
                if(fileAlreadyExists){
                    do{
                    title = AppProperties.renameDuplicateFile(title);
                    fileConnection = new File(AppProperties.curDirectory+title);
                    }while(fileConnection.exists());
                    fileConnection.createNewFile();
                    setTitle(title);
                    fileAlreadyExists = false;
                }
            }
            out = new BufferedOutputStream(new FileOutputStream(fileConnection, true));
            urlConn = (HttpURLConnection)connection.openConnection();
            urlConn.setRequestProperty("Range", "bytes="+String.valueOf(downloaded)+"-");
            urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36 OPR/29.0.1795.47");
            urlConn.setConnectTimeout(60000);
            responseCode = urlConn.getResponseCode();
            closed = false;
            if(responseCode == HttpURLConnection.HTTP_MOVED_TEMP){
                redirectUrl = urlConn.getHeaderField("Location");
                connection = new URL(redirectUrl);
                urlConn = (HttpURLConnection)connection.openConnection();
            }
                //Make sure getContentLength is not called again when resuming a download, otherwise
                //getContentLength will return a much lower value since it's a partial content.
            if(!isPausedBefore){
                fileSize = urlConn.getContentLength();
                setFileSize(fileSize);
            }
            din = new BufferedInputStream(urlConn.getInputStream());
            data = new byte[2048];
            downloadInited = true;
            if(!isPausedBefore){
                DownloadRecords.saveDownload(title, url, path, id, resolution, time, fileSize, downloaded);
            }
        }catch(UnknownHostException unke){
            STATE = paused;
            closeStreams();
            System.out.println("Resume");
            closed = true;
//            setException(unke.toString()+":\nPlease check your internet connection and try again.");
        }catch(IOException ioe){
            STATE = paused;
            closeStreams();
            closed = true;
        }catch(SecurityException se){
            STATE = paused;
            closeStreams();
            closed = true;
//        setException(se.toString());
        }
            if(responseCode == 403){
                setRetry(true);
            }
    }
    
    @Override
    public void done(){
        try{
            get();
        }catch(InterruptedException ie){
            
        }catch(ExecutionException ee){
            
        }
    }
    
    public void setDownloadPane(DownloadTaskPane taskPane){
        this.taskPane = taskPane;
    }
}