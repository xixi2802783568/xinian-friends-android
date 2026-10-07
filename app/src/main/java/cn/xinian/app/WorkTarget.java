package cn.xinian.app;
import java.net.*;
import java.util.regex.*;

final class WorkTarget {
  static String query(URI u,String name){String q=u.getRawQuery();if(q==null)return "";for(String part:q.split("&")){String[] kv=part.split("=",2);try{if(URLDecoder.decode(kv[0],"UTF-8").equals(name))return kv.length>1?URLDecoder.decode(kv[1],"UTF-8"):"";}catch(Exception ignored){}}return "";}
  static String key(String value){try{URI u=new URI(value);String host=u.getHost(),path=u.getPath();if(Network.host(host,new String[]{"douyin.com","iesdouyin.com"})){Matcher m=Pattern.compile("/(?:video|note|slides)/(\\d{10,25})(?:/|$)").matcher(path);String id=m.find()?m.group(1):query(u,"modal_id");if(id.isEmpty())id=query(u,"aweme_id");return id.matches("\\d{10,25}")?"dy:"+id:"";}
    if(Network.host(host,new String[]{"bilibili.com"})){Matcher m=Pattern.compile("/video/(BV[0-9A-Za-z]{10})(?:/|$)").matcher(path);if(m.find()){String part=query(u,"p");if(!part.matches("[1-9][0-9]{0,4}"))part="1";return "bili:"+m.group(1)+":"+part;}}
  }catch(Exception ignored){}return "";}
  static boolean matches(String expected,String actual){return !expected.isEmpty()&&expected.equals(key(actual));}
}
