import os
import re

directory = 'Dorm-Sys/backend/src/main/java/com/dorm/backend/controller/'
utils_import = 'import com.dorm.backend.common.AuthUtils;'

duplicate_code_regex = re.compile(
    r'\s*private boolean isStudent\(\) \{ return "student"\.equals\(currentUserRole\(\)\); \}'
    r'\s*private Long currentUserId\(\) \{(.*?)\}'
    r'\s*private String currentUserRole\(\) \{(.*?)\}'
    r'\s*private Object currentRequestAttribute\(String name\) \{(.*?)\}',
    re.DOTALL
)

for filename in os.listdir(directory):
    if not filename.endswith('.java'):
        continue
    filepath = os.path.join(directory, filename)
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # If it has the duplicate block
    if duplicate_code_regex.search(content):
        # Remove the block
        content = duplicate_code_regex.sub('', content)
        
        # Replace method calls
        content = content.replace('isStudent()', 'AuthUtils.isStudent()')
        content = content.replace('currentUserId()', 'AuthUtils.getCurrentUserId()')
        content = content.replace('currentUserRole()', 'AuthUtils.getCurrentUserRole()')
        
        # Add import if missing
        if utils_import not in content:
            # Find the last import and add it after
            imports = list(re.finditer(r'^import .*;', content, re.MULTILINE))
            if imports:
                last_import = imports[-1]
                content = content[:last_import.end()] + '\n' + utils_import + content[last_import.end():]
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Refactored {filename}")

