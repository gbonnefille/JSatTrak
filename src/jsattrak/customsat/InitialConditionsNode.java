/*
 * Node for Custom Sat Class Mission Designer
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
 */

package jsattrak.customsat;

import java.awt.Toolkit;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;

import jsattrak.customsat.gui.InitialConditionsPanel;
import jsattrak.customsat.swingworker.MissionDesignPropagator;
import jsattrak.gui.JCustomSatConfigPanel;
import jsattrak.gui.JSatTrak;
import jsattrak.utilities.TLElements;
import name.gano.astro.time.Time;
import name.gano.astro.time.TimeOrekit;
import name.gano.swingx.treetable.CustomTreeTableNode;

import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.CartesianOrbit;
import org.orekit.orbits.CircularOrbit;
import org.orekit.orbits.EquinoctialOrbit;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngle;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;
import org.orekit.utils.PVCoordinates;

/**
 * 
 * @author acouanon
 */
public class InitialConditionsNode extends CustomTreeTableNode {

	private static final long serialVersionUID = -4739379085877131283L;

	public static final int KEPLERIAN = 0;
	public static final int CARTESIAN = 1;
	public static final int CIRCULAR = 2;
	public static final int EQUINOCTIAL = 3;
	public static final int TLE = 4;

	// which coord to use
	private int coordinate = InitialConditionsNode.KEPLERIAN;

	private String satelliteTleName = null;

	private TLElements satelliteTleElements = null;

	private ArrayList<Integer> lastTreeSelection = null;

	private double[] keplarianElements = new double[] { 6678140, 0.01,
			45.0 * Math.PI / 180.0, 0.0, 0.0, 0.0 };

	private PVCoordinates cartesianElements = new PVCoordinates();; // x,y,z,dx,dy,dz
	private double[] circularElements = new double[6];
	private double[] equinoctialElements = new double[6];

	private double iniJulDate = 0; // (UTC) julian date of the inital conditions
									// (should set to epock of scenario by
									// default)

	// Central attraction coefficient
	private double mu = Constants.EIGEN5C_EARTH_MU;

	private TimeOrekit scenarioEpochDate;

	private AbsoluteDate absoluteDate;

	// Mean, true or eccentric
	private PositionAngle positionAngle = PositionAngle.MEAN;

	private Orbit orbitOrekit = null;

	private Frame frame = FramesFactory.getEME2000();

	private JCustomSatConfigPanel jSatConfigPanel = null;

