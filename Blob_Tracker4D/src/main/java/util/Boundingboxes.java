package util;

import java.util.ArrayList;

import ij.gui.EllipseRoi;
import ij.gui.OvalRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.AllConnectedComponents;
import net.imglib2.algorithm.labeling.Watershed;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.labeling.DefaultROIStrategyFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingROIStrategy;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.Util;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;

@SuppressWarnings("deprecation")
public class Boundingboxes {

	
	
	public static long[] GetMaxcorners(RandomAccessibleInterval<IntType> inputimg, int label) {

		Cursor<IntType> intCursor = Views.iterable(inputimg).localizingCursor();
		int n = inputimg.numDimensions();
		long[] maxVal = { inputimg.min(0), inputimg.min(1) };

		while (intCursor.hasNext()) {
			intCursor.fwd();
			int i = intCursor.get().get();
			if (i == label) {

				for (int d = 0; d < n; ++d) {

					final long p = intCursor.getLongPosition(d);
					if (p > maxVal[d])
						maxVal[d] = p;

				}

			}
		}

		return maxVal;

	}
	
	public static Roi CreateBigRoi (final Roi currentroi, final RandomAccessibleInterval<FloatType> currentimg, final double radius){
		
		
		
		double width = currentroi.getFloatWidth();
		double height = currentroi.getFloatHeight();
		
		double[] center = getCenter(currentroi, currentimg);
		
	
			Roi Bigroi = new OvalRoi(Util.round(center[0] -(width + radius)/2), Util.round(center[1] - (height + radius)/2 ), Util.round(width + radius),
					Util.round(height + radius));
			
		      if(radius == 0 )
		    	  Bigroi = currentroi;
			
		        
		        return Bigroi;
		
		
	}
	
	public static double[] getCenter(Roi roi, final RandomAccessibleInterval<FloatType> source) {

		double Intensity = 0;
		double[] center = new double[3];
		Cursor<FloatType> currentcursor = Views.iterable(source).localizingCursor();
		double SumX = 0;
		double SumY = 0;
		final double[] position = new double[source.numDimensions()];
		int count = 0;
		while (currentcursor.hasNext()) {

			currentcursor.fwd();

			currentcursor.localize(position);

			int x = (int) position[0];
			int y = (int) position[1];

			if (roi.contains(x, y)) {
				SumX += currentcursor.getDoublePosition(0) * currentcursor.get().getRealDouble();
				SumY += currentcursor.getDoublePosition(1) * currentcursor.get().getRealDouble();
				Intensity += currentcursor.get().getRealDouble();
                 count++;
			}

		}
		center[0] = SumX / Intensity;
		center[1] = SumY / Intensity;

		center[2] = 0;

		return center;

	}
	public static long[] GetMincorners(RandomAccessibleInterval<IntType> inputimg, int label) {

		Cursor<IntType> intCursor = Views.iterable(inputimg).localizingCursor();
		int n = inputimg.numDimensions();
		long[] minVal = { inputimg.max(0), inputimg.max(1) };
		while (intCursor.hasNext()) {
			intCursor.fwd();
			int i = intCursor.get().get();
			if (i == label) {

				for (int d = 0; d < n; ++d) {

					final long p = intCursor.getLongPosition(d);
					if (p < minVal[d])
						minVal[d] = p;
				}

			}
		}

		return minVal;

	}

	public static double GetBoundingbox(RandomAccessibleInterval<IntType> inputimg, int label) {

		Cursor<IntType> intCursor = Views.iterable(inputimg).localizingCursor();
		int n = inputimg.numDimensions();
		long[] position = new long[n];
		long[] minVal = { inputimg.max(0), inputimg.max(1) };
		long[] maxVal = { inputimg.min(0), inputimg.min(1) };

		while (intCursor.hasNext()) {
			intCursor.fwd();
			int i = intCursor.get().get();
			if (i == label) {

				intCursor.localize(position);
				for (int d = 0; d < n; ++d) {
					if (position[d] < minVal[d]) {
						minVal[d] = position[d];
					}
					if (position[d] > maxVal[d]) {
						maxVal[d] = position[d];
					}

				}

			}
		}

		double boxsize = Distance(minVal, maxVal);

		Pair<long[], long[]> boundingBox = new ValuePair<long[], long[]>(minVal, maxVal);
		return boxsize;
	}

	public static int GetMaxlabelsseeded(RandomAccessibleInterval<IntType> intimg) {

		// To get maximum Labels on the image
		Cursor<IntType> intCursor = Views.iterable(intimg).cursor();
		int currentLabel = 1;
		boolean anythingFound = true;
		while (anythingFound) {
			anythingFound = false;
			intCursor.reset();
			while (intCursor.hasNext()) {
				intCursor.fwd();
				int i = intCursor.get().get();
				if (i == currentLabel) {

					anythingFound = true;

				}
			}
			currentLabel++;
		}

		return currentLabel;

	}

