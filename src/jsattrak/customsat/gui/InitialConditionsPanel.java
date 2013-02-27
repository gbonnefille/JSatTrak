/*
 * InitialConditionsPanel.java
 * =====================================================================
 * Copyright (C) 2009 Shawn E. Gano
 * 
 * This file is part of JSatTrak.
 * 
 * JSatTrak is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JSatTrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with JSatTrak.  If not, see <http://www.gnu.org/licenses/>.
 * =====================================================================
 *
 * Created on January 11, 2008, 3:04 PM
 */

package jsattrak.customsat.gui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
import org.orekit.orbits.PositionAngle;
import org.orekit.utils.PVCoordinates;

/**
 * 
 * @author sgano
 */
public class InitialConditionsPanel extends javax.swing.JPanel {

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
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		applyButton = new javax.swing.JButton();
		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		inputComboBox = new javax.swing.JComboBox();
		iniTabbedPane = new javax.swing.JTabbedPane();
		kepPanel = new javax.swing.JPanel();
		jLabel10 = new javax.swing.JLabel();
		saTextField = new javax.swing.JTextField();
		jLabel11 = new javax.swing.JLabel();
		eTextField = new javax.swing.JTextField();
		jLabel12 = new javax.swing.JLabel();
		iTextField = new javax.swing.JTextField();
		jLabel13 = new javax.swing.JLabel();
		longTextField = new javax.swing.JTextField();
		jLabel14 = new javax.swing.JLabel();
		argTextField = new javax.swing.JTextField();
		jLabel15 = new javax.swing.JLabel();

		jLabel28 = new javax.swing.JLabel();
		kepComboBox = new javax.swing.JComboBox();

		// Circular orbit
		jLabel16 = new javax.swing.JLabel();
		jLabel17 = new javax.swing.JLabel();
		jLabel18 = new javax.swing.JLabel();
		jLabel19 = new javax.swing.JLabel();
		jLabel20 = new javax.swing.JLabel();
		jLabel21 = new javax.swing.JLabel();

		sacTextField = new javax.swing.JTextField();
		excTextField = new javax.swing.JTextField();
		eycTextField = new javax.swing.JTextField();
		icTextField = new javax.swing.JTextField();
		raancTextField = new javax.swing.JTextField();
		latcTextField = new javax.swing.JTextField();

		jLabel29 = new javax.swing.JLabel();
		circComboBox = new javax.swing.JComboBox();

		circPanel = new javax.swing.JPanel();

		// Equinoctial orbit
		jLabel22 = new javax.swing.JLabel();
		jLabel23 = new javax.swing.JLabel();
		jLabel24 = new javax.swing.JLabel();
		jLabel25 = new javax.swing.JLabel();
		jLabel26 = new javax.swing.JLabel();
		jLabel27 = new javax.swing.JLabel();

		saeTextField = new javax.swing.JTextField();
		exeTextField = new javax.swing.JTextField();
		eyeTextField = new javax.swing.JTextField();
		ieTextField = new javax.swing.JTextField();
		raaneTextField = new javax.swing.JTextField();
		lateTextField = new javax.swing.JTextField();

		jLabel30 = new javax.swing.JLabel();
		equiComboBox = new javax.swing.JComboBox();

		equiPanel = new javax.swing.JPanel();

		// TLE orbit
		tlePanel = new javax.swing.JPanel();

		// mu
		jLabel31 = new javax.swing.JLabel();
		muTextField = new javax.swing.JTextField();

		mTextField = new javax.swing.JTextField();
		j2kPanel = new javax.swing.JPanel();
		jLabel3 = new javax.swing.JLabel();
		jLabel4 = new javax.swing.JLabel();
		jLabel5 = new javax.swing.JLabel();
		jLabel6 = new javax.swing.JLabel();
		xTextField = new javax.swing.JTextField();
		yTextField = new javax.swing.JTextField();
		zTextField = new javax.swing.JTextField();
		jLabel7 = new javax.swing.JLabel();
		dzTextField = new javax.swing.JTextField();
		dyTextField = new javax.swing.JTextField();
		jLabel8 = new javax.swing.JLabel();
		jLabel9 = new javax.swing.JLabel();
		dxTextField = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		epochTextField = new javax.swing.JTextField();

