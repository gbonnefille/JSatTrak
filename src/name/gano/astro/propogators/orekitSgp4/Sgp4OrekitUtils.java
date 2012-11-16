package name.gano.astro.propogators.orekitSgp4;


public class Sgp4OrekitUtils {

	
	
	
	public double convertToJd(int year,int month,int day,int hour,int minute,int sec){
		double jd;
        jd = 367.0 * year -
                Math.floor((7 * (year + Math.floor((month + 9) / 12.0))) * 0.25) +
                Math.floor(275 * month / 9.0) +
                day + 1721013.5 +
                ((sec / 60.0 + minute) / 60.0 + hour) / 24.0; 

        return jd;
		
	}
}
