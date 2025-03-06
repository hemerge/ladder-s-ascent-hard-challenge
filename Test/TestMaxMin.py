import os
import random

NUM_FILES = 15000
NUM_NUMBERS = 15000
OUTPUT_DIR = "challenge-1"
MIN_LONG = -2**63
MAX_LONG = 2**63 - 1

# Create output directory
os.makedirs(OUTPUT_DIR, exist_ok=True)

# Generate files
for i in range(NUM_FILES):
    file_path = os.path.join(OUTPUT_DIR, f"file_{i}.txt")
    with open(file_path, "w") as f:
        for _ in range(NUM_NUMBERS):
            f.write(f"{random.randint(MIN_LONG, MAX_LONG)}\n")

print(f"Generated {NUM_FILES} files in '{OUTPUT_DIR}' directory.")
