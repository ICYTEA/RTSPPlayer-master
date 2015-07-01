package org.videolan.libvlc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.view.Display;
import android.view.View;
/**
 * 对图片的操作类，包括�? 截屏，保存图片，获取指定路径的图片， 图片转换成字节数组，字节数组转换成图片，对图片的缩放
 * 
 * @author cnmobi
 * 
 */
public class BitmapUtils {
	
	private static final long MB = 1024*1024;

	/**
	 * 图片转换成字节数�? 
	 * @param bm 图片对象
	 * @return
	 */
	public static byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	/**
	 * 字节数组转换成图�?
	 * 
	 * @param intent
	 *            Intent对象
	 * @return 图片对象
	 */
	public static Bitmap Bytes2Bitmap(Intent intent) {
		byte[] buff = intent.getByteArrayExtra("bitmap");
		Bitmap bm = BitmapFactory.decodeByteArray(buff, 0, buff.length);
		return bm;
	}

	/**
	 * 截屏方法
	 * 
	 * @param Activity对象
	 *            ，可以�?�过getActivity()方法获取
	 * @return
	 */
	public static Bitmap shot(Activity activity) {
		View view = activity.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Display display = activity.getWindowManager().getDefaultDisplay();
		view.layout(0, 500, display.getWidth() - 200, display.getHeight() - 250);
		Bitmap bitmap = view.getDrawingCache();
		Bitmap bmp = Bitmap.createBitmap(bitmap);
		// return Bitmap.createBitmap(bmp, 100,100, 500, 500);
		return bmp;
	}
	/**
	 * 截取指定view的视�?
	 * @param v 要截取的view对象
	 * @return Bitmap对象
	 */
	public static Bitmap getViewBitmap(View v) {
		v.clearFocus(); // 清除视图焦点
		v.setPressed(false);// 将视图设为不可点�?

		boolean willNotCache = v.willNotCacheDrawing(); // 返回视图是否可以保存他的画图缓存
		v.setWillNotCacheDrawing(false);

		// Reset the drawing cache background color to fully transparent
		// for the duration of this operation //将视图在此操作时置为透明
		int color = v.getDrawingCacheBackgroundColor(); // 获得绘制缓存位图的背景颜�?
		v.setDrawingCacheBackgroundColor(0); // 设置绘图背景颜色
		if (color != 0) { // 如果获得的背景不是黑色的则释放以前的绘图缓存
			v.destroyDrawingCache(); // 释放绘图资源�?使用的缓�?
		}
		v.buildDrawingCache(); // 重新创建绘图缓存，此时的背景色是黑色
		Bitmap cacheBitmap = v.getDrawingCache(); // 将绘图缓存得到的,注意这里得到的只是一个图像的引用
		if (cacheBitmap == null) {
			return null;
		}
		Bitmap bitmap = Bitmap.createBitmap(cacheBitmap); // 将位图实例化
		// Restore the view //恢复视图
		v.destroyDrawingCache();// 释放位图内存
		v.setWillNotCacheDrawing(willNotCache);// 返回以前缓存设置
		v.setDrawingCacheBackgroundColor(color);// 返回以前的缓存颜色设�?
		return bitmap;
	}

	/**
	 * 保存图片到指定路径的方法
	 * 
	 * @param path 图片保存的相对路�?
	 * @param name 图片的名�?
	 * @param bitmap 要保存的图片
	 * @throws IOException 读写图片文件出现的异常信�?
	 */
	public static void save(String path,String name, Bitmap bitmap) throws IOException {
		File file = new File( path , name);
		// 若图片文件在SD卡的文件夹不存在
		if (!file.getParentFile().exists()) {
			// 创建该文件夹
			file.getParentFile().mkdirs();
		}
		// 若文件不存在，则创建
		if (!file.exists()) {
			file.createNewFile();
		}
		// 创建文件输出�?
		FileOutputStream out = new FileOutputStream(file);
		// 保存图片至SD卡指定文件夹
		bitmap.compress(CompressFormat.JPEG, 100, out);
	}

	/**
	 * 获得指定路径的图�?
	 * 
	 * @param path 图片的本地路�?
	 * @param name 图片的名�?
	 * @return 图片对象
	 * @throws IOException
	 */
	public static Bitmap getBitmap(String path,String name) throws IOException {
		BitmapFactory.Options options = new BitmapFactory.Options();
		File file = new File(path,name);
		if (file.exists()&&(file.length()/MB)>1)
		{
			options.inSampleSize = 2;
		}
		Bitmap imageBitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),options);
		return imageBitmap;
	}
	
	public static Bitmap getBitmap(String path){
		Bitmap imageBitmap=null;
		try
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			File file = new File(path);
			if (file.exists()&&(file.length()/MB)>1)
			{
				options.inSampleSize = 2;
			}
			imageBitmap = BitmapFactory.decodeFile(path,options);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return imageBitmap;
	}

	/***
	 * 图片的缩放方法（图片按照给定宽高缩放�?
	 * 
	 * @param bgimage
	 *            ：源图片资源
	 * @param newWidth
	 *            ：缩放后宽度
	 * @param newHeight
	 *            ：缩放后高度
	 * @return 可用的图�? bitmap对象
	 */
	public static Bitmap zoomImage(Bitmap bm, double newWidth, double newHeight) {
		// 获取这个图片的宽和高
		float width = bm.getWidth();
		float height = bm.getHeight();
		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();
		// 计算宽高缩放�?
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 缩放图片动作
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bitmap = Bitmap.createBitmap(bm, 0, 0, (int) width,
				(int) height, matrix, true);
		return bitmap;
	}
	
	public static String getSDPath(){
		boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		if(hasSDCard){
			return Environment.getExternalStorageDirectory().toString();
		}else
			return Environment.getDownloadCacheDirectory().toString();
	}
}
