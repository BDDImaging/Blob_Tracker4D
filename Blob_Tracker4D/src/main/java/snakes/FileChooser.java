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
		final JFrame frame = new JFrame("Welcome to Karmen Tracker");
		frame.setSize(800, 300);

		/* Instantiation */
		final GridBagLayout layout = new GridBagLayout();
		final GridBagConstraints c = new GridBagConstraints();

		final Label LoadtrackText = new Label("Load image for tracking");
		final Label LoadMeasureText = new Label("Load image for measurment");
		final Label StartText = new Label("Start Blob Finding and tracking");
		LoadtrackText.setBackground(new Color(1, 0, 1));
		LoadtrackText.setForeground(new Color(255, 255, 255));
		
		LoadMeasureText.setBackground(new Color(1, 0, 1));
		LoadMeasureText.setForeground(new Color(255, 255, 255));
		
		StartText.setBackground(new Color(1, 0, 1));
		StartText.setForeground(new Color(255, 255, 255));
		
		Track = new JButton("Upload Tracking image");
		Measure = new JButton("Upload image for performing measurments");
		Done = new JButton("Done");

		/* Location */
		frame.setLayout(layout);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 4;
		c.weighty = 1.5;

		
		++c.gridy;
		c.insets = new Insets(0, 170, 0, 75);
		frame.add(LoadtrackText, c);
		

		++c.gridy;
		++c.gridy;
		++c.gridy;
		
		
		++c.gridy;
		c.insets = new Insets(0, 170, 0, 75);
		frame.add(Track, c);
		
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
		frame.add(StartText, c);

		++c.gridy;
		++c.gridy;
		++c.gridy;
		++c.gridy;
		c.insets = new Insets(0, 170, 0, 75);
		frame.add(Done, c);

		Track.addActionListener(new UploadTrackListener(frame));
		Measure.addActionListener(new MeasureListener(frame));
		Done.addActionListener(new DoneButtonListener(frame, true));
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

	protected class UploadTrackListener implements ActionListener {

		final Frame parent;

		public UploadTrackListener(Frame parent) {

			this.parent = parent;

		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {

			int result;

			chooserA = new JFileChooser();
			chooserA.setCurrentDirectory(new java.io.File("."));
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
			chooserB.setCurrentDirectory(chooserA.getCurrentDirectory());
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

	protected class DoneButtonListener implements ActionListener {
		final Frame parent;
		final boolean Done;

		public DoneButtonListener(Frame parent, final boolean Done) {
			this.parent = parent;
			this.Done = Done;
		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {
			wasDone = Done;
			
			ImagePlus impA = new Opener().openImage(chooserA.getSelectedFile().getPath());
			ImagePlus impB = new Opener().openImage(chooserB.getSelectedFile().getPath());

			// Tracking is done with imageA measurment is performed on both the
			// images
           
			    
			RandomAccessibleInterval<FloatType> originalimgA = ImageJFunctions.convertFloat(impA);
			RandomAccessibleInterval<FloatType> originalimgB = ImageJFunctions.convertFloat(impB);
			
			new InteractiveActiveContour_(originalimgA, originalimgB).run(null);
			close(parent);
		}
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
