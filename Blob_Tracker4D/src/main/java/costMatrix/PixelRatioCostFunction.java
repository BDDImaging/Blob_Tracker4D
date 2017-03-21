package costMatrix;

import snakes.SnakeObject;

public class PixelRatioCostFunction implements CostFunction< SnakeObject, SnakeObject > {

	
	
	/**
	 * Implementation of various cost functions
	 * 
	 * 
	 */

	
		@Override
		public double linkingCost( final SnakeObject source, final SnakeObject target )
		{
			return source.numberofPixelsRatioTo(target);
		}
		
		
		
		

	
	
}
