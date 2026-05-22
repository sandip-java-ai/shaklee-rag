# Shaklee RAG - Product Catalog AI Assistant

A Retrieval-Augmented Generation (RAG) application that answers 
questions about Shaklee products using real product catalog data.
Built with Spring AI, OpenAI embeddings, and SimpleVectorStore.

## Related Projects
- Agent microservice: https://github.com/sandip-java-ai/weather-agent
- Java client: https://github.com/sandip-java-ai/weather-agent-client
- Learning journey: https://github.com/sandip-java-ai/claude-api-learning

## How RAG Works

    User Question
        ↓
    Convert to embedding vector (OpenAI)
        ↓
    Search vector store for similar products
        ↓
    Retrieve top 5 most relevant products
        ↓
    Send product context + question to LLM
        ↓
    LLM answers from YOUR data only

## Tech Stack
- Java 17
- Spring Boot 3.5.x
- Spring AI 1.0.0
- OpenAI (embeddings + chat generation)
- SimpleVectorStore (in-memory + file persistence)
- Jackson XML (product feed parsing)
- OkHttp (HTTP client)

## Data Source
Public Shaklee product feed (172 base products, 361 variants):
https://storage.googleapis.com/fb-product-data-feed-prod/sf-rss-us-products-list.xml

## Setup

### Prerequisites
- Java 17
- Maven
- OpenAI API key

### Configuration
Set environment variable:

    OPENAI_API_KEY=your-openai-key

### Run

    mvn spring-boot:run

App runs on port 8085.

## Endpoints

### Ingest products (run once)

    POST http://localhost:8085/admin/ingest

Fetches product feed, generates embeddings, saves to
shaklee-vectors.json. Re-run only when catalog changes.

Response:

    {
      "success": true,
      "productsIngested": 361,
      "message": "361 products embedded and stored"
    }

### Query the catalog

    POST http://localhost:8085/rag/query
    Content-Type: application/json

    {
      "question": "What protein products do you have?"
    }

Response:

    {
      "success": true,
      "question": "What protein products do you have?",
      "answer": "We have the following protein products..."
    }

## Example Questions

    "What products are good for immune support?"
    "Tell me about weight management products"
    "What is the member price for whey protein?"
    "Compare your protein powders"
    "What products have the highest rating?"

## Key Concepts Demonstrated

    RAG pipeline          - retrieval augmented generation
    Vector embeddings     - semantic search over product catalog
    SimpleVectorStore     - file-backed vector persistence
    XML feed parsing      - real product data ingestion
    LLM grounding         - answers constrained to your data only

## Notes
- shaklee-vectors.json is gitignored — run /admin/ingest on first startup
- Embeddings cost pennies for 361 products via OpenAI API
- RAG answers are based ONLY on product catalog data
- Price/stock queries may need database integration for accuracy

## Author
Sandip Bhattacharjee
https://github.com/sandip-java-ai