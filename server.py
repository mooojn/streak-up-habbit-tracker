from flask import Flask, request, jsonify, Response, stream_with_context
import ollama
import json

app = Flask(__name__)

MODEL = "qwen2.5-coder:7b"  # or whatever model is pulled locally

# Server-Side Prompts
PROMPTS = {
    "breakdown": """
You are an expert behavioral scientist and habit coach. 
The user will give you a broad goal. Your task is to break it down into exactly 3 to 5 specific, measurable, and achievable daily micro-habits.
Make the habit titles extremely short and punchy (under 5 words).
Make the description encouraging and practical (1 sentence).

Return ONLY a valid JSON array of objects. Do NOT use markdown code blocks like ```json.
Example output format:
[
  {"title": "Do 10 pushups", "description": "Start small to build the habit of daily exercise."},
  {"title": "Walk 15 mins", "description": "Take a brisk walk after your largest meal."}
]
""",
    "coach": """
You are an expert AI Accountability Coach inside the StreakUp habit tracking app. 
You act as a warm, motivating, and science-backed accountability partner.
Keep your replies incredibly concise (1-3 sentences maximum) unless the user specifically asks for a detailed plan.
Never be preachy or judgmental. If they missed a habit, gently help them get back on track.
Do not use markdown symbols or emojis heavily, keep it clean.
"""
}

@app.route("/api/generate", methods=["POST"])
def generate():
    data = request.get_json()
    raw_prompt = data.get("prompt", "")
    prompt_type = data.get("prompt_type", "")

    if not raw_prompt:
        return jsonify({"error": "No prompt provided"}), 400

    # Apply server-side system prompts if requested
    final_prompt = raw_prompt
    if prompt_type == "breakdown":
        final_prompt = f"{PROMPTS['breakdown']}\n\nUser Goal: {raw_prompt}"

    try:
        result = ollama.generate(model=MODEL, prompt=final_prompt)
        return jsonify({"response": result["response"]})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/api/chat", methods=["POST"])
def chat():
    data = request.get_json()
    messages = data.get("messages", [])
    stream = data.get("stream", False)

    if not messages:
        return jsonify({"error": "No messages provided"}), 400

    # Ensure the coach prompt is always the first system message if it isn't already managed by Android well
    # (Android currently passes its own system prompt with dynamic habit data, which is good. 
    # But we can append our server-side strict rules to the first system message).
    if messages and messages[0].get("role") == "system":
        original_system = messages[0].get("content", "")
        messages[0]["content"] = f"{PROMPTS['coach']}\n\n{original_system}"
    else:
        messages.insert(0, {"role": "system", "content": PROMPTS["coach"]})

    try:
        if stream:
            def generate_stream():
                response_stream = ollama.chat(model=MODEL, messages=messages, stream=True)
                for chunk in response_stream:
                    # chunk is a dict containing 'message': {'role': 'assistant', 'content': '...'}
                    message_obj = chunk.get("message", {})
                    content = message_obj.content if hasattr(message_obj, 'content') else message_obj.get("content", "")
                    
                    if content:
                        # Yield NDJSON (Newline Delimited JSON)
                        yield json.dumps({"content": content}) + "\n"
                        
            return Response(stream_with_context(generate_stream()), mimetype="application/x-ndjson")
        else:
            result = ollama.chat(model=MODEL, messages=messages)
            message_obj = result["message"]
            message_dict = {
                "role": message_obj.role if hasattr(message_obj, 'role') else message_obj.get("role"),
                "content": message_obj.content if hasattr(message_obj, 'content') else message_obj.get("content")
            }
            return jsonify({"message": message_dict})
    except Exception as e:
        if stream:
            return Response(json.dumps({"error": str(e)}) + "\n", mimetype="application/x-ndjson", status=500)
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(port=5050)