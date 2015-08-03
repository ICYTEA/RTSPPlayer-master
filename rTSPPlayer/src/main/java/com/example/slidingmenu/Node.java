package com.example.slidingmenu;
import java.util.ArrayList;
import java.util.List;

public class Node {
private Node parent = null; // 父节点
private List<Node> childrens = new ArrayList<Node>();//子节点
private String name;//节点显示文字
private String cameraID;//设备Id（请求视频流）
private String cameraCode;//设备编号（调用录像）
private int port;//端口值
private int icon = -1; //icon(R.drawable的id)
private boolean isChecked = false; //是否被选中
private boolean isExpand = true;//是否处于扩展状态
private boolean hasCheckBox = true;//是否有复选框
private String parentId = null;
private String curId = null;
private boolean isVisiable = true;
private boolean isSpeedDomeCamera;
	/**
	 * 设置节点值
	 * 
	 * @param parentId
	 *            TODO
	 * @param curId
	 *            TODO
	 */
public Node(String name,String cameraID, String cameraCode, String parentId,String curId, int iconId, int port, boolean isSpeedDomeCamera) {
	// TODO Auto-generated constructor stub
	this.name = name;
	this.cameraID = cameraID;
	this.cameraCode = cameraCode;
	this.parentId = parentId;
	this.icon = iconId;
	this.curId = curId;
	this.port = port;
	this.isSpeedDomeCamera = isSpeedDomeCamera;
}
/**
 * 得到父节点
 * @return
 *
 */
public Node getParent() {
	return parent;
}
/**
 * 设置父节点
 * @param parent
 *
 */
public void setParent(Node parent) {
	this.parent = parent;
}
/**
 * 得到子节点
 * @return
 *
 */
public List<Node> getChildrens() {
	return childrens;
}
/**
 * 是否根节点
 * @return
 *
 */
public boolean isRoot(){
	return parent ==null?true:false;
}
/**
 * 是否隐藏图标
 * @return
 *
 */
public int getIcon() {
	return icon;
}
/**
 * 设置图标
 * @param icon
 *
 */
public void setIcon(int icon) {
	this.icon = icon;
}

/**
 * 是否是展开状态
 * @return
 *
 */
public boolean isExplaned() {
	return isExpand;
}
/**
 * 设置展开状态
 * @param isExplaned
 *
 */
public void setExplaned(boolean isExplaned) {
	this.isExpand = isExplaned;
}

/**
 * 得到端口值
 * @return
 *
 */
public int getPort(){
	return port;
}
/**
 * 设置端口值
 * @return
 *
 */
public void setPort(int port){
	this.port = port;
}
/**
 * 得到节点标题
 * @return
 *
 */
public String getName() {
	return name;
}
/**
 * 设置节点标题
 * @param title
 *
 */
public void setName(String name) {
	this.name = name;
}
/**
 * 得到设备Id
 * @return
 *
 */
public String getCameraID() {
	return cameraID;
}
/**
 * 设置设备Id
 * @param value
 *
 */
public void setCameraID(String cameraID) {
	this.cameraID = cameraID;
}

public String getCameraCode(){
	return this.cameraCode;
}

public void setCameraCode(String cameraCode){
	this.cameraCode = cameraCode;
}
/**
 * 增加一个子节点
 * @param node
 *
 */
public void addNode(Node node){
	if(!childrens.contains(node)){
		childrens.add(node);
	}
}
/**
 * 移除一个子节点
 * @param node
 *
 */
public void removeNode(Node node){
	if(childrens.contains(node))
		childrens.remove(node);
}
/**
 * 移除指定位置的子节点
 * @param location
 *
 */
public void removeNode(int location){
	childrens.remove(location);
}
/**
 * 清除所有子节点
 *
 */
public void clears(){
	childrens.clear();
}
/**
 * 判断给出的节点是否当前节点的父节点
 * @param node
 * @return
 *
 */
public boolean isParent(Node node){
	if(parent == null)return false;
	if(parent.equals(node))return true;
	return parent.isParent(node);
}
/**
 * 递归获取当前节点级别
 * @return
 *
 */
public int getLevel(){
	return parent ==null?0:parent.getLevel()+1;
}
/**
 * 父节点是否处于折叠的状态
 * @return
 *
 */
public boolean isParentCollapsed(){
	if(parent ==null)return false;
	if(!parent.isExplaned())return true;
	return parent.isParentCollapsed();
}
/**
 * 是否叶节点（没有展开下级的几点）
       * @return
       *
 */
public boolean isLeaf(){
       return childrens.size()<1?true:false;
}
 /**
  * 返回自己的id
 * @return
 **/
public String getCurId() {
	// TODO Auto-generated method stub
	return curId;
}
/**
 * 返回的父id
* @return
**/
public String getParentId() {
	// TODO Auto-generated method stub
	return parentId;
}
}
