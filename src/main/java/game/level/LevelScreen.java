package game.level;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.lwjgl.opengl.GL11;

import game.DebugManager;
import game.Localization;
import game.WindowManager;
import game.entity.Entity;
import game.font.Color4f;
import game.font.FontManager;
import game.gui.AbstractScreen;
import game.gui.AlmanacScreen;
import game.gui.MainMenuScreen;
import game.gui.ScaleToScreen;
import game.keys.MouseManager;
import game.level.wave.WaveSystem;
import game.texture.TextureManager;
import game.utils.AnimatedObject;
import game.utils.DebugUtils;
import game.utils.TimerUtils;

public class LevelScreen extends AbstractScreen {

	private CardsManager cardsmanager;
	private Timer animationTimer;
	private Starter starter;
	private List<AnimatedObject> animatedObjects = new ArrayList<>();
	private boolean animationStarted = false;

	public LevelScreen() {
		starter = new Starter();
		cardsmanager = new CardsManager(starter);
		starter.init();

		TextureManager.loadTexture("ButtonParam", "textures/setting/ButtonParam.png");
		TextureManager.loadTexture("ButtonParam1", "textures/setting/ButtonParam1.png");

		TextureManager.loadTexture("SeedBank", "textures/HotBar/SeedBank.png");
		TextureManager.loadTexture("SeedChooser_Background", "textures/HotBar/SeedChooser_Background.png");
		TextureManager.loadTexture("SeedPacketSilhouette", "textures/HotBar/SeedPacketSilhouette.png");

		TextureManager.loadTexture("SeedChooser_Button2", "textures/setting/SeedChooser_Button2.png");
		TextureManager.loadTexture("SeedChooser_Button2_Glow", "textures/setting/SeedChooser_Button2_Glow.png");

		TextureManager.loadTexture("SeedChooser_Button", "textures/HotBar/SeedChooser_Button.png");
		TextureManager.loadTexture("SeedChooser_Button_Disabled", "textures/HotBar/SeedChooser_Button_Disabled.png");

		TextureManager.loadTexture("SeedPacket_Larger", "textures/HotBar/SeedPacket_Larger.png");
		TextureManager.loadTexture("SeedPacket_Night", "textures/HotBar/SeedPacket_Night.png");
		TextureManager.loadTexture("SeedPacket_Other", "textures/HotBar/SeedPacket_Other.png");
		TextureManager.loadTexture("SeedPacket_Pool", "textures/HotBar/SeedPacket_Pool.png");
		TextureManager.loadTexture("cooldown", "textures/HotBar/cooldown.png");

		TextureManager.loadTexture("bar", "textures/Level/bar.png");
		TextureManager.loadTexture("bar_big", "textures/Level/bar_big.png");
		TextureManager.loadTexture("progress", "textures/Level/progress.png");
		TextureManager.loadTexture("flag", "textures/Level/flag.png");
		TextureManager.loadTexture("FlagMeterZombie", "textures/Level/FlagMeterZombie.png");

		SwingUtilities.invokeLater(() -> {

			animatedObjects.add(new AnimatedObject(1000, 0, 4, false)); // Вернхяя
			animatedObjects.add(new AnimatedObject(-1550, 0, 4, true)); // Нижняя панел

			startAnimations();
		});
		System.out.println("[NEW LEVEL STARTED]");
	}

	private void startAnimations() {
		if (starter.getCamera().isCameraStopped() && !animationStarted) {
			animationStarted = true;
			animationTimer = new Timer(16, e -> {
				boolean needsUpdate = false;

				for (AnimatedObject obj : animatedObjects) {
					needsUpdate |= obj.animate();
				}

				if (!needsUpdate) {
					animationTimer.stop();
				}
			});
			animationTimer.start();
		}
	}

	@Override
	public void render() {
		super.render();

		if (animatedObjects.isEmpty()) {
			return;
		}

		starter.render();
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		Hotbar();
		starter.getLevel().waveSystem.render();
		starter.getLevel().showSunRender();
		cardsmanager.render();
		GL11.glPopMatrix();

		if (DebugManager.isDebugMode()) {
			DebugUtils.renderHitboxes();
		}

		starter.getLevel().getTextAnimation().render();

	}

