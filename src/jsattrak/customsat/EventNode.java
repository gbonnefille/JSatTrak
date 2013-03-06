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
 * 
 */

package jsattrak.customsat;

import java.awt.Toolkit;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;

import jsattrak.customsat.gui.EventPanel;
import jsattrak.customsat.gui.EventPanel.Events;
import jsattrak.customsat.swingworker.MissionDesignPropagator;
import jsattrak.gui.JSatTrak;
import jsattrak.objects.AbstractSatellite;
import jsattrak.objects.CustomSatellite;
import jsattrak.objects.GroundStation;
import jsattrak.objects.SatelliteTleSGP4;
import jsattrak.utilities.StateVector;
import name.gano.astro.AstroConst;
import name.gano.astro.GeoFunctions;
import name.gano.astro.Kepler;
import name.gano.astro.MathUtils;
import name.gano.astro.coordinates.CoordinateConversion;
import name.gano.astro.time.Time;
import name.gano.eventsorekit.AlignmentDetectorJsat;
import name.gano.eventsorekit.AltitudeDetectorJsat;
import name.gano.eventsorekit.ApparentElevationDetectorJsat;
import name.gano.eventsorekit.ApsideDetectorJsat;
import name.gano.eventsorekit.CircularFieldOfViewDetectorJsat;
import name.gano.eventsorekit.DateDetectorJsat;
import name.gano.eventsorekit.DihedralFieldOfViewDetectorJsat;
import name.gano.eventsorekit.EclipseDetectorJsat;
import name.gano.eventsorekit.ElevationDetectorJsat;
import name.gano.eventsorekit.NodeDetectorJsat;
import name.gano.swingx.treetable.CustomTreeTableNode;

import org.apache.commons.math3.exception.util.DummyLocalizable;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.errors.OrekitException;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.PotentialCoefficientsProvider;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.events.AbstractDetector;
import org.orekit.propagation.events.AlignmentDetector;
import org.orekit.propagation.events.AltitudeDetector;
import org.orekit.propagation.events.ApparentElevationDetector;
import org.orekit.propagation.events.ApsideDetector;
import org.orekit.propagation.events.CircularFieldOfViewDetector;
import org.orekit.propagation.events.DateDetector;
import org.orekit.propagation.events.ElevationDetector;
import org.orekit.propagation.events.GroundMaskElevationDetector;
import org.orekit.propagation.events.NodeDetector;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.PVCoordinatesProvider;

/**
 * 
 * @author sgano
 */
public class EventNode extends CustomTreeTableNode {

	private double[] vncThrustVector = new double[3];

	public static final int VTHRUST = 0;
	public static final int NTHRUST = 0;
	public static final int CTHRUST = 0;

	// variables that can be set data ----------
	String[] varNames = new String[] { "V-Thrust [m/s]", "N-Thrust [m/s]",
			"C-Thrust [m/s]" };

	// -----------------------------------------

	// USED FOR GOAL CALCULATIONS
	StateVector lastStateVector = null; // last state -- to calculate goal
										// properties

	public static final int SATELLITEOBJECT = 0;
	public static final int GROUNDSTATIONOBJECT = 1;
	public static final int CELESTIALBODYOBJECT = 2;

	// which event target
	private int typeOfTarget = EventNode.SATELLITEOBJECT;

	// Events params
	private double[] eventsParams = new double[3];

	private Vector3D positionVector = Vector3D.ZERO;

	private Vector3D positionVector2 = Vector3D.ZERO;

	private Vector3D positionVector3 = Vector3D.ZERO;

	private boolean totalEclipse = true;

	GregorianCalendar currentTimeDate = new GregorianCalendar(TimeZone.getTimeZone("UTC"));

	
	
	final SimpleDateFormat dateformatShort = new SimpleDateFormat(
			"dd MMM yyyy HH:mm:ss z");

	private AbstractSatellite currentSat = null;
	private Hashtable<String, AbstractSatellite> userSatList = null;
	private Hashtable<String, GroundStation> userGroundStationsList = null;

	//
	private String targetBodyObjectName = "ISS (ZARYA)             ";
	
