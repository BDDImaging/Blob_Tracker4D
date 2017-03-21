package costMatrix;

import snakes.SnakeObject;

public class IntensityDiffCostFunction implements CostFunction< SnakeObject, SnakeObject >
	{

		
	

	@Override
	public double linkingCost( final SnakeObject source, final SnakeObject target )
	{
		return source.IntensityDistanceTo(target );
	}
		

	
}
