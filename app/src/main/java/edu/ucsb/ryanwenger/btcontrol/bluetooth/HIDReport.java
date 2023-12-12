package edu.ucsb.ryanwenger.btcontrol.bluetooth;

import java.util.Arrays;

public interface HIDReport {
    byte[] getReport();
    byte getID();

    HIDReport reset();
}

class HIDMouseReport implements HIDReport {
    public static final byte LEFT_BUTTON_MASK = 0b01;
    public static final byte RIGHT_BUTTON_MASK = 0b10;
    public static final byte MIDDLE_BUTTON_MASK = 0b100;
    public byte[] bytes = new byte[4];

    public HIDMouseReport clickLeftButton(boolean down) {
        return clickButton(down, LEFT_BUTTON_MASK);
    }

    public HIDMouseReport clickRightButton(boolean down) {
        return clickButton(down, RIGHT_BUTTON_MASK);
    }

    public HIDMouseReport clickMiddleButton(boolean down) {
        return clickButton(down, MIDDLE_BUTTON_MASK);
    }

    public HIDMouseReport clickButton(boolean down, byte mask) {
        if (down)
            bytes[0] |= mask;
        else
            bytes[0] &= ~mask;
        return this;
    }

    public void toggleButton(byte buttonMask) {
        bytes[0] ^= buttonMask;
    }

    public HIDMouseReport setDx(byte dx) {
        bytes[1] = dx;
        return this;
    }

    public HIDMouseReport setDy(byte dy) {
        bytes[2] = dy;
        return this;
    }

    public HIDMouseReport setScroll(byte dy) {
        bytes[3] = dy;
        return this;
    }

    @Override
    public HIDMouseReport reset() {
        Arrays.fill(bytes, (byte) 0);
        return this;
    }

    @Override
    public byte[] getReport() {
        return bytes;
    }

    @Override
    public byte getID() {
        return 2;
    }
}