	private String targetCelestialBodyObjectName = null;

	// Enum for orekit events
	private Events event = Events.ALIGNMENT;

	// parameters that can be used as GOALS ---
	String[] goalNames = new String[] {
			"X Position (J2000) [m]", // element 0
			"Y Position (J2000) [m]", "Z Position (J2000) [m]",
			"X Velocity (J2000) [m/s]", "Y Velocity (J2000) [m/s]",
			"Z Velocity (J2000) [m/s]", "Semimajor axis (Osculating) [m]",
			"Eccentricity (Osculating)", "Inclination (Osculating) [deg]",
			"Longitude of the ascending node (Osculating) [deg]",
			"Argument of pericenter (Osculating) [deg]",
			"Mean anomaly (Osculating) [deg]", "Orbital Radius [m]",
			"Derivative of Orbital Radius [m/s]", // can be used to find perigee
													// / apogee
			"Latitude [deg]", "Longitude [deg]", "Altitude [m]", // element 16
	};

	// ========================================

	public EventNode(CustomTreeTableNode parentNode,
			AbstractSatellite currentSat,
			Hashtable<String, AbstractSatellite> satList,
			Hashtable<String, GroundStation> groundStations) {
		super(new String[] { "Event", "", "" }); // initialize node, default
													// values
		// set icon for this type
		setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource("/icons/customSatIcons/burn.png"))));
		// set Node Type
		setNodeType("Event");

		this.currentSat = currentSat;

		this.userSatList = satList;

		this.userGroundStationsList = groundStations;

		// add this node to parent - last thing
		if (parentNode != null)
			parentNode.add(this);
	}

	// meant to be overridden by implementing classes
	@Override
	public void execute(MissionDesignPropagator missionDesign)
			throws OrekitException, IOException, ParseException {
		// dummy but should do something based on input ephemeris
		// System.out.println("Executing : " + getValueAt(0) );

		// get last stat vector (we are going to change it - impulse burn)

		// BoundedPropagator ephemeris = missionDesign.getEphemeris();
		//
		// double lastTime = ephemeris.getMaxDate().durationFrom(
		// AbsoluteDate.JULIAN_EPOCH) / 86400;
		// StateVector lastState = new StateVector(ephemeris.getPVCoordinates(
		// ephemeris.getMaxDate(), ephemeris.getFrame()), lastTime);
		//
		// // for VNC system see:
		// //
		// http://www.stk.com/resources/help/stk613/helpSystem/extfile/gator/eq-coordsys.htm
		//
		// // set inial time of the node ( TT)
		// this.setStartTTjulDate(ephemeris.getMaxDate());
		//
		// // get r and v vectors
		// double[] r = new double[] { lastState.state[1], lastState.state[2],
		// lastState.state[3] };
		// double[] v = new double[] { lastState.state[4], lastState.state[5],
		// lastState.state[6] };
		//
		// // calculate unit vector in V direction (in J2K coordinate frame)
		// double normV = MathUtils.norm(v);
		// double[] unitV = new double[] { v[0] / normV, v[1] / normV,
		// v[2] / normV };
		//
		// // calculate unit vector in N direction
		// double[] unitNorm = MathUtils.cross(r, v);
		// double normNorm = MathUtils.norm(unitNorm);
		// unitNorm[0] = unitNorm[0] / normNorm;
		// unitNorm[1] = unitNorm[1] / normNorm;
		// unitNorm[2] = unitNorm[2] / normNorm;
		//
		// // calculate unit vector in the Co-Normal direction
		// double[] unitCoNorm = MathUtils.cross(unitV, unitNorm);
		//
		// // calculate Thrust Vector in J2000.0
		// double[] thrustj2K = new double[] { 0.0, 0.0, 0.0 };
		// // add V component
		// thrustj2K = MathUtils.add(thrustj2K,
		// MathUtils.scale(unitV, vncThrustVector[0]));
		// // add N component
		// thrustj2K = MathUtils.add(thrustj2K,
		// MathUtils.scale(unitNorm, vncThrustVector[1]));
		// // add C component
		// thrustj2K = MathUtils.add(thrustj2K,
		// MathUtils.scale(unitCoNorm, vncThrustVector[2]));
		//
		// // add the trustj2k as a delta V to the last state
		// lastState.state[4] += thrustj2K[0];
		// lastState.state[5] += thrustj2K[1];
		// lastState.state[6] += thrustj2K[2];

		// copy final ephemeris state: - for goal calculations
		// lastStateVector = ephemeris.lastElement();

		// Compute the event
		AbstractDetector eventDetector = null;

		GroundStation groundStation = null;

		PotentialCoefficientsProvider provider = null;

		OneAxisEllipsoid earth = null;

		TopocentricFrame topo = null;

		PVCoordinatesProvider pvTarget = null;

		try {

			switch (event) {

			// Alignment detector
			case ALIGNMENT:

				if (typeOfTarget == EventNode.SATELLITEOBJECT) {

					AbstractSatellite abstractSat = userSatList
							.get(targetBodyObjectName);
					// Test si c'est un satellite SGP4 ou custom
					if (abstractSat.getClass().equals(SatelliteTleSGP4.class)) {

						SatelliteTleSGP4 satSGP4 = (SatelliteTleSGP4) abstractSat;
						pvTarget = satSGP4.getOrekitTlePropagator();

					} else if (abstractSat.getClass().equals(
							CustomSatellite.class)) {

						CustomSatellite customSat = (CustomSatellite) abstractSat;
						pvTarget = customSat.getEphemeris();
					} else {

						throw new OrekitException(new DummyLocalizable(
								"unknown satellite"));

					}

				}

				else if (typeOfTarget == EventNode.GROUNDSTATIONOBJECT) {

					groundStation = userGroundStationsList
							.get(targetBodyObjectName);

					// Factory used to read gravity field files in several
					// supported
					// formats
					provider = GravityFieldFactory.getPotentialProvider();
					// Earth central body reference radius

					earth = new OneAxisEllipsoid(provider.getAe(),
							Constants.WGS84_EARTH_FLATTENING,
							FramesFactory.getITRF2005());

					pvTarget = new TopocentricFrame(earth,
							groundStation.getLatLongAlt(),
							groundStation.getStationName());

				}

				else if (typeOfTarget == EventNode.CELESTIALBODYOBJECT) {

					pvTarget = CelestialBodyFactory
							.getBody(targetBodyObjectName);

				}

				else {

					throw new OrekitException(new DummyLocalizable(
							"unknown target object"));
				}

				eventDetector = new AlignmentDetectorJsat(this.currentSat,
						pvTarget, FastMath.toRadians(eventsParams[0]));

				break;

			// Altitude detector
			case ALTITUDE:

				// Factory used to read gravity field files in several
				// supported
				// formats
				provider = GravityFieldFactory.getPotentialProvider();
				// Earth central body reference radius

				earth = new OneAxisEllipsoid(provider.getAe(),
						Constants.WGS84_EARTH_FLATTENING,
						FramesFactory.getITRF2005());

				eventDetector = new AltitudeDetectorJsat(this.currentSat,
						eventsParams[0], earth);
				break;

			// Apparent Elevation detector
			case APPARENTELEVATION:

				groundStation = userGroundStationsList
						.get(targetBodyObjectName);

				// Factory used to read gravity field files in several supported
				// formats
				provider = GravityFieldFactory.getPotentialProvider();
				// Earth central body reference radius

				earth = new OneAxisEllipsoid(provider.getAe(),
						Constants.WGS84_EARTH_FLATTENING,
						FramesFactory.getITRF2005());

				topo = new TopocentricFrame(earth,
						groundStation.getLatLongAlt(),
						groundStation.getStationName());

				eventDetector = new ApparentElevationDetectorJsat(
						this.currentSat, FastMath.toRadians(eventsParams[0]),
						topo);
				break;

			// Apside detector
			case APSIDE:
				eventDetector = new ApsideDetectorJsat(this.currentSat);
				break;

			// Circular field of view detector
			case CIRCULARFIELDOFVIEW:

				if (typeOfTarget == EventNode.SATELLITEOBJECT) {

					AbstractSatellite abstractSat = userSatList
							.get(targetBodyObjectName);
					// Test si c'est un satellite SGP4 ou custom
					if (abstractSat.getClass().equals(SatelliteTleSGP4.class)) {

						SatelliteTleSGP4 satSGP4 = (SatelliteTleSGP4) abstractSat;
						pvTarget = satSGP4.getOrekitTlePropagator();

					} else if (abstractSat.getClass().equals(
							CustomSatellite.class)) {

						CustomSatellite customSat = (CustomSatellite) abstractSat;
						pvTarget = customSat.getEphemeris();
					} else {

						throw new OrekitException(new DummyLocalizable(
								"unknown satellite"));

					}

				}

				else if (typeOfTarget == EventNode.GROUNDSTATIONOBJECT) {

					groundStation = userGroundStationsList
							.get(targetBodyObjectName);

					// Factory used to read gravity field files in several
					// supported
					// formats
					provider = GravityFieldFactory.getPotentialProvider();
					// Earth central body reference radius

					earth = new OneAxisEllipsoid(provider.getAe(),
							Constants.WGS84_EARTH_FLATTENING,
							FramesFactory.getITRF2005());

					topo = new TopocentricFrame(earth,
							groundStation.getLatLongAlt(),
							groundStation.getStationName());

					pvTarget = topo;

				}

				else if (typeOfTarget == EventNode.CELESTIALBODYOBJECT) {

					pvTarget = CelestialBodyFactory
							.getBody(targetBodyObjectName);

				}

				else {

					throw new OrekitException(new DummyLocalizable(
							"unknown target object"));
				}

				eventDetector = new CircularFieldOfViewDetectorJsat(
						this.currentSat, eventsParams[0], pvTarget,
						positionVector, FastMath.toRadians(eventsParams[1]));
				break;

			// Date Detector
			case DATE:

				AbsoluteDate absoluteDate = new AbsoluteDate(
						currentTimeDate.getTime(), TimeScalesFactory.getUTC());
				eventDetector = new DateDetectorJsat(this.currentSat,
						absoluteDate);
				break;

			// Dihedral field of view detector
			case DIHEDRALFIELDOFVIEW:

				if (typeOfTarget == EventNode.SATELLITEOBJECT) {

					AbstractSatellite abstractSat = userSatList
							.get(targetBodyObjectName);
					// Test si c'est un satellite SGP4 ou custom
					if (abstractSat.getClass().equals(SatelliteTleSGP4.class)) {

						SatelliteTleSGP4 satSGP4 = (SatelliteTleSGP4) abstractSat;
						pvTarget = satSGP4.getOrekitTlePropagator();

					} else if (abstractSat.getClass().equals(
							CustomSatellite.class)) {

						CustomSatellite customSat = (CustomSatellite) abstractSat;
						pvTarget = customSat.getEphemeris();
					} else {

						throw new OrekitException(new DummyLocalizable(
								"unknown satellite"));

					}

				}

				else if (typeOfTarget == EventNode.GROUNDSTATIONOBJECT) {

					groundStation = userGroundStationsList
							.get(targetBodyObjectName);

					// Factory used to read gravity field files in several
					// supported
					// formats
					provider = GravityFieldFactory.getPotentialProvider();
					// Earth central body reference radius

					earth = new OneAxisEllipsoid(provider.getAe(),
							Constants.WGS84_EARTH_FLATTENING,
							FramesFactory.getITRF2005());

					topo = new TopocentricFrame(earth,
							groundStation.getLatLongAlt(),
							groundStation.getStationName());

					pvTarget = topo;

				}

				else if (typeOfTarget == EventNode.CELESTIALBODYOBJECT) {

					pvTarget = CelestialBodyFactory
							.getBody(targetBodyObjectName);

				}

				else {

					throw new OrekitException(new DummyLocalizable(
							"unknown target object"));
				}

				eventDetector = new DihedralFieldOfViewDetectorJsat(
						this.currentSat, eventsParams[0], pvTarget,
						positionVector, positionVector2, eventsParams[1],
						positionVector3, eventsParams[2]);
				break;

			// Eclipse detector
			case ECLIPSE:

				pvTarget = CelestialBodyFactory.getBody(targetBodyObjectName);
				
				 PVCoordinatesProvider celestialBodyOcculted = CelestialBodyFactory.getBody(targetCelestialBodyObjectName);

				eventDetector = new EclipseDetectorJsat(pvTarget,
						eventsParams[0],celestialBodyOcculted,eventsParams[1],this.currentSat,
						totalEclipse);

				break;

			// Elevation detector
			case ELEVATION:

				groundStation = userGroundStationsList
						.get(targetBodyObjectName);

				// Factory used to read gravity field files in several supported
				// formats
				provider = GravityFieldFactory.getPotentialProvider();
				// Earth central body reference radius

				earth = new OneAxisEllipsoid(provider.getAe(),
						Constants.WGS84_EARTH_FLATTENING,
						FramesFactory.getITRF2005());

				topo = new TopocentricFrame(earth,
						groundStation.getLatLongAlt(),
						groundStation.getStationName());
				eventDetector = new ElevationDetectorJsat(this.currentSat,
						FastMath.toRadians(eventsParams[0]), topo);
				break;

			// // Ground mask elevation detector
			// case GROUNDMASKELEVATION:
			// eventDetector = new GroundMaskElevationDetectorJsat(azimelev,
			// topo);
			// break;

			// Node detector
			case NODE:
				eventDetector = new NodeDetectorJsat(this.currentSat,
						this.currentSat.getMissionTree().getInitNode().getFrame());
				break;

			default:
				throw OrekitException.createInternalError(null);

			}
		} catch (Exception e) {

			throw new OrekitException(new DummyLocalizable(
					"Event step is not configured"));

		}

		// Add event to the satellite
		this.currentSat.getMissionTree().getPropNode().addEventDetector(eventDetector);

	}// execute

	// passes in main app to add the internal frame to
	public void displaySettings(JSatTrak app) {

		String windowName = "" + getValueAt(0);
		JInternalFrame iframe = new JInternalFrame(windowName, true, true,
				true, true);

		// show satellite browser window
		EventPanel panel = new EventPanel(this, iframe); // non-modal
																// version
		panel.setIframe(iframe);

		iframe.setContentPane(panel);
		iframe.setSize(420, 350); // w,h
		iframe.setLocation(5, 5);

		app.addInternalFrame(iframe);

	}

	public double[] getVncThrustVector() {
		return vncThrustVector;
	}

	public void setVncThrustVector(double[] vncThrustVector) {
		this.vncThrustVector = vncThrustVector;
	}

	// method to get variable by its integer
	public double getVar(int varInt) {
		double val = 0;

		// don't need switch case here since this one is easy
		if (varInt >= 0 && varInt <= 2) {
			val = vncThrustVector[varInt];
		}

		return val;
	} // getVar

	// method to set variable by its integer
	public void setVar(int varInt, double val) {
		// don't need switch case here since this one is easy
		if (varInt >= 0 && varInt <= 2) {
			vncThrustVector[varInt] = val;
		}
	} // setVar

	// returns the Vector list of all the Variables in this Node
	public Vector<InputVariable> getInputVarVector() {
		Vector<InputVariable> varVec = new Vector<InputVariable>(3);

		// create list
		for (int i = 0; i < 3; i++) {
			InputVariable inVar = new InputVariable(this, i, varNames[i],
					vncThrustVector[i]);
			varVec.add(inVar);
		}

		return varVec;
	} // getInputVarVector

	// meant to be over ridden if there are any input vars
	public Vector<GoalParameter> getGoalParamVector() {
		Vector<GoalParameter> varVec = new Vector<GoalParameter>(17);

		for (int i = 0; i < goalNames.length; i++) {
			GoalParameter inVar = new GoalParameter(this, i, goalNames[i],
					getGoal(i)); // hmm, need to put current value here if
									// possible
			varVec.add(inVar);
		}

		return varVec;
	}

	// meant to be over ridden if there are any input vars
	public Double getGoal(int goalInt) {
		Double val = null;

		if (lastStateVector != null) {
			// calculate goals value
			switch (goalInt) {
			case 0: // X Position (J2000) [m]
				val = lastStateVector.state[1];
				break;
			case 1: // Y Position (J2000) [m]
				val = lastStateVector.state[2];
				break;
			case 2: // Z Position (J2000) [m]
				val = lastStateVector.state[3];
				break;
			case 3: // X Velocity (J2000) [m/s]
				val = lastStateVector.state[4];
				break;
			case 4: // Y Velocity (J2000) [m/s]
				val = lastStateVector.state[5];
				break;
			case 5: // Z Velocity (J2000) [m/s]
				val = lastStateVector.state[6];
				break;
			case 6: // Semimajor axis (Osculating) [m]
				val = Kepler.SingularOsculatingElementsEarth(lastStateVector)[0];
				break;
			case 7: // Eccentricity (Osculating)
				val = Kepler.SingularOsculatingElementsEarth(lastStateVector)[1];
				break;
			case 8: // Inclination (Osculating) [deg]
				val = Kepler.SingularOsculatingElementsEarth(lastStateVector)[2]
						* 180.0 / Math.PI;
				break;
			case 9: // Longitude of the ascending node (Osculating) [deg]
				val = Kepler.SingularOsculatingElementsEarth(lastStateVector)[3]
						* 180.0 / Math.PI;
				break;
			case 10: // Argument of pericenter (Osculating) [deg]
				val = Kepler.SingularOsculatingElementsEarth(lastStateVector)[4]
						* 180.0 / Math.PI;
				break;
			case 11: // Mean anomaly (Osculating) [deg]
				val = Kepler.SingularOsculatingElementsEarth(lastStateVector)[5]
						* 180.0 / Math.PI;
				break;
			case 12: // Orbital Radius [m]
				val = Math.sqrt(Math.pow(lastStateVector.state[1], 2.0)
						+ Math.pow(lastStateVector.state[2], 2.0)
						+ Math.pow(lastStateVector.state[3], 2.0));
				break;
			case 13: // Derivative of Orbital Radius [m/s]
				// rdot = v dot R
				double[] R = new double[] { lastStateVector.state[1],
						lastStateVector.state[2], lastStateVector.state[3] };
				double normR = MathUtils.norm(R);
				R = MathUtils.scale(R, 1.0 / normR); // unit vector

				double[] v = new double[] { lastStateVector.state[4],
						lastStateVector.state[5], lastStateVector.state[6] };
				double rDot = MathUtils.dot(v, R);
				val = rDot;
				break;
			case 14: // Latitude [deg]
				// get current j2k pos
				double[] currentJ2kPos = new double[] {
						lastStateVector.state[1], lastStateVector.state[2],
						lastStateVector.state[3] };
				// mod pos
				// double[] modPos =
				// CoordinateConversion.EquatorialEquinoxFromJ2K(lastStateVector.state[0]
				// - AstroConst.JDminusMJD, currentJ2kPos)
				// teme pos
				Vector3D temePos = CoordinateConversion.J2000toTEME(
						lastStateVector.state[0] - AstroConst.JDminusMJD,
						currentJ2kPos);
				// lla (submit time in UTC)
				double deltaTT2UTC = Time.deltaT(lastStateVector.state[0]
						- AstroConst.JDminusMJD); // = TT - UTC
				double[] lla = GeoFunctions.GeodeticLLA(temePos,
						lastStateVector.state[0] - AstroConst.JDminusMJD
								- deltaTT2UTC); // tt-UTC = deltaTT2UTC

				val = lla[0] * 180.0 / Math.PI;
				break;
			case 15: // Longitude [deg]
				// get current j2k pos
				currentJ2kPos = new double[] { lastStateVector.state[1],
						lastStateVector.state[2], lastStateVector.state[3] };
				// mod pos
				// modPos =
				// CoordinateConversion.EquatorialEquinoxFromJ2K(lastStateVector.state[0]
				// - AstroConst.JDminusMJD, currentJ2kPos)
				// teme pos
				temePos = CoordinateConversion.J2000toTEME(
						lastStateVector.state[0] - AstroConst.JDminusMJD,
						currentJ2kPos);
				// lla (submit time in UTC)
				deltaTT2UTC = Time.deltaT(lastStateVector.state[0]
						- AstroConst.JDminusMJD); // = TT - UTC
				lla = GeoFunctions.GeodeticLLA(temePos,
						lastStateVector.state[0] - AstroConst.JDminusMJD
								- deltaTT2UTC); // tt-UTC = deltaTT2UTC

				val = lla[1] * 180.0 / Math.PI;
				break;
			case 16: // Altitude [m]
				// get current j2k pos
				currentJ2kPos = new double[] { lastStateVector.state[1],
						lastStateVector.state[2], lastStateVector.state[3] };
				// mod pos
				// modPos =
				// CoordinateConversion.EquatorialEquinoxFromJ2K(lastStateVector.state[0]
				// - AstroConst.JDminusMJD, currentJ2kPos)
				// teme pos
				temePos = CoordinateConversion.J2000toTEME(
						lastStateVector.state[0] - AstroConst.JDminusMJD,
						currentJ2kPos);
				// lla (submit time in UTC)
				deltaTT2UTC = Time.deltaT(lastStateVector.state[0]
						- AstroConst.JDminusMJD); // = TT - UTC
				lla = GeoFunctions.GeodeticLLA(temePos,
						lastStateVector.state[0] - AstroConst.JDminusMJD
								- deltaTT2UTC); // tt-UTC = deltaTT2UTC

				val = lla[2];
				break;

			} // switch
		} // last state not null

		return val;
	} // getGoal

	public double[] getEventsParams() {
		return eventsParams;
	}

	public void setEventsParams(double[] eventsParams) {
		this.eventsParams = eventsParams;
	}

	public Hashtable<String, AbstractSatellite> getUserSatList() {
		return userSatList;
	}

	public void setUserSatList(Hashtable<String, AbstractSatellite> userSatList) {
		this.userSatList = userSatList;
	}

	public Hashtable<String, GroundStation> getUserGroundStationsList() {
		return userGroundStationsList;
	}

	public void setUserGroundStationsList(
			Hashtable<String, GroundStation> userGroundStationsList) {
		this.userGroundStationsList = userGroundStationsList;
	}

	public AbstractSatellite getCurrentSat() {
		return currentSat;
	}

	public void setTargetBodyObjectName(String targetBodyObjectName) {
		this.targetBodyObjectName = targetBodyObjectName;
	}

	public String getTargetBodyObjectName() {
		return targetBodyObjectName;
	}

	public Events getEvent() {
		return event;
	}

	public void setEvent(Events event) {
		this.event = event;
	}

	public void setTypeOfTarget(int typeOfTarget) {
		this.typeOfTarget = typeOfTarget;
	}

	public int getTypeOfTarget() {
		return typeOfTarget;
	}

	public void setTotalEclipse(boolean totalEclipse) {
		this.totalEclipse = totalEclipse;
	}

	public boolean isTotalEclipse() {
		return totalEclipse;
	}

	public Vector3D getPositionVector() {
		return positionVector;
	}

	public void setPositionVector(Vector3D positionVector) {
		this.positionVector = positionVector;
	}

	public Vector3D getPositionVector2() {
		return positionVector2;
	}

	public void setPositionVector2(Vector3D positionVector2) {
		this.positionVector2 = positionVector2;
	}

	public Vector3D getPositionVector3() {
		return positionVector3;
	}

	public void setPositionVector3(Vector3D positionVector3) {
		this.positionVector3 = positionVector3;
	}

	public SimpleDateFormat getDateformatShort() {
		return dateformatShort;
	}

	public GregorianCalendar getCurrentTimeDate() {
		return currentTimeDate;
	}

	public void setCurrentTimeDate(GregorianCalendar currentTimeDate) {
		this.currentTimeDate = currentTimeDate;
	}

	public String getTargetCelestialBodyObjectName() {
		return targetCelestialBodyObjectName;
	}

	public void setTargetCelestialBodyObjectName(
			String targetCelestialBodyObjectName) {
		this.targetCelestialBodyObjectName = targetCelestialBodyObjectName;
	}

}