	private void Hotbar() {
		TextureManager.drawTexture("SeedBank",
				ScaleToScreen.getCenterX(320),
				ScaleToScreen.getBot(940 + animatedObjects.get(0).currentY),
				ScaleToScreen.get(1320), ScaleToScreen.get(140));
		renderGrid(ScaleToScreen.getCenterX(442), ScaleToScreen.getBot(945 + animatedObjects.get(0).currentY), 1, 13);

		FontManager.getFont("FBUSV8C5EI").drawText(
				"" + starter.getLevel().getSunCount(),
				(int) ScaleToScreen.get(32),
				ScaleToScreen.getCenterX(382),
				ScaleToScreen.getBot(982) + animatedObjects.get(0).currentY,
				0,
				new Color4f("#000000"),
				0.1f,
				0.1f,
				0,
				true);

		cardsmanager.renderSelectedPlants();

		if (starter.getStartLevel() == false) {
			drawButtonWithText(0, "ButtonParam", "ButtonParam1", Localization.get("Level.button.menu"), "FBUSV8C5EI",
					ScaleToScreen.getCenterX(1655), ScaleToScreen.getBot(1000), ScaleToScreen.get(250),
					ScaleToScreen.get(75), new Color4f("#50c878"), 32);

			TextureManager.drawTexture("SeedChooser_Background",
					ScaleToScreen.getCenterX(325),
					ScaleToScreen.getBot(animatedObjects.get(1).currentY),
					ScaleToScreen.get(860), ScaleToScreen.get(940));

			startAnimations();
			FontManager.getFont("FBUSV8C5EI").drawTextWithShadow(
					Localization.get("Level.plantselect"),
					(int) ScaleToScreen.get(38),
					ScaleToScreen.getCenterX(735),
					ScaleToScreen.getBot(934) + animatedObjects.get(1).currentY,
					0,
					new Color4f("#ffa500"),
					0.1f,
					0.1f,
					0,
					true,
					0.1f,
					0.1f,
					new Color4f("#000000"));

			renderGrid(ScaleToScreen.getCenterX(349), ScaleToScreen.getBot(119 + animatedObjects.get(1).currentY), 6,
					9);

			drawButtonWithText(2, "SeedChooser_Button", "SeedChooser_Button_Disabled", Localization.get("Level.start"),
					"FBUSV8C5EI",
					ScaleToScreen.getCenterX(610), ScaleToScreen.getBot(45 + animatedObjects.get(1).currentY),
					ScaleToScreen.get(250), ScaleToScreen.get(65), new Color4f("#ffa500"), 30);

			drawButtonWithText(1, "SeedChooser_Button2", "SeedChooser_Button2_Glow", Localization.get("Level.almanac"),
					"BRIANNETOD",
					ScaleToScreen.getCenterX(385), ScaleToScreen.getBot(55 + animatedObjects.get(1).currentY),
					ScaleToScreen.get(210), ScaleToScreen.get(45), new Color4f("#353298"), 28, 5, 5);

			cardsmanager.renderPlantList(animatedObjects.get(1).currentY);
		} else {
			LevelProgress();
			if (starter.getLevel().getStartGame() == true) {
				// startLevel();
			}
		}
	}

