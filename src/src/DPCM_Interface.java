package src;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.ietf.jgss.Oid;

import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;

public class DPCM_Interface extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final String author = "Trautmann, Heinig";		// TODO: type in your name here
	private static final String initialFilename = "test1.jpg";
	private static final File openPath = new File(".");
	private static final int border = 10;
	private static final int maxWidth = 910; 
	private static final int maxHeight = 910; 
	private static final int graySteps = 256;

	private static int width;
	private static int height;

	private static JFrame frame;

	private ImageView inputImage;
	private ImageView predictionErrorImage;
	private ImageView reconstructedImage;

	private int[] origARGB;
	private int[] predictionARGB;
	private int[] reconstructedARGB;
	
	private int[] histogramOrig = new int[256];
	private int[] histogramPred = new int[256];
	private int[] histogramReco = new int[256];
	
	/*
	 * 
	 * 
	 */
	
	private final String[] predictionModes = {"A (horizontal)", "B (vertikal)", "C (diagonal)", "A+B-C", "(A+B)/2", "adaptiv"};
	
	public DPCM_Interface(){

		super(new BorderLayout(border, border));

		// load the default image
		File input = new File(initialFilename);

		if(!input.canRead()) input = openFile(); // file not found, choose another image

		inputImage = new ImageView(input);
		inputImage.setMaxSize(new Dimension(maxWidth, maxHeight));
		
		predictionErrorImage = new ImageView(input);
		predictionErrorImage.setMaxSize(new Dimension(maxWidth, maxHeight));
		
		reconstructedImage = new ImageView(input);
		reconstructedImage.setMaxSize(new Dimension(maxWidth, maxHeight));

		// TODO: initialize the original ARGB-Pixel array from the loaded image
		height = inputImage.getImgHeight();
		width = inputImage.getImgWidth();
		
//		origARGB = new int[width*height];
		
		origARGB = inputImage.getPixels();
		predictionARGB = predictionErrorImage.getPixels();
		reconstructedARGB = reconstructedImage.getPixels();
		grayImage();

		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				int pos = y*width+x;
				int grayValue = (origARGB[pos] & 0xFF);
				histogramOrig[grayValue]++;
			}
		}
		
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				int pos = y*width+x;
				int grayValue = (predictionARGB[pos] & 0xFF);
				histogramPred[grayValue]++;
			}
		}
		
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				int pos = y*width+x;
				int grayValue = (reconstructedARGB[pos] & 0xFF);
				reconstructedARGB[grayValue]++;
			}
		}

		// load image button
		JButton load = new JButton("Open Image");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File input = openFile();
				if(input != null) {
					inputImage.loadImage(input);
					inputImage.setMaxSize(new Dimension(maxWidth, maxHeight));

					height = inputImage.getImgHeight();
					width = inputImage.getImgWidth();
					// TODO: initialize the original ARGB-Pixel array from the newly loaded image
					origARGB = new int[width*height];
					origARGB = inputImage.getPixels();
					predictionARGB = predictionErrorImage.getPixels();
					reconstructedARGB = reconstructedImage.getPixels();
					grayImage();
				}
			}        	
		});


		// some status text
		//	statusLine = new JLabel(" ");

		JComboBox<String> predictions = new JComboBox<String>(predictionModes);
		JSlider quantisizeSlider = new JSlider(10, 1000, 10);
		double quantisize = quantisizeSlider.getValue()*1.0/10;
		String quantisizeText = "Quantisize: " +quantisize;
		TitledBorder titBorderQuantisize = BorderFactory.createTitledBorder(quantisizeText);
		quantisizeSlider.setBorder(titBorderQuantisize);
		
		quantisizeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				double quantisize = quantisizeSlider.getValue()/10.0;
				String quantisizeText = "Quantisize: " + quantisize;
				TitledBorder titBorderQuantisize = BorderFactory.createTitledBorder(quantisizeText);
				quantisizeSlider.setBorder(titBorderQuantisize);
				
			}       	
		});
		
		ActionListener predictionActionListener = new ActionListener() {//add actionlistner to listen for change
            @Override
            public void actionPerformed(ActionEvent e) {

                String s = (String) predictions.getSelectedItem();//get the selected item

                switch (s) {//check for a match
                    case "A (horizontal)":
                        predictionA();
                        break;
                    case "B (vertikal)":
                        predictionB();
                        break;
                    case "C (diagonal)":
                    	predictionC();
                    	break;
                    case "A+B-C":
                		predictionAplusBminusC();
                		break;
                    case "(A+B)/2":
                    	predictionAplusBdivideByTwo();
                		break;
                    case "adaptiv":
                		adaptiv();
                		break;
                }
            }
        };

        predictions.addActionListener(predictionActionListener);

		// top view controls
		JPanel topControls = new JPanel(new GridLayout(0, 3, 10, 0));
		
		topControls.add(load);
		topControls.add(predictions);
		topControls.add(quantisizeSlider);
		

		// center view
		JPanel centerControls = new JPanel(new GridLayout(0, 3, 10, 0));	
		
		TitledBorder inputImageTitle = BorderFactory.createTitledBorder("Eingangsbild");
		inputImage.setBorder(inputImageTitle);
		
		TitledBorder predictionErrorImageTitle = BorderFactory.createTitledBorder("Prädiktionsfehlerbild");
		predictionErrorImage.setBorder(predictionErrorImageTitle);
		
		TitledBorder reconstructedImageTitle = BorderFactory.createTitledBorder("Rekonstruiertes Bild");
		reconstructedImage.setBorder(reconstructedImageTitle);
		
		centerControls.add(inputImage);
		centerControls.add(predictionErrorImage);
		centerControls.add(reconstructedImage);

		// add to main panel
		add(topControls, BorderLayout.NORTH);
		add(centerControls, BorderLayout.CENTER);
		

		// add border to main panel
		setBorder(BorderFactory.createEmptyBorder(border,border,border,border));

	}

	private File openFile() {
		// file open dialog
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (*.jpg, *.png, *.gif)", "jpg", "png", "gif");
		chooser.setFileFilter(filter);
		chooser.setCurrentDirectory(openPath);
		int ret = chooser.showOpenDialog(this);
		if(ret == JFileChooser.APPROVE_OPTION) return chooser.getSelectedFile();
		return null;		
	}

	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("Image Analysis - " + author);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JComponent contentPane = new DPCM_Interface();
		contentPane.setOpaque(true); //content panes must be opaque
		frame.setContentPane(contentPane);

		// display the window
		frame.pack();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
		frame.setVisible(true);
	}

	/**
	 * Main method. 
	 * @param args - ignored. No arguments are used by this application.
	 */
	public static void main(String[] args) {
		// schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
	
	public void grayImage(){
		for(int x = 0; x < origARGB.length-1; x++){
				int grayValue = ((origARGB[x] & 0xFF) + ((origARGB[x]>>16) & 0xFF) + ((origARGB[x]>>8) & 0xFF))/3;
				origARGB[x] = (0xFF<<24) | (grayValue<<16) | (grayValue<<8) | grayValue;
		}
		predictionErrorImage.setPixels(origARGB);
		reconstructedImage.setPixels(origARGB);
		
		inputImage.applyChanges();
		predictionErrorImage.applyChanges();
		reconstructedImage.applyChanges();
	}
	
	public void predictionA(){
		int [] actualPixels = new int[4];		
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				int pos = y*width+x;
				actualPixels = getSurrounded(x, y).clone();
				int error = putInRange(actualPixels[1] + 128);
				predictionARGB[pos] = (0xFF<<24) | (error <<16) | (error <<8) | error;				
			}
		}
		predictionErrorImage.applyChanges();
	}
	
	private void adaptiv() {
		int [] actualPixels = new int[4];
		int error;
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				int pos = y*width+x;
				actualPixels = getSurrounded(x, y).clone();
				if(AorB(actualPixels)) error = putInRange(actualPixels[2] + 128);
				else error = putInRange(actualPixels[1] + 128);
				predictionARGB[pos] = (0xFF<<24) | (error <<16) | (error <<8) | error;				
			}
		}
		predictionErrorImage.applyChanges();
	}

	private void predictionAplusBdivideByTwo() {
		int [] actualPixels = new int[4];		
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				int pos = y*width+x;
				actualPixels = getSurrounded(x, y).clone();
				int error = putInRange((actualPixels[1] + actualPixels[2]) / 2 + 128);
				predictionARGB[pos] = (0xFF<<24) | (error <<16) | (error <<8) | error;				
			}
		}
		predictionErrorImage.applyChanges();
	}

	private void predictionB() {
		int [] actualPixels = new int[4];		
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				int pos = y*width+x;
				actualPixels = getSurrounded(x, y).clone();
				int error = putInRange(actualPixels[2] + 128);
				predictionARGB[pos] = (0xFF<<24) | (error <<16) | (error <<8) | error;				
			}
		}
		predictionErrorImage.applyChanges();
	}

	private void predictionC() {
		int [] actualPixels = new int[4];		
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				int pos = y*width+x;
				actualPixels = getSurrounded(x, y).clone();
				int error = putInRange(actualPixels[3] + 128);
				predictionARGB[pos] = (0xFF<<24) | (error <<16) | (error <<8) | error;				
			}
		}
		predictionErrorImage.applyChanges();
	}

	private void predictionAplusBminusC() {
		int [] actualPixels = new int[4];		
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				int pos = y*width+x;
				actualPixels = getSurrounded(x, y).clone();
				int error = putInRange(actualPixels[1] + actualPixels[2] - actualPixels[3] + 128);
				predictionARGB[pos] = (0xFF<<24) | (error <<16) | (error <<8) | error;				
			}
		}
		predictionErrorImage.applyChanges();
	}
	
	public int[] getSurrounded(int x, int y){
		int[] pixels = new int[4];
		pixels[0] = (origARGB[y*width+x] & 0xFF);
		
		if(x == 0){
			pixels[1] = 128;
				if(y == 0) pixels[3] = 128;
		}
		else if(y == 0) pixels[2] = 128;
		else{
			pixels[1] = (origARGB[(y*width+(x-1))] & 0xFF);
			pixels[2] = (origARGB[((y-1)*width+x)] & 0xFF);
			pixels[3] = (origARGB[((y-1)*width+(x-1))] & 0xFF);
		}
		
		pixels[1] -= (origARGB[y*width+x] & 0xFF);
		pixels[2] -= (origARGB[y*width+x] & 0xFF);
		pixels[3] -= (origARGB[y*width+x] & 0xFF);
		
		return pixels;
	}
	
	private int putInRange(int colorValue) {
		if(colorValue<0)colorValue = 0;
		if(colorValue>255)colorValue = 255;
		return colorValue;
	} 
	
	private boolean AorB(int[] pixels){
		return Math.abs(pixels[1] - pixels[3]) < Math.abs(pixels[2] - pixels[3]);
	}
	
	public double calculateEnt(int[] histogram){
		double[] probability = new double[countEntropie(histogram)];
		double entropy = 0.0;
		int counter = 0;

		for(int i = 0; i < histogram.length;i++){
			if(histogram[i] > 0){
				probability[counter] = getProbability(i, histogram);
				entropy += (probability[counter])*((double)((histogram[i])/(double)(width*height)));
				counter++;
			}
		}
		return entropy;
	}

	public int countEntropie(int[] histogram){
		int counter = 0;

		for(int i = 0; i <histogram.length;i++){
			if(histogram[i] > 0)
				counter++;
		}
		return counter;
	}

	public double getProbability(int index, int[] histogram){
		return -((Math.log10(((double)histogram[index])/(width*height)))/Math.log10(2.0));
	}
}
