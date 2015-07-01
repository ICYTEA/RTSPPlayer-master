package com.example.net;

import java.io.IOException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.os.AsyncTask;
import android.util.Log;

public class NetControlAsyncTask extends AsyncTask<String, Integer, String>{
	
	private String TAG = "NetControlAsnycTask";
	
//	private String nameSpace = "http://camera.concrete.device.videomonitor.direction/";
//	private String EndPointness = "http://10.46.4.12:9100/VideoMonitor/services/VideoCameraAccessService";
	
	private String nameSpace = "http://service.sip.videomonitor.direction/";
	private String EndPointness = "http://10.46.4.12:9200/SIPServer/services/ControlCommandService";
	private String methodName;
	
	private String deviceCode;
	private int speed = 10;
	private int address = 100;
	private boolean returnSipMessage = false;
	
	private float x_changed;
	private float y_changed;
	private float coef;
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
        Log.i(TAG, "onPreExecute() called");  
	}

	@Override
	protected String doInBackground(String... params) {
		// TODO Auto-generated method stub
		Log.i(TAG, "doInBackground() called");
		
		if(params[1].equals("stop") ){
			methodName = "ptzStop";
			Log.i(TAG, "methodName = stop");
		} else {
			x_changed = Float.valueOf(params[1]);
			y_changed = Float.valueOf(params[2]);
		
			coef = Math.abs (y_changed / x_changed);
			Log.i(TAG, "coef = "+coef);
		
			if(coef > 2){
				if(y_changed > 0)
					methodName = "ptzDown";
				else methodName = "ptzUp";
			} else{
				if(x_changed > 0)
					methodName = "ptzRight";
				else
					methodName = "ptzLeft";
			}	
		}
		

		SoapObject soapObject = new SoapObject(nameSpace, methodName);
		String soapAction = nameSpace + methodName;
		Log.i(TAG, "soapAction = " + soapAction);

		deviceCode = params[0];
		
		Log.i(TAG, "deviceCode = " + deviceCode);
		Log.i(TAG, "speed = " + speed);
		Log.i(TAG, "address = "+address);
		Log.i(TAG, "returnSipMessage = "+ returnSipMessage);
		
		if(methodName.equals("ptzStop"))
		{
			soapObject.addProperty("deviceCode", deviceCode);
			soapObject.addProperty("address", address);
			soapObject.addProperty("returnSipMessage", returnSipMessage);
		} else{
			soapObject.addProperty("deviceCode", deviceCode);
			soapObject.addProperty("speed", speed);
			soapObject.addProperty("address", address);
			soapObject.addProperty("returnSipMessage", returnSipMessage);
		}
		
		//initalize httptransport service
		HttpTransportSE httpSE = new HttpTransportSE(EndPointness);
		httpSE.debug = true;
		
		// 通过SOAP1.1协议得到envelop对象,传出消息
		SoapSerializationEnvelope envelop = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelop.bodyOut = soapObject;
		
		try {
			httpSE.call(null, envelop);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
}
