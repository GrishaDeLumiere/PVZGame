package game.sound;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.apache.commons.io.IOUtils;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import game.ThreadPoolManager;

/**
 * Класс SoundEffectManager предназначен для управления звуковыми эффектами в
 * игре.
 * Он предоставляет функции для загрузки, воспроизведения, очистки и управления
 * звуковыми эффектами,
 * а также поддерживает работу с многозадачностью через пул потоков.
 * <p>
 * Возможности:
 * <ul>
 * <li>Загрузка звуковых эффектов асинхронно через пул потоков с использованием
 * {@link ThreadPoolManager}.</li>
 * <li>Проигрывание звуковых эффектов в многозадачном режиме, освобождая главный
 * поток от обработки звука.</li>
 * <li>Поддержка динамического изменения громкости через
 * {@link #setMasterVolume(float)}.</li>
 * <li>Автоматическое обновление и очистка источников звука при их завершении
 * (остановке).</li>
 * <li>Проигрывание случайного звукового эффекта из переданного списка.</li>
 * <li>Очистка всех ресурсов звуков (источников и буферов) по завершению
 * работы.</li>
 * </ul>
 */
public class SoundEffectManager {

    private static final Logger LOGGER = Logger.getLogger(SoundEffectManager.class.getName());
    private static final int MAX_SOUND_SOURCES = 255;
    private static float masterVolume = 1.0f;

    private final Map<String, Integer> soundEffects = new HashMap<>();
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

        // Используем ThreadPoolManager для асинхронной загрузки
        ThreadPoolManager.getInstance().submitTask(() -> {
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
                synchronized (soundEffects) {
                    soundEffects.put(key, buffer);
                }
                LOGGER.info("Loaded sound effect: " + key);

            } catch (UnsupportedAudioFileException | IOException e) {
                LOGGER.log(Level.SEVERE, "Error loading sound effect: " + filePath, e);
            }
        });
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
                // Устанавливаем данные для проигрывания
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
            final int sourceIndex = i;

            if (sourceInUse[sourceIndex]) {
                int state = AL10.alGetSourcei(soundEffectSources[sourceIndex], AL10.AL_SOURCE_STATE);
                if (state == AL10.AL_STOPPED) {
                    AL10.alSourceStop(soundEffectSources[sourceIndex]);
                    sourceInUse[sourceIndex] = false;
                }
            }
        }
    }

    public void cleanup() {
        for (int i = 0; i < MAX_SOUND_SOURCES; i++) {
            AL10.alDeleteSources(soundEffectSources[i]);
        }
        synchronized (soundEffects) {
            soundEffects.values().forEach(AL10::alDeleteBuffers);
        }
        ThreadPoolManager.getInstance().shutdown();
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
        synchronized (soundEffects) {
            return soundEffects.values().stream().mapToInt(Integer::intValue).toArray();
        }
    }

    public float getMasterVolume() {
        return masterVolume;
    }

    public float setMasterVolume(float volume) {
        masterVolume = Math.min(1.0f, Math.max(0.0f, volume));
        return masterVolume;
    }
}