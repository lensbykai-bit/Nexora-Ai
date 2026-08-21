const $=(s,r=document)=>r.querySelector(s); const $$=(s,r=document)=>[...r.querySelectorAll(s)];
const toast=$('#toast');

function injectWizardStyles(){
  if(document.getElementById('nexoraWizardStyles'))return;
  const style=document.createElement('style'); style.id='nexoraWizardStyles';
  style.textContent=`.wizard-dots{gap:12px}.wizard-dot{width:40px;height:40px;border:0;border-radius:50%;background:#17191c;color:#8f9498;font-weight:800;cursor:pointer}.wizard-dot.active{background:#4c9277;color:#fff;box-shadow:0 12px 28px rgba(68,160,125,.25)}.wizard-dot.done{background:#17352c;color:#62d3ae}.wizard-panel{max-width:720px}.wizard-field{display:grid;gap:8px;margin-top:14px}.wizard-field label,.two-grid label>span{font-size:13px;color:#d8dbdd}.wizard-actions{display:flex;justify-content:flex-end;gap:10px;flex-wrap:wrap;margin-top:24px}.wizard-actions .action-btn{margin-top:0}.secondary-btn{min-height:46px;padding:0 18px;border-radius:10px;border:1px solid #34383b;background:#101214;color:#e8eaeb;font-weight:700;cursor:pointer}.voice-choice-grid{display:grid;grid-template-columns:repeat(2,1fr);gap:12px}.voice-choice{min-height:110px;text-align:left;padding:16px;border-radius:12px;border:1px solid #303437;background:#0e1011;color:#fff;display:grid;grid-template-columns:34px 1fr;column-gap:10px;align-items:center;cursor:pointer}.voice-choice span{grid-row:1/3;font-size:22px}.voice-choice strong{font-size:15px}.voice-choice small{color:var(--muted)}.voice-choice.selected{border-color:var(--green);background:#10211c;box-shadow:0 0 0 1px rgba(78,198,162,.2)}.switch-row{margin-top:18px;display:flex;align-items:center;gap:10px;color:#d8dbdd}.switch-row input{min-height:auto;width:18px;height:18px}.export-review{display:grid;gap:0}.review-row{display:flex;justify-content:space-between;gap:20px;padding:13px 0;border-bottom:1px solid #272a2c}.review-row span{color:var(--muted)}.script-preview{margin-top:20px;background:#0d0f10;border:1px solid #2a2d30;border-radius:12px;padding:16px;max-height:260px;overflow:auto}.script-preview small{color:var(--green);font-weight:800}.script-preview p{line-height:1.65;color:#e8eaeb}.backend-note{margin-top:16px;border:1px solid #73551a;background:#2f240e;color:#e8be57;padding:14px;border-radius:10px}@media(max-width:700px){.voice-choice-grid{grid-template-columns:1fr}.wizard-actions{justify-content:stretch}.wizard-actions button{flex:1}.wizard-dot{width:34px;height:34px}}`;
  document.head.appendChild(style);
}
function showToast(msg){ if(!toast)return; toast.textContent=msg; toast.classList.add('show'); clearTimeout(window.__t); window.__t=setTimeout(()=>toast.classList.remove('show'),2600); }
function showView(name){
  $$('.app-view').forEach(v=>v.hidden=v.dataset.section!==name);
  $$('[data-view]').forEach(a=>a.classList.toggle('active',a.dataset.view===name));
  if(location.hash!==`#${name}`) history.replaceState(null,'',`#${name}`);
  if(name==='create') restoreCreateWizard();
  window.scrollTo({top:0,behavior:'instant'});
}
function route(){ const name=(location.hash||'#create').slice(1); const exists=$(`.app-view[data-section="${name}"]`); showView(exists?name:'create'); }
window.addEventListener('hashchange',route); window.addEventListener('DOMContentLoaded',()=>{injectWizardStyles(); route(); bindStaticActions();});

