package com.piroyon.imagej;

import ij.*;
import ij.process.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import org.ajdecon.morphology.StructuringElement;

/**
 * Morphology. Utility class which performs image processing operations from mathematical morphology
 * based on StructElement objects which define the "shape" of the operation. Images are
 * passed as ImageJ ImagePlus objects.
 *
 * For more information on mathematical morphology: http://en.wikipedia.org/wiki/Mathematical_morphology
 * For ImageJ: http://rsbweb.nih.gov/ij/ 
 *
 @author Adam DeConinck
 @version 0.1
 *
 */
public class Morphology2 {
        private final int[][] searray;
	//private final int[] searray2;
	private final int tgValue, sewidth, seheight, width, height, dx, dy, choice;
        private final ImagePlus targetImp;
        private int fgsize;
	//boolean symmetric;
        
        
       public Morphology2(ImagePlus im, StructuringElement se, int tg, int choi) {
                //imarray = im.getProcessor().getIntArray();
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
		//symmetric = sym;
                //int counter=0;             
                //get the size of the structuring element's foreground in pixels.

	}

	/**
	 * Percentile filter. Sets each pixel equal to the value of the pth
	 * percentile of its neighborhood.  Neighborhood is defined using a 
	 * structuring element s.
	 * <p>
	 * This is the core functionality of most of the morphological 
	 * operations, which at their core are rank-order filters.
	 *
	 @param imp   An ImagePlus object containing the image to be operated on.
	 *
	 @param se    StructuringElement containing the shape of the neighborhood.
	 *
	 @param perc Percentile to be used.
	 *
	 @param symmetric Determines whether the boundary conditions are symmetric, or padded with background.
	 *
	 @return An ImagePlus containing the filtered image.
	 */
        public ImagePlus doFilter(ImagePlus result, int mode, int bg, int[] b) {
                ImagePlus out = new ImagePlus("filter output", result.getProcessor().createImage() );
		ImageProcessor op = out.getProcessor();
                int [][] imarray = result.getProcessor().getIntArray();
                for(int x=0; x<width; x++) {
                    for (int y=0; y<height; y++) {
                        if ( ((x-dx<0 || x+dx>=width)) || (y-dy<0 || y+dy>=height)) { 
                            op.set(x,y, 255);
                            continue; 
                        }
                        int[] c = new int[sewidth*seheight];
                        for (int i = 0, j=x-dx; i < sewidth; i++, j++) {
                            System.arraycopy((Arrays.copyOfRange(imarray[j], y-dy, y+dy+1)), 0, c, i*seheight, seheight);                           
                        }
                        Arrays.parallelSetAll(c, i -> { return (int)Math.ceil((c[i] / 255)); });
                        //IJ.log(Arrays.toString(c));
                        int k = 0, color = 0;
                        switch (choice) {
                            case(1): //square
                            case(2): //rect
                                switch(bg) {
                                    case(0): //black                                       
                                        switch(mode) {                                            
                                            case(0): //erode                                               
                                            default:
                                                //IJ.log(Arrays.toString(c));
                                                if (Arrays.asList(c).contains(0)) color = 255;
                                                break;
                                            case(1): //dilate
                                                //IJ.log(Arrays.toString(c));
                                                if (Arrays.asList(c).contains(1)) color = 255;
                                                break;
                                        }
                                    break;
                                    case(255): //white
                                    default:
                                        switch(mode) {
                                            case(0): //erode
                                            default:
                                                if (Arrays.asList(c).contains(1)) color = 255;
                                                break;
                                            case(1): //dilate
                                                if (Arrays.asList(c).contains(0)) color = 255;
                                                break;
                                        }                                       
                                    break;
                                }
                            break;
                            case(3):  //oval
                            case(0):  //file
                            default:
                                switch (bg) {
                                    case(0): //black
                                        switch(mode) {
                                            case 0: //erode
                                            default:
                                                for(int bb : b) {
                                                    //d[k] = bb ^ c[k++];
                                                    if ( bb == 0 && ((bb ^ c[k++]) == 0)) {
                                                        color = 255;
                                                        IJ.log(Integer.toString(tgValue));
                                                        break;
                                                    }
                                                }
                                                break;
                                            case 1: //dilate
                                                for(int bb : b) {
                                                    //d[k] = bb ^ c[k++];
                                                    if ( bb == 0 && ((bb ^ c[k++]) == 1)) {
                                                        color = 255;
                                                        //IJ.log(Integer.toString(tgValue));
                                                        break;
                                                    }
                                                }
                                                break;
                                        }
                                        break;
                                    case(255): //white
                                    default:
                                        switch(mode) {                                        
                                            case 0: //erode
                                                for(int bb : b) {
                                                    if (bb == 0 && ((bb & c[k++]) == 0)) {
                                                        color = 255;
                                                        break;
                                                    }
                                                }
                                                break;
                                            case 1:  //dilate
                                                for(int bb : b) {
                                                    if (bb == 0 && ((bb & c[k++]) == 1)) {
                                                        color = 255;
                                                        break;
                                                    }
                                                }
                                                break;
                                        }
                                        break;
                                }
                                break;
                        }
                        
                        //int color = (Arrays.deepEquals(a, searray)) ? 0 : 255;
                        String xx = Integer.toString(x);
                        String yy = Integer.toString(y);
                        String cc = Integer.toString(color);
                        //IJ.log(Arrays.toString(b));
                        //IJ.log(Arrays.toString(c));
                        //IJ.log(Arrays.toString(d));
			op.set(x,y, color);
//			System.out.print(x);System.out.print(" ");System.out.print(y);System.out.println();
                    }
		
                }
		return out;
        }
        //public ImagePlus doRotate() {
            
        //}
	public ImagePlus doRmp() {
                //ImagePlus result = new ImagePlus("filter output", targetImp.getProcessor().createImage() );
		//ImagePlus in = new ImagePlus("percentile input", imp.getProcessor().convertToByte(true) );
		//ImagePlus out = new ImagePlus("percentile output", targetImp.getProcessor().createImage() );
		//ImageProcessor op = out.getProcessor();             
                int[] b = new int[sewidth*seheight];
                for(int i = 0; sewidth>i; i++) {
                    System.arraycopy(Arrays.copyOfRange(searray[i], 0, seheight),0,b,i*seheight,seheight);
                }
                Arrays.parallelSetAll(b, i -> { return (b[i] / 255); });
                //ImagePlus opend = doFilter((doFilter(targetImp, 0, tgValue, b)), 1, 255, b);
                ImagePlus opend = doFilter(targetImp, 0, tgValue, b);
                return opend;
        }


        public int[] makeSubarray(int x, int y, int[][] imarray) {
            int[] c = new int[sewidth*seheight];
            for (int i = 0, j=x-dx; i < sewidth; i++, j++) {
                System.arraycopy((Arrays.copyOfRange(imarray[j], y-dy, y+dy+1)), 0, c, i*seheight, seheight);                           
            }
            //IJ.log(Arrays.toString(c));
            Arrays.parallelSetAll(c, i -> { return (int)Math.ceil((c[i] / 255)); });
            return c;
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


