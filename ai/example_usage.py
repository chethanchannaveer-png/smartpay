from palm_matcher import PalmMatcher

def run_demo():
    # 1. Initialize Matcher
    matcher = PalmMatcher(threshold=0.7)

    # 2. Mock Database of Templates
    # Usually these would be fetched from MySQL 'palm_templates' table
    database = [
        {'user_id': 101, 'template': bytes([i % 256 for i in range(256)])},
        {'user_id': 102, 'template': bytes([(i + 50) % 256 for i in range(256)])},
        {'user_id': 103, 'template': bytes([0] * 256)}
    ]

    # 3. Target Input (Simulated scan)
    # Let's simulate a scan that is almost identical to User 101
    scanned_palm = bytes([i % 256 if i > 5 else 0 for i in range(256)])

    print("--- Palm Matching AI System ---")
    print(f"Input scanned palm data (256 bytes captured)")
    
    # 4. Perform Matching
    user_id, score = matcher.find_best_match(scanned_palm, database)

    if user_id:
        print(f"MATCH FOUND!")
        print(f"User ID: {user_id}")
        print(f"Confidence Score: {score * 100:.2f}%")
    else:
        print("NO MATCH FOUND.")
        print(f"Highest Confidence: {score * 100:.2f}% (Below threshold)")

if __name__ == "__main__":
    run_demo()
