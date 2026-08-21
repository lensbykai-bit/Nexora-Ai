# Nexora Ai

Nexora Ai is an AI-assisted short-video creator workspace inspired by the workflow of modern short-form video tools. Version 1 focuses on a polished creator UI, local video preview, multilingual script settings, and server-side Gemini script generation.

## V1 Features

- Modern responsive dashboard
- Upload MP4 / MOV / WEBM and preview locally
- TikTok / YouTube Shorts link field
- Six language flows:
  - Chinese → Khmer
  - Khmer → Chinese
  - English → Khmer
  - Khmer → English
  - English → Chinese
  - Chinese → English
- Video type, tone and duration controls
- Auto Script API endpoint
- Google Gemini integration with server-side API key
- Demo mode when no API key is configured
- Voice profile selection UI
- Three subtitle styles
- 9:16 phone preview
- Save draft to local browser storage
- Vercel-ready project

## Project Structure

```text
Nexora-Ai/
├─ api/
│  └─ script.js
├─ .env.example
├─ .gitignore
├─ app.js
├─ index.html
├─ styles.css
├─ vercel.json
└─ README.md
```

## Run Locally

Because the frontend is plain HTML/CSS/JS, you can preview it with any local static server. The `/api/script` function is designed for Vercel.

Example frontend-only preview:

```bash
python -m http.server 8080
```

Then open `http://localhost:8080`.

## Connect Gemini

1. Create a Gemini API key in Google AI Studio.
2. Do **not** paste the key directly into `app.js` or upload it publicly to GitHub.
3. On Vercel, add an Environment Variable named `GEMINI_API_KEY`.
4. Optional: set `GEMINI_MODEL=gemini-3.7-flash`.
5. Redeploy.

The browser calls `/api/script`; the server function calls Gemini. This keeps the key hidden from website visitors.

## GitHub Repository

Recommended repository name: `Nexora-Ai`

After the empty repository exists, upload all files from this package to the repository root.

## Planned Phase 2

- Real video ingestion from public links
- Video/audio transcription
- Auto-extract script from uploaded video
- AI voice generation / TTS
- Background music library
- Subtitle timing
- FFmpeg video composition
- Render queue and download
- Login and cloud project history

## Brand

Product name: **Nexora Ai**

Version: **V1**
