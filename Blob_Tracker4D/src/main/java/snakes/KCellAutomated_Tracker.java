package snakes;

import javax.swing.JFrame;

import ij.ImageJ;
import ij.plugin.PlugIn;

public class KCellAutomated_Tracker implements PlugIn {

	@Override
	public void run(String arg) {
		
			new ImageJ();
			// SpimData2 d = loadSpimData( new File(
			// "/home/preibisch/Documents/Microscopy/SPIM/Drosophila
			// MS2-GFP//dataset.xml" ) );
			// getImg( d, 0, 50 );
			// 3D mCherry-test.tif
			// 4D one_division_4d-smallz.tif

			    JFrame frame = new JFrame("");
			    FileChooser panel = new FileChooser();
			  
			    frame.getContentPane().add(panel,"Center");
			    frame.setSize(panel.getPreferredSize());
			    
		}
		
	
	
	

}
