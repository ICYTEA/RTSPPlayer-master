package com.example.slidingmenu;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import com.example.rtspplayer.R;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;

public class NodePullParse {
	
	private static String TAG = "NodePullParse";
	
    public static ArrayList<NodeResource> ParseXml(XmlPullParser parser){
        ArrayList<NodeResource> DeviceArray = new ArrayList<NodeResource>();
        
        try {
            //开始解析事件
            int eventType = parser.getEventType();

            //处理事件，不碰到文档结束就一直处理
            while (eventType != XmlPullParser.END_DOCUMENT) {                
                //因为定义了一堆静态常量，所以这里可以用switch
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        // 不做任何操作或初开始化数据
                        break;

                    case XmlPullParser.START_TAG:
                        // 解析XML节点数据
                        // 获取当前标签名字
                        String tagName = parser.getName();

                        if(tagName.equals("device")){
                        	String parentId = parser.getAttributeValue(0);
                        	String curId = parser.getAttributeValue(1);
                        	String name = parser.getAttributeValue(2);
                        	String cameraId = parser.getAttributeValue(3);
                        	String isSpeedDomeCamera = parser.getAttributeValue(4);
                            String cameraCode = parser.getAttributeValue(5);
                        	
                        	NodeResource deviceTemp = new NodeResource(parentId, curId, name, cameraId,R.drawable.icon_department, isSpeedDomeCamera, cameraCode);
                            DeviceArray.add(deviceTemp);                            
                        }else if(tagName.equals("site")||tagName.equals("company")||tagName.equals("city")||tagName.equals("province")){
                          	String parentId = parser.getAttributeValue(0);
                        	String curId = parser.getAttributeValue(1);
                        	String name = parser.getAttributeValue(2);
                        	String cameraId = "-1";
                            String cameraCode = "-1";
                        	NodeResource deviceTemp = new NodeResource(parentId, curId, name, cameraId,R.drawable.icon_department, "false", cameraCode);
                            DeviceArray.add(deviceTemp);  
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        // 单节点完成，可往集合里边添加新的数据
                        break;
                    case XmlPullParser.END_DOCUMENT:

                        break;
                }

                // next方法处理下一个事件
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        return DeviceArray;
    }
    

}
