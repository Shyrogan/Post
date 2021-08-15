[license-badge]: https://img.shields.io/github/license/Shyrogan/Post.svg

# Post [![License][license-badge]](/LICENSE)

🚀 A powerful and configurable Event Bus, nothing more.

 ## How does it work ?

Using modern technologies such as [ASM](https://asm.ow2.io/),
the Post Event Bus manages to reduce the cost of message dispatching.  
It also adapts to certain specific situations (Such as publishing the message to one/none listeners).

 ## Installation

Thanks to GitHub, you can simply add the repository:
````groovy
repositories {
    maven {
        url 'https://maven.pkg.github.com/Shyrogan/Post'
    }
}
````
followed by the dependency:
````groovy
dependencies {
    compile group: 'fr.shyrogan', name: 'post', version: '1.1.2'
}
````

 ## Getting started

Once the library is installed, you simply need to create the EventBus instance:
````java
final EventBus bus = new EventBus(/** Your configuration (if you have one) goes here**/);
````
Create your class containing your listeners:
````java
public class MyReceivers {
    @Subscribe
    public void onStringMessage(String message) {
        // In this example, the onStringMessage method will be called everytime
        // a string is published in to the EventBus.
    }
}
````
and register it:
````java
bus.subscribe(new MyReceivers());
````
finally, dispatch your messages:
````java
bus.dispatch("My message!");
````