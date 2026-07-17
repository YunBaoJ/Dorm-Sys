import zipfile
import xml.etree.ElementTree as ET
import sys

def extract_text_from_docx(docx_path, out_path):
    try:
        with zipfile.ZipFile(docx_path) as docx:
            xml_content = docx.read('word/document.xml')
            tree = ET.XML(xml_content)
            
            # The namespace for WordProcessingML
            WORD_NAMESPACE = '{http://schemas.openxmlformats.org/wordprocessingml/2006/main}'
            PARA = WORD_NAMESPACE + 'p'
            TEXT = WORD_NAMESPACE + 't'
            
            text_content = []
            for paragraph in tree.iter(PARA):
                texts = [node.text for node in paragraph.iter(TEXT) if node.text]
                if texts:
                    text_content.append(''.join(texts))
            
            with open(out_path, 'w', encoding='utf-8') as f:
                f.write('\n'.join(text_content))
    except Exception as e:
        with open(out_path, 'w', encoding='utf-8') as f:
            f.write(str(e))

if __name__ == '__main__':
    extract_text_from_docx(sys.argv[1], sys.argv[2])
