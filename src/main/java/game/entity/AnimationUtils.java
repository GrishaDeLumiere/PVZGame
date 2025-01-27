package game.entity;

import java.util.ArrayList;
import java.util.List;

public class AnimationUtils {

    public static List<String> createAnimationFrames(String prefix, int start, int end) {
        List<String> frames = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            frames.add(String.format("%s%04d", prefix, i));
        }
        return frames;
    }
}