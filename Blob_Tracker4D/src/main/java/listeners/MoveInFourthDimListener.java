package listeners;

import java.awt.Label;
import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import fiji.tool.SliceObserver;
import ij.IJ;
import ij.WindowManager;
import ij.gui.Roi;
import snakes.InteractiveActiveContour_;
import snakes.InteractiveActiveContour_.ValueChange;

public class MoveInFourthDimListener implements ActionListener {
	

	InteractiveActiveContour_ parent;
	 
	
	
	final float min, max;
	Label sliceText;
	final Scrollbar fourthDimensionScroll;

	public MoveInFourthDimListener(final InteractiveActiveContour_ parent, Scrollbar fourthDimensionScroll, Label sliceText, float min, float max) {
		this.fourthDimensionScroll = fourthDimensionScroll;
		this.min = min;
		this.max = max;
		this.parent = parent;
		this.sliceText = sliceText;
	}

	@Override
	public void actionPerformed(final ActionEvent arg0) {

		boolean dialog = parent.DialogueSlice();
		if (dialog) {

			fourthDimensionScroll
					.setValue(parent.computeIntScrollbarPositionFromValue(parent.fourthDimension, min, max, parent.scrollbarSize));
			sliceText.setText("Fourth Dimension = " + parent.fourthDimensionslider);

			if (parent.fourthDimension > parent.fourthDimensionSize) {
				IJ.log("Fourth dimension size exceeded, moving to co-ordinate in 4th dimension instead");
				parent.fourthDimension = parent.fourthDimensionSize;
				parent.CurrentView = parent.getCurrentView();
				parent.otherCurrentView = parent.getotherCurrentView();
			}

			else {

				parent.CurrentView = parent.getCurrentView();
				parent.otherCurrentView = parent.getotherCurrentView();

			}

			parent.imp = WindowManager.getCurrentImage();
		
			Roi roi = parent.imp.getRoi();
			if (roi == null) {
				// IJ.log( "A rectangular ROI is required to define the
				// area..."
				// );
				parent.imp.setRoi(parent.standardRectangle);
				roi = parent.imp.getRoi();
			}

			// compute first version
			parent.updatePreview(ValueChange.FOURTHDIM);
			parent.isStarted = true;

			// check whenever roi is modified to update accordingly
			
			parent.imp.getCanvas().addMouseListener(parent.roiListener);

		}
	}
}