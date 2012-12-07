package name.gano.eventsorekit;

import java.awt.Color;

import jsattrak.objects.CustomSatellite;

import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.orbits.Orbit;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.NodeDetector;
import org.orekit.propagation.events.EventDetector.Action;

public class NodeDetectorJsat extends NodeDetector {

	private CustomSatellite satellite = null;

	public NodeDetectorJsat(CustomSatellite sat, Frame frame) {
		super(sat.getInitNode().getOrbitOrekit(), frame);
		this.satellite = sat;
	}

	@Override
	public Action eventOccurred(SpacecraftState s, boolean increasing)
			throws OrekitException {

		if (increasing) {
			satellite.setSatColor(Color.MAGENTA);
		} else {
			satellite.setSatColor(Color.PINK);
		}

		return Action.CONTINUE;
	}

}
