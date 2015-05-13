package com.example.facer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	private static final int PIC_CODE = 0X159;
	private ImageView mPhoto;
	private Button mGetImage;
	private Button mDetect;
	private TextView mTip;
	private View mWaiting;
	
	private String mCurrentPhotoStr;//图片当前路径
	private Bitmap mPotoImgBitmap;//
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        
        initEvent();
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
			int idx=cursor.getColumnIndex(MediaStore.Images.ImageColumns.
					DATA);
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
			
			break;

		default:
			break;
		}
		
	}



    
}
