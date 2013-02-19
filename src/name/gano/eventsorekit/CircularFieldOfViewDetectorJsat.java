package name.gano.eventsorekit;

import java.awt.Color;

import jsattrak.objects.AbstractSatellite;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.CircularFieldOfViewDetector;
import org.orekit.utils.PVCoordinatesProvider;

public class CircularFieldOfViewDetectorJsat extends
		CircularFieldOfViewDetector {

	private AbstractSatellite satellite = null;

	public CircularFieldOfViewDetectorJsat(AbstractSatellite sat,
			double maxCheck, PVCoordinatesProvider pvTarget, Vector3D center,
			double halfAperture) {
		super(maxCheck, pvTarget, center, halfAperture);
		this.satellite = sat;
	}

	@Override
	public Action eventOccurred(SpacecraftState s, boolean increasing)
			throws OrekitException {

		if (increasing) {
			satellite.getSatOptions().setSatColor(Color.MAGENTA);
		} else {
			satellite.getSatOptions().setSatColor(Color.PINK);
		}

		return Action.CONTINUE;
	}

}
