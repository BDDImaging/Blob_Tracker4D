package swingClasses;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import blobfinder.BlobfinderInteractiveSnake;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ColorProcessor;
import snakes.InteractiveActiveContour_;
import snakes.SnakeObject;
import snakes.InteractiveActiveContour_.ValueChange;

public class Progress extends SwingWorker<Void, Void> {
	
	
	  final InteractiveActiveContour_ parent;
		
		
			public Progress(final InteractiveActiveContour_ parent){
			
				this.parent = parent;
			}
		

			@Override
			protected Void doInBackground() throws Exception {

				parent.AllSliceSnakes = new ArrayList<ArrayList<SnakeObject>>();

				parent.thirdDimensionslider = parent.thirdDimension;
				parent.fourthDimensionslider = parent.fourthDimension;
				parent.CurrentView = parent.getCurrentView();
				parent.otherCurrentView = parent.getotherCurrentView();
				parent.updatePreview(ValueChange.THIRDDIM);
				parent.putFeature(parent.SNAKEPROGRESS, Double.valueOf(parent.AllSliceSnakes.size()));

				BlobfinderInteractiveSnake snake;
				if (parent.NearestNeighbourRois.size() > 0)
					snake = new BlobfinderInteractiveSnake(parent.CurrentView, parent.otherCurrentView, parent.NearestNeighbourRois, parent.sizeX,
							parent.sizeY, parent.usefolder, parent.addTrackToName, parent.thirdDimensionslider, parent.fourthDimensionslider, parent.TrackinT, parent.jpb,
							parent.thirdDimensionSize);
				else

					snake = new BlobfinderInteractiveSnake(parent.CurrentView, parent.otherCurrentView, parent.Rois, parent.sizeX, parent.sizeY, parent.usefolder,
							parent.addTrackToName, parent.thirdDimensionslider, parent.fourthDimensionslider, parent.TrackinT, parent.jpb, parent.thirdDimensionSize);
				parent.jpb.setIndeterminate(false);
				snake.process();
				parent.usefolder = snake.getFolder();
				parent.addTrackToName = snake.getFile();
				parent.jpb.getValue();

				parent.currentsnakes = snake.getResult();
				parent.finalRois = snake.getfinalRois();
				parent.snakeoverlay = snake.getABsnake();

				

				int z = 1 ;
				parent.snakestack.addSlice(parent.imp.getImageStack().getProcessor(z).convertToRGB());
				parent.measuresnakestack.addSlice(parent.measureimp.getImageStack().getProcessor(z).convertToRGB());

			 parent.cp = (ColorProcessor) (parent.snakestack.getProcessor(z).duplicate());
				parent.measurecp = (ColorProcessor) (parent.measuresnakestack.getProcessor(z).duplicate());


					

					ArrayList<Roi> AllRois = new ArrayList<Roi>();
					ArrayList<Roi> AllmeasureRois = new ArrayList<Roi>();
					parent.overlay.clear();
					
					for (int i = 0; i < parent.snakeoverlay.size(); ++i) {
						Roi normalroi = parent.snakeoverlay.get(i).createRoi();
						parent.overlay.add(normalroi);
					}
					
					for (int i = 0; i < parent.snakeoverlay.size(); ++i) {

						parent.snakeoverlay.get(i).DrawSnake(parent.cp, snake.colorDraw, 1);

						Roi normalroi = parent.snakeoverlay.get(i).createRoi();

						AllRois.add(normalroi);
						
						final Roi Bigroi = util.Boundingboxes.CreateBigRoi(normalroi, parent.currentimg, parent.sizeX, parent.sizeY);
						
						AllmeasureRois.add(Bigroi);
						parent.measurecp.setColor(parent.colorBigDraw);
						parent.measurecp.setLineWidth(1);
						parent.measurecp.draw(Bigroi);
						

					}
					parent.snakestack.setPixels(parent.cp.getPixels(), z);
					parent.measuresnakestack.setPixels(parent.measurecp.getPixels(), z);

				if (parent.All3DSnakes != null) {

					for (int Listindex = 0; Listindex < parent.All3DSnakes.size(); ++Listindex) {

						SnakeObject SnakeFrame = parent.All3DSnakes.get(Listindex).get(0);
						int SnakeThirdDim = SnakeFrame.thirdDimension;
						int SnakeFourthDim = SnakeFrame.fourthDimension;

						if (SnakeThirdDim == parent.thirdDimension && SnakeFourthDim == parent.fourthDimension) {
							parent.All3DSnakes.remove(Listindex);
							parent.All3DSnakescopy.remove(Listindex);
							IJ.log(" Recomputing snakes for currentView");

						}
					}

				}

				parent.AllSliceSnakes.add(parent.currentsnakes);

				// The graph for this frame along Z is now complete, generate 3D
				// snake properties

				ArrayList<SnakeObject> ThreedimensionalSnake = parent.getCentreofMass3D();

				parent.AllSnakerois.put(parent.thirdDimension, AllRois);
				parent.AllSnakeMeasurerois.put(parent.thirdDimension, AllmeasureRois);
				
				
				parent.All3DSnakes.add(ThreedimensionalSnake);
				parent.All3DSnakescopy.add(ThreedimensionalSnake);
				parent.updatePreview(ValueChange.THIRDDIM);
				IJ.log("SnakeList Size" + parent.All3DSnakes.size());

				return null;
			}

			@Override
			protected void done() {
				try {
					parent.jpb.setIndeterminate(false);
					get();
					parent.frame.dispose();
					JOptionPane.showMessageDialog(parent.jpb.getParent(), "Success", "Success", JOptionPane.INFORMATION_MESSAGE);
				} catch (ExecutionException | InterruptedException e) {
					e.printStackTrace();
				}

			}
}
