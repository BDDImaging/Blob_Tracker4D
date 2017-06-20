package listeners;

import java.awt.Label;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import mpicbg.imglib.multithreading.SimpleMultiThreading;
import snakes.InteractiveActiveContour_;
import snakes.InteractiveActiveContour_.ValueChange;


	public class SigmaListener implements AdjustmentListener {
		final Label label;
		InteractiveActiveContour_ parent;
		
		
		final float min, max;
		final int scrollbarSize;

		final Scrollbar sigmaScrollbar1;
		final Scrollbar sigmaScrollbar2;
		final Label sigmaText2;

		public SigmaListener(final InteractiveActiveContour_ parent, final Label label, final float min, final float max, final int scrollbarSize,
				final Scrollbar sigmaScrollbar1, final Scrollbar sigmaScrollbar2, final Label sigmaText2) {
			this.label = label;
			this.min = min;
			this.max = max;
			this.scrollbarSize = scrollbarSize;
			this.parent = parent;
			this.sigmaScrollbar1 = sigmaScrollbar1;
			this.sigmaScrollbar2 = sigmaScrollbar2;
			this.sigmaText2 = sigmaText2;
			sigmaScrollbar1.addMouseListener( new StandardMouseListener( parent, ValueChange.SHOWDOG ) );
		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			parent.sigma = parent.computeValueFromScrollbarPosition(event.getValue(), min, max, scrollbarSize);

			if (!parent.enableSigma2) {
				parent.sigma2 = parent.computeSigma2(parent.sigma, parent.sensitivity);
				sigmaText2.setText("Sigma 2 = " + parent.sigma2);
				sigmaScrollbar2.setValue(parent.computeScrollbarPositionFromValue(parent.sigma2, min, max, scrollbarSize));
			} else if (parent.sigma > parent.sigma2) {
				parent.sigma = parent.sigma2 - 0.001f;
				sigmaScrollbar1.setValue(parent.computeScrollbarPositionFromValue(parent.sigma, min, max, scrollbarSize));
			}

			label.setText("Sigma 1 = " + parent.sigma);

		
		}
	
}
