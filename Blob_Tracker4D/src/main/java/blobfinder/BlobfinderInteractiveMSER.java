package blobfinder;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import graphconstructs.Logger;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.EllipseRoi;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import mserMethods.GetDelta;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.mser.Mser;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.algorithm.dog.DogDetection;
import net.imglib2.algorithm.localextrema.RefinedPeak;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import snakes.SnakeObject;


public class BlobfinderInteractiveMSER implements Blobfinder {

	
	private static final String BASE_ERROR_MSG = "[Blobfinder] ";
	protected Logger logger = Logger.DEFAULT_LOGGER;
	protected String errorMessage;
	private ArrayList<SnakeObject> ProbBlobs;
	private final RandomAccessibleInterval<FloatType> source;
	
	private final RandomAccessibleInterval<FloatType> target;
	private final MserTree<UnsignedByteType> newtree;
	
	
	public boolean darktoBright = false;
	
	private final int ndims;
	private final int fourthDimension;
	private final int thirdDimension;
	private int Roiindex;
	private Roi ellipseroi;
	private final double sizeX;
	private final double sizeY;
	

	public BlobfinderInteractiveMSER(final RandomAccessibleInterval<FloatType> source, final RandomAccessibleInterval<FloatType> target,
			MserTree<UnsignedByteType> newtree, final double sizeX, final double sizeY, final int thirdDimension,  final int fourthDimension){
		
		this.source = source;
		this.target = target;
		this.newtree = newtree;
	    this.thirdDimension = thirdDimension;
		this.fourthDimension = fourthDimension;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
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
		
		
		 final FloatType type = source.randomAccess().get().createVariable();
			

			ArrayList<double[]> ellipselist = new ArrayList<double[]>();
			ArrayList<double[]> meanandcovlist = new ArrayList<double[]>();
			
			
			
			final HashSet<Mser<UnsignedByteType>> rootset = newtree.roots();
			
			
			final Iterator<Mser<UnsignedByteType>> rootsetiterator = rootset.iterator();
			
			
			
			
			while (rootsetiterator.hasNext()) {

				Mser<UnsignedByteType> rootmser = rootsetiterator.next();

				if (rootmser.size() > 0) {

					final double[] meanandcov = { rootmser.mean()[0], rootmser.mean()[1], rootmser.cov()[0],
							rootmser.cov()[1], rootmser.cov()[2] };
					meanandcovlist.add(meanandcov);
					ellipselist.add(meanandcov);

				}
			}
			
			// We do this so the ROI remains attached the the same label and is not changed if the program is run again
		       SortListbyproperty.sortpointList(ellipselist);
			int count = 0;
				for (int index = 0; index < ellipselist.size(); ++index) {
					
					
					final ImgFactory<FloatType> factory = Util.getArrayOrCellImgFactory(source, type);
					
					RandomAccessibleInterval<FloatType>  ActualRoiimg = factory.create(source, type);
					
					final double[] mean = { ellipselist.get(index)[0], ellipselist.get(index)[1] };
					final double[] covar = { ellipselist.get(index)[2], ellipselist.get(index)[3],
							ellipselist.get(index)[4] };
					ellipseroi = GetDelta.createEllipse(mean, covar, 3);
		    		final double perimeter = ellipseroi.getLength();

		    	
		    			
		    			Roiindex = count;
		    			count++;
					//ellipseroi.setStrokeColor(Color.green);
					

					
					Cursor<FloatType> Actualsourcecursor = Views.iterable(source).localizingCursor();
					RandomAccess<FloatType> Actualranac = ActualRoiimg.randomAccess();
					while (Actualsourcecursor.hasNext()) {

						Actualsourcecursor.fwd();

						final int x = Actualsourcecursor.getIntPosition(0);
						final int y = Actualsourcecursor.getIntPosition(1);
						Actualranac.setPosition(Actualsourcecursor);
						if (ellipseroi.contains(x, y)) {
							
							Actualranac.get().set(Actualsourcecursor.get());

						}
						

					}
					
					
					final double maxextent = LargerEigenvalue(mean, covar);
					final double[] props = getProps(ellipseroi); 
					final double[] center = new double[]{props[0], props[1], props[2]};
					final double Intensitysource = props[3];
					final int Numberofpixels = (int)props[4];
					final double MeanIntensitysource = props[5];
					final double Circularity = props[6];
					final double Intensitytarget = props[7];
					final int NumberofpixelsTarget = (int)props[8];
					final double MeanIntensitytarget = props[9];
					final double Size = props[10];
					
					
					
					SnakeObject currentsnake = new SnakeObject(thirdDimension, fourthDimension, Roiindex, ellipseroi, center, Intensitysource,
							 Numberofpixels, MeanIntensitysource, Intensitytarget, NumberofpixelsTarget, MeanIntensitytarget, Circularity, Size);
					ProbBlobs.add(currentsnake);
		
		
		
		    		
				}
		
		
		
		
		
		return true;
	}
	
