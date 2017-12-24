import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:artyomov.dev@gmail.com">Vlad Artyomov</a>
 * Date: 28.11.17
 * Time: 17:58
 */
public class App {

    public static void main(String[] args) {
        // windows only
        // String[] portNames = SerialPortList.getPortNames();
        // linux & mac
        String[] portNames = SerialPortList.getPortNames("/dev/", Pattern.compile("tnt*"));

        if (portNames.length == 0) {
            System.out.println("No serial ports available");
        } else {
            System.out.println("List of available ports: " + Arrays.stream(portNames).collect(Collectors.joining(", ")));
            SerialPort serialPort = new SerialPort(portNames[0]);
            try {
                serialPort.openPort();
                serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                serialPort.addEventListener(new PortListener(serialPort));
//                serialPort.closePort();
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }
    }

    static class PortListener implements SerialPortEventListener {

        SerialPort serialPort;

        public PortListener(SerialPort serialPort) {
            this.serialPort = serialPort;
        }

        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {
            try {
                String receivedData = serialPort.readHexString(serialPortEvent.getEventValue());
                System.out.println("Received response: " + receivedData);
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }
    }
}
