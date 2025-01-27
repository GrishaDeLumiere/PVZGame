#version 120

varying vec2 textureCoords;
uniform sampler2D textureSampler;
uniform float glowIntensity;

void main() {
    vec4 color = texture2D(textureSampler, textureCoords);
    gl_FragColor = color * vec4(glowIntensity, glowIntensity, glowIntensity, 1.0);
}
