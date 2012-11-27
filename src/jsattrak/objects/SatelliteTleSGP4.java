/*
 * SatelliteProps.java
 *=====================================================================
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
 * Created on July 25, 2007, 3:14 PM
 *
 * Holds TLE and all satellite properties
 */

package jsattrak.objects;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;

import java.awt.Color;
import java.util.Random;

import jsattrak.utilities.TLElements;
import name.gano.astro.AstroConst;
import name.gano.astro.Kepler;
import name.gano.astro.coordinates.J2kCoordinateConversion;
import name.gano.astro.propogators.sgp4_cssi.SGP4SatData;
import name.gano.worldwind.modelloader.WWModel3D_new;
import net.java.joglutils.model.ModelFactory;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;
import org.orekit.utils.PVCoordinates;

/**
 * @author ganos
 * 
 */
public class SatelliteTleSGP4 extends AbstractSatellite {
	private TLElements tle;
	private final Frame ITRF2005 = FramesFactory.getITRF2005();

	private SGP4SatData sgp4SatData; // sgp4 propogator data
	private TLEPropagator orekitTlePropagator;

	// current time - julian date
	double currentJulianDate = -1;

	// TLE epoch -- used to calculate how old is TLE - Julian Date
	double tleEpochJD = -1; // no age

	// Position/velocity
	private Vector3D position = Vector3D.ZERO;
	private Vector3D velocity = Vector3D.ZERO;

	// lat,long,alt [radians, radians, m ]
	private double[] lla = new double[3];

	// plot options
	private boolean plot2d = true;
	private Color satColor = Color.RED; // randomize in future
	private boolean plot2DFootPrint = true;
	private boolean fillFootPrint = true;
	private int numPtsFootPrint = 41; // number of points in footprint, used to
										// be 101

	// ground track options -- grounds tracks draw to asending nodes,
	// re-calculated at acending nodes
	boolean showGroundTrack = true;
	private int grnTrkPointsPerPeriod = 81; // equally space in time >=2 // used
											// to be 121
	private double groundTrackLeadPeriodMultiplier = 2.0; // how far forward to
															// draw ground track
															// - in terms of
															// periods
	private double groundTrackLagPeriodMultiplier = 1.0; // how far behind to
															// draw ground track
															// - in terms of
															// periods
	double[][] latLongLead; // leading lat/long coordinates for ground track
	double[][] latLongLag; // laging lat/long coordinates for ground track
	private double[][] temePosLead; // leading TEME position coordinates for
									// ground track
	private double[][] temePosLag; // laging TEME position coordinates for
									// ground track
	private double[] timeLead; // array for holding times associated with lead
								// coordinates (Jul Date)
	private double[] timeLag; // array - times associated with lag coordinates
								// (Jul Date)
	boolean groundTrackIni = false; // if ground track has been initialized

	private boolean showName2D = true; // show name in 2D plots

	// 3D Options
	private boolean show3DOrbitTrace = true;
	private boolean show3DFootprint = true;
	private boolean show3DName = true; // not implemented to change yet
	private boolean show3D = true; // no implemented to change yet, or to modify
									// showing of sat
	private boolean showGroundTrack3d = false;
	private boolean show3DOrbitTraceECI = true; // show orbit in ECI mode
												// otherwise , ECEF

	// 3D model parameters
	private boolean use3dModel = false; // use custom 3D model (or default
										// sphere)
	private String threeDModelPath = "globalstar/Globalstar.3ds"; // path to the
																	// custom
																	// model,
																	// default=
																	// globalstar/Globalstar.3ds
																	// ?
	private transient WWModel3D_new threeDModel; // DO NOT STORE when saving --
													// need to reload this --
													// TOO MUCH DATA!
	private double threeDModelSizeFactor = 300000;

