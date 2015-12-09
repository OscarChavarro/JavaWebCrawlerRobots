package vsdk.toolkit.processing;

import vsdk.toolkit.common.VSDK;

/**
 * Circular double linked list that exposes its nodes.
 *
 * @param <E> data to hold.
 */
public class _CircDoubleLinkedList<E> { //Circular.

    private _DoubleLinkedListNode<E> head;
    //private DoubleLinkedListNode<E> last;
    private int currentSize;

    public _CircDoubleLinkedList() {
        head = null;
//        last = null;
        currentSize = 0;
    }

    /**
     * Return linked list size
     *
     * @return current size
     */
    public int size() {
        return currentSize;
    }

    /**
     * Add a new node in the linked list, if doesn't have head add like head
     * else add at the end
     *
     * @param data
     */
    public void add(E data) {
        _DoubleLinkedListNode<E> newNode = new _DoubleLinkedListNode<E>();

        newNode.data = data;
        if ( head == null ) {
            newNode.previous = newNode;
            newNode.next = newNode;
            newNode.isHead = true;
            head = newNode;
            currentSize = 1;
        } else {
            newNode.previous = head.previous;
            newNode.next = head;
            newNode.isHead = false;
            head.previous.next = newNode;
            head.previous = newNode;
            ++currentSize;
        }
    }

    /**
     * Add a new node in the linked list. If the index is equals or greater to
     * the size, the node is appended to the end.
     *
     * @param ind index.
     * @param data data in the new node to append.
     */
    public void add(int ind, E data) {
        int i;
        _DoubleLinkedListNode<E> iterator;
        _DoubleLinkedListNode<E> newNode;

        if ( head == null || ind >= currentSize ) {
            add(data);
            return;
        }
        newNode = new _DoubleLinkedListNode<E>();
        newNode.data = data;
        if ( ind == 0 ) {
            head.previous.next = newNode;
            newNode.previous = head.previous;
            newNode.next = head;
            newNode.isHead = true;
            head.previous = newNode;
            head.isHead = false;
            head = newNode;
            ++currentSize;
            return;
        }
        iterator = head;
        for ( i = 0; i < ind; ++i ) {
            iterator = iterator.next;
        }
        iterator.previous.next = newNode;
        newNode.previous = iterator.previous;
        newNode.next = iterator;
        newNode.isHead = false;
        iterator.previous = newNode;
        ++currentSize;
    }

    /**
     * Remove a node in the linked list, at the position indicated in 'ind'.
     *
     * @param ind
     */
    public void remove(int ind) {
        int i;
        _DoubleLinkedListNode<E> iterator;

        if ( ind >= currentSize ) {
            String msg;
            msg = "Circ double linked list error: index out of bounds for remove operation.";
            VSDK.reportMessage(this, VSDK.FATAL_ERROR, "remove", msg);
            return;
        }
        if ( currentSize == 1 ) {
            head = null;
            currentSize = 0;
            return;
        }
        if ( ind == 0 ) {
            head.previous.next = head.next;
            head.next.previous = head.previous;
            head = head.next;
            head.isHead = true;
        } else {
            iterator = head;
            for ( i = 0; i < ind; ++i ) {
                iterator = iterator.next;
            }
            iterator.previous.next = iterator.next;
            iterator.next.previous = iterator.previous;
        }
        --currentSize;
    }

    /**
     * Insert a new node in the linked list before a given node.
     *
     * @param data Data in the new node to append.
     * @param node After insertion, the new node will be before this node.
     * @return The linked list node created.
     */
    public _DoubleLinkedListNode<E> insertBefore(E data, _DoubleLinkedListNode<E> node) {
        _DoubleLinkedListNode<E> newNode;

        if ( (node.isHead && node != head) || head == null ) {
            String msg;
            msg = "Circ double linked list error: the node not belongs to this"
                + "linked list, insert before this node will corrupt both linked lists.";
            VSDK.reportMessage(this, VSDK.FATAL_ERROR, "insertBefore", msg);
            return null;
        }
        newNode = new _DoubleLinkedListNode<E>();
        newNode.data = data;
        if ( node == head ) {
            head.previous.next = newNode;
            newNode.isHead = true;
            newNode.previous = head.previous;
            newNode.next = head;
            head.isHead = false;
            head.previous = newNode;
            head = newNode;
        } else {
            node.previous.next = newNode;
            newNode.previous = node.previous;
            newNode.next = node;
            node.previous = newNode;
        }
        ++currentSize;
        return newNode;
    }

    /**
     * Return the head of the linked list
     *
     * @return head
     */
    public _DoubleLinkedListNode<E> getHead() {
        return head;
    }

//    public DoubleLinkedListNode<E> getLast() {
//        return last;
//    }
}
