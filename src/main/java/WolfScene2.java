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
    private static final Group[] wheelGroups = new Group[4];
    private static final Cylinder[] wheels = new Cylinder[4];
    private static final double SPACING = 100;

    private final Translate cameraTranslate = new Translate(0, 0, 600);
    private static double dX = 0;
    private static double dY = 0;
    private final Rotate rotateX = new Rotate(180, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.X_AXIS);

    private static final double[] chassisPose = new double[3];
    private final Group chassisGroup = new Group();
    private final Translate chassisTranslate = new Translate(0, 0, 0);
    private final Rotate chassisRotate = new Rotate(0, Rotate.X_AXIS);
    private Box chassis;

    @Override
    public void start(Stage stage) {
        Group root = new Group();

        // Create chassis box
        chassis = new Box(SPACING * 2, SPACING * 2, 10);
        chassis.setMaterial(new PhongMaterial(Color.SLATEGRAY));

        // Wheel material
        PhongMaterial wheelMaterial = new PhongMaterial(Color.DARKGRAY);

        // Create wheels inside groups (for rotation separation)
        wheelGroups[0] = createWheelGroup(-SPACING, SPACING, wheelMaterial);
        wheelGroups[1] = createWheelGroup(-SPACING, -SPACING, wheelMaterial);
        wheelGroups[2] = createWheelGroup(SPACING, -SPACING, wheelMaterial);
        wheelGroups[3] = createWheelGroup(SPACING, SPACING, wheelMaterial);

        // Add chassis and wheels to chassisGroup
        chassisGroup.getChildren().add(chassis);
        chassisGroup.getChildren().addAll(wheelGroups);

        chassisGroup.getTransforms().addAll(chassisTranslate, chassisRotate);
        root.getChildren().add(chassisGroup);

        // Add grid planes for reference
        root.getChildren().addAll(
                createGridPlane("XY", 1000, 100, Color.GRAY),
                createGridPlane("XZ", 1000, 100, Color.LIGHTGRAY),
                createGridPlane("YZ", 1000, 100, Color.LIGHTGRAY));

        // Setup camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000);
        camera.setFieldOfView(50);
        camera.getTransforms().addAll(cameraTranslate, rotateX, rotateY, rotateZ);

        Scene scene = new Scene(root, 800, 600, true);
        scene.setFill(Color.LIGHTBLUE);
        scene.setCamera(camera);
        stage.setTitle("Wheel Telemetry Viewer");
        stage.setScene(scene);
        stage.show();

        // Keyboard controls for camera
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case Q -> cameraTranslate.setZ(cameraTranslate.getZ() + 10);
                case E -> cameraTranslate.setZ(cameraTranslate.getZ() - 10);
                case W -> cameraTranslate.setY(cameraTranslate.getY() + 10);
                case S -> cameraTranslate.setY(cameraTranslate.getY() - 10);
                case A -> cameraTranslate.setX(cameraTranslate.getX() - 10);
                case D -> cameraTranslate.setX(cameraTranslate.getX() + 10);
                case NUMPAD1 -> {
                    resetCameraRotation();
                    rotateX.setAngle(-112.5);
                    rotateY.setAngle(45);
                    rotateZ.setAngle(11.25);
                    cameraTranslate.setZ(100);
                    dX = -212;
                    dY = -300;
                }
                case NUMPAD2 -> {
                    resetCameraRotation();
                    rotateX.setAngle(-112.5);
                    cameraTranslate.setZ(100);
                    dX = 0;
                    dY = -300;
                }
                case NUMPAD3 -> {
                    resetCameraRotation();
                    rotateX.setAngle(-112.5);
                    rotateY.setAngle(-45);
                    rotateZ.setAngle(-11.25);
                    cameraTranslate.setZ(100);
                    dX = 212;
                    dY = -300;
                }
                case NUMPAD4 -> {
                    resetCameraRotation();
                    rotateY.setAngle(112.5);
                    rotateZ.setAngle(-90);
                    cameraTranslate.setZ(100);
                    dX = -300;
                    dY = 0;
                }
                case NUMPAD5 -> {
                    resetCameraRotation();
                    rotateX.setAngle(180);
                    cameraTranslate.setZ(500);
                    dX = 0;
                    dY = 0;
                }
                case NUMPAD6 -> {
                    resetCameraRotation();
                    rotateY.setAngle(-112.5);
                    rotateZ.setAngle(90);
                    cameraTranslate.setZ(100);
                    dX = 300;
                    dY = 0;
                }
                case NUMPAD7 -> {
                    resetCameraRotation();
                    rotateX.setAngle(112.5);
                    rotateY.setAngle(45);
                    rotateZ.setAngle(168.75);
                    cameraTranslate.setZ(100);
                    dX = -212;
                    dY = 300;
                }
                case NUMPAD8 -> {
                    resetCameraRotation();
                    rotateX.setAngle(112.5);
                    rotateZ.setAngle(180);
                    cameraTranslate.setZ(100);
                    dX = 0;
                    dY = 300;
                }
                case NUMPAD9 -> {
                    resetCameraRotation();
                    rotateX.setAngle(112.5);
                    rotateY.setAngle(-45);
                    rotateZ.setAngle(-168.75);
                    cameraTranslate.setZ(100);
                    dX = 212;
                    dY = 300;
                }
                default -> {
                }
            }
        });

        READY.countDown();
    }

    /** Creates a wheel cylinder inside a Group, positioned at (x,y) */
    private Group createWheelGroup(double x, double y, PhongMaterial material) {
        Cylinder wheel = new Cylinder(40, 20);
        wheel.setMaterial(material);
        wheel.getTransforms().add(new Rotate(90, Rotate.Y_AXIS)); // Base orientation

        Group group = new Group(wheel);

        // Position the wheel group
        group.setTranslateX(x);
        group.setTranslateY(y);

        // Store references for update
        for (int i = 0; i < wheels.length; i++) {
            if (wheels[i] == null) {
                wheels[i] = wheel;
                wheelGroups[i] = group;
                break;
            }
        }
        return group;
    }

    /** Update wheels to rotate by steering angle */
    private static void updateWheels(double[] vels, double[] angles) {
        for (int i = 0; i < 4; i++) {
            Cylinder wheel = wheels[i];
            // Remove previous steering rotations (exclude base 90 deg)
            wheel.getTransforms().removeIf(t -> t instanceof Rotate && ((Rotate) t).getAngle() != 90);

            double angleDegrees = Math.toDegrees(angles[i]);
            Rotate rotate = new Rotate(angleDegrees, Rotate.X_AXIS);

            wheel.getTransforms().add(rotate);

            if(vels[i] > 0) wheel.setMaterial(new PhongMaterial(Color.GREEN));
            else if(vels[i] < 0) wheel.setMaterial(new PhongMaterial(Color.RED));
            else wheel.setMaterial(new PhongMaterial(Color.DARKGRAY));

            /*
             * Green for forward, red for backward, dark gray for stopped
             */
        }
    }

    /** Update chassis and wheels pose */
    public static void update(double[] vels, double[] angles, double[] chassisPose) {
        updateWheels(vels, angles);

        double x = chassisPose[0];
        double y = -chassisPose[1]; // Y axis inverted

        WolfScene2 instance = Instance();
        //instance.chassisRotate.setAngle(-angleDeg);
        instance.chassisTranslate.setX(x);
        instance.chassisTranslate.setY(y);

        //instance.cameraTranslate.setX(x + dX);
        //instance.cameraTranslate.setY(y + dY);
    }

    private Group createGridPlane(String axis, double size, int divisions, Color color) {
        Group grid = new Group();
        double spacing = size / divisions;

        for (int i = -divisions / 2; i <= divisions / 2; i++) {
            Line line1, line2;
            switch (axis) {
                case "XY" -> {
                    line1 = new Line(-size / 2, i * spacing, size / 2, i * spacing);
                    line2 = new Line(i * spacing, -size / 2, i * spacing, size / 2);
                }
                case "XZ" -> {
                    line1 = new Line(-size / 2, 0, size / 2, 0);
                    line2 = new Line(i * spacing, 0, i * spacing, 0);
                    line1.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
                    line2.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
                    line1.setTranslateZ(i * spacing);
                    line2.setTranslateZ(-size / 2 + i * spacing);
                }
                case "YZ" -> {
                    line1 = new Line(-size / 2, 0, size / 2, 0);
                    line2 = new Line(i * spacing, 0, i * spacing, 0);
                    line1.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));
                    line2.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));
                    line1.setTranslateX(i * spacing);
                    line2.setTranslateX(-size / 2 + i * spacing);
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

    public static void resetCameraRotation() {
        WolfScene2 instance = Instance();
        instance.rotateX.setAngle(0);
        instance.rotateY.setAngle(0);
        instance.rotateZ.setAngle(0);
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
