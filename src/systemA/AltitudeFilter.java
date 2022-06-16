package systemA;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;

public class AltitudeFilter extends Filter {
    public void run() {
        // Next we write a message to the terminal to let the world know we are alive...
        Instant start = Instant.now();
        System.out.print("\n" + this.getName() + "::Altitude Reading " + "\n");

        while (true) {
            try {
                IdData idData = readId(this.InputReadPortA);
                MeasurementData measurementData = readMeasurement(this.InputReadPortA);

                if (idData.id == Ids.Altitude.ordinal()) {
                    double feetAlt = Double.longBitsToDouble(measurementData.measurement);
                    double meters = feetAlt * 0.3048;
                    measurementData.measurement = Double.doubleToLongBits(meters);
                    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                    buffer.putLong(measurementData.measurement);
                    measurementData.bytes = buffer.array();
                } // if
                writeId(idData.bytes);
                writeMeasurement(measurementData.bytes);
            } // try
            catch (EndOfStreamException | IOException e) {
                ClosePorts();
                System.out.print("\n" + this.getName() + "::Altitude Exiting; bytes read: " + bytesRead + " bytes written: " + bytesWritten + " Duration in milliseconds: " + Duration.between(start, Instant.now()).toMillis() + "\n");
                break;
            } // catch
        } // while
    } // run
}
