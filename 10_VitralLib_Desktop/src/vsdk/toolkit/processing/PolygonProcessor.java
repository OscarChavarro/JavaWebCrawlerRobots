package vsdk.toolkit.processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;
import vsdk.toolkit.common.Vertex2D;
import vsdk.toolkit.common.dataStructures.BinaryTreeNode;
import vsdk.toolkit.environment.geometry.Polygon2D;
import vsdk.toolkit.environment.geometry._Polygon2DContour;

//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - July 14 2014 - Leonardo Rebolledo: Original base version              =
//=-------------------------------------------------------------------------=
//= References:                                                             =
//= [RAMER1972] Urs Ramer, "An iterative procedure for the polygonal        =
//= approximation of plane curves", Computer Graphics and Image Processing, =
//= 1(3), 244–256 (1972).                                                   =
//= [DOPEU1973] David Douglas & Thomas Peucker, "Algorithms for the         =
//= reduction of the number of points required to represent a digitized     =
//= line or its caricature", The Canadian Cartographer 10(2), 112–122 (1973)=
//===========================================================================



/**
 * This class implements the Ramer–Douglas–Peucker algorithm in a non recursive
 * way, to simplify polygons.
 */
public class PolygonProcessor extends ProcessingElement {

    private static final double EPSILON = 1E-18;

    /**
     * Given a Polygon2D object, return a simplified version of that set of polygons
     * using the Ramer–Douglas–Peucker algorithm.
     * @param pol2DIn array of polygons.
     * @param epsilon Umbral distance, used to leave or discard points.
     * @param copy the points of the contours are copied or referenced?
     * @return array of simplified polygons.
     */
    public static Polygon2D polygon2DSimplify(
        Polygon2D pol2DIn,
        double epsilon,
        boolean copy ) {
        Polygon2D pol2DSimp = new Polygon2D();
        int i;

        pol2DSimp.loops.clear();
//        for(_Polygon2DContour p2DContour : pol2DIn.loops) {
        for(i=0; i < pol2DIn.loops.size(); ++i) {
            _Polygon2DContour p2DContour = pol2DIn.loops.get(i);
            pol2DSimp.loops.add(polygon2DContourSimplify(p2DContour,epsilon,true));
        }
        return pol2DSimp;
    }

