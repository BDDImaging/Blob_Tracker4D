package blobfinder;

import java.util.ArrayList;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import snakes.SnakeObject;

public class FindblobsVia {

	
	
	public static enum BlobfindingMethod {
		
		MSER, DOG;
		
		
	}
	
	protected BlobfindingMethod MSER, DOG;
	
	
	
	public static ArrayList<SnakeObject> BlobfindingMethod(final Blobfinder blobfinder){
		
		ArrayList<SnakeObject> probblobs = null;
		
		blobfinder.checkInput();
		blobfinder.process();
		probblobs = blobfinder.getResult();
		
		
		return probblobs;
		
	}
	
	
	
}
