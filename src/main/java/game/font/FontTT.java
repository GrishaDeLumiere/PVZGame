package game.font;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

/**
 * @author Jeremy Adams (elias4444)
 * 
 *         Этот модуль использует модули Texture и TextureLoader
 *         для загрузки и хранения информации о текстурах. Самое
 *         сложное, что нужно знать об этих классах, это то, что TextureLoader
 *         принимает BufferedImage и преобразует его в текстуру. Если
 *         изображение
 *         не является "степенью двойки" (power of 2), TextureLoader делает его
 *         степенью двойки и устанавливает соответствующие текстурные
 *         координаты.
 *
 *         Изменено Grisha: добавлена поддержка альфа-канала для работы с
 *         прозрачностью и поддержка русских букв.
 */
public class FontTT {

	private Texture[] charactersp, characterso;
	private HashMap<String, IntObject> charlistp = new HashMap<String, IntObject>();
	private HashMap<String, IntObject> charlisto = new HashMap<String, IntObject>();
	private TextureLoader textureloader;
	private int kerneling;
	private int fontsize = 32;
	private Font font;

	/*
	 * Need a special class to hold character information in the hasmaps
	 */
	private class IntObject {
		public int charnum;

		IntObject(int charnumpass) {
			charnum = charnumpass;
		}
	}

	/*
	 * Pass in the preloaded truetype font, the resolution at which
	 * you wish the initial texture to be rendered at, and any extra
	 * kerneling you want inbetween characters
	 */
	public FontTT(Font font, int fontresolution, int extrakerneling) {
		textureloader = new TextureLoader();
		this.kerneling = extrakerneling;
		this.font = font;
		fontsize = fontresolution;

		createPlainSet();
		createOutlineSet();
	}

	/*
	 * Create a standard Java2D bufferedimage to later be transferred into a texture
	 */
	private BufferedImage getFontImage(char ch) {
		Font tempfont;
		tempfont = font.deriveFont((float) fontsize);
		// Create a temporary image to extract font size
		BufferedImage tempfontImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) tempfontImage.getGraphics();
		//// Add AntiAliasing /////
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		///////////////////////////
		g.setFont(tempfont);
		FontMetrics fm = g.getFontMetrics();
		int charwidth = fm.charWidth(ch);

		if (charwidth <= 0) {
			charwidth = 1;
		}
		int charheight = fm.getHeight();
		if (charheight <= 0) {
			charheight = fontsize;
		}

		// Create another image for texture creation
		BufferedImage fontImage;
		fontImage = new BufferedImage(charwidth, charheight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gt = (Graphics2D) fontImage.getGraphics();
		//// Add AntiAliasing /////
		gt.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		///////////////////////////
		gt.setFont(tempfont);

		//// Uncomment these to fill in the texture with a background color
		//// (used for debugging)
		// gt.setColor(Color.RED);
		// gt.fillRect(0, 0, charwidth, fontsize);

		gt.setColor(new java.awt.Color(Color4f.WHITE.getRed(), Color4f.WHITE.getGreen(), Color4f.WHITE.getBlue(),
				Color4f.WHITE.getAlpha()));
		int charx = 0;
		int chary = 0;
		gt.drawString(String.valueOf(ch), (charx), (chary) + fm.getAscent());

		return fontImage;

	}

	/*
	 * Create a standard Java2D bufferedimage for the font outline to later be
	 * converted into a texture
	 */
	private BufferedImage getOutlineFontImage(char ch) {
		Font tempfont;
		tempfont = font.deriveFont((float) fontsize);

		// Create a temporary image to extract font size
		BufferedImage tempfontImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) tempfontImage.getGraphics();
		//// Add AntiAliasing /////
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		///////////////////////////
		g.setFont(tempfont);
		FontMetrics fm = g.getFontMetrics();
		int charwidth = fm.charWidth(ch);

		if (charwidth <= 0) {
			charwidth = 1;
		}
		int charheight = fm.getHeight();
		if (charheight <= 0) {
			charheight = fontsize;
		}

		// Create another image for texture creation
		int ot = (int) ((float) fontsize / 24f);

