import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Line;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

public class WolfScene2 extends Application {
    static final java.util.concurrent.CountDownLatch READY = new java.util.concurrent.CountDownLatch(1);
    private static final Cylinder[] wheels = new Cylinder[4];
    private static final double SPACING = 100;

    private final Translate cameTrans = new Translate(0, 0, 600);
    private static double dX = 0;
    private static double dY = 0;
    private final double camDist = 400.0;
    private final double camDistX = Math.round(Math.sqrt((camDist * camDist) / 2.0));
    private final Rotate rotX = new Rotate(180, Rotate.X_AXIS);
    private final Rotate rotY = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rotZ = new Rotate(0, Rotate.Z_AXIS);

    private static final double[] chassisPose = new double[3];
    private final Group chassisGroup = new Group();
    private final Translate chassisTranslate = new Translate(0, 0, 0);
    private final Rotate chassisRotate = new Rotate(0, Rotate.Z_AXIS);
    private Box chassis;

    @Override
    public void start(Stage stage) {
        Group root = new Group();

        chassis = new Box(SPACING * 2, SPACING * 2, 10);
        chassis.setMaterial(new PhongMaterial(Color.SLATEGRAY));

        PhongMaterial wheelMaterial = new PhongMaterial(Color.DARKGRAY);
        wheels[0] = createWheel(-SPACING, SPACING, wheelMaterial);
        wheels[1] = createWheel(-SPACING, -SPACING, wheelMaterial);
        wheels[2] = createWheel(SPACING, -SPACING, wheelMaterial);
        wheels[3] = createWheel(SPACING, SPACING, wheelMaterial);

        chassisGroup.getChildren().addAll(chassis, wheels[0], wheels[1], wheels[2], wheels[3]);
        chassisGroup.getTransforms().addAll(chassisTranslate, chassisRotate);
        root.getChildren().add(chassisGroup);

        root.getChildren().addAll(
                createGrid("XY", 10000, 500, Color.RED),
                createGrid("XZ", 10000, 500, Color.GREEN),
                createGrid("YZ", 10000, 500, Color.BLUE));

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000);
        camera.setFieldOfView(50);
        camera.getTransforms().addAll(cameTrans, rotX, rotY, rotZ);

