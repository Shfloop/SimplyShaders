package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.shfloop.simply_shaders.Shadows;
import com.shfloop.simply_shaders.SimplyShaders;
import com.shfloop.simply_shaders.rendering.FinalShader;
import com.shfloop.simply_shaders.rendering.RenderFBO;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.RuntimeInfo;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.EntityShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(GameShader.class)
public abstract class GameShaderMixin   {


    @Inject(method = "initShaders()V", at = @At("HEAD")) // making it head should populate the mods assets with the replacement shaders
    static private void addShadowShaders(CallbackInfo ci) throws Exception {
        Shadows.initShadowShaders();//need these to be called first so it can create files if the need arisees

    }
    @Inject(method = "initShaders()V", at = @At("Tail")) // making it head should populate the mods assets with the replacement shaders
    static private void addShadowPassShaders(CallbackInfo ci) {
        //Instead i should just make a shader pack and have this be in it
        new ChunkShader("InternalShader/internal.shadowpass.vert.glsl","InternalShader/internal.shadowpass.frag.glsl");
        new EntityShader("InternalShader/internal.shadowEntity.vert.glsl","InternalShader/internal.shadowEntity.frag.glsl");
        ChunkShader.DEFAULT_BLOCK_SHADER = new ChunkShader("InternalShader/internal.chunk.vert.glsl", "InternalShader/internal.chunk.frag.glsl");
        new FinalShader("InternalShader/internal.final.vert.glsl", "InternalShader/internal.final.frag.glsl", new int[]{GL32.GL_COLOR_ATTACHMENT3}, false);
        new FinalShader("InternalShader/internal.composite0.vert.glsl","InternalShader/internal.composite0.frag.glsl", new int[]{GL32.GL_COLOR_ATTACHMENT3}, true); //this wont write out to any of them so it shouldnt matter
    }

    @Inject(method = "bind", at = @At("TAIL"))
    private void bindDrawBuffers(CallbackInfo ci) {
        //bind the appropriate outbuffers based on what the shader loaded from file
        if (SimplyShaders.inRender &&!Arrays.equals(RenderFBO.lastDrawBuffers, shaderDrawBuffers)) {
            GL32.glDrawBuffers(shaderDrawBuffers);
            RenderFBO.lastDrawBuffers = shaderDrawBuffers;
        }
    }




    @Shadow
    protected String vertexShaderFileName;
    @Shadow String fragShaderFileName;
    @Overwrite
    public void reload() {
        GameShader tempThis = ((GameShader) (Object)this); //maybe this works

        if (tempThis.shader != null) {
            tempThis.shader.dispose();
        }

        if (RuntimeInfo.isMac) {
            ShaderProgram.prependVertexCode = "";
            ShaderProgram.prependFragmentCode = "";
        }

        String vert = loadShaderFile(this.vertexShaderFileName, SimplyShaders.newShaderType.VERT); //preprocess doesnt do anything atm
        String frag = loadShaderFile(this.fragShaderFileName, SimplyShaders.newShaderType.FRAG);
        tempThis.validateShader(this.vertexShaderFileName, vert, this.fragShaderFileName, frag);
        ShaderProgram.pedantic = true;
        tempThis.shader = new ShaderProgram(vert, frag);
        System.out.println("Compiling shader(" + this.vertexShaderFileName + ", " + this.fragShaderFileName + ")...");
        if (!tempThis.shader.isCompiled()) {
            String log = tempThis.shader.getLog();
            throw new RuntimeException(this.getClass().getSimpleName() + " is not compiled!\n" + log);
        } else {
            for(String u : tempThis.shader.getUniforms()) {
                if (u.contains(".")) {
                    int blockIndex = GL32.glGetUniformBlockIndex(tempThis.shader.getHandle(), u.split("\\.")[0]);
                    System.out.println("Loaded uniform: " + tempThis.getUniformTypeName(u) + " " + u + " at location=" + blockIndex);
                } else {
                    System.out.println("Loaded uniform: " + tempThis.getUniformTypeName(u) + " " + u + " at location=" + tempThis.shader.getUniformLocation(u));
                }
            }

            System.out.println(tempThis.shader.getLog());
            if (RuntimeInfo.isMac) {
                ShaderProgram.prependVertexCode = GameShader.macOSPrependVertVer;
                ShaderProgram.prependFragmentCode = GameShader.macOSPrependFragVer;
            }
        }


    }
    //make better error reporting