		BufferedImage fontImage;
		fontImage = new BufferedImage(charwidth + 4 * ot, charheight + 4 * ot, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gt = (Graphics2D) fontImage.getGraphics();
		//// Add AntiAliasing /////
		gt.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		///////////////////////////
		gt.setFont(tempfont);

		//// Uncomment these to fill in the texture with a background color
		//// (used for debugging)
		// gt.setColor(Color.RED);
		// gt.fillRect(0, 0, charwidth, fontsize);

		//// Create Outline by painting the character in multiple positions and blurring
		//// it
		gt.setColor(new java.awt.Color(Color4f.WHITE.getRed(), Color4f.WHITE.getGreen(), Color4f.WHITE.getBlue(),
				Color4f.WHITE.getAlpha()));
		int charx = -fm.getLeading() + 2 * ot;
		int chary = 2 * ot;
		gt.drawString(String.valueOf(ch), (charx) + ot, (chary) + fm.getAscent());
		gt.drawString(String.valueOf(ch), (charx) - ot, (chary) + fm.getAscent());
		gt.drawString(String.valueOf(ch), (charx), (chary) + ot + fm.getAscent());
		gt.drawString(String.valueOf(ch), (charx), (chary) - ot + fm.getAscent());
		gt.drawString(String.valueOf(ch), (charx) + ot, (chary) + ot + fm.getAscent());
		gt.drawString(String.valueOf(ch), (charx) + ot, (chary) - ot + fm.getAscent());
		gt.drawString(String.valueOf(ch), (charx) - ot, (chary) + ot + fm.getAscent());
		gt.drawString(String.valueOf(ch), (charx) - ot, (chary) - ot + fm.getAscent());

		float ninth = 1.0f / 9.0f;
		float[] blurKernel = {
				ninth, ninth, ninth,
				ninth, ninth, ninth,
				ninth, ninth, ninth
		};
		BufferedImageOp blur = new ConvolveOp(new Kernel(3, 3, blurKernel));

		BufferedImage returnimage = blur.filter(fontImage, null);

		return returnimage;

	}

	/*
	 * Create and store the plain (non-outlined) set of the given fonts
	 */
	private void createPlainSet() {
		charactersp = new Texture[65536]; // Массив для хранения текстур каждого символа

		try {
			// Загружаем английские символы (A-Z, a-z) и знаки
			for (int i = 32; i <= 126; i++) {
				loadCharacter(charactersp, charlistp, i, false);
			}

			// Загружаем символы кириллицы (А-Я, а-я)
			for (int i = 1040; i <= 1103; i++) {
				loadCharacter(charactersp, charlistp, i, false);
			}

			// Добавляем символы Ё (U+0401) и ё (U+0451)
			loadCharacter(charactersp, charlistp, 1025, false); // Ё
			loadCharacter(charactersp, charlistp, 1105, false); // ё

			loadCharacter(charactersp, charlistp, 32, false);

		} catch (IOException e) {
			System.out.println("FAILED to load plain set!!!");
			e.printStackTrace();
		}
	}

	/*
	 * Create and store the outlined set of the given fonts
	 */
	private void createOutlineSet() {
		characterso = new Texture[65536]; // Массив для хранения текстур каждого символа с контуром

		try {
			// Загружаем английские символы (A-Z, a-z) и знаки
			for (int i = 32; i <= 126; i++) {
				loadCharacter(characterso, charlisto, i, true);
			}

			// Загружаем символы кириллицы (А-Я, а-я)
			for (int i = 1040; i <= 1103; i++) {
				loadCharacter(characterso, charlisto, i, true);
			}

			// Добавляем символы Ё (U+0401) и ё (U+0451)
			loadCharacter(characterso, charlisto, 1025, true); // Ё
			loadCharacter(characterso, charlisto, 1105, true); // ё

			loadCharacter(characterso, charlisto, 32, true);

		} catch (IOException e) {
			System.out.println("FAILED to load outline set!!!");
			e.printStackTrace();
		}
	}

