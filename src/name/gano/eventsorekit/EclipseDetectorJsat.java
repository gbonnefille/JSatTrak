package name.gano.eventsorekit;

import java.awt.Color;

import jsattrak.objects.CustomSatellite;

import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.EclipseDetector;
import org.orekit.propagation.events.EventDetector.Action;
import org.orekit.utils.Constants;
import org.orekit.utils.PVCoordinatesProvider;

public class EclipseDetectorJsat extends EclipseDetector {

	private CustomSatellite satellite = null;

	public EclipseDetectorJsat( PVCoordinatesProvider occultedBody,double occultedRadius ,
			PVCoordinatesProvider occultingBody,double occultingRadius,CustomSatellite sat,
			boolean totalEclipse) throws OrekitException {
		
		super(occultedBody, occultedRadius, occultingBody, occultingRadius,
				totalEclipse);
		this.satellite = (CustomSatellite) sat;
	}

	@Override
	public Action eventOccurred(SpacecraftState s, boolean increasing)
			throws OrekitException {

		if (increasing) {
			satellite.setSatColor(Color.RED);
		} else {
			satellite.setSatColor(Color.BLUE);
		}

		return Action.CONTINUE;
	}

}
