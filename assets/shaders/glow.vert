#version 120

varying vec2 textureCoords;

void main() {
    gl_Position = ftransform();
    textureCoords = gl_MultiTexCoord0.xy;
}
