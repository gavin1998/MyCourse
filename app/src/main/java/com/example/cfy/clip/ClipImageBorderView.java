package com.example.cfy.clip;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class ClipImageBorderView extends View
{
	private int mHorizontalPadding; // 水平方向与View的边距
	private int mBorderWidth = 1;  //边框的宽度 单位dp
	private Paint mPaint;

	public ClipImageBorderView(Context context)
	{
		this(context, null);
	}

	public ClipImageBorderView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public ClipImageBorderView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		//转换为PX
		mBorderWidth = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, mBorderWidth, getResources()
						.getDisplayMetrics());

		mPaint = new Paint();

	}

	//Modified by cfy
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		//设置mPaint的参数
		mPaint.setColor(Color.parseColor("#FFFFFF"));
		mPaint.setStrokeWidth(mBorderWidth);
		mPaint.setStyle(Style.STROKE);

		//绘制圆形边框
		canvas.drawCircle( getWidth()/2, getHeight()/2, getWidth()/2-mHorizontalPadding, mPaint);

        /**
         * 设置mPaint的参数为半透明
         */

		//抗锯齿
		mPaint.setAntiAlias(true);
		//对Bitmap进行滤波处理,抗锯齿
		mPaint.setFilterBitmap(true);
		// 设置paint的颜色和Alpha值(a,r,g,b)
		//Alpha值控制照片的透明度,设置为半透明
		mPaint.setColor(Color.parseColor("#aa000000"));
		//设置为填充
		mPaint.setStyle(Paint.Style.FILL);

        /**
         * 利用PATH绘制圆形和外面的整个屏幕之间的区域为半透明
         */
		Path path = new Path();
		RectF mBackRect;
		//背景区域对应的矩形：全屏幕
		mBackRect = new RectF(0, 0,getWidth(), getHeight());

		//将矩形加入path
		path.addRect(mBackRect.left, mBackRect.top, mBackRect.right, mBackRect.bottom,
				Path.Direction.CW);
		//将圆加入path：圆位于屏幕的中心，水平边距为mHorizontalPadding
		path.addCircle(getWidth()/2, getHeight()/2, getWidth()/2-mHorizontalPadding,  Path.Direction.CCW);
		//绘制path,矩形和圆构成的区域为半透明
		canvas.drawPath(path, mPaint);

	}

    /**
     * 被外部调用，设置水平边距
     * @param mHorizontalPadding
     */
	public void setHorizontalPadding(int mHorizontalPadding)
	{
		this.mHorizontalPadding = mHorizontalPadding;
	}
}
