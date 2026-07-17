import os
import re

directories = ['Dorm-Sys/frontend/src/views/manager', 'Dorm-Sys/frontend/src/views/student', 'Dorm-Sys/frontend/src/views/admin']

for directory in directories:
    for filename in os.listdir(directory):
        if not filename.endswith('.vue'):
            continue
        filepath = os.path.join(directory, filename)
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()

        # Regex to find catch blocks that have ElMessage.error but don't log the error
        # Match catch (e) { or catch(err) {
        # We will just replace catch (e) { with catch (e) { console.error(e);
        # Let's do a simple regex substitution.
        
        # Match catch\s*\((.*?)\)\s*\{
        # and ensure console.error is not already there
        def replace_catch(match):
            err_var = match.group(1)
            # If the next few characters already have console.error, don't add it
            return f"catch ({err_var}) {{ console.error({err_var});"

        original = content
        # We use a lazy match to find the catch block. 
        # Wait, simple string replacement might be safer if we know the pattern.
        content = re.sub(r'catch\s*\((.*?)\)\s*\{(?!\s*console\.error)', replace_catch, content)

        if content != original:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Added console.error to {filename}")
