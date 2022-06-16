package systemC;

import systemA.Filter;
import systemB.Frame;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class AttitudePressureFilter extends Filter {
    public AttitudePressureFilter(double attitudeLimit, double pressureLimit, int[] idsToRead) {
        super(idsToRead);
        this.attitudeLimit = attitudeLimit;
        this.pressureLimit = pressureLimit;
    }

    double pressureLimit;
    double attitudeLimit;

    ArrayList<Frame> invalidFrames = new ArrayList<>();
    Frame currentFrame;
    long firstValidPressure = -1;
    long secondValidPressure = -1;

    long firstValidAttitude = -1;
    long secondValidAttitude = -1;

    private void processFrame() throws EndOfStreamException, IOException {
        this.currentFrame = readFrame();
        double pressure = Double.longBitsToDouble(currentFrame.getPressure());
        pressure = Math.abs(pressure);
        double attitude = Double.longBitsToDouble(currentFrame.getAttitude());
        if (pressure > pressureLimit && attitude > attitudeLimit) {
            invalidFrames.add(currentFrame);
            processFrame();
        } else if (invalidFrames.size() == 0 || firstValidPressure == -1) {
            firstValidPressure = currentFrame.getPressure();
            firstValidAttitude = currentFrame.getAttitude();
        } else {
            secondValidPressure = currentFrame.getPressure();
            secondValidAttitude = currentFrame.getAttitude();
        }
    }

    private byte[] getValidMeasurementBytes(long firstValue, long secondValue) {
        double validMeasurement;
        if (secondValue == -1) {
            validMeasurement = Double.longBitsToDouble(firstValue);
        } else {
            double firstMeasurement = Double.longBitsToDouble(firstValue);
            double secondMeasurement = Double.longBitsToDouble(secondValue);
            validMeasurement = (firstMeasurement + secondMeasurement) / 2;
        }
        validMeasurement = -validMeasurement;
        long validMeasurementLng = Double.doubleToLongBits(validMeasurement);
        ByteBuffer pressureBuff = ByteBuffer.allocate(Long.BYTES);
        pressureBuff.putLong(validMeasurementLng);
        return pressureBuff.array();
    }

    private void resetInvalidFrameState() {
        invalidFrames.clear();
        if (secondValidPressure != -1) {
            firstValidPressure = secondValidPressure;
            firstValidAttitude = secondValidAttitude;
        }
        secondValidPressure = -1;
        secondValidAttitude = -1;
    }

    public void run() {
        Instant start = Instant.now();
        System.out.print("\n" + this.getName() + "::Pressure Reading " + "\n");

        while (true) {
            try {
                processFrame();
                if (invalidFrames.size() != 0) {
                    for (Frame corrected : invalidFrames) {
                        corrected.setPressureBytes(getValidMeasurementBytes(firstValidPressure, secondValidPressure));
                        corrected.setAttitudeBytes(getValidMeasurementBytes(firstValidAttitude, secondValidAttitude));
                        writeFrame(corrected);
                    }
                    resetInvalidFrameState();
                }
                writeFrame(currentFrame);
            } // try

            catch (EndOfStreamException | IOException e) {
                if (invalidFrames.size() != 0) {
                    for (Frame corrected : invalidFrames) {
                        corrected.setPressureBytes(getValidMeasurementBytes(firstValidPressure, secondValidPressure));
                        corrected.setAttitudeBytes(getValidMeasurementBytes(firstValidAttitude, secondValidAttitude));
                        try {
                            writeFrame(corrected);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
                ClosePorts();
                System.out.print("\n" + this.getName() + "::AttitudePressure Exiting; bytes read: " + bytesRead + " bytes written: " + bytesWritten + " Duration in milliseconds: " + Duration.between(start, Instant.now()).toMillis() + "\n");
                break;
            } // catch
        } // while
    }
}
