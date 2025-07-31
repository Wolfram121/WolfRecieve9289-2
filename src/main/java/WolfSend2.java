import java.util.Random;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

public class WolfSend2 {

    private double X = 0;
    private double Y = 0;
    private double ANGLE = 0;

    public WolfSend2() {
        Thread senderThread = new Thread(() -> {
            NetworkTableInstance inst = NetworkTableInstance.getDefault();
            inst.startClient4("TelemetryClient");
            inst.setServerTeam(9289);
            inst.startDSClient();
            NetworkTable t = inst.getTable("BotTelemetry");
            Random rand = new Random();
            while (true) {
                t.getEntry("LFD").setDouble(0);
                t.getEntry("LBD").setDouble(0);
                t.getEntry("RBD").setDouble(0);
                t.getEntry("RFD").setDouble(rand.nextDouble() * 360 - 180);
                t.getEntry("LFR").setDouble(rand.nextDouble());
                t.getEntry("LBR").setDouble(rand.nextDouble());
                t.getEntry("RBR").setDouble(rand.nextDouble());
                t.getEntry("RFR").setDouble(rand.nextDouble());
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
