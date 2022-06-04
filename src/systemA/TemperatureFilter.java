package systemA;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;

/******************************************************************************************************************
* File:MiddleFilter.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*
* Description:
*
* This class serves as an example for how to use the FilterRemplate to create a standard filter. This particular
* example is a simple "pass-through" filter that reads data from the filter's input port and writes data out the
* filter's output port.
*
* Parameters: 		None
*
* Internal Methods: None
*
******************************************************************************************************************/

public class TemperatureFilter extends Filter
{
	public void run()
    {
		// Next we write a message to the terminal to let the world know we are alive...

		Instant start = Instant.now();
		System.out.print( "\n" + this.getName() + "::Temperature Reading " + "\n");

		while (true)
		{
			try
			{
				readId();
				readMeasurement();

				byte[] mData = measurementData;
				if ( id == Ids.Temperature.ordinal())
				{
					double tempF = Double.longBitsToDouble(measurement);
					double tempC = (tempF - 32) * 5 / 9;
					measurement = Double.doubleToLongBits(tempC);
					ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
					buffer.putLong(measurement);
					mData = buffer.array();
				} // if

				writeId(idData);
				writeMeasurement(mData);
			} // try

			catch (EndOfStreamException | IOException e)
			{
				ClosePorts();
				System.out.print("\n" + this.getName() + "::Temperature Exiting; bytes read: " + bytesRead + " bytes written: " + bytesWritten + " Duration in milliseconds: " + Duration.between(start, Instant.now()).toMillis() + "\n");
				break;

			} // catch

		} // while

   } // run

} // MiddleFilter