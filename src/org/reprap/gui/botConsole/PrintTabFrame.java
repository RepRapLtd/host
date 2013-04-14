/*
 * PrintTabFrame.java
 *
 * Created on June 30, 2008, 1:45 PM
 */

package org.reprap.gui.botConsole;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.swing.JOptionPane;

import org.reprap.Extruder;
import org.reprap.Main;
import org.reprap.Preferences;
import org.reprap.Printer;
import org.reprap.pcb.PCB;
import org.reprap.utilities.Debug;

/**
 *
 * @author  ensab
 */
public class PrintTabFrame extends javax.swing.JInternalFrame {//AB99
//public class PrintTabFrame extends javax.swing.JPanel  {
	private static final long serialVersionUID = 1L;
	private BotConsoleFrame parentBotConsoleFrame = null;
//	private XYZTabPanel xYZTabPanel = null;
    private Printer printer;
    private boolean paused = false;
    private boolean seenSNAP = false;
    private boolean seenGCode = false;
    private long startTime = -1;
    private int oldLayer = -1;
    private String loadedFiles = "";
    private boolean loadedFilesLong = false;
    private boolean stlLoaded = false;
    private boolean gcodeLoaded = false;
    private boolean slicing = false;
    private boolean sdCard = false;
    private Thread printerFilePlay;
    private int pass;
    private int lastLayer;
    private final double passFrac[] = { 0.1234, 0.8518, 0.0248 };
    private long passEnd[] = new long[3];
    /** Creates new form PrintTabFrame */
    public PrintTabFrame(boolean pref) {
        initComponents(pref);
    	String machine = "simulator";
    	
    	//toSNAPRepRapRadioButton.setSelected(false);


    	machine = org.reprap.Preferences.RepRapMachine();


    	seenGCode = true;
    	printerFilePlay = null;
    	pass = -1;
    	lastLayer = -1;

    	printer = org.reprap.Main.gui.getPrinter();
    	enableSLoad();
    }
    
    /**
     * Keep the user amused.  If fractionDone is negative, the function
     * queries the layer statistics.  If it is 0 or positive, the function uses
     * it.
     * @param fractionDone
     */
    public void updateProgress(double fractionDone, int layer, int layers)
    {
    	//System.out.println("layer marker: " + fractionDone + ", " + layer + ", " + layers);
    	
    	
    	if(layers < 0)
    	{
    		layers = org.reprap.Main.gui.getLayers();
    	}
    	
    	if(layer < 0)
    	{
    		layer = org.reprap.Main.gui.getLayer();
    		if(layer >= 0)
        		currentLayerOutOfN.setText("" + layer + "/" + layers);
    	}
    	
    	if(fractionDone < 0)
    	{
    		// Only bother if the layer has just changed

    		if(layer == oldLayer)
    			return;
    		
    		boolean topDown = layer < oldLayer;

    		oldLayer = layer;

    		//currentLayerOutOfN.setText("" + layer + "/" + layers);
    		if(topDown)
    			fractionDone = (double)(layers - layer)/(double)layers;
    		else
    			fractionDone = (double)layer/(double)layers;
    	}
 
    	progressBar.setMinimum(0);
    	progressBar.setMaximum(100);
    	progressBar.setValue((int)(100*fractionDone));
    	
    	GregorianCalendar cal = new GregorianCalendar();
    	SimpleDateFormat dateFormat = new SimpleDateFormat("EE HH:mm:ss Z");
    	Date d = cal.getTime();
    	long e = d.getTime();
    	if(startTime < 0)
    	{
    		startTime = e;
    		passEnd[0] = -1;
    		passEnd[1] = -1;
    		passEnd[2] = -1;
    		return;
    	}
    	
       	double fin = 0;
       	
       	if(lastLayer < layer || layer == 1) // Back to the beginning
       	{
       		if(pass < 0)
       			pass = 0;
       		else
       		{
   				if(pass > 2)
   				{
   					if(printerFilePlay != null)
   					{
   						if(!printerFilePlay.isAlive())
   							printDone();
   					}
   					fin = e - startTime;
   					System.out.println("Time split: " + (double)(passEnd[0] - startTime)/fin + ":" + (double)(passEnd[1] - passEnd[0])/fin + ":" + (double)(e - passEnd[1])/fin);
   					return;
   				}
       			if(passEnd[pass] < 0)
       			{
       				passEnd[pass] = e;
       				pass++;
       			}
       		}
       	}
       	lastLayer = layer;
       	
    	//if(layer <= 0)
    		//return;
       	

       	
       	switch(pass)
       	{
       	case 0:
       		fin = (double)(e - startTime)/fractionDone;
       		fin = fin/passFrac[0];
       		break;
       		
       	case 1:
       		fin = (double)(e - passEnd[0])/fractionDone;
       		fin = fin/passFrac[1] + (double)passEnd[0];
       		break;
       		
       	case 2:
       		fin = (double)(e - passEnd[1])/fractionDone;
       		fin = fin/passFrac[2] + (double)passEnd[1];
       		break;
       		
       	default:
       			break;
       	}
    	
    	long f = (long)fin;
    	int h = (int)(f/60000L)/60;
    	int m = (int)(f/60000L)%60;
    	
    	if(m > 9)
    		expectedBuildTime.setText("" + h + ":" + m);
    	else
    		expectedBuildTime.setText("" + h + ":0" + m);
    	expectedFinishTime.setText(dateFormat.format(new Date(startTime + f)));
    	
    	if(layer >= 0)
    		currentLayerOutOfN.setText("" + layer + "/" + layers + " Pass " + (pass+1) + "/3");
    	
    	if(printerFilePlay != null)
    	{
    		if(!printerFilePlay.isAlive())
    			printDone();
    	}
    }
    