	/*
	 * Helper method to load a single character
	 */
	private void loadCharacter(Texture[] characters, Map<String, IntObject> charlist,
			int charCode, boolean outlined) throws IOException {
		char ch = (char) charCode;

		BufferedImage fontImage = outlined ? getOutlineFontImage(ch) : getFontImage(ch);
		String temptexname = (outlined ? "Charo." : "Char.") + charCode;

		characters[charCode] = textureloader.getTexture(temptexname, fontImage);
		charlist.put(String.valueOf(ch), new IntObject(charCode));
	}

	/*
	 * Draws the given characters to the screen
	 * size = size of the font (does not change resolution)
	 * x,y,z = position to draw at
	 * color = color of font to draw
	 * rotx, roty, rotz = how much to rotate the font on each axis
	 * centered = center the font at the given location, or left justify
	 * 
	 */
	public void drawText(String whatchars, float size, float x, float y, float z, Color4f color, float rotxpass,
			float rotypass, float rotzpass, boolean centered) {
		float fontsizeratio = size / (float) fontsize;

		int tempkerneling = kerneling;
		int k = 0;
		float realwidth = getWidth(whatchars, size, false);

		GL11.glPushMatrix();
		boolean islightingon = GL11.glIsEnabled(GL11.GL_LIGHTING);

		if (islightingon) {
			GL11.glDisable(GL11.GL_LIGHTING);
		}

		// Включаем альфа-блендинг для прозрачности
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // Режим смешивания для прозрачности

		GL11.glTranslatef(x, y, z);
		GL11.glRotatef(rotxpass, 1, 0, 0);
		GL11.glRotatef(rotypass, 0, 1, 0);
		GL11.glRotatef(rotzpass, 0, 0, 1);

		float totalwidth = 0;
		if (centered) {
			totalwidth = -realwidth / 2f;
		}

		// Применяем цвет с прозрачностью
		GL11.glColor4f(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()); // Устанавливаем цвет с
																								// альфа-каналом

		for (int i = 0; i < whatchars.length(); i++) {
			String tempstr = whatchars.substring(i, i + 1);

			IntObject charData = charlistp.get(tempstr);
			if (charData == null) {
				System.out.println("Missing character in charlistp: " + tempstr); // Логирование отсутствующего символа
				continue; // Пропускаем символ, если его нет в charlistp
			}

			k = charData.charnum;
			drawtexture(charactersp[k], fontsizeratio, totalwidth, 0, color, rotxpass, rotypass, rotzpass);
			totalwidth += (charactersp[k].getImageWidth() * fontsizeratio + tempkerneling);
		}

		// Отключаем альфа-блендинг после отрисовки
		GL11.glDisable(GL11.GL_BLEND);

		if (islightingon) {
			GL11.glEnable(GL11.GL_LIGHTING);
		}

		GL11.glColor4f(1, 1, 1, 1);
		GL11.glPopMatrix();
	}

