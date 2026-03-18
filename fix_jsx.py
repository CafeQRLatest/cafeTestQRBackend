import re

file_path = r"c:\Users\sayoo\OneDrive\Desktop\java rest pos\cafe-qr-frontend\pages\owner\product-management.js"

with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

# Replace </div> followed by spaces and another </div> with just a single </div>
# Specifically targeting the corrupt layout from recent edit
fixed_content = re.sub(r'</div>\s*</div>', '</div>', content)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(fixed_content)

print("JSX Syntax Repair Complete")
