package com.nvew.clockread2;

class AVLTreeNode
{
  EndPoint value;
  AVLTreeNode left;
  AVLTreeNode right;
  int height;
  
  AVLTreeNode (EndPoint val)
  {
    value = val;
    height = 1;
  }
}

public class SelfBalancingTree
{
  public static int getBalance (AVLTreeNode tree)
  {
    if (tree != null)
    {
      // these checks are finding the height of tree with null checks
      int x = 0, y = 0;
      if (tree.left != null)
        x = tree.left.height;
      if (tree.right != null)
        y = tree.right.height;
      return (x - y);
    }
    return 0;
  }
  
  public static AVLTreeNode rightRotate (AVLTreeNode x)
  {
    AVLTreeNode y = x.left;
    AVLTreeNode tmp = y.right;
    
    // right-rotation being done here
    y.right = x;
    x.left = tmp;
    
    // update the height of x node of AVL Tree
    int a = 0, b = 0;
    if (x.left != null)
      a = x.left.height;
    if (x.right != null)
      b = x.right.height;
    x.height = (a > b ? a : b) + 1;
    
    // update the height of y node of AVL Tree
    a = 0; b = 0;
    if (y.left != null)
      a = y.left.height;
    if (y.right != null)
      b = y.right.height;
    y.height = (a > b ? a : b) + 1;
    
    // y is the head of new AVL tree to be returned
    return y;
  }
  
  public static AVLTreeNode leftRotate (AVLTreeNode x)
  {
    AVLTreeNode y = x.right;
    AVLTreeNode tmp = y.left;
    
    // left-rotation being done here
    y.left = x;
    x.right = tmp;
    
    // update the height of x node of AVL Tree
    int a = 0, b = 0;
    if (x.left != null)
      a = x.left.height;
    if (x.right != null)
      b = x.right.height;
    x.height = (a > b ? a : b) + 1;
    
    // update the height of y node of AVL Tree
    a = 0; b = 0;
    if (y.left != null)
      a = y.left.height;
    if (y.right != null)
      b = y.right.height;
    y.height = (a > b ? a : b) + 1;
    
    // y is the head of new AVL tree to be returned
    return y;
  }
  
  public static AVLTreeNode insertIntoTree (AVLTreeNode tree, EndPoint val)
  {
    // this is the normal BST insert procedure
    if (tree == null)
    {
      tree = new AVLTreeNode (val);
      return tree;
    }
    if (val.self.x < tree.value.self.x)
    {
      tree.left = insertIntoTree (tree.left, val);
    }
    else
    {
      tree.right = insertIntoTree (tree.right, val);
    }
    
    // find out the height of the node formed
    int a = 0, b = 0;
    if (tree.left != null)
      a = tree.left.height;
    if (tree.right != null)
      b = tree.right.height;
    tree.height = (a > b ? a : b) + 1;
    
    int balance = getBalance (tree);
    System.out.println (val + "   " + balance);
    
    // Left Left Case
    if (balance > 1 && val.self.x < tree.left.value.self.x)
    {
      System.out.println ("left-left case : going for right : " + val + "   " + tree.right.value);
      return rightRotate(tree);
    }

    // Right Right Case
    if (balance < -1 && val.self.x > tree.right.value.self.x)
    {
      System.out.println ("right-right case : going for left : " + val + "   " + tree.right.value);
      return leftRotate(tree);
    }

    // Left Right Case
    if (balance > 1 && val.self.x > tree.left.value.self.x)
    {
      System.out.println ("left-right case : going for left and then right : " + val + "   " + tree.right.value);
      tree.left =  leftRotate(tree.left);
      return rightRotate(tree);
    }

    // Right Left Case
    if (balance < -1 && val.self.x < tree.right.value.self.x)
    {
      System.out.println ("right-left case : going for right and then left : " + val + "   " + tree.right.value);
      tree.right = rightRotate(tree.right);
      return leftRotate(tree);
    }

    return tree;
  }
  
  public static void preorderTraversal (AVLTreeNode tree)
  {
    if (tree != null)
    {
      System.out.print (tree.value + "   ");
      preorderTraversal (tree.left);
      preorderTraversal (tree.right);
    }
  }
  
  public static void inorderTraversal (AVLTreeNode tree)
  {
    if (tree != null)
    {
      inorderTraversal (tree.left);
      System.out.print (tree.value + "   ");
      inorderTraversal (tree.right);
    }
  }
  
  public static void postorderTraversal (AVLTreeNode tree)
  {
    if (tree != null)
    {
      postorderTraversal (tree.left);
      postorderTraversal (tree.right);
      System.out.print (tree.value + "   ");
    }
  }
  
}
