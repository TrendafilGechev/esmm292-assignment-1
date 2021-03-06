/******************************************************************************************************************
 * File:Plumber.java
 * Course: 17655
 * Project: Assignment 1
 * Copyright: Copyright (c) 2003 Carnegie Mellon University
 * Versions:
 *	1.0 November 2008 - Sample Pipe and Filter code (ajl).
 *
 * Description:
 *
 * This class serves as an example to illstrate how to use the PlumberTemplate to create a main thread that
 * instantiates and connects a set of filters. This example consists of three filters: a source, a middle filter
 * that acts as a pass-through filter (it does nothing to the data), and a sink filter which illustrates all kinds
 * of useful things that you can do with the input stream of data.
 *
 * Parameters: 		None
 *
 * Internal Methods:	None
 *
 ******************************************************************************************************************/
package systemB;

import systemA.AltitudeFilter;
import systemA.FilterFramework;
import systemA.TemperatureFilter;

public class Plumber {
    public static void main(String[] argv) {
        /****************************************************************************
         * Here we instantiate three filters.
         ****************************************************************************/

        int[] idsToRead = {
                FilterFramework.Ids.Time.ordinal(),
                FilterFramework.Ids.Temperature.ordinal(),
                FilterFramework.Ids.Altitude.ordinal(),
                FilterFramework.Ids.Pressure.ordinal()
        };
        SourceFilter Filter1 = new SourceFilter();
        TemperatureFilter Filter2 = new TemperatureFilter();
        AltitudeFilter Filter3 = new AltitudeFilter();
        PressureFilter Filter4 = new PressureFilter(50.0d, 80.0d, idsToRead);
        SinkFilter Filter5 = new SinkFilter();

        /****************************************************************************
         * Here we connect the filters starting with the sink filter (Filter 1) which
         * we connect to Filter2 the middle filter. Then we connect Filter2 to the
         * source filter (Filter3).
         ****************************************************************************/

        Filter5.Connect(Filter4);
        Filter4.Connect(Filter3);
        Filter3.Connect(Filter2); // This esstially says, "connect Filter3 input port to Filter2 output port
        Filter2.Connect(Filter1); // This esstially says, "connect Filter2 intput port to Filter1 output port

        /****************************************************************************
         * Here we start the filters up. All-in-all,... its really kind of boring.
         ****************************************************************************/

        Filter1.start();
        Filter2.start();
        Filter3.start();
        Filter4.start();
        Filter5.start();

    } // main

} // Plumber