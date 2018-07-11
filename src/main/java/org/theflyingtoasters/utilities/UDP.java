package org.theflyingtoasters.utilities;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDP {
    static final int SCALE_PORT = 5801;
    static Thread scaleListenerThread;
    static UDP scaleListener;
    static Double scaleAngle = 0.0;

    static int port;
    static InetAddress address;
    static DatagramSocket socket;
    static DatagramPacket packet;
    static byte[] buf;

    /**
     * Create a new UDP connection.
     *
     * @param IP   The IP of the connection.
     * @param Port The port to use.
     */
    public UDP(String IP, int Port) {
        try {
            port = Port;
            address = InetAddress.getByName(IP);
            socket = new DatagramSocket(Port);
            buf = new byte[256];
            socket.setSoTimeout(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close(){
        socket.close();
    }

    /**
     * Get the most recent message.
     *
     * @param oldResponse The last medouble getScaleAnglessage it got.
     * @return The most recent message.
     */
    public String flush(String oldResponse) {
        String response = getData();
        if (response != null) {
            return flush(response);
        } else return oldResponse;
    }

    /**
     * Get the most recent message.
     *
     * @return The most recent message.
     */
    public String flush() {
        return (flush(null));
    }

    /**
     * Sends a string over the UDP Connection.
     *
     * @param data The string to send.
     */
    public void sendData(String data) //Sends the request to the pi
    {
        try {

            buf = data.getBytes();    //Converts the String to a byte array
            packet = new DatagramPacket(buf, buf.length, address, port); //Makes a packet from the byte array, address, and port
            socket.send(packet);    //Send the packet :D
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    /**
     * Get a message from the connection.
     *
     * @return The message. Null if there is an error, or if there is none.
     */
    public String getData() {
        try {
            byte[] buf = null;
            buf = new byte[256]; //Creates the byte array for the response
            packet = new DatagramPacket(buf, buf.length); //Prepares to receive the packet
            socket.receive(packet);
            String response = new String(buf, 0, packet.getLength()); //Converts the byte array to a string
            return response;
        } catch (Exception e) {
            Logging.m("An Error occurred reading UDP data");
            return null;
        }
    }

    public static void startScaleListener() {
        //String driverStationIP = SmartDashboard.getString("DS IP", "10.36.41.43");
        if(scaleListener != null)
            scaleListener.close();
        scaleListener = new UDP("", SCALE_PORT);
        if(scaleListenerThread != null)
            scaleListenerThread.interrupt();

        scaleListenerThread = new Thread(() -> {
            while(true) {
                try {
                    synchronized (UDP.class) {
                        scaleAngle = readScaleAngleBlocking();
                    }
                } catch(Exception e){
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
    }

    public static double getScaleAngle(int advance) {
        return scaleAngle;
    }

    public static double readScaleAngleBlocking() {
        try {
            String data = scaleListener.getData();
            //if(data == null) Logging.h("Data null");
            if (data.length() < 1)
                return 0;
            else {
                String[] numbers = data.split(",");
                return Double.parseDouble(numbers[1]);
            }
        } catch (Exception e) {
            //Logging.e(e.getStackTrace());
            return 0;
        }
    }

}