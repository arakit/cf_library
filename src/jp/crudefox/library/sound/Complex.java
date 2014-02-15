package jp.crudefox.library.sound;

public class Complex {
    public double r, i;
    public Complex(double re, double im) {
        this.r = re;
        this.i = im;
    }
    public double abs() { return Math.sqrt(r*r + i*i);  }
}
