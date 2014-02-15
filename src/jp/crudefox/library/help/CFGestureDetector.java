package jp.crudefox.library.help;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.PointF;
import android.os.SystemClock;
import android.view.MotionEvent;

public class CFGestureDetector  {

	private final float mDensity;

	public class TouchManager{

		private final ArrayList<TouchInfo> mTouchsInfo = new ArrayList<CFGestureDetector.TouchInfo>();

		private TouchManager(){

		}

		public TouchInfo getById(int id){
			int size = mTouchsInfo.size();
			for(int i=0;i<size;i++){
				TouchInfo ti = mTouchsInfo.get(i);
				if(ti.pointer_id==id) return ti;
			}
			return null;
		}
		public boolean containById(int id){
			return getById(id)!=null;
		}
		public TouchInfo getByIndex(int index){
			return mTouchsInfo.get(index);
		}
		private void put(TouchInfo ti){
			removeBydId(ti.pointer_id);
			mTouchsInfo.add(ti);
		}
		public void removeBydId(int id){
			remove(getById(id));
		}
		public void remove(TouchInfo ti){
			if(ti!=null) mTouchsInfo.remove(ti);
		}


		public int getTouchCount(){
			return mTouchsInfo.size();
		}
//		public void setPinchByPointerId(int id0, int id1){
//			if(id0==id1 || !containById(id0) || !containById(id1)) return ;
//			mScalePinchId0 = id0;
//			mScalePinchId1 = id1;
//		}

	}

	public interface OnGestureListener{
		public boolean onDown(TouchManager tm,TouchInfo ti);
		public void onMoved(TouchManager tm);
		public void onMove(TouchManager tm,TouchInfo ti);
		public void onUp(TouchManager tm,TouchInfo ti,boolean cancel);
	}

	public static class TouchInfo{
		//public static final int INVALID_DOWN_ID = -1;

		private int pointer_id;


		//public int down_kid = INVALID_DOWN_ID;
		public final PointF raw_pt = new PointF();
		public final PointF down_pt = new PointF();
		public final PointF move_pt = new PointF();
		public final PointF rela_pt = new PointF();
		public final PointF dist_pt = new PointF();

		public long down_uptimeMillis;

		public boolean isMoved = false;
		//距離がこの値を超えたら(>)
		private float moved_threshold = 0;

		public Object object;

		public int getPointerId(){
			return pointer_id;
		}
		public void setMovedThreshold(float threshold){
			moved_threshold = threshold;
		}
	}

	private final Context mContext;

	private final TouchManager mTouchManager = new TouchManager();



//	private int mScalePinchId0 = -1;
//	private int mScalePinchId1 = -1;

	private OnGestureListener mClientGestureListener;



	public CFGestureDetector(Context context){
		mContext = context;
		mDensity = context.getResources().getDisplayMetrics().density;
	}


	public void setOnGestureListener(OnGestureListener lis){
		mClientGestureListener = lis;
	}

	protected TouchInfo newTouchInfo() {
		return new TouchInfo();
	}


	private void onTouchDown(int id, float x,float y,float row_x,float row_y){
		final TouchInfo ti = newTouchInfo();
		ti.pointer_id = id;
		ti.raw_pt.set(row_x, row_y);
		ti.down_pt.set(x, y);
		ti.move_pt.set(x, y);
		ti.dist_pt.set(0, 0);
		ti.rela_pt.set(0, 0);
		ti.down_uptimeMillis = SystemClock.uptimeMillis();
		mTouchManager.put(ti);

		boolean ret = false;

		if(mClientGestureListener!=null)
			ret = mClientGestureListener.onDown(mTouchManager,ti);

		if(ret==false) mTouchManager.removeBydId(id);

		return ;
	}
	private void onTouchMoved(){
		if(mTouchManager.getTouchCount()==0) return ;
		if(mClientGestureListener!=null)
			mClientGestureListener.onMoved(mTouchManager);
	}
	private void onTouchMove(int id, float x,float y,float row_x,float row_y){
		TouchInfo ti = mTouchManager.getById(id);
		if(ti==null) return ;
		ti.raw_pt.set(row_x, row_y);
		ti.rela_pt.set(x-ti.move_pt.x, y-ti.move_pt.y);
		ti.move_pt.set(x, y);
		ti.dist_pt.set(x-ti.down_pt.x, y-ti.down_pt.y);
		if(!ti.isMoved){
			if( ti.dist_pt.length() > ti.moved_threshold){
				ti.isMoved = true;
			}
		}

		if(mClientGestureListener!=null)
			mClientGestureListener.onMove(mTouchManager,ti);


	}
	private void onTouchUp(int id, float x,float y,float row_x,float row_y,boolean cancel){
		TouchInfo ti = mTouchManager.getById(id);
		if(ti==null) return ;

		if(mClientGestureListener!=null)
			mClientGestureListener.onUp(mTouchManager,ti, cancel);

		mTouchManager.removeBydId(id);
	}


	public boolean onTouchEvent(MotionEvent event) {


		int action=(event.getAction()&MotionEvent.ACTION_MASK);

        if (action==MotionEvent.ACTION_DOWN) {
        	final int intdex = 0;
        	final int id = event.getPointerId(intdex);
            float x = event.getX(intdex);
            float y = event.getY(intdex);
            LibUtil.Log("Action_Down id="+id);
            onTouchDown(id, x, y,event.getRawX(),event.getRawY());
            return true;
        }
        else if (action==MotionEvent.ACTION_POINTER_DOWN) {
//	        	int index = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
//	        			>> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
    		final int index = event.getActionIndex();
    		final int id = event.getPointerId(index);
    		float x = event.getX(index);
    		float y = event.getY(index);
            LibUtil.Log("Action_Down("+index+") id="+id);
    		onTouchDown(id, x, y,event.getRawX(),event.getRawY());
    		return true;
        }
        else if (action==MotionEvent.ACTION_MOVE) {
    	    int count = event.getPointerCount();
    	    for (int i=0; i < count; i++) {
    			int id = event.getPointerId(i);
    			float x = event.getX(i);
    			float y = event.getY(i);
    			onTouchMove(id,x,y,event.getRawX(),event.getRawY());
    	    }
    	    onTouchMoved();
    	    return true;
        }
        else if (action==MotionEvent.ACTION_CANCEL) {
    	    int count = event.getPointerCount();
    	    for (int i=0; i < count; i++) {
    			int id = event.getPointerId(i);
    			float x = event.getX(i);
    			float y = event.getY(i);
    			onTouchUp(id,x,y,event.getRawX(),event.getRawY(),true);
    	    }
            LibUtil.Log("Action_Cancel; touch.getCount()="+mTouchManager.getTouchCount());
            return true;
        }
        else if (action==MotionEvent.ACTION_UP) {
            //touchX=-1;
        	final int index = 0;
        	final int id = event.getPointerId(index);
        	onTouchUp(id,event.getX(index),event.getY(index),event.getRawX(),event.getRawY(),false);
            LibUtil.Log("Action_Up id="+id+"; touch.getCount()="+mTouchManager.getTouchCount());
            return true;
        }
        else if (action==MotionEvent.ACTION_POINTER_UP) {
//        	int index = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
//        			>> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        	final int index = event.getActionIndex();
        	final int id = event.getPointerId(index);
        	onTouchUp(id,event.getX(index),event.getY(index),event.getRawX(),event.getRawY(),false);
            LibUtil.Log("Action_Up"+index+" id="+id);
            return true;
        }

        return false;

	}

}
