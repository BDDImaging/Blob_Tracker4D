package snakes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import ij.gui.Roi;
import net.imglib2.AbstractEuclideanSpace;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import trackerType.BlobTracker;

public class SnakeObject extends AbstractEuclideanSpace implements RealLocalizable, Comparable<SnakeObject> {

	
	/*
	 * FIELDS
	 */

	public static AtomicInteger IDcounter = new AtomicInteger( -1 );

	/** Store the individual features, and their values. */
	private final ConcurrentHashMap< String, Double > features = new ConcurrentHashMap< String, Double >();

	/** A user-supplied name for this spot. */
	private String name;

	/** This spot ID. */
	private final int ID;
	
	/**
	 * @param thirdDimension
	 *            the current frame
	 * 
	 * @param Label
	 *            the label of the blob
	 * @param ArrayList<Roi>
	 *            list of all the Rois found       
	 * @param centreofMass
	 *            the co-ordinates for center of mass of the current blobs
	 * @param IntensityROI
	 *            the total intensity of the blob in source image.
	 * @param IntensitySecROI
	 *            the total intensity of the blob in target image.
	 * @param meanIntensityROI
	 *            the mean intensity of the blob in source image.
	 * @param meanIntensitySecROI
	 *            the mean intensity of the blob in target image.  
	 * @param numberofPixels
	 *            the number of pixels in the ROI.
	 * @param Circularity
	 *            the Circularity measure of the blob.
	 * @param Size 
	 *            the Max size of the blob.                                  
	 *                 
	 *            
	 * 
	 */
	public final int thirdDimension;
	public int fourthDimension;
	public final int Label;
	public final Roi roi;
	public final ArrayList<Roi> Roi3d;
	public final double[] centreofMass;
	public final double IntensityROI;
	public final double IntensitySecROI;
	public final double meanIntensityROI;
	public final double meanIntensitySecROI;
	public final int numberofPixels;
	public final int numberofPixelsSecRoI;
	public final double Circularity;
	public final double Size;
	
	

	// Parameter for the cost function to decide how much weight to give to
	// Intensity and to distance
	/*
	 * CONSTRUCTORS
	 */

	/**
	 * @param thirdDimension
	 *            the current frame
	 * 
	 * @param Label
	 *            the label of the blob
	 * @param Roi
	 *            the current ROI found       
	 * @param centreofMass
	 *            the co-ordinates for center of mass of the current blobs
	 * @param IntensityROI
	 *            the total intensity of the blob in source image.
	 * @param IntensitySecROI
	 *            the total intensity of the blob in target image.
	 * @param meanIntensityROI
	 *            the mean intensity of the blob in source image.
	 * @param meanIntensitySecROI
	 *            the mean intensity of the blob in target image.  
	 * @param numberofPixels
	 *            the number of pixels in the ROI.
	 * @param Circularity
	 *            the Circularity measure of the blob.
	 * @param Size 
	 *            the Max size of the blob.                                  
	 *                 
	 *            
	 * 
	 */
	public SnakeObject( final int thirdDimension, final int fourthDimension, final int Label, final Roi roi, 
			final double[] centreofmass, final double IntensityROI, final int numberofPixels, final double meanIntensityROI,
			final double IntensitySecROI, final int NumberofpixelsSecRoI, 
			final double meanIntensitySecROI, final double Circularity, final double Size )
	{
		super( 3 );
		this.ID = IDcounter.incrementAndGet();
		putFeature( FRAME, Double.valueOf( thirdDimension ) );
		putFeature( LABEL, Double.valueOf( Label ) );
		putFeature( XPOSITION, Double.valueOf( centreofmass[0] ) );
		putFeature( YPOSITION, Double.valueOf( centreofmass[1] ) );
		putFeature( ZPOSITION, Double.valueOf( centreofmass[2] ) );
		
			this.name = "ID" + ID;
			this.thirdDimension = thirdDimension;
			this.fourthDimension = fourthDimension;
			this.Label = Label;
			this.roi = roi;
			this.Roi3d = null;
			this.centreofMass = centreofmass;
			this.IntensityROI = IntensityROI;
			this.numberofPixels = numberofPixels;
			this.meanIntensityROI = meanIntensityROI;
			this.IntensitySecROI = IntensitySecROI;
			this.numberofPixelsSecRoI = NumberofpixelsSecRoI;
			this.meanIntensitySecROI = meanIntensitySecROI;
			this.Circularity = Circularity;
            this.Size = Size;			
			
		
	}
	
	
	
