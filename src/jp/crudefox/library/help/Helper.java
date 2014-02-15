package jp.crudefox.library.help;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.Surface;
import android.view.WindowManager;

public class Helper {
	
	
	public final static long LONG_4GB = 4294967295l;
	
	
	
	public static boolean isOk_SDK_Gingerbread(){
		return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD;
	}
	public static boolean isOk_SDK_HoneyCombo(){
		return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB;
	}
	public static boolean isOk_SDK_JellyBean(){
		return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN;
	}
	public static boolean isOk_SDK_ICS(){
		return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	}
	public static boolean isOk_SDK(int sdk_int){
		return android.os.Build.VERSION.SDK_INT >= sdk_int;
	}
	
	
	
    public static double log(double val,double tei){
    	return Math.log(val)/Math.log(tei);
    }
	
	public static final double floorPowerOf(double v,double kitei){
		v = Math.abs(v);
		double log = log(v, kitei);
		int kiri = (int) Math.floor(log);
		return Math.pow(kitei, kiri);
	}
	public static final double roundPowerOf(double v,double kitei){
		v = Math.abs(v);
		double log = log(v, kitei);
		int kiri = (int) Math.floor(log+0.5);
		return Math.pow(kitei, kiri);
	}
	
	public static String toAutoBigValueString(long v,int kitei){
		if(v<kitei)	return String.format("%d", v);
		long kb = v/kitei;
		if(kb<kitei) return String.format("%dK", kb );
		long mb = kb/kitei;
		if(mb<kitei) return String.format("%dM", mb );
		long gb = mb/kitei;
		if(gb<kitei) return String.format("%dG", gb );
		long tb = gb/kitei;
		return String.format("%dT", tb );
	}
	
	

	public static boolean containsChar(String str,CharSequence c){
		for(int i=0;i<c.length();i++){
			if(str.contains(""+c.charAt(i))) return true;
		}
		return false;
	}
	public static boolean containsChar(String str,char[] c){
		for(int i=0;i<c.length;i++){
			if(str.contains(""+c[i])) return true;
		}
		return false;
	}

	public static float to0to360ByDegree(float degree){
		degree = degree % 360;
		if(degree<0) degree += 360;
		return degree;
	}
	public static int to0to360ByDegree(int degree){
		degree = degree % 360;
		if(degree<0) degree += 360;
		return degree;
	}
	
	
    public boolean isDiffRotateRect(int display_degree,int camera_degree){
    	int difdegree = (camera_degree - display_degree + 360) % 360;
    	return difdegree!=0 && difdegree!=180;
    }

	
	public static Bitmap getBitmap(Bitmap bmp,int width,int height){
		if(bmp==null){
			//
		}
		else if(bmp.getWidth()!=width || bmp.getHeight()!=height){
			synchronized (bmp) {
				bmp.recycle();
				bmp = null;
			}
			System.gc();
		}
		
		if(bmp==null){
			try {
				bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			} catch (Exception e) {
				e.printStackTrace();
				System.gc();
			}
		}
		return bmp;
	}

	public static int getDisplayDegree(Context context){
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		return toDegreeFromSurfaceRotate( wm.getDefaultDisplay().getRotation() );
	}
	public static int toDegreeFromSurfaceRotate(int rotate){
		switch( rotate ){
		case Surface.ROTATION_0: return 0;
		case Surface.ROTATION_90: return 90;
		case Surface.ROTATION_180: return 180;
		case Surface.ROTATION_270: return 270;
		}
		return 0;
	}	
	
	public static float calcDegree0to360FromDisplayCoord(float x,float y,float def){
		if(x==0.0f && y==0.0f) return def;
		float radian = (float) Math.atan2(x,-y);
		if(Float.isNaN(radian)) return def;
		float degree = (float) Math.toDegrees(radian);
		if(degree<0.0f) degree += 360.0f;
		return degree;
	}
	public static float toWatchRadianFromMathRadian(float math_radian){
		return -math_radian+(float)(Math.PI/2.0f);
	}
	public static float toMathRadianFromWatchRadian(float watch_radian){
		return -watch_radian+(float)(Math.PI/2.0f);
	}

 
    public static boolean checkSigned(PackageManager pm,String packagename,String key) {
 
        //PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(packagename,
                    PackageManager.GET_SIGNATURES);

            LibUtil.Log("-> Signature ");
            
            for (int i = 0; i < packageInfo.signatures.length; i++) { 
                Signature signature = packageInfo.signatures[i];
                LibUtil.Log("Signature: " + signature.toCharsString());
 
                if (key.equals(signature.toCharsString())) {
                    LibUtil.Log("�L�[����v");
                    return true;
                } else {
                    LibUtil.Log("�L�[���s��v");
                }
            }
            return false;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
	
	
	