        Scene scene = new Scene(root, 1000, 800, true);
        scene.setFill(Color.LIGHTBLUE);
        scene.setCamera(camera);
        stage.setTitle("Wheel Telemetry Viewer");
        stage.setScene(scene);
        stage.show();

        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case Q -> cameTrans.setZ(cameTrans.getZ() + 10);
                case E -> cameTrans.setZ(cameTrans.getZ() - 10);
                case W -> dY += 5;
                case S -> dY -= 5;
                case A -> dX -= 5;
                case D -> dX += 5;
                case UP -> rotX.setAngle(rotX.getAngle() - 2);
                case DOWN -> rotX.setAngle(rotX.getAngle() + 2);
                case LEFT -> rotY.setAngle(rotY.getAngle() - 2);
                case RIGHT -> rotY.setAngle(rotY.getAngle() + 2);
                case OPEN_BRACKET -> rotZ.setAngle(rotZ.getAngle() - 2);
                case CLOSE_BRACKET -> rotZ.setAngle(rotZ.getAngle() + 2);
                case NUMPAD1 -> {
                    resetCamRot();
                    rotX.setAngle(-112.5);
                    rotY.setAngle(45);
                    rotZ.setAngle(11.25);
                    cameTrans.setZ(100);
                    dX = -this.camDistX;
                    dY = -this.camDistX;
                }
                case NUMPAD2 -> {
                    resetCamRot();
                    rotX.setAngle(-112.5);
                    cameTrans.setZ(100);
                    dX = 0;
                    dY = -this.camDist;
                }
                case NUMPAD3 -> {
                    resetCamRot();
                    rotX.setAngle(-112.5);
                    rotY.setAngle(-45);
                    rotZ.setAngle(-11.25);
                    cameTrans.setZ(100);
                    dX = this.camDistX;
                    dY = -this.camDistX;
                }
                case NUMPAD4 -> {
                    resetCamRot();
                    rotY.setAngle(112.5);
                    rotZ.setAngle(-90);
                    cameTrans.setZ(100);
                    dX = -this.camDist;
                    dY = 0;
                }
                case NUMPAD5 -> {
                    resetCamRot();
                    rotX.setAngle(180);
                    cameTrans.setZ(500);
                    dX = 0;
                    dY = 0;
                }
                case NUMPAD6 -> {
                    resetCamRot();
                    rotY.setAngle(-112.5);
                    rotZ.setAngle(90);
                    cameTrans.setZ(100);
                    dX = this.camDist;
                    dY = 0;
                }
                case NUMPAD7 -> {
                    resetCamRot();
                    rotX.setAngle(112.5);
                    rotY.setAngle(45);
                    rotZ.setAngle(168.75);
                    cameTrans.setZ(100);
                    dX = -this.camDistX;
                    dY = this.camDistX;
                }
                case NUMPAD8 -> {
                    resetCamRot();
                    rotX.setAngle(112.5);
                    rotZ.setAngle(180);
                    cameTrans.setZ(100);
                    dX = 0;
                    dY = this.camDist;
                }
                case NUMPAD9 -> {
                    resetCamRot();
                    rotX.setAngle(112.5);
                    rotY.setAngle(-45);
                    rotZ.setAngle(-168.75);
                    cameTrans.setZ(100);
                    dX = this.camDistX;
                    dY = this.camDistX;
                }
                default -> {
                }
            }
        });

        READY.countDown();
    }

    private Cylinder createWheel(double x, double y, PhongMaterial material) {
        Cylinder wheel = new Cylinder(40, 20);
        wheel.setMaterial(material);
        wheel.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));
        wheel.setTranslateX(x);
        wheel.setTranslateY(y);
        return wheel;
    }

    public static void updateWheels(double[] vels, double[] angles, double[] angles2) {
        for (int i = 0; i < 4; i++) {
            PhongMaterial mat = new PhongMaterial(Color.WHITE);
            double vel = vels[i] / 10;
            if (!wheelCheck(angles[i], angles2[i])) {
                mat = new PhongMaterial(Color.color(1, 0, 0));
            } else if (vel > 0) {
                vel = Math.min(1, vel);
                mat = new PhongMaterial(Color.color(0, vel, 0));
            } else if (vel == 0) {
                mat = new PhongMaterial(Color.color(0, 0, 0));
            } else if (vel < 0) {
                vel = Math.max(-1, vel);
                mat = new PhongMaterial(Color.color(0, 0, -vel));
            }
            wheels[i].setMaterial(mat);
            wheels[i].getTransforms()
                    .removeIf(t -> t instanceof Rotate && ((Rotate) t).getAxis().equals(Rotate.X_AXIS));
            wheels[i].getTransforms().add(new Rotate(angles[i] - 90.0, Rotate.X_AXIS));
        }
    }

    public static void update(double[] vels, double[] angles, double[] angles2, double[] chassisPose) {
        updateWheels(vels, angles, angles2);

        double x = chassisPose[0];
        double y = -chassisPose[1]; // Assuming Y is inverted
        double angleDeg = chassisPose[2];

        // Update chassis transform
        WolfScene2 instance = Instance();
        instance.chassisTranslate.setX(x);
        instance.chassisTranslate.setY(y);
        instance.chassisRotate.setAngle(-angleDeg);

        instance.cameTrans.setX(x + dX);
        instance.cameTrans.setY(y + dY);
    }

    private static boolean wheelCheck(double vel1, double vel2) {
        return Math.abs(vel1 - vel2) < 10;
    }

    private Group createGrid(String axis, double size, int divs, Color color) {
        Group grid = new Group();
        double spacing = size / divs;

        for (int i = -divs / 2; i <= divs / 2; i++) {
            Line line1, line2;
            switch (axis) {
                case "XY" -> {
                    line1 = new Line(-size / 2, i * spacing, size / 2, i * spacing);
                    line2 = new Line(i * spacing, -size / 2, i * spacing, size / 2);

                    line1.setTranslateZ(0);
                    line2.setTranslateZ(0);
                }
                case "XZ" -> {
                    line1 = new Line(-size / 2, i * spacing, size / 2, i * spacing);
                    line2 = new Line(i * spacing, -size / 2, i * spacing, size / 2);

                    line1.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
                    line2.getTransforms().add(new Rotate(90, Rotate.X_AXIS));

                    line1.setTranslateY(0);
                    line2.setTranslateY(0);
                }
                case "YZ" -> {
                    line1 = new Line(-size / 2, i * spacing, size / 2, i * spacing);
                    line2 = new Line(i * spacing, -size / 2, i * spacing, size / 2);

                    line1.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));
                    line2.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));

                    line1.setTranslateX(0);
                    line2.setTranslateX(0);
                }
                default -> {
                    return grid;
                }
            }

            line1.setStroke(color);
            line2.setStroke(color);
            line1.setStrokeWidth(0.25);
            line2.setStrokeWidth(0.25);
            grid.getChildren().addAll(line1, line2);
        }
        return grid;
    }

    public static void resetCamRot() {
        WolfScene2 instance = Instance();
        instance.rotX.setAngle(0);
        instance.rotY.setAngle(0);
        instance.rotZ.setAngle(0);
    }

    private static WolfScene2 instance;

    private static WolfScene2 Instance() {
        return instance;
    }

    @Override
    public void init() {
        instance = this;
    }

    public static void main(String[] args) {
        launch(args);
    }
}