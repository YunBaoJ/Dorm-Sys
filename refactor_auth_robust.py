import os
import re

directory = 'Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/'
utils_import = 'import com.dorm.backend.common.AuthUtils;'

duplicate_code_regex = re.compile(
    r'\s*private boolean isStudent\(\) \{.*?\n?'
    r'\s*private Long currentUserId\(\) \{.*?\n?'
    r'\s*private String currentUserRole\(\) \{.*?\n?'
    r'\s*private Object currentRequestAttribute\(String name\) \{.*?\}\n?',
    re.DOTALL
)

# Also a version without isStudent
duplicate_code_regex_2 = re.compile(
    r'\s*private Long currentUserId\(\) \{.*?\n?'
    r'\s*private String currentUserRole\(\) \{.*?\n?'
    r'\s*private Object currentRequestAttribute\(String name\) \{.*?\}\n?',
    re.DOTALL
)

for filename in os.listdir(directory):
    if not filename.endswith('.java'):
        continue
    filepath = os.path.join(directory, filename)
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    original = content
    # Remove the block
    content = duplicate_code_regex.sub('', content)
    content = duplicate_code_regex_2.sub('', content)
    
    # Replace method calls
    content = content.replace('isStudent()', 'AuthUtils.isStudent()')
    content = content.replace('currentUserId()', 'AuthUtils.getCurrentUserId()')
    content = content.replace('currentUserRole()', 'AuthUtils.getCurrentUserRole()')
    
    if content != original:
        # Add import if missing
        if utils_import not in content:
            imports = list(re.finditer(r'^import .*;', content, re.MULTILINE))
            if imports:
                last_import = imports[-1]
                content = content[:last_import.end()] + '\n' + utils_import + content[last_import.end():]
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Refactored {filename}")