    /**
     * So the BotConsoleFrame can let us know who it is
     * @param b
     */
    public void setConsoleFrame(BotConsoleFrame b)
    {
    	parentBotConsoleFrame = b;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents(boolean pref) {
        buttonGroup1 = new javax.swing.ButtonGroup();
        
 
            variablesButton = new javax.swing.JButton();
            variablesButton.setActionCommand("preferences");
            variablesButton.setBackground(new java.awt.Color(255, 102, 255));
            variablesButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    preferences(evt);
                }
            });
            variablesButton.setText("Variables"); 
        
            // If this isn't here it falls over.  God knows why... 
            helpButton = new javax.swing.JButton();
            helpButton.setActionCommand("Help");
            helpButton.setBackground(new java.awt.Color(255, 102, 255));
            helpButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    help(evt);
                }
            });
            helpButton.setLabel("   Help   ");      
        
        
            changeMachineButton = new javax.swing.JButton();
            changeMachineButton.setActionCommand("changeMachine");
            changeMachineButton.setBackground(new java.awt.Color(255, 255, 255));
            changeMachineButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                	changeMachine(evt);
                }
            });   
            changeMachineButton.setText("Change"); 
            
            
            
        sliceButton = new javax.swing.JButton();
        //pcbButton = new javax.swing.JButton();
        //pauseButton = new javax.swing.JButton();
        //stopButton = new javax.swing.JButton();
        exitButton = new javax.swing.JButton();
        loadSTL = new javax.swing.JButton();
        //loadGCode = new javax.swing.JButton();
        loadRFO = new javax.swing.JButton();
        saveRFO = new javax.swing.JButton();
        saveSCAD = new javax.swing.JButton();
        
        
        sliceButton.setText("Slice");
        //pcbButton.setText("PCB");
        //pauseButton.setText("Pause");       
        //stopButton.setText("STOP !");       
        exitButton.setText("Exit");       
        loadSTL.setText("Load STL/CSG");
        //loadGCode.setText("Load GCode"); 
        loadRFO.setText("Load RFO");         
        saveRFO.setText("Save RFO");
        saveSCAD.setText("Save SCAD");
        
        
        layerPauseCheck = new javax.swing.JCheckBox();
        layerPause(false);
        getWebPage = new javax.swing.JButton();
        expectedBuildTimeLabel = new javax.swing.JLabel();
        filesLabel = new javax.swing.JLabel();
        expectedBuildTime = new javax.swing.JLabel();
        expectedFinishTimeLabel = new javax.swing.JLabel();
        changeMachineLabel = new javax.swing.JLabel();
        expectedFinishTime = new javax.swing.JLabel();
        progressLabel = new javax.swing.JLabel();
        currentLayerOutOfN = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
 
        //gCodeToFileRadioButton = new javax.swing.JRadioButton();
        //fromSDCardRadioButton = new javax.swing.JRadioButton();
        //toGCodeRepRapRadioButton = new javax.swing.JRadioButton();
        fileNameBox = new javax.swing.JLabel();
  
        displayPathsCheck = new javax.swing.JCheckBox();
        displayPaths(false);

        sliceButton.setBackground(new java.awt.Color(51, 204, 0));
        sliceButton.setFont(sliceButton.getFont());
 // NOI18N
        sliceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sliceButtonActionPerformed(evt);
            }
        });
        
        //pcbButton.setBackground(new java.awt.Color(152, 99, 62));
        //pcbButton.setFont(pcbButton.getFont());
 // NOI18N
//        pcbButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                pcbButtonActionPerformed(evt);
//            }
//        });

//        pauseButton.setBackground(new java.awt.Color(255, 204, 0));
// // NOI18N
//        pauseButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                pauseButtonActionPerformed(evt);
//            }
//        });

