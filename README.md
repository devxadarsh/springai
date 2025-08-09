# LLM Response Comparison API

A Spring Boot application that provides a REST API to compare responses from different LLM providers (OpenAI's GPT-4 and Google's Vertex AI/Gemini) for the same prompt.

## Features

- RESTful API endpoint to submit prompts for comparison
- Generates markdown reports of the comparison
- Configurable through environment variables
- Proper error handling and validation
- Clean, layered architecture

## Prerequisites

- Java 17 or higher
- Maven
- OpenAI API key
- Google Cloud Project with Vertex AI API enabled
- Google Cloud credentials with appropriate permissions

## Setup

1. Clone the repository
2. Set the following environment variables:
   ```bash
   # Required
   export OPENAI_API_KEY='your-openai-api-key'
   export GCP_PROJECT_ID='your-gcp-project-id'
   export GCP_LOCATION='your-gcp-location'  # e.g., us-central1
   
   # Optional: If using Application Default Credentials is not possible
   # export GOOGLE_APPLICATION_CREDENTIALS='path/to/your/service-account-key.json'
   ```

## Running the Application

1. Build the application:
   ```bash
   mvn clean package
   ```

2. Run the application:
   ```bash
   java -jar target/llm-comparison-api-0.0.1-SNAPSHOT.jar
   ```

   Or using Maven:
   ```bash
   mvn spring-boot:run
   ```

## API Usage

### Compare LLM Responses

**Endpoint:** `POST /api/llm/compare`

**Request Body:**
```json
{
    "prompt": "Explain quantum computing to a 10-year-old"
}
```

**Response:**
```json
{
    "openAiResponse": "[OpenAI's response]...",
    "vertexResponse": "[Vertex AI's response]...",
    "reportPath": "reports/llm_comparison_20250808_153045.md",
    "timestamp": "2025-08-08T15:30:45.123456"
}
```

## Configuration

Edit `src/main/resources/application.properties` to customize:
- Server port
- Model parameters (temperature, etc.)
- Reports directory
- Logging levels

## Reports

Comparison reports are saved in the `reports` directory by default. Each report includes:
- The original prompt
- Responses from both LLMs
- A section for comparing the responses

## Development

### Project Structure

```
src/main/java/com/example/springai/
├── config/           # Configuration classes
├── controller/       # REST controllers
├── dto/              # Data Transfer Objects
├── exception/        # Exception handling
├── service/          # Business logic
└── SpringaiApplication.java  # Main application class
```

### Building

```bash
mvn clean package
```

### Running Tests

```bash
mvn test
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
# springai
