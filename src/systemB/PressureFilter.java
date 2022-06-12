package systemB;

import systemA.Filter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class PressureFilter extends Filter {

    public PressureFilter(double lowerLimit, double upperLimit, int[] idsToRead) {
        super(idsToRead);
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    double lowerLimit;
    double upperLimit;
    ArrayList<Frame> invalidFrames = new ArrayList<>();
    Frame currentFrame;
    long firstValidPressure = -1;
    long secondValidPressure = -1;


    private void processFrame() throws EndOfStreamException, IOException {
        this.currentFrame = readFrame();
        double pressure = Double.longBitsToDouble(currentFrame.getPressure());
        if (pressure > upperLimit || pressure < lowerLimit) {
            invalidFrames.add(currentFrame);
            processFrame();
        } else if (invalidFrames.size() == 0 || firstValidPressure == -1) {
            firstValidPressure = currentFrame.getPressure();
        } else {
            secondValidPressure = currentFrame.getPressure();
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
        if (secondValidPressure != -1) {
            firstValidPressure = secondValidPressure;
        }
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
