package listeners;

import java.awt.Label;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import mpicbg.imglib.multithreading.SimpleMultiThreading;
import snakes.InteractiveActiveContour_;
import snakes.InteractiveActiveContour_.ValueChange;



	
	public class ThresholdListener implements AdjustmentListener {
		
	  InteractiveActiveContour_ parent;
			
  		final Label label;
		final float min, max;
		final Scrollbar sigmaScrollbar1;
		public ThresholdListener(final InteractiveActiveContour_ parent, final Label label, final float min, final float max,final Scrollbar sigmaScrollbar1) {
			this.label = label;
			this.parent = parent;
			this.min = min;
			this.sigmaScrollbar1 = sigmaScrollbar1;
			this.max = max;
			sigmaScrollbar1.addMouseListener( new StandardMouseListener( parent, ValueChange.SHOWDOG ) );
		}

		@Override
		public void adjustmentValueChanged(final AdjustmentEvent event) {
			parent.threshold = parent.computeValueFromScrollbarPosition(event.getValue(), min, max, parent.scrollbarSize);
			label.setText("Threshold = " + parent.threshold);

		
		}
	}

	

