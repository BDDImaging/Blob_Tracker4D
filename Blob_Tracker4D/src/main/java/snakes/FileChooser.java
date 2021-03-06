package snakes;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import ij.IJ;
import ij.ImagePlus;
import ij.io.Opener;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;

import java.awt.event.*;
import java.awt.*;
import java.util.*;

public class FileChooser extends JPanel {
	boolean wasDone = false;
	boolean isFinished = false;
	JButton Track;
	JButton Measure;
	JButton Done;
	JFileChooser chooserA;
	String choosertitleA;

	JFileChooser chooserB;
	String choosertitleB;

	public FileChooser() {
		final JFrame frame = new JFrame("Welcome to KCell Automated Tracker");
		frame.setSize(800, 300);

		/* Instantiation */
		final GridBagLayout layout = new GridBagLayout();
		final GridBagConstraints c = new GridBagConstraints();

		final Label LoadtrackText = new Label("Load image for tracking (Optional, Pre-processed image to make object recognition easy)");
		final Label LoadMeasureText = new Label("Load UN-Preprocessed image ");
		final Label StartText = new Label("Start Blob Finding and tracking");
		final Checkbox usesame = new Checkbox("Use Unpreprocessed image");
		LoadtrackText.setBackground(new Color(1, 0, 1));
		LoadtrackText.setForeground(new Color(255, 255, 255));
		
		LoadMeasureText.setBackground(new Color(1, 0, 1));
		LoadMeasureText.setForeground(new Color(255, 255, 255));
		
		StartText.setBackground(new Color(1, 0, 1));
		StartText.setForeground(new Color(255, 255, 255));
		
		Track = new JButton("Open Preprocessed image");
		Measure = new JButton("Open UN-Preprocessed image");

		/* Location */
		frame.setLayout(layout);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 4;
		c.weighty = 1.5;

		++c.gridy;
		c.insets = new Insets(0, 170, 0, 75);
		frame.add(LoadMeasureText, c);

		++c.gridy;
		++c.gridy;
		++c.gridy;
		++c.gridy;
		c.insets = new Insets(0, 170, 0, 75);
		frame.add(Measure, c);
		
		++c.gridy;
		c.insets = new Insets(0, 170, 0, 75);
		frame.add(LoadtrackText, c);
		

		++c.gridy;
		++c.gridy;
		++c.gridy;
		
		++c.gridy;
		++c.gridy;
		c.insets = new Insets(0, 170, 0, 75);
		frame.add(usesame, c);
		
		++c.gridy;
		c.insets = new Insets(0, 170, 0, 75);
		frame.add(Track, c);
		
		


		

		Track.addActionListener(new UploadTrackListener(frame));
		Measure.addActionListener(new MeasureListener(frame));
		usesame.addItemListener(new UsesameimageListener(frame));
		frame.addWindowListener(new FrameListener(frame));
		frame.setVisible(true);

	}

	protected class FrameListener extends WindowAdapter {
		final Frame parent;

		public FrameListener(Frame parent) {
			super();
			this.parent = parent;
		}

		@Override
		public void windowClosing(WindowEvent e) {
			close(parent);
		}
	}

	protected class UsesameimageListener implements ItemListener {
		
		final Frame parent;
		
		public UsesameimageListener(Frame parent){
			
			this.parent = parent;
			
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			
			Done(parent, wasDone);
			
		}
		
		
	}
	protected class UploadTrackListener implements ActionListener {

		final Frame parent;

		public UploadTrackListener(Frame parent) {

			this.parent = parent;

		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {

			int result;

			chooserA = new JFileChooser();
			chooserA.setCurrentDirectory(chooserB.getCurrentDirectory());
			chooserA.setDialogTitle(choosertitleA);
			chooserA.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			//
			// disable the "All files" option.
			//
			chooserA.setAcceptAllFileFilterUsed(false);
			 FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "tif");
				
				chooserA.setFileFilter(filter);
			//
			if (chooserA.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
				System.out.println("getCurrentDirectory(): " + chooserA.getCurrentDirectory());
				System.out.println("getSelectedFile() : " + chooserA.getSelectedFile());
			} else {
				System.out.println("No Selection ");
			}
			Done(parent, wasDone);	
		}

	}

	protected class MeasureListener implements ActionListener {

		final Frame parent;

		public MeasureListener(Frame parent) {

			this.parent = parent;

		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {

			int result;

			chooserB = new JFileChooser();
			chooserB.setCurrentDirectory(new java.io.File("."));
			chooserB.setDialogTitle(choosertitleB);
			chooserB.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			//
			// disable the "All files" option.
			//
			chooserB.setAcceptAllFileFilterUsed(false);
			 FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "tif");
				
				chooserB.setFileFilter(filter);
			//
			if (chooserB.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
				System.out.println("getCurrentDirectory(): " + chooserB.getCurrentDirectory());
				System.out.println("getSelectedFile() : " + chooserB.getSelectedFile());
			} else {
				System.out.println("No Selection ");
			}
			
		}

	}

	public void Done(Frame parent, final boolean Done){
		
		
		wasDone = Done;
		
	
		
		ImagePlus impB =	new Opener().openImage(chooserB.getSelectedFile().getPath());

		
		ImagePlus impA = impB;
		
		if (chooserA!=null)
			impA = new Opener().openImage(chooserA.getSelectedFile().getPath());
		
		// Tracking is done with imageA measurment is performed on both the
		// images
       
		    
		RandomAccessibleInterval<FloatType> originalimgA = ImageJFunctions.convertFloat(impA);
		RandomAccessibleInterval<FloatType> originalimgB = ImageJFunctions.convertFloat(impB);
		
		new InteractiveActiveContour_(originalimgA, originalimgB, chooserB.getSelectedFile()).run(null);
		close(parent);
	}
	
	

	protected final void close(final Frame parent) {
		if (parent != null)
			parent.dispose();

		isFinished = true;
	}

	public Dimension getPreferredSize() {
		return new Dimension(800, 300);
	}

}