    /**
     * Given a polygon2DContour object, return a simplified version of that polygon
     * using the Ramer–Douglas–Peucker algorithm in non recursive fashion.
     * @param p2DContour   Single polygon.
     * @param epsilon Umbral distance, used to leave or discard points.
     * @param copy the points of the contour are copied or referenced?
     */
    private static _Polygon2DContour  polygon2DContourSimplify(
        _Polygon2DContour p2DContour,
        double epsilon,
        boolean copy)
    {
        _Polygon2DContour p2DContourSimp = new _Polygon2DContour();
        Vertex2D point;
        /** In missingNodes we put two vertex at a time */
        Stack<Integer> indsStkMissingNodes = new Stack<Integer>();
        int ind0,ind1,indFar;
        //float epsilon = 0.01f;///
        float[] dist = new float[1];
        //LinkedList<Vertex2D> P2DContourSimp = new LinkedList();
        /** The default initialization in java of indContourSimp is used
          * (filled with zeros)*/
        int[] ContourSimpFlags;
        int numVertex,i;


        numVertex = p2DContour.vertices.size();
        if(numVertex < 3) { //One line or one point.
            if(copy)
                for(i=0;i<numVertex;++i) {
                        point = p2DContour.vertices.get(i);
                        point = new Vertex2D(point.x, point.y, point.color.r, point.color.g, point.color.b);
                        p2DContourSimp.vertices.add(point);
                }
            else
                for(i=0;i<numVertex;++i) {
                        point = p2DContour.vertices.get(i);
                        p2DContourSimp.vertices.add(point);
                }
            return p2DContourSimp;
        }
        ContourSimpFlags = new int[numVertex];
        indsStkMissingNodes.push(numVertex-1); //The last one.
        indsStkMissingNodes.push(0); //The fist one.
        while(!indsStkMissingNodes.isEmpty()) {
            ind0 = indsStkMissingNodes.pop();
            ind1 = indsStkMissingNodes.pop();
            ContourSimpFlags[ind0] = 1;
            ContourSimpFlags[ind1] = 1;
            if((ind1-ind0)>1) {
                indFar = getFarthestNodeToLine(p2DContour,ind0,ind1,dist);
                if(dist[0]>epsilon) {
                    indsStkMissingNodes.push(indFar);
                    indsStkMissingNodes.push(ind0);
                    indsStkMissingNodes.push(ind1);
                    indsStkMissingNodes.push(indFar);
                }
            }
        }
        if(copy)
            for(i=0;i<numVertex;++i) {
                if(ContourSimpFlags[i]==1) {
                    point = p2DContour.vertices.get(i);
                    point = new Vertex2D(point.x, point.y, point.color.r, point.color.g, point.color.b);
                    p2DContourSimp.vertices.add(point);
                }
            }
        else
            for(i=0;i<numVertex;++i) {
                if(ContourSimpFlags[i]==1) {
                    point = p2DContour.vertices.get(i);
                    p2DContourSimp.vertices.add(point);
                }
            }
        return p2DContourSimp;
    }
    /**
     * Return the index of the farthest point to the line formed by the point at index ind0
     * and the point at index ind1 in the p2DContour; the distance is also returned in outDist[0].
     * @param p2DContour   Polygon.
     * @param ind0 Index 0 of the line.
     * @param ind1 Index 1 of the line.
     * @param outDist Array of one element: distance to the farthest point.
     */
    private static int getFarthestNodeToLine(_Polygon2DContour p2DContour, int ind0, int ind1, float[] outDist) {
        int i;
        float xInd0,yInd0,nx,ny,m,temp;
        float[] v = new float[2];
        float dist, maxDist;
        int indFar;

        //Find the unitary normal vector to the line.
        xInd0 = (float)p2DContour.vertices.get(ind0).x;
        yInd0 = (float)p2DContour.vertices.get(ind0).y;
        temp = (float)(p2DContour.vertices.get(ind1).y - yInd0);
        if(temp<0.00001 && temp>-0.00001) {
            nx=0;
            ny=1;
        }
        else {
            //m = Slope of the normal to the line.
            m =  -(float)((p2DContour.vertices.get(ind1).x - xInd0)
                         /temp);
            temp=(float)Math.sqrt(1+m*m);
            nx=1/temp;
            ny=m/temp;
        }
        //Find the farthest point and its distance.
        maxDist=0;
        indFar=ind0+1;
        for(i=ind0+1; i<ind1; ++i) {
            v[0] = (float)(p2DContour.vertices.get(i).x - xInd0);
            v[1] = (float)(p2DContour.vertices.get(i).y - yInd0);
            //Dot product.
            dist = Math.abs(v[0]*nx + v[1]*ny);
            if(dist>maxDist) {
                maxDist=dist;
                indFar=i;
            }
        }
        outDist[0]=maxDist;
        return indFar;
    }


    public static void classifyContourHoles(Polygon2D polygon)
    {
        int i,j;
        //Node of the Great Hole that contains everything.
        ArrayList<_Polygon2DContour> tempList;
        _Polygon2DContour p2DContour,p2DContourTest;

        // Actualize minMaxArea in all contours of the polygon, and clear the flag.
        for(i=0; i<polygon.loops.size(); ++i) {
            p2DContour = polygon.loops.get(i);
            p2DContour.calcMinMaxArea(true);
            // Indicates that the contour is in the binary tree.
            p2DContour.fleetingFlag = false;
        }
        // Head node contains no data.
        polygon.setHeadNode(new BinaryTreeNode<_Polygon2DContour>(null));
        tempList = new ArrayList<_Polygon2DContour>();
        // Fill tempList and create the binary tree.
        for(i=0; i<polygon.loops.size(); ++i) {
            p2DContour = polygon.loops.get(i);
            tempList.clear();
            tempList.add(p2DContour);
            for(j=i+1; j<polygon.loops.size(); ++j) {
                p2DContourTest = polygon.loops.get(j);
                if(!p2DContourTest.fleetingFlag) {
                     // If p2DContourTest is not in the binary tree:
                    if(contourInsidePolygon(p2DContour.vertices
                            ,p2DContourTest.vertices)) {
                        tempList.add(p2DContourTest);
                    }
                }
            }
            // Sorts in descending order.
            Collections.sort(tempList, Collections.reverseOrder());
            insertListInBinaryTree(tempList,polygon.getHeadNode());
        }
    }