	public double[] getProps(Roi roi) {

		// 3 co-ordinates for COM 1 for TotalIntensity, 1 for Number of pixels, I for Mean Intensity, 1 for Circularity = 7, Same for target image : , 8 TotalIntensitytarget, 
		// 9 Mean Intensity target, 10 size of the Blob
		double[] center = new double[ 11 ];

		double Intensity = 0;
		double NumberofpixelsSource = 0;
		double NumberofpixelsTarget = 0;
		double IntensityTarget = 0;
		
		
		
		double SumX = 0;
		double SumY = 0;
		Cursor<FloatType> currentcursor = Views.iterable(source).localizingCursor();

		
		final double[] position = new double[ 2 ];
		
		while (currentcursor.hasNext()) {

			currentcursor.fwd();

			currentcursor.localize(position);

			int x = (int) position[0];
			int y = (int) position[1];

			if (roi.contains(x, y)) {

				
				
				NumberofpixelsSource++;
				SumX += currentcursor.getDoublePosition(0) * currentcursor.get().getRealDouble();
				SumY += currentcursor.getDoublePosition(1) * currentcursor.get().getRealDouble();
				Intensity += currentcursor.get().getRealDouble();
				
				
				
				
			}

		}
		center[ 0 ] = SumX / Intensity;
		center[ 1 ] = SumY / Intensity;
		
		center[ 2 ] = 1;
		
		int width = (int)roi.getBounds().getWidth();
		int height = (int)roi.getBounds().getHeight();
		
		final OvalRoi Bigroi = new OvalRoi(Util.round(center[0] -(width + sizeX)/2), Util.round(center[1] - (height + sizeY)/2 ), Util.round(width + sizeX),
				Util.round(height + sizeY));
		
       Cursor<FloatType> secondcurrentcursor = Views.iterable(source).localizingCursor();

		
		final double[] secposition = new double[ 2 ];
		RandomAccess<FloatType> targetran = target.randomAccess();
		
		
		while(secondcurrentcursor.hasNext()){
			
			secondcurrentcursor.fwd();
			
			secondcurrentcursor.localize(secposition);
			
			int x = (int) secposition[0];
			int y = (int) secposition[1];
			
			
			if (Bigroi.contains(x, y)){
				
				NumberofpixelsTarget++;
				targetran.setPosition(currentcursor);
				IntensityTarget += targetran.get().getRealDouble();
				
			}
			
			
			
		}
		
		
		
		
		
		center[ 3 ] = Intensity;
		center[ 4 ] = NumberofpixelsSource;
		center[ 5 ] = Intensity / NumberofpixelsSource;
		final double perimeter = roi.getLength();
		center[ 6 ] = 4 * Math.PI * NumberofpixelsSource / Math.pow(perimeter, 2);
		center[ 7 ] = IntensityTarget;
		center[ 8 ] = NumberofpixelsTarget;
		center[ 9 ] = IntensityTarget / NumberofpixelsTarget;
	    center[ 10 ] = roi.getCornerDiameter();
		
		
		

		return center;

	}
	
	
	

