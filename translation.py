from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.responses import StreamingResponse
from google.cloud import translate
import uvicorn
import tempfile
import os
import mimetypes


## doesnt work because of google api :(
    
    
    
app = FastAPI()

@app.post("/translate-pdf/")
async def translate_pdf(
    file: UploadFile = File(...),
    source_language: str = "en",
    target_language: str = "pt"
):
    try:
        if file.content_type != 'application/pdf' and not file.filename.endswith('.pdf'):
            raise HTTPException(status_code=400, detail="O arquivo enviado não é um PDF.")

        # Salvar o arquivo 
        with tempfile.NamedTemporaryFile(delete=False, suffix=".pdf") as temp_pdf_file:
            temp_pdf_file.write(await file.read())
            temp_pdf_file_path = temp_pdf_file.name

        client = translate.TranslationServiceClient()


        project_id = "seu-id-do-projeto"
        location = "global"
        parent = f"projects/{project_id}/locations/{location}"

        # Ler o conteúdo do arquivo PDF
        with open(temp_pdf_file_path, 'rb') as document:
            content = document.read()

        document_input_config = {
            "content": content,
            "mime_type": "application/pdf",
        }

        # Fazer a solicitação de tradução
        response = client.translate_document(
            request={
                "parent": parent,
                "source_language_code": source_language,
                "target_language_code": target_language,
                "document_input_config": document_input_config,
            }
        )

        translated_document = response.document_translation.byte_stream_outputs[0]

        mime_type, _ = mimetypes.guess_type(file.filename)

        # Retornar o arquivo PDF traduzido como uma resposta 
        return StreamingResponse(
            content=iter([translated_document]),
            media_type=mime_type,
            headers={"Content-Disposition": f"attachment; filename=translated_{file.filename}"}
        )

    except Exception as e:
        if 'temp_pdf_file_path' in locals() and os.path.exists(temp_pdf_file_path):
            os.remove(temp_pdf_file_path)
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        if 'temp_pdf_file_path' in locals() and os.path.exists(temp_pdf_file_path):
            os.remove(temp_pdf_file_path)

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=9000)


