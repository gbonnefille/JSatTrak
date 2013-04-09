package name.gano.eventsorekit;

import jsattrak.objects.AbstractSatellite;

import org.orekit.bodies.BodyShape;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.AltitudeDetector;

public class AltitudeDetectorJsat extends AltitudeDetector {

	private static final long serialVersionUID = -8699078633593512720L;
	
	private AbstractSatellite satellite = null;

	public AltitudeDetectorJsat(AbstractSatellite sat, double altitude,
			BodyShape bodyShape) {
		super(altitude, bodyShape);
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
