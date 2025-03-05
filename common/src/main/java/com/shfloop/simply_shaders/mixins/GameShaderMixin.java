package com.shfloop.simply_shaders.mixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.llamalad7.mixinextras.sugar.Local;
import com.shfloop.simply_shaders.GameShaderInterface;
import com.shfloop.simply_shaders.pack_loading.ShaderPackLoader;
import com.shfloop.simply_shaders.Shadows;
import com.shfloop.simply_shaders.SimplyShaders;
import com.shfloop.simply_shaders.rendering.FinalShader;
import com.shfloop.simply_shaders.rendering.RenderTextureHolder;
import finalforeach.cosmicreach.ClientSingletons;
import finalforeach.cosmicreach.RuntimeInfo;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.util.Identifier;
import org.lwjgl.opengl.GL32;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(GameShader.class)
public abstract class GameShaderMixin implements GameShaderInterface {



    @Inject(method = "initShaders()V", at = @At("TAIL")) //
    static private void addShadowPassShaders(CallbackInfo ci) {

        FinalShader.initFinalShader();

        Shadows.BLOCK_ENTITY_SHADER = ChunkShader.DEFAULT_BLOCK_SHADER;
        Shadows.defaultAllShadersSize = allShaders.size;

    }
    //TODO get rid of this overwrite
    @Overwrite
    public static void reloadAllShaders() {
        if (ShaderPackLoader.shaderPackOn) {
//            for (GameShader shader: ShaderPackLoader.shader1) {
//                shader.reload();
//            }
            return;
    }
        System.out.println("Reloading all Shaders");

        for (GameShader shader: GameShaderAccessor.getShader()) {
            shader.reload();
        }

        System.out.println("Reloaded all Shaders");
    }

    @Inject(method = "bind", at = @At("HEAD"))
    private void bindDrawBuffers(CallbackInfo ci) {
        //bind the appropriate outbuffers based on what the shader loaded from file
        //i can do this at the start no problem
//        if (this.shaderInputBuffers != null) {
//            for (int pingPongBufferNum: this.shaderInputBuffers) {
//
//                SimplyShaders.buffer.pingPongBuffer(pingPongBufferNum);
//            }//should swap the textuers before i call glDrawBuffers i think not really sure if i have to
//        }






        if (SimplyShaders.inRender &&!Arrays.equals(RenderTextureHolder.boundFrameBuffer.lastDrawBuffers, shaderDrawBuffers)) {

            GL32.glDrawBuffers(shaderDrawBuffers);
            RenderTextureHolder.boundFrameBuffer.lastDrawBuffers = shaderDrawBuffers;
        }

    }

//    @Inject(method = "unbind", at = @At("HEAD"))
//    private void resetUniformBuffers(CallbackInfo ci) {
//        if (this.shaderInputBuffers != null) {
//            for (int pingPongBufferNum: this.shaderInputBuffers) {
//
//                SimplyShaders.buffer.undoUniformPingPong(pingPongBufferNum);
//            }//should swap the textuers before i call glDrawBuffers i think not really sure if i have to
//        }
//    }

///mixin to start of gameshaderinit shaders so i can initialize shaderpackloader
    //so this kinda acts the same way drawbuffers but the renderFBO needs to be able to see each shaders ping-pongable buffers so it can switch its buffers

    @Inject(method = "bind", at = @At("TAIL"))
    private void injectGameShaderBind(CallbackInfo ci, @Local Camera worldCamera) {

        ((GameShader)(Object)this).bindOptionalFloat("frameTimeCounter", (float) Gdx.graphics.getFrameId() );
        ((GameShader)(Object)this).bindOptionalFloat("viewWidth", Gdx.graphics.getWidth());
        ((GameShader)(Object)this).bindOptionalFloat("viewHeight", Gdx.graphics.getHeight());
        ((GameShader)(Object)this).bindOptionalFloat("sunAngle", Shadows.sunAngle);
        ((GameShader)(Object)this).bindOptionalUniform3f("cameraViewDirection", worldCamera.direction);
        ((GameShader)(Object)this).bindOptionalFloat("frameTime", Gdx.graphics.getDeltaTime());

    }

    @Unique
    public int[] shaderInputBuffers = null;

    @Override
    public int[] getShaderInputBuffers() {
        return this.shaderInputBuffers;
    }