const stepLabels=['Links','Script','Voice','Style','Export'];
const voiceOptions=[
  {id:'auto',name:'Auto voice',meta:'Detect the best voice automatically'},
  {id:'maya',name:'Maya',meta:'Female · warm · natural'},
  {id:'dara',name:'Dara',meta:'Male · clear · energetic'},
  {id:'srey',name:'Srey',meta:'Female · bright · youthful'},
  {id:'vannak',name:'Vannak',meta:'Male · calm · mature'}
];
function getProject(){
  try{return JSON.parse(localStorage.getItem('nexoraProject')||'{}')}catch{return {}}
}
function saveProject(p){ localStorage.setItem('nexoraProject',JSON.stringify(p)); }
function currentStep(){return Number(localStorage.getItem('nexoraWizardStep')||1)}
function setStep(step){localStorage.setItem('nexoraWizardStep',String(step)); renderCreateStep(step);}
function wizardHeader(step,title,subtitle=''){
  return `<div class="step-dots wizard-dots">${[1,2,3,4,5].map(n=>`<button type="button" class="wizard-dot ${n===step?'active':''} ${n<step?'done':''}" data-step-jump="${n}">${n<step?'✓':n}</button>`).join('')}</div>
  <p class="step-caption">Step ${step} of 5 · ${stepLabels[step-1]}</p>
  <header class="view-header centered"><h1>${title}</h1>${subtitle?`<p>${subtitle}</p>`:''}</header>`;
}
function bindStepJumps(){
  $$('[data-step-jump]').forEach(btn=>btn.addEventListener('click',()=>{
    const target=Number(btn.dataset.stepJump); const max=Math.max(1,currentStep());
    if(target<=max) setStep(target); else showToast('Complete the current step first');
  }));
}
function restoreCreateWizard(){
  const step=currentStep();
  if(step>1) renderCreateStep(step);
  else{
    const saved=getProject().sources||[];
    const inputs=$$('#sourceLinks input'); saved.slice(0,inputs.length).forEach((v,i)=>inputs[i].value=v);
    bindCreateStepOne();
  }
}
function bindCreateStepOne(){
  $('#addLinkBtn')?.addEventListener('click',()=>{ const wrap=$('#sourceLinks'); const label=document.createElement('label'); label.className='dash-input'; label.innerHTML='<span>🔗</span><input type="url" placeholder="TikTok / YouTube Shorts" />'; wrap.appendChild(label); showToast('New link field added'); });
  $('#prepareVideoBtn')?.addEventListener('click',()=>{
    const links=$$('#sourceLinks input').map(i=>i.value.trim()).filter(Boolean);
    if(!links.length)return showToast('Paste at least one link first');
    const p=getProject(); p.sources=links; saveProject(p); localStorage.setItem('nexoraSources',JSON.stringify(links));
    setStep(2); showToast('Sources saved ✓');
  });
}
function renderCreateStep(step){
  const view=$('.app-view[data-section="create"]'); if(!view)return;
  const p=getProject();
  if(step===1){ location.reload(); return; }
  if(step===2){
    view.innerHTML=`${wizardHeader(2,'Create your script','Choose a hook, language and tone. Nexora AI can prepare the first draft.')}
    <div class="panel medium-panel wizard-panel">
      <div class="wizard-field"><label>Hook style</label><select id="wizardHook"><option>Curiosity hook</option><option>Problem → solution</option><option>Bold statement</option><option>Story hook</option><option>Question hook</option></select></div>
      <div class="three-grid"><select id="wizardLanguage"><option>English to Khmer</option><option>Khmer to English</option><option>Chinese to Khmer</option><option>Khmer to Chinese</option><option>English to Chinese</option><option>Chinese to English</option></select><select id="wizardTone"><option>Natural & engaging</option><option>Energetic</option><option>Professional</option><option>Story-driven</option></select><select id="wizardDuration"><option value="30">30 seconds</option><option value="60">60 seconds</option><option value="180">3 minutes</option><option value="600">10 minutes</option></select></div>
      <textarea id="wizardBrief" class="dash-textarea" rows="5" placeholder="Describe the topic, product, story, or what the video should say...">${escapeHtml(p.brief||'')}</textarea>
      <button class="action-btn wide" id="wizardGenerateScript">✦ Generate AI script</button>
      <textarea id="wizardScript" class="dash-textarea output" rows="10" placeholder="Your script will appear here...">${escapeHtml(p.script||'')}</textarea>
      <div class="wizard-actions"><button class="secondary-btn" data-back="1">← Back</button><button class="action-btn" id="wizardScriptNext">Continue to voice →</button></div>
      <small id="wizardScriptStatus" class="muted">Gemini is used when GEMINI_API_KEY is configured. Demo mode works without a key.</small>
    </div>`;
    $('#wizardHook').value=p.hook||'Curiosity hook'; $('#wizardLanguage').value=p.language||'English to Khmer'; $('#wizardTone').value=p.tone||'Natural & engaging'; $('#wizardDuration').value=String(p.duration||30);
    $('#wizardGenerateScript').addEventListener('click',generateWizardScript);
    $('#wizardScriptNext').addEventListener('click',()=>{
      const script=$('#wizardScript').value.trim(); if(!script)return showToast('Generate or enter a script first');
      Object.assign(p,{hook:$('#wizardHook').value,language:$('#wizardLanguage').value,tone:$('#wizardTone').value,duration:$('#wizardDuration').value,brief:$('#wizardBrief').value.trim(),script}); saveProject(p); setStep(3);
    });
  }
  if(step===3){
    view.innerHTML=`${wizardHeader(3,'Choose your narrator','Use Auto voice or pick a narrator preset for this project.')}
    <div class="panel medium-panel wizard-panel"><div class="voice-choice-grid">${voiceOptions.map(v=>`<button type="button" class="voice-choice ${p.voice===v.id||(!p.voice&&v.id==='auto')?'selected':''}" data-voice="${v.id}"><span>🎙</span><strong>${v.name}</strong><small>${v.meta}</small></button>`).join('')}</div>
    <div class="wizard-field"><label>Voice speed</label><select id="voiceSpeed"><option value="0.9">0.9× Relaxed</option><option value="1" selected>1.0× Natural</option><option value="1.1">1.1× Energetic</option></select></div>
    <div class="wizard-actions"><button class="secondary-btn" data-back="2">← Back</button><button class="action-btn" id="wizardVoiceNext">Continue to style →</button></div></div>`;
    if(p.voiceSpeed) $('#voiceSpeed').value=p.voiceSpeed;
    $$('.voice-choice').forEach(b=>b.addEventListener('click',()=>{$$('.voice-choice').forEach(x=>x.classList.remove('selected'));b.classList.add('selected');}));
    $('#wizardVoiceNext').addEventListener('click',()=>{const selected=$('.voice-choice.selected'); p.voice=selected?.dataset.voice||'auto'; p.voiceSpeed=$('#voiceSpeed').value; saveProject(p); setStep(4);});
  }
  if(step===4){
    view.innerHTML=`${wizardHeader(4,'Style your video','Choose captions, music and the vertical format.')}
    <div class="panel medium-panel wizard-panel">
      <div class="two-grid"><label><span>Subtitle style</span><select id="subtitleStyle"><option>Clean white</option><option>Bold yellow highlight</option><option>Karaoke word highlight</option><option>Minimal captions</option></select></label><label><span>Caption position</span><select id="captionPosition"><option>Bottom</option><option>Center</option><option>Top</option></select></label></div>
      <div class="two-grid"><label><span>Background music</span><select id="musicStyle"><option>No music</option><option>Trending upbeat</option><option>Soft cinematic</option><option>Lo-fi</option><option>Corporate</option></select></label><label><span>Aspect ratio</span><select id="aspectRatio"><option>9:16 Vertical</option><option>1:1 Square</option><option>16:9 Landscape</option></select></label></div>
      <label class="switch-row"><input id="autoCaptions" type="checkbox" checked><span>Auto-generate subtitles from the script</span></label>
      <div class="wizard-actions"><button class="secondary-btn" data-back="3">← Back</button><button class="action-btn" id="wizardStyleNext">Review export →</button></div>
    </div>`;
    if(p.subtitleStyle) $('#subtitleStyle').value=p.subtitleStyle; if(p.captionPosition) $('#captionPosition').value=p.captionPosition; if(p.musicStyle) $('#musicStyle').value=p.musicStyle; if(p.aspectRatio) $('#aspectRatio').value=p.aspectRatio; if(typeof p.autoCaptions==='boolean') $('#autoCaptions').checked=p.autoCaptions;
    $('#wizardStyleNext').addEventListener('click',()=>{Object.assign(p,{subtitleStyle:$('#subtitleStyle').value,captionPosition:$('#captionPosition').value,musicStyle:$('#musicStyle').value,aspectRatio:$('#aspectRatio').value,autoCaptions:$('#autoCaptions').checked});saveProject(p);setStep(5);});
  }
  if(step===5){
    const voice=voiceOptions.find(v=>v.id===(p.voice||'auto'))?.name||'Auto voice';
    view.innerHTML=`${wizardHeader(5,'Review & export','Your project setup is complete. Review the choices before rendering.')}
    <div class="panel medium-panel wizard-panel export-review">
      <div class="review-row"><span>Sources</span><strong>${(p.sources||[]).length} link${(p.sources||[]).length===1?'':'s'}</strong></div>
      <div class="review-row"><span>Language</span><strong>${escapeHtml(p.language||'English to Khmer')}</strong></div>
      <div class="review-row"><span>Duration</span><strong>${escapeHtml(String(p.duration||30))} sec</strong></div>
      <div class="review-row"><span>Voice</span><strong>${voice}</strong></div>
      <div class="review-row"><span>Subtitles</span><strong>${escapeHtml(p.subtitleStyle||'Clean white')}</strong></div>
      <div class="review-row"><span>Music</span><strong>${escapeHtml(p.musicStyle||'No music')}</strong></div>
      <div class="review-row"><span>Format</span><strong>${escapeHtml(p.aspectRatio||'9:16 Vertical')}</strong></div>
      <div class="script-preview"><small>SCRIPT PREVIEW</small><p>${escapeHtml((p.script||'No script yet').slice(0,700)).replace(/\n/g,'<br>')}</p></div>
      <div class="backend-note">⚙ The editor flow is ready. Final video rendering still needs a render/TTS backend connection.</div>
      <div class="wizard-actions"><button class="secondary-btn" data-back="4">← Back</button><button class="action-btn" id="saveProjectBtn">Save project</button><button class="action-btn" id="renderProjectBtn">Render video</button></div>
    </div>`;
    $('#saveProjectBtn').addEventListener('click',()=>{p.savedAt=new Date().toISOString();saveProject(p);localStorage.setItem('nexoraLastCreation',JSON.stringify(p));showToast('Project saved ✓');});
    $('#renderProjectBtn').addEventListener('click',()=>showToast('Render engine is the next backend connection'));
  }
  $$('[data-back]').forEach(b=>b.addEventListener('click',()=>setStep(Number(b.dataset.back))));
  bindStepJumps();
}
async function generateWizardScript(){
  const brief=$('#wizardBrief').value.trim()||`Create a short-form video using these source links: ${(getProject().sources||[]).join(', ')}`;
  const btn=$('#wizardGenerateScript'), status=$('#wizardScriptStatus'); btn.disabled=true; btn.textContent='Generating…'; status.textContent='Nexora AI is preparing your script…';
  try{
    const res=await fetch('/api/script',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({brief,languagePair:$('#wizardLanguage').value,videoType:'Short-form video',tone:$('#wizardTone').value,duration:$('#wizardDuration').value})});
    const data=await res.json(); if(!res.ok)throw new Error(data.error||'Generation failed'); $('#wizardScript').value=data.script||''; status.textContent=data.demo?'Demo script generated · connect GEMINI_API_KEY for live AI.':'AI script generated successfully.'; showToast('Script generated ✓');
  }catch(e){ $('#wizardScript').value=`HOOK: Here is a faster way to turn this idea into a short-form video.\n\nMAIN: ${brief}\n\nKeep the message clear, visual, and easy to follow.\n\nCTA: Create faster with Nexora AI.`; status.textContent='Local demo mode is active.'; showToast('Using demo mode'); }
  finally{btn.disabled=false;btn.textContent='✦ Generate AI script';}
}
function escapeHtml(s=''){return String(s).replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]));}

