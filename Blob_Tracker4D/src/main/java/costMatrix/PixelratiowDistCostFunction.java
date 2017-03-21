package costMatrix;

import snakes.SnakeObject;

public class PixelratiowDistCostFunction implements CostFunction< SnakeObject, SnakeObject >
	{

	
	// Alpha is the weightage given to distance and Beta is the weightage given to the ratio of pixels
		public final double beta;
		public final double alpha;
		
		
	
		
		public double getAlpha(){
			
			return alpha;
		}
		
	  
		public double getBeta(){
			
			return beta;
		}

		public PixelratiowDistCostFunction (double alpha, double beta){
			
			this.alpha = alpha;
			this.beta = beta;
			
		}
		
		
	@Override
	public double linkingCost( final SnakeObject source, final SnakeObject target )
	{
		return source.NormalizedPixelratioandDistanceTo(target, alpha, beta);
	}
		


	
	
	
}
