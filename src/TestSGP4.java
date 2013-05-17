// Test use of SGP4 objects -- requested by:
//Bradley Crosby Jr.
//SPAWAR Atlantic 5.3.3.4
//Office: 843-218-4343
//Mobile: 843-822-5177
//bradley.crosby@navy.mil
//crosbyb@spawar-chas.navy.smil.mil


import java.io.File;

import jsattrak.customsat.SatOption;
import jsattrak.objects.CustomSatellite;
import jsattrak.utilities.TLElements;

import org.orekit.data.DataProvidersManager;
import org.orekit.data.ZipJarCrawler;
import org.orekit.errors.OrekitException;


/**
 *
 * @author Shawn Gano, 9 June 2009
 */
public class TestSGP4
{
    public static void main(String[] args) 
    {
        
    	   // create TLE object
        // TLE = name, line 1, line 2
    	TLElements newTLE = null;
    	
    	//Initialisation d'OREKIT
		try {

			// change this if your orekit data is not located in your home directory
            File zipFile = new File("data/orekit-data.zip");
            if (zipFile.exists()) {
                DataProvidersManager.getInstance().addProvider(new ZipJarCrawler(zipFile));
            } else {
                System.err.println(zipFile.getAbsolutePath() +
                        " zip file found, aborting");
                System.exit(1);
            }
				
			newTLE = new TLElements("ISS","1 25544U 98067A   09160.12255947  .00017740  00000-0  12823-3 0    24","2 25544  51.6405 348.2892 0009223  92.2562   9.3141 15.73542580604683");
		} catch (OrekitException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        // Julian Date we are interested in
        double julianDate = 2454992.0; // 09 Jun 2009 12:00:00.000 UTC

        // Create SGP4 satelite propogator
        CustomSatellite prop = null;
        try
        {
            prop = new CustomSatellite(newTLE.getSatName(), newTLE.getLine1(), newTLE.getLine2(),new SatOption());
            prop.setShowGroundTrack(false); // if we arn't using the JSatTrak plots midas well turn this off to save CPU time
        }
        catch(Exception e)
        {
            System.out.println("Error Creating SGP4 Satellite");
            System.exit(1);
        }

        // prop to the desired time
        try {
			prop.propogate2JulDate(julianDate,true);
		} catch (OrekitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // get the lat/long/altitude [radians, radians, meters]
        double[] lla = prop.getLLA();

        System.out.println("Lat [deg]:" + lla[0]*180.0/Math.PI);
        System.out.println("Lon [deg]:" + lla[1]*180.0/Math.PI);
        System.out.println("Alt [m]  :" + lla[2]); 

    } // main
}