	private void LevelProgress() {
		float progress = starter.getLevel().waveSystem.getProgress(); // Значение от 0 до 1
		int totalWaves = starter.getLevel().waveSystem.getTotalWaves(); // Общее количество волн
		int currentWave = starter.getLevel().waveSystem.getCurrentWave(); // Текущая волна

		GL11.glEnable(GL11.GL_SCISSOR_TEST); // Включаем обрезку

		// Устанавливаем область обрезки с учетом масштаба
		GL11.glScissor(
				(int) ScaleToScreen.getStretchedWidth(1215), // Начальная координата X (сдвиг немного)
				(int) ScaleToScreen.getStretchedHeight(7),
				(int) ScaleToScreen.getStretchedWidth(650), // Ширина обрезаемой области
				(int) ScaleToScreen.getStretchedHeight(70) // Высота обрезаемой области
		);

		if (totalWaves < 15) {
			TextureManager.drawTexture("bar", ScaleToScreen.getStretchedWidth(1515), 0,
					ScaleToScreen.getStretchedWidth(290), ScaleToScreen.getStretchedHeight(40));

			// Рассчитываем progress для обратного направления
			float progressInCurrentWave = (progress + (currentWave - 0)) / totalWaves;
			int progressBarWidth = (int) (progressInCurrentWave * 270);

			// Для того чтобы прогресс шел справа налево, рассчитываем его позицию справа
			int progressBarX = 1525 + (270 - progressBarWidth);

			// Теперь рисуем прогресс с правильной шириной и позиционированием
			float textureX = 0;
			float textureWidth = (float) progressBarWidth / 290;
			TextureManager.drawSubTexture("progress", ScaleToScreen.getStretchedWidth(progressBarX),
					ScaleToScreen.getStretchedHeight(15), ScaleToScreen.getStretchedWidth(progressBarWidth),
					ScaleToScreen.getStretchedHeight(15), ScaleToScreen.getStretchedWidth(textureX), 0,
					ScaleToScreen.getStretchedWidth(textureWidth), 1);

			for (int i = 0; i < totalWaves; i++) {
				// Рассчитываем X-координату флага справа налево
				int flagX = 1525 + ((totalWaves - 1 - i) * 270 / totalWaves);

				// Если флаг относится к текущей волне или предыдущим, он поднимается
				int flagY = (i + 1 <= currentWave) ? 6 : -8;

				// Рисуем флаг
				TextureManager.drawTexture("flag", ScaleToScreen.getStretchedWidth(flagX),
						ScaleToScreen.getStretchedHeight(flagY), ScaleToScreen.getStretchedWidth(26),
						ScaleToScreen.getStretchedHeight(56));
			}
		} else {

			TextureManager.drawTexture("bar_big", ScaleToScreen.getStretchedWidth(1285), 0,
					ScaleToScreen.getStretchedWidth(520), ScaleToScreen.getStretchedHeight(40));

			// Рассчитываем progress для обратного направления
			float progressInCurrentWave = (progress + (currentWave - 0)) / totalWaves;
			int progressBarWidth = (int) (progressInCurrentWave * 500);

			// Для того чтобы прогресс шел справа налево, рассчитываем его позицию справа
			int progressBarX = 1295 + (500 - progressBarWidth);

			// Теперь рисуем прогресс с правильной шириной и позиционированием
			float textureX = 0;
			float textureWidth = (float) progressBarWidth / 500;
			TextureManager.drawSubTexture("progress", ScaleToScreen.getStretchedWidth(progressBarX),
					ScaleToScreen.getStretchedHeight(15), ScaleToScreen.getStretchedWidth(progressBarWidth),
					ScaleToScreen.getStretchedHeight(15), ScaleToScreen.getStretchedWidth(textureX), 0,
					ScaleToScreen.getStretchedWidth(textureWidth), 1);

			for (int i = 0; i < totalWaves; i++) {
				// Рассчитываем X-координату флага справа налево
				int flagX = 1295 + ((totalWaves - 1 - i) * 500 / totalWaves);

				// Если флаг относится к текущей волне или предыдущим, он поднимается
				int flagY = (i + 1 <= currentWave) ? 6 : -8;

				// Рисуем флаг
				TextureManager.drawTexture("flag", ScaleToScreen.getStretchedWidth(flagX),
						ScaleToScreen.getStretchedHeight(flagY), ScaleToScreen.getStretchedWidth(26),
						ScaleToScreen.getStretchedHeight(56));
			}

		}

		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		if (totalWaves < 15) {
			FontManager.getFont("FBUSV8C5EI").drawTextAlignedLeftOf(
				Localization.get(starter.getLevel().levelName),// Текст для отрисовки
				25,                                     // Размер текста
				ScaleToScreen.getStretchedWidth(1500),  // Координата правого края
				ScaleToScreen.getStretchedHeight(38),   // Координата Y
				0,                                      // Координата Z
				new Color4f("#D4B870"),                 // Цвет текста
				0.1f,                                   // Поворот по X
				0.1f,                                   // Поворот по Y
				0,                                      // Поворот по Z
				false,                                  // Центрирование отключено
				1.25f,                                  // Размер обводки
				new Color4f("#000000")                  // Цвет обводки
			);
		} else {
			FontManager.getFont("FBUSV8C5EI").drawTextAlignedLeftOf(
				Localization.get(starter.getLevel().levelName),// Текст для отрисовки
				25,                                     // Размер текста
				ScaleToScreen.getStretchedWidth(1275),  // Координата правого края
				ScaleToScreen.getStretchedHeight(38),   // Координата Y
				0,                                      // Координата Z
				new Color4f("#D4B870"),                 // Цвет текста
				0.1f,                                   // Поворот по X
				0.1f,                                   // Поворот по Y
				0,                                      // Поворот по Z
				false,                                  // Центрирование отключено
				1.25f,                                  // Размер обводки
				new Color4f("#000000")                  // Цвет обводки
			);
		}
		List<Entity> entities = WaveSystem.getEntities();
		long zombieCount = 0;

		// Подсчёт зомби
		for (Entity entity : entities) {
			if (entity instanceof Entity) {
				zombieCount++;
			}
		}

		// Рисуем текст количества зомби
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		FontManager.getFont("FBUSV8C5EI").drawTextWithShadow(
				Localization.get("level.zombies") + ": " + zombieCount, // Текст с количеством зомби
				(int) ScaleToScreen.get(32),
				ScaleToScreen.get(160),
				ScaleToScreen.getTop(40),
				0,
				new Color4f("#FFFFFF"),
				0.1f, 0.1f, 0, true, 0.1f, 0.1f, new Color4f("#000000"));

		// Если ускорение времени больше 1x
		if (TimerUtils.getTimeScale() > 1.0f) {
			FontManager.getFont("FBUSV8C5EI").drawTextWithShadow(
					"" + TimerUtils.getTimeScale() + "x", // Дополнительный текст
					(int) ScaleToScreen.get(32),
					ScaleToScreen.get(160) + 80, // Рисуем рядом с текстом зомби
					ScaleToScreen.getTop(40),
					0,
					new Color4f("#FFFFFF"),
					0.1f, 0.1f, 0, true, 0.1f, 0.1f, new Color4f("#000000"));
		}
		// Если время замедлено (меньше 1x)
		else if (TimerUtils.getTimeScale() < 1.0f) {
			FontManager.getFont("FBUSV8C5EI").drawTextWithShadow(
					"" + TimerUtils.getTimeScale() + "x", // Дополнительный текст
					(int) ScaleToScreen.get(32),
					ScaleToScreen.get(160) + 80, // Рисуем рядом с текстом зомби
					ScaleToScreen.getTop(40),
					0,
					new Color4f("#FFFFFF"),
					0.1f, 0.1f, 0, true, 0.1f, 0.1f, new Color4f("#000000"));
		}
		GL11.glPopMatrix();

	}

