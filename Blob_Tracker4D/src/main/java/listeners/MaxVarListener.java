package listeners;

import java.awt.Label;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;


import mpicbg.imglib.multithreading.SimpleMultiThreading;
import snakes.InteractiveActiveContour_;
import snakes.InteractiveActiveContour_.ValueChange;

public class MaxVarListener implements AdjustmentListener {
	final Label label;
	final float min, max;
	final int scrollbarSize;
	InteractiveActiveContour_ parent;
	final Scrollbar MaxVarScrollbar;

	public MaxVarListener(final InteractiveActiveContour_  parent, final Label label, final float min, final float max, final int scrollbarSize,
			final Scrollbar MaxVarScrollbar) {
		this.label = label;
		this.min = min;
		this.max = max;
		this.parent = parent;
		this.scrollbarSize = scrollbarSize;
		this.MaxVarScrollbar = MaxVarScrollbar;

	}
	
	
	

	@Override
	public void adjustmentValueChanged(final AdjustmentEvent event) {
		parent.maxVar = (parent.computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize));

		MaxVarScrollbar.setValue(parent.computeScrollbarPositionFromValue((float) parent.maxVar, min, max, scrollbarSize));

		label.setText("Unstability Score = " + parent.maxVar);

		// if ( !event.getValueIsAdjusting() )
		{
			while (parent.isComputing) {
				SimpleMultiThreading.threadWait(10);
			}
			parent.updatePreview(ValueChange.MAXVAR);
		}
	}
}