//        stopButton.setBackground(new java.awt.Color(255, 0, 0));
//        stopButton.setFont(new java.awt.Font("Dialog", 1, 12));
// // NOI18N
//        stopButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                stopButtonActionPerformed(evt);
//            }
//        });

 
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitButtonActionPerformed(evt);
            }
        });

        layerPauseCheck.setText("Pause between layers"); // NOI18N
        layerPauseCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                layerPauseCheckActionPerformed(evt);
            }
        });


        getWebPage.setIcon(new javax.swing.ImageIcon(
        		ClassLoader.getSystemResource("reprappro_logo-0.5.png")));
        		//ClassLoader.getSystemResource("rr-logo-green-url.png"))); // NOI18N
        getWebPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getWebPageActionPerformed(evt);
            }
        });
        
        expectedBuildTimeLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
        expectedBuildTimeLabel.setText("Expected slice time:"); // NOI18N

        filesLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
        filesLabel.setText("File(s): "); // NOI18N

        expectedBuildTime.setFont(new java.awt.Font("Tahoma", 0, 12));
        expectedBuildTime.setText("00:00"); // NOI18N

        expectedFinishTimeLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
        expectedFinishTimeLabel.setText("Expected to finish at:"); // NOI18N

        expectedFinishTime.setFont(new java.awt.Font("Tahoma", 0, 12));
        expectedFinishTime.setText("    -"); // NOI18N
        
        changeMachineLabel.setFont(new java.awt.Font("Tahoma", 0, 15));
        changeMachineLabel.setText("RepRap in use: " + Preferences.getActiveMachineName()); // NOI18N

        progressLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
        progressLabel.setText("Top down layer progress:"); // NOI18N

        currentLayerOutOfN.setFont(new java.awt.Font("Tahoma", 0, 12));
        currentLayerOutOfN.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        currentLayerOutOfN.setText("000/000"); // NOI18N

        loadSTL.setActionCommand("loadSTL");
        loadSTL.setBackground(new java.awt.Color(0, 204, 255));
  // NOI18N
        loadSTL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSTL(evt);
            }
        });

//        loadGCode.setActionCommand("loadGCode");
//        loadGCode.setBackground(new java.awt.Color(0, 204, 255));
// // NOI18N
//        loadGCode.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                LoadGCode(evt);
//            }
//        });
//
//        buttonGroup1.add(gCodeToFileRadioButton);
//        gCodeToFileRadioButton.setText("Slice to G-Code file");
//        gCodeToFileRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
//            public void mousePressed(java.awt.event.MouseEvent evt) {
//                selectorRadioButtonMousePressed(evt);
//            }
//        });
//        
//        buttonGroup1.add(fromSDCardRadioButton);
//        fromSDCardRadioButton.setText("Print SD card G-Codes");
//        fromSDCardRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
//            public void mousePressed(java.awt.event.MouseEvent evt) {
//                selectorRadioButtonMousePressed(evt);
//            }
//        });

        loadRFO.setActionCommand("loadRFO");
        loadRFO.setBackground(new java.awt.Color(0, 204, 255));
 // NOI18N
        loadRFO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadRFO(evt);
            }
        });

