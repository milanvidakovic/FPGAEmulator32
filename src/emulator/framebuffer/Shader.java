package emulator.framebuffer;


import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glGetProgramiv;
import static org.lwjgl.opengl.GL20.glGetShaderiv;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUseProgram;


public class Shader {
	int ID;

	public Shader(String vertexSource, String fragmentSource) throws Exception {
		
		
		// Compile shaders
		int vertexShader;
		int fragmentShader;
		// First vertex shader
		vertexShader = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertexShader, vertexSource);
		glCompileShader(vertexShader);

		// Check for compile errors
		int[] status = new int[10]; //IntBuffer.allocate(1);
		glGetShaderiv(vertexShader, GL_COMPILE_STATUS, status);
		if (status[0] == 0)
		{
			throw new Exception("glGetShaderiv vertexShader failure status: " + status[0]);
		}

		// Then the fragment shader
		fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragmentShader, fragmentSource);
		glCompileShader(fragmentShader);

		// Check for compile errors
		glGetShaderiv(fragmentShader, GL_COMPILE_STATUS, status);
		if (status[0] == 0)
		{
			throw new Exception("glGetShaderiv fragmentShader failure status: " + status[0]);
		}

		ID = glCreateProgram();
		glAttachShader(ID, vertexShader);
		glAttachShader(ID, fragmentShader);
		glLinkProgram(ID);

		// Check for linking errors
		glGetProgramiv(ID, GL_LINK_STATUS, status);
		if (status[0] == 0)
		{
			throw new Exception("glGetProgram failure status: " + status[0]);
		}

		// Clear memory
		glDeleteShader(vertexShader);
		glDeleteShader(fragmentShader);
		
	}

	public void use() {
		glUseProgram(ID);
	}

	public void setInt(String name, int value) {
		glUniform1i(glGetUniformLocation(ID, name), value);
	}
	
}
