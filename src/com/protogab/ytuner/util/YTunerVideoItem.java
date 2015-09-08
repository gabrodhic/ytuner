package com.protogab.ytuner.util;

import java.util.ArrayList;
import java.util.List;

/*
 * We need to create our own Video Item, since the youtube item result changes some of it properties through out all of the different API requests.
 * I.e the snippet property from the result of the VideoItemList is different from the PlalistItemList, and so on.
 */

public class YTunerVideoItem implements Cloneable{
	
	//public YTunerVideoItem copy;//This is only to be able to copy this object by value
	public String videoID;
	public int category_id;
	public String category_name;
	public int category_number;
	public  int channel_id;  
	public String channel_name;
	public String channel_number;
	public int channel_subnumber;
	  
	public String title;
	public String description;
	public String duration;
	public String channel;
	  
	public List ytubevideoItems = null;
	
	public YTunerVideoItem(){
		
	}
	public YTunerVideoItem(String id, int CatId, String catName, int catNum, int ChanID, String chanName, String ChanNumb, int ChanSubNum, String tit, String desc, String dur, String chann, List ytubeItems){
		  this.videoID = id;
		  this.category_id = CatId;
		  this.category_name = catName;
		  this.category_number= catNum;
		  this.channel_id = ChanID;
		  this.channel_name = chanName;			  
		  this.channel_number = ChanNumb;			   
		  this.channel_subnumber = ChanSubNum;
		  this.title = tit;
		  this.description = desc;
		  this.duration = dur;
		  this.channel = chann; 
		  
		  this.ytubevideoItems = ytubeItems;
	  }
	
	/* This methods is simply to provide a clone/copy functionality, which is limited in java
	 * http://stackoverflow.com/questions/869033/how-do-i-copy-an-object-in-java
	 */
	
	public YTunerVideoItem(YTunerVideoItem another){
		this(another.videoID,another.category_id,another.category_name,another.category_number,another.channel_id,another.channel_name,another.channel_number,another.channel_subnumber,another.title,another.description,another.duration,another.channel, another.ytubevideoItems);
		
		
	}
	
	 protected Object clone() throws CloneNotSupportedException {
	        return super.clone();
	    }
}
