package jp.crudefox.library.help;


import java.util.ArrayList;

import jp.crudefox.library.R;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

public class PopupMenuDialog{

	
//	public static class CONTEXT_MENU_IDS{
//		
//		
//		public static final int BOOKMARK_GRID_EDIT = 800;
//		public static final int BOOKMARK_GRID_DELETE = 801;
//		public static final int BOOKMARK_GRID_ADD_CURRENT_PAGE = 850;
//		public static final int BOOKMARK_GRID_NEW_BOOKMARK = 851;
//		public static final int BOOKMARK_GRID_NEW_FOLDER = 852;
//		public static final int BOOKMARK_GRID_IMPORT = 853;
//
//	}
	
	public interface OnClientListener{
		public void onClick(PopupMenuDialog p,View v,int position,Object value);
		public void onDismiss(PopupMenuDialog p);
	}
	
	private OnClientListener mClientLis;
	public void setOnClientListener(OnClientListener lis){
		mClientLis = lis;
	}
	
	
	private Context mContext;
	private boolean mIsService;	
	private MyDialog mDialog;
	private WindowManager.LayoutParams mWindowParams;
	private LayoutInflater mInflater;

	private int mKiten;
	//View mKitenView;
	private Point mKitenPt = new Point();
	
	private View mRootView;
	private View mContent;
	private ListView mListView;
	private View mEmptyView;
	private TextView mEmptyTextView;

	
	
	public PopupMenuDialog(Context contxt,boolean is_service){
		mContext = contxt;
		mIsService = is_service;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		init();
	}
	
	public void initItem(){
		clearListItems(true);
	}
	
	public void addItem(Drawable icon,String text,Object val){
		ListItem item = new ListItem();
		item.title = text;
		item.icon = icon;
		item.value = val;
		mListItmes.add(item);
	}
	public void addItem(Bitmap icon,String text,Object val){
		ListItem item = new ListItem();
		item.title = text;
		item.icon = icon;
		item.value = val;
		mListItmes.add(item);
	}
	public void addItem(int icon,String text,Object val){
		ListItem item = new ListItem();
		item.title = text;
		item.icon = icon;
		item.value = val;
		mListItmes.add(item);
	}	

	
	
	private void init(){
		
		mDialog = new MyDialog(mContext,android.R.style.Theme_Translucent_NoTitleBar);
		
//		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
		
		Window window = mDialog.getWindow();

		int clrflags = 0
				| WindowManager.LayoutParams.FLAG_DIM_BEHIND;
	    int setflags = 0
	       		//| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
	    		//| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
	    		//| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
	            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
	            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
	            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
	            | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
	            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
	            //| WindowManager.LayoutParams.FLAG_DIM_BEHIND
	            ;
	    window.addFlags(setflags);
	    window.clearFlags(clrflags);

		mWindowParams = window.getAttributes();
				
		WindowManager.LayoutParams wmlp = mWindowParams;
	    wmlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
	    wmlp.width = WindowManager.LayoutParams.WRAP_CONTENT;
	    wmlp.gravity = Gravity.LEFT | Gravity.TOP;
	    wmlp.x = 0;
	    wmlp.y = 0;

	    wmlp.format = PixelFormat.TRANSLUCENT;
	    
	    if(mIsService){
	    	wmlp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
	    }

	    wmlp.dimAmount = 0;
	    
	    window.setAttributes(wmlp);
	    	    
//        if(!mIsFocusable)
//            wmlp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;		
		
	    
	    
	    
	     mRootView = mInflater.inflate(R.layout.popupmenudlg_layout, null);
	     mDialog.setContentView(mRootView);
	     
	     mContent  = mRootView.findViewById(R.id.content);
	     mListView  = (ListView) mRootView.findViewById(R.id.list_menu);
	     mEmptyView  =  mRootView.findViewById(R.id.empty_menu);
	     mEmptyTextView  = (TextView) mRootView.findViewById(R.id.empty_text);
	     
	    
		mListView.setAdapter(mListAdapter);
			
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
				ListItem item = mListItmes.get(position);
				if(mClientLis!=null){
					mClientLis.onClick( PopupMenuDialog.this ,view, position, item.value);
				}
			}
		});

		mListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
				AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
				
				int position = info.position;
				if( position == -1) return ;
