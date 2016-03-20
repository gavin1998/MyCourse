package com.example.cfy.clip;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.example.cfy.mycourse.R;
import com.example.cfy.util.ImageTools;

public class ClipActivity extends Activity{
    final static int IMAGEWIDTH=600;
    private int mHorizontalPadding =30;
    private ClipImageLayout mClipImageLayout;
	private String path; //图像文件的绝对名称
	private ProgressDialog loadingDialog;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println("Start clipactivity");
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clipimage);
		//全屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		loadingDialog=new ProgressDialog(this);
		loadingDialog.setTitle("请稍候...");

		//获取传递来的文件，保存在path (绝对文件名称）
		path=getIntent().getStringExtra("path");

		//path 为非空字符串，且文件存在
		if(TextUtils.isEmpty(path)||!(new File(path).exists())){
			Toast.makeText(this, "图片加载失败",Toast.LENGTH_SHORT).show();
			return;
		}

		//把图像文件压缩到指定大小
		Bitmap bitmap=ImageTools.convertToBitmap(path, IMAGEWIDTH,IMAGEWIDTH);
		if(bitmap==null){
			Toast.makeText(this, "图片加载失败",Toast.LENGTH_SHORT).show();
			return;
		}
		//显示图像
		mClipImageLayout = (ClipImageLayout) findViewById(R.id.id_clipImageLayout);
        mClipImageLayout.setHorizontalPadding(mHorizontalPadding);
		mClipImageLayout.setBitmap(bitmap);

		//设置确定按钮的动作：保持文件并返回
		((Button)findViewById(R.id.id_action_clip)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//显示请稍候对话框
				loadingDialog.show();
				new Thread(new Runnable() {
					@Override
					public void run() {
						//对图像进行裁剪
						Bitmap bitmap = mClipImageLayout.clip();

						//将bitmap转换为byte方式返回
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
						byte[] byteArray = stream.toByteArray();
						setResult(RESULT_OK, new Intent().putExtra("bitmap", byteArray));

						//取消请稍候对话框
						loadingDialog.dismiss();

						//结束本activity,返回调用者
						finish();
					}
				}).start();
			}
		});
	}

}