	/**
	 * Creates a new instance of SatelliteProps - default properties with given
	 * name and TLE lines
	 * 
	 * @param name
	 *            name of satellite
	 * @param tleLine1
	 *            first line of two line element
	 * @param tleLine2
	 *            second line of two line element
	 * @throws Exception
	 *             if TLE data is bad
	 */
	public SatelliteTleSGP4(String name, String tleLine1, String tleLine2)
			throws Exception {
		// create internal Orekit TLE object
		tle = new TLElements(name, tleLine1, tleLine2);

		// initialize sgp4 propogator data for the satellite
		sgp4SatData = new SGP4SatData();

		// modele de propa sgp4 Orekit
		orekitTlePropagator = TLEPropagator.selectExtrapolator(tle);

		// randomly pick color for satellite
		// === pick a random color
		Random generator = new Random();
		switch (generator.nextInt(6)) {
		case 0:
			satColor = Color.red;
			break;
		case 1:
			satColor = Color.blue;
			break;
		case 2:
			satColor = Color.green;
			break;
		case 3:
			satColor = Color.white;
			break;
		case 4:
			satColor = Color.yellow;
			break;
		case 5:
			satColor = Color.orange;
			break;
		default:
			satColor = Color.red;
			break;
		} // random color switch

		// try to load TLE into propogator

		// options - hard coded
		// char opsmode = SGP4utils.OPSMODE_IMPROVED; // OPSMODE_IMPROVED
		// SGP4unit.Gravconsttype gravconsttype = SGP4unit.Gravconsttype.wgs72;

		// load TLE data as strings and INI all SGP4 data
		// boolean loadSuccess = SGP4utils.readTLEandIniSGP4(name, tleLine1,
		// tleLine2, opsmode, gravconsttype, sgp4SatData);
		//
		// // if there is an error loading send an exception
		// if (!loadSuccess) {
		// throw new Exception("Error loading TLE error code:"
		// + sgp4SatData.error);
		// }

		// TLE age since AJD
		tleEpochJD = tle.getDate().durationFrom(AbsoluteDate.JULIAN_EPOCH) / 86400;

	}

	@Override
	public void updateTleData(TLElements newTLE) throws OrekitException {
		this.tle = newTLE; // save new TLE

		// new spg4 object
		sgp4SatData = new SGP4SatData();

		// modele de propa sgp4 Orekit
		orekitTlePropagator = TLEPropagator.selectExtrapolator(tle);

		// read TLE

		// TLE age since AJD
		tleEpochJD = tle.getDate().durationFrom(AbsoluteDate.JULIAN_EPOCH) /86400;

		// ground track needs to be redone with new data
		groundTrackIni = false;

		// System.out.println("Updated " + tle.getSatName() );
	}

	@Override
	public void propogate2JulDate(double julDate) throws OrekitException {
		// save date
		this.currentJulianDate = julDate;

		AbsoluteDate orekitJulDate = AbsoluteDate.JULIAN_EPOCH
				.shiftedBy(julDate * 86400);

		PVCoordinates posVit = orekitTlePropagator
				.getPVCoordinates(orekitJulDate);

		position = posVit.getPosition();
		velocity = posVit.getVelocity();


		// save old lat/long for ascending node check
		double[] oldLLA = lla.clone(); // copy old LLA

		// calculate Lat,Long,Alt - must use Mean of Date (MOD) Position
		// lla = GeoFunctions
		// .GeodeticLLA(posTEME, julDate - AstroConst.JDminusMJD); // j2kPos

		OneAxisEllipsoid orbit = new OneAxisEllipsoid(
				Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
				Constants.WGS84_EARTH_FLATTENING, ITRF2005);

		posVit = orekitTlePropagator.getPVCoordinates(orekitJulDate,
				this.ITRF2005);

		GeodeticPoint geodeticPoint = orbit.transform(posVit.getPosition(),
				this.ITRF2005, orekitJulDate);

		this.lla = new double[] { geodeticPoint.getLatitude(),
				geodeticPoint.getLongitude(), geodeticPoint.getAltitude() };

		// Check to see if the ascending node has been passed
		if (showGroundTrack == true) {
			if (groundTrackIni == false) // update ground track needed
			{
				initializeGroundTrack();
			} else if (oldLLA[0] < 0 && lla[0] >= 0) // check for ascending node
														// pass
			{
				// System.out.println("Ascending NODE passed: " +
				// tle.getSatName() );
				initializeGroundTrack(); // for new ini each time

			} // ascending node passed

		} // if show ground track is true

		// if 3D model - update its properties -- NOT DONE HERE - done in
		// OrbitModelRenderable (so it can be done for any sat)

	} // propogate2JulDate