//				final ListItem gitem = mListItmes.get(position);
//				MenuItem mi;
				
			}
		});
		
		mListView.setScrollingCacheEnabled(false);
		//mContent.setBackgroundColor(Color.argb(200, 255,255,255));
		mRootView.setBackgroundResource(R.drawable.popup_menu_dlg_frame);
	    
//		mContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {			
//			public void onFocusChange(View v, boolean hasFocus) {
//				
//			}
//		});
		
		//setWindowColor(0x80ff2020);
		//setContentSize(dm.widthPixels*6/8, dm.heightPixels*6/8);
		//setWindowSize(w, h);
		
		
	}
	
	public void setEmptyText(String text){
		mEmptyTextView.setText(text);
	}
	
	public void setEmptyView(){
		mListView.setVisibility(View.GONE);
		mEmptyView.setVisibility(View.VISIBLE);
		mEmptyView.bringToFront();
	}
	public void setListView(){
		mEmptyView.setVisibility(View.GONE);
		mListView.setVisibility(View.VISIBLE);
		mListView.bringToFront();
	}
	
	public boolean isShowing(){
		return mDialog.isShowing();
	}
	
	public void dismiss(){
		if( !mDialog.isShowing() ) return ;
		mDialog.dismiss();
		if(mClientLis!=null) mClientLis.onDismiss(PopupMenuDialog.this);
		clearListItems(true);
	}
	
	public void show(Point kitenPt,int kiten){
		mKiten = kiten;
		mKitenPt.set(kitenPt.x,kitenPt.y);
		//mKitenView = v;
		mListAdapter.notifyDataSetChanged();
		//int[] loc = new int[2];
		//v.getLocationOnScreen(loc);
		//setWindowPos(loc[0],loc[1]);
		setWindowPos(kitenPt.x,kitenPt.y);
		mDialog.show();
	}
	
	public void showAuto(View view){
		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
		int kiten;
		Point kitenPt = new Point();
		int[] loc = new int[2];
		view.getLocationOnScreen(loc);
		int x = loc[0]+view.getWidth()/2;
		int y = loc[1]+view.getHeight()/2;
		if(x<dm.widthPixels/2 && y<dm.heightPixels/2){
			kiten = PopupMenuDialog.KITEN_LT;
			kitenPt.set(loc[0]+view.getWidth(), loc[1]+view.getHeight());
		}
		else if(x<dm.widthPixels/2){
			kiten = PopupMenuDialog.KITEN_LB;
			kitenPt.set(loc[0]+view.getWidth(), loc[1]);
		}
		else if(y<dm.heightPixels/2){
			kiten = PopupMenuDialog.KITEN_RT;
			kitenPt.set(loc[0], loc[1]+view.getHeight());
		}
		else{
			kiten = PopupMenuDialog.KITEN_RB;
			kitenPt.set(loc[0], loc[1]);
		}
		PopupMenuDialog.this.show(kitenPt, kiten);		
	}
	
	
	
	public static final int KITEN_LT = 0;
	public static final int KITEN_RT = 1;
	public static final int KITEN_LB = 2;
	public static final int KITEN_RB = 3;

	private Point calcPopupLocation(int kiten_x,int kiten_y,int kiten){
		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
		int[] location = new int[]{kiten_x,kiten_y};
		int[] winloc = new int[]{(int)(20*dm.density),(int)(30*dm.density)};
		//mContent.getLocation
		//v.getLocationOnScreen(location);
		int w = mContent.getWidth();
		int h = mContent.getHeight();

		Point pt;
		if(kiten==KITEN_LT){
			pt = new Point(location[0],location[1]);
		}
		else if(kiten==KITEN_RT){
			pt = new Point(location[0]-w,location[1]);
		}
		else if(kiten==KITEN_LB){
			pt = new Point(location[0],location[1]-h);
		}
		else{
			pt = new Point(location[0]-w,location[1]-h);
		}

		if(pt.x+w>dm.widthPixels){
			pt.x = dm.widthPixels-w;
		}else if(pt.x<winloc[0]){
			pt.x = winloc[0];
		}
		if(pt.y+h>dm.heightPixels){
			pt.y = dm.heightPixels-h;
		}else if(pt.y<winloc[1]){
			pt.y = winloc[1];
		}		
		
		return pt;
	}

	
	public int getWindowX(){
		return mWindowParams.x;
	}
	public int getWindowY(){
		return mWindowParams.y;
	}	
	public void setWindowPos(int x,int y){
		mWindowParams.x = x;
		mWindowParams.y = y;
		updateWindow();
	}
	

