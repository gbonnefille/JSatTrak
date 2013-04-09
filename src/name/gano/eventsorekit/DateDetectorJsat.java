package name.gano.eventsorekit;

import jsattrak.objects.AbstractSatellite;

import org.orekit.errors.OrekitException;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.DateDetector;
import org.orekit.time.AbsoluteDate;

public class DateDetectorJsat extends DateDetector {

	private static final long serialVersionUID = -6035451822115113997L;
	
	private AbstractSatellite satellite = null;

	public DateDetectorJsat(AbstractSatellite sat, AbsoluteDate target) {
		super(target);
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
		satellite.getEventName().add("Date point");

		return Action.CONTINUE;
	}

}