	public SnakeObject( final int thirdDimension, final int fourthDimension, final int Label, 
			final double[] centreofmass, final double IntensityROI, final int numberofPixels, final double meanIntensityROI,
			final double IntensitySecROI, final int numberofPixelsSecRoI,  final double meanIntensitySecROI, final double Circularity, final double Size)
	{
		super( 3 );
		this.ID = IDcounter.incrementAndGet();
		putFeature( FRAME, Double.valueOf( thirdDimension ) );
		putFeature( LABEL, Double.valueOf( Label ) );
		putFeature( XPOSITION, Double.valueOf( centreofmass[0] ) );
		putFeature( YPOSITION, Double.valueOf( centreofmass[1] ) );
		putFeature( ZPOSITION, Double.valueOf( centreofmass[2] ) );
		
			this.name = "ID" + ID;
			this.thirdDimension = thirdDimension;
			this.fourthDimension = fourthDimension;
			this.Label = Label;
			this.Size = Size;
			this.roi = null;
			this.Roi3d = null;
			this.centreofMass = centreofmass;
			this.IntensityROI = IntensityROI;
			this.numberofPixels = numberofPixels;
			this.meanIntensityROI = meanIntensityROI;
			this.IntensitySecROI = IntensitySecROI;
			this.numberofPixelsSecRoI = numberofPixelsSecRoI;
			this.meanIntensitySecROI = meanIntensitySecROI;
			this.Circularity = Circularity;
		
	}
	

	
	public SnakeObject( final int thirdDimension, final int fourthDimension, final Integer Label, final ArrayList<Roi> Roi3d,
			final double[] centreofmass, final double IntensityROI, final int numberofPixels, final double meanIntensityROI,
			final double IntensitySecROI,  final int numberofPixelsSecRoI, final double meanIntensitySecROI,final double Size)
	{
		super( 3 );
		this.ID = IDcounter.incrementAndGet();
		putFeature( FRAME, Double.valueOf( thirdDimension ) );
		putFeature( LABEL, Double.valueOf( Label ) );
		putFeature( XPOSITION, Double.valueOf( centreofmass[0] ) );
		putFeature( YPOSITION, Double.valueOf( centreofmass[1] ) );
		putFeature( ZPOSITION, Double.valueOf( centreofmass[2] ) );
		
			this.name = "ID" + ID;
			this.thirdDimension = thirdDimension;
			this.fourthDimension = fourthDimension;
			this.Label = Label;
			this.Size = Size;
			this.roi = null;
			this.Roi3d = Roi3d;
			this.centreofMass = centreofmass;
			this.IntensityROI = IntensityROI;
			this.numberofPixels = numberofPixels;
			this.meanIntensityROI = meanIntensityROI;
			this.IntensitySecROI = IntensitySecROI;
			this.numberofPixelsSecRoI = numberofPixelsSecRoI;
			this.meanIntensitySecROI = meanIntensitySecROI;
			this.Circularity = 0;
		
	}
	

	



	/**
	 * Returns the squared distance between two blobs.
	 *
	 * @param target
	 *            the Blob to compare to.
	 *
	 * @return the distance to the current blob to target blob specified.
	 */

	public double squareDistanceTo(SnakeObject target) {
		// Returns squared distance between the source Blob and the target Blob.

		final double[] sourceLocation = centreofMass;
		final double[] targetLocation = target.centreofMass;

		double distance = 0;

		for (int d = 0; d < sourceLocation.length; ++d) {

			distance += (sourceLocation[d] - targetLocation[d]) * (sourceLocation[d] - targetLocation[d]);
		}

		return distance;
	}
	

	/**
	 * Returns the Normalized cost function based on squared distance between two blobs.
	 *
	 * @param target
	 *            the Blob to compare to.
	 *
	 * @return the Normalized distance to the current blob to target blob specified.
	 */

	public double NormalizedsquareDistanceTo(SnakeObject target) {
		// Returns squared distance between the source Blob and the target Blob.

		final double[] sourceLocation = centreofMass;
		final double[] targetLocation = target.centreofMass;

		double distance = 0;

		for (int d = 0; d < sourceLocation.length; ++d) {

			distance += (sourceLocation[d] - targetLocation[d]) * (sourceLocation[d] - targetLocation[d]);
		}
		
		double cost = distance / (1 + distance);
		

		return cost;
	}
	
	
	/**
	 * Returns the Noramlized cost function based on ratio of pixels between two blobs.
	 *
	 * @param target
	 *            the Blob to compare to.
	 *
	 * @return the ratio of pixels of the current blob to target blob specified.
	 */

