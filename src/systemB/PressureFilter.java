package systemB;

import systemA.Filter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class PressureFilter extends Filter {
    ArrayList<Frame> invalidFrames = new ArrayList<>();
    Frame currentFrame;
    long firstValidPressure = -1;
    long secondValidPressure = -1;

    private void readFrame() throws EndOfStreamException, IOException {
        currentFrame = new Frame();
        for (int i = 0; i < 4; i++) {
            readId();
            readMeasurement();
            if (id == Ids.Time.ordinal()) {
                currentFrame.setTimestampIdBytes(idData.clone());
                currentFrame.setTimestampBytes(measurementData.clone());
            } else if (id == Ids.Altitude.ordinal()) {
                currentFrame.setAltIdBytes(idData.clone());
                currentFrame.setAltBytes(measurementData.clone());
            } else if (id == Ids.Temperature.ordinal()) {
                currentFrame.setTempIdBytes(idData.clone());
                currentFrame.setTempBytes(measurementData.clone());
            } else if (id == Ids.Pressure.ordinal()) {
                currentFrame.setPressureIdBytes(idData.clone());
                currentFrame.setPressureBytes(measurementData.clone());
            }

            // reset id and measurement for next reading
            id = 0;
            measurement = 0;
        }
    }

    private void processFrame() throws EndOfStreamException, IOException {
        readFrame();
        double pressure = Double.longBitsToDouble(currentFrame.getPressure());
        if (pressure > 80.0d || pressure < 50.0d) {
            invalidFrames.add(currentFrame);
            processFrame();
        } else if (invalidFrames.size() == 0 || firstValidPressure == -1) {
            firstValidPressure = currentFrame.getPressure();
        } else {
            secondValidPressure = currentFrame.getPressure();
        }
    }

    private void writeFrame(Frame frame) throws IOException {
        byte[] outPutData = frame.getOutputArray();
        for (byte dataByte : outPutData) {
            WriteFilterOutputPort(dataByte);
        }
    }

    private byte[] getValidPressureBytes() {
        double validPressure;
        if (secondValidPressure == -1) {
            validPressure = Double.longBitsToDouble(firstValidPressure);
        } else {
            double firstPressure = Double.longBitsToDouble(firstValidPressure);
            double secondPressure = Double.longBitsToDouble(secondValidPressure);
            validPressure = (firstPressure + secondPressure) / 2;
        }
        validPressure = -validPressure;
        long validPressureLng = Double.doubleToLongBits(validPressure);
        ByteBuffer pressureBuff = ByteBuffer.allocate(Long.BYTES);
        pressureBuff.putLong(validPressureLng);
        return pressureBuff.array();
    }

    private void resetInvalidFrameState() {
        invalidFrames.clear();
        firstValidPressure = -1;
        secondValidPressure = -1;
    }


    public void run() {
        Instant start = Instant.now();
        System.out.print("\n" + this.getName() + "::Pressure Reading " + "\n");

        while (true) {
            try {
                processFrame();
                if (invalidFrames.size() != 0) {
                    for (Frame corrected : invalidFrames) {
                        corrected.setPressureBytes(getValidPressureBytes());
                        writeFrame(corrected);
                    }
                    resetInvalidFrameState();
                }
                writeFrame(currentFrame);
            } // try

            catch (EndOfStreamException | IOException e) {
                if (invalidFrames.size() != 0) {
                    for (Frame corrected : invalidFrames) {
                        corrected.setPressureBytes(getValidPressureBytes());
                        try {
                            writeFrame(corrected);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
                ClosePorts();
                System.out.print("\n" + this.getName() + "::Pressure Exiting; bytes read: " + bytesRead + " bytes written: " + bytesWritten + " Duration in milliseconds: " + Duration.between(start, Instant.now()).toMillis() + "\n");
                break;
            } // catch
        } // while
    }
}
