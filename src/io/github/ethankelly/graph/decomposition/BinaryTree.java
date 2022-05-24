package io.github.ethankelly.graph.decomposition;

import io.github.ethankelly.symbols.Greek;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class BinaryTree {
    /** Root of the binary tree */
    Node<Integer> root = null;

    public static void main(String[] args) {
        BinaryTree bt = new BinaryTree();

        bt.add(0);
        bt.add(2);
        bt.add(4);
        bt.add(6);
        bt.add(1);
        bt.add(3);
        bt.add(5);
        bt.add(4);

        System.out.println(bt);
    }

    @Override
    public String toString() {
        return printLevelOrderDFS();
    }

    public String printLevelOrderDFS(){
        StringBuilder sb = new StringBuilder();
        print(sb, 0, root);
        return String.valueOf(sb);
    }

    private void print(StringBuilder sb, int indent, Node<Integer> n) {
        if (!n.equals(root)) sb.append(n);
        else sb.append(Greek.LAMBDA.uni());

        if (n.getLeft() != null || n.getRight() != null) {
            if (n.getLeft() != null) {
                sb.append("\n").append("    ".repeat(indent)).append("└── ");
                print(sb, indent+1, n.getLeft());
            }
            if (n.getRight() != null) {
                sb.append("\n").append("    ".repeat(indent)).append("└── ");
                print(sb, indent+1, n.getRight());
            }
        }
    }

    /** Adds a node to the current tree using a recursive helper method, subject to the following rules:
     *   1) if the new node's value is lower than the current node's, we go to the left child;
     *   2) if the new node's value is greater than the current node's, we go to the right child; and
     *   3) when the current node is null, we've reached a leaf node, so we can insert the new node in that position.
     */
    public void add(int value) {
        root = addRecursive(root, value);
    }

    // Recursive helper for adding nodes into a binary search tree
    private Node<Integer> addRecursive(Node<Integer> current, int value) {
        // If provided node is null, return a new node with specified value
        if (current == null) {
            return new Node<>(value);
        }
        // If the specified value is less than the current value, go left. Else, go right.
        if (value < current.value) {
            current.left = addRecursive(current.left, value);
        } else if (value > current.value) {
            current.right = addRecursive(current.right, value);
        }
        // Return the current value - this gets assigned to be the root of the tree.
        return current;
    }

    /**
     * @return true if the tree is empty (i.e. the root is null) and false otherwise.
     */
    public boolean isEmpty() {
        return root == null;
    }

    public int getSize() {
        return getSizeRecursive(root);
    }

    private int getSizeRecursive(Node<Integer> current) {
        return current == null ? 0 : getSizeRecursive(current.left) + 1 + getSizeRecursive(current.right);
    }

    public boolean contains(int value) {
        return containsNodeRecursive(root, value);
    }

    private boolean containsNodeRecursive(Node<Integer> current, int value) {
        if (current == null) {
            return false;
        }

        if (value == current.value) {
            return true;
        }

        return value < current.value
                ? containsNodeRecursive(current.left, value)
                : containsNodeRecursive(current.right, value);
    }

    public void delete(int value) {
        root = deleteRecursive(root, value);
    }

    private Node<Integer> deleteRecursive(Node<Integer> current, int value) {
        if (current == null) {
            return null;
        }

        if (value == current.value) {
            // Case 1: no children
            if (current.left == null && current.right == null) {
                return null;
            }

            // Case 2: only 1 child
            if (current.right == null) {
                return current.left;
            }

            if (current.left == null) {
                return current.right;
            }

            // Case 3: 2 children
            int smallestValue = findSmallestValue(current.right);
            current.value = smallestValue;
            current.right = deleteRecursive(current.right, smallestValue);
            return current;
        }
        if (value < current.value) {
            current.left = deleteRecursive(current.left, value);
            return current;
        }

        current.right = deleteRecursive(current.right, value);
        return current;
    }

    private int findSmallestValue(Node<Integer> root) {
        return root.left == null ? root.value : findSmallestValue(root.left);
    }

    public void traverseInOrder(Node<Integer> node) {
        if (node != null) {
            traverseInOrder(node.left);
            visit(node.value);
            traverseInOrder(node.right);
        }
    }

    public void traversePreOrder(Node<Integer> node) {
        if (node != null) {
            visit(node.value);
            traversePreOrder(node.left);
            traversePreOrder(node.right);
        }
    }

    public void traversePostOrder(Node<Integer> node) {
        if (node != null) {
            traversePostOrder(node.left);
            traversePostOrder(node.right);
            visit(node.value);
        }
    }

    public void traverseLevelOrder() {
        if (root == null) {
            return;
        }

        Queue<Node<Integer>> nodes = new LinkedList<>();
        nodes.add(root);

        while (!nodes.isEmpty()) {

            Node<Integer> node = nodes.remove();

            System.out.print(" " + node.value);

            if (node.left != null) {
                nodes.add(node.left);
            }

            if (node.right != null) {
                nodes.add(node.right);
            }
        }
    }

    public void traverseInOrderWithoutRecursion() {
        Stack<Node<Integer>> stack = new Stack<>();
        Node<Integer> current = root;

        while (current != null || !stack.isEmpty()) {
            while (current != null) {
                stack.push(current);
                current = current.left;
            }

            Node<Integer> top = stack.pop();
            visit(top.value);
            current = top.right;
        }
    }

    public void traversePreOrderWithoutRecursion() {
        Stack<Node<Integer>> stack = new Stack<>();
        Node<Integer> current = root;
        stack.push(root);

        while (current != null && !stack.isEmpty()) {
            current = stack.pop();
            visit(current.value);

            if (current.right != null)
                stack.push(current.right);

            if (current.left != null)
                stack.push(current.left);
        }
    }

    public void traversePostOrderWithoutRecursion() {
        Stack<Node<Integer>> stack = new Stack<>();
        Node<Integer> prev = root;
        Node<Integer> current = root;
        stack.push(root);

        while (current != null && !stack.isEmpty()) {
            current = stack.peek();
            boolean hasChild = (current.left != null || current.right != null);
            boolean isPrevLastChild = (prev == current.right || (prev == current.left && current.right == null));

            if (!hasChild || isPrevLastChild) {
                current = stack.pop();
                visit(current.value);
                prev = current;
            } else {
                if (current.right != null) {
                    stack.push(current.right);
                }
                if (current.left != null) {
                    stack.push(current.left);
                }
            }
        }
    }

    private void visit(int value) {
        System.out.print(" " + value);
    }

    /**
     * Inner Node class contains integer values and maintains pointers to up to two child nodes.
     */
    static class Node<A> {
        /** Integer value of the current node */
        A value;

        /** Pointer to the left child */
        Node<A> left;
        /** Pointer to the right child */
        Node<A> right;

        Node(A value) {
            this.value = value;
            right = null;
            left = null;
        }

        public A getValue() {
            return this.value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }


        public Node<A> getLeft() {
            return left;
        }

        public Node<A> getRight() {
            return right;
        }
    }
}