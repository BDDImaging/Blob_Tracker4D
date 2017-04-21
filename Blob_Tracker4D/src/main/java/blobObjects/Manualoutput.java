package blobObjects;

public class Manualoutput {

	
	public final String CellName;
	public final int Framenumber;
	public final double[] location;
	public final double NumberofPixels;
	public final double Intensity;
	public final double MeanIntensity;
	public final float Radius;
	
	public Manualoutput(final String CellName, final int Framenumber, final double[] location, final double Intensity, final double NumberofPixels, final double MeanIntensity, final float Radius){
		
		this.CellName = CellName;
		this.Framenumber = Framenumber;
		this.location = location;
		this.Intensity = Intensity;
		this.NumberofPixels = NumberofPixels;
		this.MeanIntensity = MeanIntensity;
		this.Radius = Radius;
		
		
		
		
		
	}
	
	
}
