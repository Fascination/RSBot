import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.wrappers.Locatable;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.Context;

/**
 * MouseCamera is a class written for RSBot containing methods using the mouse (instead of keys) to perform camera
 * actions such as setting the yaw, pitch or turning to a locatable.
 * <p>
 * Note: Should only be used in fixed mode!
 *
 * @author Buccaneer
 */
@SuppressWarnings("deprecation unused")
public class MouseCamera {

    private final static Rectangle canvas = new Rectangle(4, 53, 512, 334);
    private final static double ppdYaw = 2.8440;
    private final static double ppdPitch = 2.5926;

    /**
     * Returns a boolean that indicates whether the camera successfully turned to given yaw and pitch with given
     * deviations.
     * <p>
     * This method should be preferred over turnTo(int yaw, int pitch) as it is less likely to be detected
     *
     * @param yaw   integer value that is the target yaw
     * @param pitch integer value that is the target pitch
     * @param ydev  integer value that is the maximum allowed deviation on the yaw
     * @param pdev  integer value that is the maximum allowed deviation on the pitch
     * @return      <tt>true</tt> if the camera is set towards the target yaw and pitch with given deviations
     * @see         Camera
     */
    public synchronized static boolean turnTo(int yaw, int pitch, int ydev, int pdev){
        final org.powerbot.game.client.input.Mouse mouse = Context.client().getMouse();
        final Component target = Context.get().getLoader().getComponent(0);
        int deltaYaw = (yaw - Camera.getYaw() + 180) % 360 - 180;
        if (deltaYaw < -180) deltaYaw += 360;
        int horizontal = (int) ((deltaYaw + Random.nextInt(-ydev,ydev)) * ppdYaw);
        int deltaPitch = pitch - Camera.getPitch();
        int vertical = (int) ((deltaPitch + Random.nextInt(-pdev, pdev) * ppdPitch));
        Point startPoint, endPoint;
        Rectangle startRect;
        if (horizontal > 0) { // Rect to the right
            if (vertical > 0) { // Rect to the top
                startRect = new Rectangle(canvas.x + horizontal, canvas.y, canvas.width - horizontal, canvas.height - vertical);
            } else { // Rect to the bottom
                startRect = new Rectangle(canvas.x + horizontal, canvas.y - vertical, canvas.width - horizontal, canvas.height + vertical);
            }
        } else { // Rect to the left
            if (vertical > 0) { // Rect to the top
                startRect = new Rectangle(canvas.x, canvas.y, canvas.width + horizontal, canvas.height - vertical);
            } else { // Rect to the bottom
                startRect = new Rectangle(canvas.x, canvas.y - vertical, canvas.width + horizontal, canvas.height + vertical);
            }
        }
        if (!startRect.contains(Mouse.getLocation())) {
            final LinkedList<Point> startPoints = new LinkedList<Point>();
            for (int x = canvas.x; x <= canvas.x + canvas.width; x++) {
                for (int y = canvas.y; y <= canvas.y + canvas.height; y++) {
                    if (startRect != null && startRect.contains(x, y)) {
                        startPoints.add(new Point(x,y));
                    }
                }
            }
            startPoint = startPoints.get(Random.nextInt(0,startPoints.size()));
            endPoint = new Point(startPoint.x - horizontal, startPoint.y + vertical);
        } else {
            startPoint = Mouse.getLocation();
            endPoint = new Point(Mouse.getX() - horizontal, Mouse.getY() + vertical);
        }
        if (mouse == null || target == null ||
                mouse.isPressed()) {
            return false;
        }
        if (!Mouse.getLocation().equals(startPoint)) {
            Mouse.move(startPoint);
        }
        mouse.sendEvent(
                new MouseEvent(target, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, Mouse.getX(), Mouse.getY(), 1, false, MouseEvent.BUTTON2)
        );
        Mouse.move(endPoint);
        mouse.sendEvent(
                new MouseEvent(target, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, Mouse.getX(), Mouse.getY(), 1, false, MouseEvent.BUTTON2)
        );
        return Mouse.getLocation().equals(endPoint) && !Mouse.isPressed();
    }

