package listeners;

import java.awt.Label;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import mpicbg.imglib.multithreading.SimpleMultiThreading;
import snakes.InteractiveActiveContour_;
import snakes.InteractiveActiveContour_.ValueChange;

public  class Sigma2Listener implements AdjustmentListener {
		final float min, max;
		final int scrollbarSize;
		InteractiveActiveContour_ parent;
		final Scrollbar sigmaScrollbar2;
		final Label sigma2Label;

		public Sigma2Listener(final InteractiveActiveContour_ parent, final float min, final float max, final int scrollbarSize,
				final Scrollbar sigmaScrollbar2, final Label sigma2Label) {
			this.min = min;
			this.max = max;
			this.scrollbarSize = scrollbarSize;

			this.parent = parent;
			this.sigmaScrollbar2 = sigmaScrollbar2;
			this.sigma2Label = sigma2Label;
		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			if (parent.enableSigma2) {
				parent.sigma2 = parent.computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);

				if (parent.sigma2 < parent.sigma) {
					parent.sigma2 = parent.sigma + 0.001f;
					sigmaScrollbar2.setValue(parent.computeScrollbarPositionFromValue(parent.sigma2, min, max, scrollbarSize));
				}

				sigma2Label.setText("Sigma 2 = " + parent.sigma2);

				if (!event.getValueIsAdjusting()) {
					while (parent.isComputing) {
						SimpleMultiThreading.threadWait(10);
					}
					parent.updatePreview(ValueChange.SIGMA);
				}

			} else {
				// if no manual adjustment simply reset it
				sigmaScrollbar2.setValue(parent.computeScrollbarPositionFromValue(parent.sigma2, min, max, scrollbarSize));
			}
		}
	}