async function generateViralScript(){
  const brief=$('#viralBrief')?.value.trim()||$('#viralLink')?.value.trim(); if(!brief)return showToast('Add a topic, notes or a link first');
  const btn=$('#viralGenerateBtn'), status=$('#viralStatus'); btn.disabled=true; btn.textContent='Generating…'; status.textContent='Nexora AI is preparing your script…';
  try{
    const res=await fetch('/api/script',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({brief,languagePair:$('#viralLanguage').value,videoType:'Short-form video',tone:$('#viralTone').value,duration:$('#viralDuration').value})});
    const data=await res.json(); if(!res.ok)throw new Error(data.error||'Generation failed'); $('#viralOutput').value=data.script||''; status.textContent=data.demo?'Demo script generated · add GEMINI_API_KEY for live AI.':'AI script generated successfully.'; showToast('Script generated ✓');
  }catch(e){ $('#viralOutput').value=`HOOK: Turn this idea into a stronger short-form story.\n\nMAIN: ${brief}\n\nNexora AI demo mode is active.\n\nCTA: Create faster with Nexora AI.`; status.textContent='Local demo mode · connect Gemini to enable live generation.'; showToast('Using demo mode'); }
  finally{btn.disabled=false;btn.textContent='✦ Generate script';}
}
function bindStaticActions(){
  $('#viralGenerateBtn')?.addEventListener('click',generateViralScript);
  $$('.plan-card .action-btn').forEach(b=>b.addEventListener('click',()=>showToast('Checkout is not connected yet')));
  $$('.voice-panel button').forEach(b=>{ if(b.classList.contains('record-orb')) b.addEventListener('click',()=>showToast('Microphone recording backend will be connected later')); });
}
