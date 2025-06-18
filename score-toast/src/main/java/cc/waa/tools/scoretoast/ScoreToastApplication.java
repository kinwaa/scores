package cc.waa.tools.scoretoast;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;
import static javax.xml.xpath.XPathConstants.NODESET;

import java.io.File;

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
      cleanNodes(xpath, doc, "//score-partwise/identification/source");
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

      Transformer transformer = this.txFactory.newTransformer();
      transformer.setOutputProperty(OMIT_XML_DECLARATION, "yes");
      transformer.transform(new DOMSource(doc), new StreamResult(file));
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
