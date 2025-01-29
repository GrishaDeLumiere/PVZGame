package game;

import static org.lwjgl.glfw.GLFW.GLFW_DONT_CARE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F11;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F12;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowIcon;
import static org.lwjgl.glfw.GLFW.glfwSetWindowMonitor;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glViewport;

import org.lwjgl.opengl.GL; // Для работы с OpenGL
import org.lwjgl.opengl.GL11; // Для базовых функций OpenGL

import java.util.Stack;

import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.font.Color4f;
import game.font.FontManager;
import game.gui.LoadingScreen;
import game.gui.MainMenuScreen;
import game.gui.ScaleToScreen;
import game.gui.Screen;
import game.keys.MouseManager;
import game.shaders.ShaderManager;
import game.sound.OpenALManager;
import game.texture.TextureLoader;
import game.texture.TextureManager;
import game.utils.DebugUtils;
import game.utils.TimerUtils;

public class WindowManager {

    private static final Logger logger = LoggerFactory.getLogger(WindowManager.class);

    private static Stack<Screen> screenStack = new Stack<>();
    private static long windowHandle;

    private static boolean fullscreen = SettingsManager.isFullscreen();
    public static int displayWidth = SettingsManager.getWidth();
    public static int displayHeight = SettingsManager.getHeight();

    private static Screen currentScreen;
    private static MouseManager mouseManager;;

    public static void setupWindow() {
        initializeWindow();
        setupOpenGL();
        ScaleToScreen.update();
        mouseManager = new MouseManager(windowHandle);
        SetupGame();
    }

