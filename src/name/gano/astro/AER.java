/*
 * Azimuth, Elevation, Range Calculations
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

package name.gano.astro;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * 
 * @author sgano
 */
public class AER {
	/**
	 * Overloaded function - Calculates the Azumuth, Elevation, and Range from
	 * Ground Station to one position
	 * 
	 * @param lla_deg_m_GS
	 *            Lat/Lon/Alt in degrees and meters of Ground Station
	 * @param vector3d
	 *            ECI position [3] for times - Mean of Date Position!! (since
	 *            SGP4 is MOD)
	 * @param time
	 *            time associated with eci_pos (Julian Date)
	 * @return Azumuth [deg], Elevation [deg], and Range vector [m], [i][3]
	 */
	public static double[] calculate_AER(double[] lla_deg_m_GS,
			Vector3D vector3d, double time) {
		double[][] eci_pos_array = new double[][] { { vector3d.getX(),
				vector3d.getY(), vector3d.getZ() } };
		double[] times = new double[] { time };

		double[][] aer = calculate_AER(lla_deg_m_GS, eci_pos_array, times);

		return aer[0];
	}

	/**
	 * Calculates the Azumuth, Elevation, and Range from Ground Station to set
	 * of positions
	 * 
	 * @param lla_deg_m_GS
	 *            Lat/Lon/Alt in degrees and meters of Ground Station
	 * @param eci_pos
	 *            ECI positions [i][3] for different times - Mean of Date
	 *            Position!! (since SGP4 is MOD)
	 * @param times
	 *            times associated with eci_pos (Julian Date)
	 * @return Azumuth [deg], Elevation [deg], and Range vector [m], [i][3]
	 */
	public static double[][] calculate_AER(double[] lla_deg_m_GS,
			double[][] eci_pos, double[] times) {
		double[][] aer = new double[times.length][3];

		// for each element
		for (int i = 0; i < times.length; i++) {

			// 0th step get local mean Sidereal time
			// first get mean sidereal time for this station
			double thetaDeg = Sidereal.Mean_Sidereal_Deg(times[i]
					- AstroConst.JDminusMJD, lla_deg_m_GS[1]);

			// first calculate ECI position of Station
			Vector3D eciGS = calculateECIpositionGivenSidereal(thetaDeg,
					lla_deg_m_GS);

			// find the vector between pos and GS
			double[] rECI = MathUtils.sub(eci_pos[i], eciGS.toArray());

			// calculate range
			aer[i][2] = MathUtils.norm(rECI);

			// now transform ECI to topocentric-horizon system (SEZ) (use
			// Geodetic Lat, not geocentric)
			double[] rSEZ = eci2sez(rECI, thetaDeg, lla_deg_m_GS[0]); // ECI
																		// vec,
																		// sidereal
																		// in
																		// Deg,
																		// latitude
																		// in
																		// deg

			// compute azimuth [radians] -> Deg
			// aer[0] = Math.atan(-rSEZ[1]/rSEZ[0]) * 180.0/Math.PI;
			aer[i][0] = Math.atan2(-rSEZ[0], rSEZ[1]) * 180.0 / Math.PI;

			// System.out.println("aer[0]_0=" + aer[0] + ", rSEZ[-0,1]=" +
			// (-rSEZ[0]) + ", " +rSEZ[1] );

			// do conversions so N=0, S=180, NW=270
			if (aer[i][0] <= 0) {
				aer[i][0] = Math.abs(aer[i][0]) + 90;
			} else {
				if (aer[i][0] <= 90) // (between 0 and 90)
				{
					aer[i][0] = -1.0 * aer[i][0] + 90.0;
				} else // between 90 and 180
				{
					aer[i][0] = -1.0 * aer[i][0] + 450.0;
				}
			}

			// compute elevation [radians]
			aer[i][1] = Math.asin(rSEZ[2] / aer[i][2]) * 180.0 / Math.PI;

			// System.out.println("SEZ: " + rSEZ[0] + ", " + rSEZ[1] + ", " +
			// rSEZ[2]);

		} // for each position
		return aer;
	} // calculate_AER

