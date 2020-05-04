package uk.gov.ons.ctp.lib.common.utility;

import java.io.StringWriter;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.springframework.core.io.ResourceLoader;

public class Mapzer {

  private ResourceLoader resourceLoader;

  public Mapzer(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  /**
   * Convert an object into its XML equivalent based on the provided schema
   *
   * @param context JAXBContext
   * @param o Object to convert to XML
   * @param cpSchemaLocation Location of *.xsd as a classpath location (don't prepend location with
   *     classpath)
   * @return xml of the object
   * @throws Exception
   */
  public String convertObjectToXml(JAXBContext context, Object o, String cpSchemaLocation)
      throws Exception {
    SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    URL xsd = resourceLoader.getResource(String.format("classpath:%s", cpSchemaLocation)).getURL();
    Schema schema = sf.newSchema(xsd);
    Marshaller mars = context.createMarshaller();
    StringWriter buffer = new StringWriter();

    mars.setSchema(schema);
    mars.marshal(o, buffer);

    return buffer.toString();
  }
}
