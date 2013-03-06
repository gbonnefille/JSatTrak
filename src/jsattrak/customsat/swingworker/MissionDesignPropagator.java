/*
 * Runs Mission Design Propogation in a seperate Thread
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

package jsattrak.customsat.swingworker;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import javax.swing.SwingWorker;

import jsattrak.customsat.InputVariable;
import jsattrak.customsat.SolverNode;
import jsattrak.customsat.StopNode;
import jsattrak.gui.JSatTrak;
import jsattrak.objects.AbstractSatellite;
import name.gano.swingx.treetable.CustomTreeTableNode;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.PVCoordinates;

/**
 * 
 * @author Shawn
 */

public class MissionDesignPropagator extends SwingWorker<Object, Integer> {

	private boolean propMissionTreeStop = false;

	CustomTreeTableNode rootNode;
	DefaultTreeTableModel treeTableModel;
	// private Vector<StateVector> ephemeris;
	private BoundedPropagator ephemeris = null;

	// /** Frame in which are defined the orbital parameters. */
	// private Frame frame;

	AbstractSatellite sat; // sat for this propogation to save data back to

	String currentMessage = "";

	JSatTrak app;

	long totalExeTime = 0;

	int childCount = 0; // count for number of children
	int currentChild = 0;

	boolean debug = false;

	public MissionDesignPropagator(CustomTreeTableNode rootNode,
			DefaultTreeTableModel treeTableModel, AbstractSatellite sat,
			JSatTrak app) {
		// transfer in needed objects to run
		this.rootNode = rootNode;
		this.treeTableModel = treeTableModel;
		// this.frame = sat.getFrame();

		this.sat = sat;
		this.app = app;

	}

