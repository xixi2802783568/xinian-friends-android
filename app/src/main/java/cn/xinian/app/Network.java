package cn.xinian.app;

import android.webkit.CookieManager;
import org.json.*;
import java.net.*;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;

final class Network {
  static final String UA="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36";
  static final String[] PLATFORMS={"douyin.com","iesdouyin.com","bilibili.com","b23.tv"};
  static final String[] CDNS={"douyin.com","iesdouyin.com","bilibili.com","b23.tv","douyinvod.com","douyinpic.com","byteimg.com","bytecdn.cn","ibytedtos.com","pstatp.com","snssdk.com","amemv.com","bilivideo.com","bilivideo.cn","hdslb.com","akamaized.net"};
  static boolean host(String h,String[] list){if(h==null)return false;h=h.toLowerCase(Locale.ROOT);for(String s:list)if(h.equals(s)||h.endsWith("."+s))return true;return false;}
  static String allowed(String value,boolean media) throws IOException {try{URI u=new URI(value);if(!Arrays.asList("http","https").contains(u.getScheme())||u.getUserInfo()!=null||(u.getPort()!=-1&&u.getPort()!=80&&u.getPort()!=443)||!host(u.getHost(),media?CDNS:PLATFORMS))throw new Exception();return new URI("https",null,u.getHost(),-1,u.getPath(),null,null).toASCIIString()+(u.getRawQuery()==null?"":"?"+u.getRawQuery());}catch(Exception e){throw new IOException("仅支持抖音、B站及其媒体资源链接");}}
  static HttpURLConnection open(String value,boolean media,String referer) throws IOException {String target=allowed(value,media);for(int i=0;i<7;i++){HttpURLConnection c=(HttpURLConnection)new URL(target).openConnection();c.setInstanceFollowRedirects(false);c.setConnectTimeout(20000);c.setReadTimeout(30000);c.setRequestProperty("User-Agent",UA);c.setRequestProperty("Referer",referer);c.setRequestProperty("Accept-Encoding","identity");
      // Cookies are restricted to the platform that owns them; never sent to media CDNs.
      if(host(URI.create(target).getHost(),PLATFORMS)){String cookie=CookieManager.getInstance().getCookie(target);if(cookie!=null)c.setRequestProperty("Cookie",cookie);}
      int code=c.getResponseCode();if(code>=300&&code<400){String next=c.getHeaderField("Location");c.disconnect();if(next==null)throw new IOException("平台跳转地址无效");target=allowed(new URL(new URL(target),next).toString(),media);continue;}if(code!=200){c.disconnect();throw new IOException("平台请求失败（"+code+"），请打开作品完成验证后再试");}return c;}throw new IOException("平台跳转次数过多");}
  static JSONObject json(String url) throws Exception {HttpURLConnection c=open(url,false,"https://www.bilibili.com/");try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){out.write(b,0,n);if(out.size()>8*1024*1024)throw new IOException("平台响应过大");}JSONObject j=new JSONObject(out.toString("UTF-8"));boolean guestNav=url.equals("https://api.bilibili.com/x/web-interface/nav")&&j.optInt("code")==-101&&j.optJSONObject("data")!=null&&j.getJSONObject("data").has("wbi_img");if(j.optInt("code")!=0&&!guestNav)throw new IOException("B站暂未返回资源（"+j.optInt("code")+"），请先登录或完成验证");return j.getJSONObject("data");}finally{c.disconnect();}}
  // Resolve only HTTP redirects and stop at the first concrete work, before its page can recommend another.
  static String resolveWork(String source) throws Exception {String target=allowed(source,false);for(int i=0;i<7;i++){if(!WorkTarget.key(target).isEmpty())return target;HttpURLConnection c=(HttpURLConnection)new URL(target).openConnection();c.setInstanceFollowRedirects(false);c.setConnectTimeout(12000);c.setReadTimeout(12000);c.setRequestProperty("User-Agent",UA);String cookie=CookieManager.getInstance().getCookie(target);if(cookie!=null)c.setRequestProperty("Cookie",cookie);try{int code=c.getResponseCode();if(code>=300&&code<400){String next=c.getHeaderField("Location");if(next==null)throw new IOException("分享链接跳转无效，请重试原链接");target=allowed(new URL(new URL(target),next).toString(),false);}else throw new IOException("暂时无法确认原作品，请重试原链接或重新复制分享链接。不会改为读取推荐视频。");}finally{c.disconnect();}}throw new IOException("分享链接跳转过多，请重新复制链接");}
  static String enc(String s) throws Exception{return URLEncoder.encode(s,"UTF-8").replace("+","%20").replace("%7E","~");}
  static String key(String url){return url.substring(url.lastIndexOf('/')+1,url.lastIndexOf('.'));}
  static String wbi(TreeMap<String,String> params) throws Exception {JSONObject nav=json("https://api.bilibili.com/x/web-interface/nav");return sign(params,objKey(nav));}
  static String objKey(JSONObject nav) throws Exception{JSONObject w=nav.getJSONObject("wbi_img");String raw=key(w.getString("img_url"))+key(w.getString("sub_url"));int[] table={46,47,18,2,53,8,23,32,15,50,10,31,58,3,45,35,27,43,5,49,33,9,42,19,29,28,14,39,12,38,41,13};StringBuilder b=new StringBuilder();for(int n:table)b.append(raw.charAt(n));return b.toString();}
  static String sign(TreeMap<String,String> p,String key) throws Exception{p.put("wts",String.valueOf(System.currentTimeMillis()/1000));StringBuilder b=new StringBuilder();for(Map.Entry<String,String> e:p.entrySet()){if(b.length()>0)b.append('&');b.append(enc(e.getKey())).append('=').append(enc(e.getValue().replaceAll("[!'()*]","")));}byte[] hash=MessageDigest.getInstance("MD5").digest((b.toString()+key).getBytes("UTF-8"));StringBuilder h=new StringBuilder();for(byte v:hash)h.append(String.format(Locale.ROOT,"%02x",v&255));return b+"&w_rid="+h;}
  static Media.Result bili(String bvid,int page) throws Exception {JSONObject details=json("https://api.bilibili.com/x/web-interface/view?bvid="+enc(bvid));JSONArray pages=details.optJSONArray("pages");long cid=details.optLong("cid");if(pages!=null&&page>0&&page<=pages.length())cid=pages.getJSONObject(page-1).getLong("cid");TreeMap<String,String> p=new TreeMap<>();p.put("bvid",bvid);p.put("cid",String.valueOf(cid));p.put("qn","80");p.put("fnval","4048");p.put("fnver","0");p.put("fourk","1");p.put("platform","pc");JSONObject play;
    try{play=json("https://api.bilibili.com/x/player/wbi/playurl?"+wbi(p));}catch(Exception first){play=json("https://api.bilibili.com/x/player/playurl?bvid="+enc(bvid)+"&cid="+cid+"&qn=80&fnval=4048&fourk=1");}
    return Media.bili(details,play);
  }
}
