package cn.xinian.app;

import org.json.*;
import java.util.*;

final class Media {
  static class Asset {
    String label,url,audio="",mime="video/mp4",extension="mp4",referer="https://www.douyin.com/";
    List<String> urls=new ArrayList<>(),audioUrls=new ArrayList<>();
    int width,height;
    Asset(String label,String url){this.label=label;this.url=url;}
  }
  static class Result {String title="作品",notice="",cover="",author="",referer="https://www.douyin.com/";List<Asset> assets=new ArrayList<>();}
  static List<String> streams(JSONObject stream){List<String> candidates=new ArrayList<>();candidates.add(url(stream));JSONArray backups=arr(stream,"backupUrl","backup_url");for(int i=0;i<backups.length();i++)candidates.add(backups.optString(i));List<String> result=new ArrayList<>();for(String s:candidates)try{String safe=Network.allowed(s,true);if(!result.contains(safe))result.add(safe);}catch(Exception ignored){}return result;}
  static JSONObject obj(JSONObject o,String... keys){for(String k:keys){JSONObject v=o==null?null:o.optJSONObject(k);if(v!=null)return v;}return new JSONObject();}
  static JSONArray arr(JSONObject o,String...keys){for(String k:keys){JSONArray a=o==null?null:o.optJSONArray(k);if(a!=null)return a;}return new JSONArray();}
  static String url(Object o){if(o instanceof String && ((String)o).startsWith("http"))return (String)o;if(o instanceof JSONArray){JSONArray a=(JSONArray)o;for(int i=0;i<a.length();i++){String v=url(a.opt(i));if(!v.isEmpty())return v;}}if(o instanceof JSONObject){JSONObject j=(JSONObject)o;for(String k:new String[]{"url_list","urlList","src","url","baseUrl","base_url"}){String v=url(j.opt(k));if(!v.isEmpty())return v;}}return "";}
  static String video(JSONObject o){for(String k:new String[]{"play_addr","playAddr","play_addr_h264","download_addr"}){String s=url(o.opt(k));if(!s.isEmpty())return s;}return "";}
  static Asset photo(String name,String u){Asset a=new Asset(name,u);a.extension=u.contains("webp")?"webp":u.contains(".png")?"png":"jpg";a.mime="image/"+(a.extension.equals("jpg")?"jpeg":a.extension);return a;}
  static Result douyin(JSONObject item){Result r=new Result();r.title=item.optString("desc",item.optString("title","抖音作品"));JSONArray images=arr(item,"images");if(images.length()==0)images=arr(obj(item,"imagePostInfo"),"images");
    for(int i=0;i<images.length();i++){JSONObject im=images.optJSONObject(i);if(im==null)continue;String u=url(im);if(u.isEmpty())u=url(im.opt("display_image"));if(!u.isEmpty())r.assets.add(photo("照片 "+(i+1),u));String m=video(obj(im,"video"));if(m.isEmpty())m=video(obj(obj(im,"live_photo"),"video"));if(!m.isEmpty()){r.assets.add(new Asset("实况片段 "+(i+1),m));r.notice="照片与实况片段分别保存，暂不封装为 OPPO 原生实况照片。";}}
    if(images.length()==0){JSONObject v=obj(item,"video");JSONArray rates=arr(v,"bit_rate","bitRate","bitRateList");Set<String> sizes=new HashSet<>();for(int i=0;i<rates.length();i++){JSONObject rate=rates.optJSONObject(i);if(rate==null||!"mp4".equals(rate.optString("format")))continue;JSONObject p=obj(rate,"play_addr","playAddr");String u=url(p);if(u.isEmpty())u=url(rate.opt("playAddr"));int w=p.optInt("width",rate.optInt("width")),h=p.optInt("height",rate.optInt("height"));boolean hevc=rate.optInt("is_h265",rate.optInt("isH265"))==1||rate.optInt("is_bytevc1")==1;String key=w+"x"+h+(hevc?"HEVC":"AVC");if(u.isEmpty()||w==0||h==0||!sizes.add(key))continue;Asset a=new Asset(Math.min(w,h)+"P · "+w+"×"+h+(hevc?" · H.265":" · H.264"),u);a.width=w;a.height=h;r.assets.add(a);}r.assets.sort((a,b)->Integer.compare(b.width*b.height,a.width*a.height));if(r.assets.isEmpty()){String u=video(v);if(!u.isEmpty())r.assets.add(new Asset("默认视频",u));}String cover=url(v.opt("origin_cover"));if(cover.isEmpty())cover=url(v.opt("originCover"));if(cover.isEmpty())cover=url(v.opt("cover"));if(!cover.isEmpty())r.assets.add(photo("视频封面",cover));}
    r.author=obj(item,"author").optString("nickname","");for(Asset a:r.assets)if(a.mime.startsWith("image/")){r.cover=a.url;break;}return r;
  }
  static Result bili(JSONObject details,JSONObject play){Result r=new Result();r.title=details.optString("title","B站视频");r.cover=details.optString("pic","");if(r.cover.startsWith("//"))r.cover="https:"+r.cover;r.author=obj(details,"owner").optString("name","");r.referer="https://www.bilibili.com/";JSONObject dash=obj(play,"dash");JSONArray audios=arr(dash,"audio");JSONObject bestAudio=null;for(int i=0;i<audios.length();i++){JSONObject a=audios.optJSONObject(i);if(a!=null&&a.optString("codecs").startsWith("mp4a")&&!streams(a).isEmpty()&&(bestAudio==null||a.optLong("bandwidth")>bestAudio.optLong("bandwidth")))bestAudio=a;}
    Map<Integer,JSONObject> best=new TreeMap<>(Collections.reverseOrder());JSONArray videos=arr(dash,"video");for(int i=0;i<videos.length();i++){JSONObject v=videos.optJSONObject(i);if(v!=null&&v.optString("codecs").startsWith("avc")&&!streams(v).isEmpty()){int id=v.optInt("id");JSONObject old=best.get(id);if(old==null||old.optLong("bandwidth")<v.optLong("bandwidth"))best.put(id,v);}}
    if(bestAudio!=null)for(JSONObject v:best.values()){int w=v.optInt("width"),h=v.optInt("height");Asset a=new Asset(Math.min(w,h)+"P · "+w+"×"+h+" · 含声音",url(v));a.urls=streams(v);a.url=a.urls.get(0);a.audioUrls=streams(bestAudio);a.audio=a.audioUrls.get(0);a.referer="https://www.bilibili.com/";a.width=w;a.height=h;r.assets.add(a);}
    JSONArray d=arr(play,"durl");if(d.length()==1){String u=url(d.opt(0));if(!u.isEmpty()){Asset a=new Asset("默认视频 · 含声音",u);a.referer="https://www.bilibili.com/";r.assets.add(a);}}
    r.notice="清晰度取决于作品和本机账号权限；高清音视频会在手机上合并。未出现 1080P 时，可先登录 B站再解析。";return r;
  }
}
