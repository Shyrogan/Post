<h1 align="center">Post</h1>

<div align="center">
  <strong>🚀 A powerful Event Bus, nothing more.</strong>
</div>

 ## How does it work ?

Using modern technologies such as [ASM](https://asm.ow2.io/),
the Post Event Bus manages to reduce the cost of message dispatching.  
It also adapts to certain specific situations (Such as publishing the message to one/none receivers).

 ## Installation

We do not (yet) publish Post to Maven's Central Repository. It is available on [JitPack](https://jitpack.io).
````groovy
dependencies {
    compile group: 'com.github.Shyrogan', name: 'Post', version: '1.1.0'
}
````

Once you added the library to your project, you can simply create a new ``EventBus`` instance and use it!
If you don't know how to use the library, dive into the tests available in the ``src/test`` folder.
  
_This README is still really incomplete, I will try my best to write it later!_