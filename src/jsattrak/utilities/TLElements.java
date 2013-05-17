package jsattrak.utilities;

import org.orekit.errors.OrekitException;
import org.orekit.propagation.analytical.tle.TLE;

public class TLElements extends TLE {

	private static final long serialVersionUID = 225572586110429121L;
	
	private String name = ""; // name line

	public TLElements(String name, String line1, String line2)
			throws OrekitException {
		super(line1, line2);
		this.name = name;
	}

	public String getSatName() {
		return name;
	}

}
