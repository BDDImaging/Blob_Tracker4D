package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import blobfinder.BlobfinderInteractiveDoG;
import blobfinder.BlobfinderInteractiveMSER;
import blobfinder.FindblobsVia;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import snakes.InteractiveActiveContour_;
import snakes.InteractiveActiveContour_.ValueChange;

public class FindBlobsListener implements ActionListener {

	
	InteractiveActiveContour_ parent;
	 
	 public FindBlobsListener(final InteractiveActiveContour_ parent){
			
			this.parent = parent;
		}
	 
	 @Override
		public void actionPerformed(final ActionEvent arg0) {

			RandomAccessibleInterval<FloatType> groundframe = parent.currentimg;

			if (parent.findBlobsViaMSER) {

				parent.updatePreview(ValueChange.SHOWMSER);

				BlobfinderInteractiveMSER newblobMser = new BlobfinderInteractiveMSER(parent.currentimg,parent.othercurrentimg,
						parent.newtree, parent.RadiusMeasure, parent.thirdDimension, parent.fourthDimension);

				parent.ProbBlobs = FindblobsVia.BlobfindingMethod(newblobMser);

			}

			if (parent.findBlobsViaDOG) {

				parent.updatePreview(ValueChange.SHOWDOG);

				BlobfinderInteractiveDoG newblobDog = new BlobfinderInteractiveDoG(parent.currentimg, parent.othercurrentimg,
						parent.lookForMaxima, parent.lookForMinima, parent.sigma, parent.sigma2, parent.peaks, parent.thirdDimension, parent.fourthDimension);

				parent.ProbBlobs = FindblobsVia.BlobfindingMethod(newblobDog);

			}

		}
	 
	
}
