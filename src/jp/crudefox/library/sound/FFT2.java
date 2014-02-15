package jp.crudefox.library.sound;


public class FFT2 {
    
    private Complex[] x;

    private static int rev(int bit, int r) {
    	int bitr = 0;
    	for (int i = 0; i < r; i++) {
    		bitr <<= 1;
    		bitr |= (bit & 1);
    		bit >>= 1;
    	}
    	return bitr;
    }

    private void swap(int a, int b) {
    	double tmp = x[a].r;
    	x[a].r = x[b].r;
    	x[b].r = tmp;
    	tmp = x[a].i;
    	x[a].i = x[b].i;
    	x[b].i = tmp;
    }
    
    public int calcSpectol(final short[] data,final int offset,final int size){

		final int N = size;//(int)(af.getSampleRate() * Double.parseDouble(args[1]));
		final double Pi = Math.PI;
		
		int result_n = -1;
		double result_amp = 0;
		
		//final Complex[] x = new Complex[N];
		x = new Complex[N];
		for (int i = 0; i < N; i++) {
			x[i] = new Complex(data[i+offset], 0);
		}
		
		int n, k, r, i, j, j1, j2, p, k1, bit;
		double a, b;		

		r = 0;
	    	for (i = N >> 1; i > 0; i >>= 1) r++;
		n = 1 << r;
		j2 = n;
		k = 0;
		j1 = r - 1;
		for (j = 0; j < r; j++) {
		    j2 >>= 1;
		    for (;;) {
				for (i = 0; i < j2; i++) {
				    p = k >> j1;
				    k1 = k + j2;
				    a = x[k1].r * Math.cos(2.0*Pi/n*rev(p,r)) 
				      + x[k1].i * Math.sin(2.0*Pi/n*rev(p,r)); 
				    b = x[k1].i * Math.cos(2.0*Pi/n*rev(p,r)) 
				      - x[k1].r * Math.sin(2.0*Pi/n*rev(p,r)); 
				    x[k1].r = x[k].r - a;	// バタフライ演算処理
				    x[k1].i = x[k].i - b;
				    x[k].r  = x[k].r + a;
				    x[k].i  = x[k].i + b;
				    k++;
				}
				k += j2;
				if (k >= n) break;
		    }
		    k = 0;
		    j1--;
		}
		for (k = 0; k < n; k++) {
		    bit = rev(k, r);
		    if (bit > k) swap(k, bit);
		}

		int thres_amp = 10000 * 10 * 5;
		
	    for (n = 0; n < N/2; n++) {
	        double amp = x[n].abs(); // 振幅スペクトル
		    //if (/*n < 2 ||*/ amp > 10000.0 * 10) {
		    	if( amp > thres_amp && amp > result_amp ){
		    		result_amp = amp;
		    		result_n = n;
		    	}
//		    	String str;
//		    	str = String.format("%12.3f　・・・　", amp);
//		    	if (n == 0) str+=String.format("直流成分\n");
//		    	else if (n == 1) str+=String.format("基本波\n");
//		    	else str+=String.format("%d次高調波\n", n);
//		    	AppUtility.Log(str);
		    //}
	    }
    	
	    return result_n;
    }
    
    public Complex[] getComplexs(){
    	return x;
    }
    

    public static final String STRING_ONKAI[] = new String[]{
    	"ド", "ド#", "レ", "レ#", "ミ", "ファ", "ファ#", "ソ", "ソ#", "ラ", "ラ#", "シ"
    };
    
    public static double log(double val,double tei){
    	return Math.log(val)/Math.log(tei);
    }

    public static String getOnkai(int n){
    	int index = (int)( Math.floor(  (12.0*log(n/440.0, 2.0)) + 0.5  ) ) + 9;
    	int oct = (int) Math.floor( index / 12.0 );
    	int onk = (index>=0) ? (index % 12) : (index % 12 + 11);
    	//if(onk<0) onk += 12;
    	return STRING_ONKAI[onk]+(oct+4);
    }
    
    public static class OnkaiInfo{
    	public int scale;
    	public int oct;
    }
    public static void getOnkai(int n,OnkaiInfo out){
    	if(n<1) n=1;
    	int index;
    	index = (int)( Math.floor(  (12.0*log(n/440.0, 2.0)) + 0.5  ) ) + 9;
    	int oct = (int) Math.floor( index / 12.0 );
    	int onk = (index>=0) ? (index % 12) : (index % 12 + 11);
    	//if(onk<0) onk += 12;
    	out.oct = oct+4;
    	out.scale = onk;
    }
    
    public static class FloatOnkaiInfo{
    	public float scale;
    	public int oct;
    }
    public static void getFloatOnkai(int n,FloatOnkaiInfo out){
    	if(n<1) n=1;
    	float index = (float)( (12.0*log(n/440.0, 2.0)) + 9.0 );
    	int oct = (int) Math.floor( index / 12.0 );
    	float onk = (index>=0) ? (index % 12) : (index % 12 + 11);
    	//if(onk<0) onk += 12;
    	out.oct = oct+4;
    	out.scale = onk;
    }
    
    
    public static void testSin(short[] data,int size,double sec,double frequency,double A){
    	for(int i=0;i<size;i++){
    		double t = i*sec/(double)size;
    		data[i] = (short)( Math.sin(2*Math.PI*t*frequency) * A );
    	}
    }
}
