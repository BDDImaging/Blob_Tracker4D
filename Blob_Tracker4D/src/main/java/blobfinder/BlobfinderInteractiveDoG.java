package blobfinder;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;

import graphconstructs.Logger;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import mpicbg.imglib.util.Util;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.algorithm.dog.DogDetection;
import net.imglib2.algorithm.localextrema.RefinedPeak;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import snakes.SnakeObject;

public class BlobfinderInteractiveDoG implements Blobfinder {

	
	
	private static final String BASE_ERROR_MSG = "[Blobfinder] ";
	protected Logger logger = Logger.DEFAULT_LOGGER;
	protected String errorMessage;
	private ArrayList<SnakeObject> ProbBlobs;
	private final RandomAccessibleInterval<FloatType> source;
	private final RandomAccessibleInterval<FloatType> target;
	private final int thirdDimension;
	private final int fourthDimension;
	private final int ndims;
	private final ArrayList<RefinedPeak<Point>> peaks;
	public final boolean lookforMaxima;
	public final boolean lookforMinima;
	public final double sigma;
	public final double sigma2;
	
	
	public BlobfinderInteractiveDoG(final RandomAccessibleInterval<FloatType> source, final RandomAccessibleInterval<FloatType> target, boolean lookforMaxima, boolean lookforMinima,
			final double sigma, final double sigma2, ArrayList<RefinedPeak<Point>> peaks,
		  final int thirdDimension, final int fourthDimension){
		
		this.source = source;
		this.target = target;
		this.lookforMaxima = lookforMaxima;
		this.lookforMinima = lookforMinima;
		this.sigma = sigma;
		this.sigma2 = sigma2;
		this.peaks = peaks;
		this.thirdDimension = thirdDimension;
		this.fourthDimension = fourthDimension;
		ndims = source.numDimensions();
	}
	
	
	
	
	
	
	@Override
	public ArrayList<SnakeObject> getResult() {
		
		return ProbBlobs;
	}

	@Override
	public boolean checkInput() {
		if (source.numDimensions() > 2 ) {
			errorMessage = BASE_ERROR_MSG + " Can only operate on 1D, 2D, make slices of your stack . Got "
					+ source.numDimensions() + "D.";
			return false;
		}
		return true;
	}

	@Override
	public boolean process() {
		
		
		 ProbBlobs = new ArrayList<SnakeObject>();
		
		
		 
		 

			
			ArrayList<Roi> ovalrois = new ArrayList<Roi>();
			int count = 0;
			for (final RefinedPeak<Point> peak : peaks) {
				float x = (float) (peak.getFloatPosition(0));
				float y = (float) (peak.getFloatPosition(1));

				count++;
				final OvalRoi or = new OvalRoi(Util.round(x - sigma), Util.round(y - sigma), Util.round(sigma + sigma2),
						Util.round(sigma + sigma2));


				ovalrois.add(or);
				
				final double[] props = getProps(or); 
				final double[] center = new double[]{props[0], props[1], props[2]};
				final double Intensitysource = props[3];
				final int Numberofpixels = (int)props[4];
				final double MeanIntensitysource = props[5];
				final double Circularity = props[6];
				final double Intensitytarget = props[7];
				final int NumberofpixelsTarget = (int)props[8];
				final double MeanIntensitytarget = props[9];
				final double Size = props[10];
				
				SnakeObject currentsnake = new SnakeObject(thirdDimension, fourthDimension, count, or, center,
						Intensitysource, Numberofpixels, MeanIntensitysource, Intensitytarget, NumberofpixelsTarget, MeanIntensitytarget, Circularity, Size);
				
				
				
				ProbBlobs.add(currentsnake);
			
			}
			
		
		return true;
	}

	
	public ArrayList<Roi> getRois(ArrayList<RefinedPeak<Point>> peaks){
		
		ArrayList<Roi> ovalrois = new ArrayList<Roi>();
		
		for (final RefinedPeak<Point> peak : peaks) {
			float x = (float) (peak.getFloatPosition(0));
			float y = (float) (peak.getFloatPosition(1));

			final OvalRoi or = new OvalRoi(Util.round(x - sigma), Util.round(y - sigma), Util.round(sigma + sigma2),
					Util.round(sigma + sigma2));

			if (lookforMaxima)
				or.setStrokeColor(Color.red);
			else
				or.setStrokeColor(Color.green);

			ovalrois.add(or);
			
			
			
		}
		
		return ovalrois;
		
		
	}
	
	
	public double[] getProps(Roi roi) {

		// 3 co-ordinates for COM 1 for TotalIntensity, 1 for Number of pixels, I for Mean Intensity, 1 for Circularity = 7, Same for target image : , 8 TotalIntensitytarget, 
		// 9 Mean Intensity target, 10 size of the Blob
		double[] center = new double[ 10 ];

		double Intensity = 0;
		double Numberofpixels = 0;
		double IntensitySec = 0;
		
		
		
		double SumX = 0;
		double SumY = 0;
		Cursor<FloatType> currentcursor = Views.iterable(source).localizingCursor();

		RandomAccess<FloatType> targetran = target.randomAccess();
		final double[] position = new double[ 2 ];

		while (currentcursor.hasNext()) {

			currentcursor.fwd();

			currentcursor.localize(position);

			int x = (int) position[0];
			int y = (int) position[1];

			if (roi.contains(x, y)) {

				targetran.setPosition(currentcursor);
				
				Numberofpixels++;
				SumX += currentcursor.getDoublePosition(0) * currentcursor.get().getRealDouble();
				SumY += currentcursor.getDoublePosition(1) * currentcursor.get().getRealDouble();
				Intensity += currentcursor.get().getRealDouble();
				IntensitySec += targetran.get().getRealDouble();
				
				
				
			}

		}
		
		center[ 0 ] = SumX / Intensity;
		center[ 1 ] = SumY / Intensity;
		
		center[ 2 ] = 1;
		center[ 3 ] = Intensity;
		center[ 4 ] = Numberofpixels;
		center[ 5 ] = Intensity / Numberofpixels;
		final double perimeter = roi.getLength();
		center[ 6 ] = 4 * Math.PI * Numberofpixels / Math.pow(perimeter, 2);
		center[ 7 ] = IntensitySec;
		center[ 8 ] = IntensitySec / Numberofpixels;
	    center[ 9 ] = roi.getCornerDiameter();
		
		
		

		return center;

	}
	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLogger(Logger logger) {
		// TODO Auto-generated method stub
		
	}

}
