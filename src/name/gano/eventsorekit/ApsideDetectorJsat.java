package name.gano.eventsorekit;

import jsattrak.objects.CustomSatellite;

import org.orekit.errors.OrekitException;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.ApsideDetector;

public class ApsideDetectorJsat extends ApsideDetector {

	private CustomSatellite satellite = null;

	public ApsideDetectorJsat(CustomSatellite sat) {
		super(sat.getInitNode().getOrbitOrekit());
		this.satellite = sat;
	}

	@Override
	public Action eventOccurred(SpacecraftState s, boolean increasing)
			throws OrekitException {

		satellite.setEventDetected(true);
		double[] LLA = satellite.getLLA();
		satellite.getEventPositions().add(
				new double[] { satellite.getCurrentJulDate(), LLA[0], LLA[1],
						LLA[2] });

		return Action.CONTINUE;
	}

}
