package nodomain.yalg;

/**
 * Created by andreas on 11.09.2015.
 */
public class Receptor extends Physical {
    GameObject statusBarRed = new GameObject();
    GameObject statusBarGreen = new GameObject();
    GameObject statusBarBlue = new GameObject();

    Receptor() {
        refractionIndex = -1;
    }

    public void render(int posHandle, int uvHandle, int laserColHandle) {
        super.render(posHandle, uvHandle, laserColHandle);
        statusBarRed.render(posHandle, uvHandle, laserColHandle);
    }
}
