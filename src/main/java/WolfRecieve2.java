// Imports necessary libraries for file I/O, date/time formatting, JSON handling, NetworkTables, and JavaFX
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import javafx.application.Application;
import javafx.application.Platform;

public class WolfRecieve2 {
    // Shared JSON array to store telemetry data
    static JSONArray jsonArray = new JSONArray();
    
    // Tracks last time data was written to file
    static long lastWriteTime = System.currentTimeMillis();
    
    // Stores the output file path
    static String FILE_PATH;

    public static void main(String[] args) throws Exception {
        // Validate and parse arguments
        if (args.length < 2) {
            System.out.println("Usage: run <TYPE: 1=live, 2=replay> <TYPE2: 1=send, 0=nosend>");
            return;
        }
        final int TYPE = Integer.parseInt(args[0]);   // 1 = live mode, 2 = replay, 3 = list files
        final int TYPE2 = Integer.parseInt(args[1]);  // 1 = send live data, 0 = just receive

        // Start the JavaFX UI unless TYPE is 3 (list files only)
        if (TYPE != 3) {
            new Thread(() -> Application.launch(WolfScene2.class), "FX-Launcher").start();
            WolfScene2.READY.await();  // Wait for the UI to initialize
        }

        if (TYPE == 1) {
            // ===== LIVE MODE =====

            // Create unique timestamped filename for saving data
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"));
            FILE_PATH = "./records/" + timestamp + ".json";

            // Start NetworkTables client
            NetworkTableInstance inst = NetworkTableInstance.getDefault();
            inst.startClient4("TelemetryClient");
            inst.setServerTeam(9289);  // Set to team number
            inst.startDSClient();

            // Get the telemetry table from NetworkTables
            NetworkTable t = inst.getTable("BotTelemetry");

            // If TYPE2 == 1, start sending data out (handled by WolfSend2)
            if (TYPE2 == 1) {
                new WolfSend2();
            }

            // Add shutdown hook to write full data to file when program exits
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try (FileWriter fw = new FileWriter(FILE_PATH)) {
                    fw.write(jsonArray.toString(4));  // Write nicely formatted JSON
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));

            // Schedule task to read data every 20ms (50Hz)
            java.util.concurrent.ScheduledExecutorService exec = java.util.concurrent.Executors
                    .newSingleThreadScheduledExecutor();

            exec.scheduleAtFixedRate(() -> {
                // Read wheel velocities
                double[] vels = {
                    t.getEntry("LFD").getDouble(0.0),
                    t.getEntry("LBD").getDouble(0.0),
                    t.getEntry("RBD").getDouble(0.0),
                    t.getEntry("RFD").getDouble(0.0)
                };
                
                // Read wheel angles
                double[] angles = {
                    t.getEntry("LFR").getDouble(0.0),
                    t.getEntry("LBR").getDouble(0.0),
                    t.getEntry("RBR").getDouble(0.0),
                    t.getEntry("RFR").getDouble(0.0)
                };

                // Read chassis position and heading
                double[] chassis = {
                    t.getEntry("POS_X").getDouble(0.0),
                    t.getEntry("POS_Y").getDouble(0.0),
                    t.getEntry("ANGLE").getDouble(0.0)
                };

                // Optional status check
                if (!t.getEntry("status").getString("FAKE").equals("FAKE")) {
                    System.out.println(t.getEntry("status").getString("FAKE"));
                }

                // Create a JSON object for this telemetry snapshot
                JSONObject entry = new JSONObject();
                entry.put("angles", new JSONArray(angles));
                entry.put("vels", new JSONArray(vels));
                entry.put("chassis", new JSONArray(chassis));
                jsonArray.put(entry);  // Append to the main array

                // Periodically write to file every 1 second
                long now = System.currentTimeMillis();
                if (now - lastWriteTime > 1000) {
                    try (FileWriter fw = new FileWriter(FILE_PATH)) {
                        fw.write(jsonArray.toString(4));
                        lastWriteTime = now;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Update UI with new telemetry
                Platform.runLater(() -> WolfScene2.update(vels, angles, chassis));
            }, 0, 20, java.util.concurrent.TimeUnit.MILLISECONDS);

        } else if (TYPE == 2) {
            // ===== REPLAY MODE =====
            final String INPUT_PATH = "./records/" + args[2] + ".json";
            JSONArray replayArray;

            // Read the specified JSON file
            try {
                String content = java.nio.file.Files.readString(java.nio.file.Paths.get(INPUT_PATH));
                replayArray = new JSONArray(content);
            } catch (Exception e) {
                System.out.println("Failed to parse file: " + e.getMessage());
                return;
            }

            java.util.concurrent.ScheduledExecutorService replayExec = java.util.concurrent.Executors
                    .newSingleThreadScheduledExecutor();
            final int[] index = { 0 };  // Current frame index

            // Schedule replay at 20ms intervals
            replayExec.scheduleAtFixedRate(() -> {
                if (index[0] >= replayArray.length()) {
                    replayExec.shutdown();  // End replay
                    System.out.println("Replay finished.");
                    return;
                }

                // Read next entry from replay file
                JSONObject entry = replayArray.getJSONObject(index[0]++);
                JSONArray anglesArr = entry.getJSONArray("angles");
                JSONArray velsArr = entry.getJSONArray("vels");
                JSONArray chassisArr = entry.getJSONArray("chassis");

                // Convert JSON arrays to primitive double arrays
                double[] angles = new double[anglesArr.length()];
                double[] vels = new double[velsArr.length()];
                double[] chassis = new double[chassisArr.length()];

                for (int i = 0; i < angles.length; i++) {
                    angles[i] = anglesArr.getDouble(i);
                }
                for (int i = 0; i < vels.length; i++) {
                    vels[i] = velsArr.getDouble(i);
                }
                for (int i = 0; i < chassis.length; i++) {
                    chassis[i] = chassisArr.getDouble(i);
                }

                // Update the UI with replayed telemetry
                Platform.runLater(() -> WolfScene2.update(vels, angles, chassis));
            }, 0, 20, java.util.concurrent.TimeUnit.MILLISECONDS);

        } else if (TYPE == 3) {
            // ===== FILE LISTING MODE =====

            // Check if records directory exists
            File recordsDir = new File("./records");
            if (!recordsDir.exists() || !recordsDir.isDirectory()) {
                System.out.println("Records directory does not exist or is not a directory.");
                return;
            }

            // List all .json files
            String[] files = recordsDir.list((dir, name) -> name.endsWith(".json"));
            if (files == null || files.length == 0) {
                System.out.println("No JSON files found in records directory.");
                return;
            }

            // Print filenames
            System.out.println("Available record files:");
            for (String file : files) {
                System.out.println(" - " + file);
            }
        }
    }
}
