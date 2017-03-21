package blobfinder;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import graphconstructs.Logger;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.EllipseRoi;
import ij.gui.GenericDialog;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.io.RoiEncoder;
import ij.plugin.frame.RoiManager;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import mserMethods.GetDelta;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
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
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.util.Util;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import snakes.ABSnakeFast;
import snakes.ComSnake;
import snakes.SnakeConfig;
import snakes.SnakeConfigDriver;
import snakes.SnakeObject;


public class BlobfinderInteractiveSnake implements Blobfinder {

	
	private static final String BASE_ERROR_MSG = "[Blobfinder] ";
	protected Logger logger = Logger.DEFAULT_LOGGER;
	protected String errorMessage;
	private ArrayList<SnakeObject> ProbBlobs;
	private final RandomAccessibleInterval<FloatType> source;
	private final RandomAccessibleInterval<FloatType> target;
	
	private final ArrayList<Roi> rois;
	ArrayList<Roi> finalrois;
	ArrayList<ComSnake> comrois;
	ImageStack pile = null;
	ImageStack Intensitypile = null;

	RandomAccessibleInterval<FloatType> resultsource;

	ArrayList<ABSnakeFast>  snakelist;
    
	RandomAccessibleInterval<FloatType> currentimg;
	ImageStack pile_seg = null;
	final boolean TrackinT;

	int currentthirdDimension = -1;
	// Dimensions of the stck :
	int thirdDimensionsize = 0;
	int length = 0;
	int height = 0;
	// ROI original
	int nbRois;
	Roi rorig = null;
	Roi processRoi = null;
	public Color colorDraw = Color.RED;
	public boolean darktoBright = false;
	
	private final int ndims;
	private final int fourthDimension;
	private final int thirdDimension;
	private final double sizeX;
	private final double sizeY;
	private int Roiindex;
	
	


	/**
	 * Parametres of Snake :
	 */
	SnakeConfigDriver configDriver;
	// number of iterations
	int ite = 200;
	// step to display snake
	int step = ite - 1;
	// threshold of edges
	int Gradthresh = 2;
	// how far to look for edges
	int DistMAX = 100;

	double Displacement_min = 0.1;
	double Displacement_max = 2.0;
	double Threshold_dist_positive = 100;
	double Threshold_dist_negative = 100;
	double Inv_alpha_min = 0.2;
	double Inv_alpha_max = 10.0;
	double Reg_min = 1;
	double Reg_max = 5;
	double Mul_factor = 0.99;

	// maximum displacement
	double force = 10;
	// regulari1ation factors, min and max
	double reg = 5;
	double regmin, regmax;
	// first and last thirdDimension to process
	// misc options
	boolean showgrad = false;
	boolean createsegimage = false;
	boolean advanced = false;
	boolean propagate = true;
	public boolean Auto = false;
	boolean saverois = false;
	public boolean displaysnake = true;
	boolean saveIntensity = true;
	boolean useroinames = false;
	boolean nosi1elessrois = true;
	boolean differentfolder = false;
	
	RoiEncoder saveRoi;
	String usefolder ;
	String addToName;
   
	public BlobfinderInteractiveSnake(final RandomAccessibleInterval<FloatType> source,
			final RandomAccessibleInterval<FloatType> target, ArrayList<Roi> rois,
			final double sizeX, final double sizeY, final String usefolder, final String addToName,
			final int thirdDimension, final int fourthDimension, final boolean TrackinT){
		
		this.source = source;
		this.target = target;
	    this.thirdDimension = thirdDimension;
		this.fourthDimension = fourthDimension;
		this.TrackinT = TrackinT;
		this.rois = rois;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.usefolder = usefolder;
		this.addToName = addToName;
		ndims = source.numDimensions();
	}
	
	
	@Override
	public ArrayList<SnakeObject> getResult() {
		
		return ProbBlobs;
	}

	public ArrayList<ComSnake> getfinalRois() {
		
		return comrois;
	}
	
	public String getFolder(){
		
		return usefolder;
	}
	
