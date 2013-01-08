package name.gano.eventsorekit;

import java.awt.Color;

import jsattrak.objects.AbstractSatellite;

import org.orekit.bodies.BodyShape;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.AltitudeDetector;

public class AltitudeDetectorJsat extends AltitudeDetector {

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
			satellite.setSatColor(Color.RED);

		} else {
			satellite.setSatColor(Color.BLUE);
		}

		return Action.CONTINUE;
	}

}
