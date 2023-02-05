import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Main {

  public static void main(String[] args)
      throws ParserConfigurationException, IOException, SAXException {

    String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
    String fileName = "data.csv";
    List<Employee> list = parseCSV(columnMapping, fileName);
    String json = listToJson(list);
    writeString(json, "result.json");

    List<Employee> list1 = parseXML("data.xml");
    String jsonXml = listToJson(list1);
    writeString(jsonXml, "resultXml.json");
  }

  public static List<Employee> parseCSV(String[] columnMapping, String filename) {
    ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>(); strategy.setType(Employee. class);
    strategy.setColumnMapping(columnMapping);
    List<Employee> list = null;
    try (CSVReader reader = new CSVReader(new FileReader(filename))) {
      CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader) .withMappingStrategy(strategy)
          .build();
      list = csv.parse();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return list;
  }

  public static String listToJson(List<Employee> list) {
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();
    Type listType = new TypeToken<List<Employee>>() {}.getType();
    String json = gson.toJson(list, listType);
    System.out.println(json);
    return json;
  }

  public static void writeString(String json, String fileName) {
    try (FileWriter writer = new FileWriter(fileName, false)) {
      writer.write(json);
      writer.flush();
    } catch (IOException ex) { System.out.println(ex.getMessage());
    }
  }

  public static List<Employee> parseXML(String filename)
      throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(new File(filename));
    Node root = document.getDocumentElement();
    List<Employee> employeeList = new ArrayList<>();
    return read(root, employeeList);
  }

  public static List<Employee> read(Node node, List<Employee> employeeList){
    NodeList nodeList = node.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node nodeChild = nodeList.item(i);
      if (Node.ELEMENT_NODE == nodeChild.getNodeType()) {
        Element element = (Element) nodeChild;
        if (element.getTagName().equals("employee")) {
          Employee employee = new Employee();
          employeeList.add(employee);
          read(nodeChild, employeeList);
        } else {
          int size = employeeList.size();
          Employee newEmployee = employeeList.get(size - 1);
          employeeList.remove(size - 1);
          switch (element.getTagName()) {
            case "id" : {
              newEmployee.id = Long.parseLong(element.getTextContent());
              break;
            }
            case "firstName" : {
              newEmployee.firstName = element.getTextContent();
              break;
            }
            case "lastName" : {
              newEmployee.lastName = element.getTextContent();
              break;
            }
            case "country" : {
              newEmployee.country = element.getTextContent();
              break;
            }
            case "age" : {
              newEmployee.age = Integer.parseInt(element.getTextContent());
              break;
            }
          }
          employeeList.add(newEmployee);
        }

      }
    }
    return employeeList;
  }
}
