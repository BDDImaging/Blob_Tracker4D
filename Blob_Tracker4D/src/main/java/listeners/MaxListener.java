package listeners;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import mpicbg.imglib.multithreading.SimpleMultiThreading;
import snakes.InteractiveActiveContour_;
import snakes.InteractiveActiveContour_.ValueChange;



	public class MaxListener implements ItemListener {
		
            InteractiveActiveContour_ parent;
		
		
		public MaxListener(final InteractiveActiveContour_ parent){
		
			this.parent = parent;
		}
		
		
		@Override
		public void itemStateChanged(final ItemEvent arg0) {
			boolean oldState = parent.lookForMaxima;

			if (arg0.getStateChange() == ItemEvent.DESELECTED)
				parent.lookForMaxima = false;
			else if (arg0.getStateChange() == ItemEvent.SELECTED)
				parent.lookForMaxima = true;

			if (parent.lookForMaxima != oldState) {
				while (parent.isComputing)
					SimpleMultiThreading.threadWait(10);

				parent.updatePreview(ValueChange.MINMAX);

			}
		}
	}
	

