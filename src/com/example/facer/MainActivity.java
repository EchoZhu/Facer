package com.example.facer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facepp.error.FaceppParseException;

public class MainActivity extends Activity implements OnClickListener {

	private static final int PIC_CODE = 0X159;
	private ImageView mPhoto;
	private Button mGetImage;
	private Button mDetect;
	private TextView mTip;
	private View mWaiting;
	
	private String mCurrentPhotoStr;//图片当前路径
	private Bitmap mPotoImgBitmap;//
	private Paint mPaint;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        
        initEvent();
        
        mPaint = new Paint();
    }


    private void initEvent() {
		// TODO Auto-generated method stub
    	mGetImage.setOnClickListener(this);
    	mDetect.setOnClickListener(this);
    	
		
	}


	private void initViews() {
		// TODO Auto-generated method stub
		mPhoto=(ImageView) findViewById(R.id.id_photo);
		mGetImage=(Button) findViewById(R.id.id_getImage);
		mDetect=(Button) findViewById(R.id.id_detect);
		mTip=(TextView) findViewById(R.id.id_tip);
		mWaiting=findViewById(R.id.id_waiting);
	}

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	// TODO Auto-generated method stub
	if (requestCode==PIC_CODE) {
		if (intent!=null) {
			Uri uri= intent.getData();
			Cursor cursor=getContentResolver().query(uri, null, null, null, null);
			cursor.moveToFirst();
			int idx=cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
			mCurrentPhotoStr = cursor.getString(idx);
			cursor.close();
			resizePhoto();
			mPhoto.setImageBitmap(mPotoImgBitmap);
			mTip.setText("Click Detect ==>");
		}
	}
	
	
	super.onActivityResult(requestCode, resultCode, intent);
}
	private void resizePhoto() {
	// TODO Auto-generated method stub
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds=true;
		BitmapFactory.decodeFile(mCurrentPhotoStr, options);
		//拿到一个比较合理的缩放的ratio（可以自行更改，保证图片大小不大于3M即可）
		double ratio = Math.max(options.outWidth * 1.0d /1024f, options.outHeight * 1.0d /1024f);
		options.inSampleSize=(int) Math.ceil(ratio);
		//只有再次设置为false之后才能加载显示图片
		options.inJustDecodeBounds=false;
		//再次加载图片，得到缩放之后的图片
		 mPotoImgBitmap = BitmapFactory.decodeFile(mCurrentPhotoStr, options);
}
    private static final int MSG_SUCESS = 0X001; 
    private static final int MSG_ERROR  = 0X002; 
	private Handler mhaHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SUCESS:
				mWaiting.setVisibility(View.GONE);
				JSONObject reJsonObject=(JSONObject) msg.obj;
				preparRsBitmap(reJsonObject);
				mPhoto.setImageBitmap(mPotoImgBitmap);
				break;
			case MSG_ERROR:
				mWaiting.setVisibility(View.GONE);
				String  erroMsg =(String) msg.obj;
				if (TextUtils.isEmpty(erroMsg)) {
					mTip.setText("Error");
					
				}else {
					mTip.setText(erroMsg);
				}
				break;

			default:
				break;
			}
			
			super.handleMessage(msg);
			
		};
	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.id_getImage:
			Intent intent_getPhoto=new Intent(Intent.ACTION_PICK);
			intent_getPhoto.setType("image/*");
			startActivityForResult(intent_getPhoto, PIC_CODE);
			break;
		case R.id.id_detect:
			mWaiting.setVisibility(View.VISIBLE);
			//如果没有选择图片，而点击了detect
			if (mCurrentPhotoStr != null && !mCurrentPhotoStr.trim().equals("")) {
				resizePhoto();
			}else {
				//Toast.makeText(this, "请选择一张需要上传的图片", Toast.LENGTH_LONG).show();
				mPotoImgBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.t4);
			}
		
			
			FaceppDetect.detect(mPotoImgBitmap, new FaceppDetect.CallBack() {
				@Override
				public void success(JSONObject result) {
					// TODO Auto-generated method stub
					Message msg = Message.obtain();
					msg.what=MSG_SUCESS;
					msg.obj=result;
					mhaHandler.sendMessage(msg);
				}
				
				@Override
				public void error(FaceppParseException exception) {
					// TODO Auto-generated method stub
					Message msg = Message.obtain();
					msg.what=MSG_ERROR;
					msg.obj=exception.getErrorMessage();
					mhaHandler.sendMessage(msg);
					
				}
			});
			break;

		default:
			break;
		}
	}


	protected void preparRsBitmap(JSONObject reJsonObject) {
		// TODO Auto-generated method stub
		Bitmap bitmap = Bitmap.createBitmap(mPotoImgBitmap.getWidth(),mPotoImgBitmap.getHeight(),mPotoImgBitmap.getConfig());
		Canvas canvas  = new Canvas(bitmap);
		canvas.drawBitmap(mPotoImgBitmap,0,0,null);
		try {
			JSONArray faces=reJsonObject.getJSONArray("face");
		    
			int faceCunt=faces.length();
			mTip.setText("共有"+faceCunt+"张脸");
			for (int i = 0; i < faceCunt; i++) {
				//拿到单独的face对象
				JSONObject face = faces.getJSONObject(i);
				JSONObject positonJsonObject = face.getJSONObject("position");
				float x =  (float) positonJsonObject.getJSONObject("center").getDouble("x");
				float y =  (float) positonJsonObject.getJSONObject("center").getDouble("y");
				float w = (float) positonJsonObject.getDouble("width");
				float h = (float) positonJsonObject.getDouble("height");
				
				//将百分比转换成实际的像素的值
				x = x/100 * bitmap.getWidth();
				y = y/100 * bitmap.getHeight();
				w = w/100 * bitmap.getWidth();
				h = h/100 * bitmap.getHeight();
				mPaint.setColor(0xffffffff);
				mPaint.setStrokeWidth(3);
				//划线
				canvas.drawLine(x-w/2, y-h/2, x-w/2, y+h/2,mPaint);
				canvas.drawLine(x-w/2, y-h/2, x+w/2, y-h/2,mPaint);
				canvas.drawLine(x+w/2, y-h/2, x+w/2, y+h/2,mPaint);
				canvas.drawLine(x-w/2, y+h/2, x+w/2, y+h/2,mPaint);
				
				//get age and gender
				int age = face.getJSONObject("attribute").getJSONObject("age").getInt("value");
				String gender = face.getJSONObject("attribute").getJSONObject("gender").getString("value");
				
				Bitmap ageBitmap = buildAgeBitmap(age,"Male".equals(gender));
				
				int ageWidth = ageBitmap.getWidth();
				int ageHight = ageBitmap.getHeight();
				if (bitmap.getWidth() < mPhoto.getWidth() && bitmap.getHeight() < mPhoto.getHeight()) {
					float ratio = Math.max(bitmap.getWidth()*1.0f/mPhoto.getWidth(), bitmap.getHeight()*1.0f/mPhoto.getHeight());
				    //缩放之后的显示框
					ageBitmap = Bitmap.createScaledBitmap(ageBitmap, (int)(ageWidth*ratio), (int)(ageHight*ratio), false);
				}
				canvas.drawBitmap(ageBitmap, x-ageBitmap.getWidth()/2, y - h/2 - ageBitmap.getHeight(),null);
				
				mPotoImgBitmap = bitmap;
				
				
			}
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	private Bitmap buildAgeBitmap(int age, boolean isMale) {
		// TODO Auto-generated method stub
		TextView tv =(TextView) mWaiting.findViewById(R.id.id_age_and_gender);
		tv.setText(age+"");
		if (isMale) {
			tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.male), null, null, null);
		}else {
			tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.female), null, null, null);
		}
		tv.setDrawingCacheEnabled(true);
		Bitmap bitmap = Bitmap.createBitmap(tv.getDrawingCache());
		tv.destroyDrawingCache();
		return bitmap;
	}



    
}