	@Override
	protected Object doInBackground() throws Exception {
		System.out.println("-----------------------------");
		System.out.println("Mission propogation initiated.\n");

		propMissionTreeStop = false; // first change state so function dosen't
										// stop imediatly
		countMissionTreeNodes(rootNode);

		// save all variable values
		propMissionTreeStop = false; // first change state so function dosen't
										// stop imediatly
		saveAllVariables(rootNode);

		propMissionTreeStop = false; // first change state so function dosen't
										// stop imediatly
		// ephemeris.clear(); // clears ephemeris before propogating
		ephemeris = null;

		long startTime = System.currentTimeMillis(); // start timer

		// Calcul de propagation
		try {
			propMissionTree(rootNode);
		} catch (OrekitException e) {
			// Recupération de l'erreur et affichage dans le log
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		totalExeTime = System.currentTimeMillis() - startTime;

		// prop done, now save results
		sat.setEphemeris(ephemeris);

		System.out.println("\nMission propogation finished. (" + totalExeTime
				/ 1000.0 + " s)");
		System.out.println("-----------------------------\n");

		return true;
	}

	@Override
	protected void done() {
		// update gui as needed when finished... stop animation reset progress
		// bar etc.

		// stop animation etc
		app.stopStatusAnimation();

		// set progress bar to 0, set invisible
		app.setStatusProgressBarVisible(false);

		// should add message with how long this took to propogate
		app.setStatusMessage(sat.getName()
				+ " - Finished Propogating; CPU time [s]: "
				+ ((totalExeTime) / 1000.0));

		// repaint
		sat.getSatOptions().setGroundTrackIni2False(); // force recalculation of
														// ground tracks
		// On ne prend pas en compte les evenements pour la trace au sol
		app.updateTime(false); // update time of everything and repaint
		// app.forceRepainting();
	}

	// runs every once in a while to update GUI, use publish( int ) and the int
	// will be added to the List
	@Override
	protected void process(List<Integer> chunks) {
		// System.out.println("process: " + chunks);

		// hmm also set progress bar message to name of currently working on
		// segment

		// currentMessage;
		app.setStatusProgressBarValue((int) (chunks.get(chunks.size() - 1) * 100.0 / (childCount)));
		app.setStatusProgressBarText(currentMessage);

	}

	protected void propMissionTree(Object o) throws IOException,
			ParseException, OrekitException, ParseException,
			IllegalArgumentException, IllegalStateException {
		// now just exe children only if a child has children it is responsible
		// for running them
		// sat.getPropNode().setEventDetector(null);
		if (!propMissionTreeStop) {
			for (int i = 0; i < treeTableModel.getChildCount(o); i++) {
				// get child
				CustomTreeTableNode child = (CustomTreeTableNode) treeTableModel
						.getChild(o, i);

				// set message to name
				currentMessage = child.getValueAt(0).toString();

				currentChild++; // increment where we are
				// publish progress
				publish(currentChild);

				// see if this is a stop node - if so end propogation
				if (child instanceof StopNode) {
					// hmm how to break the recursive call
					propMissionTreeStop = true;
					child.execute(this); // execute stop (if some clean up
											// needed)

					// temp print out ephemeris
					if (debug) {
						printEphemeris(); // DEBUG print out ephemeris
					}

					return;
				} // if stop

				child.execute(this); // run the child

			} // for each child
		} // no stop hit

		// removing recursive tree calling
		// if(!propMissionTreeStop)
		// {
		// int cc;
		// cc = treeTableModel.getChildCount(o);
		// for (int i = 0; i < cc; i++)
		// {
		// CustomTreeTableNode child = (CustomTreeTableNode)
		// treeTableModel.getChild(o, i);
		//
		// // set message to name
		// currentMessage = child.getValueAt(0).toString();
		//
		// currentChild++;
		//
		// publish(currentChild);
		//
		// // see if this is a stop node - if so end propogation
		// if (child instanceof StopNode)
		// {
		// // hmm how to break the recursive call
		// propMissionTreeStop = true;
		// child.execute(ephemeris); // execute stop (if some clean up needed)
		//
		// // temp print out ephemeris
		// if(debug)
		// {
		// printEphemeris(); // DEBUG print out ephemeris
		// }
		//
		// return;
		// }
		//
		// if (treeTableModel.isLeaf(child))
		// {
		// //System.out.println(child.getValueAt(0).toString());
		// child.execute(ephemeris);
		//
		// }
		// else
		// {
		// //System.out.println(child.getValueAt(0).toString());
		// child.execute(ephemeris);
		//
		// // now travese its children
		// propMissionTree(child);
		// }
		// } // for each child
		// } // mission stop?

	} // propMissionTree

	private void countMissionTreeNodes(Object o) {

		childCount = rootNode.getChildCount();

		// not useing recursive execution here anymore (solvers will run their
		// own children)

		// if(!propMissionTreeStop)
		// {
		// int cc;
		// cc = treeTableModel.getChildCount(o);
		// for (int i = 0; i < cc; i++)
		// {
		// CustomTreeTableNode child = (CustomTreeTableNode)
		// treeTableModel.getChild(o, i);
		//
		// childCount++;
		//
		// // see if this is a stop node - if so end propogation
		// if (child instanceof StopNode)
		// {
		// // hmm how to break the recursive call
		// propMissionTreeStop = true;
		//
		// return;
		// }
		//
		// if (treeTableModel.isLeaf(child))
		// {
		//
		// }
		// else
		// {
		//
		// // now travese its children
		// propMissionTree(child);
		// }
		// } // for each child
		// } // mission stop?

	} // countMissionTreeNodes

	// goes throug tree recursively and looks for solver nodes and saves all
	// thier variables
	private void saveAllVariables(Object o) {

		if (!propMissionTreeStop) {
			int cc;
			cc = treeTableModel.getChildCount(o);
			for (int i = 0; i < cc; i++) {
				CustomTreeTableNode child = (CustomTreeTableNode) treeTableModel
						.getChild(o, i);

				// see if this is a stop node - if so end propogation
				if (child instanceof StopNode) {
					// hmm how to break the recursive call
					propMissionTreeStop = true;

					return;
				}

				if (treeTableModel.isLeaf(child)) {

				} else {
					if (child instanceof SolverNode) {
						for (InputVariable iv : ((SolverNode) child)
								.getInputVarVec()) {
							iv.saveCurrentToPreviousValue(); // save value
						}
					}

					// now travese its children
					saveAllVariables(child);
				}
			} // for each child
		} // mission stop?

	} // save all vars

	// debug function to print epeheris
	private void printEphemeris() throws OrekitException {
		System.out.println("t, x, y, z, dx, dy, dz");
		System.out.println("===============================");

		double step = sat.getMissionTree().getPropNode().getStepSize();
		Frame frame = sat.getMissionTree().getInitNode().getFrame();
		AbsoluteDate absoluteT = ephemeris.getGeneratedEphemeris().getMinDate();
		PVCoordinates pv = null;
		Vector3D pos = null;
		Vector3D vel = null;
		double t = 0;

		while (absoluteT.compareTo(ephemeris.getGeneratedEphemeris()
				.getMaxDate()) < 0) {

			pv = ephemeris.getPVCoordinates(absoluteT, frame);

			pos = pv.getPosition();
			vel = pv.getVelocity();

			t = absoluteT.getDate().durationFrom(AbsoluteDate.JULIAN_EPOCH) / 86400;

			System.out.println(t + ", " + pos.getX() + ", " + pos.getY() + ", "
					+ pos.getZ() + ", " + vel.getX() + ", " + vel.getY() + ", "
					+ vel.getZ());

			absoluteT = absoluteT.shiftedBy(step);

		}

		System.out.println("===============================");

	} // printEphemeris

	public BoundedPropagator getEphemeris() {
		return ephemeris;
	}

	public void setEphemeris(BoundedPropagator ephemeris) {
		this.ephemeris = ephemeris;
	}

	public AbstractSatellite getSat() {
		return sat;
	}

} // MissionDesignPropagator
