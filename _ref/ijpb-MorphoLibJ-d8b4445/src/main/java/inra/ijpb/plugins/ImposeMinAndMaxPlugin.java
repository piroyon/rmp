/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.MinimaAndMaxima;
import inra.ijpb.util.IJUtils;

/**
 * Plugin for imposing minima or maxima on a grayscale image, using a specific
 * connectivity. 
 */
public class ImposeMinAndMaxPlugin implements PlugIn {

	/**
	 * A customized enumeration to choose between minima or maxima imposition.
	 */
	public enum Operation {
		IMPOSE_MINIMA("Impose Minima"),
		IMPOSE_MAXIMA("Impose Maxima");
		
		private final String label;
		
		private Operation(String label) {
			this.label = label;
		}
		
		public ImageProcessor applyTo(ImageProcessor image,
				ImageProcessor markers) {
			if (this == IMPOSE_MINIMA)
				return MinimaAndMaxima.imposeMinima(image, markers);
			if (this == IMPOSE_MAXIMA)
				return MinimaAndMaxima.imposeMaxima(image, markers);
						
			throw new RuntimeException(
					"Unable to process the " + this + " operation");
		}
		
		public ImageProcessor applyTo(ImageProcessor image,
				ImageProcessor markers, int conn) {
			if (this == IMPOSE_MINIMA)
				return MinimaAndMaxima.imposeMinima(image, markers, conn);
			if (this == IMPOSE_MAXIMA)
				return MinimaAndMaxima.imposeMaxima(image, markers, conn);
						
			throw new RuntimeException(
					"Unable to process the " + this + " operation");
		}
		
		public String toString() {
			return this.label;
		}
		
		public static String[] getAllLabels(){
			int n = Operation.values().length;
			String[] result = new String[n];
			
			int i = 0;
			for (Operation op : Operation.values())
				result[i++] = op.label;
			
			return result;
		}
		
		/**
		 * Determines the operation type from its label.
		 * 
		 * @param opLabel
		 *            the name of the operation
		 * @return the operation corresponding to the name
		 * @throws IllegalArgumentException
		 *             if operation name is not recognized.
		 */
		public static Operation fromLabel(String opLabel) {
			if (opLabel != null)
				opLabel = opLabel.toLowerCase();
			for (Operation op : Operation.values()) {
				String cmp = op.label.toLowerCase();
				if (cmp.equals(opLabel))
					return op;
			}
			throw new IllegalArgumentException("Unable to parse Operation with label: " + opLabel);
		}
	};

	private final static String[] connectivityLabels = {"4", "8"}; 
	private final static int[] connectivityValues = {4, 8}; 
	

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg) {
		// Open a dialog to choose:
		// - mask image
		// - marker image
		int[] indices = WindowManager.getIDList();
		if (indices == null) {
			IJ.error("No image", "Need at least one image to work");
			return;
		}
	
		// create the list of image names
		String[] imageNames = new String[indices.length];
		for (int i = 0; i < indices.length; i++) {
			imageNames[i] = WindowManager.getImage(indices[i]).getTitle();
		}
		
		// create the dialog
		GenericDialog gd = new GenericDialog("Impose Min & Max");
		
		gd.addChoice("Original Image", imageNames, IJ.getImage().getTitle());
		gd.addChoice("Marker Image", imageNames, IJ.getImage().getTitle());
		gd.addChoice("Operation", 
				Operation.getAllLabels(), 
				Operation.IMPOSE_MINIMA.label);
		gd.addChoice("Connectivity", connectivityLabels, connectivityLabels[0]);
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;

		// set up current parameters
		int refImageIndex = gd.getNextChoiceIndex();
		ImagePlus refImage = WindowManager.getImage(refImageIndex + 1);
		int markerImageIndex = gd.getNextChoiceIndex();
		ImagePlus markerImage = WindowManager.getImage(markerImageIndex + 1);
		Operation op = Operation.fromLabel(gd.getNextChoice());
		int conn = connectivityValues[gd.getNextChoiceIndex()];
		
		// Extract image procesors
		ImageProcessor refProc = refImage.getProcessor();
		ImageProcessor markerProc = markerImage.getProcessor();
		
		long t0 = System.currentTimeMillis();
		
		// Compute geodesic reconstruction
		ImageProcessor recProc = op.applyTo(refProc, markerProc, conn);
		
		// Keep same color model as 
		recProc.setColorModel(refProc.getColorModel());

		// create resulting image
		String newName = createResultImageName(refImage);
		ImagePlus resultImage = new ImagePlus(newName, recProc);
		resultImage.copyScale(markerImage);
		resultImage.show();
		
		long t1 = System.currentTimeMillis();
		IJ.showStatus("Elapsed time: " + (t1 - t0) / 1000. + "s");
		IJUtils.showElapsedTime(op.toString(), t1 - t0, refImage);
	}

	private static String createResultImageName(ImagePlus baseImage) {
		return baseImage.getShortTitle() + "-imp";
	}
	
}