	/*
	 * Draws the given characters to the screen with a shadow effect
	 * size = size of the font (does not change resolution)
	 * x,y,z = position to draw at
	 * color = color of font to draw
	 * rotx, roty, rotz = how much to rotate the font on each axis
	 * centered = center the font at the given location, or left justify
	 * shadowOffsetX, shadowOffsetY = смещение тени по осям X и Y
	 * shadowColor = цвет тени
	 */
	public void drawTextWithShadow(String whatchars, float size, float x, float y, float z, Color4f color,
			float rotxpass, float rotypass, float rotzpass, boolean centered,
			float shadowOffsetX, float shadowOffsetY, Color4f shadowColor) {

		float fontsizeratio = size / (float) fontsize;
		int tempkerneling = kerneling;
		int k = 0;
		float realwidth = getWidth(whatchars, size, false);

		GL11.glPushMatrix();
		boolean islightingon = GL11.glIsEnabled(GL11.GL_LIGHTING);

		if (islightingon) {
			GL11.glDisable(GL11.GL_LIGHTING);
		}

		// Включаем альфа-блендинг для прозрачности
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // Режим смешивания для прозрачности

		GL11.glTranslatef(x, y, z);
		GL11.glRotatef(rotxpass, 1, 0, 0);
		GL11.glRotatef(rotypass, 0, 1, 0);
		GL11.glRotatef(rotzpass, 0, 0, 1);

		float totalwidth = 0;
		if (centered) {
			totalwidth = -realwidth / 2f;
		}

		// Увеличиваем смещение для тени
		float shadowX = totalwidth + shadowOffsetX;
		float shadowY = 0 + shadowOffsetY;

		// Рисуем тень с увеличенным смещением и размытие
		GL11.glColor4f(shadowColor.getRed(), shadowColor.getGreen(), shadowColor.getBlue(), shadowColor.getAlpha());

		// Для размытия создаём несколько слоёв тени
		float shadowSpread = 1.5f; // Этот параметр определяет, насколько сильно будет размыта тень
		for (int j = 0; j < 2; j++) { // Мы рисуем тень несколько раз с увеличивающимся смещением
			float offsetX = shadowX + j * shadowSpread;
			float offsetY = shadowY + j * shadowSpread;
			for (int i = 0; i < whatchars.length(); i++) {
				String tempstr = whatchars.substring(i, i + 1);
				IntObject charData = charlistp.get(tempstr);
				if (charData == null) {
					System.out.println("Missing character in charlistp: " + tempstr); // Логирование отсутствующего
																						// символа
					continue; // Пропускаем символ, если его нет в charlistp
				}
				k = charData.charnum;
				drawtexture(charactersp[k], fontsizeratio, offsetX, offsetY, shadowColor, rotxpass, rotypass, rotzpass);
				offsetX += (charactersp[k].getImageWidth() * fontsizeratio + tempkerneling); // Смещаем тень
			}
		}

		// Теперь рисуем основной текст
		GL11.glColor4f(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()); // Устанавливаем основной
																								// цвет

		float totalwidthText = totalwidth;
		for (int i = 0; i < whatchars.length(); i++) {
			String tempstr = whatchars.substring(i, i + 1);
			IntObject charData = charlistp.get(tempstr);
			if (charData == null) {
				System.out.println("Missing character in charlistp: " + tempstr); // Логирование отсутствующего символа
				continue; // Пропускаем символ, если его нет в charlistp
			}
			k = charData.charnum;
			drawtexture(charactersp[k], fontsizeratio, totalwidthText, 0, color, rotxpass, rotypass, rotzpass);
			totalwidthText += (charactersp[k].getImageWidth() * fontsizeratio + tempkerneling); // Смещаем текст
		}

		// Отключаем альфа-блендинг после отрисовки
		GL11.glDisable(GL11.GL_BLEND);

		if (islightingon) {
			GL11.glEnable(GL11.GL_LIGHTING);
		}

		GL11.glColor4f(1, 1, 1, 1);
		GL11.glPopMatrix();
	}

	public void drawTextWithOutline(String whatchars, float size, float x, float y, float z, Color4f textColor,
			float rotxpass, float rotypass, float rotzpass, boolean centered,
			float outlineWidth, Color4f outlineColor) {

		float fontsizeratio = size / (float) fontsize;
		int tempkerneling = kerneling;
		int k = 0;
		float realwidth = getWidth(whatchars, size, false);

		GL11.glPushMatrix();
		boolean islightingon = GL11.glIsEnabled(GL11.GL_LIGHTING);

		if (islightingon) {
			GL11.glDisable(GL11.GL_LIGHTING);
		}

		// Включаем альфа-блендинг для прозрачности
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glTranslatef(x, y, z);
		GL11.glRotatef(rotxpass, 1, 0, 0);
		GL11.glRotatef(rotypass, 0, 1, 0);
		GL11.glRotatef(rotzpass, 0, 0, 1);

		float totalwidth = 0;
		if (centered) {
			totalwidth = -realwidth / 2f;
		}

		// Рисуем обводку
		GL11.glColor4f(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), outlineColor.getAlpha());
		for (float offsetX = -outlineWidth; offsetX <= outlineWidth; offsetX += outlineWidth / 2) {
			for (float offsetY = -outlineWidth; offsetY <= outlineWidth; offsetY += outlineWidth / 2) {
				if (offsetX == 0 && offsetY == 0)
					continue; // Пропускаем центр (основной текст)

				float offsetTotalWidth = totalwidth + offsetX;
				for (int i = 0; i < whatchars.length(); i++) {
					String tempstr = whatchars.substring(i, i + 1);
					IntObject charData = charlistp.get(tempstr);
					if (charData == null) {
						System.out.println("Missing character in charlistp: " + tempstr);
						continue;
					}
					k = charData.charnum;
					drawtexture(charactersp[k], fontsizeratio, offsetTotalWidth, offsetY, outlineColor, rotxpass,
							rotypass, rotzpass);
					offsetTotalWidth += (charactersp[k].getImageWidth() * fontsizeratio + tempkerneling);
				}
			}
		}

