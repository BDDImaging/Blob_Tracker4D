package listeners;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import mpicbg.imglib.multithreading.SimpleMultiThreading;
import snakes.InteractiveActiveContour_;
import snakes.InteractiveActiveContour_.ValueChange;



	
	public class MinListener implements ItemListener {
		
		InteractiveActiveContour_ parent;
		
		
		public MinListener(final InteractiveActiveContour_ parent){
		
			this.parent = parent;
		}
		
		
		@Override
		public void itemStateChanged(final ItemEvent arg0) {
			boolean oldState = parent.lookForMinima;

			if (arg0.getStateChange() == ItemEvent.DESELECTED)
				parent.lookForMinima = false;
			else if (arg0.getStateChange() == ItemEvent.SELECTED)
				parent.lookForMinima = true;

			if (parent.lookForMinima != oldState) {
				while (parent.isComputing)
					SimpleMultiThreading.threadWait(10);

				parent.updatePreview(ValueChange.MINMAX);
			}
		}
	}
	

