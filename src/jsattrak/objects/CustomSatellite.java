/*
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
 */

package jsattrak.objects;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;

import java.awt.Color;
import java.awt.Toolkit;
import java.util.Vector;

import javax.swing.ImageIcon;

import jsattrak.customsat.InitialConditionsNode;
import jsattrak.customsat.PropogatorNode;
import jsattrak.customsat.StopNode;
import jsattrak.utilities.TLElements;
import name.gano.astro.AstroConst;
import name.gano.astro.Kepler;
import name.gano.astro.time.Time;
import name.gano.swingx.treetable.CustomTreeTableNode;
import name.gano.worldwind.modelloader.WWModel3D_new;
import net.java.joglutils.model.ModelFactory;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;
import org.orekit.utils.PVCoordinates;

/**
 * NOTE !!!!!!!! -- internal time for epehemeris is TT time all input times UTC
 * 
 * @author sgano
 */
public class CustomSatellite extends AbstractSatellite {
	// ====================================
	private int ephemerisIncrement = 30; // number of rows added at a time to
											// improve speed of memory
											// allocation
	// internal ephemeris (Time store in TT)
	// private Vector<StateVector> ephemeris = new Vector<StateVector>(
	// ephemerisIncrement, ephemerisIncrement); // array to store ephemeris
	private BoundedPropagator ephemeris = null;

	private InitialConditionsNode initNode = null;

	private PropogatorNode propNode = null;

	// Frame ITRF2005
	private final Frame ITRF2005 = FramesFactory.getITRF2005();

	// Modele de la terre
	private final OneAxisEllipsoid earth = new OneAxisEllipsoid(
			Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
			Constants.WGS84_EARTH_FLATTENING, this.ITRF2005);

	// ====================================

	// table model for the custom config panel and holds all the mission Nodes
	private DefaultTreeTableModel missionTableModel = new DefaultTreeTableModel(); // any
																					// TreeTableModel

	String name = "Custom Sat";

	// current time - julian date
	double currentJulianDate = -1;

	// TLE epoch -- used to calculate how old is TLE - Julian Date
	double tleEpochJD = -1; // no age

	// current J2000 position and velocity vectors

	// Position & velocity
	private Vector3D position = Vector3D.ZERO;
	private Vector3D velocity = Vector3D.ZERO;

	// current lat,long,alt [radians, radians, km/m ?]
	private double[] lla;// = new double[3];

	// plot options
	private boolean plot2d = true;
	private Color satColor = Color.RED; // randomize in future
	private boolean plot2DFootPrint = true;
	private boolean fillFootPrint = true;
	private int numPtsFootPrint = 101; // number of points in footprint

	// ground track options -- grounds tracks draw to asending nodes,
	// re-calculated at acending nodes
	boolean showGroundTrack = true;
	private int grnTrkPointsPerPeriod = 121; // equally space in time >=2
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
	private double[][] temePosLead; // leading Mean of date position coordinates
									// for ground track
	private double[][] temePosLag; // laging Mean of date position coordinates
									// for ground track
	private double[] timeLead; // array for holding times associated with lead
								// coordinates (Jul Date) - UTC?
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

	private boolean showConsoleOnPropogate = true;

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

	public CustomSatellite(String name, Time scenarioEpochDate)
			throws OrekitException {
		this.name = name;
		iniMissionTableModel(scenarioEpochDate);
	}