//        buttonGroup1.add(toGCodeRepRapRadioButton);
//        toGCodeRepRapRadioButton.setText("Print computer G-Codes");
//        toGCodeRepRapRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
//            public void mousePressed(java.awt.event.MouseEvent evt) {
//                selectorRadioButtonMousePressed(evt);
//            }
//        });

        fileNameBox.setFont(new java.awt.Font("Tahoma", 0, 12));
        fileNameBox.setText(" - ");

 

        saveRFO.setActionCommand("saveRFO");
        saveRFO.setBackground(new java.awt.Color(153, 153, 153));
 // NOI18N
        saveRFO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveRFO(evt);
            }
        });
        
        saveSCAD.setActionCommand("saveSCAD");
        saveSCAD.setBackground(new java.awt.Color(153, 153, 153));
 // NOI18N
        saveSCAD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSCAD(evt);
            }
        });
 
        
        displayPathsCheck.setText("Show paths when slicing");
        displayPathsCheck.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                displayPathsCheckMouseClicked(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());//AB99
        getContentPane().setLayout(layout);
        //this.setLayout(layout);//AB99
        
        
        
        
        
        
        layout.setHorizontalGroup
        (
        	layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add
        	(
                layout.createSequentialGroup()
                .addContainerGap()
                .add
                (
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add
                    (
                    	layout.createSequentialGroup().add
                    	(
                    		layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    		.add(exitButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    		.add(sliceButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    		.add(saveRFO, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    		.add(saveSCAD, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    		.add(loadRFO, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    		.add(loadSTL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        )
                        .add(50,50,50)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add
                        (
                            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layerPauseCheck)
                            .add(displayPathsCheck)
                            .add(changeMachineLabel)
                            .add
                            (
                            		layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false).add
                                    (
                                        layout.createSequentialGroup()
                                        .add(variablesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130,org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(20,20,20)
                                        .add(helpButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(100,100,100)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(getWebPage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    )
                             )
                        )
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                        .add
//                        (
//                            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false).add
//                            (
//                                layout.createSequentialGroup()
//                                .add(variablesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130,org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                                .add(dummyButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                                .add(getWebPage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 190, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                            )
//                            .add
//                            (
//                                layout.createSequentialGroup()
//                            )
//                        )
                    ).add
//                    (
//                         layout.createSequentialGroup()
//                         .add(expectedFinishTimeLabel)
//                         .add(7, 7, 7)
//                         .add(expectedFinishTime)
//                    ).add
                    (
                    	layout.createSequentialGroup().add
                        (
                            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add
                            (
                                layout.createSequentialGroup()
                                .add(expectedBuildTimeLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(expectedBuildTime)
                            ).add
                            (
                                 layout.createSequentialGroup()
                                 .add(expectedFinishTimeLabel)
                                 .add(7, 7, 7)
                                 .add(expectedFinishTime)
                            ).add
                            (
                                layout.createSequentialGroup()
                                .add(progressLabel)
                                .add(7, 7, 7)
                                .add(currentLayerOutOfN)
                            )
                        ).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add
                        (
                            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false).add
                            (
                                layout.createSequentialGroup()
                                
                                .add(50,50,50)
                                .add(filesLabel)
                                //.addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(fileNameBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            )
                            .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 350, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        )
                   )
               )
               .addContainerGap(29, Short.MAX_VALUE)
        	)
        );
        
        
        
        
            layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .addContainerGap()
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(getWebPage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 72, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            )
                        .add(layout.createSequentialGroup()
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(layout.createSequentialGroup()
                                		
                                        .add(loadRFO, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        
                                        .add(saveRFO, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(layout.createParallelGroup()
                                        .add(loadSTL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    	
                                        .add(layout.createSequentialGroup()
                                        		
                                            	.add(layerPauseCheck)//**77
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(displayPathsCheck)
                                                
                                                
                                            		)     
                                        )
                                        
                                        
                                        
                                        
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(saveSCAD, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(layout.createParallelGroup()
                                        		.add(sliceButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                
                                        .add(layout.createSequentialGroup()
                                        		
                                            	
                                                .add(changeMachineLabel)
                                                
                                            		)     
                                        )
                                        
                                        
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        
                                        
                                        
                                        
                                        
                                        
                                        
                                        
                                        
                                        
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(exitButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    )
                                .add(variablesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(helpButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    )
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//                                .add(layout.createSequentialGroup()
//                                		
//                                	.add(layerPauseCheck)//**77
//                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                                    .add(displayPathsCheck)
//                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                                    .add(changeMachineLabel)
//                                    
//                                		)                                  
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        )
                                        )))))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(expectedBuildTimeLabel)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(expectedBuildTime)
                            .add(filesLabel)
                            .add(fileNameBox)))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(expectedFinishTimeLabel)
                        .add(expectedFinishTime))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(progressLabel)
                            .add(currentLayerOutOfN))
                        .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(24, Short.MAX_VALUE))
            ); 
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
//        layout.setHorizontalGroup(
//            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//            .add(layout.createSequentialGroup()
//                .addContainerGap()
//                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//                    .add(layout.createSequentialGroup()
//                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
//                        	//.add(pcbButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                        		.add(exitButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                        		.add(sliceButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                            .add(saveRFO, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                            .add(saveSCAD, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                            //.add(loadGCode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                            //.add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//                                .add(loadRFO, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                .add(loadSTL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                              //  )
//                                )
//                                
//                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//                            //.add(toSNAPRepRapRadioButton)
//                            //.add(toGCodeRepRapRadioButton)
//                            //.add(gCodeToFileRadioButton)
//                            //.add(fromSDCardRadioButton)
//                            .add(layerPauseCheck).add(changeMachineLabel)//**77
//                            .add(displayPathsCheck))
//                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
//                            .add(layout.createSequentialGroup()
//                                .add(variablesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                                .add(dummyButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                                .add(getWebPage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 190, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                )
//                            .add(layout.createSequentialGroup()
//                            	
//                                //.add(sliceButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 105, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                //.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                                //.add(pcbButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 72, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                //.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                                //.add(pauseButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 78, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                //.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                                //.add(stopButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 85, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                //.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                                //.add(exitButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 62, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                            		)))
//                    .add(layout.createSequentialGroup()
//                        .add(expectedFinishTimeLabel)
//                        .add(7, 7, 7)
//                        .add(expectedFinishTime))
//                    .add(layout.createSequentialGroup()
//                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//                            .add(layout.createSequentialGroup()
//                                .add(expectedBuildTimeLabel)
//                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                                .add(expectedBuildTime))
//                            .add(layout.createSequentialGroup()
//                                .add(progressLabel)
//                                .add(7, 7, 7)
//                                .add(currentLayerOutOfN)))
//                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
//                            .add(layout.createSequentialGroup()
//                                .add(hoursMinutesLabel1)
//                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                                .add(fileNameBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//                            .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 430, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
//                .addContainerGap(29, Short.MAX_VALUE)
//            		)
//        );
//        layout.setVerticalGroup(
//            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//            .add(layout.createSequentialGroup()
//                .addContainerGap()
//                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//                    .add(layout.createSequentialGroup()
//                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                        .add(getWebPage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 72, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                        )
//                    .add(layout.createSequentialGroup()
//                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//                            .add(layout.createSequentialGroup()
//                            		
//                            		
//                            		
//                                	.add(loadSTL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                	.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                                    .add(loadRFO, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                                    .add(saveRFO, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                                    .add(saveSCAD, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                                    .add(sliceButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                                    .add(exitButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                    
//                                    
//                                    
//                            	//.add(layerPauseCheck).add(changeMachineLabel)
//                                //.add(loadGCode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                //.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                                //.add(loadSTL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                )
//                            //.add(layout.createSequentialGroup()
//                                //.add(toSNAPRepRapRadioButton)
////                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
////                                .add(toGCodeRepRapRadioButton)
////                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
////                                .add(fromSDCardRadioButton)
////                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
////                                .add(gCodeToFileRadioButton)
//                              //  )
//                            .add(variablesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                            .add(dummyButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                )
//                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//                            .add(layout.createSequentialGroup()
//                            		
//                            	.add(layerPauseCheck)//**77
//                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                                .add(displayPathsCheck)
//                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                                .add(changeMachineLabel)
//                                
//                            		)
//                                
//                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
//                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//                                    //.add(pauseButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                    //.add(sliceButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                    //.add(pcbButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                    //.add(stopButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                    //.add(exitButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
//                                .add(layout.createSequentialGroup()
////                                	.add(loadSTL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
////                                	.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
////                                    .add(loadRFO, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
////                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
////                                    .add(saveRFO, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
////                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
////                                    .add(saveSCAD, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
////                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
////                                    .add(sliceButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
////                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
////                                    .add(exitButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                    )
//                                    //.add(pcbButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
//                                    )))))
//                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
//                    .add(expectedBuildTimeLabel)
//                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
//                        .add(expectedBuildTime)
//                        .add(hoursMinutesLabel1)
//                        .add(fileNameBox)))
//                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
//                    .add(expectedFinishTimeLabel)
//                    .add(expectedFinishTime))
//                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
//                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
//                        .add(progressLabel)
//                        .add(currentLayerOutOfN))
//                    .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
//                .addContainerGap(24, Short.MAX_VALUE))
//        );
        
        //pack(); //AB99
    }// </editor-fold>//GEN-END:initComponents
    
public void printLive(boolean p)
{
	slicing = true;
	if(p)
		sliceButton.setText("Printing...");
	else
		sliceButton.setText("Slicing...");
	sliceButton.setBackground(Color.gray);    	
}

private void restoreSliceButton()
{
	slicing = false;
	sliceButton.setText("Slice");
	sliceButton.setBackground(new java.awt.Color(51, 204, 0)); 
	printerFilePlay = null;	
}

public void printDone()
{
	restoreSliceButton();
	String[] options = { "Exit" };
	//int r = 
		JOptionPane.showOptionDialog(null, "The file has been processed.", "Message",
			JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
			null, options, options[0]);
	org.reprap.Main.gui.dispose();
}

private boolean worthSaving()
{
	return true;
}

private void sliceButtonActionPerformed(java.awt.event.ActionEvent evt) 
{//GEN-FIRST:event_printButtonActionPerformed
	if(slicing)
		return;
	
	if(worthSaving())
	{
		int toDo = JOptionPane.showConfirmDialog(null, "First save the build as an RFO file?");
		switch(toDo)
		{
		case JOptionPane.YES_OPTION:
			saveRFO(null);
			break;
			
		case JOptionPane.NO_OPTION:
			break;
			
		case JOptionPane.CANCEL_OPTION:
			return;
		
		default:
			saveRFO(null);
		}
	}
	
	//printLive(!gCodeToFileRadioButton.isSelected());
	printLive(false);
	
	parentBotConsoleFrame.suspendPolling();
    parentBotConsoleFrame.setFractionDone(-1, -1, -1);
    org.reprap.Main.gui.mouseToWorld();
//    if(gCodeToFileRadioButton.isSelected())
//    {
    	int sp = -1;
    	if(loadedFiles != null)
    		sp = loadedFiles.length();
    	if(sp <= 0)
    	{
    		JOptionPane.showMessageDialog(null, "There are no STLs/RFOs loaded to slice to file.");
    		restoreSliceButton();
    		return;
    	}
    	sp = Math.max(loadedFiles.indexOf(".stl"), Math.max(loadedFiles.indexOf(".STL"), Math.max(loadedFiles.indexOf(".rfo"), loadedFiles.indexOf(".RFO"))));
    	if(sp <= 0)
       	{
    		JOptionPane.showMessageDialog(null, "The loaded file is not an STL or an RFO file.");
    	}   		
    	printer.setTopDown(true);	
    	if(printer.setGCodeFileForOutput(loadedFiles.substring(0, sp)) == null)
    	{
    		restoreSliceButton();
    		return;
    	}
//    }

//    if(sdCard)
//    {
//    	if(!printer.printSDFile(loadedFiles))
//    	{
//    		JOptionPane.showMessageDialog(null, "Error printing SD file.");
//    		restorePrintButton();
//    	}
//    	return;
//    }

//	if((printerFilePlay = printer.filePlay()) != null)
//	{
//	}
//    else
    	org.reprap.Main.gui.onProduceB();
    //parentBotConsoleFrame.resumePolling();
}//GEN-LAST:event_printButtonActionPerformed

private void pcbButtonActionPerformed(java.awt.event.ActionEvent evt)
{
	if(!SLoadOK)
		return;
	Extruder pcbp = printer.getExtruder("PCB-pen");
	if(pcbp == null)
	{
		JOptionPane.showMessageDialog(null, "You have no PCB-pen among your extruders; see http://reprap.org/wiki/Plotting#Using_the_RepRap_Host_Software.");
		return;
	}	
	parentBotConsoleFrame.suspendPolling();
	File inputGerber = org.reprap.Main.gui.onOpen("PCB Gerber file", new String[] {"top", "bot"}, "");
	if(inputGerber == null)
	{
		JOptionPane.showMessageDialog(null, "No Gerber file was loaded.");
		return;
	}

	int sp = inputGerber.getAbsolutePath().toLowerCase().indexOf(".top");
	String drill;
	if(sp < 0)
	{
		sp = inputGerber.getAbsolutePath().toLowerCase().indexOf(".bot");
		drill = ".bdr";
	} else
	{
		drill = ".tdr";
	}
	String fileRoot = "";
	if(sp > 0)
		fileRoot = inputGerber.getAbsolutePath().substring(0, sp);
	drill = fileRoot+drill;
	File inputDrill = new File(drill);
	if(inputDrill == null)
	{
		JOptionPane.showMessageDialog(null, "Drill file " + drill + " not found; drill centres will not be marked");
	}
	File outputGCode = org.reprap.Main.gui.onOpen("G-Code file for PCB printing", new String[] {"gcode"}, fileRoot);
	if(outputGCode == null)
	{
		JOptionPane.showMessageDialog(null, "No G-Code file was chosen.");
		return;
	}
	PCB p = new PCB();
	
	p.pcb(inputGerber, inputDrill, outputGCode, pcbp);
	parentBotConsoleFrame.resumePolling();
}

//public void pauseAction()
//{
//    paused = !paused;
//    if(paused)
//    {
//    	pauseButton.setText("Pausing...");
//    	org.reprap.Main.gui.pause();
//    	//while(!printer.iAmPaused());
//        parentBotConsoleFrame.resumePolling();
//        parentBotConsoleFrame.getPosition();
//        //parentBotConsoleFrame.getXYZTabPanel().recordCurrentPosition();
//        pauseButton.setText("Resume");
//    } else
//    {
//    	org.reprap.Main.gui.resume();
//        parentBotConsoleFrame.suspendPolling();
//        pauseButton.setText("Pause");
//    }   
//}

//private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseButtonActionPerformed
//    pauseAction();
//}//GEN-LAST:event_pauseButtonActionPerformed

//private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
////org.reprap.Main.gui.clickCancel();
//	pauseAction(); //FIXME - best we can do at the moment
//}//GEN-LAST:event_stopButtonActionPerformed

private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) 
{//GEN-FIRST:event_exitButtonActionPerformed
	if(worthSaving())
	{
		int toDo = JOptionPane.showConfirmDialog(null, "First save the build as an RFO file?");
		switch(toDo)
		{
		case JOptionPane.YES_OPTION:
			saveRFO(null);
			break;
			
		case JOptionPane.NO_OPTION:
			break;
			
		case JOptionPane.CANCEL_OPTION:
			return;
		
		default:
			saveRFO(null);
		}
	}
	Main.ftd.killThem();
	printer.dispose();
	System.exit(0);
}//GEN-LAST:event_exitButtonActionPerformed

private void layerPause(boolean p)
{
	org.reprap.Main.gui.setLayerPause(p);	
}

private void layerPauseCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_layerPauseCheckActionPerformed
org.reprap.Main.gui.setLayerPause(layerPauseCheck.isSelected());
}//GEN-LAST:event_layerPauseCheckActionPerformed

private void selectorRadioButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_selectorRadioButtonMousePressed
	
	@SuppressWarnings("unused")
	String machine = "simulator";
	boolean closeMessage = false;

	machine = org.reprap.Preferences.RepRapMachine();

//	if(evt.getSource() == toGCodeRepRapRadioButton)
//	{
//		enableGLoad();
//		if(seenSNAP)
//			closeMessage = true;
//		seenGCode = true;
//		sdCard = false;
//	} else if(evt.getSource() == gCodeToFileRadioButton)
//	{
//
//		enableSLoad();
//		if(seenSNAP)
//			closeMessage = true;
//		seenGCode = true;
//		sdCard = false;
//	}else if(evt.getSource() == fromSDCardRadioButton)
//	{
//		enableGLoad();
//		if(seenSNAP)
//			closeMessage = true;
//		seenGCode = true;
//		sdCard = true;
//	}
	try {
		org.reprap.Preferences.saveGlobal();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	printer.refreshPreferences();
	if(!closeMessage)
		return;
	JOptionPane.showMessageDialog(null, "As you have changed the type of RepRap machine you are using,\nyou will have to exit this program and run it again.");

}//GEN-LAST:event_selectorRadioButtonMousePressed

private void getWebPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getWebPageActionPerformed
try {
           URI url = new URI("http://reprappro.com");
           Desktop.getDesktop().browse(url);//***AB
        } catch(Exception e) {
            e.printStackTrace();
        }
}//GEN-LAST:event_getWebPageActionPerformed


