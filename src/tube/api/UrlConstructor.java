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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Anaphase21
 */
public class UrlConstructor {
    static String sparamsRegex = "(sparams=)(.)+?[&]";
    static String paramsRegex = "(%2C)";
    static String VideoParameters = "";
    static String infoFile = "";
    public final static String MP42160 = "MP4 2160p+ Video only";
    public final static String MP41440 = "MP4 1440p Video only";
    public final static String MP41080V = "MP4 1080p Video only";
    public final static String MP41080 = "MP4 1080p";
    public final static String MP4720 = "MP4 720p";
    public final static String MP4360 = "MP4 360p";
    public final static String WEBM1440 = "WEBM 1440p Video only";
    public final static String WEBM1080 = "WEBM 1080p Video only";
    public final static String WEBM720 = "WEBM 720p Video only";
    public final static String WEBM360 = "WEBM 360p";
    public final static String FLV240 = "FLV 240p";
    public final static String WEBMAUDIO = "WEBM Audio only";
    public final static String MP4AUDIO = "MP4 Audio only";
    public final static String GP240 = "3GP 240p";
    public final static String GP144 = "3GP 144p";
    public HashMap<String, String> links;
    public String[] resolutionsArray;
    public ArrayList<String> resolutions;
    private String sigFunctionName;

    public UrlConstructor(){
        
    }
        
    public void constructURL(String infoFile){
        String decypheringScript = "null";
        if(infoFile == null){
            return;
        }
        String html5Player = null;
        int start1 = infoFile.indexOf("adaptive_fmts\":\"");
        int start = infoFile.indexOf("url_encoded_fmt_stream_map\":\"");
        String adaptive = infoFile.substring(start1+16, infoFile.indexOf("\"", start1+16));
        int jsbegin = infoFile.indexOf("\"js\":\"", infoFile.indexOf("assets\":\"")+5);
        html5Player = infoFile.substring(jsbegin+6, infoFile.indexOf("\"", jsbegin+6)).replaceAll("\\\\", "");//Extract the js player url
        infoFile = infoFile.substring(start+29, infoFile.indexOf("\"", start+32))+","+adaptive;
        try{
            infoFile = java.net.URLDecoder.decode(infoFile, "UTF-8");
        }catch(Exception uee){
        }
        links = new HashMap<>(13);
        resolutions = new ArrayList<>(13);
        infoFile = infoFile.replaceAll("(codecs=)(.)+?(\")", "");//remove all occurences of the codec parameter, so as to
        infoFile = infoFile.replaceAll("\\\\u0026", "&");
        //infoFile = infoFile.replaceAll("[^_](type=)(.)+?(;)", "");
        //get rid of the comma in it. We will now be left with only one comma to serve as a separator for splitting the info string
        //into parts which could then be easily manipulated to get a clean and working url.
        String[] requiredParams = null;
//        int stream_map_index = infoFile.indexOf("url_encoded_fmt_stream_map\":\"")+"url_encoded_fmt_stream_map\":\"".length();
//        int adaptive_fmts_index = infoFile.indexOf("adaptive_fmts")+"adaptive_fmts".length();
        String url = "";//This is a String which grows as each url is undergoing processing
//        int startIndex = adaptive_fmts_index < stream_map_index?adaptive_fmts_index:stream_map_index;
//        String streamMapAdaptiveData = infoFile.substring(startIndex);
        //String[] streamMapAdaptiveURLS = streamMapAdaptiveData.split("(,)");//split the streamMapData string into substrings which will contain all parameters of a youtube video url
        String[] streamMapAdaptiveURLS = infoFile.split("(,)");
        String str = "";
        
        for(String URL : streamMapAdaptiveURLS){//take-out each raw url and reconstruct it in the inner for-loop
            URL = URL.replaceAll(" ", "");
            if(!URL.contains("sparams=")){//After splitting the infoFile String into URLs, those without the 'sparams' parameter will clearly
                //Invalid URLs(i.e urls without the sparams parameter), and must be skipped.
                continue;
            }
            requiredParams = URL.split("&");//split the raw URL into it's constituent parameters
            for(String param : requiredParams){
                if((url != null)&&(url.contains(param))){//make sure there aren't duplicates of any parameter
                    continue;
                }
                if(param.startsWith("type=") || param.startsWith("xtags=")){
                    continue;//Skip type and xtags parameters. You could also split the param using '=', and if the array size is less than 2, then the param is an empty one and shouldn't be included
                }
                if((param.startsWith("&s="))||(param.startsWith("s="))){
                    if(decypheringScript.equals("null")){
                        decypheringScript = getDecypherScript("https:"+html5Player);//Get the javascript decyphering.
                    }
                    param = decypherSignature(param.substring(param.indexOf("=")+1), decypheringScript);
                }
                if(param.contains("url=http")){
                    str = url;
                    url = param.substring(4)+str;
                }else{
                    url += "&"+param;
                }
            }
            String res = videoResolution(url);
            if((res == null) || (res.equals("Unknown"))){
                url = "";
                continue;
            }
            if(url != null){
                url = url.replace(";", "");
                url = url.replaceAll("\"", "");
//                url = url+'\n'+'\n'+"<here>"+v;
//                url += AppProperties.HTML5Player;
//                url += AppProperties.script;
                links.put(res, url);
                resolutions.add(res);
            }
            url = "";
        }
    }
            
