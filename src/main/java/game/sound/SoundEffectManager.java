package game.sound;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.*;
import org.apache.commons.io.IOUtils;

public class SoundEffectManager {

    private static final Logger LOGGER = Logger.getLogger(SoundEffectManager.class.getName());
    private static final int MAX_SOUND_SOURCES = 100;
    private static float masterVolume = 1.0f;
    private static final Map<String, Integer> soundEffects = new HashMap<>();
    private final int[] soundEffectSources = new int[MAX_SOUND_SOURCES];
    private final boolean[] sourceInUse = new boolean[MAX_SOUND_SOURCES];

    public SoundEffectManager() {
        initSoundEffectSources();
    }

    private void initSoundEffectSources() {
        for (int i = 0; i < MAX_SOUND_SOURCES; i++) {
            soundEffectSources[i] = AL10.alGenSources();
            sourceInUse[i] = false;
        }
    }

    public void loadSoundEffect(String key, String filePath) {
        if (soundEffects.containsKey(key)) {
            return;
        }

        try {
            File soundFile = new File(filePath);
            if (!soundFile.exists()) {
                LOGGER.warning("Sound file not found: " + filePath);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            AudioFormat format = audioStream.getFormat();
            byte[] audioData = IOUtils.toByteArray(audioStream);
            ByteBuffer bufferData = BufferUtils.createByteBuffer(audioData.length).put(audioData);
            bufferData.flip();

            int buffer = AL10.alGenBuffers();
            int openALFormat = getOpenALFormat(format);
            if (openALFormat == -1) {
                LOGGER.warning("Unsupported audio format for file: " + filePath);
                return;
            }

            AL10.alBufferData(buffer, openALFormat, bufferData, (int) format.getSampleRate());
            soundEffects.put(key, buffer);
            LOGGER.info("Loaded sound effect: " + key);

        } catch (UnsupportedAudioFileException | IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading sound effect: " + filePath, e);
        }
    }

    private int getOpenALFormat(AudioFormat format) {
        int channels = format.getChannels();
        int sampleSizeInBits = format.getSampleSizeInBits();

        if (channels == 1) {
            if (sampleSizeInBits == 8) {
                return AL10.AL_FORMAT_MONO8;
            } else if (sampleSizeInBits == 16) {
                return AL10.AL_FORMAT_MONO16;
            }
        } else if (channels == 2) {
            if (sampleSizeInBits == 8) {
                return AL10.AL_FORMAT_STEREO8;
            } else if (sampleSizeInBits == 16) {
                return AL10.AL_FORMAT_STEREO16;
            }
        }

        return -1;  
    }

    public void playSoundEffect(String key) {
        updateSourceUsage();
        Integer buffer = soundEffects.get(key);
        if (buffer != null) {
            int source = findAvailableSource();
            if (source != -1) {
                AL10.alSourcei(source, AL10.AL_BUFFER, buffer);
                AL10.alSourcef(source, AL10.AL_GAIN, masterVolume);
                AL10.alSourcePlay(source);
                sourceInUse[source] = true;
            } else {
                LOGGER.warning("No available sound sources to play effect: " + key);
            }
        } else {
            LOGGER.warning("Sound effect not found: " + key);
        }
    }

    private int findAvailableSource() {
        for (int i = 0; i < MAX_SOUND_SOURCES; i++) {
            if (!sourceInUse[i]) {
                sourceInUse[i] = true;
                return soundEffectSources[i];
            }
        }
        return -1;
    }

    public void updateSourceUsage() {
        for (int i = 0; i < MAX_SOUND_SOURCES; i++) {
            if (sourceInUse[i]) {
                int state = AL10.alGetSourcei(soundEffectSources[i], AL10.AL_SOURCE_STATE);
                if (state == AL10.AL_STOPPED) {
                    AL10.alSourceStop(soundEffectSources[i]);
                    sourceInUse[i] = false;
                }
            }
        }
    }

    public void cleanup() {
        for (int i = 0; i < MAX_SOUND_SOURCES; i++) {
            AL10.alDeleteSources(soundEffectSources[i]);
        }
        soundEffects.values().forEach(AL10::alDeleteBuffers);
        LOGGER.info("Cleaned up sound effect resources.");
    }

    public void playRandomSoundEffect(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            LOGGER.warning("No sound effects provided to playRandomSoundEffect.");
            return;
        }
        Random random = new Random();
        playSoundEffect(keys.get(random.nextInt(keys.size())));
    }

    public int[] getSoundEffects() {
        return soundEffects.values().stream().mapToInt(Integer::intValue).toArray();
    }

    public float getMasterVolume() {
        return masterVolume;
    }

    public float setMasterVolume(float volume) {
        masterVolume = Math.min(1.0f, Math.max(0.0f, volume));
        return masterVolume;
    }

}
