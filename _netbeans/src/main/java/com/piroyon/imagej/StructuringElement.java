package com.piroyon.imagej;

import ij.*;
import ij.process.*;
import java.io.*;


/**
 * StructElement. Implements a flat Structuring Element for mathematical morphology.
 * Used in conjunction with utility methods in org.ajdecon.morphology.Morphology.
 * Uses the ImageJ ImagePlus as an image representation object.
 *
 * For more information on mathematical morphology: http://en.wikipedia.org/wiki/Mathematical_morphology
 * For ImageJ: http://rsbweb.nih.gov/ij/ 
 *
 @author Adam DeConinck
 @version 0.1
 *
 */
public final class StructuringElement {
/*
 * Constants and properties.
 */
    private int bgValue = 255;
    private static final String[] Items = {"From file","Set Square","Set Rect","Set Oval"};
    ImagePlus seimp;
    private ObjectWindow object;

/*
 * Constructors!
 */
	/**
	 * 
	 * Structuring element with arbitrary shape: constructor takes ImagePlus which defines neighborhood with
	 * black and white pixels.
	 *
	 @param imp ImagePlus defining the structuring element.  Auto-thresholded so only maximum pixels (255) are
	           seen as white.
	 @param bgWhite Defines whether the background is white (true) or black (false).
	 *
	 */
	public StructuringElement(ImagePlus imp, boolean bgWhite) {
            if (!bgWhite) {
                    //if (bgValue!=0) {
                if (imp!=null) {
                    imp.getProcessor().invert();
                    imp.updateImage();
                }
            }
            //setBgWhite(bgWhite);   
		// Make our ImagePlus contents an 8-bit image.
		//seproc = new ImagePlus("structuring element", seimp.getProcessor());
            //ImageConverter ic = new ImageConverter(imp);
            //ic.convertToGray8();
            
		// Then threshold so only 255 pixels are white.  This 
		// should already be the case, but let's make sure, shall we?
            //imp.getProcessor().threshold(254);
            seimp = imp;
         
	}

	/**
	 * When one size is supplied, StructElement can be a StructElement.CIRCLE or a 
	 * StructElement.SQUARE.
	 *
	 @param name Defines the type of structuring element.
	 @param size The radius of a circle, or the width of a square.
	 @param bgWhite Defines whether the background is white (true) or black (false).
	 //@return Square or Circle StructElement.
	 *
	 */
	public StructuringElement(String name, int size, boolean bgWhite) {
		//setBgWhite(bgWhite);
                seimp = makeRect(size, size); 
		    //if (name.equalsIgnoreCase(CIRCLE)) {
			//    contents = makeCircle(size);
		    //} else 
                    //if (name.equalsIgnoreCase(Items[1])) {
		//	    if (size % 2 == 0) {
		//		    throw new Exception("Size of structuring element must be an odd number of pixels!");
    		//	}
	    	
		//    } else {
		//	    throw new Exception("Unknown type of structuring element!");
    		//}
		//}
		//catch(Exception e) {
		//	e.printStackTrace();
		//}

	}

	/**
	 * With two sizes, produces a rectangular, ring-shaped or line-shaped structuring element.
	 *
	 @param name Defines the type of structuring element.
	 @param size1 Defines the width of a rectangle, the inner radius of a ring or the length of a line.
	 @param size2 Defines the height of a rectangle, the outer radius of a ring or the angle of a line in degrees.
	 @param bgWhite Defines whether the background is white (true) or black (false).
	 @return A rectangular, ring-shaped or line-shaped structuring element.
	 *
	 */
	public StructuringElement(String name, int size1, int size2, boolean bgWhite) {
            //setBgWhite(bgWhite);
            //try {
            /*if (name.equalsIgnoreCase(RING)) {
                contents = makeRing(size1, size2);
            } else*/
            if (name.equalsIgnoreCase(Items[2])) {
                seimp = makeRect(size1, size2); 
                //
            
            } else if (name.equalsIgnoreCase(Items[3])) {
                seimp = makeOval(size1, size2); 
                //} 
                //else if (name.equalsIgnoreCase(LINE)) {
        
                //	if (size1 % 2 == 0) {
                //		throw new Exception("Size of structuring element must be an odd number of pixels!");
                //	}
                    //seimp = makeLine(size1, size2); 
            } 
            //else {
            //        throw RuntimeException("Unknown type of structuring element!");
            //    }
            //}
            //catch(Exception e) {
		//e.printStackTrace();
            //}
	}


/*
 * private parts of constructors: build new images.
 */
	//private ImagePlus makeCircle(int radius) {
        private ImagePlus makeOval(int width, int height) {
		//ByteProcessor bp = new ByteProcessor(2*radius+1, 2*radius+1);              
                //int radius1, radius2;
                ByteProcessor bp = new ByteProcessor(width, height);
		int x = width/2 + 1;
                int y = height/2 + 1;
                bp.setColor(255);
                bp.fillOval(0, 0, width, height);
		//if (!(isBgWhite())) {
		bp.invert();
		//}
		ImagePlus result = new ImagePlus("oval SE", bp);
		return result;
	}

	private ImagePlus makeRect(int width, int height) {
		ByteProcessor bp = new ByteProcessor(width, height);
                bp.fillRect(0, 0, width, height);
		//if (!(isBgWhite())) {
		//	bp.invert();
		//}
		ImagePlus result = new ImagePlus("rect SE", bp);
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
		// Invert if needed.
		if (!(isBgWhite())) {
			bp.invert();
		}
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
		if (!(isBgWhite())) {
			bp.invert();
		}
		ImagePlus result = new ImagePlus("ring structuring element", bp);
		return result;
	}*/

/*
 * Methods
 */
	/**
	 * Is the background white?
     * @return 
	 */
	public boolean isBgWhite() {
		if (bgValue==255) {
			return true;
		}
		return false;
	}

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
	
