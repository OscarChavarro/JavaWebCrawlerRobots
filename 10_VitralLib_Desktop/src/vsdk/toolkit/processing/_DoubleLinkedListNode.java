package vsdk.toolkit.processing;

/**
 * Node of the linked list (CircDoubleLinkedListNode), this is the structure
 * node.
 *
 * @author Demami
 * @param <E>
 */
public class _DoubleLinkedListNode<E> {

    public E data;
    public _DoubleLinkedListNode<E> next;
    public _DoubleLinkedListNode<E> previous;
    public boolean isHead; //To help to avoid corrupted linked lists. See insertBefore in DoubleLinkedList class.
}