    private static void initializeWindow() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Создание окна
        windowHandle = glfwCreateWindow(1280, 800, "PvZ Plus", 0, 0);
        if (windowHandle == 0) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwMakeContextCurrent(windowHandle); // Устанавливаем текущий контекст OpenGL
        glfwSwapInterval(1); // Включаем вертикальную синхронизацию
        glfwShowWindow(windowHandle); // Показываем окно
    }

    private static void setupOpenGL() {
        GL.createCapabilities();

        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        int[] width = new int[1];
        int[] height = new int[1];
        glfwGetFramebufferSize(windowHandle, width, height);
        glViewport(0, 0, width[0], height[0]);

        glMatrixMode(GL11.GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, width[0], 0, height[0], -1, 1);
        glMatrixMode(GL11.GL_MODELVIEW);
        glLoadIdentity();

        glfwSetFramebufferSizeCallback(windowHandle, (window, newWidth, newHeight) -> {
            displayWidth = newWidth;
            displayHeight = newHeight;

            glViewport(0, 0, newWidth, newHeight);

            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            GL11.glOrtho(0, newWidth, 0, newHeight, -1, 1);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();

            ScaleToScreen.update();
        });
    }

    private static OpenALManager soundEngine;

    private static void SetupGame() {
        // Загрузка иконки
        GLFWImage.Buffer iconBuffer = TextureManager.loadIcon("assets/textures/icon.png");
        if (iconBuffer != null) {
            try {
                glfwSetWindowIcon(windowHandle, iconBuffer);
                logger.info("Window icon set successfully");
            } finally {
                iconBuffer.free();
            }
        } else {
            logger.warn("Failed to load window icon, continuing without it");
        }

        currentScreen = new LoadingScreen();
        switchFullscreenMode();

        soundEngine = new OpenALManager();

        // Основная загрузка, дабы все работало
        TimerUtils.init();
        FontManager.LoadFonts();
        TextureLoader.loadTextures();
        ShaderManager.loadAllShaders();
        DebugUtils.initVAO();

        // Запускаем процесс загрузки ресурсов
        new Thread(() -> loadAllResourcesWithProgress()).start();
        new Thread(() -> soundEngine.loadSoundEngine()).start();

        while (!glfwWindowShouldClose(WindowManager.getWindowHandle())) {
            MainThreadManager.processMainThreadQueue();
            float delta = TimerUtils.getDelta();
            TimerUtils.update();
            soundEngine.getSoundEffectManager().updateSourceUsage();

            getMouseManager().processClick();
            renderCurrentScreen();
            processKeyInput();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_TEXTURE_2D);
            glLoadIdentity();

            currentScreen.update(delta);
            currentScreen.render();
            renderFPS();

            glfwSwapBuffers(WindowManager.getWindowHandle());
            glfwPollEvents();
        }
    }

    // Загрузка всех ресурсов с отображением прогресса
    private static void loadAllResourcesWithProgress() {
        executeLoadingStep(() -> {
            TimerUtils.init();
        }, 25);
        executeLoadingStep(() -> {
            SettingsManager.loadSettings();
        }, 50);
        executeLoadingStep(() -> {

        }, 75);
        executeLoadingStep(() -> {
            Localization.load();
        }, 100);
    }

    // Выполнение одного шага загрузки с плавным обновлением прогресса
    private static void executeLoadingStep(Runnable step, int targetProgress) {
        step.run();
        updateProgressSmoothly(targetProgress);
    }

    // Обновление прогресса плавно (от текущего до целевого значения)
    private static void updateProgressSmoothly(int targetProgress) {
        Screen currentScreen = WindowManager.getCurrentScreen();

        if (currentScreen instanceof LoadingScreen) {
            LoadingScreen loadingScreen = (LoadingScreen) currentScreen;
            int currentProgress = loadingScreen.getProgress();

            while (currentProgress < targetProgress) {
                currentProgress++;
                loadingScreen.setProgress(currentProgress);

                try {
                    Thread.sleep(5); // Задержка для плавного обновления
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private static void renderFPS() {
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        FontManager.getFont("FBUSV8C5EI").drawTextWithShadow(TimerUtils.getFpsString(), (int) ScaleToScreen.get(32),
                ScaleToScreen.get(55), ScaleToScreen.getTop(40), 0, new Color4f("#FFFFFF"), 0.1f, 0.1f, 0, true, 0.1f,
                0.1f, new Color4f("#000000"));
        GL11.glPopMatrix();
    }

    public static MouseManager getMouseManager() {
        return mouseManager;
    }

    private static boolean isKeyPressed = false;
    private static boolean isDebugKeyPressed = false;

    private static void processKeyInput() {
        // Обработка переключения DebugMode (F12)
        if (glfwGetKey(windowHandle, GLFW_KEY_F12) == GLFW_PRESS && !isDebugKeyPressed) {
            DebugManager.setDebugMode(!DebugManager.isDebugMode()); // Переключаем режим
            isDebugKeyPressed = true;
            System.out.println("Debug mode: " + DebugManager.isDebugMode());
        }

        if (glfwGetKey(windowHandle, GLFW_KEY_F12) == GLFW_RELEASE) {
            isDebugKeyPressed = false; // Сбрасываем состояние клавиши
        }

        // Обработка других клавиш (пример: F11 для полноэкранного режима)
        if (glfwGetKey(windowHandle, GLFW_KEY_F11) == GLFW_PRESS && !isKeyPressed) {
            toggleFullscreen();
            isKeyPressed = true;
        }

        if (glfwGetKey(windowHandle, GLFW_KEY_F11) == GLFW_RELEASE && isKeyPressed) {
            isKeyPressed = false;
        }

        // Вызов метода обработки текущего экрана
        currentScreen.input(isKeyPressed);
    }

    private static void switchFullscreenMode() {
        fullscreen = SettingsManager.isFullscreen();
        long monitor = glfwGetPrimaryMonitor();

        if (monitor == 0) {
            System.out.println("Не удается получить основной монитор.");
            return;
        }

        GLFWVidMode videoMode = glfwGetVideoMode(monitor);

        // Проверка на null перед доступом к свойствам
        if (videoMode == null) {
            System.out.println("Не удается получить видеорежим для монитора.");
            return;
        }

        int screenWidth = videoMode.width();
        int screenHeight = videoMode.height();
        int refreshRate = videoMode.refreshRate();

        if (fullscreen) {
            glfwSetWindowMonitor(windowHandle, monitor, 0, 0, screenWidth, screenHeight, refreshRate);
        } else {
            glfwSetWindowMonitor(windowHandle, 0, 100, 100, displayWidth, displayHeight, GLFW_DONT_CARE);
        }
    }

    public static void toggleFullscreen() {
        fullscreen = !fullscreen;
        SettingsManager.setFullscreen(fullscreen);

        long monitor = glfwGetPrimaryMonitor();
        if (monitor == 0) {
            System.out.println("Не удается получить основной монитор.");
            return;
        }

        GLFWVidMode videoMode = glfwGetVideoMode(monitor);

        // Проверка на null перед доступом к свойствам
        if (videoMode == null) {
            System.out.println("Не удается получить видеорежим для монитора.");
            return;
        }

        int screenWidth = videoMode.width();
        int screenHeight = videoMode.height();
        int refreshRate = videoMode.refreshRate();

        if (fullscreen) {
            glfwSetWindowMonitor(windowHandle, monitor, 0, 0, screenWidth, screenHeight, refreshRate);
        } else {
            glfwSetWindowMonitor(windowHandle, 0, 100, 100, displayWidth, displayHeight, GLFW_DONT_CARE);
        }
    }

    public static long getWindowHandle() {
        return windowHandle;
    }

    public static void cleanup() {
        glfwDestroyWindow(windowHandle);
        glfwTerminate();
    }

    public static Screen getCurrentScreen() {
        return currentScreen;
    }

    public static void exitGame() {
        glfwSetWindowShouldClose(windowHandle, true);
        SettingsManager.setWidth(displayWidth);
        SettingsManager.setHeight(displayHeight);
    }

    public static void SwitchToScreen(Screen newScreen) {
        if (currentScreen != null) {
            currentScreen.cleanup();
        }
        currentScreen = newScreen;
    }

    public static void OpenOverlayScreen(Screen overlayScreen) {
        screenStack.push(currentScreen);
        currentScreen = overlayScreen;
    }

    public static void CloseOverlayScreen() {
        if (!screenStack.isEmpty()) {
            currentScreen.cleanup();
            currentScreen = screenStack.pop();
        } else {
            SwitchToScreen(new MainMenuScreen());
        }
    }

    public static void renderCurrentScreen() {
        if (currentScreen != null) {
            currentScreen.render(); // Отрисовываем основной экран

            // Если в стеке есть оверлей, то рисуем его поверх
            if (!screenStack.isEmpty()) {
                screenStack.peek().render(); // Рендерим последний оверлей
            }
        }
    }

    public static void inputCurrentScreen(boolean isKeyPressed) {
        if (currentScreen != null) {
            currentScreen.input(isKeyPressed);
        }
    }

    public static OpenALManager getAL() {
        return soundEngine;
    }

}