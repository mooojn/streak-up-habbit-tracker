from flask import Flask, request, jsonify
import ollama  # pip install ollama

app = Flask(__name__)

MODEL = "qwen2.5-coder:7b"  # or "llama3.2", "mistral", etc. — whatever they have pulled

@app.route("/api/generate", methods=["POST"])
def generate():
    data = request.get_json()
    prompt = data.get("prompt", "")

    if not prompt:
        return jsonify({"error": "No prompt provided"}), 400

    try:
        result = ollama.generate(model=MODEL, prompt=prompt)
        return jsonify({"response": result["response"]})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/api/chat", methods=["POST"])
def chat():
    data = request.get_json()
    messages = data.get("messages", [])

    if not messages:
        return jsonify({"error": "No messages provided"}), 400

    try:
        result = ollama.chat(model=MODEL, messages=messages)
        message_obj = result["message"]
        message_dict = {
            "role": message_obj.role if hasattr(message_obj, 'role') else message_obj.get("role"),
            "content": message_obj.content if hasattr(message_obj, 'content') else message_obj.get("content")
        }
        return jsonify({"message": message_dict})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(port=5050)