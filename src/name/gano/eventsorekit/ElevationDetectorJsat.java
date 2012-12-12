package name.gano.eventsorekit;

import java.awt.Color;

import jsattrak.objects.CustomSatellite;

import org.orekit.errors.OrekitException;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.ElevationDetector;
import org.orekit.propagation.events.EventDetector.Action;

public class ElevationDetectorJsat extends ElevationDetector {

	private CustomSatellite satellite = null;

	public ElevationDetectorJsat(CustomSatellite sat, double elevation,
			TopocentricFrame topo) {
		super(elevation, topo);
		this.satellite = sat;
	}

	@Override
	public Action eventOccurred(SpacecraftState s, boolean increasing)
			throws OrekitException {

		if (increasing) {
			satellite.setSatColor(Color.BLUE);
		} else {
			satellite.setSatColor(Color.RED);
		}

		return Action.CONTINUE;
	}

}
