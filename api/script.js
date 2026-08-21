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

  const { brief, languagePair, videoType, tone, duration } = req.body || {};
  if (!brief || typeof brief !== 'string') {
    return res.status(400).json({ error: 'A video brief is required.' });
  }

  const prompt = buildPrompt({ brief, languagePair, videoType, tone, duration });

  if (process.env.GEMINI_API_KEY) {
    for (const model of GEMINI_MODELS) {
      try {
        const result = await generateWithGemini(prompt, process.env.GEMINI_API_KEY, model);
        return res.status(200).json({ script: result, model, provider: 'google-gemini' });
      } catch (error) {
        console.error(`Gemini model ${model} failed:`, error?.message || error);
      }
    }
  }

  const gatewayToken = process.env.AI_GATEWAY_API_KEY || process.env.VERCEL_OIDC_TOKEN;
  if (gatewayToken) {
    try {
      const result = await generateWithGateway(prompt, gatewayToken);
      return res.status(200).json({ script: result, model: GATEWAY_MODEL, provider: 'vercel-ai-gateway' });
    } catch (error) {
      console.error('Vercel AI Gateway failed:', error);
    }
  }

  return res.status(200).json({
    demo: true,
    provider: 'demo',
    script: demoScript({ brief, languagePair, videoType, tone, duration })
  });
}

function buildPrompt({ brief, languagePair, videoType, tone, duration }) {
  return `You are the script engine inside Nexora AI, a short-video creator.\n\nCreate an ORIGINAL short-form narration using these settings:\n- Output/language instruction: ${languagePair || 'English'}\n- Video type: ${videoType || 'Short-form video'}\n- Tone: ${tone || 'Natural & engaging'}\n- Target duration: ${duration || 30} seconds\n- Source context / creator brief: ${brief}\n\nRequirements:\n1. Start with a strong, safe hook.\n2. Keep sentences natural and easy to speak aloud.\n3. Follow the requested language exactly.\n4. Organize as HOOK, MAIN, CTA.\n5. Make the wording original; do not copy a source video's transcript or captions.\n6. Do not include production notes unless essential.\n7. Output only the finished script.`;
}

async function generateWithGemini(prompt, apiKey, model) {
  const generationConfig = { maxOutputTokens: 1600 };
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
      max_tokens: 1600,
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