	// initalize the ground track from any starting point, as long as Juldate
	// !=-1
	private void initializeGroundTrack() throws OrekitException {
		if (currentJulianDate == -1) {
			// nothing to do yet, we haven't been given an initial time
			return;
		}

		// find time of last acending node crossing

		// initial guess -- the current time
		double lastAscendingNodeTime = currentJulianDate; // time of last
															// ascending Node
															// Time

		// calculate period - in minutes
		double periodMin = Kepler.CalculatePeriod(AstroConst.GM_Earth,
				position.toArray(), velocity.toArray()) / (60.0);
		// System.out.println("period [min] = "+periodMin);

		// time step divisions (in fractions of a day)
		double fracOfPeriod = 15.0;
		double timeStep = (periodMin / (60.0 * 24.0)) / fracOfPeriod;

		// first next guess
		double newGuess1 = lastAscendingNodeTime - timeStep;

		// latitude variables
		double lat0 = lla[0]; // current latitude
		double lat1 = (calculateLatLongAltXyz(newGuess1))[0]; // calculate
																// latitude
																// values

		// bracket the crossing using timeStep step sizes
		while (!(lat0 >= 0 && lat1 < 0)) {
			// move back a step
			lastAscendingNodeTime = newGuess1;
			lat0 = lat1;

			// next guess
			newGuess1 = lastAscendingNodeTime - timeStep;

			// calculate latitudes of the new value
			lat1 = (calculateLatLongAltXyz(newGuess1))[0];
		} // while searching for ascending node

		// secand method -- determine within a second!
		double outJul = secantMethod(lastAscendingNodeTime - timeStep,
				lastAscendingNodeTime, 1.0 / (60.0 * 60.0 * 24.0), 20);
		// System.out.println("Guess 1:" + (lastAscendingNodeTime-timeStep) );
		// System.out.println("Guess 2:" + (lastAscendingNodeTime));
		// System.out.println("Answer: " + outJul);

		// update times: Trust Period Calculations for how far in the future and
		// past to calculate out to
		// WARNING: period calculation is based on osculating elements may not
		// be 100% accurate
		// as this is just for graphical updates should be okay (no mid-course
		// corrections assumed)
		lastAscendingNodeTime = outJul;
		double leadEndTime = lastAscendingNodeTime
				+ groundTrackLeadPeriodMultiplier * periodMin / (60.0 * 24); // Julian
																				// Date
																				// for
																				// last
																				// lead
																				// point
																				// (furthest
																				// in
																				// future)
		double lagEndTime = lastAscendingNodeTime
				- groundTrackLagPeriodMultiplier * periodMin / (60.0 * 24); // Julian
																			// Date
																			// for
																			// the
																			// last
																			// lag
																			// point
																			// (furthest
																			// in
																			// past)

		// fill in lead/lag arrays
		fillGroundTrack(lastAscendingNodeTime, leadEndTime, lagEndTime);

		groundTrackIni = true;
		return;

	} // initializeGroundTrack

