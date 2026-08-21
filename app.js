const $ = (s) => document.querySelector(s);
const $$ = (s) => [...document.querySelectorAll(s)];
const toast = $('#toast');

function showToast(message) {
  toast.textContent = message;
  toast.classList.add('show');
  clearTimeout(window.__toastTimer);
  window.__toastTimer = setTimeout(() => toast.classList.remove('show'), 2600);
}

$$('.source-tab').forEach((tab) => {
  tab.addEventListener('click', () => {
    $$('.source-tab').forEach(t => t.classList.remove('active'));
    tab.classList.add('active');
    const source = tab.dataset.source;
    $('#uploadPanel').classList.toggle('active', source === 'upload');
    $('#linkPanel').classList.toggle('active', source === 'link');
  });
});

const dropzone = $('#dropzone');
const videoInput = $('#videoInput');
const videoPreview = $('#videoPreview');
const previewPlaceholder = $('#previewPlaceholder');
const fileCard = $('#fileCard');

['dragenter','dragover'].forEach(type => dropzone.addEventListener(type, e => {
  e.preventDefault(); dropzone.classList.add('dragover');
}));
['dragleave','drop'].forEach(type => dropzone.addEventListener(type, e => {
  e.preventDefault(); dropzone.classList.remove('dragover');
}));
dropzone.addEventListener('drop', e => {
  if (e.dataTransfer.files?.[0]) handleVideo(e.dataTransfer.files[0]);
});
videoInput.addEventListener('change', () => {
  if (videoInput.files?.[0]) handleVideo(videoInput.files[0]);
});

function handleVideo(file) {
  if (!file.type.startsWith('video/')) return showToast('Please choose a video file.');
  const mb = file.size / 1024 / 1024;
  if (mb > 500) return showToast('Video must be under 500 MB.');
  videoPreview.src = URL.createObjectURL(file);
  videoPreview.style.display = 'block';
  videoPreview.play().catch(() => {});
  previewPlaceholder.style.display = 'none';
  fileCard.classList.remove('hidden');
  fileCard.innerHTML = `<span>🎬 <strong>${escapeHtml(file.name)}</strong> · ${mb.toFixed(1)} MB</span><span>Ready ✓</span>`;
  $('#summarySource').textContent = 'Uploaded';
  updateReadyState();
  showToast('Video added to Nexora Ai.');
}

