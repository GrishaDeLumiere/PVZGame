package game.level.wave;

public class ZombieLaneManager {

    private final int numLanes; // Количество дорожек
    private final float gridSize; // Размер сетки между дорожками
    private final float offsetX; // Смещение по X
    private final float offsetY; // Смещение по Y

    private final float[] laneCentersX; // Центры дорожек по X
    private final float[] laneCentersY; // Центры дорожек по Y

    public ZombieLaneManager(int numLanes, float gridSize, float offsetX, float offsetY) {
        this.numLanes = numLanes;
        this.gridSize = gridSize;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        laneCentersX = new float[numLanes];
        laneCentersY = new float[numLanes];
        initializeLanes();
    }

    private void initializeLanes() {
        float laneWidth = 1f / numLanes; // Ширина каждой дорожки в нормализованном виде
        for (int i = 0; i < numLanes; i++) {
            // Инициализация координат X
            laneCentersX[i] = (laneWidth * i) + (laneWidth / 2) + offsetX;
            // Инициализация координат Y
            laneCentersY[i] = offsetY + (i * gridSize);
        }
    }

    public float getLaneX(int laneIndex) {
        if (laneIndex < 0 || laneIndex >= numLanes) {
            throw new IndexOutOfBoundsException("Invalid lane index: " + laneIndex);
        }
        return laneCentersX[laneIndex];
    }

    public float getLaneY(int laneIndex) {
        if (laneIndex < 0 || laneIndex >= numLanes) {
            throw new IndexOutOfBoundsException("Invalid lane index: " + laneIndex);
        }
        return laneCentersY[laneIndex];
    }

    public int getNumLanes() {
        return numLanes;
    }
}
