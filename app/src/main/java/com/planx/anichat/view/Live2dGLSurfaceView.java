package com.planx.anichat.view;

/**
 * Created by Administrator on 2017/5/11.
 */

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import com.planx.anichat.MyApplication;
import com.planx.anichat.activity.video.CallActivity;
import com.planx.facecapture.CameraActivity;
import com.planx.live2d.framework.L2DEyeBlink;
import com.planx.live2d.framework.L2DStandardID;

import java.io.InputStream;
import java.util.Locale;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import jp.live2d.android.Live2DModelAndroid;
import jp.live2d.android.UtOpenGL;

public class Live2dGLSurfaceView extends GLSurfaceView implements  GLSurfaceView.Renderer {

    private boolean isMe;

    private float color = 0f;
    private CallActivity mActivity;
    private Live2DModelAndroid live2DModel;
    private L2DEyeBlink mEyeBlink;
    private OnFrameAvailableListener mOnFrameAvailableHandler;
    private String TAG = "Live2D";

    private float wRatio, hRatio;


    private EGLContext mEGLCurrentContext;
//    private OnEGLContextListener mOnEGLContextHandler;

    //private L2DMotionManager mMotionManager;

    private Context mContext;

    private static class MyContextFactory implements EGLContextFactory {
        private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

        private Live2dGLSurfaceView mRenderer;

        public MyContextFactory(Live2dGLSurfaceView renderer) {
            Log.d("MyContextFactory", "MyContextFactory " + renderer);
            this.mRenderer = renderer;
        }

        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
            Log.d("MyContextFactory", "createContext " + egl + " " + display + " " + eglConfig);
            checkEglError("before createContext", egl);
            int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};

            EGLContext ctx;

            if (mRenderer.mEGLCurrentContext == null) {
                mRenderer.mEGLCurrentContext = egl.eglCreateContext(display, eglConfig,
                        EGL10.EGL_NO_CONTEXT, attrib_list);
                ctx = mRenderer.mEGLCurrentContext;
            } else {
                ctx = mRenderer.mEGLCurrentContext;
            }
            checkEglError("after createContext", egl);
            return ctx;
        }

        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            Log.d("MyContextFactory", "destroyContext " + egl + " " + display + " " + context + " " + mRenderer.mEGLCurrentContext);
            if (mRenderer.mEGLCurrentContext == null) {
                egl.eglDestroyContext(display, context);
            }
        }

        private static void checkEglError(String prompt, EGL10 egl) {
            int error;
            while ((error = egl.eglGetError()) != EGL10.EGL_SUCCESS) {
                Log.d("MyContextFactory", String.format(Locale.US, "%s: EGL error: 0x%x", prompt, error));
            }
        }
    }
    public Live2dGLSurfaceView(Context context) {
        super(context);
        this.mContext = context;
    }
    public Live2dGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

/*    public void setOnEGLContextHandler(OnEGLContextListener listener) {
        this.mOnEGLContextHandler = listener;
    }
    public interface OnEGLContextListener {
        void onEGLContextReady(EGLContext eglContext);
    }*/
    public interface OnFrameAvailableListener {
        void onFrameAvailable(int texture, EGLContext eglContext, int rotation);
    }

    public void setOnFrameAvailableHandler(OnFrameAvailableListener listener) {
        this.mOnFrameAvailableHandler = listener;
    }
