package com.example.rtspplayer;

import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.LibVlcUtil;
import org.videolan.libvlc.VLCInstance;

import com.slidingmenu.lib.SlidingMenu;
import com.example.net.StartRealTimePlayerAsyncTask;
import com.example.net.StopRealTimePlayerAsyncTask;
import com.example.slidingmenu.SampleListFragment;
import com.example.rtspplayer.VideoPlayerActivity;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.SettingNotFoundException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
	
	private static final String TAG = "LibVLC";
	
	/* Surface View */
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	
	/* Display Metrics */
	private int mVideoHeight;
	private int mVideoWidth;
	private int mVideoVisibleHeight;
	private int mVideoVisibleWidth;
	private int mSarNum;
	private int mSarDen;
    private int mSurfaceAlign; 
    private static final int SURFACE_HORIZONTAL = 1;
    private static final int SURFACE_VERTICAL = 2;
    private static final int SURFACE_SIZE = 3;  
    private static final int mCurrentSize = SURFACE_HORIZONTAL;
    
	/* Title Bar */
	private RelativeLayout mTitleBarLayout;
	private ImageButton mTitleLeftButton;
	private ImageButton mTitleRightButton;
	private TextView mTitleTextView;
	
	/* Sliding Menu*/
	public  SlidingMenu mSlidingMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Set surface view
		setContentView(R.layout.activity_main);
		mSurfaceView = (SurfaceView) findViewById(R.id.videoview);
		mSurfaceView.setKeepScreenOn(true);
		
		//Title bar view
		mTitleBarLayout = (RelativeLayout) findViewById(R.id.rl_maincenter_title);
		mTitleLeftButton = (ImageButton) findViewById(R.id.ib_menu_left);
		mTitleRightButton = (ImageButton) findViewById(R.id.ib_menu_right);
		mTitleTextView = (TextView) findViewById(R.id.tv_load_course);

		//Set SlidingMenu
		mSlidingMenu = new SlidingMenu(this);
		mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		mSlidingMenu.setShadowDrawable(R.drawable.shadow);
		mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		mSlidingMenu.setFadeDegree(0.35f);
		mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		mSlidingMenu.setMenu(R.layout.menu_frame);

		//Add List Fragment to FrameLayout
		SampleListFragment menuFrame = new SampleListFragment(mSlidingMenu);
		getSupportFragmentManager().beginTransaction()
		.replace(R.id.menu_frame, menuFrame).commit();
	   
		mTitleLeftButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mSlidingMenu.showMenu();
			}
			
		});

		mTitleRightButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			}
		});

		if(!isWifiConnected(getApplicationContext())) {
			Toast.makeText(getApplicationContext(),"请打开网络！", 500).show();
		}

	}
	
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        setSurfaceSize(mVideoWidth, mVideoHeight, mVideoVisibleWidth, mVideoVisibleHeight, mSarNum, mSarDen);
        super.onConfigurationChanged(newConfig);
    }
    
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
            }
        }
  };
  
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
           
          case SURFACE_HORIZONTAL:  
              dh = (int) (dw / ar);  
              break;  
          case SURFACE_VERTICAL:  
              dw = (int) (dh * ar);  
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

	public boolean isWifiConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (mConnectivityManager!=null){
				NetworkInfo[] infos = mConnectivityManager.getAllNetworkInfo();
				if (infos!=null){
					for (int i=0; i<infos.length; i++)
					{
						if (infos[i].getTypeName().equals("WIFI") && infos[i].isConnected())
							return true;
					}
				}
			}
		}
		return false;
	}
	
}
