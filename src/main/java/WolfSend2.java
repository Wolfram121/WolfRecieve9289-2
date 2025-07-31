import java.util.Random;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

public class WolfSend2 {

    Random rand = new Random();
    private double X = rand.nextDouble();
    private double Y = rand.nextDouble();
    private double ANGLE = rand.nextDouble();

    public WolfSend2() {
        Thread senderThread = new Thread(() -> {
            NetworkTableInstance inst = NetworkTableInstance.getDefault();
            inst.startClient4("TelemetryClient");
            inst.setServerTeam(9289);
            inst.startDSClient();
            NetworkTable t = inst.getTable("BotTelemetry");
            while (true) {
                t.getEntry("LFD").setDouble(0);
                t.getEntry("LBD").setDouble(rand.nextDouble());
                t.getEntry("RBD").setDouble(-rand.nextDouble());
                t.getEntry("RFD").setDouble(rand.nextDouble() - 0.5);
                t.getEntry("LFR").setDouble(0);
                t.getEntry("LBR").setDouble(45);
                t.getEntry("RBR").setDouble(rand.nextDouble() * 360 - 180);
                t.getEntry("RFR").setDouble(rand.nextDouble() * 360 - 180);
                t.getEntry("POS_X").setDouble(X += rand.nextDouble() * 5 - 2.5);
                t.getEntry("POS_Y").setDouble(Y += rand.nextDouble() * 5 - 2.5);
                t.getEntry("ANGLE").setDouble(ANGLE += rand.nextDouble() * 5 - 2.5);
                t.getEntry("status").setString(rand.nextBoolean() ? "OK" : "WARN");
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        senderThread.setDaemon(true);
        senderThread.start();
    }
}
