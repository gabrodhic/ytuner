package com.protogab.ytuner.util;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;




public class YTubeVideoLib
{
 


 public YTubeVideoLib()
 {
 }

 static String DownloadPage(String s)
 {
	 String s1;
	 s1 = null;//CHANGED
	 try
     {
		 
	     HttpGet httpget;
	     DefaultHttpClient defaulthttpclient;
	    
	     httpget = new HttpGet(s);
	     httpget.setHeader("User-Agent", "Mozilla/5.0");
	     
	     HttpParams httpParameters = new BasicHttpParams();
		  // Set the timeout in milliseconds until a connection is established.
		  // The default value is zero, that means the timeout is not used. 
		  int timeoutConnection = 3000;
		  HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		  // Set the default socket timeout (SO_TIMEOUT) 
		  // in milliseconds which is the timeout for waiting for data.
		  int timeoutSocket = 6000;
		  HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
	     
	     
	     defaulthttpclient = new DefaultHttpClient(httpParameters);
	     HttpEntity httpentity;
	     HttpResponse httpresponse = defaulthttpclient.execute(httpget);
	     //Log.d("Name", s);
	     //Log.d("Name", String.valueOf(httpresponse.getStatusLine().getStatusCode()));
	     if (httpresponse.getStatusLine().getStatusCode() != 200)
	     {
	         return null;
	     }
	     httpentity = httpresponse.getEntity();
	     if (httpentity != null)
	     {
	         try
	         {
	             InputStream inputstream = httpentity.getContent();
	             s1 = convertToString(inputstream);
	             inputstream.close();
	         }
	         catch (Exception exception)
	         {
	             return s1;
	         }
	     }
	     
     } catch (Exception exception)     {
         return s1;
     }
	 return s1;
 }

 static String DownloadPageipad(String s)
 {
	 String s1;
	 s1 = null;//CHANGED
	 try
     {
	    
	     HttpGet httpget;
	     DefaultHttpClient defaulthttpclient;
	     
	     httpget = new HttpGet(s);
	     httpget.setHeader("User-Agent", "Mozilla/5.0");
	     
	     HttpParams httpParameters = new BasicHttpParams();
		  // Set the timeout in milliseconds until a connection is established.
		  // The default value is zero, that means the timeout is not used. 
		  int timeoutConnection = 3000;
		  HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		  // Set the default socket timeout (SO_TIMEOUT) 
		  // in milliseconds which is the timeout for waiting for data.
		  int timeoutSocket = 6000;
		  HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		  
	     defaulthttpclient = new DefaultHttpClient(httpParameters);
	     HttpEntity httpentity;
	     HttpResponse httpresponse = defaulthttpclient.execute(httpget);
     
	     //Log.d("Name", s);
	     //Log.d("Name", String.valueOf(httpresponse.getStatusLine().getStatusCode()));
	     if (httpresponse.getStatusLine().getStatusCode() != 200)
	     {
	         return null;
	     }
	     httpentity = httpresponse.getEntity();
	     if (httpentity != null)
	     {
	         try
	         {
	             InputStream inputstream = httpentity.getContent();
	             s1 = convertToString(inputstream);
	             inputstream.close();
	         }
	         catch (Exception exception)
	         {
	             return s1;
	         }
	     }
	     
	     
     } catch (Exception exception)     {
         return s1;
     }
	 return s1;
 }
 
 static String GetBwtTex(String s, String s1, String s2)
 {
     long l = s.indexOf(s1);
     if (l == -1L)
     {
         return "";
     }
     long l1 = l + (long)s1.length();
     long l2 = s.indexOf(s2, (int)l1);
     if (l2 == -1L)
     {
         return s.substring((int)l1);
     } else
     {
         return s.substring((int)l1, (int)l2);
     }
 }
 
 

