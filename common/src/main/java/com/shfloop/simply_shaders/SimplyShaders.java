package com.shfloop.simply_shaders;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;
import com.shfloop.simply_shaders.pack_loading.ShaderPackLoader;
import com.shfloop.simply_shaders.rendering.*;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.io.SaveLocation;
import finalforeach.cosmicreach.ui.debug.DebugInfo;
import finalforeach.cosmicreach.ui.debug.DebugIntItem;
import finalforeach.cosmicreach.ui.debug.DebugLongItem;
import finalforeach.cosmicreach.ui.debug.DebugVec3Item;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.Arrays;


public class SimplyShaders {
    public static final Logger LOGGER = LoggerFactory.getLogger("SimplyShaders Mod");
    //public static RenderFBO buffer = null; //this might be a good way to go about this but im not really sure
    public static Mesh screenQuad;
    public static boolean inRender = false;

    public static RenderTextureHolder holder = null;
    public static TimerQuery timerQuery;
    private static final Vector3 timerVec = new Vector3(0,0,0);
    public static CompositeStageRenderer compositeStageRenderer;
    public static FinalStageRenderer finalStageRenderer;
   static  DecimalFormat debugPositionFormat = new DecimalFormat("0.00");
    public static void initTextureHolder() {
        if (holder != null) {
            holder.dispose();
        }
        IntArray drawBuffersUsed;
        if (ShaderPackLoader.drawBuffersUsed == null || ShaderPackLoader.drawBuffersUsed.isEmpty()) {
            //use drawbuffer 0
            drawBuffersUsed = new IntArray(1);
            drawBuffersUsed.add(GL32.GL_COLOR_ATTACHMENT0);
        } else {
            drawBuffersUsed = ShaderPackLoader.drawBuffersUsed;
        }
        BufferTexture[] textures = new BufferTexture[drawBuffersUsed.size];

        for (int i = 0; i < textures.length; i++) {
            String name = "colorTex" + (drawBuffersUsed.get(i) - GL32.GL_COLOR_ATTACHMENT0); //get the integer value 0-8
            float textureScale = 1.0f;
            boolean isMipMapEnabled = false;
            if (ShaderPackLoader.packSettings != null) {
                textureScale = ShaderPackLoader.packSettings.bufferTexturesScale.getOrDefault(name, 1.0f);
                if (textureScale != 1.0f) {
                    SimplyShaders.LOGGER.info("CHANGED SCALE {} to {}",name,textureScale);
                }
                if (ShaderPackLoader.packSettings.texesWithMipEnabled[drawBuffersUsed.get(i) - GL32.GL_COLOR_ATTACHMENT0]) {
                    isMipMapEnabled = true;

                }
            }
            textures[i] = new BufferTexture(name, (int) (textureScale * Gdx.graphics.getWidth()), (int) (textureScale * Gdx.graphics.getHeight()), GL32.GL_RGBA, GL32.GL_RGBA16F, drawBuffersUsed.get(i), isMipMapEnabled);
        }


        //created all the render textures that need to be rendered too (not including depth buffers
        //how do i knwo which ones to give depth buffers
        //i could just give the first one a depth buffer always
        ///wouldnt work for resized compute or deferred but its an easy solution that should work
        holder = new RenderTextureHolder(textures);
        boolean[] flippedBuffers = new boolean[8];
        IntArray mipMappedTextures = new IntArray();
        if (ShaderPackLoader.shaderPackOn) {
            int compStart = ShaderPackLoader.compositeStartIdx;
            CompositeShader[] compositeShaders = new CompositeShader[ShaderPackLoader.shader1.size - compStart];
            int index= 0;
            for ( int i = compStart; i < ShaderPackLoader.shader1.size; i++) {
                compositeShaders[index++] = (CompositeShader) ShaderPackLoader.shader1.get(i);
            }


            //need to add shadowTextures
            compositeStageRenderer = new CompositeStageRenderer(compositeShaders,null,flippedBuffers,null,holder ,mipMappedTextures);
        }


        finalStageRenderer = new FinalStageRenderer(FinalShader.DEFAULT_FINAL_SHADER,flippedBuffers,holder,mipMappedTextures);

    }


    public static void genMesh() {
        float[] verts = new float[20];
        int i = 0;

        verts[i++] = -1; // x1
        verts[i++] = -1; // y1
        verts[i++] = 0;
        verts[i++] = 0f; // u1
        verts[i++] = 0f; // v1

        verts[i++] = 1f; // x2
        verts[i++] = -1; // y2
        verts[i++] = 0;
        verts[i++] = 1f; // u2
        verts[i++] = 0f; // v2

        verts[i++] = 1f; // x3
        verts[i++] = 1f; // y3
        verts[i++] = 0;
        verts[i++] = 1f; // u3
        verts[i++] = 1f; // v3

        verts[i++] = -1; // x4
        verts[i++] = 1f; // y4
        verts[i++] = 0;
        verts[i++] = 0f; // u4
        verts[i++] = 1f; // v4

        screenQuad = new Mesh(true, 4, 0,
                new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));
        screenQuad.setVertices(verts);

    }


    public static void initializeBuffer() {
        LOGGER.info("Simply Shaders Initialized!");
        if (timerQuery == null) {
            //DebugInfo.addDebugItem(new DebugLongItem("Frame Time: ", SimplyShaders::getTimerQuery));
            DebugInfo.addDebugItem(new DebugVec3Item(SimplyShaders::getTimeQueryVec, (pos) -> {
                String var10000 = debugPositionFormat.format((double)pos.x);
                return "Shadow, Main, Composite: (" + var10000 + ", " + debugPositionFormat.format((double)pos.y) + ", " + debugPositionFormat.format((double)pos.z) + ")";
            }));
        }
//        if (buffer != null) {
//            buffer.dispose();
//        }
//        try {
//            buffer = new RenderFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
        initTextureHolder();
        float[] verts = new float[20];
        int i = 0;

        verts[i++] = -1; // x1
        verts[i++] = -1; // y1
        verts[i++] = 0;
        verts[i++] = 0f; // u1
        verts[i++] = 0f; // v1

        verts[i++] = 1f; // x2
        verts[i++] = -1; // y2
        verts[i++] = 0;
        verts[i++] = 1f; // u2
        verts[i++] = 0f; // v2

        verts[i++] = 1f; // x3
        verts[i++] = 1f; // y3
        verts[i++] = 0;
        verts[i++] = 1f; // u3
        verts[i++] = 1f; // v3

        verts[i++] = -1; // x4
        verts[i++] = 1f; // y4
        verts[i++] = 0;
        verts[i++] = 0f; // u4
        verts[i] = 1f; // v4

        screenQuad = new Mesh(true, 4, 0,
                new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));
        screenQuad.setVertices(verts);


    }

    private static long getTimerQuery() {
        if (timerQuery != null) {
          return timerQuery.getQuery(0);
        }
        return 0;
    }
    private static Vector3 getTimeQueryVec() {
        if (timerQuery != null) {

            timerVec.set((float)timerQuery.getQuery(0) / 1000000f, (float)timerQuery.getQuery(1) / 1000000f, (float)timerQuery.getQuery(2) / 1000000f);
        }
        return timerVec;
    }

    //call this onInitialize and whenever the window resizes

    public static void resize() {
//        if (buffer != null) {
//            buffer.dispose();
//            try {
//                buffer = new RenderFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
        initTextureHolder();

    }

    public enum newShaderType {
        FRAG,
        VERT,
        IMPORTED
    }
}

