package snakes;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Dimension;
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import blobfinder.BlobfinderInteractiveDoG;
import blobfinder.BlobfinderInteractiveMSER;
import blobfinder.BlobfinderInteractiveSnake;
import blobfinder.FindblobsVia;
import blobfinder.SortListbyproperty;
import costMatrix.CostFunction;
import costMatrix.PixelratiowDistCostFunction;
import fiji.tool.SliceListener;
import fiji.tool.SliceObserver;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.EllipseRoi;
import ij.gui.GenericDialog;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.ProgressBar;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ColorProcessor;
import kdTreeBlobs.FlagNode;
import kdTreeBlobs.NNFlagsearchKDtree;
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
import net.imglib2.algorithm.componenttree.mser.Mser;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.algorithm.dog.DogDetection;
import net.imglib2.algorithm.localextrema.RefinedPeak;
import net.imglib2.algorithm.stats.Normalize;
import net.imglib2.converter.Converter;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.RealSum;
import net.imglib2.view.Views;
import net.imglib2.view.composite.NumericComposite;
import overlaytrack.DisplayGraph;
import overlaytrack.DisplaymodelGraph;
import preProcessing.GetLocalmaxmin;
import segmentation.SegmentbyWatershed;
import trackerType.BlobTracker;
import trackerType.KFsearch;
import trackerType.NNsearch;
import trackerType.TrackModel;
import util.Boundingboxes;

/**
 * An interactive tool for getting Intensity in ROI's using Active Contour
 * 
 * @author Varun Kapoor
 */

public class InteractiveActiveContour_ implements PlugIn {
	
	final int scrollbarSize = 1000;
	/** Store the individual features, and their values. */
	private final ConcurrentHashMap< String, Double > features = new ConcurrentHashMap< String, Double >();
	float sigma = 0.5f;
	float sigma2 = 0.5f;
	float threshold = 1f;
	float deltaMin = 0;
	// steps per octave
	public static int standardSensitivity = 4;
	int sensitivity = standardSensitivity;
	 JLabel label = new JLabel("Progress..");
     JProgressBar jpb;
     
     JFrame frame = new JFrame();
     JPanel panel = new JPanel();
	float imageSigma = 0.5f;
	float sigmaMin = 0.5f;
	float sigmaMax = 100f;
	float sizeXMin = 0;
	float sizeYMin = 0;

	float sizeXMax = 100f;
	float sizeYMax = 100f;
	int sigmaInit = 30;

	float minDiversityMin = 0;
	float minDiversityMax = 1;
	int thirdDimensionslider = 0;
	int thirdDimensionsliderInit = 1;
	int timeMin = 1;
	long minSize = 1;
	long maxSize = 1000;
	long minSizemin = 0;
	long minSizemax = 100;
	long maxSizemin = 100;
	long maxSizemax = 10000;
	int fourthDimensionslider = 0;
	int fourthDimensionsliderInit = 1;
	int sliceMin = 1;
	ArrayList<Roi> SnakeRoisA;
	ArrayList<Roi> SnakeRoisB;

	// ROI original
	int nbRois;
	Roi rorig = null;
	Roi processRoi = null;
	float thresholdMin = 0f;
	float thresholdMax = 1f;
	int thresholdInit = 1;
	float thresholdHoughMin = 0f;
	float thresholdHoughMax = 1f;
	int thresholdHoughInit = 1;
	
	float sizeX = 0;
	float sizeY = 0;
	public int radius = 1;
	public float maxVar = 1;
	int Progressmin = 0;
	int Progressmax = 100;
	int max = Progressmax;
	public float minDiversity = 1;
	public float thresholdHough = 1;
	FloatType minval = new FloatType(0);
	FloatType maxval = new FloatType(1);
	boolean TrackinT = true;

	double minIntensityImage = Double.NaN;
	double maxIntensityImage = Double.NaN;
	String usefolder = IJ.getDirectory("imagej");
	String addTrackToName = "TrackedBlobsID";
	Color colorDraw = Color.yellow;
	FinalInterval interval;
	SliceObserver sliceObserver;
	RoiListener roiListener;
	ImagePlus imp;
	ImagePlus measureimp;
	boolean darktobright = false;
	int channel = 0;
	RandomAccessibleInterval<FloatType> currentimg;
	RandomAccessibleInterval<FloatType> othercurrentimg;
	RandomAccessibleInterval<FloatType> originalimgA;
	RandomAccessibleInterval<FloatType> originalimgB;
	ImageStack snakestack;
	ImageStack measuresnakestack;

	
	ArrayList<double[]> AllmeanCovar;
	float deltaMax = 400f;
	float maxVarMin = 0;
	float maxVarMax = 1;
	private SimpleWeightedGraph<SnakeObject, DefaultWeightedEdge> graphZ;
	// Dimensions of the stack :
	float alpha = 0.5f;
	float beta = 0.5f;

	int thirdDimension;
	int fourthDimension;
	public int minDiversityInit = 1;
	int thirdDimensionSize = 0;
	int fourthDimensionSize = 0;
	ArrayList<ABSnakeFast> snakeoverlay;
	ArrayList<SnakeObject> currentsnakes;
	int length = 0;
	int height = 0;
	int sizeXinit = 0;
	int sizeYinit = 0;
	RandomAccessibleInterval<FloatType> CurrentView;
	RandomAccessibleInterval<FloatType> otherCurrentView;
	ArrayList<RefinedPeak<Point>> peaks;
	Color originalColor = new Color(0.8f, 0.8f, 0.8f);
	Color inactiveColor = new Color(0.95f, 0.95f, 0.95f);
	public Rectangle standardRectangle;
	public EllipseRoi standardEllipse;
	boolean isComputing = false;
	boolean findBlobsViaMSER = false;
	boolean findBlobsViaDOG = false;
	boolean findBlobsViaSEGMSER = false;
	boolean findBlobsViaSEGDOG = false;
	boolean NormalizeImage = false;
	boolean Mediancurr = false;
	boolean MedianAll = false;
	boolean isStarted = false;
	boolean enableSigma2 = false;
	boolean sigma2IsAdjustable = true;
	boolean propagate = true;
	boolean updateThreshold = false;
	boolean lookForMinima = false;
	boolean Auto = false;
	boolean lookForMaxima = true;
	boolean showMSER = false;
	boolean showDOG = false;
	boolean showSegMSER = false;
	boolean showSegDOG = false;
	float delta = 1f;
	int deltaInit = 10;
	int maxVarInit = 1;
	int Maxlabel;
	int alphaInit = 0;
	int betaInit = 0;
	int minSizeInit = 1;
	int maxSizeInit = 100;
	boolean showKalman = true;
	boolean showNN = false;
	boolean SaveTxt = true;
	boolean SaveXLS = true;
	ImagePlus impcopy;
	double CalibrationX;
	double CalibrationY;
	boolean showNew = false;
	BlobTracker blobtracker;
	CostFunction<SnakeObject, SnakeObject> UserchosenCostFunction;

	float initialSearchradius = 10;
	float maxSearchradius = 15;
	int missedframes = 20;
	public int initialSearchradiusInit = (int) initialSearchradius;
	public float initialSearchradiusMin = 0;
	public float initialSearchradiusMax = 100;
	public float alphaMin = 0;
	public float alphaMax = 1;
	public float betaMin = 0;
	public float betaMax = 1;
	MserTree<UnsignedByteType> newtree;
	public int maxSearchradiusInit = (int) maxSearchradius;
	public float maxSearchradiusMin = 10;
	public float maxSearchradiusMax = 500;
	RandomAccessibleInterval<UnsignedByteType> newimg;
	public int missedframesInit = missedframes;
	public float missedframesMin = 0;
	public float missedframesMax = 100;
	RandomAccessibleInterval<IntType> intimg;
	ArrayList<ArrayList<SnakeObject>> AllSliceSnakes;
	ArrayList<ArrayList<SnakeObject>> All3DSnakes;
	private ArrayList<SnakeObject> ProbBlobs;
	boolean displayBitimg = false;
	boolean displayWatershedimg = false;
	boolean showProgress = false;
	ArrayList<ComSnake> finalRois;
	ArrayList<Roi> Rois;
	ArrayList<Roi> NearestNeighbourRois;
	ArrayList<Roi> BiggerRois;

	public static enum ValueChange {
		SIGMA, THRESHOLD, ROI, MINMAX, ALL, THIRDDIM, FOURTHDIM, maxSearch, iniSearch, missedframes, MINDIVERSITY, DELTA,
		MINSIZE, MAXSIZE, MAXVAR, DARKTOBRIGHT, FindBlobsVia, SHOWMSER, SHOWDOG, NORMALIZE, MEDIAN, THIRDDIMTrack, FOURTHDIMTrack, 
		SizeX, SizeY, SHOWNEW, Beta, Alphapart, Alpha, Segmentation, SHOWSEGMSER, SHOWSEGDOG, DISPLAYBITIMG, DISPLAYWATERSHEDIMG, SHOWPROGRESS
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

		thirdDimensionslider = value;
		thirdDimensionsliderInit = 1;

	}

	public void setInitialSlice(final int value) {

		fourthDimensionslider = value;
		fourthDimensionsliderInit = 1;

	}

	public double getSigma2() {
		return sigma2;
	}

	public double getThreshold() {
		return threshold;
	}

	public boolean getFindBlobsViaMSER() {
		return findBlobsViaMSER;
	}

	public void setFindBlobsViaMSER(final boolean findBlobsViaMSER) {
		this.findBlobsViaMSER = findBlobsViaMSER;
	}

	public boolean getFindBlobsViaDOG() {
		return findBlobsViaDOG;
	}

	public void setFindBlobsViaDOG(final boolean findBlobsViaDOG) {
		this.findBlobsViaDOG = findBlobsViaDOG;
	}

	public int getTime() {

		return thirdDimensionslider;

	}

