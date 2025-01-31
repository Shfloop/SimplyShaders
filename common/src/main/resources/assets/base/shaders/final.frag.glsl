#version 150
out vec4 outColor;
in vec2 v_texCoord0;
uniform sampler2D colorTex0;

uniform sampler2D noiseTex;

void main() {
    vec3 col = texture(colorTex0, v_texCoord0).rgb;
    //col = texture(noiseTex, v_texCoord0).rgb;
    outColor = vec4(col, 1.0);
}
