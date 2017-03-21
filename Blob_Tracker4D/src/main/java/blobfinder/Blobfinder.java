package blobfinder;


import java.util.ArrayList;

import graphconstructs.Logger;


import net.imglib2.algorithm.OutputAlgorithm;
import snakes.SnakeObject;


/**
 * 
 * links objects across multiple frames in time-lapse images, Creates a new graph from a list of blobs, the blob properties of the current frame
 * are enumerated in the static properties
 * @author varunkapoor
 *
 */


public interface Blobfinder extends OutputAlgorithm< ArrayList<SnakeObject> >
	{
		/**
		 * Sets the {@link Logger} instance that will receive messages from this
		 * {@link SpotTracker}.
		 *
		 * @param logger
		 *            the logger to echo messages to.
		 */
		public void setLogger( final Logger logger );
	}
	