    @Override
    public void setShaderInputBuffers(int[] arr) {
        this.shaderInputBuffers = arr;
    }

    @Unique
    private final IntArray shaderStageTexMixMaps = new IntArray(8);
    @Override
    public IntArray getShaderMipMapEnabled() {return shaderStageTexMixMaps;}
    @Unique
    private void findFragShaderValues(String fragShaderText) {
        String[] shaderLines = fragShaderText.split("[;\n]");
        for (String line: shaderLines) {

            int colorTexIdx =line.indexOf("colorTex");
            if(colorTexIdx > 0) {
                if(colorTexIdx+ 9+5 > line.length()) {
                    continue;
                }
                //SimplyShaders.LOGGER.info("FoundColorTexLine " + line.charAt(colorTexIdx + 8));
                String substring = line.substring(colorTexIdx + 9, colorTexIdx + 9 + 5);
                if(substring.equals("Clear")) {//colorTex1Clear
                    if (line.contains("false")) {
                        ShaderPackLoader.packSettings.disableBufferClearing.add(line.charAt(colorTexIdx + 8) - 48);//convert the char to int value
                        SimplyShaders.LOGGER.info("DISABLE CLEARING FOR COLORTEX: " + line.charAt(colorTexIdx + 8));
                        continue;
                    }
                }
                if (colorTexIdx + 9 +  13 > line.length()) {
                    continue;
                }
                substring = line.substring(colorTexIdx + 9, colorTexIdx + 9 + 13);
                if (substring.equals("MipmapEnabled")) { //mipmaps are only for deferred composite and final (it happens before the shaderProgram)
                    if (line.contains("true")) {
                        int texNum = line.charAt(colorTexIdx + 8) - 48;
                        shaderStageTexMixMaps.add(texNum); // add the color tex value to the array 0 -7
                        ShaderPackLoader.packSettings.texesWithMipEnabled[texNum] = true;
                        SimplyShaders.LOGGER.info("EnablingMipMaps generating for colorTex {}", texNum); //genMipMaps happens before the shader executes where it was defined
                    }
                }
            }
        }


        //uses the drawbuffers and then the uniforms colorTexnnumebrs to decide if it should ping pong the render texture/s

        int[] renderTexturesUsed = new int[16];
        int numUsedRenderTextures = 0;
        String[] uniforms = ((GameShader)(Object)this).shader.getUniforms();



        for (String uniform: uniforms) {

            if (uniform.contains("colorTex")) {
                if (numUsedRenderTextures >= 16) {
                    throw new RuntimeException("more than 16 ");
                }

                renderTexturesUsed[numUsedRenderTextures] = Integer.parseInt(String.valueOf(uniform.charAt(8)),16); //ill use hex for the buffer numberng when i add them

                numUsedRenderTextures++;

            }
        }
        int pingPongCount = 0;
        for (int i = 0; i < numUsedRenderTextures; i++) {
            int testValue = renderTexturesUsed[i];
            boolean sameTextureUsed = false;
            for (int shaderDrawBuffer : this.shaderDrawBuffers) {
                if (shaderDrawBuffer - GL32.GL_COLOR_ATTACHMENT0 == testValue) { //need to subtract by the gl constante value because im using drawbuffers like that
                    //if both are equal that means that the shader is trying to read and write to the same texture
                    //there cant be duplicate values in this so i can exit

                    sameTextureUsed = true;
                    SimplyShaders.LOGGER.info("Ping pong Buffer {}",testValue );
                    pingPongCount++;
                    break;
                }
            }
            //reuse the array by setting the values that dont match to negative one
            if (!sameTextureUsed) {
                renderTexturesUsed[i] = -1;
            }
        }
        if (pingPongCount <= 0) {
            return;
        }
        //go over one more time
        this.shaderInputBuffers = new int[pingPongCount];
        pingPongCount = 0;
        //copy over the contents
        for (int i = 0; i < numUsedRenderTextures; i++) {
            if(renderTexturesUsed[i] != -1) {
                this.shaderInputBuffers[pingPongCount] = renderTexturesUsed[i];
                pingPongCount++;

            }
        }


    }


    @Shadow
    protected Identifier vertexShaderId;
    @Shadow Identifier fragShaderId;

    @Shadow private static Array<GameShader> allShaders;

