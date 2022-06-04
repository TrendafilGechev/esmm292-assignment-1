package systemC;

import systemA.Filter;
import systemA.IdData;
import systemA.MeasurementData;
import systemB.Frame;

import java.io.IOException;
import java.io.PipedInputStream;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;

public class SortFilter extends Filter {

    long millisA = 0;
    long millisB = 0;
    boolean hasStreamClosed = false;

    Frame frameA;
    Frame frameB;

    Calendar TimeStamp = Calendar.getInstance();
    SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy:dd:HH:mm:ss:SSS");
    private Frame readFrame(PipedInputStream inputReadPort) throws EndOfStreamException, IOException {
        Frame currentFrame = new Frame();
        for (int i = 0; i < 5; i++) {
            IdData idData = readId(inputReadPort);
            MeasurementData measurementData = readMeasurement(inputReadPort);
            if (idData.id == Ids.Time.ordinal()) {
                currentFrame.setTimestampIdBytes(idData.bytes.clone());
                currentFrame.setTimestampBytes(measurementData.bytes.clone());
            } else if (idData.id == Ids.Altitude.ordinal()) {
                currentFrame.setAltIdBytes(idData.bytes.clone());
                currentFrame.setAltBytes(measurementData.bytes.clone());
            } else if (idData.id == Ids.Temperature.ordinal()) {
                currentFrame.setTempIdBytes(idData.bytes.clone());
                currentFrame.setTempBytes(measurementData.bytes.clone());
            } else if (idData.id == Ids.Pressure.ordinal()) {
                currentFrame.setPressureIdBytes(idData.bytes.clone());
                currentFrame.setPressureBytes(measurementData.bytes.clone());
            } else if (idData.id == Ids.Attitude.ordinal()) {
                currentFrame.setAttitudeIdBytes(idData.bytes.clone());
                currentFrame.setAttitudeBytes(measurementData.bytes.clone());
            }
        }
        return currentFrame;
    }

    public void run() {
        // Next we write a message to the terminal to let the world know we are alive...

        Instant start = Instant.now();
        System.out.print("\n" + this.getName() + "::Sort Reading " + "\n");

        while (true) {
            try {
                if (!hasStreamClosed) {
                    if (millisA == 0) {
                        frameA = this.readFrame(this.InputReadPortA);
                    }
                    if (millisB == 0) {
                        frameB = this.readFrame(this.InputReadPortB);
                    }

                    millisA = frameA.getTimestampInMillis();
                    millisB = frameB.getTimestampInMillis();

                    if (millisA >= millisB) {
                        TimeStamp.setTimeInMillis(millisB);
                        String formattedTime = TimeStampFormat.format(TimeStamp.getTime());
                        System.out.println("Timestamp: " + formattedTime + "\n");
                        millisB = 0;
                    } else {
                        TimeStamp.setTimeInMillis(millisA);
                        String formattedTime = TimeStampFormat.format(TimeStamp.getTime());
                        System.out.println("Timestamp: " + formattedTime + "\n");
                        millisA = 0;
                    }
                } else {
                    if (millisA == 0) { // stream A has finished first just read through rest of stream B
                        frameB = this.readFrame(this.InputReadPortB);
                        millisB = frameB.getTimestampInMillis();
                        TimeStamp.setTimeInMillis(millisB);
                        String formattedTime = TimeStampFormat.format(TimeStamp.getTime());
                        System.out.println("Timestamp: " + formattedTime + "\n");
                    } else { // stream B has finished first, read rest of stream A
                        frameA = this.readFrame(this.InputReadPortA);
                        millisA = frameA.getTimestampInMillis();
                        TimeStamp.setTimeInMillis(millisA);
                        String formattedTime = TimeStampFormat.format(TimeStamp.getTime());
                        System.out.println("Timestamp: " + formattedTime + "\n");
                    }
                }
            } // try

            catch (EndOfStreamException | IOException e) {
                if (!hasStreamClosed) {
                    hasStreamClosed = true;
                    if (millisA == 0) {
                        TimeStamp.setTimeInMillis(millisB);
                        String formattedTime = TimeStampFormat.format(TimeStamp.getTime());
                        System.out.println("Timestamp: " + formattedTime + "\n");
                    } else {
                        TimeStamp.setTimeInMillis(millisA);
                        String formattedTime = TimeStampFormat.format(TimeStamp.getTime());
                        System.out.println("Timestamp: " + formattedTime + "\n");
                    }
                } else {
                    // e.printStackTrace();
                    ClosePorts();
                    System.out.print("\n" + this.getName() + "::Sort Exiting; bytes read: " + bytesRead + " bytes written: " + bytesWritten + " Duration in milliseconds: " + Duration.between(start, Instant.now()).toMillis() + "\n");
                    break;
                }
            } // catch
        } // while
    } // run
}
