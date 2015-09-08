package com.protogab.ytuner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import com.protogab.ytuner.util.TunerDataProvider;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;

public class CloseFeedbackActivity extends Activity {
	
	int backKeyCount = 0;
	int optionSelected = -1;
	String comment = "";
	SharedPreferences prefs;
	
	boolean feedbackSent = false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        
    	prefs = PreferenceManager.getDefaultSharedPreferences(this);    	
        showCloseFeedbackDialog();
        
    }
    
    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        
       //If the user hits back on the comment screen, lets send the selected feendback option at least, so we dont lost it
       if(feedbackSent==false)
    	   sendFeedback();
    }
    
    @Override
    public void onBackPressed()
    {	  
    	
    	//super.onBackPressed();
    	backKeyCount++;
    	//Prevent closing this activity until at least 2 attempts
    	if(backKeyCount >5 ) super.onBackPressed();
    	
    }
    
    
    
    public void showCloseFeedbackDialog(){
    	//Show the feedback close dialog only once
    	  
    		  
    		  
    	      AlertDialog.Builder feedbackDialog = new AlertDialog.Builder(this);
    	      feedbackDialog.setTitle(getString(R.string.close_app_feedback)).setItems(R.array.close_app_feedback_values, 
    	    		  new DialogInterface.OnClickListener() {
    		          public void onClick(DialogInterface dialog, int which) {
    		        	  final String[] mTestArray = getResources().getStringArray(R.array.close_app_feedback_values);    		        	  
    		        	  optionSelected = which;
    		        	  Log.d(MainActivity.DBG_TAG, "CALL CLICK ON:");
    		        	  //If there might be a explination add a comment
    		        	  if(which==0){
    		        		  showCloseFeedbackDialogRateUs();
    		        		  
    		        	  }else if(which==1 ||  which==2 || which==3 || which==4 || which==9){
    		        		  showCloseFeedbackDialogComment();    		        		  
    		        	  }else{
    		        		  sendFeedback();
    		        	  }
    		        	  
    		          }
    	          });
    	      
    	      /*feedbackDialog.setOnDismissListener(
    	    		  new DialogInterface.OnDismissListener(){
    	    			  public void onDismiss(DialogInterface dialogInterface) {
    	    				  if(optionSelected == -1) sendFeedback();//If the user just close de dialog without any option selected then send it
    	    		  	  }
    	    		  });*/
    	      //feedbackDialog.setCancelable(false);
    	      
    	      feedbackDialog.setOnKeyListener(new Dialog.OnKeyListener() {
    	            @Override
    	            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
    	                // TODO Auto-generated method stub
    	                if (keyCode == KeyEvent.KEYCODE_BACK) {
    	                	backKeyCount++;
    	                	//Prevent closing this activity until at least 2 attempts
    	                	if(backKeyCount >5 ) {
    	                		sendFeedback();
    	                		return false;
    	                	}else{
    	                		return true;
    	                	}
    	                    //finish();
    	                    //dialog.dismiss();
    	                }
    	                return true;
    	            }
    	        });
    	      feedbackDialog.show();
    	  
      }
    
    
    private void showCloseFeedbackDialogRateUs(){
    	
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CloseFeedbackActivity.this);
    	 
		// set title
		alertDialogBuilder.setTitle(getString(R.string.close_app_rateus));

		// set dialog message
		alertDialogBuilder
			.setMessage(getString(R.string.close_app_rateus_desc) + "\n")
			.setCancelable(false)
			.setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					SharedPreferences.Editor editor = prefs.edit();
					editor.putBoolean("rating_dontshowagain", true);
			        editor.commit();
	            	try {
	            		CloseFeedbackActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + CloseFeedbackActivity.this.getPackageName())));
	        		} catch (ActivityNotFoundException e) {
	        			String str ="https://play.google.com/store/apps/details?id=" + CloseFeedbackActivity.this.getPackageName();
	        		 	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(str));
	        		 	CloseFeedbackActivity.this.startActivity(browserIntent);        	    	    	
	        		}
	            	sendFeedback();
	            	dialog.dismiss();
				}
			  })							  
			.setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					/*if (editor != null) {
	                    editor.putBoolean("rating_dontshowagain", true);
	                    editor.commit();
	                }	*/	        
					sendFeedback();
					dialog.cancel();
				}
			});

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
    	
    }
    
    private void showCloseFeedbackDialogComment(){
    	int optionNumber = optionSelected;
    	
    	Log.d(MainActivity.DBG_TAG, "CALL COMMENT:");
    	 final AlertDialog.Builder alert = new AlertDialog.Builder(CloseFeedbackActivity.this);
		    final EditText input = new EditText(CloseFeedbackActivity.this);
		    if(optionNumber==1){	
		    	alert.setTitle(getString(R.string.close_app_suggestion));
		    }else if(optionNumber==2){
		    	alert.setTitle(getString(R.string.close_app_videos));
		    }else if(optionNumber==3){
		    	alert.setTitle(getString(R.string.close_app_channel));
		    }else if(optionNumber==4){
		    	alert.setTitle(getString(R.string.close_app_comment));
		    }else{
		    	alert.setTitle(getString(R.string.close_app_comment));
		    }
		    alert.setView(input);
		    input.requestFocus();
		    alert.setPositiveButton(getString(R.string.close_app_comment_send), new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            comment = input.getText().toString().trim();
		            sendFeedback();
		        }
		    });
		    alert.show();
		    
		  
    }
    
    private void sendFeedback(){
    	feedbackSent = true;
    	
    	 Log.d(MainActivity.DBG_TAG, "CALL SEND FEEDBACK:");
    	Thread t = new Thread(new Runnable() { public void run() {
  		  try{
  			  
  			  String url = TunerDataProvider.contentSources[0]+"/ytuner/tuner/feedback";
  			  HttpPost httpPost = new HttpPost(url);
				  List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
				  
			      nameValuePairs.add(new BasicNameValuePair("value", String.valueOf(optionSelected)));
				  nameValuePairs.add(new BasicNameValuePair("model", Build.MODEL));
				  nameValuePairs.add(new BasicNameValuePair("comment", comment));
			      httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			      
				  HttpParams httpParameters = new BasicHttpParams();
				  // Set the timeout in milliseconds until a connection is established.
				  // The default value is zero, that means the timeout is not used. 
				  int timeoutConnection = 3000;
				  HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
				  // Set the default socket timeout (SO_TIMEOUT) 
				  // in milliseconds which is the timeout for waiting for data.
				  int timeoutSocket = 100;//No need to wait
				  HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
				  
				  
				  
				  //Set retry handler to not retry (wether there is a timeout, error, or whatever)
				  //(By default httpclient retries if the connection timed out)
				  HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
						@Override
						public boolean retryRequest(IOException exception,int executionCount, HttpContext context) {
							
							return false; ///Returning false makes it to NOT retry anymore
						}

			        };
				  
				  

				  DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
				  httpClient.setHttpRequestRetryHandler(myRetryHandler);//No need to wait
				  
				  Log.d(MainActivity.DBG_TAG, "SEND BEFORE POST:");
				  HttpResponse response = httpClient.execute(httpPost);
				  Log.d(MainActivity.DBG_TAG, "SEND AFTER POST:");

	   		  }catch(Exception ise){
	   			     
	   			  	/*
	   			  //TODO:If user closed it by mistake lets reopen it for him
		   			if(optionSelected == 8){
		   				PendingIntent intent = PendingIntent.getActivity(CloseFeedbackActivity.this.getBaseContext(), 0, new Intent(getIntent()), getIntent().getFlags());
					    AlarmManager manager = (AlarmManager) CloseFeedbackActivity.this.getSystemService(Context.ALARM_SERVICE);
					    manager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, intent);					    
		   			}*/
	   			  	System.exit(0); //Kills all threads also
	   			     //return;
	   		  }
	   		  
  		  //Toast.makeText(MainActivity.this, getString(R.string.thanks_bye), Toast.LENGTH_SHORT).show();	
  		/*
			  //TODO:If user closed it by mistake lets reopen it for him
  		  if(optionSelected == 8){
 				PendingIntent intent = PendingIntent.getActivity(CloseFeedbackActivity.this.getBaseContext(), 0, new Intent(getIntent()), getIntent().getFlags());
			    AlarmManager manager = (AlarmManager) CloseFeedbackActivity.this.getSystemService(Context.ALARM_SERVICE);
			    manager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, intent);					    
  		  }
  		  */
      	  System.exit(0); //Kills all threads also
  		  
	   	  }});
	   	  t.start();
    }
}
