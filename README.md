# Minecraft Manhunt
________
Yes, this is inspired by Dream's Minecraft Manhunt videos.
I hacked this together in a couple hours, so I could play this with my friends.
But, none of us are very good at the game.
Therefore, this plugin features lots of (soon-to-be) configurable handicaps.
A full list can be found at [Configuration & Features](#configuration--features)
## Installation
Clone and build the maven package by running 
```
mvn compile
mvn package
```
This will leave build the jar file in `./target`. 
Copy or move `MinecraftManhunt-X.X.jar` into the `/plugins` folder of your server. 
Make sure to run your server with java preview features on! (I used string templates for fun!!)
**This will only work on Paper and any of its forks**
## Some Fully Configurable Features...
* Prevent (some) Boring Deaths!!
  * Burning to death because of blazes is not going to happen!!!
  * Missing an MLG water bucket will leave you at half a heart
* Gives speed for hunters who are very far away
  * The game becomes more than a running sim when you die!
* Reset with a single command
  * `/mm reset` will teleport all players ~70k blocks away and reset achievements, set a new world spawn, and 