package name.gano.maneuversorekit;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.orekit.errors.OrekitException;
import org.orekit.forces.maneuvers.ImpulseManeuver;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.EventDetector;

/**
 * 
 * @author acouanon
 */

public class ImpulseManeuverJsat extends ImpulseManeuver {

	private static final long serialVersionUID = 7068838238985276266L;

	public ImpulseManeuverJsat(EventDetector trigger, Vector3D deltaVSat,
			double isp) {
		super(trigger, deltaVSat, isp);
	}

	@Override
	public Action eventOccurred(SpacecraftState s, boolean increasing)
			throws OrekitException {
		return Action.RESET_STATE;
	}

}