	// initalizes the mission Table Model
	private void iniMissionTableModel(Time scenarioEpochDate) throws OrekitException {
		// set names of columns
		Vector<String> tableHeaders = new Vector<String>();
		tableHeaders.add("Mission Objects");
		// tableHeaders.add("Time Start?");
		// tableHeaders.add("Time Stop?");

		missionTableModel.setColumnIdentifiers(tableHeaders);

		// Add root Node
		String[] str = new String[3];
		str[0] = name;

		// DefaultMutableTreeTableNode ttn = new
		// DefaultMutableTreeTableNode(str);
		CustomTreeTableNode rootNode = new CustomTreeTableNode(str);
		rootNode.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource("/icons/custom/sat_icon.png"))));
		missionTableModel.setRoot(rootNode);

		// must add Initial conditions
		// Initial Node
		this.initNode = new InitialConditionsNode(rootNode, scenarioEpochDate);

		// by default also add a propogator node
		// Propogator Node
		this.propNode = new PropogatorNode(rootNode, initNode);

		// ADD SOME NODES (example) -----
		// CustomTreeTableNode ttn2 = new PropogatorNode(rootNode);
		// ttn2.setValueAt("3 Jan 2008", 1); // example at setting a columns
		// value
		//
		// ttn2 = new SolverNode(rootNode, true); // parent / add default
		// children
		//
		// ttn2 = new StopNode(rootNode);
		//
		// ------------------------------

		// must add stop node
		new StopNode(rootNode);

	}

	// ================================================================
	// functions that have to be fixed yet =========================
	// =================================================================

	// this function is basically given time update all current info and update
	// lead/lag data if needed
	@Override
	public void propogate2JulDate(double julDate) throws OrekitException {
		// save date
		this.currentJulianDate = julDate; // UTC
		AbsoluteDate maxTime;
		AbsoluteDate minTime;

//		AbsoluteDate orekitJulDate = new AbsoluteDate(AbsoluteDate.JULIAN_EPOCH, julDate * 86400, TimeScalesFactory.getUTC());

		
		AbsoluteDate orekitJulDate = AbsoluteDate.JULIAN_EPOCH
				.shiftedBy(julDate * 86400);

		// find the nodes closest to the current time
		if (ephemeris != null) //
		{
			// double epochkMJD = tleEpochJD - AstroConst.JDminusMJD;

			// in UTC
			// minTime = ephemeris.get(0).state[0] - deltaTT2UTC;
			// maxTime = ephemeris.get(ephemeris.size() - 1).state[0]
			// - deltaTT2UTC;

			minTime = ephemeris.getMinDate();
			maxTime = ephemeris.getMaxDate();

			// see if the current time in inside of the ephemeris range
			if (orekitJulDate.compareTo(maxTime) <= 0
					&& orekitJulDate.compareTo(minTime) >= 0) {


				PVCoordinates pvCoordinateInertialFrame = ephemeris
						.getPVCoordinates(orekitJulDate,
								this.initNode.getFrame());

				PVCoordinates pvCoordinateEarthFrame = ephemeris
						.getPVCoordinates(orekitJulDate, ITRF2005);

				GeodeticPoint geodeticPoint = this.earth.transform(
						pvCoordinateEarthFrame.getPosition(), this.ITRF2005,
						orekitJulDate);


				// Satellite trace

				// Current position point
				position = pvCoordinateInertialFrame.getPosition();
				velocity = pvCoordinateInertialFrame.getVelocity();


				// save old lat/long for ascending node check
				double[] oldLLA = new double[3];
				if (lla != null) {
					oldLLA = lla.clone(); // copy old LLA
				}

				// current LLA
				// lla = GeoFunctions.GeodeticLLA(posTEME, currentMJDtime);

				lla = new double[] { geodeticPoint.getLatitude(),
						geodeticPoint.getLongitude(),
						geodeticPoint.getAltitude() };

				// Check to see if the ascending node has been passed
				if (showGroundTrack == true) {
					if (groundTrackIni == false || oldLLA == null) // update
																	// ground
																	// track
																	// needed
					{
						initializeGroundTrack();
					} else if (oldLLA[0] < 0 && lla[0] >= 0) // check for
																// ascending
																// node pass
					{
						// System.out.println("Ascending NODE passed: " +
						// tle.getSatName() );
						initializeGroundTrack(); // for new ini each time

					} // ascending node passed
						// ELSE if current time is not in the lead/lag interval
						// reinintialize it
					else if (timeLead[timeLead.length - 1] < julDate
							|| timeLag[0] > julDate) {
						initializeGroundTrack();
					}

				} // if show ground track is true

				// isInTime = true;
				// System.out.println("true");

			} else // not in the timeFrame
			{
				// only set to null if they aren't already
				if (position != null) {
					// set current arrays to null;
					position = null;
					lla = null;

					// clear ground track
					groundTrackIni = false;
					latLongLead = null; // save some space
					latLongLag = null; // sace some space
					temePosLag = null;
					temePosLead = null;
					timeLead = null;
					timeLag = null;
				}

				// isInTime = false;
				// System.out.println("false1");
			}

		}

	} // propogate2JulDate

	public double getSatTleEpochJulDate() {
		// if (ephemeris.size() > 0) {
		// return ephemeris.firstElement().state[0]; // returns TT time
		// } else {
		return 0; // means it hasn't been propagated yet
		// }
	}

	

	/**
	 * Calculate position of this sat at a given JulDateTime (doesn't save
	 * the time) - can be useful for event searches or optimization
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

		AbsoluteDate maxTime, minTime;

		AbsoluteDate orekitJulDate = AbsoluteDate.JULIAN_EPOCH
				.shiftedBy(julDate * 86400);

		// CAREFUL ON TIMES... TIME IN EPHMERIS IN TT NOT UTC!!

		// find the nodes closest to the current time
		if (ephemeris != null) //
		{

			minTime = ephemeris.getMinDate();
			maxTime = ephemeris.getMaxDate();

			// see if the current time in inside of the ephemeris range
			if (orekitJulDate.compareTo(maxTime) <= 0
					&& orekitJulDate.compareTo(minTime) >= 0) {

				ptPos = ephemeris.getPVCoordinates(orekitJulDate,
						this.initNode.getFrame()).getPosition();

			} 
		} // if epeheris contains anything

		return ptPos;

	} // calculatePositionFromUT

	private void initializeGroundTrack() throws OrekitException {
		// System.out.println("Ground Track Ini");

		if (currentJulianDate == -1) {
			// nothing to do yet, we haven't been given an initial time
			return;
		}

		// initial guess -- the current time (UTC)
		double lastAscendingNodeTime = currentJulianDate; // time of last
															// ascending Node
															// Time

		// calculate period - in minutes
		double periodMin = Kepler.CalculatePeriod(AstroConst.GM_Earth,
				position.toArray(), velocity.toArray()) / (60.0);

		// update times: Trust Period Calculations for how far in the future and
		// past to calculate out to
		// WARNING: period calculation is based on osculating elements may not
		// be 100% accurate
		// as this is just for graphical updates should be okay (no mid-course
		// corrections assumed)

		// lastAscendingNodeTime = outJul;
		// assume last ascending node is now
		lastAscendingNodeTime = currentJulianDate;

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

	// END -- functions to be fixed ===================

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

			AbsoluteDate absPtTime = AbsoluteDate.JULIAN_EPOCH
					.shiftedBy(ptTime * 86400);


			if (absPtTime.compareTo(ephemeris.getMinDate()) >= 0
					&& absPtTime.compareTo(ephemeris.getMaxDate()) <= 0) {

				// PUT HERE calculate lat lon
				double[] ptLlaXyz = calculateLatLongAltXyz(ptTime);

				latLongLead[i][0] = ptLlaXyz[0]; // save lat
				latLongLead[i][1] = ptLlaXyz[1]; // save long
				latLongLead[i][2] = ptLlaXyz[2]; // save altitude

				temePosLead[i][0] = ptLlaXyz[3]; // x
				temePosLead[i][1] = ptLlaXyz[4]; // y
				temePosLead[i][2] = ptLlaXyz[5]; // z
			} else // give value of NaN - so it can be detected and not used
			{
				latLongLead[i][0] = Double.NaN; // save lat
				latLongLead[i][1] = Double.NaN; // save long
				latLongLead[i][2] = Double.NaN; // save altitude

				temePosLead[i][0] = Double.NaN; // x
				temePosLead[i][1] = Double.NaN; // y
				temePosLead[i][2] = Double.NaN; // z
			}

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

			AbsoluteDate absPtTime = AbsoluteDate.JULIAN_EPOCH
					.shiftedBy(ptTime * 86400);


			if (absPtTime.compareTo(ephemeris.getMinDate()) >= 0
					&& absPtTime.compareTo(ephemeris.getMaxDate()) <= 0) {

				double[] ptLlaXyz = calculateLatLongAltXyz(ptTime);

				latLongLag[i][0] = ptLlaXyz[0]; // save lat
				latLongLag[i][1] = ptLlaXyz[1]; // save long
				latLongLag[i][2] = ptLlaXyz[2]; // save alt

				temePosLag[i][0] = ptLlaXyz[3]; // x
				temePosLag[i][1] = ptLlaXyz[4]; // y
				temePosLag[i][2] = ptLlaXyz[5]; // z
			} else // give value of NaN - so it can be detected and not used
			{
				latLongLag[i][0] = Double.NaN; // save lat
				latLongLag[i][1] = Double.NaN; // save long
				latLongLag[i][2] = Double.NaN; // save alt

				temePosLag[i][0] = Double.NaN; // x
				temePosLag[i][1] = Double.NaN; // y
				temePosLag[i][2] = Double.NaN; // z
			}

			timeLag[i] = ptTime;

		} // for each lag point
	} // fillGroundTrack

	// takes in JulDate
	private double[] calculateLatLongAltXyz(double julDate)
			throws OrekitException {

		Vector3D ptPos = calculatePositionFromUT(julDate);

		AbsoluteDate orekitJulDate = AbsoluteDate.JULIAN_EPOCH
				.shiftedBy(julDate * 86400);

		Vector3D pos = Vector3D.ZERO;
		if (ephemeris != null) //
		{

			AbsoluteDate minTime = ephemeris.getMinDate();
			AbsoluteDate maxTime = ephemeris.getMaxDate();

			// see if the current time in inside of the ephemeris range
			if (orekitJulDate.compareTo(maxTime) <= 0
					&& orekitJulDate.compareTo(minTime) >= 0) {


					pos = ephemeris.getPVCoordinates(orekitJulDate,
							this.ITRF2005).getPosition();


			} 
		} 

		GeodeticPoint geodeticPoint = null;
		try {

			geodeticPoint = this.earth.transform(pos, this.ITRF2005,
					orekitJulDate);
		} catch (OrekitException e) {
			e.printStackTrace();
		}

		double[] ptLlaXyz = new double[] { geodeticPoint.getLatitude(),
				geodeticPoint.getLongitude(), geodeticPoint.getAltitude(),
				ptPos.getX(), ptPos.getY(), ptPos.getZ() };

		return ptLlaXyz;
	} // calculateLatLongAlt

	// ==== empty functions to fulfill AbstractSatellite req ===========
	@Override
	public void updateTleData(TLElements newTLE) {
	}

	// ==================================================================

	// other functions ================

	// returns satellite's current perdiod based on current pos/vel in Minutes
	public double getPeriod() {
		if (position != null) {
			return Kepler.CalculatePeriod(AstroConst.GM_Earth,
					position.toArray(), velocity.toArray()) / (60.0);
		} else {
			return 0;
		}
	}

	public double[] getKeplarianElements() {
		return Kepler.SingularOsculatingElements(AstroConst.GM_Earth,
				position.toArray(), velocity.toArray());
	}

	// GET SET methods =================

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
		if (lla != null)
			return lla[0];
		else
			return 180; // not possible latitide
	}

	public double getLongitude() {
		if (lla != null)
			return lla[1];
		else
			return 270; // not possible long
	}

	public double getAltitude() {
		if (lla != null)
			return lla[2];
		else
			return 0;
	}

	public double[] getLLA() {
		if (lla == null) {
			return null;
		}

		return lla.clone();
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
		if (latLongLead != null)
			return latLongLead.length;
		else
			return 0;
	}

	public int getNumGroundTrackLagPts() {
		if (latLongLag != null)
			return latLongLag.length;
		else
			return 0;
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

	public String getName() {
		return name;
	}

	public double getTleEpochJD() {
		return tleEpochJD; // returns -1 since there is no TLE
	}

	public double getTleAgeDays() {
		return 0;// currentJulianDate - tleEpochJD; // SEG returns 0 since
					// really there is not TLE!!
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

	@Override
	public Vector3D getJ2000Position() {
		if (position == null) {
			return null;
		}

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

	// laging lat/long coordinates for ground track
	public double[][] getTemePosLead() {
		return temePosLead;
	}

	// leading Mean of date position coordinates for ground track
	public double[][] getTemePosLag() {
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

	public// array to store ephemeris
	// ====================================
	// table model for the custom config panel and holds all the mission Nodes
	DefaultTreeTableModel getMissionTableModel() {
		return missionTableModel;
	}

	public BoundedPropagator getEphemeris() {
		return ephemeris;
	}

	// set ephemeris
	public void setEphemeris(BoundedPropagator e) {
		this.ephemeris = e;

		// // fill out all needed arrays (such as lead or lag etc) in MOD
		// coordinates as needed
		// // latLongLead // lla
		// // modPosLead // x/y/z
		// // timeLead; // array for holding times associated with lead
		// coordinates (Jul Date) - UTC?
		//
		// int ephemerisSize = ephemeris.size();
		//
		// // create Lead (only for now -- all of ephemeris)
		// latLongLead = new double[ephemerisSize][3];
		// modPosLead = new double[ephemerisSize][3];
		// timeLead = new double[ephemerisSize];
		//
		// double[] currentJ2kPos = new double[3];
		//
		// for(int i=0;i<ephemerisSize;i++)
		// {
		// StateVector sv = ephemeris.elementAt(i);
		//
		// // get current j2k pos
		// currentJ2kPos[0] = sv.state[1];
		// currentJ2kPos[1] = sv.state[2];
		// currentJ2kPos[2] = sv.state[3];
		//
		// // save time
		// timeLead[i] = sv.state[0];
		// // mod pos
		// modPosLead[i] =
		// CoordinateConversion.EquatorialEquinoxFromJ2K(sv.state[0] -
		// AstroConst.JDminusMJD, currentJ2kPos);
		// // lla (submit time in UTC)
		// double deltaTT2UTC = Time.deltaT(sv.state[0] -
		// AstroConst.JDminusMJD); // = TT - UTC
		// latLongLead[i] = GeoFunctions.GeodeticLLA(modPosLead[i], sv.state[0]
		// - AstroConst.JDminusMJD - deltaTT2UTC); // tt-UTC = deltaTT2UTC
		//
		// }
		//
		// groundTrackIni = true; // okay ground track has been ini01

	} // set ephemeris

	public boolean isShowConsoleOnPropogate() {
		return showConsoleOnPropogate;
	}

	public void setShowConsoleOnPropogate(boolean showConsoleOnPropogate) {
		this.showConsoleOnPropogate = showConsoleOnPropogate;
	}

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
		double fn_1 = this.calculateLatLongAltXyz(xn_1)[0];
		double fn = this.calculateLatLongAltXyz(xn)[0];

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
			fn = this.calculateLatLongAltXyz(xn)[0];
		}

		System.out
				.println("Warning: Secant Method - Max Iteration limit reached finding Asending Node.");

		return xn;
	} // secantMethod

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

	@Override
	public Vector3D getJ2000Velocity() {
		if (velocity == null) {
			return null;
		}
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

	public InitialConditionsNode getInitNode() {
		return initNode;
	}

	public PropogatorNode getPropNode() {
		return propNode;
	}

	@Override
	public String toString() {
		return this.getName();
	}

}
