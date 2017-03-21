package snakes;

import fiji.tool.SliceListener;
import fiji.tool.SliceObserver;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.EllipseRoi;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.io.Opener;
import ij.io.RoiEncoder;
import ij.measure.ResultsTable;
import ij.plugin.HyperStackMaker;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import kdTreeBlobs.FlagNode;
import kdTreeBlobs.NNFlagsearchKDtree;

import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.sequence.SetupImgLoader;
import mpicbg.spim.data.sequence.TimePoint;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import spim.fiji.spimdata.SpimData2;
import spim.fiji.spimdata.XmlIoSpimData2;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Rectangle;
import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.drew.metadata.exif.DataFormat;

import ch.qos.logback.core.subst.Token.Type;
import costMatrix.CostFunction;
import costMatrix.IntensityDiffCostFunction;
import costMatrix.PixelratiowDistCostFunction;
import costMatrix.SquareDistCostFunction;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.multithreading.SimpleMultiThreading;
import mpicbg.imglib.util.Util;
import mpicbg.spim.registration.detection.DetectionSegmentation;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.IterableInterval;
import net.imglib2.KDTree;
import net.imglib2.Point;
import net.imglib2.PointSampleList;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.dog.DogDetection;
import net.imglib2.algorithm.localextrema.RefinedPeak;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.img.imageplus.FloatImagePlus;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.RealSum;
import net.imglib2.view.Views;
import overlaytrack.DisplayGraph;
import spim.process.fusion.FusionHelper;
import trackerType.BlobTracker;
import trackerType.KFsearch;
import trackerType.NNsearch;
import trackerType.TrackModel;

/**
 * An interactive tool for getting Intensity in ROI's using Active Contour
 * 
 * @author Varun Kapoor
 */

public class BigDataInteractiveActiveContour implements PlugIn {

	final int scrollbarSize = 1000;

	float sigma = 0.5f;
	float sigma2 = 0.5f;
	float threshold = 1f;

	// steps per octave
	public static int standardSensitivity = 4;
	int sensitivity = standardSensitivity;

	float imageSigma = 0.5f;
	float sigmaMin = 0.5f;
	float sigmaMax = 30f;
	int sigmaInit = 300;
	
	
	int viewStetupid = 0;
	int timePointid = 0;
	
	int timeslider = 0;
	int timesliderInit = 1;
	int timeMin = 1;
	
	int sliceslider = 0;
	int slicesliderInit = 1;
	int sliceMin = 1;
	boolean TrackinT = true;
	
	ArrayList<Roi> SnakeRois;
	// ROI original
	int nbRois;
	Roi rorig = null;
	Roi processRoi = null;
	float thresholdMin = 0f;
	float thresholdMax = 1f;
	int thresholdInit = 1;
	int initialSearchradius = 10;
	double alpha = 0.5;
	double beta = 0.5;
	int maxSearchradius = 15;
	int missedframes = 20;
	double minIntensityImage = Double.NaN;

	List<TimePoint> timelist;
	double maxIntensityImage = Double.NaN;
	String usefolder = IJ.getDirectory("imagej");
	String addToName = "BlobinFramezStackwBio";
	String addTrackToName = "TrackedBlobsID";
	Color colorDraw = null;
	FinalInterval interval;
	SliceObserver sliceObserver;
	RoiListener roiListener;

	int channel = 0;
	RandomAccessibleInterval<FloatType> img;
	RandomAccessibleInterval<FloatType> originalimg;
	
	private SimpleWeightedGraph< SnakeObject, DefaultWeightedEdge > graphZ;
	// Dimensions of the stck :

	int totalframes = 0;
	int slicesize = 0;
	int length = 0;
	int height = 0;
	RandomAccessibleInterval<FloatType> CurrentView;
	SpimData2 spim;
	ArrayList<RefinedPeak<Point>> peaks;
	int currentslice = 1;
	// first and last slice to process
	int endFrame, currentframe = 1;
	Color originalColor = new Color(0.8f, 0.8f, 0.8f);
	Color inactiveColor = new Color(0.95f, 0.95f, 0.95f);
	public Rectangle standardRectangle;
	public EllipseRoi standardEllipse;
	boolean isComputing = false;
	boolean isStarted = false;
	boolean enableSigma2 = false;
	boolean sigma2IsAdjustable = true;
	boolean propagate = true;
	boolean lookForMinima = false;
	boolean Auto = false;
	boolean lookForMaxima = true;
	
	boolean SaveTxt = false;
	boolean SaveXLS = true;
     ImagePlus	impcopy, imp;
	double CalibrationX;
	double CalibrationY;
	BlobTracker blobtracker;
	CostFunction<SnakeObject, SnakeObject> UserchosenCostFunction;
	ArrayList<ArrayList<SnakeObject>> AllSliceSnakes;
	ArrayList<ArrayList<SnakeObject>> All3DSnakes;

	public static enum ValueChange {
		SIGMA, THRESHOLD, ROI, MINMAX, ALL, FRAME, SLICE
	}

	boolean isFinished = false;
	boolean wasCanceled = false;

	public boolean isFinished() {
		return isFinished;
	}

	public boolean wasCanceled() {
		return wasCanceled;
	}

	public double getInitialSigma() {
		return sigma;
	}

	public void setInitialSigma(final float value) {
		sigma = value;
		sigmaInit = computeScrollbarPositionFromValue(sigma, sigmaMin, sigmaMax, scrollbarSize);
	}

	public void setInitialTime(final int value) {
		
		timeslider = value;
		timesliderInit = 1;
		
	}
	
   public void setInitialSlice(final int value) {
		
		sliceslider = value;
		slicesliderInit = 1;
		
	}
	
	public double getSigma2() {
		return sigma2;
	}

	public double getThreshold() {
		return threshold;
	}

	public int getTime(){
		
		return timeslider;
		
	}
	
    public int getSlice(){
		
		return sliceslider;
		
	}
	
	public double getThresholdMin() {
		return thresholdMin;
	}

	public double getThresholdMax() {
		return thresholdMax;
	}

	public void setthresholdMax(final float thresholdMax) {

		this.thresholdMax = thresholdMax;

	}

	public void setthresholdMin(final float thresholdMin) {

		this.thresholdMin = thresholdMin;

	}

	public void setThreshold(final float value) {
		threshold = value;
		thresholdInit = computeScrollbarPositionFromValue(threshold, thresholdMin, thresholdMax, scrollbarSize);
	}
	
	public void setTime(final int value) {
		timeslider = value;
		timesliderInit = 1;
	}

	public int getTimeMax(){
		
		return totalframes;
	}
	
	public boolean getSigma2WasAdjusted() {
		return enableSigma2;
	}

	public boolean getLookForMaxima() {
		return lookForMaxima;
	}

	public boolean getLookForMinima() {
		return lookForMinima;
	}

	public void setLookForMaxima(final boolean lookForMaxima) {
		this.lookForMaxima = lookForMaxima;
	}

	public void setLookForMinima(final boolean lookForMinima) {
		this.lookForMinima = lookForMinima;
	}

	public void setSigmaMax(final float sigmaMax) {
		this.sigmaMax = sigmaMax;
	}

	public void setSigma2isAdjustable(final boolean state) {
		sigma2IsAdjustable = state;
	}

	// for the case that it is needed again, we can save one conversion
	public RandomAccessibleInterval<FloatType> getConvertedImage() {
		return CurrentView;
	}

	

	
	public BigDataInteractiveActiveContour(final SpimData2 spim ) {
		this.spim = spim;
		originalimg = getImg( spim, 0, 1);
		standardRectangle = new Rectangle(20, 20, (int)originalimg.dimension(0) - 40, (int)originalimg.dimension(1) - 40);
		
	}
	
	public BigDataInteractiveActiveContour(final RandomAccessibleInterval<FloatType> originalimg ) {
		this.originalimg = originalimg;
		standardRectangle = new Rectangle(20, 20, (int)originalimg.dimension(0) - 40, (int)originalimg.dimension(1) - 40);
	}

	public void setMinIntensityImage(final double min) {
		this.minIntensityImage = min;
	}

	public void setMaxIntensityImage(final double max) {
		this.maxIntensityImage = max;
	}