    @Overwrite
    public void verifyShaderHasNoBannedKeywords(Identifier shaderId, String shaderText) {

    }
    @Overwrite
    public String removeCommentsFromShaderText(String shaderText) { //todo fix this instead of removing it
       return shaderText;
    }
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

        String vert = loadShaderFile(this.vertexShaderId, SimplyShaders.newShaderType.VERT); //preprocess doesnt do anything atm
        String frag = loadShaderFile(this.fragShaderId, SimplyShaders.newShaderType.FRAG);
        tempThis.validateShader(this.vertexShaderId, vert, this.fragShaderId, frag);
        ShaderProgram.pedantic = true;
        tempThis.shader = new ShaderProgram(vert, frag) {
            public void setVertexAttribute(String name, int size, int type, boolean normalize, int stride, int offset) {
                int location = this.getAttributeLocation(name);
                if (location != -1) {
                    this.setVertexAttribute(location, size, type, normalize, stride, offset);
                }
            }

            public void setVertexAttribute(int location, int size, int type, boolean normalize, int stride, int offset) {
                GL20 gl = Gdx.gl20;
                if (type == 5124 && Gdx.gl30 == null) {
                    type = 5126;
                }

                if (type == 5124 && Gdx.gl30 != null) {
                    Gdx.gl30.glVertexAttribIPointer(location, size, type, stride, offset);
                } else {
                    gl.glVertexAttribPointer(location, size, type, normalize, stride, offset);
                }

            }
        };



        System.out.println("Compiling shader(" + this.vertexShaderId + ", " + this.fragShaderId + ")...");
        if (!tempThis.shader.isCompiled()) {
            String log = tempThis.shader.getLog();
            throw new RuntimeException(this.getClass().getSimpleName() + " is not compiled!\nShader files: " + this.vertexShaderId + ", " + this.fragShaderId + "\n" + log);
        } else {
            for(String u : tempThis.shader.getUniforms()) {
                if (u.contains(".")) {
                    int blockIndex = GL32.glGetUniformBlockIndex(tempThis.shader.getHandle(), u.split("\\.")[0]);
                    //System.out.println("Loaded uniform: " + tempThis.getUniformTypeName(u) + " " + u + " at location=" + blockIndex);
                } else {
                    //System.out.println("Loaded uniform: " + tempThis.getUniformTypeName(u) + " " + u + " at location=" + tempThis.shader.getUniformLocation(u));
                }
            }

            System.out.println(tempThis.shader.getLog());
            if (RuntimeInfo.isMac) {
                ShaderProgram.prependVertexCode = GameShader.macOSPrependVertVer;
                ShaderProgram.prependFragmentCode = GameShader.macOSPrependFragVer;
            }
        }
        findFragShaderValues(frag);


    }
    //make better error reporting

    //adding field to each GameShader
    @Unique
    private int[] shaderDrawBuffers;
    public int[] getShaderDrawBuffers() {
        return this.shaderDrawBuffers;
    }
    private String loadShaderFile(Identifier shaderId, SimplyShaders.newShaderType shaderType) {
       // String[] rawShaderLines = GameAssetLoader.loadAsset("shaders/" + shaderName).readString().split("\n"); //
        String[] rawShaderLines = ShaderPackLoader.loadShader( shaderId, ShaderPackLoader.shaderPackOn);
        StringBuilder sb = new StringBuilder();
        String version = "";
        String define = shaderId.getName().replaceAll("[-/. ()]", "_");
        sb.append("#ifndef " + define + "\n");
        sb.append("#define " + define + "\n");
        if (ClientSingletons.shaderNUM32_IS_FLOAT) {
            sb.append("#ifndef NUM32_IS_FLOAT\n");
            sb.append("#define NUM32_IS_FLOAT\n");
            sb.append("#endif\n");
        }

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
                String importedShaderName = trimmed.replaceFirst("#import \"", "").replace("\\", "/");
                importedShaderName = importedShaderName.substring(0, importedShaderName.length() - 1);
                Identifier importedId = Identifier.of(importedShaderName);
                sb.append(loadShaderFile(importedId, SimplyShaders.newShaderType.IMPORTED) + "\n");
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
            System.out.println("}");//this should always have atleast one element
            for (int x: shaderDrawBuffers) {
                ShaderPackLoader.addDrawBuffer(x);
            }
            return true;
        }
        return false;
    }






}
