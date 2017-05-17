package listeners;

import java.awt.Label;
import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ij.IJ;
import ij.WindowManager;
import ij.gui.Roi;
import snakes.InteractiveActiveContour_;
import snakes.InteractiveActiveContour_.ValueChange;

public class MoveInThirdDimListener implements ActionListener {
	final float min, max;
	Label timeText;
	final Scrollbar thirdDimensionScroll;
	InteractiveActiveContour_ parent;
	public MoveInThirdDimListener(InteractiveActiveContour_ parent, Scrollbar thirdDimensionScroll, Label timeText, float min, float max) {
		this.thirdDimensionScroll = thirdDimensionScroll;
		this.min = min;
		this.parent = parent;
		this.max = max;
		this.timeText = timeText;
	}

	@Override
	public void actionPerformed(final ActionEvent arg0) {

		boolean dialog = parent.Dialogue();
		if (dialog) {

			thirdDimensionScroll
					.setValue(parent.computeIntScrollbarPositionFromValue(parent.thirdDimension, min, max, parent.scrollbarSize));
			timeText.setText("Third Dimension = " + parent.thirdDimensionslider);

			if (parent.thirdDimension > parent.thirdDimensionSize) {
				IJ.log("Max frame number exceeded, moving to last frame instead");
				parent.thirdDimension = parent.thirdDimensionSize;
				parent.CurrentView = parent.getCurrentView();
				parent.otherCurrentView = parent.getotherCurrentView();
			} else {

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
			parent.updatePreview(ValueChange.THIRDDIM);
			parent.isStarted = true;

			// check whenever roi is modified to update accordingly
			
			parent.imp.getCanvas().addMouseListener(parent.roiListener);

		}
	}
}
