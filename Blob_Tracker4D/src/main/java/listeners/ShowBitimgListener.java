package listeners;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import mpicbg.imglib.multithreading.SimpleMultiThreading;
import snakes.InteractiveActiveContour_;
import snakes.InteractiveActiveContour_.ValueChange;



	public class ShowBitimgListener implements ItemListener {
		
		 InteractiveActiveContour_ parent;
		 
		 public ShowBitimgListener(final InteractiveActiveContour_ parent){
				
				this.parent = parent;
			}
		
		@Override
		public void itemStateChanged(final ItemEvent arg0) {
			boolean oldState = parent.displayBitimg;

			if (arg0.getStateChange() == ItemEvent.DESELECTED)
				parent.displayBitimg = false;
			else if (arg0.getStateChange() == ItemEvent.SELECTED)
				parent.displayBitimg = true;

			if (parent.displayBitimg != oldState) {
				while (parent.isComputing)
					SimpleMultiThreading.threadWait(10);

				parent.updatePreview(ValueChange.DISPLAYBITIMG);
			}
		}
	}
	

