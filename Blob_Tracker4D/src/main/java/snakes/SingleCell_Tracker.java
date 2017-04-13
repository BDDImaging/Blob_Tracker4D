package snakes;

import javax.swing.JFrame;

import ij.ImageJ;
import ij.plugin.PlugIn;

public class SingleCell_Tracker implements PlugIn {

	@Override
	public void run(String arg) {
		
			new ImageJ();
			

			    JFrame frame = new JFrame("");
			    SimpleFileChooser panel = new SimpleFileChooser();
			  
			    frame.getContentPane().add(panel,"Center");
			    frame.setSize(panel.getPreferredSize());
			    
		}
		
	
	
	

}
