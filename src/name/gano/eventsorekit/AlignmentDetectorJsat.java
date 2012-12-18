package name.gano.eventsorekit;

import jsattrak.objects.CustomSatellite;

import org.orekit.errors.OrekitException;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.AlignmentDetector;
import org.orekit.utils.PVCoordinatesProvider;

public class AlignmentDetectorJsat extends AlignmentDetector {

	private CustomSatellite satellite = null;

	public AlignmentDetectorJsat(CustomSatellite sat,
			PVCoordinatesProvider body, double alignAngle) {
		super(sat.getInitNode().getOrbitOrekit(), body, alignAngle);
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
		satellite.getEventName().add("Align "+(satellite.getEventName().size()+1));


		return Action.CONTINUE;
	}

}