	public double[] getCentreofMass(Roi roi) {

		double[] center = new double[ 3 ];

		double Intensity = 0;
		double SumX = 0;
		double SumY = 0;
		Cursor<FloatType> currentcursor = Views.iterable(source).localizingCursor();

		final double[] position = new double[ 2 ];

		while (currentcursor.hasNext()) {

			currentcursor.fwd();

			currentcursor.localize(position);

			int x = (int) position[0];
			int y = (int) position[1];

			if (roi.contains(x, y)) {

				
				SumX += currentcursor.getDoublePosition(0) * currentcursor.get().getRealDouble();
				SumY += currentcursor.getDoublePosition(1) * currentcursor.get().getRealDouble();
				Intensity += currentcursor.get().getRealDouble();
			}

		}
		
		center[ 0 ] = SumX / Intensity;
		center[ 1 ] = SumY / Intensity;
		
		center[ 2 ] = 1;
		

		return center;

	}
	
	public double getIntensity(Roi roi) {


		double Intensity = 0;
	
		Cursor<FloatType> currentcursor = Views.iterable(source).localizingCursor();

		final double[] position = new double[ 2 ];

		while (currentcursor.hasNext()) {

			currentcursor.fwd();

			currentcursor.localize(position);

			int x = (int) position[0];
			int y = (int) position[1];

			if (roi.contains(x, y)) {

				
				
				Intensity += currentcursor.get().getRealDouble();
			}

		}
		
		
		

		return Intensity;

	}

	
	

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public void setLogger(Logger logger) {
		this.logger = logger;
		
	}
	public Roi getRoi(){
		
		return ellipseroi;
		
	}
	/**
	 * Returns the slope and the intercept of the line passing through the major axis of the ellipse
	 * 
	 * 
	 *@param mean
	 *            (x,y) components of mean vector
	 * @param cov
	 *            (xx, xy, yy) components of covariance matrix
	 * @return slope and intercept of the line along the major axis
	 */
	public  double[] LargestEigenvector( final double[] mean, final double[] cov){
		
		// For inifinite slope lines support is provided
		final double a = cov[0];
		final double b = cov[1];
		final double c = cov[2];
		final double d = Math.sqrt(a * a + 4 * b * b - 2 * a * c + c * c);
		final double[] eigenvector1 = {2 * b, c - a + d};
		double[] LargerVec = new double[eigenvector1.length + 1];

		LargerVec =  eigenvector1;
		
        final double slope = LargerVec[1] / (LargerVec[0] );
        final double intercept = mean[1] - mean[0] * slope;
       
        if (Math.abs(slope) != Double.POSITIVE_INFINITY){
        double[] pair = {slope, intercept};
        return pair;
      
        }
        else
        	return null;
       
        	 
       
		
	}
	
	
	/**
	 * Returns the smallest eigenvalue of the ellipse
	 * 
	 * 
	 *@param mean
	 *            (x,y) components of mean vector
	 * @param cov
	 *            (xx, xy, yy) components of covariance matrix
	 * @return slope and intercept of the line along the major axis
	 */
	public  double LargerEigenvalue( final double[] mean, final double[] cov){
		
		// For inifinite slope lines support is provided
		final double a = cov[0];
		final double b = cov[1];
		final double c = cov[2];
		final double d = Math.sqrt(a * a + 4 * b * b - 2 * a * c + c * c);

		
        final double largereigenvalue = (a + c + d) / 2;
       
        
        	
        	return largereigenvalue;
        	
        	 
       
		
	}
	
	/**
	 * Returns the smallest eigenvalue of the ellipse
	 * 
	 * 
	 *@param mean
	 *            (x,y) components of mean vector
	 * @param cov
	 *            (xx, xy, yy) components of covariance matrix
	 * @return slope and intercept of the line along the major axis
	 */
	public  double SmallerEigenvalue( final double[] mean, final double[] cov){
		
		// For inifinite slope lines support is provided
		final double a = cov[0];
		final double b = cov[1];
		final double c = cov[2];
		final double d = Math.sqrt(a * a + 4 * b * b - 2 * a * c + c * c);

		
        final double smalleigenvalue = (a + c - d) / 2;
       
        
        	
        	return smalleigenvalue;
        	
        	 
       
		
	}
	public double sqDistance(final double[] cordone, final double[] cordtwo) {

		double distance = 0;

		for (int d = 0; d < ndims; ++d) {

			distance += Math.pow((cordone[d] - cordtwo[d]), 2);

		}
		return (distance);
	}
}
