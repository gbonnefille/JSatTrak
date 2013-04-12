/*
 * InitialConditionsPanel.java
 * =====================================================================
 *   This file is part of JSatTrak.
 *
 *   Copyright 2007-2013 Shawn E. Gano
 *   
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * =====================================================================
 *
 * Created on January 11, 2008, 3:04 PM
 */
package jsattrak.customsat.gui;

import java.awt.BorderLayout;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import jsattrak.customsat.InitialConditionsNode;
import jsattrak.gui.JSatTrak;
import jsattrak.utilities.CustomFileFilter;
import jsattrak.utilities.SatBrowserTleDataLoader;
import jsattrak.utilities.TLEDownloader;
import jsattrak.utilities.TLElements;
import jsattrak.utilities.TreeTransferHandler;
import name.gano.astro.time.Time;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.orekit.errors.OrekitException;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.IERSConventions;
import org.orekit.orbits.PositionAngle;
import org.orekit.utils.PVCoordinates;

/**
 * 
 * @author sgano
 */
public class InitialConditionsPanel extends javax.swing.JPanel {

	private static final long serialVersionUID = 3434684446972278319L;
	
	InitialConditionsNode icNode;
	boolean componentsIni = false; // flag for when the components have been
	// inialized
	// used for diaplying settings panel
	private JInternalFrame iframe; // used to know what its parent frame is - to
	// close window
	final Time scenarioEpochDate; // used for date string functions
	// hashtable with tle's
	private Hashtable<String, TLElements> tleHash;
	// TLE sat database
	private SatBrowserTleDataLoader sbtdl;
	private JSatTrak app;

