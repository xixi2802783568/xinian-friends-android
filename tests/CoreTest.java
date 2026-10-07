package cn.xinian.app;
import org.json.*;

public final class CoreTest {
  static int checks=0;
  static void ok(boolean value,String name){if(!value)throw new AssertionError(name);checks++;}
  public static void main(String[] args)throws Exception{
    for(String bad:new String[]{"https://127.0.0.1/a","https://bilibili.com.evil.test/a","file:///etc/passwd","https://evil.test/?url=https://douyin.com","https://user@www.douyin.com/a","https://www.douyin.com:8181/a"}){boolean rejected=false;try{Network.allowed(bad,true);}catch(Exception e){rejected=true;}ok(rejected,"reject "+bad);}
    ok(Network.allowed("http://www.douyin.com/video/123?q=a%2Fb",false).equals("https://www.douyin.com/video/123?q=a%2Fb"),"URL upgrade preserves query");
    ok(Network.allowed("https://v3.douyinvod.com/a.mp4",true).startsWith("https://"),"known CDN");
    Media.Result images=Media.douyin(new JSONObject("{\"desc\":\"实况\",\"images\":[{\"url_list\":[\"https://p.douyinpic.com/photo.webp\"],\"video\":{\"play_addr\":{\"url_list\":[\"https://v.douyinvod.com/motion.mp4\"]}}}]}"));
    ok(images.assets.size()==2,"live image and motion");ok(images.assets.get(0).mime.equals("image/webp"),"webp type");ok(images.assets.get(1).label.equals("实况片段 1"),"paired index");
    Media.Result camel=Media.douyin(new JSONObject("{\"imagePostInfo\":{\"images\":[{\"urlList\":[\"https://p.douyinpic.com/a.jpg\"],\"video\":{\"playAddr\":{\"urlList\":[\"https://v.douyinvod.com/a.mp4\"]}}}]}}"));ok(camel.assets.size()==2,"camel live data");
    JSONObject play=new JSONObject("{\"dash\":{\"video\":[{\"id\":80,\"width\":1920,\"height\":1080,\"codecs\":\"avc1\",\"baseUrl\":\"https://a.bilivideo.com/v\"},{\"id\":80,\"width\":1920,\"height\":1080,\"codecs\":\"hev1\",\"baseUrl\":\"https://a.bilivideo.com/hevc\"},{\"id\":64,\"width\":1280,\"height\":720,\"codecs\":\"avc1\",\"baseUrl\":\"https://a.bilivideo.com/720\"}],\"audio\":[{\"codecs\":\"mp4a\",\"bandwidth\":100,\"baseUrl\":\"https://a.bilivideo.com/audio\"}]}}");
    Media.Result bili=Media.bili(new JSONObject("{\"title\":\"B站测试\"}"),play);ok(bili.assets.size()==2,"Bili AVC qualities");ok(bili.assets.get(0).width==1920&&!bili.assets.get(0).audio.isEmpty(),"1080 with audio");
    Media.Result noAudio=Media.bili(new JSONObject(),new JSONObject("{\"dash\":{\"video\":[{\"id\":80,\"codecs\":\"avc1\"}]}}"));ok(noAudio.assets.isEmpty(),"no silent HD option");
    JSONObject audio=play.getJSONObject("dash").getJSONArray("audio").getJSONObject(0);
    audio.put("baseUrl","https://edge.mcdn.bilivideo.cn:8082/audio");
    audio.put("backupUrl",new JSONArray().put("https://evil.test/a").put("https://upos.bilivideo.com/audio?sign=a%2Fb"));
    JSONObject details=new JSONObject().put("title","测试").put("pic","https://i0.hdslb.com/bfs/archive/cover.jpg").put("owner",new JSONObject().put("name","作者"));
    Media.Result repaired=Media.bili(details,play);
    ok(repaired.assets.size()==2,"special-port audio retains video choices");
    ok(repaired.assets.get(0).audio.equals("https://upos.bilivideo.com/audio?sign=a%2Fb"),"audio selects safe backup and preserves signature");
    ok(repaired.assets.get(0).audioUrls.size()==1,"unsafe backup rejected");
    ok(repaired.cover.equals(details.getString("pic"))&&repaired.author.equals("作者"),"Bili cover and author");
    ok(images.cover.equals(images.assets.get(0).url),"Douyin image preview");
    if(args.length>0){JSONObject raw=new JSONObject(new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(args[0])),java.nio.charset.StandardCharsets.UTF_8)).getJSONObject("data");Media.Result live=Media.bili(raw,raw);ok(!live.assets.isEmpty(),"actual reported video has options");ok(!live.cover.isEmpty(),"actual cover present");for(Media.Asset asset:live.assets){Network.allowed(asset.url,true);if(!asset.audio.isEmpty())Network.allowed(asset.audio,true);}System.out.println("Reported video: "+live.assets.size()+" usable qualities; cover present");}
    if(args.length>1){JSONObject item=new JSONObject(new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(args[1])),java.nio.charset.StandardCharsets.UTF_8)).getJSONObject("item");Media.Result dy=Media.douyin(item);ok(dy.assets.size()>1,"reported Douyin resources");ok(!dy.cover.isEmpty(),"reported Douyin cover");ok(dy.assets.get(0).width>0,"dynamic bitrate dimensions");for(Media.Asset a:dy.assets)Network.allowed(a.url,true);System.out.println("Douyin: "+dy.assets.size()+" resources, cover and quality parsed");}
    String expected="dy:7645286931332451624";
    ok(WorkTarget.matches(expected,"https://www.douyin.com/video/7645286931332451624?from=share"),"original URL matches");
    ok(WorkTarget.matches(expected,"https://www.iesdouyin.com/share/video/7645286931332451624/"),"share redirect identity");
    ok(WorkTarget.matches(expected,"https://www.douyin.com/?modal_id=7645286931332451624"),"modal identity");
    ok(!WorkTarget.matches(expected,"https://www.douyin.com/video/7689854419282467967"),"recommendation blocked");
    ok(!WorkTarget.matches(expected,"https://evil.test/video/7645286931332451624"),"foreign host rejected");
    ok(!WorkTarget.matches(expected,"https://www.douyin.com/"),"homepage never accepted as work");
    ok(WorkTarget.key("https://v.douyin.com/I50bKR4AEB8/").isEmpty(),"short link cannot establish identity");
    ok(!WorkTarget.matches("bili:BV1GJ411x7h7:1","https://www.bilibili.com/video/BV1GJ411x7h7/?p=2"),"wrong Bili part blocked");
    System.out.println("PASS: "+checks+" core checks");
  }
}
