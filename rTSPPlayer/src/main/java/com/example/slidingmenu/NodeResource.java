package com.example.slidingmenu;

import android.util.Log;

public class NodeResource {
	
	private static String TAG = "NodeResource";
	
	protected String parentId;
	protected String curId;
	protected String name;
	protected String cameraId;
	protected int iconId;
	protected int port;
	protected boolean isSpeedDomeCamera;

	public NodeResource(String parentId, String curId, String name,
			String cameraID, int iconId, String isSpeedDomeCamera) {
		super();
		this.curId = curId;
		this.parentId = parentId;
		this.name = name;
		this.cameraId = cameraID;
		this.iconId = iconId;
		this.port = Integer.parseInt(curId);
		this.isSpeedDomeCamera = isSpeedDomeCamera.equals("true")?true:false;
	}

	public boolean isSpeedDomeCamera(){
		return isSpeedDomeCamera;
	}
	
	public int getPort(){
		return port;
	}
	
	public String getParentId() {
		return parentId;
	}

	public String getName() {
		return this.name;
	}

	public String getCameraId() {
		Log.i(TAG, "NodeResource.getCameraId, return "+this.cameraId);
		return this.cameraId;
	}

	public int getIconId() {
		return iconId;
	}

	public String getCurId() {
		return curId;
	}

}
