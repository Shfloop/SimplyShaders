#version 150
in vec3 a_position;
in vec2 a_texCoord0;
out vec2 v_texCoord0;
void main()
{
    v_texCoord0 = a_texCoord0;
    gl_Position = vec4(a_position, 1.0);
}

