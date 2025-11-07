# AI Analytics Service

This service provides AI-powered analytics for news articles using Google's Gemini AI.

## Setup

1. Install Python 3.8+
2. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
3. Create a `.env` file and add your Gemini API key:
   ```
   GEMINI_API_KEY=your_api_key_here
   ```

## Running the Service

```bash
uvicorn main:app --reload
```

The service will be available at `http://localhost:8000`

## API Documentation

### 1. Analyze News Articles

**Endpoint:** `POST /api/ai/analyze-news`

Analyze a collection of news articles using Gemini AI.

**Request Body:**
```json
{
  "news": [
    {
      "id": 1,
      "title": "News Title 1",
      "content": "News content goes here...",
      "publication_date": "2023-11-01T10:00:00Z"
    },
    {
      "id": 2,
      "title": "News Title 2",
      "content": "Another news content...",
      "publication_date": "2023-11-02T11:30:00Z"
    }
  ],
  "analysis_type": "summary"
}
```

**Parameters:**
- `news`: Array of news items to analyze
  - `id`: Unique identifier for the news item
  - `title`: News article title
  - `content`: Full text content of the article
  - `publication_date`: ISO 8601 formatted date string
- `analysis_type`: Type of analysis to perform (one of: `summary`, `sentiment`, `trends`, `topics`)

**Response:**
```json
{
  "analysis": "Detailed analysis text...",
  "insights": [
    "Key insight 1",
    "Key insight 2",
    "Key insight 3"
  ],
  "generated_at": "2023-11-03T15:45:30.123456"
}
```

### 2. Health Check

**Endpoint:** `GET /health`

Check if the service is running.

**Response:**
```json
{
  "status": "healthy",
  "service": "ai-analytics"
}
```

## Analysis Types

1. **Summary**
   - Provides a concise summary of the news articles
   - Focuses on main topics and key events

2. **Sentiment**
   - Analyzes the sentiment of each article
   - Categorizes as positive, negative, or neutral
   - Provides reasoning for the sentiment analysis

3. **Trends**
   - Identifies patterns and trends across articles
   - Highlights common themes and significant events
   - Shows how topics evolve over time

4. **Topics**
   - Extracts and categorizes main topics
   - Groups related articles by topic
   - Identifies most prominent topics

## Error Handling

- `400 Bad Request`: Invalid request format or missing required fields
- `500 Internal Server Error`: Error processing the request

## Authentication

This service currently doesn't implement authentication. In production, you should:
1. Add API key authentication
2. Implement rate limiting
3. Set up proper CORS policies

## Deployment

For production deployment, consider using:
- Gunicorn with Uvicorn workers
- Environment variables for configuration
- Reverse proxy (Nginx, Apache)
- Containerization (Docker)

## Environment Variables

- `GEMINI_API_KEY`: Your Google Gemini API key
- `PORT`: Port to run the server on (default: 8000)
- `ENVIRONMENT`: Environment type (development, production)
