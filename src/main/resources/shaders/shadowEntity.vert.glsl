#version 150

in vec3 a_position;
in vec2 a_texCoord0;

out vec2 v_texCoord0;

uniform mat4 u_projViewTrans;

void main() 
{
	v_texCoord0 = a_texCoord0;
	gl_Position = u_projViewTrans * vec4(a_position, 1.0);	
}
