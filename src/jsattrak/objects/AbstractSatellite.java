/*
 * Abstract Class for Satellite Types
 * =====================================================================
 * Copyright (C) 2008-9 Shawn E. Gano
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

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;

import jsattrak.customsat.InitialConditionsNode;
import jsattrak.customsat.PropagatorNode;
import jsattrak.utilities.TLElements;
import name.gano.worldwind.modelloader.WWModel3D_new;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.AbstractPropagator;
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
	public abstract Vector3D calculatePositionFromUT(double julDate) throws OrekitException;

	public abstract double getAltitude();

	public abstract double getCurrentJulDate();

	public abstract int getGrnTrkPointsPerPeriod();

	public abstract boolean getGroundTrackIni();

	public abstract double getGroundTrackLagPeriodMultiplier();

	public abstract double getGroundTrackLeadPeriodMultiplier();

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

	public abstract int getNumPtsFootPrint();

	public abstract double getPeriod();

	public abstract boolean getPlot2D();

	public abstract boolean getPlot2DFootPrint();

	public abstract Color getSatColor();
	
	public abstract Color getGroundTrackColor();

	public abstract double getSatTleEpochJulDate();

	public abstract boolean getShowGroundTrack();

	public abstract double[] getTimeLag();

	public abstract double[] getTimeLead();

	public abstract double getTleAgeDays();

	public abstract double getTleEpochJD();

	public abstract boolean isFillFootPrint();

	public abstract boolean isShow3D();

	public abstract boolean isShow3DFootprint();

	public abstract boolean isShow3DName();

	public abstract boolean isShow3DOrbitTrace();

	public abstract boolean isShow3DOrbitTraceECI();

	public abstract boolean isShowGroundTrack3d();

	public abstract boolean isShowName2D();

	public abstract void propogate2JulDate(double julDate,boolean eventDetector) throws OrekitException;

	public abstract void setFillFootPrint(boolean fillFootPrint);

	public abstract void setGrnTrkPointsPerPeriod(int grnTrkPointsPerPeriod);

	public abstract void setGroundTrackIni2False();

	public abstract void setGroundTrackLagPeriodMultiplier(
			double groundTrackLagPeriodMultiplier);

	public abstract void setGroundTrackLeadPeriodMultiplier(
			double groundTrackLeadPeriodMultiplier);

	public abstract void setNumPtsFootPrint(int numPtsFootPrint);

	public abstract void setPlot2DFootPrint(boolean plot2DFootPrint);

	public abstract void setPlot2d(boolean plot2d);

	public abstract void setSatColor(Color satColor);

	public abstract void setShow3D(boolean show3D);

	public abstract void setShow3DFootprint(boolean show3DFootprint);

	public abstract void setShow3DName(boolean show3DName);

	public abstract void setShow3DOrbitTrace(boolean show3DOrbitTrace);

	public abstract void setShow3DOrbitTraceECI(boolean show3DOrbitTraceECI);

	public abstract void setShowGroundTrack(boolean showGrndTrk) throws OrekitException;

	public abstract void setShowGroundTrack3d(boolean showGroundTrack3d);

	public abstract void setShowName2D(boolean showName2D);

	public abstract void updateTleData(TLElements newTLE)
			throws OrekitException;

	public abstract boolean isUse3dModel();

	public abstract void setUse3dModel(boolean use3dModel);

	public abstract String getThreeDModelPath();

	public abstract void setThreeDModelPath(String path);

	public abstract WWModel3D_new getThreeDModel();

	public abstract double getThreeDModelSizeFactor();

	public abstract void setThreeDModelSizeFactor(double modelSizeFactor);

	public abstract boolean isEventDetected();

	public abstract void setEventDetected(boolean b);
	
	public abstract ArrayList<double[]> getEventPositions();
	
	public abstract ArrayList<String> getEventName();
	
	public abstract int getEventPosition2DPixelSize();
	
	public abstract void setName(String name);

	public abstract void setEphemeris(AbstractPropagator ephemeris);
	
	public abstract PropagatorNode getPropNode() ;

	public abstract InitialConditionsNode getInitNode();

	public abstract void setShowConsoleOnPropogate(boolean selected);

	public abstract AbstractPropagator getEphemeris();

	public abstract DefaultTreeTableModel getMissionTableModel();

	public abstract boolean isShowConsoleOnPropogate() ;
	

}
