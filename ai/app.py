from flask import Flask, request, jsonify, abort
from palm_matcher import PalmMatcher
import base64
import logging

# Configure Logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

app = Flask(__name__)
matcher = PalmMatcher(threshold=0.7)

# Production API Key (Should be in env var)
API_KEY = "smartpay-secret-token-2026"

# Mock Database (In production, this should be fetched from MySQL)
MOCK_DB = [
    {'user_id': 101, 'template': bytes([i % 256 for i in range(256)])},
    {'user_id': 102, 'template': bytes([(i + 50) % 256 for i in range(256)])}
]

def check_auth():
    auth_key = request.headers.get("X-API-KEY")
    if auth_key != API_KEY:
        logger.warning(f"Unauthorized access attempt from {request.remote_addr}")
        abort(401, description="Invalid or missing API Key")

@app.route('/match', methods=['POST'])
def match_palm():
    """
    Endpoint to match a palm template. Secured with API Key.
    """
    check_auth()
    
    data = request.get_json()
    if not data or 'template' not in data:
        logger.error("Request missing 'template' field")
        return jsonify({"error": "No template data provided"}), 400

    try:
        # Decode base64 template
        template_bytes = base64.b64decode(data['template'])
        logger.info(f"Processing match request (size: {len(template_bytes)} bytes)")
        
        # Perform matching
        user_id, score = matcher.find_best_match(template_bytes, MOCK_DB)
        
        if user_id:
            logger.info(f"Match found for User ID: {user_id} (Score: {score})")
            return jsonify({
                "status": "success",
                "user_id": user_id,
                "confidence": score
            })
        else:
            logger.info(f"No match found for input. High score: {score}")
            return jsonify({
                "status": "fail",
                "message": "No match found",
                "confidence": score
            }), 404

    except Exception as e:
        logger.exception("Internal error during matching")
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    logger.info("SmartPay AI Matching Server starting on port 5000...")
    app.run(port=5000)
