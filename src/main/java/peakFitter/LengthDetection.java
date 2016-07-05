package peakFitter;

import java.util.ArrayList;

import drawandOverlay.AddGaussian;
import ij.plugin.HyperStackReducer;
import labeledObjects.Finalobject;
import labeledObjects.Indexedlength;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.PointSampleList;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.neighborhood.HyperSphereNeighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.SquareStrelTest;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.algorithm.region.hypersphere.HyperSphereCursor;
import net.imglib2.roi.RectangleRegionOfInterest;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import preProcessing.GetLocalmaxmin;

public class LengthDetection {

	private final RandomAccessibleInterval<FloatType> inputimg;
	private final RandomAccessibleInterval<IntType> intimg;
	private final int ndims;

	public LengthDetection(RandomAccessibleInterval<FloatType> inputimg, RandomAccessibleInterval<IntType> intimg) {

		this.inputimg = inputimg;
		this.intimg = intimg;
		this.ndims = inputimg.numDimensions();
		assert inputimg.numDimensions() == intimg.numDimensions();

	}

	private final double[] makeBestGuess(final Localizable point, final double[][] X, final double[] I,
			final double[] psf) {

		double[] start_param = new double[2 * ndims + 2];

		double I_sum = 0;
		double[] X_sum = new double[ndims];
		for (int j = 0; j < ndims; j++) {
			X_sum[j] = 0;
			for (int i = 0; i < X.length; i++) {
				X_sum[j] += X[i][j] * I[i];
			}
		}
		double max_I = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < X.length; i++) {
			I_sum += I[i];
			if (I[i] > max_I) {
				max_I = I[i];
			}

		}

		start_param[0] = max_I;

		for (int j = 0; j < ndims; j++) {
			start_param[j+1] = X_sum[j] / I_sum;
		}
		
		for (int j = 0; j < ndims; j++) {
			double C = 0;
			double dx;
			for (int i = 0; i < X.length; i++) {
				dx = X[i][j] - start_param[j + 1];
				C += I[i] * dx * dx;
			}
			C /= I_sum;
			start_param[ndims + j + 1] = 1.0/Math.pow(psf[j],2); //1 / C;
		}
		start_param[2 * ndims + 1] = 0;