    /**
     * Returns a boolean that indicates whether the camera successfully turned to given yaw and pitch.
     *
     * @param yaw   integer value that is the target yaw
     * @param pitch integer value that is the target pitch
     * @return      <tt>true</tt> if the camera is set towards the target yaw and pitch
     * @see         Camera
     */
    public synchronized static boolean turnTo(int yaw, int pitch) {
        return turnTo(yaw, pitch, 0, 0);
    }

    /**
     * Returns a boolean that indicates whether the camera successfully turned to given locatable with given deviations.
     * <p>
     * This method should be preferred over turnTo(Locatable loc) as it is less likely to be detected.
     *
     * @param loc   locatable that is our target
     * @param ydev  integer value that is the maximum allowed deviation on the yaw
     * @param pdev  integer value that is the maximum allowed deviation on the pitch
     * @return      <tt>true</tt> if the camera is set towards the target locatable with given deviations
     * @see         Camera
     */
    public synchronized static boolean turnTo(Locatable loc, int ydev, int pdev) {
        return turnTo(getYawTo(loc), getPitchTo(loc), ydev, pdev);
    }

    /**
     * Returns a boolean that indicates whether the camera successfully turned to given locatable.
     *
     * @param loc   locatable that is our target
     * @return      <tt>true</tt> if the camera is set towards the target locatable
     * @see         Camera
     */
    public synchronized static boolean turnTo(Locatable loc) {
        return turnTo(loc, 0, 0);
    }

    /**
     * Returns a boolean that indicates whether the yaw was successfully set with given deviations.
     * <p>
     * This method should be preferred over setYaw(int yaw) as it is less likely to be detected.
     *
     * @param yaw   integer value that is the target yaw
     * @param ydev  integer value that is the maximum allowed deviation on the yaw
     * @param pdev  integer value that is the maximum allowed deviation on the pitch
     * @return      <tt>true</tt> if the current yaw matches the target yaw with given deviations
     * @see         Camera
     */
    public synchronized static boolean setYaw(int yaw, int ydev, int pdev) {
        return turnTo(yaw, Camera.getPitch(), ydev, pdev);
    }

    /**
     * Returns a boolean that indicates whether the yaw was successfully set.
     *
     * @param yaw   integer value that is the target yaw
     * @return      <tt>true</tt> if the current yaw matches the target yaw
     * @see         Camera
     */
    public synchronized static boolean setYaw(int yaw) {
        return setYaw(yaw, 0, 0);
    }

    /**
     * Returns a boolean that indicates whether the pitch was successfully set with given deviations.
     * <p>
     * This method should be preferred over setPitch(int pitch) as it is less likely to be detected.
     *
     * @param pitch integer value that is the target pitch
     * @param ydev  integer value that is the maximum allowed deviation on the yaw
     * @param pdev  integer value that is the maximum allowed deviation on the pitch
     * @return      <tt>true</tt> if the current pitch matches the target pitch with given deviations
     * @see         Camera
     */
    public synchronized static boolean setPitch(int pitch, int ydev, int pdev) {
        return turnTo(Camera.getYaw(), pitch, ydev, pdev);
    }

    /**
     * Returns a boolean that indicates whether the pitch was successfully set.
     *
     * @param pitch integer value that is the target pitch
     * @return      <tt>true</tt> if the current pitch matches the target pitch
     * @see         Camera
     */
    public synchronized static boolean setPitch(int pitch) {
        return setPitch(pitch, 0, 0);
    }

    private static int getYawTo(final Locatable locatable) {
        final Tile t = locatable.getLocation();
        final Tile me = Players.getLocal().getLocation();
        int yaw = ((int) Math.toDegrees(Math.atan2(t.getY() - me.getY(), t.getX() - me.getX()))) - 90;
        if (yaw < 0) yaw += 360;
        return yaw;
    }

    private static int getPitchTo(final Locatable locatable) {
        final int distance = (int) Players.getLocal().getLocation().distance(locatable);
        int pitch = 2;
        if (distance < 8) pitch = (int) (88 - (distance * 10.75 + Random.nextDouble(0, 10.75)));
        return pitch;
    }

}
