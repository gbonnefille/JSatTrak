package name.gano.eventsorekit;

import java.awt.Color;

import jsattrak.objects.CustomSatellite;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.CircularFieldOfViewDetector;
import org.orekit.propagation.events.EventDetector.Action;
import org.orekit.utils.PVCoordinatesProvider;

public class CircularFieldOfViewDetectorJsat extends
		CircularFieldOfViewDetector {

	private CustomSatellite satellite = null;

	public CircularFieldOfViewDetectorJsat(CustomSatellite sat,
			double maxCheck, PVCoordinatesProvider pvTarget, Vector3D center,
			double halfAperture) {
		super(maxCheck, pvTarget, center, halfAperture);
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
