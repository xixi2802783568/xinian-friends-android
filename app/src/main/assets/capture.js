(function(){
  const u=new URL(location.href),id=(u.pathname.match(/\/(?:video|note|slides)\/(\d+)/)||[])[1]||u.searchParams.get('modal_id')||u.searchParams.get('aweme_id');if(!id)return null;
  const seen=new WeakSet();let count=0;
  function find(o,d=0){if(!o||typeof o!=='object'||d>18||seen.has(o)||++count>100000)return null;seen.add(o);if(String(o.aweme_id||o.awemeId||'')===id&&(o.video||o.images||o.imagePostInfo))return o;for(const k of Object.keys(o)){if(['_owner','return','stateNode','alternate','type'].includes(k))continue;try{const v=find(o[k],d+1);if(v)return v;}catch{}}return null;}
  const data=[window._ROUTER_DATA,window.__INITIAL_STATE__,window.__NEXT_DATA__];let pace='';
  for(const s of document.scripts){const t=s.textContent||'';if(t.length>8000000)continue;
    if(/^(RENDER_DATA|__NEXT_DATA__)$/.test(s.id))try{data.push(JSON.parse(decodeURIComponent(t)));}catch{}
    const r=t.match(/window\._ROUTER_DATA\s*=\s*([\s\S]*)/);if(r)try{data.push(JSON.parse(r[1].trim().replace(/;\s*$/,'')));}catch{}
    const p=t.match(/self\.__pace_f\.push\((\[[\s\S]*\])\)\s*;?\s*$/);if(p)try{const c=JSON.parse(p[1]);if(c[0]===1&&typeof c[1]==='string')pace+=c[1];}catch{}
  }
  for(const row of pace.split('\n'))try{const raw=row.replace(/^[0-9a-f]+:/i,'');data.push(JSON.parse(raw.startsWith('%')?decodeURIComponent(raw):raw));}catch{}
  for(const video of document.querySelectorAll('video')){let element=video;for(let i=0;element&&i<16;i++,element=element.parentElement){for(const key of Object.getOwnPropertyNames(element)){if(key.startsWith('__reactProps$'))data.push(element[key]);if(key.startsWith('__reactFiber$')){let fiber=element[key];for(let j=0;fiber&&j<24;j++,fiber=fiber.return){if(fiber.memoizedProps)data.push(fiber.memoizedProps);}}}}}
  for(const d of data){const item=find(d);if(item){try{return {item:JSON.parse(JSON.stringify(item)),source:location.href};}catch{}}}
  // Never mistake a preloaded recommendation for the requested work.
  for(const video of document.querySelectorAll('video')){try{const src=new URL(video.currentSrc||video.src);if(src.protocol==='https:'&&src.searchParams.get('__vid')===id&&video.readyState>=1)return {item:{aweme_id:id,desc:document.title.replace(/ - 抖音$/,''),video:{play_addr:{url_list:[src.href],width:video.videoWidth,height:video.videoHeight},cover:{url_list:video.poster?[video.poster]:[]}}},source:location.href,playbackFallback:true};}catch{}}
  return null;
})();