    //adding field to each GameShader
    private int[] shaderDrawBuffers;
    private String loadShaderFile(String shaderName, SimplyShaders.newShaderType shaderType) {
        String[] rawShaderLines = GameAssetLoader.loadAsset("shaders/" + shaderName).readString().split("\n");
        StringBuilder sb = new StringBuilder();
        String version = "";
        String define = shaderName.replaceAll("[-/. ()]", "_");
        sb.append("#ifndef " + define + "\n");
        sb.append("#define " + define + "\n");
        boolean foundDrawBuffer = false;
        for(String shaderLine : rawShaderLines) {
            String trimmed = shaderLine.trim(); // Fix CRLF causing crashes

            if (shaderLine.startsWith("#version")) {
                version = shaderLine + "\n";
                if (RuntimeInfo.isMac) {
                    switch(shaderType.ordinal()) {
                        case 0:
                            version = version + GameShader.macOSPrependFrag;
                            break;
                        case 1:
                            version = version + GameShader.macOSPrependVert;
                    }
                }
            } else if (trimmed.startsWith("#import \"") && trimmed.endsWith("\"")) {
                String importedShaderName = shaderLine.replaceFirst("#import \"", "").replace("\\", "/");
                importedShaderName = importedShaderName.substring(0, importedShaderName.length() - 1);
                sb.append(loadShaderFile(importedShaderName, SimplyShaders.newShaderType.IMPORTED) + "\n");
            } else if (trimmed.startsWith("/*") && trimmed.endsWith("*/")) {
               foundDrawBuffer = findDrawBuffers(trimmed);
            }else {
                sb.append(shaderLine + "\n");
            }
        }
        //only want to apply drawbuffer if its a frag shader
        if (!foundDrawBuffer && shaderType.ordinal() == 0) {
            shaderDrawBuffers = new int[] {GL32.GL_COLOR_ATTACHMENT0}; //default
            System.out.println("Default drawBuffer main");
        }

        sb.append("#endif //" + define + "\n");
        return version + sb.toString();
    }
    private boolean findDrawBuffers(String shaderLine) {
        int indexOfDrawBuffers = shaderLine.indexOf(':'); // maybe change this so it actually looks for drawbuffers
        if (indexOfDrawBuffers > 0) {
            indexOfDrawBuffers++; //want the int after
            //found it
            int[] tempdrawBuffers = new int[8];
            int drawBufferLength = 0;
            //look for only 8 drawbuffers
            for (int i = 0; i <  8; i++ ) {
                //dont want index outofbound
                if (i + indexOfDrawBuffers >= shaderLine.length()) {
                    break;
                }
                char testValue = shaderLine.charAt(i + indexOfDrawBuffers);
                if (testValue>= 48 && testValue <= 55) { // its a character between 0-7
                    for (int y = 0; y <= drawBufferLength; y++) {
                        //loop through the temp buffer to check if the values already been written
                        if (tempdrawBuffers[y] == testValue) {
                            //duplicate drawbuffer should not compile
                            throw new RuntimeException("Duplicate DrawBuffer"); //TODO add error handling
                        }
                    }
                    tempdrawBuffers[drawBufferLength++] = testValue; //if it finds a number add it to tempdrawbuffers
                } else {
                    break; // break if its not a number
                }

            }
            if (drawBufferLength == 0) {
                //found drawbuffers directive but
                //no drawbuffers defined
                throw new RuntimeException("NO drawbuffers elements defined"); // could probably just make the default array
            }
            shaderDrawBuffers = new int[drawBufferLength];//drawbufer points to next open spot so should be fine
            System.out.print("Defined DrawBuffers: {");
            for (int i = 0; i < drawBufferLength; i++) {
                shaderDrawBuffers[i] = tempdrawBuffers[i] + GL32.GL_COLOR_ATTACHMENT0 -48; //48 to convert the ascii back to 0-7
                System.out.print(tempdrawBuffers[i] - 48 + ", ");
                //copy over the data with a new sized array and the appropriate colorattachment value
            }
            System.out.println("}");

            return true;
        }
        return false;
    }



//    @Inject(method = "bind(Lcom/badlogic/gdx/graphics/Camera;)V", at = @At("TAIL"))//value = "INVOKE", target = "Lfinalforeach/cosmicreach/rendering/shaders/GameShader;bindOptionalTextureBuffer(Ljava/lang/String;,  Lfinalforeach/cosmicreach/rendering/TextureBuffer; I)V")) // Lfinalforeach/cosmicreach/rendering/shaders/GameShader;bindOptionalTextureBuffer(Ljava/lang/String;,  Lfinalforeach/cosmicreach/rendering/TextureBuffer; I)V
//    private void injectShaderParam(CallbackInfo ci ) {
//        if (Shadows.shaders_on && InGame.world != null) { //should find a better way to do this
//
//            this.bindOptionalUniformMatrix("lightSpaceMatrix", Shadows.getCamera().combined);
//            this.bindOptionalUniformi("shadowMap", Shadows.shadow_map.getDepthMapTexture().id); // i think i should try and change this so it matches how texture numbers are handled in chunk shader but idk
//            ((GameShader)(Object)this).bindOptionalUniform3f("lightPos", Shadows.getCamera().position); // think this is what i need
//            ((GameShader)(Object)this).bindOptionalUniform3f("lightDir", Shadows.getCamera().direction);// to compare with normal
//
//        }
//    }
//    private void bindOptionalUniformMatrix(String uniform_name, Matrix4 mat) {
//        int u = ((GameShader)(Object)this).shader.getUniformLocation(uniform_name);
//        if (u != -1) {
//            ((GameShader)(Object)this).shader.setUniformMatrix(uniform_name,mat);
//
//        }
//    }
//
//    //may want to move these out of the mixin but i have no idea if its a bad thing
//    private void bindOptionalUniformi(String uniform_name, int id) {
//        int u = ((GameShader)(Object)this).shader.getUniformLocation(uniform_name);
//        if (u != -1) {
//            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0 + 3);//just for right now this works i thinki
//            Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, Shadows.shadow_map.getDepthMapTexture().id);
//            ((GameShader)(Object)this).shader.setUniformi(u, 3);
//
//
//        }
//    }



}