		// Теперь рисуем основной текст
		GL11.glColor4f(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), textColor.getAlpha());
		float totalwidthText = totalwidth;
		for (int i = 0; i < whatchars.length(); i++) {
			String tempstr = whatchars.substring(i, i + 1);
			IntObject charData = charlistp.get(tempstr);
			if (charData == null) {
				System.out.println("Missing character in charlistp: " + tempstr);
				continue;
			}
			k = charData.charnum;
			drawtexture(charactersp[k], fontsizeratio, totalwidthText, 0, textColor, rotxpass, rotypass, rotzpass);
			totalwidthText += (charactersp[k].getImageWidth() * fontsizeratio + tempkerneling);
		}

		// Отключаем альфа-блендинг после отрисовки
		GL11.glDisable(GL11.GL_BLEND);

		if (islightingon) {
			GL11.glEnable(GL11.GL_LIGHTING);
		}

		GL11.glColor4f(1, 1, 1, 1);
		GL11.glPopMatrix();
	}

	public void drawTextAlignedLeftOf(String whatchars, float size, float rightEdgeX, float y, float z,
			Color4f textColor,
			float rotxpass, float rotypass, float rotzpass, boolean centered,
			float outlineWidth, Color4f outlineColor) {
		// Получаем ширину текста
		float realwidth = getWidth(whatchars, size, false);

		// Вычисляем координату x, чтобы текст начинался слева и не заходил за
		// `rightEdgeX`
		float adjustedX = rightEdgeX - realwidth;

		// Вызываем метод отрисовки текста с обводкой
		drawTextWithOutline(whatchars, size, adjustedX, y, z, textColor, rotxpass, rotypass, rotzpass, centered,
				outlineWidth, outlineColor);
	}

	/*
	 * Draws the given characters to the screen with a drop shadow
	 * size = size of the font (does not change resolution)
	 * x,y,z = position to draw at
	 * color = color of font to draw
	 * shadowcolor = color of the drop shadow
	 * rotx, roty, rotz = how much to rotate the font on each axis
	 * centered = center the font at the given location, or left justify
	 * 
	 */
	public void drawText(String whatchars, float size, float x, float y, float z, Color4f color, Color4f shadowcolor,
			float rotxpass, float rotypass, float rotzpass, boolean centered) {
		drawText(whatchars, size, x + 1f, y - 1f, z, shadowcolor, rotxpass, rotypass, rotzpass, centered);
		drawText(whatchars, size, x, y, z, color, rotxpass, rotypass, rotzpass, centered);
	}

	public void drawOutlinedText(String whatchars, float size, float x, float y, float z, Color4f color,
			Color4f outlinecolor, float rotxpass, float rotypass, float rotzpass, boolean centered) {
		float fontsizeratio = size / (float) fontsize; // Ratio for font size

		int k = 0;
		int ko = 0;
		float realwidth = getWidth(whatchars, size, true); // Получаем реальную ширину текста

		GL11.glPushMatrix();
		boolean islightingon = GL11.glIsEnabled(GL11.GL_LIGHTING);

		if (islightingon) {
			GL11.glDisable(GL11.GL_LIGHTING);
		}

		GL11.glTranslatef(x, y, z); // Перемещаем в нужную позицию
		GL11.glRotatef(rotxpass, 1, 0, 0);
		GL11.glRotatef(rotypass, 0, 1, 0);
		GL11.glRotatef(rotzpass, 0, 0, 1);

		float totalwidth = 0;
		if (centered) {
			totalwidth = -realwidth / 2f; // Если нужно центрировать, смещаем влево
		}

		for (int i = 0; i < whatchars.length(); i++) {
			String tempstr = whatchars.substring(i, i + 1);
			ko = ((charlisto.get(tempstr))).charnum;

			// Отрисовываем контур
			drawtexture(characterso[ko], fontsizeratio, totalwidth, 0, outlinecolor, rotxpass, rotypass, rotzpass);

			k = ((charlistp.get(tempstr))).charnum;

			// Учет смещения для правильного отображения
			float xoffset = (characterso[k].getImageWidth() - charactersp[k].getImageWidth()) * fontsizeratio / 2f;
			float yoffset = (characterso[k].getImageHeight() - charactersp[k].getImageHeight()) * fontsizeratio / 2f;

			// Рисуем текст
			drawtexture(charactersp[k], fontsizeratio, totalwidth + xoffset, yoffset, color, rotxpass, rotypass,
					rotzpass);

			totalwidth += ((characterso[k].getImageWidth() * fontsizeratio) + kerneling); // Считаем ширину и отступы
		}

		if (islightingon) {
			GL11.glEnable(GL11.GL_LIGHTING);
		}

		GL11.glPopMatrix();
	}

	/*
	 * Draw the actual quad with character texture
	 */
	private void drawtexture(Texture texture, float ratio, float x, float y, Color4f color, float rotx, float roty,
			float rotz) {
		// Get the appropriate measurements from the texture itself
		float imgwidth = texture.getImageWidth() * ratio;
		float imgheight = -texture.getImageHeight() * ratio;
		float texwidth = texture.getWidth();
		float texheight = texture.getHeight();

		// Bind the texture
		texture.bind();

		// translate to the right location
		GL11.glColor4f(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());

		// draw a quad with to place the character onto
		GL11.glBegin(GL11.GL_QUADS);
		{
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex2f(0 + x, 0 - y);

			GL11.glTexCoord2f(0, texheight);
			GL11.glVertex2f(0 + x, imgheight - y);

			GL11.glTexCoord2f(texwidth, texheight);
			GL11.glVertex2f(imgwidth + x, imgheight - y);

			GL11.glTexCoord2f(texwidth, 0);
			GL11.glVertex2f(imgwidth + x, 0 - y);
		}
		GL11.glEnd();

	}

	/*
	 * Returns the width in pixels of the given string, size, outlined or not
	 * used for determining how to position the string, either for the user
	 * or for this object
	 * 
	 */
	public float getWidth(String whatchars, float size, boolean outlined) {
		float fontsizeratio = size / (float) fontsize;
		float tempkerneling = ((float) kerneling * fontsizeratio);
		float totalwidth = 0;
		int k = 0;

		for (int i = 0; i < whatchars.length(); i++) {
			String tempstr = whatchars.substring(i, i + 1);

			if (outlined) {
				IntObject charData = charlisto.get(tempstr);
				if (charData != null) {
					k = charData.charnum;
					totalwidth += (characterso[k].getImageWidth() * fontsizeratio) + tempkerneling;
				} else {
					System.out.println("Missing character in charlisto: " + tempstr);
				}
			} else {
				IntObject charData = charlistp.get(tempstr);
				if (charData != null) {
					k = charData.charnum;
					totalwidth += (charactersp[k].getImageWidth() * fontsizeratio) + tempkerneling;
				} else {
					System.out.println("Missing character in charlistp: " + tempstr);
				}
			}
		}
		return totalwidth;
	}

	/*
	 * For convenience of checking user input keys
	 * Can be taken out if you're not going to use it
	 * 
	 */
	public boolean keyrangevalid(int currentKey) {
		// Проверка на допустимые клавиши
		return currentKey == GLFW.GLFW_KEY_A ||
				currentKey == GLFW.GLFW_KEY_B ||
				currentKey == GLFW.GLFW_KEY_C ||
				currentKey == GLFW.GLFW_KEY_D ||
				currentKey == GLFW.GLFW_KEY_E ||
				currentKey == GLFW.GLFW_KEY_F ||
				currentKey == GLFW.GLFW_KEY_G ||
				currentKey == GLFW.GLFW_KEY_H ||
				currentKey == GLFW.GLFW_KEY_I ||
				currentKey == GLFW.GLFW_KEY_J ||
				currentKey == GLFW.GLFW_KEY_K ||
				currentKey == GLFW.GLFW_KEY_L ||
				currentKey == GLFW.GLFW_KEY_M ||
				currentKey == GLFW.GLFW_KEY_N ||
				currentKey == GLFW.GLFW_KEY_O ||
				currentKey == GLFW.GLFW_KEY_P ||
				currentKey == GLFW.GLFW_KEY_Q ||
				currentKey == GLFW.GLFW_KEY_R ||
				currentKey == GLFW.GLFW_KEY_S ||
				currentKey == GLFW.GLFW_KEY_T ||
				currentKey == GLFW.GLFW_KEY_U ||
				currentKey == GLFW.GLFW_KEY_V ||
				currentKey == GLFW.GLFW_KEY_W ||
				currentKey == GLFW.GLFW_KEY_X ||
				currentKey == GLFW.GLFW_KEY_Y ||
				currentKey == GLFW.GLFW_KEY_Z ||
				currentKey == GLFW.GLFW_KEY_0 ||
				currentKey == GLFW.GLFW_KEY_1 ||
				currentKey == GLFW.GLFW_KEY_2 ||
				currentKey == GLFW.GLFW_KEY_3 ||
				currentKey == GLFW.GLFW_KEY_4 ||
				currentKey == GLFW.GLFW_KEY_5 ||
				currentKey == GLFW.GLFW_KEY_6 ||
				currentKey == GLFW.GLFW_KEY_7 ||
				currentKey == GLFW.GLFW_KEY_8 ||
				currentKey == GLFW.GLFW_KEY_9 ||
				currentKey == GLFW.GLFW_KEY_PERIOD ||
				currentKey == GLFW.GLFW_KEY_SPACE ||
				currentKey == GLFW.GLFW_KEY_ENTER ||
				currentKey == GLFW.GLFW_KEY_COMMA ||
				currentKey == GLFW.GLFW_KEY_SLASH ||
				currentKey == GLFW.GLFW_KEY_SEMICOLON ||
				currentKey == GLFW.GLFW_KEY_LEFT_BRACKET ||
				currentKey == GLFW.GLFW_KEY_RIGHT_BRACKET ||
				currentKey == GLFW.GLFW_KEY_EQUAL ||
				currentKey == GLFW.GLFW_KEY_MINUS ||
				currentKey == GLFW.GLFW_KEY_APOSTROPHE ||
				currentKey == GLFW.GLFW_KEY_BACKSPACE;
	}

	public boolean keyrangevalidnumbers(int currentKey) {
		boolean retvalue = false;
		if (currentKey == GLFW.GLFW_KEY_0 ||
				currentKey == GLFW.GLFW_KEY_1 ||
				currentKey == GLFW.GLFW_KEY_2 ||
				currentKey == GLFW.GLFW_KEY_3 ||
				currentKey == GLFW.GLFW_KEY_4 ||
				currentKey == GLFW.GLFW_KEY_5 ||
				currentKey == GLFW.GLFW_KEY_6 ||
				currentKey == GLFW.GLFW_KEY_7 ||
				currentKey == GLFW.GLFW_KEY_8 ||
				currentKey == GLFW.GLFW_KEY_9 ||
				currentKey == GLFW.GLFW_KEY_PERIOD ||
				currentKey == GLFW.GLFW_KEY_BACKSPACE) {
			retvalue = true;
		}
		return retvalue;
	}

	public float getHeight(String text, int i) {
		Font tempfont = font.deriveFont((float) fontsize);
		// Создаем временный BufferedImage для получения высоты
		BufferedImage tempfontImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) tempfontImage.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setFont(tempfont);
		FontMetrics fm = g.getFontMetrics();

		// Получаем высоту всего текста
		int height = fm.getHeight(); // Высота текста
		return (float) height;
	}

	public float getBaselineOffset(int fontSize) {
		// Например, можно возвращать половину высоты текста для коррекции
		return getHeight("A", fontSize);
	}

}
