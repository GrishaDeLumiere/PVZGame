package game.sound;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Класс MusicManager управляет воспроизведением фоновой музыки в игре с использованием OpenAL.
 * Он позволяет загружать, воспроизводить и останавливать фоновую музыку, а также настраивать уровень громкости музыки.
 * <p>
 * Основные возможности:
 * <ul>
 *     <li>Загрузка и воспроизведение фоновой музыки с заданного пути, поддерживая форматы WAV.</li>
 *     <li>Установка громкости фоновой музыки (через поле {@link #backgroundMusicVolume}).</li>
 *     <li>Остановка текущей фоновой музыки и освобождение используемых ресурсов.</li>
 *     <li>Предотвращение повторного воспроизведения той же самой музыки.</li>
 * </ul>
 * <p>
 * Важные методы:
 * <ul>
 * <li>{@link #loadAndPlayBackgroundMusic(String)}: Загружает и начинает
 * воспроизведение музыки из файла. Если музыка уже воспроизводится, она не
 * будет загружена повторно.</li>
 * <li>{@link #stopBackgroundMusic()}: Останавливает воспроизведение текущей
 * фоновой музыки и освобождает ресурсы.</li>
 * <li>{@link #cleanup()}: Освобождает все ресурсы, связанные с музыкой, и
 * останавливает воспроизведение.</li>
 * </ul>
 * <p>
 */
public class MusicManager {

    private static final Logger LOGGER = Logger.getLogger(MusicManager.class.getName());
    private int currentMusicSource = -1;
    float backgroundMusicVolume = 1.0f;
    private static String currentMusic = "";

    public void loadAndPlayBackgroundMusic(String filePath) {
        if (backgroundMusicVolume == 0.0f) {
            LOGGER.info("Background music volume is set to 0. Skipping playback.");
            return;
        }

        if (filePath.equals(currentMusic)) {
            LOGGER.info("The same music is already playing: " + filePath);
            return;
        }

        stopBackgroundMusic();

        try {
            File musicFile = new File("assets/" + filePath);
            if (!musicFile.exists()) {
                LOGGER.warning("Music file not found: " + musicFile.getPath());
                return;
            }

            // Определяем тип файла
            String fileExtension = getFileExtension(musicFile);
            ByteBuffer bufferData = null;
            int format = -1;
            int sampleRate = 0;

            if (fileExtension.equalsIgnoreCase("ogg")) {
                // Обрабатываем OGG файл с использованием stb_vorbis
                byte[] oggBytes = Files.readAllBytes(musicFile.toPath());
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
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
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

            currentMusicSource = AL10.alGenSources();
            AL10.alSourcei(currentMusicSource, AL10.AL_BUFFER, buffer);
            AL10.alSourcef(currentMusicSource, AL10.AL_GAIN, backgroundMusicVolume);
            AL10.alSourcei(currentMusicSource, AL10.AL_LOOPING, AL10.AL_TRUE);

            AL10.alSourcePlay(currentMusicSource);
            currentMusic = filePath;

            LOGGER.info("Playing background music: " + filePath);

        } catch (UnsupportedAudioFileException e) {
            LOGGER.log(Level.SEVERE, "Unsupported audio format: " + filePath, e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading music file: " + filePath, e);
        }
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

    public void stopBackgroundMusic() {
        if (currentMusicSource != -1) {
            AL10.alSourceStop(currentMusicSource);
            AL10.alDeleteSources(currentMusicSource);
            currentMusicSource = -1;
        }
    }

    public void cleanup() {
        stopBackgroundMusic();
    }
}