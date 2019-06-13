/**
 * 
 */
package inra.ijpb.measure.region2d;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map;

import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.geometry.Box2D;
import inra.ijpb.geometry.Polygon2D;
import inra.ijpb.geometry.Polygons2D;
import inra.ijpb.label.LabelImages;

/**
 * Computes convex area and convexity for regions within a binary or label
 * image.
 * 
 * @author dlegland
 *
 */
public class Convexity extends RegionAnalyzer2D<Convexity.Result>
{
	public static final ImageProcessor convexify(ImageProcessor binaryImage)
	{
		// compute convex hull of boundary points around the binary particle
		ArrayList<Point2D> points = RegionBoundaries.boundaryPixelsMiddleEdges(binaryImage);
		Polygon2D convexHull = Polygons2D.convexHull(points);

		// create result image
		int sizeX = binaryImage.getWidth();
		int sizeY = binaryImage.getHeight();
		ImageProcessor result = new ByteProcessor(sizeX, sizeY);
		
		// determines bounds
		Box2D box = convexHull.boundingBox();
		int ymin = (int) Math.max(0, Math.floor(box.getYMin()));
		int ymax = (int) Math.min(sizeY, Math.ceil(box.getYMax()));
		int xmin = (int) Math.max(0, Math.floor(box.getXMin()));
		int xmax = (int) Math.min(sizeX, Math.ceil(box.getXMax()));
		
		// iterate over pixels within bounding box
		for (int y = ymin; y < ymax; y++)
		{
			for (int x = xmin; x < xmax; x++)
			{
				if (convexHull.contains(new Point2D.Double(x + 0.5, y + 0.5)))
				{
					result.set(x, y, 255);
				}
			}
		}
		
		return result;
	}
	
	@Override
	public ResultsTable createTable(Map<Integer, Convexity.Result> results)
	{
		// Initialize a new result table
		ResultsTable table = new ResultsTable();
	
		// Convert all results that were computed during execution of the
		// "computeGeodesicDistanceMap()" method into rows of the results table
		for (int label : results.keySet())
		{
			// current diameter
			Result res = results.get(label);
			
			// add an entry to the resulting data table
			table.incrementCounter();
			table.addLabel(Integer.toString(label));
			table.addValue("Area", res.area);
			table.addValue("ConvexArea", res.convexArea);
			table.addValue("Convexity", res.convexity);
		}
	
		return table;
	}

	@Override
	public Convexity.Result[] analyzeRegions(ImageProcessor image, int[] labels,
			Calibration calib)
	{
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();

		// create result array
		Convexity.Result[] res = new Convexity.Result[labels.length];
		
		// iterate over labels
		for (int i = 0; i < labels.length; i++)
		{
			ImageProcessor binary = LabelImages.binarize(image, labels[i]);
			
			// compute convex hull of boundary points around the binary particle
			ArrayList<Point2D> points = RegionBoundaries.boundaryPixelsMiddleEdges(binary);
			Polygon2D convexHull = Polygons2D.convexHull(points);

			// determines bounds
			Box2D box = convexHull.boundingBox();
			int ymin = (int) Math.max(0, Math.floor(box.getYMin()));
			int ymax = (int) Math.min(sizeY, Math.ceil(box.getYMax()));
			int xmin = (int) Math.max(0, Math.floor(box.getXMin()));
			int xmax = (int) Math.min(sizeX, Math.ceil(box.getXMax()));
			
			double area = 0;
			double convexArea = 0;
			
			// iterate over pixels within bounding box
			for (int y = ymin; y < ymax; y++)
			{
				for (int x = xmin; x < xmax; x++)
				{
					if (binary.get(x, y) > 0)
					{
						area++;
					}
					if (convexHull.contains(new Point2D.Double(x + 0.5, y + 0.5)))
					{
						convexArea++;
					}
					
					res[i] = new Convexity.Result(area, convexArea);
				}
			}

		}
		
		return res;
	}

	public class Result
	{
		public double area;
		public double convexArea;
		public double convexity;
		
		public Result(double area, double convexArea)
		{
			this.area = area;
			this.convexArea = convexArea;
			this.convexity = area / convexArea;
		}
	}
}
