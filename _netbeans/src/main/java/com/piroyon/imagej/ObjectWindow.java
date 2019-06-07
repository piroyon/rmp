/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.piroyon.imagej;

import ij.ImagePlus;
//import ij.*;
//import ij.process.*;
//import java.io.*;
/**
 *
 * @author hiroyo
 */
public class ObjectWindow {
    
	private final int[][] searray;
	private final int[][] imarray;
	int bgValue;
	private final int sewidth, seheight, width, height, dx, dy;
        private int fgsize;
	boolean symmetric;

	/** 
	 * Construct an ObjectWindow based on the supplied image and structuring element.
	 *
	 @param im ImagePlus containing the object image.
	 @param se Structuring element used to generate the windows.
	 @param bg Background value.
	 @param sym Determines if boundary conditions are symmetric.
	 */
	//public ObjectWindow(ImagePlus im, StructuringElement s, int bg, boolean sym) {
        public ObjectWindow(ImagePlus im, StructuringElement se, int bg, boolean sym) {
                imarray = im.getProcessor().getIntArray();
                searray = se.seimp.getProcessor().getIntArray();
		bgValue = bg;
                sewidth = se.seimp.getWidth(); 
                seheight = se.seimp.getHeight();
		width = im.getWidth(); 
                height = im.getHeight();
		dx = sewidth/2; // int division truncates
		dy = seheight/2;
		symmetric = sym;
                //int counter=0;             
                //get the size of the structuring element's foreground in pixels.
		for (int x=0; x<sewidth; x++) {
			for (int y=0; y<seheight; y++) {
				if (searray[x][y]!=bgValue) {
					fgsize++;
				}
			}
		}
	}

	/**
	 * Produce a list of pixel values which are overlapped by the structuring element when centered at a given
	 * coordinate.
	 */
	public int[] view(int x0, int y0) {
		int[] fgarray = new int[fgsize];
		int k=0;
		for (int x=-dx; x<=dx; x++) {
                    for (int y=-dx; y<=dx; y++) {
			// Check if we are in a SE foreground pixel.
			if (searray[x+dx][y+dy]!=bgValue) {
                            // Coordinates in the object image.
                            int xc = x0+x; int yc = y0+y;
                            // Check x boundary conditions.
                            if (xc<0) {
				if (!(symmetric)) {
                                    fgarray[k] = bgValue;
                                    k++;
                                    continue;
				} else {
                                    xc = width+xc;
				}
                            } else if (xc >= width ) {
				if (!(symmetric)) {
                                    fgarray[k] = bgValue;
                                    k++;
                                    continue;
                                } else {
                                    xc = xc-width;
                                }
                            }					
					// Check y boundary conditions.
					if (yc<0) {
						//System.out.print("Ymin ");
						//System.out.print(x0); System.out.print(" "); System.out.print(y0);
						//System.out.println();
						if (!(symmetric)) {
							fgarray[k]=bgValue;
							k=k+1;
							continue;
						} else {
							yc=height+yc;
						}
					} else if (yc >= height ) {
						//System.out.print("Ymax yc=");System.out.print(yc);System.out.println();
						//System.out.print(x0); System.out.print(" "); System.out.print(y0);
						//System.out.println();
						if (!(symmetric)) {
							fgarray[k]=bgValue;
							k=k+1;
							continue;
						} else {
							yc=yc-height;
						}
					}
					
					// Add this pixel to the result window.
					fgarray[k]=imarray[xc][yc];
					k=k+1;
				}
				
			}
		}
		return fgarray;	
	}
}
