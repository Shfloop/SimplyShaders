package com.shfloop.simply_shaders.rendering;

import com.shfloop.simply_shaders.SimplyShaders;
import org.lwjgl.opengl.GL33C;

public class TimerQuery {


    final int[][] queryID;
    int queryA = 0;
    int queryB = 1;
    public final int NUM_QUERIES;
    public TimerQuery(int numQueries) {
        NUM_QUERIES = numQueries;
        queryID = new int[2][numQueries];
        for (int i = 0; i < numQueries; i++) {
            queryID[queryA][i] = GL33C.glGenQueries();
            queryID[queryB][i] = GL33C.glGenQueries();
            //dummy query to prevent the first frame read from generating glError
           // GL33C.glQueryCounter(queryID[queryA][i], GL33C.GL_TIMESTAMP);
            GL33C.glBeginQuery(GL33C.GL_TIME_ELAPSED, queryID[queryB][i]);
            GL33C.glEndQuery(GL33C.GL_TIME_ELAPSED);
        }


    }

    public void swapQueryBuffers() { //Comes after endQuery and before getQuery

        if (queryA != 0) {
            queryA = 0;
            queryB =1;
        } else {
            queryA = 1;
            queryB = 0;
        }
    }
    public long getQuery(int queryNum) {//change this to queryA if you want to swap after get
       return GL33C.glGetQueryObjecti64(queryID[queryB][queryNum], GL33C.GL_QUERY_RESULT) ;
    }
    public void startQuery(int queryNum) {
        GL33C.glBeginQuery(GL33C.GL_TIME_ELAPSED, queryID[queryB][queryNum]);
    }
    public void endQuery() {
        GL33C.glEndQuery(GL33C.GL_TIME_ELAPSED);
    }
    public void dispose() {
        for (int i = 0; i < NUM_QUERIES; i++) {
            GL33C.glDeleteQueries(queryID[queryA][i]);
            GL33C.glDeleteQueries(queryID[queryB][i]);
        }
    }

}
