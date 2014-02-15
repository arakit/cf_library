package jp.crudefox.library.help;

import java.util.ArrayList;

import jp.crudefox.library.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class PopupMenu extends PopupWindow{

	Context mContext;	
	float sScaleDensity;
	
	ListView mListView = null;
	View mMenuLayout = null;
	ArrayList<ListData> mListData = null;
	OnSelectMenuItemListener mSelectListener = null;
	LayoutInflater mInflater = null;
	
	int mSelectedIndex = -1;
	
	class ListData{
		String text;
		Drawable icon;
		int id;
		Object value;

		public ListData(int id,String text,Drawable icon) {
			this.id = id;
			this.text = text;
			this.icon = icon;
			this.value = null;
		}
		public ListData(int id,Object value,String text,Drawable icon) {
			this.id = id;
			this.text = text;
			this.icon = icon;
			this.value = value;
		}
	}
	public interface OnSelectMenuItemListener{
		public void onSelected(String text,int id,Object value);
	}
	
	
	public PopupMenu(Context context) {
		super(context);
		mContext = context;
		sScaleDensity = mContext.getResources().getDisplayMetrics().scaledDensity;
		mListData = new ArrayList<PopupMenu.ListData>();
		mInflater = LayoutInflater.from(context);
		mMenuLayout = mInflater.inflate(R.layout.popupmenu_layout, null);
		mListView = (ListView) mMenuLayout.findViewById(R.id.list_menu);
		mListView.setScrollingCacheEnabled(false);
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
				if(mSelectListener!=null){
					ListData data = mListData.get(position);
					mSelectListener.onSelected(data.text,data.id,data.value);
				}
				dismiss();
			}			
		});
		Resources r = context.getResources();
		setBackgroundDrawable(r.getDrawable(R.drawable.popup_background_black));
		
	}
	
	public void setOnMenuItemSelectListener(OnSelectMenuItemListener lis){
		mSelectListener = lis;
	}
	public void setSelectedIndex(int index){
		mSelectedIndex = index;
	}
	public void setSelectedIndexByValue(Object value){
		int index = getItemIndexByValue(value);
		mSelectedIndex = index;
	}	
	public int getItemIndexByValue(Object value){
		for(int i=0;i<mListData.size();i++){
			ListData data = mListData.get(i);
			if(data.value!=null && data.value.equals(value)){
				return i;
			}
		}
		return -1;
	}

	public void setInit(){
		mListData.clear();
		mListView.setAdapter(mListAdapter);
	}
	public void addItem(String text,int id,Drawable icon){
		mListData.add(new ListData(id,text,icon));
	}
	public void addItem(String text,int id,Object value,Drawable icon){
		mListData.add(new ListData(id,value,text,icon));
	}
	public void addItem(String text,int id,Object value,int icon){
		Resources r = mContext.getResources();
		Drawable draw = icon!=0 ? r.getDrawable(icon) : null;
		mListData.add(new ListData(id,value,text,draw));
	}
	public void addItem(String text,int id,int icon){
		Resources r = mContext.getResources();
		Drawable draw = icon!=0 ? r.getDrawable(icon) : null;
		mListData.add(new ListData(id,text,draw));
	}
	
	public void showList(View anchor,int xoff,int yoff){

		setContentView(mMenuLayout);
		setWindowLayoutMode(0,0);
		setWidth((int)(250*sScaleDensity));
		setHeight(LayoutParams.WRAP_CONTENT);
		setFocusable(true);
		setTouchable(true);
		setOutsideTouchable(true);
		showAsDropDown(anchor, xoff, yoff );
	}
	public void showList(View parent){
		setContentView(mMenuLayout);
		setWindowLayoutMode(0,0);
		//setWindowLayoutMode(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		setWidth((int)(250*sScaleDensity));
		setHeight(LayoutParams.WRAP_CONTENT);
		setFocusable(true);
		setTouchable(true);
		setOutsideTouchable(true);
		showAtLocation(parent, Gravity.CENTER, 0, 0);
	}
	public void showListCenterAndSize(View parent,int width,int height){
		setContentView(mMenuLayout);
		setWindowLayoutMode(0,0);
		//setWindowLayoutMode(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		setWidth(width);
		setHeight(height);
		setFocusable(true);
		setTouchable(true);
		setOutsideTouchable(true);
		showAtLocation(parent, Gravity.CENTER, 0, 0);
	}
	
	
	
	ListAdapter mListAdapter = 	new BaseAdapter() {
		
		public View getView(int position, View convertView, ViewGroup parent) {
			View line = convertView;
			if(line==null){
				line = mInflater.inflate(R.layout.popupmenu_line, null);
			}
			ViewGroup wr = (ViewGroup) line.findViewById(R.id.popup_wrap);
			TextView tv = (TextView)line.findViewById(R.id.popup_title);
			ImageView iv = (ImageView)line.findViewById(R.id.popup_icon);
			
			ListData data = mListData.get(position);
			tv.setText(data.text);
			iv.setImageDrawable(data.icon);
			
			if(mSelectedIndex==position){
				//wr.setSelected(true);
				//line.setBackgroundColor(Color.argb(128, 128,128,255));
				wr.setBackgroundColor(Color.argb(128, 128,200,255));
			}else{
				//wr.setSelected(false);
				//line.setBackgroundColor(Color.TRANSPARENT);
				wr.setBackgroundColor(Color.TRANSPARENT);
			}
			
			return line;
		}
		
		public long getItemId(int position) {
			return mListData.get(position).id;
		}
		
		public Object getItem(int position) {
			return mListData.get(position);
		}
		
		public int getCount() {
			return mListData.size();
		}
	};
	
}
