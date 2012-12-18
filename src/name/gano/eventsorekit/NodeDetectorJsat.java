package name.gano.eventsorekit;

import jsattrak.objects.CustomSatellite;

import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.NodeDetector;

public class NodeDetectorJsat extends NodeDetector {

	private CustomSatellite satellite = null;

	public NodeDetectorJsat(CustomSatellite sat, Frame frame) {
		super(sat.getInitNode().getOrbitOrekit(), frame);
		this.satellite = sat;
	}

	@Override
	public Action eventOccurred(SpacecraftState s, boolean increasing)
			throws OrekitException {

		satellite.setEventDetected(true);
		double[] LLA = satellite.getLLA();
		satellite.getEventPositions().add(
				new double[] { satellite.getCurrentJulDate(), LLA[0], LLA[1],
						LLA[2]});
		satellite.getEventName().add("Node "+(satellite.getEventName().size()+1));

		return Action.CONTINUE;
	}

}
