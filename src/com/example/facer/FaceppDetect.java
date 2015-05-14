package com.example.facer;

import java.io.ByteArrayOutputStream;

import org.json.JSONObject;

import android.graphics.Bitmap;
import android.util.Log;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

public class FaceppDetect {

	public static Bitmap bmSmall ;
	public interface CallBack
	{
		void success(JSONObject result);
		void error(FaceppParseException exception);
		
	}

	public static void detect(final Bitmap bm, final CallBack callBack)
	{
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					//request
					HttpRequests requests = new HttpRequests(Constant.KEY,Constant.SECRET,true,true);
					bmSmall = Bitmap.createBitmap(bm,0,0,bm.getWidth(),bm.getHeight());
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					bmSmall.compress(Bitmap.CompressFormat.JPEG, 100, stream);
					byte[] arrays = stream.toByteArray();//获取图片的二进制数值
					PostParameters params=new PostParameters();
					params.setImg(arrays);
					JSONObject jsonObject =requests.detectionDetect(params);
					Log.e("TAG", jsonObject.toString());
					if (callBack!=null) {
						callBack.success(jsonObject);
					}
				} catch (FaceppParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					if (callBack!=null) {
						callBack.error(e);
					}
				}
				
			}
		}).start();
		
	}
}
