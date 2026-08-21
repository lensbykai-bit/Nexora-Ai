const $=(s,r=document)=>r.querySelector(s); const $$=(s,r=document)=>[...r.querySelectorAll(s)];
const toast=$('#toast');
function showToast(msg){ if(!toast)return; toast.textContent=msg; toast.classList.add('show'); clearTimeout(window.__t); window.__t=setTimeout(()=>toast.classList.remove('show'),2600); }
function showView(name){
  $$('.app-view').forEach(v=>v.hidden=v.dataset.section!==name);
  $$('[data-view]').forEach(a=>a.classList.toggle('active',a.dataset.view===name));
  if(location.hash!==`#${name}`) history.replaceState(null,'',`#${name}`);
  window.scrollTo({top:0,behavior:'instant'});
}
function route(){ const name=(location.hash||'#create').slice(1); const exists=$(`.app-view[data-section="${name}"]`); showView(exists?name:'create'); }
window.addEventListener('hashchange',route); window.addEventListener('DOMContentLoaded',route);

$('#addLinkBtn')?.addEventListener('click',()=>{ const wrap=$('#sourceLinks'); const label=document.createElement('label'); label.className='dash-input'; label.innerHTML='<span>🔗</span><input type="url" placeholder="TikTok / YouTube Shorts" />'; wrap.appendChild(label); showToast('New link field added'); });
$('#prepareVideoBtn')?.addEventListener('click',()=>{ const links=$$('#sourceLinks input').map(i=>i.value.trim()).filter(Boolean); if(!links.length)return showToast('Paste at least one link first'); localStorage.setItem('nexoraSources',JSON.stringify(links)); showToast('Sources saved · next customization step coming'); });

$('#viralGenerateBtn')?.addEventListener('click',async()=>{
  const brief=$('#viralBrief').value.trim()||$('#viralLink').value.trim(); if(!brief)return showToast('Add a topic, notes or a link first');
  const btn=$('#viralGenerateBtn'), status=$('#viralStatus'); btn.disabled=true; btn.textContent='Generating…'; status.textContent='Nexora AI is preparing your script…';
  try{
    const res=await fetch('/api/script',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({brief,languagePair:$('#viralLanguage').value,videoType:'Short-form video',tone:$('#viralTone').value,duration:$('#viralDuration').value})});
    const data=await res.json(); if(!res.ok)throw new Error(data.error||'Generation failed'); $('#viralOutput').value=data.script||''; status.textContent=data.demo?'Demo script generated · add GEMINI_API_KEY for live AI.':'AI script generated successfully.'; showToast('Script generated ✓');
  }catch(e){ $('#viralOutput').value=`HOOK: Turn this idea into a stronger short-form story.\n\nMAIN: ${brief}\n\nNexora AI demo mode is active. Edit this draft, choose a voice, subtitle style and hook, then continue to rendering once the backend is connected.\n\nCTA: Create faster with Nexora AI.`; status.textContent='Local demo mode · connect Gemini to enable live generation.'; showToast('Using demo mode'); }
  finally{btn.disabled=false;btn.textContent='✦ Generate script';}
});

$$('.plan-card .action-btn').forEach(b=>b.addEventListener('click',()=>showToast('Checkout is not connected yet')));
$$('.voice-panel button').forEach(b=>{ if(b.classList.contains('record-orb')) b.addEventListener('click',()=>showToast('Microphone recording backend will be connected later')); });