	// fill in the Ground Track given Jul Dates for
	//
	private void fillGroundTrack(double lastAscendingNodeTime,
			double leadEndTime, double lagEndTime) throws OrekitException {
		// points in the lead direction
		int ptsLead = (int) Math.ceil(grnTrkPointsPerPeriod
				* groundTrackLeadPeriodMultiplier);
		latLongLead = new double[ptsLead][3];
		temePosLead = new double[ptsLead][3];
		timeLead = new double[ptsLead];

		for (int i = 0; i < ptsLead; i++) {
			double ptTime = lastAscendingNodeTime + i
					* (leadEndTime - lastAscendingNodeTime) / (ptsLead - 1);

			// PUT HERE calculate lat lon
			double[] ptLlaXyz = calculateLatLongAltXyz(ptTime);

			latLongLead[i][0] = ptLlaXyz[0]; // save lat
			latLongLead[i][1] = ptLlaXyz[1]; // save long
			latLongLead[i][2] = ptLlaXyz[2]; // save altitude

			temePosLead[i][0] = ptLlaXyz[3]; // x
			temePosLead[i][1] = ptLlaXyz[4]; // y
			temePosLead[i][2] = ptLlaXyz[5]; // z

			timeLead[i] = ptTime; // save time

		} // for each lead point

		// points in the lag direction
		int ptsLag = (int) Math.ceil(grnTrkPointsPerPeriod
				* groundTrackLagPeriodMultiplier);
		latLongLag = new double[ptsLag][3];
		temePosLag = new double[ptsLag][3];
		timeLag = new double[ptsLag];

		for (int i = 0; i < ptsLag; i++) {
			double ptTime = lastAscendingNodeTime + i
					* (lagEndTime - lastAscendingNodeTime) / (ptsLag - 1);

			double[] ptLlaXyz = calculateLatLongAltXyz(ptTime);

			latLongLag[i][0] = ptLlaXyz[0]; // save lat
			latLongLag[i][1] = ptLlaXyz[1]; // save long
			latLongLag[i][2] = ptLlaXyz[2]; // save alt

			temePosLag[i][0] = ptLlaXyz[3]; // x
			temePosLag[i][1] = ptLlaXyz[4]; // y
			temePosLag[i][2] = ptLlaXyz[5]; // z

			timeLag[i] = ptTime;

		} // for each lag point
	} // fillGroundTrack

	// takes in JulDate, returns lla and teme position
	private double[] calculateLatLongAltXyz(double ptTime)
			throws OrekitException {
		Vector3D ptPos = calculatePositionFromUT(ptTime);

		// get lat and long

		AbsoluteDate orekitJulDate = AbsoluteDate.JULIAN_EPOCH
				.shiftedBy(ptTime * 86400);

		OneAxisEllipsoid orbit = new OneAxisEllipsoid(
				Constants.GRS80_EARTH_EQUATORIAL_RADIUS,
				Constants.GRS80_EARTH_FLATTENING, ITRF2005);

		PVCoordinates posVit;
		GeodeticPoint geodeticPoint = null;
		try {
			posVit = orekitTlePropagator.getPVCoordinates(orekitJulDate,
					ITRF2005);
			geodeticPoint = orbit.transform(posVit.getPosition(), ITRF2005,
					orekitJulDate);
		} catch (OrekitException e) {
			e.printStackTrace();
		}

		double[] ptLlaXyz = new double[] { geodeticPoint.getLatitude(),
				geodeticPoint.getLongitude(), geodeticPoint.getAltitude(),
				ptPos.getX(), ptPos.getY(), ptPos.getZ() };

		return ptLlaXyz;
	} // calculateLatLongAlt

	//

	/**
	 * Calculate  position of this sat
	 * at a given JulDateTime (doesn't save the time) - can be useful for event
	 * searches or optimization
	 * 
	 * @param julDate
	 *            - julian date
	 * @return position of satellite in meters
	 * @throws OrekitException
	 */
	@Override
	public Vector3D calculatePositionFromUT(double julDate)
			throws OrekitException {
		Vector3D ptPos = Vector3D.ZERO;

		AbsoluteDate orekitJulDate = AbsoluteDate.JULIAN_EPOCH
				.shiftedBy(julDate * 86400);

		PVCoordinates posVit = null;

		posVit = orekitTlePropagator.getPVCoordinates(orekitJulDate);

		ptPos = posVit.getPosition();

		return ptPos;

	} // calculatePositionFromUT