	@Override
	public void run(String arg) {
        ImageJFunctions.show(originalimg);
		if (spim!=null){
		
		timelist = spim.getSequenceDescription().getTimePoints().getTimePointsOrdered();
		currentframe = timelist.get(0).getId();
		totalframes = timelist.get(timelist.size() - 1).getId();
		originalimg = getImg( spim, 0, currentframe);
		}
		else{ 
			totalframes = 1;
		}
		endFrame = totalframes;
		
		
		
		slicesize = (int) originalimg.dimension(2);
		
		All3DSnakes =  new ArrayList<ArrayList<SnakeObject>>();
		endFrame = totalframes;
		currentslice = 1;
	    imp = WindowManager.getCurrentImage();
	    imp.show();
		impcopy = imp.duplicate();

		Roi roi = imp.getRoi();


		if (roi == null) {
			imp.setRoi(standardRectangle);
			roi = imp.getRoi();
		}

		if (roi.getType() != Roi.RECTANGLE) {
			IJ.log("Only rectangular rois are supported...");
			return;
		}

	

		// copy the ImagePlus into an ArrayImage<FloatType> for faster access
		CurrentView = getCurrentView(currentslice);

		final Float houghval = AutomaticThresholding(CurrentView);

		// Get local Minima in scale space to get Max rho-theta points
		float minPeakValue = houghval;

		setThreshold((float) (minPeakValue * 0.001));

		setthresholdMax((float) (minPeakValue));

		threshold = (float) (getThreshold());

		thresholdMax = (float) getThresholdMax();
		
		

		// show the interactive kit
		displaySliders();

		// add listener to the imageplus slice slider
		sliceObserver = new SliceObserver(imp, new ImagePlusListener());
		// compute first version#

		updatePreview(ValueChange.ALL);
		isStarted = true;

		// check whenever roi is modified to update accordingly
		roiListener = new RoiListener();
		imp.getCanvas().addMouseListener(roiListener);
		
		IJ.log("Total Slices" + slicesize + " Total Frames " + totalframes);

	}
	public void reset() {
		graphZ = new SimpleWeightedGraph<SnakeObject, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		final Iterator<SnakeObject> it = AllSliceSnakes.get(0).iterator();
		while (it.hasNext()) {
			graphZ.addVertex(it.next());
		}
	}
	private boolean Dialogue() {
		GenericDialog gd = new GenericDialog("Choose Frame");

		if (totalframes > 1) {
			gd.addNumericField("Move to frame", currentframe, 0);

		}

		gd.showDialog();
		if (totalframes > 1) {
			currentframe = (int) gd.getNextNumber();

			timeslider = currentframe;
		}
		return !gd.wasCanceled();
	}
	
	private boolean DialogueSlice() {
		GenericDialog gd = new GenericDialog("Choose Slice");

		if (slicesize > 1) {
			gd.addNumericField("Move to Slice", currentslice, 0);

		}

		gd.showDialog();
		if (slicesize > 1) {
			currentslice = (int) gd.getNextNumber();

			sliceslider = currentslice;
		}
		return !gd.wasCanceled();
	}

	private boolean Dialoguesec() {
		GenericDialog gd = new GenericDialog("Choose Final Frame");

		if (totalframes > 1) {
			gd.addNumericField("Do till frame", totalframes, 0);

			assert (int) gd.getNextNumber() > 1;
		}

		gd.showDialog();
			totalframes= (int) gd.getNextNumber();

		return !gd.wasCanceled();
	}

	private boolean DialogueTracker() {

		String[] colors = { "Red", "Green", "Blue", "Cyan", "Magenta", "Yellow", "Black", "White" };
		String[] whichtracker = { "Kalman (recommended)", "Nearest Neighbour" };
		String[] whichcost = { "Distance based", "Intensity based", "IntensitywDist based" };
		int indexcol = 0;
		int trackertype = 0;
		int functiontype = 0;

		// Create dialog
		GenericDialog gd = new GenericDialog("Tracker");

		gd.addChoice("Choose your tracker :", whichtracker, whichtracker[trackertype]);
		gd.addChoice("Choose your Cost function (for Kalman) :", whichcost, whichcost[functiontype]);
		gd.addChoice("Draw tracks with this color :", colors, colors[indexcol]);

		gd.addNumericField("Initial Search Radius", 10, 0);
		gd.addNumericField("Max Movment of Blobs per frame", 15, 0);
		gd.addNumericField("Blobs allowed to be lost for #frames", 20, 0);

		

		initialSearchradius = (int) gd.getNextNumber();
		maxSearchradius = (int) gd.getNextNumber();
		missedframes = (int) gd.getNextNumber();
		// Choice of tracker
		trackertype = gd.getNextChoiceIndex();
		if (trackertype == 0) {

			functiontype = gd.getNextChoiceIndex();
			switch (functiontype) {

			case 0:
				UserchosenCostFunction = new SquareDistCostFunction();
				break;

			case 1:
				UserchosenCostFunction = new IntensityDiffCostFunction();
				break;

			case 2:
				gd.addNumericField("Intensity Similarity Weight:", alpha, 0);
				gd.addNumericField("Distance Similarity Weight:", beta, 0);
				
				alpha = gd.getNextNumber();
				beta = gd.getNextNumber();
				
				UserchosenCostFunction = new PixelratiowDistCostFunction(alpha, beta);
				break;

			}
			
			
			
			
			blobtracker = new KFsearch(All3DSnakes, UserchosenCostFunction, maxSearchradius, initialSearchradius,
					totalframes, missedframes);
		}

		if (trackertype == 1)
			blobtracker = new NNsearch(All3DSnakes, maxSearchradius, totalframes);
		
		
		gd.showDialog();
		// color choice of display
		indexcol = gd.getNextChoiceIndex();
		switch (indexcol) {
		case 0:
			colorDraw = Color.red;
			break;
		case 1:
			colorDraw = Color.green;
			break;
		case 2:
			colorDraw = Color.blue;
			break;
		case 3:
			colorDraw = Color.cyan;
			break;
		case 4:
			colorDraw = Color.magenta;
			break;
		case 5:
			colorDraw = Color.yellow;
			break;
		case 6:
			colorDraw = Color.black;
			break;
		case 7:
			colorDraw = Color.white;
			break;
		default:
			colorDraw = Color.yellow;
		}
		return !gd.wasCanceled();
	}

	/**
	 * Updates the Preview with the current parameters (sigma, threshold, roi,
	 * slicenumber)
	 * 
	 * @param change
	 *            - what did change
	 */

	protected void updatePreview(final ValueChange change) {

		// check if Roi changed
		imp = WindowManager.getCurrentImage();
		boolean roiChanged = false;
		Roi roi = imp.getRoi();
		if (roi == null || roi.getType() != Roi.RECTANGLE) {
			imp.setRoi(new Rectangle(standardRectangle));
			roi = imp.getRoi();
			roiChanged = true;
		}

		final Rectangle rect = roi.getBounds();
		if (roiChanged || img == null || change == ValueChange.FRAME || rect.getMinX() != standardRectangle.getMinX()
				|| rect.getMaxX() != standardRectangle.getMaxX() || rect.getMinY() != standardRectangle.getMinY()
				|| rect.getMaxY() != standardRectangle.getMaxY()) {
			standardRectangle = rect;
			long[] min = { (long) standardRectangle.getMinX(), (long) standardRectangle.getMinY() };
			long[] max = { (long) standardRectangle.getMaxX(), (long) standardRectangle.getMaxY() };
			interval = new FinalInterval(min, max);
			img = extractImage(CurrentView);
			roiChanged = true;
		}

		// if we got some mouse click but the ROI did not change we can return
		if (!roiChanged && change == ValueChange.ROI) {
			isComputing = false;
			return;
		}
		// compute the Difference Of Gaussian if necessary
		if (peaks == null || roiChanged || change == ValueChange.SIGMA || change == ValueChange.FRAME
				|| change == ValueChange.THRESHOLD || change == ValueChange.SLICE
				|| change == ValueChange.ALL || change == ValueChange.MINMAX) {
			//
			// Compute the Sigmas for the gaussian folding
			//

			 DogDetection.ExtremaType type = DogDetection.ExtremaType.MINIMA;

			if (lookForMaxima)
				type = DogDetection.ExtremaType.MINIMA;
			else if (lookForMinima)
				type = DogDetection.ExtremaType.MAXIMA;
			

			final DogDetection<FloatType> newdog = new DogDetection<FloatType>(Views.extendZero(img), interval,
					new double[] { 1, 1 }, sigma, sigma2, type, threshold, true);

			peaks = newdog.getSubpixelPeaks();

		}

		// extract peaks to show
		Overlay o = imp.getOverlay();

		if (o == null) {
			o = new Overlay();
			imp.setOverlay(o);
		}

		o.clear();

		RoiManager roimanager = RoiManager.getInstance();

		if (roimanager == null) {
			roimanager = new RoiManager();
		}

		MouseEvent mev = new MouseEvent(imp.getCanvas(), MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, 0, 0,
				10, false);
		/*
		 * if ((change == ValueChange.ROI || change == ValueChange.SIGMA ||
		 * change == ValueChange.MINMAX || change == ValueChange.SLICE || change
		 * == ValueChange.THRESHOLD && RoisOrig != null)) {
		 */
		if (mev != null) {

			roimanager.close();

			roimanager = new RoiManager();

			// }
		}

		for (final RefinedPeak<Point> peak : peaks) {
			float x = (float) (peak.getFloatPosition(0));
			float y = (float) (peak.getFloatPosition(1));

			final OvalRoi or = new OvalRoi(Util.round(x - sigma), Util.round(y - sigma), Util.round(sigma + sigma2),
					Util.round(sigma + sigma2));

			if (lookForMaxima)
				or.setStrokeColor(Color.red);
			else
				or.setStrokeColor(Color.green);

			o.add(or);
			roimanager.addRoi(or);
		}

		imp.updateAndDraw();

		isComputing = false;
	}

	

