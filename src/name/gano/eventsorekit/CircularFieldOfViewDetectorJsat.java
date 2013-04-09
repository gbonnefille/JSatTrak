package name.gano.eventsorekit;

import jsattrak.objects.AbstractSatellite;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.CircularFieldOfViewDetector;
import org.orekit.utils.PVCoordinatesProvider;

public class CircularFieldOfViewDetectorJsat extends
		CircularFieldOfViewDetector {

	private static final long serialVersionUID = 27381469913207579L;
	
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
			satellite.getSatOptions().setSatColor(satellite.getSatOptions().getTrueSatColor());
			satellite.getSatOptions().setGroundTrackColor(satellite.getSatOptions().getTrueSatColor());

		} else {
			satellite.getSatOptions().setSatColor(satellite.getSatOptions().getEventSatColor());
			satellite.getSatOptions().setGroundTrackColor(satellite.getSatOptions().getEventSatColor());
		}

		return Action.CONTINUE;
	}

}