private void loadSTL(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadSTL
	if(!SLoadOK)
		return;
	if(gcodeLoaded)
	{
		int response = JOptionPane.showOptionDialog(
                null                       // Center in window.
                , "This will abandon the G Code file you loaded."        // Message
                , "Load STL"               // Title in titlebar
                , JOptionPane.YES_NO_OPTION  // Option type
                , JOptionPane.PLAIN_MESSAGE  // messageType
                , null                       // Icon (none)
                , new String[] {"OK", "Cancel"}                    // Button text as above.
                , ""    // Default button's label
              );
		if(response == 1)
			return;
		loadedFiles = "";
	}
	String fn = printer.addSTLFileForMaking();
	if(fn.length() <= 0)
	{
		JOptionPane.showMessageDialog(null, "No STL was loaded.");
		return;
	}
	
	if(loadedFilesLong)
		return;
	if(loadedFiles.length() > 50)
	{
		loadedFiles += "...";
		loadedFilesLong = true;
	} else
		loadedFiles += fn + " ";
	
	fileNameBox.setText(loadedFiles);
	stlLoaded = true;
	gcodeLoaded = false;
}//GEN-LAST:event_loadSTL

private void LoadGCode(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoadGCode
	if(!GLoadOK)
		return;
	if(seenSNAP)
	{
		JOptionPane.showMessageDialog(null, "Sorry.  Sending G Codes to SNAP RepRap machines is not implemented.");
		return;
	}

	if(!org.reprap.Preferences.GCodeUseSerial())
	{
		JOptionPane.showMessageDialog(null, "There is no point in sending a G Code file to a G Code file.");
		return;
	}

	if(stlLoaded)
	{
		int response = JOptionPane.showOptionDialog(
                null                       // Center in window.
                , "This will abandon the STL/RFO file(s) you loaded."        // Message
                , "Load GCode"               // Title in titlebar
                , JOptionPane.YES_NO_OPTION  // Option type
                , JOptionPane.PLAIN_MESSAGE  // messageType
                , null                       // Icon (none)
                , new String[] {"OK", "Cancel"}                    // Button text as above.
                , ""    // Default button's label
              );
		if(response == 1)
			return;
		org.reprap.Main.gui.deleteAllSTLs();
		loadedFiles = "";
	}
	if(gcodeLoaded)
	{
		int response = JOptionPane.showOptionDialog(
                null                       // Center in window.
                , "This will abandon the previous G Code file you loaded."        // Message
                , "Load GCode"               // Title in titlebar
                , JOptionPane.YES_NO_OPTION  // Option type
                , JOptionPane.PLAIN_MESSAGE  // messageType
                , null                       // Icon (none)
                , new String[] {"OK", "Cancel"}                    // Button text as above.
                , ""    // Default button's label
              );
		if(response == 1)
			return;
		loadedFiles = "";
	}
	
	if(sdCard)
	{
		parentBotConsoleFrame.suspendPolling();
		String[] files = printer.getSDFiles();
		if(files.length > 0)
		{	
			loadedFiles = (String)JOptionPane.showInputDialog(
					this,
					"Select the SD file to print:",
					"Customized Dialog",
					JOptionPane.PLAIN_MESSAGE,
					null,
					files,
					files[0]);

			if(loadedFiles != null)
				if (loadedFiles.length() <= 0) 
					loadedFiles = null;
		} else
		{
			JOptionPane.showMessageDialog(null, "There are no SD files available.");
			loadedFiles = null;
		}
		parentBotConsoleFrame.resumePolling();
	} else
		loadedFiles = printer.loadGCodeFileForMaking();

	if(loadedFiles == null)
	{
		JOptionPane.showMessageDialog(null, "No GCode was loaded.");
		return;
	}
	
	fileNameBox.setText(loadedFiles);
	gcodeLoaded = true;
	stlLoaded = false;
}//GEN-LAST:event_LoadGCode

