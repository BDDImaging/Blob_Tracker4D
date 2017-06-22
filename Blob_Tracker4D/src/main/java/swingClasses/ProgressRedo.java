package swingClasses;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import blobfinder.BlobfinderInteractiveSnake;
import ij.IJ;
import ij.gui.Roi;
import snakes.InteractiveActiveContour_;
import snakes.SnakeObject;
import snakes.InteractiveActiveContour_.ValueChange;

public class ProgressRedo extends SwingWorker<Void, Void>  {
	
	
	  final InteractiveActiveContour_ parent;
		
		
		public ProgressRedo(final InteractiveActiveContour_ parent){
		
			this.parent = parent;
		}
	

		@Override
		protected Void doInBackground() throws Exception {

			parent.AllSliceSnakes = new ArrayList<ArrayList<SnakeObject>>();

			parent.All3DSnakes.clear();
			parent.All3DSnakescopy.clear();
			parent.DialogueRedo();

			parent.thirdDimensionslider = parent.thirdDimension;
			parent.fourthDimensionslider = parent.fourthDimension;
			parent.CurrentView = parent.getCurrentView();
			parent.otherCurrentView = parent.getotherCurrentView();
			parent.updatePreview(ValueChange.THIRDDIMTrack);
			BlobfinderInteractiveSnake snake;
			parent.putFeature(parent.SNAKEPROGRESS, Double.valueOf(parent.AllSliceSnakes.size()));
			if (parent.NearestNeighbourRois.size() > 0)
				snake = new BlobfinderInteractiveSnake(parent,parent.CurrentView, parent.otherCurrentView, parent.NearestNeighbourRois, parent.RadiusMeasure, parent.usefolder, parent.addTrackToName, parent.thirdDimensionslider, parent.fourthDimensionslider, parent.TrackinT, parent.jpb,
						parent.thirdDimensionSize);
			else

				snake = new BlobfinderInteractiveSnake(parent,parent.CurrentView, parent.otherCurrentView, parent.Rois, parent.RadiusMeasure, parent.usefolder,
						parent.addTrackToName, parent.thirdDimensionslider, parent.fourthDimensionslider, parent.TrackinT, parent.jpb, parent.thirdDimensionSize);

			snake.process();

			parent.usefolder = snake.getFolder();
			parent.addTrackToName = snake.getFile();
			parent.currentsnakes = snake.getResult();
			parent.finalRois = snake.getfinalRois();
			parent.snakeoverlay = snake.getABsnake();

			
				int z = 1;
			

				ArrayList<Roi> AllRois = new ArrayList<Roi>();
				ArrayList<Roi> AllmeasureRois = new ArrayList<Roi>();

				for (int i = 0; i < parent.snakeoverlay.size(); ++i) {

					parent.snakeoverlay.get(i).DrawSnake(parent.cp, parent.colorDraw, 1);

					Roi normalroi = parent.snakeoverlay.get(i).createRoi();

					AllRois.add(normalroi);
					
					final Roi Bigroi = util.Boundingboxes.CreateBigRoi(normalroi, parent.currentimg, parent.RadiusMeasure);
					
					AllmeasureRois.add(Bigroi);
				}

		

			

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

			parent.All3DSnakes.addAll(parent.AllSliceSnakes);
			parent.All3DSnakescopy.addAll(parent.AllSliceSnakes);
			

			parent.AllSnakerois.put(parent.thirdDimension, AllRois);
			parent.AllSnakeMeasurerois.put(parent.thirdDimension, AllmeasureRois);
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
