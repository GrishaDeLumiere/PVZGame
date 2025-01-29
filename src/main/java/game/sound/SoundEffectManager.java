package game.sound;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.apache.commons.io.IOUtils;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
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
    
                // Определяем тип файла
                String fileExtension = getFileExtension(soundFile);
                ByteBuffer bufferData = null;
                int format = -1;
                int sampleRate = 0;
    
                if (fileExtension.equalsIgnoreCase("ogg")) {
                    // Обрабатываем OGG файл с использованием stb_vorbis
                    byte[] oggBytes = Files.readAllBytes(soundFile.toPath());
                    ByteBuffer oggBuffer = MemoryUtil.memAlloc(oggBytes.length);
                    oggBuffer.put(oggBytes);
                    oggBuffer.flip();
    
                    // Создаем буферы для каналов и частоты дискретизации
                    try (MemoryStack stack = MemoryStack.stackPush()) {
                        IntBuffer channels = stack.mallocInt(1); // Число каналов
                        IntBuffer sampleRateBuffer = stack.mallocInt(1); // Частота дискретизации
                        PointerBuffer output = stack.mallocPointer(1); // Указатель на декодированные данные
    
                        // Декодируем OGG
                        int result = STBVorbis.stb_vorbis_decode_memory(oggBuffer, channels, sampleRateBuffer, output);
                        if (result == 0) {
                            LOGGER.warning("Failed to decode OGG file: " + filePath);
                            return;
                        }
    
                        // Получаем декодированные PCM данные
                        long pcmPointer = output.get(0); // Указатель на PCM данные
                        int dataLength = result * channels.get(0); // Длина данных с учетом каналов
    
                        // Создаем ShortBuffer для PCM данных
                        ShortBuffer pcmData = MemoryUtil.memShortBuffer(pcmPointer, dataLength);
    
                        // Устанавливаем формат для OpenAL
                        format = getOpenALFormat(channels.get(0), 16); // 16 бит на сэмпл
                        sampleRate = sampleRateBuffer.get(0);
    
                        // Записываем данные в bufferData
                        bufferData = MemoryUtil.memAlloc(pcmData.remaining() * 2); // каждый сэмпл по 2 байта (16 бит)
                        while (pcmData.hasRemaining()) {
                            short sample = pcmData.get();
                            bufferData.put((byte) (sample & 0xFF)); // младший байт
                            bufferData.put((byte) ((sample >> 8) & 0xFF)); // старший байт
                        }
                        bufferData.flip();
                    }
                } else if (fileExtension.equalsIgnoreCase("wav")) {
                    // Обрабатываем WAV файл
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                    AudioFormat formatAudio = audioStream.getFormat();
                    byte[] audioData = IOUtils.toByteArray(audioStream);
                    bufferData = BufferUtils.createByteBuffer(audioData.length).put(audioData);
                    bufferData.flip();
                    format = getOpenALFormat(formatAudio);
                    sampleRate = (int) formatAudio.getSampleRate();
                }
    
                if (format == -1) {
                    LOGGER.warning("Unsupported audio format for file: " + filePath);
                    return;
                }
    
                // Создаем OpenAL буфер
                int buffer = AL10.alGenBuffers();
                AL10.alBufferData(buffer, format, bufferData, sampleRate);
    
                // Сохраняем буфер в мапу
                synchronized (soundEffects) {
                    soundEffects.put(key, buffer);
                }
                LOGGER.info("Loaded sound effect: " + key);
    
            } catch (UnsupportedAudioFileException | IOException e) {
                LOGGER.log(Level.SEVERE, "Error loading sound effect: " + filePath, e);
            }
        });
    }

    private int getOpenALFormat(int channels, int sampleSizeInBits) {
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
    
        return -1;  // Неподдерживаемый формат
    }

    private String getFileExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return ""; // Если точка не найдена, возвращаем пустую строку.
        }
        return fileName.substring(dotIndex + 1).toLowerCase();
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