	// ---------------------------------------
	// SECANT Routines to find Crossings of the Equator (hopefully Ascending
	// Nodes)
	// xn_1 = date guess 1
	// xn date guess 2
	// tol = convergence tolerance
	// maxIter = maximum iterations allowed
	// RETURNS: double = julian date of crossing
	private double secantMethod(double xn_1, double xn, double tol, int maxIter)
			throws OrekitException {

		double d;

		// calculate functional values at guesses
		double fn_1 = latitudeGivenJulianDate(xn_1);
		double fn = latitudeGivenJulianDate(xn);

		for (int n = 1; n <= maxIter; n++) {
			d = (xn - xn_1) / (fn - fn_1) * fn;
			if (Math.abs(d) < tol) // convergence check
			{
				// System.out.println("Iters:"+n);
				return xn;
			}

			// save past point
			xn_1 = xn;
			fn_1 = fn;

			// new point
			xn = xn - d;
			fn = latitudeGivenJulianDate(xn);
		}

		System.out
				.println("Warning: Secant Method - Max Iteration limit reached finding Asending Node.");

		return xn;
	} // secantMethod

	private double latitudeGivenJulianDate(double julDate)
			throws OrekitException {
		// computer latiude of the spacecraft at a given date
		Vector3D ptPos = calculatePositionFromUT(julDate);

		// get lat and long

		AbsoluteDate orekitJulDate = AbsoluteDate.JULIAN_EPOCH
				.shiftedBy(julDate * 86400);

		OneAxisEllipsoid orbit = new OneAxisEllipsoid(
				Constants.GRS80_EARTH_EQUATORIAL_RADIUS,
				Constants.GRS80_EARTH_FLATTENING, ITRF2005);

		PVCoordinates posVit;
		GeodeticPoint geodeticPoint = null;
		try {
			posVit = orekitTlePropagator.getPVCoordinates(orekitJulDate,
					ITRF2005);
			geodeticPoint = orbit.transform(posVit.getPosition(), ITRF2005,
					orekitJulDate);
		} catch (OrekitException e) {
			e.printStackTrace();
		}

		return geodeticPoint.getLatitude(); // pass back latitude

	} // latitudeGivenJulianDate

	// --------------------------------------

	public void setShowGroundTrack(boolean showGrndTrk) throws OrekitException {
		showGroundTrack = showGrndTrk;

		if (showGrndTrk == false) {
			groundTrackIni = false;
			latLongLead = new double[][] { {} }; // save some space
			latLongLag = new double[][] { {} }; // sace some space
			temePosLag = new double[][] { {} };
			temePosLead = new double[][] { {} };
			timeLead = new double[] {};
			timeLag = new double[] {};
		} else {
			// ground track needs to be initalized
			initializeGroundTrack();
		}
	}

	public boolean getShowGroundTrack() {
		return showGroundTrack;
	}

	public double getLatitude() {
		return lla[0];
	}

	public double getLongitude() {
		return lla[1];
	}

	public double getAltitude() {
		return lla[2];
	}

	public double[] getLLA() {
		return lla;
	}

	// TT or UTC? = UTC
	public double getSatTleEpochJulDate() {
		return sgp4SatData.jdsatepoch;
	}

	public double getCurrentJulDate() {
		return currentJulianDate;
	}

	public boolean getPlot2D() {
		return plot2d;
	}

	public Color getSatColor() {
		return satColor;
	}

	public boolean getPlot2DFootPrint() {
		return plot2DFootPrint;
	}

	public boolean getGroundTrackIni() {
		return groundTrackIni;
	}

	public void setGroundTrackIni2False() {
		// forces repaint of ground track next update
		groundTrackIni = false;
	}

