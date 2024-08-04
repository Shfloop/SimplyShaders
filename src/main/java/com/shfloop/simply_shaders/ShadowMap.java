package com.shfloop.simply_shaders;

import com.badlogic.gdx.Gdx;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

public class ShadowMap {

    //goping from 4096 to 2048 improvedf fps from 500 to 700 closer to 1/2 framerate i think i was getting bottlenecked somewhere withthe larger shadow map
    public static final int SHADOW_MAP_WIDTH = 2048;
    public static final int SHADOW_MAP_HEIGHT = 2048;
    private final int depth_map_fbo;
    private final ShadowTexture depth_map;

    public ShadowMap() throws Exception {
        depth_map_fbo = Gdx.gl.glGenFramebuffer();
        depth_map = new ShadowTexture(SHADOW_MAP_WIDTH,SHADOW_MAP_HEIGHT, GL20.GL_DEPTH_COMPONENT);
        Gdx.gl.glBindFramebuffer(GL32.GL_FRAMEBUFFER,depth_map_fbo); //GL_FRAMEBUFFER
        Gdx.gl.glFramebufferTexture2D(GL32.GL_FRAMEBUFFER, 36096, GL20.GL_TEXTURE_2D, depth_map.id, 0);//GL20.GL_DEPTH_ATTACHMENT
        GL20.glDrawBuffer(GL20.GL_NONE);
        GL20.glReadBuffer(GL20.GL_NONE);

        if (Gdx.gl.glCheckFramebufferStatus(36160) != 36053) { //GL_FRAMEBUFFER, GL_FRAMEBUFFER_COMPLETE
            throw new Exception("Could not create FrameBuffer");
        }


        Gdx.gl.glBindFramebuffer(36160, 0);
    }

    public ShadowTexture getDepthMapTexture() {
        return depth_map;
    }
    public int getDepthMapFbo() {
        return depth_map_fbo;
    }
    public void cleanup() {
        Gdx.gl.glDeleteFramebuffer(depth_map_fbo);
        this.depth_map.cleanup();//not sure how to get rid of shadowTexture i just used gldeleteTextures might work
    }


}