	public static NativeImgLabeling<Integer, IntType> GetlabeledImage(RandomAccessibleInterval<FloatType> inputimg,
			NativeImgLabeling<Integer, IntType> seedLabeling) {

		int n = inputimg.numDimensions();
		long[] dimensions = new long[n];

		for (int d = 0; d < n; ++d)
			dimensions[d] = inputimg.dimension(d);
		final NativeImgLabeling<Integer, IntType> outputLabeling = new NativeImgLabeling<Integer, IntType>(
				new ArrayImgFactory<IntType>().create(inputimg, new IntType()));

		final Watershed<FloatType, Integer> watershed = new Watershed<FloatType, Integer>();

		watershed.setSeeds(seedLabeling);
		watershed.setIntensityImage(inputimg);
		watershed.setStructuringElement(AllConnectedComponents.getStructuringElement(2));
		watershed.setOutputLabeling(outputLabeling);
		watershed.process();
		DefaultROIStrategyFactory<Integer> deffactory = new DefaultROIStrategyFactory<Integer>();
		LabelingROIStrategy<Integer, Labeling<Integer>> factory = deffactory
				.createLabelingROIStrategy(watershed.getResult());
		outputLabeling.setLabelingCursorStrategy(factory);

		return outputLabeling;

	}

	public static Pair<RandomAccessibleInterval<FloatType>, FinalInterval>  CurrentLabelImage(RandomAccessibleInterval<FloatType> img, EllipseRoi roi){
		
		int n = img.numDimensions();
		long[] position = new long[n];
		long[] minVal = { img.max(0), img.max(1) };
		long[] maxVal = { img.min(0), img.min(1) };
		
		Cursor<FloatType> localcursor = Views.iterable(img).localizingCursor();
		
		while (localcursor.hasNext()) {
			localcursor.fwd();
			int x = localcursor.getIntPosition(0);
			int y = localcursor.getIntPosition(1);
			if (roi.contains(x, y)){

				localcursor.localize(position);
				for (int d = 0; d < n; ++d) {
					if (position[d] < minVal[d]) {
						minVal[d] = position[d];
					}
					if (position[d] > maxVal[d]) {
						maxVal[d] = position[d];
					}

				}
				
			}
		}
		FinalInterval interval = new FinalInterval(minVal, maxVal);
		RandomAccessibleInterval<FloatType> currentimgsmall = Views.interval(img, interval);
		
		Pair<RandomAccessibleInterval<FloatType>, FinalInterval> pair = new ValuePair<RandomAccessibleInterval<FloatType>, FinalInterval>(currentimgsmall, interval);
		
		return pair;
	}
	
	
	
	
/*	
public static RandomAccessibleInterval<FloatType> CurrentLabeloffsetImage(ArrayList<CommonOutput> imgs, final EllipseRoi roi,int label) {
		
		RandomAccessibleInterval<FloatType> currentimg = imgs.get(label).Actualroi;
		int n = currentimg.numDimensions();
		long[] position = new long[n];
		long[] minVal = { currentimg.max(0), currentimg.max(1) };
		long[] maxVal = { currentimg.min(0), currentimg.min(1) };
		
		Cursor<FloatType> localcursor = Views.iterable(currentimg).localizingCursor();

		while (localcursor.hasNext()) {
			localcursor.fwd();
			int x = localcursor.getIntPosition(0);
			int y = localcursor.getIntPosition(1);
			if (roi.contains(x, y)){

				localcursor.localize(position);
				for (int d = 0; d < n; ++d) {
					if (position[d] < minVal[d]) {
						minVal[d] = position[d];
					}
					if (position[d] > maxVal[d]) {
						maxVal[d] = position[d];
					}

				}
				
			}
		}
		
		FinalInterval interval = new FinalInterval(minVal, maxVal) ;
		RandomAccessibleInterval<FloatType> currentimgsmall = Views.offsetInterval(currentimg, interval);
		return currentimgsmall;
		
	}
*/

public static FinalInterval  CurrentroiInterval(RandomAccessibleInterval<FloatType> currentimg, final  EllipseRoi roi){
	
	int n = currentimg.numDimensions();
	long[] position = new long[n];
	long[] minVal = { currentimg.max(0), currentimg.max(1) };
	long[] maxVal = { currentimg.min(0), currentimg.min(1) };
	
	Cursor<FloatType> localcursor = Views.iterable(currentimg).localizingCursor();

	while (localcursor.hasNext()) {
		localcursor.fwd();
		int x = localcursor.getIntPosition(0);
		int y = localcursor.getIntPosition(1);
		if (roi.contains(x, y)){

			localcursor.localize(position);
			for (int d = 0; d < n; ++d) {
				if (position[d] < minVal[d]) {
					minVal[d] = position[d];
				}
				if (position[d] > maxVal[d]) {
					maxVal[d] = position[d];
				}

			}
			
		}
	}
	
	FinalInterval interval = new FinalInterval(minVal, maxVal) ;
	
	return interval;
	
}

public static FinalInterval CurrentroiInterval(RandomAccessibleInterval<FloatType> currentimg, Roi roi) {
	int n = currentimg.numDimensions();
	long[] position = new long[n];
	long[] minVal = { currentimg.max(0), currentimg.max(1) };
	long[] maxVal = { currentimg.min(0), currentimg.min(1) };
	
	Cursor<FloatType> localcursor = Views.iterable(currentimg).localizingCursor();

	while (localcursor.hasNext()) {
		localcursor.fwd();
		int x = localcursor.getIntPosition(0);
		int y = localcursor.getIntPosition(1);
		if (roi.contains(x, y)){

			localcursor.localize(position);
			for (int d = 0; d < n; ++d) {
				if (position[d] < minVal[d]) {
					minVal[d] = position[d];
				}
				if (position[d] > maxVal[d]) {
					maxVal[d] = position[d];
				}

			}
			
		}
	}
	
	FinalInterval interval = new FinalInterval(minVal, maxVal) ;
	
	return interval;
}
	public static Pair<RandomAccessibleInterval<FloatType>, FinalInterval> CurrentLabeloffsetImagepair(RandomAccessibleInterval<IntType> Intimg,
			RandomAccessibleInterval<FloatType> originalimg, int currentLabel) {
		int n = originalimg.numDimensions();
		RandomAccess<FloatType> inputRA = originalimg.randomAccess();
		long[] position = new long[n];
		Cursor<IntType> intCursor = Views.iterable(Intimg).cursor();
		final FloatType type = originalimg.randomAccess().get().createVariable();
		final ImgFactory<FloatType> factory = Util.getArrayOrCellImgFactory(originalimg, type);
		RandomAccessibleInterval<FloatType> outimg = factory.create(originalimg, type);
		RandomAccess<FloatType> imageRA = outimg.randomAccess();
		long[] minVal = { originalimg.max(0), originalimg.max(1) };
		long[] maxVal = { originalimg.min(0), originalimg.min(1) };
		// Go through the whole image and add every pixel, that belongs to
		// the currently processed label

		while (intCursor.hasNext()) {
			intCursor.fwd();
			inputRA.setPosition(intCursor);
			imageRA.setPosition(inputRA);
			int i = intCursor.get().get();
			if (i == currentLabel) {
				intCursor.localize(position);
				for (int d = 0; d < n; ++d) {
					if (position[d] < minVal[d]) {
						minVal[d] = position[d];
					}
					if (position[d] > maxVal[d]) {
						maxVal[d] = position[d];
					}

				}
				imageRA.get().set(inputRA.get());

			}

		}
		FinalInterval intervalsmall = new FinalInterval(minVal, maxVal) ;

		RandomAccessibleInterval<FloatType> outimgsmall = Views.offsetInterval(outimg, intervalsmall);

		Pair<RandomAccessibleInterval<FloatType>, FinalInterval> pair = new ValuePair<RandomAccessibleInterval<FloatType>, FinalInterval>(outimgsmall, intervalsmall);
		return pair;

	}

	
	public static Pair<RandomAccessibleInterval<FloatType>, FinalInterval> CurrentLabelImagepair(RandomAccessibleInterval<IntType> Intimg,
			RandomAccessibleInterval<FloatType> originalimg, int currentLabel) {
		int n = originalimg.numDimensions();
		RandomAccess<FloatType> inputRA = originalimg.randomAccess();
		long[] position = new long[n];
		Cursor<IntType> intCursor = Views.iterable(Intimg).cursor();
		final FloatType type = originalimg.randomAccess().get().createVariable();
		final ImgFactory<FloatType> factory = Util.getArrayOrCellImgFactory(originalimg, type);
		RandomAccessibleInterval<FloatType> outimg = factory.create(originalimg, type);
		RandomAccess<FloatType> imageRA = outimg.randomAccess();
		long[] minVal = { originalimg.max(0), originalimg.max(1) };
		long[] maxVal = { originalimg.min(0), originalimg.min(1) };
		// Go through the whole image and add every pixel, that belongs to
		// the currently processed label

		while (intCursor.hasNext()) {
			intCursor.fwd();
			inputRA.setPosition(intCursor);
			imageRA.setPosition(inputRA);
			int i = intCursor.get().get();
			if (i == currentLabel) {
				intCursor.localize(position);
				for (int d = 0; d < n; ++d) {
					if (position[d] < minVal[d]) {
						minVal[d] = position[d];
					}
					if (position[d] > maxVal[d]) {
						maxVal[d] = position[d];
					}

				}
				imageRA.get().set(inputRA.get());

			}

		}
		FinalInterval intervalsmall = new FinalInterval(minVal, maxVal) ;
		RandomAccessibleInterval<FloatType> outimgsmall = Views.interval(outimg, intervalsmall);

		Pair<RandomAccessibleInterval<FloatType>, FinalInterval> pair = new ValuePair<RandomAccessibleInterval<FloatType>, FinalInterval>(outimgsmall, intervalsmall);
		return pair;

	}
	
