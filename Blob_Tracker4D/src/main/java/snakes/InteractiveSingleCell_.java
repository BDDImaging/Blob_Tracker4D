package snakes;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import blobfinder.SortListbyproperty;
import costMatrix.CostFunction;
import fiji.tool.SliceListener;
import fiji.tool.SliceObserver;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.EllipseRoi;
import ij.gui.GenericDialog;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ColorProcessor;
import mpicbg.imglib.multithreading.SimpleMultiThreading;
import mpicbg.imglib.util.Util;
import net.imglib2.FinalInterval;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.algorithm.dog.DogDetection;
import net.imglib2.algorithm.localextrema.RefinedPeak;

import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import snakes.InteractiveActiveContour_.ImagePlusListener;
import snakes.InteractiveActiveContour_.RoiListener;
import snakes.InteractiveActiveContour_.ValueChange;
import snakes.InteractiveActiveContour_.moveInThirdDimListener;
import snakes.InteractiveActiveContour_.thirdDimensionsliderListener;

public class InteractiveSingleCell_ implements PlugIn{
	
	
	String usefolder = IJ.getDirectory("imagej");
	
	String addTrackToName = "TrackedBlobsID";
	final int scrollbarSize = 1000;
	float sigma = 0.5f;
	float sigma2 = 0.5f;
	
	float deltaMin = 0;
	ColorProcessor cp = null;
	RoiListener bigroiListener;
	boolean displayoverlay;
	ImageStack prestack;
	float threshold = 1f;
	float thresholdMin = 0f;
	float thresholdMax = 1f;
	int thresholdInit = 1;
	ResultsTable rt = new ResultsTable();
	float thresholdHoughMin = 0f;
	float thresholdHoughMax = 1f;
	int thresholdHoughInit = 1;
	
	float Radius = 5f;
	float RadiusMin = 0f;
	float RadiusMax = 100f;
	int RadiusInit = 5;
	
	ArrayList<Roi> AllSelectedrois;
	public float minDiversity = 1;
	// steps per octave
	public static int standardSensitivity = 4;
	int sensitivity = standardSensitivity;
	SimpleWeightedGraph<SnakeObject, DefaultWeightedEdge> graph;
	JFrame frame = new JFrame();
	JPanel panel = new JPanel();
	float imageSigma = 0.5f;
	float sigmaMin = 0.5f;
	float sigmaMax = 100f;
	float sizeXMin = 0;
	float sizeYMin = 0;
	boolean isStarted = false;
	RoiListener roiListener;
	SliceObserver sliceObserver;
	float sizeXMax = 100f;
	float sizeYMax = 100f;
	int sigmaInit = 30;
	boolean findBlobsViaMSER = false;
	boolean findBlobsViaDOG = false;
	boolean isComputing = false;
	boolean lookForMaxima = true;
	boolean lookForMinima = false;
	boolean showMSER = false;
	boolean showDOG = false;
	boolean enableSigma2 = false;
	boolean darktobright = false;
	float delta = 1f;
	public float maxVar = 1;
	float deltaMax = 400f;
	float maxVarMin = 0;
	float maxVarMax = 1;
	public int minDiversityInit = 1;
	int minSizeInit = 1;
	int maxSizeInit = 100;
	ArrayList<double[]> AllmeanCovar;
	Overlay overlay; 
	int deltaInit = 10;
	int maxVarInit = 1;
	int thirdDimension;
	int thirdDimensionSize = 0;
	float minDiversityMin = 0;
	float minDiversityMax = 1;
	int thirdDimensionslider = 1;
	int thirdDimensionsliderInit = 1;
	ArrayList<int[]> ClickedPoints = new ArrayList<int[]>();
	int timeMin = 1;
	long minSize = 1;
	long maxSize = 1000;
	long minSizemin = 0;
	long minSizemax = 100;
	long maxSizemin = 100;
	long maxSizemax = 10000;
	float initialSearchradius = 10;
	float maxSearchradius = 15;
	public int maxSearchradiusInit = (int) maxSearchradius;
	public float maxSearchradiusMin = 10;
	public float maxSearchradiusMax = 500;
	CostFunction<SnakeObject, SnakeObject> UserchosenCostFunction;
	ArrayList<RefinedPeak<Point>> peaks;
	RandomAccessibleInterval<FloatType> currentimg;
	FinalInterval interval;
	Color colorSelect = Color.red;
	Color colorCreate = Color.red;
	Color colorDraw = Color.green;
	Color colorPrevious = Color.gray;
	ImagePlus imp;
	MouseListener ml, mlnew;
	MouseListener removeml;
	RandomAccessibleInterval<FloatType> CurrentView;
	ArrayList<Roi> Rois;
	MserTree<UnsignedByteType> newtree;
	RandomAccessibleInterval<UnsignedByteType> newimg;
	
	
	public Rectangle standardRectangle;
	RandomAccessibleInterval<FloatType> originalimgA;
	int length = 0;
	int height = 0;
	