	public int getNumGroundTrackLeadPts() {
		return latLongLead.length;
	}

	public int getNumGroundTrackLagPts() {
		return latLongLag.length;
	}

	public double[] getGroundTrackLlaLeadPt(int index) {
		return new double[] { latLongLead[index][0], latLongLead[index][1],
				latLongLead[index][2] };
	}

	public double[] getGroundTrackLlaLagPt(int index) {
		return new double[] { latLongLag[index][0], latLongLag[index][1],
				latLongLag[index][2] };
	}

	public double[] getGroundTrackXyzLeadPt(int index) {
		return new double[] { getTemePosLead()[index][0],
				getTemePosLead()[index][1], getTemePosLead()[index][2] };
	}

	public double[] getGroundTrackXyzLagPt(int index) {
		return new double[] { getTemePosLag()[index][0],
				getTemePosLag()[index][1], getTemePosLag()[index][2] };
	}

	// returns satellite's current perdiod based on current pos/vel in Minutes
	public double getPeriod() {
		return Kepler.CalculatePeriod(AstroConst.GM_Earth, position.toArray(),
				velocity.toArray()) / (60.0);
	}

	public String getName() {
		return tle.getSatName();
	}

	public double[] getKeplarianElements() {
		return Kepler.SingularOsculatingElements(AstroConst.GM_Earth,
				position.toArray(), velocity.toArray());
	}

	public double getTleEpochJD() {
		return tleEpochJD;
	}

	public double getTleAgeDays() {
		return currentJulianDate - tleEpochJD;
	}

	public int getNumPtsFootPrint() {
		return numPtsFootPrint;
	}

	public void setNumPtsFootPrint(int numPtsFootPrint) {
		this.numPtsFootPrint = numPtsFootPrint;
	}

	public boolean isShowName2D() {
		return showName2D;
	}

	public void setShowName2D(boolean showName2D) {
		this.showName2D = showName2D;
	}

	public boolean isFillFootPrint() {
		return fillFootPrint;
	}

	public void setFillFootPrint(boolean fillFootPrint) {
		this.fillFootPrint = fillFootPrint;
	}

	public int getGrnTrkPointsPerPeriod() {
		return grnTrkPointsPerPeriod;
	}

	public void setGrnTrkPointsPerPeriod(int grnTrkPointsPerPeriod) {
		this.grnTrkPointsPerPeriod = grnTrkPointsPerPeriod;
	}

	public double getGroundTrackLeadPeriodMultiplier() {
		return groundTrackLeadPeriodMultiplier;
	}

	public void setGroundTrackLeadPeriodMultiplier(
			double groundTrackLeadPeriodMultiplier) {
		this.groundTrackLeadPeriodMultiplier = groundTrackLeadPeriodMultiplier;
	}

	public double getGroundTrackLagPeriodMultiplier() {
		return groundTrackLagPeriodMultiplier;
	}

	public void setGroundTrackLagPeriodMultiplier(
			double groundTrackLagPeriodMultiplier) {
		this.groundTrackLagPeriodMultiplier = groundTrackLagPeriodMultiplier;
	}

	public void setPlot2d(boolean plot2d) {
		this.plot2d = plot2d;
	}

	public void setSatColor(Color satColor) {
		this.satColor = satColor;
	}

	public void setPlot2DFootPrint(boolean plot2DFootPrint) {
		this.plot2DFootPrint = plot2DFootPrint;
	}

	public Vector3D getJ2000Position() {
		return position;
	}

	public boolean isShow3DOrbitTrace() {
		return show3DOrbitTrace;
	}

	public void setShow3DOrbitTrace(boolean show3DOrbitTrace) {
		this.show3DOrbitTrace = show3DOrbitTrace;
	}

	public boolean isShow3DFootprint() {
		return show3DFootprint;
	}

	public void setShow3DFootprint(boolean show3DFootprint) {
		this.show3DFootprint = show3DFootprint;
	}

