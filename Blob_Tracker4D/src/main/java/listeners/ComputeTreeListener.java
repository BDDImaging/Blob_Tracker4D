package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import snakes.InteractiveActiveContour_;
import snakes.InteractiveActiveContour_.ValueChange;

public class ComputeTreeListener implements ActionListener {
	
final InteractiveActiveContour_ parent;
	
	public ComputeTreeListener (final InteractiveActiveContour_  parent ){
		
		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		parent.showMSER = true;

		parent.updatePreview(ValueChange.SHOWMSER);

	}
}