	int radiusSliderinit = 0;
	
	
	public InteractiveSingleCell_(){
	
	}
	
	public InteractiveSingleCell_(final ImagePlus imp){
		this.imp = imp;
		standardRectangle = new Rectangle(1, 1, imp.getWidth() - 2, imp.getHeight() - 2);
		originalimgA = ImageJFunctions.convertFloat(imp.duplicate());
		height = imp.getHeight();
		length = imp.getWidth();
		
	}
	
	public InteractiveSingleCell_(final RandomAccessibleInterval<FloatType> originalimgA) {
		this.originalimgA = originalimgA;
		standardRectangle = new Rectangle(1, 1, (int) originalimgA.dimension(0) - 2,
				(int) originalimgA.dimension(1) - 2);

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
		thresholdInit = util.ScrollbarUtils.computeScrollbarPositionFromValue(threshold, thresholdMin, thresholdMax, scrollbarSize);
	}
	
	@Override
	public void run(String arg){
		
		Rois = new ArrayList<Roi>();
		peaks = new ArrayList<RefinedPeak<Point>>();
		AllSelectedrois = new ArrayList<Roi>();
		
		
		if (originalimgA.numDimensions()!=3){
			
			IJ.error("For simple tracker only XYT images are supported");
			
		}
		
		thirdDimension = 1;
		thirdDimensionSize = (int) originalimgA.dimension(2);

		CurrentView = util.FindersUtils.getCurrentView(originalimgA, thirdDimension);
		imp = ImageJFunctions.show(CurrentView);
		imp.setTitle("CurrentView of image");
	
		prestack = new ImageStack((int) originalimgA.dimension(0), (int) originalimgA.dimension(1),
				java.awt.image.ColorModel.getRGBdefault());
		
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
		final Float houghval = util.FindersUtils.AutomaticThresholding(CurrentView);
		setthresholdMin(thresholdMin);
		setthresholdMax(thresholdMax);
		setThreshold(threshold / 40);


		// Get local Minima in scale space to get Max rho-theta points
		float minPeakValue = houghval;

		threshold = minPeakValue;

		thresholdMax = (float) (minPeakValue);

		thresholdMin = (float) (0);
		
		
		
		Card();
		// add listener to the imageplus slice slider
		sliceObserver = new SliceObserver(imp, new ImagePlusListener());
		// compute first version#

		updatePreview(ValueChange.ALL);
		isStarted = true;

		// check whenever roi is modified to update accordingly
		roiListener = new RoiListener();
		imp.getCanvas().addMouseListener(roiListener);

		IJ.log(" Third Dimension Size " + thirdDimensionSize);

		
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

		if (change == ValueChange.THIRDDIM ) {
			System.out.println("Current Time point: " + thirdDimension);

			if (imp != null)
				imp.close();
			imp = ImageJFunctions.show(CurrentView);
			imp.setTitle("Current View in third dimension: " + " " + thirdDimension);
			
		}

		
		
		if (change == ValueChange.THIRDDIMTrack ) {
			if (imp != null)
				imp.close();
			imp = ImageJFunctions.show(CurrentView);
			imp.setTitle("Current View in third dimension: " + " " + thirdDimension);
			overlay = imp.getOverlay();
			if (overlay == null) {
				overlay = new Overlay();
				imp.setOverlay(overlay);
			}

			overlay.clear();
			
			if (Rois != null)
				Rois.clear();
			// imp = ImageJFunctions.wrapFloat(CurrentView, "current");

			long[] min = { (long) standardRectangle.getMinX(), (long) standardRectangle.getMinY() };
			long[] max = { (long) standardRectangle.getMaxX(), (long) standardRectangle.getMaxY() };
			interval = new FinalInterval(min, max);

			currentimg = util.FindersUtils.extractImage(CurrentView, interval);

			newimg = util.FindersUtils.copytoByteImage(currentimg);

			

			if (showMSER) {
				overlay.clear();
				IJ.log(" Computing the Component tree");

				newtree = MserTree.buildMserTree(newimg, delta, minSize, maxSize, maxVar, minDiversity, darktobright);
				Rois = util.FindersUtils.getcurrentRois(newtree);
				ArrayList<double[]> centerRoi = util.FindersUtils.getRoiMean(newtree);

				for (int index = 0; index < centerRoi.size(); ++index) {

				
					Roi or = Rois.get(index);

					or.setStrokeColor(colorDraw);
					overlay.add(or);
					roimanager.addRoi(or);
				}

			}

			if (showDOG) {

				overlay.clear();
				

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

					

				
					Roi or = Rois.get(index);

					or.setStrokeColor(colorDraw);
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
			}

			final Rectangle rect = roi.getBounds();

			
			long[] min = { (long) standardRectangle.getMinX(), (long) standardRectangle.getMinY() };
			long[] max = { (long) standardRectangle.getMaxX(), (long) standardRectangle.getMaxY() };
			interval = new FinalInterval(min, max);
			currentimg = util.FindersUtils.extractImage(CurrentView, interval);

			newimg = util.FindersUtils.copytoByteImage(currentimg);
			final Float houghval = util.FindersUtils.AutomaticThresholding(currentimg);

			// Get local Minima in scale space to get Max rho-theta points

			threshold = (float) getThreshold();

			thresholdMax = (float) getThresholdMax();

			thresholdMin = (float) getThresholdMin();
			}
			
		
		
		if (change == ValueChange.SHOWMSER) {
			
			overlay = imp.getOverlay();
			if (overlay == null) {
				overlay = new Overlay();
				imp.setOverlay(overlay);
			}

			overlay.clear();
			// check if Roi changed

			// if we got some mouse click but the ROI did not change we can
			// return
			if (change == ValueChange.ROI) {
				isComputing = false;
				return;
			}
			roimanager.close();

			roimanager = new RoiManager();

			IJ.log(" Computing the Component tree");

			newtree = MserTree.buildMserTree(newimg, delta, minSize, maxSize, maxVar, minDiversity, darktobright);
			Rois = util.FindersUtils.getcurrentRois(newtree);

			IJ.log("MSER parameters:" + " " + " thirdDimension: " + " " + thirdDimension);
			IJ.log("Delta " + " " + delta + " " + "minSize " + " " + minSize + " " + "maxSize " + " " + maxSize + " "
					+ " maxVar " + " " + maxVar + " " + "minDIversity " + " " + minDiversity);
			for (int index = 0; index < Rois.size(); ++index) {

				Roi or = Rois.get(index);

				or.setStrokeColor(colorDraw);
				overlay.add(or);
				roimanager.addRoi(or);
			}
		}
		if (change == ValueChange.SHOWDOG) {

			// check if Roi changed

			overlay = imp.getOverlay();
			if (overlay == null) {
				overlay = new Overlay();
				imp.setOverlay(overlay);
			}

			overlay.clear();			// if we got some mouse click but the ROI did not change we can
			// return
			if (change == ValueChange.ROI) {
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

			IJ.log("DoG parameters:" + " " + " thirdDimension: " + " " + thirdDimension );
			IJ.log("Sigma " + " " + sigma + " " + "Sigma2 " + " " + sigma2 + " " + "Threshold " + " " + threshold);
			peaks = newdog.getSubpixelPeaks();

			Rois = util.FindersUtils.getcurrentRois(peaks, sigma, sigma2);

			for (int index = 0; index < Rois.size(); ++index) {

				Roi or = Rois.get(index);

				or.setStrokeColor(colorDraw);
				overlay.add(or);
				roimanager.addRoi(or);
			}

		}

		// if we got some mouse click but the ROI did not change we can return
		if (change == ValueChange.ROI) {
			isComputing = false;
			return;
		}
		
		
		if (change == ValueChange.RADIUS){
			
			
			
			
			
			overlay.clear();

			// if we got some mouse click but the ROI did not change we can return
			if (change == ValueChange.ROI) {
				isComputing = false;
				return;
			}
			
			int x = 0;
			int y = 0;
			for (int index = 0; index < ClickedPoints.size(); ++index){
				
				x = ClickedPoints.get(index)[0];
				y = ClickedPoints.get(index)[1];
				
				
			}
			final OvalRoi Bigroi = new OvalRoi(Util.round(x - Radius), Util.round(y - Radius), Util.round(2 * Radius),
					Util.round(2 * Radius));
			Bigroi.setStrokeColor(colorSelect);
			overlay.add(Bigroi);
			
			
			roimanager.addRoi(Bigroi);
			
			
			
		}
		
		
		}
	// Making the card
			JFrame Cardframe = new JFrame("Manual Tracker");
			JPanel panelCont = new JPanel();
			JPanel panelFirst = new JPanel();
			JPanel panelSecond = new JPanel();
			JPanel panelThird = new JPanel();
			
			private void Card(){
				
				
				CardLayout cl = new CardLayout();

				cl.preferredLayoutSize(Cardframe);
				panelCont.setLayout(cl);

				panelCont.add(panelFirst, "1");
				panelCont.add(panelSecond, "2");
				panelCont.add(panelThird, "3");
				CheckboxGroup Finders = new CheckboxGroup();

				final Checkbox mser = new Checkbox("MSER", Finders, findBlobsViaMSER);
				final Checkbox dog = new Checkbox("DoG", Finders, findBlobsViaDOG);
				
				final JButton ChooseWorkspace = new JButton("Choose Workspace to save results in");
				final JLabel outputfilename = new JLabel("Enter output filename: ");
				TextField inputField = new TextField();
				inputField.setColumns(10);
				final JButton Confirm= new JButton("Confirm Workspace Selection");
				
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
				panelFirst.add(ChooseWorkspace, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 10, 0);
				panelFirst.add(outputfilename, c);
				
				++c.gridy;
				c.insets = new Insets(10, 10, 10, 0);
				panelFirst.add(inputField, c);
				
				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelFirst.add(Confirm, c);

				panelFirst.setVisible(true);

				cl.show(panelCont, "1");
				mser.addItemListener(new MserListener());
				dog.addItemListener(new dogListener());

			
				ChooseWorkspace.addActionListener(new ChooseWorkspaceListener());
				Confirm.addActionListener(new ConfirmWorkspaceListener(inputField));
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
				
				
				Cardframe.add(panelCont, BorderLayout.CENTER);
				Cardframe.add(control, BorderLayout.SOUTH);
				Cardframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

				Cardframe.setResizable(true);
				Cardframe.setVisible(true);
				Cardframe.pack();

				
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
				maxVar = util.ScrollbarUtils.computeValueFromScrollbarPosition(maxVarInit, maxVarMin, maxVarMax, scrollbarSize);
				delta = util.ScrollbarUtils.computeValueFromScrollbarPosition(deltaInit, deltaMin, deltaMax, scrollbarSize);
				minDiversity = util.ScrollbarUtils.computeValueFromScrollbarPosition(minDiversityInit, minDiversityMin, minDiversityMax,
						scrollbarSize);
				minSize = (int) util.ScrollbarUtils.computeValueFromScrollbarPosition(minSizeInit, minSizemin, minSizemax, scrollbarSize);
				maxSize = (int) util.ScrollbarUtils.computeValueFromScrollbarPosition(maxSizeInit, maxSizemin, maxSizemax, scrollbarSize);

				final Checkbox min = new Checkbox("Look for Minima ", darktobright);
				final Button ClickFast = new Button("Click here to choose a cell, then click on image");
				final Label deltaText = new Label("delta = ", Label.CENTER);
				final Label maxVarText = new Label("maxVar = ", Label.CENTER);
				final Label minDiversityText = new Label("minDiversity = ", Label.CENTER);
				final Label minSizeText = new Label("MinSize = ", Label.CENTER);
				final Label maxSizeText = new Label("MaxSize = ", Label.CENTER);
				final Label MSparam = new Label("Determine MSER parameters");
				final Button Confirm = new Button("Confirm your selection");
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
				
				++c.gridy;
				c.insets = new Insets(10, 0, 0, 0);
				panelSecond.add(ClickFast, c);
				
				++c.gridy;
				c.insets = new Insets(10, 0, 0, 0);
				panelSecond.add(Confirm, c);

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
				ClickFast.addActionListener(new chooseblobListener());
				Confirm.addActionListener(new ConfirmListener());
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
				sigma = util.ScrollbarUtils.computeValueFromScrollbarPosition(sigmaInit, sigmaMin, sigmaMax, scrollbarSize);
				threshold = util.ScrollbarUtils.computeValueFromScrollbarPosition(thresholdInit, thresholdMin, thresholdMax, scrollbarSize);
				sigma2 = util.ScrollbarUtils.computeSigma2(sigma, sensitivity);
				final int sigma2init = util.ScrollbarUtils.computeScrollbarPositionFromValue(sigma2, sigmaMin, sigmaMax, scrollbarSize);
				final Scrollbar sigma2S = new Scrollbar(Scrollbar.HORIZONTAL, sigma2init, 10, 0, 10 + scrollbarSize);

				final Label sigmaText1 = new Label("Sigma 1 = " + sigma, Label.CENTER);
				final Label sigmaText2 = new Label("Sigma 2 = " + sigma2, Label.CENTER);

				final Label thresholdText = new Label("Threshold = " + threshold, Label.CENTER);
				final Button ClickFast = new Button("Click here to choose a cell, then click on image");
				final Checkbox min = new Checkbox("Look for Minima (green)", lookForMinima);
				final Checkbox max = new Checkbox("Look for Maxima (red)", lookForMaxima);
				final Button DisplayBlobs = new Button("Display Blobs");
				final Button Confirm = new Button("Confirm your selection");

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
				c.insets = new Insets(10, 0, 0, 0);
				panelSecond.add(thresholdS, c);

				++c.gridy;
				c.insets = new Insets(10, 10, 0, 0);
				panelSecond.add(thresholdText, c);

			

				++c.gridy;
				c.insets = new Insets(0, 180, 0, 180);
				panelSecond.add(DisplayBlobs, c);
				
				++c.gridy;
				c.insets = new Insets(10, 0, 0, 0);
				panelSecond.add(ClickFast, c);
				
				++c.gridy;
				c.insets = new Insets(10, 0, 0, 0);
				panelSecond.add(Confirm, c);

				/* Configuration */
				sigma1.addAdjustmentListener(
						new SigmaListener(sigmaText1, sigmaMin, sigmaMax, scrollbarSize, sigma1, sigma2S, sigmaText2));
				
				thresholdS.addAdjustmentListener(new ThresholdListener(thresholdText, thresholdMin, thresholdMax));
				min.addItemListener(new MinListener());
				max.addItemListener(new MaxListener());
				ClickFast.addActionListener(new chooseblobListener());
				DisplayBlobs.addActionListener(new DisplayBlobsListener());
				Confirm.addActionListener(new ConfirmListener());
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
	
	
	protected class ConfirmListener implements ActionListener{
		@Override
		public void actionPerformed(final ActionEvent arg0) {
			
			
			imp.getCanvas().removeMouseListener(ml);
			panelThird.removeAll();
			/* Instantiation */
			final GridBagLayout layout = new GridBagLayout();
			final GridBagConstraints c = new GridBagConstraints();
			panelThird.setLayout(layout);
			

			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 1;
			c.weighty = 3.5;
			
			final Label Name = new Label("Step 3", Label.CENTER);
			panelThird.add(Name, c);
			
			final Scrollbar Radiusbar = new Scrollbar(Scrollbar.HORIZONTAL, RadiusInit, 10, 0, 10 + scrollbarSize);
			final Label AdjustRadi = new Label("Adjust Cell radius");
			Radius = util.ScrollbarUtils.computeValueFromScrollbarPosition(RadiusInit, RadiusMin, RadiusMax, scrollbarSize);
			AdjustRadi.setForeground(new Color(255, 255, 255));
			AdjustRadi.setBackground(new Color(1, 0, 1));
			final JButton ConfirmSelection = new JButton("Confirm Selection");
			final Checkbox displayrois = new Checkbox("Save Selected Roi Selection");
			final Button JumpFrame = new Button("Confirm and Go to Next Frame");
			final Button Done = new Button("Tracking complete");
			final Label timeText = new Label("Time in framenumber= " + thirdDimensionslider, Label.CENTER);
			final Scrollbar thirdDimensionsliderS = new Scrollbar(Scrollbar.HORIZONTAL, thirdDimensionsliderInit, 0, 0,
					thirdDimensionSize);
			thirdDimensionsliderS.setBlockIncrement(1);
		     thirdDimensionslider = (int) util.ScrollbarUtils.computeValueFromScrollbarPosition(thirdDimensionsliderInit, timeMin,
					thirdDimensionSize, thirdDimensionSize);
			final Label sizeTextX = new Label("Radius = " + Radius, Label.CENTER);
			final Button ClickFast = new Button("Start Cell Selections");
			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);
			panelThird.add(AdjustRadi, c);
			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);
			panelThird.add(Radiusbar, c);
			
			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);
			panelThird.add(sizeTextX, c);
			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);
			panelThird.add(displayrois, c);
			
			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);
			panelThird.add(ConfirmSelection, c);
			
			++c.gridy;
			panelThird.add(thirdDimensionsliderS, c);

			++c.gridy;
			panelThird.add(timeText, c);
			
			
			++c.gridy;
			c.insets = new Insets(10, 175, 0, 175);
			panelThird.add(ClickFast, c);
			
			
			++c.gridy;
			c.insets = new Insets(10, 175, 0, 175);
			panelThird.add(JumpFrame, c);
			
			++c.gridy;
			c.insets = new Insets(10, 175, 0, 175);
			panelThird.add(Done, c);
			
			Radiusbar.addAdjustmentListener(new RadiusListener(sizeTextX, RadiusMin, RadiusMax, scrollbarSize, Radiusbar));
			ConfirmSelection.addActionListener(new OpenRTListener());
			displayrois.addItemListener(new DisplayListener());
			thirdDimensionsliderS
			.addAdjustmentListener(new thirdDimensionsliderListener(timeText, timeMin, thirdDimensionSize));
			JumpFrame.addActionListener(
					new moveInThirdDimListener(thirdDimensionsliderS, timeText, timeMin, thirdDimensionSize));
			ClickFast.addActionListener(new chooseblobsameradiListener());
			Done.addActionListener(new DisplayRoiListener());
			panelThird.repaint();
			panelThird.validate();
			Cardframe.pack();
			
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
			
			
			
			thirdDimensionslider = (int) util.ScrollbarUtils.computeIntValueFromScrollbarPosition(event.getValue(), min, max,
					scrollbarSize);
			label.setText("Time in framenumber = " + thirdDimensionslider);

			thirdDimension = thirdDimensionslider;

			sliceObserver = new SliceObserver(imp, new ImagePlusListener());
			imp.setPosition(0, imp.getSlice(), thirdDimension);
			if (thirdDimension > thirdDimensionSize) {
				IJ.log("Max frame number exceeded, moving to last frame instead");
				
				imp.setPosition(0, imp.getSlice(), thirdDimensionSize);
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

			CurrentView = util.FindersUtils.getCurrentView(originalimgA, thirdDimension);
			
				
				updatePreview(ValueChange.THIRDDIMTrack);
				
				
			
			isStarted = true;

			// check whenever roi is modified to update accordingly
			roiListener = new RoiListener();
			imp.getCanvas().addMouseListener(roiListener);

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

			RoiManager roimanager = RoiManager.getInstance();
			Roi[] RoisOrig = roimanager.getRoisAsArray();
			
			if (imp != null) {

				Overlay o = imp.getOverlay();

				if (o == null) {
					o = new Overlay();
					imp.setOverlay(o);
				}

				ImagePlus Localimp = ImageJFunctions.show(CurrentView);
				prestack.addSlice(Localimp.getImageStack()
						.getProcessor(thirdDimension).convertToRGB());
				cp = (ColorProcessor) (prestack.getProcessor(thirdDimension).duplicate());

				Localimp.close();
				o.clear();
			
			
			for (int index = 0; index < RoisOrig.length; ++index){
				
				double[] center = util.FindersUtils.getCenter(currentimg,  RoisOrig[index]);
				double Intensity = util.FindersUtils.getIntensity(currentimg, RoisOrig[index]);
				double NumberofPixels = util.FindersUtils.getNumberofPixels(currentimg, RoisOrig[index]);
				Roi or = RoisOrig[index];
				AllSelectedrois.add(or);

				or.setStrokeColor(colorSelect);
				o.add(or);

				if (displayoverlay) {

					cp.setColor(colorSelect);
					cp.setLineWidth(1);
					cp.draw(or);

				}
				if (displayoverlay && prestack != null) 
					prestack.setPixels(cp.getPixels(), thirdDimension);

				
				rt.incrementCounter();
				
				rt.addValue("FrameNumber", thirdDimension);
				rt.addValue("LocationX", center[0] );
				rt.addValue("LocationY", center[1] );
				rt.addValue("Intensity", Intensity);
				rt.addValue("Number of Pixels", NumberofPixels);
				rt.addValue("Mean Intensity", Intensity / NumberofPixels);
				rt.addValue("Radius of ROI", Radius);
			
			
			
			
		}
		
			rt.show("Intensity Measurements, Save before closing");
			
			}
			if(thirdDimension <= thirdDimensionSize)
			   thirdDimension++;
			
			else{
				
			

				
				thirdDimension = thirdDimensionSize;
				
				
				
			}
			
				thirdDimensionScroll
						.setValue(util.ScrollbarUtils.computeIntScrollbarPositionFromValue(thirdDimension, min, max, scrollbarSize));
				thirdDimensionslider = thirdDimension;
				timeText.setText("Time in framenumber = " + thirdDimensionslider);

				if (thirdDimension > thirdDimensionSize) {
					IJ.log("Max frame number exceeded, moving to last frame instead");
					thirdDimension = thirdDimensionSize;
					CurrentView = util.FindersUtils.getCurrentView(originalimgA, thirdDimension);
				} else {

					CurrentView =  util.FindersUtils.getCurrentView(originalimgA, thirdDimension);
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
				updatePreview(ValueChange.THIRDDIMTrack);
				isStarted = true;

				// check whenever roi is modified to update accordingly
				roiListener = new RoiListener();
				imp.getCanvas().addMouseListener(roiListener);
				imp.getCanvas().addMouseListener(mlnew = new MouseListener() {

					@Override
					public void mouseClicked(MouseEvent e) {
						
						int x = e.getX();
						int y = e.getY();
						if (imp.getWidth() > 1000)
							x = 2 * x;
						if (imp.getHeight() > 1000)
							y = 2 * y;
						
						System.out.println("You chose: " + x + "," + y);
						if (ClickedPoints!=null)
							ClickedPoints.clear();
							
						ClickedPoints.add(new int[] { x, y });

						overlay = imp.getOverlay();

						if (overlay == null) {
							overlay = new Overlay();

							imp.setOverlay(overlay);

						}

						if (thirdDimension == thirdDimensionSize)
						JOptionPane.showMessageDialog(Cardframe, "You are at the last frame, save results and exit after completing this step", " Warning ",  JOptionPane.WARNING_MESSAGE);
						
						final OvalRoi Bigroi = new OvalRoi(Util.round(x - Radius), Util.round(y - Radius), Util.round(2 * Radius),
								Util.round(2 * Radius));
						Bigroi.setStrokeColor(colorSelect);
						overlay.add(Bigroi);
						RoiManager roim = RoiManager.getInstance();
						if(roim.getRoisAsArray().length > 0)
						roim.runCommand("Delete");
					updatePreview(ValueChange.RADIUS);

					}

					@Override
					public void mousePressed(MouseEvent e) {

					}

					@Override
					public void mouseReleased(MouseEvent e) {

					}

					@Override
					public void mouseEntered(MouseEvent e) {

					}

					@Override
					public void mouseExited(MouseEvent e) {

					}
				});
			
		}
	}

	protected class DisplayListener implements ItemListener{
		
		@Override
		public void itemStateChanged(final ItemEvent arg0) {
			
			if (arg0.getStateChange() == ItemEvent.DESELECTED)
				displayoverlay = false;
			else if (arg0.getStateChange() == ItemEvent.SELECTED) {
				displayoverlay = true;
			}
				
				
			}
			
			
			
		}
		
		
	
	
	protected class OpenRTListener implements ActionListener{
		@Override
		public void actionPerformed(final ActionEvent arg0) {
			
			RoiManager roimanager = RoiManager.getInstance();
			Roi[] RoisOrig = roimanager.getRoisAsArray();
			
			
			if (imp != null) {

				Overlay o = imp.getOverlay();

				if (o == null) {
					o = new Overlay();
					imp.setOverlay(o);
				}

				ImagePlus Localimp = ImageJFunctions.show(CurrentView);
				prestack.addSlice(Localimp.getImageStack()
						.getProcessor(thirdDimension).convertToRGB());
				cp = (ColorProcessor) (prestack.getProcessor(thirdDimension).duplicate());

				Localimp.close();
				o.clear();
			
			
			for (int index = 0; index < RoisOrig.length; ++index){
				
				double[] center = util.FindersUtils.getCenter(currentimg,  RoisOrig[index]);
				double Intensity = util.FindersUtils.getIntensity(currentimg, RoisOrig[index]);
				double NumberofPixels = util.FindersUtils.getNumberofPixels(currentimg, RoisOrig[index]);
				Roi or = RoisOrig[index];
				AllSelectedrois.add(or);

				or.setStrokeColor(Color.red);
				o.add(or);

				if (displayoverlay) {

					cp.setColor(Color.red);
					cp.setLineWidth(1);
					cp.draw(or);

				}
				if (displayoverlay && prestack != null) 
					prestack.setPixels(cp.getPixels(), thirdDimension);

				
				rt.incrementCounter();
				
				rt.addValue("FrameNumber", thirdDimension);
				rt.addValue("LocationX", center[0] );
				rt.addValue("LocationY", center[1] );
				rt.addValue("Intensity", Intensity);
				rt.addValue("Number of Pixels", NumberofPixels);
				rt.addValue("Mean Intensity", Intensity / NumberofPixels);
				rt.addValue("Radius of ROI", Radius);
			
			
			
			
		}
			
			rt.show("Intensity Measurements, Save before closing");
		
			
		}
		}
	}
	
	protected class DisplayRoiListener implements ActionListener{
		
		@Override
		public void actionPerformed(final ActionEvent arg0) {
			
			if (displayoverlay)
				new ImagePlus("Overlays", prestack).show();
			
			
		}
		
	}
	
	protected class chooseblobListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent arg0) {

			imp.getCanvas().addMouseListener(ml = new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent e) {
					
					int x = e.getX();
					int y = e.getY();
					if (imp.getWidth() > 1000)
						x = 2 * x;
					if (imp.getHeight() > 1000)
						y = 2 * y;
					
					System.out.println("You chose: " + x + "," + y);
					if (ClickedPoints!=null)
						ClickedPoints.clear();
						
					ClickedPoints.add(new int[] { x, y });

					overlay = imp.getOverlay();

					if (overlay == null) {
						overlay = new Overlay();

						imp.setOverlay(overlay);

					}

					overlay.clear();
					
					final OvalRoi Bigroi = new OvalRoi(Util.round(x - Radius), Util.round(y - Radius), Util.round(2 * Radius),
							Util.round(2 * Radius));
					Bigroi.setStrokeColor(colorSelect);
					overlay.add(Bigroi);
					
				

				}

				@Override
				public void mousePressed(MouseEvent e) {

				}

				@Override
				public void mouseReleased(MouseEvent e) {

				}

				@Override
				public void mouseEntered(MouseEvent e) {

				}

				@Override
				public void mouseExited(MouseEvent e) {

				}
			});

		}

	}
	
	protected class chooseblobsameradiListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent arg0) {

			imp.getCanvas().addMouseListener(mlnew = new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent e) {
					
					int x = e.getX();
					int y = e.getY();
					if (imp.getWidth() > 1000)
						x = 2 * x;
					if (imp.getHeight() > 1000)
						y = 2 * y;
					
					System.out.println("You chose: " + x + "," + y);
					if (ClickedPoints!=null)
						ClickedPoints.clear();
						
					ClickedPoints.add(new int[] { x, y });

					overlay = imp.getOverlay();

					if (overlay == null) {
						overlay = new Overlay();

						imp.setOverlay(overlay);

					}

					overlay.clear();
					if (thirdDimension == thirdDimensionSize)
					JOptionPane.showMessageDialog(Cardframe, "You are at the last frame, save results and exit after completing this step", " Warning ",  JOptionPane.WARNING_MESSAGE);
					
					final OvalRoi Bigroi = new OvalRoi(Util.round(x - Radius), Util.round(y - Radius), Util.round(2 * Radius),
							Util.round(2 * Radius));
					Bigroi.setStrokeColor(colorSelect);
					overlay.add(Bigroi);
					RoiManager roim = RoiManager.getInstance();
					if(roim.getRoisAsArray().length > 0)
					roim.runCommand("Delete");
				updatePreview(ValueChange.RADIUS);

				}

				@Override
				public void mousePressed(MouseEvent e) {

				}

				@Override
				public void mouseReleased(MouseEvent e) {

				}

				@Override
				public void mouseEntered(MouseEvent e) {

				}

				@Override
				public void mouseExited(MouseEvent e) {

				}
			});

		}

	}

	protected class DisplayBlobsListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			showDOG = true;
			updatePreview(ValueChange.SHOWDOG);

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
	
	protected class ComputeTreeListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			showMSER = true;
			updatePreview(ValueChange.SHOWMSER);

		}
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
			threshold = util.ScrollbarUtils.computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);
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
			sigma = util.ScrollbarUtils.computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);

			if (!enableSigma2) {
				sigma2 = util.ScrollbarUtils.computeSigma2(sigma, sensitivity);
				sigmaText2.setText("Sigma 2 = " + sigma2);
				sigmaScrollbar2.setValue(util.ScrollbarUtils.computeScrollbarPositionFromValue(sigma2, min, max, scrollbarSize));
			} else if (sigma > sigma2) {
				sigma = sigma2 - 0.001f;
				sigmaScrollbar1.setValue(util.ScrollbarUtils.computeScrollbarPositionFromValue(sigma, min, max, scrollbarSize));
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
			delta = util.ScrollbarUtils.computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);

			deltaScrollbar.setValue(util.ScrollbarUtils.computeScrollbarPositionFromValue(delta, min, max, scrollbarSize));

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
	
	protected class RadiusListener implements AdjustmentListener {
		final Label label;
		final float min, max;
		final int scrollbarSize;

		final Scrollbar radiusScrollbar;

		public RadiusListener(final Label label, final float min, final float max, final int scrollbarSize,
				final Scrollbar radiusScrollbar) {
			this.label = label;
			this.min = min;
			this.max = max;
			this.scrollbarSize = scrollbarSize;

			this.radiusScrollbar = radiusScrollbar;

		}
		
		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			
		
				
			
			Radius = util.ScrollbarUtils.computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);

			radiusScrollbar.setValue(util.ScrollbarUtils.computeScrollbarPositionFromValue(Radius, min, max, scrollbarSize));

			label.setText("Radius = " + Radius);

			
				while (isComputing) {
					SimpleMultiThreading.threadWait(10);
				}
			
				RoiManager roim = RoiManager.getInstance();
				if(roim.getRoisAsArray().length > 0)
				roim.runCommand("Delete");
				
				if (event.getValueIsAdjusting()){
						updatePreview(ValueChange.RADIUS);
				
						
						
	
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
			minSize = (int) util.ScrollbarUtils.computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);

			minsizeScrollbar.setValue(util.ScrollbarUtils.computeScrollbarPositionFromValue(minSize, min, max, scrollbarSize));

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
			maxSize = (int) util.ScrollbarUtils.computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);

			maxsizeScrollbar.setValue(util.ScrollbarUtils.computeScrollbarPositionFromValue(maxSize, min, max, scrollbarSize));

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
			maxVar = (util.ScrollbarUtils.computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize));

			maxVarScrollbar.setValue(util.ScrollbarUtils.computeScrollbarPositionFromValue((float) maxVar, min, max, scrollbarSize));

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
			minDiversity = (util.ScrollbarUtils.computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize));

			minDiversityScrollbar
					.setValue(util.ScrollbarUtils.computeScrollbarPositionFromValue((float) minDiversity, min, max, scrollbarSize));

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
protected class ChooseWorkspaceListener implements ActionListener {


		

		@Override
		public void actionPerformed(final ActionEvent arg0) {

			JFileChooser chooserA = new JFileChooser();
			chooserA.setCurrentDirectory(new java.io.File("."));
			chooserA.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooserA.showOpenDialog(panelFirst);
			usefolder = chooserA.getSelectedFile().getAbsolutePath();

		

		}

	}

	protected class ConfirmWorkspaceListener implements ActionListener {
		
		final TextField filename;
		
		public ConfirmWorkspaceListener(TextField filename){
			
			this.filename = filename;
			
		}
		
		@Override
		public void actionPerformed(final ActionEvent arg0) {
			
			addTrackToName = filename.getText();
			
			
		}
		
		
		
		
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
	
		

	public static void main(String[] args) {
		
			new ImageJ();
			

			    JFrame frame = new JFrame("");
			    SimpleFileChooser panel = new SimpleFileChooser();
			  
			    frame.getContentPane().add(panel,"Center");
			    frame.setSize(panel.getPreferredSize());
			    
		}
	
}
