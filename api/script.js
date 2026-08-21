const MODEL = process.env.GEMINI_MODEL || 'gemini-3.7-flash';

export default async function handler(req, res) {
  if (req.method !== 'POST') return res.status(405).json({ error: 'Method not allowed' });

  const { brief, languagePair, videoType, tone, duration } = req.body || {};
  if (!brief || typeof brief !== 'string') {
    return res.status(400).json({ error: 'A video brief is required.' });
  }

  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey) {
    return res.status(200).json({
      demo: true,
      script: demoScript({ brief, languagePair, videoType, tone, duration })
    });
  }

  const prompt = `You are the script engine inside Nexora Ai, a short-video creator.\n\nCreate a polished script using these settings:\n- Language flow: ${languagePair || 'English to Khmer'}\n- Video type: ${videoType || 'Short-form video'}\n- Tone: ${tone || 'Natural & engaging'}\n- Target duration: ${duration || 30} seconds\n- User brief: ${brief}\n\nRequirements:\n1. Start with a strong, safe hook.\n2. Keep sentences easy to speak aloud.\n3. Respect the requested language direction. If translating, preserve meaning naturally rather than word-for-word.\n4. Organize as HOOK, MAIN, CTA.\n5. Do not include production notes unless essential.\n6. Output only the finished script.`;

  try {
    const response = await fetch(
      `https://generativelanguage.googleapis.com/v1beta/models/${encodeURIComponent(MODEL)}:generateContent`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'x-goog-api-key': apiKey
        },
        body: JSON.stringify({
          contents: [{ role: 'user', parts: [{ text: prompt }] }],
          generationConfig: { temperature: 0.8, maxOutputTokens: 1200 }
        })
      }
    );

    const data = await response.json();
    if (!response.ok) {
      console.error('Gemini API error:', data);
      return res.status(502).json({ error: data?.error?.message || 'AI provider request failed.' });
    }

    const script = data?.candidates?.[0]?.content?.parts?.map(p => p.text || '').join('').trim();
    if (!script) return res.status(502).json({ error: 'AI returned an empty script.' });
    return res.status(200).json({ script, model: MODEL });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ error: 'Unable to generate script right now.' });
  }
}

function demoScript({ brief, languagePair, videoType, tone, duration }) {
  return `HOOK: Turn one idea into a short-video script in seconds.\n\nMAIN: ${brief}\n\nThis draft is prepared for a ${duration || 30}-second ${videoType || 'short-form'} video with a ${tone || 'natural'} tone. Language flow: ${languagePair || 'English to Khmer'}. Connect your Gemini API key to replace this demo with live AI generation.\n\nCTA: Create smarter. Create faster. Create with Nexora Ai.`;
}