    private static void insertListInBinaryTree(
             ArrayList<_Polygon2DContour> list
            ,BinaryTreeNode<_Polygon2DContour> headNode) // Head node is in the 0 level.
    {
        _Polygon2DContour p2DContour;
        BinaryTreeNode<_Polygon2DContour> containingContourNode;
        int levelOfContainingContourNode[] = new int[1];

        if(list.isEmpty())
            return;
        p2DContour = list.get(0);
        containingContourNode = findContainingContourNode(
                 p2DContour
                ,headNode
                ,levelOfContainingContourNode);
        insertListInBinaryTreeNode(list,containingContourNode,levelOfContainingContourNode[0]);
    }

    private static void insertListInBinaryTreeNode(
             ArrayList<_Polygon2DContour> list
            ,BinaryTreeNode<_Polygon2DContour> containingListNode
            ,int levelOfContainingContourNode)
    {
        int i;
        BinaryTreeNode<_Polygon2DContour> child,lastChild=null;
        _Polygon2DContour p2DContour;
        int levelOfNode;

        levelOfNode = levelOfContainingContourNode + 1;
        if((levelOfNode%2) == 0)
            list.get(0).setExteriorContour(containingListNode.getData());
        // Find the last child of the node and add a new one.
        child = containingListNode.getChild();
        if(child == null) {
            lastChild = new BinaryTreeNode<_Polygon2DContour>(list.get(0));
            containingListNode.setChild(lastChild);
        } else {
            while(child != null) {
                lastChild = child;
                child = child.getSibling();
            }
            child = new BinaryTreeNode<_Polygon2DContour>(list.get(0));
            if ( lastChild != null ) {
                lastChild.setSibling(child);
            }
            lastChild = child;
        }
        // Indicates that the contour is in the binary tree.
        list.get(0).fleetingFlag = true;
        // Fill the binary tree with the rest of elements of list.
        for(i=1; i<list.size(); ++i) {
            ++levelOfNode;
            p2DContour = list.get(i);
            if((levelOfNode%2) == 0) { // Is a hole.
                p2DContour.setExteriorContour(lastChild.getData());
            }
            child = new BinaryTreeNode<_Polygon2DContour>(p2DContour);
            lastChild.setChild(child);
            child.getData().fleetingFlag = true;
            lastChild = child;
        }
    }

    /**
     * Finds the contour node that contains the p2DContour in the binary tree
     * represented by headNode.
     * @param p2DContour
     * @param headNode
     * @param levelOfContainingContourNode is zero based. This array contains one element.
     * @return
     */
    private static BinaryTreeNode<_Polygon2DContour> findContainingContourNode(
             _Polygon2DContour p2DContour
            ,BinaryTreeNode<_Polygon2DContour> headNode
            ,int levelOfContainingContourNode[])
    {
        boolean isTheContainingContourNode;
        BinaryTreeNode<_Polygon2DContour> child;

        levelOfContainingContourNode[0] = 0;
        while(headNode != null) {
            child = headNode.getChild();
            isTheContainingContourNode = true;
            while(child != null) {
                if(contourInsidePolygon(p2DContour.vertices,child.getData().vertices)) {
                    isTheContainingContourNode = false;
                    //findContainingContourNode(p2DContour,child);
                    headNode = child;
                    ++levelOfContainingContourNode[0];
                    break;
                }
                child = child.getSibling();
            }
            if(isTheContainingContourNode)
                return headNode;
        }
        // Reaches here only if headNode == null.
        return headNode;
    }

