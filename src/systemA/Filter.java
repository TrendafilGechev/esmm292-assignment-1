package systemA;

import systemB.Frame;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.util.stream.IntStream;

public abstract class Filter extends FilterFramework {
    protected DataInputStream in;
    protected int bytesRead = 0;               // Number of bytes read from the input file.
    protected int bytesWritten = 0;               // Number of bytes written to the stream.
    protected byte dataByte = 0;               // The byte of data read from the stream
    private long measurement;                   // This is the word used to store all measurements - conversions are illustrated.
    private int id;                           // This is the measurement id

    private int[] idsToRead = {};

    public Filter() {}

    public Filter(int[] ids2Read) {
        idsToRead = ids2Read;
    }

    private ReadData readData(int dataLength, PipedInputStream InputReadPort) throws EndOfStreamException, IOException {
        byte[] bytes = new byte[dataLength];
        for (int i = 0; i < dataLength; i++) {
            if (in != null) {
                dataByte = in.readByte();
            } else {
                dataByte = ReadFilterInputPort(InputReadPort);        // This is where we read the byte from the stream...
            }

            if (dataLength == Integer.BYTES) {
                bytes[i] = dataByte;
                id = id | (dataByte & 0xFF);        // We append the byte on to ID...
            } else {
                bytes[i] = dataByte;
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

        ReadData readData;
        if (dataLength == Integer.BYTES) {
            readData = new ReadData(bytes, this.id);
            this.id = 0;
        } else {
            readData = new ReadData(bytes, this.measurement);
            this.measurement = 0;
        }
        return readData;
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

    protected IdData readId(PipedInputStream InputReadPort) throws EndOfStreamException, IOException {
        ReadData readData = readData(Integer.BYTES, InputReadPort);
        return new IdData(readData);
    }

    protected MeasurementData readMeasurement(PipedInputStream InputReadPort) throws EndOfStreamException, IOException {
        ReadData readData = readData(Long.BYTES, InputReadPort);
        return new MeasurementData(readData);
    }

    protected Frame readFrame(PipedInputStream inputReadPort) throws EndOfStreamException, IOException {
        Frame readFrame = new Frame();
        for (int i = 0; i < idsToRead.length; i++) {
            IdData idData = readId(inputReadPort);
            MeasurementData measurementData = readMeasurement(inputReadPort);
            if (IntStream.of(idsToRead).anyMatch(id -> id == idData.id)) {
                if (idData.id == Ids.Time.ordinal()) {
                    readFrame.setTimestampIdBytes(idData.bytes.clone());
                    readFrame.setTimestampBytes(measurementData.bytes.clone());
                } else if (idData.id == Ids.Altitude.ordinal()) {
                    readFrame.setAltIdBytes(idData.bytes.clone());
                    readFrame.setAltBytes(measurementData.bytes.clone());
                } else if (idData.id == Ids.Temperature.ordinal()) {
                    readFrame.setTempIdBytes(idData.bytes.clone());
                    readFrame.setTempBytes(measurementData.bytes.clone());
                } else if (idData.id == Ids.Pressure.ordinal()) {
                    readFrame.setPressureIdBytes(idData.bytes.clone());
                    readFrame.setPressureBytes(measurementData.bytes.clone());
                } else if (idData.id == Ids.Attitude.ordinal()) {
                    readFrame.setAttitudeIdBytes(idData.bytes.clone());
                    readFrame.setAttitudeBytes(measurementData.bytes.clone());
                }
            }
        }
        return readFrame;
    }

    /**
     * Read frame from first input port
     * @return the read Frame
     */
    protected Frame readFrame() throws EndOfStreamException, IOException {
        return this.readFrame(this.InputReadPortA);
    }

    protected void writeFrame(Frame frame) throws IOException {
        byte[] outPutData = frame.getOutputArray();
        for (byte dataByte : outPutData) {
            WriteFilterOutputPort(dataByte);
        }
    }
}

