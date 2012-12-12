package name.gano.eventsorekit;

import java.awt.Color;

import jsattrak.objects.CustomSatellite;

import org.orekit.errors.OrekitException;
import org.orekit.orbits.Orbit;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.ApsideDetector;
import org.orekit.propagation.events.EventDetector.Action;

public class ApsideDetectorJsat extends ApsideDetector {

	private CustomSatellite satellite = null;

	public ApsideDetectorJsat(CustomSatellite sat) {
		super(sat.getInitNode().getOrbitOrekit());
		this.satellite = sat;
	}

	@Override
	public Action eventOccurred(SpacecraftState s, boolean increasing)
			throws OrekitException {
		
		
		//Dessiner une croix!!
		if (increasing) {
			satellite.setSatColor(Color.MAGENTA);
		} else {
			satellite.setSatColor(Color.PINK);
		}

		return Action.CONTINUE;

	}

}