    private String videoResolution(String url){
        Pattern pattern = Pattern.compile("(itag=)[0-9]+?[&]");
        Matcher match = pattern.matcher(url);
        String itag = "";
        if(match.find()){
            itag = match.group();
            itag = itag.replace("&", "");
        }else{
            return "Unknown";
        }
        String res = "";
        switch(itag){
            case "itag=18":
                res = MP4360;
                break;
            case "itag=82":
                res = MP4360;
                break;
            case "itag=137":
                res = MP41080V;
                break;
            case "itag=85":
                res = MP41080;
                break;
            case "itag=43":
                res = WEBM360;
                break;
            case "itag=100":
                res = WEBM360;
                break;
            case "itag=248":
                res = WEBM1080;
                break;
            case "itag=5":
                res = FLV240;
                break;
            case "itag=22":
                res = MP4720;
                break;
            case "itag=84":
                res = MP4720;
                break;
            case "itag=138":
                res = MP42160;
                break;
            case "itag=266":
                res = MP42160;
                break;
            case "itag=264":
                res = MP41440;
                break;
            case "itag=247":
                res = WEBM720;
                break;
            case "itag=271":
                res = WEBM1440;
                break;
            case "itag=36":
                res = GP240;
                break;
            case "itag=17":
                res = GP144;
                break;
            case "itag=140":
                res = MP4AUDIO;
                break;
            case "itag=141":
                res = MP4AUDIO;
                break;
            case "itag=171":
                res = WEBMAUDIO;
                break;
            default:
                res = null;
        }
        return res;
    }
    
    public String getDecypherScript(String jsUrl){
        tube.api.YouTubeInfoFile yt = new tube.api.YouTubeInfoFile();
        String js = null;
        String sigFunctionName = null;
        String sigFunctionBody = null;
        String param = null;
        String var = null;
        String prototype = null;
        int start = 0;
        int state = 1;
        try{
            js = yt.getPageSource(jsUrl);
        }catch(java.io.IOException ioe){
            
        }
        if(js != null){
            start = js.indexOf("\"signature\",");
            sigFunctionName = js.substring(start+12, js.indexOf("(", start+12));
            int k = js.indexOf(sigFunctionName+"=function");
            if(k == -1){
                k = js.indexOf("function "+sigFunctionName);
            }
            int l = js.indexOf("{", k+1);
            sigFunctionBody = js.substring(k, js.indexOf("}", l)+1);
            System.out.println(sigFunctionBody);
            param = js.substring(js.indexOf("(", k)+1, js.indexOf(")", k));
            String[] funcs = js.substring(l+1, js.indexOf("}", l)).split(";");//split the body into all function calls
            for(String s : funcs){
                if(!s.startsWith(param)){//if the function begins with the name of it's parameter, we will have to ignore
                    var = s.substring(0, s.indexOf("."));//This is the name of the object whose function is being called
                    break;//We don't need to go through all the function calls
                }
            }
            k = js.indexOf("var "+var+"=");
            l = k;
            if(var != null){
                k += var.length()+7;
            }
            for(int g = k; state != 0; k++){
                if(js.charAt(k) == '{'){
                    state++;
                }else if(js.charAt(k) == '}'){
                    state--;
                }
            }
            prototype = js.substring(l, k+1);
        }
        this.sigFunctionName = sigFunctionName;
//        System.out.println(prototype+sigFunctionBody+";");
        return prototype+sigFunctionBody+";";
    }
    
    String getSigFunctionName(){
        return sigFunctionName;
    }
    
    public String decypherSignature(String sig, String decypherScript){
        String cypheredSignature = null;
//      String script = "var es={UA:function(a,b){a.splice(0,b)},cF:function(a){a.reverse()},xJ:function(a,b){var c=a[0];a[0]=a[b%a.length];a[b]=c}};function fs(a){a=a.split(\"\");es.cF(a,56);es.UA(a,1);es.xJ(a,14);return a.join(\"\")};";
        javax.script.ScriptEngineManager engineManager = new javax.script.ScriptEngineManager();
        javax.script.ScriptEngine engine = engineManager.getEngineByName("nashorn");
        engine = (engine != null)?engine:engineManager.getEngineByName("rhino");
        engine.put("signature", sig);
        try{
            cypheredSignature = "signature="+(String)engine.eval(decypherScript+sigFunctionName+"(signature)");
        }catch(Exception e){
            e.printStackTrace();
            System.out.println(e.toString());
        }
        return cypheredSignature;
    }   
}