	public int getSlice() {

		return fourthDimensionslider;

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

	public void setThresholdHough(final float value) {
		thresholdHough = value;
		thresholdHoughInit = computeScrollbarPositionFromValue(thresholdHough, thresholdHoughMin, thresholdHoughMax, scrollbarSize);
	}

	
	
	public double getThresholdHoughMin() {
		return thresholdMin;
	}

	public double getThresholdHoughMax() {
		return thresholdMax;
	}

	public void setthresholdHoughMax(final float thresholdHoughMax) {

		this.thresholdHoughMax = thresholdHoughMax;

	}

	public void setthresholdHoughMin(final float thresholdHoughMin) {

		this.thresholdHoughMin = thresholdHoughMin;

	}

	public void setThreshold(final float value) {
		threshold = value;
		thresholdInit = computeScrollbarPositionFromValue(threshold, thresholdMin, thresholdMax, scrollbarSize);
	}
	
	
	public void setTime(final int value) {
		thirdDimensionslider = value;
		thirdDimensionsliderInit = 1;
	}

	public int getTimeMax() {

		return thirdDimensionSize;
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

	// for the case that it is needed again, we can save one conversion
	public RandomAccessibleInterval<FloatType> getotherConvertedImage() {
		return otherCurrentView;
	}

	public InteractiveActiveContour_() {
	};

	public InteractiveActiveContour_(final ImagePlus imp) {
		this.imp = imp;
		standardRectangle = new Rectangle(20, 20, imp.getWidth() - 40, imp.getHeight() - 40);
		originalimgA = ImageJFunctions.convertFloat(imp.duplicate());
		height = imp.getHeight();
		length = imp.getWidth();
	}

	public InteractiveActiveContour_(final RandomAccessibleInterval<FloatType> originalimgA) {
		this.originalimgA = originalimgA;
		standardRectangle = new Rectangle(20, 20, (int) originalimgA.dimension(0) - 40,
				(int) originalimgA.dimension(1) - 40);

	}

	public InteractiveActiveContour_(final RandomAccessibleInterval<FloatType> originalimgA,
			final RandomAccessibleInterval<FloatType> originalimgB) {
		this.originalimgA = originalimgA;
		this.originalimgB = originalimgB;
		standardRectangle = new Rectangle(20, 20, (int) originalimgA.dimension(0) - 40,
				(int) originalimgA.dimension(1) - 40);

	}

	public void setMinIntensityImage(final double min) {
		this.minIntensityImage = min;
	}

	public void setMaxIntensityImage(final double max) {
		this.maxIntensityImage = max;
	}

	@Override
	public void run(String arg) {

		
	
       // Progressbar color
	//	UIManager.put("ProgressBar.selectionForeground", Color.BLUE);
	//	UIManager.put("ProgressBar.foreground", Color.BLUE);
		UIManager.put("ProgressBar.font",Font.BOLD);
		//UIManager.put("ProgressBar.background", Color.BLUE);
		
		jpb = new JProgressBar();
		Rois = new ArrayList<Roi>();
		peaks = new ArrayList<RefinedPeak<Point>>();
		
		if (originalimgA.numDimensions() < 3) {

			thirdDimensionSize = 1;
			fourthDimensionSize = 1;
		}

		if (originalimgA.numDimensions() == 3) {

			// We have XYZ or XYT image, we will run snakes along the third
			// dimension

			thirdDimension = 1;
			thirdDimensionSize = (int) originalimgA.dimension(2);
			fourthDimensionSize = 1;

		}

		if (originalimgA.numDimensions() == 4) {

			// We have XYZT image and snakes will run over the third dimension
			// (Z) for each point inf the fourth dimension (T)

			thirdDimension = 1;
			fourthDimension = 1;

			thirdDimensionSize = (int) originalimgA.dimension(2);
			fourthDimensionSize = (int) originalimgA.dimension(3);

		}

		if (originalimgA.numDimensions() > 4) {

			System.out.println("Only four dimensions are currently supported.");
			return;

		}
		snakestack = new ImageStack((int) originalimgA.dimension(0), (int) originalimgA.dimension(1),
				java.awt.image.ColorModel.getRGBdefault());
		measuresnakestack = new ImageStack((int) originalimgB.dimension(0), (int) originalimgB.dimension(1),
				java.awt.image.ColorModel.getRGBdefault());
		CurrentView = getCurrentView();
		otherCurrentView = getotherCurrentView();
		final Float houghval = AutomaticThresholding(CurrentView);

		// Get local Minima in scale space to get Max rho-theta points
		float minPeakValue = houghval;

		threshold = minPeakValue;

		thresholdMax = (float) (minPeakValue );

		thresholdMin = (float) (0);

		thresholdHough = minPeakValue;

		thresholdHoughMax = (float) (minPeakValue/10 );

		thresholdHoughMin = (float) (0);
		

		setthresholdMin(thresholdMin);
		setthresholdMax(thresholdMax);
		setThreshold(threshold/40);
		
		setthresholdHoughMin(thresholdHoughMin);
		setthresholdHoughMax(thresholdHoughMax);
		setThresholdHough(thresholdHough/40);
		
		measureimp = ImageJFunctions.show(otherCurrentView);
		measureimp.setTitle("CurrentView of Measurment image");
		imp = ImageJFunctions.show(CurrentView);
		imp.setTitle("CurrentView of Tracking image");
		
		
		All3DSnakes = new ArrayList<ArrayList<SnakeObject>>();
		Rois = new ArrayList<Roi>();
		NearestNeighbourRois = new ArrayList<Roi>();
		SnakeRoisA = new ArrayList<Roi>();
		SnakeRoisB = new ArrayList<Roi>();
		impcopy = imp.duplicate();
		Roi roi = imp.getRoi();

		if (roi == null) {
			// IJ.log( "A rectangular ROI is required to define the area..." );
			imp.setRoi(standardRectangle);
			roi = imp.getRoi();
		}

		if (roi.getType() != Roi.RECTANGLE) {
			IJ.log("Only rectangular rois are supported...");
			return;
		}

		// show the interactive kit
		// displaySliders();

		Card();
		// add listener to the imageplus slice slider
		sliceObserver = new SliceObserver(imp, new ImagePlusListener());
		// compute first version#

		updatePreview(ValueChange.ALL);
		isStarted = true;

		// check whenever roi is modified to update accordingly
		roiListener = new RoiListener();
		imp.getCanvas().addMouseListener(roiListener);

		IJ.log("Fourth Dimension Size" + fourthDimensionSize + " Third Dimension Size " + thirdDimensionSize);

	}

	public void reset() {
		
		graphZ = new SimpleWeightedGraph<SnakeObject, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		final Iterator<SnakeObject> it = AllSliceSnakes.get(0).iterator();
		while (it.hasNext()) {
			graphZ.addVertex(it.next());
		}
		
	}

	private boolean Dialogue() {
		GenericDialog gd = new GenericDialog("Move in third Dimension");

		if (thirdDimensionSize > 1) {
			gd.addNumericField("Move in third dimensions", thirdDimension, 0);

		}

		gd.showDialog();
		if (thirdDimensionSize > 1) {
			thirdDimension = (int) gd.getNextNumber();

			thirdDimensionslider = thirdDimension;
		}
		return !gd.wasCanceled();
	}

	private boolean DialogueSlice() {
		GenericDialog gd = new GenericDialog("Move in fourth Dimension");

		if (fourthDimensionSize > 1) {
			gd.addNumericField("Move in fourth dimension", fourthDimension, 0);

		}

		gd.showDialog();
		if (fourthDimensionSize > 1) {
			fourthDimension = (int) gd.getNextNumber();

			fourthDimensionslider = fourthDimension;
		}
		return !gd.wasCanceled();
	}

	private boolean Dialoguesec() {
		GenericDialog gd = new GenericDialog("Choose last third Dimension co-ordinate and track in Z/T");

		if (thirdDimensionSize > 1) {
			gd.addNumericField("Run till", thirdDimensionSize, 0);
			gd.addCheckbox("Track in T", TrackinT);

			assert (int) gd.getNextNumber() > 1;
		}

		gd.showDialog();
		thirdDimensionSize = (int) gd.getNextNumber();
		if (thirdDimensionSize == 1)
			thirdDimensionSize = 2;

		return !gd.wasCanceled();
	}

	private boolean DialogueRedo() {
		GenericDialog gd = new GenericDialog("Redo Snakes in user chosen View");

		if (thirdDimensionSize > 1) {
			gd.addNumericField("Enter third Dimension", thirdDimension, 0);
			if (fourthDimensionSize > 1)
				gd.addNumericField("Enter fourth Dimension", fourthDimension, 0);

			assert (int) gd.getNextNumber() > 1;
		}

		gd.showDialog();

		if (thirdDimensionSize > 1) {
			thirdDimension = (int) gd.getNextNumber();

			if (fourthDimensionSize > 1)
				fourthDimension = (int) gd.getNextNumber();

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

		RoiManager roimanager = RoiManager.getInstance();

		if (roimanager == null) {
			roimanager = new RoiManager();
		}

		// Re-compute MSER ellipses if neccesary

		if (change == ValueChange.THIRDDIM || change == ValueChange.FOURTHDIM) {
			System.out.println("Current Time point: " + thirdDimension);

			if (imp != null)
				imp.close();
			imp = ImageJFunctions.show(CurrentView);
			imp.setTitle("Current View in third dimension: " + " " + thirdDimension + " " + "fourth dimension: " + " "
					+ fourthDimension);

		}

		boolean roiChanged = false;
		Overlay overlay = imp.getOverlay();
		if (overlay == null) {
			overlay = new Overlay();
			imp.setOverlay(overlay);
		}

		overlay.clear();

		
		
		
		if (change == ValueChange.SHOWNEW) {

			measureimp = ImageJFunctions.show(otherCurrentView);

			if (finalRois != null) {
				Roi roi = measureimp.getRoi();
				if (roi == null || roi.getType() != Roi.RECTANGLE) {
					measureimp.setRoi(new Rectangle(standardRectangle));
					roi = measureimp.getRoi();
					roiChanged = true;
				}
				Overlay overlaymeasure = measureimp.getOverlay();
				if (overlaymeasure == null) {
					overlaymeasure = new Overlay();
					measureimp.setOverlay(overlaymeasure);
				}

				overlaymeasure.clear();

				for (int index = 0; index < finalRois.size(); ++index) {

					int width = (int) finalRois.get(index).rois.getBounds().getWidth();
					int height = (int) finalRois.get(index).rois.getBounds().getHeight();

					final OvalRoi or = new OvalRoi(Util.round(finalRois.get(index).com[0] - (width + sizeX) / 2),
							Util.round(finalRois.get(index).com[1] - (height + sizeY) / 2), Util.round(width + sizeX),
							Util.round(height + sizeY));

					or.setStrokeColor(Color.red);
					overlaymeasure.add(or);
					roimanager.addRoi(or);

				}

			}

		}

		if (change == ValueChange.THIRDDIMTrack || change == ValueChange.FOURTHDIMTrack) {
			
			if (Rois!= null)
			Rois.clear();
			// imp = ImageJFunctions.wrapFloat(CurrentView, "current");

			long[] min = { (long) standardRectangle.getMinX(), (long) standardRectangle.getMinY() };
			long[] max = { (long) standardRectangle.getMaxX(), (long) standardRectangle.getMaxY() };
			interval = new FinalInterval(min, max);

			currentimg = extractImage(CurrentView);
			othercurrentimg = extractotherImage(otherCurrentView);

			newimg = copytoByteImage(currentimg);

			final List<FlagNode<double[]>> targetNodes = new ArrayList<FlagNode<double[]>>(finalRois.size());
			final List<RealPoint> targetCoords = new ArrayList<RealPoint>(finalRois.size());

			if (showMSER) {

				overlay.clear();
				IJ.log(" Computing the Component tree");

				newtree = MserTree.buildMserTree(newimg, delta, minSize, maxSize, maxVar, minDiversity, darktobright);
				Rois = getcurrentRois(newtree);
                ArrayList<double[]> centerRoi = getRoiMean(newtree);
                
                
                
				for (int index = 0; index < centerRoi.size(); ++index) {

					double[] center = new double[]{centerRoi.get(index)[0], centerRoi.get(index)[1]};
					targetCoords.add(new RealPoint(center));
					targetNodes.add(new FlagNode<double[]>(centerRoi.get(index)));
					Roi or = Rois.get(index);

					or.setStrokeColor(Color.red);
					overlay.add(or);
					roimanager.addRoi(or);
				}

			}

			if (showDOG) {

				overlay.clear();
				// if we got some mouse click but the ROI did not change we
				// can return
				if (!roiChanged && change == ValueChange.ROI) {
					isComputing = false;
					return;
				}

				final DogDetection.ExtremaType type;

				if (lookForMaxima)
					type = DogDetection.ExtremaType.MINIMA;
				else
					type = DogDetection.ExtremaType.MAXIMA;

				final DogDetection<FloatType> newdog = new DogDetection<FloatType>(Views.extendBorder(currentimg),
						interval, new double[] { 1, 1 }, sigma, sigma2, type, threshold, true);
				
				peaks = newdog.getSubpixelPeaks();

				Rois = getcurrentRois(peaks);
				for (int index = 0; index < peaks.size(); ++index) {

					double[] center = new double[]{peaks.get(index).getDoublePosition(0), peaks.get(index).getDoublePosition(1)};
					
					targetCoords.add(new RealPoint(center));
					targetNodes.add(new FlagNode<double[]>(center));
					Roi or = Rois.get(index);

					or.setStrokeColor(Color.red);
					overlay.add(or);
					roimanager.addRoi(or);
				}

			}
			
			
			if (showSegMSER){
				overlay.clear();
				if (!roiChanged && change == ValueChange.ROI) {
					isComputing = false;
					return;
				}
				roimanager.close();

				roimanager = new RoiManager();

				IJ.log(" Computing the Component tree");
				
				
				for (int label = 1; label < Maxlabel - 1; label++ ){
					
	              RandomAccessibleInterval<FloatType> currentsegimg = getCurrentSegment(label);
	              RandomAccessibleInterval<UnsignedByteType> currentnewimg = copytoByteImage(currentsegimg);
	              MserTree<UnsignedByteType> localtree = MserTree.buildMserTree(currentnewimg, delta, minSize, maxSize, maxVar, minDiversity, darktobright);
				  ArrayList<Roi> currentroi = getcurrentRois(localtree);
	              Rois.addAll(currentroi); 
					
				}

				  ArrayList<double[]> centerRoi = getRoiMean(newtree);
				for (int index = 0; index < centerRoi.size(); ++index) {

					double[] center = new double[]{centerRoi.get(index)[0], centerRoi.get(index)[1]};
					targetCoords.add(new RealPoint(center));
					targetNodes.add(new FlagNode<double[]>(centerRoi.get(index)));
					
					Roi or = Rois.get(index);

					or.setStrokeColor(Color.red);
					overlay.add(or);
					roimanager.addRoi(or);
				}
				
				

				
			
				
			}
			
			
			if (showSegDOG){
				overlay.clear();
				if (!roiChanged && change == ValueChange.ROI) {
					isComputing = false;
					return;
				}
				roimanager.close();

				roimanager = new RoiManager();

				final DogDetection.ExtremaType type;

				if (lookForMaxima)
					type = DogDetection.ExtremaType.MINIMA;
				else
					type = DogDetection.ExtremaType.MAXIMA;
				
				
				for (int label = 1; label < Maxlabel - 1; label++ ){
					
	              RandomAccessibleInterval<FloatType> currentsegimg = getCurrentSegment(label);
	              
	              final DogDetection<FloatType> newdog = new DogDetection<FloatType>(Views.extendBorder(currentsegimg), interval,
	  					new double[] { 1, 1 }, sigma, sigma2, type, threshold, true);

	              ArrayList<RefinedPeak<Point>> localpeaks = newdog.getSubpixelPeaks();
	              ArrayList<Roi> currentroi = getcurrentRois(localpeaks);
	              Rois.addAll(currentroi); 
	              peaks.addAll(localpeaks);
	              
	              
					
				}
				
				for (int index = 0; index < Rois.size(); ++index) {
                    double[] center = new double[]{peaks.get(index).getDoublePosition(0), peaks.get(index).getDoublePosition(1)};
					
					targetCoords.add(new RealPoint(center));
					targetNodes.add(new FlagNode<double[]>(center));
					Roi or = Rois.get(index);

					or.setStrokeColor(Color.red);
					overlay.add(or);
					roimanager.addRoi(or);
				}
			
				
			}

			

		}

		if (change == ValueChange.ROI) {

			Roi roi = imp.getRoi();
			if (roi == null || roi.getType() != Roi.RECTANGLE) {
				imp.setRoi(new Rectangle(standardRectangle));
				roi = imp.getRoi();
				roiChanged = true;
			}

			final Rectangle rect = roi.getBounds();

			standardRectangle = rect;
			long[] min = { (long) standardRectangle.getMinX(), (long) standardRectangle.getMinY() };
			long[] max = { (long) standardRectangle.getMaxX(), (long) standardRectangle.getMaxY() };
			interval = new FinalInterval(min, max);
			currentimg = extractImage(CurrentView);
			othercurrentimg = extractotherImage(otherCurrentView);

			newimg = copytoByteImage(currentimg);
			final Float houghval = AutomaticThresholding(currentimg);

			// Get local Minima in scale space to get Max rho-theta points

			threshold = (float) getThreshold();

			thresholdMax = (float) getThresholdMax();

			thresholdMin = (float) getThresholdMin();

			
		}
		
		
		if (change == ValueChange.SHOWSEGMSER){
			overlay.clear();
			if (Rois!= null)
				Rois.clear();
			
			if (!roiChanged && change == ValueChange.ROI) {
				isComputing = false;
				return;
			}
			roimanager.close();

			roimanager = new RoiManager();

			IJ.log(" Computing the Component tree");
			
			
			for (int label = 1; label < Maxlabel - 1; label++ ){
				
              RandomAccessibleInterval<FloatType> currentsegimg = getCurrentSegment(label);
              RandomAccessibleInterval<UnsignedByteType> currentnewimg = copytoByteImage(currentsegimg);
              MserTree<UnsignedByteType> localtree = MserTree.buildMserTree(currentnewimg, delta, minSize, maxSize, maxVar, minDiversity, darktobright);
			  ArrayList<Roi> currentroi = getcurrentRois(localtree);
              Rois.addAll(currentroi); 
				
			}

            IJ.log("MSER parameters:" +" " + " thirdDimension: " +" " + thirdDimension +" " + "fourthDimension: " +" " + fourthDimension);
            IJ.log("Delta " +" " + delta +" " + "minSize " +" " + minSize +" " + "maxSize " +" " + maxSize +" " + " maxVar " +" " + maxVar +" " + "minDIversity " +" " + minDiversity);
			for (int index = 0; index < Rois.size(); ++index) {

				Roi or = Rois.get(index);

				or.setStrokeColor(Color.red);
				overlay.add(or);
				roimanager.addRoi(or);
			}
		
			
			
		}
		if (change == ValueChange.SHOWSEGDOG){
			overlay.clear();
			if (Rois!= null)
				Rois.clear();
			if (!roiChanged && change == ValueChange.ROI) {
				isComputing = false;
				return;
			}
			roimanager.close();

			roimanager = new RoiManager();

			final DogDetection.ExtremaType type;

			if (lookForMaxima)
				type = DogDetection.ExtremaType.MINIMA;
			else
				type = DogDetection.ExtremaType.MAXIMA;
			
			
			for (int label = 1; label < Maxlabel - 1; label++ ){
				
              RandomAccessibleInterval<FloatType> currentsegimg = getCurrentSegment(label);
              
              final DogDetection<FloatType> newdog = new DogDetection<FloatType>(Views.extendBorder(currentsegimg), interval,
  					new double[] { 1, 1 }, sigma, sigma2, type, threshold, true);

              ArrayList<RefinedPeak<Point>> localpeaks = newdog.getSubpixelPeaks();
              ArrayList<Roi> currentroi = getcurrentRois(localpeaks);
              Rois.addAll(currentroi); 
              
              
				
			}

			 IJ.log("DoG parameters:" +" " + " thirdDimension: " +" " + thirdDimension +" " + "fourthDimension: " +" " + fourthDimension);
             IJ.log("Sigma " +" " + sigma +" " + "Sigma2 " +" " + sigma2 +" " + "Threshold " +" " + threshold);
			
			
			for (int index = 0; index < Rois.size(); ++index) {

				Roi or = Rois.get(index);

				or.setStrokeColor(Color.red);
				overlay.add(or);
				roimanager.addRoi(or);
			}
		
			
			
		}
		if (change == ValueChange.SHOWMSER) {
			overlay.clear();
			// check if Roi changed

			// if we got some mouse click but the ROI did not change we can
			// return
			if (!roiChanged && change == ValueChange.ROI) {
				isComputing = false;
				return;
			}
			roimanager.close();

			roimanager = new RoiManager();

			IJ.log(" Computing the Component tree");

			newtree = MserTree.buildMserTree(newimg, delta, minSize, maxSize, maxVar, minDiversity, darktobright);
			Rois = getcurrentRois(newtree);

            IJ.log("MSER parameters:" +" " + " thirdDimension: " +" " + thirdDimension +" " + "fourthDimension: " +" " + fourthDimension);
            IJ.log("Delta " +" " + delta +" " + "minSize " +" " + minSize +" " + "maxSize " +" " + maxSize +" " + " maxVar " +" " + maxVar +" " + "minDIversity " +" " + minDiversity);
			for (int index = 0; index < Rois.size(); ++index) {

				Roi or = Rois.get(index);

				or.setStrokeColor(Color.red);
				overlay.add(or);
				roimanager.addRoi(or);
			}
		}

		if (change == ValueChange.Segmentation) {

			IJ.log("Doing watershedding on the distance transformed image ");
			
			RandomAccessibleInterval<BitType> bitimg = new ArrayImgFactory<BitType>().create(newimg, new BitType());
			GetLocalmaxmin.ThresholdingBit(newimg, bitimg, thresholdHough);

			if (displayBitimg)
				ImageJFunctions.show(bitimg);

			SegmentbyWatershed<UnsignedByteType> WaterafterDisttransform = new SegmentbyWatershed<UnsignedByteType>(
					newimg, bitimg);
			WaterafterDisttransform.checkInput();
			WaterafterDisttransform.process();
			intimg = WaterafterDisttransform.getResult();
			Maxlabel = WaterafterDisttransform.GetMaxlabelsseeded(intimg);
			if (displayWatershedimg)
				ImageJFunctions.show(intimg);

		}
		
		if (change == ValueChange.SHOWDOG) {

			// check if Roi changed

			overlay.clear();
			// if we got some mouse click but the ROI did not change we can
			// return
			if (!roiChanged && change == ValueChange.ROI) {
				isComputing = false;
				return;
			}
			roimanager.close();

			roimanager = new RoiManager();

			final DogDetection.ExtremaType type;

			if (lookForMaxima)
				type = DogDetection.ExtremaType.MINIMA;
			else
				type = DogDetection.ExtremaType.MAXIMA;

			final DogDetection<FloatType> newdog = new DogDetection<FloatType>(Views.extendBorder(currentimg), interval,
					new double[] { 1, 1 }, sigma, sigma2, type, threshold, true);

			 IJ.log("DoG parameters:" +" " + " thirdDimension: " +" " + thirdDimension +" " + "fourthDimension: " +" " + fourthDimension);
             IJ.log("Sigma " +" " + sigma +" " + "Sigma2 " +" " + sigma2 +" " + "Threshold " +" " + threshold);
			peaks = newdog.getSubpixelPeaks();

			Rois = getcurrentRois(peaks);

			for (int index = 0; index < Rois.size(); ++index) {

				Roi or = Rois.get(index);

				or.setStrokeColor(Color.red);
				overlay.add(or);
				roimanager.addRoi(or);
			}

		}

		// if we got some mouse click but the ROI did not change we can return
		if (!roiChanged && change == ValueChange.ROI) {
			isComputing = false;
			return;
		}

		isComputing = false;
	}

	protected class MserListener implements ItemListener {
		@Override
		public void itemStateChanged(final ItemEvent arg0) {
			boolean oldState = findBlobsViaMSER;

			if (arg0.getStateChange() == ItemEvent.DESELECTED)
				findBlobsViaMSER = false;
			else if (arg0.getStateChange() == ItemEvent.SELECTED) {

				findBlobsViaMSER = true;
				findBlobsViaDOG = false;
				updatePreview(ValueChange.ROI);

				panelSecond.removeAll();
				
				final GridBagLayout layout = new GridBagLayout();
				final GridBagConstraints c = new GridBagConstraints();

				panelSecond.setLayout(layout);
				final Label Name = new Label("Step 2", Label.CENTER);
				panelSecond.add(Name, c);
				
				final Scrollbar deltaS = new Scrollbar(Scrollbar.HORIZONTAL, deltaInit, 10, 0, 10 + scrollbarSize);
				final Scrollbar maxVarS = new Scrollbar(Scrollbar.HORIZONTAL, maxVarInit, 10, 0, 10 + scrollbarSize);
				final Scrollbar minDiversityS = new Scrollbar(Scrollbar.HORIZONTAL, minDiversityInit, 10, 0,
						10 + scrollbarSize);
				final Scrollbar minSizeS = new Scrollbar(Scrollbar.HORIZONTAL, minSizeInit, 10, 0, 10 + scrollbarSize);
				final Scrollbar maxSizeS = new Scrollbar(Scrollbar.HORIZONTAL, maxSizeInit, 10, 0, 10 + scrollbarSize);
				final Button ComputeTree = new Button("Compute Tree and display");
				maxVar = computeValueFromScrollbarPosition(maxVarInit, maxVarMin, maxVarMax, scrollbarSize);
				delta = computeValueFromScrollbarPosition(deltaInit, deltaMin, deltaMax, scrollbarSize);
				minDiversity = computeValueFromScrollbarPosition(minDiversityInit, minDiversityMin, minDiversityMax,
						scrollbarSize);
				minSize = (int) computeValueFromScrollbarPosition(minSizeInit, minSizemin, minSizemax, scrollbarSize);
				maxSize = (int) computeValueFromScrollbarPosition(maxSizeInit, maxSizemin, maxSizemax, scrollbarSize);

				final Checkbox min = new Checkbox("Look for Minima ", darktobright);

				final Label deltaText = new Label("delta = ", Label.CENTER);
				final Label maxVarText = new Label("maxVar = ", Label.CENTER);
				final Label minDiversityText = new Label("minDiversity = ", Label.CENTER);
				final Label minSizeText = new Label("MinSize = ", Label.CENTER);
				final Label maxSizeText = new Label("MaxSize = ", Label.CENTER);
				final Label MSparam = new Label("Determine MSER parameters");
				MSparam.setBackground(new Color(1, 0, 1));
				MSparam.setForeground(new Color(255, 255, 255));
				/* Location */
				panelSecond.setLayout(layout);

				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 0;
				c.gridy = 0;
				c.weightx = 4;
				c.weighty = 1.5;

				++c.gridy;
				c.insets = new Insets(10, 175, 0, 175);
				panelSecond.add(MSparam, c);

				++c.gridy;
				c.insets = new Insets(10, 175, 0, 175);
				panelSecond.add(deltaText, c);

				++c.gridy;
				c.insets = new Insets(10, 175, 0, 175);
				panelSecond.add(deltaS, c);

				++c.gridy;
				c.insets = new Insets(10, 175, 0, 175);
				panelSecond.add(maxVarText, c);

				++c.gridy;
				c.insets = new Insets(10, 175, 0, 175);
				panelSecond.add(maxVarS, c);

				++c.gridy;
				c.insets = new Insets(10, 175, 0, 175);
				panelSecond.add(minDiversityText, c);

				++c.gridy;
				c.insets = new Insets(10, 175, 0, 175);
				panelSecond.add(minDiversityS, c);

				++c.gridy;
				c.insets = new Insets(10, 175, 0, 175);
				panelSecond.add(minSizeText, c);

				++c.gridy;
				c.insets = new Insets(10, 175, 0, 175);
				panelSecond.add(minSizeS, c);

				++c.gridy;
				c.insets = new Insets(10, 175, 0, 175);
				panelSecond.add(maxSizeText, c);

				++c.gridy;
				c.insets = new Insets(10, 175, 0, 175);
				panelSecond.add(maxSizeS, c);

				++c.gridy;
				c.insets = new Insets(10, 175, 0, 175);
				panelSecond.add(min, c);

				++c.gridy;
				c.insets = new Insets(10, 175, 0, 175);
				panelSecond.add(ComputeTree, c);

				deltaS.addAdjustmentListener(new DeltaListener(deltaText, deltaMin, deltaMax, scrollbarSize, deltaS));

				maxVarS.addAdjustmentListener(
						new maxVarListener(maxVarText, maxVarMin, maxVarMax, scrollbarSize, maxVarS));

				minDiversityS.addAdjustmentListener(new minDiversityListener(minDiversityText, minDiversityMin,
						minDiversityMax, scrollbarSize, minDiversityS));

				minSizeS.addAdjustmentListener(
						new minSizeListener(minSizeText, minSizemin, minSizemax, scrollbarSize, minSizeS));

				maxSizeS.addAdjustmentListener(
						new maxSizeListener(maxSizeText, maxSizemin, maxSizemax, scrollbarSize, maxSizeS));

				min.addItemListener(new DarktobrightListener());
				ComputeTree.addActionListener(new ComputeTreeListener());
				panelSecond.repaint();
				panelSecond.validate();
				Cardframe.pack();
			}

			if (findBlobsViaMSER != oldState) {
				while (isComputing)
					SimpleMultiThreading.threadWait(10);

				updatePreview(ValueChange.FindBlobsVia);
			}
		}
	}

	protected class SegMserListener implements ItemListener {
		@Override
		public void itemStateChanged(final ItemEvent arg0) {
			boolean oldState = findBlobsViaSEGMSER;

			if (arg0.getStateChange() == ItemEvent.DESELECTED)
				findBlobsViaSEGMSER = false;
			else if (arg0.getStateChange() == ItemEvent.SELECTED) {

				findBlobsViaSEGMSER = true;
				findBlobsViaSEGDOG = true;
				findBlobsViaMSER = false;
				findBlobsViaDOG = false;
				updatePreview(ValueChange.ROI);

				panelSecond.removeAll();
				
				final GridBagLayout layout = new GridBagLayout();
				final GridBagConstraints c = new GridBagConstraints();

				panelSecond.setLayout(layout);
				final Label Name = new Label("Step 2", Label.CENTER);
				panelSecond.add(Name, c);
			

				// IJ.log("Determining the initial threshold for the image");
				// thresholdHoughInit =
				// GlobalThresholding.AutomaticThresholding(currentPreprocessedimg);
				final Scrollbar thresholdSHough = new Scrollbar(Scrollbar.HORIZONTAL, thresholdHoughInit, 10, 0,
						10 + scrollbarSize);
				thresholdHough = computeValueFromScrollbarPosition(thresholdHoughInit, thresholdHoughMin, thresholdHoughMax, scrollbarSize);


				final Checkbox displayBit = new Checkbox("Display Bitimage ", displayBitimg);
				final Checkbox displayWatershed = new Checkbox("Display Watershedimage ", displayWatershedimg);
				final Label thresholdText = new Label("thresholdValue = ", Label.CENTER);
			
				final Button Dowatershed = new Button("Do watershedding");
				final Label Segparam = new Label("Determine Threshold level for Segmentation");
				Segparam.setBackground(new Color(1, 0, 1));
				Segparam.setForeground(new Color(255, 255, 255));

				/* Location */
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 0;
				c.gridy = 0;
				c.weightx = 1;
				c.weighty = 1.5;
				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(Segparam, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(thresholdText, c);
				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(thresholdSHough, c);


				++c.gridy;
				c.insets = new Insets(10, 175, 0, 175);
				panelSecond.add(displayBit, c);

				++c.gridy;
				c.insets = new Insets(10, 175, 0, 175);
				panelSecond.add(displayWatershed, c);
				++c.gridy;
				c.insets = new Insets(10, 175, 0, 175);
				panelSecond.add(Dowatershed, c);


				thresholdSHough.addAdjustmentListener(new ThresholdHoughListener(thresholdText, thresholdHoughMin, thresholdHoughMax));
			

				displayBit.addItemListener(new ShowBitimgListener());
				displayWatershed.addItemListener(new ShowwatershedimgListener());
				Dowatershed.addActionListener(new DowatershedListener());
				
				
				
				
				
				
				
				final Scrollbar deltaS = new Scrollbar(Scrollbar.HORIZONTAL, deltaInit, 10, 0, 10 + scrollbarSize);
				final Scrollbar maxVarS = new Scrollbar(Scrollbar.HORIZONTAL, maxVarInit, 10, 0, 10 + scrollbarSize);
				final Scrollbar minDiversityS = new Scrollbar(Scrollbar.HORIZONTAL, minDiversityInit, 10, 0,
						10 + scrollbarSize);
				final Scrollbar minSizeS = new Scrollbar(Scrollbar.HORIZONTAL, minSizeInit, 10, 0, 10 + scrollbarSize);
				final Scrollbar maxSizeS = new Scrollbar(Scrollbar.HORIZONTAL, maxSizeInit, 10, 0, 10 + scrollbarSize);
				final Button ComputeTree = new Button("Compute Tree and display");
				maxVar = computeValueFromScrollbarPosition(maxVarInit, maxVarMin, maxVarMax, scrollbarSize);
				delta = computeValueFromScrollbarPosition(deltaInit, deltaMin, deltaMax, scrollbarSize);
				minDiversity = computeValueFromScrollbarPosition(minDiversityInit, minDiversityMin, minDiversityMax,
						scrollbarSize);
				minSize = (int) computeValueFromScrollbarPosition(minSizeInit, minSizemin, minSizemax, scrollbarSize);
				maxSize = (int) computeValueFromScrollbarPosition(maxSizeInit, maxSizemin, maxSizemax, scrollbarSize);

				final Checkbox min = new Checkbox("Look for Minima ", darktobright);

				final Label deltaText = new Label("delta = ", Label.CENTER);
				final Label maxVarText = new Label("maxVar = ", Label.CENTER);
				final Label minDiversityText = new Label("minDiversity = ", Label.CENTER);
				final Label minSizeText = new Label("MinSize = ", Label.CENTER);
				final Label maxSizeText = new Label("MaxSize = ", Label.CENTER);
				final Label MSparam = new Label("Determine MSER parameters");
				MSparam.setBackground(new Color(1, 0, 1));
				MSparam.setForeground(new Color(255, 255, 255));
				/* Location */
				

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(MSparam, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(deltaText, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(deltaS, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(maxVarText, c);

				
				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(maxVarS, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(minDiversityText, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(minDiversityS, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(minSizeText, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(minSizeS, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(maxSizeText, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(maxSizeS, c);

				++c.gridy;
				c.insets = new Insets(10, 175, 0, 175);
				panelSecond.add(min, c);

				++c.gridy;
				c.insets = new Insets(10, 175, 0, 175);
				panelSecond.add(ComputeTree, c);

				deltaS.addAdjustmentListener(new DeltaListener(deltaText, deltaMin, deltaMax, scrollbarSize, deltaS));

				maxVarS.addAdjustmentListener(
						new maxVarListener(maxVarText, maxVarMin, maxVarMax, scrollbarSize, maxVarS));

				minDiversityS.addAdjustmentListener(new minDiversityListener(minDiversityText, minDiversityMin,
						minDiversityMax, scrollbarSize, minDiversityS));

				minSizeS.addAdjustmentListener(
						new minSizeListener(minSizeText, minSizemin, minSizemax, scrollbarSize, minSizeS));

				maxSizeS.addAdjustmentListener(
						new maxSizeListener(maxSizeText, maxSizemin, maxSizemax, scrollbarSize, maxSizeS));

				min.addItemListener(new DarktobrightListener());
				ComputeTree.addActionListener(new ComputesegTreeListener());
				panelSecond.repaint();
				panelSecond.validate();
				Cardframe.pack();
			}

			if (findBlobsViaSEGMSER != oldState) {
				while (isComputing)
					SimpleMultiThreading.threadWait(10);

				updatePreview(ValueChange.FindBlobsVia);
			}
		}
	}
	protected class ShowBitimgListener implements ItemListener {
		@Override
		public void itemStateChanged(final ItemEvent arg0) {
			boolean oldState = displayBitimg;

			if (arg0.getStateChange() == ItemEvent.DESELECTED)
				displayBitimg = false;
			else if (arg0.getStateChange() == ItemEvent.SELECTED)
				displayBitimg = true;

			if (displayBitimg != oldState) {
				while (isComputing)
					SimpleMultiThreading.threadWait(10);

				updatePreview(ValueChange.DISPLAYBITIMG);
			}
		}
	}
	
	protected class DowatershedListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			updatePreview(ValueChange.Segmentation);

		}
	}

	protected class ShowwatershedimgListener implements ItemListener {
		@Override
		public void itemStateChanged(final ItemEvent arg0) {
			boolean oldState = displayWatershedimg;

			if (arg0.getStateChange() == ItemEvent.DESELECTED)
				displayWatershedimg = false;
			else if (arg0.getStateChange() == ItemEvent.SELECTED)
				displayWatershedimg = true;

			if (displayWatershedimg != oldState) {
				while (isComputing)
					SimpleMultiThreading.threadWait(10);

				updatePreview(ValueChange.DISPLAYWATERSHEDIMG);
			}
		}
	}
	
	protected class FindBlobsListener implements ActionListener {

		@Override
		public void actionPerformed(final ActionEvent arg0) {

			RandomAccessibleInterval<FloatType> groundframe = currentimg;

			if (findBlobsViaMSER) {

				updatePreview(ValueChange.SHOWMSER);

				BlobfinderInteractiveMSER newblobMser = new BlobfinderInteractiveMSER(currentimg, othercurrentimg,
						newtree, sizeX, sizeY, thirdDimension, fourthDimension);

				ProbBlobs = FindblobsVia.BlobfindingMethod(newblobMser);

			}

			if (findBlobsViaDOG) {

				updatePreview(ValueChange.SHOWDOG);

				BlobfinderInteractiveDoG newblobDog = new BlobfinderInteractiveDoG(currentimg, othercurrentimg,
						lookForMaxima, lookForMinima, sigma, sigma2, peaks, thirdDimension, fourthDimension);

				ProbBlobs = FindblobsVia.BlobfindingMethod(newblobDog);

			}

		}

	}
	
	

	protected class ComputeTreeListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			showMSER = true;
			updatePreview(ValueChange.SHOWMSER);

		}
	}

	
	protected class ComputesegTreeListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			showSegMSER = true;
			updatePreview(ValueChange.SHOWSEGMSER);

		}
	}
	
	protected class DisplayBlobsListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			showDOG = true;
			updatePreview(ValueChange.SHOWDOG);

		}
	}
	
	protected class DisplaysegBlobsListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			showSegDOG = true;
			updatePreview(ValueChange.SHOWSEGDOG);

		}
	}
	

	protected class DarktobrightListener implements ItemListener {
		@Override
		public void itemStateChanged(final ItemEvent arg0) {
			boolean oldState = darktobright;

			if (arg0.getStateChange() == ItemEvent.DESELECTED)
				darktobright = false;
			else if (arg0.getStateChange() == ItemEvent.SELECTED)
				darktobright = true;

			if (darktobright != oldState) {
				while (isComputing)
					SimpleMultiThreading.threadWait(10);

				updatePreview(ValueChange.DARKTOBRIGHT);
			}
		}
	}

	protected class moveInThirdDimListener implements ActionListener {
		final float min, max;
		Label timeText;
		final Scrollbar thirdDimensionScroll;

		public moveInThirdDimListener(Scrollbar thirdDimensionScroll, Label timeText, float min, float max) {
			this.thirdDimensionScroll = thirdDimensionScroll;
			this.min = min;
			this.max = max;
			this.timeText = timeText;
		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {

			boolean dialog = Dialogue();
			if (dialog) {

				thirdDimensionScroll
						.setValue(computeIntScrollbarPositionFromValue(thirdDimension, min, max, scrollbarSize));
				timeText.setText("Third Dimension = " + thirdDimensionslider);

				if (thirdDimension > thirdDimensionSize) {
					IJ.log("Max frame number exceeded, moving to last frame instead");
					thirdDimension = thirdDimensionSize;
					CurrentView = getCurrentView();
					otherCurrentView = getotherCurrentView();
				} else {

					CurrentView = getCurrentView();
					otherCurrentView = getotherCurrentView();
				}

				imp = WindowManager.getCurrentImage();
				Roi roi = imp.getRoi();
				if (roi == null) {
					// IJ.log( "A rectangular ROI is required to define the
					// area..."
					// );
					imp.setRoi(standardRectangle);
					roi = imp.getRoi();
				}

				// compute first version
				updatePreview(ValueChange.THIRDDIM);
				isStarted = true;

				// check whenever roi is modified to update accordingly
				roiListener = new RoiListener();
				imp.getCanvas().addMouseListener(roiListener);

			}
		}
	}

	protected class moveInFourthDimListener implements ActionListener {
		final float min, max;
		Label sliceText;
		final Scrollbar fourthDimensionScroll;

		public moveInFourthDimListener(Scrollbar fourthDimensionScroll, Label sliceText, float min, float max) {
			this.fourthDimensionScroll = fourthDimensionScroll;
			this.min = min;
			this.max = max;
			this.sliceText = sliceText;
		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {

			boolean dialog = DialogueSlice();
			if (dialog) {

				fourthDimensionScroll
						.setValue(computeIntScrollbarPositionFromValue(fourthDimension, min, max, scrollbarSize));
				sliceText.setText("Fourth Dimension = " + fourthDimensionslider);

				if (fourthDimension > fourthDimensionSize) {
					IJ.log("Fourth dimension size exceeded, moving to co-ordinate in 4th dimension instead");
					fourthDimension = fourthDimensionSize;
					CurrentView = getCurrentView();
					otherCurrentView = getotherCurrentView();
				}

				else {

					CurrentView = getCurrentView();
					otherCurrentView = getotherCurrentView();

				}

				imp = WindowManager.getCurrentImage();
				sliceObserver = new SliceObserver(imp, new ImagePlusListener());
				Roi roi = imp.getRoi();
				if (roi == null) {
					// IJ.log( "A rectangular ROI is required to define the
					// area..."
					// );
					imp.setRoi(standardRectangle);
					roi = imp.getRoi();
				}

				// compute first version
				updatePreview(ValueChange.FOURTHDIM);
				isStarted = true;

				// check whenever roi is modified to update accordingly
				roiListener = new RoiListener();
				imp.getCanvas().addMouseListener(roiListener);

			}
		}
	}
	class ProgressAll extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
	
			
			
			
			// add listener to the imageplus slice slider
			sliceObserver = new SliceObserver(imp, new ImagePlusListener());

			
			All3DSnakes.clear();
			Dialoguesec();
			int next = thirdDimension;
			int nextZ = fourthDimension;
			if (snakestack != null) {
				for (int index = 1; index < snakestack.size(); ++index) {

					snakestack.deleteSlice(index);
				}
			}
			
			if (measuresnakestack != null) {
				for (int index = 1; index < measuresnakestack.size(); ++index) {

					measuresnakestack.deleteSlice(index);
				}
			}
			putFeature( SNAKEPROGRESS, Double.valueOf( AllSliceSnakes.size() ) );
			// Run snakes over a frame for each slice in that frame
			for (int indexx = next; indexx <= fourthDimensionSize; ++indexx) {
				snakestack = new ImageStack((int) originalimgA.dimension(0), (int) originalimgA.dimension(1),
						java.awt.image.ColorModel.getRGBdefault());
				measuresnakestack = new ImageStack((int) originalimgB.dimension(0), (int) originalimgB.dimension(1),
						java.awt.image.ColorModel.getRGBdefault());

				thirdDimension = indexx;
				thirdDimensionslider = thirdDimension;

				AllSliceSnakes = new ArrayList<ArrayList<SnakeObject>>();

				CurrentView = getCurrentView();
				otherCurrentView = getotherCurrentView();

				updatePreview(ValueChange.THIRDDIMTrack);

				for (int z = nextZ; z <= thirdDimensionSize; ++z) {

					fourthDimension = z;
					fourthDimensionslider = fourthDimension;

					CurrentView = getCurrentView();
					otherCurrentView = getotherCurrentView();

					updatePreview(ValueChange.FOURTHDIMTrack);
					roiListener = new RoiListener();
					imp.getCanvas().addMouseListener(roiListener);

					BlobfinderInteractiveSnake snake;
					if (NearestNeighbourRois.size() > 0)
						snake = new BlobfinderInteractiveSnake(CurrentView, otherCurrentView, NearestNeighbourRois,
								sizeX, sizeY, usefolder, addTrackToName, z, indexx, TrackinT, jpb, thirdDimensionSize);
					else

						snake = new BlobfinderInteractiveSnake(CurrentView, otherCurrentView, Rois, sizeX, sizeY,
								usefolder, addTrackToName, z, indexx, TrackinT, jpb, thirdDimensionSize);

					RoiManager manager = RoiManager.getInstance();
					if (manager != null) {
						manager.getRoisAsArray();
					}

					isStarted = true;

					if (Auto) {
						if (indexx > next || z > nextZ)
							snake.Auto = true;
					}
					snake.checkInput();
					 
					snake.process();
				        
					 
					usefolder = snake.getFolder();
					addTrackToName = snake.getFile();

					finalRois = snake.getfinalRois();
					currentsnakes = snake.getResult();

					snakeoverlay = snake.getABsnake();

				
					
					
					if (snake.displaysnake) {

						if (imp != null)
							imp.close();
						imp = ImageJFunctions.show(CurrentView);
						imp.hide();
						if (measureimp != null)
							measureimp.close();
						measureimp = ImageJFunctions.show(otherCurrentView);
						measureimp.hide();
						snakestack.addSlice(imp.getImageStack().getProcessor(z).convertToRGB());
						measuresnakestack.addSlice(measureimp.getImageStack().getProcessor(z).convertToRGB());
						
						ColorProcessor cp = (ColorProcessor) (snakestack.getProcessor(z).duplicate());
						ColorProcessor measurecp = (ColorProcessor) (snakestack.getProcessor(z).duplicate());
						
						for (int i = 0; i < snakeoverlay.size(); ++i) {

							snakeoverlay.get(i).DrawSnake(cp, snake.colorDraw, 1);
							
							double[] center = getCenter(snakeoverlay.get(i).createRoi());
							
							 Roi normalroi = snakeoverlay.get(i).createRoi();
								
								int width = (int)normalroi.getBounds().getWidth();
								int height = (int)normalroi.getBounds().getHeight();
								
								final OvalRoi Bigroi = new OvalRoi(Util.round(center[0] -(width + sizeX)/2), Util.round(center[1] - (height + sizeY)/2 ), Util.round(width + sizeX),
										Util.round(height + sizeY));
								measurecp.setColor(colorDraw);
								measurecp.setLineWidth(1);
								measurecp.draw(Bigroi);
						   
							
						}

						snakestack.setPixels(cp.getPixels(), z);
						measuresnakestack.setPixels(measurecp.getPixels(), z);

					}

					if (All3DSnakes != null) {

						for (int Listindex = 0; Listindex < All3DSnakes.size(); ++Listindex) {

							SnakeObject SnakeFrame = All3DSnakes.get(Listindex).get(0);
							int SnakeThirdDim = SnakeFrame.thirdDimension;
							int SnakeFourthDim = SnakeFrame.fourthDimension;

							if (SnakeThirdDim == thirdDimension && SnakeFourthDim == fourthDimension) {
								All3DSnakes.remove(Listindex);
								IJ.log(" Recomputing snakes for currentView");

							}
						}

					}

					AllSliceSnakes.add(currentsnakes);
					IJ.log(" " + AllSliceSnakes.size());
				           
				} // Z loop closing
					// Make KD tree to link objects along Z axis
					
				ArrayList<SnakeObject> ThreedimensionalSnake = getCentreofMass3D();

				All3DSnakes.add(ThreedimensionalSnake);

				new ImagePlus("Snakes", snakestack).draw();
				new ImagePlus("Measure", measuresnakestack).draw();
			} // t loop closing
			IJ.log("SnakeList Size" + All3DSnakes.size());
		          
			return null;
		}
		
		 @Override
         protected void done() {
             try {
            	 jpb.setIndeterminate(false);
                 get();
                 frame.dispose();
                 JOptionPane.showMessageDialog(jpb.getParent(), "Success", "Success", JOptionPane.INFORMATION_MESSAGE);
             } catch (ExecutionException | InterruptedException e) {
                 e.printStackTrace();
             }
		
		
		 }
		
	}
	
	public  void goAll() {
	      
	       
        jpb.setIndeterminate(false);
    	
	
		
	        
        jpb.setMaximum(max);
        panel.add(label);
        panel.add(jpb);
        frame.add(panel);
        frame.pack();
        frame.setSize(200,100);
        frame.setLocationRelativeTo(panelCont);
        frame.setVisible(true);
       
      
        
        
        
        ProgressAll dosnake = new ProgressAll();
		dosnake.execute();
      
       
	}
	
	
	protected class moveAllListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent arg0) {
			
			

			SwingUtilities.invokeLater(new Runnable() {
			      @Override
			      public void run() {
			    	  
			    	  goAll();
			    	  
			      }
	         
			});
			
		
	           
		}
			
		   
	}
	
	class ProgressSnake extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			
			AllSliceSnakes = new ArrayList<ArrayList<SnakeObject>>();

			All3DSnakes.clear();
			int next = thirdDimension;

			if (snakestack != null) {
				for (int index = 1; index < snakestack.size(); ++index) {

					snakestack.deleteSlice(index);

				}
			}
			if (measuresnakestack != null) {
				for (int index = 1; index < measuresnakestack.size(); ++index) {

					measuresnakestack.deleteSlice(index);

				}
			}
			Dialoguesec();
			putFeature( SNAKEPROGRESS, Double.valueOf( AllSliceSnakes.size() ) );
			
			
			
			
			for (int z = next; z <= thirdDimensionSize; ++z) {

				thirdDimension = z;
				thirdDimensionslider = thirdDimension;
				CurrentView = getCurrentView();
				otherCurrentView = getotherCurrentView();
				 
				updatePreview(ValueChange.THIRDDIMTrack);

				BlobfinderInteractiveSnake snake;
				if (NearestNeighbourRois.size() > 0)
					snake = new BlobfinderInteractiveSnake(CurrentView, otherCurrentView, NearestNeighbourRois, sizeX,
							sizeY, usefolder, addTrackToName, z, 0, TrackinT, jpb, thirdDimensionSize);
				else

					snake = new BlobfinderInteractiveSnake(CurrentView, otherCurrentView, Rois, sizeX, sizeY, usefolder,
							addTrackToName, z, 0, TrackinT, jpb, thirdDimensionSize);

				if (Auto && z > next)
					snake.Auto = true;

				snake.process();
			        
				usefolder = snake.getFolder();
				addTrackToName = snake.getFile();
				finalRois = snake.getfinalRois();
				currentsnakes = snake.getResult();

				snakeoverlay = snake.getABsnake();
				if (snake.displaysnake) {

					if (imp != null)
						imp.close();
					imp = ImageJFunctions.show(CurrentView);
					imp.hide();
					
					if (measureimp != null)
						measureimp.close();
					measureimp = ImageJFunctions.show(otherCurrentView);
					measureimp.hide();
				
					snakestack.addSlice(imp.getImageStack().getProcessor(z).convertToRGB());
					measuresnakestack.addSlice(measureimp.getImageStack().getProcessor(z).convertToRGB());
					
					ColorProcessor cp = (ColorProcessor) (snakestack.getProcessor(z).duplicate());
					ColorProcessor measurecp = (ColorProcessor) (measuresnakestack.getProcessor(z).duplicate());
					
					for (int i = 0; i < snakeoverlay.size() ; ++i) {

						snakeoverlay.get(i).DrawSnake(cp, snake.colorDraw, 1);

						
						
						 Roi normalroi = snakeoverlay.get(i).createRoi();
						 double[] center = getCenter(normalroi);
							int width = (int)normalroi.getBounds().getWidth();
							int height = (int)normalroi.getBounds().getHeight();
							
							final OvalRoi Bigroi = new OvalRoi(Util.round(center[0] -(width + sizeX)/2), Util.round(center[1] - (height + sizeY)/2 ), Util.round(width + sizeX),
									Util.round(height + sizeY));
							measurecp.setColor(colorDraw);
							measurecp.setLineWidth(1);
							measurecp.draw(Bigroi);
					}

					snakestack.setPixels(cp.getPixels(), z);
					measuresnakestack.setPixels(measurecp.getPixels(), z);

				}

				if (All3DSnakes != null) {

					for (int Listindex = 0; Listindex < All3DSnakes.size(); ++Listindex) {

						SnakeObject SnakeFrame = All3DSnakes.get(Listindex).get(0);
						int SnakeThirdDim = SnakeFrame.thirdDimension;
						int SnakeFourthDim = SnakeFrame.fourthDimension;

						if (SnakeThirdDim == thirdDimension && SnakeFourthDim == fourthDimension) {
							All3DSnakes.remove(Listindex);
							IJ.log(" Recomputing snakes for currentView");

						}
					}

				}

				AllSliceSnakes.add(currentsnakes);

			}

			All3DSnakes.addAll(AllSliceSnakes);
			new ImagePlus("Snakes", snakestack).show();
			new ImagePlus("Measure", measuresnakestack).show();
			IJ.log("SnakeList Size" + All3DSnakes.size());
		          
			
			return null;
		}
		
