package listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import snakes.InteractiveActiveContour_.ValueChange;
import snakes.InteractiveSingleCell_;

/**
 * Updates when mouse is released
 * 
 * @author spreibi
 *
 */
public class SingleStandardMouseListener implements MouseListener
{
	final InteractiveSingleCell_ parent;
	final ValueChange change;

	public SingleStandardMouseListener( final InteractiveSingleCell_  parent, final ValueChange change )
	{
		this.parent = parent;
		this.change = change;
	}
	
	

	@Override
	public void mouseReleased( MouseEvent arg0 )
	{
		
		
			try { Thread.sleep( 10 ); } catch ( InterruptedException e ) {}
		

		parent.updatePreview(change);
	}

	@Override
	public void mousePressed( MouseEvent arg0 ){}

	@Override
	public void mouseExited( MouseEvent arg0 ) {}

	@Override
	public void mouseEntered( MouseEvent arg0 ) {}

	@Override
	public void mouseClicked( MouseEvent arg0 ) {}
}