	protected class moveToFrameListener implements ActionListener {
		final float min, max;
		Label timeText;
		final Scrollbar timeScroll;
		public moveToFrameListener(Scrollbar timeScroll, Label timeText, float min, float max) {
			this.timeScroll = timeScroll;
			this.min = min;
			this.max = max;
			this.timeText = timeText;
		}
		@Override
		public void actionPerformed(final ActionEvent arg0) {

			boolean dialog = Dialogue();
			if (dialog) {
				

				timeScroll.setValue(computeIntScrollbarPositionFromValue(currentframe, min, max, scrollbarSize));
				timeText.setText("Framenumber = " + timeslider);
				if (currentframe < totalframes){
					if (spim!=null)
				originalimg = getImg(spim, viewStetupid, currentframe);
				
				}
				else{
					if (spim!=null)
					currentframe = totalframes;
					originalimg = getImg(spim, viewStetupid, currentframe);	
				}
				currentslice = 1;
				imp = WindowManager.getCurrentImage();
				// add listener to the imageplus slice slider
				sliceObserver = new SliceObserver(imp, new ImagePlusListener());
				

				Roi roi = imp.getRoi();
				if (roi == null) {
					// IJ.log( "A rectangular ROI is required to define the
					// area..."
					// );
					imp.setRoi(standardRectangle);
					roi = imp.getRoi();
				}

				// copy the ImagePlus into an ArrayImage<FloatType> for faster
				// access
				CurrentView = getCurrentView(currentslice);

				// compute first version
				updatePreview(ValueChange.FRAME);
				isStarted = true;

				// check whenever roi is modified to update accordingly
				roiListener = new RoiListener();
				imp.getCanvas().addMouseListener(roiListener);

			

				final Rectangle rect = roi.getBounds();

				for (final RefinedPeak<Point> peak : peaks) {

					final float x = peak.getFloatPosition(0);
					final float y = peak.getFloatPosition(1);

					final OvalRoi or = new OvalRoi(Util.round(x - sigma), Util.round(y - sigma),
							Util.round(sigma + sigma2), Util.round(sigma + sigma2));

					if (lookForMaxima)
						or.setStrokeColor(Color.red);
					else if (lookForMinima)
						or.setStrokeColor(Color.green);

					imp.setRoi(or);

				}
			}
		}
	}
	
	protected class moveToSliceListener implements ActionListener {
		final float min, max;
		Label sliceText;
		final Scrollbar sliceScroll;
		public moveToSliceListener(Scrollbar sliceScroll, Label sliceText, float min, float max) {
			this.sliceScroll = sliceScroll;
			this.min = min;
			this.max = max;
			this.sliceText = sliceText;
		}
		@Override
		public void actionPerformed(final ActionEvent arg0) {

			boolean dialog = DialogueSlice();
			if (dialog) {
				

				sliceScroll.setValue(computeIntScrollbarPositionFromValue(currentslice, min, max, scrollbarSize));
				sliceText.setText("Slicenumber = " + sliceslider);
				
				if (sliceslider < slicesize)
				originalimg.randomAccess().setPosition(sliceslider, 2);
				
				
				else{
					sliceslider = slicesize;
					originalimg.randomAccess().setPosition(sliceslider, 2);
					
				}
				currentslice = sliceslider;
				imp = WindowManager.getCurrentImage();
				// add listener to the imageplus slice slider
				sliceObserver = new SliceObserver(imp, new ImagePlusListener());
				
				

				Roi roi = imp.getRoi();
				if (roi == null) {
					// IJ.log( "A rectangular ROI is required to define the
					// area..."
					// );
					imp.setRoi(standardRectangle);
					roi = imp.getRoi();
				}

				// copy the ImagePlus into an ArrayImage<FloatType> for faster
				// access
				CurrentView = getCurrentView(currentslice);

				// compute first version
				updatePreview(ValueChange.SLICE);
				isStarted = true;

				// check whenever roi is modified to update accordingly
				roiListener = new RoiListener();
				imp.getCanvas().addMouseListener(roiListener);

			

				final Rectangle rect = roi.getBounds();

				for (final RefinedPeak<Point> peak : peaks) {

					final float x = peak.getFloatPosition(0);
					final float y = peak.getFloatPosition(1);

					final OvalRoi or = new OvalRoi(Util.round(x - sigma), Util.round(y - sigma),
							Util.round(sigma + sigma2), Util.round(sigma + sigma2));

					if (lookForMaxima)
						or.setStrokeColor(Color.red);
					else if (lookForMinima)
						or.setStrokeColor(Color.green);

					imp.setRoi(or);

				}
			}
		}
	}
	

