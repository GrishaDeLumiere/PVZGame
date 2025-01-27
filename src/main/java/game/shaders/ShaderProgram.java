package game.shaders;

import org.lwjgl.opengl.GL20;

public class ShaderProgram {
    private final int programId;

    public ShaderProgram(String vertexCode, String fragmentCode) {
        programId = GL20.glCreateProgram();
        if (programId == 0) {
            throw new RuntimeException("Не удалось создать шейдерную программу.");
        }

        int vertexShaderId = createShader(vertexCode, GL20.GL_VERTEX_SHADER);
        int fragmentShaderId = createShader(fragmentCode, GL20.GL_FRAGMENT_SHADER);

        GL20.glAttachShader(programId, vertexShaderId);
        GL20.glAttachShader(programId, fragmentShaderId);

        GL20.glLinkProgram(programId);
        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Ошибка линковки программы: " + GL20.glGetProgramInfoLog(programId));
        }

        GL20.glValidateProgram(programId);
        if (GL20.glGetProgrami(programId, GL20.GL_VALIDATE_STATUS) == 0) {
            System.err.println("Предупреждение при проверке программы: " + GL20.glGetProgramInfoLog(programId));
        }
    }

    private int createShader(String shaderCode, int shaderType) {
        int shaderId = GL20.glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new RuntimeException("Не удалось создать шейдер.");
        }

        GL20.glShaderSource(shaderId, shaderCode);
        GL20.glCompileShader(shaderId);

        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("Ошибка компиляции шейдера: " + GL20.glGetShaderInfoLog(shaderId));
        }

        return shaderId;
    }

    public void use() {
        GL20.glUseProgram(programId);
    }

    public void setUniform(String name, float value) {
        int location = GL20.glGetUniformLocation(programId, name);
        if (location != -1) {
            GL20.glUniform1f(location, value);
        }
    }

    public void delete() {
        GL20.glDeleteProgram(programId);
    }

    public static void reset() {
        GL20.glUseProgram(0);
    }

    public int getProgramId() {
        return programId;
    }
}
