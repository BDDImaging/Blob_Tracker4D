package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import snakes.InteractiveActiveContour_;
import snakes.InteractiveActiveContour_.ValueChange;



	
	public class DisplaysegBlobsListener implements ActionListener {

		InteractiveActiveContour_ parent;
		
		
		public DisplaysegBlobsListener(final InteractiveActiveContour_ parent){
		
			this.parent = parent;
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			parent.showSegDOG = true;
			parent.updatePreview(ValueChange.SHOWSEGDOG);

		}
	}
	

