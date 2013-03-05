/*
 * Abstract Class for Satellite Types
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

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;

import jsattrak.customsat.InitialConditionsNode;
import jsattrak.customsat.MissionTableModel;
import jsattrak.customsat.PropagatorNode;
import jsattrak.customsat.SatOption;
import jsattrak.utilities.TLElements;
import name.gano.worldwind.modelloader.WWModel3D_new;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.AbstractPropagator;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.analytical.tle.TLEPropagator;

/**
 * 
 * @author sgano
 */
public abstract class AbstractSatellite implements Serializable {

	/**
	 * Calculate J2K position of this sat at a given JulDateTime (doesn't save
	 * the time) - can be useful for event searches or optimization
	 * 
	 * @param julDate
	 *            - julian date
	 * @return j2k position of satellite in meters
	 * @throws OrekitException
	 */
	public abstract Vector3D calculatePositionFromUT(double julDate)
			throws OrekitException;

	public abstract double getAltitude();

	public abstract double getCurrentJulDate();

	public abstract double[] getGroundTrackLlaLagPt(int index);

	public abstract double[] getGroundTrackLlaLeadPt(int index);

	public abstract double[] getGroundTrackXyzLagPt(int index);

	public abstract double[] getGroundTrackXyzLeadPt(int index);

	public abstract Vector3D getJ2000Position();

	public abstract Vector3D getJ2000Velocity();

	public abstract double[] getKeplarianElements();

	public abstract double[] getLLA();

	public abstract double getLatitude();

	public abstract double getLongitude();

	public abstract double[][] getTemePosLag();

	public abstract double[][] getTemePosLead();

	public abstract String getName();

	public abstract int getNumGroundTrackLagPts();

	public abstract int getNumGroundTrackLeadPts();

	public abstract double getPeriod();

	public abstract double getSatTleEpochJulDate();

	public abstract double[] getTimeLag();

	public abstract double[] getTimeLead();

	public abstract double getTleAgeDays();

	public abstract double getTleEpochJD();

	public abstract void propogate2JulDate(double julDate, boolean eventDetector)
			throws OrekitException;

	public abstract void setShowGroundTrack(boolean showGrndTrk)
			throws OrekitException;

	public abstract void updateTleData(TLElements newTLE)
			throws OrekitException;



	public abstract void setUse3dModel(boolean use3dModel);



	public abstract void setThreeDModelPath(String path);


	public abstract MissionTableModel getMissionTree();


	public abstract void setThreeDModelSizeFactor(double modelSizeFactor);

	public abstract boolean isEventDetected();

	public abstract void setEventDetected(boolean b);

	public abstract ArrayList<double[]> getEventPositions();

	public abstract ArrayList<String> getEventName();

	public abstract int getEventPosition2DPixelSize();

	public abstract void setName(String name);

	public abstract void setEphemeris(BoundedPropagator ephemeris);

	public abstract BoundedPropagator getEphemeris();

	public abstract SatOption getSatOptions();

}
