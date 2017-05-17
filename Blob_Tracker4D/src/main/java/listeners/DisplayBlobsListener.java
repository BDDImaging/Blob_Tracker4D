package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import snakes.InteractiveActiveContour_;
import snakes.InteractiveActiveContour_.ValueChange;

public class DisplayBlobsListener implements ActionListener {
	
	
    InteractiveActiveContour_ parent;
	
	
		public DisplayBlobsListener(final InteractiveActiveContour_ parent){
		
			this.parent = parent;
		}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		parent.showDOG = true;
		parent.updatePreview(ValueChange.SHOWDOG);

	}
}