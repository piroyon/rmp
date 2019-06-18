//package prototype;

import ij.ImagePlus;
import ij.process.ByteProcessor;


/**
 * StructuringElement. implements a flat Structuring Element.
 * The type of argument determines the constructor.
 @author  hiroyo
 @version
 **/

public final class Structuring_Element {
    private int bgValue = 255;
    private static final String[] Items = {"From file","Set Square","Set Rect","Set Oval"};
    ImagePlus seimp;
    //private ObjectWindow object;

    
	/**
         * Constructor1: From image file 
	 @param imp      ImagePlus defining the structuring element.
	 @param bgWhite  t:255, f:0
	 **/
    
	public Structuring_Element(ImagePlus imp, boolean bgWhite) {
            if (!bgWhite) {
                if (imp!=null) {
                    imp.getProcessor().invert();
                    imp.updateImage();
                }
            }
            //imp.getProcessor().threshold(254);
            seimp = imp;
         
	}

	/**
         * Constructor2: Make Square SE, require one size
	 @param name     type
	 @param size     width=height
	 */
	public Structuring_Element(String name, int size) {
                seimp = makeRect(size, size); 
	}

	/**
         * Constructor3: Make Oval or Rect SE, reauire two sizes
	 @param name      type
	 @param size1     width
	 @param size2     height
	 */
	public Structuring_Element(String name, int size1, int size2) {
            if (name.equalsIgnoreCase(Items[2])) {
                seimp = makeRect(size1, size2);         
            } else if (name.equalsIgnoreCase(Items[3])) {
                seimp = makeOval(size1, size2); 
            } 
	}

        
        private ImagePlus makeOval(int width, int height) {
                ByteProcessor bp = new ByteProcessor(width, height);
                bp.setColor(255);
                bp.fillOval(0, 0, width, height);
		bp.invert();
		ImagePlus result = new ImagePlus("Oval SE", bp);
		return result;
	}

	private ImagePlus makeRect(int width, int height) {
		ByteProcessor bp = new ByteProcessor(width, height);
                //bp.fillRect(0, 0, width, height);
		ImagePlus result = new ImagePlus("Rect SE", bp);
		return result;
	}

	/*private ImagePlus makeLine(int length, int angle) {
		double radians = Math.PI/2 + angle*Math.PI/180.0;

		// Produce a square ByteProcessor big enough to hold our line SE.
		int height = (int)Math.ceil( ((double)length) * Math.sin(radians) );
		if (height % 2 == 0) {
			height+=1;
		}
		if (height <= length) {
			height=length;
		} else {
			length = height;
		}
		ByteProcessor bp = new ByteProcessor(length,height);
		// Initialize to background.
		for (int x=0;x<length;x++) {
			for (int y=0;y<height; y++) {
				bp.set(x,y,255);
			}
		}
		// Create straight line.
		for (int i=0;i<length;i++) {
			bp.set(i,(int)Math.floor(height/2),0);
		}
		// Rotate.
		bp.rotate((double)angle);
		ImagePlus result = new ImagePlus("rect structuring element", bp);
		return result;
	}*/

	/*private ImagePlus makeRing(int inside, int outside) {
		ByteProcessor bp = new ByteProcessor(2*outside+1, 2*outside+1);
		int width = 2*outside+1;

		for (int x=-outside; x<=outside; x++) {
			for (int y=-outside; y<=outside; y++) {
				if (inRadius(outside,x,y)) {
					if (inRadius(inside,x,y)) {
						bp.set(x+outside,y+outside,255);
					} else {
						bp.set(x+outside,y+outside,0);
					}
				} else {
					bp.set(x+outside,y+outside,255);
				}
			}
		}

		ImagePlus result = new ImagePlus("ring structuring element", bp);
		return result;
	}*/

	/**
	 * Set the background to white (true) or black (false).
	 */
	public void setBgWhite(boolean bgWhite) {
		if (!bgWhite) {
                    //if (bgValue!=0) {
                        if (seimp!=null) {
                            seimp.getProcessor().invert();
                        }
                    //}
                    bgValue=0;
                } else {
                    if (bgValue!=255) {
                        if (seimp!=null) {
                            seimp.getProcessor().invert();
                        }
                    }
                    bgValue=255;
                }
        }
}