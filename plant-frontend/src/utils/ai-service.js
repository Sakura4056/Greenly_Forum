export async function generateAIResponse(provider, baseUrl, apiKey, model, messages, onUpdate) {
  if (!apiKey) {
    throw new Error('请先在右上角设置中配置 API Key');
  }

  // Convert our message format to API format
  const formattedMessages = messages.map(msg => ({
    role: msg.role === 'ai' ? 'assistant' : 'user',
    content: msg.content
  }));

  // Append a system prompt if it's the first message
  const systemPrompt = {
    role: 'system',
    content: '你是一个叫 Greenly AI 的植物养护专家。请用专业、友好、简洁的中文回答关于植物养护、病虫害防治、植物识别等问题。如果用户问了与植物无关的问题，请委婉地将其引导回植物话题。'
  };

  const apiMessages = [systemPrompt, ...formattedMessages];

  try {
    if (provider === 'gemini') {
      return await handleGeminiFormat(apiKey, model, apiMessages, onUpdate);
    } else {
      // OpenAI format covers OpenAI, DeepSeek, proxy base URLs
      return await handleOpenAIFormat(baseUrl, apiKey, model, apiMessages, onUpdate);
    }
  } catch (error) {
    console.error('AI Service Error:', error);
    throw new Error(error.message || '请求失败，请检查网络或 API Key 设置');
  }
}

async function handleOpenAIFormat(baseUrl, apiKey, model, messages, onUpdate) {
  const endpoint = baseUrl.endsWith('/') ? `${baseUrl}chat/completions` : `${baseUrl}/chat/completions`;
  
  const response = await fetch(endpoint, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${apiKey}`
    },
    body: JSON.stringify({
      model: model || 'gpt-3.5-turbo',
      messages: messages,
      stream: true
    })
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.error?.message || `HTTP Request Error: ${response.status}`);
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder('utf-8');
  let fullText = '';

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    
    const chunk = decoder.decode(value, { stream: true });
    const lines = chunk.split('\n').filter(line => line.trim() !== '');
    
    for (const line of lines) {
      if (line === 'data: [DONE]') return fullText;
      if (line.startsWith('data: ')) {
        try {
          const data = JSON.parse(line.slice(6));
          if (data.choices && data.choices[0].delta && data.choices[0].delta.content) {
            fullText += data.choices[0].delta.content;
            onUpdate(fullText);
          }
        } catch (e) {
          // ignore parse error on incomplete chunks
        }
      }
    }
  }
  
  return fullText;
}

async function handleGeminiFormat(apiKey, model, messages, onUpdate) {
  const actualModel = model || 'gemini-1.5-flash';
  const endpoint = `https://generativelanguage.googleapis.com/v1beta/models/${actualModel}:streamGenerateContent?key=${apiKey}`;
  
  // Convert OpenAI messages format to Gemini format
  const geminiMessages = messages.filter(m => m.role !== 'system').map(m => ({
    role: m.role === 'assistant' ? 'model' : 'user',
    parts: [{ text: m.content }]
  }));

  // Find system prompt
  const systemMessage = messages.find(m => m.role === 'system');
  const systemInstruction = systemMessage ? {
    parts: [{ text: systemMessage.content }]
  } : undefined;

  const payload = {
    contents: geminiMessages,
    systemInstruction
  };

  const response = await fetch(endpoint, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.error?.message || `HTTP Request Error: ${response.status}`);
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder('utf-8');
  let fullText = '';
  // Simple JSON array streaming parser for Gemini SSE format... Wait, Gemini doesn't use standard SSE for streaming. It returns an array of JSON objects incrementally if we use streamGenerateContent with alt=sse, but standard POST returns JSON array stream.
  // Actually, for simplicity and stability without a complex JSON stream parser, let's use non-streaming for Gemini if we just use generateContent, OR properly parse SSE.
  // The correct endpoint for SSE streaming is: ?alt=sse
  const sseEndpoint = `${endpoint}&alt=sse`;
  
  const sseResponse = await fetch(sseEndpoint, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload)
  });

  if (!sseResponse.ok) {
     const errorData = await sseResponse.json().catch(() => ({}));
     throw new Error(errorData.error?.message || `Gemini Request Error: ${sseResponse.status}`);
  }

  const sseReader = sseResponse.body.getReader();
  let buffer = '';

  while (true) {
    const { done, value } = await sseReader.read();
    if (done) break;
    
    buffer += decoder.decode(value, { stream: true });
    
    let boundary = buffer.indexOf('\n\n');
    while (boundary !== -1) {
        const chunk = buffer.slice(0, boundary);
        buffer = buffer.slice(boundary + 2);
        
        if (chunk.startsWith('data: ')) {
            const dataStr = chunk.slice(6);
            if (dataStr.trim() !== '') {
                try {
                    const data = JSON.parse(dataStr);
                    if (data.candidates && data.candidates[0].content?.parts[0]?.text) {
                        fullText += data.candidates[0].content.parts[0].text;
                        onUpdate(fullText);
                    }
                } catch(e) {}
            }
        }
        boundary = buffer.indexOf('\n\n');
    }
  }

  return fullText;
}
