package jsattrak.customsat;

import java.awt.Toolkit;
import java.util.Vector;

import javax.swing.ImageIcon;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.orekit.errors.OrekitException;

import name.gano.astro.time.Time;
import name.gano.swingx.treetable.CustomTreeTableNode;

public class MissionTableModel {

	// table model for the custom config panel and holds all the mission Nodes
		private DefaultTreeTableModel missionTableModel = new DefaultTreeTableModel(); 
		
		String name = "Default name";
		
		private CustomTreeTableNode rootNode = null;

		private InitialConditionsNode initNode = null;

		private PropagatorNode propNode = null;

	public MissionTableModel(Time scenarioEpochDate) throws OrekitException {

		iniMissionTableModel(scenarioEpochDate);
	}
	
	
	private void iniMissionTableModel(Time scenarioEpochDate) throws OrekitException{
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
		rootNode = new CustomTreeTableNode(str);
		rootNode.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource("/icons/custom/sat_icon.png"))));
		missionTableModel.setRoot(rootNode);

		// must add Initial conditions
		// Initial Node
		this.initNode = new InitialConditionsNode(rootNode, scenarioEpochDate);

		// by default also add a propogator node
		// Propogator Node
		this.propNode = new PropagatorNode(rootNode, initNode);

		// must add stop node
		new StopNode(rootNode);
		
		
	}


	public DefaultTreeTableModel getMissionTableModel() {
		return missionTableModel;
	}


	public CustomTreeTableNode getRootNode() {
		return rootNode;
	}


	public InitialConditionsNode getInitNode() {
		return initNode;
	}


	public PropagatorNode getPropNode() {
		return propNode;
	}
	


	
}
