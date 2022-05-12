/******************************************************************************************************************
 * File:SourceFilter.java
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
package systemB;

import systemA.Filter;

import java.io.*; // note we must add this here since we use BufferedReader class to read from the keyboard
import java.time.Duration;
import java.time.Instant;

public class SourceFilter extends Filter {
	public void run() {

		String fileName = "../systemA/FlightData.dat";    // Input data file.
		Instant start = Instant.now();
		try {
			/***********************************************************************************
			 *	Here we open the file and write a message to the terminal.
			 ***********************************************************************************/

			in = new DataInputStream(new FileInputStream(fileName));
			System.out.println("\n" + this.getName() + "::Source reading file..." + "\n");

			/***********************************************************************************
			 *	Here we read the data from the file and send it out the filter's output port one
			 * 	byte at a time. The loop stops when it encounters an EOFExecption.
			 ***********************************************************************************/

			while (true) {
				readId();
				readMeasurement();

				if (id != Ids.Velocity.ordinal() && id != Ids.Attitude.ordinal()) {
					writeId(idData);
					writeMeasurement(measurementData);
				} else {
					id = 0;
					measurement = 0;
				}
			} // while

		} //try

		/***********************************************************************************
		 *	The following exception is raised when we hit the end of input file. Once we
		 * 	reach this point, we close the input file, close the filter ports and exit.
		 ***********************************************************************************/ catch (
				EOFException eoferr) {
			System.out.println("\n" + this.getName() + "::End of file reached...");
			try {
				in.close();
				ClosePorts();
				System.out.println("\n" + this.getName() + "::Read file complete, bytes read::" + bytesRead + " bytes written: " + bytesWritten + " Duration in milliseconds: " + Duration.between(start, Instant.now()).toMillis() + "\n");

			}
			/***********************************************************************************
			 *	The following exception is raised should we have a problem closing the file.
			 ***********************************************************************************/ catch (
					Exception closeerr) {
				System.out.println("\n" + this.getName() + "::Problem closing input data file::" + closeerr);

			} // catch

		} // catch

		/***********************************************************************************
		 *	The following exception is raised should we have a problem openinging the file.
		 ***********************************************************************************/ catch (IOException iox) {
			System.out.println("\n" + this.getName() + "::Problem reading input data file::" + iox);

		} // catch
		catch (EndOfStreamException e) {
			throw new RuntimeException(e);
		}

	} // run

} // SourceFilter