$('#importLinkBtn').addEventListener('click', () => {
  const value = $('#videoUrl').value.trim();
  if (!/^https?:\/\//i.test(value)) return showToast('Paste a valid TikTok or YouTube link.');
  $('#summarySource').textContent = 'Video link';
  updateReadyState();
  showToast('Link saved. Server import can be connected in Phase 2.');
});

$('#languagePair').addEventListener('change', e => $('#summaryLanguage').textContent = prettyLanguage(e.target.value));
$('#duration').addEventListener('change', e => $('#summaryDuration').textContent = `${e.target.value} sec`);
$$('input[name="voice"]').forEach(input => input.addEventListener('change', e => {
  $$('.choice-card').forEach(c => c.classList.remove('active-choice'));
  e.target.closest('.choice-card').classList.add('active-choice');
  $('#summaryVoice').textContent = e.target.value;
}));

$$('.subtitle-style').forEach(btn => btn.addEventListener('click', () => {
  $$('.subtitle-style').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  $('#captionPreview').className = `caption-preview ${btn.dataset.style}`;
}));

$('#scriptOutput').addEventListener('input', updateWordCount);

$('#generateScriptBtn').addEventListener('click', async () => {
  const brief = $('#brief').value.trim();
  if (!brief) return showToast('Add a short topic or instruction first.');
  const btn = $('#generateScriptBtn');
  const status = $('#scriptStatus');
  btn.disabled = true;
  btn.innerHTML = '<span>✦</span> Generating…';
  status.textContent = 'Nexora Ai is writing your script…';
  try {
    const response = await fetch('/api/script', {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({
        brief,
        languagePair: $('#languagePair').value,
        videoType: $('#videoType').value,
        tone: $('#tone').value,
        duration: $('#duration').value
      })
    });
    const data = await response.json();
    if (!response.ok) throw new Error(data.error || 'Script generation failed');
    $('#scriptOutput').value = data.script || '';
    updateWordCount();
    const firstWords = (data.script || '').split(/\s+/).slice(0, 5).join(' ');
    $('#captionPreview').innerHTML = `${escapeHtml(firstWords || 'YOUR AI SCRIPT')}<br><mark>READY TO EDIT</mark>`;
    status.textContent = data.demo ? 'Demo script generated. Add GEMINI_API_KEY for live AI.' : 'AI script generated successfully.';
    updateReadyState();
    showToast('Script generated ✓');
  } catch (error) {
    const demo = buildLocalDemoScript(brief);
    $('#scriptOutput').value = demo;
    updateWordCount();
    status.textContent = 'Local demo mode — connect GEMINI_API_KEY to use live AI.';
    updateReadyState();
    showToast('Using demo mode until API is connected.');
  } finally {
    btn.disabled = false;
    btn.innerHTML = '<span>✦</span> Auto Script';
  }
});

function buildLocalDemoScript(brief) {
  const pair = $('#languagePair').value;
  const seconds = $('#duration').value;
  return `HOOK: Want to make your next short video faster?\n\nMAIN: ${brief}\n\nNexora Ai turns your idea into a clear ${seconds}-second script. Selected language flow: ${pair}. Edit this draft, choose your voice and subtitle style, then prepare it for export.\n\nCTA: Create smarter. Create faster. Create with Nexora Ai.`;
}

$('#renderBtn').addEventListener('click', () => {
  const hasScript = $('#scriptOutput').value.trim().length > 0;
  if (!hasScript) return showToast('Generate or write a script before continuing.');
  showToast('Project prepared ✓ Video rendering engine is the next backend module.');
});

$('#saveDraftBtn').addEventListener('click', () => {
  const draft = {
    brief: $('#brief').value,
    script: $('#scriptOutput').value,
    languagePair: $('#languagePair').value,
    videoType: $('#videoType').value,
    tone: $('#tone').value,
    duration: $('#duration').value,
    savedAt: new Date().toISOString()
  };
  localStorage.setItem('nexoraAiDraft', JSON.stringify(draft));
  showToast('Draft saved in this browser.');
});

window.addEventListener('DOMContentLoaded', () => {
  const saved = localStorage.getItem('nexoraAiDraft');
  if (!saved) return;
  try {
    const d = JSON.parse(saved);
    $('#brief').value = d.brief || '';
    $('#scriptOutput').value = d.script || '';
    if (d.languagePair) $('#languagePair').value = d.languagePair;
    if (d.videoType) $('#videoType').value = d.videoType;
    if (d.tone) $('#tone').value = d.tone;
    if (d.duration) $('#duration').value = d.duration;
    $('#summaryLanguage').textContent = prettyLanguage($('#languagePair').value);
    $('#summaryDuration').textContent = `${$('#duration').value} sec`;
    updateWordCount();
    updateReadyState();
  } catch {}
});

function updateWordCount() {
  const text = $('#scriptOutput').value.trim();
  const count = text ? text.split(/\s+/).length : 0;
  $('#wordCount').textContent = `${count} words`;
}

function updateReadyState() {
  let ready = 2;
  if ($('#summarySource').textContent !== 'Not added') ready++;
  if ($('#scriptOutput').value.trim()) ready++;
  if ($('#summaryVoice').textContent) ready++;
  ready = Math.min(5, ready);
  $('#setupState').textContent = `${ready}/5 ready`;
}

function prettyLanguage(value) {
  return value
    .replace('English to Khmer','English → ខ្មែរ')
    .replace('Khmer to English','ខ្មែរ → English')
    .replace('Chinese to Khmer','中文 → ខ្មែរ')
    .replace('Khmer to Chinese','ខ្មែរ → 中文')
    .replace('English to Chinese','English → 中文')
    .replace('Chinese to English','中文 → English');
}

function escapeHtml(str='') {
  return str.replace(/[&<>'"]/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[c]));
}
