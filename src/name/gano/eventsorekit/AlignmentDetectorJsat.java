package name.gano.eventsorekit;

import java.awt.Color;

import jsattrak.objects.CustomSatellite;

import org.orekit.errors.OrekitException;
import org.orekit.orbits.Orbit;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.AlignmentDetector;
import org.orekit.propagation.events.EventDetector.Action;
import org.orekit.utils.PVCoordinatesProvider;

public class AlignmentDetectorJsat extends AlignmentDetector {

	
	private CustomSatellite satellite = null;
	
	public AlignmentDetectorJsat(CustomSatellite sat, PVCoordinatesProvider body,
			double alignAngle) {
		super(sat.getInitNode().getOrbitOrekit(), body, alignAngle);
		this.satellite = sat;
	}
	
	
	@Override
	public Action eventOccurred(SpacecraftState s, boolean increasing)
			throws OrekitException {

		if (increasing) {
			satellite.setSatColor(Color.MAGENTA);
		}
		else{
			satellite.setSatColor(Color.PINK);
		}
		
		
		
		return Action.CONTINUE;
	}

}
