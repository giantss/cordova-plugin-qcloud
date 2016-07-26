package org.bike.giantss;

import android.app.ProgressDialog;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.tencent.upload.Const.FileType;
import com.tencent.upload.UploadManager;
import com.tencent.upload.task.Dentry;
import com.tencent.upload.task.ITask;
import com.tencent.upload.task.IUploadTaskListener;
import com.tencent.upload.task.VideoAttr;
import com.tencent.upload.task.data.FileInfo;
import com.tencent.upload.task.impl.ObjectStatTask;
import com.tencent.upload.task.impl.VideoUploadTask;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class QCloud extends CordovaPlugin{

    public static final String ERROR_INVALID_PARAMETERS = "参数格式错误";
    public  static  final String ERROR_SAME_FILE_UPLOAD = "相同文件已上传过";
    public  static  final String ERROR_PROXY_AUTH_INVOKE   = "调用签名服务失败";
    public  static  final String ERROR_PROXY_AUTH_FAILED    = "非法签名";
    public  static  final String ERROR_PROXY_AUTH_EXPIRED  = "签名过期";
    public static final String ERROR_PROXY_SIGN_BUCKET_NOTMATCH  = "bucket与签名中的bucket不匹配";
    public static final String ERROR_UNKNOWN = "未知错误";
    public static final String ERROR_JSON_EXCEPTION = "JSON格式错误";
    private static final String TAG = "QCloud";
    private String APP_ID;
    private static final String QCLOUD_APP_ID = "qcloud_app_id";
    private String destFilePath  ;  //远程相对路径
    private String srcFilePath    = null;  //视频文件的绝对路径
    private UploadManager mFileUploadManager = null;

    private String signUrl = "http://bbs.chinabike.net/phone/uploadvideo.php";
    private String oneSign = "http://bbs.chinabike.net/phone/uploadvideo_once.php";
    private String CBURL = "http://bbs.chinabike.net/phone/video_encode.php?action=video_info&playurl=";

    private String result = null;
    private String bucket = null;
    private String sign = null;
    private String signOne = null;
    private String persistenceId = null;
    private ProgressDialog m_pDialog = null;
    private VideoUploadTask videoUploadTask = null;
    private Handler mMainHandler = new Handler();
    private  boolean isUpload = true;
    private  String isEncodeSuccess = null;
    private  String relativeSDPath = null;
    private  String absoluteSDPath = null;

    private  String fixedURL = "http://cbiphone-10047633.video.myqcloud.com";

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
       APP_ID = webView.getPreferences().getString(QCLOUD_APP_ID, "");
        // 1，创建一个上传容器 需要1.appid 2.上传文件类型3.上传缓存（类型字符串，要全局唯一否则）
        bucket = "cbiphone";
        persistenceId = "chinabikeqcloudvideo";
        mFileUploadManager = new UploadManager(this.cordova.getActivity().getApplicationContext(), APP_ID, FileType.Video,persistenceId);
        m_pDialog = new ProgressDialog(this.cordova.getActivity());
        m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        m_pDialog.setIndeterminate(false);
        m_pDialog.setCancelable(false);
    }

    @Override
    public boolean execute(String action, CordovaArgs args,
                           final CallbackContext callbackContext) throws JSONException {
        if (action.equals("upLoadVideo")) {
           return upLoadVideo(args, callbackContext);
        }
        return super.execute(action, args, callbackContext);
    }

