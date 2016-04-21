package varun;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.fft2.FFTConvolution;
//import net.imglib2.algorithm.fft2.FFTConvolution;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.algorithm.region.hypersphere.HyperSphereCursor;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

public class Kernels {

	public static void ButterflyKernel(final RandomAccessibleInterval<FloatType> inputimage) {

		final float[] butterflyKernel = new float[] { 0, -2, 0, 1, 2, 1, 0, -2, 0 };

		final Img<FloatType> Butterfly = ArrayImgs.floats(butterflyKernel, new long[] { 3, 3 });

		// apply convolution to convolve input data with kernels

		new FFTConvolution<FloatType>(inputimage, Butterfly, new ArrayImgFactory<ComplexFloatType>()).convolve();
	}

	public static void GeneralButterflyKernel(final RandomAccessibleInterval<FloatType> inputimage, double linewidth,
			double dtheta) {
		final double size = Math.sqrt((inputimage.dimension(0) * inputimage.dimension(0)
				+ inputimage.dimension(1) * inputimage.dimension(1)));
		final double length = linewidth / Math.sin(Math.toRadians(dtheta));
		final double mask = -(2 * length + size) / 2;
		final float[] butterflyKernel = new float[] { 0, (float) mask, 0, (float) length, (float) size, (float) length,
				0, (float) mask, 0 };

		final Img<FloatType> Butterfly = ArrayImgs.floats(butterflyKernel, new long[] { 3, 3 });

		// apply convolution to convolve input data with kernels

		new FFTConvolution<FloatType>(inputimage, Butterfly, new ArrayImgFactory<ComplexFloatType>()).convolve();
		ImageJFunctions.show(Butterfly);
	}

	public static void BigButterflyKernel(final RandomAccessibleInterval<FloatType> inputimage) {

		final float[] butterflyKernel = new float[] { -10, -15, -22, -22, -22, -22, -22, -15, -10, -1, -6, -13, -22,
				-22, -22, -13, -6, -1, 3, 6, 4, -3, -22, -3, 4, 6, 3, 3, 11, 19, 28, 42, 28, 19, 11, 3, 3, 11, 27, 42,
				42, 42, 27, 11, 3, 3, 11, 19, 28, 42, 28, 19, 11, 3, 3, 6, 4, -3, -22, -3, 4, 6, 3, -1, -6, -13, -22,
				-22, -22, -13, -6, -1, -10, -15, -22, -22, -22, -22, -22, -15, -10

		};

		final Img<FloatType> Butterfly = ArrayImgs.floats(butterflyKernel, new long[] { 9, 9 });

		// apply convolution to convolve input data with kernels

		new FFTConvolution<FloatType>(inputimage, Butterfly, new ArrayImgFactory<ComplexFloatType>()).convolve();

	}

	public static void SobelXFilter(final RandomAccessibleInterval<FloatType> inputimage) {

		// create sobel edge filter kernels
		final float[] sX = new float[] { 1, 0, -1, 2, 0, -2, 1, 0, -1 };
		final Img<FloatType> sobelX = ArrayImgs.floats(sX, new long[] { 3, 3 });

		// apply convolution to convolve input data with kernels

		new FFTConvolution<FloatType>(inputimage, sobelX, new ArrayImgFactory<ComplexFloatType>()).convolve();

	}
	public static void SobelYFilter(final RandomAccessibleInterval<FloatType> inputimage) {

		// create sobel edge filter kernels
		final float[] sY = new float[] { 1, 2, -1, 0, 0, 0, -1, -2, -1 };
		final Img<FloatType> sobelY = ArrayImgs.floats(sY, new long[] { 3, 3 });

		// apply convolution to convolve input data with kernels

		new FFTConvolution<FloatType>(inputimage, sobelY, new ArrayImgFactory<ComplexFloatType>()).convolve();

	}
	public static void Edgedetector(final RandomAccessibleInterval<FloatType> inputimage) {
		final float[] HorizontalEdgeFilterKernel = new float[] { 0, 0, -1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0 };

		final float[] VerticalEdgeFilterKernel = new float[] { 0, 0, -1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 4, 0, 0, 0, 0, -1,
				0, 0, 0, 0, -1, 0, 0 };
		
		final Img<FloatType> HorizontalEdgeFilter = ArrayImgs.floats(HorizontalEdgeFilterKernel, new long[] { 5, 5 });
		final Img<FloatType> VerticalEdgeFilter = ArrayImgs.floats(VerticalEdgeFilterKernel, new long[] { 5, 5 });
		// apply convolution to convolve input data with kernels

		new FFTConvolution<FloatType>(inputimage, HorizontalEdgeFilter, new ArrayImgFactory<ComplexFloatType>())
				.convolve();
		new FFTConvolution<FloatType>(inputimage, VerticalEdgeFilter, new ArrayImgFactory<ComplexFloatType>())
				.convolve();

	}

	// Do mean filtering on the inputimage
		public static void MeanFilter(RandomAccessibleInterval<FloatType> inputimage,RandomAccessibleInterval<FloatType> outimage,
				double sigma) {
			// Mean filtering for a given sigma
			Cursor<FloatType> cursorInput = Views.iterable(inputimage).cursor();
			Cursor<FloatType> cursorOutput = Views.iterable(outimage).cursor();
			FloatType mean = Views.iterable(inputimage).firstElement().createVariable();
			while (cursorInput.hasNext()) {
				cursorInput.fwd();
				cursorOutput.fwd();
				HyperSphere<FloatType> hyperSphere = new HyperSphere<FloatType>(Views.extendMirrorSingle(inputimage),
						cursorInput, (long) sigma);
				HyperSphereCursor<FloatType> cursorsphere = hyperSphere.cursor();
				cursorsphere.fwd();
				mean.set(cursorsphere.get());
				int n = 1;
				while (cursorsphere.hasNext()) {
					cursorsphere.fwd();
					n++;
					mean.add(cursorsphere.get());
				}
				mean.div(new FloatType(n));
				cursorOutput.get().set(mean);
			}

		}
	

}