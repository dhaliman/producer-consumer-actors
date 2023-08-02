# Producer-Consumer using Actors

## Producer-Consumer problem
There is a Consumer process and a Producer process with a shared buffer of finite size. The Producer appends elements to 
the buffer while the Consumer reads from the buffer. If the buffer is empty, the Consumer blocks till there are elements 
in the buffer. If the buffer is full, the Producer blocks till there is space for new elements in the buffer.

There are solutions to the Producer-Consumer problem using Semaphores and Monitors.

## The Actor model

Every actor has
1. An identity (it's address)
2. Behaviour 
3. Only interacts with asynchronous message passing.

You can not interact directly with an Actors' state like in shared memory model or call methods on it like in an object 
in OOP.

Because Actors only interact using messages no deadlock causing blocking synchronisation is necessary like in locks, 
Semaphores and Monitors.

## Actor model of Computation
Upon reception of a message the actor can do any combination of the following:
1. Send messages (to addresses of other actors)
2. Create actors (hierarchy, child actors)
3. Designate the behaviour for the next message (Actors are stateful)

## Actor-Internal Evaluation Order
An actor is effectively single-threaded:
1. Messages are received sequentially (Every actor has a queue).
2. Behaviour change is effective before processing the next message.
3. Processing one message is the atomic unit of execution.

This has the benefits of synchronised methods, but blocking is replaced by enqueueing a message.

## Message ordering
If an actor sends multiple messages to the same destination, they will not arrive out of order.

No order guarantees between messages from different actors.

## Solution to Producer-Consumer using Actors

A Solution using Semaphores uses three Semaphores:
1. One for preventing Producer to add to a full buffer.
2. One for preventing Consumer to remove from an empty buffer. 
3. One for synchronising access to the shared buffer.

We define one actor for Producer, one for Consumer and one for the shared Buffer. If the Producer Actor wants to add to the Buffer,
it sends a message to the Buffer Actor with the element to be added. The Buffer Actor sends an Acknowledgement message back 
to the Producer Actor prompting the Producer to add another element to the Buffer.

Similarly, the Consumer Actor sends a message to the Buffer Actor when it wants to consume from the Buffer. The Buffer Actor
sends an Acknowledgement message along with the element prompting the Consumer to read from the Buffer again.

Because messages are processed sequentially by an Actor, messages from Producer Actor and Consumer Actor can not access the 
Buffer at the same time and access to the Buffer is synchronised. If the Buffer is full, the Buffer Actor does not send 
and Acknowledgement to the Producer Actor and without an Acknowledgement for the previous message, it does not send a new 
element to the buffer Actor. Similarly, the Buffer Actor does not send an Acknowledgement message back to the Consumer if 
there are no elements in the Buffer. In both the cases, the Actors are not waiting or blocked like in synchronised methods 
but are simply suspended.
