#version 150

#ifdef GL_ES

precision mediump float;

#endif

uniform vec3 skyAmbientColor;

in vec2 v_texCoord0;
in vec3 worldPos;
in vec4 blocklight;
//in vec4 fragPosLightSpace;


 //.1.44
//in float normal_float;

//in vec3 normal;

uniform sampler2D texDiffuse;
uniform sampler2D shadowMap;

//uniform vec3 lightPos;
uniform vec3 lightDir; // libgdx says its already normalized




out vec4 outColor;






void main()
{

    vec2 tilingCoords = v_texCoord0;
    vec4 texColor = texture(texDiffuse, v_texCoord0);

    if (texColor.a == 0)
    {
        discard;
    }



    // vec3 ambient = vec3(1.0) * 0.5;
    // //this could definitly just be vec3(0.15);
    // vec3 it = pow(15*blocklight.rgb / 25.0, vec3(2));
    // vec3 t = 30.0/(1.0 + exp(-15.0 * it)) -15;
    // vec3 lightTint = max (t/15, blocklight.a * skyAmbientColor);

    //shadow = ShadowCalculation(fragPosLightSpace);
    outColor = vec4(texColor.rgb , texColor.a);
}



