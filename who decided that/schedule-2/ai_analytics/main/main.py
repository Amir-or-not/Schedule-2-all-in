from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import os
import google.generativeai as genai

# GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
GEMINI_API_KEY = ("Ваш токенский")


app = FastAPI(title="My FastAPI App")


class Item(BaseModel):
    id: int
    name: str

if GEMINI_API_KEY:
    genai.configure(api_key=GEMINI_API_KEY)
    model = genai.GenerativeModel(model_name="gemini-2.5-flash")
else:
    model = None

class PromptDTO(BaseModel):
    prompt: str

@app.get("/")
async def root():
    return {"message": "Hello, FastAPI!"}


@app.post("/items/")
async def create_item(item: Item):
    return {"received": item}


@app.post("/analytics/")
async def analytics(dto: PromptDTO):
    if not model:
        return {"text": f"Received prompt (no model configured): {dto.prompt}"}
    try:
        response = model.generate_content(dto.prompt)
        text = getattr(response, "text", None) or getattr(response, "output", None) or str(response)
        return {"text": text}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

