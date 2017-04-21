package util;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.jgrapht.graph.DefaultWeightedEdge;

import blobfinder.SortListbyproperty;
import ij.ImagePlus;
import ij.gui.EllipseRoi;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import kdTreeBlobs.FlagNode;
import kdTreeBlobs.NNFlagsearchKDtree;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.util.Util;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.IterableInterval;
import net.imglib2.KDTree;
import net.imglib2.Point;
import net.imglib2.PointSampleList;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.componenttree.mser.Mser;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.algorithm.localextrema.RefinedPeak;
import net.imglib2.algorithm.stats.Normalize;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.RealSum;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import snakes.SnakeObject;

public class FindersUtils {

	public static ArrayList<Roi> getcurrentRois(ArrayList<RefinedPeak<Point>> peaks, double sigma, double sigma2) {

		ArrayList<Roi> Allrois = new ArrayList<Roi>();

		for (final RefinedPeak<Point> peak : peaks) {
			float x = (float) (peak.getFloatPosition(0));
			float y = (float) (peak.getFloatPosition(1));

			final OvalRoi or = new OvalRoi(Util.round(x - sigma), Util.round(y - sigma), Util.round(sigma + sigma2),
					Util.round(sigma + sigma2));

			Allrois.add(or);

		}

		return Allrois;

	}

	public static double getNumberofPixels(RandomAccessibleInterval<FloatType> source, Roi roi) {

		double NumberofPixels = 0;

		Cursor<FloatType> currentcursor = Views.iterable(source).localizingCursor();

		final double[] position = new double[source.numDimensions()];

		while (currentcursor.hasNext()) {

			currentcursor.fwd();

			currentcursor.localize(position);

			int x = (int) position[0];
			int y = (int) position[1];

			if (roi.contains(x, y)) {

				NumberofPixels++;

			}

		}

		return NumberofPixels;

	}

	public static ArrayList<Roi> getKNearestRois(final RandomAccessibleInterval<FloatType> currentimg,
			ArrayList<Roi> Allrois, Roi kdtreeroi, int k) {

		ArrayList<Roi> KnearestRoi = new ArrayList<Roi>();
		Roi Knear = null;

		ArrayList<Pair<Double, Roi>> distRoi = new ArrayList<Pair<Double, Roi>>();
		double[] kdcenter = util.FindersUtils.getCenter(currentimg, kdtreeroi);

		

			for (int index = 0; index < Allrois.size(); ++index) {

				double[] roicenter = util.FindersUtils.getCenter(currentimg, Allrois.get(index));

				Pair<Double, Roi> distpair = new ValuePair<Double, Roi>(util.Boundingboxes.Distance(kdcenter, roicenter), Allrois.get(index));
				
				distRoi.add(distpair);

			}
			
			Comparator<Pair<Double, Roi>> distcomparison = new Comparator<Pair<Double, Roi>>() {

				@Override
				public int compare(final Pair<Double, Roi> A, final Pair<Double, Roi> B) {

					double diff= A.getA() - B.getA();
					
					if ( diff < 0 )
						return -1;
					else if ( diff > 0 )
						return 1;
					else
						return 0;
				}

			};
			
			Collections.sort(distRoi, distcomparison);
			
			for (int i = 0; i < k ; ++i){
				
				KnearestRoi.add(distRoi.get(i).getB());
				
			}
			
		

		return KnearestRoi;

	}

	public static Roi getNearestRois(ArrayList<Roi> Allrois, ArrayList<double[]> Clickedpoint) {

		Roi KDtreeroi = null;

		final List<RealPoint> targetCoords = new ArrayList<RealPoint>(Allrois.size());
		final List<FlagNode<Roi>> targetNodes = new ArrayList<FlagNode<Roi>>(Allrois.size());
		Iterator<double[]> baseobjectiterator = Clickedpoint.iterator();
		for (int index = 0; index < Allrois.size(); ++index) {

			 Roi r = Allrois.get(index);
			 Rectangle rect = r.getBounds();
			 
			 targetCoords.add( new RealPoint(rect.x + rect.width/2.0, rect.y + rect.height/2.0 ) );
			 
			//targetCoords.add(new RealPoint(util.FindersUtils.getCenter(currentimg, Allrois.get(index))));

			targetNodes.add(new FlagNode<Roi>(Allrois.get(index)));

		}

		if (targetNodes.size() > 0 && targetCoords.size() > 0) {

			final KDTree<FlagNode<Roi>> Tree = new KDTree<FlagNode<Roi>>(targetNodes, targetCoords);

			final NNFlagsearchKDtree<Roi> Search = new NNFlagsearchKDtree<Roi>(Tree);

			while (baseobjectiterator.hasNext()) {

				final double[] source = baseobjectiterator.next();
				final RealPoint sourceCoords = new RealPoint(source);
				Search.search(sourceCoords);
				final FlagNode<Roi> targetNode = Search.getSampler().get();

				KDtreeroi = targetNode.getValue();
			}

		}

		return KDtreeroi;
	}

