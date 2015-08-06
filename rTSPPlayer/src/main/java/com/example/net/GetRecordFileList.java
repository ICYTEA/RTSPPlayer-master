package com.example.net;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.MarshalDate;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;
import org.ksoap2.SoapFault;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by liangxing on 2015/7/28.
 */
public class GetRecordFileList extends AsyncTask<String, Integer, String[]> {

    private String TAG = "GetRecordFileList";
    private Context context;

    private String nameSpace = "http://camera.concrete.device.videomonitor.direction/";
    private String EndPointness = "http://10.46.4.12:9100/VideoMonitor/services/VideoCameraAccessService";
    private String methodName = "getRecordAllTypeAndStorageFileInfoForAndroid";

    private String cameraId = "di000000000000000068";
    private String beginTime;
    private String endTime;

    private String recordFileBeginTime;
    private String recordFileEndTime;

    private String[] finalResults;

    public GetRecordFileList (Context con)
    {
        this.context = con;
    }

    @Override
    public String[] doInBackground(String... strings) {

        Log.i(TAG, "doInBackground() called");

        // 根据命名空间和方法得到SoapObject对象
        SoapObject soapObject = new SoapObject(nameSpace, methodName);
        String soapAction = nameSpace+methodName;
        Log.i(TAG, "soapAction = " + soapAction);

        cameraId = strings[0];
        beginTime = strings[1];
        endTime = strings[2];


        Log.i(TAG, "cameraId = " + cameraId + "; beginTime = " + beginTime + "; endTime = " + endTime);
        soapObject.addProperty("cameraId", cameraId);
        soapObject.addProperty("beginTime", beginTime);
        soapObject.addProperty("endTime", endTime);

//        soapObject.addProperty("cameraId", cameraId);
//        soapObject.addProperty("beginTime", new Date());
//        soapObject.addProperty("endTime", new Date());

        /*
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
        */

        HttpTransportSE httpSE = new HttpTransportSE(EndPointness);
        httpSE.debug = true;

        SoapSerializationEnvelope envelop = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelop.bodyOut = soapObject;

//        envelop.bodyOut = httpSE;
//		envelop.setOutputSoapObject(soapObject);// 设置请求参数
//	    new MarshalDate().register(envelop);
	    
        try {
            httpSE.call(null, envelop);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // 服务器返回错误信息
        String temp = envelop.bodyIn.toString().substring(0, 9);
        if(temp.equals("SoapFault"))
        {
            finalResults = new String[1];
            finalResults[0] = envelop.bodyIn.toString();
            return finalResults;
        }

        try {
            SoapObject resultObj = (SoapObject) envelop.getResponse();
            String result = resultObj.toString();
            Log.i(TAG, result);
            System.out.println("getPropertycameraId:"
                    + resultObj.getProperty("cameraId"));

            System.out.println("serviceAddress:"
                    + resultObj.getProperty("serviceAddress"));

            System.out.println("数量:"+ resultObj.getPropertyCount());

            SoapObject resultObj1 = (SoapObject)resultObj.getProperty("list");

            try {
                System.out.println("recordType:"+ resultObj1.getProperty("recordType"));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                System.out.println("recordType异常：" + e.getMessage());
            }

            System.out.println("resultList数量:" + resultObj1.getPropertyCount());

            finalResults = new String[resultObj1.getPropertyCount()];

            for(int  i=0; i <(resultObj1.getPropertyCount()-1); i++){

                SoapObject   soapChilds   =(SoapObject)resultObj1.getProperty(i);
//                String bean = "";
                String bean = new String();
                finalResults[i] = new String();

                bean = soapChilds.getProperty("id") +";"+soapChilds.getProperty("cameraId")+";"+soapChilds.getProperty("deviceFullName")+";"+soapChilds.getProperty("fileName")+";"+soapChilds.getProperty("beginTime")+";"+soapChilds.getProperty("endTime")+";"+soapChilds.getProperty("filePath")+";"+soapChilds.getProperty("playUrl")+";"+soapChilds.getProperty("serviceAddress")+";"+soapChilds.getProperty("httpUrl")+";"+soapChilds.getProperty("deviceId");
                System.out.println("bean "+i+" :"+bean);

                recordFileEndTime = soapChilds.getProperty("endTime").toString();
                recordFileBeginTime = soapChilds.getProperty("beginTime").toString();
                System.out.println("record file start time = " + recordFileBeginTime);
                System.out.println("record file end time = " + recordFileEndTime);
                finalResults[i] = buildHttpURL(soapChilds.getProperty("filePath").toString(),soapChilds.getProperty("fileName").toString(), soapChilds.getProperty("httpUrl").toString());
//                if(i == 0)
//                {
//                    finalResult = buildHttpURL(soapChilds.getProperty("filePath").toString(),soapChilds.getProperty("fileName").toString(), soapChilds.getProperty("httpUrl").toString());
//                }
            }

        }catch (SoapFault soapFault){
            soapFault.printStackTrace();
        }

        return finalResults;
//        Log.i(TAG, "final result = " + finalResult);
//        return finalResult;
    }

    @Override
    public void onPostExecute(String[] finalResults) {
        super.onPostExecute(finalResults);

        if(finalResults[0] == null)
        {
            Toast.makeText(context, "该时间没有录像，请重新查询！", Toast.LENGTH_LONG).show();
        }
        if(finalResults[0] != null){
            Toast.makeText(context, finalResults[0].toString(), Toast.LENGTH_LONG).show();
        }
    }

    public Date StringToDate(String dateString){
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

    public static class RecordAllTypeAndStorageFileInfo {

        public RecordAllTypeAndStorageFileInfo() {
            Log.i("GetRecordFileList", "empty constructor invoked!");
        }

        //        public RecordAllTypeAndStorageFileInfo ()
//        {
//            super();
//        }

        private String cameraId;

        private List<RecordTypeAndStorageFileInfo> list;

        private String serviceAddress;

        public String getCameraId() {
            return cameraId;
        }

        public void setCameraId(String cameraId) {
            this.cameraId = cameraId;
        }

        public List<RecordTypeAndStorageFileInfo> getList() {
            return list;
        }

        public void setList(List<RecordTypeAndStorageFileInfo> list) {
            this.list = list;
        }

        public String getServiceAddress() {
            return serviceAddress;
        }

        public void setServiceAddress(String serviceAddress) {
            this.serviceAddress = serviceAddress;
        }
    }

    public class RecordTypeAndStorageFileInfo{

        public RecordTypeAndStorageFileInfo(){
            super();
        }
        private String recordType;

        private List<StorageFileInfo> list;

        public String getRecordType() {
            return recordType;
        }

        public void setRecordType(String recordType) {
            this.recordType = recordType;
        }

        public List<StorageFileInfo> getList() {
            return list;
        }

        public void setList(List<StorageFileInfo> list) {
            this.list = list;
        }
    }

    public class StorageFileInfo implements Serializable{

        /**
         * StorageFileInfo构造函数
         */
        public StorageFileInfo() {
            super();
        }

        /**
         * 域 编号(ID)
         */
        private String id;

        /**
         * 域 摄像机编号(CAMERA_ID）
         */
        private String cameraId;

        /**
         * 域 文件名称(FILE_NAME)
         */
        private String fileName;

        /**
         * 域 开始时间(BEGIN_TIME)
         */
        private Date beginTime;

        /**
         * 域 结束时间(END_TIME)
         */
        private Date endTime;

        /**
         * 域 文件路径(FILE_PATH)
         */
        private String filePath;

        /**
         * 域 报警编号(ALARM_ID)
         */
        private String alarmId;

        private String httpUrl;

        private String playUrl;

        private String deviceFullName;

        private String serviceAddress;

        /**
         * 属性 Id 的get方法
         * @return String
         */
        public String getId()
        {
            return id;
        }

        /**
         * 属性 Id 的set方法
         * @return void
         */
        public void setId(String id)
        {
            this.id = id;
        }


        /**
         * 属性 CameraId 的get方法
         * @return String
         */
        public String getCameraId()
        {
            return cameraId;
        }

        /**
         * 属性 CameraId 的set方法
         * @return void
         */
        public void setCameraId(String cameraId)
        {
            this.cameraId = cameraId;
        }


        /**
         * 属性 FileName 的get方法
         * @return String
         */
        public String getFileName()
        {
            return fileName;
        }

        /**
         * 属性 FileName 的set方法
         * @return void
         */
        public void setFileName(String fileName)
        {
            this.fileName = fileName;
        }


        /**
         * 属性 BeginTime 的get方法
         * @return Date
         */
        public Date getBeginTime()
        {
            return beginTime;
        }

        /**
         * 属性 BeginTime 的set方法
         * @return void
         */
        public void setBeginTime(Date beginTime)
        {
            this.beginTime = beginTime;
        }


        /**
         * 属性 EndTime 的get方法
         * @return Date
         */
        public Date getEndTime()
        {
            return endTime;
        }

        /**
         * 属性 EndTime 的set方法
         * @return void
         */
        public void setEndTime(Date endTime)
        {
            this.endTime = endTime;
        }


        /**
         * 属性 FilePath 的get方法
         * @return String
         */
        public String getFilePath()
        {
            return filePath;
        }

        /**
         * 属性 FilePath 的set方法
         * @return void
         */
        public void setFilePath(String filePath)
        {
            this.filePath = filePath;
        }


        /**
         * 属性 AlarmId 的get方法
         * @return String
         */
        public String getAlarmId()
        {
            return alarmId;
        }

        /**
         * 属性 AlarmId 的set方法
         * @return void
         */
        public void setAlarmId(String alarmId)
        {
            this.alarmId = alarmId;
        }

        public String getFilePathName(){
            return filePath+fileName;
        }

        public String getServiceAddress() {
            return serviceAddress;
        }

        public void setServiceAddress(String serviceAddress) {
            this.serviceAddress = serviceAddress;
        }

        public String getHttpUrl() {
            return httpUrl;
        }

        public void setHttpUrl(String httpUrl) {
            this.httpUrl = httpUrl;
        }

        public String getPlayUrl() {
            return playUrl;
        }

        public void setPlayUrl(String playUrl) {
            this.playUrl = playUrl;
        }

        public String getDeviceFullName() {
            return deviceFullName;
        }

        public void setDeviceFullName(String deviceFullName) {
            this.deviceFullName = deviceFullName;
        }

        public String toString()
        {
            StringBuffer sb = new StringBuffer();
            sb.append(" [ id="+ id);
            sb.append(", cameraId="+ cameraId);
            sb.append(", fileName="+ fileName);
            sb.append(", beginTime="+ beginTime);
            sb.append(", endTime="+ endTime);
            sb.append(", filePath="+ filePath);
            sb.append(", alarmId="+ alarmId);
            sb.append("]");

            return sb.toString();
        }

        @Override
        public boolean equals(Object obj) {
            StorageFileInfo other=(StorageFileInfo)obj;
            return this.toString().equals(other.toString());
        }

    }

    public String buildHttpURL(String filePath, String fileName, String httpUrl) {

        String _httpUrl;
        String virPath;

        Log.i(TAG,"befor translation, file path = " + filePath);
        filePath = filePath.replace("////", "//");
        filePath = filePath.replace("\\", "/");
        Log.i(TAG, "after translation, file path = "+filePath);


        if(filePath.indexOf(":")>0){
            virPath ="/" + filePath.substring(0, filePath.indexOf(":"));
            virPath = virPath.toLowerCase();
            filePath = filePath.substring(filePath.indexOf(":")+1);

        } else {
            virPath = "";
        }
        Log.i(TAG,"after, virPAth = "+virPath);

        _httpUrl = httpUrl.substring(0, httpUrl.indexOf("/RecordServer")); //rtmp
        Log.i(TAG, "_httpURL = " + _httpUrl);

        httpUrl = _httpUrl + virPath+ filePath + fileName;
        Log.i(TAG, "httpURL = "+httpUrl);

        return httpUrl;
    }
}