	public static RandomAccessibleInterval<FloatType> CurrentLabelImage(RandomAccessibleInterval<IntType> Intimg,
			RandomAccessibleInterval<FloatType> originalimg, int currentLabel) {
		int n = originalimg.numDimensions();
		RandomAccess<FloatType> inputRA = originalimg.randomAccess();
		long[] position = new long[n];
		Cursor<IntType> intCursor = Views.iterable(Intimg).cursor();
		final FloatType type = originalimg.randomAccess().get().createVariable();
		final ImgFactory<FloatType> factory = Util.getArrayOrCellImgFactory(originalimg, type);
		RandomAccessibleInterval<FloatType> outimg = factory.create(originalimg, type);
		RandomAccess<FloatType> imageRA = outimg.randomAccess();

		// Go through the whole image and add every pixel, that belongs to
		// the currently processed label
		
		while (intCursor.hasNext()) {
			intCursor.fwd();
			inputRA.setPosition(intCursor);
			imageRA.setPosition(inputRA);
			int i = intCursor.get().get();
			if (i == currentLabel) {
				intCursor.localize(position);
				
				imageRA.get().set(inputRA.get());

			}

		}
	

		return outimg;

	}
	
	public static RandomAccessibleInterval<FloatType> CurrentLabeloffsetImage(RandomAccessibleInterval<IntType> Intimg,
			RandomAccessibleInterval<FloatType> originalimg, int currentLabel) {
		int n = originalimg.numDimensions();
		RandomAccess<FloatType> inputRA = originalimg.randomAccess();
		Cursor<IntType> intCursor = Views.iterable(Intimg).cursor();
		final FloatType type = originalimg.randomAccess().get().createVariable();
		final ImgFactory<FloatType> factory = Util.getArrayOrCellImgFactory(originalimg, type);
		RandomAccessibleInterval<FloatType> outimg = factory.create(originalimg, type);
		RandomAccess<FloatType> imageRA = outimg.randomAccess();
	
		// Go through the whole image and add every pixel, that belongs to
		// the currently processed label

		while (intCursor.hasNext()) {
			intCursor.fwd();
			inputRA.setPosition(intCursor);
			imageRA.setPosition(inputRA);
			int i = intCursor.get().get();
			if (i == currentLabel) {
				
				imageRA.get().set(inputRA.get());

			}

		}
		

		return outimg;

	}
	
	
	public static double Distance(final long[] minCorner, final long[] maxCorner) {

		double distance = 0;

		for (int d = 0; d < minCorner.length; ++d) {

			distance += Math.pow((minCorner[d] - maxCorner[d]), 2);

		}
		return Math.sqrt(distance);
	}
	
	
	
