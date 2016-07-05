package houghandWatershed;

import java.io.File;
import java.util.ArrayList;

import com.sun.tools.javac.util.Pair;

import drawandOverlay.OverlayLines;
import drawandOverlay.PushCurves;
import ij.ImageJ;
import labeledObjects.Finalobject;
import labeledObjects.Indexedlength;
import labeledObjects.Lineobjects;
import net.imglib2.Cursor;
import net.imglib2.PointSampleList;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealCursor;
import net.imglib2.RealPointSampleList;
import net.imglib2.algorithm.stats.Normalize;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import pSF.Boundingbox.Objectproperties;
import peakFitter.LengthDetection;
import preProcessing.Kernels;
import preProcessing.Kernels.ProcessingType;
import util.ImgLib2Util;

public class HoughpostWater {

	public static void main(String[] args) throws Exception {

		RandomAccessibleInterval<FloatType> biginputimg = ImgLib2Util
				.openAs32Bit(new File("src/main/resources/2015-01-14_Seeds-1.tiff"));
		// small_mt.tif image to be used for testing
		// 2015-01-14_Seeds-1.tiff for actual
		// mt_experiment.tif for big testing
		new ImageJ();

		new Normalize();
		FloatType minval = new FloatType(0);
		FloatType maxval = new FloatType(1);
		Normalize.normalize(Views.iterable(biginputimg), minval, maxval);
		ImageJFunctions.show(biginputimg);
		final int n = biginputimg.numDimensions();
		double[] sigma = new double[biginputimg.numDimensions()];

		for (int d = 0; d < sigma.length; ++d)
			sigma[d] = 2;

		// Initialize empty images to be used later
		RandomAccessibleInterval<FloatType> inputimg = new ArrayImgFactory<FloatType>().create(biginputimg,
				new FloatType());
		
		RandomAccessibleInterval<FloatType> imgout = new ArrayImgFactory<FloatType>().create(biginputimg,
				new FloatType());
		RandomAccessibleInterval<FloatType> gaussimg = new ArrayImgFactory<FloatType>().create(biginputimg,
				new FloatType());

		// Preprocess image
		
		
		
		
		inputimg = Kernels.Preprocess(biginputimg, ProcessingType.CannyEdge);

		ImageJFunctions.show(inputimg).setTitle("Preprocessed image");

		ArrayList<Lineobjects> linelist = new ArrayList<Lineobjects>(biginputimg.numDimensions());
		Img<IntType> Intimg = new ArrayImgFactory<IntType>().create(biginputimg, new IntType());
		Pair<Img<IntType>, ArrayList<Lineobjects>> linepair = new Pair<Img<IntType>, ArrayList<Lineobjects>>(Intimg,
				linelist);

		// Do watershedding and Hough
		
		// Declare minimum length of the line(in pixels) to be detected
				double minlength = 10;
				
		linepair = PerformWatershedding.DowatersheddingandHough(biginputimg, inputimg, minlength);

		// Overlay detected lines on the image

		PointSampleList<FloatType> centroidlist = new PointSampleList<FloatType>(n);

		

		final int ndims = biginputimg.numDimensions();
		double[] final_param = new double[2 * ndims + 2];
		double[] noise_param = new double[ndims];
		double[] psf = new double[ndims];
		final long radius = 4;
		final double SNR = 4000/240;
		psf[0] = 1.7;
		psf[1] = 1.54;
		// Input the psf-sigma here to be used for convolving Gaussians on a
		// line, will not change during iteration.

		
		

		ArrayList<double[]> totalgausslist = new ArrayList<double[]>();

		// Get a rough reconstruction of the line and the list of centroids where psf of the image has to be convolved

		final ArrayList<Finalobject> finalparamlist = new ArrayList<Finalobject>();
		
		OverlayLines.GetAlllines(imgout, biginputimg, linepair.fst, centroidlist,finalparamlist, linepair.snd, radius);

		ImageJFunctions.show(imgout).setTitle("Rough-Reconstruction");


		// Input the image on which you want to do the fitting, along with the labelled image
		
		LengthDetection MTlength = new LengthDetection(biginputimg, linepair.fst);

		final ArrayList<Finalobject> totalparamlist = new ArrayList<Finalobject>();
		// Choose the noise level of the image, 0 for pre-processed image and >0 for original image
		
		for (int index = 0; index < finalparamlist.size(); ++index){
			
			final_param = MTlength.Getfinalparam(finalparamlist.get(index).centroid, radius, psf);
			noise_param = MTlength.Getnoiseparam(finalparamlist.get(index).centroid, radius);
			
			if (Math.exp(-noise_param[0] - noise_param[1]) < 2.0/SNR){
				
				finalparamlist.remove(index);
				
			}
			
			if ( Math.exp(-noise_param[0] - noise_param[1]) > 2.0/SNR){
				
				System.out.println(" Amp: " + final_param[0] + " " + "Mu X: " + final_param[1] + " "
						+ "Mu Y: " + final_param[2] + " " + "Sig X: " + Math.sqrt(1.0/final_param[3]) + " " + "Sig Y: "
						+ Math.sqrt(1.0/final_param[4]) + "  " + " Noise: "+ Math.exp(-noise_param[0] - noise_param[1])  );
				
				totalgausslist.add(final_param);
				
				Finalobject line = new Finalobject(finalparamlist.get(index).Label,
						finalparamlist.get(index).centroid, finalparamlist.get(index).Intensity, finalparamlist.get(index).slope,
						finalparamlist.get(index).intercept);

				totalparamlist.add(line);
				
			}
			
		}
		
		ArrayList<Indexedlength> finallength = new ArrayList<Indexedlength>();
		
		MTlength.Returnlengths(totalparamlist, finallength, psf);
		for (int index = 0; index< finallength.size(); ++index){
			
		if (finallength.get(index).length > 0 )	
		System.out.println("Label: " + finallength.get(index).Label + " " +
		 "Length: "+ finallength.get(index).length + " " + "Slope: " + finallength.get(index).slope +  
		 " Intercept :" + finallength.get(index).intercept + " StartposX: " + finallength.get(index).startpos[0] 
				 +" EndposX: " + finallength.get(index).endpos[0] );
		}
		// Draw the Gaussian convolved line fitted with the original data
		PushCurves.DrawDetectedGaussians(gaussimg, totalgausslist);
		ImageJFunctions.show(gaussimg).setTitle("Iterated Result");

	}
}