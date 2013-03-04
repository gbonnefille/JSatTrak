/*
 * EphemerisFromFileNode.java
 * 
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
 * Created: 10 Jul 2009
 */

package jsattrak.customsat;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;

import jsattrak.customsat.gui.EphemerisFromFilePanel;
import jsattrak.customsat.swingworker.MissionDesignPropagator;
import jsattrak.gui.JSatTrak;
import name.gano.astro.time.Time;
import name.gano.swingx.treetable.CustomTreeTableNode;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.orekit.errors.OrekitException;
import org.orekit.errors.OrekitMessages;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.Transform;
import org.orekit.orbits.CartesianOrbit;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.Ephemeris;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.DateComponents;
import org.orekit.time.TimeComponents;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.PVCoordinates;

/**
 * 
 * @author Shawn E. Gano, shawn@gano.name
 */
public class EphemerisFromFileNode extends CustomTreeTableNode {

	private String filename = "";

	public EphemerisFromFileNode(CustomTreeTableNode parentNode) {
		super(new String[] { "Ephemeris From File", "", "" }); // initialize
																// node, default
																// values

		// set icon for this type
		setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource("/icons/customSatIcons/file.png"))));
		// set Node Type
		setNodeType("Ephemeris From File");

		// add this node to parent - last thing
		if (parentNode != null)
			parentNode.add(this);
	}

	// meant to be overridden by implementing classes
	@Override
	public void execute(MissionDesignPropagator missionDesign) {

		try {
			List<SpacecraftState> spacecraftStatesList = readStkEphemeris(filename);

			// Set the ephemeris
			missionDesign.setEphemeris(new Ephemeris(spacecraftStatesList, 2));

			// set inital time for this node
			AbsoluteDate date = spacecraftStatesList.get(0).getDate();
			this.setStartTTjulDate(date);

			System.out
					.println(" - Node:" + getValueAt(0)
							+ ", Ephemeris Points Read: "
							+ spacecraftStatesList.size());

		} catch (Exception e) {
			System.out.println("Error Reading Ephemeris from File- Node:"
					+ getValueAt(0) + ", " + e.getMessage());
		}

	}// execute

	/**
	 * Reads in an STK .e formated epehermis file NOTE that time is returned in
	 * Terestrial time not UTC!! As epeheris data is typically stored in TT
	 * 
	 * @param filename
	 * @return epehermis vector Julian Data, x,y,z, dx, dy, dz (meters, m/s) -
	 *         can be null if file couldn't be read at all
	 * @throws IOException
	 * @throws Exception
	 *             error in reading file
	 */
	public List<SpacecraftState> readStkEphemeris(String filename)
			throws OrekitException, IOException {
		// clean up data
		String stkVer = "";
		String centralBody = "";
		String coordSys = "";
		String coordSysEpoch = "";
		String scenarioEpoch = "";
		this.filename = filename;

		double mu = Constants.EGM96_EARTH_MU;
		Frame frame = null;
		Frame icrfFrame = null;
		CartesianOrbit cartesianOrbit = null;
		Vector3D pos = Vector3D.ZERO;
		Vector3D vit = Vector3D.ZERO;
		PVCoordinates pvCoordinate = null;
		AbsoluteDate date = null;
		AbsoluteDate jdCoordSysEpoch = null;
		List<SpacecraftState> spacecraftStatesList = new ArrayList<SpacecraftState>();
		SpacecraftState spacecraftState = null;

		try {
			// use buffering, reading one line at a time
			// FileReader always assumes default encoding is OK!
			BufferedReader input = new BufferedReader(new FileReader(filename));
			try {
				String line = null; // not declared within while loop
				/*
				 * readLine is a bit quirky : it returns the content of a line
				 * MINUS the newline. it returns null only for the END of the
				 * stream. it returns an empty String if two newlines appear in
				 * a row.
				 */

				boolean ephemerisBegin = false; // flag for when the ephemeris
												// is about to begin

				while ((line = input.readLine()) != null && !ephemerisBegin) {
					if (line.startsWith("EphemerisTimePosVel")) {
						ephemerisBegin = true;
						break; // need to break out of loop without reading
								// another line in the while statement
					} else if (line.startsWith("stk.v")) {
						stkVer = line.trim();
					} else if (line.startsWith("CentralBody")) {
						centralBody = line.substring(11).trim();
					} else if (line.contains("CoordinateSystem ")) {
						coordSys = line.substring(16).trim();
					} else if (line.contains("CoordinateSystemEpoch")) {
						coordSysEpoch = line.substring(21).trim();
						jdCoordSysEpoch = AbsoluteDate.JULIAN_EPOCH
								.shiftedBy(convertScenarioTimeString2JulianDate(coordSysEpoch
										+ " UTC") * 86400);
					} else if (line.startsWith("ScenarioEpoch")) {
						scenarioEpoch = line.substring(13).trim();
					}

				} // while reading file

				// convert ScenarioEpoch to Julian Date -- if not read in throw
				// an Exception - the method throws exception
				// format: 1 Jul 2007 12:00:00.00 (implied UTC)
				double jdStart = convertScenarioTimeString2JulianDate(scenarioEpoch
						+ " UTC");

				// Define the good frame
				if (coordSys.equals("ICRF") || coordSys.equals("Inertial")) {
					frame = FramesFactory.getICRF();
				} else if (coordSys.equals("J2000")) {
					frame = FramesFactory.getEME2000();
				} else if (coordSys.equals("Fixed")) {
					frame = FramesFactory.getITRF2008();
					icrfFrame = FramesFactory.getICRF();
				} else if (coordSys.equals("TrueOfDate")) {
					frame = FramesFactory.getTOD(true);
				} else if (coordSys.equals("TrueOfEpoch")) {
					frame = FramesFactory.getTOD(true);
					frame.getFrozenFrame(frame, jdCoordSysEpoch, "TrueOfEpoch");
				} else if (coordSys.equals("MeanOfDate")) {
					frame = FramesFactory.getMOD(true);
				} else if (coordSys.equals("MeanOfEpoch")) {
					frame = FramesFactory.getMOD(true);
					frame.getFrozenFrame(frame, jdCoordSysEpoch, "MeanOfEpoch");
				} else if (coordSys.equals("TEMEOfDate")) {
					frame = FramesFactory.getTEME();
				} else if (coordSys.equals("TEMEOfEpoch")) {
					frame = FramesFactory.getTEME();
					frame.getFrozenFrame(frame, jdCoordSysEpoch, "TEMEOfEpoch");
				} else if (coordSys.equals("B1950")) {
					frame = FramesFactory.getMOD(true);
					AbsoluteDate b1950Date = new AbsoluteDate(
							DateComponents.FIFTIES_EPOCH, TimeComponents.H12,
							TimeScalesFactory.getTAI());
					frame.getFrozenFrame(frame, b1950Date, "B1950");
				} else {
					throw new OrekitException(
							OrekitMessages.NON_PSEUDO_INERTIAL_FRAME_NOT_SUITABLE_FOR_DEFINING_ORBITS,
							coordSys);
				}

				// read ephemeris
				while ((line = input.readLine()) != null && ephemerisBegin) {
					if (line.length() < 1) {
						// no data on this line, ignore
					} else if (line.startsWith("END")) {
						ephemerisBegin = false; // no more data
					} else // so far seems ok lets parse the line and see if it
							// is valid
					{
						String[] data = line.split(" +");
						if (data.length == 7) // data line has enough data
												// points
						{
							// convert data to doubles and save to ephem vector
							double[] state = new double[7];

							date = new AbsoluteDate(AbsoluteDate.JULIAN_EPOCH,
									jdStart * 86400
											+ Double.parseDouble(data[0]),
									TimeScalesFactory.getUTC());

							for (int i = 1; i < 7; i++) {
								state[i] = Double.parseDouble(data[i]); // can
																		// throw
																		// exception
							}

							// add state to ephemeris
							pos = new Vector3D(state[1], state[2], state[3]);
							vit = new Vector3D(state[4], state[5], state[6]);

							pvCoordinate = new PVCoordinates(pos, vit);

							// change of reference ITR2008 to EME2000
							if (coordSys.equals("Fixed")) {

								Transform transform = frame.getTransformTo(
										icrfFrame, date);
								pvCoordinate = transform
										.transformPVCoordinates(pvCoordinate);
								cartesianOrbit = new CartesianOrbit(
										pvCoordinate, icrfFrame, date, mu);
							} else {
								cartesianOrbit = new CartesianOrbit(
										pvCoordinate, frame, date, mu);
							}

							spacecraftState = new SpacecraftState(
									cartesianOrbit);

							spacecraftStatesList.add(spacecraftState);

						} // enough points
					}

				} // while reading file

			} finally {
				input.close(); // always close file even if there is an
								// exception
			}
		} catch (IOException ex) {
			throw ex;
		}

		return spacecraftStatesList;

	} // readStkEphemeris

	/**
	 * converts a time string to a Julian Date -- format string need to be
	 * somthing like: dd MMM y H:m:s.S z
	 * 
	 * @param scenarioTimeStr
	 *            string with the date
	 * @return julian date
	 * @throws Exception
	 *             if format is not correct
	 */
	public static double convertScenarioTimeString2JulianDate(
			String scenarioTimeStr) throws OrekitException {
		GregorianCalendar currentTimeDate = new GregorianCalendar(
				TimeZone.getTimeZone("UTC"));

		SimpleDateFormat dateformatShort1 = new SimpleDateFormat(
				"dd MMM y H:m:s.S z", Locale.ENGLISH);
		SimpleDateFormat dateformatShort2 = new SimpleDateFormat(
				"dd MMM y H:m:s z", Locale.ENGLISH); // no Milliseconds

		try {
			currentTimeDate.setTime(dateformatShort1.parse(scenarioTimeStr));
		} catch (Exception e2) {
			try {
				// try reading without the milliseconds
				currentTimeDate
						.setTime(dateformatShort2.parse(scenarioTimeStr));
			} catch (Exception e3) {
				// bad date input
				throw new OrekitException(
						OrekitMessages.SP3_UNSUPPORTED_TIMESYSTEM,
						scenarioTimeStr);
			} // catch 2

		} // catch 1

		// if we get here the date was acapted
		Time t = new Time();
		t.set(currentTimeDate.getTimeInMillis());

		return t.getJulianDate();

	} // convertScenarioTimeString2JulianDate

	// passes in main app to add the internal frame to
	public void displaySettings(JSatTrak app) {

		String windowName = "" + getValueAt(0);
		JInternalFrame iframe = new JInternalFrame(windowName, true, true,
				true, true);

		// show satellite browser window
		EphemerisFromFilePanel gsBrowser = new EphemerisFromFilePanel(this); // non-modal
																				// version
		gsBrowser.setIframe(iframe);

		iframe.setContentPane(gsBrowser);
		iframe.setSize(365, 150); // w,h
		iframe.setLocation(5, 5);

		app.addInternalFrame(iframe);
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename
	 *            the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

}