   public String getFile(){
		
		return addToName;
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
		
		configDriver = new SnakeConfigDriver();
		AdvancedParameters();

		snakelist = new ArrayList<ABSnakeFast>();
		// original stack

		ProbBlobs = new ArrayList<SnakeObject>();
     
		finalrois = new ArrayList<Roi>();
		
		comrois = new ArrayList<ComSnake>();

		boolean dialog;
		boolean dialogAdvanced;
		if (Auto)
			dialog = false;
		else
			dialog = Dialogue();
		// many rois
		
		nbRois = rois.size();
		IJ.log("processing " + nbRois + "rois");
		System.out.println("processing " + nbRois + "rois");
		ArrayList<Roi> RoisOrig = rois;
		Roi[] RoisCurrent = new Roi[nbRois];
		Roi[] RoisResult = new Roi[nbRois];

		if (advanced)
			dialogAdvanced = AdvancedDialog();

		regmin = reg / 2.0;
		regmax = reg;

		final FloatType type = source.randomAccess().get().createVariable();
		final ImgFactory<FloatType> factory = net.imglib2.util.Util.getArrayOrCellImgFactory(source, type);
		resultsource = factory.create(source, type);

		


		Roi roi;

		currentimg = source;

		// Expand the image by 10 pixels
		
		Interval spaceinterval = Intervals.createMinMax(new long[] {currentimg.min(0),currentimg.min(1), currentimg.max(0), currentimg.max(1)});
		Interval interval = Intervals.expand(spaceinterval, 10);
		currentimg = Views.interval(Views.extendBorder(currentimg), interval);
		
	
		ABSnakeFast snake;
		

		for (int i = 0; i < RoisOrig.size(); i++) {

			
				roi = RoisOrig.get(i);
			
			IJ.log("processing fourthDimension no. " + fourthDimension + " thirdDimension no. " +  thirdDimension  +  " with roi " + i);
			System.out.println("processing fourthDimension no. " + fourthDimension + " thirdDimension no. " +  thirdDimension  +  " with roi " + i);

			snake = processSnake(roi, i + 1);
			snake.killImages();

			//snake.DrawSnake(overlay, source, colorDraw, 1);
			RoisResult[i] = snake.createRoi();
			RoisResult[i].setName("res-" + i);
			RoisCurrent[i] = snake.createRoi();

			snakelist.add(snake);
			
			if (RoisResult[i] != null) {
			
				final double[] props = getProps(RoisResult[i]);
				final double[] center = new double[]{props[0], props[1], props[2]};
				final double Intensitysource = props[3];
				final int Numberofpixels = (int)props[4];
				final double MeanIntensitysource = props[5];
				final double Circularity = props[6];
				final double Intensitytarget = props[7];
				final int NumberofpixelsTarget = (int)props[8];
				final double MeanIntensitytarget = props[9];
				final double Size = props[10];
				finalrois.add(RoisResult[i]);
				
				
				
				ComSnake csnake = new ComSnake(thirdDimension, fourthDimension, center, RoisResult[i]);
				comrois.add(csnake);
				
				SnakeObject currentsnake = new SnakeObject(thirdDimension, fourthDimension, i, RoisResult[i], center, Intensitysource,
						 Numberofpixels, MeanIntensitysource, Intensitytarget, NumberofpixelsTarget, MeanIntensitytarget, Circularity, Size);
				ProbBlobs.add(currentsnake);
				
				
			}
			
			if (saverois){
				
				  try {
                      saveRoi = new RoiEncoder(usefolder + "//" + "SnakeRoi" + (i) + "thirdDim" + thirdDimension + "dourthDim" + fourthDimension + ".roi");
                      saveRoi.write(RoisResult[i]);
                  } catch (IOException ex) {

                  }
				
			}
		
			
			
		}
		
	    
		
		
		
		
		return true;
	}
	
	public ArrayList<ABSnakeFast> getABsnake(){
		
		
		return snakelist;
	}
	
	private boolean Dialogue() {
		// array of colors
		String[] colors = { "Red", "Green", "Blue", "Cyan", "Magenta", "Yellow", "Black", "White" };
		int indexcol = 0;
		// create dialog
		GenericDialog gd = new GenericDialog("Snake");
		gd.addNumericField("Gradient_threshold:", Gradthresh, 0);
		gd.addNumericField("Number_of_iterations:", ite, 0);
		gd.addChoice("Draw_color:", colors, colors[indexcol]);
		gd.addCheckbox("Save_rois:", saverois);
		
		gd.addCheckbox("Display Snake Stack", displaysnake);
		gd.addCheckbox("Advanced_options", advanced);
		 gd.addStringField("Use_folder:", usefolder);
		 gd.addStringField("Choose_filestartname:", addToName);
		 

		// show dialog
		gd.showDialog();

		// threshold of edge
		Gradthresh = (int) gd.getNextNumber();
		// number of iterations
		ite = (int) gd.getNextNumber();
	
		if (step > ite - 1) {
			IJ.showStatus("Warning : show step too big\n\t step assignation 1");
			step = 1;
		}

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

		saverois = gd.getNextBoolean();
		displaysnake = gd.getNextBoolean();
		advanced = gd.getNextBoolean();
		 
		usefolder = gd.getNextString();
		 addToName= gd.getNextString();
		 
		return !gd.wasCanceled();
	}

