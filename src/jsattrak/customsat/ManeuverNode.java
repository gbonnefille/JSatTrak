/*
 * Node for Custom Sat Class Mission Designer
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
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;

import jsattrak.customsat.gui.ManeuverPanel;
import jsattrak.customsat.swingworker.MissionDesignPropagator;
import jsattrak.gui.JSatTrak;
import jsattrak.objects.AbstractSatellite;
import name.gano.eventsorekit.ApsideDetectorJsat;
import name.gano.eventsorekit.DateDetectorJsat;
import name.gano.eventsorekit.NodeDetectorJsat;
import name.gano.maneuversorekit.ImpulseManeuverJsat;
import name.gano.swingx.treetable.CustomTreeTableNode;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.orekit.errors.OrekitException;
import org.orekit.forces.maneuvers.ConstantThrustManeuver;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

/**
 * 
 * @author acouanon
 */
public class ManeuverNode extends CustomTreeTableNode {

	private static final long serialVersionUID = -3880091700131050543L;

	private AbstractSatellite currentSat = null;
	// static int to determin which maneuver to use
	public static final int CONSTANTTHRUST = 0;
	public static final int IMPULSE = 1;
	// which maneuver to use
	private int maneuver = ManeuverNode.CONSTANTTHRUST;
	final SimpleDateFormat dateformatShort = new SimpleDateFormat(
			"dd MMM yyyy HH:mm:ss z");
	// Constant thrust maneuver parameters
	GregorianCalendar currentTimeDate = new GregorianCalendar(
			TimeZone.getTimeZone("UTC"));
	private double duration = 0.0;
	private double thrust = 0.0;
	private double constantThrustIsp = 0.0;
	private double impulseIsp = 0.0;
	private Vector3D direction = Vector3D.ZERO;
	// Enum for orekit events
	private ManeuverPanel.Events event = ManeuverPanel.Events.APSIDE;
	private Vector3D deltatV = Vector3D.ZERO;

	public ManeuverNode(CustomTreeTableNode parentNode,
			AbstractSatellite currentSat) {
		super(new String[] { "Maneuver", "", "" }); // initialize
		// node, default values
		// set icon for this type
		setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource("/icons/customSatIcons/burn.png"))));
		// set Node Type
		setNodeType("Maneuver");

		this.currentSat = currentSat;

		// add this node to parent - last thing
		if (parentNode != null) {
			parentNode.add(this);
		}
	}

	// meant to be overridden by implementing classes
	@Override
	public void execute(MissionDesignPropagator missionDesign)
			throws OrekitException {

		switch (this.maneuver) {

		case ManeuverNode.CONSTANTTHRUST:

			AbsoluteDate date = new AbsoluteDate(currentTimeDate.getTime(),
					TimeScalesFactory.getUTC());
			this.currentSat
					.getMissionTree()
					.getPropNode()
					.addConstantThrustManeuver(
							new ConstantThrustManeuver(date, duration, thrust,
									constantThrustIsp, direction));
			break;

		case ManeuverNode.IMPULSE:

			switch (this.event) {

			// Apside detector
			case APSIDE:

				this.currentSat
						.getMissionTree()
						.getPropNode()
						.addImpulseManeuver(
								new ImpulseManeuverJsat(new ApsideDetectorJsat(
										this.currentSat), deltatV, impulseIsp));
				break;

			// Date Detector
			case DATE:
				AbsoluteDate dateTime = new AbsoluteDate(
						currentTimeDate.getTime(), TimeScalesFactory.getUTC());
				this.currentSat
						.getMissionTree()
						.getPropNode()
						.addImpulseManeuver(
								new ImpulseManeuverJsat(new DateDetectorJsat(
										this.currentSat, dateTime), deltatV,
										impulseIsp));
				break;

			// Node detector
			case NODE:

				this.currentSat
						.getMissionTree()
						.getPropNode()
						.addImpulseManeuver(
								new ImpulseManeuverJsat(new NodeDetectorJsat(
										this.currentSat, this.currentSat
												.getMissionTree().getInitNode()
												.getFrame()), deltatV,
										impulseIsp));
				break;

			}

			break;
		}

	}// execute

	// passes in main app to add the internal frame to
	@Override
	public void displaySettings(JSatTrak app) {

		String windowName = "" + getValueAt(0);
		JInternalFrame iframe = new JInternalFrame(windowName, true, true,
				true, true);

		// show satellite browser window
		ManeuverPanel manBrowser = new ManeuverPanel(this, iframe); // non-modal
		// version

		iframe.setContentPane(manBrowser);
		iframe.setSize(315, 375); // w,h
		iframe.setLocation(5, 5);

		app.addInternalFrame(iframe);

	} // displaySettings

	public int getManeuver() {
		return maneuver;
	}

	public void setManeuver(int maneuver) {
		this.maneuver = maneuver;
	}

	public GregorianCalendar getCurrentTimeDate() {
		return currentTimeDate;
	}

	public void setCurrentTimeDate(GregorianCalendar currentTimeDate) {
		this.currentTimeDate = currentTimeDate;
	}

	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	public double getThrust() {
		return thrust;
	}

	public void setThrust(double thrust) {
		this.thrust = thrust;
	}

	public double getConstantThrustIsp() {
		return constantThrustIsp;
	}

	public void setConstantThrustIsp(double isp) {
		this.constantThrustIsp = isp;
	}

	public Vector3D getDirection() {
		return direction;
	}

	public void setDirection(Vector3D direction) {
		this.direction = direction;
	}

	public ManeuverPanel.Events getEvent() {
		return event;
	}

	public void setEvent(ManeuverPanel.Events event) {
		this.event = event;
	}

	public Vector3D getDeltatV() {
		return deltatV;
	}

	public void setDeltatV(Vector3D deltatV) {
		this.deltatV = deltatV;
	}

	public SimpleDateFormat getDateformatShort() {
		return dateformatShort;
	}

	public AbstractSatellite getCurrentSat() {
		return currentSat;
	}

	public double getImpulseIsp() {
		return impulseIsp;
	}

	public void setImpulseIsp(double impulseIsp) {
		this.impulseIsp = impulseIsp;
	}
}