    private static boolean binaryTreeContainContour(
             BinaryTreeNode<_Polygon2DContour> headNode
            ,_Polygon2DContour p2DContour)
    {
        if(headNode == null)
            return false;
        if(headNode.getData() == p2DContour)
            return true;
        if(binaryTreeContainContour(headNode.getSibling(),p2DContour))
            return true;
        return binaryTreeContainContour(headNode.getChild(),p2DContour);
    }

    /**
     * Test if the contour is inside the polygon. This function assumes that
     * the contour is fully inside or fully outside.
     * @param contour
     * @param mainPolygon
     * @return
     */
    public static boolean contourInsidePolygon(
             ArrayList<Vertex2D> contour
            ,ArrayList<Vertex2D> mainPolygon)
    {
        int i;
        Vertex2D point;
        byte result;

        for (i = 0; i < contour.size(); ++i) {
            point = contour.get(i);

            result = isPointInsidePolygon2D(point, mainPolygon);
            if(result == 1)
                return true;
            if(result == -1)
                return false;
            // If result equals 0 the point is in the contour of the mainPolygon,
            // the search continues.
        }
        // If every point is in the contour of the mainPolygon. Note that in this
        // case the contour can be anywhere(inside, outside, intersecting).
        return false;
    }


    /* Function isPointInPolygon2D is based on the function developed by W. Randolph
     Franklin:
     http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
     visited on sep 08 2014.
     The part that tests if the point is in de boundary is new.

     License to Use

     Copyright (c) 1970-2003, Wm. Randolph Franklin

     Permission is hereby granted, free of charge, to any person obtaining a
     copy of this software and associated documentation files (the "Software"),
     to deal in the Software without restriction, including without limitation
     the rights to use, copy, modify, merge, publish, distribute, sublicense,
     and/or sell copies of the Software, and to permit persons to whom the
     Software is furnished to do so, subject to the following conditions:

     Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimers.
     Redistributions in binary form must reproduce the above copyright notice
     in the documentation and/or other materials provided with the distribution.
     The name of W. Randolph Franklin may not be used to endorse or promote
     products derived from this Software without specific prior written permission.
     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
     OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
     WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
     IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
     */
    /**
     * Test if point is inside, outside or on the boundary of the polygon.
     *
     * @param point
     * @param polygon
     * @return 1 if the point is inside, -1 if the point is outside, 0 if the
     * point is on the boundary.
     */
    public static byte isPointInsidePolygon2D(Vertex2D point, ArrayList<Vertex2D> polygon)
    {
        int i,polSize;
        boolean isInside = false;
        double temp;
        Vertex2D polPoint,polPointNext;

        polSize = polygon.size();
        if(polSize<3) {
            return -1;
        }
        for(i=0; i<polSize; ++i) {
//            if(point.y == polPoint.y && point.x<polPoint.x){
//                if(polPoint.y<polPointNext.y != polPoint.y<dllnNode.previous.data.y)
//                    isInside = !isInside;
//            } else
            polPoint = polygon.get(i);
            if(i == polSize-1)
                polPointNext = polygon.get(0);
            else
                polPointNext = polygon.get(i+1);
            temp = polPoint.y - polPointNext.y;
            if ( Math.abs(temp) < EPSILON ) { //Horizontal line.
                if ( Math.abs(point.y - (polPointNext.y + temp / 2)) < EPSILON ) {
                    if ( (polPointNext.x - point.x) * (point.x - polPoint.x) >= 0 ) {
                        return 0;
                    }
                }
            }
            if ( point.y < polPoint.y != point.y < polPointNext.y ) {
                temp = ((polPointNext.x - polPoint.x) * (point.y - polPoint.y)
                        / (polPointNext.y - polPoint.y) + polPoint.x);
                if ( Math.abs(point.x - temp) < EPSILON ) {
                    return 0;
                }
                if ( point.x < temp ) {
                    isInside = !isInside;
                }
            }
        }
        if ( isInside ) {
            return 1;
        } else {
            return -1;
        }
    }
}