	public static List<String> missingPermissions(Context context, String[] expectations) {
	    return missingPermissions(context, new ArrayList<String>(Arrays.asList(expectations)));
	}
	public static List<String> missingPermissions(Context context, List<String> expectations){
	    if (context == null || expectations == null) {
	        return null;
	    }
	    try {
	        PackageManager pm = context.getPackageManager();
	        PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
	        String[] info = pi.requestedPermissions;
	        if (info != null) {
	            for (String i : info) {
	                expectations.remove(i);
	            }
	        }
	        return expectations;
	    } catch (NameNotFoundException e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	

	
	public static class FileNameAndExtention{
		public String name;
		public String ext;
		public String makeFileName(){
			if(name!=null && ext!=null){
				return name + "." + ext;
			}else if(name!=null){
				return name;
			}else if(ext!=null){
				return "." + ext;
			}else{
				return null;
			}
		}
	}
	public static FileNameAndExtention getFileNameAndExtention(String name){
		FileNameAndExtention result = new FileNameAndExtention();
		getFileNameAndExtention(name, result);
		return result;
	}
	public static void getFileNameAndExtention(String name,FileNameAndExtention out){
		int index = name.lastIndexOf('.');
		if(index!=-1){
			out.name = name.substring(0,index);
			int len = name.length();
			if(index+1 >= len) out.ext = "";
			else out.ext = name.substring(index+1,len);
		}else{
			out.name = name;
			out.ext = null;
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public static HashMap<String,?> toMapfromByteArr(byte[] barr){
		HashMap<String, ?> map = null;
		if(barr!=null){
			try {
				ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(barr));
				map = (HashMap<String,?>) is.readObject();
				is.close();
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return map;
	}
	public static byte[] toByteArrfromMap(HashMap<String,?> map){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream os = new ObjectOutputStream(bos);
			os.writeObject(map);
			os.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return bos.toByteArray();
	}

//	public static Date getDate(int year,int month,int day){
//		Calendar cal1 = Calendar.getInstance();
//		cal1.clear();
//		cal1.set(year,month,day);
//		return cal1.getTime();
//	}	
//	public static Date getDate(int year,int month,int day,int hour){
//		Calendar cal1 = Calendar.getInstance();
//		cal1.clear();
//		cal1.set(year,month,day,hour,0);
//		return cal1.getTime();
//	}		

	
//	//����̍������邩�@�O�O�F�O�O�ɐ؎̂Ă��ĂȂ��̂ŁA��ɂ����Ƃ̏ꍇ�͐؂�̂Ă��l��n���Ă�������.
//	public static int difDay(long tar,long cur){
//		return (int)( (tar - cur) / (1000*60*60*24) );
//	}
	
	public static Date getDateByClearOtherHourAndSetHour(Date date,int hour){
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.clear(); cal2.clear();
		cal1.setTime(date);
		cal2.set( cal1.get(Calendar.YEAR), cal1.get(Calendar.MONTH), cal1.get(Calendar.DAY_OF_MONTH), hour, 0);
		return cal2.getTime();
	}
	
//	//���Ԃ�艺��؂�̂�
//	public static Date getDateByClearOtherHour(Date date){
//		Calendar cal1 = Calendar.getInstance();
//		Calendar cal2 = Calendar.getInstance();
//		cal1.clear(); cal2.clear();
//		cal1.setTime(date);
//		cal2.set( cal1.get(Calendar.YEAR), cal1.get(Calendar.MONTH), cal1.get(Calendar.DAY_OF_MONTH), cal1.get(Calendar.HOUR_OF_DAY), 0);
//		return cal2.getTime();
//	}
//	//��ɂ���艺������
//	public static Date getDateByClearOtherDay(Date date){
//		Calendar cal1 = Calendar.getInstance();
//		Calendar cal2 = Calendar.getInstance();
//		cal1.clear(); cal2.clear();
//		cal1.setTime(date);
//		cal2.set(cal1.get(Calendar.YEAR), cal1.get(Calendar.MONTH), cal1.get(Calendar.DAY_OF_MONTH));
//		return cal2.getTime();
//	}	

	//zip���𓀂��܂��B
	public static boolean unZip(File src,File destDir){
		if(!src.isFile()) return false;
		if(!destDir.exists() && !destDir.mkdir()) return false;
		if(!destDir.isDirectory()) return false;
		
		final int BUF_SIZE = 8192;
		byte[] buffer = new byte[BUF_SIZE];
		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream(new FileInputStream(src));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		if(zis==null) return false;
		
        while (true) {
			try {
	            ZipEntry entry = zis.getNextEntry();
	            if (entry == null) break;
	            if (entry.isDirectory()) {
	            	File file = new File(destDir,entry.getName());
	            	file.mkdir();
	            } else {
	            	File file = new File(destDir,entry.getName());
	        		FileOutputStream fos = new FileOutputStream(file);
	        		int size = -1;
	        		while((size=zis.read(buffer))!=-1){
	        			fos.write(buffer,0,size);
	        		}
	        		fos.flush();
	        		fos.close();
	            }
	            zis.closeEntry();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
		try {
			zis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	//curDir����̑��ΓI�ȃp�X�𐶐��B../�Ƃ��͖����ł��BcurDir�̒��̃p�X�Ɍ���B
	public static String getRelativePath(File curDir,File file){
		if(!curDir.isDirectory()) return null;
		try {
			String curDirPath = curDir.getCanonicalPath();
			String filePath = file.getCanonicalPath();
			
			int index = filePath.indexOf(curDirPath);
			if(index==0){
				return filePath.substring(curDirPath.length());
			}
			throw(new IllegalArgumentException("�K�w�Ɋ܂܂�Ă��Ȃ�"));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//�w�肵���f�B���N�g���̒��g��񋓁B
	//�f�B���N�g���̒��̃f�B���N�g���́A�f�B���N�g�����g���܂߂āA����ɗ񋓁B
	public static void addListTreeFilesInDirectry(List<File> list,File dir){
		if(dir.isDirectory()){
			for(File f : dir.listFiles()){
				list.add(f);
				if(f.isDirectory()){
					addListTreeFilesInDirectry(list, f);
				}
			}
		}
	}
	//dir���̂��ǉ��B�����ė񋓁B
	public static void addListTreeFiles(List<File> list,File dir){
		if(dir.exists()){
			list.add(dir);
			if(dir.isDirectory()){
				for(File f : dir.listFiles()){
					addListTreeFiles(list, f);
				}
			}
		}
	}	
	
	//�w�肳�ꂽ�t�@�C���܂��̓f�B���N�g�����폜�B�f�B���N�g���͒��g�����ׂč폜
	public static boolean deleteDirOrFile(File file){
		if(file.exists()){
			if(file.isDirectory()){
				File[] list = file.listFiles();
				for(File f: list){
					deleteDirOrFile(f);
				}
			}
			return file.delete();
		}
		return false;
	}
	
	//format��%d���܂߂āA�g����V����̧�ٖ���T��.
	public static File getNewFilePath(String format,File dir){
		if(!dir.isDirectory()) return null;
		for(int i=1;i<Integer.MAX_VALUE;i++){
			String name = String.format(format, i);
			File file = new File(dir,name);
			if(!file.exists()) return file;
		}
		return null;
	}
	//�t�@�C���̃R�s�[
	public static boolean copyFile(File src,File dest,boolean isOverride){
		// �t�@�C���̑��݂��m�F
		if(dest==null || src==null || src.exists()==false || src.isDirectory()) return false;
		if(!isOverride && dest.exists()) return false;

		FileInputStream fi = null;
		FileOutputStream fo = null;
	    FileChannel srcChannel =  null;
	    FileChannel destChannel = null;
	    try {
			fi = new FileInputStream(src);
			fo = new FileOutputStream(dest);		
		    srcChannel = fi.getChannel();
		    destChannel = fo.getChannel();	    	
	        srcChannel.transferTo(0, srcChannel.size(), destChannel);
	    } catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
	        try {
				if(srcChannel!=null) srcChannel.close();
				if(destChannel!=null) destChannel.close();
				if(fo!=null) fo.close();
				if(fi!=null) fi.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
	    return true;
	}
	public static boolean copyFileOrDir(File src,File dest,boolean isOverride){
		if(!src.exists()) return false;
		
		if(src.isDirectory()){
			if(!isOverride && dest.exists()) return false;
			if(dest.exists() && !dest.isDirectory()){
				deleteDirOrFile(dest);
			}
			if(!dest.exists() && !dest.mkdir()){
				return false;
			}
			File[] list = src.listFiles();
			for(File f: list){
				File d = new File(dest,f.getName());
				 if(!copyFileOrDir(f, d, isOverride)){
					 return false;
				 }
			}
			return true;
		}else{
			return copyFile(src, dest, isOverride);
		}
	}
	
	//w=0:col1 ~ w=1:col2
	public static int neutralColor(int col1,int col2,float w){
		w = Helper.clamp(w, 0.0f, 1.0f);
		float inv = 1.0f-w;
		float r1 = Color.red(col1),g1 = Color.green(col1),b1 = Color.blue(col1),a1 = Color.alpha(col1);
		float r2 = Color.red(col2),g2 = Color.green(col2),b2 = Color.blue(col2),a2 = Color.alpha(col2);
		return Color.argb((int)(a1*inv+a2*w),(int)(r1*inv+r2*w),(int)(g1*inv+g2*w),(int)(b1*inv+b2*w));
	}
	
	//�����_��`�𐮐���`�ցB�Z����؎̂Ăł��B
	public static Rect toRect(RectF rcf){
		return new Rect((int)rcf.left,(int)rcf.top,(int)rcf.right,(int)rcf.bottom);
	}

	//�c������ێ����A���ĂƉ��̑傫���ق���fitsize�ɔ[�܂�悤��
	public static PointF calcFitSize(PointF size,PointF fitsize){
		float scale = calcFitSizeScale(size, fitsize);
		return new PointF(size.x*scale,size.y*scale);
	}
	public static float calcFitSizeScale(PointF size,PointF fitsize){
		float scale_x = fitsize.x / size.x;
		float scale_y = fitsize.y / size.y;
		return Math.min(scale_x, scale_y);
	}
	public static float calcFitSizeScale(float size_x,float size_y,float fitsize_x,float fitsize_y){
		float scale_x = fitsize_x / size_x;
		float scale_y = fitsize_y / size_y;
		return Math.min(scale_x, scale_y);
	}	
	
	public static class UnitInteger{
		public final int integer;
		public final String unit;
		public UnitInteger(int integer,String unit) {
			this.integer = integer;
			this.unit = unit;
		}
	}
	static final Pattern pat_parseUnitInt = Pattern.compile("^([\\+\\-]?\\d+)(.*)$");
	public static UnitInteger parseUnitInt(String string){
		Matcher mat = pat_parseUnitInt.matcher(string);
		if(mat.find()){
			int i = Integer.parseInt( mat.group(1) );
			String u = mat.group(2);
			return new UnitInteger(i, u);
		}
		return null;
	}
	
	public static int clamp(int val,int min,int max){
		return Math.min( Math.max(val, min) , max);
	}
	public static float clamp(float val,float min,float max){
		return Math.min( Math.max(val, min) , max);
	}	
	
	public static boolean isInteger(String str){
		try{
			Integer.parseInt(str);
		}catch(NumberFormatException ex){
			return false;
		}
		return true;
	}
	
	public static int Floor(int val, int base){
		if(base<0) base = -base;
		if(val>=0){
			return ((val)/base) * base; 
		}else{
			return ((val-base)/base) * base;
		}
	}
	
	public static String DV(String val,String def){
		if(val!=null) return val;
		return def;
	}
	public static Integer DV(Integer val,Integer def){
		if(val!=null) return val;
		return def;
	}

	public static int GetNearPoint(PointF[] ptArray,PointF ptCenter){
		int index = -1;
		float mindist = Float.MAX_VALUE;
		int len = ptArray.length;
		for(int i=0;i<len;i++){
			float dist = PointF.length(ptArray[i].x-ptCenter.x, ptArray[i].y-ptCenter.y);
			if(dist < mindist){
				mindist = dist;
				index = i;
			}
		}
		return index;
	}
	public static float CalcDist(PointF pt1,PointF pt2){
		return PointF.length(pt1.x-pt2.x, pt1.y-pt2.y);
	}
	public static String ColorToString(int col){
		return "a:" + Color.alpha(col) + " r:" + Color.red(col) + " g:" + Color.green(col) + " b:" + Color.blue(col);
	}
	
	//#00000000�Ă��Ȋ����̕������
	public static String toColorString(int value){
		String str = String.format("#%02x%02x%02x%02x", 
				Color.alpha(value),
				Color.red(value),
				Color.green(value),
				Color.blue(value));
		return str;
	}
	
	public static int toColorInt(String str,int defcolor){
		if(str==null) return defcolor;		
		if(str.charAt(0) == '#'){
			Integer col = _toColorInt_NoSharp(str.substring(1));
			if(col!=null){
				return col;
			}
		}
		return defcolor;
	}
	public static Integer _toColorInt_NoSharp(String str){
		
		final Pattern pat_color = Pattern.compile("([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})");

		Matcher mat = pat_color.matcher(str);
		if(mat.find()){
			int col = Color.argb(
					Integer.valueOf(mat.group(1),16), 
					Integer.valueOf(mat.group(2),16), 
					Integer.valueOf(mat.group(3),16),
					Integer.valueOf(mat.group(4),16) );			
			
			return new Integer(col);
		}
		
		return null;
	}	
}
