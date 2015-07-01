package com.example.net;

import java.io.IOException;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.VLCInstance;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

public class StopRealTimePlayerAsyncTask extends AsyncTask<String, Integer, String>{

	private String TAG = "StopRealTimePlayerAsyncTask";
	
	private String nameSpace = "http://sipserviceconsumer.monitor.videomonitor.direction/";
	private String methodName = "stopRealtimeStreamByDeviceId";
	private String EndPointness = "http://10.46.4.12:9100/VideoMonitor/services/ClientVodServicePublish";
	private String ip;
	private String cameraCode;
	private int port;
	
	@Override
	protected String doInBackground(String... params) {
		// TODO Auto-generated method stub
		
		// 根据命名空间和方法得到SoapObject对象
		SoapObject soapObject = new SoapObject(nameSpace,methodName);
		String soapAction = nameSpace+methodName;
		Log.i(TAG, "soapAction = " + soapAction);
		
		ip = params[0];
		cameraCode = params[1];
		port = Integer.valueOf(params[2]);
		
		Log.i(TAG, "ip = "+ip);
		Log.i(TAG, "cameraCode = "+ cameraCode);
		Log.i(TAG, "port = " + port);
		
//		// 设置需调用WebService接口需要传入的两个参,这里传参时要注意,有时这个地方传参,在传入参数名时,要用wsdl文件上的方法的参数名,否则有可能报错		
		soapObject.addProperty("arg0", cameraCode);
		soapObject.addProperty("arg1", ip);
		soapObject.addProperty("arg2", port);
		
		//initalize httptransport service
		HttpTransportSE httpSE = new HttpTransportSE(EndPointness);
		httpSE.debug = true;
		
		// 通过SOAP1.1协议得到envelop对象,传出消息
		SoapSerializationEnvelope envelop = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelop.bodyOut = soapObject;
		
		// 开始调用远程方法
		try {
			httpSE.call(null, envelop);
			
		} catch (IOException e) {
			e.printStackTrace();
			return "IOException";
		} catch (XmlPullParserException e) {
			e.printStackTrace();
			return "XmlPullParserException";
		}
		
		return null;
	}

}
