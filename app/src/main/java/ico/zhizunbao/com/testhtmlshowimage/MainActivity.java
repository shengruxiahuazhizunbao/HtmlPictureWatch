package ico.zhizunbao.com.testhtmlshowimage;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPreviewActivity;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * 运用Jsoup获取网页的里面所有的图片（不能获取JS里面动态的图片）
 * 通过java 和Js来代码，通过点击网页上面的图片进行交互，jsoup拿到所有的图片的集合之后，获取当前点击的图片，
 * 同时计算出该图片在所有图片中的位置，传递给大图界面，进行浏览缩放。
 *
 */
//远程修改，本地下载
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    public final static String webUrl="http://www.ukanmi.com/jingdianyul/id_499.html";

   // public final static String webUrl="http://www.jianshu.com/p/6de8e96ee561";
   static ArrayList<String> list=new ArrayList<>();
    private WebView mWebView;
    private static final int REQUEST_CODE_PERMISSION_PHOTO_PREVIEW = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);
        main();
      initView();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (requestCode == REQUEST_CODE_PERMISSION_PHOTO_PREVIEW) {
            Toast.makeText(this, "您拒绝了「图片预览」所需要的相关权限!", Toast.LENGTH_SHORT).show();
        }
    }



    /**
     * 图片预览，兼容6.0动态权限
     */
    @AfterPermissionGranted(REQUEST_CODE_PERMISSION_PHOTO_PREVIEW)
    private void photoPreviewWrapper(int postion) {

        // 保存图片的目录，改成你自己要保存图片的目录。如果不传递该参数的话就不会显示右上角的保存按钮
        File downloadDir = new File(Environment.getExternalStorageDirectory(), "BGAPhotoPickerDownload");

        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            if (list.size() == 1) {
                // 预览单张图片
                startActivity(BGAPhotoPreviewActivity.newIntent(this, downloadDir,list.get(0)));
            } else if (list.size() > 1) {
                // 预览多张图片

                startActivity(BGAPhotoPreviewActivity.newIntent(this, downloadDir, list, postion));
            }
        } else {
            EasyPermissions.requestPermissions(this, "图片预览需要以下权限:\n\n1.访问设备上的照片",
                    REQUEST_CODE_PERMISSION_PHOTO_PREVIEW, perms);
        }
    }



    /**
     * 設置網頁中圖片的點擊事件
     * @param view
     */
    private  void setWebImageClick(WebView view) {
        String jsCode="javascript:(function(){" +
                                "var imgs=document.getElementsByTagName(\"img\");" +
                                "for(var i=0;i<imgs.length;i++){" +
                                "imgs[i].onclick=function(){" +
                                "window.jsCallJavaObj.showBigImg(this.src,i);" +
                                "}}})()";
                        mWebView.loadUrl(jsCode);
    }



    public  void main() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                list.clear();
                // 利用Jsoup获得连接
                Connection connect = Jsoup.connect(webUrl);
                // Connection connect = Jsoup.connect("http://www.knowsky.com/1049746" +
                //        ".html");


                // 得到Document对象
                Document document = null;
                try {
                    document = connect.get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 查找所有img标签
                Elements imgs = document.getElementsByTag("img");
                System.out.println("共检测到异步线程下列图片URL：");
                System.out.println("开始下载");
                // 遍历img标签并获得src的属性
                for (Element element : imgs) {
                    //获取每个img标签URL "abs:"表示绝对路径
                    String imgSrc = element.attr("abs:src");
                    // 打印URL
                    System.out.println(imgSrc);
                    if(!imgSrc.endsWith(".gif")){
                    list.add(imgSrc);
                    }
                    //下载图片到本地
                }
            }
        }).start();


    };



    private void initView() {
        mWebView = (WebView) findViewById(R.id.web);

        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
//        mWebView.loadUrl("http://www.knowsky.com/1049746.html");
      mWebView.loadUrl(webUrl);
        //java回调js代码，不要忘了@JavascriptInterface这个注解，不然点击事件不起作用
        mWebView.addJavascriptInterface(new JsCallJavaObj() {
            @JavascriptInterface
            @Override
            public void showBigImg(String url,int postion) {
                int a=0;
                if(list.contains(url)){
                     a=list.indexOf(url);
                }
                photoPreviewWrapper(a);
            }
        },"jsCallJavaObj");
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setWebImageClick(view);

            }
        });
    }


    /**
     * Js調用Java接口
     */
    private interface JsCallJavaObj{
        void showBigImg(String url,int postion);
    }
}
