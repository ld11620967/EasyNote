package com.nilin.easynote;

import java.io.IOException;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.style.ImageSpan;
import android.util.Log;
import android.widget.TextView;

public class checknotes extends Activity{
	private TextView title;
	private TextView time;
	private TextView notes;
	public Cursor cursor=null;
	public String namestr="";
	private DBManage dm=null;
	private Bitmap bitmap = null;
	private String path = null;
	private int rotate=0;
	  @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.checknotes);
	        title=(TextView)findViewById(R.id.checkTitle);
	        time=(TextView)findViewById(R.id.checkTime);
		    notes=(TextView)findViewById(R.id.checkContent);
		    notes.setScroller(null);
	        dm=new DBManage(this);
			Intent intent = getIntent();//获取启动该Activity的intent对象
			String title= intent.getStringExtra("title");
			String content = intent.getStringExtra("content");
			String time= intent.getStringExtra("time");
			long t = Long.parseLong(time);
			String datetime = DateFormat.format("yyyy-MM-dd kk:mm:ss", t).toString();
			this.title.setText(title);
			this.time.setText(datetime);
			dm.open();
			int i=0;
			int start=0;
			int end=0;
			String str1=null;
			String str2="[";
			String str4="]";
			SpannableString travelsSpan =new SpannableString(content);
			for(i=0;i<content.length();i++)
			{
				str1=content.substring(i, i+1);
				//travelsString+=str1;
				Log.i("log", str1);
				if(str1.equals(str2))
				{
					start=i+1;
				}
				if(str1.equals(str4))
				{
					end=i;
				namestr=content.substring(start,end);
				Log.i("log", namestr);
				cursor=dm.selcetPathByName(namestr);
				cursor.moveToFirst();
				path=cursor.getString(cursor.getColumnIndex("path"));
				cursor.close();
				namestr=null;
				Log.i("log", path);
				if(!(cursor==null))
				{
					int count=cursor.getCount();
					Log.i("log", "count----->"+count);
					BitmapFactory.Options options =new BitmapFactory.Options();
					options.inJustDecodeBounds =true;
					bitmap =BitmapFactory.decodeFile(path, options); //此时返回bm为空
					options.inJustDecodeBounds =false;
					int be = (int)(options.outHeight/ (float)100);
					if (be <= 0)
						be = 1;
					options.inSampleSize = be;
					bitmap=BitmapFactory.decodeFile(path,options);
				       int bitmapwidth=options.outWidth;
				       int bitmapheight=options.outHeight;
					//检查图片是否要翻转
				       try {
				           ExifInterface exifInterface = new ExifInterface(path);   
				           int result = exifInterface.getAttributeInt(   
				                   ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);   
				             
				           switch(result) {   
				           case ExifInterface.ORIENTATION_ROTATE_90:   
				               rotate = 90; 
				               Log.i("log", "rotate----->"+rotate);
				               break;   
				           case ExifInterface.ORIENTATION_ROTATE_180:   
				               rotate = 180;   
				               Log.i("log", "rotate----->"+rotate);
				               break;   
				           case ExifInterface.ORIENTATION_ROTATE_270:   
				               rotate = 270;
				               Log.i("log", "rotate----->"+rotate);
				               break;   
				           default:   
				               break;   
				           } 
				       }  catch (IOException e) {
				           e.printStackTrace();   
				       }
				       if (rotate!=0&&bitmap!=null)
				        {
				        	Matrix matrix=new Matrix();
					        matrix.reset();
					        matrix.setRotate(rotate);
					        Bitmap bitmap1= Bitmap.createBitmap(bitmap, 0, 0, options.outWidth, options.outHeight, matrix, true);
					        bitmap = bitmap1;
					        rotate=0;
					        bitmapwidth=options.outHeight;
					        bitmapheight=options.outWidth;
				        }
				Drawable drawable = new BitmapDrawable(bitmap);
				drawable.setBounds(0, 0,bitmapwidth*2,bitmapheight*2);
					ImageSpan span = new ImageSpan(drawable,ImageSpan.ALIGN_BOTTOM);
					travelsSpan.setSpan(span, start-1,end+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
				else
				{
					Log.i("log", "insert icon faile");
				}
				}
				}
			dm.close();
		  notes.setText(travelsSpan);
		}
}
