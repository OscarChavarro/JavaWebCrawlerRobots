//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - May 4 2006 - Gina Chiquillo: Original base version                    =
//= - May 5 2006 - Oscar Chavarro: quality check                            =
//===========================================================================

package vsdk.toolkit.io.geometry;

import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import vsdk.toolkit.common.VSDK;
import vsdk.toolkit.common.linealAlgebra.Vector3D;
import vsdk.toolkit.environment.geometry.ParametricCurve;
import vsdk.toolkit.io.XmlException;
import vsdk.toolkit.io.PersistenceElement;

public class ParametricCurvePersistence extends PersistenceElement {
    public static final String rootName = "ParametricCurve";
    private static final String[] nodesNames = {
        "Point",
        "Vector3D",
    };
    private static final String curveAttributesNames[] = {
        "approximationSteps"};
    private static final String pointAttributesNames[] = {
        "type"};

    public static ParametricCurve nodeToParametricCurve(Node nodeRoot) throws
        XmlException {

        if ( !nodeRoot.getNodeName().equals(rootName) ) {
            throw new XmlException("The node no is a curve ");
        }
        ParametricCurve curve = new ParametricCurve();
        NamedNodeMap atts = nodeRoot.getAttributes();
        if (atts != null) {
            Node atributo;
            atributo = atts.getNamedItem(curveAttributesNames[0]);
            if(atributo!=null){
                String approximationSteps = atributo.getNodeValue();
                if (!"".equals(approximationSteps)) {
                    curve.setApproximationSteps(
                        Integer.parseInt(approximationSteps));
                }
            }
        }

        NodeList nodeList = nodeRoot.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {

            Node nodo;
            nodo = nodeList.item(i);

            NodeList subNodos = nodo.getChildNodes();
            Vector3D v3[] = new Vector3D[3];
            int pos = 0;
            for (int j = 0; j < subNodos.getLength(); j++) {
                Node subNodo = subNodos.item(j);
                if (subNodo.getLocalName() != null &&
                    subNodo.getLocalName().equals(nodesNames[1])) {
                    Node subNodo2 = subNodo.getFirstChild();

                    String coors[] = subNodo2.getNodeValue().trim().split(" ");
                    v3[pos] = new Vector3D(Double.parseDouble(coors[0]),
                                           Double.parseDouble(coors[1]),
                                           Double.parseDouble(coors[2]));
                    pos++;
                }
            }

            atts = nodo.getAttributes();
            if (atts != null) {
                Node atributo = atts.getNamedItem(pointAttributesNames[0]);
                String type = atributo.getNodeValue();
                if (!"".equals(type)) {
                    curve.addPoint(v3, Integer.parseInt(type));
                }
            }
        }

        return curve;
    }



    public static Element toElement(ParametricCurve curve,
                                    Document document) throws XmlException{
        Element nodeCurve = document.createElement(rootName);
        try {

            nodeCurve.setAttribute(curveAttributesNames[0],
                                   "" + curve.getApproximationSteps());
            for (int i = 0; i < curve.getPointSize(); i++) {
                Element ePoint = document.createElement(nodesNames[0]);
                ePoint.setAttribute(pointAttributesNames[0], curve.types.get(i) + "");
                Vector3D v3[] = curve.getPoint(i);
                for (int j = 0; j < v3.length; j++) {
                    Element eVector = document.createElement(nodesNames[1]);
                    Vector3D v = v3[j];
                    eVector.setTextContent(v.x + " " + v.y + " " + v.z);
                    ePoint.appendChild(eVector);
                }
                nodeCurve.appendChild(ePoint);
            }
        }

        catch (TransformerFactoryConfigurationError ex1) {
            VSDK.reportMessage(null, VSDK.FATAL_ERROR, "toElement", "" + ex1);
        }

        return nodeCurve;
    }
}
//===========================================================================
//= EOF                                                                     =
//===========================================================================
