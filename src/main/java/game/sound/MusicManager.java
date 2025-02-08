package game.sound;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import game.ThreadPoolManager;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Класс MusicManager управляет воспроизведением фоновой музыки в игре с
 * использованием OpenAL.
 * Он позволяет загружать, воспроизводить и останавливать фоновую музыку, а
 * также настраивать уровень громкости музыки.
 * <p>
 * Основные возможности:
 * <ul>
 * <li>Загрузка и воспроизведение фоновой музыки с заданного пути, поддерживая
 * форматы WAV.</li>
 * <li>Установка громкости фоновой музыки (через поле
 * {@link #backgroundMusicVolume}).</li>
 * <li>Остановка текущей фоновой музыки и освобождение используемых
 * ресурсов.</li>
 * <li>Предотвращение повторного воспроизведения той же самой музыки.</li>
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

        ThreadPoolManager.getInstance().submitTask(() -> {
            try {
                File musicFile = new File("assets/" + filePath);
                if (!musicFile.exists()) {
                    LOGGER.warning("Music file not found: " + musicFile.getPath());
                    return;
                }

                // Определяем тип файла и обрабатываем его
                String fileExtension = OpenALUtils.getFileExtension(musicFile);
                ByteBuffer bufferData = null;
                int format = -1;
                int sampleRate = 0;

                if (fileExtension.equalsIgnoreCase("ogg")) {
                    int[] formatOut = new int[1];
                    int[] sampleRateOut = new int[1];
                    bufferData = OpenALUtils.handleOggFile(musicFile, formatOut, sampleRateOut);
                    format = formatOut[0];
                    sampleRate = sampleRateOut[0];
                } else if (fileExtension.equalsIgnoreCase("wav")) {
                    int[] formatArr = new int[1];
                    int[] sampleRateArr = new int[1];
                    bufferData = OpenALUtils.handleWavFile(musicFile, formatArr, sampleRateArr);
                    format = formatArr[0];
                    sampleRate = sampleRateArr[0];
                }

                if (format == -1) {
                    LOGGER.warning("Unsupported audio format for file: " + filePath);
                    return;
                }

                // Создаем OpenAL буфер и источники
                createAndPlayMusic(bufferData, format, sampleRate, filePath);

            } catch (IOException | UnsupportedAudioFileException e) {
                LOGGER.log(Level.SEVERE, "Error loading music file: " + filePath, e);
            }
        });
    }

    private void createAndPlayMusic(ByteBuffer bufferData, int format, int sampleRate, String filePath) {
        int buffer = AL10.alGenBuffers();
        AL10.alBufferData(buffer, format, bufferData, sampleRate);

        currentMusicSource = AL10.alGenSources();
        AL10.alSourcei(currentMusicSource, AL10.AL_BUFFER, buffer);
        AL10.alSourcef(currentMusicSource, AL10.AL_GAIN, backgroundMusicVolume);
        AL10.alSourcei(currentMusicSource, AL10.AL_LOOPING, AL10.AL_TRUE);

        AL10.alSourcePlay(currentMusicSource);
        currentMusic = filePath;

        LOGGER.info("Playing background music: " + filePath);
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