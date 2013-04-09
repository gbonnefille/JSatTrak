package name.gano.eventsorekit;

import jsattrak.objects.CustomSatellite;

import org.orekit.errors.OrekitException;
import org.orekit.frames.TopocentricFrame;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.GroundMaskElevationDetector;

public class GroundMaskElevationDetectorJsat extends GroundMaskElevationDetector {

	private static final long serialVersionUID = 87166242430411471L;
	
	private CustomSatellite satellite = null;
	
	public GroundMaskElevationDetectorJsat(CustomSatellite sat,double[][] azimelev,
			TopocentricFrame topo) {
		super(azimelev, topo);
		this.satellite = sat;
	}
	
	@Override
	public Action eventOccurred(SpacecraftState s, boolean increasing)
			throws OrekitException {
		// TODO Auto-generated method stub
		return Action.CONTINUE;
	}

}