//	public void setWindowSize(int w,int h){
//		mWindowParams.width = w;
//		mWindowParams.height = h;
//		updateWindow();
//	}
	public void updateWindow(){
		mDialog.getWindow().setAttributes(mWindowParams);
	}
	public void setContentSize(int w,int h){
		FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mContent.getLayoutParams();
		lp.width = w;
		lp.height = h;
		mContent.setLayoutParams(lp);
	}
	public void setContentWidth(int w){
		FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mContent.getLayoutParams();
		lp.width = w;
		mContent.setLayoutParams(lp);
	}
	public void setContentHeight(int h){
		FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mContent.getLayoutParams();
		lp.height = h;
		mContent.setLayoutParams(lp);
	}
	
	public int getContentLayoutWidth(){
		return mContent.getLayoutParams().width;
	}
	public int getContentLayoutHeight(){
		return mContent.getLayoutParams().height;
	}


	
	
//	enum BookmarksGridItemType{
//		Bookmark,
//		New,
//	}
	
	
	private static class ListItem{
//		String url;
//		Bitmap icon;
//		String title;
		
		Object icon;
		
		String title;
		Object value;
		
//		BookmarksGridItemType type = BookmarksGridItemType.Bookmark;
		
//		Bookmark item;
		
		public void clean(){
//			item = null;
//			if(icon!=null && !icon.isMutable() && !icon.isRecycled())
//				icon.recycle();
//			icon = null;
//			url = null;
//			title = null;
		}
	}
	
	//BookmarkManager.BookmarkItem mBookmarkDir = null;
	//Bookmark mBookmarkDir;
	private final ArrayList<ListItem> mListItmes = new ArrayList<ListItem>();

	private final BaseAdapter mListAdapter = new BaseAdapter() {
		
		public View getView(int position, View convertView, ViewGroup parent) {
			if(position>=mListItmes.size()) return null;
			
			View view =  convertView;
			if(view==null){
				view = mInflater.inflate(R.layout.popupmenudlg_line, null);
			}
			
			ImageView iv = (ImageView) view.findViewById(R.id.popup_icon);
			TextView tv = (TextView) view.findViewById(R.id.popup_title);

			ListItem item = mListItmes.get(position);


			if(item.icon instanceof Bitmap){
				iv.setImageBitmap((Bitmap)item.icon);
			}else if(item.icon instanceof Drawable){
				iv.setImageDrawable((Drawable)item.icon);
			}else if(item.icon instanceof Integer){
				iv.setImageResource((Integer)item.icon);
			}else{
				iv.setImageResource(0);
			}
			
			tv.setText(item.title!=null?item.title:"");


//			if( mFloatGallery.getSelectItem() == item ){
//				wr.setBackgroundColor(Color.argb(200, 0, 200, 255));
//			}else{
//				wr.setBackgroundColor(0);
//			}
			
			return view;
		}
		
		public long getItemId(int position) {
			return position;
		}
		
		public Object getItem(int position) {
			return mListItmes.get(position);
		}
		
		public int getCount() {
			return mListItmes.size();
		}
	};
	
	
	
	private void clearListItems(boolean notify){
		for(ListItem item : mListItmes){
			item.clean();
		}
		mListItmes.clear();
//		mBookmarkDir = null;
		
		if(notify) mListAdapter.notifyDataSetChanged();
	}
	