private void loadRFO(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadRFO
		if(!SLoadOK)
			return;
		if(gcodeLoaded)
		{
			int response = JOptionPane.showOptionDialog(
					null                       // Center in window.
					, "This will abandon the previous GCode file you loaded."        // Message
					, "Load RFO"               // Title in titlebar
					, JOptionPane.YES_NO_OPTION  // Option type
					, JOptionPane.PLAIN_MESSAGE  // messageType
					, null                       // Icon (none)
					, new String[] {"OK", "Cancel"}                    // Button text as above.
					, ""    // Default button's label
			);
			if(response == 1)
				return;
			loadedFiles = "";
		}

		String fn = printer.loadRFOFileForMaking();
		if(fn.length() <= 0)
		{
			JOptionPane.showMessageDialog(null, "No .rfo file was loaded.");
			return;
		}

		if(loadedFilesLong)
			return;
		if(loadedFiles.length() > 50)
		{
			loadedFiles += "...";
			loadedFilesLong = true;
		} else
			loadedFiles += fn + " ";

		fileNameBox.setText(loadedFiles);
		stlLoaded = true;
		gcodeLoaded = false;
}//GEN-LAST:event_loadRFO

private void preferences(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preferences
	org.reprap.gui.Preferences prefs = new org.reprap.gui.Preferences();
	prefs.setVisible(true);	// prefs.show();
}//GEN-LAST:event_preferences

