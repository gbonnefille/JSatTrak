package name.gano.eventsorekit;

import jsattrak.objects.AbstractSatellite;

import org.orekit.errors.OrekitException;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.EclipseDetector;
import org.orekit.utils.PVCoordinatesProvider;

public class EclipseDetectorJsat extends EclipseDetector {

	private static final long serialVersionUID = 8540412142549067901L;
	
	private AbstractSatellite satellite = null;

	public EclipseDetectorJsat(PVCoordinatesProvider occultedBody,
			double occultedRadius, PVCoordinatesProvider occultingBody,
			double occultingRadius, AbstractSatellite sat, boolean totalEclipse)
			throws OrekitException {

		super(occultedBody, occultedRadius, occultingBody, occultingRadius,
				totalEclipse);
		this.satellite = sat;
	}

	@Override
	public Action eventOccurred(SpacecraftState s, boolean increasing)
			throws OrekitException {

		if (increasing) {
			satellite.getSatOptions().setSatColor(satellite.getSatOptions().getTrueSatColor());
			satellite.getSatOptions().setGroundTrackColor(satellite.getSatOptions().getTrueSatColor());

		} else {
			satellite.getSatOptions().setSatColor(satellite.getSatOptions().getEventSatColor());
			satellite.getSatOptions().setGroundTrackColor(satellite.getSatOptions().getEventSatColor());
		}

		return Action.CONTINUE;
	}

}