//	private void setBookmarksItems(Bookmark dir,boolean notify){
//		
//		clearListItems(false);
//		
////		BookmarkManager bm = BookmarkManager.getInstance();
////		BookmarkItem br = bm.getBookmarkRoot();
//		
////		mBookmarkDir = dir;
//
//		BookmarksGridItem bi_new = new BookmarksGridItem();
//		bi_new.type = BookmarksGridItemType.New;
//		mBookmarksItmes.add(bi_new);
//		
//		if(dir.isFolder()){
//			for( Bookmark bitem : dir.list() ){
//				BookmarksGridItem item = new BookmarksGridItem();
//				item.item = bitem;
//				mBookmarksItmes.add(item);
//			}
//		}
//		
////		setBtnState();
//
//		if(notify) mBookmarksGridAdapter.notifyDataSetChanged();
//	}	
	

	class MyDialog extends Dialog{

		public MyDialog(Context context, int theme) {
			super(context, theme);

		}
		

		@Override
		public void onWindowFocusChanged(boolean hasFocus) {			
			super.onWindowFocusChanged(hasFocus);
			
			Point pt = calcPopupLocation(mKitenPt.x,mKitenPt.y, mKiten);
			
			int[] con_loc = new int[2];
			mContent.getLocationInWindow(con_loc);
			
			setWindowPos(pt.x-con_loc[0], pt.y-con_loc[1]);
			
//			DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
//			
//			View dec = getWindow().getDecorView();
//			int w = dec.getWidth();
//			int h = dec.getHeight();
//			
//			int x = getWindowX();
//			int y = getWindowY();
//
//			
//			//setWindowPos(x, y);
			
		}


		@Override
		public boolean onTouchEvent(MotionEvent event) {
			
			int action = event.getActionMasked();
			
			if(action==MotionEvent.ACTION_OUTSIDE){
				
				PopupMenuDialog.this.dismiss();
				
				return true;
			}
			
			return super.onTouchEvent(event);
		}


		@Override
		public void onBackPressed() {
			//super.onBackPressed();
			//mWebWindow.onBackPressed();
			PopupMenuDialog.this.dismiss();
		}



		@Override
		public boolean dispatchKeyEvent(KeyEvent event) {
			
			
			return super.dispatchKeyEvent(event);
		}

		@Override
		public boolean dispatchTouchEvent(MotionEvent ev) {
			
			
			return super.dispatchTouchEvent(ev);
		}

		@Override
		public boolean onContextItemSelected(MenuItem item) {
			
			LibUtil.Log("onContextItemSelected");
			
//			if( mWebWindow.onContextItemSelected(item) ){
//				return true;
//			}			
			return super.onContextItemSelected(item);
		}
		

		@Override
		public boolean onMenuItemSelected(int featureId, MenuItem item) {
			
			LibUtil.Log("onMenuItemSelected "+featureId);
			
	
			
			return super.onMenuItemSelected(featureId, item);
		}

		@Override
		public void onContextMenuClosed(Menu menu) {
			
			LibUtil.Log("onContextMenuClosed");

			super.onContextMenuClosed(menu);
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {

			LibUtil.Log("onCreateContextMenu");
			
			super.onCreateContextMenu(menu, v, menuInfo);
		}
		
		
		
	}
	
	private Toast mToast;
	public void toast(String str){
    	if(mToast==null){
    		mToast = Toast.makeText(mContext.getApplicationContext(), str, Toast.LENGTH_SHORT);
    	}else{
    		mToast.setText(str);
    	}
    	mToast.show();
    }	
	
	
	
}