	public InitialConditionsNode(CustomTreeTableNode parentNode,
			TimeOrekit scenarioEpochDate) throws OrekitException {
		super(new String[] { "Initial Conditions", "", "" }); // initialize
																// node, default
																// values
		this.scenarioEpochDate = scenarioEpochDate;
		iniJulDate = scenarioEpochDate.getJulianDay();

		// set icon for this type
		setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource("/icons/customSatIcons/ini.png"))));
		// set Node Type
		setNodeType("Initial Conditions");

		// AbsoluteDate absoluteDateTest =
		// AbsoluteDate.JULIAN_EPOCH.shiftedBy(iniJulDate * 86400);

		absoluteDate = AbsoluteDate.JULIAN_EPOCH.shiftedBy(iniJulDate * 86400);

		// absoluteDate = new AbsoluteDate(AbsoluteDate.JULIAN_EPOCH, iniJulDate
		// * 86400, TimeScalesFactory.getUTC());

		// Setup the keplerian orbit
		this.orbitOrekit = new KeplerianOrbit(this.keplarianElements[0],
				this.keplarianElements[1], this.keplarianElements[2],
				this.keplarianElements[4], this.keplarianElements[3],
				this.keplarianElements[5], this.positionAngle, this.frame,
				this.absoluteDate, this.mu);

		// add this node to parent - last thing
		if (parentNode != null)
			parentNode.add(this);
	}

	// meant to be overridden by implementing classes
	@Override
	public void execute(MissionDesignPropagator missionDesign) {

		// set inial time of the node
		this.setStartTTjulDate(AbsoluteDate.JULIAN_EPOCH
				.shiftedBy(iniJulDate * 86400));

	}// execute

	// passes in main app to add the internal frame to
	public void displaySettings(JSatTrak app) {

		String windowName = "" + getValueAt(0);
		JInternalFrame iframe = new JInternalFrame(windowName, true, true,
				true, true);

		// show satellite browser window
		InitialConditionsPanel gsBrowser = new InitialConditionsPanel(app,
				this, scenarioEpochDate); // non-modal version

		gsBrowser.setIframe(iframe);

		iframe.setContentPane(gsBrowser);
		iframe.setSize(375, 420); // w,h
		iframe.setLocation(5, 5);

		app.addInternalFrame(iframe);

	}

	public double[] convertToKeplerian() {

		KeplerianOrbit kep = (KeplerianOrbit) OrbitType.KEPLERIAN
				.convertType(this.orbitOrekit);

		double[] out = new double[] { kep.getA(), kep.getE(), kep.getI(),
				kep.getPerigeeArgument(),
				kep.getRightAscensionOfAscendingNode(), kep.getMeanAnomaly() };

		return out;

	}

	public PVCoordinates convertToCartesian() {

		// Convert label to cartesian element
		CartesianOrbit carte = (CartesianOrbit) OrbitType.CARTESIAN
				.convertType(this.orbitOrekit);

		return carte.getPVCoordinates();

	}

	public double[] convertToCircular() {

		CircularOrbit circ = (CircularOrbit) OrbitType.CIRCULAR
				.convertType(this.orbitOrekit);

		double[] out = new double[] { circ.getA(), circ.getEquinoctialEx(),
				circ.getEquinoctialEy(), circ.getI(),
				circ.getRightAscensionOfAscendingNode(),
				circ.getAlpha(this.positionAngle) };

		return out;

	}

	public double[] convertToEquinoctial() {

		EquinoctialOrbit equi = (EquinoctialOrbit) OrbitType.EQUINOCTIAL
				.convertType(this.orbitOrekit);

		double[] out = new double[] { equi.getA(), equi.getEquinoctialEx(),
				equi.getEquinoctialEy(), equi.getHx(), equi.getHy(),
				equi.getL(this.positionAngle) };

		return out;

	}

	public double[] getKeplarianElements() {
		return keplarianElements;
	}

	/**
	 * Sets Keplarian elements initial State. Be sure to set epoch first, this
	 * method also automatically updates Cartesian j2k to agree. (assumes
	 * element set is for current epoch)
	 * 
	 * @param keplarianElements
	 *            keplarian elements
	 */
	public void setKeplarianElements(double[] keplarianElements) {

		// set keplerian elements
		this.keplarianElements = keplarianElements;

		// build the keplerian orbit
		this.orbitOrekit = new KeplerianOrbit(keplarianElements[0],
				keplarianElements[1], keplarianElements[2],
				keplarianElements[4], keplarianElements[3],
				keplarianElements[5], this.positionAngle, this.frame,
				this.absoluteDate, this.mu);

	}

	public PVCoordinates getCartesianElements() {
		return cartesianElements;
	}

	/**
	 * Sets j2k cartesian coordinate initial State. Be sure to set epoch first,
	 * this method also automatically updates keplarian elements to agree.
	 * 
	 * @param cartesianElements
	 *            j2k cartesian state
	 */
	public void setCartesianElements(PVCoordinates cartesianElements) {
		this.cartesianElements = cartesianElements;

		this.orbitOrekit = new CartesianOrbit(cartesianElements, this.frame,
				this.absoluteDate, this.mu);
	}

	public double getIniJulDate() {
		return iniJulDate;
	}

	public void setIniJulDate(double iniJulDate) {
		this.iniJulDate = iniJulDate;
	}

	public void setAbsoluteDate(double date) throws OrekitException {
		// this.absoluteDate = new AbsoluteDate(AbsoluteDate.JULIAN_EPOCH, date
		// * 86400, TimeScalesFactory.getUTC());
		this.absoluteDate = new AbsoluteDate(AbsoluteDate.JULIAN_EPOCH,
				date * 86400);
	}

	public Orbit getOrbitOrekit() {
		return orbitOrekit;
	}

	public Frame getFrame() {
		return frame;
	}

	public void setFrame(Frame frame) {
		this.frame = frame;
	}

	public double getMu() {
		return mu;
	}

	public void setMu(double mu) {
		this.mu = mu;
	}

	public double[] getCircularElements() {
		return circularElements;
	}

	public void setCircularElements(double[] circularElements) {
		this.circularElements = circularElements;
		this.orbitOrekit = new CircularOrbit(circularElements[0],
				circularElements[1], circularElements[2], circularElements[3],
				circularElements[4], circularElements[5], this.positionAngle,
				this.frame, this.absoluteDate, this.mu);
	}

	public double[] getEquinoctialElements() {
		return equinoctialElements;
	}

	public void setEquinoctialElements(double[] equinoctialElements) {
		this.equinoctialElements = equinoctialElements;
		this.orbitOrekit = new EquinoctialOrbit(equinoctialElements[0],
				equinoctialElements[1], equinoctialElements[2],
				equinoctialElements[3], equinoctialElements[4],
				equinoctialElements[5], this.positionAngle, this.frame,
				this.absoluteDate, this.mu);
	}

	public int getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(int coordinate) {
		this.coordinate = coordinate;
	}

	public PositionAngle getPositionAngle() {
		return positionAngle;
	}

	public void setPositionAngle(PositionAngle positionAngle) {
		this.positionAngle = positionAngle;
	}

	public String getSatelliteTleName() {
		return satelliteTleName;
	}

	public void setSatelliteTleName(String satelliteTleName) {
		this.satelliteTleName = satelliteTleName;

	}

	public void setJcustomSatConfigPanel(JCustomSatConfigPanel jSatConfigPanel) {
		this.jSatConfigPanel = jSatConfigPanel;
	}

	public JCustomSatConfigPanel getjSatConfigPanel() {
		return jSatConfigPanel;
	}

	public TLElements getSatelliteTleElements() {
		return satelliteTleElements;
	}

	public void setSatelliteTleElements(TLElements satelliteTleElements) {
		this.satelliteTleElements = satelliteTleElements;

	}

	public ArrayList<Integer> getLastTreeSelection() {
		return lastTreeSelection;
	}

	public void setLastTreeSelection(ArrayList<Integer> lastTreeSelection) {
		this.lastTreeSelection = lastTreeSelection;
	}

}
