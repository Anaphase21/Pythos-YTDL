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
package properties;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.awt.*;
import javax.imageio.ImageIO;
import java.io.DataInputStream;
import java.io.File;

/**
 *
 * @author Anaphase21
 */

public class AppProperties{
    
    public static String curDirectory = null;
    public static Image backgroundImg;
    public static String title = null;
    public static String HTML5Player = "";
    public static String script = "";
    
    private AppProperties(){
    }

    public static Image loadBackgroundImage(){//This method is called by main class(UltimateYTDL) during start up
        try{
            DataInputStream din = new DataInputStream(AppProperties.class.getResourceAsStream("/res/wallpapers/RobBye.jpg"));
            backgroundImg = ImageIO.read(din);
        }catch(IOException ioe){}
        return backgroundImg;
    }
        
    static char[] invalidChars = {'?', '*', '/', ';', ':', '<', '>', '|', '\n', '\\', '"'};
        
    public static String replaceInvalidCharacters(String title){
        int currentChar = 0;
        int len = invalidChars.length;
        String str = null;
        String titl = title;
        str = " ";
            for(currentChar = 0; currentChar < len; currentChar++){
               str = titl.replace(invalidChars[currentChar], '_');
               titl = str;
            }
        return str;
    }
    
    public static String getExtension(String key){
        String ext = null;
        String extension = null;
        int start = 0;
        int end = 0;
        if(key != null){
            if(key.contains("MP4")){
                ext = ".mp4";
                start = key.indexOf(" ");
                end = key.indexOf(" ", start+1);
                if(end == -1){
                    extension = "_"+key.substring(start+1);
                }else{
                    extension = "_"+key.substring(start+1, end);
                }
                extension += ext;
            }else if(key.contains("WEBM")){
                ext = ".webm";
                start = key.indexOf(" ");
                end = key.indexOf(" ", start+1);
                if(end == -1){
                    extension = "_"+key.substring(start+1);
                }else{
                    extension = "_"+key.substring(start+1, end);
                }
                extension += ext;
           }else if(key.contains("FLV")){
                ext = ".flv";
                start = key.indexOf(" ");
                end = key.indexOf(" ", start+1);
                if(end == -1){
                    extension = "_"+key.substring(start+1);
                }else{
                    extension = "_"+key.substring(start+1, end);
                }
                extension += ext;
           }else if(key.contains("3GP")){
                ext = ".3gp";
                start = key.indexOf(" ");
                end = key.indexOf(" ", start+1);
                if(end == -1){
                    extension = "_"+key.substring(start+1);
                }else{
                    extension = "_"+key.substring(start+1, end);
                }
                extension += ext;
           }
        }
        return extension;
    }
        
    public static String bytesToMegaGiga(long numBytes){
        String size = null;
        double kilo = 0.0;
        double mega = 0.0;
        double giga = 0.0;
        double tera = 0.0;
        if(numBytes < 1024){
            size = String.valueOf(numBytes)+" B";
        }
        if(numBytes >= 1024){
            kilo = numBytes/1024;
            size = trimSize(String.valueOf(kilo))+" KB";
        }
        if(kilo >= 1024){
            mega = kilo/1024;
            size = trimSize(String.valueOf(mega))+" MB";
        }
        if(mega >= 1024){
            giga = mega/1024;
            size = trimSize(String.valueOf(giga))+" GB";
        }
        if(giga >= 1024){
            tera = giga/1024;
            size = trimSize(String.valueOf(tera))+" TB";
        }
        return size;
    }
    
    static String trimSize(String size){
        String str = null;
        int index = size.indexOf(".");
        if(index != -1){
            if(size.length() >= index+3){
            str = size.substring(0, index+3);
            }else{
                str = size;
            }
        }else{
            str = size;
        }
        return str;
    }
    
    public static String renameDuplicateFile(String duplicate){
        int j = duplicate.lastIndexOf('.');
        if(duplicate.charAt(j-1) != ')'){
            String s = "(1)";
            String g = duplicate.substring(j);
            duplicate = duplicate.substring(0, j)+s+g;
            return duplicate;
        }
        Pattern pattern = Pattern.compile("\\(([1-9])+\\)");
        Matcher matcher = pattern.matcher(duplicate);
        String s = null;
        while(matcher.find()){
           s = matcher.group();
        }
        if(s != null){
            int k = Integer.parseInt(s.substring(1, s.indexOf(")")));
            s = s.replaceAll("([1-9])+", String.valueOf(++k));
            String g = duplicate.substring(duplicate.lastIndexOf("."));
            duplicate = duplicate.substring(0, duplicate.lastIndexOf("("))+s+g;
            return duplicate;
        }else{
            s = "(1)";
            String g = duplicate.substring(duplicate.lastIndexOf("."));
            duplicate = duplicate.substring(0, duplicate.lastIndexOf("."))+s+g;
        }
        return duplicate;
    }
    
    public static long[] sort(long[] numbs){
        int l = numbs.length;
        long key = 0;
        int i = 0;
        for(int j = 1; j < l; ++j){
            key = numbs[j];
            i = j - 1;
            while((i >= 0) && (numbs[i] > key)){
                numbs[i+1] = numbs[i];
                --i;
            }
            numbs[i+1] = key;
        }
        return numbs;
    }
    
    public static boolean fileExists(String file){
        File fileConn = new File(file);
        return fileConn.exists()?true:false;
    }
}
