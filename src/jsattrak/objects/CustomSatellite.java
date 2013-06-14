/*
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

package jsattrak.objects;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import jsattrak.customsat.MissionTableModel;
import jsattrak.customsat.SatOption;
import jsattrak.utilities.TLElements;
import name.gano.astro.AstroConst;
import name.gano.astro.Kepler;
import name.gano.worldwind.modelloader.WWModel3D_new;
import net.java.joglutils.model.ModelFactory;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.propagation.events.EventDetector;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;
import org.orekit.utils.PVCoordinates;

/**
 * NOTE !!!!!!!! -- internal time for epehemeris is TT time all input times UTC
 * 
 * @author sgano
 * @author acouanon
 */
public class CustomSatellite extends AbstractSatellite {

	private static final long serialVersionUID = -7936848462137206875L;
	// ====================================
	
	private TLElements tle;
	
	private boolean isTle = false;
	
	private BoundedPropagator ephemeris = null;

	private ArrayList<double[]> eventPositions = new ArrayList<double[]>();

	private ArrayList<String> eventName = new ArrayList<String>();

	private boolean lastStepInitGroundTrack = false;

	private int eventPosition2DPixelSize = 10;

	// Terrestrial Frame ITRF2005
	private final Frame ITRF2005 = FramesFactory.getITRF2005();
	
	//Inertial Frame
	private final Frame inertialFrame = FramesFactory.getGCRF();
	
//	//Inertial Frame
//		private final Frame inertialFrame = FramesFactory.getICRF();

	// Earth model
	private final OneAxisEllipsoid earth = new OneAxisEllipsoid(
			Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
			Constants.WGS84_EARTH_FLATTENING, this.ITRF2005);

	// ====================================

	String name = "New Satellite";

	private MissionTableModel missionTree = null;

	private SatOption satOptions = null;

	// current time - julian date
	double currentJulianDate = -1;


	// current J2000 position and velocity vectors

	// Position & velocity
	private Vector3D position = Vector3D.ZERO;
	private Vector3D velocity = Vector3D.ZERO;

	// current lat,long,alt [radians, radians, km/m ?]
	private double[] lla;// = new double[3];

	private boolean eventDetected = false;

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

	public CustomSatellite(MissionTableModel missionTree, SatOption satOptions)
			throws OrekitException {

		this.missionTree = missionTree;
		this.satOptions = satOptions;
		// iniMissionTableModel(scenarioEpochDate);
	}
	
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
	public CustomSatellite(String name, String tleLine1, String tleLine2,
			SatOption satOptions) throws OrekitException {
		// create internal Orekit TLE object

		this.satOptions = satOptions;

		this.name = name;
		
		this.isTle = true;
		
		tle = new TLElements(name, tleLine1, tleLine2);

		// modele de propa sgp4 Orekit
		ephemeris = TLEPropagator.selectExtrapolator(tle)
				.getGeneratedEphemeris();

		// randomly pick color for satellite
		// === pick a random color
		Random generator = new Random();
		switch (generator.nextInt(6)) {
		case 0:
			satOptions.setSatColor(Color.red);
			break;
		case 1:
			satOptions.setSatColor(Color.blue);
			break;
		case 2:
			satOptions.setSatColor(Color.green);
			break;
		case 3:
			satOptions.setSatColor(Color.white);
			break;
		case 4:
			satOptions.setSatColor(Color.yellow);
			break;
		case 5:
			satOptions.setSatColor(Color.orange);
			break;
		default:
			satOptions.setSatColor(Color.red);
			break;
		} // random color switch

		satOptions.setGroundTrackColor(satOptions.getSatColor());

	}

