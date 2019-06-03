package com.hiroyo.imagej;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.*;
import ij.io.OpenDialog;
import ij.process.*;
import ij.measure.*;

/**	This plugin performs erosion, dilation, opening and closing of binary images.
	The plugin requires 8-bits binary images (Process/Binary/treshold)
	The number of neighborhood  pixels (between 1 and 8) and itearatiosn (1-25) can be choosen at will.
	The plugin corresponds to the Binary/Set Count option in NIH and Scion Image.

	Erode - Removes pixels from the edges of objects in binary images, where contiguous black areas
			in the image are considered objects, and background is assumed to be white.
			A pixel is removed (set to white) if entered value or more of its eight neighbors are white.
			Erosion separates objects that are touching and removes isolated pixels.

	Dilate- Adds pixels to the edges of objects in binary images. A pixel is added (set to black)
			if entered value or more of its eight neighbors are black.
			Dilation connects discontinuous objects and fills in holes.

	Open - 	Performs an erosion operation, followed by dilation, which smoothes objects and remove isolated pixels.

	Close - Performs a dilation operation, followed by erosion, which smoothes objects and fill in small holes.

	Set Count - Allows you to specify the number of adjacent background or foreground pixels necessary
			before a pixel is removed from or added to the edge of objects during erosion or dilation operations.
			The default is four.

	Set Iterations - Allows you to specify the number of times erosion, dilation, opening, and closing are performed.
			The default is one.

	The plugin also writes results like number of pixels in the original image, new filtered image and residual image.
	If required, the residual image containing the removed (erosion) or added (dilation) pixels is displayed.

	Gary Chinga 	020726

*/


public class Rmp_prototype implements PlugInFilter {

	ImagePlus imp, impRemaining, impRemoved;
	ImageStack imsRemoved;
	int pixelCount,pixelThreshold,i,j,w,h;
	int nIterations;
	boolean canceled=false,dilate=false,display=false, displayRem=false,erode = false, open=false,close=false;
	ResultsTable rt = new ResultsTable();
	int index,sum;
	int p1,p2,p3,p4,p5,p6,p7,p8,p9;
	int nRemoved,rPixels;
	int blackPixels, imageSize;

	private static final String[] items = {"Erode","Dilate","Open","Close"};
	protected static final int ERODE=0,DILATE=1,OPEN=2,CLOSE=3;
	protected static int Choice;

	byte[] remain,remove,pixels,origPixels;
	ByteProcessor ipRemoved;

