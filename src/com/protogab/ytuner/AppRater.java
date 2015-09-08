package com.protogab.ytuner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//see http://www.androidsnippets.com/prompt-engaged-users-to-rate-your-app-in-the-android-market-appirater
public class AppRater {
    private final static String APP_TITLE = "YOUR-APP-NAME";
    private final static String APP_PNAME = "YOUR-PACKAGE-NAME";
    
    private final static int DAYS_UNTIL_PROMPT = 1;
    private final static int LAUNCHES_UNTIL_PROMPT = 3;
    
    public static void app_launched(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("rating_dontshowagain", false)) { return ; }
        
        SharedPreferences.Editor editor = prefs.edit();
        
        // Increment launch counter
        long launch_count = prefs.getLong("rating_launch_count", 0) + 1;
        editor.putLong("rating_launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("rating_date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("rating_date_firstlaunch", date_firstLaunch);
        }
        
        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch + 
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(mContext, editor);
            }
        }
        
        editor.commit();
    }   
    
    public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
        /*final Dialog dialog = new Dialog(mContext);
        dialog.setTitle("Rate " + APP_TITLE);

        LinearLayout ll = new LinearLayout(mContext);
        ll.setOrientation(LinearLayout.VERTICAL);
        
        TextView tv = new TextView(mContext);
        tv.setText("If you enjoy using " + APP_TITLE + ", please take a moment to rate it. Thanks for your support!");
        tv.setWidth(240);
        tv.setPadding(4, 0, 4, 10);
        ll.addView(tv);
        
        Button b1 = new Button(mContext);
        b1.setText("Rate " + APP_TITLE);
        b1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
            	try {
            		mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
        		} catch (ActivityNotFoundException e) {
        			String str ="https://play.google.com/store/apps/details?id=" + APP_PNAME;
        		 	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(str));
        		 	mContext.startActivity(browserIntent);        	    	    	
        		}
                
                
                dialog.dismiss();
            }
        });        
        ll.addView(b1);

        Button b2 = new Button(mContext);
        b2.setText("Remind me later");
        b2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ll.addView(b2);

        Button b3 = new Button(mContext);
        b3.setText("No, thanks");
        b3.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });
        ll.addView(b3);

        dialog.setContentView(ll);        
        dialog.show();
        */
        
        
        
        
        
                
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
 
			// set title
			alertDialogBuilder.setTitle("Rate Us");
 
			// set dialog message
			alertDialogBuilder
				.setMessage("Would you like to rate this app?" + "\n" + "If you like this app then rate us, it's just a second!" + "\n")
				.setCancelable(false)
				.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						if (editor != null) {
		                    editor.putBoolean("rating_dontshowagain", true);
		                    editor.commit();
		                }
		            	try {
		            		mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mContext.getPackageName())));
		        		} catch (ActivityNotFoundException e) {
		        			String str ="https://play.google.com/store/apps/details?id=" + mContext.getPackageName();
		        		 	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(str));
		        		 	mContext.startActivity(browserIntent);        	    	    	
		        		}
		            	dialog.dismiss();
					}
				  })
				.setNeutralButton("Remind me later",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						if (editor != null) {
							editor.putLong("rating_launch_count", 0);
		                    editor.commit();
		                }						
						dialog.cancel();
					}
				  })				  
				.setNegativeButton("No, never",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						if (editor != null) {
		                    editor.putBoolean("rating_dontshowagain", true);
		                    editor.commit();
		                }		                
						dialog.cancel();
					}
				});
 
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
			
        
    }
}

