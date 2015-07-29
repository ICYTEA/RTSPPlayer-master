package com.example.net;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalDate;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by liangxing on 2015/7/28.
 */
public class GetRecordFileList extends AsyncTask<String, Integer, String> {

    private String TAG = "GetRecordFileList";
    private Context context;

    private String nameSpace = "http://camera.concrete.device.videomonitor.direction/";
    private String EndPointness = "http://10.46.4.12:9100/VideoMonitor/services/VideoCameraAccessService";
    private String methodName = "getRecordAllTypeAndStorageFileInfo";

    private String cameraId = "di000000000000000068";
    private Date beginTime;
    private Date endTime;

    public GetRecordFileList (Context con)
    {
        this.context = con;
    }

    @Override
    protected String doInBackground(String... strings) {

        Log.i(TAG, "doInBackground() called");

        // 根据命名空间和方法得到SoapObject对象
        SoapObject soapObject = new SoapObject(nameSpace, methodName);
        String soapAction = nameSpace+methodName;
        Log.i(TAG, "soapAction = " + soapAction);

        //cameraId = strings[0];
        beginTime = StringToDate(strings[1]);
        endTime = StringToDate(strings[2]);


        Log.i(TAG, "cameraId = " + cameraId + "; beginTime = " + beginTime + "; endTime = " + endTime);
/*
        soapObject.addProperty("cameraId", cameraId);
        soapObject.addProperty("beginTime", new Date());
        soapObject.addProperty("endTime", new Date());
        */
		PropertyInfo pi = new PropertyInfo();
		pi.setName("cameraId");
		pi.setValue(cameraId);
		pi.setType(PropertyInfo.STRING_CLASS);
		soapObject.addProperty(pi);

		pi = new PropertyInfo();
		pi.setName("beginTime");
		pi.setValue(beginTime);
		pi.setType(MarshalDate.DATE_CLASS);
		soapObject.addProperty(pi);
		
		pi = new PropertyInfo();
		pi.setName("endTime");
		pi.setValue(endTime);
		pi.setType(MarshalDate.DATE_CLASS);
		soapObject.addProperty(pi);
        
        
        Log.i(TAG, "new Date() = "+new Date());
        HttpTransportSE httpSE = new HttpTransportSE(EndPointness);
        httpSE.debug = true;

        SoapSerializationEnvelope envelop = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        //envelop.bodyOut = soapObject;

        envelop.bodyOut = httpSE;  
		envelop.setOutputSoapObject(soapObject);// 设置请求参数  
	    new MarshalDate().register(envelop);    
	    
        try {
            httpSE.call(null, envelop);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

//        // 服务器返回错误信息
//        String temp = envelop.bodyIn.toString().substring(0, 9);
//        if(temp.equals("SoapFault"))
//            return envelop.bodyIn.toString();

        // 得到服务器传回的数据
        SoapObject resultObj = (SoapObject) envelop.bodyIn;
        String result = resultObj.getProperty(0).toString();
        Log.i(TAG, result);

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if(result != null){
            Toast.makeText(context, result, Toast.LENGTH_LONG).show();
        }
    }

    private Date StringToDate(String dateString){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = sdf.parse(dateString);
            System.out.println(date.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}