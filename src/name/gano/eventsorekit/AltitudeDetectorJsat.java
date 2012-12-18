package name.gano.eventsorekit;

import java.awt.Color;

import jsattrak.objects.CustomSatellite;

import org.orekit.bodies.BodyShape;
import org.orekit.errors.OrekitException;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.AltitudeDetector;

public class AltitudeDetectorJsat extends AltitudeDetector {

	private CustomSatellite satellite = null;
	
	public AltitudeDetectorJsat(CustomSatellite sat,double altitude, BodyShape bodyShape) {
		super(altitude, bodyShape);
		this.satellite = sat;
	}
	
	@Override
	public Action eventOccurred(SpacecraftState s, boolean increasing)
			throws OrekitException {

		if (increasing) {
				satellite.setSatColor(Color.RED);
			
		}
		else{
			satellite.setSatColor(Color.BLUE);
		}
		
		
		return Action.CONTINUE;
	}

	
}
