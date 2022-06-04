package systemA;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PipedInputStream;

public abstract class Filter extends FilterFramework {
    protected DataInputStream in;
    protected int bytesRead = 0;               // Number of bytes read from the input file.
    protected int bytesWritten = 0;               // Number of bytes written to the stream.
    protected byte dataByte = 0;               // The byte of data read from the stream
    protected byte[] idData = new byte[Integer.BYTES];
    protected byte[] measurementData = new byte[Long.BYTES];
    protected long measurement;                   // This is the word used to store all measurements - conversions are illustrated.
    protected int id;                           // This is the measurement id

    private void readData(int dataLength, PipedInputStream InputReadPort) throws EndOfStreamException, IOException {
        for (int i = 0; i < dataLength; i++) {
            if (in != null) {
                dataByte = in.readByte();
            } else {
                dataByte = ReadFilterInputPort(InputReadPort);        // This is where we read the byte from the stream...
            }

            if (dataLength == Integer.BYTES) {
                idData[i] = dataByte;
                id = id | (dataByte & 0xFF);        // We append the byte on to ID...
            } else {
                measurementData[i] = dataByte;
                if (in == null) {
                    measurement = measurement | (dataByte & 0xFF);
                }
            }

            if (i != dataLength - 1)                // If this is not the last byte, then slide the
            {
                if (dataLength == Integer.BYTES) {
                    id = id << 8;
                } else if (in == null) {
                    measurement = measurement << 8;
                }
            }
            bytesRead++;
        }
    }

    protected void writeData(byte[] data) {
        for (byte datum : data) {
            WriteFilterOutputPort(datum);
            bytesWritten++;
        }
    }

    protected void writeId(byte[] data) {
        id = 0;
        writeData(data);
    }

    protected void writeMeasurement(byte[] data) {
        measurement = 0;
        writeData(data);
    }

    protected void readId(PipedInputStream InputReadPort) throws EndOfStreamException, IOException {
        readData(Integer.BYTES, InputReadPort);
    }

    protected void readMeasurement(PipedInputStream InputReadPort) throws EndOfStreamException, IOException {
        readData(Long.BYTES, InputReadPort);
    }
}

