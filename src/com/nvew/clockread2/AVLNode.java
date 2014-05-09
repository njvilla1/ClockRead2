package com.nvew.clockread2;

/**
 * @author: Michael Levet
 * @date: 6/20/2011
 *
 * This class represents a Node in an AVLTree, which is a
 * balanced binary search tree. As such, all the AVLNodes must be
 * mutually Comparable to properly order the elements. Each AVLNode
 * also maintains the balance factor for its subtrees. A balance factor
 * less than 0 indicates more elements to the left of this AVLNode, and a
 * balance factor greater than 0 indicates more elements to the right of
 * this AVLNode.
 * */
public class AVLNode<E extends Comparable<? super E>> {

   private AVLNode<E> left, right;
   private E element;

   //the default constructor used to initialize
   //this AVLNode with a null element
   public AVLNode(){
        this(null);
    }

   /**
    * @param element: The element this AVLNode is encapsulating
    *
    * The left and right children are initialized as empty.
    ***/
   public AVLNode(E element){
        this.element = element;
        left = right = null;
   }

   public E getElement(){ return element; }
   public void setElement(E element){ this.element = element; }
   
   public AVLNode<E> getLeft(){ return left; }
   public AVLNode<E> getRight(){ return right; }

   //the setLeft() and setRight() functions
   //simply set the elements for the left and right
   //AVLNodes. the actual ordering is handled by the
   //AVLTree class
   public void setLeft(E element){
       if(left == null)
          this.left = new AVLNode<E>(element);
       else
          this.left.setElement(element);
   }

   public void setRight(E element){
       if(right == null)
          this.right = new AVLNode<E>(element);
       else
          this.right.setElement(element);
   }

   public void setLeftNode(AVLNode<E> temp){
       this.left = temp;
   }

   public void setRightNode(AVLNode<E> temp){
        this.right = temp;
   }

   public int getBalance(){
        int leftHeight = (left == null)?0:left.height();
        int rightHeight = (right == null)?0:right.height();

        return rightHeight - leftHeight;
   }
   private int height(){
        int leftHeight = (left == null)?0:left.height();
        int rightHeight = (right == null)?0:right.height();

        return 1 + Math.max(leftHeight, rightHeight);

   }

   public String toString(){
        return assemble(this, 0);
   }

   private String assemble(AVLNode<E> temp, int offset){
       String ret= "";
       for(int i = 0; i < offset; i++)
           ret += "\t";

       ret += temp.getElement() + "\n";

       if(temp.getLeft() != null){
            ret += "Left: " + assemble(temp.getLeft(), offset + 1);
       }
       if(temp.getRight() != null){
            ret += "Right: " + assemble(temp.getRight(), offset + 1);
       }
       return ret;

   }
}
