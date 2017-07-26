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
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
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
import java.util.HashMap;
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
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

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
import ij.io.FileSaver;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ColorProcessor;
import kdTreeBlobs.FlagNode;
import kdTreeBlobs.NNFlagsearchKDtree;
import listeners.DogListener;
import listeners.MoveInFourthDimListener;
import listeners.MoveInThirdDimListener;
import listeners.MserListener;
import listeners.SegDogListener;
import listeners.SegMserListener;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.multithreading.SimpleMultiThreading;
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
import net.imglib2.util.Pair;
import net.imglib2.util.RealSum;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import net.imglib2.view.composite.NumericComposite;
import overlaytrack.DisplayGraph;
import overlaytrack.DisplaymodelGraph;
import preProcessing.GetLocalmaxmin;
import segmentation.SegmentbyWatershed;
import swingClasses.Progress;
import swingClasses.ProgressAll;
import swingClasses.ProgressRedo;
import swingClasses.ProgressSnake;
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

	public final int scrollbarSize = 1000;
	/** Store the individual features, and their values. */
	private final ConcurrentHashMap<String, Double> features = new ConcurrentHashMap<String, Double>();
	public float sigma = 0.5f;
	public float sigma2 = 0.5f;
	public float threshold = 1f;
	public float deltaMin = 0;
	public ColorProcessor cp = null;
	public ColorProcessor measurecp = null;
	// steps per octave
	public static int standardSensitivity = 4;
	public int sensitivity = standardSensitivity;
	public JLabel label = new JLabel("Progress..");
	public JProgressBar jpb;
	public SimpleWeightedGraph<SnakeObject, DefaultWeightedEdge> graph;
	public JFrame frame = new JFrame();
	public JPanel panel = new JPanel();
	public float imageSigma = 0.5f;
	public float sigmaMin = 0.5f;
	public float sigmaMax = 100f;
	public float RadiusMeasureMin = 0;
	public float sizeYMin = 0;

	public float RadiusMeasureMax = 100f;
	public float sizeYMax = 100f;
	public int sigmaInit = 30;

	public int displaySelectedTrack;
	public ArrayList<Integer> IDALL = new ArrayList<Integer>();
	public float minDiversityMin = 0;
	public float minDiversityMax = 1;
	public int thirdDimensionslider = 1;
	public int thirdDimensionsliderInit = 1;
	public int timeMin = 1;
	public long minSize = 1;
	public long maxSize = 1000;
	public long minSizemin = 0;
	public long minSizemax = 10000;
	public long maxSizemin = 1;
	public long maxSizemax = 10000;
	public int fourthDimensionslider = 1;
	public int fourthDimensionsliderInit = 1;
	public int sliceMin = 1;
	public ArrayList<Roi> SnakeRoisA;
	public ArrayList<Roi> SnakeRoisB;

	// ROI original
	public int nbRois;
	public Roi rorig = null;
	public Roi processRoi = null;
	public float thresholdMin = 0f;
	public float thresholdMax = 1f;
	public int thresholdInit = 1;
	public float thresholdHoughMin = 0f;
	public float thresholdHoughMax = 255f;
	public int thresholdHoughInit = 1;

	public float RadiusMeasure = 0;
	
	public int radius = 1;
	public float maxVar = 1;
	public int Progressmin = 0;
	public int Progressmax = 100;
	public int max = Progressmax;
	public float minDiversity = 1;
	public float thresholdHough = 1;
	public FloatType minval = new FloatType(0);
	public FloatType maxval = new FloatType(1);
	public boolean TrackinT = true;

	public double minIntensityImage = Double.NaN;
	public double maxIntensityImage = Double.NaN;
	public String usefolder = IJ.getDirectory("imagej");
	public String addTrackToName = "TrackedBlobsID";
	public FinalInterval interval;
	public SliceObserver sliceObserver;
	public RoiListener roiListener;
	public RoiListener measureroiListener;
	public ImagePlus imp;
	public ImagePlus measureimp;
	public boolean darktobright = false;
	public int channel = 0;
	public RandomAccessibleInterval<FloatType> currentimg;
	public RandomAccessibleInterval<FloatType> othercurrentimg;
	public RandomAccessibleInterval<FloatType> originalimgA;
	public RandomAccessibleInterval<FloatType> originalimgB;
	public ImageStack snakestack;
	public ImageStack measuresnakestack;
	public HashMap<Integer, ArrayList<Roi>> AllSnakerois = new HashMap<Integer, ArrayList<Roi>>();
	public HashMap<Integer, ArrayList<Roi>> AllSnakeMeasurerois = new HashMap<Integer, ArrayList<Roi>>();
	public ArrayList<double[]> AllmeanCovar;
	public float deltaMax = 400f;
	public float maxVarMin = 0;
	public float maxVarMax = 1;
	private SimpleWeightedGraph<SnakeObject, DefaultWeightedEdge> graphZ;
	// Dimensions of the stack :
	public float alpha = 0.5f;
	public float beta = 0.5f;

	public int thirdDimension;
	public int fourthDimension;
	public int minDiversityInit = 1;
	public int thirdDimensionSize = 1;
	public int fourthDimensionSize = 1;
	public ArrayList<ABSnakeFast> snakeoverlay;
	public ArrayList<SnakeObject> currentsnakes;
	public int length = 0;
	public int height = 0;
	public int RadiusMeasureinit = 0;
	public int sizeYinit = 0;
	public RandomAccessibleInterval<FloatType> CurrentView;
	public RandomAccessibleInterval<FloatType> otherCurrentView;
	public ArrayList<RefinedPeak<Point>> peaks;
	public Color inactiveColor = new Color(0.95f, 0.95f, 0.95f);
	public Rectangle standardRectangle;
	public EllipseRoi standardEllipse;
	public boolean isComputing = false;
	public boolean showMSER = false;
	public boolean showDOG = false;
	public boolean showSegMSER = false;
	public boolean showSegDOG = false;
	public boolean NormalizeImage = false;
	public boolean Mediancurr = false;
	public boolean MedianAll = false;
	public boolean isStarted = false;
	public boolean enableSigma2 = false;
	public boolean sigma2IsAdjustable = true;
	public boolean propagate = true;
	public boolean updateThreshold = false;
	public boolean lookForMinima = false;
	public boolean Auto = true;
	public boolean lookForMaxima = true;
	
	public  File userfile;
	public float delta = 1f;
	public int deltaInit = 10;
	public int maxVarInit = 1;
	public int Maxlabel;
	public int alphaInit = 1;
	public int betaInit = 0;
	public int minSizeInit = 1;
	public int maxSizeInit = 100;
	public boolean showKalman = true;
	public boolean showNN = false;
	public boolean SaveTxt = true;
	public boolean SaveXLS = false;
	public ImagePlus impcopy;
	public double CalibrationX;
	public double CalibrationY;
	public boolean showNew = false;
	public BlobTracker blobtracker;
	public CostFunction<SnakeObject, SnakeObject> UserchosenCostFunction;
	public Color colorSelect = Color.red;
	public Color coloroutSelect = Color.CYAN;
	public Color colorCreate = Color.red;
	public Color colorDraw = Color.green;
	public Color colorKDtree = Color.blue;
	public Color colorOld = Color.MAGENTA;
	public Color colorPrevious = Color.gray;
	public Color colorFinal = Color.YELLOW;
	public Color colorRadius = Color.yellow;
	public float initialSearchradius = 10;
	public float maxSearchradius = 15;
	public int missedframes = 20;
	public int initialSearchradiusInit = (int) initialSearchradius;
	public float initialSearchradiusMin = 0;
	public float initialSearchradiusMax = 100;
	public float alphaMin = 0;
	public float alphaMax = 1;
	public float betaMin = 0;
	public float betaMax = 1;
	public MserTree<UnsignedByteType> newtree;
	public int maxSearchradiusInit = (int) maxSearchradius;
	public float maxSearchradiusMin = 10;
	public float maxSearchradiusMax = 500;
	public RandomAccessibleInterval<UnsignedByteType> newimg;
	public int missedframesInit = missedframes;
	public float missedframesMin = 0;
	public float missedframesMax = 100;
	public RandomAccessibleInterval<IntType> intimg;
	public ArrayList<ArrayList<SnakeObject>> AllSliceSnakes;
	public ArrayList<ArrayList<SnakeObject>> All3DSnakes;
	public ArrayList<ArrayList<SnakeObject>> All3DSnakescopy;
	public ArrayList<SnakeObject> ProbBlobs;
	ArrayList<Pair<Integer, Roi>> AllSelectedrois;
	ArrayList<Pair<Integer, double[]>> AllSelectedcenter;
	public boolean displayBitimg = false;
	public boolean displayWatershedimg = false;
	public boolean showProgress = false;
	public ArrayList<ComSnake> finalRois;
	public ArrayList<Roi> Rois;
	public ArrayList<Roi> NearestNeighbourRois;
	public ArrayList<Roi> BiggerRois;
	public Overlay overlay;
	public Overlay measureoverlay;
	public int inix = 1;
	public int iniy = 1;
	public static enum ValueChange {
		SIGMA, THRESHOLD, ROI, MINMAX, ALL, THIRDDIM, FOURTHDIM, maxSearch, iniSearch, missedframes, MINDIVERSITY, DELTA, MINSIZE, MAXSIZE, MAXVAR, DARKTOBRIGHT, FindBlobsVia, SHOWMSER, SHOWDOG, NORMALIZE, MEDIAN, THIRDDIMTrack, FOURTHDIMTrack, RadiusMeasure, SizeY, SHOWNEW, Beta, Alphapart, Alpha, Segmentation, SHOWSEGMSER, SHOWSEGDOG, DISPLAYBITIMG, DISPLAYWATERSHEDIMG, SHOWPROGRESS, RADIUS
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

	public boolean getshowMSER() {
		return showMSER;
	}

	public void setshowMSER(final boolean showMSER) {
		this.showMSER = showMSER;
	}

	public boolean getshowDOG() {
		return showDOG;
	}

	public void setshowDOG(final boolean showDOG) {
		this.showDOG = showDOG;
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
		thresholdHoughInit = computeScrollbarPositionFromValue(thresholdHough, thresholdHoughMin, thresholdHoughMax,
				scrollbarSize);
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
		standardRectangle = new Rectangle(inix, iniy, imp.getWidth() - 2 * inix, imp.getHeight() - 2 * iniy);
		originalimgA = ImageJFunctions.convertFloat(imp.duplicate());
		
		
		height = imp.getHeight();
		length = imp.getWidth();
	}

	public InteractiveActiveContour_(final RandomAccessibleInterval<FloatType> originalimgA, File userfile ) {
		this.originalimgA = originalimgA;
		this.userfile = userfile;
		standardRectangle = new Rectangle(inix, iniy, (int) originalimgA.dimension(0) - 2 * inix,
				(int) originalimgA.dimension(1) - 2 * iniy);

	}

	public InteractiveActiveContour_(final RandomAccessibleInterval<FloatType> originalimgA,
			final RandomAccessibleInterval<FloatType> originalimgB, File userfile) {
		this.originalimgA = originalimgA;
		this.originalimgB = originalimgB;
		this.userfile = userfile;
		standardRectangle = new Rectangle(inix, iniy, (int) originalimgA.dimension(0) - 2 * inix,
				(int) originalimgA.dimension(1) - 2 * iniy);

	}

	public void setMinIntensityImage(final double min) {
		this.minIntensityImage = min;
	}

	public void setMaxIntensityImage(final double max) {
		this.maxIntensityImage = max;
	}

	public void setInitialminDiversity(final float value) {
		minDiversity = value;
		minDiversityInit = computeScrollbarPositionFromValue(minDiversity, minDiversityMin, minDiversityMax,
				scrollbarSize);
	}

	public double getInitialminDiversity(final float value) {

		return minDiversity;

	}

	public void setInitialminSize(final int value) {
		minSize = value;
		minSizeInit = computeScrollbarPositionFromValue(minSize, minSizemin, minSizemax, scrollbarSize);
	}

	public double getInitialminSize(final int value) {

		return minSize;

	}

	public void setInitialmaxSize(final int value) {
		maxSize = value;
		maxSizeInit = computeScrollbarPositionFromValue(maxSize, maxSizemin, maxSizemax, scrollbarSize);
	}

	public double getInitialmaxSize(final int value) {

		return maxSize;

	}

	public void setInitialDelta(final float value) {
		delta = value;
		deltaInit = computeScrollbarPositionFromValue(delta, deltaMin, deltaMax, scrollbarSize);
	}

	public double getInitialDelta(final float value) {

		return delta;

	}

	public void setInitialAlpha(final float value) {
		alpha = value;
		alphaInit = computeScrollbarPositionFromValue(alpha, alphaMin, alphaMax, scrollbarSize);
	}

	public double getInitialAlpha(final float value) {

		return alpha;

	}
	
	public void setInitialBeta(final float value) {
		beta = value;
		betaInit = computeScrollbarPositionFromValue(beta, betaMin, betaMax, scrollbarSize);
	}

	public double getInitialBeta(final float value) {

		return beta;

	}
	
	public void setInitialsearchradius(final float value) {
		initialSearchradius = value;
		initialSearchradiusInit = computeScrollbarPositionFromValue(initialSearchradius, initialSearchradiusMin,
				initialSearchradiusMax, scrollbarSize);
	}

	public void setInitialmaxsearchradius(final float value) {
		maxSearchradius = value;
		maxSearchradiusInit = computeScrollbarPositionFromValue(maxSearchradius, maxSearchradiusMin, maxSearchradiusMax,
				scrollbarSize);
	}

	public double getInitialsearchradius(final float value) {

		return initialSearchradius;

	}

	public void setInitialUnstability_Score(final float value) {
		maxVar = value;
		maxVarInit = computeScrollbarPositionFromValue(maxVar, maxVarMin, maxVarMax, scrollbarSize);
	}

	public double getInitialUnstability_Score(final float value) {

		return maxVar;

	}

	@Override
	public void run(String arg) {
		usefolder  = userfile.getParentFile().getAbsolutePath();
		
		// Progressbar color
		// UIManager.put("ProgressBar.selectionForeground", Color.BLUE);
		// UIManager.put("ProgressBar.foreground", Color.BLUE);
		UIManager.put("ProgressBar.font", Font.BOLD);
		// UIManager.put("ProgressBar.background", Color.BLUE);
		AllSelectedrois = new ArrayList<Pair<Integer, Roi>>();
		AllSelectedcenter = new ArrayList<Pair<Integer, double[]>>();
		setInitialUnstability_Score(maxVarInit);
		setInitialDelta(deltaInit);
		setInitialAlpha(alphaInit);
		setInitialBeta(betaInit);
		setInitialminDiversity(minDiversityInit);
		setInitialmaxSize(maxSizeInit);
		setInitialminSize(minSizeInit);
		setInitialsearchradius(initialSearchradiusInit);
		setInitialmaxsearchradius(maxSearchradius);
		overlay = new Overlay();
		measureoverlay = new Overlay();
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

		thresholdMax = (float) (minPeakValue);

		thresholdMin = (float) (0);

		thresholdHough = minPeakValue;

		thresholdHoughMax = (float) (minPeakValue / 10);

		thresholdHoughMin = (float) (0);

		setthresholdMin(0);
		setthresholdMax(255);
		setThreshold(100);

		setthresholdHoughMin(0);
		setthresholdHoughMax(255);
		setThresholdHough(100);

		measureimp = ImageJFunctions.show(otherCurrentView);
		measureimp.setTitle("CurrentView of Measurment image");
		imp = ImageJFunctions.show(CurrentView);
		imp.setTitle("CurrentView of Tracking image");

		All3DSnakes = new ArrayList<ArrayList<SnakeObject>>();
		All3DSnakescopy = new ArrayList<ArrayList<SnakeObject>>();
		Rois = new ArrayList<Roi>();
		NearestNeighbourRois = new ArrayList<Roi>();
		SnakeRoisA = new ArrayList<Roi>();
		SnakeRoisB = new ArrayList<Roi>();
		impcopy = imp.duplicate();
		

		

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

	public boolean Dialogue() {
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

	public boolean DialogueSlice() {
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

	public boolean Dialoguesec() {
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

	public boolean DialogueRedo() {
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

	public void updatePreview(final ValueChange change) {
		
		
		CurrentView = getCurrentView();
		otherCurrentView = getotherCurrentView();
		
		
		overlay = imp.getOverlay();
		boolean roiChanged = false;
		overlay = imp.getOverlay();
		if (overlay == null) {

			overlay = new Overlay();
			imp.setOverlay(overlay);
		}
		
		measureoverlay = measureimp.getOverlay();

		if (measureoverlay == null) {

			measureoverlay = new Overlay();
			measureimp.setOverlay(measureoverlay);
		}

		// Re-compute MSER ellipses if neccesary

		if (change == ValueChange.THIRDDIM || change == ValueChange.FOURTHDIM) {
			
			long[] min = { (long) standardRectangle.getMinX(), (long) standardRectangle.getMinY() };
			long[] max = { (long) standardRectangle.getMaxX(), (long) standardRectangle.getMaxY() };
			interval = new FinalInterval(min, max);
			currentimg = util.FindersUtils.extractImage(CurrentView, interval);
			othercurrentimg = util.FindersUtils.extractImage(otherCurrentView, interval);

			newimg = util.FindersUtils.copytoByteImage(currentimg);
			
		//	System.out.println("Current Time point: " + thirdDimension);
			if (imp == null)
				imp = ImageJFunctions.show(CurrentView);
			else {
				final float[] pixels = (float[]) imp.getProcessor().getPixels();
				final Cursor<FloatType> c = Views.iterable(CurrentView).cursor();

				for (int i = 0; i < pixels.length; ++i)
					pixels[i] = c.next().get();

				imp.updateAndDraw();

			}

			imp.setTitle("Current View in third dimension: " + " " + thirdDimension + " " + "fourth dimension: " + " "
					+ fourthDimension);

			if (measureimp == null)
				measureimp = ImageJFunctions.show(otherCurrentView);
			else {
				final float[] pixels = (float[]) measureimp.getProcessor().getPixels();
				final Cursor<FloatType> c = Views.iterable(otherCurrentView).cursor();

				for (int i = 0; i < pixels.length; ++i)
					pixels[i] = c.next().get();

				measureimp.updateAndDraw();

			}

			measureimp.setTitle("Measure image Current View in third dimension: " + " " + thirdDimension + " " + "fourth dimension: " + " "
					+ fourthDimension);
			
			
		}

		
		


		if (change == ValueChange.SHOWNEW) {

			if (imp == null)
				imp = ImageJFunctions.show(CurrentView);
			else {
				final float[] pixels = (float[]) imp.getProcessor().getPixels();
				final Cursor<FloatType> c = Views.iterable(CurrentView).cursor();

				for (int i = 0; i < pixels.length; ++i)
					pixels[i] = c.next().get();

				imp.updateAndDraw();

			}

			imp.setTitle("Current View in third dimension: " + " " + thirdDimension + " " + "fourth dimension: " + " "
					+ fourthDimension);

			if (measureimp == null)
				measureimp = ImageJFunctions.show(otherCurrentView);
			else {
				final float[] pixels = (float[]) measureimp.getProcessor().getPixels();
				final Cursor<FloatType> c = Views.iterable(otherCurrentView).cursor();

				for (int i = 0; i < pixels.length; ++i)
					pixels[i] = c.next().get();

				measureimp.updateAndDraw();

			}

			measureimp.setTitle("Measure image Current View in third dimension: " + " " + thirdDimension + " " + "fourth dimension: " + " "
					+ fourthDimension);
			
			
		

			
			

				if (measureimp != null) {

					for (int i = 0; i < measureoverlay.size(); ++i) {
						if (measureoverlay.get(i).getStrokeColor() == colorDraw ) {
							measureoverlay.remove(i);
							--i;
						}

					}
				}
				if(finalRois!=null){
				
				for (int index = 0; index < finalRois.size(); ++index) {

					final Roi or = util.Boundingboxes.CreateBigRoi((PolygonRoi) finalRois.get(index).rois,
							othercurrentimg, RadiusMeasure);

					or.setStrokeColor(colorDraw);
					measureoverlay.add(or);
				

				}

				}
				else
				
					for (int index = 0; index < Rois.size(); ++index){
						
						final Roi or = util.Boundingboxes.CreateBigRoi(Rois.get(index),
								othercurrentimg, RadiusMeasure);
						or.setStrokeColor(colorDraw);
						measureoverlay.add(or);
						
					}

		}

		if (change == ValueChange.THIRDDIMTrack || change == ValueChange.FOURTHDIMTrack) {

			if (imp == null)
				imp = ImageJFunctions.show(CurrentView);
			else {
				final float[] pixels = (float[]) imp.getProcessor().getPixels();
				final Cursor<FloatType> c = Views.iterable(CurrentView).cursor();

				for (int i = 0; i < pixels.length; ++i)
					pixels[i] = c.next().get();

				imp.updateAndDraw();

			}

			imp.setTitle("Current View in third dimension: " + " " + thirdDimension + " " + "fourth dimension: " + " "
					+ fourthDimension);

			if (measureimp == null)
				measureimp = ImageJFunctions.show(otherCurrentView);
			else {
				final float[] pixels = (float[]) measureimp.getProcessor().getPixels();
				final Cursor<FloatType> c = Views.iterable(otherCurrentView).cursor();

				for (int i = 0; i < pixels.length; ++i)
					pixels[i] = c.next().get();

				measureimp.updateAndDraw();

			}

			measureimp.setTitle("Measure image Current View in third dimension: " + " " + thirdDimension + " " + "fourth dimension: " + " "
					+ fourthDimension);
			// imp = ImageJFunctions.wrapFloat(CurrentView, "current");
			
			

				for (int i = 0; i < overlay.size(); ++i) {
					if (overlay.get(i).getStrokeColor() == colorDraw  ) {
						overlay.remove(i);
						--i;
					}

				}
			
		

				for (int i = 0; i < measureoverlay.size(); ++i) {
					if (measureoverlay.get(i).getStrokeColor() == colorDraw  ) {
						measureoverlay.remove(i);
						--i;
					}

				}
			
			
			long[] min = { (long) standardRectangle.getMinX(), (long) standardRectangle.getMinY() };
			long[] max = { (long) standardRectangle.getMaxX(), (long) standardRectangle.getMaxY() };
			interval = new FinalInterval(min, max);

			currentimg = util.FindersUtils.extractImage(CurrentView, interval);
			othercurrentimg = util.FindersUtils.extractImage(otherCurrentView, interval);

			newimg = util.FindersUtils.copytoByteImage(currentimg);

			if (showMSER) {

				IJ.log(" Computing the Component tree");

				newtree = MserTree.buildMserTree(newimg, delta, minSize, maxSize, maxVar, minDiversity, darktobright);
				Rois = util.FindersUtils.getcurrentRois(newtree);
				ArrayList<double[]> centerRoi = util.FindersUtils.getRoiMean(newtree);
				for (int index = 0; index < centerRoi.size(); ++index) {

					double[] center = new double[] { centerRoi.get(index)[0], centerRoi.get(index)[1] };

					Roi or = Rois.get(index);

					or.setStrokeColor(colorDraw);
					overlay.add(or);
				}

		
			}

			if (showDOG) {

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

				Rois = util.FindersUtils.getcurrentRois(peaks, sigma, sigma2);
				
				
				for (int index = 0; index < peaks.size(); ++index) {

					double[] center = new double[] { peaks.get(index).getDoublePosition(0),
							peaks.get(index).getDoublePosition(1) };

					Roi or = Rois.get(index);

					or.setStrokeColor(colorDraw);
					overlay.add(or);
				}

			}

			if (showSegMSER) {
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
				if (!roiChanged && change == ValueChange.ROI) {
					isComputing = false;
					return;
				}

				IJ.log(" Computing the Component tree");

				for (int label = 1; label < Maxlabel - 1; label++) {

					RandomAccessibleInterval<FloatType> currentsegimg = getCurrentSegment(label);
					RandomAccessibleInterval<UnsignedByteType> currentnewimg = util.FindersUtils.copytoByteImage(currentsegimg);
					MserTree<UnsignedByteType> localtree = MserTree.buildMserTree(currentnewimg, delta, minSize,
							maxSize, maxVar, minDiversity, darktobright);
					ArrayList<Roi> currentroi = util.FindersUtils.getcurrentRois(localtree);
					Rois.addAll(currentroi);

				}

				ArrayList<double[]> centerRoi = util.FindersUtils.getRoiMean(newtree);
				
				
				
				for (int index = 0; index < centerRoi.size(); ++index) {

					double[] center = new double[] { centerRoi.get(index)[0], centerRoi.get(index)[1] };

					Roi or = Rois.get(index);

					or.setStrokeColor(colorDraw);
					overlay.add(or);
				}

			}

			if (showSegDOG) {
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
				if (!roiChanged && change == ValueChange.ROI) {
					isComputing = false;
					return;
				}

				final DogDetection.ExtremaType type;

				if (lookForMaxima)
					type = DogDetection.ExtremaType.MINIMA;
				else
					type = DogDetection.ExtremaType.MAXIMA;

				for (int label = 1; label < Maxlabel - 1; label++) {

					RandomAccessibleInterval<FloatType> currentsegimg = getCurrentSegment(label);

					final DogDetection<FloatType> newdog = new DogDetection<FloatType>(
							Views.extendBorder(currentsegimg), interval, new double[] { 1, 1 }, sigma, sigma2, type,
							threshold, true);

					ArrayList<RefinedPeak<Point>> localpeaks = newdog.getSubpixelPeaks();
					ArrayList<Roi> currentroi = util.FindersUtils.getcurrentRois(localpeaks, sigma, sigma2);
					Rois.addAll(currentroi);
					peaks.addAll(localpeaks);

				}
				
				

				for (int index = 0; index < Rois.size(); ++index) {
					double[] center = new double[] { peaks.get(index).getDoublePosition(0),
							peaks.get(index).getDoublePosition(1) };

					Roi or = Rois.get(index);

					or.setStrokeColor(colorDraw);
					overlay.add(or);
				}

			}

		}

	

		if (change == ValueChange.SHOWSEGMSER) {
			long[] min = { (long) standardRectangle.getMinX(), (long) standardRectangle.getMinY() };
			long[] max = { (long) standardRectangle.getMaxX(), (long) standardRectangle.getMaxY() };
			interval = new FinalInterval(min, max);

			currentimg = util.FindersUtils.extractImage(CurrentView, interval);
			othercurrentimg = util.FindersUtils.extractImage(otherCurrentView, interval);

			newimg = util.FindersUtils.copytoByteImage(currentimg);

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
			if (Rois != null)
				Rois.clear();

			if (!roiChanged && change == ValueChange.ROI) {
				isComputing = false;
				return;
			}

			IJ.log(" Computing the Component tree");

			for (int label = 1; label < Maxlabel - 1; label++) {

				RandomAccessibleInterval<FloatType> currentsegimg = getCurrentSegment(label);
				RandomAccessibleInterval<UnsignedByteType> currentnewimg = util.FindersUtils.copytoByteImage(currentsegimg);
				MserTree<UnsignedByteType> localtree = MserTree.buildMserTree(currentnewimg, delta, minSize, maxSize,
						maxVar, minDiversity, darktobright);
				ArrayList<Roi> currentroi = util.FindersUtils.getcurrentRois(localtree);
				Rois.addAll(currentroi);

			}

			IJ.log("MSER parameters:" + " " + " thirdDimension: " + " " + thirdDimension + " " + "fourthDimension: "
					+ " " + fourthDimension);
			IJ.log("Delta " + " " + delta + " " + "minSize " + " " + minSize + " " + "maxSize " + " " + maxSize + " "
					+ " maxVar " + " " + maxVar + " " + "minDIversity " + " " + minDiversity);
			
			if (imp != null) {

				for (int i = 0; i < overlay.size(); ++i) {
					if (overlay.get(i).getStrokeColor() == colorDraw) {
						overlay.remove(i);
						--i;
					}

				}
			}
			if (measureimp != null) {

				for (int i = 0; i < measureoverlay.size(); ++i) {
					if (measureoverlay.get(i).getStrokeColor() == colorDraw ) {
						measureoverlay.remove(i);
						--i;
					}

				}
			}
			
			for (int index = 0; index < Rois.size(); ++index) {

				Roi or = Rois.get(index);

				or.setStrokeColor(colorDraw);
				overlay.add(or);
			}

		}
		if (change == ValueChange.SHOWSEGDOG) {
			long[] min = { (long) standardRectangle.getMinX(), (long) standardRectangle.getMinY() };
			long[] max = { (long) standardRectangle.getMaxX(), (long) standardRectangle.getMaxY() };
			interval = new FinalInterval(min, max);

			currentimg = util.FindersUtils.extractImage(CurrentView, interval);
			othercurrentimg = util.FindersUtils.extractImage(otherCurrentView, interval);

			newimg = util.FindersUtils.copytoByteImage(currentimg);

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
			if (Rois != null)
				Rois.clear();
			if (!roiChanged && change == ValueChange.ROI) {
				isComputing = false;
				return;
			}


			final DogDetection.ExtremaType type;

			if (lookForMaxima)
				type = DogDetection.ExtremaType.MINIMA;
			else
				type = DogDetection.ExtremaType.MAXIMA;

			for (int label = 1; label < Maxlabel - 1; label++) {

				RandomAccessibleInterval<FloatType> currentsegimg = getCurrentSegment(label);

				final DogDetection<FloatType> newdog = new DogDetection<FloatType>(Views.extendBorder(currentsegimg),
						interval, new double[] { 1, 1 }, sigma, sigma2, type, threshold, true);

				ArrayList<RefinedPeak<Point>> localpeaks = newdog.getSubpixelPeaks();
				ArrayList<Roi> currentroi = util.FindersUtils.getcurrentRois(localpeaks, sigma, sigma2);
				Rois.addAll(currentroi);

			}

			IJ.log("DoG parameters:" + " " + " thirdDimension: " + " " + thirdDimension + " " + "fourthDimension: "
					+ " " + fourthDimension);
			IJ.log("Sigma " + " " + sigma + " " + "Sigma2 " + " " + sigma2 + " " + "Threshold " + " " + threshold);

			if (imp != null) {

				for (int i = 0; i < overlay.size(); ++i) {
					if (overlay.get(i).getStrokeColor() == colorDraw ) {
						overlay.remove(i);
						--i;
					}

				}
			}
			if (measureimp != null) {

				for (int i = 0; i < measureoverlay.size(); ++i) {
					if (measureoverlay.get(i).getStrokeColor() == colorDraw ) {
						measureoverlay.remove(i);
						--i;
					}

				}
			}
			
			for (int index = 0; index < Rois.size(); ++index) {

				Roi or = Rois.get(index);

				or.setStrokeColor(colorDraw);
				overlay.add(or);
			}

		}
		if (change == ValueChange.SHOWMSER) {
			long[] min = { (long) standardRectangle.getMinX(), (long) standardRectangle.getMinY() };
		long[] max = { (long) standardRectangle.getMaxX(), (long) standardRectangle.getMaxY() };
		interval = new FinalInterval(min, max);

		currentimg = util.FindersUtils.extractImage(CurrentView, interval);
		othercurrentimg = util.FindersUtils.extractImage(otherCurrentView, interval);

		newimg = util.FindersUtils.copytoByteImage(currentimg);

			// check if Roi changed

			// if we got some mouse click but the ROI did not change we can
			// return
			if (!roiChanged && change == ValueChange.ROI) {
				isComputing = false;
				return;
			}

			IJ.log(" Computing the Component tree");

			newtree = MserTree.buildMserTree(newimg, delta, minSize, maxSize, maxVar, minDiversity, darktobright);
			Rois = util.FindersUtils.getcurrentRois(newtree);

			IJ.log("MSER parameters:" + " " + " thirdDimension: " + " " + thirdDimension + " " + "fourthDimension: "
					+ " " + fourthDimension);
			IJ.log("Delta " + " " + delta + " " + "minSize " + " " + minSize + " " + "maxSize " + " " + maxSize + " "
					+ " maxVar " + " " + maxVar + " " + "minDIversity " + " " + minDiversity);
			
			
			
			if (imp != null) {

				for (int i = 0; i < overlay.size(); ++i) {
					if (overlay.get(i).getStrokeColor() == colorDraw ) {
						overlay.remove(i);
						--i;
					}

				}
			}
			if (measureimp != null) {

				for (int i = 0; i < measureoverlay.size(); ++i) {
					if (measureoverlay.get(i).getStrokeColor() == colorDraw ) {
						measureoverlay.remove(i);
						--i;
					}

				}
			}
			for (int index = 0; index < Rois.size(); ++index) {

				Roi or = Rois.get(index);

				or.setStrokeColor(colorDraw);
				overlay.add(or);
			}
		}

		if (change == ValueChange.Segmentation) {
			long[] min = { (long) standardRectangle.getMinX(), (long) standardRectangle.getMinY() };
			long[] max = { (long) standardRectangle.getMaxX(), (long) standardRectangle.getMaxY() };
			interval = new FinalInterval(min, max);

			currentimg = util.FindersUtils.extractImage(CurrentView, interval);
			othercurrentimg = util.FindersUtils.extractImage(otherCurrentView, interval);

			newimg = util.FindersUtils.copytoByteImage(currentimg);

			IJ.log("Doing watershedding on the distance transformed image ");

			RandomAccessibleInterval<BitType> bitimg = new ArrayImgFactory<BitType>().create(newimg, new BitType());
			
			GetLocalmaxmin.ThresholdingBit(newimg, bitimg,  thresholdHough);

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
			long[] min = { (long) standardRectangle.getMinX(), (long) standardRectangle.getMinY() };
			long[] max = { (long) standardRectangle.getMaxX(), (long) standardRectangle.getMaxY() };
			interval = new FinalInterval(min, max);

			currentimg = util.FindersUtils.extractImage(CurrentView, interval);
			othercurrentimg = util.FindersUtils.extractImage(otherCurrentView, interval);

			newimg = util.FindersUtils.copytoByteImage(currentimg);

			
			// check if Roi changed

			// if we got some mouse click but the ROI did not change we can
			// return
			if (!roiChanged && change == ValueChange.ROI) {
				isComputing = false;
				return;
			}

			final DogDetection.ExtremaType type;

			if (lookForMaxima)
				type = DogDetection.ExtremaType.MINIMA;
			else
				type = DogDetection.ExtremaType.MAXIMA;

			final DogDetection<FloatType> newdog = new DogDetection<FloatType>(Views.extendBorder(currentimg), interval,
					new double[] { 1, 1 }, sigma, sigma2, type, threshold, true);

			IJ.log("DoG parameters:" + " " + " thirdDimension: " + " " + thirdDimension + " " + "fourthDimension: "
					+ " " + fourthDimension);
			IJ.log("Sigma " + " " + sigma + " " + "Sigma2 " + " " + sigma2 + " " + "Threshold " + " " + threshold);
			peaks = newdog.getSubpixelPeaks();

			Rois = util.FindersUtils.getcurrentRois(peaks, sigma, sigma2);
			if (imp != null) {

				for (int i = 0; i < overlay.size(); ++i) {
					if (overlay.get(i).getStrokeColor() == colorDraw ) {
						overlay.remove(i);
						--i;
					}

				}
			}
			if (measureimp != null) {

				for (int i = 0; i < measureoverlay.size(); ++i) {
					if (measureoverlay.get(i).getStrokeColor() == colorDraw ) {
						measureoverlay.remove(i);
						--i;
					}

				}
			}
			
			for (int index = 0; index < Rois.size(); ++index) {

				Roi or = Rois.get(index);

				or.setStrokeColor(colorDraw);
				overlay.add(or);
			}

		}

		// if we got some mouse click but the ROI did not change we can return
		if (!roiChanged && change == ValueChange.ROI) {
			isComputing = false;
			return;
		}
		if (imp != null)
			imp.updateAndDraw();
		roiListener = new RoiListener();
		imp.getCanvas().addMouseListener(roiListener);
		if (measureimp != null)
			measureimp.updateAndDraw();
		measureroiListener = new RoiListener();
		measureimp.getCanvas().addMouseListener(measureroiListener);
		
		isComputing = false;
		
	}

	public void goAll() {

		jpb.setIndeterminate(false);

		jpb.setMaximum(max);
		panel.add(label);
		panel.add(jpb);
		frame.add(panel);
		frame.pack();
		frame.setSize(200, 100);
		frame.setLocationRelativeTo(panelCont);
		frame.setVisible(true);

		ProgressAll dosnake = new ProgressAll(this);
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

	public void goSnake() {

		panel.removeAll();

		jpb.setIndeterminate(false);
		jpb.setMaximum(max);

		panel.add(jpb);
		frame.add(panel);
		frame.pack();
		frame.setSize(200, 100);
		frame.setLocationRelativeTo(panelCont);
		frame.setVisible(true);

		ProgressSnake dosnake = new ProgressSnake(this);
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

	public void goRedo() {

		jpb.setIndeterminate(false);

		jpb.setMaximum(max);
		panel.add(label);
		panel.add(jpb);
		frame.add(panel);
		frame.pack();
		frame.setSize(200, 100);
		frame.setLocationRelativeTo(panelCont);
		frame.setVisible(true);

		ProgressRedo dosnake = new ProgressRedo(this);
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

	public void go() {

		jpb.setIndeterminate(false);

		jpb.setMaximum(max);
		panel.add(label);
		panel.add(jpb);
		frame.add(panel);
		frame.pack();
		frame.setSize(200, 100);
		frame.setLocationRelativeTo(panelCont);
		frame.setVisible(true);

		Progress dosnake = new Progress(this);
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

	public void displaystack() {

		ImagePlus Localimp = ImageJFunctions.show(originalimgA);
		ImagePlus Measureimp = ImageJFunctions.show(originalimgB);

		for (int i = thirdDimensionsliderInit; i < thirdDimensionSize; ++i) {

			snakestack.addSlice(Localimp.getImageStack().getProcessor(i).convertToRGB());
			measuresnakestack.addSlice(Measureimp.getImageStack().getProcessor(i).convertToRGB());
			cp = (ColorProcessor) (snakestack.getProcessor(i).duplicate());

			ArrayList<Roi> Rois = AllSnakerois.get(i);

			for (int index = 0; index < Rois.size(); ++index) {

				Roi or = Rois.get(index);

				or.setStrokeColor(Color.red);

				cp.setColor(Color.red);
				cp.setLineWidth(1);
				cp.draw(or);

			}

			ArrayList<Roi> MeasureRois = AllSnakeMeasurerois.get(i);

			for (int index = 0; index < MeasureRois.size(); ++index) {

				Roi or = MeasureRois.get(index);

				or.setStrokeColor(Color.red);

				measurecp.setColor(Color.red);
				measurecp.setLineWidth(1);
				measurecp.draw(or);

			}

			if (snakestack != null)

				snakestack.setPixels(cp.getPixels(), 1);

			if (measuresnakestack != null)

				measuresnakestack.setPixels(measurecp.getPixels(), 1);

			Localimp.hide();
			Measureimp.hide();
		}

		new ImagePlus("Snakes", snakestack).show();
		new ImagePlus("Snakes", measuresnakestack).show();

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

						final Roi Bigroi = util.Boundingboxes.CreateBigRoi(roi, othercurrentimg, RadiusMeasure);

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
	public static double computeK( final float stepsPerOctave ) { return Math.pow( 2f, 1f / stepsPerOctave ); }
	public static double computeK( final int stepsPerOctave ) { return Math.pow( 2f, 1f / stepsPerOctave ); }
	public static float computeKWeight( final float k ) { return 1.0f / (k - 1.0f); }
	public static float[] computeSigma( final float k, final float initialSigma )
	{
		final float[] sigma = new float[ 2 ];

		sigma[ 0 ] = initialSigma;
		sigma[ 1 ] = sigma[ 0 ] * k;

		return sigma;
	}
	public float computeSigma2(final float sigma1, final int sensitivity) {
		final float k = (float) computeK(sensitivity);
		final float[] sigma = computeSigma(k, sigma1);

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

	protected class ChooseWorkspaceListener implements ActionListener {

		final TextField filename;

		public ChooseWorkspaceListener(TextField filename) {

			this.filename = filename;

		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {

			JFileChooser chooserA = new JFileChooser();
			chooserA.setCurrentDirectory(userfile);
			chooserA.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooserA.showOpenDialog(panelFirst);
			usefolder = chooserA.getSelectedFile().getAbsolutePath();

			addTrackToName = filename.getText();

		}

	}

	// Making the card
	public JFrame Cardframe = new JFrame("KCell Automated Tracker");
	public JPanel panelCont = new JPanel();
	public JPanel panelFirst = new JPanel();
	public JPanel panelSecond = new JPanel();
	public JPanel panelThird = new JPanel();
	public JPanel panelFourth = new JPanel();
	public JPanel panelFifth = new JPanel();


	public void Card() {

		CardLayout cl = new CardLayout();

		cl.preferredLayoutSize(Cardframe);
		panelCont.setLayout(cl);

		panelCont.add(panelFirst, "1");
		panelCont.add(panelSecond, "2");
		panelCont.add(panelThird, "3");
		panelCont.add(panelFourth, "4");
		panelCont.add(panelFifth, "5");
		CheckboxGroup Finders = new CheckboxGroup();

		final Checkbox mser = new Checkbox("MSER + Snakes", Finders, showMSER);
		final Checkbox dog = new Checkbox("DoG + Snakes", Finders, showDOG);
		final Checkbox Segmser = new Checkbox("Segmentation + MSER + Snakes", Finders, showSegMSER);
		final Checkbox Segdog = new Checkbox("Segmentation + DoG + Snakes", Finders, showSegDOG);
		final Button JumpFrame = new Button("Jump in third dimension to :");
		final Button JumpSlice = new Button("Jump in fourth dimension to :");
		final Label timeText = new Label("Third Dimensíonal slice = " + this.thirdDimensionslider, Label.CENTER);
		final Label sliceText = new Label("Fourth Dimensional slice = " + this.fourthDimensionslider, Label.CENTER);

		final JButton ChooseWorkspace = new JButton("Choose Directory");
		final JLabel outputfilename = new JLabel("Enter output filename: ");
		TextField inputField = new TextField();
		inputField.setText(userfile.getName().replaceFirst("[.][^.]+$", ""));

		inputField.setColumns(10);

		final Scrollbar thirdDimensionsliderS = new Scrollbar(Scrollbar.HORIZONTAL, thirdDimensionsliderInit, 0, 0,
				thirdDimensionSize);
		thirdDimensionsliderS.setBlockIncrement(1);
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
			panelFirst.add(thirdDimensionsliderS, c);

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

		++c.gridy;
		c.insets = new Insets(10, 10, 10, 0);
		panelFirst.add(outputfilename, c);

		++c.gridy;
		c.insets = new Insets(10, 10, 10, 0);
		panelFirst.add(inputField, c);

		++c.gridy;
		c.insets = new Insets(10, 10, 0, 0);
		panelFirst.add(ChooseWorkspace, c);

		panelFirst.setVisible(true);

		cl.show(panelCont, "1");

		mser.addItemListener(new MserListener(this));
		dog.addItemListener(new DogListener(this));

		Segmser.addItemListener(new SegMserListener(this));
		Segdog.addItemListener(new SegDogListener(this));
		ChooseWorkspace.addActionListener(new ChooseWorkspaceListener(inputField));
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

		final Scrollbar RadiusMeasurebar = new Scrollbar(Scrollbar.HORIZONTAL, RadiusMeasureinit, 10, 0, 10 + scrollbarSize);
		final Label Adjustradi = new Label("Adjust Radius around Rois for measurment");
		RadiusMeasure = computeValueFromScrollbarPosition(RadiusMeasureinit, RadiusMeasureMin, RadiusMeasureMax, scrollbarSize);
		
		Adjustradi.setForeground(new Color(255, 255, 255));
		Adjustradi.setBackground(new Color(1, 0, 1));

		final Label sizeTextX = new Label("Increase Radius for Intensity Measurement = " + RadiusMeasure, Label.CENTER);
		// Tracker choice panel

		final Checkbox KalmanTracker = new Checkbox("Use Kalman Filter for tracking");
		final Checkbox NearestNeighborTracker = new Checkbox("Use Nearest Neighbour tracker");
		final Label Kal = new Label("Use Kalman Filter for probabilistic tracking");
		final Label Det = new Label("Use Nearest Neighbour tracking");
		final Checkbox txtfile = new Checkbox("Save tracks as TXT file", SaveTxt);
		final Checkbox xlsfile = new Checkbox("Save tracks as XLS file", SaveXLS);
		final Checkbox sameradius = new Checkbox("Use same radius for measurment", false);
		Kal.setForeground(new Color(255, 255, 255));
		Kal.setBackground(new Color(1, 0, 1));
		Det.setBackground(new Color(1, 0, 1));
		Det.setForeground(new Color(255, 255, 255));
		Progressmax = Rois.size();

		panelThird.setLayout(layout);
		final Label Namesnake = new Label("Step 3", Label.CENTER);

		panelThird.add(Namesnake, c);

	
	

		++c.gridy;
		c.insets = new Insets(10, 10, 0, 0);
		panelThird.add(Adjustradi, c);
		++c.gridy;
		c.insets = new Insets(10, 10, 0, 0);
		panelThird.add(RadiusMeasurebar, c);

		++c.gridy;
		c.insets = new Insets(10, 10, 0, 0);
		panelThird.add(sameradius, c);
		
		++c.gridy;
		c.insets = new Insets(10, 10, 0, 0);
		panelThird.add(sizeTextX, c);
	
		if (thirdDimensionSize > 1) {
			
			++c.gridy;
			c.insets = new Insets(10, 160, 10, 160);
			panelThird.add(snakes, c);
		}
		if (fourthDimensionSize > 1) {
			++c.gridy;
			c.insets = new Insets(0, 145, 0, 145);
			panelThird.add(AutomatedSnake, c);
		}

		++c.gridy;
		c.insets = new Insets(10, 160, 10, 160);
		panelThird.add(RedoViewsnakes, c);
		++c.gridy;
		++c.gridy;
		c.insets = new Insets(10, 10, 0, 50);
		panelThird.add(Kal, c);

		++c.gridy;
		c.insets = new Insets(10, 10, 0, 50);
		panelThird.add(KalmanTracker, c);

		++c.gridy;
		c.insets = new Insets(10, 10, 0, 50);
		panelThird.add(Det, c);

		++c.gridy;
		c.insets = new Insets(10, 10, 0, 50);
		panelThird.add(NearestNeighborTracker, c);

		++c.gridy;
		c.insets = new Insets(10, 10, 0, 0);
		panelThird.add(txtfile, c);

		/* Configuration */
		RadiusMeasurebar.addAdjustmentListener(new RadiusMeasureListener(sizeTextX, RadiusMeasureMin, RadiusMeasureMax));
		

		sameradius.addItemListener(new SameMeasureListener());
		
	

		

	
		
		KalmanTracker.addItemListener(new KalmanchoiceListener());
		NearestNeighborTracker.addItemListener(new NNListener());
		txtfile.addItemListener(new SaveasTXT());
		xlsfile.addItemListener(new SaveasXLS());
		thirdDimensionsliderS
				.addAdjustmentListener(new thirdDimensionsliderListener(timeText, timeMin, thirdDimensionSize));
		fourthDimensionslider
				.addAdjustmentListener(new fourthDimensionsliderListener(sliceText, sliceMin, fourthDimensionSize));

		JumpFrame.addActionListener(
				new MoveInThirdDimListener(this, thirdDimensionsliderS, timeText, timeMin, thirdDimensionSize));
		JumpSlice.addActionListener(
				new MoveInFourthDimListener(this, fourthDimensionslider, sliceText, sliceMin, fourthDimensionSize));

		Singlesnake.addActionListener(new SinglesnakeButtonListener());
		Redosnake.addActionListener(new SinglesnakeButtonListener());
		snakes.addActionListener(new snakeButtonListener());
		RedoViewsnakes.addActionListener(new RedosnakeButtonListener());
		AutomatedSnake.addActionListener(new moveAllListener());
		Ends.setForeground(new Color(255, 255, 255));
		Ends.setBackground(new Color(1, 0, 1));

		Cardframe.add(panelCont, BorderLayout.CENTER);
		Cardframe.add(control, BorderLayout.SOUTH);
		Cardframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		Cardframe.setResizable(true);
		Cardframe.setVisible(true);
		Cardframe.pack();

	}

	public class TrackDisplayListener implements ActionListener {

		final JTable cb;
		final RandomAccessibleInterval<FloatType> seedimg;

		public TrackDisplayListener(JTable cb, RandomAccessibleInterval<FloatType> seedimg) {

			this.cb = cb;
			this.seedimg = seedimg;

		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {

			displaySelectedTrack = cb.getSelectedRow();

			ImagePlus displayimp, displaymeasureimp;

			displayimp = ImageJFunctions.show(originalimgA);
			displaymeasureimp = ImageJFunctions.show(originalimgB);
			displaymeasureimp.setTitle("Display Tracks on Measurement image");
			displayimp.setTitle("Display Tracks");

			Overlay o = displayimp.getOverlay();

			if (displayimp.getOverlay() == null) {
				o = new Overlay();
				displayimp.setOverlay(o);
			}

			o.clear();

			Overlay osec = displayimp.getOverlay();

			if (displayimp.getOverlay() == null) {
				osec = new Overlay();
				displaymeasureimp.setOverlay(osec);
			}

			osec.clear();

			TrackModel model = new TrackModel(graph);
			model.getDirectedNeighborIndex();

			if (displaySelectedTrack == 0) {

				
				DisplaymodelGraph totaldisplaytracks = new DisplaymodelGraph(displayimp, graph, colorDraw, true, 0);
				totaldisplaytracks.getImp();

				DisplaymodelGraph totaldisplaytracksmeasure = new DisplaymodelGraph(displaymeasureimp, graph, colorDraw,
						true, 0);
				totaldisplaytracksmeasure.getImp();
               
				
			}

			else {

				for (int index = 0; index < IDALL.size(); ++index) {
					if (displaySelectedTrack == index + 1) {

						DisplaymodelGraph totaldisplaytracks = new DisplaymodelGraph(displayimp, graph, colorDraw,
								false, displaySelectedTrack);
						totaldisplaytracks.getImp();

						DisplaymodelGraph totaldisplaymeasuretracks = new DisplaymodelGraph(displaymeasureimp, graph,
								colorDraw, false, displaySelectedTrack);
						totaldisplaymeasuretracks.getImp();

					}

				}

			}

		}

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

	protected class SameMeasureListener implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent e) {
			
			
				showNew = true;
				updatePreview(ValueChange.SHOWNEW);
			
			
		}

		
		
		
		
	}
	
	protected class RadiusMeasureListener implements AdjustmentListener {
		final Label label;
		final float min, max;

		public RadiusMeasureListener(final Label label, final float min, final float max) {
			this.label = label;
			this.min = min;
			this.max = max;
		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			RadiusMeasure = computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);
			label.setText("RadiusMeasure = " + RadiusMeasure);

			if (!isComputing) {
				showNew = true;
				updatePreview(ValueChange.SHOWNEW);
			} else if (!event.getValueIsAdjusting()) {
				while (isComputing) {
					SimpleMultiThreading.threadWait(10);
				}
				showNew = true;
				updatePreview(ValueChange.SHOWNEW);
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
				panelFourth.removeAll();
				panelFourth.repaint();
				final GridBagLayout layout = new GridBagLayout();
				final GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 0;
				c.gridy = 0;
				c.weightx = 1;
				panelFourth.setLayout(layout);
				final Label Name = new Label("Step 4", Label.CENTER);
				panelFourth.add(Name, c);
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
				panelFourth.add(MaxMovText, c);
				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelFourth.add(Maxrad, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelFourth.add(track, c);

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
				panelFourth.removeAll();

				final GridBagLayout layout = new GridBagLayout();
				final GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 0;
				c.gridy = 0;
				c.weightx = 1;
				panelFourth.setLayout(layout);
				final Label Name = new Label("Step 4", Label.CENTER);
				panelFourth.add(Name, c);
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
				panelFourth.add(SearchText, c);
				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelFourth.add(rad, c);

				final Scrollbar Maxrad = new Scrollbar(Scrollbar.HORIZONTAL, maxSearchradiusInit, 10, 0,
						10 + scrollbarSize);
				maxSearchradius = computeValueFromScrollbarPosition(maxSearchradiusInit, maxSearchradiusMin,
						maxSearchradiusMax, scrollbarSize);
				final Label MaxMovText = new Label("Max Movment of Objects per frame: " + maxSearchradius,
						Label.CENTER);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelFourth.add(MaxMovText, c);
				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelFourth.add(Maxrad, c);

				final Scrollbar Miss = new Scrollbar(Scrollbar.HORIZONTAL, missedframesInit, 10, 0, 10 + scrollbarSize);
				Miss.setBlockIncrement(1);
				missedframes = (int) computeValueFromScrollbarPosition(missedframesInit, missedframesMin,
						missedframesMax, scrollbarSize);
				final Label LostText = new Label("Objects allowed to be lost for #frames" + missedframes, Label.CENTER);
				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelFourth.add(LostText, c);
				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelFourth.add(Miss, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelFourth.add(CostalphaText, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelFourth.add(alphabar, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelFourth.add(CostbetaText, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelFourth.add(betabar, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 50);
				panelFourth.add(track, c);

				rad.addAdjustmentListener(
						new SearchradiusListener(SearchText, initialSearchradiusMin, initialSearchradiusMax));
				Maxrad.addAdjustmentListener(
						new maxSearchradiusListener(MaxMovText, maxSearchradiusMin, maxSearchradiusMax));
				Miss.addAdjustmentListener(new missedFrameListener(LostText, missedframesMin, missedframesMax));
				alphabar.addAdjustmentListener(new AlphabarListener(CostalphaText, alphaInit, alphaMax));
				betabar.addAdjustmentListener(new BetabarListener(CostbetaText, betaInit, betaMax));

				track.addActionListener(new TrackerButtonListener());

				panelFourth.repaint();
				panelFourth.validate();
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
				sigma2.setBackground(Color.WHITE);
				enableSigma2 = true;
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

	public void reset3D(final int third) {

		thirdDimension = third;
	}

	public void reset4D(final int fourth) {

		fourthDimension = fourth;
	}

	protected class TrackerButtonListener implements ActionListener {

		public void actionPerformed(final ActionEvent arg0) {

			IJ.log("Start Tracking");

			// impcopy = ImageJFunctions.show(originalimg);

			UserchosenCostFunction = new PixelratiowDistCostFunction(alpha, beta);
			ArrayList<ArrayList<SnakeObject>> All3DSnakesA = new ArrayList<ArrayList<SnakeObject>>();

			if (showKalman) {

				blobtracker = new KFsearch(Collections.unmodifiableList(All3DSnakes), UserchosenCostFunction,
						maxSearchradius, initialSearchradius, thirdDimension, thirdDimensionSize, missedframes);

				IJ.log("Kalman Filter parameters : ");
				IJ.log("Distance weightage alpha" + " " + alpha + " " + " Pixel ratio weightage beta" + " " + beta + " "
						+ "  maxSearchradius " + " " + maxSearchradius);
				IJ.log(" initialSearchradius " + "  " + initialSearchradius + " " + " missedframes " + " "
						+ missedframes);
			}
			if (showNN) {
				blobtracker = new NNsearch(All3DSnakes, maxSearchradius, thirdDimension, thirdDimensionSize);
				IJ.log("Kalman Filter parameters : ");
				IJ.log("maxSearchradius " + " " + maxSearchradius);

			}


				blobtracker.process();

			graph = blobtracker.getResult();


				IJ.log("Tracking Complete " + " " + "Displaying results");

			ImagePlus totalimp = ImageJFunctions.show(originalimgA);

			TrackModel model = new TrackModel(graph);

			model.getDirectedNeighborIndex();
			
		//	DisplayGraph tracks = new DisplayGraph(totalimp, graph, colorDraw);
		//	tracks.getClass();
			
			ResultsTable rt = new ResultsTable();
			// Get all the track id's
			for (final Integer id : model.trackIDs(true)) {

				IDALL.add(id);
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

					list.add(currentsnake);

				}
				Collections.sort(list, ThirdDimcomparison);
				if (fourthDimensionSize > 1)
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

				}

				if (SaveXLS)
					saveResultsToExcel(usefolder + "//" + addTrackToName + ".xls", rt);

			}

			rt.show("Results");

			/* Instantiation */
			final GridBagLayout layout = new GridBagLayout();
			final GridBagConstraints c = new GridBagConstraints();

			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 4;
			c.weighty = 1.5;
			final Label Name = new Label("Step 5", Label.CENTER);
			panelFifth.add(Name, c);
			panelFifth.setLayout(layout);

			final Label Done = new Label("Hope that everything was to your satisfaction!");
			final Button Exit = new Button("Close and exit");
			JLabel lbl = new JLabel("Select the TrackID to display");

			String[] choicestrack = new String[IDALL.size() + 1];

			choicestrack[0] = "Display All (disabled)";
			Comparator<Integer> Seedidcomparison = new Comparator<Integer>() {

				@Override
				public int compare(final Integer A, final Integer B) {

					return A - B;

				}

			};

			Collections.sort(IDALL, Seedidcomparison);
			for (int index = 0; index < IDALL.size(); ++index) {

				String currenttrack = Double.toString(IDALL.get(index));

				choicestrack[index + 1] = "Track " + currenttrack;
			}
			
			DefaultTableModel userTableModel = new DefaultTableModel(new Object[]{"Track ID"}, 0) {
			    @Override
			    public boolean isCellEditable(int row, int column) {
			        return false;
			    }
			};
			
		
			userTableModel.addRow(new String[]{"Display All"});
				for (int index = 0; index < IDALL.size(); ++index) {
					String[] currenttrack = {Double.toString(IDALL.get(index))};
					userTableModel.addRow(currenttrack);
					
					
				}
				
				
				  
				JTable table = new JTable(userTableModel);
			   
				 
			

			JComboBox<String> cbtrack = new JComboBox<String>(choicestrack);

			Done.setBackground(new Color(1, 0, 1));
			Done.setForeground(new Color(255, 255, 255));

			panelFifth.removeAll();
			++c.gridy;
			c.insets = new Insets(10, 10, 0, 50);
			panelFifth.add(lbl, c);

			++c.gridy;
			c.insets = new Insets(10, 10, 0, 50);
			panelFifth.add(table, c);

			++c.gridy;
			c.insets = new Insets(10, 10, 0, 50);
			panelFifth.add(Done, c);

			++c.gridy;
			c.insets = new Insets(10, 10, 0, 50);
			panelFifth.add(Exit, c);

			table.addMouseListener(new MouseAdapter() {
				  public void mouseClicked(MouseEvent e) {
				    if (e.getClickCount() == 1) {
				      JTable target = (JTable)e.getSource();
				      int row = target.getSelectedRow();
				      int column = target.getSelectedColumn();
				      // do some action if appropriate column
				      
				      displayclicked(row);
				    }
				  }
				});
			
			Exit.addActionListener(new FinishedButtonListener(Cardframe, true));

			panelFifth.repaint();
			panelFifth.validate();
			Cardframe.pack();

		}

	}
public void displayclicked(final int trackindex){
		
		ImagePlus displayimp, displaymeasureimp;

		displayimp = ImageJFunctions.show(originalimgA);
		displaymeasureimp = ImageJFunctions.show(originalimgB);
		displaymeasureimp.setTitle("Display Tracks on Measurement image");
		displayimp.setTitle("Display Tracks");

		Overlay o = displayimp.getOverlay();

		if (displayimp.getOverlay() == null) {
			o = new Overlay();
			displayimp.setOverlay(o);
		}

		o.clear();

		Overlay osec = displayimp.getOverlay();

		if (displayimp.getOverlay() == null) {
			osec = new Overlay();
			displaymeasureimp.setOverlay(osec);
		}

		osec.clear();

		TrackModel model = new TrackModel(graph);
		model.getDirectedNeighborIndex();

	
		if (trackindex == 0) {

			
			DisplaymodelGraph totaldisplaytracks = new DisplaymodelGraph(displayimp, graph, colorDraw, true, 0);
			totaldisplaytracks.getImp();

			DisplaymodelGraph totaldisplaytracksmeasure = new DisplaymodelGraph(displaymeasureimp, graph, colorDraw,
					true, 0);
			totaldisplaytracksmeasure.getImp();
           
			
		}

		else {
		

			for (int index = 0; index < IDALL.size(); ++index) {
				if (trackindex == index + 1) {

					DisplaymodelGraph totaldisplaytracks = new DisplaymodelGraph(displayimp, graph, colorDraw,
							false, trackindex);
					totaldisplaytracks.getImp();

					DisplaymodelGraph totaldisplaymeasuretracks = new DisplaymodelGraph(displaymeasureimp, graph,
							colorDraw, false, trackindex);
					totaldisplaymeasuretracks.getImp();

				}

			

		}

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

	public float computeValueFromScrollbarPosition(final int scrollbarPosition, final float min, final float max,
			final int scrollbarSize) {
		return min + (scrollbarPosition / (float) scrollbarSize) * (max - min);
	}

	protected static float computeIntValueFromScrollbarPosition(final int scrollbarPosition, final float min,
			final float max, final int scrollbarSize) {
		return min + (scrollbarPosition / (max)) * (max - min);
	}

	public int computeScrollbarPositionFromValue(final float sigma, final float min, final float max,
			final int scrollbarSize) {
		return Util.round(((sigma - min) / (max - min)) * scrollbarSize);
	}

	public int computeIntScrollbarPositionFromValue(final float thirdDimensionslider, final float min, final float max,
			final int scrollbarSize) {
		return Util.round(((thirdDimensionslider - min) / (max - min)) * max);
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

	

	/** The name of the frame feature. */
	public final String SNAKEPROGRESS = "SNAKEPROGRESS";

	public final Double getFeature(final String feature) {
		return features.get(feature);
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
	public final void putFeature(final String feature, final Double value) {
		features.put(feature, value);
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
