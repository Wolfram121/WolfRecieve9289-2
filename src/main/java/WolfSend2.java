import java.util.Random;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

/**
 * This class simulates telemetry data being sent to NetworkTables
 * for testing or visualization purposes. It runs in a background
 * thread and sends randomized values to mimic robot telemetry.
 */
public class WolfSend2 {

    // Random number generator for simulating sensor data
    Random rand = new Random();

    // Initial simulated robot position and orientation
    private double X = rand.nextDouble();
    private double Y = rand.nextDouble();
    private double ANGLE = rand.nextDouble();

    // Constructor starts a new thread that continuously sends data
    public WolfSend2() {
        Thread senderThread = new Thread(() -> {
            // Get the default NetworkTable instance
            NetworkTableInstance inst = NetworkTableInstance.getDefault();

            // Start the client for NetworkTables communication
            inst.startClient4("TelemetryClient");   // Client name
            inst.setServerTeam(9289);               // Set FRC team number
            inst.startDSClient();                   // Connect to Driver Station server

            // Access the custom table where telemetry data will be stored
            NetworkTable t = inst.getTable("BotTelemetry");

            // Continuously send updated telemetry data every 20 ms
            while (true) {
                // Simulated drive motor distances (in meters or ticks)
                t.getEntry("LFD").setDouble(0);                                // Left front drive
                t.getEntry("LBD").setDouble(rand.nextDouble() * 10);          // Left back drive
                t.getEntry("RBD").setDouble(-rand.nextDouble() * 10);         // Right back drive
                t.getEntry("RFD").setDouble((rand.nextDouble() - 0.5) * 10);  // Right front drive

                // Simulated rotation angles for each swerve module (in degrees)
                t.getEntry("LFR").setDouble(0);                                // Left front rotation
                t.getEntry("LBR").setDouble(45);                               // Left back rotation
                t.getEntry("RBR").setDouble(rand.nextDouble() * 360 - 180);   // Right back rotation
                t.getEntry("RFR").setDouble(rand.nextDouble() * 360 - 180);   // Right front rotation

                // Simulated robot pose (X, Y in meters; ANGLE in degrees)
                t.getEntry("POS_X").setDouble(X += rand.nextDouble() * 5 - 2.5);
                t.getEntry("POS_Y").setDouble(Y += rand.nextDouble() * 5 - 2.5);
                t.getEntry("ANGLE").setDouble(ANGLE += rand.nextDouble() * 5 - 2.5);

                // Simulated robot status message
                t.getEntry("status").setString(rand.nextBoolean() ? "OK" : "WARN");

                // Wait 20 ms before next update (50 Hz loop rate)
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    break;  // Exit the loop if the thread is interrupted
                }
            }
        });

        // Mark the thread as daemon so it doesn't block program exit
        senderThread.setDaemon(true);

        // Start the background thread
        senderThread.start();
    }
}