	public static int CummulativeDistance (final double[] pointT, final double[] pointTp1, final double[] pointTp2,   final double oldlength){
		
		int grow = 0;

		
		
		if ((pointTp2[0] < pointTp1[0]) && (pointTp2[0] > pointT[0]) && (pointTp2[0] < pointTp1[0]) && (pointTp2[0] > pointT[0])  )
			grow = -1;
		
		
		
			
			
		return grow;
		
		
	}
	
	public static float Distancesq(final double[] minCorner, final double[] maxCorner) {

		float distance = 0;

		for (int d = 0; d < minCorner.length; ++d) {

			distance += Math.pow((minCorner[d] - maxCorner[d]), 2);

		}
		return distance;
	}
	
	public static double Distance(final double[] minCorner, final double[] maxCorner) {

		double distance = 0;

		for (int d = 0; d < minCorner.length; ++d) {

			distance += Math.pow((minCorner[d] - maxCorner[d]), 2);

		}
		return Math.sqrt(distance);
	}
	
	public static double VelocityX(final double[] oldpoint, final double[] newpoint) {

		double Velocity = 0;

		int d = 0;

			Velocity = (-oldpoint[d] + newpoint[d]);

		
		return Velocity;
	}
	
	public static double VelocityY(final double[] oldpoint, final double[] newpoint) {

		double Velocity = 0;

		int d = oldpoint.length - 1;

			Velocity = (-oldpoint[d] + newpoint[d]);

		
		return Velocity;
	}

	
}