        @Override
	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about"))
			{showAbout(); return DONE;}
		this.imp = imp;
		return DOES_8G;
	}

        @Override
	public void run(ImageProcessor ip) {

		// Get details
		getDetails();
		if(canceled) return;

		// Get information of Stack
		int nSlices = imp.getStackSize();
		ImageStack stack = imp.getStack();
		w = stack.getWidth();
		h = stack.getHeight();
		imageSize=w*h;

		// Create new stack for output
		ImageStack imsRemained = new ImageStack(w,h);
		if (displayRem){imsRemoved = new ImageStack(w,h);}

		// Go through the slices of input stack
		for(i=0;i<nSlices;i++){
			blackPixels=0;nRemoved=0;rPixels=0;
			IJ.showProgress((double)i/nSlices);
			IJ.showStatus("a:"+i+"/"+nSlices);

			// Get a slice and copy the pixel arrays
			ImageProcessor ipSlice = stack.getProcessor(i+1);
			origPixels = (byte[])ipSlice.getPixelsCopy();
			pixels = (byte[])ipSlice.getPixelsCopy();


			// Make places for results
			ByteProcessor ipRemain = new ByteProcessor(w,h);
			remain = (byte[])ipRemain.getPixels();

                    switch (Choice) {
                        case ERODE:
                            doFilter();
                            break;
                        case DILATE:
                            invertForDilation();
                            doFilter();
                            invertFromDilation();
                            break;
                        case OPEN:
                            doFilter();
                            for (int y=1; y<(h-1); y++) {
                                for (int x=1; x<(w-1); x++) {
                                    index=x+y*w;
                                    pixels[index]=remain[index];
                                }
                            }
                            invertForDilation();
                            doFilter();
                            invertFromDilation();
                            break;
                        case CLOSE:
                            invertForDilation();
                            doFilter();
                            invertFromDilation();
                            for (int y=1; y<(h-1); y++) {
                                for (int x=1; x<(w-1); x++) {
                                    index=x+y*w;
                                    pixels[index]=remain[index];
                                }
                            }
                            doFilter();
                            break;
                        default:
                            break;
                    }

			// Put results in destination stack
			ipRemain.setMinAndMax(0,0);
			imsRemained.addSlice("Eroded image",ipRemain);


			if (displayRem){
				ipRemoved = new ByteProcessor(w,h);
				remove = (byte[])ipRemoved.getPixels();

				for (int y=1; y<(h-1); y++) {
					for (int x=1; x<(w-1); x++) {
						index=x+y*w;
						remove[index]=(byte)(origPixels[index]-remain[index]);
						if (remain[index]==0) rPixels++;
						if (origPixels[index]==0) blackPixels++;
						//Correct pixel values when eroding and dilating
						if (remove[index]==0) remove[index]= (byte)255;
						else {remove[index]=0;nRemoved++;}

					}
				}
				ipRemoved.setMinAndMax(0,0);
				imsRemoved.addSlice("Erosion image",ipRemoved);
			}
			//if dilate, the pixels counted are background white pixels. We need to subtract the whites from image
			if (dilate) {blackPixels=((w-2)*(h-2))-blackPixels;}
			if (display) writeResults();

		}

		// Create new images using new stacks
		IJ.showProgress(1.0);

		impRemaining = new ImagePlus("Filtered image",imsRemained);
		if (displayRem) impRemoved = new ImagePlus("Residual image",imsRemoved);
		impRemaining.setStack(null,imsRemained);
		if (displayRem) impRemoved.setStack(null,imsRemoved);

		impRemaining.show();
		if (displayRem) impRemoved.show();
	}

	void showAbout() {
		IJ.showMessage("About binary filter...",
			"Blah\n" );
	}

	void getDetails(){
		GenericDialog gd = new GenericDialog("Binary filters..");
        gd.addNumericField("Set count (1-8):",4,0);
		gd.addNumericField("Iterations (1-25): ",1,0);
        		gd.addChoice("Do", items, items[Choice]);
		gd.addCheckbox("Display residual image ", displayRem);
		gd.addCheckbox("Display Results ", display);

        gd.showDialog();
		if(gd.wasCanceled()){
        		canceled = true;
         		return;
        }

		//Correct the number of required neighborhood black pixels
        pixelCount = (int)gd.getNextNumber();
		pixelThreshold = (2040-255*pixelCount);
		pixelThreshold = (255*pixelCount);
		nIterations = (int)gd.getNextNumber();

		if(pixelCount>8 || pixelCount <1){
			canceled = true;
			IJ.showMessage("Values for neighborhood pixel count: 1-8");
			return;
		}
		if(nIterations>25 || nIterations<1){
			canceled = true;
			IJ.showMessage("Values for number iterations: 1-25");
			return;
		}
 		Choice = gd.getNextChoiceIndex();
	    	displayRem=gd.getNextBoolean();
		display = gd.getNextBoolean();
    	}

	void writeResults(){
		//blackPixels=blackPixels/imageSize;
		int fArea;
		rt.incrementCounter();
		fArea=blackPixels-nRemoved;
		rt.addValue("Object Area", blackPixels);
		rt.addValue("Filtered Area", rPixels);
		rt.addValue("Residual image",nRemoved);
		//rt.addValue("Removed fraction (%)",(100*nRemoved/blackPixels));

		if(i==0){
			IJ.setColumnHeadings(rt.getColumnHeadings());}
			IJ.write(rt.getRowAsString(rt.getCounter()-1));

	}

	void invertForDilation(){
		//INVERT THE IMAGE FOR DILATION
		for (int y=0; y<(h); y++) {
			for (int x=0; x<(w); x++) {
				index=x+y*w;
				pixels[index]=(byte)(255-pixels[index]);

			}
		}

	}

	void invertFromDilation(){
		//INVERT THE IMAGE IF DILATED
		for (int y=0; y<(h); y++) {
			for (int x=0; x<(w); x++) {
				index=x+y*w;
				pixels[index]=(byte)(255-pixels[index]);
				remain[index]=(byte)(255-remain[index]);
			}
		}

	}



	void doFilter(){
		// Eroding
		for (j=0;j<nIterations;j++){
			for (int y=1; y<(h-1); y++) {
				for (int x=1; x<(w-1); x++) {
					index=x+y*w;
					if(pixels[index] == 0){
						// Adding values around index
						p1=(pixels[index-w-1]&0xff); 	p2=(pixels[index-w]&0xff); 	p3=(pixels[index-w+1]&0xff);
						p4=(pixels[index-1]&0xff); 		p5=(pixels[index]&0xff);	p6=(pixels[index+1]&0xff);
						p7=(pixels[index+w-1]&0xff);	p8=(pixels[index+w]&0xff);	p9=(pixels[index+w+1]&0xff);
						sum =  p1 + p2 + p3 + p4 + p6 + p7 + p8+  p9;
						// Check if sum is higher than threshold
						if(sum >= pixelThreshold){remain[index] = (byte)255;}
						else {remain[index]=0;}
					}
					else {remain[index]=(byte)255;}
				}
			}
			//PASS THE VALUES IN REMAIN TO PIXELS FOR NEXT ITERATION
			for (int y=1; y<(h-1); y++) {
				for (int x=1; x<(w-1); x++) {
					index=x+y*w;
					pixels[index]=remain[index];
				}
			}

		}
	}
        public static void main(String[] args) {
		// set the plugins.dir property to make the plugin appear in the Plugins menu
		Class<?> clazz;
                clazz = Rmp_prototype.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
		System.setProperty("plugins.dir", pluginsDir);

            // start ImageJ
                final ImageJ imagej = new ImageJ();
                OpenDialog od = new OpenDialog("Slect...");
                String directory = od.getDirectory();
		String name = od.getFileName();
		String path = directory + name;
		ImagePlus image = IJ.openImage(path);
		 //open the Clown sample
		//ImagePlus image = IJ.openImage("http://imagej.net/images/clown.jpg");
		image.show();

		// run the plugin
		IJ.runPlugIn(clazz.getName(), "Rmp_prototype");
	}
}
