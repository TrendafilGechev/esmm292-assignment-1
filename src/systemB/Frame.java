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

    public byte[] getTimestampIdBytes() {
        return timestampId;
    }

    public void setTimestampIdBytes(byte[] timestampId) {
        this.timestampId = timestampId;
    }

    public byte[] getTimestampBytes() {
        return timestampValue;
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
        long pressure = 0;
        for (int i = 0; i < pressureValue.length; i++) {
            pressure = pressure | (pressureValue[i] & 0xFF);
            if (i != pressureValue.length - 1) {
                pressure = pressure << 8;
            }
        }
        return pressure;
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
        return outputStream.toByteArray();
    }
}