	public static double getIntensity(RandomAccessibleInterval<FloatType> source, Roi roi) {

		double Intensity = 0;

		Cursor<FloatType> currentcursor = Views.iterable(source).localizingCursor();

		final double[] position = new double[source.numDimensions()];

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

	public static double[] getCenter(RandomAccessibleInterval<FloatType> source, Roi roi) {

		double Intensity = 0;
		double[] center = new double[3];
		Cursor<FloatType> currentcursor = Views.iterable(source).localizingCursor();
		double SumX = 0;
		double SumY = 0;
		final double[] position = new double[source.numDimensions()];

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
		center[0] = SumX / Intensity;
		center[1] = SumY / Intensity;

		center[2] = 0;

		return center;

	}

	public static ArrayList<Roi> getcurrentRois(MserTree<UnsignedByteType> newtree) {

		final HashSet<Mser<UnsignedByteType>> rootset = newtree.roots();

		ArrayList<Roi> Allrois = new ArrayList<Roi>();
		final Iterator<Mser<UnsignedByteType>> rootsetiterator = rootset.iterator();

		ArrayList<double[]> AllmeanCovar = new ArrayList<double[]>();

		while (rootsetiterator.hasNext()) {

			Mser<UnsignedByteType> rootmser = rootsetiterator.next();

			if (rootmser.size() > 0) {

				final double[] meanandcov = { rootmser.mean()[0], rootmser.mean()[1], rootmser.cov()[0],
						rootmser.cov()[1], rootmser.cov()[2] };
				AllmeanCovar.add(meanandcov);

			}
		}

		// We do this so the ROI remains attached the the same label and is not
		// changed if the program is run again
		SortListbyproperty.sortpointList(AllmeanCovar);
		for (int index = 0; index < AllmeanCovar.size(); ++index) {

			final double[] mean = { AllmeanCovar.get(index)[0], AllmeanCovar.get(index)[1] };
			final double[] covar = { AllmeanCovar.get(index)[2], AllmeanCovar.get(index)[3],
					AllmeanCovar.get(index)[4] };

			EllipseRoi roi = createEllipse(mean, covar, 3);
			Allrois.add(roi);

		}

		return Allrois;

	}

	public static RandomAccessibleInterval<FloatType> getCurrentView(RandomAccessibleInterval<FloatType> originalimgA,
			int thirdDimension) {

		final FloatType type = originalimgA.randomAccess().get().createVariable();
		long[] dim = { originalimgA.dimension(0), originalimgA.dimension(1) };
		final ImgFactory<FloatType> factory = net.imglib2.util.Util.getArrayOrCellImgFactory(originalimgA, type);
		RandomAccessibleInterval<FloatType> totalimg = factory.create(dim, type);

		totalimg = Views.hyperSlice(originalimgA, 2, thirdDimension - 1);

		return totalimg;

	}

	/**
	 * Extract the current 2d region of interest from the souce image
	 * 
	 * @param CurrentView
	 *            - the CurrentView image, a {@link Image} which is a copy of
	 *            the {@link ImagePlus}
	 * 
	 * @return
	 */

	public static RandomAccessibleInterval<FloatType> extractImage(
			final RandomAccessibleInterval<FloatType> intervalView, FinalInterval interval) {

		final FloatType type = intervalView.randomAccess().get().createVariable();
		final ImgFactory<FloatType> factory = net.imglib2.util.Util.getArrayOrCellImgFactory(intervalView, type);
		RandomAccessibleInterval<FloatType> totalimg = factory.create(intervalView, type);

		final RandomAccessibleInterval<FloatType> img = Views.interval(intervalView, interval);
		totalimg = Views.interval(Views.extendBorder(img), intervalView);

		return totalimg;
	}

	/**
	 * Generic, type-agnostic method to create an identical copy of an Img
	 *
	 * @param currentPreprocessedimg2
	 *            - the Img to copy
	 * @return - the copy of the Img
	 */
	public static Img<UnsignedByteType> copytoByteImage(final RandomAccessibleInterval<FloatType> input) {
		// create a new Image with the same properties
		// note that the input provides the size for the new image as it
		// implements
		// the Interval interface
		RandomAccessibleInterval<FloatType> inputcopy = copyImage(input);
		Normalize.normalize(Views.iterable(inputcopy), new FloatType(0), new FloatType(255));
		final UnsignedByteType type = new UnsignedByteType();
		final ImgFactory<UnsignedByteType> factory = net.imglib2.util.Util.getArrayOrCellImgFactory(inputcopy, type);
		final Img<UnsignedByteType> output = factory.create(inputcopy, type);
		// create a cursor for both images
		RandomAccess<FloatType> ranac = inputcopy.randomAccess();
		Cursor<UnsignedByteType> cursorOutput = output.cursor();

		// iterate over the input
		while (cursorOutput.hasNext()) {
			// move both cursors forward by one pixel
			cursorOutput.fwd();

			ranac.setPosition(cursorOutput);

			// set the value of this pixel of the output image to the same as
			// the input,
			// every Type supports T.set( T type )
			cursorOutput.get().set((int) ranac.get().get());
		}

		// return the copy
		return output;
	}

	public static ArrayList<double[]> getRoiMean(MserTree<UnsignedByteType> newtree) {

		final HashSet<Mser<UnsignedByteType>> rootset = newtree.roots();

		final Iterator<Mser<UnsignedByteType>> rootsetiterator = rootset.iterator();

		ArrayList<double[]> AllmeanCovar = new ArrayList<double[]>();

		while (rootsetiterator.hasNext()) {

			Mser<UnsignedByteType> rootmser = rootsetiterator.next();

			if (rootmser.size() > 0) {

				final double[] meanandcov = { rootmser.mean()[0], rootmser.mean()[1] };
				AllmeanCovar.add(meanandcov);

			}
		}

		// We do this so the ROI remains attached the the same label and is not
		// changed if the program is run again
		SortListbyproperty.sortpointList(AllmeanCovar);

		return AllmeanCovar;

	}

	public static Img<FloatType> copyImage(final RandomAccessibleInterval<FloatType> input) {
		// create a new Image with the same dimensions but the other imgFactory
		// note that the input provides the size for the new image by
		// implementing the Interval interface
		Img<FloatType> output = new ArrayImgFactory<FloatType>().create(input, Views.iterable(input).firstElement());

		// create a cursor that automatically localizes itself on every move
		Cursor<FloatType> cursorInput = Views.iterable(input).localizingCursor();
		RandomAccess<FloatType> randomAccess = output.randomAccess();

		// iterate over the input cursor
		while (cursorInput.hasNext()) {
			// move input cursor forward
			cursorInput.fwd();

			// set the output cursor to the position of the input cursor
			randomAccess.setPosition(cursorInput);

			// set the value of this pixel of the output image, every Type
			// supports T.set( T type )
			randomAccess.get().set(cursorInput.get());
		}

		// return the copy
		return output;
	}

	public static Float AutomaticThresholding(RandomAccessibleInterval<FloatType> inputimg) {

		FloatType max = new FloatType();
		FloatType min = new FloatType();
		RandomAccessibleInterval<FloatType> inputimgcopy = copy(inputimg);
		Float ThresholdNew, Thresholdupdate;

		max = computeMaxIntensity(inputimgcopy);
		min = computeMinIntensity(inputimgcopy);

		ThresholdNew = (max.get() - min.get()) / 2;

		// Get the new threshold value after segmenting the inputimage with
		// thresholdnew
		Thresholdupdate = SegmentbyThresholding(Views.iterable(inputimgcopy), ThresholdNew);

		while (true) {

			ThresholdNew = SegmentbyThresholding(Views.iterable(inputimgcopy), Thresholdupdate);

			// Check if the new threshold value is close to the previous value
			if (Math.abs(Thresholdupdate - ThresholdNew) < 1.0E-2)
				break;
			Thresholdupdate = ThresholdNew;
		}

		return ThresholdNew;

	}

	public static Img<FloatType> copy(final RandomAccessibleInterval<FloatType> input) {
		// create a new Image with the same dimensions but the other imgFactory
		// note that the input provides the size for the new image by
		// implementing the Interval interface
		Img<FloatType> output = new ArrayImgFactory<FloatType>().create(input, Views.iterable(input).firstElement());

		// create a cursor that automatically localizes itself on every move
		Cursor<FloatType> cursorInput = Views.iterable(input).localizingCursor();
		RandomAccess<FloatType> randomAccess = output.randomAccess();

		// iterate over the input cursor
		while (cursorInput.hasNext()) {
			// move input cursor forward
			cursorInput.fwd();

			// set the output cursor to the position of the input cursor
			randomAccess.setPosition(cursorInput);

			// set the value of this pixel of the output image, every Type
			// supports T.set( T type )
			randomAccess.get().set(cursorInput.get());
		}

		// return the copy
		return output;
	}

	public static FloatType computeMaxIntensity(final RandomAccessibleInterval<FloatType> inputimg) {
		// create a cursor for the image (the order does not matter)
		final Cursor<FloatType> cursor = Views.iterable(inputimg).cursor();

		// initialize min and max with the first image value
		FloatType type = cursor.next();
		FloatType max = type.copy();

		// loop over the rest of the data and determine min and max value
		while (cursor.hasNext()) {
			// we need this type more than once
			type = cursor.next();

			if (type.compareTo(max) > 0) {
				max.set(type);

			}
		}

		return max;
	}

	public static FloatType computeMinIntensity(final RandomAccessibleInterval<FloatType> inputimg) {
		// create a cursor for the image (the order does not matter)
		final Cursor<FloatType> cursor = Views.iterable(inputimg).cursor();

		// initialize min and max with the first image value
		FloatType type = cursor.next();
		FloatType min = type.copy();

		// loop over the rest of the data and determine min and max value
		while (cursor.hasNext()) {
			// we need this type more than once
			type = cursor.next();

			if (type.compareTo(min) < 0) {
				min.set(type);

			}
		}

		return min;
	}

	// Segment image by thresholding, used to determine automatic thresholding
	// level
	public static Float SegmentbyThresholding(IterableInterval<FloatType> inputimg, Float Threshold) {

		int n = inputimg.numDimensions();
		Float ThresholdNew;
		PointSampleList<FloatType> listA = new PointSampleList<FloatType>(n);
		PointSampleList<FloatType> listB = new PointSampleList<FloatType>(n);
		Cursor<FloatType> cursor = inputimg.localizingCursor();
		while (cursor.hasNext()) {
			cursor.fwd();

			if (cursor.get().get() < Threshold) {
				Point newpointA = new Point(n);
				newpointA.setPosition(cursor);
				listA.add(newpointA, cursor.get().copy());
			} else {
				Point newpointB = new Point(n);
				newpointB.setPosition(cursor);
				listB.add(newpointB, cursor.get().copy());
			}
		}
		final RealSum realSumA = new RealSum();
		long countA = 0;

		for (final FloatType type : listA) {
			realSumA.add(type.getRealDouble());
			++countA;
		}

		final double sumA = realSumA.getSum() / countA;

		final RealSum realSumB = new RealSum();
		long countB = 0;

		for (final FloatType type : listB) {
			realSumB.add(type.getRealDouble());
			++countB;
		}

		final double sumB = realSumB.getSum() / countB;

		ThresholdNew = (float) (sumA + sumB) / 2;

		return ThresholdNew;

	}

	/**
	 * 2D correlated Gaussian
	 * 
	 * @param mean
	 *            (x,y) components of mean vector
	 * @param cov
	 *            (xx, xy, yy) components of covariance matrix
	 * @return ImageJ roi
	 */
	public static EllipseRoi createEllipse(final double[] mean, final double[] cov, final double nsigmas) {

		final double a = cov[0];
		final double b = cov[1];
		final double c = cov[2];
		final double d = Math.sqrt(a * a + 4 * b * b - 2 * a * c + c * c);
		final double scale1 = Math.sqrt(0.5 * (a + c + d)) * nsigmas;
		final double scale2 = Math.sqrt(0.5 * (a + c - d)) * nsigmas;
		final double theta = 0.5 * Math.atan2((2 * b), (a - c));
		final double x = mean[0];
		final double y = mean[1];
		final double dx = scale1 * Math.cos(theta);
		final double dy = scale1 * Math.sin(theta);
		final EllipseRoi ellipse = new EllipseRoi(x - dx, y - dy, x + dx, y + dy, scale2 / scale1);
		return ellipse;
	}
}
