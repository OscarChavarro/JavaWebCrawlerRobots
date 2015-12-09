//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - May 4 2006 - Gina Chiquillo: Original base version                    =
//= - May 5 2006 - Oscar Chavarro: quality check                            =
//===========================================================================

package vsdk.toolkit.io.geometry;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.TransformerFactoryConfigurationError;

import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.environment.geometry.ParametricBiCubicPatch;
import vsdk.toolkit.io.XmlException;
import vsdk.toolkit.io.PersistenceElement;

public class ParametricBiCubicPatchPersistence extends PersistenceElement {
  public static String rootName = "ParametricBiCubicPatch";
  private static String patchAttributesNames[] = {
      "type", "approximationSteps"};

  public static ParametricBiCubicPatch nodeToParametricBiCubicPatch(Node
      nodeRoot) throws
      XmlException {

    if ( !nodeRoot.getNodeName().equals(rootName) ) {
      throw new XmlException("The node no is a patch ");
    }
    ParametricBiCubicPatch patch = null;
    NamedNodeMap atts = nodeRoot.getAttributes();
    if (atts != null) {
      Node atributo;
      atributo = atts.getNamedItem(patchAttributesNames[0]);
      if (atributo != null) {
        String type = atributo.getNodeValue();
        if (!"".equals(type)) {
          patch = new ParametricBiCubicPatch();
          patch.setType(Integer.parseInt(type));
          atributo = atts.getNamedItem(patchAttributesNames[1]);
          if (atributo != null) {
            String approximationSteps = atributo.getNodeValue();
            if (!"".equals(approximationSteps)) {
              patch.setApproximationSteps(Integer.parseInt(approximationSteps));
            }
          }

          NodeList nodeList = nodeRoot.getChildNodes();
          // patch.contourCurve = the first curve node
          for (int i = 0; i < nodeList.getLength(); i++) {
            Node nodeCurve = nodeList.item(i);
            patch.contourCurve = ParametricCurvePersistence.
                nodeToParametricCurve(nodeCurve);
            if(patch.contourCurve!=null){
              break;
            }
          }
        }
      }
    }

    return patch;
    // Se usan los errores de SAX
  }

  public static Element toElement(ParametricBiCubicPatch patch,
                                  Document document) throws XmlException {
    Element nodeRoot = document.createElement(rootName);
    try {

      nodeRoot.setAttribute(patchAttributesNames[1],
                            "" + patch.getApproximationSteps());
      nodeRoot.setAttribute(patchAttributesNames[0],
                            "" + patch.getType());

      Element eCurve;

      eCurve = ParametricCurvePersistence.toElement(patch.
          contourCurve, document);

      nodeRoot.appendChild(eCurve);

    }
    catch (TransformerFactoryConfigurationError ex1) {
      VSDK.reportMessage(null, VSDK.FATAL_ERROR, "toElement", ""  + ex1);
    }
    return nodeRoot;

  }
}
//===========================================================================
//= EOF                                                                     =
//===========================================================================
