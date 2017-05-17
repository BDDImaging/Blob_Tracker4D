package listeners;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import mpicbg.imglib.multithreading.SimpleMultiThreading;
import snakes.InteractiveActiveContour_;
import snakes.InteractiveActiveContour_.ValueChange;



	
	public class ShowwatershedimgListener implements ItemListener {
		
		InteractiveActiveContour_ parent;
		 
		 public ShowwatershedimgListener(final InteractiveActiveContour_ parent){
				
				this.parent = parent;
			}
		
		@Override
		public void itemStateChanged(final ItemEvent arg0) {
			boolean oldState = parent.displayWatershedimg;

			if (arg0.getStateChange() == ItemEvent.DESELECTED)
				parent.displayWatershedimg = false;
			else if (arg0.getStateChange() == ItemEvent.SELECTED)
				parent.displayWatershedimg = true;

			if (parent.displayWatershedimg != oldState) {
				while (parent.isComputing)
					SimpleMultiThreading.threadWait(10);

				parent.updatePreview(ValueChange.DISPLAYWATERSHEDIMG);
			}
		}
	}
	

