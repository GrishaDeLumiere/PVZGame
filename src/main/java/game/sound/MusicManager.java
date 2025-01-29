package game.sound;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

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

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
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