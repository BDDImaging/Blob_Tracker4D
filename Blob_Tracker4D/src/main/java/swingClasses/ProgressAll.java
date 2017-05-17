package swingClasses;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import blobfinder.BlobfinderInteractiveSnake;
import fiji.tool.SliceObserver;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.frame.RoiManager;
import snakes.InteractiveActiveContour_;
import snakes.SnakeObject;

import snakes.InteractiveActiveContour_.ValueChange;


public class ProgressAll extends SwingWorker<Void, Void>  {
	
	
        final InteractiveActiveContour_ parent;
	
	
	public ProgressAll(final InteractiveActiveContour_ parent){
	
		this.parent = parent;
	}
	
	
	
	@Override
	protected Void doInBackground() throws Exception {

		// add listener to the imageplus slice slider

		parent.All3DSnakes.clear();
		parent.All3DSnakescopy.clear();
		parent.Dialoguesec();
		int next = parent.thirdDimension;
		int nextZ = parent.fourthDimension;
		if (parent.snakestack != null) {
			for (int index = 1; index < parent.snakestack.size(); ++index) {

				parent.snakestack.deleteSlice(index);
			}
		}

		if (parent.measuresnakestack != null) {
			for (int index = 1; index < parent.measuresnakestack.size(); ++index) {

				parent.measuresnakestack.deleteSlice(index);
			}
		}
		parent.putFeature(parent.SNAKEPROGRESS, Double.valueOf(parent.AllSliceSnakes.size()));
		// Run snakes over a frame for each slice in that frame
		for (int indexx = next; indexx <= parent.fourthDimensionSize; ++indexx) {
			parent.snakestack = new ImageStack((int) parent.originalimgA.dimension(0), (int) parent.originalimgA.dimension(1),
					java.awt.image.ColorModel.getRGBdefault());
			parent.measuresnakestack = new ImageStack((int) parent.originalimgB.dimension(0), (int) parent.originalimgB.dimension(1),
					java.awt.image.ColorModel.getRGBdefault());

			parent.thirdDimension = indexx;
			parent.thirdDimensionslider = parent.thirdDimension;

			parent.AllSliceSnakes = new ArrayList<ArrayList<SnakeObject>>();

			parent.CurrentView = parent.getCurrentView();
			parent.otherCurrentView = parent.getotherCurrentView();

			parent.updatePreview(ValueChange.THIRDDIMTrack);

			for (int z = nextZ; z <= parent.thirdDimensionSize; ++z) {

				parent.fourthDimension = z;
				parent.fourthDimensionslider = parent.fourthDimension;

				parent.CurrentView = parent.getCurrentView();
				parent.otherCurrentView = parent.getotherCurrentView();

				parent.updatePreview(ValueChange.FOURTHDIMTrack);
				parent.imp.getCanvas().addMouseListener(parent.roiListener);

				BlobfinderInteractiveSnake snake;
				if (parent.NearestNeighbourRois.size() > 0)
					snake = new BlobfinderInteractiveSnake(parent.CurrentView, parent.otherCurrentView, parent.NearestNeighbourRois,
							parent.sizeX, parent.sizeY, parent.usefolder, parent.addTrackToName, z, indexx, parent.TrackinT, parent.jpb, parent.thirdDimensionSize);
				else

					snake = new BlobfinderInteractiveSnake(parent.CurrentView, parent.otherCurrentView, parent.Rois, parent.sizeX, parent.sizeY,
							parent.usefolder, parent.addTrackToName, z, indexx, parent.TrackinT, parent.jpb, parent.thirdDimensionSize);

				RoiManager manager = RoiManager.getInstance();
				if (manager != null) {
					manager.getRoisAsArray();
				}

				parent.isStarted = true;

				if (parent.Auto) {
					if (indexx > next || z > nextZ)
						snake.Auto = true;
				}
				snake.checkInput();

				snake.process();

				parent.usefolder = snake.getFolder();
				parent.addTrackToName = snake.getFile();

				parent.finalRois = snake.getfinalRois();
				parent.currentsnakes = snake.getResult();

				parent.snakeoverlay = snake.getABsnake();

				

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
				IJ.log(" " + parent.AllSliceSnakes.size());

			} // Z loop closing
				// Make KD tree to link objects along Z axis

			ArrayList<SnakeObject> ThreedimensionalSnake = parent.getCentreofMass3D();

			parent.All3DSnakes.add(ThreedimensionalSnake);
			parent.All3DSnakescopy.add(ThreedimensionalSnake);
			new ImagePlus("Snakes", parent.snakestack).draw();
			new ImagePlus("Measure", parent.measuresnakestack).draw();
		} // t loop closing
		IJ.log("SnakeList Size" +parent.All3DSnakes.size());

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
