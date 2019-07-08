package com.github.echat.chat.utils;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;


import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.LogUtils;
import com.github.echat.chat.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * Created by xuhaoyang on 2018/1/22.
 */


public class RequestUtils {

    private final static String TAG = RequestUtils.class.getSimpleName();

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");//mdiatype 这个需要和服务端保持一致
    private static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");//mdiatype 这个需要和服务端保持一致
    private static final MediaType MEDIA_OBJECT_STREAM = MediaType.parse("application/octet-stream");
    private static final MediaType MEDIA_FORM_DATA = MediaType.parse("multipart/form-data");

    private static final String BASE_URL = "http://xxx.com/openapi";//请求接口根地址
    private static volatile RequestUtils mInstance;//单利引用
    public static final int TYPE_GET = 0;//get请求
    public static final int TYPE_POST_JSON = 1;//post请求参数为json
    public static final int TYPE_POST_FORM = 2;//post请求参数为表单
    private OkHttpClient mOkHttpClient;//okHttpClient 实例
    private OkHttpClient mOkHttpClient2;//okHttpClient 实例
    private Handler okHttpHandler;//全局处理子线程和M主线程通信
    private ArrayList<Call> updateCalls;
    private WeakHashMap<String, Call> downloadCalls;


    /*--------------DEBUG----------------*/
    public class LogInterceptor implements Interceptor {

        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            long startTime = System.currentTimeMillis();
            okhttp3.Response response = chain.proceed(chain.request());
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            okhttp3.MediaType mediaType = response.body().contentType();
            String content = response.body().string();
            //总输出
            StringBuilder logResult = new StringBuilder();
            logResult.append("\n")
                    .append("----------Start----------------")
                    .append("\n")
                    .append("| " + request.toString())
                    .append("\n");

            String method = request.method();
            if ("POST".equals(method)) {
                StringBuilder sb = new StringBuilder();
                if (request.body() instanceof FormBody) {
                    FormBody body = (FormBody) request.body();
                    for (int i = 0; i < body.size(); i++) {
                        sb.append(body.encodedName(i) + "=" + body.encodedValue(i) + ",");
                    }
                    sb.delete(sb.length() - 1, sb.length());
                    logResult.append("| RequestParams:{" + sb.toString() + "}").append("\n");
                } else if (request.body() instanceof MultipartBody) {

                    logResult.append("| RequestParams:{")
                            .append("\n");

                    MultipartBody body = (MultipartBody) request.body();
                    List<MultipartBody.Part> parts = body.parts();
                    for (MultipartBody.Part part : parts) {
                        Headers headers = part.headers();
                        LogUtils.i(headers.toString());
                        for (int i = 0; i < headers.names().size(); i++) {


                            String value = headers.value(i);//value form-data; name="article_type"
                            String bodycontent = bodyToString(part.body());
                            String replaceValue = "form-data; name=";//这段在MultipartBody.Part源码中看到
                            logResult
                                    .append("|      ")
                                    .append(value)
                                    .append(" ---- ");
                            if (!TextUtils.isEmpty(bodycontent) && bodycontent.length() < 2000) {
                                logResult.append(bodycontent);
                            } else {
                                logResult.append("文件流 省略");
                            }
                            if (headers.values("Content-Type").size() > 0) {
                                for (String s : headers.values("Content-Type")) {
                                    logResult.append(",")
                                            .append(s)
                                            .append(",");

                                }
                            }

                            if (i < headers.names().size() - 1) {
                                logResult.append(",");
                            }
                            logResult.append("\n");
//                            if (value.contains(replaceValue)) {
//                                String key = value.replace(replaceValue, "").replaceAll("\"", "");
//                                Log.d(TAG, "Part - Key =" + key);
//                                Log.d(TAG, "Part - value =" + bodyToString(part.body()));
//                                break;
//                            }
                        }
                    }

                    logResult.append("| }");
                }
            }

            logResult
                    .append("\n")
                    .append("| Response:" + content)
                    .append("| \n")
                    .append("----------End:" + duration + "毫秒----------");
            LogUtils.i(TAG, logResult.toString());
            return response.newBuilder()
                    .body(okhttp3.ResponseBody.create(mediaType, content))
                    .build();
        }

