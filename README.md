# WumpleUtil

Minecraft mod: Shared library of useful classes and functions for mod development.  Does nothing on its own.

Reduces boilerplate and duplicate code.  Used by Stormwind's mods.

Features include:

* MatchingConfig classes to help make a mod highly configurable in relation to things in game
* A BaseChest class based on vanilla Minecraft's chest, from which to derive new chest types
* An adapter set, IThing, for basic use of ItemStack, Entity, and TileEntity instances via a single interface
* Map manipulation library
* Client/Server proxy bases classes derived from Choonster's TestMod3
* Container capability listeners derived from Choonster's TestMod3
* BlockRepair library derived from Corosus' CoroUtil
* Many more utility classes
