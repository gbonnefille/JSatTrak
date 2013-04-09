package name.gano.eventsorekit;

import jsattrak.objects.AbstractSatellite;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.DihedralFieldOfViewDetector;
import org.orekit.utils.PVCoordinatesProvider;

public class DihedralFieldOfViewDetectorJsat extends
		DihedralFieldOfViewDetector {

	private static final long serialVersionUID = 410407143823094136L;
	
	private AbstractSatellite satellite = null;

	public DihedralFieldOfViewDetectorJsat(AbstractSatellite sat,
			double maxCheck, PVCoordinatesProvider pvTarget, Vector3D center,
			Vector3D axis1, double halfAperture1, Vector3D axis2,
			double halfAperture2) {
		super(maxCheck, pvTarget, center, axis1, halfAperture1, axis2,
				halfAperture2);
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
