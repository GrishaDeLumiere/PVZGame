package game.sound;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class OpenALUtils {

    private static final Logger LOGGER = Logger.getLogger(OpenALUtils.class.getName());

    public static ByteBuffer handleOggFile(File musicFile, int[] formatOut, int[] sampleRateOut) {
        try {
            byte[] oggBytes = Files.readAllBytes(musicFile.toPath());
            ByteBuffer oggBuffer = MemoryUtil.memAlloc(oggBytes.length);
            oggBuffer.put(oggBytes);
            oggBuffer.flip();
    
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer channels = stack.mallocInt(1); // Число каналов
                IntBuffer sampleRateBuffer = stack.mallocInt(1); // Частота дискретизации
                PointerBuffer output = stack.mallocPointer(1); // Указатель на декодированные данные
    
                // Декодируем OGG
                int result = STBVorbis.stb_vorbis_decode_memory(oggBuffer, channels, sampleRateBuffer, output);
                if (result == 0) {
                    LOGGER.warning("Failed to decode OGG file: " + musicFile.getName());
                    return null;
                }
    
                // Логируем параметры канала и частоты дискретизации
                LOGGER.info("Decoded OGG file. Channels: " + channels.get(0) + ", Sample rate: " + sampleRateBuffer.get(0));
    
                // Получаем декодированные PCM данные
                long pcmPointer = output.get(0); // Указатель на PCM данные
                int dataLength = result * channels.get(0); // Длина данных с учетом каналов
    
                // Создаем ShortBuffer для PCM данных
                ShortBuffer pcmData = MemoryUtil.memShortBuffer(pcmPointer, dataLength);
    
                // Устанавливаем формат для OpenAL
                int format = OpenALUtils.getOpenALFormat(channels.get(0), 16); // 16 бит на сэмпл
                int sampleRate = sampleRateBuffer.get(0); // Используем частоту из декодированного файла
    
                // Записываем данные в bufferData
                ByteBuffer bufferData = MemoryUtil.memAlloc(pcmData.remaining() * 2); // каждый сэмпл по 2 байта (16 бит)
                while (pcmData.hasRemaining()) {
                    short sample = pcmData.get();
                    bufferData.put((byte) (sample & 0xFF)); // младший байт
                    bufferData.put((byte) ((sample >> 8) & 0xFF)); // старший байт
                }
                bufferData.flip();
    
                // Возвращаем формат и частоту в параметры
                formatOut[0] = format;
                sampleRateOut[0] = sampleRate;
    
                return bufferData;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading OGG file.", e);
        }
        return null;
    }

    public static ByteBuffer handleWavFile(File musicFile, int[] format, int[] sampleRate)
            throws IOException, UnsupportedAudioFileException {
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
        AudioFormat formatAudio = audioStream.getFormat();
        byte[] audioData = IOUtils.toByteArray(audioStream);

        // Создаём ByteBuffer для хранения данных
        ByteBuffer bufferData = BufferUtils.createByteBuffer(audioData.length).put(audioData);
        bufferData.flip();

        // Устанавливаем формат и частоту дискретизации
        format[0] = OpenALUtils.getOpenALFormat(formatAudio);
        sampleRate[0] = (int) formatAudio.getSampleRate();

        return bufferData;
    }

    // Метод для получения формата OpenAL из AudioFormat (для WAV)
    public static int getOpenALFormat(AudioFormat format) {
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

        return -1; // Неподдерживаемый формат
    }

    // Метод для получения формата OpenAL из количества каналов и размера сэмпла
    // (для OGG)
    public static int getOpenALFormat(int channels, int sampleSizeInBits) {
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

        return -1; // Неподдерживаемый формат
    }

    public static String getFileExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return ""; // Если точка не найдена, возвращаем пустую строку.
        }
        return fileName.substring(dotIndex + 1).toLowerCase();
    }

}
