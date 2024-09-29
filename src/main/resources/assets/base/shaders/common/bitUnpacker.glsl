#define GET_TEX_COORDS getTexCoordsFromUVIdx(int(a_uvIdx))
#define GET_VERT_NORMAL getVertNormalFromIdx(int(a_uvIdx))
#define GET_FACE_NORMAL getFaceNormalFromIdx(int(a_uvIdx))
#define GET_BLOCK_VERT_POSITION getBlockVertexPosition(int(a_uvIdx), int(a_positionPacked))

#define NUM_FLOATS_PER_FACE_UVTEXBUFF (2 + 3 + 3 + 3 + 1)

in float a_positionPacked;
in float a_uvIdx;
uniform samplerBuffer texBuffer;

vec2 getTexCoordsFromUVIdx(int modelId)
{
    int i = NUM_FLOATS_PER_FACE_UVTEXBUFF * modelId;
    return vec2(texelFetch(texBuffer, i).r,
    texelFetch(texBuffer, i+1).r);
}
vec3 getVertNormalFromIdx(int modelId)
{
    int i = NUM_FLOATS_PER_FACE_UVTEXBUFF * modelId;
    return vec3(texelFetch(texBuffer, i+2).r,
    texelFetch(texBuffer, i+3).r,
    texelFetch(texBuffer, i+4).r);
}
vec3 getFaceNormalFromIdx(int modelId)
{
    int i = NUM_FLOATS_PER_FACE_UVTEXBUFF * modelId;
    return vec3(texelFetch(texBuffer, i+5).r,
    texelFetch(texBuffer, i+6).r,
    texelFetch(texBuffer, i+7).r);
}

vec3 getBlockVertexPosition(int modelId, int packedPosition)
{
    int i = NUM_FLOATS_PER_FACE_UVTEXBUFF * modelId;
    vec3 offset = vec3(texelFetch(texBuffer, i+8).r,
    texelFetch(texBuffer, i+9).r,
    texelFetch(texBuffer, i+10).r);

    int px = (packedPosition >> 12) & 0x3F;
    int py = (packedPosition >> 6) & 0x3F;
    int pz = (packedPosition) & 0x3F;
    vec3 blockPos = vec3(float(px), float(py), float(pz));

    return blockPos + offset;
}

int getUvAoBits(int uvAoPackedColor)
{
    return (uvAoPackedColor >> 16) & 65535;
}

int getPackedColorBits(int uvAoPackedColor)
{
    return uvAoPackedColor & 65535;
}

vec4 getBlockLight(int packedColorBits)
{
    return vec4(((packedColorBits) & 15), (packedColorBits >> 4) & 15, (packedColorBits >> 8) & 15, (packedColorBits >> 12)) / 16.0;
}