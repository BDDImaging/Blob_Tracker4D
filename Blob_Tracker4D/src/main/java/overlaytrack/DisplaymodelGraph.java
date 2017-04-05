package overlaytrack;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.Subgraph;

import fiji.tool.SliceListener;
import fiji.tool.SliceObserver;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Line;
import ij.gui.Overlay;
import math3d.Point3d;
import net.imagej.DrawingTool;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import snakes.SnakeObject;
import trackerType.TrackModel;

public class DisplaymodelGraph {
	// add listener to the imageplus slice slider
	private ImagePlus imp;
	private final SimpleWeightedGraph<SnakeObject, DefaultWeightedEdge> graph;
	private final int ndims;
	final Color colorDraw;
	final boolean displayAll;
	final int displayindex;
	
	public DisplaymodelGraph(final ImagePlus imp, SimpleWeightedGraph<SnakeObject, DefaultWeightedEdge> graph,
			Color colorDraw, boolean displayAll, int displayindex){
		
		this.imp = imp;
		this.graph = graph;
		this.colorDraw = colorDraw;
		ndims = imp.getNDimensions();
		this.displayAll = displayAll;
		this.displayindex = displayindex;
		
		// add listener to the imageplus slice slider
				SliceObserver sliceObserver = new SliceObserver( imp, new ImagePlusListener() );
	}
	
	
	public ImagePlus getImp() { return this.imp; } 
	
	
	protected  class ImagePlusListener implements SliceListener
	{
		@Override
		public void sliceChanged(ImagePlus arg0)
		{
			
			
			TrackModel model = new TrackModel(graph);
			model.getDirectedNeighborIndex();
			
			imp.show();
			Overlay o = imp.getOverlay();
			
			if( getImp().getOverlay() == null )
			{
				o = new Overlay();
				getImp().setOverlay( o ); 
			}

			o.clear();
			getImp().getOverlay().clear();
			
			
			for (final Integer id : model.trackIDs(true)) {

				// Get the corresponding set for each id
				model.setName(id, "Track" + id);
				final HashSet<SnakeObject> Snakeset = model.trackSnakeObjects(id);
				ArrayList<SnakeObject> list = new ArrayList<SnakeObject>();

				Comparator<SnakeObject> ThirdDimcomparison = new Comparator<SnakeObject>() {

					@Override
					public int compare(final SnakeObject A, final SnakeObject B) {

						return A.thirdDimension - B.thirdDimension;

					}

				};

				Comparator<SnakeObject> FourthDimcomparison = new Comparator<SnakeObject>() {

					@Override
					public int compare(final SnakeObject A, final SnakeObject B) {

						return A.fourthDimension - B.fourthDimension;

					}

				};

				Iterator<SnakeObject> Snakeiter = Snakeset.iterator();
				while (Snakeiter.hasNext()) {

					SnakeObject currentsnake = Snakeiter.next();

					for (int d = 0; d < imp.getNDimensions(); ++d)
						if (currentsnake.centreofMass[d] != Double.NaN)
							list.add(currentsnake);

				}
				Collections.sort(list, ThirdDimcomparison);
				if (imp.getNDimensions() > 3)
					Collections.sort(list, FourthDimcomparison);
				
				
				for (DefaultWeightedEdge e : model.edgeSet()) {
					
			        SnakeObject Spotbase = model.getEdgeSource(e);
			        SnakeObject Spottarget = model.getEdgeTarget(e);
			        
			        
			       
			        final double[] startedge = new double[ndims];
			        final double[] targetedge = new double[ndims];
			        for (int d = 0; d < ndims - 1; ++d){
			        	
			        	startedge[d] = Spotbase.centreofMass[d];
			        	
			        	targetedge[d] = Spottarget.centreofMass[d];
			        	
			        }
			        
				
				if (displayAll){
				
                Line newellipse = new Line(list.get(0).centreofMass[0], list.get(0).centreofMass[1], list.get(0).centreofMass[0], list.get(0).centreofMass[1]);
				

				newellipse.setStrokeColor(Color.WHITE);
				newellipse.setStrokeWidth(1);
				newellipse.setName("TrackID: " + id);
				
				o.add(newellipse);
				o.drawLabels(true);
				
				o.drawNames(true);
				
				
				  
		        Line newline = new Line(startedge[0], startedge[1], targetedge[0], targetedge[1]);
				newline.setStrokeColor(colorDraw);
				newline.setStrokeWidth(graph.degreeOf(Spottarget));

			
				o.add(newline);
				
				}
				
				else{
					
					
						if (id == displayindex - 1){
						  Line newellipse = new Line(list.get(0).centreofMass[0], list.get(0).centreofMass[1], list.get(0).centreofMass[0], list.get(0).centreofMass[1]);
							

							newellipse.setStrokeColor(Color.WHITE);
							newellipse.setStrokeWidth(1);
							newellipse.setName("TrackID: " + id);
							
							o.add(newellipse);
							o.drawLabels(true);
							
							o.drawNames(true);
						}
							if ( model.trackIDOf(Spotbase) == displayindex - 1){
							  
					        Line newline = new Line(startedge[0], startedge[1], targetedge[0], targetedge[1]);
							newline.setStrokeColor(colorDraw);
							newline.setStrokeWidth(graph.degreeOf(Spottarget));

						
							o.add(newline);
						
					}
					
					
					
					
				}
				
				
			}
			
			}
			imp.updateAndDraw();
		}		
	}
	
}
