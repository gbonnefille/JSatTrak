package name.gano.eventsorekit;

import java.awt.Color;

import jsattrak.objects.CustomSatellite;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.DihedralFieldOfViewDetector;
import org.orekit.propagation.events.EventDetector.Action;
import org.orekit.utils.PVCoordinatesProvider;

public class DihedralFieldOfViewDetectorJsat extends
		DihedralFieldOfViewDetector {

	private CustomSatellite satellite = null;

	public DihedralFieldOfViewDetectorJsat(CustomSatellite sat,
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
			satellite.setSatColor(Color.MAGENTA);
		} else {
			satellite.setSatColor(Color.PINK);
		}

		return Action.CONTINUE;
	}

}
