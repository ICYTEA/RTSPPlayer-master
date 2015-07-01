package com.example.slidingmenu;

import java.util.ArrayList;
import java.util.List;

import com.example.rtspplayer.R;
import com.slidingmenu.lib.SlidingMenu;

import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SampleListFragment extends Fragment {

	private TreeListView treeView;
	private View mView;
	private SlidingMenu mSlidingMenu;
	
	public SampleListFragment(SlidingMenu mSlidingMenu) {
		// TODO Auto-generated constructor stub
		this.mSlidingMenu = mSlidingMenu;
	}	

	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		treeView = (TreeListView) inflater.inflate(R.layout.fragmentlist, container, false);
		treeView = new TreeListView(getActivity(),initNodeTree(),this.mSlidingMenu);
		return treeView;
	}
	
	 public List<NodeResource> initNodeTree(){
	    	List <NodeResource> list = new ArrayList<NodeResource>();
	    	NodePullParse nodePullParse = new NodePullParse();
	    	XmlResourceParser xmlParser = null;
		        try {
		        	xmlParser = getActivity().getResources().getXml(R.xml.provinceandcity);
		        } catch (Exception e) { 
		            e.printStackTrace(); 
		        }
	    	list = nodePullParse.ParseXml(xmlParser);
//	    	NodeResource n1 = new NodeResource(""+10098, ""+0, "根节点,自己是0", "dfs", R.drawable.icon_department);
//	    	list.add(n1);
	    	return list;   
	    }

}