 protected static List DownloadWebPagef(String s)
 {
     ArrayList arraylist = new ArrayList();
     Map map;
     String data = DownloadPage(s);//ADDED
     if(data==null) return null;//ADDED
     map = parseQueryString(data);
     if (map.get("reason") == null)
     {
    	 String as[] = URLDecoder.decode((String)map.get("url_encoded_fmt_stream_map")).split(",");
         String s1 = "";
         int i = as.length;
         int j = 0;
         while (j < i){


    	     Map map1;
    	     String s2;
    	     String s3;
    	     map1 = parseQueryString(as[j]);
    	     s2 = URLDecoder.decode((String)map1.get("type"));
    	     s3 = URLDecoder.decode((String)map1.get("url"));
    	     if (s1.equals(""))
    	     {
    	         s1 = GetBwtTex(s3, "", "?");
    	     }
    	     if (s3.contains("signature")){
    	    	 
    	    	 
    	     }else{
    	 	     if (map1.get("sig") == null){
    	 	    	try
	       		     {
	       		         return DownloadWebPagefex(s, URLDecoder.decode((String)map.get("title")), (String)map.get("length_seconds"));
	       		     }
	       		     catch (Exception exception)
	       		     {
	       		         exception.printStackTrace();
	       		     }
	       	    	 break;
        	     }else{
        	    	 StringBuilder stringbuilder3 = new StringBuilder(String.valueOf(s3));
        	         s3 = stringbuilder3.append("&signature=").append(URLDecoder.decode((String)map1.get("sig"))).toString();

        	     }
    	    	 
    	     }
    	     String s4 = s3;
	         YTubeVideoItem youtubeitem;
	         StringBuilder stringbuilder = new StringBuilder(String.valueOf(s3));


	         String s5 = stringbuilder.append("&type=").append(s2).toString();
	         String s6 = URLDecoder.decode((String)map.get("title"));
	         StringBuilder stringbuilder1 = new StringBuilder(String.valueOf(s5));
	         stringbuilder1.append("&title=").append(s6).toString();
	         youtubeitem = new YTubeVideoItem();
	         youtubeitem.VedioTitle = s6;
	         StringBuilder stringbuilder2 = new StringBuilder(String.valueOf((String)map1.get("quality")));
	         stringbuilder2.append(" - ").append(s2.split(";")[0].split("/")[1]).toString();
	         youtubeitem.DownloadUrl = s4;
	         youtubeitem.VedioLeantgh = getTimeFormat(Long.parseLong((String)map.get("length_seconds")));

    	
    	     arraylist.add(youtubeitem);
    	     j++;
       
         
     	}
         return arraylist;
     }

     if (map.get("reason") != null){
	     if (((String)map.get("reason")).contains("restricted"))
	     {
	         return DownloadWebPagef((new StringBuilder(String.valueOf(s))).append("&el=detailpage&ps=default&eurl=&gl=US&hl=en").toString());
	     }
	     /*else{

	    	 return null;
	     }*/
     }
     String s7;
     String s8;
     s7 = "";
     s8 = "0";
     if (map.get("title") != null)
     {
         s7 = URLDecoder.decode((String)map.get("title"));
         s8 = (String)map.get("length_seconds");
         
     }
     return DownloadWebPagefex(s, s7, s8); //     ///* CHFECK LINEEEEEEEEEEEEEEEEEEE ****************
     
     //return arraylist;
     
 }

 static List DownloadWebPagefex(String s, String s1, String s2)
 {
     ArrayList arraylist = new ArrayList();
     String as[];
     int i;		String obfusC1 = "http://m.youtube.com/";  String obfusC2 = "watch?v=";	String obfusC3 = "url_encoded_fmt_stream_map";  String obfusC4 = "=(.+?)\\\\u0026";  String obfusC5=obfusC3+obfusC4;
     String s3 = DownloadPageipad((new StringBuilder(obfusC1+obfusC2)).append(GetId(s)).append("&desktop_uri=%2Fwatch%3Fv%3D").append(GetId(s)).toString());
     if(s3==null) return null;//ADDED
     Matcher matcher = Pattern.compile(obfusC5).matcher(s3);
     matcher.find();
     //if(matcher.groupCount()==0) return null;
     String toMatch = null;
     try {
    	 toMatch = matcher.group(1).toString();
	} catch (Exception e) {
		return null;
	}
     as = URLDecoder.decode(toMatch).split(",");
     i = as.length-1;
     int j = 0;
     while (j <= i){

	    Map map;
	    String s4;
	    String s5;
	    map = parseQueryString(as[j]);
	    //CHANGED s4 = URLDecoder.decode((String)map.get("type"));
	    s5 = URLDecoder.decode((String)map.get("url"));
	    
										     
		if (s5.contains("signature")){
			
			
		}else{
			if (map.get("sig") == null){
				return null;//END HERE
				 /*try
			     {
			         return DownloadWebPagefex(s, s1, s2);
			     }
			     catch (Exception exception)
			     {
			         exception.printStackTrace();
			     }
			     break; */
			}else{
				s5 = (new StringBuilder(String.valueOf(s5))).append("&signature=").append(URLDecoder.decode((String)map.get("sig"))).toString();
			}
		}	
		
		YTubeVideoItem youtubeitem;
		//CHANGED String s6 = (new StringBuilder(String.valueOf(s5))).append("&type=").append(s4).toString();
	     youtubeitem = new YTubeVideoItem();
	     youtubeitem.VedioTitle = s1;
	     StringBuilder stringbuilder = new StringBuilder(String.valueOf((String)map.get("quality")));
	   //CHANGED stringbuilder.append(" - ").append(s4.split(";")[0].split("/")[1]).toString();
	     s5 = s5.replace("\\", "");//ADDED. by some reason youtube url gets a trailing backslash
	     youtubeitem.DownloadUrl = s5;//CAHNGED s6;
	     youtubeitem.VedioLeantgh = getTimeFormat(Long.parseLong(s2));
	
	     arraylist.add(youtubeitem);
	     j++;
	     continue; /* Loop/switch isn't completed */
		     
			   
     }

     return arraylist;
 }

