package com.planx.facecapture;

import android.opengl.GLSurfaceView;
import android.util.Log;

import com.planx.anichat.MyApplication;
import com.planx.live2d.framework.L2DEyeBlink;
import com.planx.live2d.framework.L2DStandardID;

import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.live2d.android.Live2DModelAndroid;
import jp.live2d.android.UtOpenGL;

/**
 * Author: WoDeFeiZhu
 * Date: 2018/6/18
 */
public class Live2dRenderer implements GLSurfaceView.Renderer {
    private CameraActivity mActivity;

    private Live2DModelAndroid live2DModel;
    private L2DEyeBlink mEyeBlink;


    private int tag;

    private float wRatio, hRatio;

    public void setUpModel(CameraActivity activity, int tag,
                           float wRatio, float hRatio) {
        this.mActivity = activity;
        this.tag = tag;
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
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        //live2DModel.loadParam();
        //boolean update = mMotionManager.updateParam(live2DModel);
        mEyeBlink.updateParam(live2DModel);
        //live2DModel.saveParam();

        live2DModel.setParamFloat(L2DStandardID.PARAM_ANGLE_Z, (float) MyApplication.emotion[0], 0.75f);
        live2DModel.setParamFloat(L2DStandardID.PARAM_ANGLE_X , (float) MyApplication.emotion[1], 0.75f);
        live2DModel.setParamFloat(L2DStandardID.PARAM_ANGLE_Y , (float) MyApplication.emotion[2], 0.75f);
        live2DModel.setParamFloat(L2DStandardID.PARAM_MOUTH_OPEN_Y, (float) MyApplication.emotion[3], 0.75f);
        live2DModel.setParamFloat(L2DStandardID.PARAM_MOUTH_FORM, (float) MyApplication.emotion[3], 0.75f);

        live2DModel.setGL(gl);
        live2DModel.update();
        live2DModel.draw();
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
        loadLive2dModel(gl, MyApplication.getModelPath(tag), MyApplication.getTexturePaths(tag));
    }
}