		applyButton.setText("Apply");
		applyButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				applyButtonActionPerformed(evt);
			}
		});

		okButton.setText("Ok");
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				okButtonActionPerformed(evt);
			}
		});

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		jLabel10.setText("Semimajor axis [m]:");

		saTextField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saTextFieldActionPerformed(evt);
			}
		});

		jLabel11.setText("Eccentricity:");

		eTextField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				eTextFieldActionPerformed(evt);
			}
		});

		jLabel12.setText("Inclination [deg]:");

		iTextField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				iTextFieldActionPerformed(evt);
			}
		});

		jLabel13.setText("Long. of asc. node [deg]:");

		longTextField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				longTextFieldActionPerformed(evt);
			}
		});

		jLabel14.setText("Arg. of perigee [deg]:");

		argTextField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				argTextFieldActionPerformed(evt);
			}
		});

		jLabel15.setText("Anomaly [deg]:");

		mTextField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				mTextFieldActionPerformed(evt);
			}
		});

		jLabel28.setText("Type of anomaly:");

		kepComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
				"Mean", "True", "Eccentric" }));

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
														.addComponent(jLabel11)
														.addComponent(jLabel12)
														.addComponent(jLabel10)
														.addComponent(jLabel14)
														.addComponent(jLabel15)
														.addComponent(jLabel13)
														.addComponent(jLabel28))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												kepPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																eTextField,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																172,
																Short.MAX_VALUE)
														.addComponent(
																iTextField,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																172,
																Short.MAX_VALUE)
														.addComponent(
																longTextField,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																172,
																Short.MAX_VALUE)
														.addComponent(
																argTextField,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																172,
																Short.MAX_VALUE)
														.addComponent(
																mTextField,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																172,
																Short.MAX_VALUE)
														.addComponent(
																saTextField,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																172,
																Short.MAX_VALUE)
														.addComponent(
																kepComboBox,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																172,
																Short.MAX_VALUE))
										.addGap(28, 28, 28)));
		kepPanelLayout
				.setVerticalGroup(kepPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								kepPanelLayout
										.createSequentialGroup()
										.addGroup(
												kepPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel10)
														.addComponent(
																saTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(1, 1, 1)
										.addGroup(
												kepPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel11)
														.addComponent(
																eTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(1, 1, 1)
										.addGroup(
												kepPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel12)
														.addComponent(
																iTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(1, 1, 1)
										.addGroup(
												kepPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel13)
														.addComponent(
																longTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(1, 1, 1)
										.addGroup(
												kepPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel14)
														.addComponent(
																argTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(1, 1, 1)
										.addGroup(
												kepPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel15)
														.addComponent(
																mTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(1, 1, 1)
										.addGroup(
												kepPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel28)
														.addComponent(
																kepComboBox,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		iniTabbedPane.addTab("Keplarian", kepPanel);

		jLabel3.setText("X:");

		jLabel4.setText("Y:");

		jLabel5.setText("Z:");

		jLabel6.setFont(new java.awt.Font("Tahoma", 0, 10));
		jLabel6.setForeground(new java.awt.Color(102, 102, 102));
		jLabel6.setText("* Note: Units are in meters");

		xTextField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				xTextFieldActionPerformed(evt);
			}
		});

		yTextField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				yTextFieldActionPerformed(evt);
			}
		});

		zTextField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				zTextFieldActionPerformed(evt);
			}
		});

		jLabel7.setText("dZ:");

		dzTextField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				dzTextFieldActionPerformed(evt);
			}
		});

		dyTextField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				dyTextFieldActionPerformed(evt);
			}
		});

		jLabel8.setText("dY:");

		jLabel9.setText("dX:");

		dxTextField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				dxTextFieldActionPerformed(evt);
			}
		});

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
										.addContainerGap()
										.addGroup(
												j2kPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																j2kPanelLayout
																		.createSequentialGroup()
																		.addGroup(
																				j2kPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addGroup(
																								j2kPanelLayout
																										.createSequentialGroup()
																										.addComponent(
																												jLabel3)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												xTextField,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												127,
																												javax.swing.GroupLayout.PREFERRED_SIZE))
																						.addGroup(
																								j2kPanelLayout
																										.createSequentialGroup()
																										.addComponent(
																												jLabel4)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												yTextField,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												127,
																												javax.swing.GroupLayout.PREFERRED_SIZE))
																						.addGroup(
																								j2kPanelLayout
																										.createSequentialGroup()
																										.addComponent(
																												jLabel5)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												zTextField,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												127,
																												javax.swing.GroupLayout.PREFERRED_SIZE)))
																		.addGap(18,
																				18,
																				18)
																		.addGroup(
																				j2kPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addGroup(
																								j2kPanelLayout
																										.createSequentialGroup()
																										.addComponent(
																												jLabel9)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												dxTextField,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												127,
																												javax.swing.GroupLayout.PREFERRED_SIZE))
																						.addGroup(
																								j2kPanelLayout
																										.createSequentialGroup()
																										.addComponent(
																												jLabel8)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												dyTextField,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												127,
																												javax.swing.GroupLayout.PREFERRED_SIZE))
																						.addGroup(
																								j2kPanelLayout
																										.createSequentialGroup()
																										.addComponent(
																												jLabel7)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												dzTextField,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												127,
																												javax.swing.GroupLayout.PREFERRED_SIZE))))
														.addComponent(jLabel6))
										.addContainerGap(11, Short.MAX_VALUE)));
		j2kPanelLayout
				.setVerticalGroup(j2kPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								j2kPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												j2kPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																j2kPanelLayout
																		.createSequentialGroup()
																		.addGroup(
																				j2kPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.BASELINE)
																						.addComponent(
																								jLabel3)
																						.addComponent(
																								xTextField,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(
																				j2kPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.BASELINE)
																						.addComponent(
																								jLabel4)
																						.addComponent(
																								yTextField,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(
																				j2kPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.BASELINE)
																						.addComponent(
																								jLabel5)
																						.addComponent(
																								zTextField,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE)))
														.addGroup(
																j2kPanelLayout
																		.createSequentialGroup()
																		.addGroup(
																				j2kPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.BASELINE)
																						.addComponent(
																								jLabel9)
																						.addComponent(
																								dxTextField,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(
																				j2kPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.BASELINE)
																						.addComponent(
																								jLabel8)
																						.addComponent(
																								dyTextField,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(
																				j2kPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.BASELINE)
																						.addComponent(
																								jLabel7)
																						.addComponent(
																								dzTextField,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE))))
										.addGap(18, 18, 18)
										.addComponent(jLabel6)
										.addContainerGap(22, Short.MAX_VALUE)));

		iniTabbedPane.addTab("Cartesian", j2kPanel);

		/**
		 * Circular panel
		 */

		jLabel16.setText("Semimajor axis [m]:");

		jLabel17.setText("Excentricity ex:");

		jLabel18.setText("Excentricity ey:");

		jLabel19.setText("Inclinaison [deg]:");

		jLabel20.setText("Right ascension of asc. node [deg]:");

		jLabel21.setText("Latitude argument [deg]:");

		jLabel29.setText("Type of latitude:");

		circComboBox.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "Mean", "True", "Eccentric" }));

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
														.addComponent(jLabel16)
														.addComponent(jLabel17)
														.addComponent(jLabel18)
														.addComponent(jLabel19)
														.addComponent(jLabel20)
														.addComponent(jLabel21)
														.addComponent(jLabel29))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												circPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																sacTextField,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																172,
																Short.MAX_VALUE)
														.addComponent(
																excTextField,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																172,
																Short.MAX_VALUE)
														.addComponent(
																eycTextField,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																172,
																Short.MAX_VALUE)
														.addComponent(
																icTextField,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																172,
																Short.MAX_VALUE)
														.addComponent(
																raancTextField,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																172,
																Short.MAX_VALUE)
														.addComponent(
																latcTextField,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																172,
																Short.MAX_VALUE)
														.addComponent(
																circComboBox,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																172,
																Short.MAX_VALUE))
										.addGap(28, 28, 28)));
		circPanelLayout
				.setVerticalGroup(circPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								circPanelLayout
										.createSequentialGroup()
										.addGroup(
												circPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel16)
														.addComponent(
																sacTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(1, 1, 1)
										.addGroup(
												circPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel17)
														.addComponent(
																excTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(1, 1, 1)
										.addGroup(
												circPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel18)
														.addComponent(
																eycTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(1, 1, 1)
										.addGroup(
												circPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel19)
														.addComponent(
																icTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(1, 1, 1)
										.addGroup(
												circPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel20)
														.addComponent(
																raancTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(1, 1, 1)
										.addGroup(
												circPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel21)
														.addComponent(
																latcTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(1, 1, 1)
										.addGroup(
												circPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel29)
														.addComponent(
																circComboBox,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		iniTabbedPane.addTab("Circular", circPanel);

		/**
		 * Equinoctial panel
		 */

		jLabel22.setText("Semimajor axis [m]:");

		jLabel23.setText("Excentricity ex:");

		jLabel24.setText("Excentricity ey:");

		jLabel25.setText("Inclinaison hx:");

		jLabel26.setText("Inclinaison hy:");

		jLabel27.setText("Longitude argument [deg]:");

		jLabel30.setText("Type of longitude:");

		equiComboBox.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "Mean", "True", "Eccentric" }));

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
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(jLabel22)
														.addComponent(jLabel23)
														.addComponent(jLabel24)
														.addComponent(jLabel25)
														.addComponent(jLabel26)
														.addComponent(jLabel27)
														.addComponent(jLabel30))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												equiPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																saeTextField,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																172,
																Short.MAX_VALUE)
														.addComponent(
																exeTextField,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																172,
																Short.MAX_VALUE)
														.addComponent(
																eyeTextField,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																172,
																Short.MAX_VALUE)
														.addComponent(
																ieTextField,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																172,
																Short.MAX_VALUE)
														.addComponent(
																raaneTextField,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																172,
																Short.MAX_VALUE)
														.addComponent(
																lateTextField,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																172,
																Short.MAX_VALUE)
														.addComponent(
																equiComboBox,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																172,
																Short.MAX_VALUE))
										.addGap(28, 28, 28)));
		equiPanelLayout
				.setVerticalGroup(equiPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								equiPanelLayout
										.createSequentialGroup()
										.addGroup(
												equiPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel22)
														.addComponent(
																saeTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(1, 1, 1)
										.addGroup(
												equiPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel23)
														.addComponent(
																exeTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(1, 1, 1)
										.addGroup(
												equiPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel24)
														.addComponent(
																eyeTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(1, 1, 1)
										.addGroup(
												equiPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel25)
														.addComponent(
																ieTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(1, 1, 1)
										.addGroup(
												equiPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel26)
														.addComponent(
																raaneTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(1, 1, 1)
										.addGroup(
												equiPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel27)
														.addComponent(
																lateTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(1, 1, 1)
										.addGroup(
												equiPanelLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel30)
														.addComponent(
																equiComboBox,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		iniTabbedPane.addTab("Equinoctial", equiPanel);

		/**
		 * TLE panel
		 */

		// Initialize tree
		DefaultTreeModel treeModel;

		// top node
		DefaultMutableTreeNode topTreeNode;

		outsidePanel = new javax.swing.JPanel();
		mainPanel = new javax.swing.JPanel();
		jPanel1 = new javax.swing.JPanel();
		jScrollPane1 = new javax.swing.JScrollPane();
		satTree = new javax.swing.JTree();
		jPanelTle = new javax.swing.JPanel();
		jScrollPane3 = new javax.swing.JScrollPane();
		tleOutputTextArea = new javax.swing.JTextArea();
		jMenuBar1 = new javax.swing.JMenuBar();
		jMenu1 = new javax.swing.JMenu();
		loadCustomTLEMenuItem = new javax.swing.JMenuItem();
		createCustomSatMenuItem = new javax.swing.JMenuItem();

		outsidePanel.setLayout(new java.awt.BorderLayout());

		satTree.setToolTipText("Drag Satellite(s) over to Satellite List");
		satTree.setDragEnabled(true);
		satTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
			public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
				satTreeValueChanged(evt);
			}
		});

		jScrollPane1.setViewportView(satTree);

		// Button
		jMenu1.setText("Options");

		loadCustomTLEMenuItem.setIcon(new javax.swing.ImageIcon(getClass()
				.getResource("/icons/gnome_2_18/folder-open.png"))); // NOI18N
		loadCustomTLEMenuItem.setText("Load Custom TLE Data File");
		loadCustomTLEMenuItem
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						loadCustomTLEMenuItemActionPerformed(evt);
					}
				});
		jMenu1.add(loadCustomTLEMenuItem);
		jMenuBar1.add(jMenu1);

		// panel
		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(
				jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addComponent(
				jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 384,
				Short.MAX_VALUE));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addComponent(
				jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING,
				javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE));

		tleOutputTextArea.setColumns(20);
		tleOutputTextArea.setRows(5);
		tleOutputTextArea.setToolTipText("Satellite TLE");
		jScrollPane3.setViewportView(tleOutputTextArea);

		javax.swing.GroupLayout jPanelTextFieldTleLayout = new javax.swing.GroupLayout(
				jPanelTle);
		jPanelTle.setLayout(jPanelTextFieldTleLayout);
		jPanelTextFieldTleLayout.setHorizontalGroup(jPanelTextFieldTleLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

				.addComponent(jScrollPane3,
						javax.swing.GroupLayout.Alignment.TRAILING,
						javax.swing.GroupLayout.DEFAULT_SIZE, 384,
						Short.MAX_VALUE));
		jPanelTextFieldTleLayout.setVerticalGroup(jPanelTextFieldTleLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

				.addComponent(jScrollPane3,
						javax.swing.GroupLayout.Alignment.TRAILING,
						javax.swing.GroupLayout.DEFAULT_SIZE, 60,
						Short.MAX_VALUE));

		javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(
				mainPanel);
		mainPanel.setLayout(mainPanelLayout);
		mainPanelLayout.setHorizontalGroup(

		mainPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jMenuBar1)
				.addComponent(jPanelTle, javax.swing.GroupLayout.DEFAULT_SIZE,
						javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE,
						javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		mainPanelLayout
				.setVerticalGroup(mainPanelLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)

						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								mainPanelLayout
										.createSequentialGroup()
										.addComponent(jMenuBar1)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jPanel1,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jPanelTle,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)));

		outsidePanel.add(mainPanel, java.awt.BorderLayout.CENTER);

		iniTabbedPane.addTab("TLE", outsidePanel);

		topTreeNode = new DefaultMutableTreeNode("Satellites");

		// create a hashmap of TLEs with key as text from tree
		tleHash = new Hashtable<String, TLElements>();

		treeModel = new DefaultTreeModel(topTreeNode); // create tree model
														// using root node
		satTree.setModel(treeModel); // set the tree's model

		ArrayList<Integer> lastTreeSelection = icNode.getLastTreeSelection();

		sbtdl = new SatBrowserTleDataLoader(app, topTreeNode, tleHash,
				tleOutputTextArea, satTree, lastTreeSelection);

		sbtdl.execute();
		
		 // Drag and Drop Handler
        // setup transfer handler
        satTree.setTransferHandler(new TreeTransferHandler(tleHash));
        
        // allow mutiple selections
        satTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    
        // if local files exist wait for thread to finish before exiting
        TLEDownloader tleDownloader = new TLEDownloader();
        if( (new File(tleDownloader.getLocalPath()).exists()) && (new File(tleDownloader.getTleFilePath(0)).exists()) )
        {
            while(!sbtdl.isDone())
            {
                
                try
                {
                    Thread.sleep(50); // sleep for a little bit to wait for the thread
                    
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            
        } // wait for thread if needed

		// add a change a listener to tabbed pane
		iniTabbedPane.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent evt) {

				iniTabbedPaneStateChanged(evt);

			}
		});

		jLabel2.setText("Epoch:");

		jLabel1.setText("Frame Type:");

		jLabel31.setText("Mu:");

		inputComboBox.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "EME2000", "CIRF2000", "GCRF", "MOD", "TEME",
						"TOD", "Veis1950" }));

		javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(
				jPanel3);
		jPanel3.setLayout(jPanel8Layout);
		jPanel8Layout
				.setHorizontalGroup(jPanel8Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel8Layout
										.createSequentialGroup()
										.addContainerGap()

										.addGroup(
												jPanel8Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(jLabel2)

														.addComponent(jLabel31)

														.addComponent(jLabel1))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel8Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(

																muTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																172,
																javax.swing.GroupLayout.PREFERRED_SIZE)

														.addComponent(
																epochTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																172,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																inputComboBox,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																172,
																javax.swing.GroupLayout.PREFERRED_SIZE))

										.addGap(28, 28, 28)));
		jPanel8Layout
				.setVerticalGroup(jPanel8Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel8Layout
										.createSequentialGroup()
										.addContainerGap()

										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)

										.addGroup(
												jPanel8Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel2)
														.addComponent(
																epochTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))

										.addGap(1, 1, 1)
										.addGroup(
												jPanel8Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel31)
														.addComponent(
																muTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(1, 1, 1)
										.addGroup(
												jPanel8Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel1)
														.addComponent(
																inputComboBox,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addContainerGap()));

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(
				jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout
				.setHorizontalGroup(jPanel2Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel2Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanel2Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																iniTabbedPane,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																332,
																Short.MAX_VALUE)
														.addComponent(
																jPanel3,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																332,
																Short.MAX_VALUE)

														.addGap(28, 28, 28))));
		jPanel2Layout
				.setVerticalGroup(jPanel2Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel2Layout
										.createSequentialGroup()
										.addContainerGap()

										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												iniTabbedPane,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												164, Short.MAX_VALUE)

										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jPanel3,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)

										.addContainerGap()));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
												.addGroup(
														javax.swing.GroupLayout.Alignment.TRAILING,
														layout.createSequentialGroup()
																.addContainerGap()
																.addComponent(
																		okButton)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(
																		cancelButton)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(
																		applyButton))
												.addComponent(
														jPanel2,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														Short.MAX_VALUE))
								.addContainerGap()));
		layout.setVerticalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						javax.swing.GroupLayout.Alignment.TRAILING,
						layout.createSequentialGroup()
								.addComponent(jPanel2,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(applyButton)
												.addComponent(cancelButton)
												.addComponent(okButton))));
	}// </editor-fold>//GEN-END:initComponents

	private void satTreeValueChanged(javax.swing.event.TreeSelectionEvent evt)// GEN-FIRST:event_satTreeValueChanged
	{// GEN-HEADEREND:event_satTreeValueChanged
		// some other sat was selected

		if (satTree.getSelectionCount() > 0) {
			if (tleHash.containsKey(satTree.getLastSelectedPathComponent()
					.toString())) {
				TLElements selectedTLE = tleHash.get(satTree
						.getLastSelectedPathComponent().toString());

				try {
					tleOutputTextArea.setText(selectedTLE.getLine1() + "\n"
							+ selectedTLE.getLine2());
				} catch (OrekitException e) {

					e.printStackTrace();
				}
			} else // clear text area
			{
				tleOutputTextArea.setText("");
			}
		} // something is selected
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
					icNode.setFrame(FramesFactory.getCIRF2000());
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
	private javax.swing.JTextField dxTextField;
	private javax.swing.JTextField dyTextField;
	private javax.swing.JTextField dzTextField;
	private javax.swing.JTextField eTextField;
	private javax.swing.JTextField epochTextField;
	private javax.swing.JTextField iTextField;
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

	private javax.swing.JLabel jLabel28;
	private javax.swing.JComboBox kepComboBox;

	// Circular orbit
	private javax.swing.JLabel jLabel16;
	private javax.swing.JLabel jLabel17;
	private javax.swing.JLabel jLabel18;
	private javax.swing.JLabel jLabel19;
	private javax.swing.JLabel jLabel20;
	private javax.swing.JLabel jLabel21;

	private javax.swing.JTextField sacTextField;
	private javax.swing.JTextField excTextField;
	private javax.swing.JTextField eycTextField;
	private javax.swing.JTextField icTextField;
	private javax.swing.JTextField raancTextField;
	private javax.swing.JTextField latcTextField;

	private javax.swing.JLabel jLabel29;
	private javax.swing.JComboBox circComboBox;

	private javax.swing.JPanel circPanel;

	// Equinoctial orbit
	private javax.swing.JLabel jLabel22;
	private javax.swing.JLabel jLabel23;
	private javax.swing.JLabel jLabel24;
	private javax.swing.JLabel jLabel25;
	private javax.swing.JLabel jLabel26;
	private javax.swing.JLabel jLabel27;

	private javax.swing.JTextField saeTextField;
	private javax.swing.JTextField exeTextField;
	private javax.swing.JTextField eyeTextField;
	private javax.swing.JTextField ieTextField;
	private javax.swing.JTextField raaneTextField;
	private javax.swing.JTextField lateTextField;

	private javax.swing.JLabel jLabel30;
	private javax.swing.JComboBox equiComboBox;

	private javax.swing.JPanel equiPanel;

	// TLE orbit
	private javax.swing.JPanel tlePanel;

	// MU
	private javax.swing.JLabel jLabel31;
	private javax.swing.JTextField muTextField;

	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JLabel jLabel7;
	private javax.swing.JLabel jLabel8;
	private javax.swing.JLabel jLabel9;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel kepPanel;
	private javax.swing.JTextField longTextField;
	private javax.swing.JTextField mTextField;
	private javax.swing.JButton okButton;
	private javax.swing.JTextField saTextField;
	private javax.swing.JTextField xTextField;
	private javax.swing.JTextField yTextField;
	private javax.swing.JTextField zTextField;

	// tle components
	private javax.swing.JMenuItem createCustomSatMenuItem;
	private javax.swing.JMenu jMenu1;
	private javax.swing.JMenuBar jMenuBar1;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanelTle;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane3;
	private javax.swing.JMenuItem loadCustomTLEMenuItem;
	private javax.swing.JPanel mainPanel;
	private javax.swing.JPanel outsidePanel;
	private javax.swing.JTree satTree;
	private javax.swing.JTextArea tleOutputTextArea;

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
		if (count1 <= count2)
			return false;
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
				if (tree.isExpanded(path))
					buf.append("," + String.valueOf(i - row));
			} else
				break;
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