	/**
	 * Dialog advanced
	 *
	 * @return dialog ok ?
	 */

	private boolean AdvancedDialog() {

		// dialog
		GenericDialog gd = new GenericDialog("Snake Advanced");
		gd.addNumericField("Distance_Search", DistMAX, 0);
		gd.addNumericField("Displacement_min", Displacement_min, 2);
		gd.addNumericField("Displacement_max", Displacement_max, 2);
		gd.addNumericField("Threshold_dist_positive", Threshold_dist_positive, 0);
		gd.addNumericField("Threshold_dist_negative", Threshold_dist_negative, 0);
		gd.addNumericField("Inv_alpha_min", Inv_alpha_min, 2);
		gd.addNumericField("Inv_alpha_max", Inv_alpha_max, 2);
		gd.addNumericField("Reg_min", Reg_min, 2);
		gd.addNumericField("Reg_max", Reg_max, 2);
		gd.addNumericField("Mul_factor", Mul_factor, 4);
		// show dialog
		gd.showDialog();

		DistMAX = (int) gd.getNextNumber();
		Displacement_min = gd.getNextNumber();
		Displacement_max = gd.getNextNumber();
		Threshold_dist_positive = gd.getNextNumber();
		Threshold_dist_negative = gd.getNextNumber();
		Inv_alpha_min = gd.getNextNumber();
		Inv_alpha_max = gd.getNextNumber();
		Reg_min = gd.getNextNumber();
		Reg_max = gd.getNextNumber();
		Mul_factor = gd.getNextNumber();

		return !gd.wasCanceled();

	}

	private void AdvancedParameters() {
		// see advanced dialog class
		configDriver.setMaxDisplacement(Displacement_min, Displacement_max);
		configDriver.setInvAlphaD(Inv_alpha_min, Inv_alpha_max);
		configDriver.setReg(Reg_min, Reg_max);
		configDriver.setStep(Mul_factor);
	}

	public ABSnakeFast processSnake(Roi roi, int numRoi) {

		int i;

		SnakeConfig config;

		processRoi = roi;

		// initialisation of the snake
		ABSnakeFast snake = new ABSnakeFast();
		snake.Init(processRoi);

		snake.setOriginalImage(currentimg);

		// start of computation
		IJ.showStatus("Calculating snake...");

		

		double InvAlphaD = configDriver.getInvAlphaD(false);
		double regMax = configDriver.getReg(false);
		double regMin = configDriver.getReg(true);
		double DisplMax = configDriver.getMaxDisplacement(false);
		double mul = configDriver.getStep();

		config = new SnakeConfig(Gradthresh, DisplMax, DistMAX, regMin, regMax, 1.0 / InvAlphaD);
		snake.setConfig(config);
		// compute image gradient
		snake.computeGrad(currentimg);

		double dist0 = 0.0;
		double dist;

		for (i = 0; i < ite; i++) {
			if (IJ.escapePressed()) {
				break;
			}
			// each iteration
			dist = snake.process();

			if ((dist >= dist0) && (dist < force)) {
				// System.out.println("update " + config.getAlpha());
				snake.computeGrad(currentimg);
				config.update(mul);
			}
			dist0 = dist;

			// display of the snake
			if ((step > 0) && ((i % step) == 0)) {
				IJ.showStatus("Show intermediate result (iteration n" + (i + 1) + ")");

			}
		}

		snake.setOriginalImage(null);

		return snake;
	}

	public double getIntensity(Roi roi) {

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
				targetran.setPosition(secondcurrentcursor);
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
	
	
	
	
	
	

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public void setLogger(Logger logger) {
		this.logger = logger;
		
	}
	
	
	
	
	
	
	
	public double sqDistance(final double[] cordone, final double[] cordtwo) {

		double distance = 0;

		for (int d = 0; d < ndims; ++d) {

			distance += Math.pow((cordone[d] - cordtwo[d]), 2);

		}
		return (distance);
	}
}

