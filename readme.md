# Features
### Optifine/Iris like shaderpacks
* Swappable packs in Options menu (bottom left shader button)
* Optionally 8 render textures configured with /* DRAWBUFFERS:01234 */
  * use `layout (location = <n>) out "anyName"` in frag shader to specify renderTarget
    * for now i require `#version 330` for glsl to be able to use layout 
  * Defaults to texColor0 if not specified
  * Use textures with `uniform sampler2D colerTex<n>` in shader
* Optionally 8 composite render passes with `composite<n>.frag/vert.glsl` in shaderpack
* Required Final shader similar to composite but only renders to screen
* Required Shadowpass - renders before everything else
  * Renders to Texture labeled shadowMap
  * Runs shadowpass shaders
> Rendering pipeline similar to Optifine with less features [Shader Labs Pipeline](https://shaderlabs.org/wiki/Rendering_Pipeline_(OptiFine,_ShadersMod)) gives a basic overview
* Most likely wont play well with other mods affecting rendering
* Only uses base game shaders from jar  when shaderpack disabled for now
## How to use
* Run like anyother quilt mod 
* add shaderpacks into /mods/shaderpacks 
* find the shader button (bottom left of options menu) 
* select shader pack and enable it 

# Cosmic Quilt Example Mod
> The example mod for the [Cosmic Quilt](https://codeberg.org/CRModders/cosmic-quilt) Loader

## How to test/build
For testing in the dev env, you can use the `gradle run` task

For building, the usual `gradle build` task can be used. The output will be in the `build/libs/` folder

## Wiki
For a wiki on how to use Cosmic Quilt & Quilt, please look at the [Cosmic Quilt wiki](https://codeberg.org/CRModders/cosmic-quilt/wiki) 


