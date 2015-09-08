package com.protogab.ytuner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.protogab.ytuner.MyApplication.TrackerName;
import com.protogab.ytuner.util.TunerDataProvider;
import com.protogab.ytuner.util.YTubeVideoItem;
import com.protogab.ytuner.util.YTunerVideoItem;
import com.protogab.ytuner.util.YTunerListingObject;

import android.app.Activity;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/*
 * Example at:
 * http://www.vogella.com/tutorials/AndroidListView/article.html
 */

public class ChannelGuideActivity extends Activity {
	SharedPreferences prefs;
	 YTunerListingObject channListing;
	
	private ListView lView;
	
	
	List<String> listItems = new ArrayList<String>();
	List<Integer> channIds = new ArrayList<Integer>();
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.channels_layout);
		lView = (ListView) findViewById(R.id.ListView01);			
		
		
	    //fill the array data
		prefs = PreferenceManager.getDefaultSharedPreferences(this);	    
	    channListing = (YTunerListingObject) (new Gson()).fromJson(getIntent().getStringExtra("channelListingOBJ"), YTunerListingObject.class);
	    
	    String csvValues = prefs.getString(getString(R.string.pref_key_selected_channels), "");		  	
	    System.out.println("ggggggggggggggggggg: "+ csvValues);
		String[]  selectedIds = csvValues.split(",");

		
		  //Add all the channels to the ListView and select the ones that are stored in preferences
		  Iterator it = channListing.categories.iterator();										
			while (it.hasNext()) {								
				YTunerListingObject.Category arrCatDetails = (YTunerListingObject.Category) it.next();
				ArrayList<YTunerListingObject.Channel> arrChannels = (ArrayList) arrCatDetails.channels;
				//int category_id = arrCatDetails.get("channels")category_id													
				Iterator it2 = arrChannels.iterator();
				while (it2.hasNext()) {
					YTunerListingObject.Channel arrChannDetails = (YTunerListingObject.Channel)  it2.next();
					String channFullName =  arrChannDetails.channel_number + " " + arrChannDetails.channel_name;
					listItems.add(channFullName);
					channIds.add(arrChannDetails.channel_id);//This allows us to save the position for later usage
						/*if(csvValues.length()>0){
							if(Arrays.asList(selectedIds).contains(arrChannDetails.channel_id) ==true )
								list.add(new Model(arrChannDetails.channel_id, channFullName , true));
							else
								list.add(new Model(arrChannDetails.channel_id, channFullName , false));
						}else{
							list.add(new Model(arrChannDetails.channel_id, channFullName , true));
						}
						*/
				}
			}
			
		
		//	 Set option as Multiple Choice. So that user can able to select more the one option from list
		lView.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice, listItems));
		lView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		
		//Now select the ones that need to be selected
		if(csvValues.length() > 0){
			for (String chanIdValue : selectedIds) {
				
			    int position = channIds.indexOf(Integer.valueOf(chanIdValue));
			    System.out.println("sssssssssssss: "+ chanIdValue + "/" + position);
			    lView.setItemChecked(position, true);
			}
		}else{
			//Show them all checked
			for (int i = 0; i <= (channIds.size()-1); i++) {				
					lView.setItemChecked(i, true);		
			}
		}
		
		lView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,int position, long id) {
				long[] ggtemp = lView.getCheckItemIds();
				int totalItemChecks =  ggtemp.length; //Some adnroid devices dont suport this method: lView.getCheckedItemCount();
				for (int i = 0; i <= (channIds.size()-1); i++) {
					if(totalItemChecks > ((channIds.size()-1)/2))
						lView.setItemChecked(i, false);
					else
						lView.setItemChecked(i, true);
					  
					}
				//Toast.makeText(ChannelGuideActivity.this, "LOONG ", Toast.LENGTH_SHORT).show();
				return false;
			}

	      });
		
		
				
	}
	
	@Override
	  public void onPause() {
		super.onPause();// Always call the superclass method first
		  
		  //Save selected items in preferences on CSV format as a string
		  String csvValues = "";

		  
		  SparseBooleanArray arrayChkItsPos = lView.getCheckedItemPositions();//Returns the list indicating which item positions are check true or false		  
		  System.out.println("sizzzzzzze :: "+ arrayChkItsPos.size());
		//Some adnroid devices dont suport this method:  //System.out.println("couuuuuunt :: "+ lView.getCheckedItemCount());
		  //
		  long[] gg = lView.getCheckItemIds();
		  for (int i = 0; i < (gg.length); i++) {
			  System.out.println("fooooooooooooooor2: "+ gg[i]);			  
			  csvValues += String.valueOf(channIds.get( (int)gg[i] ))+ ",";
		  }
		  
		  
		 /* for (int i = 1; i < (arrayChkItsPos.size()); i++) {
				System.out.println("fooooooooooooooor: "+ arrayChkItsPos.keyAt(i) + "/"+ Boolean.valueOf(arrayChkItsPos.get(i)));
				//csvValues += String.valueOf(channIds.get(arrayChkItsPos.keyAt(i)))+ ","; 
				
			}
		  */
		
		  if(csvValues.length()>0) csvValues = csvValues.substring(0, csvValues.length()-1);//Remove last comma
		  
		  System.out.println("fffffffffffffffffffffffff: "+ csvValues);
		  
		  SharedPreferences.Editor editor = prefs.edit();
		  editor.putString(getString(R.string.pref_key_selected_channels), csvValues);
		  editor.commit();	
		  
		  
		//Toast.makeText(ChannelGuideActivity.this, "is selected: "+ Boolean.valueOf(list.get(1).isSelected()), Toast.LENGTH_LONG).show();
		  
		  
	  }
	
	 @Override
	  public void onStart() {
	    super.onStart();
	    	    
	    //Google Analytics. Get tracker.
	    Tracker t = ((MyApplication) this.getApplication()).getTracker(TrackerName.APP_TRACKER);	    
	    GoogleAnalytics.getInstance(this).reportActivityStart(this); 
	    	    
	    // Set screen name.
	    // Where path is a String representing the screen name.
	    t.setScreenName(null);
	    // Send a screen view. 
	    t.send(new HitBuilders.AppViewBuilder().build());
	    
	    
	  }
	 
	 @Override
	  public void onStop(){
		  super.onStop();
		
		  GoogleAnalytics.getInstance(this).reportActivityStop(this);
	  }
}

