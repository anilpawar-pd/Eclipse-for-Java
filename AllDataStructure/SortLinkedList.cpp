#include "SortLinkedList.h"


// Insert item x into the list.
template <class Object>
void SortedLList<Object>::insert( const Object & x )
{
    LListItr<Object> prev = zeroth( );
    LListItr<Object> curr = first( );

    while( !curr.isPastEnd( ) && curr.retrieve( ) < x )
    {
        prev.advance( );
        curr.advance( );
    }

    LList<Object>::insert( x, prev );
}
