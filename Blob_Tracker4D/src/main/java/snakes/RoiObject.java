package snakes;

import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import ij.gui.Roi;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;

public class RoiObject  {

	
	/*
	 * FIELDS
	 */

	
	/**
	 * @param Framenumber
	 *            the current frame
	 * 
	 * @param Label
	 *            the label of the blob
	 * @param centreofMass
	 *            the co-ordinates for center of mass of the current blobs
	 * @param IntensityCherry
	 *            the total intensity of the blob in mCherrs.
	 * @param IntensityBio
	 *            the total intensity of the blob in Luciferas.
	 * 
	 */
	public final int Framenumber;
	public final int Slicenumber;
	public final int Label;
	public final Roi roi;
	
	
	public RoiObject( final int Framenumber, final int Slicenumber, final int Label, final Roi roi)
	{

		    this.Framenumber = Framenumber;
			this.Slicenumber = Slicenumber;
			this.Label = Label;
			this.roi = roi;
			
			
		
	}
	
	
	
	
	
	
	
	
	
}
