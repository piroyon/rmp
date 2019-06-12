package com.piroyon.imagej;

import ij.*;
import ij.process.*;
import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;

//import org.ajdecon.morphology.StructuringElement;

/**
 * Morphology. Utility class which performs image processing operations from mathematical morphology
 * based on StructElement objects which define the "shape" of the operation. Images are
 * passed as ImageJ ImagePlus objects.
 *
 * For more information on mathematical morphology: http://en.wikipedia.org/wiki/Mathematical_morphology
 * For ImageJ: http://rsbweb.nih.gov/ij/ 
 *
 @author piroyon
 @version 0.1
 *
 */
public class Morphology2 {
        private final int[][] searray;
	private final int sewidth, seheight, width, height, dx, dy, tgValue, choice;
        private final ImagePlus targetImp;
        //private int fgsize;
        
        
       public Morphology2(ImagePlus im, StructuringElement se, int tg, int choi) {
                searray = se.seimp.getProcessor().getIntArray();
		tgValue = tg;
                targetImp = im;
                sewidth = se.seimp.getWidth();
                seheight = se.seimp.getHeight();
                choice = choi;
		width = im.getWidth(); 
                height = im.getHeight();
		dx = sewidth/2; // int division truncates
		dy = seheight/2;
	}

	/**
	 * doFilter. 
	 @param imp   An ImagePlus object containing the image to be operated on
	 @param mode  f:erosion, t:dilation
	 @param tg    imp's back ground color (0 or 255)
	 @param b     1-dim SE array
	 @return An ImagePlus containing the filtered image.
	 **/
        public ImagePlus doFilter(ImagePlus imp, boolean mode, int tg, int[] b) {
                ImagePlus out = new ImagePlus("filter output", imp.getProcessor().createImage() );
		//ByteProcessor op = out.getProcessor().convertToByteProcessor();
                ImageProcessor op = out.getProcessor();
                int [][] imarray = imp.getProcessor().getIntArray();
                for(int x=0; x<width; x++) {
                    for (int y=0; y<height; y++) {
                        if ( ((x-dx<0 || x+dx>=width)) || (y-dy<0 || y+dy>=height)) { 
                            op.set(x,y, 255);
                            continue; 
                        }
                        //int[][] a = new int[sewidth][seheight];
                        int[] c = new int[sewidth*seheight];
                        for (int i = 0, j=x-dx; i < sewidth; i++, j++) {
                            //a[i] = Arrays.copyOfRange(imarray[j], y-dy, y+dy+1);
                            System.arraycopy((Arrays.copyOfRange(imarray[j], y-dy, y+dy+1)), 0, c, i*seheight, seheight);                           
                        }
                        Arrays.parallelSetAll(c, i -> { return (int)Math.ceil((c[i] / 255)); });                        
                        int k = 0, color = mode ? 255 : 0;
                        switch (choice) {
                            case 1: //square
                            case 2: //rect
                                switch(tg) {
                                    case 0: //white object on black
                                        if (mode) { //dilate
                                            //IJ.log(Arrays.toString(c));
                                            if (ArrayUtils.contains(c, 1)) color = 0; //exist objcolor cell?
                                        } else { //erode
                                            IJ.log(Arrays.toString(c));
                                            if (ArrayUtils.contains(c, 0)) color = 255; //exist bgcolor cell? 
                                        }
                                    break;
                                    case 255: //black obj on white
                                    default:
                                        if (mode) { //dilate
                                            if (ArrayUtils.contains(c, 0)) color = 0; //exist objcolor cell?
                                        } else { //erode
                                            if (ArrayUtils.contains(c, 1)) color = 255; //exist bgcolor cell?
                                        }                                       
                                    break;
                                }
                            break;
                            case 3: //oval
                            case 0: //fromfile
                            default:
                                switch (tg) {
                                    case 0: //white object on black
                                        if (mode) { //dilate
                                            for(int bb : b) {
                                                if ( bb == 0 && c[k++] == 1) {
                                                    color = 0;
                                                    break;
                                                }
                                            }
                                        } else { //erode
                                            for(int bb : b) {
                                                if ( bb == 0 && c[k++]== 0) {
                                                    color = 255;
                                                    break;
                                                }
                                            }
                                        }
                                        break;
                                    case 255: //black obj on white
                                    default:
                                        if (mode) {   //dilate       
                                            //IJ.log(Arrays.toString(c));
                                            for(int bb : b) {
                                                if (bb == 0 && c[k++] == 0) {
                                                    color = 0;
                                                    break;
                                                } //IJ.log(Integer.toString(color));
                                            }
                                        } else {  //erode
                                            for(int bb : b) {
                                                if (bb == 0 && c[k++] == 1) {
                                                    color = 255;
                                                    break;
                                                }
                                            }
                                        }
                                    break;
                                }
                            break;
                        }
			op.set(x,y, color);
                    }		
                }
		return out;
        }
        public ImagePlus doRotate() {
            
        }
        
	public ImagePlus doRmp() {
                int[] b = new int[sewidth*seheight];
                for(int i = 0; sewidth>i; i++) {
                    System.arraycopy(Arrays.copyOfRange(searray[i], 0, seheight),0,b,i*seheight,seheight);
                }
                Arrays.parallelSetAll(b, i -> { return (b[i] / 255); });
                return doFilter((doFilter(targetImp, false, tgValue, b)), true, 255, b);
                //return doFilter(targetImp, false, tgValue, b);
                //return doFilter(targetImp, true, tgValue, b);
                //return opend;
        }

