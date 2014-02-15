package jp.crudefox.library.help;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.ImageButton;

public class SwitchImageButton extends ImageButton implements Checkable{
	
	boolean mIsChecked = false;
	
//	Drawable mONDrawable;
//	Drawable mOFFDrawable;

	public SwitchImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}

	public boolean isChecked() {
		return mIsChecked;
	}

	public void setChecked(boolean checked) {
		mIsChecked = checked;
		//Drawable d = getDrawable();
		if(mIsChecked){
			setImageState(new int[]{ android.R.attr.state_checked}, true);
			//if(d!=null) d.setState(new int[]{ android.R.attr.state_checked});
			//setImageState(state, merge);
			//setImageDrawable(mONDrawable);
		}else{
			setImageState(new int[]{}, true);
			//if(d!=null) d.setState(new int[]{});
			//setImageDrawable(mOFFDrawable);
		}
	}

	public void toggle() {
		setChecked(!isChecked());
	}

	@Override
	public boolean performClick() {
		toggle();
		return super.performClick();
	}
	
	

	
}
