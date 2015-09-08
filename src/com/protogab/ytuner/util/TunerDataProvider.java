package com.protogab.ytuner.util;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;


import android.text.TextUtils;
import android.util.Log;


import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.protogab.ytuner.BuildConfig;
import com.protogab.ytuner.MainActivity;
import com.protogab.ytuner.R;

public class TunerDataProvider {
	
	
	 
	  /** Used to know if the thread has finished **/
	  public boolean finishedProcessing = false;
	  /** Used to control(usually form outside this class) and stop any thread that is downloading information about video(s) **/
	  public boolean stopTuning = false;
	  /** Used to control(usually form outside this class) and pause any thread that is downloading information about video(s) **/
	  public boolean pauseTuning = false;
	  
	  public static final String RANDOM_CHANNEL= "RANDOM";
	  public List<String> vidsHistory = new ArrayList<String>();
	  
	  public String slectedChannelsIds = "";
	  
	  
	  public List<YTunerVideoItem> yTunervideoItemList = new ArrayList<YTunerVideoItem>();
	  public int totalTuningVidItems = 0;
	  
	  public static String[] contentSources = {"http://protogab.com","http://protogab.com"};
	  int sourceNumber = 0;
	  public YTunerListingObject channelListing;
	  LinkedHashMap<String,ArrayList> channelsVideos = new LinkedHashMap<String,ArrayList>();
	  
	  public boolean channelListingConnectionError = false;
	  public boolean getTunerVideosConnectionError = false;
	  
	  
	  public TunerDataProvider(){
		  
		 
	  }
	  
