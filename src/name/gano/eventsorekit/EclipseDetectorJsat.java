package name.gano.eventsorekit;

import java.awt.Color;

import jsattrak.objects.CustomSatellite;

import org.orekit.errors.OrekitException;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.EclipseDetector;
import org.orekit.propagation.events.EventDetector.Action;
import org.orekit.utils.PVCoordinatesProvider;

public class EclipseDetectorJsat extends EclipseDetector {

	private CustomSatellite satellite = null;

	public EclipseDetectorJsat(CustomSatellite sat, double occultedRadius,
			PVCoordinatesProvider occulting, double occultingRadius,
			boolean totalEclipse) {
		super(sat.getEphemeris(), occultedRadius, occulting, occultingRadius,
				totalEclipse);
		this.satellite = (CustomSatellite) sat;
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
