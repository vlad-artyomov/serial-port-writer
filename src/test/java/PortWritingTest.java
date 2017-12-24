import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:artyomov.dev@gmail.com">Vlad Artyomov</a>
 * Date: 30.11.17
 * Time: 13:02
 */
public class PortWritingTest {

    @Test
    public void writeString() throws InterruptedException {
        String[] portNames = SerialPortList.getPortNames("/dev/", Pattern.compile("tnt*"));
        if (portNames.length == 0) {
            System.out.println("No serial ports available");
        } else {
            System.out.println("List of available ports: " + Arrays.stream(portNames).collect(Collectors.joining(", ")));
            SerialPort serialPort = new SerialPort(portNames[1]);
            try {
                serialPort.openPort();
                serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                for (int i = 0; i < 10; i++) {
                    serialPort.writeBytes(("This is a test string " + String.valueOf(i + 1)).getBytes());
                    Thread.sleep(1000);
                }
                serialPort.writeBytes("This is the last string".getBytes());
                serialPort.closePort();
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void writeBytes() throws InterruptedException, IOException {
        String[] portNames = SerialPortList.getPortNames("/dev/", Pattern.compile("tnt*"));
        if (portNames.length == 0) {
            System.out.println("No serial ports available");
        } else {
            System.out.println("List of available ports: " + Arrays.stream(portNames).collect(Collectors.joining(", ")));
            SerialPort serialPort = new SerialPort(portNames[1]);
            try {
                serialPort.openPort();
                serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                List<byte[]> byteMessages = produceByteMessages();
                for (byte[] msg : byteMessages) {
                    serialPort.writeBytes(msg);
                    Thread.sleep(300);
                }
                serialPort.closePort();
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }
    }

    private List<byte[]> produceByteMessages() throws IOException {
        File file = new File(getClass().getResource("dump.txt").getFile());
        List<String> allLines = FileUtils.readLines(file, "UTF-16LE");
        List<String> writtenMessages = new ArrayList<>();
        List<String> readMessages = new ArrayList<>();
        List<byte[]> readMessagesBytes = new ArrayList<>();

        StringBuilder buf = new StringBuilder();
        String block;

        for (String line : allLines) {
            if (line.contains("Written data")) {
                block = buf.toString().trim();
                if (!block.isEmpty()) {
                    readMessages.add(block);
                    readMessagesBytes.add(DatatypeConverter.parseHexBinary(block));
                    buf.setLength(0);
                }
            } else if (line.contains("Read data")) {
                block = buf.toString().trim();
                if (!block.isEmpty()) {
                    writtenMessages.add(block);
                    buf.setLength(0);
                }
            } else {
                buf.append(line.substring(4, 51).trim().replace(" ", ""));
            }
        }

        return readMessagesBytes;
    }
}
