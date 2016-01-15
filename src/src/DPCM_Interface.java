package src;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

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
	private int[] dstARGB;
	private final String[] predictionModes = {"A", "B", "C", "A+B-C", "(A+B)/2", "adaptiv"};
	
	public DPCM_Interface(){

		super(new BorderLayout(border, border));

		// load the default image
		File input = new File(initialFilename);
		File pre = new File("1.png");
		File re = new File("2.png");

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
		origARGB = new int[width*height];
		dstARGB = new int[width*height];
		dstARGB = inputImage.getPixels();
		origARGB = dstARGB.clone();


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
					dstARGB = new int[width*height];
					dstARGB = (int[])inputImage.getPixels();
					origARGB = dstARGB.clone();
					frame.pack();
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

		// top view controls
		JPanel topControls = new JPanel(new GridLayout(0, 3, 10, 0));
		
		topControls.add(load);
		topControls.add(predictions);
		topControls.add(quantisizeSlider);
		

		// center view
		JPanel centerControls = new JPanel(new GridLayout(0, 3, 10, 0));		
		
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
}
