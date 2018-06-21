package com.planx.facecapture;

/**
 * Created by Administrator on 2017/5/11.
 */

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class Live2dGLSurfaceView extends GLSurfaceView {

    private String TAG = "Live2D";

    Live2dRenderer mLive2dRenderer;

    //private L2DMotionManager mMotionManager;

    private Context mContext;

    public Live2dGLSurfaceView(Context context) {
        super(context);
        this.mContext = context;
    }
    public Live2dGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void init(CameraActivity activity, String MODEL_PATH, String[] TEXTURE_PATHS,
                     float wRatio, float hRatio) {
//        final String MODEL_PATH = "live2d/haru/haru.moc";
//        final String[] TEXTURE_PATHS = {
//                "live2d/haru/haru.1024/texture_00.png",
//                "live2d/haru/haru.1024/texture_01.png",
//                "live2d/haru/haru.1024/texture_02.png"
//        };

        this.mLive2dRenderer = new Live2dRenderer();
        this.mLive2dRenderer.setUpModel(activity, MODEL_PATH, TEXTURE_PATHS, wRatio, hRatio);
        this.setRenderer(this.mLive2dRenderer);
    }
}

