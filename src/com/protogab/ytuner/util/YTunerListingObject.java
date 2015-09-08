package com.protogab.ytuner.util;

import java.io.Serializable;
import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/*
 * NOTE: This class has serilize on each element so that when in proguard the Object properties are not obfuscated put maintain the same name
 * so that it can be parsed on the json response.
 * NOTE2: The Parcebale class implementation its just for the putextra intent needed to pass this object
 */
public final  class YTunerListingObject  {
	  @SerializedName("categories")
	  public ArrayList<Category> categories;
	  @SerializedName("test")
	  public String test = "jajaj";
	  
	  
	  public final  class Category {
		  @SerializedName("category_id")
		  public int category_id;
		  @SerializedName("category_name")
		  public String category_name;
		  @SerializedName("category_number")
		  public int category_number;
		  @SerializedName("channels")
		  public ArrayList<Channel> channels;

	  }
	  
	  public final  class Channel {
		  @SerializedName("channel_id")
		  public int channel_id; 
		  @SerializedName("channel_name")
		  public String channel_name;
		  @SerializedName("channel_number")
		  public String channel_number;
		  @SerializedName("channel_subnumber")
		  public int channel_subnumber;
		  			 
	  }
	  /*
	  private int mData;

	    // everything below here is for implementing Parcelable 

	    // 99.9% of the time you can just ignore this
	    public int describeContents() {
	        return 0;
	    }

	    // write your object's data to the passed-in Parcel
	    @Override
	    public void writeToParcel(Parcel out, int flags) {
	        //out.writeInt(mData);
	        out.writeList(categories);
	    }

	    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
	    public static final Parcelable.Creator<YTunerListingObject> CREATOR = new Parcelable.Creator<YTunerListingObject>() {
	        public YTunerListingObject createFromParcel(Parcel in) {
	            return new YTunerListingObject(in);
	        }

	        public YTunerListingObject[] newArray(int size) {
	            return new YTunerListingObject[size];
	        }
	    };

	    // example constructor that takes a Parcel and gives you an object populated with it's values
	    private YTunerListingObject(Parcel in) {
	        //mData = in.readInt();
	        categories = in.readArrayList(null);
	    }
*/
		
		
	  		  
}
