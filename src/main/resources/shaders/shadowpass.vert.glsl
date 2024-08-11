#version 150

in vec3 a_position;
in vec4 a_lighting;
// might not work as a float
uniform mat4 u_projViewTrans;

uniform mat4 u_modelMat;// added 1.4

out vec2 v_texCoord0;
out vec3 worldPos;
out vec4 blocklight;

uniform mat4 lightSpaceMatrix;

//in float as_normal_dir;
//out float normal_float;

//uniform samplerBuffer texBuffer;



//in float a_uvIdx;
#import "common/bitUnpacker.glsl"

//just gonna switch to botunpacker for .1.44 for normal and uv



//need to remove the imports
void main() {


    worldPos = a_position;
    blocklight = a_lighting;
    //int texId = int(a_uvIdx);
    v_texCoord0 = GET_TEX_COORDS;
    //vec2(texelFetch(texBuffer, 2*texId).r, texelFetch(texBuffer, (2*texId)+1).r);


    //normal_float = as_normal_dir; // this makes glass transparent to shadows for some reaason??



    gl_Position = (u_projViewTrans * u_modelMat * vec4(worldPos, 1.0));

}