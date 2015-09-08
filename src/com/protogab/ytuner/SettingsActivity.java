package com.protogab.ytuner;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

		try {
			PackageInfo pInfo;
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			String currentVersionName = pInfo.versionName;
			
			Preference prefereces2=findPreference("pref_key_version");
			prefereces2.setSummary(currentVersionName);

		        
		} catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
        
		
		
		
		Preference prefFBPage=findPreference("pref_key_facebook_page");
		prefFBPage.setOnPreferenceClickListener (new Preference.OnPreferenceClickListener(){
            public boolean onPreferenceClick(Preference preference){
            	try {
		   	            
		   	          String uri = "facebook://facebook.com/pages/YTuner/614994511925662";
		   	          Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
		   	          startActivity(intent);
		   	         } catch (Exception e) {
		   	        	String str = "https://www.facebook.com/pages/YTuner/614994511925662";
			 		 	 Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(str));
			 		 	 startActivity(browserIntent);
		   	         }
            	
            	return false;
            }
        });
        
		Preference prefPlusPage=findPreference("pref_key_plus_page");    	
		prefPlusPage.setOnPreferenceClickListener (new Preference.OnPreferenceClickListener(){
            public boolean onPreferenceClick(Preference preference){
            	String str = "https://plus.google.com/109183165905480737468";
	 		 	 Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(str));
	 		 	 startActivity(browserIntent);
            	
            	return false;
            }
        });
		
		Preference prefRateUs=findPreference("pref_key_rateus");        
		prefRateUs.setOnPreferenceClickListener (new Preference.OnPreferenceClickListener(){
            public boolean onPreferenceClick(Preference preference){
            	try {
            		Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=" + SettingsActivity.this.getPackageName()));                
                    startActivity(intent);
        		} catch (ActivityNotFoundException e) {
        			String str ="https://play.google.com/store/apps/details?id=" + SettingsActivity.this.getPackageName();
        		 	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(str));
        		 	startActivity(browserIntent);        	    	    	
        		}
                
                return false;
            }
        });
		
    }
}
