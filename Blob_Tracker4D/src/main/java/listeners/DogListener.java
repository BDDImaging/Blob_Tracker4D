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

public class DogListener implements ItemListener {
	
	InteractiveActiveContour_ parent;
	
	public DogListener( final InteractiveActiveContour_ parent){
		
		this.parent = parent;
		
	}
	
	@Override
	public void itemStateChanged(final ItemEvent arg0) {

		boolean oldState = parent.showDOG;
		if (arg0.getStateChange() == ItemEvent.DESELECTED)
			parent.showDOG = false;
		else if (arg0.getStateChange() == ItemEvent.SELECTED) {

			parent.showDOG = true;
			parent.showMSER = false;
			parent.updatePreview(ValueChange.THIRDDIM);

			parent.panelSecond.removeAll();

			final GridBagLayout layout = new GridBagLayout();
			final GridBagConstraints c = new GridBagConstraints();

			parent.panelSecond.setLayout(layout);
			final Label Name = new Label("Step 2", Label.CENTER);
			parent.panelSecond.add(Name, c);
			final Label DogText = new Label("Use DoG to find Blobs ", Label.CENTER);
			final Scrollbar sigma1 = new Scrollbar(Scrollbar.HORIZONTAL, parent.sigmaInit, 10, 0, 10 + parent.scrollbarSize);

			final Scrollbar thresholdS = new Scrollbar(Scrollbar.HORIZONTAL, parent.thresholdInit, 10, 0,
					10 + parent.scrollbarSize);
			parent.sigma = parent.computeValueFromScrollbarPosition(parent.sigmaInit, parent.sigmaMin, parent.sigmaMax, parent.scrollbarSize);
			parent.threshold = parent.computeValueFromScrollbarPosition(parent.thresholdInit, parent.thresholdMin, parent.thresholdMax, parent.scrollbarSize);
			parent.sigma2 = parent.computeSigma2(parent.sigma, parent.sensitivity);
			final int sigma2init = parent.computeScrollbarPositionFromValue(parent.sigma2, parent.sigmaMin, parent.sigmaMax, parent.scrollbarSize);
			final Scrollbar sigma2S = new Scrollbar(Scrollbar.HORIZONTAL, sigma2init, 10, 0, 10 + parent.scrollbarSize);

			final Label sigmaText1 = new Label("Sigma 1 = " + parent.sigma, Label.CENTER);
			final Label sigmaText2 = new Label("Sigma 2 = " + parent.sigma2, Label.CENTER);

			final Label thresholdText = new Label("Threshold = " + parent.threshold, Label.CENTER);

			final Checkbox min = new Checkbox("Look for Minima (green)", parent.lookForMinima);
			final Checkbox max = new Checkbox("Look for Maxima (red)", parent.lookForMaxima);
			final Button DisplayBlobs = new Button("Display Blobs");

			final Label MSparam = new Label("Determine DoG parameters");
			MSparam.setBackground(new Color(1, 0, 1));
			MSparam.setForeground(new Color(255, 255, 255));
			/* Location */
			parent.panelSecond.setLayout(layout);

			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 4;
			c.weighty = 1.5;

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
			c.insets = new Insets(10, 0, 0, 0);
			parent.panelSecond.add(min, c);

			++c.gridy;
			c.insets = new Insets(10, 0, 0, 0);
			parent.panelSecond.add(thresholdS, c);

			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);
			parent.panelSecond.add(thresholdText, c);

		

			/* Configuration */
			sigma1.addAdjustmentListener(
					new SigmaListener(parent, sigmaText1, parent.sigmaMin, parent.sigmaMax, parent.scrollbarSize, sigma1, sigma2S, sigmaText2));
			sigma2S.addAdjustmentListener(
					new Sigma2Listener(parent, parent.sigmaMin, parent.sigmaMax, parent.scrollbarSize, sigma2S, sigmaText2));
			thresholdS.addAdjustmentListener(new ThresholdListener(parent, thresholdText, parent.thresholdMin, parent.thresholdMax));
			min.addItemListener(new MinListener(parent));
			max.addItemListener(new MaxListener(parent));
			DisplayBlobs.addActionListener(new DisplayBlobsListener(parent));
			parent.panelSecond.repaint();
			parent.panelSecond.validate();
			parent.Cardframe.pack();
		}

		

			parent.updatePreview(ValueChange.SHOWDOG);
		
	}
}