	@Override
	public void propogate2JulDate(AbsoluteDate orekitJulDate, boolean eventDetector)
			throws OrekitException {
		// save date
		this.currentJulianDate = orekitJulDate.durationFrom(AbsoluteDate.JULIAN_EPOCH)/86400;
		AbsoluteDate maxTime;
		AbsoluteDate minTime;
		Collection<EventDetector> events = null;

		// find the nodes closest to the current time
		if (ephemeris != null) //
		{

			minTime = ephemeris.getMinDate();
			maxTime = ephemeris.getMaxDate();

			// see if the current time is inside of the ephemeris range
			if (orekitJulDate.compareTo(maxTime) <= 0
					&& orekitJulDate.compareTo(minTime) >= 0) {

				// Deleting events detection if you step back in time
				if (this.isEventDetected()
						&& this.getCurrentJulDate() < this.getEventPositions()
								.get(this.getEventPositions().size() - 1)[0]) {

					this.getEventPositions().remove(
							this.getEventPositions().size() - 1);
					this.getEventName().remove(this.getEventName().size() - 1);
					if (this.getEventPositions().isEmpty())
						this.eventDetected = false;

				}

				// Au step qui suit le calcul de la trace au sol il faut
				// desactiver
				// la detection d'evenement car s'il y a eut un evenement
				// precedement il sera redétecté car au premier tour qui suit le
				// calcul
				// de la trace au sol la propagation se fait depuis la
				// "date début"
				if (lastStepInitGroundTrack) {
					eventDetector = false;
					lastStepInitGroundTrack = false;
				}
				// Removing events
				if (!eventDetector) {
					events = ephemeris.getEventsDetectors();
					ephemeris.clearEventsDetectors();
				}


				PVCoordinates pvCoordinateInertialFrame;
				
				if(!isTle){
				
				 pvCoordinateInertialFrame = ephemeris
						.getPVCoordinates(orekitJulDate, this.missionTree
								.getInitNode().getFrame());
				}
				else{
					
				pvCoordinateInertialFrame = ephemeris
							.getPVCoordinates(orekitJulDate, inertialFrame);
					
				}

				PVCoordinates pvCoordinateEarthFrame = ephemeris
						.getPVCoordinates(orekitJulDate, ITRF2005);

				// Restore events
				if (!eventDetector) {
					for (EventDetector event : events)
						ephemeris.addEventDetector(event);
				}

				GeodeticPoint geodeticPoint = this.earth.transform(
						pvCoordinateEarthFrame.getPosition(), this.ITRF2005,
						orekitJulDate);

				// Satellite trace

				// Current position point in inertial frame
				position = pvCoordinateInertialFrame.getPosition();
				velocity = pvCoordinateInertialFrame.getVelocity();

				// save old lat/long for ascending node check
				double[] oldLLA = new double[3];
				if (lla != null) {
					oldLLA = lla.clone(); // copy old LLA
				}

				// current LLA
				lla = new double[] { geodeticPoint.getLatitude(),
						geodeticPoint.getLongitude(),
						geodeticPoint.getAltitude() };

				// Check to see if the ascending node has been passed
				if (satOptions.isShowGroundTrack() == true) {

					// update ground track needed || check for ascending node
					// pass || ascending node passed

					if ((satOptions.isGroundTrackIni() == false || oldLLA == null)
							|| (oldLLA[0] < 0 && lla[0] >= 0)
							|| (timeLead[timeLead.length - 1] < this.currentJulianDate || timeLag[0] > this.currentJulianDate)) {
						initializeGroundTrack();
						lastStepInitGroundTrack = true;
					}

				} // if show ground track is true

			} else // not in the timeFrame
			{
				// only set to null if they aren't already
				if (position != null) {
					// set current arrays to null;
					position = null;
					lla = null;

					// clear ground track
					satOptions.setGroundTrackIni(false);
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

	/**
	 * Calculate position of this sat at a given JulDateTime (doesn't save the
	 * time) - can be useful for event searches or optimization
	 * 
	 * @param julDate
	 *            - julian date
	 * @return position of satellite in meters
	 * @throws OrekitException
	 */
	@Override
	public Vector3D calculatePositionFromUT(AbsoluteDate orekitAbsoluteDate)
			throws OrekitException {
		Vector3D ptPos = Vector3D.ZERO;

		AbsoluteDate maxTime, minTime;

		// CAREFUL ON TIMES... TIME IN EPHMERIS IN TT NOT UTC!!

		// find the nodes closest to the current time
		if (ephemeris != null) //
		{

			minTime = ephemeris.getMinDate();
			maxTime = ephemeris.getMaxDate();

			// see if the current time in inside of the ephemeris range
			if (orekitAbsoluteDate.compareTo(maxTime) <= 0
					&& orekitAbsoluteDate.compareTo(minTime) >= 0) {

				if(!this.isTle){
				ptPos = ephemeris.getPVCoordinates(orekitAbsoluteDate,
						this.missionTree.getInitNode().getFrame())
						.getPosition();
				}
				else{
					ptPos = ephemeris.getPVCoordinates(orekitAbsoluteDate,
							inertialFrame)
							.getPosition();
					
				}

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

		double leadEndTime = lastAscendingNodeTime
				+ satOptions.getGroundTrackLeadPeriodMultiplier() * periodMin
				/ (60.0 * 24); // Julian
		// Date
		// for
		// last
		// lead
		// point
		// (furthest
		// in
		// future)
		double lagEndTime = lastAscendingNodeTime
				- satOptions.getGroundTrackLagPeriodMultiplier() * periodMin
				/ (60.0 * 24); // Julian
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

		satOptions.setGroundTrackIni(true);
		return;

	} // initializeGroundTrack

	// END -- functions to be fixed ===================

	// fill in the Ground Track given Jul Dates for
	//
	private void fillGroundTrack(double lastAscendingNodeTime,
			double leadEndTime, double lagEndTime) throws OrekitException {
		// points in the lead direction
		int ptsLead = (int) Math.ceil(satOptions.getGrnTrkPointsPerPeriod()
				* satOptions.getGroundTrackLeadPeriodMultiplier());
		latLongLead = new double[ptsLead][3];
		temePosLead = new double[ptsLead][3];
		timeLead = new double[ptsLead];

		// Withdrawal of events for the the ground track calculation
		Collection<EventDetector> events = ephemeris.getEventsDetectors();
		ephemeris.clearEventsDetectors();

		for (int i = 0; i < ptsLead; i++) {
			double ptTime = lastAscendingNodeTime + i
					* (leadEndTime - lastAscendingNodeTime) / (ptsLead - 1);
			
			//UTC time
			AbsoluteDate absPtTime = AbsoluteDate.JULIAN_EPOCH
					.shiftedBy(ptTime * 86400);


			if (absPtTime.compareTo(ephemeris.getMinDate()) >= 0
					&& absPtTime.compareTo(ephemeris.getMaxDate()) <= 0) {

				// PUT HERE calculate lat lon
				double[] ptLlaXyz = calculateLatLongAltXyz(absPtTime);

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
		int ptsLag = (int) Math.ceil(satOptions.getGrnTrkPointsPerPeriod()
				* satOptions.getGroundTrackLagPeriodMultiplier());
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

				double[] ptLlaXyz = calculateLatLongAltXyz(absPtTime);

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

		// Restore events
		for (EventDetector event : events)
			ephemeris.addEventDetector(event);

	} // fillGroundTrack

	// takes in JulDate
	private double[] calculateLatLongAltXyz(AbsoluteDate absoluteDate)
			throws OrekitException {		

		Vector3D pos = Vector3D.ZERO;
		Vector3D ptPos = Vector3D.ZERO;
		if (ephemeris != null) //
		{

			AbsoluteDate minTime = ephemeris.getMinDate();
			AbsoluteDate maxTime = ephemeris.getMaxDate();

			// see if the current time in inside of the ephemeris range
			if (absoluteDate.compareTo(maxTime) <= 0
					&& absoluteDate.compareTo(minTime) >= 0) {

				ptPos = ephemeris.getPVCoordinates(absoluteDate, this.inertialFrame)
						.getPosition();
				
				
				if(!this.isTle){
					pos = ephemeris.getPVCoordinates(absoluteDate,
							this.missionTree.getInitNode().getFrame())
							.getPosition();
					}
					else{
						pos = ephemeris.getPVCoordinates(absoluteDate,
								ITRF2005)
								.getPosition();
						
					}
				
			}
		}


		GeodeticPoint geodeticPoint = this.earth.transform(pos, this.ITRF2005, absoluteDate);

		double[] ptLlaXyz = new double[] { geodeticPoint.getLatitude(),
				geodeticPoint.getLongitude(), geodeticPoint.getAltitude(),
				ptPos.getX(), ptPos.getY(), ptPos.getZ() };

		return ptLlaXyz;
	} // calculateLatLongAlt

	@Override
	public void updateTleData(TLElements newTLE) throws OrekitException {
		this.tle = newTLE; // save new TLE

		// modele de propa sgp4 Orekit
		ephemeris = TLEPropagator.selectExtrapolator(tle)
				.getGeneratedEphemeris();

		// ground track needs to be redone with new data
		satOptions.setGroundTrackIni(false);

		// System.out.println("Updated " + tle.getSatName() );
	}

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
		satOptions.setShowGroundTrack(showGrndTrk);

		if (showGrndTrk == false) {
			satOptions.setGroundTrackIni(false);
			latLongLead = new double[][] { {} }; // save some space
			latLongLag = new double[][] { {} }; // sace some space
			temePosLag = new double[][] { {} };
			temePosLead = new double[][] { {} };
			timeLead = new double[] {};
			timeLag = new double[] {};
		} else {
			// ground track needs to be initalized

			// <<<<<<<<>soit retirer puis ajouter les evt comme au dessus soit
			// le faire pour tous dans
			// initializegroundtrack>>>>>>>
			initializeGroundTrack();
		}
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

	public double getTleAgeDays() {
		return 0;// currentJulianDate - tleEpochJD; // SEG returns 0 since
					// really there is not TLE!!
	}

	@Override
	public Vector3D getJ2000Position() {
		if (position == null) {
			return null;
		}

		return position;
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

	// 3D model -------------------------

	public void setUse3dModel(boolean use3dModel) {
		this.satOptions.setUse3dModel(use3dModel);

		if (use3dModel && satOptions.getThreeDModelPath().length() > 0) {
			// check that file exsists? - auto done in loader

			// String path = "data/models/globalstar/Globalstar.3ds";
			// String path = "data/models/isscomplete/iss_complete.3ds";

			loadNewModel(satOptions.getThreeDModelPath());
		}
	}

	/**
	 * Relative path to the model -- relative from "user.dir"/data/models/
	 * 
	 * @param path
	 */
	public void setThreeDModelPath(String path) {
		if (this.satOptions.isUse3dModel()
				&& !(path
						.equalsIgnoreCase(this.satOptions.getThreeDModelPath()))) {
			// need to load the model
			loadNewModel(path);// "test/data/globalstar/Globalstar.3ds");
		}

		this.satOptions.setThreeDModelPath(path); // save path no matter
	}

	private void loadNewModel(String path) {
		String localPath = "data/models/"; // path to models root from user.dir

		try {
			net.java.joglutils.model.geometry.Model model3DS = ModelFactory
					.createModel(localPath + path);
			// model3DS.setUseLighting(false); // turn off lighting!

			this.satOptions.setThreeDModel(new WWModel3D_new(model3DS,
					new Position(Angle.fromRadians(this.getLatitude()), Angle
							.fromRadians(this.getLongitude()), this
							.getAltitude())));

			this.satOptions.getThreeDModel().setMaitainConstantSize(true);
			this.satOptions.getThreeDModel().setSize(
					this.satOptions.getThreeDModelSizeFactor()); // this needs
																	// to be a
			// property!

			this.satOptions.getThreeDModel().updateAttitude(this); // fixes
																	// attitude
																	// intitially

		} catch (Exception e) {
			System.out.println("ERROR LOADING 3D MODEL");
		}
	}

	@Override
	public Vector3D getJ2000Velocity() {
		if (velocity == null) {
			return null;
		}
		return velocity;
	}

	public void setThreeDModelSizeFactor(double modelSizeFactor) {
		// should the 3D model be reloaded now?
		if (modelSizeFactor != this.satOptions.getThreeDModelSizeFactor()
				&& this.satOptions.isUse3dModel()
				&& this.satOptions.getThreeDModelPath().length() > 0) {
			// loadNewModel(threeDModelPath);
			if (this.satOptions.getThreeDModel() != null) {
				this.satOptions.getThreeDModel().setSize(modelSizeFactor);
			}
		}

		this.satOptions.setThreeDModelSizeFactor(modelSizeFactor);
	}

	public boolean isEventDetected() {
		return eventDetected;
	}

	public void setEventDetected(boolean eventDetected) {
		this.eventDetected = eventDetected;
	}

	public ArrayList<double[]> getEventPositions() {
		return eventPositions;
	}

	public int getEventPosition2DPixelSize() {
		return eventPosition2DPixelSize;
	}

	public ArrayList<String> getEventName() {
		return eventName;
	}

	@Override
	public String toString() {
		return this.getName();
	}

	@Override
	public void setName(String name) {
		this.name = name;
		this.missionTree.getRootNode().setValueAt(name, 0);
	}

	@Override
	public void setEphemeris(BoundedPropagator ephemeris) {
		this.ephemeris = ephemeris;

	}

	public BoundedPropagator getEphemeris() {
		return ephemeris;
	}

	public SatOption getSatOptions() {
		return satOptions;
	}

	public MissionTableModel getMissionTree() {
		return missionTree;
	}

	public boolean isTle() {
		return isTle;
	}

}
