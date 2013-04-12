/*
 * Propogator Node for Custom Sat Class Mission Designer
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;

import jsattrak.customsat.gui.PropagatorPanel;
import jsattrak.customsat.swingworker.MissionDesignPropagator;
import jsattrak.gui.JSatTrak;
import jsattrak.objects.SatelliteTleSGP4;
import jsattrak.utilities.StateVector;
import jsattrak.utilities.TLElements;
import name.gano.astro.AstroConst;
import name.gano.astro.GeoFunctions;
import name.gano.astro.Kepler;
import name.gano.astro.MathUtils;
import name.gano.astro.coordinates.CoordinateConversion;
import name.gano.astro.propogators.solvers.OrbitProblem;
import name.gano.astro.time.Time;
import name.gano.swingx.treetable.CustomTreeTableNode;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataProvidersManager;
import org.orekit.errors.OrekitException;
import org.orekit.forces.ForceModel;
import org.orekit.forces.SphericalSpacecraft;
import org.orekit.forces.drag.DTM2000;
import org.orekit.forces.drag.DragForce;
import org.orekit.forces.drag.MarshallSolarActivityFutureEstimation;
import org.orekit.forces.drag.MarshallSolarActivityFutureEstimation.StrengthLevel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.ThirdBodyAttraction;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.forces.radiation.SolarRadiationPressure;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.Orbit;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.EcksteinHechlerPropagator;
import org.orekit.propagation.analytical.KeplerianPropagator;
import org.orekit.propagation.events.AbstractDetector;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;
import org.orekit.utils.PVCoordinatesProvider;

/**
 * 
 * @author sgano
 */
public class PropagatorNode extends CustomTreeTableNode implements OrbitProblem {
	
	private static final long serialVersionUID = 7899130530816296619L;
	
	// static int to determin which propogator to use
	public static final int NUMERICAL = 0;
	public static final int KEPLERIAN = 1;
	public static final int ECKSTEINHECHLER = 2;
	public static final int SEMIANALYTICAL = 3;
	public static final int TLE = 4;

	// which prop to use
	private int propogator = PropagatorNode.NUMERICAL;

	// Orbit
	private Orbit orbitOrekit = null;

	private InitialConditionsNode initNode = null;

	private ArrayList<AbstractDetector> eventDetector = new ArrayList<AbstractDetector>();

	// Central attraction coefficient
	private double mu = Constants.EIGEN5C_EARTH_MU;

	// Un-normalized zonal coefficient
	private double[] zonalCoefficients = new double[] { -1.08e-3, 2.53e-6,
			1.62e-6, 2.28e-7, -5.41e-7 };

	// Hprop settings
	private int n_max = 20; // degree
	private int m_max = 20; // order
	private boolean includeLunarPert = false;
	private boolean includeSunPert = false;
	private boolean includeSolRadPress = false;
	private boolean includeAtmosDrag = false;
	private double mass = 1000.0; // [kg] mass of spacecraft
	private double area = 5.0; // [m^2] Cross-section Area
	private double CR = 1.3; // Solar radiation pressure coefficient
	private double CD = 1.5; // spacecraft drag coefficient
	private double stepSize = 60.0; // seconds (ini step for Hprop 7-8)

	// Hprop 7-8 unique
	private double minStepSize = 1.0; // 1 second
	private double maxStepSize = 600.0; // 10 minutes

	// Relative accuracy (m)
	private double dP = 1.0;

	private double popogateTimeLen = 86400; // in seconds

	private int numberOfStep = (int) Math.ceil(popogateTimeLen / this.stepSize);

	// stopping conditions used
	private boolean stopOnApogee = false;
	private boolean stopOnPerigee = false;

	// USED FOR GOAL CALCULATIONS
	StateVector lastStateVector = null; // last state -- to calculate goal
										// properties

	// variables that can be set data ----------
	String[] varNames = new String[] { "Propagation Time [s]" };
	// -----------------------------------------

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

