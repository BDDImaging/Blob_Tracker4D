package kdTreeBlobs;

import net.imglib2.KDTree;
import net.imglib2.KDTreeNode;
import net.imglib2.RealLocalizable;
import net.imglib2.Sampler;
import net.imglib2.neighborsearch.NearestNeighborSearch;

public class NNFlagsearchKDtree<T>  implements NearestNeighborSearch< FlagNode<T> > {

protected KDTree< FlagNode<T>  > tree;
	
	protected final int n;
	protected final double[] pos;

	protected KDTreeNode< FlagNode<T> > bestPoint;
	protected double bestSquDistance;
	
	public NNFlagsearchKDtree( KDTree< FlagNode<T>  > tree ) {
		n = tree.numDimensions();
		pos = new double[ n ];
		this.tree = tree;
	}
	
	@Override
	public int numDimensions()	{
		return n;
	}
	
	@Override
	public void search( RealLocalizable p ) {
		p.localize( pos );
		bestSquDistance = Double.MAX_VALUE;
		searchNode( tree.getRoot() );
	}
	
	protected void searchNode( KDTreeNode< FlagNode<T>  > current )	{
		// consider the current node
		final double distance = current.squDistanceTo( pos );
		boolean visited = current.get().isVisited();
		if ( distance < bestSquDistance && !visited  ) {
			bestSquDistance = distance;
			bestPoint = current;
		}
		
		final double axisDiff = pos[ current.getSplitDimension() ] - current.getSplitCoordinate();
		final double axisSquDistance = axisDiff * axisDiff;
		final boolean leftIsNearBranch = axisDiff < 0;

		// search the near branch
		final KDTreeNode< FlagNode<T> > nearChild = leftIsNearBranch ? current.left : current.right;
		final KDTreeNode< FlagNode<T> > awayChild = leftIsNearBranch ? current.right : current.left;
		if ( nearChild != null )
			searchNode( nearChild );

	    // search the away branch - maybe
		if ( ( axisSquDistance <= bestSquDistance ) && ( awayChild != null ) )
			searchNode( awayChild );
	}

	@Override
	public Sampler<FlagNode<T>>  getSampler() {
		return bestPoint;
	}

	@Override
	public RealLocalizable getPosition() {
		return bestPoint;
	}

	@Override
	public double getSquareDistance() {
		return bestSquDistance;
	}

	@Override
	public double getDistance() {
		return Math.sqrt( bestSquDistance );
	}
	
	@Override
	public NNFlagsearchKDtree<T> copy() {
		final NNFlagsearchKDtree<T> copy = new NNFlagsearchKDtree<T>( tree );
		System.arraycopy( pos, 0, copy.pos, 0, pos.length );
		copy.bestPoint = bestPoint;
		copy.bestSquDistance = bestSquDistance;
		return copy;
	}
	
	
}
