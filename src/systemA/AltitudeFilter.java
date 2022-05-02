package systemA;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;

public class AltitudeFilter extends Filter {
    public void run()
    {
        // Next we write a message to the terminal to let the world know we are alive...

        Instant start = Instant.now();
        System.out.print( "\n" + this.getName() + "::Altitude Reading " + "\n");

        while (true)
        {
            try
            {
                readId();
                readMeasurement();

                byte[] mData = measurementData;
                if ( id == Ids.Altitude.ordinal())
                {
                    double feetAlt = Double.longBitsToDouble(measurement);
                    double meters = feetAlt * 0.3048;
                    measurement = Double.doubleToLongBits(meters);
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
                System.out.print("\n" + this.getName() + "::Altitude Exiting; bytes read: " + bytesRead + " bytes written: " + bytesWritten + " Duration in milliseconds: " + Duration.between(start, Instant.now()).toMillis() + "\n");
                break;

            } // catch

        } // while

    } // run
}