        private String bodyToString(final RequestBody request) {
            try {
                final RequestBody copy = request;
                final Buffer buffer = new Buffer();
                copy.writeTo(buffer);
                return buffer.readUtf8();
            } catch (final IOException e) {
                return "did not work";
            }
        }
    }
    /*--------------DEBUG----------------*/


    private RequestUtils(Context context) {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();

        //初始化OkHttpClient
        mOkHttpClient = builder
                .connectTimeout(60, TimeUnit.SECONDS)//设置超时时间
                .readTimeout(10, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(60, TimeUnit.SECONDS)//设置写入超时时间
                .build();
        mOkHttpClient2 = new OkHttpClient().newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)//设置超时时间
                .readTimeout(5, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(5, TimeUnit.SECONDS)//设置写入超时时间
                .build();
        //初始化Handler
        okHttpHandler = new Handler(context.getMainLooper());
        updateCalls = new ArrayList<>();
        downloadCalls = new WeakHashMap<>();
    }

    /**
     * 获取单例
     *
     * @param context
     * @return
     */
    public static RequestUtils getInstance(Context context) {
        RequestUtils inst = mInstance;
        if (inst == null) {
            synchronized (RequestUtils.class) {
                inst = mInstance;
                if (inst == null) {
                    inst = new RequestUtils(context.getApplicationContext());
                    mInstance = inst;
                }
            }
        }
        return inst;
    }

    /**
     * okHttp同步请求统一入口
     *
     * @param actionUrl   接口地址
     * @param requestType 请求类型
     * @param paramsMap   请求参数
     */
    public void requestSyn(String actionUrl, int requestType, HashMap<String, String> paramsMap) {
        switch (requestType) {
            case TYPE_GET:
                requestGetBySyn(actionUrl, paramsMap);
                break;
            case TYPE_POST_JSON:
                requestPostBySyn(actionUrl, paramsMap);
                break;
            case TYPE_POST_FORM:
                requestPostBySynWithForm(actionUrl, paramsMap);
                break;
        }
    }

    /**
     * okHttp get同步请求
     *
     * @param actionUrl 接口地址
     * @param paramsMap 请求参数
     */
    private void requestGetBySyn(String actionUrl, HashMap<String, String> paramsMap) {
        StringBuilder tempParams = new StringBuilder();
        try {
            //处理参数
            int pos = 0;
            for (String key : paramsMap.keySet()) {
                if (pos > 0) {
                    tempParams.append("&");
                }
                //对参数进行URLEncoder
                tempParams.append(String.format("%s=%s", key, URLEncoder.encode(paramsMap.get(key), "utf-8")));
                pos++;
            }
            //补全请求地址
            String requestUrl = String.format("%s/%s?%s", BASE_URL, actionUrl, tempParams.toString());
            //创建一个请求
            Request request = addHeaders().url(requestUrl).build();
            //创建一个Call
            final Call call = mOkHttpClient.newCall(request);
            //执行请求
            final Response response = call.execute();
            response.body().string();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    /**
     * okHttp post同步请求
     *
     * @param actionUrl 接口地址
     * @param paramsMap 请求参数
     */
    private void requestPostBySyn(String actionUrl, HashMap<String, String> paramsMap) {
        try {
            //处理参数
            StringBuilder tempParams = new StringBuilder();
            int pos = 0;
            for (String key : paramsMap.keySet()) {
                if (pos > 0) {
                    tempParams.append("&");
                }
                tempParams.append(String.format("%s=%s", key, URLEncoder.encode(paramsMap.get(key), "utf-8")));
                pos++;
            }
            //补全请求地址
            String requestUrl = String.format("%s/%s", BASE_URL, actionUrl);
            //生成参数
            String params = tempParams.toString();
            //创建一个请求实体对象 RequestBody
            RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, params);
            //创建一个请求
            final Request request = addHeaders().url(requestUrl).post(body).build();
            //创建一个Call
            final Call call = mOkHttpClient.newCall(request);
            //执行请求
            Response response = call.execute();
            //请求执行成功
            if (response.isSuccessful()) {
                //获取返回数据 可以是String，bytes ,byteStream
                Log.e(TAG, "response ----->" + response.body().string());
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    /**
     * okHttp post同步请求表单提交
     *
     * @param actionUrl 接口地址
     * @param paramsMap 请求参数
     */
    private void requestPostBySynWithForm(String actionUrl, HashMap<String, String> paramsMap) {
        try {
            //创建一个FormBody.Builder
            FormBody.Builder builder = new FormBody.Builder();
            for (String key : paramsMap.keySet()) {
                //追加表单信息
                builder.add(key, paramsMap.get(key));
            }
            //生成表单实体对象
            RequestBody formBody = builder.build();
            //补全请求地址
            String requestUrl = String.format("%s/%s", BASE_URL, actionUrl);
            //创建一个请求
            final Request request = addHeaders().url(requestUrl).post(formBody).build();
            //创建一个Call
            final Call call = mOkHttpClient2.newCall(request);
            //执行请求
            Response response = call.execute();
            if (response.isSuccessful()) {
                Log.e(TAG, "response ----->" + response.body().string());
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    /**
     * okHttp异步请求统一入口
     *
     * @param url         接口地址
     * @param requestType 请求类型
     * @param paramsMap   请求参数
     * @param callBack    请求返回数据回调
     * @param <T>         数据泛型
     **/
    public <T> Call requestAsyn(String url, int requestType, HashMap<String, String> paramsMap, ReqCallBack<T> callBack) {
        Call call = null;
        switch (requestType) {
            case TYPE_GET:
                call = requestGetByAsyn(url, paramsMap, callBack);
                break;
            case TYPE_POST_JSON:
                call = requestPostByAsyn(url, paramsMap, callBack);
                break;
            case TYPE_POST_FORM:
                call = requestPostByAsynWithForm(url, paramsMap, callBack);
                break;
        }
        return call;
    }

    /**
     * okHttp get异步请求
     *
     * @param url       接口地址
     * @param paramsMap 请求参数
     * @param callBack  请求返回数据回调
     * @param <T>       数据泛型
     * @return
     */
    private <T> Call requestGetByAsyn(String url, HashMap<String, String> paramsMap, final ReqCallBack<T> callBack) {
        try {
            String requestUrl = UrlUtils.appendParams(url, paramsMap);
            final Request request = addHeaders().url(requestUrl).build();
            final Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    failedCallBack("访问失败", callBack);
                    Log.e(TAG, e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String string = response.body().string();
                        successCallBack((T) string, callBack);
                    } else {
                        failedCallBack("服务器错误", callBack);
                    }
                }
            });
            return call;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }


    /**
     * okHttp post异步请求
     *
     * @param actionUrl 地址
     * @param paramsMap 请求参数
     * @param callBack  请求返回数据回调
     * @param <T>       数据泛型
     * @return
     */
    private <T> Call requestPostByAsyn(String actionUrl, HashMap<String, String> paramsMap, final ReqCallBack<T> callBack) {
        try {
            StringBuilder tempParams = new StringBuilder();
            int pos = 0;
            for (String key : paramsMap.keySet()) {
                if (pos > 0) {
                    tempParams.append("&");
                }
                tempParams.append(String.format("%s=%s", key, URLEncoder.encode(paramsMap.get(key), "utf-8")));
                pos++;
            }
            String params = tempParams.toString();
            RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, params);
            String requestUrl = actionUrl;
            final Request request = addHeaders().url(requestUrl).post(body).build();
            final Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    failedCallBack("访问失败", callBack);
                    Log.e(TAG, e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String string = response.body().string();
                        Log.e(TAG, "response ----->" + string);
                        successCallBack((T) string, callBack);
                    } else {
                        Log.e(TAG, response.body().toString());
                        failedCallBack("服务器错误", callBack);
                    }
                }
            });
            return call;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }

    /**
     * @param url
     * @param getParamsMap
     * @param jsonParam
     * @param callBack
     * @param <T>
     * @return
     */
    public <T> Call requestPostJsonByAsyn(String url, HashMap<String, String> getParamsMap, String jsonParam, final ReqCallBack<T> callBack) {
        try {
            String requestUrl = UrlUtils.appendParams(url, getParamsMap);
            RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, jsonParam);
            Log.e(TAG, "url: " + requestUrl);
            final Request request = addHeaders().url(requestUrl).post(body).build();
            final Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    failedCallBack("访问失败", callBack);
                    Log.e(TAG, e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String string = response.body().string();
                        Log.e(TAG, "response ----->" + string);
                        successCallBack((T) string, callBack);
                    } else {
                        Log.e(TAG, response.body().string());
                        failedCallBack("服务器错误", callBack);
                    }
                }
            });
            return call;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }

    /**
     * okHttp post异步请求表单提交
     *
     * @param actionUrl 地址
     * @param paramsMap 请求参数
     * @param callBack  请求返回数据回调
     * @param <T>       数据泛型
     * @return
     */
    private <T> Call requestPostByAsynWithForm(String actionUrl, HashMap<String, String> paramsMap, final ReqCallBack<T> callBack) {
        try {
            FormBody.Builder builder = new FormBody.Builder();
            for (String key : paramsMap.keySet()) {
                builder.add(key, paramsMap.get(key));
            }
            RequestBody formBody = builder.build();
            String requestUrl = actionUrl;
            final Request request = addHeaders().url(requestUrl).post(formBody).build();
            final Call call = mOkHttpClient2.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    failedCallBack("访问失败", callBack);
                    Log.e(TAG, e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String string = response.body().string();
                        Log.e(TAG, "response ----->" + string);
                        successCallBack((T) string, callBack);
                    } else {
                        failedCallBack("服务器错误", callBack);
                    }
                }
            });
            return call;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }

    /**
     * 统一为请求添加头信息
     *
     * @return
     */
    private Request.Builder addHeaders() {
        Request.Builder builder = new Request.Builder()
                .addHeader("Connection", "keep-alive")
                .addHeader("platform", "2")
                .addHeader("phoneModel", Build.MODEL)
                .addHeader("systemVersion", Build.VERSION.RELEASE);
        return builder;
    }

    /**
     * 统一同意处理成功信息
     *
     * @param result
     * @param callBack
     * @param <T>
     */
    private <T> void successCallBack(final T result, final ReqCallBack<T> callBack) {
        okHttpHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {
                    callBack.onReqSuccess(result);
                }
            }
        });
    }

    /**
     * 统一处理失败信息
     *
     * @param errorMsg
     * @param callBack
     * @param <T>
     */
    private <T> void failedCallBack(final String errorMsg, final ReqCallBack<T> callBack) {
        okHttpHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {
                    callBack.onReqFailed(errorMsg);
                }
            }
        });
    }

    /**
     * 取消同步方式的上传请求
     */
    public void cancelAllUploudRequestSync() {
        //取消全部call，包括同步异步
        if (!updateCalls.isEmpty()) {
            for (int i = 0; i < updateCalls.size(); i++) {
                updateCalls.get(i).cancel();
            }
        }
    }


    /**
     * 创建带进度的RequestBody
     *
     * @param contentType MediaType
     * @param file        准备上传的文件
     * @param callBack    回调
     * @param <T>
     * @return
     */
    public <T> RequestBody createProgressRequestBody(final MediaType contentType, final File file, final ReqProgressCallBack<T> callBack) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return file.length();
            }


            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Source source;

                try {
                    source = Okio.source(file);
                    Buffer buf = new Buffer();
                    long remaining = contentLength();
                    long current = 0;
                    for (long readCount; (readCount = source.read(buf, 2048)) != -1; ) {
                        sink.write(buf, readCount);
                        current += readCount;
                        progressCallBack(remaining, current, callBack);
                    }
                } catch (Exception e) {
                    LogUtils.eTag(TAG, e);
                    //failedCallBack("网络异常" + e.getLocalizedMessage(), callBack);
                }
            }
        };
    }

    class DownloadInfo {
        public static final long TOTAL_ERROR = -1;
        public String url;
        public long total;
        public long progress;
        public String fileName;
        public String outFileDir;

        public DownloadInfo(String url, String fileName, String destFileDir) {
            this.url = url;
            this.fileName = fileName;
            this.outFileDir = destFileDir;
        }
    }

    private DownloadInfo createDownInfo(String url, String fileName, String destFileDir) throws IOException {
        DownloadInfo downloadInfo = new DownloadInfo(url, fileName, destFileDir);
        downloadInfo.total = getContentLength(url);
        downloadInfo = checkFileName(downloadInfo);
        return downloadInfo;

    }

    private DownloadInfo checkFileName(DownloadInfo downloadInfo) {
        String fileName = downloadInfo.fileName;
        long downloadLength = 0, contentLength = downloadInfo.total;
        File file = new File(downloadInfo.outFileDir, downloadInfo.fileName);
        if (file.exists()) {
            //说明有这个文件
            downloadLength = file.length();
        }

        int count = 1;
        //如果下载大小大于总大小，就要下载一个新的
        if (contentLength > 0) {
            while (downloadLength > contentLength) {
                int dotIndex = fileName.lastIndexOf(".");
                String fileNameOther;
                if (dotIndex == -1) {
                    fileNameOther = fileName + "(" + count + ")";
                } else {
                    fileNameOther = fileName.substring(0, dotIndex)
                            + "(" + count + ")" + fileName.substring(dotIndex);
                }

                file = new File(downloadInfo.outFileDir, fileNameOther);
                downloadLength = file.length();
                count++;
            }
        }

        downloadInfo.progress = downloadLength;
        downloadInfo.fileName = file.getName();
        return downloadInfo;
    }

    /**
     * 获取下载长度
     * ps: 正常的下载是不会下载完才告诉我文件大小
     *
     * @param downloadUrl
     * @return
     */
    private long getContentLength(String downloadUrl) throws IOException {
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = mOkHttpClient.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            response.close();
            return contentLength <= 0 ? DownloadInfo.TOTAL_ERROR : contentLength;
        }

        return DownloadInfo.TOTAL_ERROR;
    }


    /**
     * 下载文件
     *
     * @param downloadUrl
     * @param fileName
     * @param destFileDir
     * @param callBack
     * @param <T>
     */
    public <T> void downLoadFile(String downloadUrl, String fileName, final String destFileDir, final ReqCallBack<T> callBack) {
        final File file = new File(destFileDir, fileName);
        if (!file.getParentFile().exists()) {//如果上级目录不存在就创建
            file.getParentFile().mkdirs();
        }
        if (file.exists()) {
            successCallBack((T) file, callBack);
            return;
        }
        final Request request = new Request.Builder().url(downloadUrl).build();
        final Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, e.toString());
                failedCallBack("下载失败", callBack);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    long total = response.body().contentLength();
                    long current = 0;
                    is = response.body().byteStream();
                    fos = new FileOutputStream(file);
                    while ((len = is.read(buf)) != -1) {
                        current += len;
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                    successCallBack((T) file, callBack);
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                    failedCallBack("下载失败", callBack);
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }
                }
            }
        });
    }


    /**
     * 取消下载
     *
     * @param url
     */
    public void cancelDownload(String url) {
        final Call call = downloadCalls.get(url);
        downloadCalls.remove(call);
        if (call != null) {
            call.cancel();
        }
    }

    public interface ReqProgressCallBack<T> extends ReqCallBack<T> {
        /**
         * 响应进度更新
         */
        void onProgress(long total, long current);
    }

    /**
     * 统一处理进度信息
     *
     * @param total    总计大小
     * @param current  当前进度
     * @param callBack
     * @param <T>
     */
    private <T> void progressCallBack(final long total, final long current, final ReqProgressCallBack<T> callBack) {
        okHttpHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {
                    callBack.onProgress(total, current);
                }
            }
        });
    }

    public interface ReqCallBack<T> {
        /**
         * 响应成功
         */
        void onReqSuccess(T result);

        /**
         * 响应失败
         */
        void onReqFailed(String errorMsg);
    }
}
