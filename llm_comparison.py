import os
import google.generativeai as genai
from openai import OpenAI
from pathlib import Path
from datetime import datetime

class LLMComparator:
    def __init__(self, openai_api_key=None, google_api_key=None):
        """Initialize both API clients"""
        # Initialize OpenAI client
        self.openai_client = OpenAI(api_key=openai_api_key or os.getenv('OPENAI_API_KEY'))
        
        # Configure Google's API
        if google_api_key:
            genai.configure(api_key=google_api_key)
        self.vertex_client = genai.GenerativeModel('gemini-pro')
    
    def query_openai(self, prompt, model="gpt-4"):
        """Query OpenAI's API"""
        try:
            response = self.openai_client.chat.completions.create(
                model=model,
                messages=[{"role": "user", "content": prompt}]
            )
            return response.choices[0].message.content
        except Exception as e:
            return f"Error querying OpenAI: {str(e)}"
    
    def query_vertex(self, prompt):
        """Query Google's Vertex AI (Claude)"""
        try:
            response = self.vertex_client.generate_content(prompt)
            return response.text
        except Exception as e:
            return f"Error querying Vertex AI: {str(e)}"
    
    def generate_report(self, prompt, openai_response, vertex_response):
        """Generate a markdown report comparing the responses"""
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        filename = f"llm_comparison_{timestamp}.md"
        
        report = f"# LLM Response Comparison\n\n"
        report += f"**Prompt:** {prompt}\n\n"
        
        report += "## OpenAI GPT-4 Response\n\n"
        report += f"{openai_response}\n\n"
        
        report += "## Vertex AI (Claude) Response\n\n"
        report += f"{vertex_response}\n\n"
        
        report += "## Comparison\n\n"
        report += "### Key Differences\n"
        report += "- **Length:** Compare the length of responses\n"
        report += "- **Tone:** Note any differences in tone or approach\n"
        report += "- **Technical Depth:** Compare the level of technical detail\n"
        report += "- **Examples:** Note any examples or analogies used\n\n"
        
        with open(filename, 'w', encoding='utf-8') as f:
            f.write(report)
        
        return filename

def main():
    # Get API keys from environment variables or user input
    openai_api_key = os.getenv('OPENAI_API_KEY')
    google_api_key = os.getenv('GOOGLE_API_KEY')
    
    if not openai_api_key:
        openai_api_key = input("Enter your OpenAI API key: ")
    if not google_api_key:
        google_api_key = input("Enter your Google API key: ")
    
    # Initialize the comparator
    comparator = LLMComparator(openai_api_key, google_api_key)
    
    # Define the prompt
    prompt = "Explain quantum computing to a 10-year-old"
    
    print("Querying OpenAI GPT-4...")
    openai_response = comparator.query_openai(prompt)
    
    print("Querying Vertex AI (Claude)...")
    vertex_response = comparator.query_vertex(prompt)
    
    # Generate and save the report
    report_file = comparator.generate_report(prompt, openai_response, vertex_response)
    
    print("\n" + "="*50)
    print(f"Comparison complete! Report saved as: {report_file}")
    print("="*50)

if __name__ == "__main__":
    main()
