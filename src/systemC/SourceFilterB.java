/******************************************************************************************************************
 * File:SourceFilterB.java
 * Course: 17655
 * Project: Assignment 1
 * Copyright: Copyright (c) 2003 Carnegie Mellon University
 * Versions:
 *	1.0 November 2008 - Sample Pipe and Filter code (ajl).
 *
 * Description:
 *
 * This class serves as an example for how to use the SourceFilterTemplate to create a source filter. This particular
 * filter is a source filter that reads some input from the FlightData.dat file and writes the bytes up stream.
 *
 * Parameters: 		None
 *
 * Internal Methods: None
 *
 ******************************************************************************************************************/
package systemC;

import systemA.Filter;
import systemA.IdData;
import systemA.MeasurementData;

import java.io.*; // note we must add this here since we use BufferedReader class to read from the keyboard
import java.time.Duration;
import java.time.Instant;

public class SourceFilterB extends Filter {
    public void run() {
        String fileName = "src/systemC/SubSetB.dat";    // Input data file.
        Instant start = Instant.now();
        try {
            /***********************************************************************************
             *	Here we open the file and write a message to the terminal.
             ***********************************************************************************/

            in = new DataInputStream(new FileInputStream(fileName));
            System.out.println("\n" + this.getName() + "::Source B reading file..." + "\n");

            /***********************************************************************************
             *	Here we read the data from the file and send it out the filter's output port one
             * 	byte at a time. The loop stops when it encounters an EOFExecption.
             ***********************************************************************************/

            while (true) {
                // input port not actually used since this is source filter
                IdData idData = readId(this.InputReadPortA);
                MeasurementData measurementData = readMeasurement(this.InputReadPortA);

                if (idData.id != Ids.Velocity.ordinal()) {
                    writeId(idData.bytes);
                    writeMeasurement(measurementData.bytes);
                }
            } // while
        } //try
        catch (EOFException eoferr) {
            System.out.println("\n" + this.getName() + "::End of file reached...");
            try {
                in.close();
                ClosePorts();
                System.out.println("\n" + this.getName() + "::Read file complete, bytes read::" + bytesRead + " bytes written: " + bytesWritten + " Duration in milliseconds: " + Duration.between(start, Instant.now()).toMillis() + "\n");
            } catch (Exception closeerr) {
                System.out.println("\n" + this.getName() + "::Problem closing input data file::" + closeerr);
            } // catch
        } // catch
        catch (IOException iox) {
            System.out.println("\n" + this.getName() + "::Problem reading input data file::" + iox);
        } // catch
        catch (EndOfStreamException e) {
            throw new RuntimeException(e);
        }
    } // run
} // SourceFilterB