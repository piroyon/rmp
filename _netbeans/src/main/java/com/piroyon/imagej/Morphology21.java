package com.piroyon.imagej;

import ij.ImagePlus;
import ij.ImageStack;
import ij.IJ;
import ij.process.ImageProcessor;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Morphology. Utility class which performs image processing operations from mathematical morphology
 * based on StructElement objects which define the "shape" of the operation. Images are
 * passed as ImageJ ImagePlus objects.
 *
 @author piroyon
 @version
 *
 */
public class Morphology21 {
        private final int[][] searray;
	private final int sewidth, seheight, width, height, choice, rotate;
        private final ImagePlus targetImp;
        //private int fgsize;
        
        
       public Morphology21(ImagePlus im, StructuringElement se, int choi, int rt) {
                searray = se.seimp.getProcessor().getIntArray();
		//tgValue = tg;
                targetImp = im;
                rotate = rt;
                sewidth = se.seimp.getWidth();
                seheight = se.seimp.getHeight();
                choice = choi;
		width = im.getWidth(); 
                height = im.getHeight();
	}

	/**
	 * doFilter. 
	   @param outp  ByteProcessor to be operated on
           @param tg    outp's back ground color (f:0, t:255)
	   @param mode  f:erosion, t:dilation	 
	   @param b     1-dim SE array
	   @return      filterd ByteProcessor
	 **/
        public ByteProcessor doFilter(ByteProcessor outp, boolean tg, boolean mode, int[] b) {
                int [][] imarray = outp.getIntArray();
                int dx = sewidth/2;
		int dy = seheight/2;
                for(int x=0; x<width; x++) {
                    for (int y=0; y<height; y++) {
                        if ( ((x-dx<0 || x+dx>=width)) || (y-dy<0 || y+dy>=height)) { 
                            outp.set(x,y, 0);
                            continue; 
                        }
                        //int[][] a = new int[sewidth][seheight]; //use deep equal?
                        int[] c = new int[sewidth*seheight];
                        for (int i = 0, j=x-dx; i < sewidth; i++, j++) {
                            //a[i] = Arrays.copyOfRange(imarray[j], y-dy, y+dy+1);
                            System.arraycopy((Arrays.copyOfRange(imarray[j], y-dy, y+dy+1)), 0, c, i*seheight, seheight);                           
                        }
                        Arrays.parallelSetAll(c, i -> { return (int)Math.ceil((c[i] / 255)); });                        
                        int k = 0, color = mode ? 0 : 255;
                        switch (choice) {
                            case 1: //square
                            case 2: //rect
                                if (!tg) { //white object on black
                                    if (mode) { //dilate
                                        //IJ.log(Arrays.toString(c));
                                        if (ArrayUtils.contains(c, 1)) color = 255; //exist objcolor cell?
                                    } else { //erode
                                        //IJ.log(Arrays.toString(c));
                                        if (ArrayUtils.contains(c, 0)) color = 0; //exist bgcolor cell? 
                                    }
                                } else { //black obj on white
                                    if (mode) { //dilate
                                        if (ArrayUtils.contains(c, 0)) color = 255; //exist objcolor cell?
                                    } else { //erode
                                        if (ArrayUtils.contains(c, 1)) color = 0; //exist bgcolor cell?
                                    }                                       
                                }
                            break;
                            case 3: //oval
                            case 0: //fromfile
                            default:
                                if (!tg) { //white object on black
                                    if (mode) { //dilate
                                        for(int bb : b) {
                                            if ( bb == 0 && c[k++] == 1) {
                                                color = 255;
                                                break;
                                            }
                                        }
                                    } else { //erode
                                        for(int bb : b) {
                                            if ( bb == 0 && c[k++]== 0) {
                                                color = 0;
                                                break;
                                            }
                                        }
                                    }
                                } else { //black obj on white
                                    if (mode) {   //dilate       
                                        //IJ.log(Arrays.toString(c));
                                        for(int bb : b) {
                                            if (bb == 0 && c[k++] == 0) {
                                                color = 255;
                                                break;
                                            } //IJ.log(Integer.toString(color));
                                        }
                                    } else {  //erode
                                        for(int bb : b) {
                                            if (bb == 0 && c[k++] == 1) {
                                                color = 0;
                                                break;
                                            }
                                        }
                                    }
                                }
                            break;
                        }
			outp.set(x,y, color);
                    }		
                }
		return outp;
        }
        
        public ImageProcessor getMaxValue(ImageStack rstack) {
                int[][] max = new int[width][height];
                int k = 0;
                for(int j=1; j<=rotate; j++) {
                    ImageProcessor mimp = rstack.getProcessor(j);
                    for(int x=0; x<width; x++) {
                        for (int y=0; y<height; y++) {
                             int val = mimp.get(x,y);
                             if( j==1 || max[x][y]<val) {
                                 max[x][y]=val;
                             }
                        }
                        k++;
                    }
                }
                ImageProcessor ipMax = new FloatProcessor(max);
                return ipMax;   
        }
        /**
	 * doRmp. 
           @param tg      target image's back ground color (f:0, t:255)
	   @return        an ImageStack.
	 **/
	public ImageStack doRmp(boolean tg) {
                int[] b = new int[sewidth*seheight];
                double angle = 180 / rotate;
                for(int i = 0; sewidth>i; i++) {
                    System.arraycopy(Arrays.copyOfRange(searray[i], 0, seheight),0,b,i*seheight,seheight);
                }
                Arrays.parallelSetAll(b, i -> { return (b[i] / 255); });
                ImagePlus out = new ImagePlus("tmpImage", targetImp.getProcessor().createImage() );
                ImageProcessor op = out.getProcessor();
                ImageStack rstack = new ImageStack(width, height);
                
                for (int i=0; i<rotate; i++) {
                    ByteProcessor tmpop = op.duplicate().convertToByteProcessor();
                    tmpop.setBackgroundValue((tg ? 0 : 255));
                    tmpop.rotate(i*angle);
                    IJ.showProgress(i+1,rotate+1);
                    tmpop = (doFilter((doFilter(tmpop, tg, false, b)), false, true, b)); //erode->dilate
                    tmpop.setBackgroundValue(0);  //filterd image's bg=0
                    tmpop.rotate(-1*i*angle);
                    rstack.addSlice(tmpop);
                }
                out = null;  //need?
                return rstack;
        }

	private static double percentile(int[] values, double perc) {
		Arrays.sort(values);

		// n is the rank of the percentile value in the sorted array.
		double n = (perc/100.0)*(values.length - 1) + 1.0;

		if (Math.round(n)==1) {
			return values[0];
		} else if (Math.round(n)==values.length) {
			return values[values.length-1];
		} else {
			int k = (int)n;
			double d = n-k;
			return values[k-1] + d*(values[k] - values[k-1]);
		}
	}
}


