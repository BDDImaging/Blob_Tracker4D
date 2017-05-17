package listeners;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import mpicbg.imglib.multithreading.SimpleMultiThreading;
import snakes.InteractiveActiveContour_;
import snakes.InteractiveActiveContour_.ValueChange;

public class DarktobrightListener implements ItemListener {

	InteractiveActiveContour_ parent;
	 
	 public DarktobrightListener(final InteractiveActiveContour_ parent){
			
			this.parent = parent;
		}
	
	
	@Override
	public void itemStateChanged(final ItemEvent arg0) {
		boolean oldState = parent.darktobright;

		if (arg0.getStateChange() == ItemEvent.DESELECTED)
			parent.darktobright = false;
		else if (arg0.getStateChange() == ItemEvent.SELECTED)
			parent.darktobright = true;

		if (parent.darktobright != oldState) {
			while (parent.isComputing)
				SimpleMultiThreading.threadWait(10);

			parent.updatePreview(ValueChange.DARKTOBRIGHT);
		}
	}
	
}