//    @Override
//    public synchronized void onFrameAvailable(SurfaceTexture surfaceTexture) {
//        requestRender();
//    }
    public void init(boolean isMe,CallActivity activity,
                     float wRatio, float hRatio) {
//        final String MODEL_PATH = "live2d/haru/haru.moc";
//        final String[] TEXTURE_PATHS = {
//                "live2d/haru/haru.1024/texture_00.png",
//                "live2d/haru/haru.1024/texture_01.png",
//                "live2d/haru/haru.1024/texture_02.png"
//        };

//        setEGLContextClientVersion(1);
//        setEGLContextFactory(new MyContextFactory(this));
//        setPreserveEGLContextOnPause(true);
        this.isMe = isMe;
        setUpModel(activity, wRatio, hRatio);
        setRenderer(this);
//        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);


    }
    public void setUpModel(CallActivity activity,
                           float wRatio, float hRatio) {
        this.mActivity = activity;

        this.wRatio = wRatio;
        this.hRatio = hRatio;

        this.mEyeBlink = new L2DEyeBlink();
    }

    private void loadLive2dModel(GL10 gl, String modelPath, String[] texturePath) {
        try {
            InputStream in = this.mActivity.getAssets().open(modelPath);
            live2DModel = Live2DModelAndroid.loadModel(in);
            in.close();

            for (int i = 0; i < texturePath.length; i++) {
                InputStream tin = this.mActivity.getAssets().open(texturePath[i]);
                int texNo = UtOpenGL.loadTexture(gl, tin, true);
                live2DModel.setTexture(i, texNo);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
//        GLES20.glClearColor(0.0f, 255f, color, 1.0f);
//        color=(color+1)%255;
//        Log.i("onDrawFrame","++++++++++++++++++++");
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        //live2DModel.loadParam();
        //boolean update = mMotionManager.updateParam(live2DModel);
        mEyeBlink.updateParam(live2DModel);
        //live2DModel.saveParam();

        if(isMe) {
            live2DModel.setParamFloat(L2DStandardID.PARAM_ANGLE_Z, (float) MyApplication.emotion[0], 0.75f);
            live2DModel.setParamFloat(L2DStandardID.PARAM_ANGLE_X, (float) MyApplication.emotion[1], 0.75f);
            live2DModel.setParamFloat(L2DStandardID.PARAM_ANGLE_Y, (float) MyApplication.emotion[2], 0.75f);
            live2DModel.setParamFloat(L2DStandardID.PARAM_MOUTH_OPEN_Y, (float) MyApplication.emotion[3], 0.75f);
            live2DModel.setParamFloat(L2DStandardID.PARAM_MOUTH_FORM, (float) MyApplication.emotion[3], 0.75f);
        }else{
            live2DModel.setParamFloat(L2DStandardID.PARAM_ANGLE_Z, (float) MyApplication.emotionH[0], 0.75f);
            live2DModel.setParamFloat(L2DStandardID.PARAM_ANGLE_X, (float) MyApplication.emotionH[1], 0.75f);
            live2DModel.setParamFloat(L2DStandardID.PARAM_ANGLE_Y, (float) MyApplication.emotionH[2], 0.75f);
            live2DModel.setParamFloat(L2DStandardID.PARAM_MOUTH_OPEN_Y, (float) MyApplication.emotionH[3], 0.75f);
            live2DModel.setParamFloat(L2DStandardID.PARAM_MOUTH_FORM, (float) MyApplication.emotionH[3], 0.75f);
        }
        live2DModel.setGL(gl);
        live2DModel.update();
        live2DModel.draw();

//        if (mOnFrameAvailableHandler != null) {
//            mOnFrameAvailableHandler.onFrameAvailable(1, mEGLCurrentContext, 0);
//        }
    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        gl.glViewport(0 , 0 , width , height);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        float modelWidth = live2DModel.getCanvasWidth();
        float aspect = (float)width/height;

        gl.glOrthof(0, wRatio*modelWidth, hRatio*modelWidth / aspect, 0, 0.5f, -0.5f);
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d("Render", "onSurfaceCreated");
//        CameraPreview cameraPreview = new CameraPreview(mActivity);
//        cameraPreview.init(mActivity);
        loadLive2dModel(gl, MyApplication.MODEL_PATH, MyApplication.TEXTURE_PATHS);
//        if (mOnEGLContextHandler != null) {
//            if (mEGLCurrentContext != null) {
//                mOnEGLContextHandler.onEGLContextReady(mEGLCurrentContext);
//            }
//        }

    }
}