	/**
	 * Assume symmetric bc if not supplied.
	 *
	 */
	/*public static ImagePlus percentileFilter(ImagePlus imp, StructuringElement se,
			double perc) {
		return percentileFilter(imp, se, perc, true);
	}

	/**
	 * Morphological erosion.  Compares each pixel to its neighborhood as 
	 * defined by a structuring element, s.  That pixel is then set to be 
	 * equal to the value found in its neighborhood which is closest to the
	 * background value.
	 * <p>
	 * If the background is white (255) then this corresponds to a local
	 * maximum filter.
	 * <p>
	 * If the background is black (0) then this corresponds to a local 
	 * minimum filter.
	 * <p>
	 * The structuring element defines the color of the background.
	 *
	 @param im An ImagePlus object containing the image to be operated on.
	 *
	 @param s Structuring Element defining the neighborhood.
	 *
	 @return An ImagePlus containing the filtered image.
	 *
	 */
	/*public static ImagePlus erode(ImagePlus imp, StructuringElement se) {
		if (se.isBgWhite()) {
			return percentileFilter(imp, se, 100.0);
		} else {
			return percentileFilter(imp, se, 0.0);
		}
	}

	/**
	 * Morphological dilation.  Compares each pixel to its neighborhood as 
	 * defined by a structuring element, s.  That pixel is then set to be 
	 * equal to the value found in its neighborhood which is closest to the
	 * foreground value.
	 * <p>
	 * Note that this is the inverse operation to erosion.
	 * <p>
	 * If the background is white (255) then this corresponds to a local
	 * minimum filter.
	 * <p>
	 * If the background is black (0) then this corresponds to a local 
	 * maximum filter.
	 * <p>
	 * The structuring element defines the color of the background.
	 @param im An ImagePlus object containing the image to be operated on.
	 *
	 @param s Structuring Element defining the neighborhood.
	 *
	 @return An ImagePlus containing the filtered image.
	 *
	 */
	/*public static ImagePlus dilate(ImagePlus imp, StructuringElement se) {
		if (se.isBgWhite()) {
			return percentileFilter(imp, se, 0.0);
		} else {
			return percentileFilter(imp, se, 100.0);
		}
	}

	/**
	 * Morphological opening.  Corresponds to an erosion followed
	 * by a dilation.
	 *
	 */
	/*public static ImagePlus open(ImagePlus imp, StructuringElement se) {
		//return dilate( erode( imp, se ), se);
                return doFilter();
	}

	/**
	 * Morphological closing. Corresponds to a dilation followed by 
	 * an erosion.
	 *
	 */
	/*public static ImagePlus close(ImagePlus imp, StructuringElement se) {
		return erode( dilate( imp, se), se );
	}

	/** 
	 * Morphological gradient.  Difference between the dilation and the erosion.
	 *
	 */
	/*public static ImagePlus gradient(ImagePlus imp, StructuringElement se) {
		ImagePlus d = dilate(imp,se);
		ImagePlus e = erode(imp,se);

		int width=imp.getWidth(); int height=imp.getHeight();
		ImageProcessor dp = d.getProcessor();
		ImageProcessor ep = e.getProcessor();

		for (int x=0;x<width;x++) {
			for (int y=0; y<height; y++) {
				dp.set(x,y, (int)dp.getPixel(x,y) - (int)ep.getPixel(x,y) );
			}
		}
		return d;
	}

	/**
	 * Morphological hit-or-miss transform. Takes one image and two structuring
	 * elements. Result is the set of positions, where the first structuring element fits
	 * in the foreground of the input, and the second misses it completely.
	 
	 @param imp ImagePlus object containing the image to be operated on.
	 @param fse Structuring element for the foreground.
	 @param bse Structuring element for the background.
	 @return ImagePlus containing the filtered image.
	 *
	 */
	/*public static ImagePlus hitOrMiss(ImagePlus imp, 
			StructuringElement fse, StructuringElement bse) {

		/* Note that both fg and bg are returned from erode, so are 
		 * guaranteed to be 8-bit images with ByteProcessors. */
	/*	ImagePlus fg = erode(imp,fse);
		imp.getProcessor().invert();
		ImagePlus bg = erode( new ImagePlus( " ", imp.getProcessor() ), bse);

		int width=fg.getWidth();
		int height=fg.getHeight();
		ImageProcessor ip = fg.getProcessor();

		for (int x=0; x<width; x++) {
			for (int y=0; y<width; y++) {
				ip.set( x, y, ip.getPixel(x,y) & bg.getProcessor().getPixel(x,y) );
			}
		}
		return fg;
	}

	/**
	 * Morphological top-hat transform. Can be "white" or "black" top-hat.
	 * White corresponds to difference between image and its opening.
	 * Black corresponds to difference between closing and image.
	 *
	 */
	/*public static ImagePlus topHat(ImagePlus imp, StructuringElement se, boolean white) {

		int width = imp.getWidth();
		int height = imp.getHeight();

		ImagePlus result = new ImagePlus("top hat", imp.getProcessor());
		ImageConverter ic = new ImageConverter(result);
		ic.convertToGray8();
		ImageProcessor rip = result.getProcessor();

		if (white) {
			ImageProcessor o = open(imp, se).getProcessor();
			
			for (int x=0; x<width; x++) {
				for (int y=0; y<height; y++) {
					rip.set(x,y, (rip.getPixel(x,y) - o.getPixel(x,y)));
				}
			}
		} else {
			ImageProcessor c = close(imp, se).getProcessor();

			for (int x=0; x< width; x++) {
				for (int y=0; y<height; y++) {
					rip.set(x,y, (c.getPixel(x,y) - rip.getPixel(x,y)));
				}
			}
		}
		return result;
	}

	/*
	 * Calculates the value of the perc-th percentile of values[],
	 * using the calculation technique from the Engineering
	 * Statistics Handbook published by NIST, section 7.2.5.2.
	 *
	 */
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


