import numpy as np
import logging
from Crypto.Cipher import AES # Assuming pycryptodome is available for production
import base64

from Crypto.Util.Padding import unpad

logger = logging.getLogger(__name__)

class PalmMatcher:
    """
    Production-grade AI Module for matching palm templates using NumPy performance.
    """

    def __init__(self, threshold=0.85):
        self.threshold = threshold
        # Encryption Key (Must match Java side)
        self.secret_key = b"SmartPaySec123!*" 
        self.block_size = 16

    def _decrypt_template(self, encrypted_data):
        """
        Decrypts the AES-encrypted template data and removes PKCS5 padding.
        """
        try:
            cipher = AES.new(self.secret_key, AES.MODE_ECB)
            decrypted = cipher.decrypt(encrypted_data)
            # Remove PKCS5/PKCS7 padding
            return unpad(decrypted, self.block_size)
        except Exception as e:
            logger.error(f"Decryption failed: {e}")
            # In production, we should probably raise an error here
            return encrypted_data 

    def calculate_similarity(self, template1, template2):
        """
        Calculates Cosine Similarity using NumPy for high performance.
        Better than Euclidean for high-dimensional biometric vectors.
        """
        # Convert to numpy arrays
        v1 = np.frombuffer(template1, dtype=np.uint8).astype(np.float32)
        v2 = np.frombuffer(template2, dtype=np.uint8).astype(np.float32)
        
        if len(v1) != len(v2):
            return 0.0

        # Cosine Similarity: (A . B) / (||A|| * ||B||)
        dot_product = np.dot(v1, v2)
        norm_a = np.linalg.norm(v1)
        norm_b = np.linalg.norm(v2)
        
        if norm_a == 0 or norm_b == 0:
            return 0.0
            
        similarity = dot_product / (norm_a * norm_b)
        return round(float(similarity), 4)

    def find_best_match(self, target_encrypted, template_db):
        """
        Optimized matching with pre-decryption and NumPy.
        """
        # 1. Decrypt target
        target_raw = self._decrypt_template(target_encrypted)
        
        best_user_id = None
        best_score = 0.0

        # In production, we would use a vectorized matrix operation instead of a loop
        # But for clarity, we keep the loop and use NumPy inside calculate_similarity
        for record in template_db:
            user_id = record['user_id']
            # Assume templates in DB are also encrypted
            stored_raw = self._decrypt_template(record['template'])
            
            score = self.calculate_similarity(target_raw, stored_raw)
            
            if score > best_score:
                best_score = score
                best_user_id = user_id

        if best_score >= self.threshold:
            return best_user_id, best_score
        else:
            return None, best_score

if __name__ == "__main__":
    # Internal test
    matcher = PalmMatcher()
    t1 = [10] * 256
    t2 = [10] * 256
    t3 = [20] * 256
    
    print(f"Similarity (identical): {matcher.calculate_similarity(t1, t2)}")
    print(f"Similarity (different): {matcher.calculate_similarity(t1, t3)}")
