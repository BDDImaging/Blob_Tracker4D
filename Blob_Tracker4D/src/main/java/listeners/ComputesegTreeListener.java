package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import snakes.InteractiveActiveContour_;
import snakes.InteractiveActiveContour_.ValueChange;



	
	public class ComputesegTreeListener implements ActionListener {

            InteractiveActiveContour_ parent;
		
		
		public ComputesegTreeListener(final InteractiveActiveContour_ parent){
		
			this.parent = parent;
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			parent.showSegMSER = true;
			parent.updatePreview(ValueChange.SHOWSEGMSER);

		}
	
	
}