 static String GetId(String s)
 {
     return s.substring(10 + s.indexOf("?video_id="));
 }

 static String convertToString(InputStream inputstream)
 {
	 StringBuffer stringbuffer = new StringBuffer();
	 try { 
     
	     BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));
	     BufferedReader bufferedreader1 = bufferedreader;
	     String s ;
	     while ((s = bufferedreader1.readLine()) != null) {
	    	 stringbuffer.append(s);
	     }
	 }catch(Exception ex){
		 return null;
	 }
     return stringbuffer.toString();
     
     
     
     
 }

 static Map parseQueryString(String s)
 {
     HashMap hashmap = new HashMap();
     String as[] = s.split("&");
     int i = 0;
     do
     {
         if (i >= as.length)
         {
             return hashmap;
         }
         String as1[] = as[i].split("=");
         if (as1.length == 2)
         {
             hashmap.put(as1[0], as1[1]);
         }
         i++;
     } while (true);
 }

 static String getTimeFormat(long l)
 {
     if (l / 3600L > 0L)
     {
         Object aobj1[] = new Object[3];
         aobj1[0] = Long.valueOf((long)Math.ceil(l / 3600L));
         aobj1[1] = Long.valueOf((long)Math.ceil((l % 3600L) / 60L));
         aobj1[2] = Long.valueOf(l % 60L);
         return String.format("%02d:%02d:%02d", aobj1);
     } else
     {
         Object aobj[] = new Object[2];
         aobj[0] = Long.valueOf((long)Math.ceil(l / 60L));
         aobj[1] = Long.valueOf(l % 60L);
         return String.format("%02d:%02d", aobj);
     }
 }

 Long getsize(String s)
 {
	 int i = 0;
	 try
     {
	    
	     HttpResponse httpresponse;
	     int j;
	     HttpGet httpget = new HttpGet(s);
	     httpget.setHeader("User-Agent", "Mozilla/5.0");
	     httpget.setHeader("Accept", "*/*");
	     
	     
	     HttpParams httpParameters = new BasicHttpParams();
		  // Set the timeout in milliseconds until a connection is established.
		  // The default value is zero, that means the timeout is not used. 
		  int timeoutConnection = 3000;
		  HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		  // Set the default socket timeout (SO_TIMEOUT) 
		  // in milliseconds which is the timeout for waiting for data.
		  int timeoutSocket = 6000;
		  HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
	     
	     
	     DefaultHttpClient defaulthttpclient = new DefaultHttpClient(httpParameters);
	     //Log.d("ErrorWeb", "Start");
	     httpresponse = defaulthttpclient.execute(httpget);
	     //Log.d("ErrorWeb", "Con1");
	     //Log.d("ErrorWeb", String.valueOf(httpresponse.getStatusLine().getStatusCode()));
	     j = httpresponse.getStatusLine().getStatusCode();
	     i = 0;
	     if (j == 200)
	     {
	         try
	         {
	             i = Integer.valueOf(httpresponse.getFirstHeader("Content-Length").getValue()).intValue();
	             //Log.d("ErrorWeb", (new StringBuilder("Response content size = ")).append(i).toString());
	         }
	         catch (Exception exception)
	         {
	             //Log.d("ErrorWeb", exception.toString());
	         }
	     }
     }catch (Exception exception)
        {
    	 
        }
     return Long.valueOf(i);
 }

 Long[] getsize(String as[])
 {
     ArrayList arraylist = new ArrayList();
     int i = as.length;
     int j = 0;
     do
     {
         if (j >= i)
         {
             return (Long[])arraylist.toArray();
         }
         String s = as[j];
         try
         {
             URLConnection urlconnection = (new URL(s)).openConnection();
             urlconnection.setConnectTimeout(6000);
             InputStream inputstream = urlconnection.getInputStream();
             //Log.d("ErrorWeb", "Response content size = ####");
             int k = inputstream.available();
             arraylist.add(Long.valueOf(k));
             //Log.d("ErrorWeb", "Response content s323ize = ####");
             //Log.d("ErrorWeb", (new StringBuilder("Response content size = ")).append(k).toString());
         }
         catch (IOException ioexception) { }
         j++;
     } while (true);
 }

}

