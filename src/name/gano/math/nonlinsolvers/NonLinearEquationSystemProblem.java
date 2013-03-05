/*
 * Class meant to be extended for a Nonlinear root finding problem definition
 * =====================================================================
 * Copyright (C) 2009 Shawn E. Gano
 * 
 * This file is part of JSatTrak.
 * 
 * JSatTrak is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JSatTrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with JSatTrak.  If not, see <http://www.gnu.org/licenses/>.
 * =====================================================================
 */

package name.gano.math.nonlinsolvers;

import java.io.IOException;
import java.text.ParseException;

import org.orekit.errors.OrekitException;

/**
 *
 * @author sgano
 */
public interface  NonLinearEquationSystemProblem 
{
       
    // function to be over written
    // returns vector of functions evaluated at x
    public abstract double[] evaluateSystemOfEquations(double[] x) throws IOException, ParseException, OrekitException;
    
}
