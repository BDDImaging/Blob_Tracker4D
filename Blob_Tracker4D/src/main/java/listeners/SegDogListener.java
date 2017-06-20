package listeners;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Scrollbar;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import mpicbg.imglib.multithreading.SimpleMultiThreading;
import snakes.InteractiveActiveContour_;
import snakes.InteractiveActiveContour_.ValueChange;

public class SegDogListener implements ItemListener {

	
	 InteractiveActiveContour_ parent;
		
		
		public SegDogListener(final InteractiveActiveContour_ parent){
		
			this.parent = parent;
		}
		
	
	@Override
	public void itemStateChanged(final ItemEvent arg0) {
		boolean oldState = parent.findBlobsViaSEGMSER;

		if (arg0.getStateChange() == ItemEvent.DESELECTED)
			parent.findBlobsViaSEGDOG = false;
		else if (arg0.getStateChange() == ItemEvent.SELECTED) {

			parent.findBlobsViaSEGDOG = true;
			parent.findBlobsViaMSER = false;
			parent.findBlobsViaDOG = false;
			parent.findBlobsViaSEGMSER = false;
			parent.updatePreview(ValueChange.THIRDDIM);

			parent.panelSecond.removeAll();

			final GridBagLayout layout = new GridBagLayout();
			final GridBagConstraints c = new GridBagConstraints();

			parent.panelSecond.setLayout(layout);
			final Label Name = new Label("Step 2", Label.CENTER);
			parent.panelSecond.add(Name, c);
			final Label exthresholdText = new Label("threshold = threshold to create Bitimg for watershedding.",
					Label.CENTER);

			// IJ.log("Determining the initial threshold for the image");
			// thresholdHoughInit =
			// GlobalThresholding.AutomaticThresholding(currentPreprocessedimg);
			final Scrollbar thresholdSHough = new Scrollbar(Scrollbar.HORIZONTAL, parent.thresholdHoughInit, 10, 0,
					10 + parent.scrollbarSize);
			parent.thresholdHough = parent.computeValueFromScrollbarPosition(parent.thresholdHoughInit, parent.thresholdHoughMin,
					parent.thresholdHoughMax, parent.scrollbarSize);

			final Checkbox displayBit = new Checkbox("Display Bitimage ", parent.displayBitimg);
			final Checkbox displayWatershed = new Checkbox("Display Watershedimage ", parent.displayWatershedimg);
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
			parent.panelSecond.add(Segparam, c);

			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);
			parent.panelSecond.add(exthresholdText, c);
			++c.gridy;

			c.insets = new Insets(10, 10, 0, 0);
			parent.panelSecond.add(thresholdText, c);
			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);

			parent.panelSecond.add(thresholdSHough, c);
			++c.gridy;

			c.insets = new Insets(10, 175, 0, 175);
			parent.panelSecond.add(displayBit, c);

			++c.gridy;
			c.insets = new Insets(10, 175, 0, 175);
			parent.panelSecond.add(displayWatershed, c);
			++c.gridy;
			c.insets = new Insets(10, 175, 0, 175);
			parent.panelSecond.add(Dowatershed, c);

			thresholdSHough.addAdjustmentListener(
					new ThresholdHoughListener(parent, thresholdText, parent.thresholdHoughMin, parent.thresholdHoughMax));

			displayBit.addItemListener(new ShowBitimgListener(parent));
			displayWatershed.addItemListener(new ShowwatershedimgListener(parent));
			Dowatershed.addActionListener(new DowatershedListener(parent));

			final Label DogText = new Label("Use DoG to find Blobs ", Label.CENTER);
			final Scrollbar sigma1 = new Scrollbar(Scrollbar.HORIZONTAL, parent.sigmaInit, 10, 0, 10 + parent.scrollbarSize);

			final Scrollbar thresholdSsec = new Scrollbar(Scrollbar.HORIZONTAL, parent.thresholdInit, 10, 0,
					10 + parent.scrollbarSize);
			parent.sigma = parent.computeValueFromScrollbarPosition(parent.sigmaInit, parent.sigmaMin, parent.sigmaMax, parent.scrollbarSize);
			parent.threshold = parent.computeValueFromScrollbarPosition(parent.thresholdInit, parent.thresholdMin, parent.thresholdMax, parent.scrollbarSize);
			parent.sigma2 = parent.computeSigma2(parent.sigma, parent.sensitivity);
			final int sigma2init = parent.computeScrollbarPositionFromValue(parent.sigma2, parent.sigmaMin, parent.sigmaMax, parent.scrollbarSize);
			final Scrollbar sigma2S = new Scrollbar(Scrollbar.HORIZONTAL, sigma2init, 10, 0, 10 + parent.scrollbarSize);

			final Label sigmaText1 = new Label("Sigma 1 = " + parent.sigma, Label.CENTER);
			final Label sigmaText2 = new Label("Sigma 2 = " + parent.sigma2, Label.CENTER);

			final Label thresholdsecText = new Label("Threshold = " + parent.threshold, Label.CENTER);

			final Checkbox min = new Checkbox("Look for Minima (green)", parent.lookForMinima);
			final Checkbox max = new Checkbox("Look for Maxima (red)", parent.lookForMaxima);
			final Button DisplayBlobs = new Button("Display Blobs");

			final Label MSparam = new Label("Determine DoG parameters");
			MSparam.setBackground(new Color(1, 0, 1));
			MSparam.setForeground(new Color(255, 255, 255));
			/* Location */

			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);
			parent.panelSecond.add(MSparam, c);
			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);
			parent.panelSecond.add(sigma1, c);

			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);
			parent.panelSecond.add(sigmaText1, c);

			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);
			parent.panelSecond.add(sigma2S, c);

			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);
			parent.panelSecond.add(sigmaText2, c);

			++c.gridy;
			c.insets = new Insets(10, 0, 0, 0);
			parent.panelSecond.add(thresholdSsec, c);

			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);
			parent.panelSecond.add(thresholdsecText, c);

			++c.gridy;
			c.insets = new Insets(0, 170, 0, 75);
			parent.panelSecond.add(min, c);

			++c.gridy;
			c.insets = new Insets(0, 170, 0, 75);
			parent.panelSecond.add(max, c);

		

			/* Configuration */
			sigma1.addAdjustmentListener(
					new SigmaListener(parent, sigmaText1, parent.sigmaMin, parent.sigmaMax, parent.scrollbarSize, sigma1, sigma2S, sigmaText2));
			sigma2S.addAdjustmentListener(
					new Sigma2Listener(parent, parent.sigmaMin, parent.sigmaMax, parent.scrollbarSize, sigma2S, sigmaText2));
			thresholdSsec
					.addAdjustmentListener(new ThresholdListener(parent, thresholdsecText, parent.thresholdMin, parent.thresholdMax));
			min.addItemListener(new MinListener(parent));
			max.addItemListener(new MaxListener(parent));
			DisplayBlobs.addActionListener(new DisplaysegBlobsListener(parent));
			parent.panelSecond.repaint();
			parent.panelSecond.validate();
			parent.Cardframe.pack();
		}

		

			parent.updatePreview(ValueChange.SHOWSEGDOG);
		
	}
	
}
