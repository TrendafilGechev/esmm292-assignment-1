package systemC;

import systemA.Filter;
import systemB.Frame;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class SortFilter extends Filter {

    long millisA = 0;
    long millisB = 0;
    boolean hasStreamClosed = false;

    Frame frameA;
    Frame frameB;

    public SortFilter(int[] ids2Read) {
        super(ids2Read);
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
                        writeFrame(frameB);
                        millisB = 0;
                    } else {
                        writeFrame(frameA);
                        millisA = 0;
                    }
                } else {
                    if (millisA == 0) { // stream A has finished first just read through rest of stream B
                        frameB = this.readFrame(this.InputReadPortB);
                        writeFrame(frameB);
                    } else { // stream B has finished first, read rest of stream A
                        frameA = this.readFrame(this.InputReadPortA);
                        writeFrame(frameA);
                    }
                }
            } // try

            catch (EndOfStreamException | IOException e) {
                if (!hasStreamClosed) {
                    hasStreamClosed = true;
                    if (millisA == 0) {
                        try {
                            writeFrame(frameB);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    } else {
                        try {
                            writeFrame(frameA);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                } else {
                    ClosePorts();
                    System.out.print("\n" + this.getName() + "::Sort Exiting; bytes read: " + bytesRead + " bytes written: " + bytesWritten + " Duration in milliseconds: " + Duration.between(start, Instant.now()).toMillis() + "\n");
                    break;
                }
            } // catch
        } // while
    } // run
}
