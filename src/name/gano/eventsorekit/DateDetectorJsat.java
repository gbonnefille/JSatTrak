package name.gano.eventsorekit;

import java.awt.Color;
import java.util.Random;

import jsattrak.objects.CustomSatellite;

import org.orekit.errors.OrekitException;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.DateDetector;
import org.orekit.propagation.events.EventDetector.Action;
import org.orekit.time.AbsoluteDate;

public class DateDetectorJsat extends DateDetector {

	private CustomSatellite satellite = null;

	public DateDetectorJsat(CustomSatellite sat, AbsoluteDate target) {
		super(target);
		this.satellite = sat;
	}

	@Override
	public Action eventOccurred(SpacecraftState s, boolean increasing)
			throws OrekitException {

		if(satellite.getSatColor()==Color.blue){
			satellite.setSatColor(Color.red);
		}else{
			satellite.setSatColor(Color.blue);
		}
			
//			
//			// randomly pick color for satellite
//			// === pick a random color
//			Random generator = new Random();
//			Color satColor;
//			switch (generator.nextInt(6)) {
//			case 0:
//				satColor = Color.red;
//				break;
//			case 1:
//				satColor = Color.blue;
//				break;
//			case 2:
//				satColor = Color.green;
//				break;
//			case 3:
//				satColor = Color.white;
//				break;
//			case 4:
//				satColor = Color.yellow;
//				break;
//			case 5:
//				satColor = Color.orange;
//				break;
//			default:
//				satColor = Color.red;
//				break;
//			} // random color switch



		return Action.CONTINUE;
	}

}
