package com.piroyon.imagej;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.*;
import ij.io.OpenDialog;
import ij.process.*;
import ij.measure.*;
import ij.WindowManager;
import java.awt.*;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
import java.util.Arrays;
import static java.util.Objects.*;

/**	This plugin performs rmp... maybe
	The plugin requires 8-bits binary images (Process/Binary/treshold)
        * 
	@piroyon 2019
*/


public class Rmp_prototype implements PlugInFilter {

	ImagePlus targetImp, impRemaining, impRemoved, seImp;
        ImageCanvas ic;
	ImageStack imsRemoved;
        int tg, bg, size1, size2;
	int pixelCount,pixelThreshold,i,j,w,h;
	//int nIterations;
	//boolean canceled=false,dilate=false,display=false, displayRem=false,erode = false, open=false,close=false;
	ResultsTable rt = new ResultsTable();
	int index,sum;
        boolean tiWhite, bgWhite;
	//int p1,p2,p3,p4,p5,p6,p7,p8,p9;
	int nRemoved,rPixels;
	int blackPixels, imageSize;
        private static String sename;

        private static final String[] Items = {"From file","Set Square","Set Rect","Set Oval"};
        //private static final String[] items = {"Erode","Dilate","Open","Close"};
	//protected static final int SEfile=0,Square=1,Rect=2,Oval=3;
	//protected static int Choice;

	byte[] remain,remove,pixels,origPixels;
	ByteProcessor ipRemoved;

        @Override
	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about"))
			{showAbout(); return DONE;}
		this.targetImp = imp;
		return DOES_8G;
	}

        @Override
	public void run(ImageProcessor ip) {
                //openSE();
		String[] imageList = new String[WindowManager.getImageCount()];
                imageList[0] = targetImp.getTitle();
                //imageList[1] = seImp.getTitle();
                GenericDialog gd = getDetails(imageList);
                gd.showDialog();
                if (gd.wasCanceled()) return;
                //targetImp = ij.WindowManager.getImage(gd.getNextChoice());
                StructuringElement se;
                tiWhite = gd.getNextBoolean();
                tg = tiWhite ? 255 : 0;
		bgWhite = gd.getNextBoolean();
                int choice = Arrays.asList(Items).indexOf(gd.getNextRadioButton());
                switch (choice) {
                        case 0: //fromFile
                            if (isNull(seImp)) {
                                String seName = gd.getNextChoice();
                                if (seName.equals(imageList[0])) {
                                   IJ.error("ERROR: Target image and SE image are same.");
                                   return;
                                }
                                seImp = ij.WindowManager.getImage(seName);
                                seImp.show();
                            }
                            if (seImp.getType() != 0) {
                                IJ.error("The SE image must be a binary image.");
                                return;
                            }
                            final int[][] searray = seImp.getProcessor().getIntArray();
                            for (int x=0; x<seImp.getWidth(); x++) {
                                for (int y=0; y<seImp.getHeight(); y++) {
                                    if (searray[x][y]!=255 && searray[x][y]!=0) {
					IJ.error("The SE image must be a binary(1-bit) image");
                                        return;
                                    }
                                }
                            }
                            if ((!ckOdd((int)seImp.getWidth())) || (!ckOdd((int)seImp.getHeight()))) {
                                return;
                            }
                            se = new StructuringElement(seImp, bgWhite);
                            break;
                        case 1:  //square
                            size1 = (int)gd.getNextNumber();
                            if (!ckOdd(size1)) {
                                return;
                            }
                            se = new StructuringElement(Items[choice], size1, bgWhite);
                            se.seimp.show();
                            break;
                        case 2:  //rect
                        case 3:  //oval                           
                            size1 = (int)gd.getNextNumber();
                            size2 = (int)gd.getNextNumber();
                            if ((!ckOdd(size1)) || (!ckOdd(size2))) {
                                return;
                            }
                            se = new StructuringElement(Items[choice], size1, size2, bgWhite);
                            se.seimp.show();
                            break;
                        default:
                            return;
                    }
                Morphology2 mo = new Morphology2(targetImp, se, 255, tg);
                ImagePlus e = mo.doFilter();
                e.show();
		// Get information of Stack
                
		int nSlices = targetImp.getStackSize();
		ImageStack stack = targetImp.getStack();
		w = stack.getWidth();
		h = stack.getHeight();
		imageSize=w*h;

		// Create new stack for output
		ImageStack imsRemained = new ImageStack(w,h);
		//if (displayRem){imsRemoved = new ImageStack(w,h);}

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

                    /*switch (Choice) {
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
                    }*/

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
        
        //public StructuringElement makeSE() {
          //  
        //}
        
        public void openSE () {
		OpenDialog od = new OpenDialog("Select SE");
		String directory = od.getDirectory();
		String name = od.getFileName();
		String path = directory + name;
		seImp = IJ.openImage(path);
                //int setype = seImp.getType();
		seImp.show();
                //ic = imp.getCanvas();  //ImageCanvasにImageを渡す
	}

	public void showAbout() {
		IJ.showMessage("rmp prototype",
			"test" );
	}

	private GenericDialog getDetails(String imageList[]){
            final GenericDialog gd;
            gd = new GenericDialog("rmp prototype...");
            final Panel pnl = new Panel(new FlowLayout());
            final Button btn = new Button("Open SE image file");
            pnl.add(btn);
            btn.addActionListener((final ActionEvent e) -> {
                final OpenDialog od = new OpenDialog("Select SE");
                //need canceling action
                String directory = od.getDirectory();
                sename = od.getFileName();
                String path = directory + sename;
                seImp = IJ.openImage(path);
                seImp.show();
            });
            gd.setInsets(5, 1, 10);
            gd.addCheckbox("Target image's Background is white",false);
            gd.addRadioButtonGroup("Structuring Element (SE):", Items, 1, 4, "From file");
            gd.addPanel(pnl);
            gd.addChoice("or Select SE from opened...",imageList,imageList[0]);     
            gd.addNumericField("Size1: (SQUARE or (RECT or OVAL Side1)", 3, 0, 2, "px,");
            //gd.addToSameRow();
            gd.addNumericField("Size2: (RECT or OVAL Side2)", 7, 0, 2, "px");
            gd.addCheckbox("SE Background is white:",true);
            gd.setInsets(25,0,10);
            gd.addNumericField("Number of rotations", 8, 0, 2, "");
		/*return result;		
                gd.addNumericField("Set count (1-8):",4,0);
		gd.addNumericField("Iterations (1-25): ",1,0);
        		gd.addChoice("Do", items, items[Choice]);
		gd.addCheckbox("Display residual image ", displayRem);
		gd.addCheckbox("Display Results ", display);*/

		//Correct the number of required neighborhood black pixels
        /*pixelCount = (int)gd.getNextNumber();
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
		display = gd.getNextBoolean();*/
            return gd;
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


	
        //public void ckOdd(int size) throws ApplicationException {
        public boolean ckOdd(int size) {
            switch(size % 2) {
                case 0:
                default:
                    //throw new ApplicationException("Structuring elements must have odd height and width");
                    IJ.error("Structuring elements must have odd height and width");
                    return false;
                case 1:
                    return true;       
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
                OpenDialog od = new OpenDialog("Select...", null);
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
