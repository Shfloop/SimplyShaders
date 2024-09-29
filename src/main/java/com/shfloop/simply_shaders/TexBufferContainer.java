package com.shfloop.simply_shaders;



public class TexBufferContainer {
    public static final int NUM_FLOATS_PER_FACE_UVTEXBUFF = 12;
    public float[] floats = new float[NUM_FLOATS_PER_FACE_UVTEXBUFF];

    public TexBufferContainer() {
    }

    public TexBufferContainer(TexBufferContainer tmpFloats) {
        for(int i = 0; i < this.floats.length; ++i) {
            this.floats[i] = tmpFloats.floats[i];
        }
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;

        for(int i = 0; i < this.floats.length; ++i) {
            result = 31 * result + Float.floatToIntBits(this.floats[i]);
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            TexBufferContainer other = (TexBufferContainer) obj;
            if (this.floats.length != other.floats.length) {
                return false;
            } else {
                for(int i = 0; i < this.floats.length; ++i) {
                    if (Float.floatToIntBits(this.floats[i]) != Float.floatToIntBits(other.floats[i])) {
                        return false;
                    }
                }

                return true;
            }
        }
    }
}