	/**
	 * Creates new form InitialConditionsPanel
	 * 
	 * @param icNode
	 *            inital conditions node
	 * @param scenarioEpochDate
	 *            scenario epoch date
	 */
	public InitialConditionsPanel(JSatTrak app, InitialConditionsNode icNode,
			final Time scenarioEpochDate) {
		this.icNode = icNode;
		this.scenarioEpochDate = scenarioEpochDate;
		this.app = app;

		initComponents();

		// Add listener to tabbedPane
		iniTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
				iniTabbedPaneStateChanged(evt);
			}
		});

		// BEGIN load sat tle Panel

		// Initialize tree
		DefaultTreeModel treeModel;

		// top node
		DefaultMutableTreeNode topTreeNode;

		topTreeNode = new DefaultMutableTreeNode("Satellites");

		// create a hashmap of TLEs with key as text from tree
		tleHash = new Hashtable<String, TLElements>();

		treeModel = new DefaultTreeModel(topTreeNode); // create tree model
		// using root node
		satTree.setModel(treeModel); // set the tree's model

		sbtdl = new SatBrowserTleDataLoader(app, topTreeNode, tleHash,
				tleOutputTextArea, satTree, icNode.getLastTreeSelection());

		sbtdl.execute();

		// Drag and Drop Handler
		// setup transfer handler
		satTree.setTransferHandler(new TreeTransferHandler(tleHash));

		// allow mutiple selections
		satTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		// if local files exist wait for thread to finish before exiting
		TLEDownloader tleDownloader = new TLEDownloader();
		if ((new File(tleDownloader.getLocalPath()).exists())
				&& (new File(tleDownloader.getTleFilePath(0)).exists())) {
			while (!sbtdl.isDone()) {

				try {
					Thread.sleep(50); // sleep for a little bit to wait for the
										// thread

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} // wait for thread if needed

		// END load sat tle Panel

		muTextField.setText("" + icNode.getMu());

		// Set the right tablePane
		switch (icNode.getCoordinate()) {

		case 0:
			// fill out keplerian elements
			iniTabbedPane.setSelectedIndex(0);
			double[] kep = icNode.getKeplarianElements();
			setKepElementsInGUI(kep);

			switch (icNode.getPositionAngle()) {
			case MEAN:
				kepComboBox.setSelectedIndex(0);
				break;
			case TRUE:
				kepComboBox.setSelectedIndex(1);
				break;
			case ECCENTRIC:
				kepComboBox.setSelectedIndex(2);
				break;
			}

			break;

		case 1:
			// fill out cartesian elements
			iniTabbedPane.setSelectedIndex(1);
			PVCoordinates state = icNode.getCartesianElements();
			setCatesianElementsInGui(state);

			break;

		case 2:
			// fill out circular elements
			iniTabbedPane.setSelectedIndex(2);
			double[] circu = icNode.getCircularElements();
			setCircularElementsInGUI(circu);

			switch (icNode.getPositionAngle()) {
			case MEAN:
				circComboBox.setSelectedIndex(0);
				break;
			case TRUE:
				circComboBox.setSelectedIndex(1);
				break;
			case ECCENTRIC:
				circComboBox.setSelectedIndex(2);
				break;
			}

			break;
		case 3:
			// fill out equinoctial elements
			iniTabbedPane.setSelectedIndex(3);
			double[] equi = icNode.getEquinoctialElements();
			setEquinoctialElementsInGUI(equi);

			switch (icNode.getPositionAngle()) {
			case MEAN:
				equiComboBox.setSelectedIndex(0);
				break;
			case TRUE:
				equiComboBox.setSelectedIndex(1);
				break;
			case ECCENTRIC:
				equiComboBox.setSelectedIndex(2);
				break;
			}

			break;

		case 4:
			// Select the TLE panel
			iniTabbedPane.setSelectedIndex(4);

			break;

		default:
			break;

		}

		// fill in epoch
		epochTextField.setText(scenarioEpochDate.convertJD2String(icNode
				.getIniJulDate()));

		componentsIni = true;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed"
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		applyButton = new javax.swing.JButton();
		iniTabbedPane = new javax.swing.JTabbedPane();
		kepPanel = new javax.swing.JPanel();
		jLabel11 = new javax.swing.JLabel();
		jLabel12 = new javax.swing.JLabel();
		jLabel10 = new javax.swing.JLabel();
		jLabel14 = new javax.swing.JLabel();
		jLabel15 = new javax.swing.JLabel();
		jLabel13 = new javax.swing.JLabel();
		jLabel28 = new javax.swing.JLabel();
		eTextField = new javax.swing.JTextField();
		iTextField = new javax.swing.JTextField();
		longTextField = new javax.swing.JTextField();
		argTextField = new javax.swing.JTextField();
		mTextField = new javax.swing.JTextField();
		saTextField = new javax.swing.JTextField();
		kepComboBox = new javax.swing.JComboBox();
		j2kPanel = new javax.swing.JPanel();
		jLabel3 = new javax.swing.JLabel();
		xTextField = new javax.swing.JTextField();
		jLabel4 = new javax.swing.JLabel();
		yTextField = new javax.swing.JTextField();
		jLabel5 = new javax.swing.JLabel();
		zTextField = new javax.swing.JTextField();
		jLabel9 = new javax.swing.JLabel();
		dxTextField = new javax.swing.JTextField();
		jLabel8 = new javax.swing.JLabel();
		dyTextField = new javax.swing.JTextField();
		jLabel7 = new javax.swing.JLabel();
		dzTextField = new javax.swing.JTextField();
		jLabel6 = new javax.swing.JLabel();
		circPanel = new javax.swing.JPanel();
		jLabel16 = new javax.swing.JLabel();
		jLabel17 = new javax.swing.JLabel();
		jLabel18 = new javax.swing.JLabel();
		jLabel19 = new javax.swing.JLabel();
		jLabel20 = new javax.swing.JLabel();
		jLabel21 = new javax.swing.JLabel();
		jLabel29 = new javax.swing.JLabel();
		sacTextField = new javax.swing.JTextField();
		excTextField = new javax.swing.JTextField();
		eycTextField = new javax.swing.JTextField();
		icTextField = new javax.swing.JTextField();
		raancTextField = new javax.swing.JTextField();
		latcTextField = new javax.swing.JTextField();
		circComboBox = new javax.swing.JComboBox();
		equiPanel = new javax.swing.JPanel();
		jLabel22 = new javax.swing.JLabel();
		jLabel23 = new javax.swing.JLabel();
		jLabel24 = new javax.swing.JLabel();
		jLabel25 = new javax.swing.JLabel();
		jLabel27 = new javax.swing.JLabel();
		jLabel30 = new javax.swing.JLabel();
		saeTextField = new javax.swing.JTextField();
		exeTextField = new javax.swing.JTextField();
		eyeTextField = new javax.swing.JTextField();
		ieTextField = new javax.swing.JTextField();
		raaneTextField = new javax.swing.JTextField();
		lateTextField = new javax.swing.JTextField();
		jLabel26 = new javax.swing.JLabel();
		equiComboBox = new javax.swing.JComboBox();
		outsidePanel = new javax.swing.JPanel();
		jPanel4 = new javax.swing.JPanel();
		jScrollPane2 = new javax.swing.JScrollPane();
		satTree = new javax.swing.JTree();
		jScrollPane4 = new javax.swing.JScrollPane();
		tleOutputTextArea = new javax.swing.JTextArea();
		jPanel3 = new javax.swing.JPanel();
		jLabel2 = new javax.swing.JLabel();
		jLabel31 = new javax.swing.JLabel();
		jLabel1 = new javax.swing.JLabel();
		muTextField = new javax.swing.JTextField();
		epochTextField = new javax.swing.JTextField();
		inputComboBox = new javax.swing.JComboBox();

		okButton.setText("Ok");
		okButton.setMaximumSize(new java.awt.Dimension(50, 26));
		okButton.setMinimumSize(new java.awt.Dimension(50, 26));
		okButton.setPreferredSize(new java.awt.Dimension(50, 26));
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				okButtonActionPerformed(evt);
			}
		});

		cancelButton.setText("Cancel");
		cancelButton.setMaximumSize(new java.awt.Dimension(73, 26));
		cancelButton.setMinimumSize(new java.awt.Dimension(73, 26));
		cancelButton.setPreferredSize(new java.awt.Dimension(73, 26));
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		applyButton.setText("Apply");
		applyButton.setMaximumSize(new java.awt.Dimension(65, 26));
		applyButton.setMinimumSize(new java.awt.Dimension(65, 26));
		applyButton.setPreferredSize(new java.awt.Dimension(65, 26));
		applyButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				applyButtonActionPerformed(evt);
			}
		});

		iniTabbedPane.setMinimumSize(new java.awt.Dimension(354, 210));
		iniTabbedPane.setPreferredSize(new java.awt.Dimension(427, 309));

		kepPanel.setMinimumSize(new java.awt.Dimension(272, 163));
		kepPanel.setPreferredSize(new java.awt.Dimension(362, 163));

		jLabel11.setText("Eccentricity:");
		jLabel11.setMaximumSize(new java.awt.Dimension(70, 16));
		jLabel11.setMinimumSize(new java.awt.Dimension(70, 16));
		jLabel11.setPreferredSize(new java.awt.Dimension(70, 16));

		jLabel12.setText("Inclination [deg]:");
		jLabel12.setMaximumSize(new java.awt.Dimension(93, 16));
		jLabel12.setMinimumSize(new java.awt.Dimension(93, 16));
		jLabel12.setPreferredSize(new java.awt.Dimension(93, 16));

		jLabel10.setText("Semimajor axis [m]:");
		jLabel10.setMaximumSize(new java.awt.Dimension(114, 16));
		jLabel10.setMinimumSize(new java.awt.Dimension(114, 16));
		jLabel10.setPreferredSize(new java.awt.Dimension(114, 16));

		jLabel14.setText("Arg. of perigee [deg]:");
		jLabel14.setMaximumSize(new java.awt.Dimension(118, 16));
		jLabel14.setMinimumSize(new java.awt.Dimension(118, 16));
		jLabel14.setPreferredSize(new java.awt.Dimension(118, 16));

		jLabel15.setText("Anomaly [deg]:");
		jLabel15.setMaximumSize(new java.awt.Dimension(84, 16));
		jLabel15.setMinimumSize(new java.awt.Dimension(84, 16));
		jLabel15.setPreferredSize(new java.awt.Dimension(84, 16));

		jLabel13.setText("Long. of asc. node [deg]:");
		jLabel13.setMaximumSize(new java.awt.Dimension(138, 16));
		jLabel13.setMinimumSize(new java.awt.Dimension(138, 16));
		jLabel13.setPreferredSize(new java.awt.Dimension(138, 16));

		jLabel28.setText("Type of anomaly:");
		jLabel28.setMaximumSize(new java.awt.Dimension(95, 16));
		jLabel28.setMinimumSize(new java.awt.Dimension(95, 16));
		jLabel28.setPreferredSize(new java.awt.Dimension(95, 16));

		kepComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
				"Mean", "True", "Eccentric" }));
		kepComboBox.setMinimumSize(new java.awt.Dimension(82, 25));
		kepComboBox.setPreferredSize(new java.awt.Dimension(82, 25));
		kepComboBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				kepComboBoxActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout kepPanelLayout = new javax.swing.GroupLayout(
				kepPanel);
		kepPanel.setLayout(kepPanelLayout);
		kepPanelLayout
				.setHorizontalGroup(kepPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								kepPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												kepPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jLabel28,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel13,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel11,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel12,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel10,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel15,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel14,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												kepPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING,
																false)
														.addComponent(
																eTextField)
														.addComponent(
																mTextField)
														.addComponent(
																longTextField,
																javax.swing.GroupLayout.Alignment.TRAILING)
														.addComponent(
																iTextField)
														.addComponent(
																kepComboBox, 0,
																140,
																Short.MAX_VALUE)
														.addComponent(
																saTextField)
														.addComponent(
																argTextField))
										.addContainerGap(54, Short.MAX_VALUE)));
		kepPanelLayout
				.setVerticalGroup(kepPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								kepPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												kepPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																saTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel10,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												kepPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																eTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel11,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												kepPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																iTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel12,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												kepPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																longTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel13,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												kepPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel14,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																argTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												kepPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																mTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel15,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												kepPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																kepComboBox,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel28,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap(14, Short.MAX_VALUE)));

		iniTabbedPane.addTab("Keplarian", kepPanel);

		j2kPanel.setMinimumSize(new java.awt.Dimension(349, 127));
		j2kPanel.setPreferredSize(new java.awt.Dimension(349, 137));

		jLabel3.setText("X:");
		jLabel3.setMaximumSize(new java.awt.Dimension(11, 16));
		jLabel3.setMinimumSize(new java.awt.Dimension(11, 16));
		jLabel3.setPreferredSize(new java.awt.Dimension(11, 16));

		jLabel4.setText("Y:");
		jLabel4.setMaximumSize(new java.awt.Dimension(10, 16));
		jLabel4.setMinimumSize(new java.awt.Dimension(10, 16));
		jLabel4.setPreferredSize(new java.awt.Dimension(10, 16));

		jLabel5.setText("Z:");
		jLabel5.setMaximumSize(new java.awt.Dimension(10, 16));
		jLabel5.setMinimumSize(new java.awt.Dimension(10, 16));
		jLabel5.setPreferredSize(new java.awt.Dimension(10, 16));

		jLabel9.setText("dX:");
		jLabel9.setMaximumSize(new java.awt.Dimension(18, 16));
		jLabel9.setMinimumSize(new java.awt.Dimension(18, 16));
		jLabel9.setPreferredSize(new java.awt.Dimension(18, 16));

		jLabel8.setText("dY:");
		jLabel8.setMaximumSize(new java.awt.Dimension(17, 16));
		jLabel8.setMinimumSize(new java.awt.Dimension(17, 16));
		jLabel8.setPreferredSize(new java.awt.Dimension(17, 16));

		jLabel7.setText("dZ:");
		jLabel7.setMaximumSize(new java.awt.Dimension(17, 16));
		jLabel7.setMinimumSize(new java.awt.Dimension(17, 16));
		jLabel7.setPreferredSize(new java.awt.Dimension(17, 16));

		dzTextField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				dzTextFieldActionPerformed_FormGen(evt);
			}
		});

		jLabel6.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
		jLabel6.setForeground(new java.awt.Color(102, 102, 102));
		jLabel6.setText("* Note: Units are in meters");

		javax.swing.GroupLayout j2kPanelLayout = new javax.swing.GroupLayout(
				j2kPanel);
		j2kPanel.setLayout(j2kPanelLayout);
		j2kPanelLayout
				.setHorizontalGroup(j2kPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								j2kPanelLayout
										.createSequentialGroup()
										.addGap(25, 25, 25)
										.addGroup(
												j2kPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(jLabel6)
														.addGroup(
																j2kPanelLayout
																		.createSequentialGroup()
																		.addGroup(
																				j2kPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								jLabel3,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								jLabel4,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								jLabel5,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(
																				j2kPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								false)
																						.addComponent(
																								zTextField,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								94,
																								Short.MAX_VALUE)
																						.addComponent(
																								yTextField)
																						.addComponent(
																								xTextField))
																		.addGap(38,
																				38,
																				38)
																		.addGroup(
																				j2kPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								false)
																						.addGroup(
																								j2kPanelLayout
																										.createSequentialGroup()
																										.addComponent(
																												jLabel8,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												javax.swing.GroupLayout.PREFERRED_SIZE)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												dyTextField,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												95,
																												javax.swing.GroupLayout.PREFERRED_SIZE))
																						.addGroup(
																								j2kPanelLayout
																										.createSequentialGroup()
																										.addComponent(
																												jLabel7,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												javax.swing.GroupLayout.PREFERRED_SIZE)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												dzTextField))
																						.addGroup(
																								j2kPanelLayout
																										.createSequentialGroup()
																										.addComponent(
																												jLabel9,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												javax.swing.GroupLayout.PREFERRED_SIZE)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												dxTextField,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												94,
																												javax.swing.GroupLayout.PREFERRED_SIZE)))))
										.addContainerGap(61, Short.MAX_VALUE)));
		j2kPanelLayout
				.setVerticalGroup(j2kPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								j2kPanelLayout
										.createSequentialGroup()
										.addGap(15, 15, 15)
										.addGroup(
												j2kPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																xTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel9,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																dxTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel3,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addGroup(
												j2kPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel4,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																yTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel8,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																dyTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addGroup(
												j2kPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel5,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																zTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel7,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																dzTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(jLabel6)
										.addContainerGap(85, Short.MAX_VALUE)));

		iniTabbedPane.addTab("Cartesian", j2kPanel);

		circPanel.setMinimumSize(new java.awt.Dimension(332, 163));
		circPanel.setPreferredSize(new java.awt.Dimension(422, 163));

		jLabel16.setText("Semimajor axis [m]:");
		jLabel16.setMaximumSize(new java.awt.Dimension(114, 16));
		jLabel16.setMinimumSize(new java.awt.Dimension(114, 16));
		jLabel16.setPreferredSize(new java.awt.Dimension(114, 16));

		jLabel17.setText("Excentricity ex:");
		jLabel17.setMaximumSize(new java.awt.Dimension(87, 16));
		jLabel17.setMinimumSize(new java.awt.Dimension(87, 16));
		jLabel17.setPreferredSize(new java.awt.Dimension(87, 16));

		jLabel18.setText("Excentricity ey:");
		jLabel18.setMaximumSize(new java.awt.Dimension(86, 16));
		jLabel18.setMinimumSize(new java.awt.Dimension(86, 16));
		jLabel18.setPreferredSize(new java.awt.Dimension(86, 16));

		jLabel19.setText("Inclinaison [deg]:");
		jLabel19.setMaximumSize(new java.awt.Dimension(96, 16));
		jLabel19.setMinimumSize(new java.awt.Dimension(96, 16));
		jLabel19.setPreferredSize(new java.awt.Dimension(96, 16));

		jLabel20.setText("Right ascension of asc. node [deg]:");
		jLabel20.setMaximumSize(new java.awt.Dimension(198, 16));
		jLabel20.setMinimumSize(new java.awt.Dimension(198, 16));
		jLabel20.setPreferredSize(new java.awt.Dimension(198, 16));

		jLabel21.setText("Latitude argument [deg]:");
		jLabel21.setMaximumSize(new java.awt.Dimension(139, 16));
		jLabel21.setMinimumSize(new java.awt.Dimension(139, 16));
		jLabel21.setPreferredSize(new java.awt.Dimension(139, 16));

		jLabel29.setText("Type of latitude:");
		jLabel29.setMaximumSize(new java.awt.Dimension(89, 16));
		jLabel29.setMinimumSize(new java.awt.Dimension(89, 16));
		jLabel29.setPreferredSize(new java.awt.Dimension(89, 16));

		circComboBox.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "Mean", "True", "Eccentric" }));
		circComboBox.setMinimumSize(new java.awt.Dimension(82, 25));
		circComboBox.setPreferredSize(new java.awt.Dimension(82, 25));

		javax.swing.GroupLayout circPanelLayout = new javax.swing.GroupLayout(
				circPanel);
		circPanel.setLayout(circPanelLayout);
		circPanelLayout
				.setHorizontalGroup(circPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								circPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												circPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																circPanelLayout
																		.createSequentialGroup()
																		.addGroup(
																				circPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								jLabel20,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								171,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								jLabel16,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								jLabel17,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								jLabel18,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								jLabel19,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(
																				circPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								false)
																						.addComponent(
																								sacTextField,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								130,
																								Short.MAX_VALUE)
																						.addComponent(
																								excTextField)
																						.addComponent(
																								eycTextField)
																						.addComponent(
																								icTextField)
																						.addComponent(
																								raancTextField)))
														.addGroup(
																circPanelLayout
																		.createSequentialGroup()
																		.addGroup(
																				circPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								jLabel21,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								jLabel29,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addGap(36,
																				36,
																				36)
																		.addGroup(
																				circPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								false)
																						.addComponent(
																								latcTextField)
																						.addComponent(
																								circComboBox,
																								0,
																								130,
																								Short.MAX_VALUE))))
										.addContainerGap(31, Short.MAX_VALUE)));
		circPanelLayout
				.setVerticalGroup(circPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								circPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												circPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel16,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																sacTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												circPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel17,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																excTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												circPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel18,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																eycTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												circPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel19,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																icTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												circPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel20,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																raancTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												circPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel21,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																latcTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												circPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel29,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																circComboBox,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap(14, Short.MAX_VALUE)));

		iniTabbedPane.addTab("Circular", circPanel);

		equiPanel.setMinimumSize(new java.awt.Dimension(283, 163));
		equiPanel.setPreferredSize(new java.awt.Dimension(373, 163));

		jLabel22.setText("Semimajor axis [m]:");
		jLabel22.setMaximumSize(new java.awt.Dimension(114, 16));
		jLabel22.setMinimumSize(new java.awt.Dimension(114, 16));
		jLabel22.setPreferredSize(new java.awt.Dimension(114, 16));

		jLabel23.setText("Excentricity ex:");
		jLabel23.setMaximumSize(new java.awt.Dimension(87, 16));
		jLabel23.setMinimumSize(new java.awt.Dimension(87, 16));
		jLabel23.setPreferredSize(new java.awt.Dimension(87, 16));

		jLabel24.setText("Excentricity ey:");
		jLabel24.setMaximumSize(new java.awt.Dimension(86, 16));
		jLabel24.setMinimumSize(new java.awt.Dimension(86, 16));
		jLabel24.setPreferredSize(new java.awt.Dimension(86, 16));

		jLabel25.setText("Inclinaison hx:");
		jLabel25.setMaximumSize(new java.awt.Dimension(81, 16));
		jLabel25.setMinimumSize(new java.awt.Dimension(81, 16));
		jLabel25.setPreferredSize(new java.awt.Dimension(81, 16));

		jLabel27.setText("Longitude argument [deg]:");
		jLabel27.setMaximumSize(new java.awt.Dimension(149, 16));
		jLabel27.setMinimumSize(new java.awt.Dimension(149, 16));
		jLabel27.setPreferredSize(new java.awt.Dimension(149, 16));

		jLabel30.setText("Type of longitude:");
		jLabel30.setMaximumSize(new java.awt.Dimension(99, 16));
		jLabel30.setMinimumSize(new java.awt.Dimension(99, 16));
		jLabel30.setPreferredSize(new java.awt.Dimension(99, 16));

		jLabel26.setText("Inclinaison hy:");
		jLabel26.setMaximumSize(new java.awt.Dimension(80, 16));
		jLabel26.setMinimumSize(new java.awt.Dimension(80, 16));
		jLabel26.setPreferredSize(new java.awt.Dimension(80, 16));

		equiComboBox.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "Mean", "True", "Eccentric" }));
		equiComboBox.setMinimumSize(new java.awt.Dimension(82, 25));
		equiComboBox.setPreferredSize(new java.awt.Dimension(82, 25));

		javax.swing.GroupLayout equiPanelLayout = new javax.swing.GroupLayout(
				equiPanel);
		equiPanel.setLayout(equiPanelLayout);
		equiPanelLayout
				.setHorizontalGroup(equiPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								equiPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												equiPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING,
																false)
														.addGroup(
																equiPanelLayout
																		.createSequentialGroup()
																		.addGroup(
																				equiPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.TRAILING)
																						.addComponent(
																								jLabel22,
																								javax.swing.GroupLayout.Alignment.LEADING,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								jLabel23,
																								javax.swing.GroupLayout.Alignment.LEADING,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								jLabel24,
																								javax.swing.GroupLayout.Alignment.LEADING,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								jLabel25,
																								javax.swing.GroupLayout.Alignment.LEADING,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								jLabel26,
																								javax.swing.GroupLayout.Alignment.LEADING,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								jLabel30,
																								javax.swing.GroupLayout.Alignment.LEADING,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addGap(39,
																				39,
																				39)
																		.addGroup(
																				equiPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								ieTextField,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								130,
																								Short.MAX_VALUE)
																						.addComponent(
																								eyeTextField)
																						.addComponent(
																								exeTextField)
																						.addComponent(
																								saeTextField)
																						.addComponent(
																								raaneTextField)
																						.addComponent(
																								equiComboBox,
																								0,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)))
														.addGroup(
																equiPanelLayout
																		.createSequentialGroup()
																		.addComponent(
																				jLabel27,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				lateTextField)))
										.addGap(0, 53, Short.MAX_VALUE)));
		equiPanelLayout
				.setVerticalGroup(equiPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								equiPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												equiPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel22,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																saeTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												equiPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel23,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																exeTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												equiPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																eyeTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel24,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												equiPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																ieTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel25,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												equiPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel26,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																raaneTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												equiPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel27,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																lateTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												equiPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel30,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																equiComboBox,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(67, 67, 67)));

		iniTabbedPane.addTab("Equinoctial", equiPanel);

		outsidePanel.setMaximumSize(new java.awt.Dimension(2147483647,
				2147483647));
		outsidePanel.setMinimumSize(new java.awt.Dimension(22, 96));
		outsidePanel.setPreferredSize(new java.awt.Dimension(384, 281));
		outsidePanel.setLayout(new java.awt.BorderLayout());
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Options");
		JMenuItem menuItem = new JMenuItem("Load Custom TLE Data File");
		menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/icons/gnome_2_18/folder-open.png")));

		menuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				loadCustomTLEMenuItemActionPerformed(evt);
			}
		});

		menu.add(menuItem);
		menuBar.add(menu);
		menuBar.setBounds(0, 0, 441, 21);
		outsidePanel.add(menuBar, BorderLayout.NORTH);

		satTree.setToolTipText("Drag Satellite(s) over to Object List");
		satTree.setDragEnabled(true);
		satTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
			public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
				satTreeValueChanged(evt);
			}
		});
		jScrollPane2.setViewportView(satTree);

		tleOutputTextArea.setColumns(20);
		tleOutputTextArea.setRows(5);
		tleOutputTextArea.setToolTipText("Satellite TLE");
		jScrollPane4.setViewportView(tleOutputTextArea);

		javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(
				jPanel4);
		jPanel4.setLayout(jPanel4Layout);
		jPanel4Layout
				.setHorizontalGroup(jPanel4Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel4Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanel4Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jScrollPane2,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																292,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jScrollPane4,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																292,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap(44, Short.MAX_VALUE)));
		jPanel4Layout
				.setVerticalGroup(jPanel4Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel4Layout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(
												jScrollPane2,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												131, Short.MAX_VALUE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jScrollPane4,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												47,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addContainerGap()));

		outsidePanel.add(jPanel4, java.awt.BorderLayout.CENTER);

		iniTabbedPane.addTab("TLE", outsidePanel);

		jPanel3.setMaximumSize(new java.awt.Dimension(293, 91));
		jPanel3.setMinimumSize(new java.awt.Dimension(293, 91));
		jPanel3.setPreferredSize(new java.awt.Dimension(293, 91));

		jLabel2.setText("Epoch:");
		jLabel2.setMaximumSize(new java.awt.Dimension(38, 16));
		jLabel2.setMinimumSize(new java.awt.Dimension(38, 16));
		jLabel2.setPreferredSize(new java.awt.Dimension(38, 16));

		jLabel31.setText("Mu:");
		jLabel31.setMaximumSize(new java.awt.Dimension(20, 16));
		jLabel31.setMinimumSize(new java.awt.Dimension(20, 16));
		jLabel31.setPreferredSize(new java.awt.Dimension(20, 16));

		jLabel1.setText("Frame Type:");
		jLabel1.setMaximumSize(new java.awt.Dimension(69, 16));
		jLabel1.setMinimumSize(new java.awt.Dimension(69, 16));
		jLabel1.setPreferredSize(new java.awt.Dimension(69, 16));

		muTextField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				muTextFieldActionPerformed(evt);
			}
		});

		inputComboBox.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "EME2000", "CIRF2000", "GCRF", "MOD", "TEME",
						"TOD", "Veis1950" }));
		inputComboBox.setMinimumSize(new java.awt.Dimension(81, 25));
		inputComboBox.setPreferredSize(new java.awt.Dimension(81, 25));

		javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(
				jPanel3);
		jPanel3.setLayout(jPanel3Layout);
		jPanel3Layout
				.setHorizontalGroup(jPanel3Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel3Layout
										.createSequentialGroup()
										.addGroup(
												jPanel3Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																jPanel3Layout
																		.createSequentialGroup()
																		.addGap(24,
																				24,
																				24)
																		.addGroup(
																				jPanel3Layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								jLabel31,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								jLabel2,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addGap(35,
																				35,
																				35))
														.addGroup(
																javax.swing.GroupLayout.Alignment.TRAILING,
																jPanel3Layout
																		.createSequentialGroup()
																		.addContainerGap()
																		.addComponent(
																				jLabel1,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
										.addGroup(
												jPanel3Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.TRAILING)
														.addComponent(
																muTextField,
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																epochTextField)
														.addComponent(
																inputComboBox,
																0,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE))
										.addGap(29, 29, 29)));
		jPanel3Layout
				.setVerticalGroup(jPanel3Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel3Layout
										.createSequentialGroup()
										.addGap(7, 7, 7)
										.addGroup(
												jPanel3Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel2,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																epochTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel3Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jLabel31,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																muTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel3Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																inputComboBox,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jLabel1,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap(21, Short.MAX_VALUE)));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.TRAILING)
												.addGroup(
														layout.createSequentialGroup()
																.addComponent(
																		okButton,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		javax.swing.GroupLayout.DEFAULT_SIZE,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(
																		cancelButton,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		javax.swing.GroupLayout.DEFAULT_SIZE,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(
																		applyButton,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		javax.swing.GroupLayout.DEFAULT_SIZE,
																		javax.swing.GroupLayout.PREFERRED_SIZE))
												.addGroup(
														layout.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING,
																false)
																.addComponent(
																		iniTabbedPane,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		351,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addComponent(
																		jPanel3,
																		javax.swing.GroupLayout.DEFAULT_SIZE,
																		351,
																		Short.MAX_VALUE)))
								.addContainerGap(
										javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)));
		layout.setVerticalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addGap(6, 6, 6)
								.addComponent(iniTabbedPane,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										234,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jPanel3,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										105,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(
														okButton,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														cancelButton,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														Short.MAX_VALUE)
												.addComponent(
														applyButton,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE))
								.addContainerGap()));
	}// </editor-fold>//GEN-END:initComponents

	private void kepComboBoxActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_kepComboBoxActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_kepComboBoxActionPerformed

	private void dzTextFieldActionPerformed_FormGen(
			java.awt.event.ActionEvent evt) {// GEN-FIRST:event_dzTextFieldActionPerformed_FormGen
		// TODO add your handling code here:
	}// GEN-LAST:event_dzTextFieldActionPerformed_FormGen

	private void muTextFieldActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_muTextFieldActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_muTextFieldActionPerformed

	private void satTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {// GEN-FIRST:event_satTreeValueChanged
		// TODO add your handling code here:
	}// GEN-LAST:event_satTreeValueChanged

	private void loadCustomTLEMenuItemActionPerformed(
			java.awt.event.ActionEvent evt)// GEN-FIRST:event_loadCustomTLEMenuItemActionPerformed
	{// GEN-HEADEREND:event_loadCustomTLEMenuItemActionPerformed
		// ask user to browse for file (.tle, .dat, .txt, other?)
		final JFileChooser fc = new JFileChooser(
				new TLEDownloader().getTleFilePath(0));
		CustomFileFilter xmlFilter = new CustomFileFilter("tle", "*.tle");
		fc.addChoosableFileFilter(xmlFilter);
		xmlFilter = new CustomFileFilter("dat", "*.dat");
		fc.addChoosableFileFilter(xmlFilter);
		xmlFilter = new CustomFileFilter("txt", "*.txt");
		fc.addChoosableFileFilter(xmlFilter);

		int returnVal = fc.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();

			Boolean result = sbtdl.loadTLEDataFile(file, "Custom", null);
			// System.out.println("Result Loading: " + result.toString());
		}

	}// GEN-LAST:event_loadCustomTLEMenuItemActionPerformed

	private void xTextFieldActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_xTextFieldActionPerformed
	{// GEN-HEADEREND:event_xTextFieldActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_xTextFieldActionPerformed

	private void yTextFieldActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_yTextFieldActionPerformed
	{// GEN-HEADEREND:event_yTextFieldActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_yTextFieldActionPerformed

	private void zTextFieldActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_zTextFieldActionPerformed
	{// GEN-HEADEREND:event_zTextFieldActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_zTextFieldActionPerformed

	private void dzTextFieldActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_dzTextFieldActionPerformed
	{// GEN-HEADEREND:event_dzTextFieldActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_dzTextFieldActionPerformed

	private void dyTextFieldActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_dyTextFieldActionPerformed
	{// GEN-HEADEREND:event_dyTextFieldActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_dyTextFieldActionPerformed

	private void dxTextFieldActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_dxTextFieldActionPerformed
	{// GEN-HEADEREND:event_dxTextFieldActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_dxTextFieldActionPerformed

	private void saTextFieldActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_saTextFieldActionPerformed
	{// GEN-HEADEREND:event_saTextFieldActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_saTextFieldActionPerformed

	private void eTextFieldActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_eTextFieldActionPerformed
	{// GEN-HEADEREND:event_eTextFieldActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_eTextFieldActionPerformed

	private void iTextFieldActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_iTextFieldActionPerformed
	{// GEN-HEADEREND:event_iTextFieldActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_iTextFieldActionPerformed

	private void longTextFieldActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_longTextFieldActionPerformed
	{// GEN-HEADEREND:event_longTextFieldActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_longTextFieldActionPerformed

	private void argTextFieldActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_argTextFieldActionPerformed
	{// GEN-HEADEREND:event_argTextFieldActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_argTextFieldActionPerformed

	private void mTextFieldActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_mTextFieldActionPerformed
	{// GEN-HEADEREND:event_mTextFieldActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_mTextFieldActionPerformed

	private void inputComboBoxActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_inputComboBoxActionPerformed
	{// GEN-HEADEREND:event_inputComboBoxActionPerformed
	}// GEN-LAST:event_inputComboBoxActionPerformed

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_okButtonActionPerformed
	{// GEN-HEADEREND:event_okButtonActionPerformed
		boolean success = false;
		try {
			success = saveSettings();
		} catch (OrekitException e1) {
			JOptionPane.showMessageDialog(this, e1.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}

		// close internal frame
		if (success) {
			try {
				iframe.dispose(); // could setClosed(true)
			} catch (Exception e) {
			}
		}
	}// GEN-LAST:event_okButtonActionPerformed

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_cancelButtonActionPerformed
	{// GEN-HEADEREND:event_cancelButtonActionPerformed
		// close internal frame
		try {
			iframe.dispose(); // could setClosed(true)
		} catch (Exception e) {
		}
	}// GEN-LAST:event_cancelButtonActionPerformed

	private void applyButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_applyButtonActionPerformed
	{// GEN-HEADEREND:event_applyButtonActionPerformed
		try {
			saveSettings();
		} catch (OrekitException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}// GEN-LAST:event_applyButtonActionPerformed

	private void iniTabbedPaneStateChanged(ChangeEvent evt) {

		double[] ret = null;
		PVCoordinates retCart = null;
		// Enable epoch, mu & frame type in IHM
		jPanel3.setVisible(true);

		// Transmit the position angle to other panel
		switch (icNode.getPositionAngle()) {

		case MEAN:
			kepComboBox.setSelectedIndex(0);
			circComboBox.setSelectedIndex(0);
			equiComboBox.setSelectedIndex(0);

			break;

		case TRUE:
			kepComboBox.setSelectedIndex(1);
			circComboBox.setSelectedIndex(1);
			equiComboBox.setSelectedIndex(1);
			break;

		case ECCENTRIC:
			kepComboBox.setSelectedIndex(2);
			circComboBox.setSelectedIndex(2);
			equiComboBox.setSelectedIndex(2);
			break;
		}

		// Convert element to other coordinate system
		switch (iniTabbedPane.getSelectedIndex()) {
		case 0:
			// Convert label to keplerian element
			ret = icNode.convertToKeplerian();
			setKepElementsInGUI(ret);
			break;
		case 1:
			// Convert label to Cartesian element
			retCart = icNode.convertToCartesian();
			setCatesianElementsInGui(retCart);
			break;
		case 2:
			// Convert label to circular element
			ret = icNode.convertToCircular();
			setCircularElementsInGUI(ret);

			break;
		case 3:
			// Convert label to equinoctial element
			ret = icNode.convertToEquinoctial();
			setEquinoctialElementsInGUI(ret);
			break;
		case 4:
			// TLE panel
			// Disable epoch, mu & frame type in IHM
			jPanel3.setVisible(false);

			break;

		default:
			break;

		}

	}

	private boolean saveSettings() throws OrekitException {
		// save settings back to Node
		boolean saveSuccess = true;

		try {

			// Wich coordinate system
			icNode.setCoordinate(iniTabbedPane.getSelectedIndex());

			// if not TLE
			if (iniTabbedPane.getSelectedIndex() != 4) {
				// save epoch
				saveSuccess = saveEpoch();

				// Save mu
				icNode.setMu(Double.parseDouble(muTextField.getText()));

				switch (inputComboBox.getSelectedIndex()) {

				case 0:

					icNode.setFrame(FramesFactory.getEME2000());
					break;

				case 1:
					icNode.setFrame(FramesFactory
							.getCIRF2000(IERSConventions.IERS_2010));
					break;

				case 2:
					icNode.setFrame(FramesFactory.getGCRF());
					break;

				case 3:
					icNode.setFrame(FramesFactory.getMOD(true));
					break;
				case 4:
					icNode.setFrame(FramesFactory.getTEME());
					break;
				case 5:
					icNode.setFrame(FramesFactory.getTOD(true));
					break;
				case 6:
					icNode.setFrame(FramesFactory.getVeis1950());
					break;

				}
			}

			// get correct coordinate system elements & save them
			switch (iniTabbedPane.getSelectedIndex()) {

			case 0:
				// Keplerian system

				// Which position angle
				switch (kepComboBox.getSelectedIndex()) {

				case 0:

					icNode.setPositionAngle(PositionAngle.MEAN);
					break;

				case 1:
					icNode.setPositionAngle(PositionAngle.TRUE);
					break;

				case 2:
					icNode.setPositionAngle(PositionAngle.ECCENTRIC);
					break;

				}

				// fill in fields with saved values
				double[] kep = this.getKepElementsFromGUI();
				icNode.setKeplarianElements(kep);

				break;

			case 1:
				// Cartesian system
				PVCoordinates j2k = this.getCartesianElementsInGUI();
				icNode.setCartesianElements(j2k);

				break;

			case 2:
				// Circular system

				// Which position angle
				switch (circComboBox.getSelectedIndex()) {

				case 0:

					icNode.setPositionAngle(PositionAngle.MEAN);
					break;

				case 1:
					icNode.setPositionAngle(PositionAngle.TRUE);
					break;

				case 2:
					icNode.setPositionAngle(PositionAngle.ECCENTRIC);
					break;

				}

				double[] circu = this.getCircuElementsFromGUI();
				icNode.setCircularElements(circu);

				break;

			case 3:
				// Equinoctial system

				// Which position angle
				switch (equiComboBox.getSelectedIndex()) {

				case 0:

					icNode.setPositionAngle(PositionAngle.MEAN);
					break;

				case 1:
					icNode.setPositionAngle(PositionAngle.TRUE);
					break;

				case 2:
					icNode.setPositionAngle(PositionAngle.ECCENTRIC);
					break;

				}

				double[] equi = this.getEquinoctialElementsFromGUI();
				icNode.setEquinoctialElements(equi);

				break;

			case 4:
				// TLE
				try {

					// get the satellite name
					String satName = satTree.getLastSelectedPathComponent()
							.toString();

					// Check if the user has selected a leaf and not a node
					if (!tleHash.containsKey(satName)) {
						throw new Exception();

					}

					icNode.setSatelliteTleElements(tleHash.get(satName));
					icNode.setSatelliteTleName(satName);

					// Save the current satellite TLE selection
					TreePath treePath = satTree.getLeadSelectionPath();

					ArrayList<Integer> lastTreeSelection = new ArrayList<Integer>();

					while (null != treePath) {

						lastTreeSelection.add(satTree.getRowForPath(treePath));
						treePath = treePath.getParentPath();
					}
					icNode.setLastTreeSelection(lastTreeSelection);

					// Set the name of the selected satellite in the panel
					icNode.getjSatConfigPanel().setSatNameTextField(
							satName.trim());
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this,
							"Please select a satellite", "Information",
							JOptionPane.INFORMATION_MESSAGE);
					saveSuccess = false;
				}

				break;

			default:
				break;

			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					"Coordinate format error, check input.", "Data ERROR",
					JOptionPane.ERROR_MESSAGE);
			saveSuccess = false;
		}

		return saveSuccess;

	}

	// date formats for displaying and reading in
	private SimpleDateFormat dateformat = new SimpleDateFormat(
			"dd MMM yyyy HH:mm:ss.SSS z");
	private SimpleDateFormat dateformatShort1 = new SimpleDateFormat(
			"dd MMM y H:m:s.S z");
	private SimpleDateFormat dateformatShort2 = new SimpleDateFormat(
			"dd MMM y H:m:s z"); // no Milliseconds

	private boolean saveEpoch() throws OrekitException {

		// enter hit in date/time box...
		// System.out.println("Date Time Changed");

		GregorianCalendar currentTimeDate = new GregorianCalendar(
				TimeZone.getTimeZone("UTC"));
		// or
		// GregorianCalendar currentTimeDate = new GregorianCalendar();

		boolean dateAccepted = true; // assume date valid at first
		try {
			currentTimeDate.setTime(dateformatShort1.parse(epochTextField
					.getText()));
			epochTextField
					.setText(dateformat.format(currentTimeDate.getTime()));
		} catch (Exception e2) {
			try {
				// try reading without the milliseconds
				currentTimeDate.setTime(dateformatShort2.parse(epochTextField
						.getText()));
				epochTextField.setText(dateformat.format(currentTimeDate
						.getTime()));
			} catch (Exception e3) {
				// bad date input put back the old date string
				epochTextField.setText(scenarioEpochDate
						.convertJD2String(icNode.getIniJulDate()));
				dateAccepted = false;
				// System.out.println(" -- Rejected");
			} // catch 2

		} // catch 1

		if (dateAccepted) {
			// date entered was good...
			// System.out.println(" -- Accepted");

			// save

			Time newTime = new Time();
			newTime.set(currentTimeDate.getTimeInMillis());

			icNode.setIniJulDate(newTime.getJulianDate());
			icNode.setAbsoluteDate(newTime.getJulianDate());

		} // if date accepted
		else {
			JOptionPane.showMessageDialog(this, "Epoch Date format incorrect",
					"Epoch ERROR", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton applyButton;
	private javax.swing.JTextField argTextField;
	private javax.swing.JButton cancelButton;
	private javax.swing.JComboBox circComboBox;
	private javax.swing.JPanel circPanel;
	private javax.swing.JTextField dxTextField;
	private javax.swing.JTextField dyTextField;
	private javax.swing.JTextField dzTextField;
	private javax.swing.JTextField eTextField;
	private javax.swing.JTextField epochTextField;
	private javax.swing.JComboBox equiComboBox;
	private javax.swing.JPanel equiPanel;
	private javax.swing.JTextField excTextField;
	private javax.swing.JTextField exeTextField;
	private javax.swing.JTextField eycTextField;
	private javax.swing.JTextField eyeTextField;
	private javax.swing.JTextField iTextField;
	private javax.swing.JTextField icTextField;
	private javax.swing.JTextField ieTextField;
	private javax.swing.JTabbedPane iniTabbedPane;
	private javax.swing.JComboBox inputComboBox;
	private javax.swing.JPanel j2kPanel;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel10;
	private javax.swing.JLabel jLabel11;
	private javax.swing.JLabel jLabel12;
	private javax.swing.JLabel jLabel13;
	private javax.swing.JLabel jLabel14;
	private javax.swing.JLabel jLabel15;
	private javax.swing.JLabel jLabel16;
	private javax.swing.JLabel jLabel17;
	private javax.swing.JLabel jLabel18;
	private javax.swing.JLabel jLabel19;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel20;
	private javax.swing.JLabel jLabel21;
	private javax.swing.JLabel jLabel22;
	private javax.swing.JLabel jLabel23;
	private javax.swing.JLabel jLabel24;
	private javax.swing.JLabel jLabel25;
	private javax.swing.JLabel jLabel26;
	private javax.swing.JLabel jLabel27;
	private javax.swing.JLabel jLabel28;
	private javax.swing.JLabel jLabel29;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel30;
	private javax.swing.JLabel jLabel31;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JLabel jLabel7;
	private javax.swing.JLabel jLabel8;
	private javax.swing.JLabel jLabel9;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JScrollPane jScrollPane4;
	private javax.swing.JComboBox kepComboBox;
	private javax.swing.JPanel kepPanel;
	private javax.swing.JTextField latcTextField;
	private javax.swing.JTextField lateTextField;
	private javax.swing.JTextField longTextField;
	private javax.swing.JTextField mTextField;
	private javax.swing.JTextField muTextField;
	private javax.swing.JButton okButton;
	private javax.swing.JPanel outsidePanel;
	private javax.swing.JTextField raancTextField;
	private javax.swing.JTextField raaneTextField;
	private javax.swing.JTextField saTextField;
	private javax.swing.JTextField sacTextField;
	private javax.swing.JTextField saeTextField;
	private javax.swing.JTree satTree;
	private javax.swing.JTextArea tleOutputTextArea;
	private javax.swing.JTextField xTextField;
	private javax.swing.JTextField yTextField;
	private javax.swing.JTextField zTextField;

	// End of variables declaration//GEN-END:variables

	private double[] getKepElementsFromGUI() {
		double[] kepElements = new double[6];

		kepElements[0] = Double.parseDouble(saTextField.getText());
		kepElements[1] = Double.parseDouble(eTextField.getText());
		kepElements[2] = Double.parseDouble(iTextField.getText()) * Math.PI
				/ 180.0;
		kepElements[3] = Double.parseDouble(longTextField.getText()) * Math.PI
				/ 180.0;
		kepElements[4] = Double.parseDouble(argTextField.getText()) * Math.PI
				/ 180.0;
		kepElements[5] = Double.parseDouble(mTextField.getText()) * Math.PI
				/ 180.0;

		return kepElements;
	}

	private void setKepElementsInGUI(double[] kep) {
		saTextField.setText(kep[0] + "");
		eTextField.setText(kep[1] + "");
		iTextField.setText((kep[2] * 180.0 / Math.PI) + "");
		longTextField.setText((kep[3] * 180.0 / Math.PI) + "");
		argTextField.setText((kep[4] * 180.0 / Math.PI) + "");
		mTextField.setText((kep[5] * 180.0 / Math.PI) + "");
	}

	private PVCoordinates getCartesianElementsInGUI() {

		Vector3D position = new Vector3D(Double.parseDouble(xTextField
				.getText()), Double.parseDouble(yTextField.getText()),
				Double.parseDouble(zTextField.getText()));
		Vector3D velocity = new Vector3D(Double.parseDouble(dxTextField
				.getText()), Double.parseDouble(dyTextField.getText()),
				Double.parseDouble(dzTextField.getText()));

		PVCoordinates state = new PVCoordinates(position, velocity);

		return state;
	}

	private void setCatesianElementsInGui(PVCoordinates state) {

		Vector3D position = state.getPosition();
		Vector3D velocity = state.getVelocity();

		xTextField.setText(position.getX() + "");
		yTextField.setText(position.getY() + "");
		zTextField.setText(position.getZ() + "");
		dxTextField.setText(velocity.getX() + "");
		dyTextField.setText(velocity.getY() + "");
		dzTextField.setText(velocity.getZ() + "");
	}

	private double[] getCircuElementsFromGUI() {
		double[] circuElements = new double[6];

		circuElements[0] = Double.parseDouble(sacTextField.getText());
		circuElements[1] = Double.parseDouble(excTextField.getText());
		circuElements[2] = Double.parseDouble(eycTextField.getText());
		circuElements[3] = Double.parseDouble(icTextField.getText()) * Math.PI
				/ 180.0;
		circuElements[4] = Double.parseDouble(raancTextField.getText())
				* Math.PI / 180.0;
		circuElements[5] = Double.parseDouble(latcTextField.getText())
				* Math.PI / 180.0;

		return circuElements;
	}

	private void setCircularElementsInGUI(double[] circu) {
		sacTextField.setText(circu[0] + "");
		excTextField.setText(circu[1] + "");
		eycTextField.setText(circu[2] + "");
		icTextField.setText((circu[3] * 180.0 / Math.PI) + "");
		raancTextField.setText((circu[4] * 180.0 / Math.PI) + "");
		latcTextField.setText((circu[5] * 180.0 / Math.PI) + "");
	}

	private double[] getEquinoctialElementsFromGUI() {
		double[] equiElements = new double[6];

		equiElements[0] = Double.parseDouble(saeTextField.getText());
		equiElements[1] = Double.parseDouble(exeTextField.getText());
		equiElements[2] = Double.parseDouble(eyeTextField.getText());
		equiElements[3] = Double.parseDouble(ieTextField.getText());
		equiElements[4] = Double.parseDouble(raaneTextField.getText());
		equiElements[5] = Double.parseDouble(lateTextField.getText()) * Math.PI
				/ 180.0;

		return equiElements;
	}

	private void setEquinoctialElementsInGUI(double[] equi) {
		saeTextField.setText(equi[0] + "");
		exeTextField.setText(equi[1] + "");
		eyeTextField.setText(equi[2] + "");
		ieTextField.setText(equi[3] + "");
		raaneTextField.setText(equi[4] + "");
		lateTextField.setText((equi[5] * 180.0 / Math.PI) + "");
	}

	public JInternalFrame getIframe() {
		return iframe;
	}

	public void setIframe(JInternalFrame iframe) {
		this.iframe = iframe;
	}

	public void expand(TreePath path) {

		satTree.expandPath(path);
	}

	// is path1 descendant of path2
	public static boolean isDescendant(TreePath path1, TreePath path2) {
		int count1 = path1.getPathCount();
		int count2 = path2.getPathCount();
		if (count1 <= count2) {
			return false;
		}
		while (count1 != count2) {
			path1 = path1.getParentPath();
			count1--;
		}
		return path1.equals(path2);
	}

	public static String getExpansionState(JTree tree, int row) {
		TreePath rowPath = tree.getPathForRow(row);
		StringBuffer buf = new StringBuffer();
		int rowCount = tree.getRowCount();
		for (int i = row; i < rowCount; i++) {
			TreePath path = tree.getPathForRow(i);
			if (i == row || isDescendant(path, rowPath)) {
				if (tree.isExpanded(path)) {
					buf.append("," + String.valueOf(i - row));
				}
			} else {
				break;
			}
		}
		return buf.toString();
	}

	public static void restoreExpanstionState(JTree tree, int row,
			String expansionState) {
		StringTokenizer stok = new StringTokenizer(expansionState, ",");
		while (stok.hasMoreTokens()) {
			int token = row + Integer.parseInt(stok.nextToken());
			tree.expandRow(token);
		}
	}
}
