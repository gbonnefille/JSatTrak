package name.gano.eventsorekit;

import java.awt.Color;

import jsattrak.objects.AbstractSatellite;

import org.orekit.errors.OrekitException;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.ApparentElevationDetector;

public class ApparentElevationDetectorJsat extends ApparentElevationDetector {

	private AbstractSatellite satellite = null;

	public ApparentElevationDetectorJsat(AbstractSatellite sat,
			double elevation, TopocentricFrame topo) {
		super(elevation, topo);
		this.satellite = sat;
	}

	@Override
	public Action eventOccurred(SpacecraftState s, boolean increasing)
			throws OrekitException {

		if (increasing) {
			satellite.getSatOptions().setSatColor(Color.BLUE);
		} else {
			satellite.getSatOptions().setSatColor(Color.RED);
		}

		return Action.CONTINUE;
	}

}
