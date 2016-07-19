package org.bike.giantss;

import android.app.ProgressDialog;
import android.os.Handler;
import android.text.TextUtils;
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
    public static final String ERROR_UNKNOWN = "未知错误";
    private static final String TAG = "QCloud";
    private String APP_ID;
    private static final String QCLOUD_APP_ID = "qcloud_app_id";
    private static int RESULT_LOAD_VIDEO = 2;

    private String destFilePath  ;  //远程相对路径
    private String srcFilePath    = null;  //视频文件的绝对路径
    private UploadManager mFileUploadManager = null;

    private String signUrl = "http://bbs.chinabike.net/phone/uploadvideo.php";
    private String oneSign = "http://bbs.chinabike.net/phone/uploadvideo_once.php";

    private String result = null;
    private String bucket = null;
    private String sign = null;
    private String signOne = null;
    private String persistenceId = null;
    private ProgressDialog m_pDialog = null;
    private VideoUploadTask videoUploadTask = null;
    private Handler mMainHandler = new Handler();

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
       APP_ID = webView.getPreferences().getString(QCLOUD_APP_ID, "");
        // 去用户的业务务器获取签名
        getUploadImageSign(signUrl);
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

        if (TextUtils.isEmpty(srcFilePath   )) {
            Toast.makeText(this.cordova.getActivity().getApplicationContext(), "请先选择文件", Toast.LENGTH_SHORT).show();
            return true;
        }

        VideoAttr videoAttr = new VideoAttr();
        videoAttr.isCheck = false;
        videoAttr.title = "red-1";
        videoAttr.desc = "cos-video-desc" + srcFilePath   ;

        String[] strarray = srcFilePath   .split("[/]");
        destFilePath   = strarray[strarray.length - 1];

        m_pDialog.show();
        // 构建要上传的任务
        //IUploadTaskListener iUploadTaskListener =
        videoUploadTask = new VideoUploadTask(bucket, srcFilePath   , "/" + destFilePath  , "",videoAttr,true,new IUploadTaskListener() {
            @Override
            public void onUploadSucceed(final FileInfo result) {
                //Toast.makeText(cordova.getActivity().getApplicationContext(), "上传成功", Toast.LENGTH_SHORT).show();
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        m_pDialog.hide();
                       String url = result.url;
                        callbackContext.success(url);
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
        Toast.makeText(cordova.getActivity().getApplicationContext(), sign, Toast.LENGTH_SHORT).show();
        videoUploadTask.setAuth(sign);
       Boolean r = mFileUploadManager.upload(videoUploadTask); // 开始上传
        Toast.makeText(cordova.getActivity().getApplicationContext(), r.toString(), Toast.LENGTH_SHORT).show();
        return true;
    }


    // 查询视频
    public void queryImg() {
        ObjectStatTask filetask = null;
        filetask = new ObjectStatTask(FileType.Video, bucket, "/VID_20160718_153409.mp4"
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
    // 获取app 的签名
    private void getUploadImageSign(final String s) {
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
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }).start();

    }
    /**
     * 组装JSON
     *
     * @param access_token
     * @param userid
     * @param expires_time
     * @return
     */
    private JSONObject makeJson(String access_token, String userid, long expires_time) {
        String json = "{\"access_token\": \"" + access_token + "\", " +
                " \"userid\": \"" + userid + "\", " +
                " \"expires_time\": \"" + String.valueOf(expires_time) + "\"" +
                "}";
        JSONObject jo = null;
        try {
            jo = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo;
    }
}
