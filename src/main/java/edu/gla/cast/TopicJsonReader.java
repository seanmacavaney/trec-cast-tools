package edu.gla.cast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import cast.topics.TopicDef.*;
import com.google.protobuf.util.JsonFormat;

/**
 * Read in a topic text file and create a protocol buffer / json file output.
 *
 */
public class TopicTextToProto {


  /**
   * Parses a topic text file and produces a list of Topic objects.
   * A text file has the format:
   * Number: 1
   * Title: sample topic
   * Description: A sample topic description.
   * 1  This is the first turn
   * 2  This is the second turn.
   * ...
   * A blank line separates topics.
   *
   */
  public List<Topic> parseTopicTextFile(String topicFile) throws Exception {
    List<String> lines = Files.readAllLines(Paths.get(topicFile));
    Topic.Builder topic = Topic.newBuilder();
    List<Topic> topicList = new ArrayList<Topic>();
    for (String line : lines) {
      System.out.println(line);
      String lowercased = line.toLowerCase();
      String[] fields = line.split("\t");
      if (lowercased.startsWith("number:")) {
        String numberString = line.replace("Number:", "").trim();
        int number = Integer.parseInt(numberString);
        topic.setNumber(number);
      } else if (lowercased.startsWith("title:")) {
        String titleString = line.replace("Title:", "").trim();
        topic.setTitle(titleString);
      } else if (lowercased.startsWith("description:")) {
        String descriptionString = line.replace("Description:", "").trim();
        topic.setDescription(descriptionString);
      } else if (lowercased.isEmpty()) {
        topicList.add(topic.build());
        topic = Topic.newBuilder();
      } else if (fields.length == 2) {
        // An individual turn in the topic.
        int turnNumber = Integer.parseInt(fields[0]);
        String utterance = fields[1];
        Turn.Builder turn = Turn.newBuilder();
        turn.setNumber(turnNumber);
        turn.setRawUtterance(utterance);
        topic.addTurn(turn.build());
      } else {
        throw new Exception("Invalid text file format on line: " + line);
      }
    }
    topicList.add(topic.build());
    return topicList;
  }

  public void writeTopicToFile(List<Topic> topics, String outputFile) throws IOException {
    FileWriter writer = new FileWriter(outputFile);
    try {
      for (Topic topic : topics) {
        String jsonString = JsonFormat.printer()
                .preservingProtoFieldNames()
                .print(topic);
        System.out.println("Writing json string: " + jsonString);
        writer.write(jsonString);
      }
    } finally {
      writer.close();
    }

  }

  public static void main(String[] args) throws Exception{
    System.out.println("Loading topics.");
    TopicTextToProto topicTextToProto = new TopicTextToProto();
    List<Topic> topicList = topicTextToProto.parseTopicTextFile(args[0]);
    System.out.println("Number of topics:" + topicList.size());
    topicTextToProto.writeTopicToFile(topicList, args[1]);
  }
}