		 @Override
         protected void done() {
             try {
            	 jpb.setIndeterminate(false);
                 get();
                 frame.dispose();
                 JOptionPane.showMessageDialog(jpb.getParent(), "Success", "Success", JOptionPane.INFORMATION_MESSAGE);
             } catch (ExecutionException | InterruptedException e) {
                 e.printStackTrace();
             }
		
		
		 }
		
	}
	
	
	
	
	public  void goSnake() {
		
		
		panel.removeAll();
		 
        jpb.setIndeterminate(false);
        jpb.setMaximum(max);
       
      
        panel.add(jpb);
        frame.add(panel);
        frame.pack();
        frame.setSize(200,100);
        frame.setLocationRelativeTo(panelCont);
        frame.setVisible(true);
       
      
        
        
        
        ProgressSnake dosnake = new ProgressSnake();
		dosnake.execute();
      
       
	}
	
	
	
	protected class snakeButtonListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent arg0) {

			SwingUtilities.invokeLater(new Runnable() {
			      @Override
			      public void run() {
			    	  
			    	  goSnake();
			    	  
			      }
	         
			});
		            
		            }
		            
		            

	}
	
	class ProgressRedo extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
	
			AllSliceSnakes = new ArrayList<ArrayList<SnakeObject>>();

			All3DSnakes.clear();
			DialogueRedo();

			thirdDimensionslider = thirdDimension;
			fourthDimensionslider = fourthDimension;
			CurrentView = getCurrentView();
			otherCurrentView = getotherCurrentView();
			updatePreview(ValueChange.THIRDDIMTrack);
			BlobfinderInteractiveSnake snake;
			putFeature( SNAKEPROGRESS, Double.valueOf( AllSliceSnakes.size() ) );
			if (NearestNeighbourRois.size() > 0)
				snake = new BlobfinderInteractiveSnake(CurrentView, otherCurrentView, NearestNeighbourRois, sizeX,
						sizeY, usefolder, addTrackToName, thirdDimensionslider, fourthDimensionslider, TrackinT, jpb, thirdDimensionSize);
			else

				snake = new BlobfinderInteractiveSnake(CurrentView, otherCurrentView, Rois, sizeX, sizeY, usefolder,
						addTrackToName, thirdDimensionslider, fourthDimensionslider, TrackinT, jpb, thirdDimensionSize);


			
			snake.process();
		          
			usefolder = snake.getFolder();
			addTrackToName = snake.getFile();
			currentsnakes = snake.getResult();
			finalRois = snake.getfinalRois();
		    snakeoverlay = snake.getABsnake();

			if (snake.displaysnake) {
                 int z = 1;
				if (imp != null)
					imp.close();
				imp = ImageJFunctions.show(CurrentView);
				imp.hide();
				if (measureimp != null)
					measureimp.close();
				measureimp = ImageJFunctions.show(otherCurrentView);
				measureimp.hide();
				snakestack.addSlice(imp.getImageStack().getProcessor(z).convertToRGB());
				measuresnakestack.addSlice(measureimp.getImageStack().getProcessor(z).convertToRGB());
				
				ColorProcessor cp = (ColorProcessor) (snakestack.getProcessor(z).duplicate());
				ColorProcessor measurecp = (ColorProcessor) (snakestack.getProcessor(z).duplicate());
				
				for (int i = 0; i < snakeoverlay.size(); ++i) {

					snakeoverlay.get(i).DrawSnake(cp, snake.colorDraw, 1);

					double[] center = getCenter(snakeoverlay.get(i).createRoi());
	                Roi normalroi = snakeoverlay.get(i).createRoi();
					
					int width = (int)normalroi.getBounds().getWidth();
					int height = (int)normalroi.getBounds().getHeight();
					
					final OvalRoi Bigroi = new OvalRoi(Util.round(center[0] -(width + sizeX)/2), Util.round(center[1] - (height + sizeY)/2 ), Util.round(width + sizeX),
							Util.round(height + sizeY));
					measurecp.setColor(colorDraw);
					measurecp.setLineWidth(1);
					measurecp.draw(Bigroi);
				}

				snakestack.setPixels(cp.getPixels(), z);
				measuresnakestack.setPixels(measurecp.getPixels(), z);

			}

			if (All3DSnakes != null) {

				for (int Listindex = 0; Listindex < All3DSnakes.size(); ++Listindex) {

					SnakeObject SnakeFrame = All3DSnakes.get(Listindex).get(0);
					int SnakeThirdDim = SnakeFrame.thirdDimension;
					int SnakeFourthDim = SnakeFrame.fourthDimension;

					if (SnakeThirdDim == thirdDimension && SnakeFourthDim == fourthDimension) {
						All3DSnakes.remove(Listindex);
						IJ.log(" Recomputing snakes for currentView");

					}
				}

			}

			AllSliceSnakes.add(currentsnakes);

			All3DSnakes.addAll(AllSliceSnakes);
			new ImagePlus("Snakes", snakestack).show();
			new ImagePlus("Measure", measuresnakestack).show();
			snakestack.deleteLastSlice();
			measuresnakestack.deleteLastSlice();
			IJ.log("SnakeList Size" + All3DSnakes.size());
		         
		          
			return null;
		}
		
		 @Override
         protected void done() {
             try {
            	 jpb.setIndeterminate(false);
                 get();
                 frame.dispose();
                 JOptionPane.showMessageDialog(jpb.getParent(), "Success", "Success", JOptionPane.INFORMATION_MESSAGE);
             } catch (ExecutionException | InterruptedException e) {
                 e.printStackTrace();
             }
		
		
		 }
		
	}
	
	public  void goRedo() {
	      
	       
        jpb.setIndeterminate(false);
    	
	
		  
        
	        
        jpb.setMaximum(max);
        panel.add(label);
        panel.add(jpb);
        frame.add(panel);
        frame.pack();
        frame.setSize(200,100);
        frame.setLocationRelativeTo(panelCont);
        frame.setVisible(true);
       
      
        
        
        
        ProgressRedo dosnake = new ProgressRedo();
		dosnake.execute();
      
       
	}
	

	protected class RedosnakeButtonListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent arg0) {
			
		
			SwingUtilities.invokeLater(new Runnable() {
			      @Override
			      public void run() {
			    	  
			    	  goRedo();
			    	  
			      }
	         
			});
		            }
	}
	class Progress extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			 
			AllSliceSnakes = new ArrayList<ArrayList<SnakeObject>>();

			thirdDimensionslider = thirdDimension;
			fourthDimensionslider = fourthDimension;
			CurrentView = getCurrentView();
			otherCurrentView = getotherCurrentView();
			updatePreview(ValueChange.THIRDDIM);
			putFeature( SNAKEPROGRESS, Double.valueOf( AllSliceSnakes.size() ) );
			
			BlobfinderInteractiveSnake snake;
			if (NearestNeighbourRois.size() > 0)
				snake = new BlobfinderInteractiveSnake(CurrentView, otherCurrentView, NearestNeighbourRois, sizeX,
						sizeY, usefolder, addTrackToName, thirdDimensionslider, fourthDimensionslider, TrackinT, jpb, thirdDimensionSize);
			else

				snake = new BlobfinderInteractiveSnake(CurrentView, otherCurrentView, Rois, sizeX, sizeY, usefolder,
						addTrackToName, thirdDimensionslider, fourthDimensionslider, TrackinT, jpb, thirdDimensionSize);
			jpb.setIndeterminate(false);
			snake.process();
			usefolder = snake.getFolder();
			addTrackToName = snake.getFile();
			jpb.getValue();
			
			currentsnakes = snake.getResult();
			finalRois = snake.getfinalRois();
			snakeoverlay = snake.getABsnake();
	
			
			snakestack.addSlice(imp.getImageStack().getProcessor(1).convertToRGB());
			measuresnakestack.addSlice(measureimp.getImageStack().getProcessor(1).convertToRGB());
			
			if (snake.displaysnake) {

				ColorProcessor cp = (ColorProcessor) (snakestack.getProcessor(1).duplicate());

				ColorProcessor measurecp = (ColorProcessor) (measuresnakestack.getProcessor(1).duplicate());
				
				for (int i = 0; i < snakeoverlay.size(); ++i) {

					snakeoverlay.get(i).DrawSnake(cp, snake.colorDraw, 1);
					double[] center = getCenter(snakeoverlay.get(i).createRoi());
					
					Roi normalroi = snakeoverlay.get(i).createRoi();
					
					int width = (int)normalroi.getBounds().getWidth();
					int height = (int)normalroi.getBounds().getHeight();
					
					final OvalRoi Bigroi = new OvalRoi(Util.round(center[0] -(width + sizeX)/2), Util.round(center[1] - (height + sizeY)/2 ), Util.round(width + sizeX),
							Util.round(height + sizeY));
					measurecp.setColor(colorDraw);
					measurecp.setLineWidth(1);
					measurecp.draw(Bigroi);
					
					

				}

				snakestack.setPixels(cp.getPixels(), 1);
				
				measuresnakestack.setPixels(measurecp.getPixels(), 1);
			}

			if (All3DSnakes != null) {

				for (int Listindex = 0; Listindex < All3DSnakes.size(); ++Listindex) {

					SnakeObject SnakeFrame = All3DSnakes.get(Listindex).get(0);
					int SnakeThirdDim = SnakeFrame.thirdDimension;
					int SnakeFourthDim = SnakeFrame.fourthDimension;

					if (SnakeThirdDim == thirdDimension && SnakeFourthDim == fourthDimension) {
						All3DSnakes.remove(Listindex);
						IJ.log(" Recomputing snakes for currentView");

					}
				}

			}

			AllSliceSnakes.add(currentsnakes);

			// The graph for this frame along Z is now complete, generate 3D
			// snake properties

			ArrayList<SnakeObject> ThreedimensionalSnake = getCentreofMass3D();

			All3DSnakes.add(ThreedimensionalSnake);
			new ImagePlus("Snakes", snakestack).show();
			snakestack.deleteLastSlice();
			measuresnakestack.deleteLastSlice();
			IJ.log("SnakeList Size" + All3DSnakes.size());
		          
			return null;
		}
		 @Override
         protected void done() {
             try {
            	 jpb.setIndeterminate(false);
                 get();
                 frame.dispose();
                 JOptionPane.showMessageDialog(jpb.getParent(), "Success", "Success", JOptionPane.INFORMATION_MESSAGE);
             } catch (ExecutionException | InterruptedException e) {
                 e.printStackTrace();
             }
		
		
		 }
		
	}
	
	
	public  void go() {
	      
	       
        jpb.setIndeterminate(false);
    	
	
		  
        
	        
        jpb.setMaximum(max);
        panel.add(label);
        panel.add(jpb);
        frame.add(panel);
        frame.pack();
        frame.setSize(200,100);
        frame.setLocationRelativeTo(panelCont);
        frame.setVisible(true);
       
      
        
        
        
        Progress dosnake = new Progress();
		dosnake.execute();
      
       
	}
	
	
	protected class SinglesnakeButtonListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent arg0) {
			
			

			
			SwingUtilities.invokeLater(new Runnable() {
			      @Override
			      public void run() {
			    	  
			    	  go();
			    	  
			      }
	         
			});
			
			
		
		}

	}

	public ArrayList<SnakeObject> getCentreofMass3D() {

		if (fourthDimensionSize > 1) {
			reset();
			for (int z = fourthDimension; z < fourthDimensionSize - 2; z++) {

				ArrayList<SnakeObject> Spotmaxbase = AllSliceSnakes.get(z);

				ArrayList<SnakeObject> Spotmaxtarget = AllSliceSnakes.get(z + 1);

				Iterator<SnakeObject> baseobjectiterator = Spotmaxbase.iterator();

				final int Targetblobs = Spotmaxtarget.size();

				final List<RealPoint> targetCoords = new ArrayList<RealPoint>(Targetblobs);

				final List<FlagNode<SnakeObject>> targetNodes = new ArrayList<FlagNode<SnakeObject>>(Targetblobs);

				// For linking the 2D center of masses on Rois to make a 3D
				// object
				for (int index = 0; index < Spotmaxtarget.size(); ++index) {

					targetCoords.add(new RealPoint(Spotmaxtarget.get(index).centreofMass));

					targetNodes.add(new FlagNode<SnakeObject>(Spotmaxtarget.get(index)));

				}
				if (targetNodes.size() > 0 && targetCoords.size() > 0) {

					final KDTree<FlagNode<SnakeObject>> Tree = new KDTree<FlagNode<SnakeObject>>(targetNodes,
							targetCoords);

					final NNFlagsearchKDtree<SnakeObject> Search = new NNFlagsearchKDtree<SnakeObject>(Tree);

					while (baseobjectiterator.hasNext()) {

						final SnakeObject source = baseobjectiterator.next();
						final RealPoint sourceCoords = new RealPoint(source.centreofMass);
						Search.search(sourceCoords);
						final double squareDist = Search.getSquareDistance();
						final FlagNode<SnakeObject> targetNode = Search.getSampler().get();

						// Connect the Source (source) and the Target
						// (targetNode.getValue())

						targetNode.setVisited(true);

						synchronized (graphZ) {

							graphZ.addVertex(source);
							graphZ.addVertex(targetNode.getValue());
							final DefaultWeightedEdge edge = graphZ.addEdge(source, targetNode.getValue());
							graphZ.setEdgeWeight(edge, squareDist);

						}

					}

					System.out.println(" In Frame " + thirdDimension + " " + "Linked objects between " + z + " "
							+ (z + 1) + " Moving to next slice");
				}

			}

			// The graph for this frame along Z is now complete, generate 3D
			// snake properties

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

						return A.fourthDimension - B.fourthDimension;

					}

				};

				Iterator<SnakeObject> Snakeiter = Snakeset.iterator();
				while (Snakeiter.hasNext()) {

					SnakeObject currentsnake = Snakeiter.next();
					for (int d = 0; d < currentimg.numDimensions(); ++d)
						if (currentsnake.centreofMass[d] != Double.NaN)
							list.add(currentsnake);

				}
				Collections.sort(list, Slicecomparison);

				int ndims = originalimgA.numDimensions();
				double[] centerA = new double[3];
				double[] centerB = new double[3];
				final double[] position = new double[ndims];
				double IntensityA = 0;
				double IntensityB = 0;
				double size = 0;
				double[] SumXYZA = new double[3];
				double[] SumXYZB = new double[3];
				Cursor<FloatType> currentcursorA, secondcurrentcursorA;
				RandomAccess<FloatType> randomAccessB;
				int NumberofpixelsA = 0;
				int NumberofpixelsB = 0;
				// Compute the center of mass here, trackID is in Z.
				for (int index = 0; index < list.size(); ++index) {

					if (ndims > 4) {
						currentcursorA = Views
								.iterable(Views.hyperSlice(originalimgA, 2, list.get(index).fourthDimension))
								.localizingCursor();
						secondcurrentcursorA = Views
								.iterable(Views.hyperSlice(originalimgA, 2, list.get(index).fourthDimension))
								.localizingCursor();

						randomAccessB = (Views.hyperSlice(originalimgB, 2, list.get(index).fourthDimension))
								.randomAccess();
					}

					else {
						currentcursorA = Views.iterable(originalimgA).localizingCursor();
						secondcurrentcursorA = Views.iterable(originalimgA).localizingCursor();
						randomAccessB = originalimgB.randomAccess();

					}

					while (currentcursorA.hasNext()) {

						currentcursorA.fwd();

						currentcursorA.localize(position);
						randomAccessB.setPosition(currentcursorA);

						int x = (int) position[0];
						int y = (int) position[1];

						Roi roi = list.get(index).roi;
						if (roi.contains(x, y)) {

							size += roi.getLength();
							SumXYZA[0] += currentcursorA.getDoublePosition(0) * currentcursorA.get().getRealDouble();
							SumXYZA[1] += currentcursorA.getDoublePosition(1) * currentcursorA.get().getRealDouble();
							SumXYZA[2] += list.get(index).fourthDimension * currentcursorA.get().getRealDouble();

							IntensityA += currentcursorA.get().getRealDouble();
							NumberofpixelsA++;
						}

					}

					centerA[0] = SumXYZA[0] / IntensityA;
					centerA[1] = SumXYZA[1] / IntensityA;
					centerA[2] = SumXYZA[2] / IntensityA;

					while (secondcurrentcursorA.hasNext()) {

						secondcurrentcursorA.fwd();

						currentcursorA.localize(position);
						randomAccessB.setPosition(secondcurrentcursorA);

						int x = (int) position[0];
						int y = (int) position[1];

						Roi roi = list.get(index).roi;

						int width = (int) roi.getFloatPolygon().getBounds().getWidth();
						int height = (int) roi.getFloatPolygon().getBounds().getHeight();

						final OvalRoi Bigroi = new OvalRoi(Util.round(centerA[0] - (width + sizeX) / 2),
								Util.round(centerA[1] - (height + sizeY) / 2), Util.round(width + sizeX),
								Util.round(height + sizeY));

						if (Bigroi.contains(x, y)) {

							IntensityB += randomAccessB.get().getRealDouble();
							NumberofpixelsB++;
						}

					}

					SnakeRoisA.add(list.get(index).roi);

				}

				final double meanIntensityA = IntensityA / NumberofpixelsA;
				final double meanIntensityB = IntensityB / NumberofpixelsB;

				final SnakeObject ThreeDSnake = new SnakeObject(list.get(0).thirdDimension, list.get(0).fourthDimension,
						id, SnakeRoisA, centerA, IntensityA, NumberofpixelsA, meanIntensityA, IntensityB,
						NumberofpixelsB, meanIntensityB, size);

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
						+ nf.format(currentsnakes.get(index).IntensityROI) + "\n");
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

	/**
	 * Extract the current 2d region of interest from the image on which
	 * measurements have to be performed
	 * 
	 * @param CurrentView
	 *            - the CurrentView image, a {@link Image} which is a copy of
	 *            the {@link ImagePlus}
	 * 
	 * @return
	 */
	protected RandomAccessibleInterval<FloatType> extractotherImage(
			final RandomAccessibleInterval<FloatType> intervalView) {

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

	// Making the card
	JFrame Cardframe = new JFrame("4D Tracker");
	JPanel panelCont = new JPanel();
	JPanel panelFirst = new JPanel();
	JPanel panelSecond = new JPanel();
	JPanel panelThird = new JPanel();
	JPanel panelFourth = new JPanel();
	JPanel panelFifth = new JPanel();
	JPanel panelSixth = new JPanel();
	JPanel panelSeventh = new JPanel();

	public void Card() {

		CardLayout cl = new CardLayout();

		cl.preferredLayoutSize(Cardframe);
		panelCont.setLayout(cl);

		
		
		panelCont.add(panelFirst, "1");
		panelCont.add(panelSecond, "2");
		panelCont.add(panelThird, "3");
		panelCont.add(panelFourth, "4");
		panelCont.add(panelFifth, "5");
		panelCont.add(panelSixth, "6");
		panelCont.add(panelSeventh, "7");
		CheckboxGroup Finders = new CheckboxGroup();

		
		final Checkbox mser = new Checkbox("MSER + Snakes", Finders, findBlobsViaMSER);
		final Checkbox dog = new Checkbox("DoG + Snakes", Finders, findBlobsViaDOG);
		final Checkbox Segmser = new Checkbox("Segmentation + MSER + Snakes", Finders, findBlobsViaSEGMSER);
		final Checkbox Segdog = new Checkbox("Segmentation + DoG + Snakes", Finders, findBlobsViaSEGDOG);
		final Button JumpFrame = new Button("Jump in third dimension to :");
		final Button JumpSlice = new Button("Jump in fourth dimension to :");
		final Label timeText = new Label("Third Dimensíonal slice = " + this.thirdDimensionslider, Label.CENTER);
		final Label sliceText = new Label("Fourth Dimensional slice = " + this.fourthDimensionslider, Label.CENTER);
		
	
		final Scrollbar thirdDimensionslider = new Scrollbar(Scrollbar.HORIZONTAL, thirdDimensionsliderInit, 0, 0,
				thirdDimensionSize);
		thirdDimensionslider.setBlockIncrement(1);
		this.thirdDimensionslider = (int) computeValueFromScrollbarPosition(thirdDimensionsliderInit, timeMin,
				thirdDimensionSize, thirdDimensionSize);

		final Scrollbar fourthDimensionslider = new Scrollbar(Scrollbar.HORIZONTAL, fourthDimensionsliderInit, 0, 0,
				fourthDimensionSize);
		fourthDimensionslider.setBlockIncrement(1);
		this.fourthDimensionslider = (int) computeValueFromScrollbarPosition(fourthDimensionsliderInit, sliceMin,
				fourthDimensionSize, fourthDimensionSize);
		

		/* Instantiation */
		final GridBagLayout layout = new GridBagLayout();
		final GridBagConstraints c = new GridBagConstraints();

		panelFirst.setLayout(layout);
		
		final Label Name = new Label("Step 1", Label.CENTER);
		panelFirst.add(Name, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 4;
		c.weightx = 1;

		final Label Ends = new Label("Method Choice for finding Blobs");

		++c.gridy;
		panelFirst.add(Ends, c);

		++c.gridy;
		c.insets = new Insets(10, 10, 0, 0);
		panelFirst.add(mser, c);

		++c.gridy;
		c.insets = new Insets(10, 10, 0, 0);
		panelFirst.add(dog, c);
		
		
		++c.gridy;
		c.insets = new Insets(10, 10, 0, 0);
		panelFirst.add(Segmser, c);

		++c.gridy;
		c.insets = new Insets(10, 10, 0, 0);
		panelFirst.add(Segdog, c);
		

		if (thirdDimensionSize > 1) {
			++c.gridy;
			panelFirst.add(thirdDimensionslider, c);

			++c.gridy;
			panelFirst.add(timeText, c);

			++c.gridy;
			c.insets = new Insets(0, 175, 0, 175);
			panelFirst.add(JumpFrame, c);
		}

		if (fourthDimensionSize > 1) {
			++c.gridy;
			panelFirst.add(fourthDimensionslider, c);

			++c.gridy;
			panelFirst.add(sliceText, c);

			++c.gridy;
			c.insets = new Insets(0, 225, 0, 225);
			panelFirst.add(JumpSlice, c);
		}

		panelFirst.setVisible(true);

		cl.show(panelCont, "1");

		mser.addItemListener(new MserListener());
		dog.addItemListener(new dogListener());
		
		Segmser.addItemListener(new SegMserListener());
		Segdog.addItemListener(new SegDogListener());
		

		JPanel control = new JPanel();
		control.add(new JButton(new AbstractAction("\u22b2Prev") {

			@Override
			public void actionPerformed(ActionEvent e) {
				CardLayout cl = (CardLayout) panelCont.getLayout();
				cl.previous(panelCont);
			}
		}));
		control.add(new JButton(new AbstractAction("Next\u22b3") {

			@Override
			public void actionPerformed(ActionEvent e) {
				CardLayout cl = (CardLayout) panelCont.getLayout();
				cl.next(panelCont);
			}
		}));

		// Snakes Panel

		final Button Singlesnake = new Button("Apply snakes to CurrentView");
		final Button Redosnake = new Button("Redo snakes for CurrentView");
		final Button snakes = new Button("Apply snakes to the third Dimension");
		final Button RedoViewsnakes = new Button("Redo snakes for User chosen View");
		final Button AutomatedSnake = new Button("Automated Snake run over third D for all slices along four D");
		final Checkbox Auto = new Checkbox("Constant parameters over Frames");

		// Selection tool panel

		final Scrollbar sizeXbar = new Scrollbar(Scrollbar.HORIZONTAL, sizeXinit, 10, 0, 10 + scrollbarSize);
		final Scrollbar sizeYbar = new Scrollbar(Scrollbar.HORIZONTAL, sizeYinit, 10, 0, 10 + scrollbarSize);
		final Label AdjustX = new Label("Adjust Size X");
		final Label AdjustY = new Label("Adjust Size Y");
		sizeX = computeValueFromScrollbarPosition(sizeXinit, sizeXMin, sizeXMax, scrollbarSize);
		sizeY = computeValueFromScrollbarPosition(sizeYinit, sizeYMin, sizeYMax, scrollbarSize);
		AdjustY.setForeground(new Color(255, 255, 255));
		AdjustY.setBackground(new Color(1, 0, 1));

		AdjustX.setForeground(new Color(255, 255, 255));
		AdjustX.setBackground(new Color(1, 0, 1));
		final Button ComputeRoi = new Button("Compute new ROI");

		final Label sizeTextX = new Label("Size X = " + sizeX, Label.CENTER);
		final Label sizeTextY = new Label("Size Y = " + sizeY, Label.CENTER);
		
		Progressmax = Rois.size();
		
		
		
		panelThird.setLayout(layout);
		final Label Namesnake = new Label("Step 3", Label.CENTER);
		
	
		panelThird.add(Namesnake, c);
		
		
		++c.gridy;
		c.insets = new Insets(10, 160, 10, 160);
		panelThird.add(Singlesnake, c);

		++c.gridy;
		c.insets = new Insets(10, 160, 10, 160);
		panelThird.add(Redosnake, c);

		++c.gridy;
		c.insets = new Insets(10, 10, 0, 0);
		panelThird.add(AdjustX, c);
		++c.gridy;
		c.insets = new Insets(10, 10, 0, 0);
		panelThird.add(sizeXbar, c);

		++c.gridy;
		c.insets = new Insets(10, 10, 0, 0);
		panelThird.add(sizeTextX, c);
		++c.gridy;
		c.insets = new Insets(10, 10, 0, 0);
		panelThird.add(AdjustY, c);
		++c.gridy;
		c.insets = new Insets(10, 10, 0, 0);
		panelThird.add(sizeYbar, c);

		++c.gridy;
		c.insets = new Insets(10, 10, 0, 0);
		panelThird.add(sizeTextY, c);

		++c.gridy;
		c.insets = new Insets(10, 175, 0, 175);
		panelThird.add(ComputeRoi, c);

		/* Configuration */
		sizeXbar.addAdjustmentListener(new SizeXListener(sizeTextX, sizeXMin, sizeXMax));
		sizeYbar.addAdjustmentListener(new SizeYListener(sizeTextY, sizeYMin, sizeYMax));
		ComputeRoi.addActionListener(new ComputenewRoiListener());
		
		

		
		
		/* Location */
		panelFourth.setLayout(layout);
		final Label Namebig = new Label("Step 4", Label.CENTER);
		panelFourth.add(Namebig, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 4;
		c.weighty = 1.5;

		if (thirdDimensionSize > 1) {
			++c.gridy;
			c.insets = new Insets(0, 145, 0, 105);
			panelFourth.add(Auto, c);

			++c.gridy;
			c.insets = new Insets(10, 160, 10, 160);
			panelFourth.add(snakes, c);
		}
		if (fourthDimensionSize > 1) {
			++c.gridy;
			c.insets = new Insets(0, 145, 0, 145);
			panelFourth.add(AutomatedSnake, c);
		}

		++c.gridy;
		c.insets = new Insets(10, 160, 10, 160);
		panelFourth.add(RedoViewsnakes, c);
		++c.gridy;

		// Tracker choice panel

		final Checkbox KalmanTracker = new Checkbox("Use Kalman Filter for tracking");
		final Checkbox NearestNeighborTracker = new Checkbox("Use Nearest Neighbour tracker");
		final Label Kal = new Label("Use Kalman Filter for probabilistic tracking");
		final Label Det = new Label("Use Nearest Neighbour tracking");
		final Checkbox txtfile = new Checkbox("Save tracks as TXT file", SaveTxt);
		final Checkbox xlsfile = new Checkbox("Save tracks as XLS file", SaveXLS);

		Kal.setForeground(new Color(255, 255, 255));
		Kal.setBackground(new Color(1, 0, 1));
		Det.setBackground(new Color(1, 0, 1));
		Det.setForeground(new Color(255, 255, 255));
		panelFifth.setLayout(layout);
		final Label Nametrack = new Label("Step 5", Label.CENTER);
		panelFifth.add(Nametrack, c);
		++c.gridy;
		c.insets = new Insets(10, 10, 0, 50);
		panelFifth.add(Kal, c);

		++c.gridy;
		c.insets = new Insets(10, 10, 0, 50);
		panelFifth.add(KalmanTracker, c);

		++c.gridy;
		c.insets = new Insets(10, 10, 0, 50);
		panelFifth.add(Det, c);

		++c.gridy;
		c.insets = new Insets(10, 10, 0, 50);
		panelFifth.add(NearestNeighborTracker, c);

		++c.gridy;
		c.insets = new Insets(10, 10, 0, 0);
		panelFifth.add(txtfile, c);

		++c.gridy;
		c.insets = new Insets(10, 10, 0, 0);
		panelFifth.add(xlsfile, c);

		KalmanTracker.addItemListener(new KalmanchoiceListener());
		NearestNeighborTracker.addItemListener(new NNListener());
		txtfile.addItemListener(new SaveasTXT());
		xlsfile.addItemListener(new SaveasXLS());
		thirdDimensionslider
				.addAdjustmentListener(new thirdDimensionsliderListener(timeText, timeMin, thirdDimensionSize));
		fourthDimensionslider
				.addAdjustmentListener(new fourthDimensionsliderListener(sliceText, sliceMin, fourthDimensionSize));

		JumpFrame.addActionListener(
				new moveInThirdDimListener(thirdDimensionslider, timeText, timeMin, thirdDimensionSize));
		JumpSlice.addActionListener(
				new moveInFourthDimListener(fourthDimensionslider, sliceText, sliceMin, fourthDimensionSize));

		Singlesnake.addActionListener(new SinglesnakeButtonListener());
		Redosnake.addActionListener(new SinglesnakeButtonListener());
		snakes.addActionListener(new snakeButtonListener());
		RedoViewsnakes.addActionListener(new RedosnakeButtonListener());
		AutomatedSnake.addActionListener(new moveAllListener());
		Auto.addItemListener(new AutoListener());
		Ends.setForeground(new Color(255, 255, 255));
		Ends.setBackground(new Color(1, 0, 1));
		
		
		
		panelSeventh.setLayout(layout);
		final Label Done = new Label("Hope that everything was to your satisfaction!");
		final Button Exit= new Button("Close and exit");
		
		
		Done.setBackground(new Color(1, 0, 1));
		Done.setForeground(new Color(255, 255, 255));
		++c.gridy;
		c.insets = new Insets(10, 10, 0, 50);
		panelSeventh.add(Done, c);
		
		++c.gridy;
		c.insets = new Insets(10, 10, 0, 50);
		panelSeventh.add(Exit, c);
		
		

		Exit.addActionListener(new FinishedButtonListener(Cardframe, true));
		
		Cardframe.add(panelCont, BorderLayout.CENTER);
		Cardframe.add(control, BorderLayout.SOUTH);
		Cardframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		Cardframe.setResizable(true);
		Cardframe.setVisible(true);
		Cardframe.pack();

	}
	protected class FinishedButtonListener implements ActionListener {
		final JFrame parent;
		final boolean cancel;

		public FinishedButtonListener(JFrame parent, final boolean cancel) {
			this.parent = parent;
			this.cancel = cancel;
		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {
			wasCanceled = cancel;
			close(parent, sliceObserver, roiListener);
		}
	}
	
	protected final void close(final JFrame parent, final SliceObserver sliceObserver, RoiListener roiListener) {
		if (parent != null)
			parent.dispose();

	

		isFinished = true;
	}
	protected class ComputenewRoiListener implements ActionListener {

		
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			showNew = true;
			updatePreview(ValueChange.SHOWNEW);

		}
	}

	
	

	
	protected class SizeXListener implements AdjustmentListener {
		final Label label;
		final float min, max;

		public SizeXListener(final Label label, final float min, final float max) {
			this.label = label;
			this.min = min;
			this.max = max;
		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			sizeX = computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);
			label.setText("SizeX = " + sizeX);

			if (!isComputing) {
				updatePreview(ValueChange.SizeX);
			} else if (!event.getValueIsAdjusting()) {
				while (isComputing) {
					SimpleMultiThreading.threadWait(10);
				}
				updatePreview(ValueChange.SizeX);
			}
		}
	}

	protected class SizeYListener implements AdjustmentListener {
		final Label label;
		final float min, max;

		public SizeYListener(final Label label, final float min, final float max) {
			this.label = label;
			this.min = min;
			this.max = max;
		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			sizeY = computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);
			label.setText("SizeY = " + sizeY);

			if (!isComputing) {
				updatePreview(ValueChange.SizeY);
			} else if (!event.getValueIsAdjusting()) {
				while (isComputing) {
					SimpleMultiThreading.threadWait(10);
				}
				updatePreview(ValueChange.SizeY);
			}
		}
	}

	
	protected class SegDogListener implements ItemListener {
		@Override
		public void itemStateChanged(final ItemEvent arg0) {
			boolean oldState = findBlobsViaSEGMSER;

			if (arg0.getStateChange() == ItemEvent.DESELECTED)
				findBlobsViaSEGDOG = false;
			else if (arg0.getStateChange() == ItemEvent.SELECTED) {

				findBlobsViaSEGDOG = true;
				findBlobsViaMSER = false;
				findBlobsViaDOG = false;
				findBlobsViaSEGMSER = false;
				updatePreview(ValueChange.ROI);

				panelSecond.removeAll();
				
				final GridBagLayout layout = new GridBagLayout();
				final GridBagConstraints c = new GridBagConstraints();

				panelSecond.setLayout(layout);
				final Label Name = new Label("Step 2", Label.CENTER);
				panelSecond.add(Name, c);
				final Label exthresholdText = new Label("threshold = threshold to create Bitimg for watershedding.",
						Label.CENTER);
			

				// IJ.log("Determining the initial threshold for the image");
				// thresholdHoughInit =
				// GlobalThresholding.AutomaticThresholding(currentPreprocessedimg);
				final Scrollbar thresholdSHough = new Scrollbar(Scrollbar.HORIZONTAL, thresholdHoughInit, 10, 0,
						10 + scrollbarSize);
				thresholdHough = computeValueFromScrollbarPosition(thresholdHoughInit, thresholdHoughMin, thresholdHoughMax, scrollbarSize);


				final Checkbox displayBit = new Checkbox("Display Bitimage ", displayBitimg);
				final Checkbox displayWatershed = new Checkbox("Display Watershedimage ", displayWatershedimg);
				final Label thresholdText = new Label("thresholdValue = ", Label.CENTER);
			
				final Button Dowatershed = new Button("Do watershedding");
				final Label Segparam = new Label("Determine Threshold level for Segmentation");
				Segparam.setBackground(new Color(1, 0, 1));
				Segparam.setForeground(new Color(255, 255, 255));

				/* Location */

				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 0;
				c.gridy = 0;
				c.weightx = 1;
				c.weighty = 1.5;
				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(Segparam, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(exthresholdText, c);
				++c.gridy;

			
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(thresholdText, c);
				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				
				panelSecond.add(thresholdSHough, c);
				++c.gridy;
				
				c.insets = new Insets(10, 175, 0, 175);
				panelSecond.add(displayBit, c);

				++c.gridy;
				c.insets = new Insets(10, 175, 0, 175);
				panelSecond.add(displayWatershed, c);
				++c.gridy;
				c.insets = new Insets(10, 175, 0, 175);
				panelSecond.add(Dowatershed, c);


				thresholdSHough.addAdjustmentListener(new ThresholdHoughListener(thresholdText, thresholdHoughMin, thresholdHoughMax));

			

				displayBit.addItemListener(new ShowBitimgListener());
				displayWatershed.addItemListener(new ShowwatershedimgListener());
				Dowatershed.addActionListener(new DowatershedListener());
				
				
				
				
				
				final Label DogText = new Label("Use DoG to find Blobs ", Label.CENTER);
				final Scrollbar sigma1 = new Scrollbar(Scrollbar.HORIZONTAL, sigmaInit, 10, 0, 10 + scrollbarSize);

				final Scrollbar thresholdSsec = new Scrollbar(Scrollbar.HORIZONTAL, thresholdInit, 10, 0,
						10 + scrollbarSize);
				sigma = computeValueFromScrollbarPosition(sigmaInit, sigmaMin, sigmaMax, scrollbarSize);
				threshold = computeValueFromScrollbarPosition(thresholdInit, thresholdMin, thresholdMax, scrollbarSize);
				sigma2 = computeSigma2(sigma, sensitivity);
				final int sigma2init = computeScrollbarPositionFromValue(sigma2, sigmaMin, sigmaMax, scrollbarSize);
				final Scrollbar sigma2S = new Scrollbar(Scrollbar.HORIZONTAL, sigma2init, 10, 0, 10 + scrollbarSize);

				final Label sigmaText1 = new Label("Sigma 1 = " + sigma, Label.CENTER);
				final Label sigmaText2 = new Label("Sigma 2 = " + sigma2, Label.CENTER);

				final Label thresholdsecText = new Label("Threshold = " + threshold, Label.CENTER);

				final Checkbox min = new Checkbox("Look for Minima (green)", lookForMinima);
				final Checkbox max = new Checkbox("Look for Maxima (red)", lookForMaxima);
				final Button DisplayBlobs = new Button("Display Blobs");

				final Label MSparam = new Label("Determine DoG parameters");
				MSparam.setBackground(new Color(1, 0, 1));
				MSparam.setForeground(new Color(255, 255, 255));
				/* Location */


				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(MSparam, c);
				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(sigma1, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(sigmaText1, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(sigma2S, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(sigmaText2, c);

				++c.gridy;
				c.insets = new Insets(10, 0, 0, 0);
				panelSecond.add(thresholdSsec, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(thresholdsecText, c);

				++c.gridy;
				c.insets = new Insets(0, 170, 0, 75);
				panelSecond.add(min, c);

				++c.gridy;
				c.insets = new Insets(0, 170, 0, 75);
				panelSecond.add(max, c);

				++c.gridy;
				c.insets = new Insets(0, 180, 0, 180);
				panelSecond.add(DisplayBlobs, c);

				/* Configuration */
				sigma1.addAdjustmentListener(
						new SigmaListener(sigmaText1, sigmaMin, sigmaMax, scrollbarSize, sigma1, sigma2S, sigmaText2));
				sigma2S.addAdjustmentListener(
						new Sigma2Listener(sigmaMin, sigmaMax, scrollbarSize, sigma2S, sigmaText2));
				thresholdSsec.addAdjustmentListener(new ThresholdListener(thresholdsecText, thresholdMin, thresholdMax));
				min.addItemListener(new MinListener());
				max.addItemListener(new MaxListener());
				DisplayBlobs.addActionListener(new DisplaysegBlobsListener());
				panelSecond.repaint();
				panelSecond.validate();
				Cardframe.pack();
			}

			if (findBlobsViaSEGDOG != oldState) {
				while (isComputing)
					SimpleMultiThreading.threadWait(10);

				updatePreview(ValueChange.FindBlobsVia);
			}
		}
	}
	
	
	protected class dogListener implements ItemListener {

		@Override
		public void itemStateChanged(final ItemEvent arg0) {

			boolean oldState = findBlobsViaDOG;
			if (arg0.getStateChange() == ItemEvent.DESELECTED)
				findBlobsViaDOG = false;
			else if (arg0.getStateChange() == ItemEvent.SELECTED) {

				findBlobsViaDOG = true;
				findBlobsViaMSER = false;
				updatePreview(ValueChange.ROI);

				panelSecond.removeAll();
				
				final GridBagLayout layout = new GridBagLayout();
				final GridBagConstraints c = new GridBagConstraints();

				panelSecond.setLayout(layout);
				final Label Name = new Label("Step 2", Label.CENTER);
				panelSecond.add(Name, c);
				final Label DogText = new Label("Use DoG to find Blobs ", Label.CENTER);
				final Scrollbar sigma1 = new Scrollbar(Scrollbar.HORIZONTAL, sigmaInit, 10, 0, 10 + scrollbarSize);

				final Scrollbar thresholdS = new Scrollbar(Scrollbar.HORIZONTAL, thresholdInit, 10, 0,
						10 + scrollbarSize);
				sigma = computeValueFromScrollbarPosition(sigmaInit, sigmaMin, sigmaMax, scrollbarSize);
				threshold = computeValueFromScrollbarPosition(thresholdInit, thresholdMin, thresholdMax, scrollbarSize);
				sigma2 = computeSigma2(sigma, sensitivity);
				final int sigma2init = computeScrollbarPositionFromValue(sigma2, sigmaMin, sigmaMax, scrollbarSize);
				final Scrollbar sigma2S = new Scrollbar(Scrollbar.HORIZONTAL, sigma2init, 10, 0, 10 + scrollbarSize);

				final Label sigmaText1 = new Label("Sigma 1 = " + sigma, Label.CENTER);
				final Label sigmaText2 = new Label("Sigma 2 = " + sigma2, Label.CENTER);

				final Label thresholdText = new Label("Threshold = " + threshold, Label.CENTER);

				final Checkbox min = new Checkbox("Look for Minima (green)", lookForMinima);
				final Checkbox max = new Checkbox("Look for Maxima (red)", lookForMaxima);
				final Button DisplayBlobs = new Button("Display Blobs");

				final Label MSparam = new Label("Determine DoG parameters");
				MSparam.setBackground(new Color(1, 0, 1));
				MSparam.setForeground(new Color(255, 255, 255));
				/* Location */
				panelSecond.setLayout(layout);

				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 0;
				c.gridy = 0;
				c.weightx = 4;
				c.weighty = 1.5;

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(MSparam, c);
				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(sigma1, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(sigmaText1, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(sigma2S, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(sigmaText2, c);

				++c.gridy;
				c.insets = new Insets(10, 0, 0, 0);
				panelSecond.add(thresholdS, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(thresholdText, c);

				++c.gridy;
				c.insets = new Insets(0, 170, 0, 75);
				panelSecond.add(min, c);

				++c.gridy;
				c.insets = new Insets(0, 170, 0, 75);
				panelSecond.add(max, c);

				++c.gridy;
				c.insets = new Insets(0, 180, 0, 180);
				panelSecond.add(DisplayBlobs, c);

				/* Configuration */
				sigma1.addAdjustmentListener(
						new SigmaListener(sigmaText1, sigmaMin, sigmaMax, scrollbarSize, sigma1, sigma2S, sigmaText2));
				sigma2S.addAdjustmentListener(
						new Sigma2Listener(sigmaMin, sigmaMax, scrollbarSize, sigma2S, sigmaText2));
				thresholdS.addAdjustmentListener(new ThresholdListener(thresholdText, thresholdMin, thresholdMax));
				min.addItemListener(new MinListener());
				max.addItemListener(new MaxListener());
				DisplayBlobs.addActionListener(new DisplayBlobsListener());
				panelSecond.repaint();
				panelSecond.validate();
				Cardframe.pack();
			}

			if (findBlobsViaDOG != oldState) {
				while (isComputing)
					SimpleMultiThreading.threadWait(10);

				updatePreview(ValueChange.FindBlobsVia);
			}
		}

	}

	protected class NNListener implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent arg0) {
			if (arg0.getStateChange() == ItemEvent.DESELECTED) {
				showNN = false;

			} else if (arg0.getStateChange() == ItemEvent.SELECTED) {

				showNN = true;
				panelSixth.removeAll();
				panelSixth.repaint();
				final GridBagLayout layout = new GridBagLayout();
				final GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 0;
				c.gridy = 0;
				c.weightx = 1;
				panelSixth.setLayout(layout);
				final Label Name = new Label("Step 6", Label.CENTER);
				panelSixth.add(Name, c);
				initialSearchradius = computeValueFromScrollbarPosition(initialSearchradiusInit, initialSearchradiusMin,
						initialSearchradiusMax, scrollbarSize);

				final Button track = new Button("Start Tracking");

				final Scrollbar Maxrad = new Scrollbar(Scrollbar.HORIZONTAL, maxSearchradiusInit, 10, 0,
						10 + scrollbarSize);
				maxSearchradius = computeValueFromScrollbarPosition(maxSearchradiusInit, maxSearchradiusMin,
						maxSearchradiusMax, scrollbarSize);
				final Label MaxMovText = new Label("Max Movment of Objects per frame: " + maxSearchradius,
						Label.CENTER);
				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelSixth.add(MaxMovText, c);
				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelSixth.add(Maxrad, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelSixth.add(track, c);

				Maxrad.addAdjustmentListener(
						new maxSearchradiusListener(MaxMovText, maxSearchradiusMin, maxSearchradiusMax));

				track.addActionListener(new TrackerButtonListener());

			}

		}

	}

	protected class KalmanchoiceListener implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent arg0) {
			if (arg0.getStateChange() == ItemEvent.DESELECTED) {
				showKalman = false;

			} else if (arg0.getStateChange() == ItemEvent.SELECTED) {

				showKalman = true;
				panelSixth.removeAll();

				final GridBagLayout layout = new GridBagLayout();
				final GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 0;
				c.gridy = 0;
				c.weightx = 1;
				panelSixth.setLayout(layout);
				final Label Name = new Label("Step 6", Label.CENTER);
				panelSixth.add(Name, c);
				final Scrollbar rad = new Scrollbar(Scrollbar.HORIZONTAL, initialSearchradiusInit, 10, 0,
						10 + scrollbarSize);
				initialSearchradius = computeValueFromScrollbarPosition(initialSearchradiusInit, initialSearchradiusMin,
						initialSearchradiusMax, scrollbarSize);

				final Scrollbar alphabar = new Scrollbar(Scrollbar.HORIZONTAL, alphaInit, 10, 0, 10 + scrollbarSize);
				alpha = computeValueFromScrollbarPosition(alphaInit, alphaMin, alphaMax, scrollbarSize);

				final Scrollbar betabar = new Scrollbar(Scrollbar.HORIZONTAL, betaInit, 10, 0, 10 + scrollbarSize);
				beta = computeValueFromScrollbarPosition(betaInit, betaMin, betaMax, scrollbarSize);
				final Label CostalphaText = new Label(
						"Weightage for minimizing costs based on distance ( 0 - 1):  " + alpha, Label.CENTER);
				final Label CostbetaText = new Label(
						"Weightage for minimizing costs based on pixel ratio (0 - 1):  " + beta, Label.CENTER);
				final Button track = new Button("Start Tracking");

				final Label SearchText = new Label("Initial Search Radius: " + initialSearchradius, Label.CENTER);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelSixth.add(SearchText, c);
				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelSixth.add(rad, c);

				final Scrollbar Maxrad = new Scrollbar(Scrollbar.HORIZONTAL, maxSearchradiusInit, 10, 0,
						10 + scrollbarSize);
				maxSearchradius = computeValueFromScrollbarPosition(maxSearchradiusInit, maxSearchradiusMin,
						maxSearchradiusMax, scrollbarSize);
				final Label MaxMovText = new Label("Max Movment of Objects per frame: " + maxSearchradius,
						Label.CENTER);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelSixth.add(MaxMovText, c);
				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelSixth.add(Maxrad, c);

				final Scrollbar Miss = new Scrollbar(Scrollbar.HORIZONTAL, missedframesInit, 10, 0, 10 + scrollbarSize);
				Miss.setBlockIncrement(1);
				missedframes = (int) computeValueFromScrollbarPosition(missedframesInit, missedframesMin,
						missedframesMax, scrollbarSize);
				final Label LostText = new Label("Objects allowed to be lost for #frames" + missedframes, Label.CENTER);
				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelSixth.add(LostText, c);
				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelSixth.add(Miss, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelSixth.add(CostalphaText, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelSixth.add(alphabar, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelSixth.add(CostbetaText, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelSixth.add(betabar, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelSixth.add(track, c);

				rad.addAdjustmentListener(
						new SearchradiusListener(SearchText, initialSearchradiusMin, initialSearchradiusMax));
				Maxrad.addAdjustmentListener(
						new maxSearchradiusListener(MaxMovText, maxSearchradiusMin, maxSearchradiusMax));
				Miss.addAdjustmentListener(new missedFrameListener(LostText, missedframesMin, missedframesMax));
				alphabar.addAdjustmentListener(new AlphabarListener(CostalphaText, alphaInit, alphaMax));
				betabar.addAdjustmentListener(new BetabarListener(CostbetaText, betaInit, betaMax));

				track.addActionListener(new TrackerButtonListener());

				panelSixth.repaint();
				panelSixth.validate();
			}

		}

	}

	protected class SearchradiusListener implements AdjustmentListener {
		final Label label;
		final float min, max;

		public SearchradiusListener(final Label label, final float min, final float max) {
			this.label = label;
			this.min = min;
			this.max = max;
		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			initialSearchradius = computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);
			label.setText("Initial Search Radius:  = " + initialSearchradius);

			if (!isComputing) {
				updatePreview(ValueChange.iniSearch);
			} else if (!event.getValueIsAdjusting()) {
				while (isComputing) {
					SimpleMultiThreading.threadWait(10);
				}
				updatePreview(ValueChange.iniSearch);
			}
		}
	}

	protected class AlphabarListener implements AdjustmentListener {
		final Label label;
		final float min, max;

		public AlphabarListener(final Label label, final float min, final float max) {
			this.label = label;
			this.min = min;
			this.max = max;
		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			alpha = computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);
			label.setText("Weightage for minimizing costs based on distance ( 0 - 1):  = " + alpha);

			if (!event.getValueIsAdjusting()) {
				while (isComputing) {
					SimpleMultiThreading.threadWait(10);
				}
				updatePreview(ValueChange.Alpha);
			}
		}
	}

	protected class BetabarListener implements AdjustmentListener {
		final Label label;
		final float min, max;

		public BetabarListener(final Label label, final float min, final float max) {
			this.label = label;
			this.min = min;
			this.max = max;
		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			beta = computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);
			label.setText("Weightage for minimizing costs based on pixel ratio ( 0 - 1):  = " + beta);

			if (!event.getValueIsAdjusting()) {
				while (isComputing) {
					SimpleMultiThreading.threadWait(10);
				}
				updatePreview(ValueChange.Beta);
			}
		}
	}

	protected class maxSearchradiusListener implements AdjustmentListener {
		final Label label;
		final float min, max;

		public maxSearchradiusListener(final Label label, final float min, final float max) {
			this.label = label;
			this.min = min;
			this.max = max;
		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			maxSearchradius = computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);
			label.setText("Max Search Radius:  = " + maxSearchradius);

			if (!isComputing) {
				updatePreview(ValueChange.maxSearch);
			} else if (!event.getValueIsAdjusting()) {
				while (isComputing) {
					SimpleMultiThreading.threadWait(10);
				}
				updatePreview(ValueChange.maxSearch);
			}
		}
	}

	protected class missedFrameListener implements AdjustmentListener {
		final Label label;
		final float min, max;

		public missedFrameListener(final Label label, final float min, final float max) {
			this.label = label;
			this.min = min;
			this.max = max;
		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			missedframes = (int) computeIntValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);
			label.setText("Missed frames:  = " + missedframes);

			if (!isComputing) {
				updatePreview(ValueChange.missedframes);
			} else if (!event.getValueIsAdjusting()) {
				while (isComputing) {
					SimpleMultiThreading.threadWait(10);
				}
				updatePreview(ValueChange.missedframes);
			}
		}
	}

	/**
	 * Instantiates the panel for adjusting the paramters
	 */

	

	protected class fourthDimensionsliderListener implements AdjustmentListener {
		final Label label;
		final float min, max;

		public fourthDimensionsliderListener(final Label label, final float min, final float max) {
			this.label = label;
			this.min = min;
			this.max = max;
		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			fourthDimensionslider = (int) computeIntValueFromScrollbarPosition(event.getValue(), min, max,
					scrollbarSize);
			label.setText("Slicenumber = " + fourthDimensionslider);

			fourthDimension = fourthDimensionslider;

			if (!isComputing) {
				imp = WindowManager.getCurrentImage();
				sliceObserver = new SliceObserver(imp, new ImagePlusListener());
				imp.setPosition(channel, fourthDimension, imp.getFrame());
				if (fourthDimension > fourthDimensionSize) {
					IJ.log("Max slice number exceeded, moving to last slice instead");
					imp.setPosition(channel, fourthDimensionSize, imp.getFrame());
					fourthDimension = fourthDimensionSize;
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
				CurrentView = getCurrentView();
				otherCurrentView = getotherCurrentView();
				MouseEvent mev = new MouseEvent(imp.getCanvas(), MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(),
						0, 0, 0, 10, false);
				/*
				 * if ((change == ValueChange.ROI || change == ValueChange.SIGMA
				 * || change == ValueChange.MINMAX || change ==
				 * ValueChange.FOURTHDIM || change == ValueChange.THRESHOLD &&
				 * RoisOrig != null)) {
				 */
				if (mev != null) {
					// compute first version
					updatePreview(ValueChange.FOURTHDIM);

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
				imp.setPosition(channel, fourthDimension, imp.getFrame());
				if (fourthDimension > fourthDimensionSize) {
					IJ.log("Max frame number exceeded, moving to last frame instead");
					imp.setPosition(channel, fourthDimensionSize, imp.getFrame());
					fourthDimension = fourthDimensionSize;
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
				CurrentView = getCurrentView();
				otherCurrentView = getotherCurrentView();
				MouseEvent mev = new MouseEvent(imp.getCanvas(), MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(),
						0, 0, 0, 10, false);
				/*
				 * if ((change == ValueChange.ROI || change == ValueChange.SIGMA
				 * || change == ValueChange.MINMAX || change ==
				 * ValueChange.FOURTHDIM || change == ValueChange.THRESHOLD &&
				 * RoisOrig != null)) {
				 */
				if (mev != null) {
					// compute first version
					updatePreview(ValueChange.FOURTHDIM);
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

	protected class SaveasTXT implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent arg0) {
			if (arg0.getStateChange() == ItemEvent.DESELECTED)
				SaveTxt = false;

			else if (arg0.getStateChange() == ItemEvent.SELECTED)
				SaveTxt = true;

		}

	}

	protected class SaveasXLS implements ItemListener {

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

	public ArrayList<Roi> getcurrentRois(ArrayList<RefinedPeak<Point>> peaks) {

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

	public ArrayList<Roi> getcurrentRois(MserTree<UnsignedByteType> newtree) {

		final HashSet<Mser<UnsignedByteType>> rootset = newtree.roots();

		ArrayList<Roi> Allrois = new ArrayList<Roi>();
		final Iterator<Mser<UnsignedByteType>> rootsetiterator = rootset.iterator();

		AllmeanCovar = new ArrayList<double[]>();

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

	public ArrayList<double[]> getRoiMean(MserTree<UnsignedByteType> newtree) {

		final HashSet<Mser<UnsignedByteType>> rootset = newtree.roots();

		final Iterator<Mser<UnsignedByteType>> rootsetiterator = rootset.iterator();

		AllmeanCovar = new ArrayList<double[]>();

		while (rootsetiterator.hasNext()) {

			Mser<UnsignedByteType> rootmser = rootsetiterator.next();

			if (rootmser.size() > 0) {

				final double[] meanandcov = { rootmser.mean()[0], rootmser.mean()[1]};
				AllmeanCovar.add(meanandcov);

			}
		}

		// We do this so the ROI remains attached the the same label and is not
		// changed if the program is run again
		SortListbyproperty.sortpointList(AllmeanCovar);
		

		return AllmeanCovar;

	}
	
	
	public RandomAccessibleInterval<FloatType> getCurrentSegment(final int label) {

		RandomAccessibleInterval<FloatType> Roiimg = Boundingboxes.CurrentLabelImage(intimg, currentimg, label);

		return Roiimg;

	}

	public RandomAccessibleInterval<FloatType> getCurrentView() {

		final FloatType type = originalimgA.randomAccess().get().createVariable();
		long[] dim = { originalimgA.dimension(0), originalimgA.dimension(1) };
		final ImgFactory<FloatType> factory = net.imglib2.util.Util.getArrayOrCellImgFactory(originalimgA, type);
		RandomAccessibleInterval<FloatType> totalimg = factory.create(dim, type);

		if (thirdDimensionSize == 1) {

			totalimg = originalimgA;
		}

		if (fourthDimensionSize == 1 && thirdDimensionSize > 1) {

			totalimg = Views.hyperSlice(originalimgA, 2, thirdDimension - 1);

		}

		if (fourthDimensionSize > 1) {

			totalimg = Views.hyperSlice(originalimgA, 3, fourthDimension - 1);
			totalimg = Views.hyperSlice(originalimgA, 2, thirdDimension - 1);

		}

		return totalimg;

	}

	public RandomAccessibleInterval<FloatType> getotherCurrentView() {

		final FloatType type = originalimgB.randomAccess().get().createVariable();
		long[] dim = { originalimgB.dimension(0), originalimgB.dimension(1) };
		final ImgFactory<FloatType> factory = net.imglib2.util.Util.getArrayOrCellImgFactory(originalimgB, type);
		RandomAccessibleInterval<FloatType> totalimg = factory.create(dim, type);

		if (thirdDimensionSize == 1) {

			totalimg = originalimgB;
		}

		if (fourthDimensionSize == 1 && thirdDimensionSize > 1) {

			totalimg = Views.hyperSlice(originalimgB, 2, thirdDimension - 1);

		}

		if (fourthDimensionSize > 1) {

			totalimg = Views.hyperSlice(originalimgB, 3, fourthDimension - 1);
			totalimg = Views.hyperSlice(originalimgB, 2, thirdDimension - 1);

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
			final Roi roi = imp.getRoi();

			if (roi == null || roi.getType() != Roi.RECTANGLE)
				return;

			while (isComputing)
				SimpleMultiThreading.threadWait(10);

			updatePreview(ValueChange.ROI);
		}

	}

	protected class DeltaListener implements AdjustmentListener {
		final Label label;
		final float min, max;
		final int scrollbarSize;

		final Scrollbar deltaScrollbar;

		public DeltaListener(final Label label, final float min, final float max, final int scrollbarSize,
				final Scrollbar deltaScrollbar) {
			this.label = label;
			this.min = min;
			this.max = max;
			this.scrollbarSize = scrollbarSize;

			this.deltaScrollbar = deltaScrollbar;

		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			delta = computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);

			deltaScrollbar.setValue(computeScrollbarPositionFromValue(delta, min, max, scrollbarSize));

			label.setText("Delta = " + delta);

			// if ( !event.getValueIsAdjusting() )
			{
				while (isComputing) {
					SimpleMultiThreading.threadWait(10);
				}
				updatePreview(ValueChange.DELTA);
			}
		}
	}

	protected class minSizeListener implements AdjustmentListener {
		final Label label;
		final float min, max;
		final int scrollbarSize;

		final Scrollbar minsizeScrollbar;

		public minSizeListener(final Label label, final float min, final float max, final int scrollbarSize,
				final Scrollbar minsizeScrollbar) {
			this.label = label;
			this.min = min;
			this.max = max;
			this.scrollbarSize = scrollbarSize;

			this.minsizeScrollbar = minsizeScrollbar;

		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			minSize = (int) computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);

			minsizeScrollbar.setValue(computeScrollbarPositionFromValue(minSize, min, max, scrollbarSize));

			label.setText("MinSize = " + minSize);

			// if ( !event.getValueIsAdjusting() )
			{
				while (isComputing) {
					SimpleMultiThreading.threadWait(10);
				}
				updatePreview(ValueChange.MINSIZE);
			}
		}
	}

	protected class maxSizeListener implements AdjustmentListener {
		final Label label;
		final float min, max;
		final int scrollbarSize;

		final Scrollbar maxsizeScrollbar;

		public maxSizeListener(final Label label, final float min, final float max, final int scrollbarSize,
				final Scrollbar maxsizeScrollbar) {
			this.label = label;
			this.min = min;
			this.max = max;
			this.scrollbarSize = scrollbarSize;

			this.maxsizeScrollbar = maxsizeScrollbar;

		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			maxSize = (int) computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);

			maxsizeScrollbar.setValue(computeScrollbarPositionFromValue(maxSize, min, max, scrollbarSize));

			label.setText("MaxSize = " + maxSize);

			// if ( !event.getValueIsAdjusting() )
			{
				while (isComputing) {
					SimpleMultiThreading.threadWait(10);
				}
				updatePreview(ValueChange.MAXSIZE);
			}
		}
	}

	protected class maxVarListener implements AdjustmentListener {
		final Label label;
		final float min, max;
		final int scrollbarSize;

		final Scrollbar maxVarScrollbar;

		public maxVarListener(final Label label, final float min, final float max, final int scrollbarSize,
				final Scrollbar maxVarScrollbar) {
			this.label = label;
			this.min = min;
			this.max = max;
			this.scrollbarSize = scrollbarSize;
			this.maxVarScrollbar = maxVarScrollbar;

		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			maxVar = (computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize));

			maxVarScrollbar.setValue(computeScrollbarPositionFromValue((float) maxVar, min, max, scrollbarSize));

			label.setText("MaxVar = " + maxVar);

			// if ( !event.getValueIsAdjusting() )
			{
				while (isComputing) {
					SimpleMultiThreading.threadWait(10);
				}
				updatePreview(ValueChange.MAXVAR);
			}
		}
	}

	protected class minDiversityListener implements AdjustmentListener {
		final Label label;
		final float min, max;
		final int scrollbarSize;

		final Scrollbar minDiversityScrollbar;

		public minDiversityListener(final Label label, final float min, final float max, final int scrollbarSize,
				final Scrollbar minDiversityScrollbar) {
			this.label = label;
			this.min = min;
			this.max = max;
			this.scrollbarSize = scrollbarSize;
			this.minDiversityScrollbar = minDiversityScrollbar;

		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			minDiversity = (computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize));

			minDiversityScrollbar
					.setValue(computeScrollbarPositionFromValue((float) minDiversity, min, max, scrollbarSize));

			label.setText("MinDiversity = " + minDiversity);

			// if ( !event.getValueIsAdjusting() )
			{
				while (isComputing) {
					SimpleMultiThreading.threadWait(10);
				}
				updatePreview(ValueChange.MINDIVERSITY);
			}
		}
	}

	/**
	 * Generic, type-agnostic method to create an identical copy of an Img
	 *
	 * @param currentPreprocessedimg2
	 *            - the Img to copy
	 * @return - the copy of the Img
	 */
	public Img<UnsignedByteType> copytoByteImage(final RandomAccessibleInterval<FloatType> input) {
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

	public Img<FloatType> copyImage(final RandomAccessibleInterval<FloatType> input) {
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

	/**
	 * 2D correlated Gaussian
	 * 
	 * @param mean
	 *            (x,y) components of mean vector
	 * @param cov
	 *            (xx, xy, yy) components of covariance matrix
	 * @return ImageJ roi
	 */
	public EllipseRoi createEllipse(final double[] mean, final double[] cov, final double nsigmas) {

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

	protected class TrackerButtonListener implements ActionListener {

		public TrackerButtonListener() {

		}

		public void actionPerformed(final ActionEvent arg0) {

			IJ.log("Start Tracking");

			// impcopy = ImageJFunctions.show(originalimg);

			UserchosenCostFunction = new PixelratiowDistCostFunction(alpha, beta);
			if (showKalman){
				blobtracker = new KFsearch(All3DSnakes, UserchosenCostFunction, maxSearchradius, initialSearchradius,
						thirdDimensionSize, missedframes);
				
				IJ.log("Kalman Filter parameters : ");
				IJ.log("Distance weightage alpha" + " " + alpha + " " +" Pixel ratio weightage beta" + " " + beta + " " + "  maxSearchradius " + " " + maxSearchradius);
				IJ.log(" initialSearchradius " + "  " + initialSearchradius + " " + " missedframes " + " " + missedframes);	
			}
			if (showNN){
				blobtracker = new NNsearch(All3DSnakes, maxSearchradius, thirdDimensionSize);
				IJ.log("Kalman Filter parameters : ");
				IJ.log("maxSearchradius " + " " + maxSearchradius);
				
				
			}
			
			
			
			blobtracker.reset();

			blobtracker.process();

			SimpleWeightedGraph<SnakeObject, DefaultWeightedEdge> graph = blobtracker.getResult();

			IJ.log("Tracking Complete " + " " + "Displaying results");

			ImagePlus totalimp = ImageJFunctions.show(originalimgA);
			DisplaymodelGraph totaldisplaytracks = new DisplaymodelGraph(totalimp, graph, colorDraw);
			totaldisplaytracks.getImp();

			TrackModel model = new TrackModel(graph);
			model.getDirectedNeighborIndex();
			ResultsTable rt = new ResultsTable();
			// Get all the track id's
			for (final Integer id : model.trackIDs(true)) {

				// Get the corresponding set for each id
				model.setName(id, "Track" + id);
				final HashSet<SnakeObject> Snakeset = model.trackSnakeObjects(id);
				ArrayList<SnakeObject> list = new ArrayList<SnakeObject>();

				Comparator<SnakeObject> ThirdDimcomparison = new Comparator<SnakeObject>() {

					@Override
					public int compare(final SnakeObject A, final SnakeObject B) {

						return A.thirdDimension - B.thirdDimension;

					}

				};

				Comparator<SnakeObject> FourthDimcomparison = new Comparator<SnakeObject>() {

					@Override
					public int compare(final SnakeObject A, final SnakeObject B) {

						return A.fourthDimension - B.fourthDimension;

					}

				};

				Iterator<SnakeObject> Snakeiter = Snakeset.iterator();

				while (Snakeiter.hasNext()) {

					SnakeObject currentsnake = Snakeiter.next();

					for (int d = 0; d < currentimg.numDimensions(); ++d)
						if (currentsnake.centreofMass[d] != Double.NaN)
							list.add(currentsnake);

				}
				Collections.sort(list, ThirdDimcomparison);
				if (fourthDimensionSize > 0)
					Collections.sort(list, FourthDimcomparison);
				// Write tracks with same track id to file
				if (SaveTxt) {

					NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
					nf.setMaximumFractionDigits(3);
					try {
						File fichier = new File(usefolder + "//" + addTrackToName + "TrackID" + id + ".txt");
						FileWriter fw = new FileWriter(fichier);
						BufferedWriter bw = new BufferedWriter(fw);

						bw.write(
								"\tFramenumber\tTrackID\tCenterofMassX\tCenterofMassY\tCenterofMassZ\tIntensityTrackimage"
										+ "\tMeanIntensityTrackimage\tNumberofpixelsTrack\tIntensityMeasureimage\tMeanIntensityMeasureimage\tNumberofpixelsMeasure\n");

						for (int index = 0; index < list.size(); ++index) {

							bw.write("\t" + list.get(index).thirdDimension + "\t" + "\t" + id + "\t" + "\t"
									+ nf.format(list.get(index).centreofMass[0]) + "\t" + "\t"
									+ nf.format(list.get(index).centreofMass[1]) + "\t" + "\t"
									+ nf.format(list.get(index).centreofMass[2]) + "\t" + "\t"
									+ nf.format(list.get(index).IntensityROI) + "\t" + "\t"
									+ nf.format(list.get(index).numberofPixels) + "\t" + "\t"
									+ nf.format(list.get(index).meanIntensityROI) + "\t" + "\t"
									+ nf.format(list.get(index).IntensitySecROI) + "\t" + "\t"
									+ nf.format(list.get(index).numberofPixelsSecRoI) + "\t" + "\t"
									+ nf.format(list.get(index).meanIntensitySecROI) + "\t" + "\t" + "\n");

						}
						bw.close();
						fw.close();
					} catch (IOException e) {
					}
				}
				for (int index = 0; index < list.size(); ++index) {

					rt.incrementCounter();

					rt.addValue("FrameNumber", list.get(index).thirdDimension);
					rt.addValue("Track iD", id);
					rt.addValue("Center of Mass X", list.get(index).centreofMass[0]);
					rt.addValue("Center of Mass Y", list.get(index).centreofMass[1]);
					rt.addValue("Center of Mass Z", list.get(index).centreofMass[2]);
					rt.addValue("Intensity Trackimage", list.get(index).IntensityROI);
					rt.addValue("Number of Pixels Trackimage ", list.get(index).numberofPixels);
					rt.addValue("Mean Intensity Trackimage", list.get(index).meanIntensityROI);
					rt.addValue("Intensity Measureimage", list.get(index).IntensitySecROI);
					rt.addValue("Number of Pixels Measureimage ", list.get(index).numberofPixelsSecRoI);
					rt.addValue("Mean Intensity Measureimage ", list.get(index).meanIntensitySecROI);
					Overlay o = totaldisplaytracks.getImp().getOverlay();

				
				

				}

				if (SaveXLS)
					saveResultsToExcel(usefolder + "//" + addTrackToName + ".xls", rt);

				
			
				
				
			}
			
			


			rt.show("Results");

		}

	}

	public void saveResultsToExcel(String xlFile, ResultsTable rt) {

		FileOutputStream xlOut = null;
		try {

			xlOut = new FileOutputStream(xlFile);
		} catch (FileNotFoundException ex) {

			Logger.getLogger(InteractiveActiveContour_.class.getName()).log(Level.SEVERE, null, ex);
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

		int numRows = rt.size();
		String[] colHeaders = rt.getHeadings();
		int rownum = 0;

		// Create a Header
		r = xlSheet.createRow(rownum);

		for (int cellnum = 0; cellnum < colHeaders.length; cellnum++) {

			c = r.createCell((short) cellnum);
			c.setCellStyle(cb);
			c.setCellValue(colHeaders[cellnum]);

		}
		rownum++;

		for (int row = 0; row < numRows; row++) {

			r = xlSheet.createRow(rownum + row);
			int numCols = rt.getLastColumn() + 1;

			for (int cellnum = 0; cellnum < numCols; cellnum++) {

				c = r.createCell((short) cellnum);
				c.setCellValue(rt.getValueAsDouble(cellnum, row));
			}

		}

		try {
			xlBook.write(xlOut);
			xlOut.close();
		} catch (IOException ex) {
			Logger.getLogger(InteractiveActiveContour_.class.getName()).log(Level.SEVERE, null, ex);

		}

	}

	protected class CancelButtonListener implements ActionListener {
		final Frame parent;
		final boolean cancel;

		// usefolder + "//" + "StaticPropertieszStack" + "-z", fourthDimension
		public CancelButtonListener(Frame parent, final boolean cancel) {
			this.parent = parent;
			this.cancel = cancel;
		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {
			wasCanceled = cancel;
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

	protected static float computeIntValueFromScrollbarPosition(final int scrollbarPosition, final float min,
			final float max, final int scrollbarSize) {
		return min + (scrollbarPosition / (max)) * (max - min);
	}

	protected static int computeScrollbarPositionFromValue(final float sigma, final float min, final float max,
			final int scrollbarSize) {
		return Util.round(((sigma - min) / (max - min)) * scrollbarSize);
	}

	protected static int computeIntScrollbarPositionFromValue(final float thirdDimensionslider, final float min,
			final float max, final int scrollbarSize) {
		return Util.round(((thirdDimensionslider - min) / (max - min)) * max);
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
	
	protected class ThresholdHoughListener implements AdjustmentListener {
		final Label label;
		final float min, max;

		public ThresholdHoughListener(final Label label, final float min, final float max) {
			this.label = label;
			this.min = min;
			this.max = max;
		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			thresholdHough = computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);
			label.setText("Threshold = " + thresholdHough);

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

	protected class thirdDimensionsliderListener implements AdjustmentListener {
		final Label label;
		final float min, max;

		public thirdDimensionsliderListener(final Label label, final float min, final float max) {
			this.label = label;
			this.min = min;
			this.max = max;
		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			thirdDimensionslider = (int) computeIntValueFromScrollbarPosition(event.getValue(), min, max,
					scrollbarSize);
			label.setText("Framenumber = " + thirdDimensionslider);

			thirdDimension = thirdDimensionslider;

			sliceObserver = new SliceObserver(imp, new ImagePlusListener());
			imp.setPosition(channel, imp.getSlice(), thirdDimension);
			if (thirdDimension > thirdDimensionSize) {
				IJ.log("Max frame number exceeded, moving to last frame instead");
				imp.setPosition(channel, imp.getSlice(), thirdDimensionSize);
				thirdDimension = thirdDimensionSize;
			}

			Roi roi = imp.getRoi();
			if (roi == null) {
				// IJ.log( "A rectangular ROI is required to define the
				// area..."
				// );
				imp.setRoi(standardRectangle);
				roi = imp.getRoi();
			}

			CurrentView = getCurrentView();
			otherCurrentView = getotherCurrentView();
			MouseEvent mev = new MouseEvent(imp.getCanvas(), MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0,
					0, 0, 10, false);
			/*
			 * if ((change == ValueChange.ROI || change == ValueChange.SIGMA ||
			 * change == ValueChange.MINMAX || change == ValueChange.FOURTHDIM
			 * || change == ValueChange.THRESHOLD && RoisOrig != null)) {
			 */
			if (mev != null) {
				// compute first version
				updatePreview(ValueChange.THIRDDIM);

			}
			isStarted = true;

			// check whenever roi is modified to update accordingly
			roiListener = new RoiListener();
			imp.getCanvas().addMouseListener(roiListener);

		}
	}

	protected class ImagePlusListener implements SliceListener {
		@Override
		public void sliceChanged(ImagePlus arg0) {
			if (isStarted) {
				while (isComputing) {
					SimpleMultiThreading.threadWait(10);
				}
				updatePreview(ValueChange.THIRDDIM);

			}
		}
	}

	public double[] getCenter(Roi roi) {

		double[] center = new double[3];

		long[] maxVal = {Long.MIN_VALUE, Long.MIN_VALUE };
		long[] minVal = { Long.MAX_VALUE, Long.MAX_VALUE };
		int n = currentimg.numDimensions();
		Cursor<FloatType> currentcursor = Views.iterable(currentimg).localizingCursor();
		while(currentcursor.hasNext()){
			
			currentcursor.fwd();
			
			int x = currentcursor.getIntPosition(0);
			int y = currentcursor.getIntPosition(1);
			
			if (roi.contains(x, y)){
				
				for (int d = 0; d < n; ++d) {
				final long p = currentcursor.getLongPosition(d);
						if (p > maxVal[d])
							maxVal[d] = p;
						if (p < minVal[d])
							minVal[d] = p;
				
						
						
						
				}
				
				
				
			}
			
			
			
		}
		
		
		
		center[0] = (minVal[0] + maxVal[0]) / 2;
		center[1] = (minVal[1] + maxVal[1]) / 2;
		center[2] = 0;
		
		
		return center;

	}
	
	/** The name of the frame feature. */
	public static final String SNAKEPROGRESS= "SNAKEPROGRESS";
	
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
	

	public static void main(String args[]) {
		
		// SpimData2 d = loadSpimData( new File(
				// "/home/preibisch/Documents/Microscopy/SPIM/Drosophila
				// MS2-GFP//dataset.xml" ) );
				// getImg( d, 0, 50 );
				// 3D mCherry-test.tif
				// 4D one_division_4d-smallz.tif
		
		new ImageJ();
		

		JFrame frame = new JFrame("");
		FileChooser panel = new FileChooser();

		frame.getContentPane().add(panel, "Center");
		frame.setSize(panel.getPreferredSize());
	            
	
	}
}
