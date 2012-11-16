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
 * Created June 2009
 */
// simple test of the SGP4 propagator
package name.gano.astro.propogators.sgp4_cssi;

import java.io.File;

import jsattrak.utilities.TLElements;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.ZipJarCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.PVCoordinates;

/**
 * 19 June 2009
 * 
 * @author Shawn E. Gano, shawn@gano.name
 */
public class SGP4test {
	public static void main(String[] args) {

		// Initialisation d'OREKIT

		// change this if your orekit data is not located in your home directory
		File zipFile = new File("data/orekit-data.zip");
		if (zipFile.exists()) {
			DataProvidersManager.getInstance().addProvider(
					new ZipJarCrawler(zipFile));
		} else {
			System.err.println(zipFile.getAbsolutePath()
					+ " zip file found, aborting");
			System.exit(1);
		}

		// new sat data object
		SGP4SatData data = new SGP4SatData();

		// tle data
		String name = "ISS (ZARYA)";
		String line1 = "1 25544U 98067A   09161.51089941  .00015706  00000-0  11388-3 0   112";
		String line2 = "2 25544  51.6406 341.1646 0009228  98.8703 312.6668 15.73580432604904";

		// prop to a given date
		double propJD = 2454994.0; // JD to prop to
		double minutesSinceEpoch = (propJD - data.jdsatepoch) * 24.0 * 60.0;
		Vector3D pos = Vector3D.ZERO;
		Vector3D vel = Vector3D.ZERO;

		TLElements tle;
		TLEPropagator orekitTlePropagator = null;
		try {
			tle = new TLElements(name, line1, line2);
			orekitTlePropagator = TLEPropagator.selectExtrapolator(tle);
		} catch (OrekitException e1) {

			e1.printStackTrace();
		}
		// modele de propa sgp4 Orekit


		AbsoluteDate orekitJulDate = AbsoluteDate.JULIAN_EPOCH
				.shiftedBy(propJD * 86400);

		PVCoordinates posVit = null;
		try {
			posVit = orekitTlePropagator.getPVCoordinates(orekitJulDate);
		} catch (OrekitException e) {

			e.printStackTrace();
		}

		pos = posVit.getPosition();
		vel = posVit.getVelocity();

		// output
		System.out.println("Epoch of TLE (JD): " + orekitJulDate.toString());
		System.out.println(minutesSinceEpoch + ", " + pos.getX()/1000 + ", "
				+ pos.getY()/1000 + ", " + pos.getZ()/1000 + ", " + vel.getX()/1000 + ", "
				+ vel.getY()/1000 + ", " + vel.getZ()/1000);

		Vector3D stk8Results = new Vector3D(-2881017.428533447,
				-3207508.188455666, -5176685.907342243);
		Vector3D stk9Results = new Vector3D(-2881017.432281017,
				-3207508.189681858, -5176685.904856035);

		double dX = (pos.subtract(stk8Results))
				.getNorm();
		System.out.println("Error from STk8 (m) : " + dX);
		double dX2 = (pos.subtract(stk9Results))
				.getNorm();
		System.out.println("Error from STk9 (m) : " + dX2);

	}

	/**
	 * vector subtraction
	 * 
	 * @param a
	 *            vector of length 3
	 * @param b
	 *            vector of length 3
	 * @return a-b
	 */
	public static double[] sub(double[] a, double[] b) {
		double[] c = new double[3];
		for (int i = 0; i < 3; i++) {
			c[i] = a[i] - b[i];
		}

		return c;
	}

	// vector 2-norm
	/**
	 * vector 2-norm
	 * 
	 * @param a
	 *            vector of length 3
	 * @return norm(a)
	 */
	public static double norm(double[] a) {
		double c = 0.0;

		for (int i = 0; i < a.length; i++) {
			c += a[i] * a[i];
		}

		return Math.sqrt(c);
	}

	// multiply a vector times a scalar
	/**
	 * multiply a vector times a scalar
	 * 
	 * @param a
	 *            a vector of length 3
	 * @param b
	 *            scalar
	 * @return a * b
	 */
	public static double[] scale(double[] a, double b) {
		double[] c = new double[3];

		for (int i = 0; i < 3; i++) {
			c[i] = a[i] * b;
		}

		return c;
	}
}
