//package prototype;

import ij.IJ;
import ij.ImageJ; //no need installed plugin
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.ImageStack;
import ij.ImagePlus;
import ij.io.OpenDialog;
import ij.WindowManager;
import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import static java.util.Objects.isNull;

/**	This plugin performs rmp... maybe
	* Thes plugin requires 8-bits binary images (Process/Binary/treshold)
        * i'd like to dedicate this program to my dearist collaborators developing this algorithm
        @author  hiroyo
        @version 
        * 
*/


public class NB_Prototype implements PlugInFilter {

	private ImagePlus targetImp, seImp;
        private static int choice, size1, size2, rotate, interpolationMethod;
        private static boolean bgWhite, tgWhite;
        private static String sename;
        private static final String[] Items = {"From file","Set Square","Set Rect","Set Oval"};

        @Override
	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about"))
			{showAbout(); return DONE;}
		this.targetImp = imp;
		return DOES_8G;
	}

        @Override
	public void run(ImageProcessor ip) {
		String[] imageList = new String[WindowManager.getImageCount()];
                imageList[0] = targetImp.getTitle();
                GenericDialog gd = getDetails(imageList);
                gd.showDialog();
                if (gd.wasCanceled()) return;
                Structuring_Element se;
                tgWhite = gd.getNextBoolean();
		bgWhite = gd.getNextBoolean();
                choice = Arrays.asList(Items).indexOf(gd.getNextRadioButton());
                size1 = (int)gd.getNextNumber();
                size2 = (int)gd.getNextNumber();
                rotate = (int)gd.getNextNumber();
                String seName = gd.getNextChoice();
                interpolationMethod = gd.getNextChoiceIndex();
                switch (choice) {
                        case 0: //fromFile
                            if (isNull(seImp)) {
                                //String seName = gd.getNextChoice();
                                if (seName.equals(imageList[0])) {
                                   IJ.error("ERROR: Target image and SE image are same.");
                                   return;
                                }
                                seImp = ij.WindowManager.getImage(seName);
                                try {
                                    seImp.show();
                                } catch (NullPointerException e) {
                                    IJ.error("Select correct image file.. "+e);
                                    return;
                                }
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
                            se = new Structuring_Element(seImp, bgWhite);
                            break;
                        case 1:  //square
                            if (size1 == 0) {
                                IJ.error("Input SE height & width > 0");
                                return;
                            }
                            if (!ckOdd(size1)) {
                                return;
                            }
                            se = new Structuring_Element(Items[choice], size1);
                            se.seimp.show();
                            break;
                        case 2:  //rect
                        case 3:  //oval                           
                            if (size1 == 0 || size2 == 0) {
                                IJ.error("Input SE height & width > 0");
                                return;
                            }
                            if ((!ckOdd(size1)) || (!ckOdd(size2))) {
                                return;
                            }
                            se = new Structuring_Element(Items[choice], size2, size1);
                            se.seimp.show();
                            break;
                        default:  //?
                            IJ.error("Select SE shape");
                            return;
                    }

                Execute_process mo = new Execute_process(targetImp, se, choice, rotate);
                //
                IJ.showStatus("Rotating and filtering "+Integer.toString(rotate)+" times...");
                ImageStack rstack = mo.doRmp(tgWhite, interpolationMethod);
                IJ.showStatus("Make Stack from "+Integer.toString(rotate)+" images...");
                ImagePlus rotatedIP = new ImagePlus("rotate result", rstack);
                rotatedIP.show();
                IJ.showStatus("Calcurating maximum value...");
                ImagePlus resultIP = new ImagePlus("result", mo.getMaxValue(rstack));
                resultIP.show();
                

	}
  
        /*public void openSE () {
		OpenDialog od = new OpenDialog("Select SE");
		String directory = od.getDirectory();
		String name = od.getFileName();
		String path = directory + name;
		seImp = IJ.openImage(path);
                //int setype = seImp.getType();
		seImp.show();
	}*/

	public void showAbout() {
		IJ.showMessage("rmp prototype",
			"test" );
	}

	private GenericDialog getDetails(String imageList[]){
            final GenericDialog gd;
            final String[] methods = ImageProcessor.getInterpolationMethods();
            gd = new GenericDialog("rmp prototype...");
            final Panel pnl = new Panel(new FlowLayout());
            final Button btn = new Button("Open SE image file");
            pnl.add(btn);
            btn.addActionListener((final ActionEvent ae) -> {
                final OpenDialog od = new OpenDialog("Select SE");
                String directory = od.getDirectory();
                sename = od.getFileName();
                if (sename != null) {
                    String path = directory + sename;
                    seImp = IJ.openImage(path);
                    try {
                        seImp.show();
                    } catch (NullPointerException e) {
                        IJ.error("Select correct image file.. "+e);
                    }
                }
            });
            gd.setInsets(5, 1, 10);
            gd.addCheckbox("Target image's Background is white",false);
            gd.addRadioButtonGroup("Structuring Element (SE):", Items, 1, 4, "From file");
            gd.addPanel(pnl);
            gd.addChoice("or Select SE from opened...",imageList,imageList[0]);
            gd.addCheckbox("SE Background is white:",true);
            gd.setInsets(25,0,10);
            gd.addNumericField("Make SE Height:", 3, 0, 2, "px,");
            //gd.addToSameRow();
            gd.addNumericField("Width:", 3, 0, 2, "px");        
            gd.setInsets(25,0,10);
            gd.addNumericField("Number of rotations", 8, 0, 2, "");
            gd.addChoice("Interpolation:", methods, methods[1]);
            return gd;
    	}

        public boolean ckOdd(int size) {
            switch(size % 2) {
                case 0:
                default:
                    IJ.error("Structuring elements must have odd height and width");
                    return false;
                case 1:
                    return true;       
            }
	}
        
    /*  public static void main(String[] args) {   // to run on netbeans 
		Class<?> clazz;
                clazz = NB_Prototype.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
                
		String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
                
		System.setProperty("plugins.dir", pluginsDir);

            // start ImageJ
                final ImageJ imagej = new ImageJ();
                IJ.showMessage(url);
                IJ.showMessage(pluginsDir);
                OpenDialog od = new OpenDialog("Select...", null);
                String directory = od.getDirectory();
		String name = od.getFileName();
		String path = directory + name;
		ImagePlus image = IJ.openImage(path);
                try {
                    image.show();
                } catch(NullPointerException e) {
                    IJ.error("Cannnot open image "+e);
                }

		// run the plugin
		IJ.runPlugIn(clazz.getName(), "Prototype");
	}*/

 
}