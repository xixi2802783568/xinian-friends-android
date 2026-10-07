import fs from 'node:fs';
import vm from 'node:vm';
import assert from 'node:assert/strict';
const script=fs.readFileSync(new URL('../app/src/main/assets/capture.js',import.meta.url),'utf8');
const id='7689854419282467967';
function run({scripts=[],videos=[]}={}){return vm.runInNewContext(script,{URL,location:{href:'https://www.douyin.com/video/'+id},window:{},document:{title:'目标作品 - 抖音',scripts,querySelectorAll:()=>videos}},{timeout:2000});}
const item={awemeId:id,desc:'目标作品',video:{playAddr:[{src:'https://v.douyinvod.com/video'}]}};
const parent={__reactProps$test:{item},parentElement:null};
assert.equal(run({videos:[{parentElement:parent}]}).item.awemeId,id);
assert.equal(run({scripts:[{id:'RENDER_DATA',textContent:encodeURIComponent(JSON.stringify({item}))}]}).item.awemeId,id);
assert.equal(run({videos:[{src:'https://v.douyinvod.com/video?__vid=123',readyState:3}]}),null);
assert.equal(run({videos:[{src:'https://v.douyinvod.com/video?__vid='+id,readyState:3,videoWidth:720,videoHeight:1280}]}).playbackFallback,true);
assert.equal(run({videos:[{src:'blob:random',readyState:3}]}),null);
console.log('PASS: dynamic props, hydration, matching playback and recommendation rejection');
