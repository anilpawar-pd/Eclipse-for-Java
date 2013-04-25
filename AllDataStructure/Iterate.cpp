#include "Iterate.h"
#include <stdlib.h>

//////////// Base class

// Return the item stored in the current position.
// Throw BadIterator exception if the current position is invalid.
template <class Object>
const Object & TreeIterator<Object>::retrieve( ) const
{
    if( !isValid( ) )
        throw BadIterator( "Illegal retrieve" );
    return current->element;
}


//////////// PREORDER

// Constructor.
template <class Object>
PreOrder<Object>::PreOrder( const BinaryTree<Object> & theTree )
  : TreeIterator<Object>( theTree )
{
    s.push( root );
}

// Set the current position to the first.
template <class Object>
void PreOrder<Object>::first( )
{
    s.makeEmpty( );
    if( root != NULL )
    {
        s.push( root );
        advance( );
    }
}

// Advance to the next position.
// Throw BadIterator exception if the iteration has been
// exhausted prior to the call.
template <class Object>
void PreOrder<Object>::advance( )
{
    if( s.isEmpty( ) )
    {
        if( current == NULL )
            throw BadIterator( "Advance past end" );
        current = NULL;
        return;
    }

    current = s.topAndPop( );
    if( current->right != NULL )
        s.push( current->right );
    if( current->left != NULL )
        s.push( current->left );
}

////////// POSTORDER

// Constructor.
template <class Object>
PostOrder<Object>::PostOrder( const BinaryTree<Object> & theTree )
  : TreeIterator<Object>( theTree )
{
    s.push( StNode<Object>( root ) );
}

// Set the current position to the first.
template <class Object>
void PostOrder<Object>::first( )
{
    s.makeEmpty( );
    if( root != NULL )
    {
        s.push( StNode<Object>( root ) );
        advance( );
    }
}

// Advance to the next position.
// Throw BadIterator exception if the iteration has been
// exhausted prior to the call.
template <class Object>
void PostOrder<Object>::advance( )
{
    if( s.isEmpty( ) )
    {
        if( current == NULL )
            throw BadIterator( "Advance past end" );
        current = NULL;
        return;
    }

    StNode <Object> cnode;

    for( ; ; )
    {
        cnode = s.topAndPop( );
    
        if( ++cnode.timesPopped == 3 )
        {
            current = cnode.node;
            return;
        }
    
        s.push( cnode );
        if( cnode.timesPopped == 1 )
        {
            if( cnode.node->left != NULL )
                s.push( StNode<Object>( cnode.node->left ) );
        }
        else  // cnode.timesPopped == 2
        {
            if( cnode.node->right != NULL )
                s.push( StNode<Object>( cnode.node->right ) );
        }
    }
}


////////// INORDER

// Advance to the next position.
// Throw BadIterator exception if the iteration has been
// exhausted prior to the call.
template <class Object>
void InOrder<Object>::advance( )
{

    if( s.isEmpty( ) )
    {
        if( current == NULL )
            throw BadIterator( "Advance past end" );
        current = NULL;
        return;
    }

    StNode<Object> cnode;

    for( ; ; )
    {
        cnode = s.topAndPop( );
    
        if( ++cnode.timesPopped == 2 )
        {
            current = cnode.node;
            if( cnode.node->right != NULL )
                s.push( StNode<Object>( cnode.node->right ) );
            return;
        }
    
          // First time through
        s.push( cnode );
        if( cnode.node->left != NULL )
            s.push( StNode<Object>( cnode.node->left ) );
    }
}


////////// LEVEL ORDER

// Constructor.
template <class Object>
LevelOrder<Object>::LevelOrder( const BinaryTree<Object> & theTree )
  : TreeIterator<Object>( theTree )
{
    q.enqueue( root );
}

// Set the current position to the first.
template <class Object>
void LevelOrder<Object>::first( )
{
    q.makeEmpty( );
    if( root != NULL )
    {
        q.enqueue( root );
        advance( );
    }
}

// Advance to the next position.
// Throw BadIterator exception if the iteration has been
// exhausted prior to the call.
template <class Object>
void LevelOrder<Object>::advance( )
{
    if( q.isEmpty( ) )
    {
        if( current == NULL )
            throw BadIterator( "Advance past end" );
        current = NULL;
        return;
    }

    current = q.getFront( );
    q.dequeue( );

    if( current->left != NULL )
        q.enqueue( current->left );
    if( current->right != NULL )
        q.enqueue( current->right );
}
