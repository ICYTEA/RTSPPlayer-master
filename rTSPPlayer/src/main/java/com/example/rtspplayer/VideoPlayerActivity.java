package com.example.rtspplayer;

import java.io.File;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.videolan.libvlc.BitmapUtils;
import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.VLCInstance;
import org.videolan.libvlc.WeakHandler;

import com.example.net.GetRecordFileList;
import com.example.net.NetControlAsyncTask;
import com.example.net.StartRealTimePlayerAsyncTask;
import com.example.net.StopRealTimePlayerAsyncTask;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.SettingNotFoundException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class VideoPlayerActivity extends Activity implements Callback, IVideoPlayer {
	
	private static String TAG = "VideoPlayerActivity";

	/*SurfaceView to display */
	private FrameLayout mSurfaceFrameLayout;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;

	/*Loading Animation View*/
	private View mOverlayView;
	private ImageButton mOverlayPlay;
	private ImageView mOverlayLoading;
	private TextView mOverlayLoadingText;
	private boolean mShowing = false;

	/*Controlling Overlay View*/
	private View mOverlayHeader;
	private View mOverlayProgress;
	private TextView mOverlayTitle;
	private TextView mOverlayBattery;
	private TextView mOverlaySystime;
	private ImageButton mOverlayPlayPause;
	private ImageButton mOverlayScreenShot;
	private ImageButton mOverlayRecord;
	private ImageButton mOverlayMore;
	private ImageButton mOverlayLock;
	private ImageButton mOverlayBackward;
	private ImageButton mOverlayForward;
	private String mPlayRecordFileSpeed = "1";
	private TextView mOverlaySpeed;
	private TextView mOverlayDate;
	private TextView mOverlaySelectedTime;
	private TextView mOverlayTime;
	private SeekBar mOverlaySeekbar;
	private TextView mOverlayLength;

	/*Select Calender*/
	private int year;
	private int month;
	private int day;
	private Calendar calendar;

	private boolean recording = false;
	private boolean isPlaying = false;
	private boolean selectDate = false;
	private boolean selectTime = false;
	private boolean isFirstRecordPlaying = false;
	private LibVLC mLibVLC;

	/*Intent parameters*/
	private Intent intent;
	private String ip;
	private String cameraId;
	private String cameraCode;
	private String port;
	private String MRL;
	
	/* Display Metrics */
	private int mVideoHeight;
	private int mVideoWidth;
	private int mVideoVisibleHeight;
	private int mVideoVisibleWidth;
	private int mSarNum;
	private int mSarDen;
    private int mSurfaceAlign;  
    private static final int SURFACE_SIZE = 3;              
    private static final int SURFACE_BEST_FIT = 0;    
    private static final int SURFACE_FIT_HORIZONTAL = 1;    
    private static final int SURFACE_FIT_VERTICAL = 2;    
    private static final int SURFACE_FILL = 3;    
    private static final int SURFACE_16_9 = 4;    
    private static final int SURFACE_4_3 = 5;    
    private static final int SURFACE_ORIGINAL = 6;    
    private int mCurrentSize = SURFACE_BEST_FIT;
    private static final int FADE_OUT = 1;
    
	/* Tips view */
	private int mSurfaceYDisplayRange;
    private float mTouchY, mTouchX, mVol;
    private int mTouchAction;
    
    /*Set Surface*/
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_VOLUME = 1;
    private static final int TOUCH_BRIGHTNESS = 2;
    private static final int TOUCH_SEEK = 3;
    private static final int OVERLAY_TIMEOUT = 4000;
    private boolean mIsFirstBrightnessGesture = true;

	private Timer timer;
    
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.i(TAG,"onCreate");
		//Set surface view
		setContentView(R.layout.player);
		mSurfaceFrameLayout = (FrameLayout)findViewById(R.id.player_surface_frame);
		mSurfaceView = (SurfaceView) findViewById(R.id.player_surface);
		mSurfaceView.setKeepScreenOn(true);
		
		mOverlayView = (View)findViewById(R.id.player_overlay_tips);
		mOverlayView.bringToFront();
		mOverlayView.invalidate();
		
		//Set LibVLC
		 try {
	            mLibVLC = VLCInstance.getLibVlcInstance();

	        } catch (LibVlcException e) {
	            Log.i(TAG, "LibVLC initialisation failed");
	            return;
	        }
//		mLibVLC.eventVideoPlayerActivityCreated(true);
//		mLibVLC.setHardwareAcceleration(LibVLC.HW_ACCELERATION_DISABLED);
		
		//Set surface holder
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
		mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
		mSurfaceHolder.addCallback(this);

		//Initial Option Overlay View
		mOverlayHeader = (View)findViewById(R.id.player_overlay_header);
		mOverlayTitle = (TextView)findViewById(R.id.player_overlay_title);
		mOverlayBattery = (TextView)findViewById(R.id.player_overlay_battery);
		mOverlaySystime = (TextView)findViewById(R.id.player_overlay_systime);
		mOverlayScreenShot = (ImageButton)findViewById(R.id.player_overlay_size);
		mOverlayScreenShot.setOnClickListener(mScreenShotListener);
		mOverlayRecord = (ImageButton) findViewById(R.id.player_overlay_record);
		mOverlayRecord.setOnClickListener(mOverlayRecordListener);
		mOverlayMore = (ImageButton)findViewById(R.id.player_overlay_more);
		mOverlayMore.setOnClickListener(mOverlayMoreListener);

		//Initial Progress Overlay View
		mOverlayProgress = (View)findViewById(R.id.progress_overlay);
		mOverlayPlayPause = (ImageButton) findViewById(R.id.player_overlay_play);
		mOverlayPlayPause.setOnClickListener(mPlayPauseListener);
		mOverlayLock = (ImageButton)findViewById(R.id.lock_overlay_button);
		mOverlayBackward = (ImageButton)findViewById(R.id.player_overlay_backward);
		mOverlayBackward.setOnClickListener(mOverlayBackwardListener);
		mOverlayForward = (ImageButton)findViewById(R.id.player_overlay_forward);
		mOverlayForward.setOnClickListener(mOverlayForwardListener);
		mOverlaySpeed = (TextView)findViewById(R.id.player_overlay_speed);
		calendar = Calendar.getInstance();
		year = calendar.get(Calendar.YEAR);
		month = calendar.get(Calendar.MONTH) + 1;
		day = calendar.get(Calendar.DATE);
		mOverlayDate = (TextView)findViewById(R.id.player_overlay_date);
		mOverlayDate.setText(year+"-"+month+"-"+day);
		mOverlayDate.setOnClickListener(mOverlayDateListener);
		mOverlaySelectedTime = (TextView)findViewById(R.id.player_overlay_selectedtime);
		mOverlaySelectedTime.setOnClickListener(mOverlaySelectedTimeListener);
		mOverlayTime = (TextView)findViewById(R.id.player_overlay_time);
		mOverlaySeekbar = (SeekBar)findViewById(R.id.player_overlay_seekbar);
		mOverlaySeekbar.setOnSeekBarChangeListener(mSeekListener);
		mOverlayLength = (TextView)findViewById(R.id.player_overlay_length);

		//Loading Animation
		mOverlayLoading = (ImageView)findViewById(R.id.player_overlay_loading);
		mOverlayLoadingText = (TextView)findViewById(R.id.player_overlay_loading_text);
		startLoadingAnimation();

		//Event Handler listen to view changes
	    EventHandler em = EventHandler.getInstance();  
	    em.addHandler(eventHandler);

		pathIsExist();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.i(TAG,"onResume");
		if(mLibVLC != null){
			intent = getIntent();
			MRL = intent.getStringExtra("result");
			cameraId = intent.getStringExtra("cameraId");
			cameraCode = intent.getStringExtra("cameraCode");
			mLibVLC.playMRL(MRL);
			isPlaying = true;
			Log.i(TAG, "MRL = "+MRL);
		}
		Log.i(TAG, "on Resume");
	}
	
	@Override 
	protected void onPause(){
		super.onPause();
		Log.i(TAG,"onPauer");
		if(mLibVLC!=null){
			mLibVLC.stop();
			Log.i(TAG, "on Pause, mLibVLC.stop");
			mSurfaceView.setKeepScreenOn(false);
			Log.i(TAG, "mSurfaceView.setKeepScreenOn(false)");
		}
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		Log.i(TAG, "on Destroy");
		if(mLibVLC!=null){
			//mLibVLC.eventVideoPlayerActivityCreated(false);
			String ip = intent.getStringExtra("ip");
			String cameraId = intent.getStringExtra("cameraId");
			int port = intent.getExtras().getInt("port");
			StopRealTimePlayerAsyncTask stp = new StopRealTimePlayerAsyncTask();
			stp.execute(ip, cameraId, String.valueOf(port));
		}
	}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        setSurfaceSize(mVideoWidth, mVideoHeight, mVideoVisibleWidth, mVideoVisibleHeight, mSarNum, mSarDen);
        super.onConfigurationChanged(newConfig);
    }
    
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if(mLibVLC != null){
			mSurfaceHolder = holder;
			mLibVLC.attachSurface(holder.getSurface(), VideoPlayerActivity.this);
			Log.i(TAG, "mLibVLC.Detach");
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		mSurfaceHolder = holder;
		if(mLibVLC != null){
			mLibVLC.attachSurface(holder.getSurface(),VideoPlayerActivity.this);
		}
		if(width>0){
			mVideoHeight = height;
			mVideoWidth = width;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if(mLibVLC != null){
			mLibVLC.detachSurface();
		}
	}
	
	/* Set Surface Compatible to Display */
	private void changeSurfaceSize() {  
        // get screen size  
        int dw = getWindow().getDecorView().getWidth();  
        int dh = getWindow().getDecorView().getHeight();  

        // getWindow().getDecorView() doesn't always take orientation into account, we have to correct the values  
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;  
        if (dw > dh && isPortrait || dw < dh && !isPortrait) {  
            int d = dw;  
            dw = dh;  
            dh = d;  
        }  

        // sanity check  
        if (dw * dh == 0 || mVideoWidth * mVideoHeight == 0) {  
            Log.e(TAG, "Invalid surface size");  
            return;  
        }  

        // compute the aspect ratio  
        double ar, vw;  
        double density = (double)mSarNum / (double)mSarDen;  
        if (density == 1.0) {  
            /* No indication about the density, assuming 1:1 */  
            vw = mVideoWidth;  
            ar = (double)mVideoWidth / (double)mVideoHeight;  
        } else {  
            /* Use the specified aspect ratio */  
            vw = mVideoWidth * density;  
            ar = vw / mVideoHeight;  
        }  

        // compute the display aspect ratio  
        double dar = (double) dw / (double) dh;  

        switch (mCurrentSize) {  
            case SURFACE_BEST_FIT:  
                if (dar < ar)  
                    dh = (int) (dw / ar);  
                else  
                    dw = (int) (dh * ar);  
                break;  
            case SURFACE_FIT_HORIZONTAL:  
                dh = (int) (dw / ar);  
                break;  
            case SURFACE_FIT_VERTICAL:  
                dw = (int) (dh * ar);  
                break;  
            case SURFACE_FILL:  
                break;  
            case SURFACE_16_9:  
                ar = 16.0 / 9.0;  
                if (dar < ar)  
                    dh = (int) (dw / ar);  
                else  
                    dw = (int) (dh * ar);  
                break;  
            case SURFACE_4_3:  
                ar = 4.0 / 3.0;  
                if (dar < ar)  
                    dh = (int) (dw / ar);  
                else  
                    dw = (int) (dh * ar);  
                break;  
            case SURFACE_ORIGINAL:  
                dh = mVideoHeight;  
                dw = (int) vw;  
                break;  
        }  

        // align width on 16bytes  
        int alignedWidth = (mVideoWidth + mSurfaceAlign) & ~mSurfaceAlign;  

        // force surface buffer size  
        mSurfaceHolder.setFixedSize(alignedWidth, mVideoHeight);  

        // set display size  
        ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();  
        lp.width = dw * alignedWidth / mVideoWidth;  
        lp.height = dh;  
        mSurfaceView.setLayoutParams(lp);  
        mSurfaceView.invalidate();
    }  
	
	@Override
	public void setSurfaceSize(int width, int height, int visible_width, int visible_height, int sar_num, int sar_den) {
		// TODO Auto-generated method stub
			mVideoHeight = height;
		    mVideoWidth = width;
		    mVideoVisibleHeight = visible_height;
		    mVideoVisibleWidth = visible_width;
		    mSarNum = sar_num;
		    mSarDen = sar_den;
		    mHandler.removeMessages(SURFACE_SIZE);
		    mHandler.sendEmptyMessage(SURFACE_SIZE);
	}
	
	private Handler mHandler = new Handler(){

	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            case SURFACE_SIZE:
	                changeSurfaceSize();
	                break;
	            case FADE_OUT:
	            	hideOverlay(false);
	                break;
	            }
	        }
	  };

	/**
	 * show/hide the overlay
	 */
	public boolean onTouchEvent(MotionEvent event){
			
			DisplayMetrics screen = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(screen);
			
			if(mSurfaceYDisplayRange == 0)
				mSurfaceYDisplayRange = Math.min(screen.widthPixels, screen.heightPixels);
			
			float y_changed = event.getRawY() - mTouchY;
			float x_changed = event.getRawX() - mTouchX;
			
			//coef is the gradient's move to determine a neutral zone
			float coef = Math.abs(y_changed / x_changed);
			
			switch(event.getAction()){
			
			case MotionEvent.ACTION_DOWN:
				//Audio & Brightness 
				Log.i(TAG, "MotionEvent = Down");
				mTouchY = event.getRawY();
				mTouchX = event.getRawX();
				mTouchAction = TOUCH_NONE;
				break;
				
			case MotionEvent.ACTION_MOVE:
				Log.i(TAG, "MotionEvent = Move");
				// No volume/brightness action if coef < 2 
				
					//doBrightnessTouch(y_changed);	
					startCameraControl(x_changed, y_changed);
				
				break;
				
			case MotionEvent.ACTION_UP:
				Log.i(TAG, "MotionEvent = Up");
				
				if(mTouchAction == TOUCH_NONE && !isPlaying){
		             if (!mShowing) {
		                 showOverlay();
		             } else {
		                 hideOverlay(true);
		             }
				}
				stopCameraControl();
				break;
			}
			return mTouchAction != TOUCH_NONE;
			
		}
		
	private void initBrightnessTouch(){
			if(!mIsFirstBrightnessGesture)
				return;
			
			float brightnesstemp = 0.01f;
			
			try {
				brightnesstemp = android.provider.Settings.System.getInt(getContentResolver(), 
						android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
			} catch (SettingNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			WindowManager.LayoutParams lp = getWindow().getAttributes();
			lp.screenBrightness = brightnesstemp;
			getWindow().setAttributes(lp);
			mIsFirstBrightnessGesture = false;
		}

	private void doBrightnessTouch(float y_changed) {
			Log.i(TAG, "RTSP y_changed = "+y_changed);
			// TODO Auto-generated method stub
			if(mIsFirstBrightnessGesture) 
				initBrightnessTouch();
			mTouchAction = TOUCH_BRIGHTNESS;
			
			float delta = - y_changed / mSurfaceYDisplayRange * 0.07f;
			
			WindowManager.LayoutParams lp = getWindow().getAttributes();
		    lp.screenBrightness =  Math.min(Math.max(lp.screenBrightness + delta, 0.01f), 1);
		    Log.i(TAG, "screen brightness = "+lp.screenBrightness);
		    getWindow().setAttributes(lp);
		}  
		
	private void startCameraControl(float x_changed, float y_changed){
			Log.i(TAG, "start camera control");
		    Log.i(TAG, "x_changed = "+Math.floor(x_changed)+", y_changed = "+Math.floor(y_changed));
		    
		    //Enable net work control 
		    NetControlAsyncTask netControl = new NetControlAsyncTask();
		    netControl.execute(cameraId, String.valueOf(x_changed), String.valueOf(y_changed));
		    
		}
		
	private void stopCameraControl(){
			Log.i(TAG, "stop camera control");
			
			NetControlAsyncTask netControl = new NetControlAsyncTask();
			netControl.execute(cameraId, "stop");
		}

	/* Overlay Tips*/
	public void onClickOverlayTips(View v) {
		        mOverlayView.setVisibility(View.GONE);
		  }

	/**
	 *  Handle libvlc asynchronous events
	 */
	private final Handler eventHandler = new VideoPlayerEventHandler(this);

	private class VideoPlayerEventHandler extends WeakHandler<VideoPlayerActivity> {
	        public VideoPlayerEventHandler(VideoPlayerActivity owner) {
	            super(owner);
	        }

	        @Override
	        public void handleMessage(Message msg) {
	            VideoPlayerActivity activity = getOwner();
	            if(activity == null) return;
	            // Do not handle events if we are leaving the VideoPlayerActivity

	            switch (msg.getData().getInt("event")) {
	                case EventHandler.HardwareAccelerationError:
	                    Log.i(TAG, "HardwareAccelerationError");
	                    activity.handleHardwareAccelerationError();
	                    break;
	                case EventHandler.MediaPlayerTimeChanged:
	                    // avoid useless error logs
	                    break;
	                case EventHandler.MediaPlayerPlaying:
	                	Log.i(TAG, "stop anim");
	                	activity.stopLoadingAnimation();
	                	break;
					case EventHandler.MediaPlayerPaused:
						Log.i(TAG, "MediaPlayerPaused");
						break;
					case EventHandler.MediaPlayerStopped:
						Log.i(TAG, "MediaPlayerStopped");
						break;
	                default:
	                    //Log.e(TAG, String.format("Event not handled (0x%x)", msg.getData().getInt("event")));
	                    break;
	            }
	            activity.updateOverlayPausePlay();
	        }
	    };

	/* Handle Hardware Acceleration Error */
	private void handleHardwareAccelerationError() {
	        mLibVLC.stop();
	        AlertDialog dialog = new AlertDialog.Builder(VideoPlayerActivity.this)
	        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int id) {
//	                mDisabledHardwareAcceleration = true;
//	                mLibVLC.setHardwareAcceleration(LibVLC.HW_ACCELERATION_DISABLED);
//	                mPreviousHardwareAccelerationMode = mLibVLC.getHardwareAcceleration();
//	                loadMedia();
	            }
	        })
	        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int id) {
	                finish();
	            }
	        })
	        .setTitle(R.string.hardware_acceleration_error_title)
	        .setMessage(R.string.hardware_acceleration_error_message)
	        .create();
	        if(!isFinishing())
	            dialog.show();
	    }

	/**
	 * Start the video loading animation.
	 */
	private void startLoadingAnimation() {
	        AnimationSet anim = new AnimationSet(true);
	        RotateAnimation rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
	        rotate.setDuration(800);
	        rotate.setInterpolator(new DecelerateInterpolator());
	        rotate.setRepeatCount(RotateAnimation.INFINITE);
	        anim.addAnimation(rotate);
	        mOverlayLoading.startAnimation(anim);
	        mOverlayLoadingText.setVisibility(View.VISIBLE);
	    }

	/**
	 * Stop the video loading animation.
	 */
	private void stopLoadingAnimation() {
	    	mOverlayLoading.setVisibility(View.INVISIBLE);
	    	mOverlayLoading.clearAnimation();
	    	mOverlayLoadingText.setVisibility(View.GONE);
	    }

	/**
	 * show overlay
	 */
	private void showOverlay(){
	    	showOverlay(OVERLAY_TIMEOUT);
	    }
	    
	private void showOverlay(int timeout) {

	        if (!mShowing) {
	            mShowing = true;
	            mOverlayHeader.setVisibility(View.VISIBLE);
	            mOverlayPlayPause.setVisibility(View.VISIBLE);
				mOverlayProgress.setVisibility(View.VISIBLE);
	        }
	        Message msg = mHandler.obtainMessage(FADE_OUT);
	        if (timeout != 0) {
	            mHandler.removeMessages(FADE_OUT);
	            mHandler.sendMessageDelayed(msg, timeout);
	        }
			updateOverlayPausePlay();
	   }

	/**
	 * hider overlay
	 */
	private void hideOverlay(boolean fromUser) {
	         if (mShowing) {
	             if (mOverlayView != null) mOverlayView.setVisibility(View.INVISIBLE);
	             if (!fromUser) {
	                 mOverlayHeader.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
	                 mOverlayPlayPause.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
					 mOverlayProgress.startAnimation(AnimationUtils.loadAnimation(this,android.R.anim.fade_out));
//	                 mOverlayOption.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
//	                 mOverlayProgress.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
//	                 mMenu.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
	             }
	             mOverlayHeader.setVisibility(View.INVISIBLE);
	             mOverlayPlayPause.setVisibility(View.INVISIBLE);
				 mOverlayProgress.setVisibility(View.INVISIBLE);
	             mShowing = false;

	         }
	     }

	/**
	 * Select record day by calender
	 */
	private OnClickListener mOverlayDateListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.i(TAG,"textView on click");

				dialog().show();

			}
			public DatePickerDialog dialog() {
				mLibVLC.pause();
				DatePickerDialog datePickerDialog = new DatePickerDialog(VideoPlayerActivity.this, new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						String date = Integer.toString(year) + "-" + Integer.toString(monthOfYear+1) + "-" + Integer.toString(dayOfMonth);
						Log.i(TAG, "mOverlayDate = "+date);
						mOverlayDate.setText(date);
					}
				}, year, month, day);

				selectDate = true;

				return datePickerDialog;
			}
		};

	private OnClickListener mOverlaySelectedTimeListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.i(TAG, "mOverlaySelectedTime on click");
				dialog().show();
			}

			public TimePickerDialog dialog(){
				mLibVLC.pause();
				TimePickerDialog timePickerDialog = new TimePickerDialog(VideoPlayerActivity.this, new TimePickerDialog.OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker timePicker, int hour, int minute) {
						mOverlaySelectedTime.setText((hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute));
						float temp = ((float)hour / 24 + (float)minute/3600 )*100;
						mOverlaySeekbar.setProgress(Math.round(temp));
						Log.i(TAG, "mOverlaySeekbar.setProgress = " + Math.round(temp));
					}
				}, 12, 00, true);
				selectTime = true;
				return  timePickerDialog;
			}
		};

	private OnClickListener mOverlayBackwardListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			Log.i(TAG, "on backward");

			int temp = Integer.parseInt(mPlayRecordFileSpeed);
			if(temp > 1) {
				mPlayRecordFileSpeed = Integer.toString(temp - 1);
				mOverlaySpeed.setText(mPlayRecordFileSpeed + "x");
				mLibVLC.setRate(temp-1);
			}
		}
	};

	private  OnClickListener mOverlayForwardListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			Log.i(TAG, "on forward");
			int temp = Integer.parseInt(mPlayRecordFileSpeed);
			if(temp < 4) {
				mPlayRecordFileSpeed = Integer.toString(temp + 1);
				mOverlaySpeed.setText(mPlayRecordFileSpeed + "x");
				mLibVLC.setRate(temp+1);
			}
		}
	};

	/**
	 * update Overlay
	 */
	private void updateOverlayPausePlay() {
	        if (mLibVLC == null)
	            return;
			mOverlayPlayPause.setBackgroundResource(mLibVLC.isPlaying() ? R.drawable.ic_pause_circle_big_o
	                            : R.drawable.ic_play_circle_big_o);
			Log.i(TAG,"mOverlayPlayPause changed");
	    }

	/**
	 *  seekbar listener
	 */
	private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				Log.i(TAG, "onProgressChanged");
				Log.i(TAG, "the progress = " + i);
				String hourTime = (i*24/100)<10 ? "0"+Integer.toString(i * 24 / 100) : Integer.toString(i * 24 / 100);
				int minuteTime = Math.round(((float) (i * 24) / 100) % 1 * 60);
				String minutetime = minuteTime<10 ? "0"+Integer.toString(minuteTime) : Integer.toString(minuteTime);
				mOverlaySelectedTime.setText(hourTime+":"+minutetime);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				mLibVLC.pause();
				selectTime = true;
				Log.i(TAG,"onStartTrackingTouch");
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Log.i(TAG, "onStopTrackingTouch");
			}
		};

	/**
	 *  pause media playing
	 */
	private final OnClickListener mPlayPauseListener = new OnClickListener() {
	        @Override
	        public void onClick(View v) {
	            if (mLibVLC.isPlaying())
	            {
	                mLibVLC.pause();
	                mSurfaceView.setKeepScreenOn(false);
	                Log.i(TAG, "media playing on click listener start!");
	                Log.i(TAG, "mLibVLC.pause");
	            }
	            else
	            {
					if(selectTime || selectDate || isFirstRecordPlaying)
					{
						Log.i(TAG, "ask for record file");

						String beginTime = mOverlayDate.getText() + " " + mOverlaySelectedTime.getText()+":00";
						String endTime;

						//Calculate end time
						if(mOverlaySelectedTime.getText().toString().substring(0,2).equals("23"))
						{
							endTime = mOverlayDate.getText()+" "+"24:00:00";
						}
						else
						{
							String temp = new String(mOverlaySelectedTime.getText().toString().substring(0,2));
							int tempInt = Integer.parseInt(temp) + 1 ;
							if(tempInt < 10)
								temp = "0"+Integer.toString(tempInt);
							else temp = Integer.toString(tempInt);
							//temp.replace(temp.charAt(3), (Integer.parseInt(temp.charAt(3)) + 1).toString());
							endTime = mOverlayDate.getText()+" " + temp + mOverlaySelectedTime.getText().toString().substring(2,5)+":00";
						}

						//Ask for record file
						GetRecordFileList getRecordFileList = new GetRecordFileList(getApplicationContext());
						getRecordFileList.execute(cameraCode,beginTime,endTime);
						Log.i(TAG, "getRecordFilelist, cameraCode = " + cameraCode);
						try{
							String recordMRL = getRecordFileList.get();
							Log.i(TAG, "record MRL = "+recordMRL);
							if(recordMRL != null)
							{
								mLibVLC.playMRL(recordMRL);

								//设置连续播放
								timer = new Timer();
								timer.schedule(new TimerTask() {
									@Override
									public void run() {
										if(mLibVLC.isPlaying())
											return;
										else {
											timer.cancel();
//											GetRecordFileList getRecordFileList = new GetRecordFileList(getApplicationContext());
//											getRecordFileList.execute(cameraCode, , );

											Log.i(TAG, "timer cancel!");
										}
									}
								}, 1000, 1000);
							}
						}catch (InterruptedException e){
							e.printStackTrace();
						}catch (ExecutionException e) {
							e.printStackTrace();
						}

						Log.i(TAG, "beginTime = "+ beginTime + "; endTime = " + endTime);
						selectDate = false;
						selectDate = false;
						isFirstRecordPlaying = false;

					}
					else {
						mLibVLC.play();
						mSurfaceView.setKeepScreenOn(true);
						Log.i(TAG, "media playing on click listener start!");
						Log.i(TAG, "mLibVLC.play");

					}
	            }
				showOverlay();
	        }
	    };
	    
	private final OnClickListener mScreenShotListener = new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mLibVLC.isPlaying())
				{
					snapShot();
					Log.i(TAG, "Screen Shot taken");
				}
			}
	    };

	private final  OnClickListener mOverlayRecordListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				videoRecord();
			}
		};

	private final  OnClickListener mOverlayMoreListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				//Toast.makeText(getApplicationContext(), "more listener", 1000).show();
				if(isPlaying) {
					mLibVLC.stop();
					showOverlay(2000000);
					isPlaying = false;
					isFirstRecordPlaying = true;
//					startLoadingAnimation();
//					playVideoRecorded();

				}
				else {
					mLibVLC.playMRL(MRL);
					isPlaying = true;
				}

			}
		};

	private void playVideoRecorded(){
			//mLibVLC.playMRL("rtsp://admin:12345@10.46.4.16/h264/ch1/main/av_stream");
			//mLibVLC.playMRL(MRL);
		}

	/**
	 * take a snapshot
	 */
	private void snapShot() {
			try {
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
				String name = df.format(new Date());
				name = BitmapUtils.getSDPath() + "/ab/capture/" + name + ".png";
				java.lang.System.out.println("===" + name);
				File file = new File(name);
				if (!file.exists())
					file.createNewFile();
				if (mLibVLC.takeSnapShot(name, mVideoWidth, mVideoHeight)) {
					Toast.makeText(getApplicationContext(), "已保存", 1000).show();
				} else {
					Toast.makeText(getApplicationContext(), "截图失败", 1000).show();

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	/**
	 * 录像和停止录像
	 */
	private void videoRecord() {
		try {
			if(!recording){
				SimpleDateFormat df = new SimpleDateFormat(
						"yyyy-MM-dd HH-mm-ss");
				String name = df.format(new Date());
				String path = BitmapUtils.getSDPath()+"/ab/video/"+name;
				mLibVLC.videoRecordStart(path);
				if (mLibVLC.videoIsRecording()) {
					Toast.makeText(getApplicationContext(), "开始录像", 500)
							.show();
				} else {
					Toast.makeText(getApplicationContext(), "开始录像失败", 500)
							.show();
				}
				recording = true;
			}
			else if(recording){
				mLibVLC.videoRecordStop();
					Toast.makeText(getApplicationContext(), "停止录像", 500)
							.show();
				recording = false;
			}
			Log.i(TAG,"mlibVLC.isRecording = "+mLibVLC.videoIsRecording());
			Log.i(TAG,"recording = "+recording);
//			if (mLibVLC.videoIsRecording()) {
//				if (mLibVLC.videoRecordStop()) {
//					Toast.makeText(getApplicationContext(), "停止录像", 500)
//							.show();
//				} else {
//					Toast.makeText(getApplicationContext(), "停止录像失败", 500)
//							.show();
//				}
//			} else {
//				SimpleDateFormat df = new SimpleDateFormat(
//						"yyyy-MM-dd HH-mm-ss");
//				String name = df.format(new Date());
//				String path = BitmapUtils.getSDPath()+"/ab/video/"+name;
//				if (mLibVLC.videoRecordStart(path)) {
//					Toast.makeText(getApplicationContext(), "开始录像", 500)
//							.show();
//				} else {
//					Toast.makeText(getApplicationContext(), "开始录像失败", 500)
//							.show();
//				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 路径是否存在 不存在则创建
	 */
	private void pathIsExist() {
		File file = new File(BitmapUtils.getSDPath() + "/ab/capture/");
		if (!file.exists())
			file.mkdirs();

		File file1 = new File(BitmapUtils.getSDPath() + "/ab/video/");
		if (!file1.exists())
			file1.mkdirs();
	}
}