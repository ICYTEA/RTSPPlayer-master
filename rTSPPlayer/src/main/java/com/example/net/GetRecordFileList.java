package com.example.net;

import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.util.Log;

import org.ksoap2.serialization.SoapObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by liangxing on 2015/7/28.
 */
public class GetRecordFileList extends AsyncTask<String, Integer, String> {

    private String TAG = "GetRecordFileList";

    private String nameSpace = "http://camera.concrete.device.videomonitor.direction/";
    private String EndPointness = "http:/getRecordAllTypeAndStorageFileInfo/10.46.4.12:9100/VideoMonitor/services/VideoCameraAccessService";
    private String methodName = "getRecordAllTypeAndStorageFileInfo";

    private String cameraId;
    private Date startTime;
    private Date endTime;

    @Override
    protected String doInBackground(String... strings) {

        Log.i(TAG, "doInBackground() called");

        // 根据命名空间和方法得到SoapObject对象
        SoapObject soapObject = new SoapObject(nameSpace, methodName);
        String soapAction = nameSpace+methodName;
        Log.i(TAG, "soapAction = " + soapAction);

        cameraId = strings[0];
        startTime = StringToDate(strings[1]);
        endTime = StringToDate(strings[2]);
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
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
