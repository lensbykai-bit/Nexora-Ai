const PRIMARY_MODEL = process.env.GEMINI_MODEL || 'gemini-3.7-flash';
const GEMINI_MODELS = [...new Set([
  PRIMARY_MODEL,
  'gemini-3.6-flash',
  'gemini-3.5-flash-lite',
  'gemini-2.5-flash'
])];
const GATEWAY_MODEL = process.env.AI_GATEWAY_MODEL || 'google/gemini-3.5-flash-lite';

export default async function handler(req, res) {
  if (req.method !== 'POST') return res.status(405).json({ error: 'Method not allowed' });

  const { brief, languagePair, videoType, tone, duration, mode = 'script' } = req.body || {};
  if (!brief || typeof brief !== 'string') {
    return res.status(400).json({ error: 'A video brief is required.' });
  }

  const prompt = mode === 'creator-pack'
    ? buildCreatorPackPrompt({ brief, languagePair, videoType, tone, duration })
    : buildPrompt({ brief, languagePair, videoType, tone, duration });

  if (process.env.GEMINI_API_KEY) {
    for (const model of GEMINI_MODELS) {
      try {
        const result = await generateWithGemini(prompt, process.env.GEMINI_API_KEY, model);
        return res.status(200).json({ script: result, model, provider: 'google-gemini', mode });
      } catch (error) {
        console.error(`Gemini model ${model} failed:`, error?.message || error);
      }
    }
  }

  const gatewayToken = process.env.AI_GATEWAY_API_KEY || process.env.VERCEL_OIDC_TOKEN;
  if (gatewayToken) {
    try {
      const result = await generateWithGateway(prompt, gatewayToken);
      return res.status(200).json({ script: result, model: GATEWAY_MODEL, provider: 'vercel-ai-gateway', mode });
    } catch (error) {
      console.error('Vercel AI Gateway failed:', error);
    }
  }

  return res.status(200).json({
    demo: true,
    provider: 'demo',
    mode,
    script: mode === 'creator-pack'
      ? demoCreatorPack({ brief, languagePair, videoType, tone, duration })
      : demoScript({ brief, languagePair, videoType, tone, duration })
  });
}

function buildPrompt({ brief, languagePair, videoType, tone, duration }) {
  return `You are the script engine inside Nexora AI, a short-video creator.\n\nCreate an ORIGINAL short-form narration using these settings:\n- Output/language instruction: ${languagePair || 'English'}\n- Video type: ${videoType || 'Short-form video'}\n- Tone: ${tone || 'Natural & engaging'}\n- Target duration: ${duration || 30} seconds\n- Source context / creator brief: ${brief}\n\nRequirements:\n1. Start with a strong, safe hook.\n2. Keep sentences natural and easy to speak aloud.\n3. Follow the requested language exactly.\n4. Organize as HOOK, MAIN, CTA.\n5. Make the wording original; do not copy a source video's transcript or captions.\n6. Do not include production notes unless essential.\n7. Output only the finished script.`;
}

function buildCreatorPackPrompt({ brief, languagePair, videoType, tone, duration }) {
  return `You are Nexora Quick Creator. Create a COMPLETE, ORIGINAL short-form creator pack from the user's idea.\n\nSETTINGS\n- Output language: ${languagePair || 'English'}\n- Platform / format: ${videoType || 'TikTok / Shorts / Reels'}\n- Tone: ${tone || 'Natural & engaging'}\n- Target duration: ${duration || 30} seconds\n- Topic, notes, or source context: ${brief}\n\nRULES\n- Do not copy or reconstruct a copyrighted source transcript. Use the topic only as inspiration/context.\n- Keep the content suitable for mainstream social platforms.\n- Follow the requested output language consistently, except hashtags may contain commonly used English terms when natural.\n- Make the hook strong but not deceptive.\n- Keep the script easy to speak aloud.\n- Return EXACTLY these labels so the app can split the result:\n\nTITLE: one concise title\nHOOK: one strong opening line\nSCRIPT:\nfull narration script\nCAPTION: one ready-to-post caption\nHASHTAGS: 5 to 8 relevant hashtags on one line\nTHUMBNAIL_PROMPT: one clear image-generation prompt for a vertical 9:16 thumbnail, no logos or copyrighted characters\n\nDo not add any extra headings before or after these labels.`;
}

async function generateWithGemini(prompt, apiKey, model) {
  const generationConfig = { maxOutputTokens: 2200 };
  if (!/^gemini-3\.(5|6)/.test(model)) generationConfig.temperature = 0.8;

  const response = await fetch(
    `https://generativelanguage.googleapis.com/v1beta/models/${encodeURIComponent(model)}:generateContent`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'x-goog-api-key': apiKey
      },
      body: JSON.stringify({
        contents: [{ role: 'user', parts: [{ text: prompt }] }],
        generationConfig
      })
    }
  );

  const data = await response.json();
  if (!response.ok) throw new Error(data?.error?.message || `Gemini ${model} request failed.`);
  const script = data?.candidates?.[0]?.content?.parts?.map(p => p.text || '').join('').trim();
  if (!script) throw new Error(`Gemini ${model} returned an empty script.`);
  return script;
}

async function generateWithGateway(prompt, token) {
  const response = await fetch('https://ai-gateway.vercel.sh/v1/chat/completions', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      model: GATEWAY_MODEL,
      messages: [{ role: 'user', content: prompt }],
      max_tokens: 2200,
      stream: false
    })
  });
  const data = await response.json();
  if (!response.ok) throw new Error(data?.error?.message || data?.message || 'AI Gateway request failed.');
  const script = data?.choices?.[0]?.message?.content?.trim();
  if (!script) throw new Error('AI Gateway returned an empty script.');
  return script;
}

function demoScript({ brief, languagePair, videoType, tone, duration }) {
  return `HOOK: Turn this idea into an attention-grabbing opening.\n\nMAIN: ${brief}\n\nThis original draft is prepared for a ${duration || 30}-second ${videoType || 'short-form'} video with a ${tone || 'natural'} tone. Language instruction: ${languagePair || 'English'}.\n\nCTA: Create smarter. Create faster. Create with Nexora AI.`;
}

function demoCreatorPack({ brief, languagePair, videoType, tone, duration }) {
  return `TITLE: Your next short-form content idea\nHOOK: Here is a faster way to turn one idea into a complete post.\nSCRIPT:\n${brief}\n\nThis demo is prepared for a ${duration || 30}-second ${videoType || 'short-form'} video with a ${tone || 'natural'} tone in ${languagePair || 'English'}. Connect Gemini to replace this demo with live AI output.\nCAPTION: One idea. One click. A complete creator pack ready to refine and post.\nHASHTAGS: #contentcreator #shortvideo #tiktokideas #youtubeshorts #reels\nTHUMBNAIL_PROMPT: Vertical 9:16 social video thumbnail, bold central subject, clean high-contrast composition, modern creator aesthetic, dramatic lighting, no logos, no text.`;
}