private void changeMachine(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preferences
		
}//GEN-LAST:event_preferences

private void help(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preferences
	try {
        URI url = new URI("http://reprap.org/wiki/RepRapPro_Slicer");
        Desktop.getDesktop().browse(url);//***AB
     } catch(Exception e) {
         e.printStackTrace();
     }
}//GEN-LAST:event_preferences

private void saveRFO(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveRFO
	if(!SLoadOK)
		return;
	if(loadedFiles.contentEquals(""))
	{
		JOptionPane.showMessageDialog(null, "There's nothing to save...");
		return;
	}
	int sp = Math.max(loadedFiles.indexOf(".stl"), Math.max(loadedFiles.indexOf(".STL"), Math.max(loadedFiles.indexOf(".rfo"), loadedFiles.indexOf(".RFO"))));
	if(sp <= 0)
   	{
		JOptionPane.showMessageDialog(null, "The loaded file is not an STL or an RFO file.");
	} 
	printer.saveRFOFile(loadedFiles.substring(0, sp));
}//GEN-LAST:event_saveRFO

private void saveSCAD(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSCAD
	if(!SLoadOK)
		return;
	if(loadedFiles.contentEquals(""))
	{
		JOptionPane.showMessageDialog(null, "There's nothing to save...");
		return;
	}
	int sp = Math.max(loadedFiles.indexOf(".stl"), Math.max(loadedFiles.indexOf(".STL"), Math.max(loadedFiles.indexOf(".rfo"), loadedFiles.indexOf(".RFO"))));
	if(sp <= 0)
   	{
		JOptionPane.showMessageDialog(null, "The loaded file is not an STL or an RFO file.");
	} 
	org.reprap.Main.gui.saveSCAD(loadedFiles.substring(0, sp));
}

