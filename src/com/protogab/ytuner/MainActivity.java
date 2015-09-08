/*
 * Main Activity
 * 
 */

package com.protogab.ytuner;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.protogab.ytuner.MyApplication.TrackerName;
import com.protogab.ytuner.util.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;


import android.view.SurfaceHolder;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Sample activity showing how to properly enable custom fullscreen behavior.
 * <p>
 * This is the preferred way of handling fullscreen because the default fullscreen implementation
 * will cause re-buffering of the video.
 */
public class MainActivity extends Activity implements 
	View.OnClickListener,
    SurfaceHolder.Callback{
	
	AnimationDrawable animation;
	
	public static final String DBG_TAG = "TEST";
	final static String HISTORY_FILE_NAME = "videos_history.txt";
	public final static int CHANNEL_MODE_CAT = 1;//Navigate between all categories
	public final static int CHANNEL_MODE_SUBCAT = 2;//Navigate between all channels in X category
	public final static int CHANNEL_MODE_CHANN = 3;//Navigate in the same channel
  

  int sdkVersion = android.os.Build.VERSION.SDK_INT;
  
  boolean init_success = false;
  SharedPreferences prefs;
  private RelativeLayout baseLayout;
  private CustomVideoView playerView;
      
  
  DisplayMetrics metrics;
  
 
  int cAnimation = 1;

  
  Drawable back_logo;
  PopupWindow splashLayerPW;
  PopupWindow transitionLayerPW = null;
  PopupWindow playerControlsLayerPW;
  PopupWindow playerEmptyLayerPW;
  PopupWindow userInstrucPW;
  LinearLayout linearLayoutLL;
  LayoutInflater layoutInflater;
  View transitionLayerView;
  View controlsLayerView;
  View emptyLayerView;
  View swipeInstrucView;
  ImageView playPauseIV;
  ImageView previousIV;
  ImageView nextIV;
  ImageView statusbarIV;
  ImageView shareIV; 
  ImageView menuIV;
  ImageView relatedIV;
  ImageView searchIV;
  ImageView infoIV;
  ImageView scanIV;
  ImageView helpIV;
  ImageView configIV;
  TextView txvCurrentTitle;  
  TextView  txvCurrentChanNum;
  
  
  TunerDataProvider tunerProvider;
  private YTunerPlayer ytunerPlayer;
  
  List<String> wordsListDictionary = new ArrayList<String>();
  FileOutputStream vidsHistoryoutputStream;
  
  private static Pattern myPattern;
  int currentVideoIndex = -1;
  List<Integer> sessionHistory = new ArrayList<Integer>();
  boolean lastUserActionPlayPrevious = false; 
  YTunerVideoItem currentVideoListItem;
  boolean videoFirstPlayback = false;
  boolean stopAllThreads = false;
  
  SeekBar seekbar;
  TextView txtSeekbarCurrentTime;
  TextView txtSeekbarMaxTime;
  
  long startTimeShowPlayerControls; 

  String settingsLastCountrySelected;
  String settingsLastLanguageSelected;
  String settingsLastChannelsSelected;
  
  private GestureDetector gestureDetector;
  
  

  private int mCurrentBufferPercentage;  
  private int mSeekWhenPrepared;
  
  private boolean mStartWhenPrepared;
  private int mSurfaceHeight;
  public SurfaceHolder mSurfaceHolder;
  public int mVideoHeight;
  public int mVideoWidth;
  //onSurface s;
  protected int type;
  
  String toastMessage;
  private int consecutiveVideoErrors = 0;
  private boolean showCloseFeedback = false;
  
  private android.media.MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new android.media.MediaPlayer.OnBufferingUpdateListener() {
      @Override
      public void onBufferingUpdate(MediaPlayer mediaplayer, int i)
      {
          mCurrentBufferPercentage = i;
      }
   
  };
  private android.media.MediaPlayer.OnCompletionListener mCompletionListener = new android.media.MediaPlayer.OnCompletionListener() {   
  	@Override
      public void onCompletion(MediaPlayer mediaplayer)
      {
  		
  	  	//Play Next video
  		goNextVideo();  
  		
      }
  };
 
  private android.media.MediaPlayer.OnErrorListener mErrorListener = new android.media.MediaPlayer.OnErrorListener() {
      @Override
      public boolean onError(MediaPlayer mediaplayer, int i, int j)
      {              	  
	      validateInternetConnection();
	      Log.d(DBG_TAG,(new StringBuilder("Error: ")).append(i).append(",").append(j).toString());

	    	
	      consecutiveVideoErrors++;
	      //If we've reached more than 5 consecutive video errors, there might be some really big problem trying to decode video urls on this specific device.
	      //Maybe because of the ip or something like that, so lets notify the user.
	      if(consecutiveVideoErrors >= 5){
		      AlertDialog videoErrorDialog = new AlertDialog.Builder(MainActivity.this).create(); //Read Update
	   	      videoErrorDialog.setTitle(getString(R.string.load_video_error));
	   	      videoErrorDialog.setMessage(getString(R.string.load_video_error_desc));		
	   	      videoErrorDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.exit_app),  new DialogInterface.OnClickListener() {
	   	          public void onClick(DialogInterface dialog, int which) {              
	   	        	System.exit(0); //Kills all threads also      	   
	   	          }
	   	       });
	   	      videoErrorDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.retry),  new DialogInterface.OnClickListener() {
	   	         public void onClick(DialogInterface dialog, int which) {              
	   	        	 //There might be some content restriction(content,country,copyright,etc) for the current video, so skip to the next
	   	        	 if(lastUserActionPlayPrevious == false)
	   	    		  	goNextVideo();
	   	        	 else
	   	        		 changePreviousVideo();
	   	    	  	         	   
	   	         }
	   	      });	    				   	
	   	      videoErrorDialog.show(); 
	      }else{
	    	  //There might be some content restriction(content,country,copyright,etc) for the current video, so skip to the next
	    	  if(lastUserActionPlayPrevious == false)
	    		  goNextVideo();
	    	  else
	    		  changePreviousVideo();
	      }
	    	  	      
          return true;
      }       
  };
  
  android.media.MediaPlayer.OnPreparedListener mPreparedListener = new android.media.MediaPlayer.OnPreparedListener() {      
  	@Override
      public void onPrepared(MediaPlayer mediaplayer)
      {
  		consecutiveVideoErrors--;
  		
  		
  				
  		//Hide the transition layer screen  
  		 hideTransitionLayer();
	  
  		 
  		showPlayerControls();
  		
  		/*
          mIsPrepared = true;
          if (mOnPreparedListener != null)
          {
              mOnPreparedListener.onPrepared(mMediaPlayer);
          }
          if (mMediaController != null)
          {
              mMediaController.setEnabled(true);
          }
          mVideoWidth = mediaplayer.getVideoWidth();
          mVideoHeight = mediaplayer.getVideoHeight();
          if (mVideoWidth == 0 || mVideoHeight == 0){
          	 if (mSeekWhenPrepared != 0)
               {
                   mMediaPlayer.seekTo(mSeekWhenPrepared);
                   mSeekWhenPrepared = 0;
               }
               if (mStartWhenPrepared)
               {
                   mMediaPlayer.start();
                   mStartWhenPrepared = false;
                   return;
               }
          }*/
      }

  };
  
  

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    setContentView(R.layout.main);
    
    metrics = new DisplayMetrics(); 
    getWindowManager().getDefaultDisplay().getMetrics(metrics);
    
    gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
        @Override
        public void onLongPress(MotionEvent e) {
            Toast.makeText(MainActivity.this, "long ptap", Toast.LENGTH_SHORT).show();
            
        }
    });
    //Load the videos(we need to do this as soon as posibble, to be able to start playing something fast)

    
    
    
    baseLayout = (RelativeLayout) findViewById(R.id.main_layout);
        
    prefs = PreferenceManager.getDefaultSharedPreferences(this);
    
    //Save the time of first time launch(for different uses through out the app) if its the first time
	// Get date of first launch
      Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
      if (date_firstLaunch == 0) {
          date_firstLaunch = System.currentTimeMillis();
          SharedPreferences.Editor editor = prefs.edit();
          editor.putLong("date_firstlaunch", date_firstLaunch);
          editor.commit();
      }
      
      //Track number of launch
      int appLaunchCount = prefs.getInt("app_launch_count", 0);
      appLaunchCount++;
      prefs.edit().putInt("app_launch_count",  appLaunchCount).commit();

	//Setup user app rating remembers alerts
     //AppRater.app_launched(this);
    
    //Initialize tunerprovider
    tunerProvider = new TunerDataProvider();
    
    Log.d(DBG_TAG,"Ninitializing yTunerProvd");
    
    Log.d(DBG_TAG,"METRICS SCREEEN " + metrics.widthPixels);
    
    
    //WE need to start() the animation onResume() http://stackoverflow.com/questions/3284083/starting-an-animationdrawable-in-android
    animation = new AnimationDrawable();
    
    //Load each drawable using a scale down version if needed(some devices have no enough memory)
    //http://developer.android.com/training/displaying-bitmaps/load-bitmap.html    
    Drawable d1 = new BitmapDrawable(decodeSampledBitmapFromResource(getResources(), R.drawable.logo_ytuner1, metrics.widthPixels, metrics.heightPixels));
    Drawable d2 = new BitmapDrawable(decodeSampledBitmapFromResource(getResources(), R.drawable.logo_ytuner2, metrics.widthPixels, metrics.heightPixels));
    Drawable d3 = new BitmapDrawable(decodeSampledBitmapFromResource(getResources(), R.drawable.logo_ytuner3, metrics.widthPixels, metrics.heightPixels));
    Drawable d4 = new BitmapDrawable(decodeSampledBitmapFromResource(getResources(), R.drawable.logo_ytuner4, metrics.widthPixels, metrics.heightPixels));
    animation.addFrame(d1, 400);
    animation.addFrame(d2, 400);
    animation.addFrame(d3, 400);
    animation.addFrame(d4, 400);
    
    d1 = null;
    d2 = null;
    d3 = null;
    d4 = null;
    
    
    //animation.addFrame(getResources().getDrawable(R.drawable.llogo_ytuner1), 400);
    //animation.addFrame(getResources().getDrawable(R.drawable.llogo_ytuner2), 400);
    //animation.addFrame(getResources().getDrawable(R.drawable.llogo_ytuner3), 400);
    //animation.addFrame(getResources().getDrawable(R.drawable.llogo_ytuner4), 400);    
    animation.setOneShot(false);//Loop over frames       
    
    //http://stackoverflow.com/questions/11592820/writing-backwards-compatible-android-code
    //TODO: write compatible code as above. For now this works OK.
    //if(sdkVersion >= 16){	    	
    	//baseLayout.setBackground(animation);	    	
    //}else{
    	baseLayout.setBackgroundDrawable (animation);
    //}
   
    
    //baseLayout.post(new Starter());
    
    
   
    
       
    //Initialize
    tunerProvider.init();
    
    //Open videos history file for storing users history      
    BufferedReader breader = null;    
    InputStreamReader inputStreamReader = null;    
    try {   
    	
    	File file = getBaseContext().getFileStreamPath(HISTORY_FILE_NAME);
        if( file.exists()){
    	
	    	inputStreamReader = new InputStreamReader(openFileInput(HISTORY_FILE_NAME));
	        breader = new BufferedReader(inputStreamReader);
	    	    
	        String str;
	        while ((str = breader.readLine()) != null) {
	    
	        	tunerProvider.vidsHistory.add(str);
	        }
        }
    
    } catch (Exception e) {
    	Log.e("ERROR", "Oooops error reading ytuner history file: " +e.getMessage());
        e.printStackTrace();
        
    } finally {
        
        	try {
        		breader.close();
        		inputStreamReader.close();        		
        	} catch (Exception e) {}
        
    }
    
        
    
    checkAppUpdate();
    startMonitoringThread();
    
    loadDataChannelListing();
    loadDataChannelMode();
    
    
    //Notify the user if current connection is NOT wifi      	  
	ConnectivityManager connectivity = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
	  if (connectivity != null) 
	  {
	      NetworkInfo wifiinfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	      if (wifiinfo != null){ 
	    	if(wifiinfo.getState() != NetworkInfo.State.CONNECTED)
	    		Toast.makeText(MainActivity.this, getString(R.string.wifi_desconn_warning), Toast.LENGTH_LONG).show();
	      } 	
	  }
	      
  	
    

    playerView = (CustomVideoView) findViewById(R.id.videoview);
    playerView.setVisibility(View.INVISIBLE);

    
    
    //Setup ytuner player options
    ytunerPlayer = new YTunerPlayer();
    ytunerPlayer.setScanMode(false);
    
    	
    	//Load the transition layer popupwindow	    
	    linearLayoutLL = new LinearLayout(this);
	    layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
	   transitionLayerView = layoutInflater.inflate(R.layout.transition_layer, null);

	   transitionLayerView.setOnTouchListener(new OnSwipeTouchListener() {	
		    public boolean onSwipeRight() {
		    	changePreviousVideo();
		        return true;
		    }
		    public boolean onSwipeLeft() {
		    	changeNextVideo();		        
		        return true;
		    }
		    public boolean onSwipeBottom() {
		    	Log.d(DBG_TAG,"Swipe bottom");
		    	voteCurrentVideo();
		        return true;
		    }
		   
		});
        transitionLayerPW = new PopupWindow(
          transitionLayerView, 
          LayoutParams.MATCH_PARENT, //Change this depending on size desired, ie fullscreen  
          LayoutParams.MATCH_PARENT //Change this depending on size desired, ie fullscreen
          );  
	            
       
	     
	     
	     //Load instructions
        swipeInstrucView = layoutInflater.inflate(R.layout.instructions_view, null);  
	    userInstrucPW = new PopupWindow(
	    		 swipeInstrucView, 
	    		 LayoutParams.MATCH_PARENT, //Change this depending on size desired, ie fullscreen  
	    		 LayoutParams.MATCH_PARENT //Change this depending on size desired, ie fullscreen
        );	     
	    swipeInstrucView.setOnClickListener(new View.OnClickListener() {
	    	 @Override  
	    	 public void onClick(View v) {	    		 
	    		 userInstrucPW.dismiss();
	    		 showPlayerControls(); 
	       }
 		});	 	 
        
	     //Load the controls layer popupwindow
	     controlsLayerView = layoutInflater.inflate(R.layout.controls_layer, null);  	     
	     playerControlsLayerPW = new PopupWindow(
	    		 controlsLayerView, 
	    		 LayoutParams.MATCH_PARENT, //Change this depending on size desired, ie fullscreen  
	    		 LayoutParams.MATCH_PARENT //Change this depending on size desired, ie fullscreen
         );
	     
	     controlsLayerView.setOnClickListener(new View.OnClickListener() {
	    	 @Override  
	    	 public void onClick(View v) {	    		 
	    		 if(playerControlsLayerPW.isShowing() == true) {
	    			  hidePlayerControls();
	    		 }else{
	    			  showPlayerControls();
	    		 }
	       }
  		});	 	    
	     controlsLayerView.setOnTouchListener(new OnSwipeTouchListener() {	
		    public boolean onSwipeRight() {
		    	changePreviousVideo();
		        return true;
		    }
		    public boolean onSwipeLeft() {
		    	changeNextVideo();		        
		        return true;
		    }
		    public boolean onSwipeBottom() {
		    	Log.d(DBG_TAG,"Swipe bottom");
		    	voteCurrentVideo();
		        return true;
		    }
		   
		});
	     playPauseIV = (ImageView)controlsLayerView.findViewById(R.id.image_play_pause);
	     previousIV = (ImageView)controlsLayerView.findViewById(R.id.image_previous);
	     nextIV = (ImageView)controlsLayerView.findViewById(R.id.image_next);
	     
	     txvCurrentTitle = (TextView)controlsLayerView.findViewById(R.id.current_title);
	     
	     txvCurrentChanNum = (TextView)controlsLayerView.findViewById(R.id.txv_chan_number);
	     
	     //relatedIV = (ImageView)controlsLayerView.findViewById(R.id.img_related_icon);
	     shareIV = (ImageView)controlsLayerView.findViewById(R.id.img_share_icon);
	     //searchIV = (ImageView)controlsLayerView.findViewById(R.id.img_search_icon);
	     statusbarIV = (ImageView)controlsLayerView.findViewById(R.id.img_statusbar_icon);
	     infoIV = (ImageView)controlsLayerView.findViewById(R.id.img_info_icon);
	     //scanIV = (ImageView)controlsLayerView.findViewById(R.id.img_scan_icon);    
	     //helpIV = (ImageView)controlsLayerView.findViewById(R.id.img_help_icon);
	     configIV = (ImageView)controlsLayerView.findViewById(R.id.img_config_icon);
	     menuIV = (ImageView)controlsLayerView.findViewById(R.id.img_menu_icon);
	     
	     
	     txvCurrentChanNum.setOnClickListener( new ImageView.OnClickListener() {
	         public void onClick(View v) {
	        	 showChannelGuide();  	 
             }
	     });
	     playPauseIV.setOnClickListener( new ImageView.OnClickListener() {
	         public void onClick(View v) {
	        	 playPauseIV.setImageResource(R.drawable.pause);
	        	 if((Integer)playPauseIV.getTag() == R.drawable.play ){ //we stored previously on the imageview tag's property the drawable resource id, which is an integer value
	        		//Report Event to Google Analytics
	    		  	  Tracker t = ((MyApplication) MainActivity.this.getApplication()).getTracker(TrackerName.APP_TRACKER);
	    		  	  t.send(new HitBuilders.EventBuilder()
	    		        .setCategory(getString(R.string.event_videoCategory))
	    		        .setAction(getString(R.string.event_videoPlay))
	    		        .setLabel(currentVideoListItem.channel_name)
	    		        .build());
	        		 
	        		 playerView.start();//playerView.resume();//player.play();
	        	 }else{
	        		//Report Event to Google Analytics
	    		  	  Tracker t = ((MyApplication) MainActivity.this.getApplication()).getTracker(TrackerName.APP_TRACKER);
	    		  	  t.send(new HitBuilders.EventBuilder()
	    		        .setCategory(getString(R.string.event_videoCategory))
	    		        .setAction(getString(R.string.event_videoPause))
	    		        .setLabel(currentVideoListItem.channel_name)
	    		        .build());
	        		 
	        		 playerView.pause();//player.pause();
	        	 }
             }
	     });
	     previousIV.setOnClickListener( new ImageView.OnClickListener() {
	         public void onClick(View v) {
	        	 changePreviousVideo();
             }
	     });
	     nextIV.setOnClickListener( new ImageView.OnClickListener() {
	         public void onClick(View v) {
	        	 changeNextVideo();
             }
	     });
	     infoIV.setOnClickListener( new ImageView.OnClickListener() {
	         public void onClick(View v) {
	        	 String str = "http://www.youtube.com/watch?v="+currentVideoListItem.videoID;
	 		 	 Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(str));
	 		 	 MainActivity.this.startActivity(browserIntent);        
             }
	     });
	    
	     menuIV.setTag(0);
	     menuIV.setOnClickListener( new ImageView.OnClickListener() {
	         public void onClick(View v) {
	        	 
	     	 	
	        	 
	        	 if( prefs.getBoolean("unlocked_menu_button", false) == false){
	        		 	AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create(); //Read Update
		 	     	  	alertDialog.setTitle(getString(R.string.option_locked));
		 	     	  	alertDialog.setMessage(getString(R.string.option_locked_desc));	      
		 	     	  	alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",  new DialogInterface.OnClickListener() {
		 	     	  	  public void onClick(DialogInterface dialog, int which) {
		        				//Report Event to Google Analytics
			 	   		  	  	Tracker t = ((MyApplication) MainActivity.this.getApplication()).getTracker(TrackerName.APP_TRACKER);
			 	   		  	  	t.send(new HitBuilders.EventBuilder()
			 	   		        .setCategory(getString(R.string.event_socialCategory))
			 	   		        .setAction(getString(R.string.event_socialShareUnlockCancel))
			 	   		        .setLabel(currentVideoListItem.channel_name)
			 	   		        .build());
		 	     	  		  
		 	     	  	   
		 	     	  	  }
		 	     	  	});
		 	     	  	alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok",  new DialogInterface.OnClickListener() {
		 	     	  	 public void onClick(DialogInterface dialog, int which) {
		 	     			//Report Event to Google Analytics
			 	   		  	  	Tracker t = ((MyApplication) MainActivity.this.getApplication()).getTracker(TrackerName.APP_TRACKER);
			 	   		  	  	t.send(new HitBuilders.EventBuilder()
			 	   		        .setCategory(getString(R.string.event_socialCategory))
			 	   		        .setAction(getString(R.string.event_socialShareUnlockOk))
			 	   		        .setLabel(currentVideoListItem.channel_name)
			 	   		        .build());
		 	     	  		 
		 	     	  		 
		 	     	  		 
		 		        	 Intent sendIntent = new Intent();
		 		    	     sendIntent.setAction(Intent.ACTION_SEND);
		 		    	     sendIntent.putExtra(Intent.EXTRA_SUBJECT, "I am yTuning");
		 		    	     String url ="https://play.google.com/store/apps/details?id=com.protogab.ytuner&referrer=utm_source%3Dapp";
		 		    	     sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out \"yTuner\" app on Google Play " + Uri.parse(url));
		 		    	     sendIntent.setType("text/plain");
		 		    	     startActivity(sendIntent);
		        			
			 	     	  	SharedPreferences.Editor editor = prefs.edit();
		        			editor.putBoolean("unlocked_menu_button", true);
		        			editor.commit();		 	     	  	   
		 	     	  	 }
		 	     	  	});	
		 	     	  	alertDialog.show();
	        	 }else{
	        		 if((Integer)menuIV.getTag() == 0){	        			        			
		        		 showButtonsMenu();
	        		 }else{
		        		 hideButtonsMenu();
	        		 }
	        	 }
	        			
	        	 
	        	 	 
             }
	     });
	     configIV.setOnClickListener( new ImageView.OnClickListener() {
	         public void onClick(View v) {
	        	 	//Report Event to Google Analytics
	   		  	  	Tracker t = ((MyApplication) MainActivity.this.getApplication()).getTracker(TrackerName.APP_TRACKER);
	   		  	  	t.send(new HitBuilders.EventBuilder()
	   		        .setCategory(getString(R.string.event_configCategory))
	   		        .setAction(getString(R.string.event_configSettings))
	   		        .setLabel(currentVideoListItem.channel_name)
	   		        .build());
		        	 
	        	 
	        	 
	        	 Intent intent = new Intent(MainActivity.this, SettingsActivity.class);	        	   
	        	 startActivity(intent);
             }
	     });
	     statusbarIV.setTag(0);
	     statusbarIV.setOnClickListener( new ImageView.OnClickListener() {
	         public void onClick(View v) {
	        	 
	        	 if((Integer)statusbarIV.getTag() == 0)
	        		 showStatusBar();
	        	else
	        		 hideStatusBar();	        	 
             }
	     });	     
	     shareIV.setOnClickListener( new ImageView.OnClickListener() {
	         public void onClick(View v) {
	        	//Report Event to Google Analytics
   		  	  	Tracker t = ((MyApplication) MainActivity.this.getApplication()).getTracker(TrackerName.APP_TRACKER);
   		  	  	t.send(new HitBuilders.EventBuilder()
   		        .setCategory(getString(R.string.event_socialCategory))
   		        .setAction(getString(R.string.event_socialShareVideo))
   		        .setLabel(currentVideoListItem.channel_name)
   		        .build());
	        	 
	        	 
	        	 Intent sendIntent = new Intent();
	    	     sendIntent.setAction(Intent.ACTION_SEND);
	    	     sendIntent.putExtra(Intent.EXTRA_SUBJECT, "On yTuner app: "+currentVideoListItem.title);
	    	     sendIntent.putExtra(Intent.EXTRA_TEXT, "http://www.youtube.com/watch?v="+currentVideoListItem.videoID);
	    	     sendIntent.setType("text/plain");
	    	     startActivity(sendIntent);
             }
	     });
	     txtSeekbarCurrentTime = (TextView) controlsLayerView.findViewById(R.id.txv_video_currenttime);
	     txtSeekbarMaxTime =  (TextView) controlsLayerView.findViewById(R.id.txv_video_endtime);
	     seekbar = (SeekBar) controlsLayerView.findViewById(R.id.videoSeekBar);
	     
	     /*
	     controlsLayerView.dispatchTouchEvent(MotionEvent event) {
	   	  //http://stackoverflow.com/questions/11001282/ontouchevent-not-working-on-child-views

	   	  startTimeShowPlayerControls = System.nanoTime();//Restart the showplayercontrols counter on any touch received 
	   	  super.dispatchTouchEvent(event);
	   	  return false;
	   	  //return super.dispatchTouchEvent(event);
	     }
	     */
	     /*controlsLayerView.onTouchEvent(MotionEvent event) {
	   	  //http://stackoverflow.com/questions/11001282/ontouchevent-not-working-on-child-views
	  
	   	  startTimeShowPlayerControls = System.nanoTime();//Restart the showplayercontrols counter on any touch received 
	   	  super.dispatchTouchEvent(event);
	   	  return false;
	   	  //return super.dispatchTouchEvent(event);
	     }*/

	     seekbar.setOnSeekBarChangeListener( new OnSeekBarChangeListener()
	     {
	    	 public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser)
		     {
	    		 if(fromUser) {
	    			//Report Event to Google Analytics
	    		  	  Tracker t = ((MyApplication) MainActivity.this.getApplication()).getTracker(TrackerName.APP_TRACKER);
	    		  	  t.send(new HitBuilders.EventBuilder()
	    		        .setCategory(getString(R.string.event_videoCategory))
	    		        .setAction(getString(R.string.event_videoSeekBar))
	    		        .setLabel(currentVideoListItem.channel_name)
	    		        .build());
	    			 
	    			 
	    			 updateSeekbarInfo(progress,true);
	    		 }else{
	    			 updateSeekbarInfo(progress,false);
	    		 }

		     }
	
		     public void onStartTrackingTouch(SeekBar seekBar)
		     {
		    	
		     }
	
		     public void onStopTrackingTouch(SeekBar seekBar)
		     {
		    	 
		     }
	     });
	    
	     
	     
	     
	     emptyLayerView = layoutInflater.inflate(R.layout.empty_layer, null);  	      
	     emptyLayerView.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
	            	
	            	Log.d(DBG_TAG, "onClick emptyLayer");
	                if(playerControlsLayerPW.isShowing() == true) 
	                	hidePlayerControls();
	                else
	                	showPlayerControls();
	            }
	        });
	     emptyLayerView.setOnTouchListener(new OnSwipeTouchListener() {	
			    public boolean onSwipeRight() {
			    	changePreviousVideo();
			        return true;
			    }
			    public boolean onSwipeLeft() {
			    	changeNextVideo();		        
			        return true;
			    }
			    public boolean onSwipeBottom() {
			    	Log.d(DBG_TAG,"Swipe bottom");
			    	voteCurrentVideo();
			        return true;
			    }
			   
			});	     
	     playerEmptyLayerPW = new PopupWindow(
	    		 emptyLayerView, 
	    		 LayoutParams.MATCH_PARENT, //Change this depending on size desired, ie fullscreen  
	    		 LayoutParams.MATCH_PARENT //Change this depending on size desired, ie fullscreen
         );
	     
	     
	   //Load the splash layout immediately
	     
		  /*LayoutInflater layoutInflater2 = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
		 View  splashView = layoutInflater2.inflate(R.layout.splash_layer, null);  
		 splashLayerPW = new PopupWindow(
	    		 splashView, 
	       LayoutParams.MATCH_PARENT, //Change this depending on size desired, ie fullscreen  
	       LayoutParams.MATCH_PARENT //Change this depending on size desired, ie fullscreen
	       );  
		 splashLayerPW.showAtLocation(new LinearLayout(this), Gravity.CENTER, 0, 0);
		  */
		  
	    playerView.setOnCompletionListener(mCompletionListener);
	    playerView.setOnErrorListener(mErrorListener);
	    playerView.setOnPreparedListener(mPreparedListener);
	    
	    
	    //Media controller
	    /*mMediaController= new MediaController(this);    
	    mMediaController.setMediaPlayer(playerView);
	    mMediaController.hide();
	    playerView.setMediaController(mMediaController);*/
	    
	    //Full screen
	    
	    android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) playerView.getLayoutParams();
	    params.width =  metrics.widthPixels;
	    params.height = metrics.heightPixels;
	    params.leftMargin = 0;
	    //playerView.setLayoutParams(params);
	    
	    //FLAT TO WRAP OR FILL SCREEN http://blog.kasenlam.com/2012/02/android-how-to-stretch-video-to-fill.html
	    /*ON RELATIVELAYOUT PARENT <VideoView android:id="@+id/videoViewRelative"
	         android:layout_alignParentTop="true"
	         android:layout_alignParentBottom="true"
	         android:layout_alignParentLeft="true"
	         android:layout_alignParentRight="true"
	         android:layout_width="fill_parent"
	         android:layout_height="fill_parent">
	    </VideoView>*/
	    RelativeLayout.LayoutParams videoviewlp = new RelativeLayout.LayoutParams(params.width, params.height);
	    videoviewlp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
	    videoviewlp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
	    //playerView.setLayoutParams(videoviewlp);
	    //playerView.invalidate();
	    
	    playerView.setPlayPauseListener(new CustomVideoView.PlayPauseListener() {
		    @Override
		    public void onPlay() {
		        System.out.println("Play!");
		        playPauseIV.setImageResource(R.drawable.pause);
			    playPauseIV.setTag(R.drawable.pause);
			    
			    
			    
			      int time =  Math.round(playerView.getDuration()/1000);
		    	  int minutes = (int) Math.floor(time / 60);
			      int seconds = time % 60;
		    	  seekbar.setMax(time);	    	  	    	 
		    	  txtSeekbarMaxTime.setText(minutes + ":"+ seconds);
			      
			      
			      
			      Thread t = new Thread(new Runnable() { public void run() {
		    		  try{
		    		  while(playerView.isPlaying()==true ){	    			  	    			  
		    			  //Update the seekbar information on the controlsLayerView
		    	    	  runOnUiThread(new Runnable() {
		    	    		     @Override
		    	    		     public void run() {
		    	    		    	 int time =  Math.round(playerView.getCurrentPosition()/1000);
		    	    		    	 int minutes = (int) Math.floor(time / 60);
		    	    		    	 int seconds = time % 60;
		    	    		    	 
		    	    		    	 //Every 5 minutes send the watching video event to google analytics(this keeps the session alive)
		    	    		    	 if(minutes != 0 && minutes % 5 == 0 && seconds == 0) {
		    	    		    		//Report Event to Google Analytics
		    	    		   	  	  	Tracker t = ((MyApplication) MainActivity.this.getApplication()).getTracker(TrackerName.APP_TRACKER);
		    	    		   	  	  	t.send(new HitBuilders.EventBuilder()
		    	    		   	        .setCategory(getString(R.string.event_videoCategory))
		    	    		   	        .setAction(getString(R.string.event_videoWatching))
		    	    		   	        .setLabel(currentVideoListItem.channel_name)
		    	    		   	        .setValue(minutes)
		    	    		   	        .build());
		    	    		    	 }
		    	    		    	 
		    	    		    	 
		    	    		    	 
		    	    		    	 updateSeekbarInfo(time,false);		  
		    	    		    }
		    	    		});
		    	    	  Thread.sleep(1100);//1.1 seconds. The extra 100 miliseconds its for google analytics so we dont send twice the 5 min event
		    		  }

	   		  }catch(Exception ise){
	   			     //do nothing probably device go rotated
	   			     return;
	   			}
	   		  
	   	  }});
	   	  t.start();
			  
			      
		    }
		    @Override
		    public void onPause() {
		        System.out.println("Pause!");
		        playPauseIV.setImageResource(R.drawable.play);
			      playPauseIV.setTag(R.drawable.play);
		    }
		});

	     
	    setChannelModeDisplay();
    	
	
	    //Since TunerDataProvider.getChannelVideos uses a thread we need to do a while until it there is at least one element on the list    
	     
	     Thread thread = new Thread(new Runnable(){
			    @Override
			    public void run() {		     
				     while(tunerProvider.yTunervideoItemList.isEmpty()==true){ 	//while(tunerProvider.finishedProcessing == false){
				    	if(stopAllThreads==true) break;
				    	//Do nothing
				    }

				     //Start playing the video        
				     if(stopAllThreads==false) playNextVideo();  
				     
				     	//Ugly hack, put this in some other place
				     	//If after getting the results its empty it was not empty, maybe its because of a language change that was not recorded so we need to clear it
						//NOTE that getTunerVideos() makes a retry clearing slectedChannelsIds variable if selected channels_ids dont match with listing
						Log.d(DBG_TAG, "BEFORE COMPARE ");
						if(tunerProvider.slectedChannelsIds == ""){
							prefs.edit().putString(getString(R.string.pref_key_selected_channels), "").commit();
							Log.d(DBG_TAG, "AFTER COMPARE ");
						}
			}
		});
		thread.start(); 
			
		
		
    
    
  }
  
  
  
 

  @Override
  public void onDestroy(){
	  super.onDestroy();
	  
	  stopAllThreads = true;
	  //We need to close all windows/popupwindows so that we dont have errors  http://stackoverflow.com/questions/9251649/publisher-closed-input-channel-or-an-error-occurred-events-0x8
	  playerControlsLayerPW.dismiss();
	  playerEmptyLayerPW.dismiss();
	  Log.d(DBG_TAG, "ENTER DESTROY");
	  

	
	  
     //Only if we are not showing the showCloseFeedback, since if we exit here the feedback activity would be closed also, making it useless
	  if(showCloseFeedback==false)
		  System.exit(0); //Kills all threads also  
	  
	  
  }
  
  @Override
  public void onStart() {
    super.onStart();
    
    
    //Google Analytics. Get tracker.
    Tracker t = ((MyApplication) this.getApplication()).getTracker(TrackerName.APP_TRACKER);
    
    //http://stackoverflow.com/questions/22611295/android-google-analytics-availability-in-google-play-services
    //THIS IS THE REAL: http://developer.android.com/reference/com/google/android/gms/analytics/GoogleAnalytics.html
    //It reads on AndroidManifest.xml:  <meta-data android:name="com.google.android.gms.analytics.globalConfigResource" android:resource="@xml/app_tracker" /> 
    
    //OLD EasyTracker.getInstance(this).activityStart(this);  // Google Analytics
    GoogleAnalytics.getInstance(this).reportActivityStart(this); 
    
    //GoogleAnalytics.getInstance(this).getLogger().setLogLevel(LogLevel.VERBOSE);//**************************
    //GoogleAnalytics.getInstance(this).setDispatchPeriod(5); //GoogleAnalytics.getInstance(this).setLocalDispatchPeriod(5);
    //GoogleAnalytics.getInstance(this).setLocalDispatchPeriod(5);
    
    // Set screen name.
    // Where path is a String representing the screen name.
    t.setScreenName(null);
    // Send a screen view. (And session)
    //https://developers.google.com/analytics/devguides/collection/android/v4/sessions
    //http://stackoverflow.com/questions/19254023/short-session-lengths-in-google-analytics-for-android
    //t.send(new HitBuilders.AppViewBuilder().setNewSession().build());
    t.send(new HitBuilders.AppViewBuilder().build());
    
    
  }
  
  
  
  
  @Override
  public void onStop(){
	  super.onStop();
	  Log.d(DBG_TAG, "CALLED onStop()");
	  
	//http://stackoverflow.com/questions/22611295/android-google-analytics-availability-in-google-play-services
	  //OLD EasyTracker.getInstance(this).activityStop(this);  //Google Analytics.
	  GoogleAnalytics.getInstance(this).reportActivityStop(this);
	  
	  
  }
  
  
  @Override
  public void onPause(){
	  super.onPause();
	  
	  tunerProvider.stopTuning =true;
	  stopAllThreads = true;
//TEMP	  if( transitionLayerPW !=null) transitionLayerPW.dismiss();
	  
	  
	  
	  settingsLastLanguageSelected = prefs.getString("pref_key_content_language", "WW");
	  settingsLastCountrySelected = prefs.getString("pref_key_content_country", "WW");
	  
	  settingsLastChannelsSelected = prefs.getString(getString(R.string.pref_key_selected_channels), "");
	  
	  playerView.pause();//Pause the video
	  
	  
  }
  @Override
  public void onResume(){
	  super.onResume();
	  
	  
	  if(animation != null){//if is the first time loading the app
		  animation.start();
	  }
	  
	  
	  tunerProvider.stopTuning = false;
	  stopAllThreads = false;
	  startMonitoringThread();
	  if( playerControlsLayerPW !=null){
		  
		  /*playerControlsLayerPW = new PopupWindow(
		    		 controlsLayerView, 
		    		 LayoutParams.MATCH_PARENT, //Change this depending on size desired, ie fullscreen  
		    		 LayoutParams.MATCH_PARENT //Change this depending on size desired, ie fullscreen
	         );*/
//TEMPORAL		 playerControlsLayerPW.showAtLocation(new LinearLayout(this), Gravity.BOTTOM, 0, 0);
	  }
	  if( statusbarIV !=null){
		  hideStatusBar();//By some reason we cant resotre well to titlebar mode on resume, so we force fullscreen always on resume
		  
		  //if((Integer)statusbarIV.getTag() == 0)
     		// showStatusBar();
		  //else
     		 //hideStatusBar();
	  }
	  
	  
	  
	
	  
	  //If the users changed, country or language, restart the app
	  if(settingsLastLanguageSelected != null && settingsLastCountrySelected != null){
		  Log.d(DBG_TAG, "LAST LANG" + settingsLastLanguageSelected);
		  if( (settingsLastLanguageSelected != prefs.getString("pref_key_content_language", "WW")) || (settingsLastCountrySelected != prefs.getString("pref_key_content_country", "WW")) ){
			  Log.d(DBG_TAG, "LAST LANG2");
			  //clear selected channels first
			  prefs.edit().putString(getString(R.string.pref_key_selected_channels), "").commit();
			  loadDataChannelListing();
			  showProgressDialog();			  
			  loadDataChannelMode();
			  
			  /*Intent intent = getIntent();
			  System.exit(0);//finish();
			  startActivity(intent);
			  */
			  
			  /*
			  if (Build.VERSION.SDK_INT >= 11) {
				    recreate();
				} else {
				    Intent intent = getIntent();
				    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				    finish();
				    overridePendingTransition(0, 0);

				    startActivity(intent);
				    overridePendingTransition(0, 0);
				}
			  */
			  /*
			  Intent i = getBaseContext().getPackageManager()
			             .getLaunchIntentForPackage( getBaseContext().getPackageName() );
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			*/
			  
			  /* DISABLED
			  PendingIntent intent = PendingIntent.getActivity(this.getBaseContext(), 0, new Intent(getIntent()), getIntent().getFlags());
			    AlarmManager manager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
			    manager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, intent);
			    System.exit(0);
			    */
		  }else{
			  
			//If the user came from ChannelGuideAcitivty changed the selection of channels then lets reload data 
			  Log.d(DBG_TAG, "LAST SELECTED CHANNELS " + settingsLastChannelsSelected + "/" + prefs.getString(getString(R.string.pref_key_selected_channels), ""));
			  if(settingsLastChannelsSelected != null){
				  Log.d(DBG_TAG, "LAST SELECTED CHANNELS <> NULL");
				  if(settingsLastChannelsSelected != prefs.getString(getString(R.string.pref_key_selected_channels), "")){
					  loadDataChannelMode();
					  showProgressDialog();
					  
				  }else{
					  playerView.start();//Resume the video
				  }
			  }
			  
			  
		  }
			  
	  }
	  
	  
	
	  

  }
  

  public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_MENU) {
	    	//launch the settings Activity
	    	Intent intent = new Intent(MainActivity.this, SettingsActivity.class);	        	   
       	 	startActivity(intent);
	        return true;
	    }
	    return super.onKeyUp(keyCode, event);
	}
  
  @Override
  public void onBackPressed()
  {	  
	  
	  
	  
	  if(playerView != null){
		  if(playerView.isPlaying()==true){
			  //The first time lets close the app immideatly, so that user sees the Feedback dialog ASAP
			  if(prefs.getInt("app_launch_count", 0) == 1){
				  stopAllThreads = true;
				  showCloseFeedbackDialog();
			  }else{
				  showPlayerControls();
				  playerView.pause();//player.pause();  
			  }
			  	
			  
		  }else{
			  stopAllThreads = true;

			  if(prefs.getBoolean("close_feedback_sent", false) == false){
	    		  showCloseFeedbackDialog();
			  }else{
				  System.exit(0); //Kills all threads also
				  super.onBackPressed(); 
			  }
	    	  
		  }			  
	  }else{
		  stopAllThreads = true;
		  if(prefs.getBoolean("close_feedback_sent", false) == false){
    		  showCloseFeedbackDialog();
		  }else{			  
			  super.onBackPressed(); 
		  }
	  }
	  
  }
  
  public boolean onKeyDown(int keyCode, KeyEvent event) 
  { 
     /*if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) { 
         if( keyCode == KeyEvent.KEYCODE_VOLUME_UP) playNextVideo();
         return true;
     } else {
         return super.onKeyDown(keyCode, event); 
     }
     */
	  return super.onKeyDown(keyCode, event); 
  }

  /*
  @Override
  public boolean dispatchTouchEvent(MotionEvent event) {
	  //http://stackoverflow.com/questions/11001282/ontouchevent-not-working-on-child-views
	  //TODO: Doesnt works, maybe becasue of the controlslayer is in a popup view, would have to override this for a view class...seee
	  //http://stackoverflow.com/questions/9586032/android-difference-between-onintercepttouchevent-and-dispatchtouchevent
	  	  
	  Log.d(DBG_TAG, "TOUCH EVENT RECEIVED");
	  startTimeShowPlayerControls = System.nanoTime();//Restart the showplayercontrols counter on any touch received 
	  
	  //if (!gestureDetector.onTouchEvent(event))
        //  return super.onTouchEvent(event);
	  
	  return super.dispatchTouchEvent(event);
	  //super.dispatchTouchEvent(event);
	  //return false;
	  
  }
	
  @Override
  public boolean onTouchEvent(MotionEvent event) {
      if (!gestureDetector.onTouchEvent(event))
          return super.onTouchEvent(event);
      return true;
  }
  */
  
  
  


  @Override
  public void onClick(View v) {
	 
    //player.setFullscreen(!fullscreen);
    
  }
  
  /*
   * Checks to see if there is a new app update on the store.
   * It does so by invoking a url file on the website which holds the current version number.
   */
  public void checkAppUpdate(){
	  Thread t = new Thread(new Runnable() { public void run() {
  		  try{
  			  
  			  String url = TunerDataProvider.contentSources[0]+"/ytuner/tuner/appversion";
  			  
  			  //We make a post request to make sure there is noone on the middle making cache
  			  HttpPost httpPost = new HttpPost(url);
			  List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);			  			  
		      nameValuePairs.add(new BasicNameValuePair("something", "some"));//just something			  			  
		      httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
  			  
  			  //HttpGet httpGet = new HttpGet(url);
  			  
			  HttpParams httpParameters = new BasicHttpParams();
			  // Set the timeout in milliseconds until a connection is established.
			  // The default value is zero, that means the timeout is not used. 
			  int timeoutConnection = 3000;//We set it a bit high because it could be on edge/3g
			  HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			  // Set the default socket timeout (SO_TIMEOUT) 
			  // in milliseconds which is the timeout for waiting for data.
			  int timeoutSocket = 6000;//We set it a bit high because it could be on edge/3g
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
	                        	//If there is a error, do nothing, keep with the app flow
	                        	//This is not strictly obligated anyway	                        	
	                        	//e.printStackTrace();
	                        }
	                        
	                    } else {
	                    	//If there is a error, do nothing, keep with the app flow
                        	//This is not strictly obligated anyway
	                        //throw new ClientProtocolException("Unexpected response status: " + status);
	                    }
	                    return null;
	                }
	            };
			  						  
			  //HttpResponse response = httpClient.execute(httpGet);
			  String responseString = httpClient.execute(httpPost, responseHandler);
			  if(responseString!=null){
				  int lastestAppVersionCode = (int) Integer.valueOf(responseString.toString());
				  PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);				  
				  int currentVersioncode = pInfo.versionCode;
				  
				  if(lastestAppVersionCode > currentVersioncode){
					  
	    	    	  runOnUiThread(new Runnable() {
	    	    		     @Override
	    	    		     public void run() {
	    	    		    	 
	    	    		    	 AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create(); //Read Update
		   					      alertDialog.setTitle(getString(R.string.app_update_notify));
		   					      alertDialog.setMessage(getString(R.string.app_update_notify_desc));
		   				
		   					      alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok),  new DialogInterface.OnClickListener() {
		   					          public void onClick(DialogInterface dialog, int which) {
		   					        	 //Send user to Google Play store
		   					         	try {
		   				            		MainActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + MainActivity.this.getPackageName())));
		   				        		} catch (ActivityNotFoundException e) {
		   				        			String str ="https://play.google.com/store/apps/details?id=" + MainActivity.this.getPackageName();
		   				        		 	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(str));
		   				        		 	MainActivity.this.startActivity(browserIntent);        	    	    	
		   				        		}		   					        	  
		   					        	
		   					          }
	   					       });
	   					      alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),  new DialogInterface.OnClickListener() {
	   					         public void onClick(DialogInterface dialog, int which) {           
	   					      	   //Do nothing
	   					      	   
	   					         }
	   					      });	   					
	   					      alertDialog.show(); 
	    	    
	    				   	  }
	    	    		});

				  }
				  
			  }

	   		  }catch(Exception ise){
	   			     //do nothing probably device go rotated
	   		  }

	   	  }});
	   	  t.start();
  }

  public void startMonitoringThread(){
	//Check for connection/response errors and that stuff
	    Thread errorCheckThread = new Thread(new Runnable(){	  	  
		    @Override
		    public void run() {  	    	
			     while(1==1 && stopAllThreads==false){//This thread its inifinite as long as the app is running
			    	 
			    	 if(tunerProvider.channelListingConnectionError==true){
			    		 Log.d(DBG_TAG, "thread error true channelListingConnectionError");
		    	    	  runOnUiThread(new Runnable() {
		    	    		     @Override
		    	    		     public void run() {
		    	    		    	 if(validateInternetConnection()==true){//If the divice is connected to the internet
		    	    		    		 Log.d(DBG_TAG, "thread error true2 x");
			    	    		    	 AlertDialog connErrorDialog = new AlertDialog.Builder(MainActivity.this).create(); //Read Update
			    				   	      connErrorDialog.setTitle(getString(R.string.server_conn_error));
			    				   	      connErrorDialog.setMessage(getString(R.string.server_conn_error_desc));		
			    				   	      connErrorDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.exit_app),  new DialogInterface.OnClickListener() {
			    				   	          public void onClick(DialogInterface dialog, int which) {              
			    				   	        	System.exit(0); //Kills all threads also      	   
			    				   	          }
			    				   	       });
			    				   	      connErrorDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.retry),  new DialogInterface.OnClickListener() {
			    				   	         public void onClick(DialogInterface dialog, int which) {              
			    				   	        	tunerProvider.getChannelListng(prefs.getString("pref_key_content_language", "WW")
			    				   	         		,prefs.getString("pref_key_content_country", "WW")
			    				   	         		,Locale.getDefault().getLanguage()
			    				   	         		);//Retry      	   
			    				   	         }
			    				   	      });	    				   	
			    				   	      connErrorDialog.show(); 
		    	    		    	 }
		    				   	  }
		    	    		});

				   	      tunerProvider.channelListingConnectionError=false;//Reset it		    		
			    	 }
			    	 
			    	 
			    	 if(tunerProvider.getTunerVideosConnectionError==true){
			    		 Log.d(DBG_TAG, "thread error true getTunerVideosConnectionError");
		    	    	  runOnUiThread(new Runnable() {
		    	    		     @Override
		    	    		     public void run() {
		    	    		    	 if(validateInternetConnection()==true){//If the divice is connected to the internet
		    	    		    		 Log.d(DBG_TAG, "thread error true2 y");
			    	    		    	 AlertDialog connErrorDialog = new AlertDialog.Builder(MainActivity.this).create(); //Read Update
			    				   	      connErrorDialog.setTitle(getString(R.string.server_conn_error));
			    				   	      connErrorDialog.setMessage(getString(R.string.server_conn_error_desc));		
			    				   	      connErrorDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.exit_app),  new DialogInterface.OnClickListener() {
			    				   	          public void onClick(DialogInterface dialog, int which) {              
			    				   	        	System.exit(0); //Kills all threads also      	   
			    				   	          }
			    				   	       });
			    				   	      connErrorDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.retry),  new DialogInterface.OnClickListener() {
			    				   	         public void onClick(DialogInterface dialog, int which) {              
			    				   	        	loadDataChannelMode();//Retry    	   
			    				   	         }
			    				   	      });	    				   	
			    				   	      connErrorDialog.show(); 
		    	    		    	 }
		    				   	  }
		    	    		});

				   	      tunerProvider.getTunerVideosConnectionError=false;//Reset it		    		
			    	 }
			    	 

		
			    	 Log.d(DBG_TAG, "thread check error");
	 	
			    	 try { 			  		
		    	    	  	Thread.sleep(1000); 			  		 
					  } catch (InterruptedException e) {
							e.printStackTrace();
					  }
			    }  
			}
		});
	    errorCheckThread.start();//TEMP //TODO: DISBLE 
	  
	  
	  
  }
  
  
  public void showCloseFeedbackDialog(){
	  showCloseFeedback = true;
	
	  SharedPreferences.Editor editor = prefs.edit();
	  editor.putBoolean("close_feedback_sent", true);
	  editor.commit();
	  
	  Intent intent = new Intent(this, CloseFeedbackActivity.class);	        	   
 	 startActivity(intent);
 	finish();//Finish this current activity
  }
  
  //This method is needed due to some limitations in image size on some devices
  //http://stackoverflow.com/questions/12651777/android-bitmap-loading-out-of-memory-error-galaxy-s3-wxga
  //http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
  public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inDither=false;//ADEDD
	    options.inPurgeable=true;//ADEDD
	    options.inInputShareable=true;//ADEDD
	    options.inPreferredConfig = Bitmap.Config.RGB_565;//ADDED
	    options.inScaled = false;//ADDED http://stackoverflow.com/questions/12651777/android-bitmap-loading-out-of-memory-error-galaxy-s3-wxga
	    //options.inTempStorage=new byte[16 * 1024];//ADEDD
	    
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}
  
  
  //http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
  public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
  // Raw height and width of image
  final int height = options.outHeight;
  final int width = options.outWidth;
  int inSampleSize = 1;

  if (height > reqHeight || width > reqWidth) {

      final int halfHeight = height / 2;
      final int halfWidth = width / 2;

      // Calculate the largest inSampleSize value that is a power of 2 and keeps both
      // height and width larger than the requested height and width.
      while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
          inSampleSize *= 2;
      }
      //TODO: make resize so that image gets its aspect ratio distortioned for some circumstances
	  /*// Calculate ratios of height and width to requested height and width
      final int heightRatio = Math.round((float) height / (float) reqHeight);
      final int widthRatio  = Math.round((float) width / (float) reqWidth);

      // Choose the smallest ratio as inSampleSize value, this will guarantee
      // a final image with both dimensions larger than or equal to the
      // requested height and width.
      inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
      */
      /*
      if (width > height) {
          inSampleSize = Math.round((float) width / (float) reqWidth);
      } else {
          inSampleSize = Math.round((float) height / (float) reqHeight);
      }*/
	  
  }

  return inSampleSize;
}

  
  
	void showChannelGuide(){
		
		//Report Event to Google Analytics
	  	  Tracker t = ((MyApplication) MainActivity.this.getApplication()).getTracker(TrackerName.APP_TRACKER);
	  	  t.send(new HitBuilders.EventBuilder()
	        .setCategory(getString(R.string.event_configCategory))
	        .setAction(getString(R.string.event_configChannelGuide))
	        .setLabel(currentVideoListItem.channel_name)
	        .build());
		
		
		 Intent intent = new Intent(MainActivity.this, ChannelGuideActivity.class);
		 intent.putExtra("channelListingOBJ", (new Gson()).toJson(tunerProvider.channelListing));
    	 startActivity(intent);
		
	}
	
	void setChannelModeDisplay(){
		 DisplayMetrics metrics = new DisplayMetrics(); 
		 getWindowManager().getDefaultDisplay().getMetrics(metrics);
		 
		 
     	 txvCurrentChanNum.setTextSize(TypedValue.COMPLEX_UNIT_PX,(float)(metrics.heightPixels*0.17)); txvCurrentChanNum.setShadowLayer(3.0f, 1, 1,  Color.DKGRAY);	      	
		 
	}
	
	void loadDataChannelListing(){
		tunerProvider.getChannelListng(prefs.getString("pref_key_content_language", "WW")
	    		,prefs.getString("pref_key_content_country", "WW")
	    		,Locale.getDefault().getLanguage()
	    		);
	}
	
	void loadDataChannelMode(){
		
		sessionHistory.clear();
		currentVideoIndex = -1;
		
		//Lets set the selected channelIds
		//If we are returning from ChannellistActivity, we need to update the selected channels
		tunerProvider.slectedChannelsIds = prefs.getString(getString(R.string.pref_key_selected_channels), "");		
		
		
		/*SharedPreferences.Editor editor = prefs.edit();
		editor.putString(getString(R.string.pref_key_selected_channels), tunerProvider.slectedChannelsIds);
		editor.commit();	*/
		
			tunerProvider.getTunerVideos(prefs.getString("pref_key_content_language", "WW")
		    		,prefs.getString("pref_key_content_country", "WW")
		    		,Locale.getDefault().getLanguage()
		    		);
			
			
			
				
			
		
	}
  
  void updateSeekbarInfo(int currentSeconds,boolean updateVideo){
	  int time = currentSeconds;
	  int minutes = (int) Math.floor(time / 60);
 	 int seconds = time % 60;
 	 txtSeekbarCurrentTime.setText(minutes + ":"+ seconds);
 	 seekbar.setProgress(time);
 	 
 	 if(updateVideo==true) playerView.seekTo(time*1000);//player.seekToMillis(time*1000);
 	 
  }

  private void showButtonsMenu() {	  
	//Report Event to Google Analytics
  	  	Tracker t = ((MyApplication) MainActivity.this.getApplication()).getTracker(TrackerName.APP_TRACKER);
  	  	t.send(new HitBuilders.EventBuilder()
        .setCategory(getString(R.string.event_configCategory))
        .setAction(getString(R.string.event_configShowMenu))
        .setLabel(currentVideoListItem.channel_name)
        .build());
	  
	  
	  menuIV.setTag(1);
	  //txvCurrentTitle.setText("");
	  txvCurrentTitle.setLayoutParams( new LayoutParams(0, LayoutParams.WRAP_CONTENT, 50));
	  statusbarIV.setLayoutParams( new LayoutParams(0, LayoutParams.MATCH_PARENT, 10));
	  shareIV.setLayoutParams( new LayoutParams(0, LayoutParams.MATCH_PARENT, 10));
	  infoIV.setLayoutParams( new LayoutParams(0, LayoutParams.MATCH_PARENT, 10));
	  /*searchIV.setLayoutParams( new LayoutParams(0, LayoutParams.MATCH_PARENT, 10));
	  relatedIV.setLayoutParams( new LayoutParams(0, LayoutParams.MATCH_PARENT, 10));	  
	  
	  scanIV.setLayoutParams( new LayoutParams(0, LayoutParams.MATCH_PARENT, 10));    
	  helpIV.setLayoutParams( new LayoutParams(0, LayoutParams.MATCH_PARENT, 10));*/
	  configIV.setLayoutParams( new LayoutParams(0, LayoutParams.MATCH_PARENT, 10));
	  menuIV.setLayoutParams( new LayoutParams(0, LayoutParams.MATCH_PARENT, 10));
	  
	  
  }
  
  private void hideButtonsMenu() {	
	//Report Event to Google Analytics
  	  	Tracker t = ((MyApplication) MainActivity.this.getApplication()).getTracker(TrackerName.APP_TRACKER);
  	  	t.send(new HitBuilders.EventBuilder()
        .setCategory(getString(R.string.event_configCategory))
        .setAction(getString(R.string.event_configHideMenu))
        .setLabel(currentVideoListItem.channel_name)
        .build());
	  
	  menuIV.setTag(0);
	  txvCurrentTitle.setText(txvCurrentTitle.getTag().toString());
	  txvCurrentTitle.setLayoutParams( new LayoutParams(0, LayoutParams.WRAP_CONTENT, 70));
	  statusbarIV.setLayoutParams( new LayoutParams(0, LayoutParams.MATCH_PARENT, 10));	  
	  shareIV.setLayoutParams( new LayoutParams(0, LayoutParams.MATCH_PARENT, 10));
	  /*searchIV.setLayoutParams( new LayoutParams(0, LayoutParams.MATCH_PARENT, 0));
	  relatedIV.setLayoutParams( new LayoutParams(0, LayoutParams.MATCH_PARENT, 8));
	  
	  scanIV.setLayoutParams( new LayoutParams(0, LayoutParams.MATCH_PARENT, 0));    
	  helpIV.setLayoutParams( new LayoutParams(0, LayoutParams.MATCH_PARENT, 0));*/
	  infoIV.setLayoutParams( new LayoutParams(0, LayoutParams.MATCH_PARENT, 0));
	  configIV.setLayoutParams( new LayoutParams(0, LayoutParams.MATCH_PARENT, 0));
	  menuIV.setLayoutParams( new LayoutParams(0, LayoutParams.MATCH_PARENT, 10));
  }
  
  private void hideStatusBar() {
	  statusbarIV.setTag(0);
	  statusbarIV.setImageResource(R.drawable.icon_statusbar_down);
	  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	  getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
  }
  
  private void showStatusBar() {
	  statusbarIV.setTag(1);
	  statusbarIV.setImageResource(R.drawable.icon_statusbar_up);
	  getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	  getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
  }
  
  private void hideStatusBar2() {

	  statusbarIV.setTag(0);
	  statusbarIV.setImageResource(R.drawable.icon_statusbar_down);
	  //player.setFullscreen(true);//!fullscreen);
      //WindowManager.LayoutParams attrs = getWindow().getAttributes();
      //attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
      //getWindow().setAttributes(attrs);
	  getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
      
      
      playerControlsLayerPW.dismiss();
      playerControlsLayerPW.setWidth(LayoutParams.MATCH_PARENT);
      playerControlsLayerPW.setHeight(LayoutParams.MATCH_PARENT);
//TEMPORAL      playerControlsLayerPW.showAtLocation(new LinearLayout(this), Gravity.BOTTOM, 0, 0); //We user Gravity.BOTTOM instead of CENTER, the PW is full size anyway
      
//TEMP      transitionLayerPW.dismiss();
//TEMP	  transitionLayerPW.setWidth(LayoutParams.MATCH_PARENT);
//TEMP	  transitionLayerPW.setHeight(LayoutParams.MATCH_PARENT);
      
  }

  private void showStatusBar2() {
	  
	  
	  statusbarIV.setTag(1);
	  statusbarIV.setImageResource(R.drawable.icon_statusbar_up);
	  //player.setFullscreen(false);
	  
	     
      //WindowManager.LayoutParams attrs = getWindow().getAttributes();
      //attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
      //getWindow().setAttributes(attrs);
      getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
      
      
	  
	  
	  Rect rectgle= new Rect();
	  Window window= getWindow();
	  window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
	  int StatusBarHeight= rectgle.top;	  	 
	  
	  
	  int result = 0;
	   int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
	   if (resourceId > 0) 
	      result = getResources().getDimensionPixelSize(resourceId);
	  
	  
	   playerControlsLayerPW.dismiss();
	   playerControlsLayerPW.setWidth(LayoutParams.MATCH_PARENT);
	  playerControlsLayerPW.setHeight(rectgle.height()-(result));
	  playerControlsLayerPW.showAtLocation(new LinearLayout(this), Gravity.BOTTOM, 0, 0); //We user Gravity.BOTTOM instead of CENTER, the PW is full size anyway
	  
//TEMP	  transitionLayerPW.dismiss();
//TEMP	  transitionLayerPW.setWidth(LayoutParams.MATCH_PARENT);
//TEMP	  transitionLayerPW.setHeight(rectgle.height()-(result));
      
  }
  
  private YTubeVideoItem GetUrlRes(int i)
  {
      String s = "";
      
      switch (i) {
      case 144:  s = "3gp";
               break;
      case 240:  s = "3gp";
               break;
      case 360:  s = "mp4";
               break;
      case 480:  s = "mp4";
               break;
      case 720:  s = "mp4";
               break;
      case 1080:  s = "mp4";
               break;
      
      }

      int j = 0;
//_L7:
      while(1==1){
	      if (j >= currentVideoListItem.ytubevideoItems.size())
	      {
	          return null;
	      }
	      //break MISSING_BLOCK_LABEL_118;
	
	      //Log.d("res", (new StringBuilder(String.valueOf(((YoutubeItem)urls.get(j)).hight_vedio))).append(",").append(((YoutubeItem)urls.get(j)).Extention).toString());
	      if (((YTubeVideoItem)currentVideoListItem.ytubevideoItems.get(j)).height_video == i && ((YTubeVideoItem)currentVideoListItem.ytubevideoItems.get(j)).Extention.equals(s))
	      {
	          return (YTubeVideoItem)currentVideoListItem.ytubevideoItems.get(j);
	      }
	      j++;
  	}
  }
  
  YTubeVideoItem getVideoItemByQuality(){
	  YTubeVideoItem ytviditem = null;
	  Iterator it;
	  

	  if(prefs.getString("pref_key_default_quality", "low").toString() == "verylow"){
		  	it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "verylow") return ytviditem;
			}
			it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "low") return ytviditem;
			}
			it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "medium") return ytviditem;
			}
			it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "mediumhigh") return ytviditem;
			}
			it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "high") return ytviditem;
			}
			it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "veryhigh") return ytviditem;
			}
			 
	  }else if(prefs.getString("pref_key_default_quality", "low").equals("low")){
		  	it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "low") return ytviditem;
			}
		  	it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "verylow") return ytviditem;
			}			
			it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "medium") return ytviditem;
			}
			it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "mediumhigh") return ytviditem;
			}
			it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "high") return ytviditem;
			}
			it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "veryhigh") return ytviditem;
			}
		  
	  }else if(prefs.getString("pref_key_default_quality", "low").equals("medium")){
		  	it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "medium") return ytviditem;
			}
			it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "mediumhigh") return ytviditem;
			}
		  	it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "low") return ytviditem;
			}		  	
			it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "high") return ytviditem;
			}
			it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "verylow") return ytviditem;
			}	
			it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "veryhigh") return ytviditem;
			}
		  
	  } else if(prefs.getString("pref_key_default_quality", "low").equals("mediumhigh")){
		  	it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "mediumhigh") return ytviditem;
			}
			it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "high") return ytviditem;
			}
			it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "medium") return ytviditem;
			}
			it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "veryhigh") return ytviditem;
			}		  			
		  	it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "low") return ytviditem;
			}		  	
			
			it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "verylow") return ytviditem;
			}	
					  
	  } else if(prefs.getString("pref_key_default_quality", "low").equals("high")){
		 it = currentVideoListItem.ytubevideoItems.iterator();
		 while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "high") return ytviditem;
			}
		it = currentVideoListItem.ytubevideoItems.iterator();
		while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "veryhigh") return ytviditem;
		}
		it = currentVideoListItem.ytubevideoItems.iterator();
		while (it.hasNext()) {
			 ytviditem =  (YTubeVideoItem) it.next();
			 if(ytviditem.quality == "mediumhigh") return ytviditem;
		}
		
		it = currentVideoListItem.ytubevideoItems.iterator();
		while (it.hasNext()) {
			 ytviditem =  (YTubeVideoItem) it.next();
			 if(ytviditem.quality == "medium") return ytviditem;
		}
				  			
	  	it = currentVideoListItem.ytubevideoItems.iterator();
		while (it.hasNext()) {
			 ytviditem =  (YTubeVideoItem) it.next();
			 if(ytviditem.quality == "low") return ytviditem;
		}		  	
		
		it = currentVideoListItem.ytubevideoItems.iterator();
		while (it.hasNext()) {
			 ytviditem =  (YTubeVideoItem) it.next();
			 if(ytviditem.quality == "verylow") return ytviditem;
		}	
		
	  
	  } else if(prefs.getString("pref_key_default_quality", "low").equals("veryhigh")){
		  it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
					 ytviditem =  (YTubeVideoItem) it.next();
					 if(ytviditem.quality == "veryhigh") return ytviditem;
			} 
		  it = currentVideoListItem.ytubevideoItems.iterator();
			 while (it.hasNext()) {
					 ytviditem =  (YTubeVideoItem) it.next();
					 if(ytviditem.quality == "high") return ytviditem;
				}
			
			it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "mediumhigh") return ytviditem;
			}
			
			it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "medium") return ytviditem;
			}
					  			
		  	it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "low") return ytviditem;
			}		  	
			
			it = currentVideoListItem.ytubevideoItems.iterator();
			while (it.hasNext()) {
				 ytviditem =  (YTubeVideoItem) it.next();
				 if(ytviditem.quality == "verylow") return ytviditem;
			}						  
	  }
	  return ytviditem;
	  
  }
  
  /**
 	* Returns the seconds number at which to start a video.
	* @param videoDuration the video duration in youtube api format. ie: "duration": "PT23M28S"
	* @return
	*/
	int getVideoStartingTime(String videoDuration){
		//TODO: change this to new url_info format ie: 04:26
		myPattern = Pattern.compile("^.*([0-9]+)H([0-9]+)M([0-9]+)S");
		//TODO: fix error here Log.d(DBG_TAG,"DURRRACEL: "+videoDuration);
		Matcher m = myPattern.matcher(videoDuration);
		myPattern = Pattern.compile("^([A-Z]+)([0-9]+)M([0-9]+)S");
		Matcher m2 = myPattern.matcher(videoDuration);

		if (m.find()) {
			int hoursSecs = 60  * (60 * Integer.parseInt(m.group(1).toString()));//in secs
			int totalSeconds = (60 * Integer.parseInt(m.group(2).toString()));
			totalSeconds += Integer.parseInt(m.group(3).toString());
			totalSeconds += hoursSecs;
		    		    	   
		    if(totalSeconds > 3000)
		    	return 700;
		    if(totalSeconds > 900)
		    	return 180;
		}
		
		if (m2.find()) {
			
			int totalSeconds = (60 * Integer.parseInt(m2.group(2).toString()));
			totalSeconds += Integer.parseInt(m2.group(3).toString());
			
		  
		    		    	   
		    if(totalSeconds > 3000)
		    	return 700;
		    if(totalSeconds > 900)
		    	return 180;
		}
	  return 0;
	}
	
  
  boolean loadCurrentVideo(){
	  //TODO:Fix this error Regex.Matcher error finding this
	  /*int startTimeMillis;
	   		Log.d(DBG_TAG,"DURRR: "+currentVideoListItem.duration);
	  if(prefs.getBoolean("pref_key_random_start", true) == true)
		  startTimeMillis = getVideoStartingTime(currentVideoListItem.duration) * 1000;//* 1000 to convert to millis
	  else
		  startTimeMillis = 0;*/
	  	  
	  /*if (startTimeMillis == 0)
		  player.loadVideo(currentVideoListItem.videoID);
	  else
		  player.loadVideo(currentVideoListItem.videoID,startTimeMillis);*/
	  
	  //Find the valid url for the current user video quality preferences(low,high,medium)
	  YTubeVideoItem ytviditem = getVideoItemByQuality();
	  if (ytviditem == null) return false;///There is nothing to do, no videos urls were found
	  
	  
	  
	  
		//VieoView HOW TO http://androidexample.com/index.php?view=article_discription&aid=124&aaid=144
      final Uri uri = Uri.parse(ytviditem.DownloadUrl);

      runOnUiThread(new Runnable() {
    	   public void run() {
    		   
    		   if(animation != null){ 
		   			animation.stop();   		   
		   			animation = null;
		   		}

    		   playerView.setVideoURI(uri);
    		      //playerView.requestFocus();
    		   		
    		   		
    		   		playerView.setVisibility(View.VISIBLE);
    		      playerView.start();

    	  }
    	});
      
      
      return true;
  }
  
 
  
  
  /*
   * Play previous video, but also track the event to Google Analytics
   */
  void changePreviousVideo(){
	  
	//Report Event to Google Analytics
  	  Tracker t = ((MyApplication) MainActivity.this.getApplication()).getTracker(TrackerName.APP_TRACKER);
  	  t.send(new HitBuilders.EventBuilder()
        .setCategory(getString(R.string.event_videoCategory))
        .setAction(getString(R.string.event_videoChangePrevious))
        .setLabel(currentVideoListItem.channel_name)
        .build());
	  
  	  playPreviousVideo();
	  
  }
  
  /*
   * Play next video, but also track the event to Google Analytics
   */
  void changeNextVideo(){
	//Report Event to Google Analytics
  	  Tracker t = ((MyApplication) MainActivity.this.getApplication()).getTracker(TrackerName.APP_TRACKER);
  	  t.send(new HitBuilders.EventBuilder()
        .setCategory(getString(R.string.event_videoCategory))
        .setAction(getString(R.string.event_videoChangeNext))
        .setLabel(currentVideoListItem.channel_name)
        .build());
	  
	  playNextVideo();
	  
  }
  
  /*
   * Play next video, but also track the event to Google Analytics
   */
  void goNextVideo(){
	//Report Event to Google Analytics
  	  Tracker t = ((MyApplication) MainActivity.this.getApplication()).getTracker(TrackerName.APP_TRACKER);
  	  t.send(new HitBuilders.EventBuilder()
        .setCategory(getString(R.string.event_videoCategory))
        .setAction(getString(R.string.event_videoGoNext))
        .setLabel(currentVideoListItem.channel_name)
        .build());
  	  
  	  playNextVideo();
  }
  
  
  
  void playPreviousVideo(){
	  lastUserActionPlayPrevious = true;
	  
	  
	  for (int i = 0; i < sessionHistory.size(); i++) {
		   if (sessionHistory.get(i).equals(currentVideoIndex)) {
		    
		    
		    if(i == 0)
		    	currentVideoIndex = sessionHistory.get(sessionHistory.size()-1);
		    else
		    	currentVideoIndex = sessionHistory.get(i-1);	
		    break;
		  }
		}
	  
	  /*
	  if(currentVideoIndex == 0)
		  currentVideoIndex =  tunerProvider.yTunervideoItemList.size()-1;
	  else
		  currentVideoIndex--;
	  */
	  playNextVideo(true);
	  
  }
  
  void playNextVideo(){
	  

	  
	  Log.d(DBG_TAG, "PLAY NEXT VIDEO" + tunerProvider.yTunervideoItemList.size());
	  lastUserActionPlayPrevious = false;
	 //TODO:TEMP 
	 /*//TEMP if( sessionHistory.contains( currentVideoIndex )){
		  for (int i = 0; i < sessionHistory.size(); i++) {
			   if (sessionHistory.get(i).equals(currentVideoIndex)) {
				   if(i == sessionHistory.size()-1){
					   playNextVideo(false);
				   }else{
					   currentVideoIndex = sessionHistory.get(i+1);
					   playNextVideo(true);					   
				   }
				   break;
			   }
		  }
		  
	  }else{*/
		  playNextVideo(false);  
	  /*}*/
	  
	  
  }

   
  void playNextVideo(boolean goPreviousVideo){
	  playerControlsLayerPW.dismiss();
	  videoFirstPlayback = true;
	  
	  

	  
	  
	  if (tunerProvider.yTunervideoItemList.size()==0) return;//We can't do nothing as the list is empty(the getvideos thread might still be working). The user needs to triger this method again.
	  Iterator testEentries = tunerProvider.yTunervideoItemList.iterator();
	  while (testEentries.hasNext())  Log.d(MainActivity.DBG_TAG,"CURRENT videoIds: " +((YTunerVideoItem) testEentries.next()).videoID);
	  if(goPreviousVideo==false){		  
		  currentVideoIndex++;	  	  	  
		  if(currentVideoIndex >= tunerProvider.yTunervideoItemList.size() ){
			  
			runOnUiThread(new Runnable() {
    		     @Override
    		     public void run() {	    		    	 
    		    	 showProgressDialog();
    		   }
    		});
			  
			  loadDataChannelMode();
			  playNextVideo(goPreviousVideo);
			  return;
			  //currentVideoIndex = 0;//Reset the counter if we have reached the end
		  }
		 
	  }
	  

	  
	  //if(currentVideoIndex >= tunerProvider.yTunervideoItemList.size() ) currentVideoIndex = 0;//We need to do this again as findNextUnwatchedVideo() might got incorrect. TODO: check this 
	  currentVideoListItem = tunerProvider.yTunervideoItemList.get(currentVideoIndex);
	  
	//Write to users history
	  if(! sessionHistory.contains( currentVideoIndex)) sessionHistory.add(currentVideoIndex);	 
	  
	  //Resume fetching new info to the cache if needed
	  if( tunerProvider.pauseTuning  == true && (tunerProvider.totalTuningVidItems - currentVideoIndex) <= 2) {
		  Log.d(DBG_TAG, "RESUME TIUNIMNG:"+currentVideoListItem.videoID);
		  tunerProvider.pauseTuning = false;//Resume tuning thread	  
	  }
	

	  
	  
	  
	  TextView txvTitle = (TextView)transitionLayerView.findViewById(R.id.video_title);
	  TextView txvDescription = (TextView)transitionLayerView.findViewById(R.id.video_description);
      String titleValue = (currentVideoListItem.title.length() > 100) ? currentVideoListItem.title.substring(0, 97) + "..." : currentVideoListItem.title;   
      String descValue = "";
      if(! (currentVideoListItem.description == null))
    	  descValue =  (currentVideoListItem.description.length() > 300) ? currentVideoListItem.description.substring(0, 297) + "..." : currentVideoListItem.description;
      txvTitle.setText(currentVideoListItem.channel_name);//txvTitle.setText(titleValue);
      //txvDescription.setText(descValue);
      TextView txvLoading = (TextView)transitionLayerView.findViewById(R.id.txv_loading);
      txvLoading.setText(currentVideoListItem.channel_number); //txvLoading.setText(currentVideoListItem.channel_name +" "+ currentVideoListItem.channel_number);
      
      //Set controls layer values
      txvCurrentTitle.setTag(titleValue);
      txvCurrentTitle.setText(titleValue);      
      txvCurrentChanNum.setText(String.valueOf(currentVideoListItem.channel_number));
                  
      Log.d(DBG_TAG, "CURRRRRRRRENT VIDEOOOOOOOO:"+currentVideoListItem.videoID);
	  
      
      //Show the details to the user on the pre-load screen layer            
      showTransitionLayer();
      
      //write to history
	  if(! tunerProvider.vidsHistory.contains( currentVideoListItem.videoID)){
		  OutputStreamWriter outputStreamWriter = null;
		  BufferedWriter bwriter = null;
		  try {
		        outputStreamWriter = new OutputStreamWriter(openFileOutput(HISTORY_FILE_NAME, Context.MODE_APPEND));
		        bwriter = new BufferedWriter(outputStreamWriter);
		        bwriter.newLine();
		        bwriter.write(currentVideoListItem.videoID);		        
		        tunerProvider.vidsHistory.add(currentVideoListItem.videoID);		        
		    }
		    catch (IOException e) {
		    	Log.e("ERROR","Oooops error writng to ytuner history file : "+currentVideoListItem.videoID +" - " +e.getMessage());		    			        
		    }finally{
		    	 try {
		    		 bwriter.close();
		    		 outputStreamWriter.close();		    		 
		    	 } catch (Exception e) {		    		 
		    	 }		    	
		    }
	  }
      
    //If there was an error loading current video go with next one and skip
	  if(loadCurrentVideo() == false) {
		  playNextVideo(goPreviousVideo);
		  return;
	  }
	  
	  
      
	  //If we are on debug or special moderator code is entered then send to the web vid history database for debugguing purposes 
      
      if ( BuildConfig.DEBUG == true || prefs.getString("pref_key_moderator_code", "").equals("debug")){
    	  
    	  Thread tr = new Thread(new Runnable() { public void run() {
      		  try{
      			  
      			  String url = TunerDataProvider.contentSources[0]+"/ytuner/tuner/historydebug";
      			  HttpPost httpPost = new HttpPost(url);
    				  List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
    				  
    			      nameValuePairs.add(new BasicNameValuePair("videoid", currentVideoListItem.videoID));    				  
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
    				  
    				  Log.d(DBG_TAG, "Sending debug history, dont worry if timeout");
    				  
    				  HttpResponse response = httpClient.execute(httpPost);

    	   		  }catch(Exception ise){
    	   			     //do nothing probably device go rotated
    	   		  }
    	   		  
      		  
    	   	  }});
    	   	  tr.start();
    	  
    	  
      }
      
       
  }
  
  
  int findNextUnwatchedVideo(){
	  
	  int index = currentVideoIndex;  	  
	  
	  while(index < tunerProvider.yTunervideoItemList.size() ){
		  YTunerVideoItem videoListItem = tunerProvider.yTunervideoItemList.get(index);
		  
		  if(! tunerProvider.vidsHistory.contains( videoListItem.videoID))
			  return index;
		    
		  index++;
	  }
	  
	  //if(index >= tunerProvider.yTunervideoItemList.size() )
		return currentVideoIndex;

	  
  }
  
  
  	
	void voteCurrentVideo(){
		
		
		if( prefs.getString("pref_key_moderator_code", "") != ""){
	    	Thread thread = new Thread(new Runnable(){			  			  	  
			    @Override
			    public void run() {			         
			  		  try {
			  			 String url = "http://protogab.com/ytuner/tuner/vote.php";//?mod_code=bguprt&videoid=OOsqkQytHOs;
			  			Log.d(DBG_TAG,"Swipe bottom2");
	
			  			HttpPost post = new HttpPost(url); 
			  			 HttpParams httpParameters = new BasicHttpParams();
						  int timeoutConnection = 3000;
						  HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
						  int timeoutSocket = 5000;
						  HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
						  DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
						  
			  		   
			  		      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			  		      nameValuePairs.add(new BasicNameValuePair("mod_code", prefs.getString("pref_key_moderator_code", "") ));				  		    
			  		      nameValuePairs.add(new BasicNameValuePair("videoid",currentVideoListItem.videoID));
			  		      post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			  		 
			  		      HttpResponse response = httpClient.execute(post);
			  		      BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			  		      final String line = "";
			  		      if( (toastMessage = rd.readLine()) != null ) {
			  		    	
			  		    	 runOnUiThread(new Runnable() {
			  		    	  public void run() {
			  		    	  	changeNextVideo();
			  		    		Toast.makeText(MainActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
			  		    	  }
			  		    	});
			  		    	 
			  		    	
			  		      }
	
			  		  }catch (Throwable t) {
			  			Log.d(DBG_TAG,t.getMessage());
			  			//Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
			  		  }
			    }
	    	});
	    	thread.start(); 
		}
	}
	
	
	void showTransitionLayer(){
		init_success = true;//for backgorud loadign animation
		runOnUiThread(new Runnable() {
			  public void run() {
				 try {
			    	  if(stopAllThreads==false){
			    		  playerEmptyLayerPW.dismiss();

			    		  Log.d(DBG_TAG,"SHOW TANSITION LAYER");
			    		  findViewById(R.id.main_layout).post(new Runnable() {
				   			   public void run() {
				   				   transitionLayerPW.showAtLocation(linearLayoutLL, Gravity.BOTTOM, 0, 0);
			    		  
				   			}
			    			});
			    	  }
			    	}
				    catch (Exception e) {
				    	e.printStackTrace();
				    }
			  }
		});
		     
	}
	
	void hideTransitionLayer(){
		runOnUiThread(new Runnable() {
	    	  public void run() {
	    		  findViewById(R.id.main_layout).post(new Runnable() {
	   			   public void run() {
	   				   playerEmptyLayerPW.showAtLocation(linearLayoutLL, Gravity.BOTTOM, 0, 0);	    			
	   				   if(transitionLayerPW != null){
	   					if(transitionLayerPW.isShowing()){
	   					   transitionLayerPW.dismiss();
	   					}
	   				   }
	   			   	}
	    			});
	    	  }
	    	});

		
		
	}
	
	
	void showPlayerControls(){
		playerEmptyLayerPW.dismiss();
		//transitionLayerPW.showAtLocation(linearLayoutLL, Gravity.BOTTOM, 0, 0);
		
		//We need to put this in a runnable because for some devices it does not works. It seems its too early
		//http://stackoverflow.com/questions/4187673/problems-creating-a-popup-window-in-android-activity
		findViewById(R.id.main_layout).post(new Runnable() {
			   public void run() {
				   playerControlsLayerPW.showAtLocation(linearLayoutLL, Gravity.BOTTOM, 0, 0);		
				   
				   
					Thread t = new Thread(new Runnable() { public void run() {
						startTimeShowPlayerControls = System.nanoTime(); 
						long elapsedTime = 0;
						double seconds= 0;
				  		  while(seconds < 3 && playerControlsLayerPW.isShowing()){
				  			elapsedTime = System.nanoTime() - startTimeShowPlayerControls;
							seconds = (double)elapsedTime / 1000000000.0;
							 try {
								 
				    	    	  	Thread.sleep(1000);
			 			  		 
							  } catch (InterruptedException e) {
									e.printStackTrace();
							  }
				  		  }
				  		  if(stopAllThreads==false){
					  		  if(playerControlsLayerPW.isShowing())
						    	  runOnUiThread(new Runnable() {
						    		     @Override
						    		     public void run() {
						    		    	 hidePlayerControls();
						    		    }
						    		});
				  		  }
					}
					});
					t.start();
	
			   }
		});
		

	}
	
	void hidePlayerControls(){
		playerControlsLayerPW.dismiss();
		if(stopAllThreads==false) {	
			playerEmptyLayerPW.showAtLocation(linearLayoutLL, Gravity.BOTTOM, 0, 0);
		}
		
		
		//Load the first time launch instructions
        if(prefs.getBoolean(getString(R.string.pref_swipe_instruc), false) == false){
        	if(currentVideoIndex >= 0){      		        		  
      		runOnUiThread(new Runnable() {
		    	  public void run() {  	
		    		  ImageView instructImageView = (ImageView)swipeInstrucView.findViewById(R.id.instructions_image);
		    		  Drawable myDraw = new BitmapDrawable(decodeSampledBitmapFromResource(getResources(), R.drawable.swipe_instruc, metrics.widthPixels, metrics.heightPixels));
		    		  //instructImageView.setImageDrawable(myDraw);
		    		  instructImageView.setBackgroundDrawable(myDraw);//Use background instead of src to make it alter apect ratio if needed:http://stackoverflow.com/questions/2521959/how-to-scale-an-image-in-imageview-to-keep-the-aspect-ratio 
		    		  //instructImageView.setImageResource(R.drawable.swipe_instruc);
		    		  playerView.pause();
		    		  userInstrucPW.showAtLocation(linearLayoutLL, Gravity.BOTTOM, 0, 0);    	
		    	  }
		    	});    	  
      		
      		  SharedPreferences.Editor editor = prefs.edit();
			  editor.putBoolean(getString(R.string.pref_swipe_instruc), true);
			  editor.commit();
      	  }      	 
        }
        
        if(prefs.getBoolean(getString(R.string.pref_tap_instruc), false) == false){
      	  if(currentVideoIndex >= 3){		  			  
  			 runOnUiThread(new Runnable() {
		    	  public void run() {  	
		    		  ImageView instructImageView = (ImageView)swipeInstrucView.findViewById(R.id.instructions_image);
		    		  Drawable myDraw = new BitmapDrawable(decodeSampledBitmapFromResource(getResources(), R.drawable.tap_instruc, metrics.widthPixels, metrics.heightPixels));
		    		  //instructImageView.setImageDrawable(myDraw);
		    		  instructImageView.setBackgroundDrawable(myDraw);//Use background instead of src to make it alter apect ratio if needed:http://stackoverflow.com/questions/2521959/how-to-scale-an-image-in-imageview-to-keep-the-aspect-ratio
		    		  //instructImageView.setImageResource(R.drawable.tap_instruc);
		    		  playerView.pause();
		    		  userInstrucPW.showAtLocation(linearLayoutLL, Gravity.BOTTOM, 0, 0);    	
		    	  }
		    	});   
  			  

  			  
  			  SharedPreferences.Editor editor = prefs.edit();
  			  editor.putBoolean(getString(R.string.pref_tap_instruc), true);
  			  editor.commit();
  			  
  		  }  		  
        }
        
        //Show the give us your comments/video suggestions etc on Social Networks
        if(currentVideoIndex >= 4){
        	if(prefs.getInt("socialfeedback_count", 0) <= 3){        		        		
        		Long lastDateSocialFeedback = prefs.getLong("last_date_socialfeedback", System.currentTimeMillis());
			    int daysUntilPrompt = 3;
			    
			    if (System.currentTimeMillis() >= lastDateSocialFeedback +  (daysUntilPrompt * 24 * 60 * 60 * 1000)) {
			    	
			    	prefs.edit().putLong("last_date_socialfeedback", System.currentTimeMillis()).commit();
			    	int SFShownCount = prefs.getInt("socialfeedback_count", 0);
			    	SFShownCount++;
			    	prefs.edit().putInt("socialfeedback_count", SFShownCount).commit();
			    	
			    	playerView.pause();
			    	
			    	 AlertDialog commentSocialDialog = new AlertDialog.Builder(MainActivity.this).create(); //Read Update
			   	      commentSocialDialog.setTitle(getString(R.string.social_feedback));
			   	      commentSocialDialog.setMessage(getString(R.string.social_feedback_desc));
			   	      
			   	      commentSocialDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.social_feedback_facebook),  new DialogInterface.OnClickListener() {
			   	          public void onClick(DialogInterface dialog, int which) {
			   	        	  
			   	        	//Report Event to Google Analytics
			   	       	  Tracker t = ((MyApplication) MainActivity.this.getApplication()).getTracker(TrackerName.APP_TRACKER);
			   	       	  t.send(new HitBuilders.EventBuilder()
			   	             .setCategory(getString(R.string.event_socialCategory))
			   	             .setAction(getString(R.string.event_socialFeedbackFacebook))			   	             
			   	             .build());
			   	        	  
			   	        	try {
			   	            			   	            
				   	          String uri = "facebook://facebook.com/pages/YTuner/614994511925662";
				   	          Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
				   	          startActivity(intent);
				   	         } catch (Exception e) {
				   	        	String str = "https://www.facebook.com/pages/YTuner/614994511925662";
					 		 	 Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(str));
					 		 	 MainActivity.this.startActivity(browserIntent);
				   	         }
			   	        	   
			   	          }
			   	       });
			   	      
			   	      commentSocialDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.social_feedback_plus),  new DialogInterface.OnClickListener() {
			   	          public void onClick(DialogInterface dialog, int which) {
			   	        	//Report Event to Google Analytics
				   	       	  Tracker t = ((MyApplication) MainActivity.this.getApplication()).getTracker(TrackerName.APP_TRACKER);
				   	       	  t.send(new HitBuilders.EventBuilder()
				   	             .setCategory(getString(R.string.event_socialCategory))
				   	             .setAction(getString(R.string.event_socialFeedbackPlus))			   	             
				   	             .build());
			   	        	  
			   	        	String str = "https://plus.google.com/109183165905480737468";
				 		 	 Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(str));
				 		 	 MainActivity.this.startActivity(browserIntent);
			   	          }
			   	       });
			   	      commentSocialDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.social_feedback_play),  new DialogInterface.OnClickListener() {
			   	          public void onClick(DialogInterface dialog, int which) {
				   	        	//Report Event to Google Analytics
					   	       	  Tracker t = ((MyApplication) MainActivity.this.getApplication()).getTracker(TrackerName.APP_TRACKER);
					   	       	  t.send(new HitBuilders.EventBuilder()
					   	             .setCategory(getString(R.string.event_socialCategory))
					   	             .setAction(getString(R.string.event_socialFeedbackPlay))			   	             
					   	             .build());
			   	        	  
			   	        	  		//Send user to Google Play store
						         	try {
					            		MainActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + MainActivity.this.getPackageName())));
					        		} catch (ActivityNotFoundException e) {
					        			String str ="https://play.google.com/store/apps/details?id=" + MainActivity.this.getPackageName();
					        		 	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(str));
					        		 	MainActivity.this.startActivity(browserIntent);        	    	    	
					        		}
			   	          }
			   	       });
			   	      	    				   	
			   	      commentSocialDialog.show(); 
			    	
			        //showRateDialog(mContext, editor);
			    }
        	}
        }

		
	}
	
	
	public void showProgressDialog() {
		final ProgressDialog ringProgressDialog;		
		
		ringProgressDialog = ProgressDialog.show(MainActivity.this, getString(R.string.loading), getString(R.string.please_wait), true);	
		
		
		ringProgressDialog.setCancelable(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {						    	 
			    	 if(currentVideoListItem != null){//If is not the app launch
			    		 while(tunerProvider.yTunervideoItemList.size()==0){
			    			 //Do Nothing
			    			 Thread.sleep(500);
			    		 }			    		 
			    	 }

				} catch (Exception e) {

				}
				if(stopAllThreads==false){
					runOnUiThread(new Runnable() {
		    		     @Override
		    		     public void run() {	    		    	 
		    		    	ringProgressDialog.dismiss();
		    				//Toast.makeText(MainActivity.this, getString(R.string.swipe_left), Toast.LENGTH_SHORT).show();
		    		    	playNextVideo();
		    		    }
		    		});
				}
			}
		}).start();
	}
	


  
  public boolean isConnectedToInternet(){
	  //TODO: add an extra validation by symply calling to a url (ie: http://youtube.com)and check if there is an actual response
	  //Because this method only check if its connected to the network but not if connected to the internet
	    ConnectivityManager connectivity = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
	      if (connectivity != null) 
	      {
	          NetworkInfo[] info = connectivity.getAllNetworkInfo();
	          if (info != null) 
	              for (int i = 0; i < info.length; i++) 
	                  if (info[i].getState() == NetworkInfo.State.CONNECTED)
	                  {
	                      return true;
	                  }

	      }
	      return false;
	}
  
  /*
   * Returns true if there is connection to the internet, otherwise false and an alert dialog.
   * NOTE: This only checks if the connection to internet is active, it does not truly checks if there is full connection.
   * ie: The user might be behind a Acces Point with requiered user authentication before full internet accces.
   */
  
  public boolean validateInternetConnection(){

	  //if(1==1) return true;//TEMP TODO: Disable here
	  if(isConnectedToInternet())
	  {
	     return true;
		  // Run AsyncTask
	   }
	  else
	  {
		       
	  	
	
	  	
	  	AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create(); //Read Update
	      alertDialog.setTitle(getString(R.string.no_internet_conn));
	      alertDialog.setMessage(getString(R.string.no_internet_conn_desc));
	      
	
	      
	      alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.exit_app),  new DialogInterface.OnClickListener() {
	          public void onClick(DialogInterface dialog, int which) {              
	        	  System.exit(0); //Kills all threads also
	       	   
	          }
	       });
	      alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.check_settings),  new DialogInterface.OnClickListener() {
	         public void onClick(DialogInterface dialog, int which) {              
	      	   startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
	      	   
	         }
	      });
	
	      alertDialog.show();  //<-- See This!
		    
	   }
	  
	  return false;
  }
  
  
  


  private final class YTunerPlayer{
	  boolean scanMode = false;


	/**
	 * @return the scanMode
	 */
	public boolean isScanMode() {
		return scanMode;
	}

	/**
	 * @param scanMode the scanMode to set
	 */
	public void setScanMode(boolean scanMode) {
		this.scanMode = scanMode;
	}
	  
  }
  
  
  
  
  
  
  /*NEWWW CHANGES ADDITONS */
  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width,
          int height) {
      
       
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    
       
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    
  }
  
  

}
