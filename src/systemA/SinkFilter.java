/******************************************************************************************************************
 * File:SinkFilter.java
 * Course: 17655
 * Project: Assignment 1
 * Copyright: Copyright (c) 2003 Carnegie Mellon University
 * Versions:
 *	1.0 November 2008 - Sample Pipe and Filter code (ajl).
 *
 * Description:
 *
 * This class serves as an example for using the SinkFilterTemplate for creating a sink filter. This particular
 * filter reads some input from the filter's input port and does the following:
 *
 *	1) It parses the input stream and "decommutates" the measurement ID
 *	2) It parses the input steam for measurments and "decommutates" measurements, storing the bits in a long word.
 *
 * This filter illustrates how to convert the byte stream data from the upstream filterinto useable data found in
 * the stream: namely time (long type) and measurements (double type).
 *
 *
 * Parameters: 	None
 *
 * Internal Methods: None
 *
 ******************************************************************************************************************/
package systemA;

import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;                        // This class is used to interpret time words
import java.text.SimpleDateFormat;        // This class is used to format and write time in a string format.

public class SinkFilter extends Filter {
    @Override public void run() {
        /************************************************************************************
         *	TimeStamp is used to compute time using java.util's Calendar class.
         * 	TimeStampFormat is used to format the time value so that it can be easily printed
         *	to the terminal.
         *************************************************************************************/
        Instant start = Instant.now();
        Calendar TimeStamp = Calendar.getInstance();
        SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy:dd:HH:mm:ss");

        DecimalFormat df = new DecimalFormat("0.00000");
        df.setRoundingMode(RoundingMode.HALF_UP);

        StringBuilder outputLine = new StringBuilder();

        boolean readTemperature = false;
        boolean readAltitude = false;

        /*************************************************************
         *	First we announce to the world that we are alive...
         **************************************************************/

        System.out.print("\n" + this.getName() + "::Sink Reading " + "\n");
        outputLine.append("Time ").append("\t"+"\t"+"\t"+"\t"+"\t"+"\t"+"\t"+"\t").append("Meters: ").append("\t"+"\t"+"\t"+"\t"+"\t").append("Temperature (C): ").append("\t"+"\t"+"\t"+"\t"+"\t");
        outputLine.append("\n");
        while (true) {
            try {
                id = 0;
                measurement = 0;
                readId();
                readMeasurement();

                /****************************************************************************
                 // Here we look for an ID of 0 which indicates this is a time measurement.
                 // Every frame begins with an ID of 0, followed by a time stamp which correlates
                 // to the time that each proceeding measurement was recorded. Time is stored
                 // in milliseconds since Epoch. This allows us to use Java's calendar class to
                 // retrieve time and also use text format classes to format the output into
                 // a form humans can read. So this provides great flexibility in terms of
                 // dealing with time arithmetically or for string display purposes. This is
                 // illustrated below.
                 ****************************************************************************/
                String formattedTime;
                String formattedTemp;
                String formattedAltitude;
                if (id == Ids.Time.ordinal()) {
                    TimeStamp.setTimeInMillis(measurement);
                    formattedTime = TimeStampFormat.format(TimeStamp.getTime());
                    outputLine.append(formattedTime).append("\t"+"\t"+"\t"+"\t"+"\t");
                } // if

                /****************************************************************************
                 // Here we pick up a measurement (ID = 4 in this case), but you can pick up
                 // any measurement you want to. All measurements in the stream are
                 // decommutated by this class. Note that all data measurements are double types
                 // This illustrates how to convert the bits read from the stream into a double
                 // type. Its pretty simple using Double.longBitsToDouble(long value). So here
                 // we print the time stamp and the data associated with the ID we are interested
                 // in.
                 ****************************************************************************/

                else if (id == Ids.Temperature.ordinal()) {
                    double temp = Double.longBitsToDouble(measurement);
                     formattedTemp = df.format(temp);
                    outputLine.append(formattedTemp).append("\t"+"\t"+"\t"+"\t"+"\t");
                     readTemperature = true;
                } // if

                else if (id == Ids.Altitude.ordinal()) {
                        double altitude = Double.longBitsToDouble(measurement);
                        formattedAltitude = df.format(altitude);
                        outputLine.append(formattedAltitude).append("\t"+"\t"+"\t"+"\t"+"\t");
                        readAltitude = true;
                    }

                if (readTemperature && readAltitude) {
                    System.out.println(outputLine);
                    readTemperature = false;
                    readAltitude = false;
                    outputLine.append("\n");
                    writeOutputToFile(outputLine);
                }

            } // try

            /*******************************************************************************
             *	The EndOfStreamException below is thrown when you reach end of the input
             *	stream (duh). At this point, the filter ports are closed and a message is
             *	written letting the user know what is going on.
             ********************************************************************************/
            catch (EndOfStreamException | IOException e) {
                ClosePorts();
                System.out.print("\n" + this.getName() + "::Sink Exiting; bytes read: " + bytesRead + " Duration in milliseconds: " + Duration.between(start, Instant.now()).toMillis() + "\n");
                break;
            } // catch
        } // while
    } // run

    private void writeOutputToFile(StringBuilder outputLine) {
        FileWriter writer = null;
        try {
            writer = new FileWriter("OutputA.txt");
            System.out.print("\n" + this.getName() + "::Sink Writing to file" + "\n" + outputLine.toString());
            writer.write(outputLine.toString());
        } catch (IOException e) {
            System.err.println("IO Error in SinkFilter: " + e.getMessage());
        }
        try {
            if (writer != null) {
                System.out.print("\n" + this.getName() + "::Sink Closing writer" + "\n");
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("IO Error in SinkFilter: " + e.getMessage());
        }
    }
} // SingFilter