		return start_param;
	}

	private final double[] makeNoiseGuess() {
		double[] start_param = new double[ndims];

		for (int d = 0; d < ndims; ++d)
			start_param[d] = 0.5;

		return start_param;
	}

	private final double[] makeBestpointsGuess(final Localizable point, final double[][] X, final double[] I) {

		double[] start_param = new double[2 * ndims + 2];

		double I_sum = 0;
		
		double[] X_sum = new double[ndims];
		for (int j = 0; j < ndims; j++) {
			X_sum[j] = 0;
			for (int i = 0; i < X.length; i++) {
				X_sum[j] += X[i][j] * I[i];
			}
		}
		double max_I = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < X.length; i++) {
			I_sum += I[i];
			if (I[i] > max_I) {
				max_I = I[i];
			}

		}

		start_param[0] = max_I;

		for (int j = 0; j < ndims; j++) {
			start_param[j+1] = X_sum[j] / I_sum;
		}
		
		
		for (int j = 0; j < ndims; j++) {
			double C = 0;
			double dx;
			for (int i = 0; i < X.length; i++) {
				dx = X[i][j] - start_param[j + 1];
				C += I[i] * dx * dx;
			}
			C /= I_sum;
			start_param[ndims + j + 1] = 1 / C;
		}

		start_param[2 * ndims + 1] = 0;

		return start_param;
	}

	// Get final parameters for the Gaussian fit along the line, input the
	// centroid position and value where the Gaussian has to be fitted
	// in the image
	public double[] Getfinalparam(final Localizable point, final long radius, final double[] psf) throws Exception {

		PointSampleList<FloatType> datalist = gatherData(point, radius);

		final Cursor<FloatType> listcursor = datalist.localizingCursor();

		double[][] X = new double[(int) datalist.size()][ndims];
		double[] I = new double[(int) datalist.size()];
		int index = 0;
		while (listcursor.hasNext()) {
			listcursor.fwd();

			for (int d = 0; d < ndims; d++) {
				X[index][d] = listcursor.getDoublePosition(d);
			}

			I[index] = listcursor.get().getRealDouble();

			index++;
		}

		final double[] start_param = makeBestGuess(point, X, I, psf);

		final double[] finalparam = start_param.clone();
		int maxiter = 1000;
		double lambda = 1e-3;
		double termepsilon = 1e-1;

		LevenbergMarquardtSolverLocal.solve(X, finalparam, I, new GaussianMultiDLM(), lambda, termepsilon, maxiter);

		// NaN protection: we prefer returning the crude estimate than NaN
		for (int j = 0; j < finalparam.length; j++) {
			if (Double.isNaN(finalparam[j]))
				finalparam[j] = start_param[j];
		}

		return finalparam;

	}

	public double[] Getfinalpointsparam(final Localizable point, final long radius) throws Exception {

		PointSampleList<FloatType> datalist = gatherPointsData(point, radius);

		final Cursor<FloatType> listcursor = datalist.localizingCursor();

		double[][] X = new double[(int) datalist.size()][ndims];
		double[] I = new double[(int) datalist.size()];
		int index = 0;
		while (listcursor.hasNext()) {
			listcursor.fwd();

			for (int d = 0; d < ndims; d++) {
				X[index][d] = listcursor.getDoublePosition(d);
			}

			I[index] = listcursor.get().getRealDouble();

			index++;
		}

		final double[] start_param = makeBestpointsGuess(point, X, I);

		final double[] finalparam = start_param.clone();
		int maxiter = 1000;
		double lambda = 1e-3;
		double termepsilon = 1e-1;

		LevenbergMarquardtSolverLocal.solve(X, finalparam, I, new GaussianandConstNoise(), lambda, termepsilon,
				maxiter);

		// NaN protection: we prefer returning the crude estimate than NaN
		for (int j = 0; j < finalparam.length; j++) {
			if (Double.isNaN(finalparam[j]))
				finalparam[j] = start_param[j];
		}

		return finalparam;

	}

	public double[] Getnoiseparam(final Localizable point, long radius) throws Exception {

		PointSampleList<FloatType> datalist = gatherData(point, radius);

		final Cursor<FloatType> listcursor = datalist.localizingCursor();

		double[][] X = new double[(int) datalist.size()][ndims];
		double[] I = new double[(int) datalist.size()];
		int index = 0;
		while (listcursor.hasNext()) {
			listcursor.fwd();

			for (int d = 0; d < ndims; d++) {
				X[index][d] = listcursor.getDoublePosition(d);
			}

			I[index] = listcursor.get().getRealDouble();

			index++;
		}

		final double[] start_param = makeNoiseGuess();

		final double[] finalparam = start_param.clone();
		int maxiter = 1000;
		double lambda = 1e-3;
		double termepsilon = 1e-1;

		LevenbergMarquardtSolverLocal.solve(X, finalparam, I, new Nonoisepoiss(), lambda, termepsilon, maxiter);

		// NaN protection: we prefer returning the crude estimate than NaN
		for (int j = 0; j < finalparam.length; j++) {
			if (Double.isNaN(finalparam[j]))
				finalparam[j] = start_param[j];
		}

		return finalparam;

	}

	private PointSampleList<FloatType> gatherData(final Localizable point, final long radius) {
		final PointSampleList<FloatType> datalist = new PointSampleList<FloatType>(ndims);

		RandomAccess<FloatType> ranac = inputimg.randomAccess();

		ranac.setPosition(point);
		final double[] position = new double[ndims];
		point.localize(position);

		// Gather data around the point

		HyperSphere<FloatType> region = new HyperSphere<FloatType>(inputimg, point, radius);

		HyperSphereCursor<FloatType> localcursor = region.localizingCursor();
		RandomAccess<IntType> intranac = intimg.randomAccess();
		intranac.setPosition(point);
		final int label = intranac.get().get();
		boolean outofbounds = false;
		while (localcursor.hasNext()) {
			localcursor.fwd();

			for (int d = 0; d < ndims; d++) {

				if (localcursor.getDoublePosition(d) < 0 || localcursor.getDoublePosition(d) >= inputimg.dimension(d)) {
					outofbounds = true;
					break;
				}
			}
			if (outofbounds) {
				outofbounds = false;
				continue;
			}
			intranac.setPosition(localcursor);
			int i = intranac.get().get();
			if (i == label) {

				Point newpoint = new Point(localcursor);
				datalist.add(newpoint, localcursor.get().copy());

			}
		}

		return datalist;
	}

	private PointSampleList<FloatType> gatherPointsData(final Localizable point, final long radius) {
		final PointSampleList<FloatType> datalist = new PointSampleList<FloatType>(ndims);

		RandomAccess<IntType> intranac = intimg.randomAccess();
		final double[] position = new double[ndims];
		point.localize(position);
		intranac.setPosition(point);
		final int label = intranac.get().get();
		// Gather data around the point
		HyperSphere<FloatType> region = new HyperSphere<FloatType>(inputimg, point, radius);

		HyperSphereCursor<FloatType> localcursor = region.localizingCursor();

		boolean outofbounds = false;
		while (localcursor.hasNext()) {
			localcursor.fwd();

			for (int d = 0; d < ndims; d++) {

				if (localcursor.getDoublePosition(d) < 0 || localcursor.getDoublePosition(d) >= inputimg.dimension(d)) {
					outofbounds = true;
					break;
				}
			}
			if (outofbounds) {
				outofbounds = false;
				continue;
			}
			intranac.setPosition(localcursor);
			int i = intranac.get().get();
			if (i == label) {

				Point newpoint = new Point(localcursor);
				datalist.add(newpoint, localcursor.get().copy());

			}
		}

		return datalist;
	}

	public double Distance(final double[] cordone, final double[] cordtwo) {

		double distance = 0;

		for (int d = 0; d < ndims; ++d) {

			distance += Math.pow((cordone[d] - cordtwo[d]), 2);

		}
		return Math.sqrt(distance);
	}

	public void Returnlengths(ArrayList<Finalobject> finalparam, ArrayList<Indexedlength> finallength, double[] sigma) {

		int Maxlabel = houghandWatershed.PerformWatershedding.GetMaxlabelsseeded(intimg);

		for (int label = 1; label < Maxlabel - 1; ++label) {
			final double[] minVal = { Double.MAX_VALUE, Double.MAX_VALUE };
			final double[] maxVal = { Double.MIN_VALUE, Double.MIN_VALUE };
			double length = 0;

			double[] minposition = new double[ndims];
			double[] maxposition = new double[ndims];
			double[] negminposition = new double[ndims];
			double[] negmaxposition = new double[ndims];
			double[] startpos = new double[ndims];
			double[] endpos = new double[ndims];
			double slope = 0;
			double intercept = 0;
			double fwhmfactor = 2.3548 * 0.5;
			for (int index = 0; index < finalparam.size(); ++index) {

				if (finalparam.get(index).Label == label) {

					slope = finalparam.get(index).slope;
					intercept = finalparam.get(index).intercept;
					
					for (int d = 0; d < ndims; ++d) {
						minposition[d] = finalparam.get(index).centroid.getDoublePosition(d);
						maxposition[d] = finalparam.get(index).centroid.getDoublePosition(d);

						if (minposition[d] <= minVal[d]) {
							minVal[d] = minposition[d];
						}
						if (maxposition[d] >= maxVal[d]) {
							maxVal[d] = maxposition[d];
						}

					}

					for (int d = 0; d < ndims; ++d) {

						startpos[d] = minVal[d] - fwhmfactor * sigma[d];
						endpos[d] = maxVal[d] + fwhmfactor * sigma[d];


					}

					if (finalparam.get(index).slope >= 0) {

						length = Distance(startpos, endpos);

					}

					if (finalparam.get(index).slope < 0) {

						negminposition[0] = maxVal[0];
						negminposition[1] = minVal[1];

						startpos[0] = negminposition[0] + fwhmfactor * sigma[0];
						startpos[1] = negminposition[1] - fwhmfactor * sigma[1];

						negmaxposition[0] = minVal[0];
						negmaxposition[1] = maxVal[1];

						endpos[0] = negmaxposition[0] - fwhmfactor * sigma[0];
						endpos[1] = negmaxposition[1] + fwhmfactor * sigma[1];

						length = Distance(startpos, endpos);

					}

				}

			}

			final Indexedlength currentlength = new Indexedlength(label, length, startpos, endpos, slope, intercept);

			finallength.add(currentlength);

		}

	}

}
