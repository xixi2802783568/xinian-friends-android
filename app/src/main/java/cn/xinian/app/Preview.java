package cn.xinian.app;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.net.HttpURLConnection;
import java.io.*;

final class Preview {
  static Bitmap load(String url,String referer) throws Exception {
    HttpURLConnection connection=Network.open(url,true,referer);
    byte[] data;
    try(InputStream in=connection.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
      String type=connection.getContentType();if(type!=null&&!type.startsWith("image/")&&!type.startsWith("application/octet-stream"))throw new IOException("不是图片");
      byte[] buffer=new byte[16384];int count;
      while((count=in.read(buffer))!=-1){if(Thread.currentThread().isInterrupted())throw new IOException("取消");if(out.size()+count>10*1024*1024)throw new IOException("预览图片过大");out.write(buffer,0,count);}data=out.toByteArray();
    }finally{connection.disconnect();}
    BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(data,0,data.length,options);
    if(options.outWidth<=0||options.outHeight<=0)throw new IOException("图片无法解码");options.inSampleSize=1;
    while(Math.max(options.outWidth,options.outHeight)/options.inSampleSize>1200)options.inSampleSize*=2;
    options.inJustDecodeBounds=false;Bitmap result=BitmapFactory.decodeByteArray(data,0,data.length,options);if(result==null)throw new IOException("图片无法解码");return result;
  }
}
