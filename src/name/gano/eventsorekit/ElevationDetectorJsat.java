package name.gano.eventsorekit;

import jsattrak.objects.AbstractSatellite;

import org.orekit.errors.OrekitException;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.ElevationDetector;

public class ElevationDetectorJsat extends ElevationDetector {

	private static final long serialVersionUID = 5775925259423055971L;
	
	private AbstractSatellite satellite = null;

	public ElevationDetectorJsat(AbstractSatellite sat, double elevation,
			TopocentricFrame topo) {
		super(elevation, topo);
		this.satellite = sat;
	}

	@Override
	public Action eventOccurred(SpacecraftState s, boolean increasing)
			throws OrekitException {

		if (increasing) {
			satellite.getSatOptions().setSatColor(
					satellite.getSatOptions().getEventSatColor());
			satellite.getSatOptions().setGroundTrackColor(
					satellite.getSatOptions().getEventSatColor());

		} else {
			satellite.getSatOptions().setSatColor(
					satellite.getSatOptions().getTrueSatColor());
			satellite.getSatOptions().setGroundTrackColor(
					satellite.getSatOptions().getTrueSatColor());
		}

		return Action.CONTINUE;
	}

}