	public double numberofPixelsRatioTo(SnakeObject target) {
		// Returns squared distance between the source Blob and the target Blob.

		final int sourcePixels = numberofPixels;
		final int targetPixels = target.numberofPixels;

		
		if (targetPixels > 0){
		double ratio = sourcePixels/ targetPixels;

		double sigma = 10; 
		double cost = 0;
		double coeff = 1 - Math.exp(-1/(4 * sigma));
		double a = -4*Math.log(coeff);
		
		
		if (ratio > 0  && ratio <= 0.5)
			cost = Math.exp(-a * ratio *ratio);
		if (ratio > 0.5 && ratio <= 1.5)
			cost = 1 - Math.exp(- (ratio - 1) *(ratio - 1)/ sigma);
		if (ratio > 1.5 && ratio <= 2)
			cost = Math.exp(-a * (ratio - 2)* (ratio - 2));
		else
			cost = 1;
		
		

		return cost;
		}
		
		else
			return 0;
	}
	
	
	/**
	 * Returns the Normalized combo cost function based on ratio of pixels b/w blobs and the Normalized square distances between two blobs.
	 *
	 * @param target
	 *            the Blob to compare to.
	 *
	 * @return the Normalized distance to the current blob to target blob specified.
	 */

	public double NormalizedPixelratioandDistanceTo(SnakeObject target, final double alpha, final double beta) {
		// Returns squared distance between the source Blob and the target Blob.

		final double[] sourceLocation = centreofMass;
		final double[] targetLocation = target.centreofMass;

		double distance = 0;

		for (int d = 0; d < sourceLocation.length; ++d) {

			distance += (sourceLocation[d] - targetLocation[d]) * (sourceLocation[d] - targetLocation[d]);
		}
		
		double cost = distance / (1 + distance);
		
		final int sourcePixels = numberofPixels;
		final int targetPixels = target.numberofPixels;

		if (targetPixels > 0){
		double ratio = sourcePixels/ targetPixels;

		double sigma = 10; 
		double ratiocost = 0;
		double coeff = 1 - Math.exp(-1/(4 * sigma));
		double a = -4*Math.log(coeff);
		
		
		if (ratio > 0  && ratio <= 0.5)
			ratiocost = Math.exp(-a * ratio * ratio);
		if (ratio > 0.5 && ratio <= 1.5)
			ratiocost = 1 - Math.exp(- (ratio - 1) * (ratio - 1)/ sigma);
		if (ratio > 1.5 && ratio <= 2)
			ratiocost = Math.exp(-a * (ratio - 2) * (ratio - 2));
		else
			ratiocost = 1;
		
		double combinedcost = (alpha * cost + beta * ratiocost) / (alpha + beta);
		

		return combinedcost;
		}
		
		else
			
			return 0;
	}
	

	
	
	private static final class ComparableRealPoint extends RealPoint implements Comparable<ComparableRealPoint> {
		public ComparableRealPoint(final double[] A) {
			// Wrap array.
			super(A, false);
		}

		/**
		 * Sort based on X, Y
		 */
		@Override
		public int compareTo(final ComparableRealPoint o) {
			int i = 0;
			while (i < n) {
				if (getDoublePosition(i) != o.getDoublePosition(i)) {
					return (int) Math.signum(getDoublePosition(i) - o.getDoublePosition(i));
				}
				i++;
			}
			return hashCode() - o.hashCode();
		}
	}

	
	
	
	
	public static Comparator<SnakeObject> Framecomparison = new Comparator<SnakeObject>(){
		
		@Override
       public int compare(final SnakeObject A, final SnakeObject B){
		
			int FramenumberA = A.thirdDimension;
			int FramenumberB = B.thirdDimension;
			
			if (FramenumberA > FramenumberB)
			
			return A.compareTo(B);
			
			else
			return B.compareTo(A);
		
	}

	
		
		
	};
	
	
   public static Comparator<SnakeObject> Slicecomparison = new Comparator<SnakeObject>(){
		
		@Override
       public int compare(final SnakeObject A, final SnakeObject B){
		
			int SlicenumberA = A.fourthDimension;
			int SlicenumberB = B.fourthDimension;
			
			if (SlicenumberA > SlicenumberB)
			
			return A.compareTo(B);
			
			else
			return B.compareTo(A);
		
	}

	
		
		
	};
	
	
	/**
	 * Returns the Intnesity weighted squared distance between two blobs.
	 *
	 * @param target
	 *            the Blob to compare to.
	 *
	 * @return the Intensity weighted distance to the current blob to target
	 *         blob specified.
	 */

