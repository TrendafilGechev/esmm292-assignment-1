package systemB;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Frame {
    byte[] timestampId;
    byte[] timestampValue;
    byte[] tempId;
    byte[] tempValue;
    byte[] altId;
    byte[] altValue;
    byte[] pressureId;
    byte[] attitudeId;

    public byte[] getAttitudeIdBytes() {
        return attitudeId;
    }

    public void setAttitudeIdBytes(byte[] attitudeId) {
        this.attitudeId = attitudeId;
    }

    public byte[] getAttitudeBytes() {
        return attitudeValue;
    }

    public void setAttitudeBytes(byte[] attitudeValue) {
        this.attitudeValue = attitudeValue;
    }

    byte[] attitudeValue;

    public byte[] getTimestampIdBytes() {
        return timestampId;
    }

    public void setTimestampIdBytes(byte[] timestampId) {
        this.timestampId = timestampId;
    }

    public byte[] getTimestampBytes() {
        return timestampValue;
    }

    public long getTimestampInMillis() {
        return bytesToLong(timestampValue);
    }

    public void setTimestampBytes(byte[] timestampValue) {
        this.timestampValue = timestampValue;
    }

    public byte[] getTempIdBytes() {
        return tempId;
    }

    public void setTempIdBytes(byte[] tempId) {
        this.tempId = tempId;
    }

    public byte[] getTempBytes() {
        return tempValue;
    }

    public void setTempBytes(byte[] tempValue) {
        this.tempValue = tempValue;
    }

    public byte[] getAltIdBytes() {
        return altId;
    }

    public void setAltIdBytes(byte[] altId) {
        this.altId = altId;
    }

    public byte[] getAltBytes() {
        return altValue;
    }

    public void setAltBytes(byte[] altValue) {
        this.altValue = altValue;
    }

    public byte[] getPressureIdBytes() {
        return pressureId;
    }

    public void setPressureIdBytes(byte[] pressureId) {
        this.pressureId = pressureId;
    }

    public byte[] getPressureBytes() {
        return pressureValue;
    }

    public void setPressureBytes(byte[] pressureValue) {
        this.pressureValue = pressureValue;
    }

    byte[] pressureValue;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    public long getPressure() {
        return bytesToLong(pressureValue);
    }

    public long getAttitude() {
        return bytesToLong(attitudeValue);
    }

    public long bytesToLong(byte[] bytes) {
        long val = 0;
        for (int i = 0; i < bytes.length; i++) {
            val = val | (bytes[i] & 0xFF);
            if (i != bytes.length - 1) {
                val = val << 8;
            }
        }
        return val;
    }

    public byte[] getOutputArray() throws IOException {
        outputStream.write(timestampId);
        outputStream.write(timestampValue);
        outputStream.write(altId);
        outputStream.write(altValue);
        outputStream.write(pressureId);
        outputStream.write(pressureValue);
        outputStream.write(tempId);
        outputStream.write(tempValue);
        if (attitudeId != null && attitudeId.length > 0) {
            outputStream.write(attitudeId);
            outputStream.write(attitudeValue);
        }
        return outputStream.toByteArray();
    }
}