	// ECI position in meters of a position (tpyically ground site) - Uses Earth
	// Flattening; WGS-84
	// theta is pass in as Degrees!! (it is the local mean sidereal time)
	private static Vector3D calculateECIpositionGivenSidereal(double theta,
			double[] lla_deg_m) {
		// calc local mean sidereal time
		// double theta =
		// Sidereal.Mean_Sidereal_Deg(currentJulianDate-AstroConst.JDminusMJD,
		// lla_deg_m[1]);

		// calculate the ECI j2k position vector of the ground station at the
		// current time

		// // calculate geocentric latitude - using non spherical earth (in
		// radians)
		// // http://celestrak.com/columns/v02n03/
		// double geocentricLat = Math.atan( Math.pow(1.0-AstroConst.f_Earth,
		// 2.0) * Math.tan( lla_deg_m[0]*Math.PI/180.0 ) ); // (1-f)^2 tan(?).
		//
		// eciVec[2] = AstroConst.R_Earth * Math.sin( geocentricLat );
		// //lla_deg_m[0]*Math.PI/180.0 );
		// double r = AstroConst.R_Earth * Math.cos( geocentricLat );
		// //lla_deg_m[0]*Math.PI/180.0 );
		// eciVec[0] = r * Math.cos(theta*Math.PI/180.0);
		// eciVec[1] = r * Math.sin(theta*Math.PI/180.0);

		// alternate way to calcuate ECI position - using earth flattening
		// http://celestrak.com/columns/v02n03/
		double C = 1.0 / Math.sqrt(1.0 + AstroConst.f_Earth
				* (AstroConst.f_Earth - 2.0)
				* Math.pow(Math.sin(lla_deg_m[0] * Math.PI / 180.0), 2.0));
		double S = Math.pow(1.0 - AstroConst.f_Earth, 2.0) * C;

		double eciX = AstroConst.R_Earth * C
				* Math.cos(lla_deg_m[0] * Math.PI / 180.0)
				* Math.cos(theta * Math.PI / 180.0);
		double eciY = AstroConst.R_Earth * C
				* Math.cos(lla_deg_m[0] * Math.PI / 180.0)
				* Math.sin(theta * Math.PI / 180.0);
		double eciZ = AstroConst.R_Earth * S
				* Math.sin(lla_deg_m[0] * Math.PI / 180.0);

		return new Vector3D(eciX, eciY, eciZ);

	} // calculateECIposition

	// overloaded with no inputs -- calculates sidereal time for you
	/**
	 * Calculate the ECI position from Lat/Long/Alt and a Julian Date (uses
	 * Earth Flattening; WGS-84)
	 * 
	 * @param currentJulianDate
	 *            Julian Date
	 * @param lla_deg_m
	 *            lat [deg], long [deg], altitude [m]
	 * @return ECI position [x,y,z]
	 */
	public static Vector3D calculateECIposition(double currentJulianDate,
			double[] lla_deg_m) {
		// calculate the ECI j2k position vector of the ground station at the
		// current time

		// first get mean sidereal time for this station
		double theta = Sidereal.Mean_Sidereal_Deg(currentJulianDate
				- AstroConst.JDminusMJD, lla_deg_m[1]);

		return calculateECIpositionGivenSidereal(theta, lla_deg_m);

	} // calculateECIposition

	// transform ECI to topocentric-horizon system (SEZ) (south-East-Zenith)
	private static double[] eci2sez(double[] rECI, double thetaDeg,
			double latDeg) {
		double[] rSEZ = new double[3]; // new postion in SEZ coorinates

		// ? (the local sidereal time) -> (thetaDeg*Math.PI)
		// ? (the observer's latitude) - > (latDeg*Math.PI)
		rSEZ[0] = Math.sin(latDeg * Math.PI / 180.0)
				* Math.cos(thetaDeg * Math.PI / 180.0) * rECI[0]
				+ Math.sin(latDeg * Math.PI / 180.0)
				* Math.sin(thetaDeg * Math.PI / 180.0) * rECI[1]
				- Math.cos(latDeg * Math.PI / 180.0) * rECI[2];
		rSEZ[1] = -Math.sin(thetaDeg * Math.PI / 180.0) * rECI[0]
				+ Math.cos(thetaDeg * Math.PI / 180.0) * rECI[1];
		rSEZ[2] = Math.cos(latDeg * Math.PI / 180.0)
				* Math.cos(thetaDeg * Math.PI / 180.0) * rECI[0]
				+ Math.cos(latDeg * Math.PI / 180.0)
				* Math.sin(thetaDeg * Math.PI / 180.0) * rECI[1]
				+ Math.sin(latDeg * Math.PI / 180.0) * rECI[2];

		return rSEZ;
	}

}
