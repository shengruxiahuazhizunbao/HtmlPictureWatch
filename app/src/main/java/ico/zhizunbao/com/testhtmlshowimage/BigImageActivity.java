package ico.zhizunbao.com.testhtmlshowimage;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPreviewActivity;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by food on 2017/10/11.
 */

public class BigImageActivity extends Activity {
    private ImageView iv_show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_image);
        initView();
    }
    private void initView() {
        iv_show = (ImageView) findViewById(R.id.iv_show);
        Glide.with(this).load(getIntent().getStringExtra("URL").toString()).into(iv_show);
    }



}
