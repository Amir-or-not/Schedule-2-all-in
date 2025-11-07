# cd ai_analytics
# uvicorn main:app --reload


from fastapi import FastAPI, HTTPException, Depends, Header
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
import google.generativeai as genai
import os
from dotenv import load_dotenv

load_dotenv()

app = FastAPI(title="AI Analytics API",
             description="API for generating analytics using Gemini AI",
             version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Configure Gemini
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
if not GEMINI_API_KEY:
    raise ValueError("GEMINI_API_KEY environment variable not set")

genai.configure(api_key=GEMINI_API_KEY)
model = genai.GenerativeModel('gemini-pro')

# Models
class NewsItem(BaseModel):
    id: int
    title: str
    content: str
    publication_date: str

class AnalyticsRequest(BaseModel):
    news: List[NewsItem]
    analysis_type: str = "summary" 

class AnalyticsResponse(BaseModel):
    analysis: str
    insights: List[str]
    generated_at: str

def generate_analysis(news_items: List[NewsItem], analysis_type: str) -> dict:
    try:
        if not news_items:
            return {"analysis": "No news items provided", "insights": []}

        if analysis_type == "summary":
            prompt = f"""
            Analyze the following news articles and provide a concise summary.
            Focus on the main topics, key events, and important details.
            
            News Articles:
            {"\n".join([f"Title: {item.title}\nContent: {item.content[:500]}..." for item in news_items])}
            
            Summary:
            """
        elif analysis_type == "sentiment":
            prompt = f"""
            Analyze the sentiment of the following news articles.
            For each article, provide sentiment (positive, negative, neutral) and brief reasoning.
            
            News Articles:
            {"\n".join([f"Title: {item.title}\nContent: {item.content[:500]}..." for item in news_items])}
            
            Sentiment Analysis:
            """
        elif analysis_type == "trends":
            prompt = f"""
            Identify key trends and patterns across these news articles.
            Look for common themes, recurring topics, and significant events.
            
            News Articles:
            {"\n".join([f"Title: {item.title}\nContent: {item.content[:500]}..." for item in news_items])}
            
            Identified Trends:
            """
        elif analysis_type == "topics":
            prompt = f"""
            Extract and categorize the main topics from these news articles.
            Group related articles and identify the most prominent topics.
            
            News Articles:
            {"\n".join([f"Title: {item.title}\nContent: {item.content[:500]}..." for item in news_items])}
            
            Topic Analysis:
            """
        else:
            return {"analysis": "Invalid analysis type", "insights": []}

        # Generate the response
        response = model.generate_content(prompt)
        
        # Format the response
        analysis = response.text
        insights = [line.strip() for line in analysis.split('\n') if line.strip()]
        
        return {
            "analysis": analysis,
            "insights": insights[:5]
        }
        
    except Exception as e:
        return {"analysis": f"Error generating analysis: {str(e)}", "insights": []}

# Endpoints
@app.post("/api/ai/analyze-news", response_model=AnalyticsResponse)
async def analyze_news(request: AnalyticsRequest):
    """
    Analyze news articles using Gemini AI.
    
    - **news**: List of news items to analyze
    - **analysis_type**: Type of analysis to perform (summary, sentiment, trends, topics)
    """
    result = generate_analysis(request.news, request.analysis_type)
    return {
        "analysis": result["analysis"],
        "insights": result["insights"],
        "generated_at": datetime.utcnow().isoformat()
    }

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {"status": "healthy", "service": "ai-analytics"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
