package com.mule.einstein.internal.helpers.chatmemory;

import com.mule.einstein.internal.connection.EinsteinConnection;
import com.mule.einstein.internal.helpers.PayloadHelper;
import com.mule.einstein.internal.models.ParamsModelDetails;

import java.util.List;

public class ChatMemoryHelper {

  private static ChatMemory intializeChatMemory(String memoryPath, String memoryName) {
    return new ChatMemory(memoryPath, memoryName);
  }

  private static List<String> getKeepLastMessage(ChatMemory chatMemory, Integer keepLastMessages) {

    // Retrieve all messages in ascending order of messageId
    List<String> messagesAsc = chatMemory.getAllMessagesByMessageIdAsc();

    // Keep only the last index messages
    if (messagesAsc.size() > keepLastMessages) {
      messagesAsc = messagesAsc.subList(messagesAsc.size() - keepLastMessages, messagesAsc.size());
    }
    return messagesAsc;
  }

  private static void addMessageToMemory(ChatMemory chatMemory, String prompt) {
    if (!isQuestion(prompt)) {
      chatMemory.addMessage(chatMemory.getMessageCount() + 1L, prompt);
    }
  }

  private static boolean isQuestion(String message) {
    // Check if the message ends with a question mark
    if (message.trim().endsWith("?")) {
      return true;
    }
    // Check if the message starts with a question word (case insensitive)
    String[] questionWords = {"who", "what", "when", "where", "why", "how", "tell", "tell me", "do you",};
    String lowerCaseMessage = message.trim().toLowerCase();
    for (String questionWord : questionWords) {
      if (lowerCaseMessage.startsWith(questionWord + " ")) {
        return true;
      }
    }
    return false;
  }

  private static String formatMemoryPrompt(List<String> messages) {
    StringBuilder formattedPrompt = new StringBuilder();
    for (String message : messages) {
      formattedPrompt.append(message).append("\n");
    }
    return formattedPrompt.toString().trim();
  }


  public static String chatWithMemory(String prompt, String memoryPath, String memoryName, Integer keepLastMessages,
                                      EinsteinConnection connection, ParamsModelDetails parameters) {

    //Chat memory initialization
    ChatMemory chatMemory = intializeChatMemory(memoryPath, memoryName);

    //Get keepLastMessages
    List<String> keepLastMessagesList = getKeepLastMessage(chatMemory, keepLastMessages);
    keepLastMessagesList.add(prompt);
    String memoryPrompt = formatMemoryPrompt(keepLastMessagesList);

    String response = PayloadHelper.executeGenerateText(memoryPrompt, connection, parameters);

    addMessageToMemory(chatMemory, prompt);

    return response;
  }


}
