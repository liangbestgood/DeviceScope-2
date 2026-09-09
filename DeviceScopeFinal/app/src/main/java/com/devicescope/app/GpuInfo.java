package com.devicescope.app;

import android.opengl.EGL14;
import android.opengl.EGLDisplay;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLSurface;
import android.opengl.GLES20;

public final class GpuInfo {
    private GpuInfo() {}
    public static String renderer() {
        EGLDisplay d=EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if(d==EGL14.EGL_NO_DISPLAY) return "Unknown GPU";
        int[] v=new int[2]; if(!EGL14.eglInitialize(d,v,0,v,1)) return "Unknown GPU";
        int[] a={EGL14.EGL_RENDERABLE_TYPE,EGL14.EGL_OPENGL_ES2_BIT,EGL14.EGL_SURFACE_TYPE,EGL14.EGL_PBUFFER_BIT,EGL14.EGL_NONE};
        EGLConfig[] cs=new EGLConfig[1]; int[] n=new int[1]; if(!EGL14.eglChooseConfig(d,a,0,cs,0,1,n,0)||n[0]==0){EGL14.eglTerminate(d);return "Unknown GPU";}
        int[] ca={EGL14.EGL_CONTEXT_CLIENT_VERSION,2,EGL14.EGL_NONE}; EGLContext c=EGL14.eglCreateContext(d,cs[0],EGL14.EGL_NO_CONTEXT,ca,0);
        int[] pa={EGL14.EGL_WIDTH,1,EGL14.EGL_HEIGHT,1,EGL14.EGL_NONE}; EGLSurface s=EGL14.eglCreatePbufferSurface(d,cs[0],pa,0);
        String r="Unknown GPU"; if(c!=EGL14.EGL_NO_CONTEXT&&s!=EGL14.EGL_NO_SURFACE&&EGL14.eglMakeCurrent(d,s,s,c)){String x=GLES20.glGetString(GLES20.GL_RENDERER); if(x!=null)r=x;}
        EGL14.eglMakeCurrent(d,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_CONTEXT); if(s!=EGL14.EGL_NO_SURFACE)EGL14.eglDestroySurface(d,s); if(c!=EGL14.EGL_NO_CONTEXT)EGL14.eglDestroyContext(d,c); EGL14.eglTerminate(d); return r;
    }
}
