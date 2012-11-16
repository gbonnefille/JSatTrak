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
// runs the Verification TLEs just like Vallado's C++ code does
//
// 19 June 2009 Results: only one digit different in the entire file: ( for OPSMODE_IMPROVED, wgs72)
//line 655 - only diff  x value of position:
// all error codes and times of errors where also the same
// value that was different (x pos line 655)
//cpp  = -23575.69186056
//java = -23575.69186057
//
// other combinations for the opsmode and gravity constants had similar results with very few
// differences in the cpp vs java output files

package name.gano.astro.propogators.sgp4_cssi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

import jsattrak.utilities.TLElements;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.PVCoordinates;

/**
 * 
 * @author Shawn E. Gano, shawn@gano.name
 */
public class SGP4verification {
	public static void main(String[] args) {
		// settings
		char opsmode = SGP4utils.OPSMODE_IMPROVED; // OPSMODE_IMPROVED
		SGP4unit.Gravconsttype gravconsttype = SGP4unit.Gravconsttype.wgs72;

		// tle verification file (with extra start, stop, timestep params on
		// line 2)
		String verTLEfile = "sgp4-ver.tle"; // sgp4-ver.tle shawn_ver.tle

		// output results to this file
		String javaResults = "java_sgp4_ver.out";

		// comparison file, cpp results
		String cppResultsFile = "tcppver.out";

		// internal variables -------------------------
		Vector3D ro = Vector3D.ZERO;
		Vector3D vo = Vector3D.ZERO;

		// get constants -------------------------------
		double[] gtt = SGP4unit.getgravconst(gravconsttype);// , tumin, mu,
															// radiusearthkm,
															// xke, j2, j3, j4,
															// j3oj2 );
		double tumin = gtt[0];
		double mu = gtt[1];
		double radiusearthkm = gtt[2];
		double xke = gtt[3];
		double j2 = gtt[4];
		double j3 = gtt[5];
		double j4 = gtt[6];
		double j3oj2 = gtt[7];

		System.out.println("======  PROPOGATING VERIFICATION TLEs ====== ");
		// open the TLE file and propogate each TLE --------------------
		try {
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(verTLEfile);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			// output file
			// Create file
			FileWriter outStream = new FileWriter(javaResults);
			BufferedWriter out = new BufferedWriter(outStream);

			String strLine1;
			String strLine2;
			// Read File Line By Line
			while ((strLine1 = br.readLine()) != null) {
				if (!strLine1.startsWith("#")) // ignore lines starting with #
				{
					// there should always be a second line
					strLine2 = br.readLine();

					// retrive parts of the line that contain
					// start/stop/timestep info
					// split on white space after normal line 2 info
					String[] sst = strLine2.substring(69).trim().split("\\s+");

					double startmfe = Double.parseDouble(sst[0]);
					double stopmfe = Double.parseDouble(sst[1]);
					double deltamin = Double.parseDouble(sst[2]);

					// call the propagator to get the initial state vector value

					TLElements tle = null;
					TLEPropagator orekitTlePropagator = null;
					try {
						tle = new TLElements("", strLine1, strLine2);
						orekitTlePropagator = TLEPropagator
								.selectExtrapolator(tle);
					} catch (OrekitException e1) {

						e1.printStackTrace();
					}

					out.write(tle.getSatelliteNumber() + " xx\n");
					System.out.println(" " + tle.getSatelliteNumber());

					// modele de propa sgp4 Orekit

					AbsoluteDate orekitJulDate = AbsoluteDate.JULIAN_EPOCH;

					PVCoordinates posVit = null;
					try {
						posVit = orekitTlePropagator
								.getPVCoordinates(orekitJulDate);
					} catch (OrekitException e) {

						e.printStackTrace();
					}

					Vector3D pos = posVit.getPosition();
					Vector3D vit = posVit.getVelocity();

					out.write(String
							.format(" %16.8f %16.8f %16.8f %16.8f %12.9f %12.9f %12.9f\n",
									0, pos.getX(), pos.getY(), pos.getZ(),
									vit.getX(), vit.getY(), vit.getZ()));

					double tsince = startmfe;

					// check so the first value isn't written twice
					if (Math.abs(tsince) > 1.0e-8) {
						tsince = tsince - deltamin;
					}

					// ----------------- loop to perform the propagation
					// ----------------
					while ((tsince < stopmfe)) {
						tsince = tsince + deltamin;

						if (tsince > stopmfe) {
							tsince = stopmfe;
						}

						try {
							posVit = orekitTlePropagator
									.getPVCoordinates(orekitJulDate
											.shiftedBy(tsince * 86400));
						} catch (OrekitException e) {

							e.printStackTrace();
						}

						pos = posVit.getPosition();
						vit = posVit.getVelocity();

					} // while propagating the orbit

				} // ignore lines starting with #
			}
			// Close the input stream
			in.close();
			outStream.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		// -- end main prop loop -------------------------------------------

		System.out.println("======  RUNNNING COMPARISON ====== ");
		// now compare ------------------------------------------------
		try {
			// Open the results files
			FileInputStream fstream = new FileInputStream(javaResults);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader javaResultsBR = new BufferedReader(
					new InputStreamReader(in));

			FileInputStream fstream2 = new FileInputStream(cppResultsFile);
			DataInputStream in2 = new DataInputStream(fstream2);
			BufferedReader cppResultsBR = new BufferedReader(
					new InputStreamReader(in2));

			String cppLine = javaResultsBR.readLine();
			String javaLine = cppResultsBR.readLine();

			int line = 1;
			int lineMismatches = 0;
			do {
				if (!cppLine.equals(javaLine)) {
					lineMismatches++;

					// line not equal
					System.out.print("Line " + line + " doesn't match:  ");

					// figure out how many chars are different
					int charMismatch = 0;
					System.out.print("pos=[");
					try {
						for (int i = 0; i < cppLine.length(); i++) {
							if (cppLine.charAt(i) != javaLine.charAt(i)) {
								charMismatch++;
								System.out.print((i + 1) + " ");
							}
						}

					} catch (Exception e) {
						System.out.print("(Error checking line details) ");
					}
					System.out.print("] ");

					//
					System.out.print(charMismatch + " of " + cppLine.length()
							+ " mismatched characters\n");
				}

				cppLine = javaResultsBR.readLine();
				javaLine = cppResultsBR.readLine();
				line++;
			} while (cppLine != null && javaLine != null);

			System.out.println("---------------------");

			if (cppLine == null && javaLine == null) {
				System.out.println("** Files have the same number of lines **");
			} else {
				System.out
						.println("** Files have DIFFERENT number of lines **");
			}

			System.out.println("Total lines that don't match: "
					+ lineMismatches);
			System.out.println("Total number of lines in shortest file: "
					+ (line - 1));

			in.close();
			in2.close();

		} catch (Exception e) {
			System.out.println("Error in comparing verification results:\n"
					+ e.toString());
		}

	}
}
