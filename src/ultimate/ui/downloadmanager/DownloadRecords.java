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

/**
 *
 * @author Anaphase21
 */
public class DownloadRecords{
    public static String fullFileName = "downloads.josh";;
    public static HashMap<String, ArrayList> downloadRecords;
    public static String downloadDirectory;
    public DownloadRecords(){
        
    }
    
    public static void openDownloadRecords(){
        DataInputStream inputStream = null;
        ArrayList<String> list = new ArrayList(60);
        downloadRecords = new HashMap(15);
        String title = null;
        String url = null;
        String path = null;
        long fileSize = 0;
        long downloaded = 0;
        File file = new File(AppProperties.curDirectory+fullFileName);
        try{
            inputStream = new DataInputStream(new FileInputStream(file));
            while(true){
                title = inputStream.readUTF();
                url = inputStream.readUTF();
                path = inputStream.readUTF();
                fileSize = inputStream.readLong();
                downloaded = inputStream.readLong();
                list.add(url);
                list.add(path);
                list.add(String.valueOf(fileSize));
                list.add(String.valueOf(downloaded));
                downloadRecords.put(title, list);
            }
        }catch(EOFException eof){
        }
        catch(IOException ioe){
            try{
                if(inputStream != null){
                    inputStream.close();
                }
            }catch(IOException ioee){
                
                
            }
            JOptionPane.showMessageDialog(null, ioe.toString()+"\nCouldn't read", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void saveDownload(String title, String url, String path, long fileSize, long downloaded){
        DataOutputStream dout = null;
        DataInputStream inputStream = null; 
        try{
            File file = new File(AppProperties.curDirectory+fullFileName);
            if(!file.exists()){
                file.createNewFile();
            }
            dout = new DataOutputStream(new FileOutputStream(file, true));
            dout.writeUTF(title);
            dout.writeUTF(url);
            dout.writeUTF(path);
            dout.writeLong(fileSize);
            dout.writeLong(downloaded);
            if(inputStream != null){
                inputStream.close();
            }
            dout.close();
        }catch(IOException ioe){
        }
    }
    
    public static void deleteRecord(String title, String url, String path, long downl, long size){
        DataInputStream din = null;
        DataOutputStream dout = null;
        File file = new File(AppProperties.curDirectory+fullFileName);
        File newFile = null;
        String titl = null;
        String urlthis = null;
        String paththis = null;
        long downloaded = 0;
        long fileSize = 0;
        try{
            if(!file.exists()){
                return;
            }
            newFile = new File(AppProperties.curDirectory+fullFileName+".bak");
            din = new DataInputStream(new FileInputStream(file));
            dout = new DataOutputStream(new FileOutputStream(newFile));
            while(true){
                titl = din.readUTF();
                urlthis = din.readUTF();
                paththis = din.readUTF();
                fileSize = din.readLong();
                downloaded = din.readLong();
                if(!((downloaded == downl) && (titl.equals(title)) && (fileSize == size) &&(urlthis.equals(url)) && (paththis.equals(path)))){
                    dout.writeUTF(titl);
                    dout.writeUTF(urlthis);
                    dout.writeUTF(paththis);
                    dout.writeLong(fileSize);
                    dout.writeLong(downloaded);
                }
            }
        }catch(IOException ioe){
            try{
                if(din != null){
                    din.close();
                }
                if(dout != null){
                    dout.close();
                }
                boolean deleted = (new File(AppProperties.curDirectory+fullFileName)).delete();
               if(deleted){
                   if(newFile != null){
                        newFile.renameTo(file);
                   }
               }
            }catch(IOException ioe2){ 
        }
    }
    }
    
    public static void saveDownloadsFilePath(){
        if(AppProperties.curDirectory == null){
            return;
        }
        DataOutputStream dout = null;
        try{
            dout = new DataOutputStream(new FileOutputStream(new File(System.getProperty("user.home")+File.separator+"rec")));
            dout.writeUTF(AppProperties.curDirectory);
            dout.close();
        }catch(IOException ioe){
            if(dout != null){
                try{
                    dout.close();
                }catch(IOException ioe2){
                    
                }
            }
        }
    }
    
    public static void openDownloadsFilepath(){
        DataInputStream din = null;
        try{
            din = new DataInputStream(new FileInputStream(new File(System.getProperty("user.home")+File.separator+"rec")));
            AppProperties.curDirectory = din.readUTF();
                        JOptionPane.showMessageDialog(null, AppProperties.curDirectory+": prop", "Error", JOptionPane.ERROR_MESSAGE);
            din.close();
        }catch(IOException ioe){
            JOptionPane.showMessageDialog(null, ioe.toString()+" "+AppProperties.curDirectory+":error", "Error", JOptionPane.ERROR_MESSAGE);
            if(din != null){
                try{
                    din.close();
                }catch(IOException ioe2){
                    
                }
            }
        }
    }
}