private void displayPaths(boolean disp)
{
		org.reprap.Preferences.setSimulate(disp);
}

private void displayPathsCheckMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_displayPathsCheckMouseClicked
	displayPaths(displayPathsCheck.isSelected());
}//GEN-LAST:event_displayPathsCheckMouseClicked


private void enableSLoad()
{
	SLoadOK = true;
	GLoadOK = false;
//	loadGCode.setBackground(new java.awt.Color(153, 153, 153));
	loadSTL.setBackground(new java.awt.Color(0, 204, 255));
	loadRFO.setBackground(new java.awt.Color(0, 204, 255));
	saveRFO.setBackground(new java.awt.Color(0, 204, 255));
	saveSCAD.setBackground(new java.awt.Color(0, 204, 255));
//	pcbButton.setBackground(new java.awt.Color(152, 99, 62));
	try
	{	
		org.reprap.Preferences.setRepRapMachine("GCodeRepRap");
		org.reprap.Preferences.setGCodeUseSerial(false);
	} catch (Exception e)
	{
		JOptionPane.showMessageDialog(null, e.toString());
	}	
//	toGCodeRepRapRadioButton.setSelected(false);
//	fromSDCardRadioButton.setSelected(false);
//	gCodeToFileRadioButton.setSelected(true);
}

//private void enableGLoad()
//{
//	SLoadOK = false;
//	GLoadOK = true;
//	loadGCode.setBackground(new java.awt.Color(0, 204, 255));
//	loadSTL.setBackground(new java.awt.Color(153, 153, 153));
//    loadRFO.setBackground(new java.awt.Color(153, 153, 153));
//    saveRFO.setBackground(new java.awt.Color(153, 153, 153));
//    saveSCAD.setBackground(new java.awt.Color(153, 153, 153));
//    pcbButton.setBackground(new java.awt.Color(153, 153, 153));
//	try
//	{
//		org.reprap.Preferences.setRepRapMachine("GCodeRepRap");
//		org.reprap.Preferences.setGCodeUseSerial(true);
//	} catch (Exception e)
//	{
//		JOptionPane.showMessageDialog(null, e.toString());
//	}
//	toGCodeRepRapRadioButton.setSelected(true);
//	fromSDCardRadioButton.setSelected(false);
//	gCodeToFileRadioButton.setSelected(false);
//}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel currentLayerOutOfN;
    private javax.swing.JCheckBox displayPathsCheck;

    private javax.swing.JLabel expectedBuildTime;
    private javax.swing.JLabel expectedBuildTimeLabel;
    private javax.swing.JLabel expectedFinishTime;
    private javax.swing.JLabel expectedFinishTimeLabel;
    private javax.swing.JLabel fileNameBox;
    //private javax.swing.JRadioButton gCodeToFileRadioButton;
    private javax.swing.JLabel changeMachineLabel;

    private javax.swing.JLabel filesLabel;
    private javax.swing.JCheckBox layerPauseCheck;
    private javax.swing.JButton getWebPage;
    
    //private javax.swing.JButton loadGCode;
    private javax.swing.JButton loadRFO;
    private javax.swing.JButton loadSTL;
    //private javax.swing.JButton pauseButton;
    private javax.swing.JButton variablesButton;
    private javax.swing.JButton sliceButton;
    //private javax.swing.JButton pcbButton;
    private javax.swing.JButton exitButton;
    private javax.swing.JButton saveRFO;
    private javax.swing.JButton saveSCAD;
    //private javax.swing.JButton stopButton;
    private javax.swing.JButton changeMachineButton;
    
    
 //   private java.awt.Button loadGCode;
 //   private java.awt.Button loadRFO;
//    private java.awt.Button loadSTL;
//    private java.awt.Button pauseButton;
//    private java.awt.Button preferencesButton;
    
      private javax.swing.JButton helpButton;
//    private java.awt.Button printButton;
//    private java.awt.Button pcbButton;
//    private java.awt.Button exitButton;
//    private java.awt.Button saveRFO;
//    private java.awt.Button stopButton;
    
    
    
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel progressLabel;
 
    //private javax.swing.JRadioButton toGCodeRepRapRadioButton;
    //private javax.swing.JRadioButton fromSDCardRadioButton;
    //private javax.swing.JRadioButton toSNAPRepRapRadioButton;
    // End of variables declaration//GEN-END:variables
    private boolean SLoadOK = false;
    private boolean GLoadOK = false;

}
