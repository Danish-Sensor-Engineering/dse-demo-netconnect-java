package dse.cli.connect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectClient implements Runnable {

    private final static long SECOND_IN_NANOS = 1_000_000_000;

    private long lastTimestamp;
    private boolean keepRunning = true;

    private byte[] readBuffer = new byte[2048];
    private final Socket socket;
    private final OutputStream output;
    private final InputStream input;

    private final AtomicInteger distance = new AtomicInteger();
    private final AtomicInteger profiles = new AtomicInteger();


    public ConnectClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        output = socket.getOutputStream();
        input = socket.getInputStream();
    }


    @Override
    public void run() {
        try {
            readBuffer = new byte[2048]; // Large buffer to autosize down
            output.write("start".getBytes());
            output.write(System.lineSeparator().getBytes());
            output.flush();
            read();
        } catch (Exception e) {
            System.err.printf("run() %s%n", e);
        }
    }


    public void read() throws IOException {
        while(keepRunning && input.read(readBuffer) > 1) {
            parse(readBuffer);
        }
    }


    public void stop() throws IOException {
        keepRunning = false;
        output.write("stop".getBytes());
        output.write(System.lineSeparator().getBytes());
        output.write("exit".getBytes());
        output.write(System.lineSeparator().getBytes());
        output.flush();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
        socket.close();
    }



    private void parse(byte[] input) {

        // Measurement Datagram
        //
        // <---------------------- HEADER 22 bytes -------------------->     <----- DATA ----->
        //    _short   _short   _short    _int       _int      _long
        //   2-bytes  2-bytes  2-bytes  4-bytes   4-bytes    8-bytes
        //        ID  VERSION     TYPE   LENGTH  SEQUENCE  TIMESTAMP
        //
        // - ID: Unique fingerprint for this kind of binary payload
        // - VERSION: To accommodate future changes
        // - TYPE: Error, Distance Sensor or Profile Scanner ( 400, 800, 1600 )
        // - LENGTH: Size of datagram including header of 22 bytes
        // - SEQUENCE: Counter that wraps at MAX and starts over
        // - TIMESTAMP: Time in milliseconds (Unix Epoch) when measurement was received from device
        // - DATA: Depends on TYPE
        //   - int (4-bytes) when ERROR and DISTANCE
        //   - list (no. points in sweep) of 2 x double (8-bytes) for X & Y coordinates when PROFILE

        if(input.length < 22) {
            System.err.printf("parse() - invalid size of payload: %d%n", input.length);
            return;
        }

        ByteBuffer buffer = ByteBuffer.wrap(input);
        short _magic = buffer.getShort();
        if(_magic != 0x1b1e) {
            System.err.printf("parse() - invalid magic-id of payload: %s%n", Arrays.toString(buffer.array()));
            return;
        }

        // Parse header bytes
        short _version = buffer.getShort();
        short _type = buffer.getShort();
        int _length = buffer.getInt();
        int _sequence = buffer.getInt();
        long _timestamp = buffer.getLong();

        long elapsedNanos = System.nanoTime() - lastTimestamp;
        if(elapsedNanos > SECOND_IN_NANOS) {
            if(readBuffer.length != _length) {
                readBuffer = new byte[_length]; // Auto-size read-buffer
            }
            lastTimestamp = System.nanoTime();
            System.err.printf("id: %d, version: %d, type: %d, length: %d, sequence: %d, timestamp: %s%n", _magic, _version, _type, _length, _sequence, Instant.ofEpochMilli(_timestamp));
        }


        // Based on type, get profile, distance or error
        switch (_type) {
            case 1:                 // Error
                int error = buffer.getInt();
                System.err.printf("sensor error: %s%n", SensorError.getError(error));
                return;
            case 11:                // Distance
                distance.set(buffer.getInt());
                System.out.printf("sensor distance: %d%n", distance.get());
                break;
            case 21, 22, 23, 24:    // Profile
                int count = 0;
                while(buffer.position() <= buffer.limit() - 16) {    // 2 x double == 16
                    double x = buffer.getDouble();
                    double y = buffer.getDouble();
                    count++;
                }
                profiles.set(count);
                System.out.printf("sensor profiles: %d%n", profiles.get());
                break;
        }

    }


    public enum SensorError {

        UNKNOWN(99, "An unknown errorCode occurred."),
        LIGHT_6(6, "Too little light returned or there is no target at all."),
        LIGHT_5(5, "Too much light returned/blinding or false light."),
        LIGHT_4(4, "False light or an undefined spot recorded."),
        TARGET_2(2, "A target is observed but outside the measuring range."),
        TARGET_1(1, "A target is observed but outside the measuring range."),
        TARGET_0(0, "A target is observed but outside the measuring range.");

        private final int id;
        private final String message;

        SensorError(int id, String message) {
            this.id = id;
            this.message = message;
        }

        public int getId() { return id; }

        public String getMessage() { return message; }


        public static String getError(int errorCode) {

            SensorError error = switch (errorCode) {
                case 0 -> SensorError.TARGET_0;
                case 1 -> SensorError.TARGET_1;
                case 2 -> SensorError.TARGET_2;
                case 4 -> SensorError.LIGHT_4;
                case 5 -> SensorError.LIGHT_5;
                case 6 -> SensorError.LIGHT_6;
                default -> SensorError.UNKNOWN;
            };

            return error.getMessage();
        }
    }
}