	protected class moveAllListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent arg0) {

			// add listener to the imageplus slice slider
		

			Dialoguesec();
			int next = 0;
			int nextZ = originalimg.randomAccess().getIntPosition(2);
				// Run snakes over a frame for each slice in that frame
			List<TimePoint> timelist = spim.getSequenceDescription().getTimePoints().getTimePointsOrdered();
			

			
			for ( TimePoint t : timelist ){
				next++;
				currentframe = t.getId();
				
				if (currentframe < totalframes){
					if (spim!=null)
				originalimg = getImg(spim, viewStetupid, currentframe);
					imp = WindowManager.getCurrentImage();
					sliceObserver = new SliceObserver(imp, new ImagePlusListener());
				AllSliceSnakes = new ArrayList<ArrayList<SnakeObject>>();
				updatePreview(ValueChange.FRAME);
				
				for (int z = nextZ; z <= slicesize; ++z ){	
					
				currentslice = z;	
				originalimg.randomAccess().setPosition(currentslice, 2);	
				
				CurrentView = getCurrentView(currentslice);
				
				// Has to be corrected for and modified 
				InteractiveSnakeFast snake = null; //new InteractiveSnakeFast(CurrentView, z,  currentframe, TrackinT);

				RoiManager manager = RoiManager.getInstance();
				if (manager != null) {
					manager.getRoisAsArray();
				}

				
				updatePreview(ValueChange.SLICE);
				

				isStarted = true;

				// check whenever roi is modified to update accordingly
				roiListener = new RoiListener();
				imp.getCanvas().addMouseListener(roiListener);

				for (final RefinedPeak<Point> peak : peaks) {

					final float x = (float) peak.getDoublePosition(0);
					final float y = (float) peak.getDoublePosition(1);

					final OvalRoi or = new OvalRoi(Util.round(x - sigma), Util.round(y - sigma),
							Util.round(sigma + sigma2), Util.round(sigma + sigma2));

					if (lookForMaxima)
						or.setStrokeColor(Color.red);
					else if (lookForMinima)
						or.setStrokeColor(Color.green);

					imp.setRoi(or);

				}
				

				if (Auto) {
					if ( next > 1 || z > nextZ)
						snake.Auto = true;
				}
				snake.run();

				Overlay result = snake.getResult();
				ImagePlus resultimp = ImageJFunctions.show(CurrentView); 
				resultimp.setOverlay( result ); 
				resultimp.show();
			
				
				
				
				
				ArrayList<SnakeObject> currentsnakes = snake.getRoiList();
				
				if (AllSliceSnakes!= null ) {

					for (int Listindex = 0; Listindex < AllSliceSnakes.size(); ++Listindex) {

						SnakeObject SnakeFrame = AllSliceSnakes.get(Listindex).get(0);
						int Frame = SnakeFrame.thirdDimension;
						int Slice = SnakeFrame.fourthDimension;

						if (Frame == currentframe && Slice == currentslice ) {
							AllSliceSnakes.remove(Listindex);

						}
					}

				}
				
				AllSliceSnakes.add(currentsnakes);
				IJ.log(" " + AllSliceSnakes.size());
				
				} // Z loop closing
				// Make KD tree to link objects along Z axis
				
                ArrayList<SnakeObject>ThreedimensionalSnake =  getCentreofMass3D();
				
				All3DSnakes.add(ThreedimensionalSnake);
				
				
				
			} // t loop closing
			
				
			}
			
			
		
		}
		
	}

	protected class snakeButtonListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent arg0) {

			
		int count = 0;
		
			AllSliceSnakes = new ArrayList<ArrayList<SnakeObject>>();
			imp = WindowManager.getCurrentImage();
			for (int z = currentslice; z <= slicesize; ++z){
				count++;
				originalimg.randomAccess().setPosition(z, 2);
				CurrentView = getCurrentView(z);
				
			// has to be correctd for and modified	
			InteractiveSnakeFast snake = null; //new InteractiveSnakeFast(CurrentView, z,  currentframe, TrackinT);

			for (final RefinedPeak<Point> peak : peaks) {

				final float x = (float) peak.getDoublePosition(0);
				final float y = (float) peak.getDoublePosition(1);

				final OvalRoi or = new OvalRoi(Util.round(x - sigma), Util.round(y - sigma), Util.round(sigma + sigma2),
						Util.round(sigma + sigma2));

				if (lookForMaxima)
					or.setStrokeColor(Color.red);
				else if (lookForMinima)
					or.setStrokeColor(Color.green);

				imp.setRoi(or);

			}

			if (count > 1)
			snake.Auto = true;
			snake.run();
			Overlay result = snake.getResult();
			ImagePlus resultimp = ImageJFunctions.show(CurrentView); 
			resultimp.setOverlay( result ); 
			resultimp.show();
			ArrayList<SnakeObject> currentsnakes = snake.getRoiList();
			if (AllSliceSnakes != null) {

				for (int Listindex = 0; Listindex < AllSliceSnakes.size(); ++Listindex) {

					SnakeObject SnakeFrame = AllSliceSnakes.get(Listindex).get(0);
					int Frame = SnakeFrame.thirdDimension;
					int Slice = SnakeFrame.fourthDimension;

					if (Frame == currentframe && Slice == currentslice ) {
						AllSliceSnakes.remove(Listindex);

					}
				}

			}

			AllSliceSnakes.add(currentsnakes);
			}
			
			
			
			// The graph for this frame along Z is now complete, generate 3D snake properties
			
			
				ArrayList<SnakeObject>ThreedimensionalSnake =  getCentreofMass3D();
				
				All3DSnakes.add(ThreedimensionalSnake);
				
				
			
		
			
		}
	}
	public ArrayList<SnakeObject> getCentreofMass3D() {
		
   if (slicesize > 1){
		reset();
		for (int z = currentslice; z < slicesize - 2; z++){
			
			ArrayList<SnakeObject> Spotmaxbase = AllSliceSnakes.get(z);
			
			ArrayList<SnakeObject> Spotmaxtarget = AllSliceSnakes.get(z + 1);
			
			
			
			Iterator<SnakeObject> baseobjectiterator = Spotmaxbase.iterator();
			
			
			
	        final int Targetblobs = Spotmaxtarget.size();
	        
			final List<RealPoint> targetCoords = new ArrayList<RealPoint>(Targetblobs);

			final List<FlagNode<SnakeObject>> targetNodes = new ArrayList<FlagNode<SnakeObject>>(Targetblobs);
			
			
	      // For linking the 2D center of masses on Rois to make a 3D object
			for (int index = 0; index < Spotmaxtarget.size(); ++index){
				
				
				
				targetCoords.add(new RealPoint(Spotmaxtarget.get(index).centreofMass));

				targetNodes.add(new FlagNode<SnakeObject>(Spotmaxtarget.get(index)));
				
				
			}
			if (targetNodes.size() > 0 && targetCoords.size() > 0){
				
				final KDTree<FlagNode<SnakeObject>> Tree = new KDTree<FlagNode<SnakeObject>>(targetNodes, targetCoords);
				
				final NNFlagsearchKDtree<SnakeObject> Search = new NNFlagsearchKDtree<SnakeObject>(Tree);
				
				
				
				while(baseobjectiterator.hasNext()){
					
					final SnakeObject source = baseobjectiterator.next();
					final RealPoint sourceCoords = new RealPoint(source.centreofMass);
					Search.search(sourceCoords);
					final double squareDist = Search.getSquareDistance();
					final FlagNode<SnakeObject> targetNode = Search.getSampler().get();
				
				
					
					// Connect the Source (source) and the Target (targetNode.getValue())
					
					targetNode.setVisited(true);
					
					synchronized (graphZ) {
						
						graphZ.addVertex(source);
						graphZ.addVertex(targetNode.getValue());
						final DefaultWeightedEdge edge = graphZ.addEdge(source, targetNode.getValue());
						graphZ.setEdgeWeight(edge, squareDist);
						
						
					}
					
					
					
				
			       
				}
				
				System.out.println(" In Frame " + currentframe +  " " +"Linked objects between " + z + " " + (z+1) + " Moving to next slice");
			}
			
			
			
		}
		
		
		// The graph for this frame along Z is now complete, generate 3D snake properties
		
		
		
		TrackModel model = new TrackModel(graphZ);
		model.getDirectedNeighborIndex();
		ArrayList<SnakeObject> ThreedimensionalSnake = new ArrayList<SnakeObject>();
		// Get all the track id's
		for (final Integer id : model.trackIDs(true)) {

			// Get the corresponding set for each id
			model.setName(id, "Track" + id);

			final HashSet<SnakeObject> Snakeset = model.trackSnakeObjects(id);
			
			// Make a list of objects along the Z axis as a list.
			ArrayList<SnakeObject> list = new ArrayList<SnakeObject>();
			Comparator<SnakeObject> Slicecomparison = new Comparator<SnakeObject>() {

				@Override
				public int compare(final SnakeObject A, final SnakeObject B) {

					return A.fourthDimension- B.fourthDimension;

				}

			};
		
			Iterator<SnakeObject> Snakeiter = Snakeset.iterator();
			while (Snakeiter.hasNext()) {

				SnakeObject currentsnake = Snakeiter.next();

				list.add(currentsnake);

			}
			Collections.sort(list, Slicecomparison);
			
			int ndims = originalimg.numDimensions();
			double[] center = new double[ 3 ];
			final double[] position = new double[ ndims];
			double Intensity = 0;
			double size = 0;
			double[] SumXYZ = new double[ 3 ];
			Cursor<FloatType> currentcursor;
			// Compute the center of mass here, trackID is in Z.
						for (int index = 0; index < list.size(); ++index) {
							
							if (ndims > 4)
			currentcursor = Views.iterable(Views.hyperSlice(originalimg, 2, list.get(index).fourthDimension)).localizingCursor();
			
							else
			currentcursor = Views.iterable(originalimg).localizingCursor();

								
			
			
			while (currentcursor.hasNext()) {

				currentcursor.fwd();

				currentcursor.localize(position);

				
				int x = (int) position[0];
				int y = (int) position[1];
			
				
				Roi roi = list.get(index).roi;
				if (roi.contains(x, y)) {

					size+=roi.getLength();
					SumXYZ[0] += currentcursor.getDoublePosition(0) * currentcursor.get().getRealDouble();
					SumXYZ[1] += currentcursor.getDoublePosition(1) * currentcursor.get().getRealDouble();
					SumXYZ[2] += list.get(index).fourthDimension  * currentcursor.get().getRealDouble();
					
					Intensity += currentcursor.get().getRealDouble();
				}
				
				
				
			}
			
			SnakeRois.add(list.get(index).roi);
			
			}
			
			center[ 0 ] = SumXYZ[0] / Intensity;
			center[ 1 ] = SumXYZ[1] / Intensity;
			center[ 2 ] = SumXYZ[2] / Intensity;
			
			
		// Has to be corrected for and modified	
			
		final SnakeObject ThreeDSnake = null; //new SnakeObject(list.get(0).thirdDimension, list.get(0).fourthDimension, id, SnakeRois, center, Intensity, size);
		
		
		ThreedimensionalSnake.add(ThreeDSnake);
		
		}
   
		return ThreedimensionalSnake;
		}
		
   
   else 
	   
	   return AllSliceSnakes.get(AllSliceSnakes.size() - 1);
	
	}
	
	
	
	
	public static float computeSigma2(final float sigma1, final int sensitivity) {
		final float k = (float) DetectionSegmentation.computeK(sensitivity);
		final float[] sigma = DetectionSegmentation.computeSigma(k, sigma1);

		return sigma[1];
	}

	public static void writeIntensities(String nom, int nb, ArrayList<SnakeObject> currentsnakes) {
		NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(3);
		try {
			File fichier = new File(nom + nb + ".txt");
			FileWriter fw = new FileWriter(fichier);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("\tFramenumber\tRoiLabel\tCenterofMassX\tCenterofMassY\tIntensityCherry\tIntensityBio\n");
			for (int index = 0; index < currentsnakes.size(); ++index) {
				bw.write("\t" + nb + "\t" + "\t" + currentsnakes.get(index).Label + "\t" + "\t"
						+ nf.format(currentsnakes.get(index).centreofMass[0]) + "\t" + "\t"
						+ nf.format(currentsnakes.get(index).centreofMass[1]) + "\t" + "\t"
						+ nf.format(currentsnakes.get(index).IntensityROI) +  "\n");
			}
			bw.close();
			fw.close();
		} catch (IOException e) {
		}
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

	protected RandomAccessibleInterval<FloatType> extractImage(final RandomAccessibleInterval<FloatType> intervalView) {

		final FloatType type = intervalView.randomAccess().get().createVariable();
		final ImgFactory<FloatType> factory = net.imglib2.util.Util.getArrayOrCellImgFactory(intervalView, type);
		RandomAccessibleInterval<FloatType> totalimg = factory.create(intervalView, type);

		final RandomAccessibleInterval<FloatType> img = Views.interval(intervalView, interval);
		totalimg = Views.interval(Views.extendBorder(img), intervalView);

		return totalimg;
	}

	

	public static Float AutomaticThresholding(RandomAccessibleInterval<FloatType> inputimg) {

		FloatType max = new FloatType();
		FloatType min = new FloatType();
		Float ThresholdNew, Thresholdupdate;

		max = computeMaxIntensity(inputimg);
		min = computeMinIntensity(inputimg);

		ThresholdNew = (max.get() - min.get()) / 2;

		// Get the new threshold value after segmenting the inputimage with
		// thresholdnew
		Thresholdupdate = SegmentbyThresholding(Views.iterable(inputimg), ThresholdNew);

		while (true) {

			ThresholdNew = SegmentbyThresholding(Views.iterable(inputimg), Thresholdupdate);

			// Check if the new threshold value is close to the previous value
			if (Math.abs(Thresholdupdate - ThresholdNew) < 1.0E-2)
				break;
			Thresholdupdate = ThresholdNew;
		}

		return ThresholdNew;

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
	 * Instantiates the panel for adjusting the paramters
	 */

	protected void displaySliders() {
		final Frame frame = new Frame("Find Blobs and TrackEM");
		frame.setSize(650, 650);
		/* Instantiation */
		final GridBagLayout layout = new GridBagLayout();
		final GridBagConstraints c = new GridBagConstraints();
		final Label DogText = new Label("Use DoG to find Blobs ", Label.CENTER);
		final Scrollbar sigma1 = new Scrollbar(Scrollbar.HORIZONTAL, sigmaInit, 10, 0, 10 + scrollbarSize);
		this.sigma = computeValueFromScrollbarPosition(sigmaInit, sigmaMin, sigmaMax, scrollbarSize);
		
		final Scrollbar timeslider = new Scrollbar(Scrollbar.HORIZONTAL, timesliderInit, 0, 0,  totalframes);
		timeslider.setBlockIncrement(1);
		this.timeslider = (int) computeValueFromScrollbarPosition(timesliderInit, timeMin,totalframes , totalframes);
		
		
		final Scrollbar sliceslider = new Scrollbar(Scrollbar.HORIZONTAL, slicesliderInit, 0, 0,  slicesize);
		sliceslider.setBlockIncrement(1);
		this.sliceslider = (int) computeValueFromScrollbarPosition(slicesliderInit, sliceMin,slicesize , slicesize);
		

		final Scrollbar threshold = new Scrollbar(Scrollbar.HORIZONTAL, thresholdInit, 10, 0, 10 + scrollbarSize);
		this.threshold = computeValueFromScrollbarPosition(thresholdInit, thresholdMin, thresholdMax, scrollbarSize);
		this.sigma2 = computeSigma2(this.sigma, this.sensitivity);
		final int sigma2init = computeScrollbarPositionFromValue(this.sigma2, sigmaMin, sigmaMax, scrollbarSize);
		final Scrollbar sigma2 = new Scrollbar(Scrollbar.HORIZONTAL, sigma2init, 10, 0, 10 + scrollbarSize);

		final Label sigmaText1 = new Label("Sigma 1 = " + this.sigma, Label.CENTER);
		final Label sigmaText2 = new Label("Sigma 2 = " + this.sigma2, Label.CENTER);
		final Label timeText = new Label("Framenumber = " + this.timeslider, Label.CENTER);
		final Label sliceText = new Label("Slicenumber = " + this.sliceslider, Label.CENTER);
		final Label thresholdText = new Label("Threshold = " + this.threshold, Label.CENTER);

		final Button track = new Button("Start Tracking");
		final Button cancel = new Button("Cancel");
		final Button snakes = new Button("Apply snakes to current Frame selection");
		final Button JumpFrame = new Button("Jump to Frame:");
		final Button JumpSlice = new Button("Jump to Slice:");
		final Button AutomatedSnake = new Button("Automated Snake run for all frames");
		final Checkbox Auto = new Checkbox("Constant parameters over Frames");
		final Checkbox min = new Checkbox("Look for Minima (green)", lookForMinima);
		final Checkbox max = new Checkbox("Look for Maxima (red)", lookForMaxima);
		final Checkbox txtfile = new Checkbox("Save tracks as TXT file", SaveTxt);
		final Checkbox xlsfile = new Checkbox("Save tracks as XLS file", SaveXLS);

		/* Location */
		frame.setLayout(layout);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;

		frame.add(DogText, c);
		++c.gridy;
		++c.gridy;
		++c.gridy;
		frame.add(sigma1, c);

		++c.gridy;
		frame.add(sigmaText1, c);

		++c.gridy;
		frame.add(sigma2, c);

		++c.gridy;
		frame.add(sigmaText2, c);

		
		

		++c.gridy;
		c.insets = new Insets(10, 0, 0, 0);
		frame.add(threshold, c);
		c.insets = new Insets(0, 0, 0, 0);

		++c.gridy;
		frame.add(thresholdText, c);

		
		if (totalframes > 1){
		++c.gridy;
		frame.add(timeslider, c);

		++c.gridy;
		frame.add(timeText, c);
		

		++c.gridy;
		c.insets = new Insets(0, 225, 0, 225);
		frame.add(JumpFrame, c);
		}
		
		if (slicesize > 1){
		++c.gridy;
		frame.add(sliceslider, c);

		++c.gridy;
		frame.add(sliceText, c);
		

		++c.gridy;
		c.insets = new Insets(0, 225, 0, 225);
		frame.add(JumpSlice, c);
		}
		
		
		++c.gridy;
		c.insets = new Insets(0, 170, 0, 75);
		frame.add(min, c);

		++c.gridy;
		c.insets = new Insets(0, 170, 0, 75);
		frame.add(max, c);
	


		++c.gridy;
		c.insets = new Insets(10, 160, 10, 160);
		frame.add(snakes, c);

		++c.gridy;
		c.insets = new Insets(0, 145, 0, 105);
		frame.add(Auto, c);

		++c.gridy;
		c.insets = new Insets(0, 145, 0, 145);
		frame.add(AutomatedSnake, c);

		++c.gridy;
		c.insets = new Insets(10, 245, 0, 245);
		frame.add(track, c);

		++c.gridy;
		c.insets = new Insets(10, 275, 0, 275);
		frame.add(cancel, c);
		
		++c.gridy;
		c.insets = new Insets(10, 10, 0, 0);
		frame.add(txtfile, c);
		
		++c.gridy;
		c.insets = new Insets(10, 10, 0, 0);
		frame.add(xlsfile, c);

		/* Configuration */
		sigma1.addAdjustmentListener(
				new SigmaListener(sigmaText1, sigmaMin, sigmaMax, scrollbarSize, sigma1, sigma2, sigmaText2));
		sigma2.addAdjustmentListener(new Sigma2Listener(sigmaMin, sigmaMax, scrollbarSize, sigma2, sigmaText2));
		threshold.addAdjustmentListener(new ThresholdListener(thresholdText, thresholdMin, thresholdMax));
		timeslider.addAdjustmentListener(new TimeSliderListener(timeText, timeMin, totalframes));
		sliceslider.addAdjustmentListener(new SliceSliderListener(sliceText, sliceMin, slicesize));
		track.addActionListener(new TrackerButtonListener(frame));
		cancel.addActionListener(new CancelButtonListener(frame, true));
		snakes.addActionListener(new snakeButtonListener());
		JumpFrame.addActionListener(new moveToFrameListener(timeslider,timeText, timeMin, totalframes));
		JumpSlice.addActionListener(new moveToSliceListener(sliceslider,sliceText, sliceMin, slicesize));
		
		AutomatedSnake.addActionListener(new moveAllListener());
		min.addItemListener(new MinListener());
		max.addItemListener(new MaxListener());
		Auto.addItemListener(new AutoListener());
		txtfile.addItemListener(new SaveasTXT());
		xlsfile.addItemListener(new SaveasXLS());

	

		frame.addWindowListener(new FrameListener(frame));

		frame.setVisible(true);

		originalColor = sigma2.getBackground();
		DogText.setFont(thresholdText.getFont().deriveFont(Font.BOLD));
		sigma2.setFont(sigmaText2.getFont().deriveFont(Font.BOLD));
		sigmaText1.setFont(sigmaText1.getFont().deriveFont(Font.BOLD));
		thresholdText.setFont(thresholdText.getFont().deriveFont(Font.BOLD));
		timeText.setFont(thresholdText.getFont().deriveFont(Font.BOLD));
		sliceText.setFont(thresholdText.getFont().deriveFont(Font.BOLD));
	}

	protected class EnableListener implements ItemListener {
		final Scrollbar sigma2;
		final Label sigmaText2;

		public EnableListener(final Scrollbar sigma2, final Label sigmaText2) {
			this.sigmaText2 = sigmaText2;
			this.sigma2 = sigma2;
		}

		@Override
		public void itemStateChanged(final ItemEvent arg0) {
			if (arg0.getStateChange() == ItemEvent.DESELECTED) {
				sigmaText2.setFont(sigmaText2.getFont().deriveFont(Font.PLAIN));
				sigma2.setBackground(inactiveColor);
				enableSigma2 = false;
			} else if (arg0.getStateChange() == ItemEvent.SELECTED) {
				sigmaText2.setFont(sigmaText2.getFont().deriveFont(Font.BOLD));
				sigma2.setBackground(originalColor);
				enableSigma2 = true;
			}
		}
	}

	protected class MinListener implements ItemListener {
		@Override
		public void itemStateChanged(final ItemEvent arg0) {
			boolean oldState = lookForMinima;

			if (arg0.getStateChange() == ItemEvent.DESELECTED)
				lookForMinima = false;
			else if (arg0.getStateChange() == ItemEvent.SELECTED)
				lookForMinima = true;

			if (lookForMinima != oldState) {
				while (isComputing)
					SimpleMultiThreading.threadWait(10);

				updatePreview(ValueChange.MINMAX);
			}
		}
	}

	protected class AutoListener implements ItemListener {
		@Override
		public void itemStateChanged(final ItemEvent arg0) {

			if (arg0.getStateChange() == ItemEvent.DESELECTED)
				Auto = false;
			else if (arg0.getStateChange() == ItemEvent.SELECTED)
				Auto = true;

		}
	}
 protected class SaveasTXT implements ItemListener{
		 
		 @Override
	  		public void itemStateChanged(ItemEvent arg0) {
			 if (arg0.getStateChange() == ItemEvent.DESELECTED)
				 SaveTxt = false;
			 
			  else if (arg0.getStateChange() == ItemEvent.SELECTED)
				  SaveTxt = true;
			 
		 }
		 
	 }
	 
     protected class SaveasXLS implements ItemListener{
		 
		 @Override
	  		public void itemStateChanged(ItemEvent arg0) {
			 if (arg0.getStateChange() == ItemEvent.DESELECTED)
				 SaveXLS = false;
			 
			  else if (arg0.getStateChange() == ItemEvent.SELECTED)
				  SaveXLS = true;
			 
		 }
		 
	 }
	protected class MaxListener implements ItemListener {
		@Override
		public void itemStateChanged(final ItemEvent arg0) {
			boolean oldState = lookForMaxima;

			if (arg0.getStateChange() == ItemEvent.DESELECTED)
				lookForMaxima = false;
			else if (arg0.getStateChange() == ItemEvent.SELECTED)
				lookForMaxima = true;

			if (lookForMaxima != oldState) {
				while (isComputing)
					SimpleMultiThreading.threadWait(10);

				updatePreview(ValueChange.MINMAX);

			}
		}
	}

	public RandomAccessibleInterval<FloatType> getCurrentView(int Slice) {

		RandomAccess<FloatType> ran = originalimg.randomAccess();

		final int ndims = originalimg.numDimensions();

		final FloatType type = originalimg.randomAccess().get().createVariable();
		long[] dim = { originalimg.dimension(0), originalimg.dimension(1) };
		final ImgFactory<FloatType> factory = net.imglib2.util.Util.getArrayOrCellImgFactory(originalimg, type);
		RandomAccessibleInterval<FloatType> totalimg = factory.create(dim, type);
		Cursor<FloatType> cursor = Views.iterable(totalimg).localizingCursor();

		ran.setPosition(Slice - 1, ndims - 1);
		
		while (cursor.hasNext()) {

			cursor.fwd();

			long x = cursor.getLongPosition(0);
			long y = cursor.getLongPosition(1);

			ran.setPosition(x, 0);
			ran.setPosition(y, 1);

			cursor.get().set(ran.get());

		}
		
		return totalimg;

	}

	/**
	 * Tests whether the ROI was changed and will recompute the preview
	 * 
	 * @author Stephan Preibisch
	 */
	protected class RoiListener implements MouseListener {
		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(final MouseEvent e) {
			// here the ROI might have been modified, let's test for that
			imp = WindowManager.getCurrentImage();
			final Roi roi = imp.getRoi();

			if (roi == null || roi.getType() != Roi.RECTANGLE)
				return;

			while (isComputing)
				SimpleMultiThreading.threadWait(10);

			updatePreview(ValueChange.ROI);
		}

	}

	protected class TrackerButtonListener implements ActionListener {
		final Frame parent;

		public TrackerButtonListener(Frame parent) {
			this.parent = parent;
		}

		public void actionPerformed(final ActionEvent arg0) {
			
			IJ.log("Start Tracking");

			boolean dialog = DialogueTracker();
			if (dialog) {

				blobtracker.process();

				SimpleWeightedGraph<SnakeObject, DefaultWeightedEdge> graph = blobtracker.getResult();

				IJ.log("Tracking Complete " + " " + "Displaying results");

				DisplayGraph totaldisplaytracks = new DisplayGraph(impcopy, graph, colorDraw);
				totaldisplaytracks.getImp();

				TrackModel model = new TrackModel(graph);
				model.getDirectedNeighborIndex();
				IJ.log(" " + graph.vertexSet().size());
				ResultsTable rt = new ResultsTable();
				// Get all the track id's
				for (final Integer id : model.trackIDs(true)) {

					// Get the corresponding set for each id
					model.setName(id, "Track" + id);
					final HashSet<SnakeObject> Snakeset = model.trackSnakeObjects(id);
					ArrayList<SnakeObject> list = new ArrayList<SnakeObject>();
					Comparator<SnakeObject> Framecomparison = new Comparator<SnakeObject>() {

						@Override
						public int compare(final SnakeObject A, final SnakeObject B) {

							return A.thirdDimension - B.thirdDimension;

						}

					};

					Iterator<SnakeObject> Snakeiter = Snakeset.iterator();

					// Write tracks with same track id to file
					if (SaveTxt){
					
					NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
					nf.setMaximumFractionDigits(3);
					try {
						File fichier =  new File(usefolder + "//" + addTrackToName + "ID" + id + ".txt");
						FileWriter fw = new FileWriter(fichier);
						BufferedWriter bw = new BufferedWriter(fw);
						
						
						
						bw.write("\tFramenumber\tTrackID\tCenterofMassX\tCenterofMassY\tCenterofMassZ\tIntensity\n");
					
					
					
						while (Snakeiter.hasNext()) {

							SnakeObject currentsnake = Snakeiter.next();

							list.add(currentsnake);

						}
						
						Collections.sort(list, Framecomparison);
						
					
						for (int index = 0; index < list.size(); ++index) {
							
							bw.write("\t" + list.get(index).thirdDimension + "\t" + "\t" + id + "\t" + "\t"
									+ nf.format(list.get(index).centreofMass[0]) + "\t" + "\t"
									+ nf.format(list.get(index).centreofMass[1]) + "\t" + "\t"
									+ nf.format(list.get(index).centreofMass[2]) + "\t" + "\t"
									+ nf.format(list.get(index).IntensityROI) +  "\n");				
							
							
						}
						bw.close();
						fw.close();
					} catch (IOException e) {
					}
					}
						for (int index = 0; index < list.size(); ++index) {
							
							rt.incrementCounter();
						
							rt.addValue("FrameNumber", list.get(index).thirdDimension );
							rt.addValue("Track iD", id);
							rt.addValue("Center of Mass X", list.get(index).centreofMass[0] );
							rt.addValue("Center of Mass Y", list.get(index).centreofMass[1] );
							rt.addValue("Center of Mass Z", list.get(index).centreofMass[2] );
							rt.addValue("Intensity in ROI", list.get(index).IntensityROI);
							
							
							
							
							IJ.log("\t" + list.get(index).thirdDimension + "\t" + "\t" + id + "\t" + "\t"
									+ (list.get(index).centreofMass[0]) + "\t" + "\t"
									+ (list.get(index).centreofMass[1]) + "\t" + "\t"
									+ (list.get(index).centreofMass[2]) + "\t" + "\t"
									+ (list.get(index).IntensityROI) + "\n");
						
					
					}
						
						
					
						
					

				}
				
				rt.show("Results");
				if (SaveXLS)
				saveResultsToExcel(usefolder + "//" + addTrackToName + "test" + ".xls" , rt);
			}
			
			
			

		}

	}

	
	 public void saveResultsToExcel(String xlFile, ResultsTable rt){
			
			
			FileOutputStream xlOut = null;
			try {
				
				xlOut = new FileOutputStream(xlFile);
			}
			catch (FileNotFoundException ex){
				
				Logger.getLogger(BigDataInteractiveActiveContour.class.getName()).log(Level.SEVERE, null, ex);
			}
			
			HSSFWorkbook xlBook = new HSSFWorkbook();
			HSSFSheet xlSheet = xlBook.createSheet("Results Object Tracker");
			
			HSSFRow r = null;
			HSSFCell c = null;
			HSSFCellStyle cs = xlBook.createCellStyle();
			HSSFCellStyle cb = xlBook.createCellStyle();
			HSSFFont f = xlBook.createFont();
			HSSFFont fb = xlBook.createFont();
			HSSFDataFormat df = xlBook.createDataFormat();
			f.setFontHeightInPoints((short) 12);
			fb.setFontHeightInPoints((short) 12);
			fb.setBoldweight((short) Font.BOLD);
			cs.setFont(f);
			cb.setFont(fb);
			cs.setDataFormat(df.getFormat("#,##0.000"));
			cb.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
			cb.setFont(fb);
			
			int numRows = rt.size();
			String[] colHeaders = rt.getHeadings();
			int rownum = 0;
			
			// Create a Header
			r = xlSheet.createRow(rownum);
			
			for (int cellnum = 0; cellnum < colHeaders.length; cellnum++){
				
				c = r.createCell((short) cellnum);
				c.setCellStyle(cb);
				c.setCellValue(colHeaders[cellnum]);
				
			}
			rownum++;
			
			for (int row = 0; row < numRows; row++){
				
				r = xlSheet.createRow(rownum + row);
				int numCols = rt.getLastColumn() + 1;
				
				for (int cellnum = 0; cellnum < numCols; cellnum++){
					
					c = r.createCell((short) cellnum);
					c.setCellValue(rt.getValueAsDouble(cellnum, row));
				}
				
			}
			
			try { xlBook.write(xlOut); xlOut.close();}
			catch (IOException ex){
				Logger.getLogger(BigDataInteractiveActiveContour.class.getName()).log(Level.SEVERE, null, ex);
				
			}
			
			
		}
	
	


	protected class CancelButtonListener implements ActionListener {
		final Frame parent;
		final boolean cancel;

		// usefolder + "//" + "StaticPropertieszStack" + "-z", currentslice
		public CancelButtonListener(Frame parent, final boolean cancel) {
			this.parent = parent;
			this.cancel = cancel;
		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {
			wasCanceled = cancel;
			imp = WindowManager.getCurrentImage();
			close(parent, sliceObserver, imp, roiListener);
		}
	}

	protected class FrameListener extends WindowAdapter {
		final Frame parent;

		public FrameListener(Frame parent) {
			super();
			this.parent = parent;
		}

		@Override
		public void windowClosing(WindowEvent e) {
			close(parent, sliceObserver, imp, roiListener);
		}
	}

	protected final void close(final Frame parent, final SliceObserver sliceObserver, final ImagePlus imp,
			final RoiListener roiListener) {
		if (parent != null)
			parent.dispose();

		if (sliceObserver != null)
			sliceObserver.unregister();

		if (imp != null) {
			if (roiListener != null)
				imp.getCanvas().removeMouseListener(roiListener);

			imp.getOverlay().clear();
			imp.updateAndDraw();
		}

		isFinished = true;
	}

	protected class Sigma2Listener implements AdjustmentListener {
		final float min, max;
		final int scrollbarSize;

		final Scrollbar sigmaScrollbar2;
		final Label sigma2Label;

		public Sigma2Listener(final float min, final float max, final int scrollbarSize,
				final Scrollbar sigmaScrollbar2, final Label sigma2Label) {
			this.min = min;
			this.max = max;
			this.scrollbarSize = scrollbarSize;

			this.sigmaScrollbar2 = sigmaScrollbar2;
			this.sigma2Label = sigma2Label;
		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			if (enableSigma2) {
				sigma2 = computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);

				if (sigma2 < sigma) {
					sigma2 = sigma + 0.001f;
					sigmaScrollbar2.setValue(computeScrollbarPositionFromValue(sigma2, min, max, scrollbarSize));
				}

				sigma2Label.setText("Sigma 2 = " + sigma2);

				if (!event.getValueIsAdjusting()) {
					while (isComputing) {
						SimpleMultiThreading.threadWait(10);
					}
					updatePreview(ValueChange.SIGMA);
				}

			} else {
				// if no manual adjustment simply reset it
				sigmaScrollbar2.setValue(computeScrollbarPositionFromValue(sigma2, min, max, scrollbarSize));
			}
		}
	}

	protected class SigmaListener implements AdjustmentListener {
		final Label label;
		final float min, max;
		final int scrollbarSize;

		final Scrollbar sigmaScrollbar1;
		final Scrollbar sigmaScrollbar2;
		final Label sigmaText2;

		public SigmaListener(final Label label, final float min, final float max, final int scrollbarSize,
				final Scrollbar sigmaScrollbar1, final Scrollbar sigmaScrollbar2, final Label sigmaText2) {
			this.label = label;
			this.min = min;
			this.max = max;
			this.scrollbarSize = scrollbarSize;

			this.sigmaScrollbar1 = sigmaScrollbar1;
			this.sigmaScrollbar2 = sigmaScrollbar2;
			this.sigmaText2 = sigmaText2;
		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			sigma = computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);

			if (!enableSigma2) {
				sigma2 = computeSigma2(sigma, sensitivity);
				sigmaText2.setText("Sigma 2 = " + sigma2);
				sigmaScrollbar2.setValue(computeScrollbarPositionFromValue(sigma2, min, max, scrollbarSize));
			} else if (sigma > sigma2) {
				sigma = sigma2 - 0.001f;
				sigmaScrollbar1.setValue(computeScrollbarPositionFromValue(sigma, min, max, scrollbarSize));
			}

			label.setText("Sigma 1 = " + sigma);

			// if ( !event.getValueIsAdjusting() )
			{
				while (isComputing) {
					SimpleMultiThreading.threadWait(10);
				}
				updatePreview(ValueChange.SIGMA);
			}
		}
	}

	protected static float computeValueFromScrollbarPosition(final int scrollbarPosition, final float min,
			final float max, final int scrollbarSize) {
		return min + (scrollbarPosition / (float) scrollbarSize) * (max - min);
	}
	protected static float computeIntValueFromScrollbarPosition(final int scrollbarPosition, final float min,final float max, final int scrollbarSize) {
		return min + (scrollbarPosition /(max))* (max - min) ;
	}
	
	protected static int computeScrollbarPositionFromValue(final float sigma, final float min, final float max,
			final int scrollbarSize) {
		return Util.round(((sigma - min) / (max - min)) * scrollbarSize);
	}
	protected static int computeIntScrollbarPositionFromValue(final float timeslider, final float min, final float max,
			final int scrollbarSize) {
		return Util.round(((timeslider - min) / (max - min)) * max);
	}
	

	protected class ThresholdListener implements AdjustmentListener {
		final Label label;
		final float min, max;

		public ThresholdListener(final Label label, final float min, final float max) {
			this.label = label;
			this.min = min;
			this.max = max;
		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			threshold = computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);
			label.setText("Threshold = " + threshold);

			if (!isComputing) {
				updatePreview(ValueChange.THRESHOLD);
			} else if (!event.getValueIsAdjusting()) {
				while (isComputing) {
					SimpleMultiThreading.threadWait(10);
				}
				updatePreview(ValueChange.THRESHOLD);
			}
		}
	}

	protected class TimeSliderListener implements AdjustmentListener {
		final Label label;
		final float min, max;

		public TimeSliderListener(final Label label, final float min, final float max) {
			this.label = label;
			this.min = min;
			this.max = max;
		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			timeslider = (int) computeIntValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);
			label.setText("Framenumber = " + timeslider);

			currentframe = timeslider;
			
			
			if (!isComputing) {
				imp = WindowManager.getCurrentImage();
				sliceObserver = new SliceObserver(imp, new ImagePlusListener());
				imp.setPosition(channel, imp.getSlice(), currentframe);
				 if(currentframe > totalframes) {
					IJ.log("Max frame number exceeded, moving to last frame instead");
					imp.setPosition(channel, imp.getSlice(), totalframes);
					currentframe = totalframes;
				}

				if (imp.getType() == ImagePlus.COLOR_RGB || imp.getType() == ImagePlus.COLOR_256) {
					IJ.log("Color images are not supported, please convert to 8, 16 or 32-bit grayscale");
					return;
				}

				Roi roi = imp.getRoi();
				if (roi == null) {
					// IJ.log( "A rectangular ROI is required to define the
					// area..."
					// );
					imp.setRoi(standardRectangle);
					roi = imp.getRoi();
				}

				// copy the ImagePlus into an ArrayImage<FloatType> for faster
				// access
				CurrentView = getCurrentView(currentslice);
				MouseEvent mev = new MouseEvent(imp.getCanvas(), MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, 0, 0,
						10, false);
				/*
				 * if ((change == ValueChange.ROI || change == ValueChange.SIGMA ||
				 * change == ValueChange.MINMAX || change == ValueChange.SLICE || change
				 * == ValueChange.THRESHOLD && RoisOrig != null)) {
				 */
				if (mev != null) {
				// compute first version
				updatePreview(ValueChange.FRAME);
				
				}
				isStarted = true;

				// check whenever roi is modified to update accordingly
				roiListener = new RoiListener();
				imp.getCanvas().addMouseListener(roiListener);

			

				final Rectangle rect = roi.getBounds();

				for (final RefinedPeak<Point> peak : peaks) {

					final float x = peak.getFloatPosition(0);
					final float y = peak.getFloatPosition(1);

					final OvalRoi or = new OvalRoi(Util.round(x - sigma), Util.round(y - sigma),
							Util.round(sigma + sigma2), Util.round(sigma + sigma2));

					if (lookForMaxima)
						or.setStrokeColor(Color.red);
					else if (lookForMinima)
						or.setStrokeColor(Color.green);

					imp.setRoi(or);

				}
			
		
			} else if (!event.getValueIsAdjusting()) {
				while (isComputing) {
					SimpleMultiThreading.threadWait(10);
				}		
				imp = WindowManager.getCurrentImage();
				sliceObserver = new SliceObserver(imp, new ImagePlusListener());
				imp.setPosition(channel, imp.getSlice(), currentframe);
				if (currentframe > totalframes)  {
					IJ.log("Max frame number exceeded, moving to last frame instead");
					imp.setPosition(channel, imp.getSlice(), totalframes);
					currentframe = totalframes;
				}

				if (imp.getType() == ImagePlus.COLOR_RGB || imp.getType() == ImagePlus.COLOR_256) {
					IJ.log("Color images are not supported, please convert to 8, 16 or 32-bit grayscale");
					return;
				}

				Roi roi = imp.getRoi();
				if (roi == null) {
					// IJ.log( "A rectangular ROI is required to define the
					// area..."
					// );
					imp.setRoi(standardRectangle);
					roi = imp.getRoi();
				}

				// copy the ImagePlus into an ArrayImage<FloatType> for faster
				// access
				CurrentView = getCurrentView(currentslice);
				MouseEvent mev = new MouseEvent(imp.getCanvas(), MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, 0, 0,
						10, false);
				/*
				 * if ((change == ValueChange.ROI || change == ValueChange.SIGMA ||
				 * change == ValueChange.MINMAX || change == ValueChange.SLICE || change
				 * == ValueChange.THRESHOLD && RoisOrig != null)) {
				 */
				if (mev != null) {
				// compute first version
				updatePreview(ValueChange.FRAME);
				}
				isStarted = true;

				// check whenever roi is modified to update accordingly
				roiListener = new RoiListener();
				imp.getCanvas().addMouseListener(roiListener);

			

				final Rectangle rect = roi.getBounds();

				for (final RefinedPeak<Point> peak : peaks) {

					final float x = peak.getFloatPosition(0);
					final float y = peak.getFloatPosition(1);

					final OvalRoi or = new OvalRoi(Util.round(x - sigma), Util.round(y - sigma),
							Util.round(sigma + sigma2), Util.round(sigma + sigma2));

					if (lookForMaxima)
						or.setStrokeColor(Color.red);
					else if (lookForMinima)
						or.setStrokeColor(Color.green);

					imp.setRoi(or);

				}
			}
		
			
		}
	}
	protected class SliceSliderListener implements AdjustmentListener {
		final Label label;
		final float min, max;

		public SliceSliderListener(final Label label, final float min, final float max) {
			this.label = label;
			this.min = min;
			this.max = max;
		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			sliceslider = (int) computeIntValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);
			label.setText("Slicenumber = " + sliceslider);

			currentslice = sliceslider;
			
			
			if (!isComputing) {
				imp = WindowManager.getCurrentImage();
				sliceObserver = new SliceObserver(imp, new ImagePlusListener());
				imp.setPosition(channel, currentslice, imp.getFrame());
				 if(currentslice > slicesize) {
					IJ.log("Max slice number exceeded, moving to last slice instead");
					imp.setPosition(channel, slicesize, imp.getFrame());
					currentslice = slicesize;
				}

				if (imp.getType() == ImagePlus.COLOR_RGB || imp.getType() == ImagePlus.COLOR_256) {
					IJ.log("Color images are not supported, please convert to 8, 16 or 32-bit grayscale");
					return;
				}

				Roi roi = imp.getRoi();
				if (roi == null) {
					// IJ.log( "A rectangular ROI is required to define the
					// area..."
					// );
					imp.setRoi(standardRectangle);
					roi = imp.getRoi();
				}

				// copy the ImagePlus into an ArrayImage<FloatType> for faster
				// access
				CurrentView = getCurrentView(currentslice);
				MouseEvent mev = new MouseEvent(imp.getCanvas(), MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, 0, 0,
						10, false);
				/*
				 * if ((change == ValueChange.ROI || change == ValueChange.SIGMA ||
				 * change == ValueChange.MINMAX || change == ValueChange.SLICE || change
				 * == ValueChange.THRESHOLD && RoisOrig != null)) {
				 */
				if (mev != null) {
				// compute first version
				updatePreview(ValueChange.SLICE);
				
				}
				isStarted = true;

				// check whenever roi is modified to update accordingly
				roiListener = new RoiListener();
				imp.getCanvas().addMouseListener(roiListener);

			

				final Rectangle rect = roi.getBounds();

				for (final RefinedPeak<Point> peak : peaks) {

					final float x = peak.getFloatPosition(0);
					final float y = peak.getFloatPosition(1);

					final OvalRoi or = new OvalRoi(Util.round(x - sigma), Util.round(y - sigma),
							Util.round(sigma + sigma2), Util.round(sigma + sigma2));

					if (lookForMaxima)
						or.setStrokeColor(Color.red);
					else if (lookForMinima)
						or.setStrokeColor(Color.green);

					imp.setRoi(or);

				}
			
		
			} else if (!event.getValueIsAdjusting()) {
				while (isComputing) {
					SimpleMultiThreading.threadWait(10);
				}
				imp = WindowManager.getCurrentImage();
				sliceObserver = new SliceObserver(imp, new ImagePlusListener());
				imp.setPosition(channel, currentslice, imp.getFrame());
				if (currentslice > slicesize)  {
					IJ.log("Max frame number exceeded, moving to last frame instead");
					imp.setPosition(channel, slicesize, imp.getFrame());
					currentslice = slicesize;
				}

				if (imp.getType() == ImagePlus.COLOR_RGB || imp.getType() == ImagePlus.COLOR_256) {
					IJ.log("Color images are not supported, please convert to 8, 16 or 32-bit grayscale");
					return;
				}

				Roi roi = imp.getRoi();
				if (roi == null) {
					// IJ.log( "A rectangular ROI is required to define the
					// area..."
					// );
					imp.setRoi(standardRectangle);
					roi = imp.getRoi();
				}

				// copy the ImagePlus into an ArrayImage<FloatType> for faster
				// access
				CurrentView = getCurrentView(currentslice);
				MouseEvent mev = new MouseEvent(imp.getCanvas(), MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, 0, 0,
						10, false);
				/*
				 * if ((change == ValueChange.ROI || change == ValueChange.SIGMA ||
				 * change == ValueChange.MINMAX || change == ValueChange.SLICE || change
				 * == ValueChange.THRESHOLD && RoisOrig != null)) {
				 */
				if (mev != null) {
				// compute first version
				updatePreview(ValueChange.SLICE);
				}
				isStarted = true;

				// check whenever roi is modified to update accordingly
				roiListener = new RoiListener();
				imp.getCanvas().addMouseListener(roiListener);

			

				final Rectangle rect = roi.getBounds();

				for (final RefinedPeak<Point> peak : peaks) {

					final float x = peak.getFloatPosition(0);
					final float y = peak.getFloatPosition(1);

					final OvalRoi or = new OvalRoi(Util.round(x - sigma), Util.round(y - sigma),
							Util.round(sigma + sigma2), Util.round(sigma + sigma2));

					if (lookForMaxima)
						or.setStrokeColor(Color.red);
					else if (lookForMinima)
						or.setStrokeColor(Color.green);

					imp.setRoi(or);

				}
			}
		
			
		}
	}

	
	protected class ImagePlusListener implements SliceListener {
		@Override
		public void sliceChanged(ImagePlus arg0) {
			if (isStarted) {
				while (isComputing) {
					SimpleMultiThreading.threadWait(10);
				}
				updatePreview(ValueChange.SLICE);

			}
		}
	}

	public static SpimData2 loadSpimData( final File xml ) throws SpimDataException
	{
	XmlIoSpimData2 io = new XmlIoSpimData2( "" );
	return io.load( xml.getAbsolutePath() );
	}

	public static RandomAccessibleInterval<FloatType> getImg( final SpimData2 spimData2, final int viewSetupId, final int timepointId )
	{
		
		
	SetupImgLoader< ? > setup = spimData2.getSequenceDescription().getImgLoader().getSetupImgLoader( viewSetupId );
	
	RandomAccessibleInterval<FloatType> CurrentImage = setup.getFloatImage(timepointId, true);
	
	ImageJFunctions.show( CurrentImage );
	
	return CurrentImage;
	
	
	}
	/*
	public static void main(String[] args) throws SpimDataException {
		new ImageJ();

		RandomAccessibleInterval<FloatType> img = util.ImgLib2Util.openAs32Bit(new File("../res/one_division_4d-smallz-onlyz.tif"),
				new ArrayImgFactory<FloatType>());
		//SpimData2 data = loadSpimData( new File( "/home/preibisch/Documents/Microscopy/SPIM/Drosophila MS2-GFP//dataset.xml" ) );
		
		new BigDataInteractiveActiveContour(img).run(null);
	
		//new BigDataInteractiveActiveContour(data).run(null);
		
		
		
		
		
	}
	*/
}

