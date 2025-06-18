package cc.waa.tools.scoretoast;

import static java.lang.System.lineSeparator;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;
import static javax.xml.xpath.XPathConstants.NODESET;
import static org.apache.commons.lang3.StringUtils.joinWith;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class ScoreToastApplication implements CommandLineRunner {

   private static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";

   private static final String HEAD = joinWith(lineSeparator(),
         "<score-partwise",
         "   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"",
         "   xmlns=\"http://www.musicxml.org/xsd/MusicXML\"",
         "   xsi:schemaLocation=\"",
         "      http://www.musicxml.org/xsd/MusicXML",
         "      https://i.kinwaa.cn/schema/musicxml-4.0/musicxml.xsd\"",
         "   version=\"4.0\">");

   private DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

   private XPathFactory pathFactory = XPathFactory.newInstance();

   private TransformerFactory txFactory = TransformerFactory.newInstance();

   private void cleanNodes(XPath xpath, Document doc, String path) throws Exception {
      NodeList list = (NodeList) xpath.evaluate(path, doc, NODESET);

      for (int i = 0; i < list.getLength(); i++) {
         Node node = list.item(i);
         node.getParentNode().removeChild(node);
      }
   }

   private void cleanAttributes(XPath xpath, Document doc, String path, String... attrNames) throws Exception {
      if (attrNames == null || attrNames.length <= 0) {
         return;
      }

      NodeList list = (NodeList) xpath.evaluate(path, doc, NODESET);

      for (int i = 0; i < list.getLength(); i++) {
         Node node = list.item(i);
         NamedNodeMap attrs = node.getAttributes();

         stream(attrNames)
               .filter(d -> attrs.getNamedItem(d) != null)
               .forEach(d -> attrs.removeNamedItem(d));
      }
   }

   public void checkFile(final File file) throws Exception {
      Document doc = this.builderFactory.newDocumentBuilder().parse(file);
      XPath xpath = this.pathFactory.newXPath();

      cleanNodes(xpath, doc, "//score-partwise/identification/encoding");
      // cleanNodes(xpath, doc, "//score-partwise/identification/source"); // 这个保留会好些
      cleanNodes(xpath, doc, "//score-partwise/defaults");
      cleanNodes(xpath, doc, "//score-partwise/credit");
      cleanNodes(xpath, doc, "//score-partwise/part/measure/print");
      // score-partwise/part/measure/note/beam不能删，会影响符尾的连接
      cleanNodes(xpath, doc, "//score-partwise/part/measure/note/stem");
      cleanNodes(xpath, doc, "//score-partwise/part/measure/note/lyric/syllabic");
      cleanAttributes(xpath, doc, "//score-partwise/part/measure", "width");
      cleanAttributes(xpath, doc, "//score-partwise/part/measure/note", "default-x", "default-y");
      cleanAttributes(xpath, doc, "//score-partwise/part/measure/note/dot", "default-x", "default-y");
      cleanAttributes(xpath, doc, "//score-partwise/part/measure/note/lyric", "default-x", "default-y", "relative-y");
      cleanAttributes(xpath, doc, "//score-partwise/part/measure/note/notations/fermata", "default-y", "relative-y");

      StringBuilder content = new StringBuilder();

      try (StringWriter out = new StringWriter()) {
         Transformer transformer = this.txFactory.newTransformer();
         transformer.setOutputProperty(OMIT_XML_DECLARATION, "yes");
         transformer.transform(new DOMSource(doc), new StreamResult(out));

         out.flush();
         content.append(out.toString());
      }

      // 换回标准的语法
      final int start = content.indexOf("<score-partwise");
      final int end = content.indexOf(">", start + 15);
      content.delete(start, end + 1);
      content.insert(start, HEAD);

      if (content.indexOf("<?xml") != 0) { // 如果没有XML头
         content.insert(0, lineSeparator());
         content.insert(0, XML);
      }

      // 让无内容的标签关闭得好看点
      int cur = 0;
      while ((cur = content.indexOf("/>", cur + 2)) > 0) {
         if (content.charAt(cur - 1) != ' ') {
            content.insert(cur, " ");
            cur++;
         }
      }

      try (FileWriter out = new FileWriter(file)) {
         out.write(content.toString());
         out.flush();
      }
   }

   @Override
   public void run(String... args) {
      asList(new File(".").listFiles(d -> d.getName().endsWith(".musicxml"))).parallelStream().forEach(f -> {
         try {
            checkFile(f);

            log.info("成功处理文件[{}]", f.getAbsolutePath());
         } catch (Exception e) {
            log.error("处理文件[{}]出错", f.getAbsolutePath(), e);
         }
      });
   }
}