	public void renderGrid(float x, float y, int rows, int cols) {
		float offsetX = ScaleToScreen.get(90); // Размеры ячеек по оси X
		float offsetY = ScaleToScreen.get(125); // Размеры ячеек по оси Y

		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				float posX = x + col * offsetX;
				float posY = y + row * offsetY;

				// Отрисовываем объект
				TextureManager.drawTexture("SeedPacketSilhouette",
						posX,
						posY,
						offsetX, offsetY);
			}
		}
	}

	@Override
	public void cleanup() {
	}

	@Override
	public void update(float delta) {
		super.update(delta);
		MouseManager mouseManager = WindowManager.getMouseManager();
		starter.update(delta);
		if (mouseManager.isLeftMousePressed()) {
			starter.getLevel().onMouseClick((float) mouseManager.getMouseX(), (float) mouseManager.getMouseY());
		}
		cardsmanager.update(delta);
		starter.getLevel().getTextAnimation().updateAnimation(delta);
	}

	private boolean isEscapePressed = false;
	private boolean isKeyCPressed = false;
	private boolean isKeyXPressed = false;

	@Override
	public void input(boolean isKeyPressed) {
		cardsmanager.input(isKeyPressed);

		// Обработка клавиши Escape
		int keyEscape = 256;
		if (glfwGetKey(WindowManager.getWindowHandle(), keyEscape) == GLFW_PRESS && !isEscapePressed) {
			WindowManager.SwitchToScreen(new MainMenuScreen());
			isEscapePressed = true;
		} else if (glfwGetKey(WindowManager.getWindowHandle(), keyEscape) == GLFW_RELEASE) {
			isEscapePressed = false;
		}

		// Обработка клавиши "C" для ускорения времени
		int keySpeedUp = GLFW_KEY_C;
		if (glfwGetKey(WindowManager.getWindowHandle(), keySpeedUp) == GLFW_PRESS && !isKeyCPressed) {
			// Переключаем между ускорением и нормальной скоростью
			if (TimerUtils.getTimeScale() == 1.0f) {
				TimerUtils.setTimeScale(2.0f); // Ускоряем время
			} else {
				TimerUtils.setTimeScale(1.0f); // Возвращаем нормальную скорость
			}
			isKeyCPressed = true;
		} else if (glfwGetKey(WindowManager.getWindowHandle(), keySpeedUp) == GLFW_RELEASE) {
			isKeyCPressed = false;
		}

		// Обработка клавиши "X" для замедления времени
		int keySlowDown = GLFW_KEY_X;
		if (glfwGetKey(WindowManager.getWindowHandle(), keySlowDown) == GLFW_PRESS && !isKeyXPressed) {
			// Устанавливаем замедление времени
			if (TimerUtils.getTimeScale() == 1.0f) {
				TimerUtils.setTimeScale(0.5f); // Замедляем время
			} else {
				TimerUtils.setTimeScale(1.0f); // Возвращаем нормальную скорость
			}
			isKeyXPressed = true;
		} else if (glfwGetKey(WindowManager.getWindowHandle(), keySlowDown) == GLFW_RELEASE) {
			isKeyXPressed = false;
		}
	}

	@Override
	public void onButtonClick(int id) {
		cardsmanager.onButtonClick(id);

		switch (id) {
			case 0: // Меню
				WindowManager.getAL().getSoundEffectManager().playSoundEffect("gravebutton");
				WindowManager.SwitchToScreen(new MainMenuScreen());
				break;
			case 1: // Альманах
				WindowManager.getAL().getSoundEffectManager().playSoundEffect("tap2");
				WindowManager.OpenOverlayScreen(new AlmanacScreen());
				break;

			case 2: // Старт игры
				WindowManager.getAL().getSoundEffectManager().playSoundEffect("tap2");
				starter.endPlantSelection();
				break;
			default:
				break;
		}
	}
}