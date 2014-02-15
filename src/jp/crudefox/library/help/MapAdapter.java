package jp.crudefox.library.help;

import java.util.ArrayList;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;


public class MapAdapter<T> extends BaseAdapter{
	
	ArrayAdapter<String> mAdapter;
	ArrayList<Data> mList = new ArrayList<MapAdapter<T>.Data>();
	
	
	public MapAdapter(Context context, int textViewResourceId) {
		super();
		mAdapter = new ArrayAdapter<String>(context, textViewResourceId);
	}

	public void addItem(String text,T value){
		mList.add(new Data(text, value));
		mAdapter.add(text);
	}
	public int getItemPosition(T item){
		for(int i=0;i<mList.size();i++){
			if(mList.get(i).value.equals(item)){
				return i;
			}
		}
		return -1;
	}
	
	public void clearItems(){
		mList.clear();
		mAdapter.clear();
	}
	
	

	class Data{
		String text;
		T value;
		public Data(String text,T value){
			this.text = text;
			this.value = value;
		}
	}



	public int getCount() {
		return mList.size();
	}

	public T getItem(int position) {
		return mList.get(position).value;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {	
		return mAdapter.getView(position, convertView, parent);
	}
	
}
