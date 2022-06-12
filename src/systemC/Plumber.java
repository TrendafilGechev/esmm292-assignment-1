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
package systemC;

import systemA.FilterFramework;
import systemB.PressureFilter;

public class Plumber {
    public static void main(String[] argv) {
        /****************************************************************************
         * Here we instantiate three filters.
         ****************************************************************************/

        int[] idsToRead = {
                FilterFramework.Ids.Time.ordinal(),
                FilterFramework.Ids.Temperature.ordinal(),
                FilterFramework.Ids.Altitude.ordinal(),
                FilterFramework.Ids.Pressure.ordinal(),
                FilterFramework.Ids.Attitude.ordinal()
        };
        SourceFilterA FilterA = new SourceFilterA();
        SourceFilterB FilterB = new SourceFilterB();
        SortFilter Filter3 = new SortFilter(idsToRead);
        PressureFilter Filter4 = new PressureFilter(45.0d, 90.0d, idsToRead);
        SinkFilter Filter6 = new SinkFilter();

        /****************************************************************************
         * Here we connect the filters starting with the sink filter (Filter 1) which
         * we connect to Filter2 the middle filter. Then we connect Filter2 to the
         * source filter (Filter3).
         ****************************************************************************/

        Filter3.Connect(FilterA, FilterB);
        Filter4.Connect(Filter3);
        Filter6.Connect(Filter4);

        /****************************************************************************
         * Here we start the filters up. All-in-all,... its really kind of boring.
         ****************************************************************************/

        FilterA.start();
        FilterB.start();
        Filter3.start();
        Filter4.start();
        Filter6.start();
    } // main
} // Plumber