	  public void init(){
		  	
		  
	  }
	  
	  
		/*
		 * The first validation is to check that we have DNS resolving.
		 * There is no way to control the timeout of DNS lookup, so we use this method.
		 * Note that on any network call there is a DNS lookup(of cource if it is a domain name not an ip)
		 * http://stackoverflow.com/questions/18217335/can-i-set-the-getaddrinfo-timeout-in-android-for-defaulthttpclient
		 */
		  private boolean testDNS(String hostname) {
			  try
			  {
			    DNSResolver dnsRes = new DNSResolver(hostname);
			    Thread t = new Thread(dnsRes);
			    t.start();
			    t.join(2000);//Only wait for 1 second
			    InetAddress inetAddr = dnsRes.get();            
			    return inetAddr != null;
			  }
			  catch(Exception e)
			  { 
			    return false;
			  }
			}
	  
	  
	  /*
	   * Gets the listing about each channel on the ytuner programming.
	   * 
	   */	  
	  public void getChannelListng(final String language, final String country, final String locale){
		  
		  stopTuning = true;//Stop tuning if there is a thread running gettingVideosTHIS MUST BE FIRST
		  yTunervideoItemList.clear();
		  yTunervideoItemList  = new ArrayList<YTunerVideoItem>();
		  	
		  sourceNumber = (sourceNumber == contentSources.length-1) ? 0 : sourceNumber+1;
		  
		  
		  Thread thread = new Thread(new Runnable(){			  			  	  
			    @Override
			    public void run() {			         
			  		  try {			
			  			  
			  			//This is needed before we enter in the thread, for some cases
			  			  if( testDNS("google.com") == false) {
			  				  channelListingConnectionError = true;
			  				  return;
			  			  }

			  			Log.d(MainActivity.DBG_TAG,"Before listing request");
						  String url = contentSources[sourceNumber]+"/ytuner/tuner/listing2?language="+language+"&country="+country+"&locale="+locale;
						  HttpGet httpGet = new HttpGet(url);
						  HttpParams httpParameters = new BasicHttpParams();
						  // Set the timeout in milliseconds until a connection is established.
						  // The default value is zero, that means the timeout is not used. 
						  int timeoutConnection = 8000;//We set it a bit high because it could be on edge/3g
						  HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
						  // Set the default socket timeout (SO_TIMEOUT) 
						  // in milliseconds which is the timeout for waiting for data.
						  int timeoutSocket = 10000;//We set it a bit high because it could be on edge/3g
						  HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

						  DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
						  //Create a custom response handler
				          ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
				                public String handleResponse(final HttpResponse response) {
			                    	int status = response.getStatusLine().getStatusCode();
				                    if (status >= 200 && status < 300) {
				                        HttpEntity entity = response.getEntity();
				                        try{
				                        	return entity != null ? EntityUtils.toString(entity) : null;
				                        }catch (Exception e){
				                        	//Lets, retry				                      
				                        	channelListingConnectionError = true;
				                        	e.printStackTrace();
				                        }
				                        
				                    } else {
				                    	//Lets, retry				                      
				                    	channelListingConnectionError =true;
				                        //throw new ClientProtocolException("Unexpected response status: " + status);
				                    }
				                    return null;
				                }
				            };
						  						  
						  //HttpResponse response = httpClient.execute(httpGet);
						  String responseString = httpClient.execute(httpGet, responseHandler);
						  if(responseString==null)return;
						  
						  Log.d(MainActivity.DBG_TAG,"RESPONSE STRING " + responseString);
						  //TODO:Validate is a json response
						  //IF IS VALID JSON RESPONSE THEN:
						  
						  	
							Gson mygson = new Gson();
							channelListing = mygson.fromJson(responseString,YTunerListingObject.class);
							Log.d(MainActivity.DBG_TAG,"listing: "+channelListing.test);
							
							Log.d(MainActivity.DBG_TAG,"After listing request");

			  		  } 
			  		  catch (Throwable t) {
			  			//Lets, retry				                      
			  			channelListingConnectionError = true;
			  			t.printStackTrace();
			  		  }
				}
			});
			thread.start(); 
		  
	  }
	  
	  
	  /*
	   * NOTE: Although this returns a YTunerVideoItem, it is used to get the details of a channel
	   */
	  private YTunerVideoItem getDetailsChannelID(int channel_id){
		  
		  Iterator it = channelListing.categories.iterator();										
			while (it.hasNext()) {								
				YTunerListingObject.Category arrCatDetails = (YTunerListingObject.Category) it.next();
				ArrayList<YTunerListingObject.Channel> arrChannels = (ArrayList) arrCatDetails.channels;
				//int category_id = arrCatDetails.get("channels")category_id													
				Iterator it2 = arrChannels.iterator();
				while (it2.hasNext()) {
					YTunerListingObject.Channel arrChannDetails = (YTunerListingObject.Channel)  it2.next();
					if(arrChannDetails.channel_id == Integer.valueOf(channel_id)){
						
						 return new YTunerVideoItem(""
						      		,arrCatDetails.category_id
						      		,arrCatDetails.category_name
						      		,arrCatDetails.category_number												      		
						      		,arrChannDetails.channel_id
						      		,arrChannDetails.channel_name.toString()
						      		,arrChannDetails.channel_number.toString()
						      		,arrChannDetails.channel_subnumber
								 	,""
						      		,""
						      		,""
						      		,""
									,new ArrayList()
							);
						
					}
				}
			}
			
		  
		  return  null;
	  }
	  
	  
	  private YTunerVideoItem getCategoryNextVideoItem(YTunerVideoItem currentVideoItem, ArrayList<String> arrcurrentVidIds){
			 //Get the number of categories
			 int num_categories = channelListing.categories.size();
			 int hieghest_catNumber = channelListing.categories.get(channelListing.categories.size()-1).category_number;
			 int lowest_catNumber = channelListing.categories.get(0).category_number;
			 
			 //Iterate throud all categories
	          for (int i = 0; i <= channelListing.categories.size()-1; i++)
	          {
	        	  Log.d(MainActivity.DBG_TAG, "FOOR CATS" + i);
					 if(channelListing.categories.get(i).category_number > currentVideoItem.category_number || (channelListing.categories.get(i).category_number == lowest_catNumber && hieghest_catNumber == currentVideoItem.category_number) ){
						 //WARNING: notice New Array.....(this way we copy and dont just make a reference, since we are goind to do a shuffle bellow
						 //http://stackoverflow.com/questions/15292980/java-arraylist-pass-by-reference
						 List<YTunerListingObject.Channel> itChannels = new ArrayList<YTunerListingObject.Channel>( (ArrayList<YTunerListingObject.Channel>) channelListing.categories.get(i).channels);
								 
						 //Collections.shuffle(itChannels);//Allready shuffled server-side
						 Iterator itC = itChannels.iterator();
						 while (itC.hasNext()) {
							 //Log.d(MainActivity.DBG_TAG, "WHILE CHANNELS");
							 YTunerListingObject.Channel ytunerChann = (YTunerListingObject.Channel) itC.next();							
							 List<String> itChannVids = (ArrayList<String>) channelsVideos.get(String.valueOf(ytunerChann.channel_id));
							 
							 if(itChannVids == null) continue;//It can be null since although a channel can be in the listing guide it might not be on the channels response(since the might be no vidoes for that channel)
							 
							 Log.d(MainActivity.DBG_TAG, "CHANN ID -- "+ytunerChann.channel_id);
							// Log.d(MainActivity.DBG_TAG, "CHANN SIZE -- "+channelsVideos.size());
							 Log.d(MainActivity.DBG_TAG, "CHANN2 SIZE -- "+itChannVids.size());
							 
							 //Now lets find a video not added yet
							 Iterator it = itChannVids.iterator();
							 while (it.hasNext()) {
								 Log.d(MainActivity.DBG_TAG, "WHILE VIDS");
								 String itVidId = it.next().toString(); 
								 if(! arrcurrentVidIds.contains( itVidId )){
									 
									 //Now lets try to find a video not watched in user's history. If all videos are already watched then the loop will give chance to other channel in category.									
										return new YTunerVideoItem(itVidId
									      		,channelListing.categories.get(i).category_id
									      		,channelListing.categories.get(i).category_name
									      		,channelListing.categories.get(i).category_number												      		
									      		,ytunerChann.channel_id
									      		,ytunerChann.channel_name.toString()
									      		,ytunerChann.channel_number.toString()
									      		,ytunerChann.channel_subnumber
											 	,""
									      		,""
									      		,""
									      		,""
												,new ArrayList()
										);	  
									
											  										    
										
									 
								 }
							 }	
							 
						 }
				
					 }
	          }
			 
			 /*
			 //lets access the channel list randomly, to give fair chances to all videos. //http://stackoverflow.com/questions/6017338/how-do-you-shuffle-elements-in-a-map
			 List keys = new ArrayList(channelsVideos.keySet());
			 Collections.shuffle(keys);
			 for (Object o : keys) {
			     // Access keys/values in a random order				 
				
				 YTunerVideoItem itChanDetails = getDetailsChannelID(Integer.valueOf(o.toString()));
				 if(itChanDetails.category_number > currentVideoItem.category_number || (itChanDetails.category_number == 1 && hieghest_catNumber == currentVideoItem.category_number) ){
					 List<String> itChannVids = (ArrayList<String>) channelsVideos.get(o);
					 //Now lets find a video not added yet
					 Iterator it = itChannVids.iterator();
					 while (it.hasNext()) {
						 if(! arrcurrentVidIds.contains( it.next().toString() )){
							 return new YTunerVideoItem(it.next().toString()
							      		,itChanDetails.category_id
							      		,itChanDetails.category_name
							      		,itChanDetails.category_number												      		
							      		,itChanDetails.channel_id
							      		,itChanDetails.channel_name.toString()
							      		,itChanDetails.channel_number.toString()
							      		,itChanDetails.channel_subnumber
									 	,""
							      		,""
							      		,""
							      		,""
										,new ArrayList()
								);
						 }
					 }					 
					 
				 }
			 }
			 */
			 
			 /*Iterator it = channelsVideos.values().iterator();
			 int lastCahnnelID = 0;
			 int lastCategoryNumber = 0;
			 while (it.hasNext()) {	
				 Map.Entry pairs = (Map.Entry)it.next();
				 YTunerVideoItem itChanDetails = getDetailsChannelID(Integer.valueOf(pairs.getKey().toString()));
				 if(itChanDetails.category_number > currentVideoItem.category_number || (hieghest_catNumber == currentVideoItem.category_number) ){
					 List<String> vids = (ArrayList<String>) pairs.getValue();
					 //Now lets find a video not added yet
					 arrVidIdsInit.add(vids.get(1).toString());//NOTE (1)
				 }
				 
				 
			 }*/
	          Log.d(MainActivity.DBG_TAG, "RETURN NULL");
			 return null;
	  }
	  
	  
	  private YTunerVideoItem getSubCategoryNextVideoItem(YTunerVideoItem currentVideoItem, ArrayList<String> arrcurrentVidIds){
			 //Get the number of categories
			 int num_categories = channelListing.categories.size();
			 int hieghest_catNumber = channelListing.categories.get(channelListing.categories.size()-1).category_number;
			 
			 //Iterate through all categories
	          for (int i = 0; i <= channelListing.categories.size()-1; i++)
	          {
	        	  Log.d(MainActivity.DBG_TAG, "FOOR CATS SUB" + i);
					 if(channelListing.categories.get(i).category_number == currentVideoItem.category_number ){
						 //WARNING: notice New Array.....(this way we copy and dont just make a reference, since we are goind to do a shuffle bellow
						 //http://stackoverflow.com/questions/15292980/java-arraylist-pass-by-reference
						 List<YTunerListingObject.Channel> itChannels = new ArrayList<YTunerListingObject.Channel>((ArrayList<YTunerListingObject.Channel>) channelListing.categories.get(i).channels);
								 
						 int hieghest_catSubNumber = channelListing.categories.get(i).channels.get(channelListing.categories.get(i).channels.size()-1).channel_subnumber;
						 int lowest_catSubNumber = channelListing.categories.get(i).channels.get(0).channel_subnumber;
						 String chan_name = channelListing.categories.get(i).channels.get( 0 ).channel_name;
						 
						 Log.d(MainActivity.DBG_TAG, "SIZE AND VALUE " + channelListing.categories.get(i).channels.size() +"/"+ i);
						 //Collections.shuffle(itChannels);
						 Iterator itC = itChannels.iterator();
						 while (itC.hasNext()) {
							
							 
							 YTunerListingObject.Channel ytunerChann = (YTunerListingObject.Channel) itC.next();
							 Log.d(MainActivity.DBG_TAG, "WHILE CHANNELS SUB " + ytunerChann.channel_subnumber+ "/"+currentVideoItem.channel_subnumber +"/"+hieghest_catSubNumber+"/"+chan_name);
							 if(ytunerChann.channel_subnumber > currentVideoItem.channel_subnumber || (ytunerChann.channel_subnumber == lowest_catSubNumber && hieghest_catSubNumber == currentVideoItem.channel_subnumber) ){
								 List<String> itChannVids = (ArrayList<String>) channelsVideos.get(String.valueOf(ytunerChann.channel_id));
								 
								 Log.d(MainActivity.DBG_TAG, "CHANN ID -- "+ytunerChann.channel_id);
								 
								 if(itChannVids == null) continue;//It can be null since although a channel can be in the listing guide it might not be on the channels response(since the might be no vidoes for that channel)
								 
								 
								// Log.d(MainActivity.DBG_TAG, "CHANN SIZE -- "+channelsVideos.size());
								 Log.d(MainActivity.DBG_TAG, "CHANN2 SIZE -- "+itChannVids.size());
								 
								 //Now lets find a video not added yet
								 Iterator it = itChannVids.iterator();
								 while (it.hasNext()) {
									 Log.d(MainActivity.DBG_TAG, "WHILE VIDS SUB");
									 String itVidId = it.next().toString(); 
									 if(! arrcurrentVidIds.contains( itVidId ) || it.hasNext()==false){//if is the last one it means theres nothing more to search, so return it
										 										
											Log.d(MainActivity.DBG_TAG, "RETURN SUB");
											return new YTunerVideoItem(itVidId
										      		,channelListing.categories.get(i).category_id
										      		,channelListing.categories.get(i).category_name
										      		,channelListing.categories.get(i).category_number												      		
										      		,ytunerChann.channel_id
										      		,ytunerChann.channel_name.toString()
										      		,ytunerChann.channel_number.toString()
										      		,ytunerChann.channel_subnumber
												 	,""
										      		,""
										      		,""
										      		,""
													,new ArrayList()
											);	  
										
									 }
								 }
							 
							 
							} 
						 }
						 
					 }
	          }
			 
			 
	          Log.d(MainActivity.DBG_TAG, "RETURN NULL SUB");
			 return null;
	  }
	  
	  
	  
	  private YTunerVideoItem getCategoryPreviousVideoItem(YTunerVideoItem currentVideoItem, ArrayList<String> arrcurrentVidIds){
			 //Get the number of categories
			 int num_categories = channelListing.categories.size();
			 int hieghest_catNumber = channelListing.categories.get(channelListing.categories.size()-1).category_number;
			 
			 //Iterate throud all categories
	          for (int i = channelListing.categories.size()-1; i>=0; i--)
	          {
	     		 
					 if(channelListing.categories.get(i).category_number < currentVideoItem.category_number || (channelListing.categories.get(i).category_number == hieghest_catNumber && currentVideoItem.category_number==1) ){
						 List<YTunerListingObject.Channel> itChannels = (ArrayList<YTunerListingObject.Channel>) channelListing.categories.get(i).channels;
								 
						 Collections.shuffle(itChannels);
						 Iterator itC = itChannels.iterator();
						 while (itC.hasNext()) {
							 YTunerListingObject.Channel ytunerChann = (YTunerListingObject.Channel) itC.next();							
							 List<String> itChannVids = (ArrayList<String>) channelsVideos.get(String.valueOf(ytunerChann.channel_id));
							 
							 if(itChannVids == null) continue;//It can be null since although a channel can be in the listing guide it might not be on the channels response(since the might be no vidoes for that channel)
							 
							 Log.d(MainActivity.DBG_TAG, "CHANN ID -- "+ytunerChann.channel_id);
							 Log.d(MainActivity.DBG_TAG, "CHANN SIZE -- "+channelsVideos.size());
							 Log.d(MainActivity.DBG_TAG, "CHANN2 SIZE -- "+itChannVids.size());
							 
							 //Now lets find a video not added yet
							 Iterator it = itChannVids.iterator();
							 while (it.hasNext()) {
								 if(! arrcurrentVidIds.contains( it.next().toString() )){
									 return new YTunerVideoItem(it.next().toString()
									      		,channelListing.categories.get(i).category_id
									      		,channelListing.categories.get(i).category_name
									      		,channelListing.categories.get(i).category_number												      		
									      		,ytunerChann.channel_id
									      		,ytunerChann.channel_name.toString()
									      		,ytunerChann.channel_number.toString()
									      		,ytunerChann.channel_subnumber
											 	,""
									      		,""
									      		,""
									      		,""
												,new ArrayList()
										);
								 }
							 }	
							 
						 }

					 }
	          }
			 
			 
			 return null;
	  }
	  
	  /*
	   * Gets video from official ytuner service in the tv channel format according received parameters.
	   * 
	   */	  
	  public void getTunerVideos(final String language, final String country, final String locale){
		  
		  stopTuning = true;//Stop tuning if there is a thread running gettingVideosTHIS MUST BE FIRST
		  yTunervideoItemList.clear();
		  yTunervideoItemList  = new ArrayList<YTunerVideoItem>();		  		  
		  sourceNumber = (sourceNumber == contentSources.length-1) ? 0 : sourceNumber+1;
		  
		  
		  Thread thread = new Thread(new Runnable(){			  			  	  
			    @Override
			    public void run() {			         
			  		  try {				
			  			
			  			//We should not do anything untile the listing thread has finished populating the categories on the listing gui
			  			while(channelListing == null){
			  				//do nothing
			  				if(channelListingConnectionError==true) return;
			  			}
			  			  
			  			Log.d(MainActivity.DBG_TAG,"Before ytuning2 request");
			  			String url = contentSources[sourceNumber]+"/ytuner/tuner/ytuning5";
			  			
			  			
						  //HttpGet httpGet = new HttpGet(url);
						  HttpPost httpPost = new HttpPost(url);
						  List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
						  
						  
						  Log.d(MainActivity.DBG_TAG,"SELECTED CHANN IDS " + slectedChannelsIds);
						  
					      nameValuePairs.add(new BasicNameValuePair("language", language));
						  nameValuePairs.add(new BasicNameValuePair("country", country));
						  nameValuePairs.add(new BasicNameValuePair("locale", locale));	
						  nameValuePairs.add(new BasicNameValuePair("channel_ids", slectedChannelsIds));
						  if(BuildConfig.DEBUG)
							  nameValuePairs.add(new BasicNameValuePair("history", "debug"));
						  else
							  nameValuePairs.add(new BasicNameValuePair("history", TextUtils.join(",", vidsHistory)));
						  
					      httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					      
						  HttpParams httpParameters = new BasicHttpParams();
						  // Set the timeout in milliseconds until a connection is established.
						  // The default value is zero, that means the timeout is not used. 
						  int timeoutConnection = 8000;//We set it a bit high because it could be on edge/3g
						  HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
						  // Set the default socket timeout (SO_TIMEOUT) 
						  // in milliseconds which is the timeout for waiting for data.
						  int timeoutSocket = 10000;//We set it a bit high because it could be on edge/3g
						  HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

						  DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
						  //Create a custom response handler
				          ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
				                public String handleResponse(final HttpResponse response) {
			                    	int status = response.getStatusLine().getStatusCode();
				                    if (status >= 200 && status < 300) {
				                        HttpEntity entity = response.getEntity();
				                        try{
				                        	return entity != null ? EntityUtils.toString(entity) : null;
				                        }catch (Exception e){
				                        	//Lets, retry				   
				                        	getTunerVideosConnectionError = true;								  			
								  			e.printStackTrace();
				                        }
				                        
				                    } else {
				                    	//Lets, retry				                      
				                    	getTunerVideosConnectionError = true;
				                        //throw new ClientProtocolException("Unexpected response status: " + status);
				                    }
				                    return null;
				                }
				            };
						  						  
						  //HttpResponse response = httpClient.execute(httpGet);
						  String responseString = httpClient.execute(httpPost, responseHandler);
						  if(responseString==null) return;
						  
						  Log.d(MainActivity.DBG_TAG,"RESPONSE STRING " + responseString);
						  
						  Log.d(MainActivity.DBG_TAG,"After ytuning2 request");
						  
						  //TODO:Validate is a json response
						  //IF IS VALID JSON RESPONSE THEN:
						  
						  	//JsonParserFactory factory=JsonParserFactory.getInstance();
							//JSONParser parser=factory.newJsonParser();
							//RESPONSE FORMAT (channel_id):[vid,vid,vid,vid], (channel_id):[vid,vid,vid,vid]
							Gson mygson = new Gson();
							channelsVideos = mygson.fromJson(responseString,LinkedHashMap.class);
							//channelsVideos= (HashMap) parser.parseJson(responseString);
							int totalChannels =  channelsVideos.values().size();//It can be only one if it is on channel mode
							
							Log.d(MainActivity.DBG_TAG,"CAHNEL VIDEOS " + totalChannels);
							Log.d(MainActivity.DBG_TAG,"TACKER 1");
							/*int youtubeMaxItems = 50;//Maximum  number of items youtube returns on any API Response
							
							int videosByChannel;
							if(totalChannels ==1){
								videosByChannel = youtubeMaxItems;
							}else{
								int videosByChannel = totalChannels / youtubeMaxItems;
							}*/
							
							
						
							ArrayList<String> arrVidIds = new ArrayList<String>();
							ArrayList<YTunerVideoItem> arrVidsItems = new ArrayList<YTunerVideoItem>();
							String videoIds = "";	
							Log.d(MainActivity.DBG_TAG,"PROG 1");
	
							//Get the total number of vidIds in the response
							int totalVidsinResponse = responseString.length() - responseString.replace(",", "").length() +1;//(each item is separated by ,)
							
							//If there are no enough videos, diselect current selection of channels and select them all
							if(totalVidsinResponse < 5){
								slectedChannelsIds = "";
								getTunerVideos(language, country, locale);
								return;
							}
							
							Log.d(MainActivity.DBG_TAG,"PROG 3");
							
								
								
								
								//We need to iterate X number of times.
								//ie: if the total of videos in response are 90 an there are 3 channels in response then 90/3=30
								
								//First store all channelsid details in an array, so that on each iteration we dont need to search again the details
								List keys0 = new ArrayList(channelsVideos.keySet());
								//List<YTunerVideoItem> channIdsList = new ArrayList<YTunerVideoItem>();
								HashMap<Integer,YTunerVideoItem> channIdsList = new HashMap<Integer,YTunerVideoItem>();
								for (Object o0 : keys0) {								    				 								
									YTunerVideoItem itChanDetails = getDetailsChannelID(Integer.valueOf(o0.toString()));
									
									//If its null its very probably there was a change in language which not refleted to selected channel_ids of the lastLanguageselecrted
									//So, lets clean selection and retry
									if(itChanDetails == null){
										slectedChannelsIds = "";
										getTunerVideos(language, country, locale);
										return;
										//break;
										
									}
									
									channIdsList.put(Integer.valueOf(o0.toString()),itChanDetails);
									
								}
							
								
								//while(arrVidIds.size() < totalVidsinResponse-1){
								while(arrVidIds.size() < (int)(totalVidsinResponse/2)){//just choose half of it(to speed up), it comes random anyway
								//for(int i=0; i <= (totalVidsinResponse / channelsVideos.size()); i++){
								
									 List keys = new ArrayList(channelsVideos.keySet());
									 //Collections.shuffle(keys);
									 for (Object o : keys) {								    				 								
										 	 //TOO SLOW DISABLED YTunerVideoItem itChanDetails = getDetailsChannelID(Integer.valueOf(o.toString()));
										 	 //YTunerVideoItem itChanDetails0 = channIdsList.get(Integer.valueOf(o.toString()));
						
										 	
										 	YTunerVideoItem itChanDetails = new YTunerVideoItem(channIdsList.get(Integer.valueOf(o.toString())));
										 	//itChanDetails = itChanDetails0;
										 
											 List<String> itChannVids = (ArrayList<String>) channelsVideos.get(o);
											 //Log.d(MainActivity.DBG_TAG,"CHANNID " + o);
											 //Now lets find a video not added yet
											 Iterator it = itChannVids.iterator();
											 while (it.hasNext()) {
												 itChanDetails.videoID = it.next().toString();
												 //Log.d(MainActivity.DBG_TAG,"CHANNVID " + itChanDetails.videoID);
												 if(! arrVidIds.contains( itChanDetails.videoID )){		
													 //Log.d(MainActivity.DBG_TAG,"CHANN VID ADD " + itChanDetails.videoID);
													 arrVidIds.add(itChanDetails.videoID);
													 arrVidsItems.add(itChanDetails);
													 break;												 
												 }
											 }					 
											 
										 
									 }
								
								
								}
								
								
												
							 
								 
		
						
							
							
							
							
							//String videoIds = "";
							videoIds = TextUtils.join(",", arrVidIds);
							Log.d(MainActivity.DBG_TAG,"VIDIDS " + videoIds);
							
							Log.d(MainActivity.DBG_TAG,"TACKER 4");
							
							Log.d(MainActivity.DBG_TAG,"Before getVideos");
							
							
							getTubeVideos(arrVidsItems); //getVideos(arrVidIds); //getVideos(videoIds);
						  
						  
						  

			  		  } 
			  		  catch (Throwable t) {
			  			//Lets, retry				                      
			  			getTunerVideosConnectionError = true;
			  			t.printStackTrace();
			  		  }
				}
			});
			thread.start(); 
		  
	  }
	  
	  
	
	  
	  
	  /*
	   * Gets youtube video list from a csv list of video ids
	   * ArrayList<String> arrVidIds = new ArrayList<String>();
	   */
	  public void getTubeVideos(final ArrayList<YTunerVideoItem> videoItemss){
		  stopTuning= false;//On each call we need to reset, otherwise this wont do nothing
		  pauseTuning = false;
		  totalTuningVidItems = 0;
		  
		  yTunervideoItemList.clear();
		  yTunervideoItemList  = new ArrayList<YTunerVideoItem>();
		  
		  //Log.d(MainActivity.DBG_TAG,"RECEVIED videoIds: " +videoIds);
		  Log.d(MainActivity.DBG_TAG,"FIRTST : " +((YTunerVideoItem) videoItemss.get(0)).videoID);
		  Iterator testEentries = videoItemss.iterator();
		  //while (testEentries.hasNext())  Log.d(MainActivity.DBG_TAG,"RECEVIED videoIds: " +((YTunerVideoItem) testEentries.next()).videoID);
		  
		  Thread thread = new Thread(new Runnable(){
			    @Override
			    public void run() {		
					  try {						  						       
					        	Iterator videoEntries = videoItemss.iterator();							
								
									//List<String> vids = (ArrayList<String>) it.next();		
					        
							      //Now lets iterate through each youtube video item and store it into the proper yTuner category/channel 
							      while (videoEntries.hasNext() && stopTuning==false) {
							    	  Log.d(MainActivity.DBG_TAG,"ENT1ER WHILE THREAD: " + Thread.currentThread().getId());
							    	  
							    	  //If we are on pause do nothing
							    	  if(pauseTuning==true){
							    		  Thread.sleep(1000);
							    		  continue;
							    	  }
							    	  
							    	  
							    	  YTunerVideoItem currentVideoItem = (YTunerVideoItem) videoEntries.next();
								      
								      Log.d(MainActivity.DBG_TAG,"Before getvideoitems " + currentVideoItem.videoID);
										List arraylist = new ArrayList();
										arraylist  = getYTubeVideoItems(new StringBuilder("http://www.youtube.com/get_video_info?video_id=").append(currentVideoItem.videoID).toString());
										if(arraylist  == null) continue;//If we could not get the video(maybe country restriction, etc, skip)
										Log.d(MainActivity.DBG_TAG,"After getvideoitems" + currentVideoItem.videoID);
										
										yTunervideoItemList.add(new YTunerVideoItem(currentVideoItem.videoID
									      		,currentVideoItem.category_id
									      		,currentVideoItem.category_name
									      		,currentVideoItem.category_number												      		
									      		,currentVideoItem.channel_id
									      		,currentVideoItem.channel_name.toString()
									      		,currentVideoItem.channel_number.toString()
									      		,currentVideoItem.channel_subnumber
											 	,((YTubeVideoItem) arraylist.get(0)).VedioTitle
									      		,""//description
									      		,((YTubeVideoItem) arraylist.get(0)).VedioLeantgh //currentVideoItem.getContentDetails().getDuration()
									      		,""//currentVideoItem.getSnippet().getChannelTitle()
												,arraylist
										));

										totalTuningVidItems++;
										if(totalTuningVidItems % 5 == 0) 
										{
											pauseTuning = true;
											Log.d(MainActivity.DBG_TAG, "PAUSE TIUNIERG");
										}
										
										//if(totalNewVidItems>=10) pauseTuning = true;
										//Log.d(MainActivity.DBG_TAG,"STOEPPED TUNING "+totalTuningVidItems + " " + Boolean.toString(stopTuning));
										//We need to sleep here also since getYTubeVideoItems() also makes requests to the network
										//DONT SLEEP THIS WOULD MAKE OUT OF SYNC
							
							      }

					    } catch (Throwable t) {
					      t.printStackTrace();
					    }
			    }
		  });

		  thread.start(); 
	
	  }
	  


	  
	  public static List getYTubeVideoItems(String s)
	    {
	        ArrayList arraylist;
	        boolean flag;
	        boolean flag1;
	        int i;
	        String extreq = null;
	        int hight = 0;
	        arraylist = new ArrayList();
	        flag = false;
	        List yo = YTubeVideoLib.DownloadWebPagef(s);
	        if(yo == null) return null;//ADDED
	        //Log.d("urls", (new StringBuilder("Load Page Url :")).append(s).toString());
	        //GetImageLocation(s);
	        
	        YTubeVideoItem youtubeitem;
	        YTubeVideoItem youtubeitem1;
	        YTubeVideoItem youtubeitem2;
	        
	        flag1 = false;
	        i = 0;
	//_L9:
	        while (i < yo.size()) {
	        	//_L2:
	    	       
	    	        youtubeitem = (YTubeVideoItem)yo.get(i);
	    	        if (youtubeitem.getQuality().booleanValue())
	    	        {
	    	            //Log.d("req", (new StringBuilder(String.valueOf(youtubeitem.hight_vedio))).toString());
	    	        	//Log.d("reqq", (new StringBuilder(String.valueOf(hight))).toString());
	    	            //Log.d("reqq", (new StringBuilder(String.valueOf(extreq))).toString());
	    	            //Log.d("reqq", (new StringBuilder(String.valueOf(youtubeitem.Extention))).toString());
	    	            if (youtubeitem.Extention.equals(extreq) && youtubeitem.height_video == hight)
	    	            {
	    	                //Log.d("reqq", youtubeitem.Extention);
	    	                //youtubeitem.VedioSize;
	    	                flag1 = true;
	    	                //Log.d("reqq", youtubeitem.Extention);
	    	            }
	    	        }
	    	       
	        
	        	//_L1:
	    	        if (flag1){
	    	        	//_L4:
	    	    	        int j;
	    	    	        if (!flag1)
	    	    	        {
	    	    	            break;
	    	    	        	//break MISSING_BLOCK_LABEL_517;
	    	    	        }
	    	    	        j = 0;
	    	    	//_L7:
	    	    	       
	    	    	        while (1==1){
		    	    	        if (j >= yo.size())
		    	    	        {
		    	    	            if (arraylist.size() <= 0 || !flag)
		    	    	            {
		    	    	                return null;
		    	    	            } else{
		    	    	                //chr = "Load (Size,Quality)";
		    	    	                return arraylist;
		    	    	            }
		    	    	            
		    	    	            //break MISSING_BLOCK_LABEL_401;
		    	    	            //break;
		    	    	        }
		    	    	       
		    	    	        
		    	    	        youtubeitem1 = (YTubeVideoItem)yo.get(j);
		    	    	        //Log.d("Loaded", "Load Loop Timr");
		    	    	        youtubeitem1.VedioSize = 0L;
		    	    	        //youtubeitem1.VedioSize;
		    	    	        //Log.d("Size", "Load Loop Size");
		    	    	        if (youtubeitem1.getQuality().booleanValue())
		    	    	        {
		    	    	            if (youtubeitem1.Extention == "mp4" || youtubeitem1.Extention == "3gp" || youtubeitem1.Extention == "flv")
		    	    	            {
		    	    	                flag = true;
		    	    	            }
		    	    	            arraylist.add(youtubeitem1);
		    	    	        }
		    	    	        j++;
		    	    	          
	    	    	        }	    	    	        	    	    	      
	    	    	        
	    	        }else{
	    	        	//_L3
	    	    	        int k = 0;	
	    	    	        //_L6
	    	    	        while (k < yo.size()){	    	    	        	
	    	    	    	        //k;
	    	    	    	        youtubeitem2 = (YTubeVideoItem)yo.get(k);
	    	    	    	        if (youtubeitem2.getQuality().booleanValue())
	    	    	    	        {
	    	    	    	            if ("mp4".equals(youtubeitem2.Extention))
	    	    	    	            {
	    	    	    	                //Log.d("req", "Load Loop Timr");
	    	    	    	                //youtubeitem2.VedioSize;
	    	    	    	                flag1 = true;
	    	    	    	            } else
	    	    	    	            {
	    	    	    	                flag1 = true;
	    	    	    	            }
	    	    	    	        }
	    	    	    	        k++;	    	    	    	          
	    	    	        }
	    	    	        	
	    	        }	 
	          
	    	        	i++;
	    	        
	        }
	        
	        if (arraylist.size() <= 0)           
                return null;
	        else
	        	return arraylist;
	       //return yo;
	        //return arraylist.addAll(yo.iterator());
	    }
	  
	  	  	  	  	
	  
	
	  

	 /**
	   * Gets a list of videos related to another one.
	 */
	  void getRelatedVideos(){
		  
	  }
	  
	 /**
	   * Gets a list of videos related to another one.
	 */
	  String getRandomWord(List<String> wordsList){
		  
		  int Min = 1;
		  int Max = wordsList.size(); 
		  int randWord = Min + (int)(Math.random() * (Max - Min));
		  
		  return wordsList.get(randWord);
		  
	  }
	  
	  
	  public static int nthIndexOf(String text, char needle, int n)
      {
          for (int i = 0; i < text.length(); i++)
          {
              if (text.charAt(i) == needle)
              {
                  n--;
                  if (n == 0)
                  {
                      return i;
                  }
              }
          }
          return -1;
      }
	  
	 
}
