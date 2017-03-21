package snakes;

import java.util.ArrayList;

import ij.gui.Roi;

public class ComSnake {

	public final int thirdDimension;
	public final int fourthDimension;
	public final double[] com;
	public final Roi rois;
	
	
	public ComSnake(final int thirdDimension, final int fourthDimension, final double[] com, final Roi rois){
		
		this.thirdDimension = thirdDimension;
		this.fourthDimension = fourthDimension;
		this.com = com;
		this.rois = rois;
		
	}
	
	
	
}
