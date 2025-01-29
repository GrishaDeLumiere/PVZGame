package game.sound;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;

import game.SettingsManager;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.logging.Logger;
import java.util.logging.Level;

public class OpenALManager {

    private static final Logger LOGGER = Logger.getLogger(OpenALManager.class.getName());

    private static final int REFRESH_RATE = 60;
    private static final boolean ALC_SYNC_FLAG = false;

    private static long device;
    private static ALCCapabilities deviceCaps;
    private static long context;
    private SoundEffectManager soundEffectManager;
    private MusicManager musicManager;

    public OpenALManager() {
        try {
            initOpenAL();
            soundEffectManager = new SoundEffectManager();
            musicManager = new MusicManager();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize OpenAL", e);
        }
    }

    private void initOpenAL() throws Exception {
        device = ALC10.alcOpenDevice((ByteBuffer) null);
        if (device == 0) {
            throw new IllegalStateException("Failed to open the default OpenAL device.");
        }

        deviceCaps = ALC.createCapabilities(device);

        IntBuffer contextAttribList = BufferUtils.createIntBuffer(16);
        contextAttribList.put(ALC10.ALC_REFRESH).put(REFRESH_RATE);
        contextAttribList.put(ALC10.ALC_SYNC).put(ALC_SYNC_FLAG ? ALC10.ALC_TRUE : ALC10.ALC_FALSE);
        contextAttribList.flip();

        context = ALC10.alcCreateContext(device, contextAttribList);
        if (!ALC10.alcMakeContextCurrent(context)) {
            throw new Exception("Failed to make context current");
        }
        AL.createCapabilities(deviceCaps);

        LOGGER.info("OpenAL initialized successfully.");
    }

    public SoundEffectManager getSoundEffectManager() {
        return soundEffectManager;
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }

    public void loadSoundEngine() {
        soundEffectManager.setMasterVolume(SettingsManager.getMasterVolume());
        musicManager.backgroundMusicVolume = SettingsManager.getBackgroundMusicVolume();

        String soundFolderPath = "assets/sounds/";

        File folder = new File(soundFolderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            LOGGER.warning("Sound folder does not exist or is not a directory: " + folder.getPath());
            return;
        }

        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".wav")) {
                String key = file.getName().substring(0, file.getName().lastIndexOf('.'));
                String filePath = soundFolderPath + file.getName();
                soundEffectManager.loadSoundEffect(key, filePath);
            }
        }

        LOGGER.info("Loaded sound effects: " + soundEffectManager.getSoundEffects().length);
        musicManager.loadAndPlayBackgroundMusic("music/MainMenu.wav");
    }

}