//    /**
//     * 视频上传任务
//     * @param bucket        bucket
//     * @param srcFilePath   本地绝对路径
//     * @param destFilePath  远程相对路径
//     * @param bizAttr       文件私有属性，选填
//     * @param videoAttr     视频私有属性，选填
//     * @param listener      文件上传结果监听器，选填
//     */

    // 上传视频
    public boolean upLoadVideo(CordovaArgs args, final CallbackContext callbackContext)throws JSONException {
        Log.d(TAG, "onUploadClicked");

        final JSONObject params;
        try {
            params = args.getJSONObject(0);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }
        srcFilePath    = params.getString("path");
        VideoAttr videoAttr = new VideoAttr();
        videoAttr.isCheck = false;
        videoAttr.title = "video1";
        videoAttr.desc = "cos-video-desc" + srcFilePath;
        videoAttr.coverUrl = "http://www.chinabike.net/uploadfile/2016/0719/20160719063757571.jpg";

        String[] strarray = srcFilePath   .split("[/]");
        destFilePath   = strarray[strarray.length - 1];
        m_pDialog.setMessage("上传进度: " + 0 + "%");
        m_pDialog.show();
        // 构建要上传的任务
        videoUploadTask = new VideoUploadTask(bucket, srcFilePath   , "/android/" + destFilePath  , "",videoAttr,true,new IUploadTaskListener() {
            @Override
            public void onUploadSucceed(final FileInfo result) {
                //Toast.makeText(cordova.getActivity().getApplicationContext(), "上传成功", Toast.LENGTH_SHORT).show();
                Log.i(TAG,String.valueOf(result));
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        //m_pDialog.hide();
                        m_pDialog.setMessage("视频处理中...");
                       String url = result.url;
                        //callbackContext.success(url);
                        relativeSDPath = "/10047633/cbiphone"+ url.substring(url.indexOf("/android"),url.length()) + ".f20.mp4";
                        getEncodeState(CBURL+relativeSDPath, callbackContext);


                        //queryImg();
                    }
                });
            }
            @Override
            public void onUploadStateChange(ITask.TaskState state) {
//                Toast.makeText(cordova.getActivity().getApplicationContext(), "上传状态改变", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUploadProgress(final long totalSize,
                                         final long sendSize) {
                long p = (long) ((sendSize * 100) / (totalSize * 1.0f));
                Log.i(TAG, "上传进度: " + p + "%");
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        long p = (long) ((sendSize * 100) / (totalSize * 1.0f));
                        m_pDialog.setMessage("上传进度: " + p + "%");
                    }
                });
            }

            @Override
            public void onUploadFailed(final int errorCode,
                                       final String errorMsg) {
                //Toast.makeText(cordova.getActivity().getApplicationContext(), "上传失败", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "上传结果:失败! ret:" + errorCode + " msg:" + errorMsg);
                switch(errorCode){
                    case -4018:
                        callbackContext.error(ERROR_SAME_FILE_UPLOAD);
                        break;
                    case -61:
                        callbackContext.error(ERROR_PROXY_SIGN_BUCKET_NOTMATCH);
                        break;
                    case -96:
                        callbackContext.error(ERROR_PROXY_AUTH_EXPIRED);
                        break;
                    case -97:
                        callbackContext.error(ERROR_PROXY_AUTH_FAILED);
                        break;
                    case -98:
                        callbackContext.error(ERROR_PROXY_AUTH_INVOKE);
                        break;
                    default:
                        callbackContext.error(ERROR_UNKNOWN);
                }
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        m_pDialog.hide();
                        Toast.makeText(cordova.getActivity().getApplicationContext(),
                                errorCode + " msg:" + errorMsg,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        // 去用户的业务务器获取签名
        getUploadImageSign(signUrl, videoUploadTask, callbackContext);
        return true;
    }


    // 查询视频
    public void queryImg() {
        ObjectStatTask filetask = null;
        filetask = new ObjectStatTask(FileType.Video, bucket, "/android/"+ destFilePath
                , Dentry.VIDEO, new ObjectStatTask.IListener() {
            @Override
            public void onSuccess(final ObjectStatTask.CmdTaskRsp result) {

                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Log.i(TAG, "-------:query:");
                        Dentry dentry = result.inode;
                     /*   String info = "name:" + dentry.name + "\n";
                        info += " sha:" + dentry.sha + "\n";
                        info += " path:" + dentry.path + "\n";
                        info += " type:" + dentry.type + "\n";
                        info += " accessUrl:" + dentry.accessUrl + "\n";
                        info += " attribute:" + dentry.attribute + "\n";
                        info += " fileSize:" + dentry.fileSize + "\n";
                        info += " fileLength:" + dentry.fileLength + "\n";
                        info += " createTime:" + dentry.createTime + "\n";
                        info += " modifyTime:" + dentry.modifyTime + "\n";
                        info += " authority:" + dentry.eauth;
                        Log.i(TAG, "-------:query:" + info);*/
                    }
                });
            }

            @Override
            public void onFailure(final int ret, final String msg) {
                Log.d(TAG, "查询结果:失败! ret:" + ret + " msg:" + msg);
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

                    }
                });
            }
        });
        filetask.setAuth(sign);
        mFileUploadManager.sendCommand(filetask);
    }

    /**
     * 转码回调
     * @param path
     * @param callbackContext
     */
    private void  getEncodeState(final String path, final CallbackContext callbackContext){
        try {
            URL url = new URL(path);
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();
            InputStreamReader in = new InputStreamReader(urlConnection
                    .getInputStream());
            BufferedReader buffer = new BufferedReader(in);
            String inpuLine = null;
            while ((inpuLine = buffer.readLine()) != null) {
                result = inpuLine + "\n";
            }
            JSONObject jsonData = new JSONObject(result);
            isEncodeSuccess = jsonData.getString("success");
            Log.i(TAG,isEncodeSuccess);
            if(isEncodeSuccess.equals("1")){
                m_pDialog.hide();
                String cbUrl = jsonData.getString("playurl");
                String cbCoverUrl = jsonData.getString("cover_url");
                absoluteSDPath = fixedURL + cbUrl.substring(cbUrl.indexOf("/android"),cbUrl.length());
                callbackContext.success(makeJson(absoluteSDPath, cbCoverUrl, callbackContext));

            }else{
                getEncodeState(CBURL+relativeSDPath,callbackContext);
            }


        } catch (Exception e) {
            // TODO: handle exception
        }
    }
    // 获取app 的签名
    private void getUploadImageSign(final String s, final VideoUploadTask videoUploadTask, final  CallbackContext callbackContext ) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    URL url = new URL(s);
                    HttpURLConnection urlConnection = (HttpURLConnection) url
                            .openConnection();
                    InputStreamReader in = new InputStreamReader(urlConnection
                            .getInputStream());
                    BufferedReader buffer = new BufferedReader(in);
                    String inpuLine = null;
                    while ((inpuLine = buffer.readLine()) != null) {
                        result = inpuLine + "\n";
                    }
                    JSONObject jsonData = new JSONObject(result);
                    sign = jsonData.getString("sign");
                    Log.i(TAG,sign);
                    videoUploadTask.setAuth(sign);
                    isUpload = mFileUploadManager.upload(videoUploadTask); // 开始上传
                    Log.i(TAG,String.valueOf(isUpload));
                } catch (Exception e) {
                    callbackContext.error(String.valueOf(e));
                    // TODO: handle exception
                }
            }
        }).start();

    }
    /**
     * 组装JSON
     *
     * @param absoluteSDPath
     * @param cbCoverUrl
     * @return
     */
    private JSONObject makeJson(String absoluteSDPath, String cbCoverUrl, final  CallbackContext callbackContext) {
        String json = "{\"absoluteSDPath\": \"" + absoluteSDPath + "\", " + " \"cbCoverUrl\": \"" + cbCoverUrl + "\"}";
        JSONObject jo = null;
        try {
            jo = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            callbackContext.error(ERROR_JSON_EXCEPTION);
        }
        return jo;
    }
}
