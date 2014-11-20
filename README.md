![parchment_bad.png](https://bitbucket.org/repo/y8ARnx/images/2370432128-parchment_bad.png)

Parchment allows rapid scripting of mine craft based on a TCL like scripting language.

### Getting Started ###

* Throw the JAR file in your plugins folder.
* Create a Parchment folder in your plugins older.
* Create a spells folder inside the Parchment folder.
* Throw the below archer.tcl file in the spells folder
* Be an op, type /cast archer
* TCL scripts should be sourced as they are changed without reloading.
* Ignore all the crazy debuging messages.

### How do I get set up? ###

* Summary of set up
* Configuration
* Dependencies
* Database configuration
* How to run tests
* Deployment instructions

### Quick Examples ###

Archer


```
#!tcl

# archer.tcl
# Turn the caster into the archer class.
# Type `/cast archer` to activate

bind cast onCast

proc onCast {} {
	set who $caster	
	player $who clear 
	
	set bow [item new]
	item $bow type bow
	item $bow enchant "Punch 2"
	item $bow name "Power Bow"
	item $bow lore "This great bow\nWill Rock your socks."
	item $bow forceInv $who 0
	item $bow bind powerbow
	

	set plate [item new]
	item $plate type diamond_chestplate
	item $plate equip $who
	

	set helmet [item new] 
	item $helmet type diamond_helmet
	item $helmet equip $who
	
        item new type arrow more give $caster
}

```

Jump

```
#!tcl

# jump.tcl
# Hold an item in your hand and type /cast item bind jump
# Point toward where you want to go and left click.

bind cast x

proc x {} {
	entity $caster still
	entity $caster teleport 250
	entity $caster still
}

```

### Differences from TCL ###

* If is missing elseif.
* Index anything! (Arrays, Lists, Dicts)
* Values are more strongly typed.
* Not all language constructs are implemented yet. (String is noticeably missing).
* Some variables are "prototype" like behavior ($caster, $target, $world, $server).
* Written from scratch by me.
* More bugs.