	public double IntensityDistanceTo(SnakeObject target) {
		// Returns squared distance between the source Blob and the target Blob.

		

		double IntensityDistance = 1 -  Math.pow((IntensityROI / target.IntensityROI), 2);

		return IntensityDistance;
	}

	

	/**
	 * Returns the Intnesity weighted squared distance between two blobs.
	 *
	 * @param target
	 *            the Blob to compare to.
	 *
	 * @return the Intensity weighted distance to the current blob to target
	 *         blob specified.
	 */

	public double IntensityweightedsquareDistanceTo(SnakeObject target) {
		// Returns squared distance between the source Blob and the target Blob.

		final double[] sourceLocation = centreofMass;
		final double[] targetLocation = target.centreofMass;

		double distance = 0;

		for (int d = 0; d < sourceLocation.length; ++d) {

			distance += (sourceLocation[d] - targetLocation[d]) * (sourceLocation[d] - targetLocation[d]);
		}

		double IntensityweightedDistance = (distance) * Math.pow((IntensityROI / target.IntensityROI), 2);

		return IntensityweightedDistance;
	}

	/**
	 * Returns the difference between the location of two blobs, this operation
	 * returns ( <code>A.diffTo(B) = - B.diffTo(A)</code>)
	 *
	 * @param target
	 *            the Blob to compare to.
	 * @param int
	 *            n n = 0 for X- coordinate, n = 1 for Y- coordinate
	 * @return the difference in co-ordinate specified.
	 */
	public double diffTo(final SnakeObject target, int n) {

		final double thisBloblocation = centreofMass[n];
		final double targetBloblocation = target.centreofMass[n];
		return thisBloblocation - targetBloblocation;
	}

	/**
	 * Returns the difference between the Intensity of two blobs, this operation
	 * returns ( <code>A.diffTo(B) = - B.diffTo(A)</code>)
	 *
	 * @param target
	 *            the Blob to compare to.
	 * 
	 * @return the difference in Intensity of Blobs.
	 */
	public double IntensitydiffTo(final SnakeObject target) {
		final double thisBloblocation = IntensityROI;
		final double targetBloblocation = target.IntensityROI;
		return thisBloblocation - targetBloblocation;
	}

	
	
	
	
	@Override
	public int compareTo(SnakeObject o) {

		return hashCode() - o.hashCode();
	}

	@Override
	public void localize(float[] position) {
		int n = position.length;
		for (int d = 0; d < n; ++d)
			position[d] = getFloatPosition(d);

	}

	@Override
	public void localize(double[] position) {
		int n = position.length;
		for (int d = 0; d < n; ++d)
			position[d] = getDoublePosition(d);
	}

	@Override
	public float getFloatPosition(int d) {
		return (float) getDoublePosition(d);
	}

	@Override
	public double getDoublePosition(int d) {
		return getDoublePosition(d);
	}

	@Override
	public int numDimensions() {

		return centreofMass.length;
	}

	/*
	 * STATIC KEYS
	 */

	



	/** The name of the blob X position feature. */
	public static final String XPOSITION = "XPOSITION";

	/** The name of the blob Y position feature. */
	public static final String YPOSITION = "YPOSITION";
	
	/** The name of the blob Y position feature. */
	public static final String ZPOSITION = "ZPOSITION";
	
	
	/** The label of the blob position feature. */
	public static final String LABEL = "LABEL";

	/** The name of the frame feature. */
	public static final String FRAME = "FRAME";
	
	public final Double getFeature( final String feature )
	{
		return features.get( feature );
	}

	/**
	 * Stores the specified feature value for this spot.
	 *
	 * @param feature
	 *            the name of the feature to store, as a {@link String}.
	 * @param value
	 *            the value to store, as a {@link Double}. Using
	 *            <code>null</code> will have unpredicted outcomes.
	 */
	public final void putFeature( final String feature, final Double value )
	{
		features.put( feature, value );
	}
	
}
