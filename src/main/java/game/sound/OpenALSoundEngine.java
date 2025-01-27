package game.sound;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;

import game.SettingsManager;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OpenALSoundEngine {

    private static final Logger LOGGER = Logger.getLogger(OpenALSoundEngine.class.getName());

    private static final int REFRESH_RATE = 60;
    private static final boolean ALC_SYNC_FLAG = false;

    private static String currentMusic = "";  
    private static float masterVolume;
    private static float backgroundMusicVolume;
    private static Map<String, Integer> soundEffects = new HashMap<>();
    private static long device;
    private static ALCCapabilities deviceCaps;
    private static long context;
    
    private static final int MAX_SOUND_SOURCES = 100;
    private final int[] soundEffectSources = new int[MAX_SOUND_SOURCES];
    private final boolean[] sourceInUse = new boolean[MAX_SOUND_SOURCES];

    public OpenALSoundEngine() {
        try {
            initOpenAL();
            initSoundEffectSources();
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

    private void initSoundEffectSources() {
        for (int i = 0; i < MAX_SOUND_SOURCES; i++) {
            soundEffectSources[i] = AL10.alGenSources();
            sourceInUse[i] = false;
        }
    }

    public void loadSoundEngine() {
        masterVolume = SettingsManager.getMasterVolume();
        backgroundMusicVolume = SettingsManager.getBackgroundMusicVolume();

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
                loadSoundEffect(key, filePath);
            }
        }

        LOGGER.info("Loaded sound effects: " + soundEffects.keySet());

        loadAndPlayBackgroundMusic("music/MainMenu.wav");
    }

    private static int currentMusicSource = -1;

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

        } catch (UnsupportedAudioFileException e) {
            LOGGER.log(Level.SEVERE, "Unsupported audio format: " + filePath, e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading sound file: " + filePath, e);
        }
    }

    public void playSoundEffect(String key) {
        updateSourceUsage(); // Проверяем и освобождаем завершенные источники

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

    public void stopBackgroundMusic() {
        if (currentMusicSource != -1) {
            AL10.alSourceStop(currentMusicSource);
            AL10.alDeleteSources(currentMusicSource);
            currentMusicSource = -1;
            LOGGER.info("Stopped and deleted current music source.");
        }
        currentMusic = "";
    }

    public void cleanup() {
        for (int i = 0; i < MAX_SOUND_SOURCES; i++) {
            AL10.alDeleteSources(soundEffectSources[i]);
        }

        soundEffects.values().forEach(AL10::alDeleteBuffers);
        if (device != 0) {
            ALC10.alcCloseDevice(device);
        }
        LOGGER.info("Cleaned up OpenAL resources.");
    }
    
    public void playRandomSoundEffect(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            LOGGER.warning("No sound effects provided to playRandomSoundEffect.");
            return;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(keys.size()); 
        String randomKey = keys.get(randomIndex); 

        playSoundEffect(randomKey); 
    }
    
}
