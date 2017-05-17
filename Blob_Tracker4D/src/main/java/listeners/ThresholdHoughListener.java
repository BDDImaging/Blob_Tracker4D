package listeners;

import java.awt.Label;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import mpicbg.imglib.multithreading.SimpleMultiThreading;
import snakes.InteractiveActiveContour_;
import snakes.InteractiveActiveContour_.ValueChange;


	public class ThresholdHoughListener implements AdjustmentListener {
		final Label label;
		final float min, max;
		InteractiveActiveContour_ parent;
		public ThresholdHoughListener(final InteractiveActiveContour_ parent ,final Label label, final float min, final float max) {
			this.label = label;
			this.parent = parent;
			this.min = min;
			this.max = max;
		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			parent.thresholdHough = parent.computeValueFromScrollbarPosition(event.getValue(), min, max, parent.scrollbarSize);
			label.setText("Threshold = " + parent.thresholdHough);

			if (!parent.isComputing) {
				parent.updatePreview(ValueChange.THRESHOLD);
			} else if (!event.getValueIsAdjusting()) {
				while (parent.isComputing) {
					SimpleMultiThreading.threadWait(10);
				}
				parent.updatePreview(ValueChange.THRESHOLD);
			}
		}
	}
	

