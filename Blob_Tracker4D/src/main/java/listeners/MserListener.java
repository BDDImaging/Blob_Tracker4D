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


public class MserListener implements ItemListener {
	
	InteractiveActiveContour_ parent;
	
	public MserListener(final InteractiveActiveContour_ parent){
	
		this.parent = parent;
	}
	
	
	@Override
	public void itemStateChanged(final ItemEvent arg0) {
		boolean oldState = parent.findBlobsViaMSER;

		if (arg0.getStateChange() == ItemEvent.DESELECTED)
			parent.findBlobsViaMSER = false;
		else if (arg0.getStateChange() == ItemEvent.SELECTED) {

			parent.findBlobsViaMSER = true;
			parent.findBlobsViaDOG = false;
			parent.updatePreview(ValueChange.THIRDDIM);

			parent.panelSecond.removeAll();

			final GridBagLayout layout = new GridBagLayout();
			final GridBagConstraints c = new GridBagConstraints();

			parent.panelSecond.setLayout(layout);
			final Label Name = new Label("Step 2", Label.CENTER);
			parent.panelSecond.add(Name, c);

			final Scrollbar deltaS = new Scrollbar(Scrollbar.HORIZONTAL, parent.deltaInit, 10, 0, 10 + parent.scrollbarSize);
			final Scrollbar maxVarS = new Scrollbar(Scrollbar.HORIZONTAL, parent.maxVarInit, 10, 0, 10 + parent.scrollbarSize);
			final Scrollbar minDiversityS = new Scrollbar(Scrollbar.HORIZONTAL, parent.minDiversityInit, 10, 0,
					10 + parent.scrollbarSize);
			final Scrollbar minSizeS = new Scrollbar(Scrollbar.HORIZONTAL, parent.minSizeInit, 10, 0, 10 + parent.scrollbarSize);
			final Scrollbar maxSizeS = new Scrollbar(Scrollbar.HORIZONTAL, parent.maxSizeInit, 10, 0, 10 + parent.scrollbarSize);
			final Button ComputeTree = new Button("Compute Tree and display");
			parent.maxVar = parent.computeValueFromScrollbarPosition(parent.maxVarInit, parent.maxVarMin, parent.maxVarMax, parent.scrollbarSize);
			parent.delta = parent.computeValueFromScrollbarPosition(parent.deltaInit, parent.deltaMin, parent.deltaMax, parent.scrollbarSize);
			parent.minDiversity = parent.computeValueFromScrollbarPosition(parent.minDiversityInit, parent.minDiversityMin, parent.minDiversityMax,
					parent.scrollbarSize);
			parent.minSize = (int) parent.computeValueFromScrollbarPosition(parent.minSizeInit, parent.minSizemin, parent.minSizemax, parent.scrollbarSize);
			parent.maxSize = (int) parent.computeValueFromScrollbarPosition(parent.maxSizeInit, parent.maxSizemin, parent.maxSizemax, parent.scrollbarSize);


			final Label deltaText = new Label("Grey Level Seperation between Components = " + parent.delta, Label.CENTER);
			final Label maxVarText = new Label("Unstability Score = " + parent.maxVar, Label.CENTER);
			final Label minDiversityText = new Label("minDiversity = " + parent.minDiversity, Label.CENTER);
			final Label minSizeText = new Label("Min # of pixels inside MSER Ellipses = " + parent.minSize, Label.CENTER);
			final Label maxSizeText = new Label("Max # of pixels inside MSER Ellipses = " + parent.maxSize, Label.CENTER);
			final Label MSparam = new Label("Determine MSER parameters");
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
			parent.panelSecond.add(deltaText, c);

			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);
			parent.panelSecond.add(deltaS, c);

			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);
			parent.panelSecond.add(maxVarText, c);

			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);
			parent.panelSecond.add(maxVarS, c);

			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);
			parent.panelSecond.add(minDiversityText, c);

			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);
			parent.panelSecond.add(minDiversityS, c);

			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);
			parent.panelSecond.add(minSizeText, c);

			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);
			parent.panelSecond.add(minSizeS, c);

			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);
			parent.panelSecond.add(maxSizeText, c);

			++c.gridy;
			c.insets = new Insets(10, 10, 0, 0);
			parent.panelSecond.add(maxSizeS, c);

			
			

			deltaS.addAdjustmentListener(new DeltaListener(parent, deltaText, parent.deltaMin, parent.deltaMax, parent.scrollbarSize, deltaS));

			maxVarS.addAdjustmentListener(
					new MaxVarListener(parent, maxVarText, parent.maxVarMin, parent.maxVarMax, parent.scrollbarSize, maxVarS));

			minDiversityS.addAdjustmentListener(new MinDiversityListener(parent, minDiversityText, parent.minDiversityMin,
					parent.minDiversityMax, parent.scrollbarSize, minDiversityS));

			minSizeS.addAdjustmentListener(
					new MinSizeListener(parent, minSizeText, parent.minSizemin, parent.minSizemax, parent.scrollbarSize, minSizeS));

			maxSizeS.addAdjustmentListener(
					new MaxSizeListener(parent, maxSizeText, parent.maxSizemin, parent.maxSizemax, parent.scrollbarSize, maxSizeS));

			parent.panelSecond.repaint();
			parent.panelSecond.validate();
			parent.Cardframe.pack();
			parent.updatePreview(ValueChange.SHOWMSER);
		}

		
	}
	

}