	/**
	 *
	 @return An ImagePlus representing the structuring element.
	 */
	/*public ImagePlus getImage() {
		return contents;
	}*/

	/**
	 *
	 @return The ImageProcessor of the internal structuring element.
	 */
	/*public ImageProcessor getProcessor() {
		return contents.getProcessor();
	}*/

	/**
	 * Store an internal representation of the object image to be operated on by 
	 * this structuring element.
	 *
	 @param imp Image to be operated on.
	 @param symmetric Determines if boundary conditions are symmetric or if the edges are padded with background.
	 *
	 */
	public void setObject(ImagePlus imp, boolean symmetric) {
		//object = new ObjectWindow(imp, this, bgValue, symmetric);
                object = new ObjectWindow(imp, this, bgValue, symmetric);
	}

	/**
	 * Return a list of pixel values corresponding to the object image's contents when the structuring element
	 * overlaps the specified coordinates.
	 *
	 */
	public int[] window(int x0, int y0) {
		return object.view(x0,y0);			
	}

	/**
	 *
	 @return The size of the structuring element's foreground in pixels.
	 */
	public int getSize() {
		int[][] se = seimp.getProcessor().getIntArray();
		int sewidth=seimp.getWidth(); int seheight=seimp.getHeight();
		int counter=0;

		for (int x=0; x<sewidth; x++) {
			for (int y=0; y<seheight; y++) {
				if (se[x][y]!=bgValue) {
					counter += 1;
				}
			}
		}
		return counter;
	}

	/**
	 * Print a character-based representation of the structuring element.
	 *
	 @param pr PrintStream to use.
	 */
	public void printStructure(PrintStream pr) {

            int[][] pixels = seimp.getProcessor().getIntArray();
            for (int[] pixel : pixels) {
                for (int x = 0; x < pixel.length; x++) {
                    if (pixel[x] == bgValue) {
                        pr.print(0);
                    } else {
                        pr.print(1);
                    }
                    pr.print(" ");
                }
                pr.println();
            }
	}

	public void printStructure() {
		printStructure(System.out);
	}


/*
 * miscellaneous functions
 *
 */
	private boolean inRadius(int r, int x, int y) {
		double rsq = Math.pow((double)r,2);
		double dist = Math.pow((double)x,2) + Math.pow((double)y,2);
		if (dist<=rsq) {
			return true;
		}
		return false;
	}

    private Exception RuntimeException(String unknown_type_of_structuring_element) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

/**
 * Container for the object image which can return windowed views where the structuring element overlaps.
 *
 @author Adam DeConinck
 @version 0.1
class ObjectWindow {

	private final int[][] se;
	private final int[][] object;
	int bgValue;
	private int sewidth, seheight, width, height, dx, dy, size;
	boolean symmetric;

	/** 
	 * Construct an ObjectWindow based on the supplied image and structuring element.
	 *
	 @param im ImagePlus containing the object image.
	 @param s Structuring element used to generate the windows.
	 @param bg Background value.
	 @param sym Determines if boundary conditions are symmetric.
	 **/
/**
	//public ObjectWindow(ImagePlus im, StructElement s, int bg, boolean sym) {
        public ObjectWindow(ImagePlus im, ImagePlus s, int bg, boolean sym) {
                object = im.getProcessor().getIntArray();
		//se = s.getImage.getProcessor().getIntArray();
                se = s.getProcessor().getIntArray();
		

		bgValue = bg;

		//sewidth=s.getImage().getWidth(); seheight=s.getImage().getHeight();
                sewidth=s.getWidth(); 
                seheight=s.getHeight();
		width=im.getWidth(); 
                height=im.getHeight();

		dx=sewidth/2; // int division truncates
		dy=seheight/2;

		symmetric = sym;
                //int counter=0;
                
                //get the size of the structuring element's foreground in pixels.
		for (int x=0; x<sewidth; x++) {
			for (int y=0; y<seheight; y++) {
				if (se[x][y]!=bgValue) {
					size += 1;
				}
			}
		}
		//size = s.getSize();
	}
**/
	/**
	 * Produce a list of pixel values which are overlapped by the structuring element when centered at a given
	 * coordinate.
	 */
/**
	public int[] view(int x0, int y0) {

		int[] result = new int[size];
		int k=0;

		for (int x=-dx; x<=dx; x++) {
			for (int y=-dx; y<=dx; y++) {
				// Check if we are in a SE foreground pixel.
				if (se[x+dx][y+dy]!=bgValue) {
					// Coordinates in the object image.
					int xc=x0+x; int yc=y0+y;

					// Check x boundary conditions.
					if (xc<0) {
						if (!(symmetric)) {
							result[k]=bgValue;
							k=k+1;
							continue;
						} else {
							xc=width+xc;
						}
					} else if (xc >= width ) {
						if (!(symmetric)) {
							result[k]=bgValue;
							k=k+1;
							continue;
						} else {
							xc=xc-width;
						}
					}
					
					// Check y boundary conditions.
					if (yc<0) {
						//System.out.print("Ymin ");
						//System.out.print(x0); System.out.print(" "); System.out.print(y0);
						//System.out.println();
						if (!(symmetric)) {
							result[k]=bgValue;
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
							result[k]=bgValue;
							k=k+1;
							continue;
						} else {
							yc=yc-height;
						}
					}
					
					// Add this pixel to the result window.
					result[k]=object[xc][yc];
					k=k+1;
				}
				
			}
		}
		return result;	
	}
}
**/