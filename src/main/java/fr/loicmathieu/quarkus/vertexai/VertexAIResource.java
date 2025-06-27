package fr.loicmathieu.quarkus.vertexai;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import java.io.IOException;

// 1. Standard REST endpoint available on /chat
@Path("/chat")
public class VertexAIResource {
    // 2. System message tellin the LLM it is a chatbot
    private static final String SYSTEM_INSTRUCTION = """
        You are a chatbot named Loïc. Don't pretend you know everything but try to be helpful with a touch of humour.""";

    private GenerativeModel model;
    private ChatSession chatSession;

    // 3. Injection of the provided VertexAI object
    @Inject
    VertexAI vertexAI;

    // 4. Init the model with Gemini 2.5 Flash
    @PostConstruct
    void initModel() {
        this.model =  new GenerativeModel("gemini-2.5-flash", vertexAI)
            .withSystemInstruction(ContentMaker.fromString(SYSTEM_INSTRUCTION));

        // For simplicity: we use a single chat session, on real use cases, we should use one per user
        this.chatSession = this.model.startChat();
    }

    // 5. Generate a chat response on each call to /chat
    @GET
    public String chat(@QueryParam("message") String message) throws IOException {
        var response = chatSession.sendMessage(message);

        // For simplicity: we only take the first part of the first candidate
        return response.getCandidatesList().getFirst().getContent().getPartsList().getFirst().getText() + "\n";
    }
}
