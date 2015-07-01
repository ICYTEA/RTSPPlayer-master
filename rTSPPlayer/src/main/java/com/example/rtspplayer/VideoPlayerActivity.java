package com.example.rtspplayer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.videolan.libvlc.BitmapUtils;
import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.VLCInstance;
import org.videolan.libvlc.WeakHandler;

import com.example.net.NetControlAsyncTask;
import com.example.net.StopRealTimePlayerAsyncTask;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class VideoPlayerActivity extends Activity implements Callback, IVideoPlayer {
	
	private static String TAG = "VideoPlayerActivity";
	
	private FrameLayout mSurfaceFrameLayout;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	
	private View mOverlayView;
	private ImageButton mOverlayPlay;
	private ImageView mOverlayLoading;
	private TextView mOverlayLoadingText;
	private boolean mShowing = false;
	
	private View mOverlayHeader;
	private TextView mOverlayTitle;
	private TextView mOverlayBattery;
	private TextView mOverlaySystime;
	private ImageButton mOverlayPlayPause;
	private ImageButton mOverlayScreenShot;
	private ImageButton mOverlayRecord;
	private ImageButton mOverlayMore;
	private boolean recording = false;
	private LibVLC mLibVLC;
	
	private Intent intent;
	private String ip;
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
    
    //set surface size
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_VOLUME = 1;
    private static final int TOUCH_BRIGHTNESS = 2;
    private static final int TOUCH_SEEK = 3;
    private static final int OVERLAY_TIMEOUT = 4000;
    private boolean mIsFirstBrightnessGesture = true;
   
    
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
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
		
		mOverlayHeader = (View)findViewById(R.id.player_overlay_header);
		mOverlayTitle = (TextView)findViewById(R.id.player_overlay_title);
		mOverlayBattery = (TextView)findViewById(R.id.player_overlay_battery);
		mOverlaySystime = (TextView)findViewById(R.id.player_overlay_systime);
        mOverlayPlayPause = (ImageButton) findViewById(R.id.player_overlay_play);
        mOverlayPlayPause.setOnClickListener(mPlayPauseListener);
		mOverlayScreenShot = (ImageButton)findViewById(R.id.player_overlay_size);
		mOverlayScreenShot.setOnClickListener(mScreenShotListener);
		mOverlayRecord = (ImageButton) findViewById(R.id.player_overlay_record);
		mOverlayRecord.setOnClickListener(mOverlayRecordListener);
		mOverlayMore = (ImageButton)findViewById(R.id.player_overlay_more);
		mOverlayMore.setOnClickListener(mOverlayMoreListener);
		
		mOverlayLoading = (ImageView)findViewById(R.id.player_overlay_loading);
		mOverlayLoadingText = (TextView)findViewById(R.id.player_overlay_loading_text);
		startLoadingAnimation();
		
	    EventHandler em = EventHandler.getInstance();  
	    em.addHandler(eventHandler);

		pathIsExist();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(mLibVLC != null){
			intent = getIntent();
			MRL = intent.getStringExtra("result");
			cameraCode = intent.getStringExtra("cameraCode");
			mLibVLC.playMRL(MRL);
			Log.i(TAG, "MRL = "+MRL);
		}
		Log.i(TAG, "on Resume");
	}
	
	@Override 
	protected void onPause(){
		super.onPause();
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
			String cameraCode = intent.getStringExtra("cameraCode");
			int port = intent.getExtras().getInt("port");
			StopRealTimePlayerAsyncTask stp = new StopRealTimePlayerAsyncTask();
			stp.execute(ip, cameraCode, String.valueOf(port));
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
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
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
	public void setSurfaceSize(int width, int height, int visible_width,
			int visible_height, int sar_num, int sar_den) {
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
				
//				if(mTouchAction == TOUCH_NONE){
//		             if (!mShowing) {
//		                 showOverlay();
//		             } else {
//		                 hideOverlay(true);
//		             }
//				}
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
		    netControl.execute(cameraCode, String.valueOf(x_changed), String.valueOf(y_changed));
		    
		}
		
		private void stopCameraControl(){
			Log.i(TAG, "stop camera control");
			
			NetControlAsyncTask netControl = new NetControlAsyncTask();
			netControl.execute(cameraCode, "stop");
		}
		/* Overlay Tips*/
		public void onClickOverlayTips(View v) {
		        mOverlayView.setVisibility(View.GONE);
		  }
		
		  /**
	     *  Handle libvlc asynchronous events
	     */
	    private final Handler eventHandler = new VideoPlayerEventHandler(this);

	    private static class VideoPlayerEventHandler extends WeakHandler<VideoPlayerActivity> {
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
	                default:
	                    //Log.e(TAG, String.format("Event not handled (0x%x)", msg.getData().getInt("event")));
	                    break;
	            }
//	            activity.updateOverlayPausePlay();
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
//	            mOverlayOption.setVisibility(View.VISIBLE);
//	            mMenu.setVisibility(View.VISIBLE);
//	            dimStatusBar(false);
//	            mOverlayProgress.setVisibility(View.VISIBLE);
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
//	                 mOverlayOption.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
//	                 mOverlayProgress.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
//	                 mMenu.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
	             }
	             mOverlayHeader.setVisibility(View.INVISIBLE);
	             mOverlayPlayPause.setVisibility(View.INVISIBLE);
	             mShowing = false;
//	             mOverlayOption.setVisibility(View.INVISIBLE);
//	             mOverlayProgress.setVisibility(View.INVISIBLE);
//	             mMenu.setVisibility(View.INVISIBLE);
//	             dimStatusBar(true);
	         }
	     }
	     
	   /**
	    * updata Overlay
	    */
	    private void updateOverlayPausePlay() {
	        if (mLibVLC == null)
	            return;
	            mOverlayPlayPause.setBackgroundResource(mLibVLC.isPlaying() ? R.drawable.ic_pause_circle_big_o
	                            : R.drawable.ic_play_circle_big_o);
	    }

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
	            	mLibVLC.play();
	            	mSurfaceView.setKeepScreenOn(true);
	            	Log.i(TAG, "media playing on click listener start!");
		            Log.i(TAG, "mLibVLC.play");
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
				Toast.makeText(getApplicationContext(), "more listener", 1000).show();
			}
		};
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

			if (recording == true) {
				if (mLibVLC.videoRecordStop()) {
					Toast.makeText(getApplicationContext(), "停止录像", 500)
							.show();
				} else {
					Toast.makeText(getApplicationContext(), "停止录像失败", 500)
							.show();
				}
				recording = false;
			} else {

				SimpleDateFormat df = new SimpleDateFormat(
						"yyyy-MM-dd HH-mm-ss");
				String name = df.format(new Date());
				if (mLibVLC.videoRecordStart(BitmapUtils.getSDPath()
						+ "/ab/video/" + name)) {
					Toast.makeText(getApplicationContext(), "开始录像", 500)
							.show();
					recording = true;
				} else {
					Toast.makeText(getApplicationContext(), "开始录像失败", 500)
							.show();
				}
			}
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