	public PropagatorNode(CustomTreeTableNode parentNode,
			InitialConditionsNode initNode) {
		super(new String[] { "Propagate", "", "" }); // initialize node, default
														// values
		this.initNode = initNode;

		// set icon for this type
		setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource("/icons/customSatIcons/prop.png"))));
		// set Node Type
		setNodeType("Propagator");

		// add this node to parent - last thing
		if (parentNode != null)
			parentNode.add(this);

	} // PropogatorNode

	// propogate sat (using starting point as last ephemeris pt)
	public void execute(MissionDesignPropagator missionDesign)
			throws IOException, ParseException, OrekitException,
			IllegalArgumentException, IllegalStateException {

		// save initial time of the node
		this.setStartTTjulDate(this.initNode.getStartTTjulDate());

		// Set the orbit except for TLE propagation
		if (this.initNode.getCoordinate() != InitialConditionsNode.TLE) {
			// Set the orbit
			this.orbitOrekit = initNode.getOrbitOrekit();
		} else {
			// Set the propagator if the uses hasn't validate the propagator
			// configuration
			this.propogator = PropagatorNode.TLE;
		}

		// Duration of the propagation (Only use in exportEphemeris for TLE
		// propagation)
		AbsoluteDate EndOfSimulation = this.getStartTTjulDate().shiftedBy(
				this.popogateTimeLen);
		// Number of steps for ephemeris file
		this.numberOfStep = (int) Math.ceil(popogateTimeLen / this.stepSize);

		// run correct propogator

		// ///////////////////////
		// Numerical propogator///
		// ///////////////////////

		BoundedPropagator ephemeris = null;

		if (propogator == PropagatorNode.NUMERICAL) {

			double[][] tolerance = NumericalPropagator.tolerances(this.dP,
					this.orbitOrekit, this.orbitOrekit.getType());

			DormandPrince853Integrator dormanIntegrator = new DormandPrince853Integrator(
					this.minStepSize, this.maxStepSize, tolerance[0],
					tolerance[1]);

			NumericalPropagator prop = new NumericalPropagator(dormanIntegrator);

			prop.setOrbitType(this.orbitOrekit.getType());

			prop.setInitialState(new SpacecraftState(this.orbitOrekit,
					this.mass));

			// Earth Gravity model
			NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory
					.getNormalizedProvider(n_max, m_max);
			prop.addForceModel(new HolmesFeatherstoneAttractionModel(
					FramesFactory.getITRF2008(), provider));
			// 3rd bodies

			if (this.includeSunPert) {
				prop.addForceModel(new ThirdBodyAttraction(CelestialBodyFactory
						.getSun()));
			}

			if (this.includeLunarPert) {
				prop.addForceModel(new ThirdBodyAttraction(CelestialBodyFactory
						.getMoon()));
			}

			// Drag
			if (this.includeAtmosDrag) {

				OneAxisEllipsoid earth = new OneAxisEllipsoid(
						Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
						Constants.WGS84_EARTH_FLATTENING,
						FramesFactory.getITRF2008());
				PVCoordinatesProvider sun = CelestialBodyFactory.getSun();

				final String supportedNames = "(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\p{Digit}\\p{Digit}\\p{Digit}\\p{Digit}F10\\.(?:txt|TXT)";
				MarshallSolarActivityFutureEstimation msafe = new MarshallSolarActivityFutureEstimation(
						supportedNames, StrengthLevel.AVERAGE);
				DataProvidersManager.getInstance().feed(
						msafe.getSupportedNames(), msafe);
				DTM2000 atmosphere = new DTM2000(msafe, sun, earth);
				ForceModel drag = new DragForce(atmosphere,
						new SphericalSpacecraft(area, CD, 0., 0.));

				prop.addForceModel(drag);

			}

			// SRP
			if (this.includeSolRadPress) {

				// Solar radiation pressure force model
				PVCoordinatesProvider sunSRP = CelestialBodyFactory.getSun();

				// kR compute & kA=0
				double kR = 1 - 9 / 4 * (this.CR - 1);

				ForceModel pressureNUM = new SolarRadiationPressure(sunSRP,
						Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
						new SphericalSpacecraft(area, 0., 0, kR));

				prop.addForceModel(pressureNUM);
			}

			// Activate the ephemeris mode
			prop.setEphemerisMode();

			// take into account the events if there is

			prop.propagate(EndOfSimulation);

			ephemeris = prop.getGeneratedEphemeris();

			if (!eventDetector.isEmpty()) {

				Iterator<AbstractDetector> eventIterator = this.eventDetector
						.iterator();

				while (eventIterator.hasNext()) {
					AbstractDetector event = eventIterator.next();

					ephemeris.addEventDetector(event);

				}
			}

			missionDesign.setEphemeris(ephemeris);

			// Reset the event if not use in the next simulation
			this.eventDetector.clear();

		}

		// ///////////////////////
		// Keplerian propogator//
		// ///////////////////////

		else if (propogator == PropagatorNode.KEPLERIAN) {

			KeplerianPropagator prop = new KeplerianPropagator(
					this.orbitOrekit, this.mu);

			// Activate the ephemeris mode
			prop.setEphemerisMode();

			prop.propagate(EndOfSimulation);

			ephemeris = prop.getGeneratedEphemeris();

			if (!eventDetector.isEmpty()) {

				Iterator<AbstractDetector> eventIterator = this.eventDetector
						.iterator();

				while (eventIterator.hasNext()) {
					AbstractDetector event = eventIterator.next();

					ephemeris.addEventDetector(event);

				}
			}

			missionDesign.setEphemeris(ephemeris);

			// Reset the event if not use in the next simulation
			this.eventDetector.clear();

		}

		// /////////////////////////////
		// Eckstein Hechler Propogator//
		// /////////////////////////////

		else if (propogator == PropagatorNode.ECKSTEINHECHLER) {

			EcksteinHechlerPropagator prop = new EcksteinHechlerPropagator(
					orbitOrekit, this.mass,
					Constants.EGM96_EARTH_EQUATORIAL_RADIUS, this.mu,
					this.zonalCoefficients[0], this.zonalCoefficients[1],
					this.zonalCoefficients[2], this.zonalCoefficients[3],
					this.zonalCoefficients[4]);

			// Activate the ephemeris mode
			prop.setEphemerisMode();

			prop.propagate(EndOfSimulation);

			ephemeris = prop.getGeneratedEphemeris();

			if (!eventDetector.isEmpty()) {

				Iterator<AbstractDetector> eventIterator = this.eventDetector
						.iterator();

				while (eventIterator.hasNext()) {
					AbstractDetector event = eventIterator.next();

					ephemeris.addEventDetector(event);

				}
			}

			missionDesign.setEphemeris(ephemeris);

			// Reset the event if not use in the next simulation
			this.eventDetector.clear();

			// ////////////////////////////
			// Semi-Analytical propogator//
			// ////////////////////////////

		} else if (propogator == PropagatorNode.SEMIANALYTICAL) {

			// Not implemented yet

		}

		// /////////////
		// ////TLE//////
		// /////////////

		else if (propogator == PropagatorNode.TLE) {

			TLElements tle = initNode.getSatelliteTleElements();

			String satName = initNode.getSatelliteTleName();

			SatelliteTleSGP4 sat = new SatelliteTleSGP4(satName,
					tle.getLine1(), tle.getLine2(), new SatOption());

			ephemeris = sat.getEphemeris();

			if (!eventDetector.isEmpty()) {

				Iterator<AbstractDetector> eventIterator = this.eventDetector
						.iterator();

				while (eventIterator.hasNext()) {
					AbstractDetector event = eventIterator.next();

					ephemeris.addEventDetector(event);

				}
			}

			missionDesign.setEphemeris(ephemeris);

			// Reset the event if not use in the next simulation
			this.eventDetector.clear();

		}

	}// execute

	// passes in main app to add the internal frame to
	public void displaySettings(JSatTrak app) {

		String windowName = "" + getValueAt(0);
		JInternalFrame iframe = new JInternalFrame(windowName, true, true,
				true, true);

		// show satellite browser window
		PropagatorPanel gsBrowser = new PropagatorPanel(this, iframe); // non-modal
																		// version

		iframe.setContentPane(gsBrowser);
		iframe.setSize(415 + 60, 386 + 115); // w,h
		iframe.setLocation(5, 5);

		app.addInternalFrame(iframe);

	} // displaySettings

	// ==========================================
	// Get-Set Methods ==========================
	// ==========================================

	public int getPropogator() {
		return propogator;
	}

	public void setPropogator(int propogator) {
		this.propogator = propogator;
	}

	public int getN_max() {
		return n_max;
	}

	public void setN_max(int n_max) {
		this.n_max = n_max;
	}

	public int getM_max() {
		return m_max;
	}

	public void setM_max(int m_max) {
		this.m_max = m_max;
	}

	public boolean isIncludeLunarPert() {
		return includeLunarPert;
	}

	public void setIncludeLunarPert(boolean includeLunarPert) {
		this.includeLunarPert = includeLunarPert;
	}

	public boolean isIncludeSunPert() {
		return includeSunPert;
	}

	public void setIncludeSunPert(boolean includeSunPert) {
		this.includeSunPert = includeSunPert;
	}

	public boolean isIncludeSolRadPress() {
		return includeSolRadPress;
	}

	public void setIncludeSolRadPress(boolean includeSolRadPress) {
		this.includeSolRadPress = includeSolRadPress;
	}

	public boolean isIncludeAtmosDrag() {
		return includeAtmosDrag;
	}

	public void setIncludeAtmosDrag(boolean includeAtmosDrag) {
		this.includeAtmosDrag = includeAtmosDrag;
	}

	public double getMass() {
		return mass;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}

	public double getCR() {
		return CR;
	}

	public void setCR(double CR) {
		this.CR = CR;
	}

	public double getCD() {
		return CD;
	}

	public void setCD(double CD) {
		this.CD = CD;
	}

	public double getStepSize() {
		return stepSize;
	}

	public void setStepSize(double stepSize) {
		this.stepSize = stepSize;
	}

	public double getMinStepSize() {
		return minStepSize;
	}

	public void setMinStepSize(double minStepSize) {
		this.minStepSize = minStepSize;
	}

	public double getMaxStepSize() {
		return maxStepSize;
	}

	public void setMaxStepSize(double maxStepSize) {
		this.maxStepSize = maxStepSize;
	}

	public double getRelAccuracy() {
		return dP;
	}

	public void setRelAccuracy(double relAccuracy) {
		this.dP = relAccuracy;
	}

	public double getPopogateTimeLen() {
		return popogateTimeLen;
	}

	public void setPopogateTimeLen(double popogateTimeLen) {
		this.popogateTimeLen = popogateTimeLen;
	}

	// ====================================================
	// ======= ORBIT Problem Functions ===================
	// ====================================================
	/***************************************************************************
	 * Equations of Motion (accelerations) for 2-Body Problem + perturbations
	 ************************************************************************* 
	 * @param var
	 * @param vel
	 * @param t
	 * @return
	 */
	public double[] deriv(double[] var, double[] vel, double t) {
		// double[] acc = new double[3];
		// double[][] E = new double[3][3];
		// double[][] T = new double[3][3];
		//
		// // CAREFUL on time, should use TT then convert to UTC later or
		// // something??
		// // otherwise UTC time is not uniform length??
		//
		// // prepare - Transformation matrix to body-fixed system
		// // double Mjd_TT = Mjd0_TT + t / 86400.0; // mjd_tt = start epic
		// double Mjd_TT = (JD_TT0 + t / 86400.0) - AstroConst.JDminusMJD; //
		// Convert
		// // sim
		// // time
		// // to JD
		// // to
		// // MJD
		//
		// // calculate current UT time from TT
		// double Mjd_UT1 = Mjd_TT - Time.deltaT(Mjd_TT); //
		//
		// // careful if Mjd_TT > J2000.0 - should be take care of in
		// // PrecMatrix_Equ_Mjd
		// // good use of PrecMatrix_Equ_Mjd - followed by nutation to get TOD
		// T = MathUtils.mult(CoordinateConversion.NutMatrix(Mjd_TT),
		// CoordinateConversion.PrecMatrix_Equ_Mjd(AstroConst.MJD_J2000,
		// Mjd_TT));
		// // E = CoordinateConversion.GHAMatrix(Mjd_UT1);
		// E = MathUtils.mult(CoordinateConversion.GHAMatrix(Mjd_UT1), T);
		//
		// // Acceleration due to harmonic gravity field
		// acc = GravityField.AccelHarmonic(var, E, AstroConst.GM_Earth,
		// AstroConst.R_Earth, AstroConst.CS, n_max, m_max);
		//
		// // Luni-solar perturbations
		// double[] r_Sun = new double[3];
		// if (includeSunPert || includeSolRadPress) {
		// r_Sun = Sun.calculateSunPositionLowTT(Mjd_TT);
		// }
		//
		// if (includeSunPert) {
		// acc = MathUtils.add(acc,
		// GravityField.AccelPointMass(var, r_Sun, AstroConst.GM_Sun));
		// }
		//
		// double[] r_Moon = new double[3];
		// if (includeLunarPert) {
		// r_Moon = Moon.MoonPosition(Mjd_TT);
		// acc = MathUtils.add(acc, GravityField.AccelPointMass(var, r_Moon,
		// AstroConst.GM_Moon));
		// }
		//
		// // Solar radiation pressure
		// if (includeSolRadPress) {
		// acc = MathUtils.add(acc, MathUtils.scale(Sun.AccelSolrad(var,
		// r_Sun, area, mass, CR, AstroConst.P_Sol, AstroConst.AU),
		// Sun.Illumination(var, r_Sun)));
		// }
		//
		// // Atmospheric drag [uses, altitude]
		// if (includeAtmosDrag) {
		// acc = MathUtils.add(acc,
		// Atmosphere.AccelDrag(Mjd_TT, var, vel, T, area, mass, CD));
		// }

		double[] acc = null;
		return acc;

	} // deriv

	// verbose - debug
	private boolean verbose = false;

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public boolean getVerbose() {
		return verbose;
	}

	// public Vector getEphemerisVector();
	public void addState2Ephemeris(StateVector state) {
		// // add new points to ephemeris converting back to TT (and skipping 0)
		// if (!(state.state[0] == 0.0)) {
		// state.state[0] = (JD_TT0 + state.state[0] / 86400.0);
		// ephemeris.add(state);
		// }
	}

	// meant to be over ridden if there are any input vars
	public double getVar(int varInt) {
		double var = 0;

		switch (varInt) {
		case 0:
			var = popogateTimeLen;
			break;
		default:
			var = 0;
			break;
		}

		return var;
	}

	// meant to be over ridden if there are any input vars
	public void setVar(int varInt, double val) {
		switch (varInt) {
		case 0:
			popogateTimeLen = val;
			break;
		default:
			break;
		}
	}

	// meant to be over ridden if there are any input vars
	public Vector<InputVariable> getInputVarVector() {
		Vector<InputVariable> varVec = new Vector<InputVariable>(1);

		InputVariable inVar = new InputVariable(this, 0, varNames[0],
				popogateTimeLen);
		varVec.add(inVar);

		return varVec;
	}

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
				// mod pos -needs to be TEME of date
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
				// mod pos - needs to be TEME of date
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
				// mod pos - needs to be TEME of date
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

	public// in seconds
	// stopping conditions used
	boolean isStopOnApogee() {
		return stopOnApogee;
	}

	public void setStopOnApogee(boolean stopOnApogee) {
		this.stopOnApogee = stopOnApogee;
	}

	public boolean isStopOnPerigee() {
		return stopOnPerigee;
	}

	public void setStopOnPerigee(boolean stopOnPerigee) {
		this.stopOnPerigee = stopOnPerigee;
	}

	public double getMu() {
		return mu;
	}

	public void setMu(double mu) {
		this.mu = mu;
	}

	public double[] getZonalCoefficients() {
		return zonalCoefficients;
	}

	public void setZonalCoefficients(double c20, double c30, double c40,
			double c50, double c60) {
		this.zonalCoefficients = new double[] { c20, c30, c40, c50, c60 };
	}

	public int getNumberOfStep() {
		return numberOfStep;
	}

	public void addEventDetector(AbstractDetector eventDetector) {
		this.eventDetector.add(eventDetector);
	}

	public InitialConditionsNode getInitNode() {
		return initNode;
	}

}