	public boolean isShow3DName() {
		return show3DName;
	}

	public void setShow3DName(boolean show3DName) {
		this.show3DName = show3DName;
	}

	public boolean isShowGroundTrack3d() {
		return showGroundTrack3d;
	}

	public void setShowGroundTrack3d(boolean showGroundTrack3d) {
		this.showGroundTrack3d = showGroundTrack3d;
	}

	public boolean isShow3DOrbitTraceECI() {
		return show3DOrbitTraceECI;
	}

	public void setShow3DOrbitTraceECI(boolean show3DOrbitTraceECI) {
		this.show3DOrbitTraceECI = show3DOrbitTraceECI;
	}

	public boolean isShow3D() {
		return show3D;
	}

	public void setShow3D(boolean show3D) {
		this.show3D = show3D;
	}

	public// laging lat/long coordinates for ground track
	double[][] getTemePosLead() {
		return temePosLead;
	}

	public// leading Mean of date position coordinates for ground track
	double[][] getTemePosLag() {
		return temePosLag;
	}

	public// laging Mean of date position coordinates for ground track
	double[] getTimeLead() {
		return timeLead;
	}

	public// array for holding times associated with lead coordinates (Jul Date)
	double[] getTimeLag() {
		return timeLag;
	}

	// 3D model -------------------------
	public boolean isUse3dModel() {
		return use3dModel;
	}

	public void setUse3dModel(boolean use3dModel) {
		this.use3dModel = use3dModel;

		if (use3dModel && threeDModelPath.length() > 0) {
			// check that file exsists? - auto done in loader

			// String path = "data/models/globalstar/Globalstar.3ds";
			// String path = "data/models/isscomplete/iss_complete.3ds";

			loadNewModel(threeDModelPath);
		}
	}

	public String getThreeDModelPath() {
		return threeDModelPath;
	}

	/**
	 * Relative path to the model -- relative from "user.dir"/data/models/
	 * 
	 * @param path
	 */
	public void setThreeDModelPath(String path) {
		if (use3dModel && !(path.equalsIgnoreCase(this.threeDModelPath))) {
			// need to load the model
			loadNewModel(path);// "test/data/globalstar/Globalstar.3ds");
		}

		this.threeDModelPath = path; // save path no matter
	}

	private void loadNewModel(String path) {
		String localPath = "data/models/"; // path to models root from user.dir

		try {
			net.java.joglutils.model.geometry.Model model3DS = ModelFactory
					.createModel(localPath + path);
			// model3DS.setUseLighting(false); // turn off lighting!

			threeDModel = new WWModel3D_new(model3DS, new Position(
					Angle.fromRadians(this.getLatitude()),
					Angle.fromRadians(this.getLongitude()), this.getAltitude()));

			threeDModel.setMaitainConstantSize(true);
			threeDModel.setSize(threeDModelSizeFactor); // this needs to be a
														// property!

			threeDModel.updateAttitude(this); // fixes attitude intitially

		} catch (Exception e) {
			System.out.println("ERROR LOADING 3D MODEL");
		}
	}

	public WWModel3D_new getThreeDModel() {
		return threeDModel;
	}

	public Vector3D getJ2000Velocity() {
		return velocity;
	}

	public double getThreeDModelSizeFactor() {
		return threeDModelSizeFactor;
	}

	public void setThreeDModelSizeFactor(double modelSizeFactor) {
		// should the 3D model be reloaded now?
		if (modelSizeFactor != threeDModelSizeFactor && use3dModel
				&& threeDModelPath.length() > 0) {
			// loadNewModel(threeDModelPath);
			if (threeDModel != null) {
				threeDModel.setSize(modelSizeFactor);
			}
		}

		this.threeDModelSizeFactor = modelSizeFactor;
	}

	@Override
	public String toString() {
		return this.tle.getSatName();
	}

} // SatelliteProps
